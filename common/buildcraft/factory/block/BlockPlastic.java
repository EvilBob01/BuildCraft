/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.block;

import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.DyeColor;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;

public class BlockPlastic extends BlockBCBase_Neptune {
    public BlockPlastic(String id) {
        super(Block.Properties.of(), id);
        setDefaultState(getDefaultState().setValue(BuildCraftProperties.BLOCK_COLOR, DyeColor.WHITE));
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(BuildCraftProperties.BLOCK_COLOR);
    }
}
