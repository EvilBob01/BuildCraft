/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.IAdvDebugTarget;

public class ItemDebugger extends ItemBC_Neptune {
    public ItemDebugger(String id) {
        super(id);
    }

    @Override
    public InteractionResult onItemUseFirst(Player player, Level world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, InteractionHand hand) {
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile == null) {
            return InteractionResult.FAIL;
        }
        if (tile instanceof IAdvDebugTarget) {
            BCAdvDebugging.setCurrentDebugTarget((IAdvDebugTarget) tile);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public static boolean isShowDebugInfo(Player player) {
        return player.capabilities.isCreativeMode ||
            player.getHeldItem(InteractionHand.MAIN_HAND).getItem() instanceof ItemDebugger ||
            player.getHeldItem(InteractionHand.OFF_HAND).getItem() instanceof ItemDebugger;
    }
}
