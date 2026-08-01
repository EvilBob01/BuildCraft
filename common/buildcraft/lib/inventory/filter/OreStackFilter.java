/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import javax.annotation.Nonnull;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.recipes.StackDefinition;

import buildcraft.lib.misc.StackUtil;

// TODO (Phase 10 — Registry): OreDictionary was removed in 1.13. Ore names (e.g. "ingotIron") map to tags
// (e.g. c:ingots/iron). This filter needs to be rewritten to use TagKey<Item> matching via
// stack.is(ItemTags.create(ResourceLocation.parse("c:ingots/iron"))) or a pre-built tag lookup.
/** Returns true if the stack matches any one of the ore-name filter strings. Currently a no-op stub. */
public class OreStackFilter implements IStackFilter {

    private final String[] ores;

    public OreStackFilter(String... iOres) {
        ores = iOres;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        // stub — OreDictionary removed in 1.13; always false until tag migration
        return false;
    }

    @Override
    public NonNullList<ItemStack> getExamples() {
        return NonNullList.create();
    }

    public static StackDefinition definition(int count, String... ores) {
        return new StackDefinition(new OreStackFilter(ores), count);
    }

    public static StackDefinition definition(String... ores) {
        return definition(1, ores);
    }
}
