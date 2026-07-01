/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import net.minecraftforge.fluids.BlockFluidClassic;
import net.neoforged.neoforge.fluids.FluidType;

public class BCFluidBlock extends BlockFluidClassic {
    private boolean sticky = false;

    public BCFluidBlock(Fluid fluid, Material material) {
        super(fluid, material);
        Boolean displaceWater = fluid.getDensity() > 1000;
        displacements.put(Blocks.WATER, displaceWater);
        displacements.put(Blocks.FLOWING_WATER, displaceWater);

        Boolean displaceLava = fluid.getDensity() > 9000;
        displacements.put(Blocks.LAVA, displaceLava);
        displacements.put(Blocks.FLOWING_LAVA, displaceLava);

        renderLayer = BlockRenderLayer.SOLID;
    }

    @Override
    public Boolean isEntityInsideMaterial(BlockGetter world, BlockPos pos, BlockState state, Entity entity, double yToTest, Material material, boolean testingHead) {
        if (material == Block.Properties.of()) {
            return true;
        }
        return null;
    }

    @Override
    public int getFlammability(BlockGetter world, BlockPos pos, Direction face) {
        return blockMaterial.getCanBurn() ? 200 : 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockGetter world, BlockPos pos, Direction face) {
        return blockMaterial.getCanBurn() ? 200 : 0;
    }

    @Override
    public void onEntityCollidedWithBlock(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
        if (sticky) {
            entityIn.setInWeb();
        }
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }
}
