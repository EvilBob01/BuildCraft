/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

public class BCEnergySprites {
    public static void fmlPreInit() {
        NeoForge.EVENT_BUS.register(BCEnergySprites.class);
    }

    @SubscribeEvent
    public static void onTextureStitchPre(TextureAtlasStitchedEvent event) {
        // TODO Phase 7: custom fluid sprite stitching (AtlasSpriteFluid) needs porting to the
        // 1.21 sprite-source pipeline; TextureMap.registerSprite/setTextureEntry no longer exist.
    }
}
