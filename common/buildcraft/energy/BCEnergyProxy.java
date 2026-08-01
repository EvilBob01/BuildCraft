/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import buildcraft.energy.event.ChristmasHandler;

public class BCEnergyProxy {
    private static final BCEnergyProxy INSTANCE = new BCEnergyProxy();

    public static BCEnergyProxy getProxy() {
        return INSTANCE;
    }

    public static void init(IEventBus modEventBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientInit();
        } else {
            serverInit();
        }
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    private static void serverInit() {
        ChristmasHandler.fmlPreInitDedicatedServer();
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientInit() {
        ChristmasHandler.fmlPreInitClient();
        // TODO (Phase 7 — rendering): BCEnergyModels.fmlPreInit();
        // TODO (Phase 7 — rendering): BCEnergySprites.fmlPreInit();
        // TODO (Phase 7 — rendering): ClientRegistry.bindTileEntitySpecialRenderer(TileEngineStone_BC8.class, RenderEngineStone.INSTANCE);
        // TODO (Phase 7 — rendering): ClientRegistry.bindTileEntitySpecialRenderer(TileEngineIron_BC8.class, RenderEngineIron.INSTANCE);
        // TODO (Phase 7 — rendering): ClientRegistry.bindTileEntitySpecialRenderer(TileEngineRF.class, RenderEngineRF.INSTANCE);
        // TODO (Phase 7 — rendering): ClientRegistry.bindTileEntitySpecialRenderer(TileDynamoMJ.class, RenderDynamoMJ.INSTANCE);
    }
}
