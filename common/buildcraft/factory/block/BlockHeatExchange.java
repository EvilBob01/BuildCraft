/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import java.util.List;
import java.util.Locale;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.transport.pipe.ICustomPipeConnection;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.tile.TileHeatExchange;

public class BlockHeatExchange extends BlockBCTile_Neptune implements ICustomPipeConnection, IBlockWithFacing {

    public enum EnumExchangePart implements IStringSerializable {
        START,
        MIDDLE,
        END;

        private final String lowerCaseName = name().toLowerCase(Locale.ROOT);

        @Override
        public String getName() {
            return lowerCaseName;
        }
    }

    public static final Property<EnumExchangePart> PROP_PART = PropertyEnum.create("part", EnumExchangePart.class);
    public static final Property<Boolean> PROP_CONNECTED_Y = PropertyBool.create("connected_y");
    public static final Property<Boolean> PROP_CONNECTED_LEFT = PropertyBool.create("connected_left");
    public static final Property<Boolean> PROP_CONNECTED_RIGHT = PropertyBool.create("connected_right");

    public BlockHeatExchange(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(PROP_PART);
        properties.add(PROP_CONNECTED_Y);
        properties.add(PROP_CONNECTED_LEFT);
        properties.add(PROP_CONNECTED_RIGHT);
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            EnumExchangePart part;
            if (exchange.isStart()) {
                part = EnumExchangePart.START;
            } else if (exchange.isEnd()) {
                part = EnumExchangePart.END;
            } else {
                part = EnumExchangePart.MIDDLE;
            }
            Direction thisFacing = state.getValue(PROP_FACING);
            state = state.withProperty(PROP_PART, part);
            state = state.withProperty(PROP_CONNECTED_Y, false);

            boolean connectLeft = doesNeighbourConnect(world, pos, thisFacing, thisFacing.rotateY());
            state = state.withProperty(PROP_CONNECTED_LEFT, connectLeft);

            boolean connectRight = doesNeighbourConnect(world, pos, thisFacing, thisFacing.rotateYCCW());
            state = state.withProperty(PROP_CONNECTED_RIGHT, connectRight);
        }
        state = state.withProperty(PROP_CONNECTED_Y, false);
        return state;
    }

    private static boolean doesNeighbourConnect(BlockGetter world, BlockPos pos, Direction thisFacing,
        Direction dir) {
        BlockState neighbour = world.getBlockState(pos.offset(dir));
        if (neighbour.getBlock() == BCFactoryBlocks.heatExchange) {
            return neighbour.getValue(PROP_FACING) == thisFacing;
        }
        return false;
    }

    @Override
    public boolean rotateBlock(Level world, BlockPos pos, Direction axis) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            return exchange.rotate();
        }
        return false;
    }

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileHeatExchange) {
            TileHeatExchange exchange = (TileHeatExchange) tile;
            return exchange.rotate() ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileHeatExchange();
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public float getExtension(Level world, BlockPos pos, Direction face, BlockState state) {
        return 0;
    }
}
