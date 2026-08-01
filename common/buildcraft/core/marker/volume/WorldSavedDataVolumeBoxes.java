/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.server.level.ServerLevel;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.MessageManager;

public class WorldSavedDataVolumeBoxes extends SavedData {
    private static final String DATA_NAME = "buildcraft_volume_boxes";
    public final Level world;
    public final List<VolumeBox> volumeBoxes = new ArrayList<>();

    public WorldSavedDataVolumeBoxes(Level world) {
        this.level = world;
    }

    public static SavedData.Factory<WorldSavedDataVolumeBoxes> factory(Level world) {
        return new SavedData.Factory<>(
            () -> new WorldSavedDataVolumeBoxes(world),
            (nbt, registries) -> WorldSavedDataVolumeBoxes.load(nbt, registries, world),
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
        );
    }

    public static WorldSavedDataVolumeBoxes load(CompoundTag nbt, HolderLookup.Provider registries, Level world) {
        WorldSavedDataVolumeBoxes instance = new WorldSavedDataVolumeBoxes(world);
        instance.volumeBoxes.clear();
        NBTUtilBC.readCompoundList(nbt.get("volumeBoxes"))
            .map(volumeBoxTag -> new VolumeBox(world, volumeBoxTag))
            .forEach(instance.volumeBoxes::add);
        return instance;
    }

    public VolumeBox getVolumeBoxAt(BlockPos pos) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.box.contains(pos)).findFirst().orElse(null);
    }

    public void addVolumeBox(BlockPos pos) {
        volumeBoxes.add(new VolumeBox(world, pos));
    }

    public VolumeBox getVolumeBoxFromId(UUID id) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.id.equals(id)).findFirst().orElse(null);
    }

    public VolumeBox getCurrentEditing(Player player) {
        return volumeBoxes.stream().filter(volumeBox -> volumeBox.isEditingBy(player)).findFirst().orElse(null);
    }

    public void tick() {
        AtomicBoolean dirty = new AtomicBoolean(false);
        volumeBoxes.stream().filter(VolumeBox::isEditing).forEach(volumeBox -> {
            Player player = volumeBox.getPlayer(world);
            if (player == null) {
                volumeBox.pauseEditing();
                dirty.set(true);
            } else {
                AABB oldAabb = volumeBox.box.getBoundingBox();
                volumeBox.box.reset();
                volumeBox.box.extendToEncompass(volumeBox.getHeld());
                BlockPos lookingAt = new BlockPos(
                    player.position()
                        .add(0, player.getEyeHeight(), 0)
                        .add(player.getLookAngle().scale(volumeBox.getDist()))
                );
                volumeBox.box.extendToEncompass(lookingAt);
                if (!volumeBox.box.getBoundingBox().equals(oldAabb)) {
                    dirty.set(true);
                }
            }
        });
        for (VolumeBox volumeBox : volumeBoxes) {
            List<Lock> locksToRemove = new ArrayList<>(volumeBox.locks).stream()
                .filter(lock -> !lock.cause.stillWorks(world))
                .collect(Collectors.toList());
            if (!locksToRemove.isEmpty()) {
                volumeBox.locks.removeAll(locksToRemove);
                dirty.set(true);
            }
        }
        if (dirty.get()) {
            setDirty();
        }
    }

    @Override
    public void setDirty() {
        super.setDirty();
        MessageManager.sendToDimension(new MessageVolumeBoxes(volumeBoxes), world.dimension().location().hashCode());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.put("volumeBoxes", NBTUtilBC.writeCompoundList(volumeBoxes.stream().map(VolumeBox::writeToNBT)));
        return nbt;
    }

    public static WorldSavedDataVolumeBoxes get(Level world) {
        if (world.isClientSide) {
            throw new IllegalArgumentException("Tried to create a world saved data instance on the client!");
        }
        return ((ServerLevel) world).getDataStorage().computeIfAbsent(factory(world), DATA_NAME);
    }
}
