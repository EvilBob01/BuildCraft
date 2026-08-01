/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.block;

import java.util.Random;
import net.minecraft.world.level.block.Block;
import java.util.function.Supplier;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraft.api.enums.EnumSpring;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.data.XorShift128Random;

public class BlockSpring extends BlockBCBase_Neptune {
    public static final Property<EnumSpring> SPRING_TYPE = BuildCraftProperties.SPRING_TYPE;

    public static final XorShift128Random rand = new XorShift128Random();

    public BlockSpring(String id) {
        super(Block.Properties.of(), id);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setSoundType(SoundType.STONE);

        disableStats();
        setTickRandomly(true);
        setDefaultState(getDefaultState().setValue(SPRING_TYPE, EnumSpring.WATER));
    }

    // BlockState

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SPRING_TYPE);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(SPRING_TYPE).ordinal();
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        if (meta == EnumSpring.OIL.ordinal()) {
            return getDefaultState().setValue(SPRING_TYPE, EnumSpring.OIL);
        } else {
            return getDefaultState().setValue(SPRING_TYPE, EnumSpring.WATER);
        }
    }

    // Other

    @Override
    public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
        for (EnumSpring type : EnumSpring.VALUES) {
            list.add(new ItemStack(this, 1));
        }
    }

    @Override
    public int damageDropped(BlockState state) {
        return state.getValue(SPRING_TYPE).ordinal();
    }

    @Override
    public void updateTick(Level world, BlockPos pos, BlockState state, Random random) {
        generateSpringBlock(world, pos, state);
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
    
    // @Override
    // public void onNeighborBlockChange(Level world, int x, int y, int z, int blockid) {
    // assertSpring(world, x, y, z);
    // }

    @Override
    public void onBlockAdded(Level world, BlockPos pos, BlockState state) {
        super.onBlockAdded(world, pos, state);
        world.scheduleTick(pos, this, state.getValue(SPRING_TYPE).tickRate);
    }

    private void generateSpringBlock(Level world, BlockPos pos, BlockState state) {
        EnumSpring spring = state.getValue(SPRING_TYPE);
        world.scheduleTick(pos, this, spring.tickRate);
        if (!spring.canGen || spring.liquidBlock == null) {
            return;
        }
        if (!world.isEmptyBlock(pos.up())) {
            return;
        }
        if (spring.chance != -1 && rand.nextInt(spring.chance) != 0) {
            return;
        }
        world.setBlock(pos.up(), spring.liquidBlock, 3);
    }

    // Prevents updates on chunk generation
    // @Override
    // public boolean func_149698_L() {
    // return false;
    // }
}
