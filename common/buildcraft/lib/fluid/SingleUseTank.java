/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib.fluid;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.common.util.Constants;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidStack;

public class SingleUseTank extends Tank {

    private static final String NBT_ACCEPTED_FLUID = "acceptedFluid";

    private FluidStack acceptedFluid;

    public SingleUseTank(@Nonnull String name, int capacity, BlockEntity tile) {
        super(name, capacity, tile);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null) {
            return 0;
        }

        if (doFill && acceptedFluid == null) {
            acceptedFluid = resource.copy();
            acceptedFluid.amount = 1;
        }

        if (acceptedFluid == null || acceptedFluid.isFluidEqual(resource)) {
            return super.fill(resource, doFill);
        }

        return 0;
    }

    public void reset() {
        acceptedFluid = null;
    }

    public void setAcceptedFluid(Fluid fluid) {
        if (fluid == null) {
            this.acceptedFluid = null;
        } else {
            this.acceptedFluid = new FluidStack(fluid, 1);
        }
    }

    public void setAcceptedFluid(FluidStack fluid) {
        if (fluid == null) {
            this.acceptedFluid = null;
        } else {
            this.acceptedFluid = new FluidStack(fluid, 1);
        }
    }

    public FluidStack getAcceptedFluid() {
        return acceptedFluid;
    }

    @Override
    public void writeTankToNBT(CompoundTag nbt) {
        super.writeTankToNBT(nbt);
        if (acceptedFluid != null) {
            nbt.setTag(NBT_ACCEPTED_FLUID, acceptedFluid.saveAdditional(new CompoundTag()));
        }
    }

    @Override
    public void readTankFromNBT(CompoundTag nbt) {
        super.readTankFromNBT(nbt);
        if (nbt.hasKey(NBT_ACCEPTED_FLUID, Constants.NBT.TAG_STRING)) {
            setAcceptedFluid(FluidRegistry.getFluid(nbt.getString(NBT_ACCEPTED_FLUID)));
        } else {
            acceptedFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(NBT_ACCEPTED_FLUID));
        }
    }
}
