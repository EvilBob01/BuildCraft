/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;

import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.plug.PluggableLens;

public class ItemPluggableLens extends ItemBC_Neptune implements IItemPluggable {
    public ItemPluggableLens(String id) {
        super(id);
    }

    public static LensData getData(ItemStack stack) {
        return new LensData(stack);
    }

    @Nonnull
    public ItemStack getStack(DyeColor colour, boolean isFilter) {
        return getStack(new LensData(colour, isFilter));
    }

    @Nonnull
    public ItemStack getStack(LensData variant) {
        ItemStack stack = new ItemStack(this);
        variant.writeToStack(stack);
        return stack;
    }

    @Override
    public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand) {
        IPipe pipe = holder.getPipe();
        if (pipe == null || !(pipe.getFlow() instanceof IFlowItems)) {
            return null;
        }
        LensData data = getData(stack);
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), Blocks.STONE.defaultBlockState());
        PluggableDefinition def = BCSiliconPlugs.lens;
        return new PluggableLens(def, holder, side, data.colour, data.isFilter);
    }

    @Override
    public Component getName(ItemStack stack) {
        LensData data = getData(stack);
        String colour = data.colour == null ? LocaleUtil.localize("color.clear")
            : ColourUtil.getTextFullTooltipSpecial(data.colour);
        String first = LocaleUtil.localize(data.isFilter ? "item.Filter.name" : "item.Lens.name");
        return Component.literal(colour + " " + first);
    }

    public static class LensData {
        @Nullable
        public final DyeColor colour;
        public final boolean isFilter;

        public LensData(@Nullable DyeColor colour, boolean isFilter) {
            this.colour = colour;
            this.isFilter = isFilter;
        }

        public LensData(ItemStack stack) {
            CompoundTag tag = NBTUtilBC.getItemData(stack);
            if (tag.contains("colour")) {
                colour = DyeColor.byName(tag.getString("colour"), null);
            } else {
                colour = null;
            }
            isFilter = tag.getBoolean("isFilter");
        }

        public ItemStack writeToStack(ItemStack stack) {
            CompoundTag tag = NBTUtilBC.getItemData(stack);
            if (colour != null) {
                tag.putString("colour", colour.getName());
            } else {
                tag.remove("colour");
            }
            tag.putBoolean("isFilter", isFilter);
            return stack;
        }
    }
}
