/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import javax.annotation.Nullable;

import net.neoforged.neoforge.fluids.FluidStack;

public class TankProperties implements IFluidTankProperties {

    @Nullable
    private final FluidStack contents;
    private final int capacity;
    private final boolean canFill, canDrain;

    /** Constructor matching {@code FluidTankProperties(FluidStack, int, boolean, boolean)} from old Forge. */
    public TankProperties(@Nullable FluidStack contents, int capacity, boolean canFill, boolean canDrain) {
        this.contents = contents == null ? null : contents.copy();
        this.capacity = capacity;
        this.canFill = canFill;
        this.canDrain = canDrain;
    }

    /** Constructor matching {@code FluidTankProperties(FluidStack, int)} from old Forge (fill+drain both true). */
    public TankProperties(@Nullable FluidStack contents, int capacity) {
        this(contents, capacity, true, true);
    }

    /** Constructor from a live {@link Tank}. */
    public TankProperties(Tank tank, boolean canFill, boolean canDrain) {
        FluidStack current = tank.getFluid();
        this.contents = current == null ? null : current.copy();
        this.capacity = tank.getCapacity();
        this.canFill = canFill;
        this.canDrain = canDrain;
    }

    @Override
    @Nullable
    public FluidStack getContents() {
        return contents == null ? null : contents.copy();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean canFill() {
        return canFill;
    }

    @Override
    public boolean canDrain() {
        return canDrain;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluidStack) {
        return canFill;
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluidStack) {
        return canDrain;
    }
}
