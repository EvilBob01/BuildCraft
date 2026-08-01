/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.model.ModelHolderRegistry;
import buildcraft.lib.client.reload.ReloadManager;
import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.ClientDebuggables;
import buildcraft.lib.item.ItemDebugger;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.misc.FakePlayerProvider;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.net.MessageDebugRequest;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;

public enum BCLibEventDist {
    INSTANCE;

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer) {
            ServerPlayer playerMP = (ServerPlayer) entity;
            MessageUtil.doDelayedServer(() -> MarkerCache.onPlayerJoinWorld(playerMP));
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            MarkerCache.onWorldUnload(level);
        }
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            FakePlayerProvider.INSTANCE.unloadWorld(serverLevel);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onReloadFinish(EventBuildCraftReload.FinishLoad event) {
        GuideManager.INSTANCE.onRegistryReload(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onConnectToServer(ClientPlayerNetworkEvent.LoggingIn event) {
        BuildCraftObjectCaches.onClientJoinServer();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void textureStitchPre(TextureAtlasStitchedEvent event) {
        ReloadManager.INSTANCE.preReloadResources();
        SpriteHolderRegistry.onTextureStitchPre(event.getAtlas());
        ModelHolderRegistry.onTextureStitchPre(event.getAtlas());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @OnlyIn(Dist.CLIENT)
    public static void textureStitchPreLow(TextureAtlasStitchedEvent event) {
        FluidRenderer.onTextureStitchPre(event.getAtlas());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void textureStitchPost(TextureAtlasStitchedEvent event) {
        SpriteHolderRegistry.onTextureStitchPost();
        FluidRenderer.onTextureStitchPost(event.getAtlas());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void modelBake(ModelEvent.BakingCompleted event) {
        SpriteHolderRegistry.exportTextureMap();
        LaserRenderer_BC8.clearModels();
        ModelHolderRegistry.onModelBake();
        ModelVariableData.onModelBake();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void renderWorldLast(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        DetachedRenderer.INSTANCE.renderWorldLastEvent(player, partialTicks);
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        BCAdvDebugging.INSTANCE.onServerPostTick();
        MessageUtil.postServerTick();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void clientTick(ClientTickEvent.Post event) {
        BuildCraftObjectCaches.onClientTick();
        MessageUtil.postClientTick();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && ItemDebugger.isShowDebugInfo(player)) {
            HitResult hitResult = mc.hitResult;
            if (hitResult instanceof BlockHitResult mouseOver) {
                IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mouseOver);
                if (debuggable instanceof BlockEntity) {
                    BlockEntity tile = (BlockEntity) debuggable;
                    MessageManager.sendToServer(new MessageDebugRequest(tile.getBlockPos(), mouseOver.getDirection()));
                } else if (debuggable instanceof Entity) {
                    // TODO: Support entities!
                }
            }
        }
    }
}
