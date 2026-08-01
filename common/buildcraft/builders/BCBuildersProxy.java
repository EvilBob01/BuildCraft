/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import buildcraft.api.BCModules;

import buildcraft.lib.net.MessageManager;

import buildcraft.builders.snapshot.MessageSnapshotRequest;
import buildcraft.builders.snapshot.MessageSnapshotResponse;

public class BCBuildersProxy {
    private static final BCBuildersProxy INSTANCE = new BCBuildersProxy();

    public static BCBuildersProxy getProxy() {
        return INSTANCE;
    }

    public static void init(IEventBus modEventBus) {
        MessageManager.registerMessageClass(BCModules.BUILDERS, MessageSnapshotRequest.class, MessageSnapshotRequest.HANDLER, Dist.DEDICATED_SERVER);
        MessageManager.registerMessageClass(BCModules.BUILDERS, MessageSnapshotResponse.class, Dist.CLIENT);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientInit();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientInit() {
        // TODO (Phase 7 — rendering): stencil setup (Framebuffer → RenderTarget API change);
        //   BCBuildersConfig.enableStencil check, internalStencilCrashTest guard, and
        //   RenderTarget.enableStencil() call from old ClientProxy.fmlPreInit()
        // TODO (Phase 7 — rendering): BCBuildersSprites.fmlPreInit();
        // TODO (Phase 7 — rendering): RenderQuarry.init();
        // TODO (Phase 7 — rendering): replace ClientRegistry.bindTileEntitySpecialRenderer calls —
        //   TileArchitectTable → RenderArchitectTable
        //   TileBuilder      → RenderBuilder
        //   TileFiller       → RenderFiller
        //   TileQuarry       → RenderQuarry
        //   DetachedRenderer.INSTANCE.addRenderer(FROM_WORLD_ORIGIN, RenderArchitectTables.INSTANCE)
        MessageManager.setHandler(MessageSnapshotResponse.class, MessageSnapshotResponse.HANDLER, Dist.CLIENT);
    }
}
