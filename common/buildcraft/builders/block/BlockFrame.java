/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.loot.LootParams;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.RotationUtil;

public class BlockFrame extends BlockBCBase_Neptune {
    public static final Map<Direction, Property<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public static final AABB BASE_AABB = new AABB(4 / 16D, 4 / 16D, 4 / 16D, 12 / 16D, 12 / 16D, 12 / 16D);
    public static final AABB CONNECTION_AABB = new AABB(4 / 16D, 0 / 16D, 4 / 16D, 12 / 16D, 4 / 16D, 12 / 16D);
    private static final VoxelShape BASE_SHAPE = Shapes.box(4 / 16D, 4 / 16D, 4 / 16D, 12 / 16D, 12 / 16D, 12 / 16D);

    public BlockFrame(BlockBehaviour.Properties props, String id) {
        super(props, id);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    // In 1.21, neighbor-dependent state is updated via updateShape instead of getActualState.
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Property<Boolean> prop = CONNECTED_MAP.get(direction);
        if (prop != null) {
            Block neighborBlock = neighborState.getBlock();
            state = state.setValue(prop, neighborBlock instanceof BlockFrame || neighborBlock instanceof BlockQuarry);
        }
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = BASE_SHAPE;
        for (Map.Entry<Direction, Property<Boolean>> entry : CONNECTED_MAP.entrySet()) {
            if (state.getValue(entry.getValue())) {
                AABB rotated = RotationUtil.rotateAABB(CONNECTION_AABB, entry.getKey());
                shape = Shapes.or(shape, Shapes.box(rotated.minX, rotated.minY, rotated.minZ, rotated.maxX, rotated.maxY, rotated.maxZ));
            }
        }
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.emptyList();
    }
}
