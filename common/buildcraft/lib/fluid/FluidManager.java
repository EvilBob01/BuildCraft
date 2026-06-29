/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.client.renderer.block.statemap.StateMap;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModListState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.lib.registry.RegistrationHelper;

public class FluidManager {

    private static final RegistrationHelper HELPER = new RegistrationHelper();
    private static final List<BCFluidBlock> fluidBlocks = new ArrayList<>();

    /** Should only ever be called during pre-init */
    public static <F extends BCFluid> F register(F fluid) {

        if (!Loader.instance().isInState(LoaderState.PREINITIALIZATION)) {
            throw new IllegalStateException("Can only call this during pre-init!");
        }

        FluidRegistry.registerFluid(fluid);

        Material material = new BCMaterialFluid(fluid.getMapColour(), fluid.isFlammable());
        BCFluidBlock block = new BCFluidBlock(fluid, material);
        block/* setRegistryName removed - use registry directly */.getModContainerById(BCLib.MODID).orElse(null).getModId(), "fluid_block_" + fluid.getBlockName());
        block/* setUnlocalizedName removed in 1.21 */);
        block.setLightOpacity(fluid.getLightOpacity());
        HELPER.addForcedBlock(block);
        fluid.setBlock(block);
        FluidRegistry.addBucketForFluid(fluid);
        fluidBlocks.add(block);
        return fluid;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onModelBake(ModelBakeEvent event) {
        for (BCFluidBlock fluid : fluidBlocks) {
            event.getModelManager().getBlockModelShapes().registerBlockWithStateMapper(fluid,
                new StateMap.Builder().ignore(BlockFluidBase.LEVEL).build());
        }
    }
}
