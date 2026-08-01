/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.List;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.transport.pipe.ICustomPipeConnection;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.tile.TileTank;

public class BlockTank extends BlockBCTile_Neptune implements ICustomPipeConnection, ITankBlockConnector {
    private static final Property<Boolean> JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;
    private static final AABB BOUNDING_BOX = new AABB(2 / 16D, 0 / 16D, 2 / 16D, 14 / 16D, 16 / 16D, 14 / 16D);
    private static final VoxelShape SHAPE = Shapes.box(2 / 16D, 0 / 16D, 2 / 16D, 14 / 16D, 16 / 16D, 14 / 16D);

    public BlockTank(BlockBehaviour.Properties props, String id) {
        super(props, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileTank();
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(JOINED_BELOW);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return direction.getAxis() == Axis.Y && adjacentState.getBlock() instanceof ITankBlockConnector;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            return state.setValue(JOINED_BELOW, neighborState.getBlock() instanceof ITankBlockConnector);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileTank) {
            return ((TileTank) tile).getComparatorLevel();
        }
        return 0;
    }

    @Override
    public float getExtension(Level world, BlockPos pos, Direction face, BlockState state) {
        return face.getAxis() == Axis.Y ? 0 : 2 / 16f;
    }
}
