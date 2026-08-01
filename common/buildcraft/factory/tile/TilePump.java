/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.HolderLookup;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.base.Stopwatch;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidStack;
import buildcraft.lib.net.MessageContext;
import net.neoforged.api.distmarker.Dist;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.ProfilerUtil;
import buildcraft.lib.misc.ProfilerUtil.ProfilerEntry;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.mj.MjRedstoneBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.tile.ITileOilSpring;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.factory.BCFactoryBlocks;

public class TilePump extends TileMiner {
    public static final boolean DEBUG_PUMP = BCDebugging.shouldDebugComplex("factory.pump");

    private static final Direction[] SEARCH_NORMAL = new Direction[] { //
        Direction.UP, Direction.NORTH, Direction.SOUTH, //
        Direction.WEST, Direction.EAST //
    };

    private static final Direction[] SEARCH_GASEOUS = new Direction[] { //
        Direction.DOWN, Direction.NORTH, Direction.SOUTH, //
        Direction.WEST, Direction.EAST //
    };

    static final class FluidPath {
        public final BlockPos thisPos;

        @Nullable
        public final FluidPath parent;

        public FluidPath(BlockPos thisPos, FluidPath parent) {
            this.thisPos = thisPos;
            this.parent = parent;
        }

        public FluidPath and(BlockPos pos) {
            return new FluidPath(worldPosition, this);
        }
    }

    private static final ResourceLocation ADVANCEMENT_DRAIN_ANY
        = ResourceLocation.parse("buildcraftfactory:draining_the_world");

    private static final ResourceLocation ADVANCEMENT_DRAIN_OIL
        = ResourceLocation.parse("buildcraftfactory:oil_platform");

    private final Tank tank = new Tank("tank", 16 * FluidType.BUCKET_VOLUME, this);
    private boolean queueBuilt = false;
    private final Map<BlockPos, FluidPath> paths = new HashMap<>();
    private BlockPos fluidConnection;
    private final Deque<BlockPos> queue = new ArrayDeque<>();
    private boolean isInfiniteWaterSource;
    private final SafeTimeTracker rebuildDelay = new SafeTimeTracker(30);

    /** The position just below the bottom of the pump tube. */
    private BlockPos targetPos;

    @Nullable
    private BlockPos oilSpringPos;

