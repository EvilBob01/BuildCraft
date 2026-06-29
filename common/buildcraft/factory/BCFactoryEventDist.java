/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;

public enum BCFactoryEventDist {
    INSTANCE;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void textureStitchPost(TextureStitchEvent.Post event) {
        RenderPump.textureStitchPost();
        RenderMiningWell.textureStitchPost();
    }
}
