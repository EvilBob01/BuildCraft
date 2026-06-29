/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import buildcraft.lib.block.BlockBCBase_Neptune;

import buildcraft.factory.tile.TileMiner;

public class BlockTube extends BlockBCBase_Neptune {
    private static final AABB BOUNDING_BOX = new AABB(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 16 / 16D, 12 / 16D);

    public BlockTube(Material material, String id) {
        super(material, id);
        setBlockUnbreakable();
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
    public boolean removedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest) {
        BlockPos currentPos = pos;
        // noinspection StatementWithEmptyBody
        while (world.getBlockState(currentPos = currentPos.up()).getBlock() == this) {
        }
        if (!(world.getBlockEntity(currentPos) instanceof TileMiner)) {
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        } else {
            return false;
        }
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return BOUNDING_BOX;
    }
}
