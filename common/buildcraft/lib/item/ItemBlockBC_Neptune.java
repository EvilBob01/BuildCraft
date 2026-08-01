/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.ChatFormatting;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.LocaleUtil;

public class ItemBlockBC_Neptune extends BlockItem implements IItemBuildCraft {
    public final String id;

    public ItemBlockBC_Neptune(BlockBCBase_Neptune block) {
        super(block);
        this.id = "item." + block.id;
        init();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltip, flags);
        String tipId = getDescriptionId(stack) + ".tip";
        if (LocaleUtil.canLocalize(tipId)) {
            tooltip.add(Component.literal(LocaleUtil.localize(tipId)).withStyle(ChatFormatting.GRAY));
        } else if (flags.isAdvanced()) {
            tooltip.add(Component.literal(tipId).withStyle(ChatFormatting.GRAY));
        }
    }
}
