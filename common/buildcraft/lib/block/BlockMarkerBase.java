/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.properties.BuildCraftProperties;

public abstract class BlockMarkerBase extends BlockBCTile_Neptune implements ICustomRotationHandler {
    private static final Map<Direction, AABB> BOUNDING_BOXES = new EnumMap<>(Direction.class);

    static {
        double halfWidth = 0.1;
        double h = 0.65;
        final double nw = 0.5 - halfWidth;
        final double pw = 0.5 + halfWidth;
        final double ih = 1 - h;
        BOUNDING_BOXES.put(Direction.DOWN, new AABB(nw, ih, nw, pw, 1, pw));
        BOUNDING_BOXES.put(Direction.UP, new AABB(nw, 0, nw, pw, h, pw));
        BOUNDING_BOXES.put(Direction.SOUTH, new AABB(nw, nw, 0, pw, pw, h));
        BOUNDING_BOXES.put(Direction.NORTH, new AABB(nw, nw, ih, pw, pw, 1));
        BOUNDING_BOXES.put(Direction.EAST, new AABB(0, nw, nw, h, pw, pw));
        BOUNDING_BOXES.put(Direction.WEST, new AABB(ih, nw, nw, 1, pw, pw));
    }

    public BlockMarkerBase(BlockBehaviour.Properties props, String id) {
        super(props, id);

        BlockState defaultState = getDefaultState();
        defaultState = defaultState.setValue(BuildCraftProperties.BLOCK_FACING_6, Direction.UP);
        defaultState = defaultState.setValue(BuildCraftProperties.ACTIVE, false);
        setDefaultState(defaultState);
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(BuildCraftProperties.BLOCK_FACING_6);
        properties.add(BuildCraftProperties.ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        AABB aabb = BOUNDING_BOXES.get(state.getValue(BuildCraftProperties.BLOCK_FACING_6));
        if (aabb == null) return Shapes.block();
        return Shapes.box(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getDefaultState().setValue(BuildCraftProperties.BLOCK_FACING_6, context.getClickedFace());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction sideOn = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
        BlockPos supportPos = pos.relative(sideOn.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, sideOn);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, isMoving);
        if (!canSurvive(state, world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof BlockMarkerBase) {
            Property<Direction> prop = BuildCraftProperties.BLOCK_FACING_6;
            return VanillaRotationHandlers.rotateEnumFacing(world, pos, state, prop, VanillaRotationHandlers.ROTATE_FACING);
        } else {
            return InteractionResult.PASS;
        }
    }
}
