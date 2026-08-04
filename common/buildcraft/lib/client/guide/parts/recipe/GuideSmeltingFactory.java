/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.StackUtil;

public class GuideSmeltingFactory implements GuidePartFactory {
    @Nonnull
    private final ItemStack input, output;
    private final int hash;

    public GuideSmeltingFactory(ItemStack input, ItemStack output) {
        this.input = StackUtil.asNonNull(input);
        this.output = StackUtil.asNonNull(output);
        this.hash = Objects.hash(input.getItem(), output.getItem());
    }

    // TODO: Rewrite using 1.21 RecipeManager; FurnaceRecipes removed in 1.13
    public static GuideSmeltingFactory create(ItemStack stack) {
        return null;
    }

    public static GuideSmeltingFactory create(Item output) {
        return create(new ItemStack(output));
    }

    @Override
    public GuideSmelting createNew(GuiGuide gui) {
        return new GuideSmelting(gui, input, output);
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
        GuideSmeltingFactory other = (GuideSmeltingFactory) obj;
        if (hash != other.hash) return false;
        return ItemStack.isSameItem(input, other.input)
            && ItemStack.isSameItem(output, other.output);
    }
}
