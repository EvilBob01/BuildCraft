/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import buildcraft.api.inventory.IItemTransactor;

/** Provides various {@code @Nonnull} static final fields storing the {@link BlockCapability}'s used by BuildCraft.
 * <p>
 * Under NeoForge 1.21.1, capabilities are no longer polled from an {@code ICapabilityProvider} instance living on the
 * block entity: instead each capability type is a {@link BlockCapability} (or {@code ItemCapability}) that is
 * registered once, up front, and then looked up externally via
 * {@code level.getCapability(capability, pos, state, blockEntity, side)}. Block entities (or their owning blocks)
 * register providers for these capabilities inside a listener for
 * {@code net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent}, fired on the mod bus.
 * <p>
 * This class simply holds the shared {@link BlockCapability} instances so that both the registration code (the
 * {@code RegisterCapabilitiesEvent} listener) and the query call-sites can refer to the same capability object. */
public class CapUtil {

    /** Standard vanilla/NeoForge item handler capability, sided by {@link Direction}. Equivalent to the old Forge
     * {@code CapabilityItemHandler.ITEM_HANDLER_CAPABILITY}. */
    @Nonnull
    public static final BlockCapability<IItemHandler, Direction> CAP_ITEMS = Capabilities.ItemHandler.BLOCK;

    /** Standard vanilla/NeoForge fluid handler capability, sided by {@link Direction}. Equivalent to the old Forge
     * {@code CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY}. */
    @Nonnull
    public static final BlockCapability<IFluidHandler, Direction> CAP_FLUIDS = Capabilities.FluidHandler.BLOCK;

    /** BuildCraft's own item transactor capability. This has no NeoForge-provided equivalent, so it is registered
     * with a BuildCraft-owned {@link ResourceLocation}. */
    @Nonnull
    public static final BlockCapability<IItemTransactor, Direction> CAP_ITEM_TRANSACTOR = BlockCapability.createSided(
        ResourceLocation.fromNamespaceAndPath("buildcraftlib", "item_transactor"),
        IItemTransactor.class
    );

    private CapUtil() {}

    /** Attempts to fetch the given capability from the level at the given position, or returns null if either of
     * those two are null.
     * <p>
     * This replaces the old {@code getCapability(ICapabilityProvider, Capability, Direction)} helper: under NeoForge
     * capabilities are queried from the {@link net.minecraft.world.level.Level}, not from a provider instance. Callers
     * that used to hold an {@code ICapabilityProvider} should be updated to query via the level instead. */
    @Nullable
    public static <T> T getCapability(
        @Nullable net.minecraft.world.level.Level level,
        @Nonnull BlockCapability<T, Direction> capability,
        @Nullable net.minecraft.core.BlockPos pos,
        @Nullable Direction side
    ) {
        if (level == null || pos == null || capability == null) {
            return null;
        }
        return level.getCapability(capability, pos, side);
    }
}
