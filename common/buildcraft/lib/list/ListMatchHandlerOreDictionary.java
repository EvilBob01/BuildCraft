/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import buildcraft.api.lists.ListMatchHandler;

/** TODO (Phase 11 — see ROADMAP.md): this handler matched items via the 1.12.2 {@code OreDictionary}
 * (int-based ore IDs, {@code OreDictionary.getOres(name)}, wildcard metadata). All of that was removed in
 * favor of the modern {@code TagKey<Item>} data pack tag system, which has no int-ID or wildcard-metadata
 * equivalent. This handler is stubbed out (never matches, no client examples) until it is rewritten against
 * {@code ItemTags} / {@code BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)}. */
public class ListMatchHandlerOreDictionary extends ListMatchHandler {

    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        return false;
    }
}