    public TilePump() {
        tank.setCanFill(false);
        tankManager.add(tank);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tank, EnumPipePart.VALUES);
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjRedstoneBatteryReceiver(battery);
    }

    private void buildQueue() {
        queue.clear();
        paths.clear();
        Fluid queueFluid = null;
        isInfiniteWaterSource = false;
        Set<BlockPos> checked = new HashSet<>();
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        for (targetPos = worldPosition.down(); !level.isOutsideBuildHeight(targetPos); targetPos = targetPos.down()) {
            if (worldPosition.getY() - targetPos.getY() > BCCoreConfig.miningMaxDepth) {
                break;
            }
            if (BlockUtil.getFluidWithFlowing(level, targetPos) != null) {
                queueFluid = BlockUtil.getFluidWithFlowing(level, targetPos);
                nextPosesToCheck.add(targetPos);
                paths.put(targetPos, new FluidPath(targetPos, null));
                checked.add(targetPos);
                if (BlockUtil.getFluid(level, targetPos) != null) {
                    queue.add(targetPos);
                }
                fluidConnection = targetPos;
                break;
            }
            if (!level.isEmptyBlock(targetPos) && level.getBlockState(targetPos).getBlock() != BCFactoryBlocks.tube) {
                break;
            }
        }
        if (nextPosesToCheck.isEmpty() || queueFluid == null) {
            return;
        }

        Profiler debugProf = new Profiler();
        debugProf.profilingEnabled = DEBUG_PUMP;
        ProfilerEntry prof = ProfilerUtil.createEntry(debugProf, level.getProfiler());
        Stopwatch watch = Stopwatch.createStarted();
        debugProf.push("root");
        buildQueue0(prof, queueFluid, nextPosesToCheck, checked);
        debugProf.pop();
        watch.stop();
        if (DEBUG_PUMP) {
            ProfilerUtil.logProfilerResults(debugProf, "root", watch.elapsed(TimeUnit.NANOSECONDS));
        }
    }

    private void buildQueue0(
        ProfilerEntry prof, Fluid queueFluid, List<BlockPos> nextPosesToCheck, Set<BlockPos> checked
    ) {
        prof.push("build");
        Direction[] directions = queueFluid.isGaseous() ? SEARCH_GASEOUS : SEARCH_NORMAL;
        boolean isWater
            = !BCCoreConfig.pumpsConsumeWater && FluidUtilBC.areFluidsEqual(queueFluid, Fluids.WATER);
        final int maxLengthSquared = BCCoreConfig.pumpMaxDistance * BCCoreConfig.pumpMaxDistance;
        outer: while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos posToCheck : nextPosesToCheckCopy) {
                int count = 0;
                for (Direction side : directions) {
                    prof.push("check");
                    BlockPos offsetPos = posToCheck.relative(side);
                    if (offsetPos.distSqr(targetPos) > maxLengthSquared) {
                        prof.pop();
                        continue;
                    }
                    boolean isNew = checked.add(offsetPos);
                    prof.pop();
                    if (isNew) {
                        prof.push("push");
                        prof.push("eq_get");
                        Fluid fluidAt = BlockUtil.getFluidWithFlowing(level, offsetPos);
                        prof.endStartSection("eq_cmp");
                        boolean eq = FluidUtilBC.areFluidsEqual(fluidAt, queueFluid);
                        prof.pop();
                        if (eq) {
                            prof.push("prevPath");
                            FluidPath oldPath = paths.get(posToCheck);
                            prof.endStartSection("new");
                            FluidPath path = new FluidPath(offsetPos, oldPath);
                            prof.endStartSection("putNew");
                            paths.put(offsetPos, path);
                            prof.endStartSection("getFluid");
                            if (BlockUtil.getFluid(level, offsetPos) != null) {
                                prof.endStartSection("addToQueue");
                                queue.add(offsetPos);
                            }
                            prof.endStartSection("next");
                            nextPosesToCheck.add(offsetPos);
                            count++;
                            prof.pop();
                        }
                        prof.pop();
                    } else {
                        // We've already tested this block: it *must* be a valid water source
                        count++;
                    }
                }
                if (isWater) {
                    prof.push("water_check");
                    if (count >= 2) {
                        BlockState below = level.getBlockState(posToCheck.down());
                        // Same check as in BlockDynamicLiquid.updateTick:
                        // if that method changes how it checks for adjacent
                        // water sources then this also needs updating
                        Fluid fluidBelow = BlockUtil.getFluidWithoutFlowing(below);
                        if (
                            FluidUtilBC.areFluidsEqual(fluidBelow, Fluids.WATER) || below.getMaterial().isSolid()
                        ) {
                            isInfiniteWaterSource = true;
                            prof.pop();
                            break outer;
                        }
                    }
                    prof.pop();
                }
            }
        }
        prof.endStartSection("oil_spring_search");
        if (isOil(queueFluid)) {
            List<BlockPos> springPositions = new ArrayList<>();
            BlockPos center = VecUtil.replaceValue(getBlockPos(), Axis.Y, 0);
            for (BlockPos spring : BlockPos.getAllInBox(center.offset(-10, 0, -10), center.offset(10, 0, 10))) {
                if (level.getBlockState(spring).getBlock() == BCCoreBlocks.spring) {
                    BlockEntity tile = level.getBlockEntity(spring);
                    if (tile instanceof ITileOilSpring) {
                        springPositions.add(spring);
                    }
                }
            }
            switch (springPositions.size()) {
                case 0:
                    break;
                case 1:
                    oilSpringPos = springPositions.get(0);
                    break;
                default:
                    springPositions.sort(Comparator.comparingDouble(pos::distanceSq));
                    oilSpringPos = springPositions.get(0);
            }

        }
        prof.pop();
    }

    private static boolean isOil(Fluid queueFluid) {
        if (BCModules.ENERGY.isLoaded()) {
            return FluidUtilBC.areFluidsEqual(queueFluid, BCEnergyFluids.crudeOil[0]);
        }
        return false;
    }

    private boolean canDrain(BlockPos blockPos) {
        Fluid fluid = BlockUtil.getFluid(level, blockPos);
        return tank.isEmpty() ? fluid != null : FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType());
    }

    private void nextPos() {
        while (!queue.isEmpty()) {
            currentPos = queue.removeLast();
            if (canDrain(currentPos)) {
                updateLength();
                return;
            }
        }
        currentPos = null;
        updateLength();
    }

    @Override
    protected BlockPos getTargetPos() {
        if (queue.isEmpty()) {
            return null;
        }
        return targetPos;
    }

    @Override
    public void update() {
        if (!queueBuilt && !level.isClientSide) {
            buildQueue();
            queueBuilt = true;
        }

        super.update();

        if (!level.isClientSide) {
            FluidUtilBC.pushFluidAround(level, pos, tank);
        }
    }

    @Override
    public void mine() {
        if (tank.getFluidAmount() > tank.getCapacity() / 2) {
            return;
        }
        long target = 10 * MjAPI.MJ;
        if (currentPos != null && paths.containsKey(currentPos)) {
            progress += battery.extractPower(0, target - progress);
            if (progress < target) {
                return;
            }

            FluidStack drain = BlockUtil.drainBlock(level, currentPos, false);

            drain_attempt: {

                if (drain == null) {
                    if (DEBUG_PUMP) {
                        BCLog.logger.info(
                            "Pump @ " + getBlockPos() + " tried to drain " + currentPos
                                + " but couldn't because no fluid was drained!"
                        );
                    }
                    break drain_attempt;
                }

                BlockPos invalid = getFirstInvalidPointOnPath(currentPos);
                if (invalid != null) {
                    if (DEBUG_PUMP) {
                        BCLog.logger.info(
                            "Pump @ " + getBlockPos() + " tried to drain " + currentPos
                                + " but couldn't because the path stopped at " + invalid + "!"
                        );
                    }
                    break drain_attempt;
                } else if (!canDrain(currentPos)) {
                    if (DEBUG_PUMP) {
                        BCLog.logger.info(
                            "Pump @ " + getBlockPos() + " tried to drain " + currentPos
                                + " but couldn't because it couldn't be drained!"
                        );
                    }
                    break drain_attempt;
                }
                tank.fillInternal(drain, true);
                progress = 0;
                isInfiniteWaterSource &= !BCCoreConfig.pumpsConsumeWater;
                if (isInfiniteWaterSource) {
                    isInfiniteWaterSource = FluidUtilBC.areFluidsEqual(drain.getFluid(), Fluids.WATER);
                }
                AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_DRAIN_ANY);
                if (!isInfiniteWaterSource) {
                    BlockUtil.drainBlock(level, currentPos, true);
                    if (isOil(drain.getFluid())) {
                        AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_DRAIN_OIL);
                        if (oilSpringPos != null) {
                            BlockEntity tile = level.getBlockEntity(oilSpringPos);
                            if (tile instanceof ITileOilSpring) {
                                ((ITileOilSpring) tile).onPumpOil(getOwner(), currentPos);
                            }
                        }
                    }
                    paths.remove(currentPos);
                    nextPos();
                }
                return;
            }
            if (!rebuildDelay.markTimeIfDelay(level)) {
                return;
            }
        } else {
            if (currentPos == null && !rebuildDelay.markTimeIfDelay(level)) {
                return;
            }
            if (DEBUG_PUMP) {
                if (currentPos == null) {
                    BCLog.logger.info("Pump @ " + getBlockPos() + " is rebuilding it's queue...");
                } else {
                    BCLog.logger.info(
                        "Pump @ " + getBlockPos() + " is rebuilding it's queue because we don't have a path for "
                            + currentPos
                    );
                }
            }
        }
        buildQueue();
        nextPos();
    }

    @Nullable
    private BlockPos getFirstInvalidPointOnPath(BlockPos from) {
        FluidPath path = paths.get(from);
        if (path == null) {
            return from;
        }
        do {
            if (BlockUtil.getFluidWithFlowing(level, path.thisPos) == null) {
                return path.thisPos;
            }
        } while ((path = path.parent) != null);
        return null;
    }

    // NBT

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        oilSpringPos = NBTUtilBC.readBlockPos(nbt.get("oilSpringPos"));
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        if (oilSpringPos != null) {
            nbt.put("oilSpringPos", NBTUtilBC.writeBlockPos(oilSpringPos));
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_LED_STATUS, buffer, side);
            } else if (id == NET_LED_STATUS) {
                tank.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Dist.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side, ctx);
            } else if (id == NET_LED_STATUS) {
                tank.readFromBuffer(buffer);
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("fluid = " + tank.getDebugString());
        left.add("queue size = " + queue.size());
        left.add("infinite = " + isInfiniteWaterSource);
    }

    @Override
    protected long getBatteryCapacity() {
        return 50 * MjAPI.MJ;
    }
}

