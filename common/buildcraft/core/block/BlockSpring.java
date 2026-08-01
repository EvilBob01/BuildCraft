/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.block;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.api.enums.EnumSpring;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.data.XorShift128Random;

public class BlockSpring extends BlockBCBase_Neptune {
    public static final Property<EnumSpring> SPRING_TYPE = BuildCraftProperties.SPRING_TYPE;

    public static final XorShift128Random rand = new XorShift128Random();

    public BlockSpring(String id) {
        // Block properties (indestructible, sound, random ticks) are set at registration via BlockBehaviour.Properties
        super(BlockBehaviour.Properties.of().strength(-1f, 3600000f), id);
        setDefaultState(getDefaultState().setValue(SPRING_TYPE, EnumSpring.WATER));
    }

    @Override
    protected void addProperties(List<Property<?>> properties) {
        super.addProperties(properties);
        properties.add(SPRING_TYPE);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(SPRING_TYPE).tileConstructor != null;
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        Supplier<BlockEntity> constructor = state.getValue(SPRING_TYPE).tileConstructor;
        if (constructor != null) {
            return constructor.get();
        }
        return null;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, world, pos, oldState, movedByPiston);
        world.scheduleTick(pos, this, state.getValue(SPRING_TYPE).tickRate);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        generateSpringBlock(world, pos, state);
    }

    private void generateSpringBlock(Level world, BlockPos pos, BlockState state) {
        EnumSpring spring = state.getValue(SPRING_TYPE);
        world.scheduleTick(pos, this, spring.tickRate);
        if (!spring.canGen || spring.liquidBlock == null) {
            return;
        }
        if (!world.isEmptyBlock(pos.above())) {
            return;
        }
        if (spring.chance != -1 && rand.nextInt(spring.chance) != 0) {
            return;
        }
        world.setBlock(pos.above(), spring.liquidBlock, 3);
    }
}
