/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.tools.IToolWrench;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.SoundUtil;

public class ItemWrench_Neptune extends ItemBC_Neptune implements IToolWrench {
    private static final ResourceLocation ADVANCEMENT = new ResourceLocation("buildcraftcore:wrenched");

    public ItemWrench_Neptune(String id) {
        super(id);
        setMaxStackSize(1);
    }

    @Override
    public boolean canWrench(Player player, InteractionHand hand, ItemStack wrench, BlockHitResult rayTrace) {
        return true;
    }

    @Override
    public void wrenchUsed(Player player, InteractionHand hand, ItemStack wrench, BlockHitResult rayTrace) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        player.swingArm(hand);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, BlockGetter world, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
        // FIXME: Disabled world check as it doesn't allow us to swing the player's arm!
        // if (world.isClientSide) {
        // return InteractionResult.PASS;
        // }
        BlockState state = world.getBlockState(pos);
        state = state.getActualState(world, pos);
        InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
        if (result == InteractionResult.SUCCESS) {
            wrenchUsed(player, hand, player.getHeldItem(hand), new BlockHitResult(new Vec3(hitX, hitY, hitZ), side, pos));
        }
        SoundUtil.playSlideSound(world, pos, state, result);
        return result;
    }
}
