/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileFiller;

public class BlockFiller extends BlockBCTile_Neptune implements IBlockWithFacing {
    // public static final Property<EnumFillerPattern> PATTERN = BuildCraftProperties.FILLER_PATTERN;

    public BlockFiller(BlockBehaviour.Properties props, String id) {
        super(props, id);
        // setDefaultState(getDefaultState().setValue(PATTERN, EnumFillerPattern.NONE));
    }

    // BlockState

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        // properties.add(PATTERN);
    }

    // Others

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileFiller();
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand,
        Direction side, float hitX, float hitY, float hitZ) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileFiller) {
            if (!((TileFiller) tile).hasBox()) {
                return false;
            }
        }
        if (!world.isClientSide) {
            BCBuildersGuis.FILLER.openGUI(player, pos);
        }
        return true;
    }

    @Override
    public boolean canBeRotated(Level world, BlockPos pos, BlockState state) {
        return false;
    }
}
