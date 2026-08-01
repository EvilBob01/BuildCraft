/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

public class BCFactoryProxy {
    private static final BCFactoryProxy INSTANCE = new BCFactoryProxy();

    public static BCFactoryProxy getProxy() {
        return INSTANCE;
    }

    public static void init(IEventBus modEventBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientInit();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientInit() {
        // TODO (Phase 7 — rendering): RenderPump.init(); RenderMiningWell.init();
        // TODO (Phase 7 — rendering): BCFactoryModels.fmlPreInit(); BCFactoryModels.fmlInit();
    }
}
