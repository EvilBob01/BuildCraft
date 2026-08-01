/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public enum BCFactoryEventDist {
    INSTANCE;

    // TODO (Phase 7 — rendering): textureStitchPost (TextureStitchEvent → NeoForge atlas stitch event)
    // RenderPump.textureStitchPost() and RenderMiningWell.textureStitchPost() should be called after atlas rebuild.
}
