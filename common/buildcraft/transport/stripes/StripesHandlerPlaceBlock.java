/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.transport.stripes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;

public enum StripesHandlerPlaceBlock implements IStripesHandlerItem {
    INSTANCE;

    @Override
    public boolean handle(Level world,
                          BlockPos pos,
                          Direction direction,
                          ItemStack stack,
                          Player player,
                          IStripesActivator activator) {
        if (!(stack.getItem() instanceof BlockItem)) {
            return false;
        }
        if (!world.isEmptyBlock(pos.relative(direction))) {
            return false;
        }
        stack.getItem().onItemUse(
            player,
            world,
            pos.relative(direction),
            InteractionHand.MAIN_HAND,
            direction,
            0.5f,
            0.5f,
            0.5f
        );
        return true;
    }
}
