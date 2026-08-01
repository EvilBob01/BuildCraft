/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;


import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.compat.CompatManager;
import buildcraft.lib.inventory.TransactorEntityItem;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.world.SingleBlockAccess;

import buildcraft.core.BCCoreConfig;

public final class BlockUtil {

    /** @return A list of itemstacks that are dropped from the block, or null if the block is air */
    @Nullable
    public static NonNullList<ItemStack> getItemStackFromBlock(ServerLevel world, BlockPos pos, GameProfile owner) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return null;
        }

        List<ItemStack> drops = Block.getDrops(state, world, pos, world.getBlockEntity(pos));
        Player fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner, pos);

        NonNullList<ItemStack> returnList = NonNullList.create();
        for (ItemStack s : drops) {
            if (world.getRandom().nextFloat() <= 1.0F) {
                returnList.add(s);
            }
        }

        return returnList;
    }

    public static boolean breakBlock(ServerLevel world, BlockPos pos, BlockPos ownerPos, GameProfile owner) {
        return breakBlock(world, pos, BCLibConfig.itemLifespan * 20, ownerPos, owner);
    }

    public static boolean breakBlock(ServerLevel world, BlockPos pos, int forcedLifespan, BlockPos ownerPos,
        GameProfile owner) {
        NonNullList<ItemStack> items = NonNullList.create();

        if (breakBlock(world, pos, items, ownerPos, owner)) {
            for (ItemStack item : items) {
                dropItem(world, pos, forcedLifespan, item);
            }
            return true;
        }
        return false;
    }

    public static boolean harvestBlock(ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer fakePlayer = getFakePlayerWithTool(world, tool, owner);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        NeoForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        BlockState state = world.getBlockState(pos);

        if (!state.canHarvestBlock(world, pos, fakePlayer)) {
            return false;
        }

        state.getBlock().playerWillDestroy(world, pos, state, fakePlayer);
        state.getBlock().playerDestroy(world, fakePlayer, pos, state, world.getBlockEntity(pos), tool);
        // Don't drop items as we do that ourselves
        world.destroyBlock(pos, /* dropBlock = */ false);

        return true;
    }

    public static boolean destroyBlock(ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer fakePlayer = getFakePlayerWithTool(world, tool, owner);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        NeoForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        world.destroyBlock(pos, true);

        return true;
    }

    public static FakePlayer getFakePlayerWithTool(ServerLevel world, @Nonnull ItemStack tool, GameProfile owner) {
        FakePlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner);
        int i = 0;

        while (player.getMainHandItem() != tool && i < 9) {
            if (i > 0) {
                player.getInventory().setItem(i - 1, StackUtil.EMPTY);
            }

            player.getInventory().setItem(i, tool);
            i++;
        }

        return player;
    }

    public static boolean breakBlock(ServerLevel world, BlockPos pos, NonNullList<ItemStack> drops, BlockPos ownerPos,
        GameProfile owner) {
        FakePlayer fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(world, owner, ownerPos);
        BreakEvent breakEvent = new BreakEvent(world, pos, world.getBlockState(pos), fakePlayer);
        NeoForge.EVENT_BUS.post(breakEvent);

        if (breakEvent.isCanceled()) {
            return false;
        }

        if (!world.isEmptyBlock(pos) && !world.isClientSide && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            drops.addAll(getItemStackFromBlock(world, pos, owner));
        }
        world.removeBlock(pos, false);

        return true;
    }

    public static void dropItem(ServerLevel world, BlockPos pos, int forcedLifespan, ItemStack stack) {
        float var = 0.7F;
        double dx = world.getRandom().nextFloat() * var + (1.0F - var) * 0.5D;
        double dy = world.getRandom().nextFloat() * var + (1.0F - var) * 0.5D;
        double dz = world.getRandom().nextFloat() * var + (1.0F - var) * 0.5D;
        ItemEntity entityitem = new ItemEntity(world, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack);

        entityitem.age = -forcedLifespan;
        entityitem.setDefaultPickUpDelay();

        world.addFreshEntity(entityitem);
    }

    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerLevel world, BlockPos pos,
        @Nonnull ItemStack tool, GameProfile owner) {
        return breakBlockAndGetDrops(world, pos, tool, owner, false);
    }

    /** @param grabAll If true then this will pickup every item in range of the position, false to only get the items
     *            that the dropped while breaking the block. */
    public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerLevel world, BlockPos pos,
        @Nonnull ItemStack tool, GameProfile owner, boolean grabAll) {
        AABB aabb = new AABB(pos).inflate(1);
        Set<Entity> entities;
        if (grabAll) {
            entities = Collections.emptySet();
        } else {
            entities = Sets.newIdentityHashSet();
            entities.addAll(world.getEntitiesOfClass(ItemEntity.class, aabb));
        }
        if (!harvestBlock(world, pos, tool, owner)) {
            if (!destroyBlock(world, pos, tool, owner)) {
                return Optional.empty();
            }
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemEntity entity : world.getEntitiesOfClass(ItemEntity.class, aabb)) {
            if (entities.contains(entity)) {
                continue;
            }
            TransactorEntityItem transactor = new TransactorEntityItem(entity);
            ItemStack stack;
            while (!(stack = transactor.extract(StackFilter.ALL, 0, Integer.MAX_VALUE, false)).isEmpty()) {
                stacks.add(stack);
            }
        }
        return Optional.of(stacks);
    }

    public static boolean canChangeBlock(Level world, BlockPos pos, GameProfile owner) {
        return canChangeBlock(world.getBlockState(pos), world, pos, owner);
    }

    public static boolean canChangeBlock(BlockState state, Level world, BlockPos pos, GameProfile owner) {
        if (state == null) return true;

        if (state.isAir()) {
            return true;
        }

        if (isUnbreakableBlock(world, pos, state, owner)) {
            return false;
        }

        if (state.getBlock() == Blocks.LAVA) {
            return false;
        }
        // TODO (Phase 9 - fluids): check for dense fluids using FluidState

        return true;
    }

    public static float getBlockHardnessMining(Level world, BlockPos pos, BlockState state, GameProfile owner) {
        if (world instanceof ServerLevel) {
            Player fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerLevel) world, owner);
            float relativeHardness = state.getDestroyProgress(fakePlayer, world, pos);
            if (relativeHardness <= 0.0F) {
                return -1.0F;
            }
        }
        return state.getDestroySpeed(world, pos);
    }

    public static boolean isUnbreakableBlock(Level world, BlockPos pos, BlockState state, GameProfile owner) {
        return getBlockHardnessMining(world, pos, state, owner) < 0;
    }

    public static boolean isUnbreakableBlock(Level world, BlockPos pos, GameProfile owner) {
        return isUnbreakableBlock(world, pos, world.getBlockState(pos), owner);
    }

    /** Returns true if a block cannot be harvested without a tool. */
    public static boolean isToughBlock(Level world, BlockPos pos) {
        // TODO: getMaterial().isToolNotRequired() removed - use requiresCorrectToolForDrops
        return world.getBlockState(pos).requiresCorrectToolForDrops();
    }

    public static boolean isFullFluidBlock(Level world, BlockPos pos) {
        return isFullFluidBlock(world.getBlockState(pos), world, pos);
    }

    public static boolean isFullFluidBlock(BlockState state, Level world, BlockPos pos) {
        // TODO (Phase 9 - fluids): Reimplement using FluidState
        if (!state.getFluidState().isEmpty()) {
            FluidStack fluid = drainBlock(world, pos, false);
            return fluid == null || fluid.getAmount() > 0;
        }
        return false;
    }

    public static Fluid getFluid(Level world, BlockPos pos) {
        FluidStack fluid = drainBlock(world, pos, false);
        return fluid != null ? fluid.getFluid() : null;
    }

    public static Fluid getFluidWithFlowing(Level world, BlockPos pos) {
        // TODO (Phase 9 - fluids): Use FluidState
        return getFluid(world, pos);
    }

    public static Fluid getFluid(Block block) {
        // TODO (Phase 9 - fluids): FluidRegistry removed; use block's fluid state
        return null;
    }

    public static Fluid getFluidWithoutFlowing(BlockState state) {
        // TODO (Phase 9 - fluids): FluidRegistry/BlockFluidClassic removed; use FluidState
        if (state.getBlock() instanceof LiquidBlock) {
            if (state.getFluidState().isSource()) {
                Fluid f = state.getFluidState().getType();
                if (f != Fluids.EMPTY) return f;
            }
        }
        return null;
    }

    public static Fluid getFluidWithFlowing(Block block) {
        // TODO (Phase 9 - fluids): FluidRegistry/BlockFluidBase removed; use FluidState
        if (block == Blocks.LAVA) return Fluids.LAVA;
        if (block == Blocks.WATER) return Fluids.WATER;
        return null;
    }

    public static FluidStack drainBlock(Level world, BlockPos pos, boolean doDrain) {
        IFluidHandler handler = FluidUtil.getFluidHandler(world, pos, null).orElse(null);
        if (handler != null) {
            return handler.drain(FluidType.BUCKET_VOLUME, doDrain ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
        } else {
            return null;
        }
    }

    /** Create an explosion which only affects a single block. */
    public static void explodeBlock(Level world, BlockPos pos) {
        // TODO: SPacketExplosion removed; server-side effect packets are handled differently in 1.21
        if (world.isClientSide) {
            return;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        world.explode(null, x, y, z, 3f, net.minecraft.world.level.Explosion.BlockInteraction.DESTROY_WITH_DECAY);
    }

    public static long computeBlockBreakPower(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        float hardness = state.getDestroySpeed(world, pos);
        return (long) Math.floor(16 * MjAPI.MJ * ((hardness + 1) * 2) * BCCoreConfig.miningMultiplier);
    }

    /** The following functions let you avoid unnecessary chunk loads, which is nice. */
    public static BlockEntity getTileEntity(Level world, BlockPos pos) {
        return getTileEntity(world, pos, false);
    }

    public static BlockEntity getTileEntity(Level world, BlockPos pos, boolean force) {
        return CompatManager.getTile(world, pos, force);
    }

    public static BlockState getBlockState(Level world, BlockPos pos) {
        return getBlockState(world, pos, false);
    }

    public static BlockState getBlockState(Level world, BlockPos pos, boolean force) {
        return CompatManager.getState(world, pos, force);
    }

    public static boolean useItemOnBlock(Level world, Player player, ItemStack stack, BlockPos pos,
        Direction direction) {
        // TODO (Phase 6 - items): Item.onItemUseFirst/onItemUse removed; use ItemStack.useOn(UseOnContext)
        return false;
    }

    public static void onComparatorUpdate(Level world, BlockPos pos, Block block) {
        world.updateComparatorOutputLevel(pos, block);
    }

    @Nullable
    public static ChestBlockEntity getOtherDoubleChest(BlockEntity inv) {
        // TODO: Adjacent chest fields removed; use ChestBlock.getConnectedChest() or similar
        return null;
    }

    public static <T extends Comparable<T>> BlockState copyProperty(Property<T> property, BlockState dst,
        BlockState src) {
        return dst.getProperties().contains(property) ? dst.setValue(property, src.getValue(property)) : dst;
    }

    public static <T extends Comparable<T>> int compareProperty(Property<T> property, BlockState a, BlockState b) {
        return a.getValue(property).compareTo(b.getValue(property));
    }

    public static <T extends Comparable<T>> String getPropertyStringValue(BlockState blockState,
        Property<T> property) {
        return property.getName(blockState.getValue(property));
    }

    public static Map<String, String> getPropertiesStringMap(BlockState blockState,
        Collection<Property<?>> properties) {
        ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<>();
        for (Property<?> property : properties) {
            mapBuilder.put(property.getName(), getPropertyStringValue(blockState, property));
        }
        return mapBuilder.build();
    }

    public static Map<String, String> getPropertiesStringMap(BlockState blockState) {
        return getPropertiesStringMap(blockState, blockState.getProperties());
    }

    public static Comparator<BlockState> blockStateComparator() {
        return (blockStateA, blockStateB) -> {
            Block blockA = blockStateA.getBlock();
            Block blockB = blockStateB.getBlock();
            if (blockA != blockB) {
                return blockA.builtInRegistryHolder().key().location().toString().compareTo(blockB.builtInRegistryHolder().key().location().toString());
            }
            for (Property<?> property : Sets.intersection(new HashSet<>(blockStateA.getProperties()),
                new HashSet<>(blockStateB.getProperties()))) {
                int compareResult = BlockUtil.compareProperty(property, blockStateA, blockStateB);
                if (compareResult != 0) {
                    return compareResult;
                }
            }
            return 0;
        };
    }

    public static boolean blockStatesWithoutBlockEqual(BlockState a, BlockState b,
        Collection<Property<?>> ignoredProperties) {
        return Sets.intersection(new HashSet<>(a.getProperties()), new HashSet<>(b.getProperties())).stream()
            .filter(property -> !ignoredProperties.contains(property))
            .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static boolean blockStatesWithoutBlockEqual(BlockState a, BlockState b) {
        return Sets.intersection(new HashSet<>(a.getProperties()), new HashSet<>(b.getProperties())).stream()
            .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static boolean blockStatesEqual(BlockState a, BlockState b, Collection<Property<?>> ignoredProperties) {
        return a.getBlock() == b.getBlock()
            && Sets.intersection(new HashSet<>(a.getProperties()), new HashSet<>(b.getProperties())).stream()
                .filter(property -> !ignoredProperties.contains(property))
                .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static boolean blockStatesEqual(BlockState a, BlockState b) {
        return a.getBlock() == b.getBlock()
            && Sets.intersection(new HashSet<>(a.getProperties()), new HashSet<>(b.getProperties())).stream()
                .allMatch(property -> Objects.equals(a.getValue(property), b.getValue(property)));
    }

    public static Comparator<BlockPos> uniqueBlockPosComparator(Comparator<BlockPos> parent) {
        return (a, b) -> {
            int parentValue = parent.compare(a, b);
            if (parentValue != 0) {
                return parentValue;
            } else if (a.getX() != b.getX()) {
                return Integer.compare(a.getX(), b.getX());
            } else if (a.getY() != b.getY()) {
                return Integer.compare(a.getY(), b.getY());
            } else if (a.getZ() != b.getZ()) {
                return Integer.compare(a.getZ(), b.getZ());
            } else {
                return 0;
            }
        };
    }
}
