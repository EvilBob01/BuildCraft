/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;

public class SchematicEntityDefault implements ISchematicEntity {
    private CompoundTag entityNbt;
    private Vec3 pos;
    private BlockPos hangingPos;
    private Direction hangingFacing;
    private Rotation entityRotation = Rotation.NONE;

    private static CompoundTag saveEntity(Entity entity) {
        CompoundTag tag = new CompoundTag();
        entity.save(tag);
        return tag;
    }

    private static float getRotationOffset(Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_90: return 90.0f;
            case CLOCKWISE_180: return 180.0f;
            case COUNTERCLOCKWISE_90: return -90.0f;
            default: return 0.0f;
        }
    }

    public static boolean predicate(SchematicEntityContext context) {
        ResourceLocation registryName = BuiltInRegistries.ENTITY_TYPE.getKey(context.entity.getType());
        return registryName != null &&
            RulesLoader.READ_DOMAINS.contains(registryName.getNamespace()) &&
            RulesLoader.getRules(
                registryName,
                saveEntity(context.entity)
            )
                .stream()
                .anyMatch(rule -> rule.capture);
    }

    @Override
    public void init(SchematicEntityContext context) {
        entityNbt = saveEntity(context.entity);
        pos = context.entity.position().subtract(new Vec3(context.basePos.getX(), context.basePos.getY(), context.basePos.getZ()));
        if (context.entity instanceof HangingEntity) {
            HangingEntity entityHanging = (HangingEntity) context.entity;
            hangingPos = entityHanging.blockPosition().subtract(context.basePos);
            hangingFacing = entityHanging.getDirection();
        } else {
            hangingPos = new BlockPos(pos);
            hangingFacing = Direction.NORTH;
        }
    }

    @Override
    public Vec3 getBlockPos() {
        return pos;
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
        Set<JsonRule> rules = RulesLoader.getRules(
            ResourceLocation.parse(entityNbt.getString("id")),
            entityNbt
        );
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("Rules are empty");
        }
        return rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(requiredExtractor -> requiredExtractor.extractItemsFromEntity(entityNbt).stream())
            .filter(((Predicate<ItemStack>) ItemStack::isEmpty).negate())
            .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids() {
        Set<JsonRule> rules = RulesLoader.getRules(
            ResourceLocation.parse(entityNbt.getString("id")),
            entityNbt
        );
        return rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(requiredExtractor -> requiredExtractor.extractFluidsFromEntity(entityNbt).stream())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public SchematicEntityDefault getRotated(Rotation rotation) {
        SchematicEntityDefault schematicEntity = SchematicEntityManager.createCleanCopy(this);
        schematicEntity.entityNbt = entityNbt;
        schematicEntity.pos = RotationUtil.rotateVec3d(pos, rotation);
        schematicEntity.hangingPos = hangingPos.rotate(rotation);
        schematicEntity.hangingFacing = rotation.rotate(hangingFacing);
        schematicEntity.entityRotation = entityRotation.add(rotation);
        return schematicEntity;
    }

    @Override
    public Entity build(Level world, BlockPos basePos) {
        Set<JsonRule> rules = RulesLoader.getRules(
            ResourceLocation.parse(entityNbt.getString("id")),
            entityNbt
        );
        CompoundTag replaceNbt = rules.stream()
            .map(rule -> rule.replaceNbt)
            .filter(Objects::nonNull)
            .map(Tag.class::cast)
            .reduce(NBTUtilBC::merge)
            .map(CompoundTag.class::cast)
            .orElse(null);
        Vec3 placePos = new Vec3(basePos.getX(), basePos.getY(), basePos.getZ()).add(pos);
        BlockPos placeHangingPos = basePos.offset(hangingPos);
        CompoundTag newEntityNbt = new CompoundTag();
        entityNbt.getAllKeys().stream()
            .map(key -> Pair.of(key, entityNbt.get(key)))
            .forEach(kv -> newEntityNbt.put(kv.getKey(), kv.getValue()));
        newEntityNbt.put("Pos", NBTUtilBC.writeVec3d(placePos));
        newEntityNbt.putUUID("UUID", UUID.randomUUID());
        boolean rotate = false;
        if (Stream.of("TileX", "TileY", "TileZ", "Facing").allMatch(newEntityNbt::contains)) {
            newEntityNbt.putInt("TileX", placeHangingPos.getX());
            newEntityNbt.putInt("TileY", placeHangingPos.getY());
            newEntityNbt.putInt("TileZ", placeHangingPos.getZ());
            newEntityNbt.putByte("Facing", (byte) hangingFacing.get2DDataValue());
        } else {
            rotate = true;
        }
        Entity entity = EntityType.create(
            replaceNbt != null
                ? (CompoundTag) NBTUtilBC.merge(newEntityNbt, replaceNbt)
                : newEntityNbt,
            world
        ).orElse(null);
        if (entity != null) {
            if (rotate) {
                entity.moveTo(
                    placePos.x,
                    placePos.y,
                    placePos.z,
                    entity.getYRot() - getRotationOffset(entityRotation),
                    entity.getXRot()
                );
            }
            world.addFreshEntity(entity);
        }
        return entity;
    }

    @Override
    public Entity buildWithoutChecks(Level world, BlockPos basePos) {
        return build(world, basePos);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("entityNbt", entityNbt);
        nbt.put("pos", NBTUtilBC.writeVec3d(pos));
        nbt.put("hangingPos", NbtUtils.writeBlockPos(hangingPos));
        nbt.put("hangingFacing", NBTUtilBC.writeEnum(hangingFacing));
        nbt.put("entityRotation", NBTUtilBC.writeEnum(entityRotation));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        entityNbt = nbt.getCompound("entityNbt");
        pos = NBTUtilBC.readVec3d(nbt.get("pos"));
        hangingPos = NbtUtils.readBlockPos(nbt, "hangingPos").orElse(BlockPos.ZERO);
        hangingFacing = NBTUtilBC.readEnum(nbt.get("hangingFacing"), Direction.class);
        entityRotation = NBTUtilBC.readEnum(nbt.get("entityRotation"), Rotation.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SchematicEntityDefault that = (SchematicEntityDefault) o;

        return entityNbt.equals(that.entityNbt) &&
            pos.equals(that.pos) &&
            hangingPos.equals(that.hangingPos) &&
            hangingFacing == that.hangingFacing &&
            entityRotation == that.entityRotation;
    }

    @Override
    public int hashCode() {
        int result = entityNbt.hashCode();
        result = 31 * result + pos.hashCode();
        result = 31 * result + hangingPos.hashCode();
        result = 31 * result + hangingFacing.hashCode();
        result = 31 * result + entityRotation.hashCode();
        return result;
    }
}
