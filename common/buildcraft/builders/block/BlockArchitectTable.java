/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.List;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileArchitectTable;

public class BlockArchitectTable extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final Property<Boolean> PROP_VALID = BuildCraftProperties.VALID;

    private static final int META_VALID_INDEX = 4;

    public BlockArchitectTable(BlockBehaviour.Properties props, String id) {
        super(props, id);
        setDefaultState(getDefaultState().setValue(PROP_VALID, Boolean.TRUE));
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(PROP_VALID);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        BlockState state = super.getStateFromMeta(meta);
        state = state.setValue(PROP_VALID, (meta & META_VALID_INDEX) == 0);
        return state;
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return super.getMetaFromState(state) | (state.getValue(PROP_VALID) ? 0 : META_VALID_INDEX);
    }

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileArchitectTable();
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide) {
            BCBuildersGuis.ARCHITECT.openGUI(player, pos);
        }
        return true;
    }

    @Override
    public boolean canBeRotated(Level world, BlockPos pos, BlockState state) {
        return false;
    }
}
