/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import io.netty.handler.codec.http2.Http2FrameLogger.Direction;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.SupportType;
import buildcraft.lib.misc.BlockFaceShape;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleBlockDust;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

// TODO (Phase 7 — Rendering): ExtendedBlockState / IExtendedBlockState / IUnlistedProperty removed in 1.21.
// Tile→model data now flows via BlockEntity.getModelData() / ModelData / ModelProperty<T>.

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.blocks.ICustomPaintHandler;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.WireNode;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.prop.UnlistedNonNullProperty;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.energy.BCEnergyProxy;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.PipeModelCacheBase;
import buildcraft.transport.client.model.PipeModelCachePluggable;
import buildcraft.transport.client.render.PipeWireRenderer;
import buildcraft.transport.item.ItemWire;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;

public class BlockPipeHolder extends BlockBCTile_Neptune implements ICustomPaintHandler {
    public static final UnlistedNonNullProperty<WeakReference<TilePipeHolder>> PROP_TILE
        = new UnlistedNonNullProperty<>("tile");

    private static final AABB BOX_CENTER = new AABB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    private static final AABB BOX_DOWN = new AABB(0.25, 0, 0.25, 0.75, 0.25, 0.75);
    private static final AABB BOX_UP = new AABB(0.25, 0.75, 0.25, 0.75, 1, 0.75);
    private static final AABB BOX_NORTH = new AABB(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    private static final AABB BOX_SOUTH = new AABB(0.25, 0.25, 0.75, 0.75, 0.75, 1);
    private static final AABB BOX_WEST = new AABB(0, 0.25, 0.25, 0.25, 0.75, 0.75);
    private static final AABB BOX_EAST = new AABB(0.75, 0.25, 0.25, 1, 0.75, 0.75);
    private static final AABB[] BOX_FACES = { BOX_DOWN, BOX_UP, BOX_NORTH, BOX_SOUTH, BOX_WEST, BOX_EAST };

    private static final ResourceLocation ADVANCEMENT_LOGIC_TRANSPORTATION
        = ResourceLocation.parse("buildcrafttransport:logic_transportation");

    public BlockPipeHolder(BlockBehaviour.Properties props, String id) {
        super(props, id);

        setHardness(0.25f);
        setResistance(3.0f);
        setLightOpacity(0);
    }

    // basics

    // TODO (Phase 7): createBlockState() / ExtendedBlockState removed; migrate to createBlockStateDefinition()

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        return new TilePipeHolder();
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    // Collisions

    @Override
    @Deprecated
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        if (source.isEmptyBlock(pos)) {
            // Permit placing pipes below when jumping
            return BOX_CENTER;
        }
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public void addCollisionBoxToList(
        BlockState state, Level world, BlockPos pos, AABB entityBox, List<AABB> collidingBoxes,
        Entity entityIn, boolean isPistonMoving
    ) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, FULL_BLOCK_AABB);
            return;
        }
        boolean added = false;
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, BOX_CENTER);
            added = true;
            for (Direction face : Direction.values()) {
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0) {
                    AABB aabb = BOX_FACES[face.ordinal()];
                    if (conSize != 0.25f) {
                        Vec3 center = VecUtil.offset(new Vec3(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
                        Vec3 radius = new Vec3(0.25, 0.25, 0.25);
                        radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
                        Vec3 min = center.subtract(radius);
                        Vec3 max = center.add(radius);
                        aabb = BoundingBoxUtil.makeFrom(min, max);
                    }
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb);
                }
            }
        }
        for (Direction face : Direction.values()) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                AABB bb = pluggable.getBoundingBox();
                addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
                added = true;
            }
        }
        for (EnumWirePart part : tile.getWireManager().parts.keySet()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, part.boundingBox);
            added = true;
        }
        for (EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, between.boundingBox);
            added = true;
        }
        if (!added) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, FULL_BLOCK_AABB);
        }
    }

    @Nullable
    public BlockHitResult rayTrace(Level world, BlockPos pos, Player player) {
        Vec3 start = player.position().add(0, player.getEyeHeight(), 0);
        double reachDistance = 5;
        if (player instanceof ServerPlayer) {
            reachDistance = ((ServerPlayer) player).interactionManager.getBlockReachDistance();
        }
        Vec3 end = start.add(player.getLookVec().normalize().scale(reachDistance));
        return rayTrace(world, pos, start, end);
    }

    @Override
    @Nullable
    public BlockHitResult collisionRayTrace(BlockState state, Level world, BlockPos pos, Vec3 start, Vec3 end) {
        return rayTrace(world, pos, start, end);
    }

    @Nullable
    public BlockHitResult rayTrace(Level world, BlockPos pos, Vec3 start, Vec3 end) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return computeTrace(null, pos, start, end, FULL_BLOCK_AABB, 400);
        }
        BlockHitResult best = null;
        Pipe pipe = tile.getPipe();
        boolean computed = false;
        if (pipe != null) {
            computed = true;
            best = computeTrace(best, pos, start, end, BOX_CENTER, 0);
            for (Direction face : Direction.values()) {
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0) {
                    AABB aabb = BOX_FACES[face.ordinal()];
                    if (conSize != 0.25f) {
                        Vec3 center = VecUtil.offset(new Vec3(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
                        Vec3 radius = new Vec3(0.25, 0.25, 0.25);
                        radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
                        Vec3 min = center.subtract(radius);
                        Vec3 max = center.add(radius);
                        aabb = BoundingBoxUtil.makeFrom(min, max);
                    }
                    best = computeTrace(best, pos, start, end, aabb, face.ordinal() + 1);
                }
            }
        }
        for (Direction face : Direction.values()) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                AABB bb = pluggable.getBoundingBox();
                best = computeTrace(best, pos, start, end, bb, face.ordinal() + 1 + 6);
                computed = true;
            }
        }
        for (EnumWirePart part : tile.getWireManager().parts.keySet()) {
            best = computeTrace(best, pos, start, end, part.boundingBox, part.ordinal() + 1 + 6 + 6);
            computed = true;
        }
        for (EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
            best = computeTrace(best, pos, start, end, between.boundingBox, between.ordinal() + 1 + 6 + 6 + 8);
            computed = true;
        }
        if (!computed) {
            return computeTrace(null, pos, start, end, FULL_BLOCK_AABB, 400);
        }
        return best;
    }

    @Nullable
    public static EnumWirePart rayTraceWire(BlockPos pos, Vec3 start, Vec3 end) {
        Vec3 realStart = start.subtract(pos.getX(), pos.getY(), pos.getZ());
        Vec3 realEnd = end.subtract(pos.getX(), pos.getY(), pos.getZ());
        EnumWirePart best = null;
        double dist = 1000;
        for (EnumWirePart part : EnumWirePart.VALUES) {
            BlockHitResult trace = part.boundingBoxPossible.calculateIntercept(realStart, realEnd);
            if (trace != null) {
                if (best == null) {
                    best = part;
                    dist = trace.hitVec.squareDistanceTo(realStart);
                } else {
                    double nextDist = trace.hitVec.squareDistanceTo(realStart);
                    if (dist > nextDist) {
                        best = part;
                        dist = nextDist;
                    }
                }
            }
        }
        return best;
    }

    private BlockHitResult computeTrace(
        BlockHitResult lastBest, BlockPos pos, Vec3 start, Vec3 end, AABB aabb, int part
    ) {
        BlockHitResult next = super.rayTrace(pos, start, end, aabb);
        if (next == null) {
            return lastBest;
        }
        next.subHit = part;
        if (lastBest == null) {
            return next;
        }
        double distLast = lastBest.hitVec.squareDistanceTo(start);
        double distNext = next.hitVec.squareDistanceTo(start);
        return distLast > distNext ? next : lastBest;
    }

    @Nullable
    public static Direction getPartSideHit(BlockHitResult trace) {
        if (trace.subHit <= 0) {
            return trace.sideHit;
        }
        if (trace.subHit <= 6) {
            return Direction.values()[trace.subHit - 1];
        }
        if (trace.subHit <= 6 + 6) {
            return Direction.values()[trace.subHit - 1 - 6];
        }
        return null;
    }

    @Nullable
    public static EnumWirePart getWirePartHit(BlockHitResult trace) {
        if (trace.subHit <= 6 + 6) {
            return null;
        } else if (trace.subHit <= 6 + 6 + 8) {
            return EnumWirePart.VALUES[trace.subHit - 1 - 6 - 6];
        } else {
            return null;
        }
    }

    @Nullable
    public static EnumWireBetween getWireBetweenHit(BlockHitResult trace) {
        if (trace.subHit <= 6 + 6 + 8) {
            return null;
        } else if (trace.subHit <= 6 + 6 + 8 + EnumWireBetween.VALUES.length) {
            return EnumWireBetween.VALUES[trace.subHit - 1 - 6 - 6 - 8];
        } else {
            return null;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getSelectedBoundingBox(BlockState state, Level world, BlockPos pos) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return FULL_BLOCK_AABB;
        }
        BlockHitResult trace = Minecraft.getInstance().objectMouseOver;
        if (trace == null || trace.subHit < 0 || !pos.equals(trace.getBlockPos())) {
            // Perhaps we aren't the object the mouse is over
            return FULL_BLOCK_AABB;
        }
        int part = trace.subHit;
        AABB aabb = FULL_BLOCK_AABB;
        if (part == 0) {
            aabb = BOX_CENTER;
        } else if (part < 1 + 6) {
            aabb = BOX_FACES[part - 1];
            Pipe pipe = tile.getPipe();
            if (pipe != null) {
                Direction face = Direction.values()[part - 1];
                float conSize = pipe.getConnectedDist(face);
                if (conSize > 0 && conSize != 0.25f) {
                    Vec3 center = VecUtil.offset(new Vec3(0.5, 0.5, 0.5), face, 0.25 + (conSize / 2));
                    Vec3 radius = new Vec3(0.25, 0.25, 0.25);
                    radius = VecUtil.replaceValue(radius, face.getAxis(), conSize / 2);
                    Vec3 min = center.subtract(radius);
                    Vec3 max = center.add(radius);
                    aabb = BoundingBoxUtil.makeFrom(min, max);
                }
            }
        } else if (part < 1 + 6 + 6) {
            Direction side = Direction.values()[part - 1 - 6];
            PipePluggable pluggable = tile.getPluggable(side);
            if (pluggable != null) {
                aabb = pluggable.getBoundingBox();
            }
        } else if (part < 1 + 6 + 6 + 8) {
            EnumWirePart wirePart = EnumWirePart.VALUES[part - 1 - 6 - 6];
            aabb = wirePart.boundingBox;
        } else if (part < 1 + 6 + 6 + 6 + 8 + 36) {
            EnumWireBetween wireBetween = EnumWireBetween.VALUES[part - 1 - 6 - 6 - 8];
            aabb = wireBetween.boundingBox;
        }
        if (part >= 1 + 6 + 6) {
            return aabb.offset(pos);
        } else {
            return (aabb == FULL_BLOCK_AABB ? aabb : aabb.grow(1 / 32.0)).offset(pos);
        }
    }

    @Override
    public ItemStack getPickBlock(
        BlockState state, BlockHitResult target, Level world, BlockPos pos, Player player
    ) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null || target == null) {
            return ItemStack.EMPTY;
        }
        if (target.subHit <= 6) {
            Pipe pipe = tile.getPipe();
            if (pipe != null) {
                PipeDefinition def = pipe.getDefinition();
                Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(def);
                if (item != null) {
                    // TODO (Phase 10 — Registry): pipe colour was encoded as item metadata in 1.12;
                    // 1.21 items use NBT/DataComponents for subtypes. Colour encoding dropped for now.
                    return new ItemStack(item);
                }
            }
        } else if (target.subHit <= 12) {
            int pluggableHit = target.subHit - 7;
            Direction face = Direction.values()[pluggableHit];
            PipePluggable plug = tile.getPluggable(face);
            if (plug != null) {
                return plug.getPickStack();
            }
        } else {
            EnumWirePart part = null;
            EnumWireBetween between = null;

            if (target.subHit > 6) {
                part = getWirePartHit(target);
                between = getWireBetweenHit(target);
            }

            if (part != null && tile.wireManager.getColorOfPart(part) != null) {
                return new ItemStack(BCTransportItems.wire, 1);
            } else if (between != null && tile.wireManager.getColorOfPart(between.parts[0]) != null) {
                return new ItemStack(BCTransportItems.wire, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean onBlockActivated(
        Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX,
        float hitY, float hitZ
    ) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return false;
        }
        BlockHitResult trace = rayTrace(world, pos, player);
        if (trace == null) {
            return false;
        }
        Direction realSide = getPartSideHit(trace);
        if (realSide == null) {
            realSide = side;
        }
        if (trace.subHit > 6 && trace.subHit <= 12) {
            PipePluggable existing = tile.getPluggable(realSide);
            if (existing != null) {
                return existing.onPluggableActivate(player, trace, hitX, hitY, hitZ);
            }
        }

        EnumPipePart part = trace.subHit == 0 ? EnumPipePart.CENTER : EnumPipePart.fromFacing(realSide);

        ItemStack held = player.getItemInHand(hand);
        Item item = held.isEmpty() ? null : held.getItem();
        PipePluggable existing = tile.getPluggable(realSide);
        if (item instanceof IItemPluggable && existing == null) {
            IItemPluggable itemPlug = (IItemPluggable) item;
            PipePluggable plug = itemPlug.onPlace(held, tile, realSide, player, hand);
            if (plug == null) {
                return false;
            } else {
                tile.replacePluggable(realSide, plug);
                plug.onPlacedBy(player);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                return true;
            }
        }
        if (item instanceof ItemWire) {
            EnumWirePart wirePartHit = getWirePartHit(trace);
            EnumWirePart wirePart;
            TilePipeHolder attachTile = tile;
            if (wirePartHit != null) {
                WireNode node = new WireNode(pos, wirePartHit);
                node = node.offset(trace.sideHit);
                wirePart = node.part;
                if (!node.pos.equals(pos)) {
                    attachTile = getPipe(world, node.pos, false);
                }
            } else {
                wirePart = EnumWirePart.get(
                    (trace.hitVec.x % 1 + 1) % 1 > 0.5, (trace.hitVec.y % 1 + 1) % 1 > 0.5,
                    (trace.hitVec.z % 1 + 1) % 1 > 0.5
                );
            }
            if (wirePart != null && attachTile != null) {
                DyeColor colour = DyeColor.byId(held.getDamageValue());
                boolean attached = attachTile.getWireManager().addPart(wirePart, colour);
                attachTile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
                if (attached) {
                    WireNode from = new WireNode(attachTile.getPipePos(), wirePart);

                    boolean isNowConnected = false;
                    for (Direction dir : Direction.values()) {
                        WireNode to = from.relative(dir);
                        if (to.pos == attachTile.getPipePos()) {
                            if (attachTile.getWireManager().getColorOfPart(to.part) == colour) {
                                isNowConnected = true;
                                break;
                            }
                        } else {
                            BlockEntity localTile = attachTile.getLocalTile(to.pos);
                            if (localTile instanceof TilePipeHolder) {
                                if (((TilePipeHolder) localTile).getWireManager().getColorOfPart(to.part) == colour) {
                                    isNowConnected = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (isNowConnected) {
                        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_LOGIC_TRANSPORTATION);
                    }

                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
                if (attached) {
                    return true;
                }
            }
        }
        Pipe pipe = tile.getPipe();
        if (pipe == null) {
            return false;
        }
        if (pipe.behaviour.onPipeActivate(player, trace, hitX, hitY, hitZ, part)) {
            return true;
        }
        if (pipe.flow.onFlowActivate(player, trace, hitX, hitY, hitZ, part)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean removedByPlayer(
        BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest
    ) {
        if (world.isClientSide) {
            return false;
        }

        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        }

        NonNullList<ItemStack> toDrop = NonNullList.create();
        BlockHitResult trace = rayTrace(world, pos, player);
        Direction side = null;
        EnumWirePart part = null;
        EnumWireBetween between = null;

        if (trace != null && trace.subHit > 6) {
            side = getPartSideHit(trace);
            part = getWirePartHit(trace);
            between = getWireBetweenHit(trace);
        }

        if (side != null) {
            removePluggable(side, tile, toDrop);
            if (!player.getAbilities().instabuild) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
            return false;
        } else if (part != null) {
            toDrop.add(new ItemStack(BCTransportItems.wire, 1));
            tile.wireManager.removePart(part);
            if (!player.getAbilities().instabuild) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
            tile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
            return false;
        } else if (between != null) {
            toDrop.add(new ItemStack(BCTransportItems.wire, between.to == null ? 2 : 1));
            if (between.to == null) {
                tile.wireManager.removeParts(Arrays.asList(between.parts));
            } else {
                tile.wireManager.removePart(between.parts[0]);
            }
            if (!player.getAbilities().instabuild) {
                InventoryUtil.dropAll(world, pos, toDrop);
            }
            tile.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.WIRES);
            return false;
        } else {
            toDrop.addAll(getDrops(world, pos, state, 0));
            for (Direction face : Direction.values()) {
                removePluggable(face, tile, NonNullList.create());
            }
        }
        if (!player.getAbilities().instabuild) {
            InventoryUtil.dropAll(world, pos, toDrop);
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void getDrops(
        NonNullList<ItemStack> toDrop, BlockGetter world, BlockPos pos, BlockState state, int fortune
    ) {
        TilePipeHolder tile = getPipe(world, pos, false);
        for (Direction face : Direction.values()) {
            PipePluggable pluggable = tile.getPluggable(face);
            if (pluggable != null) {
                pluggable.addDrops(toDrop, fortune);
            }
        }
        for (DyeColor color : tile.wireManager.parts.values()) {
            toDrop.add(new ItemStack(BCTransportItems.wire, 1));
        }
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            pipe.addDrops(toDrop, fortune);
        }
    }

    @Override
    public float getExplosionResistance(Level world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        if (exploder != null) {
            Vec3 subtract = exploder.position().subtract(new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(VecUtil.VEC_HALF)).normalize();
            Direction side = Arrays.stream(Direction.values())
                .min(Comparator.comparing(facing -> Vec3.atLowerCornerOf(facing.getNormal()).distanceTo(subtract)))
                .orElseThrow(IllegalArgumentException::new);
            TilePipeHolder tile = getPipe(world, pos, true);
            if (tile != null) {
                PipePluggable pluggable = tile.getPluggable(side);
                if (pluggable != null) {
                    float explosionResistance = pluggable.getExplosionResistance(exploder, explosion);
                    if (explosionResistance > 0) {
                        return explosionResistance;
                    }
                }
            }
        }
        return super.getExplosionResistance(world, pos, exploder, explosion);
    }

    @Override
    public void onEntityCollidedWithBlock(Level world, BlockPos pos, BlockState state, Entity entity) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return;
        }
        Pipe pipe = tile.getPipe();
        if (pipe != null) {
            pipe.getBehaviour().onEntityCollide(entity);
        }
    }

    @Override
    public void harvestBlock(
        Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack
    ) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
    }

    @Override
    public boolean canBeConnectedTo(BlockGetter world, BlockPos pos, Direction facing) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return false;
        }
        PipePluggable pluggable = tile.getPluggable(facing);
        return pluggable != null && pluggable.canBeConnected();
    }

    @Override
    public boolean isSideSolid(BlockState base_state, BlockGetter world, BlockPos pos, Direction side) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return false;
        }
        PipePluggable pluggable = tile.getPluggable(side);
        return pluggable != null && pluggable.isSideSolid();
    }

    public BlockFaceShape getBlockFaceShape(BlockGetter world, BlockState state, BlockPos pos, Direction face) {
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile == null) {
            return BlockFaceShape.UNDEFINED;
        }
        PipePluggable pluggable = tile.getPluggable(face);
        return pluggable != null ? pluggable.getBlockFaceShape() : BlockFaceShape.UNDEFINED;
    }

    private static void removePluggable(Direction side, TilePipeHolder tile, NonNullList<ItemStack> toDrop) {
        PipePluggable removed = tile.replacePluggable(side, null);
        if (removed != null) {
            removed.onRemove();
            removed.addDrops(toDrop, 0);
        }
    }

    public static TilePipeHolder getPipe(BlockGetter access, BlockPos pos, boolean requireServer) {
        if (access instanceof World) {
            return getPipe((Level) access, pos, requireServer);
        }
        if (requireServer) {
            return null;
        }
        BlockEntity tile = access.getBlockEntity(pos);
        if (tile instanceof TilePipeHolder) {
            return (TilePipeHolder) tile;
        }
        return null;
    }

    public static TilePipeHolder getPipe(Level world, BlockPos pos, boolean requireServer) {
        if (requireServer && world.isClientSide) {
            return null;
        }
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TilePipeHolder) {
            return (TilePipeHolder) tile;
        }
        return null;
    }

    // Block overrides

    @Override
    public boolean addLandingEffects(
        BlockState state, ServerLevel world, BlockPos pos, BlockState iblockstate, LivingEntity entity,
        int numberOfParticles
    ) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TilePipeHolder) {
            TilePipeHolder pipeHolder = ((TilePipeHolder) te);

            pipeHolder.createAndSendMessage(TilePipeHolder.NET_CREATE_LANDING_PARTICLE, new IPayloadWriter() {

                @Override
                public void write(PacketBufferBC buffer) {
                    buffer.writeDouble(entity.posX);
                    buffer.writeDouble(entity.posY);
                    buffer.writeDouble(entity.posZ);
                    buffer.writeInt(numberOfParticles);
                }
            });
            return true;
        }

        return super.addLandingEffects(state, world, pos, iblockstate, entity, numberOfParticles);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide) {
            return super.addRunningEffects(state, world, pos, entity);
        }

        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TilePipeHolder) {
            TilePipeHolder pipeHolder = ((TilePipeHolder) te);

            spawnRunningParticles(pipeHolder, entity.posX, entity.getEntityBoundingBox().minY, entity.posZ, entity.width, entity.motionX, entity.motionZ);

            return true;
        }

        return super.addRunningEffects(state, world, pos, entity);
    }

    @OnlyIn(Dist.CLIENT)
    public static void spawnLandingParticles(
        TilePipeHolder pipe, double posX, double posY, double posZ, int numberOfParticles
    ) {
        int subHit = 0;
        if (pipe.getPluggable(Direction.UP) != null) {
            subHit = 6 + 1 + Direction.UP.ordinal();
        }
        HitSpriteInfo info = getHitSpriteInfo(subHit, pipe);
        if (info != null) {

            Random random = pipe.getLevel().rand;

            for (int i = 0; i < numberOfParticles; i++) {

                double speedX = random.nextGaussian() * 0.15;
                double speedY = random.nextGaussian() * 0.15;
                double speedZ = random.nextGaussian() * 0.15;

                ParticleDigging particle
                    = new ParticleBlockDust(pipe.getLevel(), posX, posY, posZ, speedX, speedY, speedZ, pipe.getCurrentState()) {
                        // Just to make the constructor public
                    };
                particle.setBlockPos(pipe.getBlockPos());
                particle.setParticleTexture(info.sprite);

                Minecraft.getInstance().effectRenderer.addEffect(particle);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void spawnRunningParticles(TilePipeHolder pipe, double posX, double posY, double posZ, float entityWidth, double motionX, double motionZ) {
        int subHit = 0;
        if (pipe.getPluggable(Direction.UP) != null) {
            subHit = 6 + 1 + Direction.UP.ordinal();
        }
        HitSpriteInfo info = getHitSpriteInfo(subHit, pipe);
        if (info != null) {

            Random random = pipe.getLevel().rand;

            posX += (random.nextFloat() - 0.5) * entityWidth;
            posY += 0.1;
            posZ += (random.nextFloat() - 0.5) * entityWidth;

            double speedX = motionX * -0.4;
            double speedY = 0.15;
            double speedZ = motionZ * -0.4;

            ParticleDigging particle
                = new ParticleBlockDust(pipe.getLevel(), posX, posY, posZ, speedX, speedY, speedZ, pipe.getCurrentState()) {
                    // Just to make the constructor public
                };
            particle.setBlockPos(pipe.getBlockPos());
            particle.setParticleTexture(info.sprite);

            Minecraft.getInstance().effectRenderer.addEffect(particle);
        }
    }

    private static HitSpriteInfo getHitSpriteInfo(BlockHitResult target, TilePipeHolder pipeHolder) {
        return getHitSpriteInfo(target.subHit, pipeHolder);
    }

    private static HitSpriteInfo getHitSpriteInfo(int subHit, TilePipeHolder pipeHolder) {
        int p = subHit;
        AABB aabb = null;
        TextureAtlasSprite sprite = SpriteUtil.missingSprite();
        if (0 <= p && p <= 6) {
            aabb = p == 0 ? BOX_CENTER : BOX_FACES[p - 1];
            PipeDefinition def = pipeHolder.getPipe().definition;
            TextureAtlasSprite[] sprites = PipeModelCacheBase.generator.getItemSprites(def);
            sprite = sprites.length == 0 ? SpriteUtil.missingSprite() : sprites[0];
        } else if (6 + 1 <= p && p < 6 + 6 + 1) {
            PipePluggable plug = pipeHolder.getPluggable(Direction.values()[p - 6 - 1]);
            if (plug == null) {
                return null;
            }
            aabb = plug.getBoundingBox();
            if (aabb == null) {
                return null;
            }
            PluggableModelKey keyC = plug.getModelRenderKey(BlockRenderLayer.CUTOUT);
            PluggableModelKey keyT = plug.getModelRenderKey(BlockRenderLayer.TRANSLUCENT);
            if (keyC == null && keyT == null) {
                return null;
            }
            List<BakedQuad> quads = null;
            if (keyC != null) quads = PipeModelCachePluggable.cacheCutoutSingle.bake(keyC);
            if (quads == null || quads.isEmpty()) {
                if (keyT == null) {
                    return null;
                }
                quads = PipeModelCachePluggable.cacheTranslucentSingle.bake(keyT);
                if (quads == null || quads.isEmpty()) {
                    return null;
                }
            }
            sprite = quads.get(0).getSprite();
        } else if (6 + 6 + 1 <= p && p < 1 + 6 + 6 + 8) {
            EnumWirePart wirePart = EnumWirePart.values()[p - 6 - 6 - 1];
            aabb = wirePart.boundingBox;
            DyeColor colour = pipeHolder.getWireManager().getColorOfPart(wirePart);
            if (colour == null) {
                return null;
            }
            sprite = PipeWireRenderer.getWireSprite(colour).getSprite();
        } else if (6 + 6 + 1 + 8 < p && p <= 6 + 6 + 1 + 8 + 36) {
            EnumWireBetween wireBetween = EnumWireBetween.values()[p - 6 - 6 - 1 - 8];
            aabb = wireBetween.boundingBox;
            DyeColor colour = pipeHolder.getWireManager().betweens.get(wireBetween);
            if (colour == null) {
                return null;
            }
            sprite = PipeWireRenderer.getWireSprite(colour).getSprite();
        } else {
            return null;
        }
        if (aabb == null) {
            throw new IllegalStateException("Null aabb for index " + p + " (and sprite " + sprite + ")");
        }
        return new HitSpriteInfo(aabb, sprite);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(BlockState state, Level world, BlockHitResult target, ParticleManager manager) {

        BlockEntity te = world.getBlockEntity(target.getBlockPos());
        if (te instanceof TilePipeHolder) {
            TilePipeHolder pipeHolder = ((TilePipeHolder) te);
            HitSpriteInfo info = getHitSpriteInfo(target, pipeHolder);

            if (info == null) {
                return false;
            }

            double x = Math.random() * (info.aabb.maxX - info.aabb.minX) + info.aabb.minX;
            double y = Math.random() * (info.aabb.maxY - info.aabb.minY) + info.aabb.minY;
            double z = Math.random() * (info.aabb.maxZ - info.aabb.minZ) + info.aabb.minZ;

            switch (target.sideHit) {
                case DOWN:
                    y = info.aabb.minY - 0.1;
                    break;
                case UP:
                    y = info.aabb.maxY + 0.1;
                    break;
                case NORTH:
                    z = info.aabb.minZ - 0.1;
                    break;
                case SOUTH:
                    z = info.aabb.maxZ + 0.1;
                    break;
                case WEST:
                    x = info.aabb.minX - 0.1;
                    break;
                default:
                    x = info.aabb.maxX + 0.1;
                    break;
            }

            x += target.getBlockPos().getX();
            y += target.getBlockPos().getY();
            z += target.getBlockPos().getZ();

            ParticleDigging particle = new ParticleDigging(world, x, y, z, 0, 0, 0, state) {
                // Just to make the constructor public
            };
            particle.setBlockPos(target.getBlockPos());
            particle.setParticleTexture(info.sprite);
            particle.multiplyVelocity(0.2F);
            particle.multipleParticleScaleBy(0.6F);
            manager.addEffect(particle);
            return true;
        }

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(Level world, BlockPos pos, ParticleManager manager) {
        BlockHitResult hitResult = Minecraft.getInstance().objectMouseOver;
        if (hitResult == null || !pos.equals(hitResult.getBlockPos())) {
            return false;
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TilePipeHolder) {
            TilePipeHolder pipeHolder = ((TilePipeHolder) te);
            HitSpriteInfo info = getHitSpriteInfo(hitResult, pipeHolder);
            if (info == null) {
                return false;
            }

            double sizeX = info.aabb.maxX - info.aabb.minX;
            double sizeY = info.aabb.maxY - info.aabb.minY;
            double sizeZ = info.aabb.maxZ - info.aabb.minZ;

            int countX = (int) Math.max(2, 4 * sizeX);
            int countY = (int) Math.max(2, 4 * sizeY);
            int countZ = (int) Math.max(2, 4 * sizeZ);

            BlockState state = world.getBlockState(pos);
            for (int x = 0; x < countX; x++) {
                for (int y = 0; y < countY; y++) {
                    for (int z = 0; z < countZ; z++) {

                        double _x = pos.getX() + info.aabb.minX + (x + 0.5) * sizeX / countX;
                        double _y = pos.getY() + info.aabb.minY + (y + 0.5) * sizeY / countY;
                        double _z = pos.getZ() + info.aabb.minZ + (z + 0.5) * sizeZ / countZ;

                        ParticleDigging particle = new ParticleDigging(world, _x, _y, _z, 0, 0, 0, state) {
                            // Just to make the constructor public
                        };
                        particle.setBlockPos(pos);
                        particle.setParticleTexture(info.sprite);
                        manager.addEffect(particle);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private static final class HitSpriteInfo {
        final AABB aabb;
        final TextureAtlasSprite sprite;

        HitSpriteInfo(AABB aabb, TextureAtlasSprite sprite) {
            this.aabb = aabb;
            this.sprite = sprite;
        }
    }

    // paint

    @Override
    public InteractionResult attemptPaint(
        Level world, BlockPos pos, BlockState state, Vec3 hitPos, Direction hitSide, DyeColor paintColour
    ) {
        TilePipeHolder tile = getPipe(world, pos, true);
        if (tile == null) {
            return InteractionResult.PASS;
        }

        Pipe pipe = tile.getPipe();
        if (pipe == null) {
            return InteractionResult.FAIL;
        }
        if (pipe.getColour() == paintColour || !pipe.definition.canBeColoured) {
            return InteractionResult.FAIL;
        } else {
            pipe.setColour(paintColour);
            return InteractionResult.SUCCESS;
        }
    }

    // rendering

    // TODO (Phase 7): getExtendedState / IExtendedBlockState removed; use TilePipeHolder.getModelData() instead

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
        if (side == null) return false;
        TilePipeHolder tile = getPipe(world, pos, false);
        if (tile != null) {
            PipePluggable pluggable = tile.getPluggable(side.getOpposite());
            return pluggable != null && pluggable.canConnectToRedstone(side);
        }
        return false;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getStrongPower(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (side == null) {
            return 0;
        }
        TilePipeHolder tile = getPipe(blockAccess, pos, false);
        if (tile != null) {
            return tile.getRedstoneOutput(side.getOpposite());
        }
        return 0;
    }

    @Override
    public boolean isBlockNormalCube(BlockState state) {
        return false;
    }

    @Override
    public int getWeakPower(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return getStrongPower(blockState, blockAccess, pos, side);
    }
}
