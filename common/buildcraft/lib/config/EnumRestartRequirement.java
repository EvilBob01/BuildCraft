/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.config;

// TODO (Phase 8): net.minecraftforge.common.config.Property removed in NeoForge 1.21.1;
// config now uses ModConfigSpec. setTo() needs to be rewritten against
// ModConfigSpec.Builder/ConfigValue's worldRestart()/restart() equivalents.

public enum EnumRestartRequirement {
    NONE(false, false),
    WORLD(true, false),
    GAME(true, true);

    private final boolean restartWorld, restartGame;

    EnumRestartRequirement(boolean restartWorld, boolean restartGame) {
        this.restartWorld = restartWorld;
        this.restartGame = restartGame;
    }

    public boolean hasBeenRestarted(EnumRestartRequirement requirement) {
        if (restartGame && !requirement.restartGame) return false;
        if (restartWorld && !requirement.restartWorld) return false;
        return true;
    }
}
