/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import java.util.function.Function;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.block.BlockBCBase_Neptune;

/** Multi-variant block item. In 1.21 metadata is gone; variants must be encoded in NBT or as separate items. */
public class ItemBlockBCMulti extends ItemBlockBC_Neptune {
    protected final Function<ItemStack, String> nameFunction;

    public ItemBlockBCMulti(BlockBCBase_Neptune block, Function<ItemStack, String> nameFunction) {
        super(block);
        this.nameFunction = nameFunction;
    }

    public ItemBlockBCMulti(BlockBCBase_Neptune block, final String[] namesByMeta) {
        this(block, stack -> namesByMeta[0]);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId() + "." + this.nameFunction.apply(stack);
    }
}
