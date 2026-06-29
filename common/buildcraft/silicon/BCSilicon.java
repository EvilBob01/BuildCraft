/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import java.util.function.Consumer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import buildcraft.api.BCModules;
import buildcraft.api.facades.FacadeAPI;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.CreativeTabManager.CreativeTabBC;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

import buildcraft.core.BCCore;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadeStateManager;

@Mod(BCSilicon.MODID)
public class BCSilicon {
    public static final String MODID = "buildcraftsilicon";

    public static BCSilicon INSTANCE;

    private static CreativeTabBC tabPlugs;
    private static CreativeTabBC tabFacades;

    public BCSilicon(IEventBus modEventBus, ModContainer modContainer) {
        INSTANCE = this;
        RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);

        tabPlugs = CreativeTabManager.createTab("buildcraft.plugs");
        tabFacades = CreativeTabManager.createTab("buildcraft.facades");
        FacadeAPI.registry = FacadeStateManager.INSTANCE;

        BCSiliconConfig.init();
        BCSiliconBlocks.init(modEventBus);
        BCSiliconPlugs.init(modEventBus);
        BCSiliconItems.init(modEventBus);
        BCSiliconStatements.init();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadComplete);

        BCSiliconProxy.init(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(FacadeStateManager::init);
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            if (BCSiliconItems.plugFacade != null) {
                FacadeBlockStateInfo state = FacadeStateManager.previewState;
                FacadeInstance inst = FacadeInstance.createSingle(state, false);
                tabFacades.setItem(BCSiliconItems.plugFacade.createItemStack(inst));
            }
            if (!BCModules.TRANSPORT.isLoaded()) {
                tabPlugs.setItem(BCSiliconItems.plugGate);
            }
        });
    }

    static {
        startBatch();
        registerTag("item.redstone_chipset").reg("redstone_chipset").locale("redstone_chipset").model("redstone_chipset/");
        registerTag("item.gate_copier").reg("gate_copier").locale("gateCopier").model("gatecopier_");
        registerTag("item.plug.gate").reg("plug_gate").locale("gate").model("pluggable/gate").tab("buildcraft.plugs").oldReg("plug_gate");
        registerTag("item.plug.lens").reg("plug_lens").locale("lens").model("pluggable/lens").tab("buildcraft.plugs").oldReg("plug_lens");
        registerTag("item.plug.pulsar").reg("plug_pulsar").locale("pulsar").model("plug_pulsar").tab("buildcraft.plugs").oldReg("plug_pulsar");
        registerTag("item.plug.light_sensor").reg("plug_light_sensor").locale("light_sensor").model("plug_light_sensor").tab("buildcraft.plugs").oldReg("plug_light_sensor");
        registerTag("item.plug.timer").reg("plug_timer").locale("timer").model("plug_timer").tab("buildcraft.plugs").oldReg("plug_timer");
        registerTag("item.plug.facade").reg("plug_facade").locale("Facade").model("plug_facade").tab("buildcraft.facades").oldReg("plug_facade");
        registerTag("item.block.laser").reg("laser").locale("laserBlock").model("laser");
        registerTag("item.block.assembly_table").reg("assembly_table").locale("assemblyTableBlock").model("assembly_table");
        registerTag("item.block.advanced_crafting_table").reg("advanced_crafting_table").locale("assemblyWorkbenchBlock").model("advanced_crafting_table");
        registerTag("item.block.integration_table").reg("integration_table").locale("integrationTableBlock").model("integration_table");
        registerTag("item.block.charging_table").reg("charging_table").locale("chargingTableBlock").model("charging_table");
        registerTag("item.block.programming_table").reg("programming_table").locale("programmingTableBlock").model("programming_table");
        registerTag("block.laser").reg("laser").oldReg("laserBlock").locale("laserBlock").model("laser");
        registerTag("block.assembly_table").reg("assembly_table").oldReg("assemblyTableBlock").locale("assemblyTableBlock").model("assembly_table");
        registerTag("block.advanced_crafting_table").reg("advanced_crafting_table").oldReg("advancedCraftingTableBlock").locale("assemblyWorkbenchBlock").model("advanced_crafting_table");
        registerTag("block.integration_table").reg("integration_table").oldReg("integrationTableBlock").locale("integrationTableBlock").model("integration_table");
        registerTag("block.charging_table").reg("charging_table").oldReg("chargingTableBlock").locale("chargingTableBlock").model("charging_table");
        registerTag("block.programming_table").reg("programming_table").oldReg("programmingTableBlock").locale("programmingTableBlock").model("programming_table");
        registerTag("tile.laser").reg("laser");
        registerTag("tile.assembly_table").reg("assembly_table");
        registerTag("tile.advanced_crafting_table").reg("advanced_crafting_table");
        registerTag("tile.integration_table").reg("integration_table");
        registerTag("tile.charging_table").reg("charging_table");
        registerTag("tile.programming_table").reg("programming_table");
        endBatch(TagManager.prependTags("buildcraftsilicon:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION)
            .andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) { return TagManager.registerTag(id); }
    private static void startBatch() { TagManager.startBatch(); }
    private static void endBatch(Consumer<TagEntry> consumer) { TagManager.endBatch(consumer); }
}
