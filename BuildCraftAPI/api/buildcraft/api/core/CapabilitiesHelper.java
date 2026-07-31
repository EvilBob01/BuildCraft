package buildcraft.api.core;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.capabilities.BlockCapability;

/** Creates the {@link BlockCapability} instances used by the BuildCraft API.
 * <p>
 * <b>This class previously did something very different.</b> Under old Forge, capabilities were registered by class
 * with {@code CapabilityManager}, which did not hand back the registered {@code Capability} instance — so this class
 * reflected into {@code CapabilityManager}'s private {@code providers} map to retrieve it. All of that machinery
 * ({@code Capability}, {@code CapabilityManager}, {@code IStorage}, {@code CapabilityInject}, and the NBT
 * read/write storage classes) was removed in NeoForge 1.21.1 and no longer exists.
 * <p>
 * NeoForge's replacement is far simpler: a capability is just a {@link BlockCapability} value that you create once
 * with a unique {@link ResourceLocation} and then look up externally via
 * {@code level.getCapability(capability, pos, state, blockEntity, side)}. Providers are attached per
 * block-entity-type inside a {@code net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent} listener on the
 * mod bus, rather than by the block entity implementing a provider interface.
 * <p>
 * The {@link #registerCapability(Class)} signature is deliberately unchanged so that existing API call sites
 * ({@code MjAPI}, {@code TilesAPI}, {@code PipeApi}) did not need to be rewritten — only the declared field types
 * changed from the old Forge {@code Capability} type to {@code BlockCapability<T, Direction>}.
 * <p>
 * See {@code buildcraft.lib.misc.CapUtil} for the equivalent on the non-API side. */
public class CapabilitiesHelper {

    /** Namespace used for all API-owned capability identifiers. */
    public static final String NAMESPACE = "buildcraftapi";

    private CapabilitiesHelper() {}

    /** Creates a sided {@link BlockCapability} for the given type.
     * <p>
     * The capability's {@link ResourceLocation} is derived deterministically from the class's simple name, so the
     * identifier is stable across runs without every call site having to spell one out: a leading {@code I}
     * interface prefix is dropped and the remaining camelCase is converted to snake_case. For example
     * {@code IMjConnector} becomes {@code buildcraftapi:mj_connector} and {@code PipePluggable} becomes
     * {@code buildcraftapi:pipe_pluggable}.
     * <p>
     * Note this keys off the <i>simple</i> name, so two capability types with the same simple name in different
     * packages would collide. That is not the case for any current BuildCraft capability.
     *
     * @param clazz The type that all instances must derive from.
     * @return The created {@link BlockCapability}. */
    @Nonnull
    public static <T> BlockCapability<T, Direction> registerCapability(Class<T> clazz) {
        return BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, toCapabilityPath(clazz)),
            clazz
        );
    }

    /** Converts a class's simple name into a {@link ResourceLocation}-safe snake_case path. Package-private so it can
     * be unit tested. */
    static String toCapabilityPath(Class<?> clazz) {
        String name = clazz.getSimpleName();

        // Drop a leading interface-style "I", but only when it actually prefixes another word, so that a type
        // legitimately starting with a lowercase letter after "I" (e.g. "Item") is left alone.
        if (name.length() > 1 && name.charAt(0) == 'I' && Character.isUpperCase(name.charAt(1))) {
            name = name.substring(1);
        }

        StringBuilder path = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    path.append('_');
                }
                path.append(Character.toLowerCase(c));
            } else {
                path.append(c);
            }
        }
        return path.toString();
    }
}
