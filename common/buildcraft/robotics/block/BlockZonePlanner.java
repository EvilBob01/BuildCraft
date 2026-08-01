/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.robotics.RoboticsGuis;
import buildcraft.robotics.tile.TileZonePlanner;

public class BlockZonePlanner extends BlockBCTile_Neptune implements IBlockWithFacing {
    public BlockZonePlanner(BlockBehaviour.Properties props, String id) {
        super(props, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileZonePlanner();
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand,
        Direction side, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide) {
            RoboticsGuis.ZONE_PLANTER.openGUI(player, pos);
        }
        return true;
    }
}
