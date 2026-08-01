/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import buildcraft.lib.tile.TileBC_Neptune;

public abstract class BlockBCTile_Neptune extends BlockBCBase_Neptune {
    public BlockBCTile_Neptune(BlockBehaviour.Properties props, String id) {
        super(props, id);
    }

    @Override
    @Nullable
    public abstract TileBC_Neptune createTileEntity(Level world, BlockState state);

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public void onBlockExploded(Level world, BlockPos pos, Explosion explosion) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onExplode(explosion);
        }
        super.onBlockExploded(world, pos, explosion);
    }

    // BC internal hook for block removal — not a vanilla @Override. Called from onRemove.
    // Subclasses may override this for cleanup logic.
    protected void breakBlock(Level world, BlockPos pos, BlockState state) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onRemove();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            breakBlock(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // BC internal hook for block placement — not a vanilla @Override. Called from setPlacedBy.
    // Subclasses may override this.
    protected void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onPlacedBy(placer, stack);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        onBlockPlacedBy(world, pos, state, placer, stack);
        super.setPlacedBy(world, pos, state, placer, stack);
    }

    // BC internal hook for right-click — not a vanilla @Override. Called from useWithoutItem.
    // Subclasses may override this for block-specific GUI logic.
    protected boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand,
        Direction facing, float hitX, float hitY, float hitZ) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            return tileBC.onActivated(player, hand, facing, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        float hitX = (float)(hit.getLocation().x - pos.getX());
        float hitY = (float)(hit.getLocation().y - pos.getY());
        float hitZ = (float)(hit.getLocation().z - pos.getZ());
        boolean result = onBlockActivated(level, pos, state, player, InteractionHand.MAIN_HAND, hit.getDirection(), hitX, hitY, hitZ);
        return result ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileBC_Neptune) {
            TileBC_Neptune tileBC = (TileBC_Neptune) tile;
            tileBC.onNeighbourBlockChanged(block, fromPos);
        }
    }
}
