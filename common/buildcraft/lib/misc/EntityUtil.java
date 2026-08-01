/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import buildcraft.api.tools.IToolWrench;

public class EntityUtil {
    public static NonNullList<ItemStack> collectItems(Level world, BlockPos around, double radius) {
        return collectItems(world, new Vec3(around).addVector(0.5, 0.5, 0.5), radius);
    }

    public static NonNullList<ItemStack> collectItems(Level world, Vec3 around, double radius) {
        NonNullList<ItemStack> stacks = NonNullList.create();

        AABB aabb = BoundingBoxUtil.makeAround(around, radius);
        for (ItemEntity ent : world.getEntitiesWithinAABB(ItemEntity.class, aabb)) {
            if (!ent.isDead) {
                ent.isDead = true;
                stacks.add(ent.getItem());
            }
        }
        return stacks;
    }

    public static Vec3 getVec(Entity entity) {
        return new Vec3(entity.posX, entity.posY, entity.posZ);
    }

    public static void setVec(Entity entity, Vec3 vec) {
        entity.setPosition(vec.x, vec.y, vec.z);
    }

    public static InteractionHand getWrenchHand(LivingEntity entity) {
        ItemStack stack = entity.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return InteractionHand.MAIN_HAND;
        }
        stack = entity.getHeldItemOffhand();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    public static void activateWrench(Player player, BlockHitResult trace) {
        ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, InteractionHand.MAIN_HAND, stack, trace);
            return;
        }
        stack = player.getHeldItemOffhand();
        if (!stack.isEmpty() && stack.getItem() instanceof IToolWrench) {
            IToolWrench wrench = (IToolWrench) stack.getItem();
            wrench.wrenchUsed(player, InteractionHand.OFF_HAND, stack, trace);
        }
    }

    @Nonnull
    public static ItemStack getArrowStack(EntityArrow arrow) {
        // FIXME: Replace this with an invocation of arrow.getArrowStack
        // (but its protected so we can't)
        if (arrow instanceof EntitySpectralArrow) {
            return new ItemStack(Items.SPECTRAL_ARROW);
        }
        return new ItemStack(Items.ARROW);
    }
}
