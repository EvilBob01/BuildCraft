/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

public enum BCCoreGuis {
    LIST;

    /* TODO (Phase 6 — GUI): replace with player.openMenu(MenuProvider) using the NeoForge 1.21.1 menu system.
     * The old IGuiHandler / player.openGui() approach was removed; containers must now implement MenuProvider
     * and be opened via ServerPlayer#openMenu or NetworkHooks.openScreen. */
    public void openGUI(Player player) {
    }

    public void openGUI(Player player, BlockPos pos) {
    }
}
