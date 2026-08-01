/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.energy;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import net.minecraft.resources.ResourceLocation;

import buildcraft.lib.config.EnumRestartRequirement;

import buildcraft.core.BCCoreConfig;

// TODO (Phase 8): net.minecraftforge.common.config.{Configuration,Property} and
// net.minecraftforge.fml.client.event.ConfigChangedEvent removed in NeoForge 1.21.1. Config
// now uses ModConfigSpec + the ModConfigEvent bus. Also ForgeRegistries.BIOMES no longer exists
// (biomes are a dynamic registry in 1.21.1). This class needs a full rewrite; for now defaults
// are hardcoded, config load/save/reload are no-ops, and biome-validation methods are no-ops so
// dependent code compiles.
public class BCEnergyConfig {

    public static boolean enableOilOceanBiome;
    public static boolean enableOilDesertBiome;

    public static boolean enableOilGeneration;
    public static double oilWellGenerationRate;
    public static boolean enableOilSpouts;
    public static boolean enableOilBurn;
    public static boolean oilIsSticky;
    public static boolean enableRfEngine;
    public static boolean enableMjDynamo;

    public static int smallSpoutMinHeight;
    public static int smallSpoutMaxHeight;
    public static int largeSpoutMinHeight;
    public static int largeSpoutMaxHeight;

    public static double smallOilGenProb;
    public static double mediumOilGenProb;
    public static double largeOilGenProb;

    public static final TIntSet excludedDimensions = new TIntHashSet();
    /** If false then {@link #excludedDimensions} should be treated as a whitelist rather than a blacklist. */
    public static boolean excludedDimensionsIsBlackList;
    public static final Set<ResourceLocation> excessiveBiomes = new HashSet<>();
    public static final Set<ResourceLocation> surfaceDepositBiomes = new HashSet<>();
    public static final Set<ResourceLocation> excludedBiomes = new HashSet<>();
    /** If false then {@link #excludedBiomes} should be treated as a whitelist rather than a blacklist. */
    public static boolean excludedBiomesIsBlackList;
    public static SpecialEventType christmasEventStatus = SpecialEventType.DAY_ONLY;

    public static void preInit() {
        // TODO (Phase 8): rewrite against ModConfigSpec.
        reloadConfig(EnumRestartRequirement.GAME);
        BCCoreConfig.addReloadListener(BCEnergyConfig::reloadConfig);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        // TODO (Phase 8): rewrite against ModConfigSpec.
        enableOilOceanBiome = true;
        enableOilDesertBiome = true;

        enableOilGeneration = true;
        oilWellGenerationRate = 1.0;
        enableOilSpouts = true;
        enableOilBurn = true;
        oilIsSticky = false;
        enableRfEngine = false;
        enableMjDynamo = false;

        smallSpoutMinHeight = 6;
        smallSpoutMaxHeight = 12;
        largeSpoutMinHeight = 10;
        largeSpoutMaxHeight = 20;

        // Original config values were percentages; divide by 100 to get probabilities.
        smallOilGenProb = 2.0 / 100;
        mediumOilGenProb = 0.1 / 100;
        largeOilGenProb = 0.04 / 100;

        excessiveBiomes.clear();
        excessiveBiomes.add(ResourceLocation.fromNamespaceAndPath(BCEnergy.MODID, "oil_desert"));
        excessiveBiomes.add(ResourceLocation.fromNamespaceAndPath(BCEnergy.MODID, "oil_ocean"));

        surfaceDepositBiomes.clear();

        excludedBiomes.clear();
        excludedBiomes.add(ResourceLocation.fromNamespaceAndPath("minecraft", "hell"));
        excludedBiomes.add(ResourceLocation.fromNamespaceAndPath("minecraft", "sky"));

        excludedBiomesIsBlackList = true;

        excludedDimensions.clear();
        excludedDimensions.add(-1);
        excludedDimensions.add(1);
        excludedDimensionsIsBlackList = true;

        christmasEventStatus = SpecialEventType.DAY_ONLY;
    }

    /** Called in post-init, after all biomes should have been registered.
     * TODO (Phase 8): reimplement using the dynamic biome registry once ModConfigSpec is wired up.
     * ForgeRegistries.BIOMES does not exist in NeoForge 1.21.1 — biomes are now a dynamic registry. */
    public static void validateBiomeNames() {
        // No-op: ForgeRegistries.BIOMES does not exist in NeoForge 1.21.1.
    }

    public enum SpecialEventType {
        DISABLED,
        DAY_ONLY,
        MONTH,
        ENABLED;

        public final String lowerCaseName = name().toLowerCase(Locale.ROOT);

        public boolean isEnabled(MonthDay date) {
            if (this == DISABLED) {
                return false;
            }
            if (this == ENABLED) {
                return true;
            }
            LocalDateTime now = LocalDateTime.now();
            if (now.getMonth() != date.getMonth()) {
                return false;
            }
            if (this == MONTH) {
                return true;
            }
            int thisDay = now.getDayOfMonth();
            int wantedDay = date.getDayOfMonth();
            return thisDay >= wantedDay - 1 && thisDay <= wantedDay + 1;
        }
    }
}
