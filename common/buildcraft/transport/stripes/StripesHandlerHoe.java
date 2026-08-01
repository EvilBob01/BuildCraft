/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.transport.stripes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemHoe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerHoe implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          Player player,
                          IStripesActivator activator) {

        if (!(stack.getItem() instanceof ItemHoe)) {
            return false;
        }

        pos = pos.relative(direction);
        if (stack.onItemUse(
                player,
                world,
                pos,
                InteractionHand.MAIN_HAND,
                Direction.UP,
                0.0f,
                0.0f,
                0.0f
        ) != InteractionResult.PASS) {
            return true;
        }

        if (direction != Direction.UP && stack.onItemUse(
                player,
                world,
                pos.below(),
                InteractionHand.MAIN_HAND,
                Direction.UP,
                0.0f,
                0.0f,
                0.0f
        ) != InteractionResult.PASS) {
            return true;
        }

        return false;
    }

}
