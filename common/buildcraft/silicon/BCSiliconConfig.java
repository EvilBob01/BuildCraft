/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

// TODO (Phase 8): net.minecraftforge.common.config.{Configuration,Property} and
// net.minecraftforge.fml.client.event.ConfigChangedEvent removed in NeoForge 1.21.1. Config
// now uses ModConfigSpec + the ModConfigEvent bus. This class needs a full rewrite; for now
// the default value is hardcoded so dependent code still compiles.

public class BCSiliconConfig {

    public static boolean renderLaserBeams = true;

    public static void preInit() {
        // TODO (Phase 8): rewrite against ModConfigSpec.
    }

    public static void reloadConfig(buildcraft.lib.config.EnumRestartRequirement restarted) {
        // TODO (Phase 8): rewrite against ModConfigSpec.
    }
}
