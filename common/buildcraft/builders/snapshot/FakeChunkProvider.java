/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

// TODO (Phase 7 — Rendering): ChunkSource is abstract and needs a LevelLightEngine; this stub provides
// enough for FakeWorld block placement but will NPE if lighting is accessed.
public class FakeChunkProvider extends ChunkSource {
    private final FakeWorld world;
    public final Map<ChunkPos, LevelChunk> chunks = new HashMap<>();

    public FakeChunkProvider(FakeWorld world) {
        this.world = world;
    }

    @Nullable
    @Override
    public LevelChunk getChunk(int x, int z, ChunkStatus status, boolean create) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        return chunks.computeIfAbsent(chunkPos, k -> new LevelChunk(world, k));
    }

    @Nullable
    @Override
    public LevelChunk getChunkNow(int x, int z) {
        return chunks.get(new ChunkPos(x, z));
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return null; // TODO Phase 7
    }

    @Override
    public String gatherStats() {
        return "fake";
    }

    @Override
    public int getLoadedChunksCount() {
        return chunks.size();
    }

    @Override
    public void tick(java.util.function.BooleanSupplier hasTimeLeft, boolean tickChunks) {}

    @Override
    public net.minecraft.world.level.chunk.LightChunkGetter getLightChunkGetter() {
        return null; // TODO Phase 7
    }
}
