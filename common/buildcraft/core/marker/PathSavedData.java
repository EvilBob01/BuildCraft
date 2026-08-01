/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.marker;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import buildcraft.lib.marker.MarkerSavedData;

public class PathSavedData extends MarkerSavedData<PathSubCache, PathConnection> {
    public static final String NAME = "buildcraft_marker_path";

    public PathSavedData() {}

    public static PathSavedData load(CompoundTag nbt, HolderLookup.Provider registries) {
        PathSavedData instance = new PathSavedData();
        instance.loadFromNBT(nbt);
        return instance;
    }

    public static SavedData.Factory<PathSavedData> factory() {
        return new SavedData.Factory<>(PathSavedData::new, PathSavedData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public void loadInto(PathSubCache subCache) {
        setCache(subCache);
        for (BlockPos p : markerPositions) {
            subCache.loadMarker(p, null);
        }
        for (List<BlockPos> list : markerConnections) {
            subCache.addConnection(new PathConnection(subCache, list));
        }
    }
}
