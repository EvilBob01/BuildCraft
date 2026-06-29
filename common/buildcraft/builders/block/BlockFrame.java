/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.RotationUtil;

public class BlockFrame extends BlockBCBase_Neptune {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public static final AABB BASE_AABB = new AABB(4 / 16D, 4 / 16D, 4 / 16D, 12 / 16D, 12 / 16D, 12 / 16D);
    public static final AABB CONNECTION_AABB = new AABB(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 4 / 16D, 12 / 16D);

    public BlockFrame(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        for (Direction side : CONNECTED_MAP.keySet()) {
            Block block = world.getBlockState(pos.offset(side)).getBlock();
            state = state.withProperty(CONNECTED_MAP.get(side), block instanceof BlockFrame || block instanceof BlockQuarry);
        }
        return state;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
        BlockState actualState = state.getActualState(world, pos);
        Direction[] facings = CONNECTED_MAP.keySet().stream()
                .filter(facing -> actualState.getValue(CONNECTED_MAP.get(facing)))
                .toArray(Direction[]::new);
        if (facings.length == 1) {
            return side != facings[0];
        } else if (facings.length == 2 && facings[0] == facings[1].getOpposite()) {
            return side != facings[0] && side != facings[1];
        }
        return true;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter world, BlockPos pos) {
        BlockState actualState = state.getActualState(world, pos);
        AtomicReference<AABB> box = new AtomicReference<>(BASE_AABB);
        CONNECTED_MAP.forEach((side, property) -> {
            if (actualState.getValue(property)) {
                box.set(box.get().union(RotationUtil.rotateAABB(CONNECTION_AABB, side)));
            }
        });
        return box.get();
    }

    @Override
    public void addCollisionBoxToList(BlockState state, Level world, BlockPos pos, AABB entityBox, List<AABB> collidingBoxes, @Nullable Entity entity, boolean isPistonMoving) {
        BlockState actualState = state.getActualState(world, pos);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
        CONNECTED_MAP.keySet().stream()
                .filter(side -> actualState.getValue(CONNECTED_MAP.get(side)))
                .map(side -> RotationUtil.rotateAABB(CONNECTION_AABB, side))
                .forEach(box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box));
    }

    @Override
    public List<ItemStack> getDrops(BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        return Collections.emptyList();
    }
}
