/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.config.FileConfigManager;
import buildcraft.lib.registry.RegistryConfig;

// TODO (Phase 8): net.minecraftforge.common.config.{Configuration,Property} and
// net.minecraftforge.fml.client.event.ConfigChangedEvent removed in NeoForge 1.21.1. Config
// now uses ModConfigSpec + the ModConfigEvent bus. This class needs a full rewrite; for now
// defaults are hardcoded and config load/save/reload are no-ops so dependent code compiles.
public class BCCoreConfig {
    private static final List<Consumer<EnumRestartRequirement>> reloadListeners = new ArrayList<>();

    public static File configFolder;

    public static Object config;
    public static Object objConfig;
    public static FileConfigManager detailedConfigManager;

    public static boolean worldGen = true;
    public static boolean worldGenWaterSpring = true;
    public static boolean minePlayerProtected = false;
    public static boolean hidePower;
    public static boolean hideFluid;
    public static boolean pumpsConsumeWater;
    public static int markerMaxDistance = 64;
    public static int pumpMaxDistance = 64;
    public static int networkUpdateRate = 10;
    public static double miningMultiplier = 1;
    public static int miningMaxDepth = 512;

    public static void preInit(File cfgFolder) {
        configFolder = cfgFolder;
        // TODO (Phase 8): rewrite against ModConfigSpec.
        RegistryConfig.setRegistryConfig(BCCore.MODID, new File(cfgFolder, "objects.cfg"));
        detailedConfigManager = new FileConfigManager(
            " The buildcraft detailed configuration file. This contains a lot of miscellaneous options that have no "
                + "affect on gameplay.\n You should refer to the BC source code for a detailed description of what these do. (https://github.com/BuildCraft/BuildCraft)\n"
                + " This file will be overwritten every time that buildcraft starts, so don't change anything other than the values.");
        detailedConfigManager.setConfigFile(new File(cfgFolder, "detailed.properties"));

        reloadConfig(EnumRestartRequirement.GAME);
        addReloadListener(BCCoreConfig::reloadConfig);
    }

    public static void addReloadListener(Consumer<EnumRestartRequirement> listener) {
        reloadListeners.add(listener);
    }

    public static void postInit() {
        saveConfigs();
    }

    public static void saveConfigs() {
        // TODO (Phase 8): rewrite against ModConfigSpec.
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        // TODO (Phase 8): rewrite against ModConfigSpec.
    }
}
