/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import net.minecraft.world.item.ItemStack;

import buildcraft.api.core.IEngineType;

import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.item.ItemBlockBCMulti;

public class ItemEngine_BC8<E extends Enum<E> & IEngineType> extends ItemBlockBCMulti {
    private final BlockEngineBase_BC8<E> engineBlock;

    public ItemEngine_BC8(BlockEngineBase_BC8<E> block) {
        // In 1.21, engine type is encoded in block state, not item damage.
        // Name function always uses the default engine type for now.
        super(block, stack -> {
            E type = block.getEngineProperty().getAllowedValues().iterator().next();
            return block.getUnlocalizedName(type);
        });
        engineBlock = block;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        E type = engineBlock.getEngineProperty().getAllowedValues().iterator().next();
        return "tile." + engineBlock.getUnlocalizedName(type);
    }
}
