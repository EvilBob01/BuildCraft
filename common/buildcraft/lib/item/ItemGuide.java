/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import net.minecraft.nbt.Tag;

import buildcraft.lib.BCLib;
import buildcraft.lib.guide.GuideBook;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.NBTUtilBC;

public class ItemGuide extends ItemBC_Neptune {
    private static final String DEFAULT_BOOK = "buildcraftcore:main";
    private static final ResourceLocation ADVANCEMENT = ResourceLocation.parse("buildcraftcore:guide");
    private static final String TAG_BOOK_NAME = "BookName";

    public ItemGuide(String id) {
        super(id);
        setContainerItem(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        // TODO (Phase 6 — GUI): player.openGui removed; open guide screen via MenuProvider/NetworkHooks.openScreen
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    protected void addSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            ItemStack stack = new ItemStack(this);
            if (!book.name.toString().equals(ItemGuide.DEFAULT_BOOK)) {
                setBookName(stack, book.name.toString());
            }
            items.add(stack);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String bookName = getBookName(stack);
        GuideBook book = GuideBookRegistry.INSTANCE.getBook(bookName);
        if (book != null) {
            return book.title.getFormattedText();
        }
        return super.getItemStackDisplayName(stack);
    }

    public static String getBookName(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains(TAG_BOOK_NAME, Tag.TAG_STRING)) {
            // So that existing guide books continue to work
            return ItemGuide.DEFAULT_BOOK;
        }
        return nbt.getString(TAG_BOOK_NAME);
    }

    public static void setBookName(ItemStack stack, String book) {
        CompoundTag nbt = NBTUtilBC.getItemData(stack);
        nbt.putString(TAG_BOOK_NAME, book);
    }
}
