/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

public enum BCBuildersGuis {
    ARCHITECT,
    BUILDER,
    FILLER,
    LIBRARY,
    REPLACER,
    FILLER_PLANNER;

    public void openGUI(Player player) {
        // TODO (Phase 6 — GUI): player.openGui removed; // BCBuilders.INSTANCE, ordinal(), 0, 0, 0);
    }

    public void openGUI(Player player, BlockPos pos) {
        // TODO (Phase 6 — GUI): player.openGui removed; // BCBuilders.INSTANCE, ordinal(), pos.getX(), pos.getY(), pos.getZ());
    }
}
