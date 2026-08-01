/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import buildcraft.api.BCModules;
import buildcraft.api.transport.pipe.PipeApiClient;

import buildcraft.lib.net.MessageManager;

import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.client.render.PipeWireRenderer;
import buildcraft.transport.net.MessageMultiPipeItem;
import buildcraft.transport.wire.MessageWireSystems;
import buildcraft.transport.wire.MessageWireSystemsPowered;

public class BCTransportProxy {
    private static final BCTransportProxy INSTANCE = new BCTransportProxy();

    public static BCTransportProxy getProxy() {
        return INSTANCE;
    }

    public static void init(IEventBus modEventBus) {
        MessageManager.registerMessageClass(BCModules.TRANSPORT, MessageWireSystems.class, Dist.CLIENT);
        MessageManager.registerMessageClass(BCModules.TRANSPORT, MessageWireSystemsPowered.class, Dist.CLIENT);
        MessageManager.registerMessageClass(BCModules.TRANSPORT, MessageMultiPipeItem.class, Dist.CLIENT);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientInit();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientInit() {
        // TODO (Phase 7 — rendering): BCTransportSprites.fmlPreInit();
        // TODO (Phase 7 — rendering): BCTransportModels.fmlPreInit(); BCTransportModels.fmlInit(); BCTransportModels.fmlPostInit();
        PipeApiClient.registry = PipeRegistryClient.INSTANCE;
        PipeWireRenderer.init();
        MessageManager.setHandler(MessageWireSystems.class, MessageWireSystems.HANDLER, Dist.CLIENT);
        MessageManager.setHandler(MessageWireSystemsPowered.class, MessageWireSystemsPowered.HANDLER, Dist.CLIENT);
        MessageManager.setHandler(MessageMultiPipeItem.class, MessageMultiPipeItem.HANDLER, Dist.CLIENT);
    }
}
