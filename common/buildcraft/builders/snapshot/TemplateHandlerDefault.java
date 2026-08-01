/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.template.ITemplateHandler;

public enum TemplateHandlerDefault implements ITemplateHandler {
    INSTANCE;

    @Override
    public boolean handle(Level world, BlockPos pos, Player player, ItemStack stack) {
        Vec3 hitVec = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult);
        return stack.useOn(context) == InteractionResult.SUCCESS
            || stack.useOn(context) == InteractionResult.CONSUME;
    }
}
