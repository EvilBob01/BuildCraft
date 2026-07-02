/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

// TODO (Phase 8): net.minecraftforge.common.config.Property removed in NeoForge 1.21.1;
// config now uses ModConfigSpec. preInit()/reloadConfig() need a full rewrite; defaults are
// hardcoded below so dependent code still compiles.

import buildcraft.lib.config.EnumRestartRequirement;

public class BCBuildersConfig {
    /** Blueprints that save larger than this are stored externally, smaller ones are stored directly in the item. */
    public static int bptStoreExternalThreshold = 20_000;

    /** The minimum height that all quarry frames must be. */
    public static int quarryFrameMinHeight = 4;

    /** If true then the frame will move with the drill in both axis, if false then only 1 axis will follow the
     * drill. */
    public static boolean quarryFrameMoveBoth;

    public static int quarryMaxTasksPerTick = 4;
    public static int quarryTaskPowerDivisor = 2;
    public static double quarryMaxFrameMoveSpeed = 0;
    public static double quarryMaxBlockMineRate = 0;

    /** Client-side config to enable stencils-based drawing for the architect table. */
    public static boolean enableStencil = true;

    public static void preInit() {
        // TODO (Phase 8): rewrite against ModConfigSpec.
        reloadConfig(EnumRestartRequirement.GAME);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        // TODO (Phase 8): rewrite against ModConfigSpec.
    }
}
