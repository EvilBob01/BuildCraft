/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.tile.TileQuarry;

public class BlockQuarry extends BlockBCTile_Neptune implements IBlockWithFacing {
    private static final ResourceLocation ADVANCEMENT = ResourceLocation.parse("buildcraftbuilders:shaping_the_world");

    public BlockQuarry(BlockBehaviour.Properties props, String id) {
        super(props, id);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(BuildCraftProperties.CONNECTED_MAP.values());
    }

    private boolean isConnected(BlockGetter world, BlockPos pos, BlockState state, Direction side) {
        Direction facing = side;
        if (Arrays.asList(Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new)).contains(facing)) {
            facing = Direction.from2DDataValue(
                side.get2DDataValue() + 2 + state.getValue(getFacingProperty()).get2DDataValue());
        }
        BlockEntity tile = world.getBlockEntity(pos.relative(facing));
        return tile != null && CapUtil.hasCapability(tile, CapUtil.CAP_ITEMS, facing.getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Property<Boolean> prop = BuildCraftProperties.CONNECTED_MAP.get(direction);
        if (prop != null) {
            return state.setValue(prop, isConnected(level, pos, state, direction));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TileQuarry();
    }

    @Override
    public boolean canBeRotated(Level world, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void breakBlock(Level world, BlockPos pos, BlockState state) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileQuarry) {
            for (BlockPos blockPos : ((TileQuarry) tile).framePoses) {
                if (world.getBlockState(blockPos).getBlock() == BCBuildersBlocks.frame) {
                    world.removeBlock(blockPos, false);
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public SoundType getSoundType(BlockState state, Level world, BlockPos pos, @Nullable Entity entity) {
        return SoundType.ANVIL;
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer,
        ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof Player) {
            AdvancementUtil.unlockAdvancement((Player) placer, ADVANCEMENT);
        }
    }
}
