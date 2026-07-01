/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import java.util.function.Consumer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCLog;

import buildcraft.lib.block.VanillaPaintHandlers;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.command.CommandBuildCraft;
import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.list.VanillaListHandlers;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.misc.ExpressionCompat;
import buildcraft.lib.net.MessageManager;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.registry.MigrationManager;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;
import buildcraft.lib.script.ReloadableRegistryManager;

@Mod(BCLib.MODID)
public class BCLib {
    public static final String MODID = "buildcraftlib";
    public static final String VERSION = "8.0.1-1.21.1";
    public static final String MC_VERSION = "1.21.1";
    public static final String GIT_BRANCH = "8.0.x-1.21.1-neoforge";
    public static final String GIT_COMMIT_HASH = "unknown";
    public static final String GIT_COMMIT_MSG = "NeoForge 1.21.1 port";
    public static final String GIT_COMMIT_AUTHOR = "EvilBob01";

    public static final boolean DEV = Boolean.getBoolean("buildcraft.dev");

    public static BCLib INSTANCE;
    public static ModContainer MOD_CONTAINER;

    public BCLib(IEventBus modEventBus, ModContainer modContainer) {
        INSTANCE = this;
        MOD_CONTAINER = modContainer;

        BCLib.logStartupInfo();
        ExpressionDebugManager.logger = BCLog.logger::info;
        ExpressionCompat.setup();

        BCLibRegistries.init(modEventBus);
        BCLibItems.init(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadComplete);

        NeoForge.EVENT_BUS.register(BCLibEventDist.class);
        NeoForge.EVENT_BUS.register(MigrationManager.INSTANCE);
        NeoForge.EVENT_BUS.addListener(this::serverStarting);

        BCLibProxy.init(modEventBus);
        BuildCraftObjectCaches.init(modEventBus);
        MessageManager.init(modEventBus);
    }

    private static void logStartupInfo() {
        BCLog.logger.info("Starting BuildCraft " + BCLib.VERSION);
        BCLog.logger.info("Copyright (c) the BuildCraft team, 2011-2018");
        BCLog.logger.info("https://www.mod-buildcraft.com");
        BCLog.logger.info("Loaded Modules:");
        for (BCModules module : BCModules.VALUES) {
            if (module.isLoaded()) {
                BCLog.logger.info("  - " + module.lowerCaseName);
            }
        }
        BCLog.logger.info("Missing Modules:");
        for (BCModules module : BCModules.VALUES) {
            if (!module.isLoaded()) {
                BCLog.logger.info("  - " + module.lowerCaseName);
            }
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BCLibRegistries.commonSetup();
            VanillaListHandlers.fmlInit();
            VanillaPaintHandlers.fmlInit();
            VanillaRotationHandlers.fmlInit();
            RegistrationHelper.registerTagEntries();
        });
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            ReloadableRegistryManager.loadAll();
            BuildCraftObjectCaches.fmlPostInit();
            VanillaListHandlers.fmlPostInit();
            MarkerCache.postInit();
        });
    }

    private void serverStarting(ServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher().register(CommandBuildCraft.register());
    }

    public static Error throwBadClass(Error e, Class<?> cls) throws Error {
        throw new Error(
            "Bad " + cls + " loaded from " + cls.getClassLoader() + " domain: " + cls.getProtectionDomain(), e
        );
    }

    static {
        startBatch();
        registerTag("item.guide").reg("guide").locale("buildcraft.guide").model("guide").tab("vanilla.misc");
        registerTag("item.guide.note").reg("guide_note").locale("buildcraft.guide_note").model("guide_note")
            .tab("vanilla.misc");
        registerTag("item.debugger").reg("debugger").locale("debugger").model("debugger").tab("vanilla.misc");
        endBatch(TagManager.prependTags("buildcraftlib:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION));
    }

    private static TagEntry registerTag(String id) {
        return TagManager.registerTag(id);
    }

    private static void startBatch() {
        TagManager.startBatch();
    }

    private static void endBatch(Consumer<TagEntry> consumer) {
        TagManager.endBatch(consumer);
    }
}
