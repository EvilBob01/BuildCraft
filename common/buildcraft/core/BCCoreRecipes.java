/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import net.minecraft.world.item.crafting.Recipe;
/** TODO (Phase 8 — see ROADMAP.md): all in-code recipe registration below was written for the 1.12.2
 * legacy recipe system (meta-based {@code ItemStack} constructors, {@code ShapelessOreRecipe}, and a
 * non-generic {@code RegistryEvent<Recipe>}), none of which exist in 1.21.1. Recipes are now data-driven
 * via JSON in {@code buildcraft_resources/data/<modid>/recipe/}. This class is stubbed to a no-op until
 * that conversion happens. */
public class BCCoreRecipes {

    public static void init(net.neoforged.bus.api.IEventBus modEventBus) {
        // no-op: recipes are now defined as data pack JSON, see TODO above
    }
}
