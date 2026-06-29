package buildcraft.transport.client;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.transport.tile.TilePipeHolder;

public enum PipeBlockColours implements IBlockColor {
    INSTANCE;

    @Override
    public int colorMultiplier(BlockState state, @Nullable BlockGetter world, @Nullable BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TilePipeHolder) {
                TilePipeHolder tilePipeHolder = (TilePipeHolder) tile;
                Direction side = Direction.from3DDataValue(tintIndex % Direction.VALUES.length);
                PipePluggable pluggable = tilePipeHolder.getPluggable(side);
                if (pluggable != null) {
                    return pluggable.getBlockColor(tintIndex / 6);
                }
            }
        }
        return -1;
    }
}
