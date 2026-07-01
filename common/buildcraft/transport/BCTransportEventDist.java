/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.transport.client.render.PipeWireRenderer;
import buildcraft.transport.net.PipeItemMessageQueue;
import buildcraft.transport.wire.WorldSavedDataWireSystems;

public enum BCTransportEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isClientSide && event.world.getMinecraftServer() != null) {
            WorldSavedDataWireSystems.get(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        PipeItemMessageQueue.serverTick();
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent event) {
        WorldSavedDataWireSystems.get(event.getPlayer().world).changedPlayers.add(event.getPlayer());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Post event) {
        PipeWireRenderer.clearWireCache();
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        // event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        // event.setCanceled(true);
    }
}
