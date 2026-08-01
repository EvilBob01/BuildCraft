/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

import buildcraft.lib.item.IItemBuildCraft;

// ISpecialArmor and ArmorProperties were removed from NeoForge 1.21.
// Custom armor defense behavior must be implemented via attributes or data-driven armor materials.
public class ItemGoggles extends ArmorItem implements IItemBuildCraft {
    private final String id;

    public ItemGoggles(String id) {
        super(ArmorMaterials.CHAIN, Type.HELMET, new Item.Properties().stacksTo(1));
        this.id = id;
        init();
    }

    @Override
    public String id() {
        return id;
    }
}
