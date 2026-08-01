/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.tile.TileFloodGate;

public class BlockFloodGate extends BlockBCTile_Neptune {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP;

    static {
        CONNECTED_MAP = new HashMap<>(BuildCraftProperties.CONNECTED_MAP);
        CONNECTED_MAP.remove(Direction.UP);
    }

    public BlockFloodGate(BlockBehaviour.Properties props, String id) {
        super(props, id);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileFloodGate();
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileFloodGate) {
            for (Direction side : CONNECTED_MAP.keySet()) {
                state = state.setValue(CONNECTED_MAP.get(side), ((TileFloodGate) tile).openSides.contains(side));
            }
        }
        return state;
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand,
        Direction side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof IToolWrench) {
            if (!world.isClientSide) {
                if (side != Direction.UP) {
                    BlockEntity tile = world.getBlockEntity(pos);
                    if (tile instanceof TileFloodGate) {
                        if (CONNECTED_MAP.containsKey(side)) {
                            TileFloodGate floodGate = (TileFloodGate) tile;
                            if (!floodGate.openSides.remove(side)) {
                                floodGate.openSides.add(side);
                            }
                            floodGate.queue.clear();
                            floodGate.sendNetworkUpdate(TileBC_Neptune.NET_RENDER_DATA);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }
}
