/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;

public class BlockBCBase_Neptune extends Block {
    public static final Property<Direction> PROP_FACING = BuildCraftProperties.BLOCK_FACING;
    public static final Property<Direction> BLOCK_FACING_6 = BuildCraftProperties.BLOCK_FACING_6;

    /** The tag used to identify this in the {@link TagManager}. */
    public final String id;

    public BlockBCBase_Neptune(BlockBehaviour.Properties props, String id) {
        super(props);
        this.id = id == null ? "" : id;

        if (!this.id.isEmpty() && this instanceof IBlockWithFacing bwf) {
            registerDefaultState(defaultBlockState().setValue(bwf.getFacingProperty(), Direction.NORTH));
        }
    }

    /** Convenience constructor for code that still passes Block.Properties.of() as the first arg */
    public BlockBCBase_Neptune(String id) {
        this(BlockBehaviour.Properties.of().strength(5f, 10f), id);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        List<Property<?>> props = new ArrayList<>();
        addProperties(props);
        props.forEach(builder::add);
    }

    protected void addProperties(List<Property<?>> properties) {
        if (this instanceof IBlockWithFacing bwf) {
            properties.add(bwf.getFacingProperty());
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = defaultBlockState();
        if (this instanceof IBlockWithFacing bwf) {
            LivingEntity placer = ctx.getPlayer();
            Direction orientation = placer != null ? placer.getDirection() : Direction.NORTH;
            if (bwf.canFaceVertically() && placer != null) {
                BlockPos pos = ctx.getClickedPos();
                if (Mth.abs((float) placer.getX() - pos.getX()) < 2f
                        && Mth.abs((float) placer.getZ() - pos.getZ()) < 2f) {
                    double eyeY = placer.getY() + placer.getEyeHeight(placer.getPose());
                    if (eyeY - pos.getY() > 2.0) orientation = Direction.DOWN;
                    if (pos.getY() - eyeY > 0.0) orientation = Direction.UP;
                }
            }
            state = state.setValue(bwf.getFacingProperty(), orientation.getOpposite());
        }
        return state;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        if (this instanceof IBlockWithFacing bwf) {
            Property<Direction> prop = bwf.getFacingProperty();
            state = state.setValue(prop, rot.rotate(state.getValue(prop)));
        }
        return state;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (this instanceof IBlockWithFacing bwf) {
            Property<Direction> prop = bwf.getFacingProperty();
            state = state.setValue(prop, mirror.mirror(state.getValue(prop)));
        }
        return state;
    }
}
