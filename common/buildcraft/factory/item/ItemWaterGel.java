/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.item;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import buildcraft.lib.item.ItemBC_Neptune;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockWaterGel;
import buildcraft.factory.block.BlockWaterGel.GelStage;

public class ItemWaterGel extends ItemBC_Neptune {

    public ItemWaterGel(String id) {
        super(id);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 start = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(7));
        BlockHitResult ray = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, player));

        if (ray.getBlockPos() == null) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        Block b = world.getBlockState(ray.getBlockPos()).getBlock();
        if (b != Blocks.WATER) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        if (!player.getAbilities().instabuild) {
            stack.setCount(stack.getCount() - 1);
        }

        // Same as ItemSnowball
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
                0.5F, 0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!world.isClientSide) {
            world.setBlock(ray.getBlockPos(), BCFactoryBlocks.waterGel.defaultBlockState().setValue(BlockWaterGel.PROP_STAGE, GelStage.SPREAD_0), 3);
            world.scheduleTick(ray.getBlockPos(), BCFactoryBlocks.waterGel, 200);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

}
