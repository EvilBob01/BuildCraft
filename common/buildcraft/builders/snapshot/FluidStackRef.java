/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class FluidStackRef {
    private final NbtRef<StringTag> fluid;
    private final NbtRef<IntTag> amount;

    public FluidStackRef(NbtRef<StringTag> fluid, NbtRef<IntTag> amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public FluidStack get(Tag nbt) {
        String fluidName = fluid
            .get(nbt)
            .orElseThrow(NullPointerException::new)
            .getString();
        Fluid f = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(fluidName));
        return new FluidStack(
            Objects.requireNonNull(f),
            Optional.ofNullable(amount)
                .flatMap(ref -> ref.get(nbt))
                .map(IntTag::getInt)
                .orElse(FluidType.BUCKET_VOLUME)
        );
    }
}
