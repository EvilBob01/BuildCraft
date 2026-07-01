/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.DamageSource;

import net.minecraftforge.common.ISpecialArmor;

import buildcraft.lib.item.IItemBuildCraft;

public class ItemGoggles extends ItemArmor implements IItemBuildCraft, ISpecialArmor {
    private static final ArmorProperties HELMET_PROPERTIES = new ArmorProperties(0, 0, 0);
    private final String id;

    public ItemGoggles(String id) {
        super(ArmorMaterial.CHAIN, 0, EntityEquipmentSlot.HEAD);
        this.id = id;
        init();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public ArmorProperties getProperties(LivingEntity player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
        return HELMET_PROPERTIES;
    }

    @Override
    public int getArmorDisplay(Player player, @Nonnull ItemStack armor, int slot) {
        return 0;
    }

    @Override
    public void damageArmor(LivingEntity entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot) {
        // Invulnerable goggles
    }
}
