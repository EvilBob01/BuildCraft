/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import java.util.function.Consumer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

import buildcraft.core.BCCore;

@Mod(BCRobotics.MODID)
public class BCRobotics {
    public static final String MODID = "buildcraftrobotics";

    public static BCRobotics INSTANCE;

    public BCRobotics(IEventBus modEventBus, ModContainer modContainer) {
        INSTANCE = this;
        RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);

        BCRoboticsBlocks.init(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadComplete);

        BCRoboticsProxy.init(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(BCRoboticsRecipes::init);
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        // no-op
    }

    static {
        startBatch();
        registerTag("item.block.zone_planner").reg("zone_planner").locale("zonePlannerBlock").model("zone_planner");
        registerTag("block.zone_planner").reg("zone_planner").oldReg("zonePlannerBlock").locale("zonePlannerBlock").model("zone_planner");
        registerTag("tile.zone_planner").reg("zone_planner");
        endBatch(TagManager.prependTags("buildcraftrobotics:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION)
            .andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) { return TagManager.registerTag(id); }
    private static void startBatch() { TagManager.startBatch(); }
    private static void endBatch(Consumer<TagEntry> consumer) { TagManager.endBatch(consumer); }
}
