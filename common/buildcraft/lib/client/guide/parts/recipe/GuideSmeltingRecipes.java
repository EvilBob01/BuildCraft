/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

// TODO (Phase 6 — GUI): FurnaceRecipes was removed in 1.13. Smelting recipes are now SmeltingRecipe instances
// accessible via RecipeManager.getAllRecipesFor(RecipeType.SMELTING). Stub until the guide recipe system is reworked.
public enum GuideSmeltingRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack stack) {
        return Collections.emptyList();
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack) {
        return Collections.emptyList();
    }
}
