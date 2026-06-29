/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy;

import java.util.function.Consumer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

import buildcraft.lib.BCLib;
import buildcraft.lib.registry.MigrationManager;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.TagEntry;

import buildcraft.core.BCCore;

@Mod(BCEnergy.MODID)
public class BCEnergy {
    public static final String MODID = "buildcraftenergy";

    public static BCEnergy INSTANCE;

    public BCEnergy(IEventBus modEventBus, ModContainer modContainer) {
        INSTANCE = this;
        RegistryConfig.useOtherModConfigFor(MODID, BCCore.MODID);

        BCEnergyConfig.init();
        BCEnergyEntities.init(modEventBus);
        BCEnergyFluids.init(modEventBus);
        BCEnergyBlocks.init(modEventBus);
        BCEnergyItems.init(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::loadComplete);

        BCEnergyProxy.init(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BCEnergyRecipes.init();
            BCEnergyWorldGen.init();
        });
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            BCEnergyConfig.validateBiomeNames();
            registerMigrations();
        });
    }

    private static void registerMigrations() {
        MigrationManager.INSTANCE.addBlockMigration(BCEnergyFluids.crudeOil[0].getBlock(), "fluid_block_oil");
        MigrationManager.INSTANCE.addBlockMigration(BCEnergyFluids.fuelLight[0].getBlock(), "fluid_block_fuel");
    }

    static {
        startBatch();
        registerTag("item.glob.oil").reg("glob_of_oil").oldReg("glob_oil").locale("globOil").model("glob_oil");
        registerTag("item.block.mj_dynamo").reg("mj_dynamo").locale("mjDynamo").model("mj_dynamo");
        registerTag("block.mj_dynamo").reg("mj_dynamo").locale("mjDynamo").model("mj_dynamo");
        registerTag("tile.engine.stone").reg("engine.stone");
        registerTag("tile.engine.iron").reg("engine.iron");
        registerTag("tile.engine.rf").reg("engine.rf");
        registerTag("tile.spring.oil").reg("spring.oil");
        registerTag("tile.mj_dynamo").reg("mj_dynamo");
        endBatch(TagManager.prependTags("buildcraftenergy:", EnumTagType.REGISTRY_NAME, EnumTagType.MODEL_LOCATION)
            .andThen(TagManager.setTab("buildcraft.main")));
    }

    private static TagEntry registerTag(String id) { return TagManager.registerTag(id); }
    private static void startBatch() { TagManager.startBatch(); }
    private static void endBatch(Consumer<TagEntry> consumer) { TagManager.endBatch(consumer); }
}
