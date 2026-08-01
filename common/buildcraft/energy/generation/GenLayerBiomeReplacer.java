/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

// TODO (Phase 8 — World Gen): GenLayer and IntCache were removed in 1.13. Biome placement is now handled by
// BiomeSource in the data-driven world gen pipeline (ConfiguredFeature / PlacedFeature). This stub preserves the
// class name so subclass references compile; the actual replacement algorithm needs a NoiseBasedBiomeSource rewrite.
public abstract class GenLayerBiomeReplacer {
    public static final int OFFSET_RANGE = 500000;
}
