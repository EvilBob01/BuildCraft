/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.cap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;

import net.neoforged.neoforge.capabilities.BlockCapability;

import buildcraft.api.core.EnumPipePart;

/** Provides a simple way of mapping {@link BlockCapability}'s to instances, keyed by the side of the block entity
 * that is being queried.
 * <p>
 * Under NeoForge 1.21.1 capabilities are no longer polled through an {@code ICapabilityProvider} living on the block
 * entity itself. Instead every capability is registered once via
 * {@code net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent}, with a per-block-entity-type factory of the
 * shape {@code (blockEntity, side) -> T}. This class is meant to be held as a field on a block entity (or other
 * capability owner) and exposed to that factory, e.g.:
 *
 * <pre>{@code
 * event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MyBlockEntityType.INSTANCE,
 *     (be, side) -> be.caps.getCapability(Capabilities.ItemHandler.BLOCK, side));
 * }</pre>
 *
 * Additional providers (other {@link CapabilityHelper} instances, e.g. from composed components) can be chained via
 * {@link #addProvider(CapabilityHelper)}. */
public class CapabilityHelper {
    private final Map<EnumPipePart, Map<BlockCapability<?, Direction>, Supplier<?>>> caps = new EnumMap<>(
        EnumPipePart.class
    );
    private final List<CapabilityHelper> additional = new ArrayList<>();

    public CapabilityHelper() {
        for (EnumPipePart face : EnumPipePart.VALUES) {
            caps.put(face, new HashMap<>());
        }
    }

    private Map<BlockCapability<?, Direction>, Supplier<?>> getCapMap(@Nullable Direction facing) {
        return caps.get(EnumPipePart.fromFacing(facing));
    }

    public <T> void addCapabilityInstance(@Nullable BlockCapability<T, Direction> cap, T instance, EnumPipePart... parts) {
        Supplier<T> supplier = () -> instance;
        addCapability(cap, supplier, parts);
    }

    public <T> void addCapability(@Nullable BlockCapability<T, Direction> cap, Supplier<T> getter, EnumPipePart... parts) {
        if (cap == null) {
            return;
        }
        for (EnumPipePart part : parts) {
            caps.get(part).put(cap, getter);
        }
    }

    public <T> void addCapability(
        @Nullable BlockCapability<T, Direction> cap, Function<Direction, T> getter, EnumPipePart... parts
    ) {
        if (cap == null) {
            return;
        }
        for (EnumPipePart part : parts) {
            caps.get(part).put(cap, () -> getter.apply(part.face));
        }
    }

    public CapabilityHelper addProvider(@Nullable CapabilityHelper provider) {
        if (provider != null) {
            additional.add(provider);
        }
        return provider;
    }

    public <T> boolean hasCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing) {
        return getCapability(capability, facing) != null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing) {
        Map<BlockCapability<?, Direction>, Supplier<?>> capMap = getCapMap(facing);
        Supplier<?> supplier = capMap.get(capability);
        if (supplier != null) {
            return (T) supplier.get();
        }
        for (CapabilityHelper provider : additional) {
            T value = provider.getCapability(capability, facing);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
