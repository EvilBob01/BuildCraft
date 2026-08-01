/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class IngredientStack {
    public final Ingredient ingredient;
    public final int count;

    public IngredientStack(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }

    public IngredientStack(Ingredient ingredient) {
        this(ingredient, 1);
    }

    // TODO (Phase 8 — Recipes): CraftingHelper.getIngredient(Object) removed.
    // In 1.21 use Ingredient.of(ItemStack), Ingredient.of(TagKey), etc. directly.
    public static IngredientStack of(Object o) {
        if (o instanceof Ingredient) return new IngredientStack((Ingredient) o);
        if (o instanceof ItemStack) return new IngredientStack(Ingredient.of((ItemStack) o));
        if (o instanceof net.minecraft.world.item.Item) return new IngredientStack(Ingredient.of((net.minecraft.world.item.Item) o));
        if (o instanceof net.minecraft.world.level.block.Block b) return new IngredientStack(Ingredient.of(b.asItem()));
        return new IngredientStack(Ingredient.EMPTY);
    }
}
