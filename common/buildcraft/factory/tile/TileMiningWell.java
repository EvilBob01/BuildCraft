/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import net.minecraft.world.level.material.Fluid;
import javax.annotation.Nonnull;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.fluids.FluidType;

import net.minecraft.world.level.block.entity.BlockEntityType;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.IMjReceiver;

import buildcraft.lib.inventory.AutomaticProvidingTransactor;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.world.WorldEventListenerAdapter;

import buildcraft.core.BCCoreConfig;
import buildcraft.factory.BCFactoryBlocks;

public class TileMiningWell extends TileMiner {
    private boolean shouldCheck = true;
    private final SafeTimeTracker tracker = new SafeTimeTracker(256);
    private final IWorldEventListener worldEventListener = new WorldEventListenerAdapter() {
        @Override
        public void notifyBlockUpdate(@Nonnull Level world,
                                      @Nonnull BlockPos pos,
                                      @Nonnull BlockState oldState,
                                      @Nonnull BlockState newState,
                                      int flags) {
            if (worldPosition.getX() == TileMiningWell.this.worldPosition.getX() &&
                worldPosition.getY() <= TileMiningWell.this.worldPosition.getY() &&
                worldPosition.getZ() == TileMiningWell.this.worldPosition.getZ()) {
                shouldCheck = true;
            }
        }
    };

    public TileMiningWell(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        caps.addCapabilityInstance(CapUtil.CAP_ITEM_TRANSACTOR, AutomaticProvidingTransactor.INSTANCE, EnumPipePart.VALUES);
    }

    @Override
    protected void mine() {
        if (currentPos != null && canBreak()) {
            shouldCheck = true;
            long target = BlockUtil.computeBlockBreakPower(level, currentPos);
            progress += battery.extractPower(0, target - progress);
            if (progress >= target) {
                progress = 0;
                level.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                BlockUtil.breakBlockAndGetDrops(
                    (ServerLevel) world,
                    currentPos,
                    new ItemStack(Items.DIAMOND_PICKAXE),
                    getOwner()
                ).ifPresent(stacks ->
                    stacks.forEach(stack -> InventoryUtil.addToBestAcceptor(level, pos, null, stack))
                );
                nextPos();
            } else {
                if (!level.isEmptyBlock(currentPos)) {
                    level.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
                }
            }
        } else if (shouldCheck || tracker.markTimeIfDelay(level)) {
            nextPos();
            if (currentPos == null) {
                shouldCheck = false;
            }
        }
    }

    private boolean canBreak() {
        if (level.isEmptyBlock(currentPos) || BlockUtil.isUnbreakableBlock(level, currentPos, getOwner())) {
            return false;
        }

        Fluid fluid = BlockUtil.getFluidWithFlowing(level, currentPos);
        return fluid == null || fluid.getViscosity() <= 1000;
    }

    private void nextPos() {
        currentPos = worldPosition;
        while (true) {
            currentPos = currentPos.down();
            if (level.isOutsideBuildHeight(currentPos)) {
                break;
            }
            if (worldPosition.getY() - currentPos.getY() > BCCoreConfig.miningMaxDepth) {
                break;
            }
            if (canBreak()) {
                updateLength();
                return;
            } else if (!level.isEmptyBlock(currentPos) && level.getBlockState(currentPos).getBlock() != BCFactoryBlocks.tube) {
                break;
            }
        }
        currentPos = null;
        updateLength();
    }

    @Override
    public void validate() {
        super.validate();
        if (!level.isClientSide) {
            level.addEventListener(worldEventListener);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!level.isClientSide) {
            level.removeEventListener(worldEventListener);
            if (currentPos != null) {
                level.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
            }
        }
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReceiver(battery);
    }
}
