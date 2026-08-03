/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.transport.net.PipeItemMessageQueue;
import buildcraft.transport.wire.WorldSavedDataWireSystems;

public enum BCTransportEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel
                && serverLevel.getServer() != null) {
            WorldSavedDataWireSystems.get(serverLevel).tick();
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        PipeItemMessageQueue.serverTick();
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent event) {
        WorldSavedDataWireSystems.get(event.getPlayer().level()).changedPlayers.add(event.getPlayer());
    }

    // TODO (Phase 7 — rendering): onTextureStitch (TextureStitchEvent → NeoForge atlas stitch event)
    // PipeWireRenderer.clearWireCache() should be called after atlas rebuild.

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.BlockPlaceEvent event) {
        // event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        // event.setCanceled(true);
    }
}
