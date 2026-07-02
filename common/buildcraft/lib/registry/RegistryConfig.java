/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.transport.pipe.IItemPipe;

// TODO (Phase 8): net.minecraftforge.common.config.{Configuration,Property} removed in
// NeoForge 1.21.1, and Loader.instance()/ModContainer-keyed config storage is 1.7-era Forge
// API that no longer exists either. This whole per-mod "disable this item/block via config"
// system needs a rewrite against ModConfigSpec; for now isEnabled() always returns true so
// dependent code compiles and nothing gets silently disabled.
public class RegistryConfig {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.registry");
    private static final Map<String, Set<String>> disabled = new HashMap<>();

    // #######################
    //
    // Setup
    //
    // #######################

    /** @deprecated TODO (Phase 8): Configuration removed in NeoForge 1.21.1; this is a no-op
     *              stub kept only so call sites still compile. */
    @Deprecated
    public static Object setRegistryConfig(String modid, java.io.File file) {
        return null;
    }

    /** @deprecated TODO (Phase 8): Configuration removed in NeoForge 1.21.1; this is a no-op
     *              stub kept only so call sites still compile. */
    @Deprecated
    public static Object useOtherModConfigFor(String from, String to) {
        return null;
    }

    // #######################
    //
    // Checking
    //
    // #######################

    public static boolean isEnabled(Item item) {
        return true;
    }

    public static boolean isEnabled(Block block) {
        return true;
    }

    public static boolean isEnabled(String category, String resourcePath, String langKey) {
        return true;
    }

    public static boolean hasItemBeenDisabled(ResourceLocation loc) {
        return hasObjectBeenDisabled("items", loc) || hasObjectBeenDisabled("pipes", loc);
    }

    public static boolean hasBlockBeenDisabled(ResourceLocation loc) {
        return hasObjectBeenDisabled("blocks", loc);
    }

    /** @return True if the given location has been passed to {@link #isEnabled(Block)}, {@link #isEnabled(Item)}, or
     *         {@link #isEnabled(String, String, String)}, and it returned false (because it has been disabled in the
     *         appropriate mod's config) */
    public static boolean hasObjectBeenDisabled(String category, ResourceLocation loc) {
        Set<String> locations = disabled.get(category);
        return locations != null && locations.contains(loc.getPath());
    }

    // #######################
    //
    // Internals
    //
    // #######################

    private static String getCategory(Object obj) {
        if (obj instanceof IItemPipe) {
            return "pipes";
        } else if (obj instanceof Block) {
            return "blocks";
        } else {
            return "items";
        }
    }

    static void setDisabled(String category, String resourcePath) {
        disabled.computeIfAbsent(category, k -> new HashSet<>()).add(resourcePath);
    }
}
