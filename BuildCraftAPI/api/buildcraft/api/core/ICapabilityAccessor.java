package buildcraft.api.core;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;

import net.neoforged.neoforge.capabilities.BlockCapability;

/** Something that can supply BuildCraft capability instances for a given side.
 * <p>
 * This exists because NeoForge 1.21.1 removed the old Forge {@code ICapabilityProvider} that BuildCraft's various
 * capability holders used to share. NeoForge's replacement of the same name is a generic
 * {@code ICapabilityProvider<O, C, T>} describing a <i>registration-time factory</i>, not a thing an object
 * implements to be polled — so it cannot serve the role BuildCraft needed.
 * <p>
 * BuildCraft still wants a common type for "an object holding capability instances that can be chained together",
 * so that {@code CapabilityHelper}, {@code MjCapabilityHelper} and {@code ItemHandlerManager} can be composed via
 * {@code CapabilityHelper#addProvider}. That is all this interface is.
 * <p>
 * Implementations are not registered with the game directly. They are reached from a
 * {@code net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent} listener, whose per-block-entity-type
 * factory delegates into {@link #getCapability(BlockCapability, Direction)}. */
public interface ICapabilityAccessor {

    /** @return This object's instance for the given capability and side, or null if it doesn't provide one. */
    @Nullable
    <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing);
}
