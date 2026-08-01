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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileBuilder;

public class BlockBuilder extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final Property<EnumOptionalSnapshotType> SNAPSHOT_TYPE = BuildCraftProperties.SNAPSHOT_TYPE;

    public BlockBuilder(BlockBehaviour.Properties props, String id) {
        super(props, id);
        setDefaultState(getDefaultState().setValue(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE));
    }

    // BlockState

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(SNAPSHOT_TYPE);
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBuilder) {
            return state
                    .setValue(
                            SNAPSHOT_TYPE,
                            EnumOptionalSnapshotType.fromNullable(((TileBuilder) tile).snapshotType)
                    );
        }
        return state;
    }

    // Others

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileBuilder();
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide) {
            BCBuildersGuis.BUILDER.openGUI(player, pos);
        }
        return true;
    }

    @Override
    public boolean canBeRotated(Level world, BlockPos pos, BlockState state) {
        BlockEntity tile = world.getBlockEntity(pos);
        return !(tile instanceof TileBuilder) || ((TileBuilder) tile).getBuilder() == null;
    }
}
