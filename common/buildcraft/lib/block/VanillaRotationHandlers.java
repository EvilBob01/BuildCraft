/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.block;

import java.util.function.Predicate;

import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.blocks.ICustomRotationHandler;

import buildcraft.lib.misc.collect.OrderedEnumMap;

public class VanillaRotationHandlers {
    public static final OrderedEnumMap<Direction> ROTATE_HORIZONTAL, ROTATE_FACING, ROTATE_TORCH, ROTATE_HOPPER;

    static {
        Direction e = Direction.EAST, w = Direction.WEST;
        Direction u = Direction.UP, d = Direction.DOWN;
        Direction n = Direction.NORTH, s = Direction.SOUTH;
        ROTATE_HORIZONTAL = new OrderedEnumMap<>(Direction.class, e, s, w, n);
        ROTATE_FACING = new OrderedEnumMap<>(Direction.class, e, s, d, w, n, u);
        ROTATE_TORCH = new OrderedEnumMap<>(Direction.class, e, s, w, n, u);
        ROTATE_HOPPER = new OrderedEnumMap<>(Direction.class, e, s, w, n, d);
    }

    public static void fmlInit() {
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ButtonBlock.class, VanillaRotationHandlers::rotateButton);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(TripWireHookBlock.class, VanillaRotationHandlers::rotateTripWireHook);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(DoorBlock.class, VanillaRotationHandlers::rotateDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(PistonBaseBlock.class, VanillaRotationHandlers::rotatePiston);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ShulkerBoxBlock.class, VanillaRotationHandlers::rotateShulkerBox);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(DispenserBlock.class, getHandlerFreely(DispenserBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ObserverBlock.class, getHandlerFreely(ObserverBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(EndRodBlock.class, getHandlerFreely(EndRodBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(FenceGateBlock.class, getHandlerHorizontalFreely(FenceGateBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(DiodeBlock.class, getHandlerHorizontalFreely(DiodeBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(CarvedPumpkinBlock.class, getHandlerHorizontalFreely(CarvedPumpkinBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(GlazedTerracottaBlock.class, getHandlerHorizontalFreely(GlazedTerracottaBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(AnvilBlock.class, getHandlerHorizontalFreely(AnvilBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(EnderChestBlock.class, getHandlerHorizontalFreely(EnderChestBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(FurnaceBlock.class, getHandlerHorizontalFreely(FurnaceBlock.class));
        CustomRotationHelper.INSTANCE.registerHandlerForAll(LadderBlock.class, VanillaRotationHandlers::rotateLadder);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(HopperBlock.class, VanillaRotationHandlers::rotateHopper);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ChestBlock.class, VanillaRotationHandlers::rotateChest);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(TrapDoorBlock.class, VanillaRotationHandlers::rotateTrapDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(StairBlock.class, VanillaRotationHandlers::rotateStairs);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(BannerBlock.class, VanillaRotationHandlers::rotateStandingBanner);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(WallBannerBlock.class, VanillaRotationHandlers::rotateHangingBanner);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(WallSignBlock.class, VanillaRotationHandlers::rotateWallSign);
    }

    public static <T> int getOrdinal(T side, T[] array) {
        for (int i = 0; i < array.length; i++) {
            if (side == array[i]) return i;
        }
        return 0;
    }

    private static InteractionResult rotateDoor(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof DoorBlock) {
            BlockPos upperPos, lowerPos;
            BlockState upperState, lowerState;

            if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                upperPos = pos;
                upperState = state;
                lowerPos = upperPos.below();
                lowerState = world.getBlockState(lowerPos);
                if (!(lowerState.getBlock() instanceof DoorBlock)) {
                    return InteractionResult.PASS;
                }
            } else {
                lowerPos = pos;
                lowerState = state;
                upperPos = lowerPos.above();
                upperState = world.getBlockState(upperPos);
                if (!(upperState.getBlock() instanceof DoorBlock)) {
                    return InteractionResult.PASS;
                }
            }

            if (lowerState.getValue(DoorBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
                DoorHingeSide hinge = upperState.getValue(DoorBlock.HINGE);
                if (hinge == DoorHingeSide.LEFT) {
                    hinge = DoorHingeSide.RIGHT;
                } else {
                    hinge = DoorHingeSide.LEFT;
                }
                world.setBlock(upperPos, upperState.setValue(DoorBlock.HINGE, hinge), 3);
            }

            return rotateOnce(world, lowerPos, lowerState, DoorBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateButton(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof ButtonBlock) {
            return rotateOnce(world, pos, state, ButtonBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateTripWireHook(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof TripWireHookBlock) {
            return rotateOnce(world, pos, state, TripWireHookBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotatePiston(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof PistonBaseBlock) {
            boolean extended = state.getValue(PistonBaseBlock.EXTENDED);
            if (extended) return InteractionResult.FAIL;
            return rotateOnce(world, pos, state, DirectionalBlock.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateHopper(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof HopperBlock) {
            return rotateOnce(world, pos, state, HopperBlock.FACING, ROTATE_HOPPER);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateShulkerBox(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof ShulkerBoxBlock) {
            return rotateOnce(world, pos, state, ShulkerBoxBlock.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }

    private static ICustomRotationHandler getHandlerFreely(Class<? extends Block> blockClass) {
        return (world, pos, state, sideWrenched) -> rotateFreely(world, pos, state, blockClass);
    }

    private static InteractionResult rotateFreely(Level world, BlockPos pos, BlockState state, Class<? extends Block> blockClass) {
        if (blockClass.isInstance(state.getBlock())) {
            return rotateOnce(world, pos, state, DirectionalBlock.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }

    private static ICustomRotationHandler getHandlerHorizontalFreely(Class<? extends Block> blockClass) {
        return (world, pos, state, sideWrenched) -> rotateHorizontalFreely(world, pos, state, blockClass);
    }

    private static InteractionResult rotateHorizontalFreely(Level world, BlockPos pos, BlockState state, Class<? extends Block> blockClass) {
        if (blockClass.isInstance(state.getBlock())) {
            return rotateOnce(world, pos, state, HorizontalDirectionalBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateLadder(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof LadderBlock) {
            return rotateOnce(world, pos, state, LadderBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateChest(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof ChestBlock) {
            BlockPos otherPos = null;
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                BlockPos candidate = pos.relative(facing);
                if (world.getBlockState(candidate).getBlock() == state.getBlock()) {
                    otherPos = candidate;
                    break;
                }
            }

            if (otherPos != null) {
                BlockState otherState = world.getBlockState(otherPos);
                Direction facing = state.getValue(ChestBlock.FACING);
                if (otherState.getValue(ChestBlock.FACING) == facing) {
                    world.setBlock(pos, state.setValue(ChestBlock.FACING, facing.getOpposite()), 3);
                    world.setBlock(otherPos, otherState.setValue(ChestBlock.FACING, facing.getOpposite()), 3);
                    return InteractionResult.SUCCESS;
                }
            }

            return rotateOnce(world, pos, state, ChestBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateTrapDoor(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof TrapDoorBlock) {
            if (state.getValue(TrapDoorBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
                Half half = state.getValue(TrapDoorBlock.HALF);
                half = half == Half.TOP ? Half.BOTTOM : Half.TOP;
                state = state.setValue(TrapDoorBlock.HALF, half);
            }
            return rotateOnce(world, pos, state, TrapDoorBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateStairs(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof StairBlock) {
            if (state.getValue(StairBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
                Half half = state.getValue(StairBlock.HALF);
                half = half == Half.TOP ? Half.BOTTOM : Half.TOP;
                state = state.setValue(StairBlock.HALF, half);
            }
            return rotateOnce(world, pos, state, StairBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateHangingBanner(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof WallBannerBlock) {
            return rotateOnce(world, pos, state, WallBannerBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateWallSign(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof WallSignBlock) {
            return rotateOnce(world, pos, state, WallSignBlock.FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateStandingBanner(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (state.getBlock() instanceof BannerBlock) {
            world.setBlock(pos, state.setValue(BannerBlock.ROTATION, (state.getValue(BannerBlock.ROTATION) + 1) % 16), 3);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult rotateEnumFacing(Level world, BlockPos pos, BlockState state, Property<Direction> prop, OrderedEnumMap<Direction> possible) {
        return rotateOnce(world, pos, state, prop, possible);
    }

    public static <E extends Enum<E> & Comparable<E>> InteractionResult rotateOnce(
        Level world, BlockPos pos, BlockState state, Property<E> prop, OrderedEnumMap<E> possible
    ) {
        E current = state.getValue(prop);
        current = possible.next(current);
        world.setBlock(pos, state.setValue(prop, current), 3);
        return InteractionResult.SUCCESS;
    }

    public static <E extends Enum<E> & Comparable<E>> InteractionResult rotateAnyTypeManual(
        Level world, BlockPos pos, BlockState state, Property<E> prop, OrderedEnumMap<E> possible, Predicate<E> canPlace
    ) {
        E current = state.getValue(prop);
        for (int i = possible.getOrderLength(); i > 1; i--) {
            current = possible.next(current);
            if (canPlace.test(current)) {
                world.setBlock(pos, state.setValue(prop, current), 3);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
