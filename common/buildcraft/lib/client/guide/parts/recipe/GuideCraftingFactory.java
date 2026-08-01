/* Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/ */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.NonNullMatrix;

public class GuideCraftingFactory implements GuidePartFactory {

    private final NonNullMatrix<Ingredient> input;
    private final @Nonnull ItemStack output;
    private final int hash;

    public GuideCraftingFactory(Ingredient[][] input, ItemStack output) {
        this.input = new NonNullMatrix<>(input, Ingredient.EMPTY);
        this.output = StackUtil.asNonNull(output);
        this.hash = computeHash(this.input);
    }

    private static int computeHash(NonNullMatrix<Ingredient> input) {
        int[] ids = new int[input.getWidth() * input.getHeight()];
        int i = 0;
        for (Ingredient ingredient : input) {
            ItemStack[] stacks = ingredient.getMatchingStacks();
            ids[i++] = stacks.length == 0 ? 0 : BuiltInRegistries.ITEM.getId(stacks[0].getItem());
        }
        return Arrays.hashCode(ids);
    }

    // TODO (Phase 6): Rewrite using 1.21 RecipeManager; ForgeRegistries.RECIPES and OreDictionary removed.
    public static GuidePartFactory create(@Nonnull ItemStack stack) {
        return null;
    }

    public static GuidePartFactory getFactory(Recipe<?> recipe) {
        ItemStack output = recipe.getResultItem(net.minecraft.core.RegistryAccess.EMPTY);
        NonNullList<Ingredient> input = recipe.getIngredients();
        if (input == null || input.isEmpty() || output.isEmpty()) {
            return null;
        }
        Ingredient[][] matrix = new Ingredient[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int idx = x + y * 3;
                matrix[x][y] = idx < input.size() ? input.get(idx) : Ingredient.EMPTY;
            }
        }
        return new GuideCraftingFactory(matrix, output);
    }

    public static GuidePartFactory create(Item output) {
        return create(new ItemStack(output));
    }

    @Override
    public GuideCrafting createNew(GuiGuide gui) {
        return new GuideCrafting(gui, input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GuideCraftingFactory other = (GuideCraftingFactory) obj;
        if (hash != other.hash) return false;
        if (input.getWidth() != other.input.getWidth() || input.getHeight() != other.input.getHeight()) return false;
        return Objects.equals(computeHash(input), computeHash(other.input));
    }
}
