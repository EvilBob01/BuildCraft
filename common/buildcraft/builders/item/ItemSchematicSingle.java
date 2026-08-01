/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.item;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;

import buildcraft.lib.inventory.InventoryWrapper;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.builders.snapshot.SchematicBlockManager;

public class ItemSchematicSingle extends ItemBC_Neptune {
    public static final int DAMAGE_CLEAN = 0;
    public static final int DAMAGE_USED = 1;
    public static final String NBT_KEY = "schematic";

    public ItemSchematicSingle(String id) {
        super(id);
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return stack.getItemDamage() == DAMAGE_CLEAN ? 16 : super.getItemStackLimit(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, DAMAGE_CLEAN, "clean");
        addVariant(variants, DAMAGE_USED, "used");
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = StackUtil.asNonNull(player.getItemInHand(hand));
        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        if (player.isSneaking()) {
            CompoundTag itemData = NBTUtilBC.getItemData(stack);
            itemData.removeTag(NBT_KEY);
            if (itemData.hasNoTags()) {
                stack.setTagCompound(null);
            }
            stack.setItemDamage(DAMAGE_CLEAN);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    @Override
    public InteractionResult onItemUseFirst(Player player, Level world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, InteractionHand hand) {
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSneaking()) {
            CompoundTag itemData = NBTUtilBC.getItemData(StackUtil.asNonNull(stack));
            itemData.removeTag(NBT_KEY);
            if (itemData.hasNoTags()) {
                stack.setTagCompound(null);
            }
            stack.setItemDamage(DAMAGE_CLEAN);
            return InteractionResult.SUCCESS;
        }
        int damage = stack.getItemDamage();
        if (damage != DAMAGE_USED) {
            BlockState state = world.getBlockState(pos);
            ISchematicBlock schematicBlock = SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(
                world,
                pos,
                pos,
                state,
                state.getBlock()
            ));
            if (schematicBlock.isAir()) {
                return InteractionResult.FAIL;
            }
            NBTUtilBC.getItemData(stack).put(NBT_KEY, SchematicBlockManager.saveAdditional(schematicBlock));
            stack.setItemDamage(DAMAGE_USED);
            return InteractionResult.SUCCESS;
        } else {
            BlockPos placePos = pos;
            boolean replaceable = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
            if (!replaceable) {
                placePos = placePos.offset(side);
            }
            if (!world.mayPlace(world.getBlockState(pos).getBlock(), placePos, false, side, null)) {
                return InteractionResult.FAIL;
            }
            if (replaceable && !world.isEmptyBlock(placePos)) {
                world.removeBlock(placePos, false);
            }
            try {
                ISchematicBlock schematicBlock = getSchematic(stack);
                if (schematicBlock != null) {
                    if (!schematicBlock.isBuilt(world, placePos) && schematicBlock.canBuild(world, placePos)) {
                        List<FluidStack> requiredFluids = schematicBlock.computeRequiredFluids();
                        List<ItemStack> requiredItems = schematicBlock.computeRequiredItems();
                        if (requiredFluids.isEmpty()) {
                            InventoryWrapper itemTransactor = new InventoryWrapper(player.inventory);
                            if (StackUtil.mergeSameItems(requiredItems).stream().noneMatch(s ->
                                itemTransactor.extract(
                                    extracted -> StackUtil.canMerge(s, extracted),
                                    s.getCount(),
                                    s.getCount(),
                                    true
                                ).isEmpty()
                            )) {
                                if (schematicBlock.build(world, placePos)) {
                                    StackUtil.mergeSameItems(requiredItems).forEach(s ->
                                        itemTransactor.extract(
                                            extracted -> StackUtil.canMerge(s, extracted),
                                            s.getCount(),
                                            s.getCount(),
                                            false
                                        )
                                    );
                                    SoundUtil.playBlockPlace(world, placePos);
                                    player.swingArm(hand);
                                    return InteractionResult.SUCCESS;
                                }
                            } else {
                                player.sendStatusMessage(
                                    new TextComponentString(
                                        "Not enough items. Total needed: " +
                                            StackUtil.mergeSameItems(requiredItems).stream()
                                                .map(s -> s.getTextComponent().getFormattedText() + " x " + s.getCount())
                                                .collect(Collectors.joining(", "))
                                    ),
                                    true
                                );
                            }
                        } else {
                            player.sendStatusMessage(
                                new TextComponentString("Schematic requires fluids"),
                                true
                            );
                        }
                    }
                }
            } catch (InvalidInputDataException e) {
                player.sendStatusMessage(
                    new TextComponentString("Invalid schematic: " + e.getMessage()),
                    true
                );
                e.printStackTrace();
            }
            return InteractionResult.FAIL;
        }
    }

    public static ISchematicBlock getSchematic(@Nonnull ItemStack stack) throws InvalidInputDataException {
        if (stack.getItem() instanceof ItemSchematicSingle) {
            return SchematicBlockManager.loadAdditional(NBTUtilBC.getItemData(stack).getCompound(NBT_KEY));
        }
        return null;
    }

    public static ISchematicBlock getSchematicSafe(@Nonnull ItemStack stack) {
        try {
            return getSchematic(stack);
        } catch (InvalidInputDataException e) {
            BCLog.logger.warn("Invalid schematic " + e.getMessage());
            return null;
        }
    }
}
