/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;

public class SoundUtil {
    public static void playBlockPlace(Level world, BlockPos pos) {
        playBlockPlace(world, pos, world.getBlockState(pos));
    }

    public static void playBlockPlace(Level world, BlockPos pos, BlockState state) {
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, null);
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playBlockBreak(Level world, BlockPos pos) {
        playBlockBreak(world, pos, world.getBlockState(pos));
    }

    public static void playBlockBreak(Level world, BlockPos pos, BlockState state) {
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, null);
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundType.getBreakSound(), SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playLeverSwitch(Level world, BlockPos pos, boolean isNowOn) {
        float pitch = isNowOn ? 0.6f : 0.5f;
        SoundEvent soundEvent = SoundEvents.BLOCK_LEVER_CLICK;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 0.2f, pitch);
    }

    public static void playChangeColour(Level world, BlockPos pos, @Nullable DyeColor colour) {
        SoundType soundType = SoundType.SLIME;
        final SoundEvent soundEvent;
        if (colour == null) {
            soundEvent = SoundEvents.ITEM_BUCKET_EMPTY;
        } else {
            // FIXME: is this a good sound? Idk tbh.
            // TODO: Look into configuring this kind of stuff.
            soundEvent = SoundEvents.ENTITY_SLIME_SQUISH;
        }
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playSlideSound(Level world, BlockPos pos) {
        playSlideSound(world, pos, world.getBlockState(pos));
    }

    public static void playSlideSound(Level world, BlockPos pos, InteractionResult result) {
        playSlideSound(world, pos, world.getBlockState(pos), result);
    }

    public static void playSlideSound(Level world, BlockPos pos, BlockState state) {
        playSlideSound(world, pos, state, InteractionResult.SUCCESS);
    }

    public static void playSlideSound(Level world, BlockPos pos, BlockState state, InteractionResult result) {
        if (result == InteractionResult.PASS) return;
        SoundType soundType = state.getBlock().getSoundType(state, world, pos, null);
        SoundEvent event;
        if (result == InteractionResult.SUCCESS) {
            event = SoundEvents.BLOCK_PISTON_CONTRACT;
        } else {
            event = SoundEvents.BLOCK_PISTON_EXTEND;
        }
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, event, SoundCategory.BLOCKS, volume, pitch);
    }

    public static void playBucketEmpty(Level world, BlockPos pos, FluidStack moved) {
        SoundEvent sound = moved.getFluid().getEmptySound(moved);
        world.playSound(null, pos, sound, SoundCategory.PLAYERS, 1, 1);
    }

    public static void playBucketFill(Level world, BlockPos pos, FluidStack moved) {
        SoundEvent sound = moved.getFluid().getFillSound(moved);
        world.playSound(null, pos, sound, SoundCategory.PLAYERS, 1, 1);
    }
}
