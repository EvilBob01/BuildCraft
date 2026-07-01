/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.event.entity.EntityJoinWorldEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.MessageManager;

import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public enum BCCoreEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world != null && !event.world.isClientSide && event.world.getMinecraftServer() != null) {
            WorldSavedDataVolumeBoxes.get(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayedServer(() ->
                MessageManager.sendTo(
                    new MessageVolumeBoxes(WorldSavedDataVolumeBoxes.get(event.getEntity().world).volumeBoxes),
                    (ServerPlayer) event.getEntity()
                )
            );
            WorldSavedDataVolumeBoxes.get(((ServerPlayer) event.getEntity()).world).volumeBoxes.stream()
                .filter(volumeBox -> volumeBox.isPausedEditingBy((ServerPlayer) event.getEntity()))
                .forEach(VolumeBox::resumeEditing);
        }
    }
}
