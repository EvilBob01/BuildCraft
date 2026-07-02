/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;

import buildcraft.lib.net.MessageContext;
import buildcraft.api.BCModules;

import buildcraft.lib.net.MessageContainer;
import buildcraft.lib.net.MessageDebugRequest;
import buildcraft.lib.net.MessageDebugResponse;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.MessageMarker;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.cache.MessageObjectCacheRequest;
import buildcraft.lib.net.cache.MessageObjectCacheResponse;

/** Replaces the old {@code @SidedProxy}-based {@code ServerProxy}/{@code ClientProxy} split. Modern NeoForge
 * mods don't need a proxy pattern for side-specific code; instead this is a plain class whose methods either
 * work the same on both sides, or are gated by {@link FMLEnvironment#dist} / {@link OnlyIn} where they touch
 * client-only classes like {@link Minecraft}.
 *
 * The old {@code IGuiHandler} implementation ({@code getServerGuiElement}/{@code getClientGuiElement}) has been
 * removed entirely -- it's obsolete under the modern {@code player.openMenu(MenuProvider)} menu system, which
 * hasn't been ported yet (see stubs below). */
public class BCLibProxy {
    private static final BCLibProxy INSTANCE = new BCLibProxy();

    public static BCLibProxy getProxy() {
        return INSTANCE;
    }

    /** Called by {@link BCLib}'s constructor to register this module's networking messages on the mod event bus.
     * Mirrors {@link MessageManager#init(IEventBus)} in the same package. */
    public static void init(IEventBus modEventBus) {
        MessageManager.registerMessageClass(BCModules.LIB, MessageUpdateTile.class, MessageUpdateTile.HANDLER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageContainer.class, MessageContainer.HANDLER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageMarker.class, Dist.CLIENT);
        MessageManager.registerMessageClass(BCModules.LIB, MessageObjectCacheRequest.class,
            MessageObjectCacheRequest.HANDLER, Dist.DEDICATED_SERVER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageObjectCacheResponse.class, Dist.CLIENT);
        MessageManager.registerMessageClass(BCModules.LIB, MessageDebugRequest.class, MessageDebugRequest.HANDLER,
            Dist.DEDICATED_SERVER);
        MessageManager.registerMessageClass(BCModules.LIB, MessageDebugResponse.class, Dist.CLIENT);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientInit();
        }
    }

    // TODO (Phase 6.5): resource pack reload listener registration, sprite/config-listener registration,
    // and detached renderer registration are all client setup that used to live in ClientProxy#fmlPreInit /
    // fmlPostInit. Not wired up here yet -- needs the client-side registries (GuideManager, DetachedRenderer,
    // BCLibSprites, GuiConfigManager) to be re-checked against the ported NeoForge client APIs first.
    @OnlyIn(Dist.CLIENT)
    private static void clientInit() {
        // TODO (Phase 6.5): port ClientProxy#fmlPreInit/fmlPostInit client-only setup here.
    }

    public Level getClientWorld() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return getClientWorldClient();
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private static Level getClientWorldClient() {
        return Minecraft.getInstance().level;
    }

    public Player getClientPlayer() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return getClientPlayerClient();
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private static Player getClientPlayerClient() {
        return Minecraft.getInstance().player;
    }

    public Player getPlayerForContext(MessageContext ctx) {
        if (ctx.side == Dist.DEDICATED_SERVER) {
            return ctx.getServerHandler().player;
        }
        return getClientPlayer();
    }

    public void addScheduledTask(Level world, Runnable task) {
        if (world instanceof ServerLevel) {
            ServerLevel server = (ServerLevel) world;
            server.getServer().execute(task);
        } else if (FMLEnvironment.dist == Dist.CLIENT) {
            addScheduledTaskClient(task);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void addScheduledTaskClient(Runnable task) {
        Minecraft.getInstance().execute(task);
    }

    public <T extends BlockEntity> T getServerTile(T tile) {
        // TODO (Phase 6.5): the old ClientProxy implementation used DimensionManager.getWorld(id), which no
        // longer exists in NeoForge 1.21.1. Server-tile lookup on the client (singleplayer integrated server)
        // isn't ported yet; just return the client-side tile for now.
        return tile;
    }

    public InputStream getStreamForIdentifier(ResourceLocation identifier) throws IOException {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return getStreamForIdentifierClient(identifier);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private static InputStream getStreamForIdentifierClient(ResourceLocation identifier) throws IOException {
        // TODO (Phase 6.5): IResourceManager#getResource's return type and open-stream API changed significantly
        // between 1.12.2 and 1.21.1. Stubbed out until the resource pack / resource manager API is re-ported.
        return null;
    }

    public File getGameDirectory() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return getGameDirectoryClient();
        }
        // TODO (Phase 6.5): dedicated server game directory lookup (was FMLServerHandler.instance()
        // .getServer().getDataDirectory() in 1.12.2) needs to be re-derived from the NeoForge server context.
        return new File(".");
    }

    @OnlyIn(Dist.CLIENT)
    private static File getGameDirectoryClient() {
        return Minecraft.getInstance().gameDirectory;
    }

    public Iterable<File> getLoadedResourcePackFiles() {
        // TODO (Phase 6.5): ResourcePackRepository/AbstractResourcePack internals changed substantially; the old
        // reflection-based file lookup doesn't map cleanly onto the modern repository API. Stubbed for now.
        return Collections.emptySet();
    }
}
