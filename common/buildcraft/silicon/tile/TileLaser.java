/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import buildcraft.lib.net.MessageContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.mj.ILaserTargetBlock;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.block.ILocalBlockUpdateSubscriber;
import buildcraft.lib.block.LocalBlockUpdateNotifier;
import buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VolumeUtil;
import buildcraft.lib.misc.data.AverageLong;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.client.render.AdvDebuggerLaser;

public class TileLaser extends TileBC_Neptune implements ITickable, IDebuggable, ILocalBlockUpdateSubscriber {
    private static final int TARGETING_RANGE = 6;

    private final SafeTimeTracker clientLaserMoveInterval = new SafeTimeTracker(5, 10);
    private final SafeTimeTracker serverTargetMoveInterval = new SafeTimeTracker(10, 20);

    private final List<BlockPos> targetPositions = new ArrayList<>();
    private BlockPos targetPos;
    public Vec3 laserPos;
    private boolean worldHasUpdated = true;

    private final AverageLong avgPower = new AverageLong(100);
    private long averageClient;
    private final MjBattery battery;

    public TileLaser() {
        super();
        battery = new MjBattery(1024 * MjAPI.MJ);
        caps.addProvider(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    @Override
    public int getUpdateRange() {
        return TARGETING_RANGE;
    }

    @Override
    public BlockPos getSubscriberPos() {
        return getPos();
    }

    @Override
    public void setWorldUpdated(Level world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags) {
        this.worldHasUpdated = true;
    }

    private void findPossibleTargets() {
        targetPositions.clear();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() != BCSiliconBlocks.laser) {
            return;
        }
        Direction face = state.getValue(BuildCraftProperties.BLOCK_FACING_6);

        VolumeUtil.iterateCone(world, pos, face, TARGETING_RANGE, true, (w, s, p, visible) -> {
            if (!visible) {
                return;
            }
            BlockState stateAt = world.getBlockState(p);
            if (stateAt.getBlock() instanceof ILaserTargetBlock) {
                BlockEntity tileAt = world.getBlockEntity(p);
                if (tileAt instanceof ILaserTarget) {
                    targetPositions.add(p);

                }
            }
        });
    }

    private void randomlyChooseTargetPos() {
        List<BlockPos> targetsNeedingPower = new ArrayList<>();
        for(BlockPos position: targetPositions) {
            if (isPowerNeededAt(position)) {
                targetsNeedingPower.add(position);
            }
        }
        if (targetsNeedingPower.isEmpty()) {
            targetPos = null;
            return;
        }
        targetPos = targetsNeedingPower.get(world.rand.nextInt(targetsNeedingPower.size()));
    }

    private boolean isPowerNeededAt(BlockPos position) {
        if (position != null) {
            BlockEntity tile = world.getBlockEntity(position);
            if (tile instanceof ILaserTarget) {
                ILaserTarget target = (ILaserTarget) tile;
                return target.getRequiredLaserPower() > 0;
            }
        }
        return false;
    }

    private ILaserTarget getTarget() {
        if (targetPos != null) {
            if (world.getBlockEntity(targetPos) instanceof ILaserTarget) {
                return (ILaserTarget) world.getBlockEntity(targetPos);
            }
        }
        return null;
    }

    private void updateLaser() {
        if (targetPos != null) {
            laserPos = new Vec3(targetPos)
                .addVector(
                    (5 + world.rand.nextInt(6) + 0.5) / 16D,
                    9 / 16D,
                    (5 + world.rand.nextInt(6) + 0.5) / 16D
                );
        } else {
            laserPos = null;
        }
    }

    public long getAverageClient() {
        return averageClient;
    }

    public long getMaxPowerPerTick() {
        return 4 * MjAPI.MJ;
    }

    @Override
    public void update() {
        if (world.isClientSide) {
            // set laser render position on client side
            if (clientLaserMoveInterval.markTimeIfDelay(world) || targetPos == null) {
                updateLaser();
            }
            return;
        }

        // set target tile on server side
        avgPower.tick();

        BlockPos previousTargetPos = targetPos;
        if (worldHasUpdated) {
            findPossibleTargets();
            worldHasUpdated = false;
        }

        if (!isPowerNeededAt(targetPos)) {
            targetPos = null;
        }

        if (serverTargetMoveInterval.markTimeIfDelay(world) || !isPowerNeededAt(targetPos)) {
            randomlyChooseTargetPos();
        }

        ILaserTarget target = getTarget();
        if (target != null) {
            long max = getMaxPowerPerTick();
            max *= battery.getStored() + max;
            max /= battery.getCapacity() / 2;
            max = Math.min(Math.min(max, getMaxPowerPerTick()), target.getRequiredLaserPower());
            long power = battery.extractPower(0, max);
            long excess = target.receiveLaserPower(power);
            if (excess > 0) {
                battery.addPowerChecking(excess, false);
            }
            avgPower.push(power - excess);
        } else {
            avgPower.clear();
        }

        if (!Objects.equals(previousTargetPos, targetPos) || true) {
            sendNetworkUpdate(NET_RENDER_DATA);
        }

        markChunkDirty();
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.setTag("battery", battery.serializeNBT());
        if (laserPos != null) {
            nbt.setTag("laser_pos", NBTUtilBC.writeVec3d(laserPos));
        }
        if (targetPos != null) {
            nbt.setTag("target_pos", NBTUtilBC.writeBlockPos(targetPos));
        }
        avgPower.writeToNbt(nbt, "average_power");
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        super.loadAdditional(nbt);
        // TODO: remove in next version
        if (nbt.hasKey("mj_battery")) {
            nbt.setTag("battery", nbt.getTag("mj_battery"));
        }
        battery.deserializeNBT(nbt.getCompoundTag("battery"));
        targetPos = NBTUtilBC.readBlockPos(nbt.getTag("target_pos"));
        laserPos = NBTUtilBC.readVec3d(nbt.getTag("laser_pos"));
        avgPower.readFromNbt(nbt, "average_power");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                battery.writeToBuffer(buffer);
                buffer.writeBoolean(targetPos != null);
                if (targetPos != null) {
                    MessageUtil.writeBlockPos(buffer, targetPos);
                }
                buffer.writeLong((long) avgPower.getAverage());
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Dist.CLIENT) {
            if (id == NET_RENDER_DATA) {
                battery.readFromBuffer(buffer);
                if (buffer.readBoolean()) {
                    targetPos = MessageUtil.readBlockPos(buffer);
                } else {
                    targetPos = null;
                }
                averageClient = buffer.readLong();
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("target = " + targetPos);
        left.add("laser = " + laserPos);
        left.add("average = " + LocaleUtil.localizeMjFlow(averageClient == 0 ? (long) avgPower.getAverage() : averageClient));
    }

    @Override
    public void validate() {
        super.validate();
        if (!world.isClientSide) {
            LocalBlockUpdateNotifier.instance(world).registerSubscriberForUpdateNotifications(this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!world.isClientSide) {
            LocalBlockUpdateNotifier.instance(world).removeSubscriberFromUpdateNotifications(this);
        }
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return new Box(this).extendToEncompass(targetPos).getBoundingBox();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IDetachedRenderer getDebugRenderer() {
        return new AdvDebuggerLaser(this);
    }
}
