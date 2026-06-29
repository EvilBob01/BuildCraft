/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.item.ItemShears;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.IShearable;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerShears implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          Player player,
                          IStripesActivator activator) {
        if (!(stack.getItem() instanceof ItemShears)) {
            return false;
        }

        pos = pos.offset(direction);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof IShearable) {
            IShearable shearableBlock = (IShearable) block;
            if (shearableBlock.isShearable(stack, world, pos)) {
                List<ItemStack> drops = shearableBlock.onSheared(stack, world, pos, 0);
                if (stack.attemptDamageItem(1, player.getRNG(), player instanceof ServerPlayer ? (ServerPlayer) player : null)) {
                    stack.shrink(1);
                }
                world.setBlock(pos, Blocks.AIR.getDefaultState(), 11); // Might become obsolete in 1.12+
                for (ItemStack dropStack : drops) {
                    activator.sendItem(dropStack, direction);
                }
                return true;
            }
        }
        return false;
    }
}
