/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryGuis;
import buildcraft.factory.tile.TileChute;

public class BlockChute extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public BlockChute(BlockBehaviour.Properties props, String id) {
        super(props, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileChute();
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand,
        Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide) {
            BCFactoryGuis.CHUTE.openGUI(player, pos);
        }
        return true;
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
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        for (Direction side : Direction.values()) {
            state = state.setValue(CONNECTED_MAP.get(side), side != state.getValue(getFacingProperty())
                && TileChute.hasInventoryAtPosition(world, pos.relative(side), side));
        }
        return state;
    }

    // IBlockWithFacing

    @Override
    public boolean canFaceVertically() {
        return true;
    }
}
