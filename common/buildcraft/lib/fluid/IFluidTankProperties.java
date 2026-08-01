/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import javax.annotation.Nullable;

import net.neoforged.neoforge.fluids.FluidStack;

/**
 * BuildCraft-owned replacement for the removed {@code net.minecraftforge.fluids.capability.IFluidTankProperties}.
 * NeoForge 1.21.1 dropped this interface entirely; BuildCraft still uses it internally for its own fluid containers.
 * External fluid handlers expose tanks via {@link net.neoforged.neoforge.fluids.capability.IFluidHandler}'s
 * {@code getTanks()} / {@code getFluidInTank(int)} / {@code getTankCapacity(int)} API instead.
 */
public interface IFluidTankProperties {

    /** @return A copy of the fluid currently in this tank, or null if empty. */
    @Nullable
    FluidStack getContents();

    /** @return The maximum amount of fluid this tank can hold. */
    int getCapacity();

    /** @return Whether this tank can be filled at all. */
    boolean canFill();

    /** @return Whether this tank can be drained at all. */
    boolean canDrain();

    /** @return Whether this tank can accept the given fluid type. */
    boolean canFillFluidType(FluidStack fluidStack);

    /** @return Whether this tank can supply the given fluid type. */
    boolean canDrainFluidType(FluidStack fluidStack);
}
