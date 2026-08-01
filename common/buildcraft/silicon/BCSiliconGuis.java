/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.silicon;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

public enum BCSiliconGuis {
    ASSEMBLY_TABLE,
    ADVANCED_CRAFTING_TABLE,
    INTEGRATION_TABLE,
    GATE;

    // TODO (Phase 6 — GUI): player.openGui removed; use ServerPlayer.openMenu or NetworkHooks.openScreen
    public void openGUI(Player player) {}

    public void openGUI(Player player, BlockPos pos) {}

    public void openGui(Player player, BlockPos pos, int data) {}
}
