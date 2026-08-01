/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidStack;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;

import buildcraft.lib.misc.BlockUtil;

public class SchematicBlockFluid implements ISchematicBlock {
    private BlockState blockState;
    private boolean isFlowing;

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        return BlockUtil.getFluidWithFlowing(context.world, context.pos) != null &&
            (BlockUtil.getFluid(context.world, context.pos) == null ||
                BlockUtil.getFluidWithoutFlowing(context.world.getBlockState(context.pos)) != null);
    }

    @Override
    public void init(SchematicBlockContext context) {
        blockState = context.blockState;
        isFlowing = BlockUtil.getFluid(context.world, context.pos) == null;
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return Stream.concat(Arrays.stream(Direction.HORIZONTALS), Stream.of(Direction.DOWN))
            .map(Direction::getDirectionVec)
            .map(BlockPos::new)
            .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids() {
        return Optional.ofNullable(BlockUtil.getFluidWithoutFlowing(blockState))
            .map(fluid -> new FluidStack(fluid, Fluid.BUCKET_VOLUME))
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList);
    }

    @Override
    public SchematicBlockFluid getRotated(Rotation rotation) {
        SchematicBlockFluid schematicBlock = SchematicBlockManager.createCleanCopy(this);
        schematicBlock.blockState = blockState;
        schematicBlock.isFlowing = isFlowing;
        return schematicBlock;
    }

    @Override
    public boolean canBuild(Level world, BlockPos blockPos) {
        return world.isEmptyBlock(blockPos) ||
            BlockUtil.getFluidWithFlowing(world, blockPos) == BlockUtil.getFluidWithFlowing(blockState.getBlock()) &&
                BlockUtil.getFluid(world, blockPos) == null;
    }

    @Override
    public boolean build(Level world, BlockPos blockPos) {
        if (isFlowing) {
            return true;
        }
        if (world.setBlock(blockPos, blockState, 11)) {
            Stream.concat(
                Stream.of(Direction.VALUES)
                    .map(Direction::getDirectionVec)
                    .map(BlockPos::new),
                Stream.of(BlockPos.ORIGIN)
            )
                .map(blockPos::add)
                .forEach(updatePos -> world.notifyNeighborsOfStateChange(updatePos, blockState.getBlock(), false));
            return true;
        }
        return false;
    }

    @Override
    public boolean buildWithoutChecks(Level world, BlockPos blockPos) {
        return world.setBlock(blockPos, blockState, 0);
    }

    @Override
    public boolean isBuilt(Level world, BlockPos blockPos) {
        return isFlowing || BlockUtil.blockStatesEqual(blockState, world.getBlockState(blockPos));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("blockState", NbtUtils.writeBlockState(new CompoundTag(), blockState));
        nbt.putBoolean("isFlowing", isFlowing);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        blockState = NbtUtils.readBlockState(nbt.getCompound("blockState"));
        isFlowing = nbt.getBoolean("isFlowing");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SchematicBlockFluid that = (SchematicBlockFluid) o;

        return isFlowing == that.isFlowing && blockState.equals(that.blockState);
    }

    @Override
    public int hashCode() {
        int result = blockState.hashCode();
        result = 31 * result + (isFlowing ? 1 : 0);
        return result;
    }
}
