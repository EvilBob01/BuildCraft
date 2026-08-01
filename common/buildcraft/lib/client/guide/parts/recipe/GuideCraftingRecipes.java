/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.NonNullList;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;

// TODO (Phase 6 — GUI): ForgeRegistries.RECIPES was removed in 1.21; recipe iteration now requires a RecipeManager
// from Minecraft.getInstance().getConnection().getRecipeManager(). Index generation is currently stubbed to empty.
public enum GuideCraftingRecipes implements IStackRecipes {
    INSTANCE;

    private static final boolean USE_INDEX = true;

    private Map<Item, Set<Recipe>> inputIndexMap, outputIndexMap;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack target) {
        generateInputIndex();
        Set<Recipe> recipes = inputIndexMap.get(target.getItem());
        if (recipes == null) {
            return ImmutableList.of();
        }

        List<GuidePartFactory> list = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (checkRecipeUses(recipe, target)) {
                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    public void generateIndices() {
        generateInputIndex();
        generateOutputIndex();
    }

    private void generateInputIndex() {
        if (inputIndexMap == null) {
            inputIndexMap = new IdentityHashMap<>();
            // TODO (Phase 6): iterate RecipeManager recipes here
        }
    }

    private static void generateIngredientIndex(Recipe recipe, Ingredient ing, Map<Item, Set<Recipe>> indexMap) {
        for (ItemStack stack : ing.getItems()) {
            appendIndex(stack, recipe, indexMap);
        }
    }

    private static void appendIndex(ItemStack stack, Recipe recipe, Map<Item, Set<Recipe>> indexMap) {
        Set<Recipe> list = indexMap.get(stack.getItem());
        if (list == null) {
            list = new LinkedHashSet<>();
            indexMap.put(stack.getItem(), list);
        }
        list.add(recipe);
    }

    private static boolean checkRecipeUses(Recipe recipe, @Nonnull ItemStack target) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            if (recipe instanceof IRecipeViewable) {
                // TODO!
            }
        }
        for (Ingredient ing : ingredients) {
            if (ing.test(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack target) {
        generateOutputIndex();
        Set<Recipe> recipes = outputIndexMap.get(target.getItem());
        if (recipes == null) {
            return ImmutableList.of();
        }

        List<GuidePartFactory> list = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (checkRecipeOutputs(recipe, target)) {
                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory != null) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    private void generateOutputIndex() {
        if (outputIndexMap == null) {
            outputIndexMap = new IdentityHashMap<>();
            // TODO (Phase 6): iterate RecipeManager recipes here
        }
    }

    private static boolean checkRecipeOutputs(Recipe recipe, ItemStack target) {
        if (recipe instanceof IRecipeViewable) {
            ChangingItemStack changing = ((IRecipeViewable) recipe).getRecipeOutputs();
            if (changing.matches(target)) {
                return true;
            }
        } else {
            // TODO (Phase 6): recipe.getResultItem(RegistryAccess) requires a RegistryAccess; stub for now
        }
        return false;
    }

    private static boolean matches(@Nonnull ItemStack target, @Nullable Object in) {
        if (in instanceof ItemStack) {
            return StackUtil.doesEitherStackMatch((ItemStack) in, target);
        } else if (in instanceof List) {
            for (Object obj : (List<?>) in) {
                if (obj instanceof ItemStack) {
                    if (StackUtil.doesEitherStackMatch((ItemStack) obj, target)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
