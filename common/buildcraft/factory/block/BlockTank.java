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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.transport.pipe.ICustomPipeConnection;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.tile.TileTank;

public class BlockTank extends BlockBCTile_Neptune implements ICustomPipeConnection, ITankBlockConnector {
    private static final Property<Boolean> JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;
    private static final AABB BOUNDING_BOX = new AABB(2 / 16D, 0 / 16D, 2 / 16D, 14 / 16D, 16 / 16D, 14 / 16D);

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

    @OnlyIn(Dist.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter world, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldSideBeRendered(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        return side.getAxis() != Axis.Y || !(world.getBlockState(pos.relative(side)).getBlock() instanceof ITankBlockConnector);
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        boolean isTankBelow = world.getBlockState(pos.below()).getBlock() instanceof ITankBlockConnector;
        return state.setValue(JOINED_BELOW, isTankBelow);
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
