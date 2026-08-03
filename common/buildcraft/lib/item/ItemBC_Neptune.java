/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.registry.TagManager;

public class ItemBC_Neptune extends Item implements IItemBuildCraft {
    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public ItemBC_Neptune(String id) {
        super(new Item.Properties());
        this.id = id;
        init();
    }

    @Override
    public String id() {
        return id;
    }

    /** @deprecated Subtypes via damage values were removed in 1.13; no-op stub for source compatibility. */
    @Deprecated
    protected final void setHasSubtypes(boolean has) {}

    /** @deprecated Creative tab population via getSubItems was removed in 1.16; use BuildCreativeModeTabContentsEvent.
     *  This method is kept for source compatibility with subclasses but is never called by the game. */
    protected void addSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }
}
