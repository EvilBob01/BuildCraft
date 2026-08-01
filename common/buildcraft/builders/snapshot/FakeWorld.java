/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.schematics.ISchematicBlock;

// TODO (Phase 7 — Rendering): FakeWorld was a 1.12 Level subclass used for client-side snapshot preview rendering.
// The Level constructor and all abstract methods have changed significantly since 1.13. This stub compiles but
// cannot be instantiated at runtime — needs a proper 1.21 ClientLevel-based implementation.
@SuppressWarnings({"NullableProblems", "ConstantConditions"})
@OnlyIn(Dist.CLIENT)
public class FakeWorld extends Level {
    public static final BlockPos BLUEPRINT_OFFSET = new BlockPos(0, 127, 0);

    private final FakeChunkProvider fakeChunkProvider;

    // TODO (Phase 7): Level constructor in 1.21 requires WritableLevelData, ResourceKey<Level>,
    // RegistryAccess, Holder<DimensionType>, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChain.
    // Replace the null/0 placeholders once the full registry system is wired up.
    @SuppressWarnings("DataFlowIssue")
    public FakeWorld() {
        super(
            (WritableLevelData) null,   // TODO Phase 7
            Level.OVERWORLD,
            (RegistryAccess) null,      // TODO Phase 7
            (Holder<DimensionType>) null, // TODO Phase 7
            true,   // isClientSide
            false,  // isDebug
            0L,     // biomeZoomSeed
            1000    // maxChainedNeighborUpdates
        );
        this.fakeChunkProvider = new FakeChunkProvider(this);
    }

    public void clear() {
        fakeChunkProvider.chunks.clear();
    }

    public void uploadSnapshot(Snapshot snapshot) {
        for (int z = 0; z < snapshot.size.getZ(); z++) {
            for (int y = 0; y < snapshot.size.getY(); y++) {
                for (int x = 0; x < snapshot.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).offset(BLUEPRINT_OFFSET.getX(), BLUEPRINT_OFFSET.getY(), BLUEPRINT_OFFSET.getZ());
                    if (snapshot instanceof Blueprint) {
                        ISchematicBlock schematicBlock = ((Blueprint) snapshot).palette
                            .get(((Blueprint) snapshot).data[snapshot.posToIndex(x, y, z)]);
                        if (!schematicBlock.isAir()) {
                            schematicBlock.buildWithoutChecks(this, pos);
                        }
                    }
                    if (snapshot instanceof Template) {
                        if (((Template) snapshot).data.get(snapshot.posToIndex(x, y, z))) {
                            setBlock(pos, Blocks.QUARTZ_BLOCK.defaultBlockState(), 0);
                        }
                    }
                }
            }
        }
        if (snapshot instanceof Blueprint) {
            ((Blueprint) snapshot).entities.forEach(schematicEntity ->
                schematicEntity.buildWithoutChecks(this, BLUEPRINT_OFFSET)
            );
        }
    }

    @Override
    public ChunkSource getChunkSource() {
        return fakeChunkProvider;
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return 1.0f;
    }

    @Override
    public LevelTickAccess<net.minecraft.world.level.block.Block> getBlockTicks() {
        return LevelTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return LevelTickAccess.emptyLevelList();
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

    @Override
    public void setSpawnSettings(boolean hostile, boolean peaceful) {}

    @Override
    public void playSeededSound(@Nullable Player player, double x, double y, double z,
        Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {}

    @Override
    public void playSeededSound(@Nullable Player player, Entity entity,
        Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {}

    @Override
    public String gatherChunkSourceStats() {
        return "fake";
    }

    @Nullable
    @Override
    public Entity getEntity(int id) {
        return null;
    }

    @Nullable
    @Override
    public MapItemSavedData getMapData(MapId id) {
        return null;
    }

    @Override
    public void setMapData(MapId id, MapItemSavedData data) {}

    @Override
    public MapId getFreeMapId() {
        return new MapId(0);
    }

    @Override
    public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

    @Override
    public Scoreboard getScoreboard() {
        return new Scoreboard();
    }

    @Nullable
    @Override
    public RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    public LevelEntityGetter<Entity> getEntities() {
        return null;
    }
}
