/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.MessageManager;

import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public enum BCCoreEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel
                && serverLevel.getServer() != null) {
            WorldSavedDataVolumeBoxes.get(serverLevel).tick();
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayedServer(() ->
                MessageManager.sendTo(
                    new MessageVolumeBoxes(WorldSavedDataVolumeBoxes.get(player.serverLevel()).volumeBoxes),
                    player
                )
            );
            WorldSavedDataVolumeBoxes.get(player.serverLevel()).volumeBoxes.stream()
                .filter(volumeBox -> volumeBox.isPausedEditingBy(player))
                .forEach(VolumeBox::resumeEditing);
        }
    }
}
