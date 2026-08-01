/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;

public class BlockDecoration extends BlockBCBase_Neptune {
    public static final Property<EnumDecoratedBlock> DECORATED_TYPE = BuildCraftProperties.DECORATED_BLOCK;

    public BlockDecoration(String id) {
        super(Block.Properties.of(), id);
        registerDefaultState(defaultBlockState().setValue(DECORATED_TYPE, EnumDecoratedBlock.DESTROY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DECORATED_TYPE);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        EnumDecoratedBlock type = state.getValue(DECORATED_TYPE);
        return type.lightValue;
    }
}
