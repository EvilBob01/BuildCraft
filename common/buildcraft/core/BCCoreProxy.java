/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import java.util.List;

import net.minecraft.world.level.Level;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

import buildcraft.api.BCModules;

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.DetachedRenderer.RenderMatrixType;
import buildcraft.lib.net.MessageManager;

import buildcraft.core.client.render.RenderVolumeBoxes;
import buildcraft.core.list.ListTooltipHandler;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

/** Replaces the old {@code @SidedProxy}-based split. See {@code BCLibProxy} for the migration notes. */
public class BCCoreProxy {
    private static final BCCoreProxy INSTANCE = new BCCoreProxy();

    public static BCCoreProxy getProxy() {
        return INSTANCE;
    }

    public static void init(IEventBus modEventBus) {
        MessageManager.registerMessageClass(BCModules.CORE, MessageVolumeBoxes.class, Dist.CLIENT);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            clientInit();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientInit() {
        // TODO (Phase 7 — rendering): BCCoreSprites.fmlPreInit(); BCCoreModels.fmlPreInit();
        DetachedRenderer.INSTANCE.addRenderer(RenderMatrixType.FROM_WORLD_ORIGIN, RenderVolumeBoxes.INSTANCE);
        NeoForge.EVENT_BUS.register(ListTooltipHandler.INSTANCE);
        MessageManager.setHandler(MessageVolumeBoxes.class, MessageVolumeBoxes.HANDLER, Dist.CLIENT);
        // TODO (Phase 7 — rendering): BCCoreModels.fmlInit(); NeoForge.EVENT_BUS.register(RenderTickListener.class);
    }

    public List<VolumeBox> getVolumeBoxes(Level world) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return getVolumeBoxesClient(world);
        }
        return WorldSavedDataVolumeBoxes.get(world).volumeBoxes;
    }

    @OnlyIn(Dist.CLIENT)
    private static List<VolumeBox> getVolumeBoxesClient(Level world) {
        return world.isClientSide ? ClientVolumeBoxes.INSTANCE.volumeBoxes
            : WorldSavedDataVolumeBoxes.get(world).volumeBoxes;
    }
}
