/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

public enum MigrationManager {
    INSTANCE;

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.migrate");

    private final Map<String, Item> itemMigrations = new HashMap<>();
    private final Map<String, Block> blockMigrations = new HashMap<>();

    public void addItemMigration(Item to, String... oldNames) {
        // If we mistakenly try to migrate null then it must have been disabled.
        if (to == null) {
            return;
        }
        for (String old : oldNames) {
            String oldLowerCase = old.toLowerCase(Locale.ROOT);
            if (itemMigrations.containsKey(oldLowerCase)) {
                throw new IllegalArgumentException("Already registered item migration \"" + oldLowerCase + "\"!");
            }
            itemMigrations.put(oldLowerCase, to);
            if (DEBUG) {
                BCLog.logger
                    .info("[lib.migrate] Adding item migration from " + oldLowerCase + " to " + to.builtInRegistryHolder().key().location());
            }
        }
    }

    public void addBlockMigration(Block to, String... oldNames) {
        // If we mistakenly try to migrate null then it must have been disabled.
        if (to == null) {
            return;
        }
        for (String old : oldNames) {
            String oldLowerCase = old.toLowerCase(Locale.ROOT);
            if (blockMigrations.containsKey(oldLowerCase)) {
                throw new IllegalArgumentException("Already registered block migration \"" + oldLowerCase + "\"!");
            }
            blockMigrations.put(oldLowerCase, to);
            if (DEBUG) {
                BCLog.logger
                    .info("[lib.migrate] Adding item migration from " + oldLowerCase + " to " + to.builtInRegistryHolder().key().location());
            }
        }
    }

    // TODO (Phase 10 — Registry): rewrite using NeoForge 1.21 MissingMappingsEvent
    // (net.neoforged.neoforge.registries.MissingMappingsEvent). The old RegistryEvent.MissingMappings
    // and IForgeRegistryEntry<T> APIs were removed.
}
