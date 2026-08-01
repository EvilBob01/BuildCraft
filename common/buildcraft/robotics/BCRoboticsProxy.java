/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import buildcraft.api.BCModules;

import buildcraft.lib.net.MessageManager;

import buildcraft.robotics.zone.MessageZoneMapRequest;
import buildcraft.robotics.zone.MessageZoneMapResponse;

public class BCRoboticsProxy {
    private static final BCRoboticsProxy INSTANCE = new BCRoboticsProxy();

    public static BCRoboticsProxy getProxy() {
        return INSTANCE;
    }

    public static void init(IEventBus modEventBus) {
        MessageManager.registerMessageClass(BCModules.ROBOTICS, MessageZoneMapRequest.class, MessageZoneMapRequest.HANDLER, Dist.DEDICATED_SERVER);
        MessageManager.registerMessageClass(BCModules.ROBOTICS, MessageZoneMapResponse.class, Dist.CLIENT);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientInit();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientInit() {
        MessageManager.setHandler(MessageZoneMapResponse.class, MessageZoneMapResponse.HANDLER, Dist.CLIENT);
        // TODO (Phase 7 — rendering): ClientRegistry.bindTileEntitySpecialRenderer(TileZonePlanner.class, new RenderZonePlanner());
    }
}
