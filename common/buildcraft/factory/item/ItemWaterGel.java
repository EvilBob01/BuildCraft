/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.SoundCategory;
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
        this.maxStackSize = 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 start = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3 look = player.getLookVec();
        Vec3 end = start.add(look.scale(7));
        BlockHitResult ray = world.rayTraceBlocks(start, end, true, false, true);

        if (ray == null || ray.getBlockPos() == null) {
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
        world.playSound(null, player.posX, player.posY, player.posZ,//
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,//
                0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isClientSide) {
            world.setBlock(ray.getBlockPos(), BCFactoryBlocks.waterGel.defaultBlockState().withProperty(BlockWaterGel.PROP_STAGE, GelStage.SPREAD_0));
            world.scheduleTick(ray.getBlockPos(), BCFactoryBlocks.waterGel, 200);

            // TODO: Snowball stuff

            // EntitySnowball entitysnowball = new EntitySnowball(world, player);
            // entitysnowball.setHeadingFromThrower(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            // world.spawnEntityInWorld(entitysnowball);
        }

        // player.addStat(StatList.getObjectUseStats(this));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

}
