/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import net.minecraft.world.level.biome.Biome;

// TODO (Phase 8 — World Gen): 1.12 biome subclassing (BiomeOcean, BiomeProperties, setRegistryName) was removed in
// 1.13. Oil biomes must be registered as data-driven JSON biomes in the data/buildcraftenergy/worldgen/biome/ folder.
// The INSTANCE field below is a placeholder; wire it up via DeferredRegister<Biome> in Phase 10.
public final class BiomeOilOcean {
    public static final Biome INSTANCE = null;
}
