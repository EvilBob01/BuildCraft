package buildcraft.energy.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SupportType;
import buildcraft.lib.misc.BlockFaceShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import buildcraft.api.blocks.ICustomRotationHandler;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.energy.tile.TileDynamoMJ;

public class BlockDynamoMJ extends BlockBCTile_Neptune implements ICustomRotationHandler {

    public BlockDynamoMJ(BlockBehaviour.Properties props, String id) {
        super(props, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileDynamoMJ();
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullBlock(BlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(BlockState state) {
        return false;
    }

    public BlockFaceShape getBlockFaceShape(BlockGetter world, BlockState state, BlockPos pos, Direction side) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            if (side == engine.getCurrentDirection().getOpposite()) {
                return BlockFaceShape.SOLID;
            } else {
                return BlockFaceShape.UNDEFINED;
            }
        }
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @Deprecated
    public boolean isSideSolid(BlockState base_state, BlockGetter world, BlockPos pos, Direction side) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            return side == engine.getCurrentDirection().getOpposite();
        }
        return false;
    }

    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        if (world.isClientSide) return;
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            engine.rotateIfInvalid();
        }
    }

    // ICustomRotationHandler

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            return engine.attemptRotation();
        }
        return InteractionResult.FAIL;
    }
}
