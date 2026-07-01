/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;

public class BlockPlastic extends BlockBCBase_Neptune {
    public BlockPlastic(String id) {
        super(Block.Properties.of(), id);
        setDefaultState(getStateFromMeta(0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BuildCraftProperties.BLOCK_COLOR);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        DyeColor colour = state.getValue(BuildCraftProperties.BLOCK_COLOR);
        return colour.getMetadata();
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BuildCraftProperties.BLOCK_COLOR, DyeColor.byMetadata(meta));
    }

    @Override
    public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
        for (DyeColor dye : DyeColor.values()) {
            list.add(new ItemStack(this, 1, dye.getMetadata()));
        }
    }
}
