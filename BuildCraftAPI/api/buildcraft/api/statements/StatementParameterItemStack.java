/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.core.render.ISprite;

public class StatementParameterItemStack implements IStatementParameter {
    // needed because ItemStack.EMPTY doesn't have @Nonnull applied to it :/
    @Nonnull
    private static final ItemStack EMPTY_STACK;

    /** Immutable parameter that has the {@link ItemStack#EMPTY} as it's {@link #stack}. */
    public static final StatementParameterItemStack EMPTY;

    static {
        ItemStack stack = ItemStack.EMPTY;
        if (stack == null) throw new Error("Somehow ItemStack.EMPTY was null!");
        EMPTY_STACK = stack;
        EMPTY = new StatementParameterItemStack();
    }

    @Nonnull
    protected final ItemStack stack;

    public StatementParameterItemStack() {
        stack = EMPTY_STACK;
    }

    public StatementParameterItemStack(@Nonnull ItemStack stack) {
        this.stack = stack;
    }

    public StatementParameterItemStack(CompoundTag nbt) {
        // TODO Phase 9: ItemStack no longer has a plain-CompoundTag constructor/save method in
        // 1.21 -- both need a HolderLookup.Provider (registry access) threaded through
        // IStatementParameter's (de)serialization API. Falls back to EMPTY until that's wired up.
        stack = EMPTY_STACK;
    }

    @Override
    public void writeToNbt(CompoundTag compound) {
        // TODO Phase 9: see constructor above -- needs a HolderLookup.Provider to call
        // ItemStack.save(provider) properly.
    }

    @Override
    public ISprite getSprite() {
        return null;
    }

    @Override
    @Nonnull
    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public StatementParameterItemStack onClick(
        IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse
    ) {
        if (stack.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack newStack = stack.copy();
            newStack.setCount(1);
            return new StatementParameterItemStack(newStack);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterItemStack) {
            StatementParameterItemStack param = (StatementParameterItemStack) object;

            return ItemStack.isSameItemSameComponents(stack, param.stack);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public String getDescription() {
        throw new UnsupportedOperationException("Don't call getDescription directly!");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<String> getTooltip() {
        if (stack.isEmpty()) {
            return ImmutableList.of();
        }
        List<String> tooltip = new java.util.ArrayList<>();
        for (net.minecraft.network.chat.Component line : stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)) {
            tooltip.add(line.getString());
        }
        if (!tooltip.isEmpty()) {
            tooltip.set(0, stack.getRarity().color() + tooltip.get(0));
            for (int i = 1; i < tooltip.size(); i++) {
                tooltip.set(i, ChatFormatting.GRAY + tooltip.get(i));
            }
        }
        return tooltip;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:stack";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return null;
    }
}
