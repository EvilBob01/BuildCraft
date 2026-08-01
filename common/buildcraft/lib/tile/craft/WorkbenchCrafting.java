/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.craft;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.NonNullList;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public class WorkbenchCrafting implements CraftingContainer {
    enum EnumRecipeType {
        INGREDIENTS,
        EXACT_STACKS;
    }

    private final BlockEntity tile;
    private final ItemHandlerSimple invBlueprint;
    private final ItemHandlerSimple invMaterials;
    private final ItemHandlerSimple invResult;
    private final NonNullList<ItemStack> items;
    private final int width;
    private final int height;
    private boolean isBlueprintDirty = true;
    private boolean areMaterialsDirty = true;
    private boolean cachedHasRequirements = false;

    @Nullable
    private CraftingRecipe currentRecipe;
    private ItemStack assumedResult = ItemStack.EMPTY;

    private EnumRecipeType recipeType = null;

    public WorkbenchCrafting(int width, int height, TileBC_Neptune tile, ItemHandlerSimple invBlueprint,
        ItemHandlerSimple invMaterials, ItemHandlerSimple invResult) {
        this.width = width;
        this.height = height;
        this.items = NonNullList.withSize(width * height, ItemStack.EMPTY);
        this.tile = tile;
        this.invBlueprint = invBlueprint;
        if (invBlueprint.getSlots() < this.getContainerSize()) {
            throw new IllegalArgumentException("Passed blueprint has a smaller size than width * height! ( expected "
                + getContainerSize() + ", got " + invBlueprint.getSlots() + ")");
        }
        this.invMaterials = invMaterials;
        this.invResult = invResult;
    }

    // CraftingContainer / Container interface implementation

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : items) {
            if (!item.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return isBlueprintDirty ? invBlueprint.getStackInSlot(index) : items.get(index);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    @Override
    public void setChanged() {
        // no-op: changes tracked via onInventoryChange
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        items.replaceAll(stack -> ItemStack.EMPTY);
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (ItemStack item : items) {
            contents.accountSimpleStack(item);
        }
    }

    // Build a CraftingInput snapshot from current item storage for recipe matching
    private CraftingInput toCraftingInput() {
        List<ItemStack> copy = new ArrayList<>(items);
        return CraftingInput.of(width, height, copy);
    }

    public ItemStack getAssumedResult() {
        return assumedResult;
    }

    public void onInventoryChange(IItemHandler inv) {
        if (inv == invBlueprint) {
            isBlueprintDirty = true;
        } else if (inv == invMaterials) {
            areMaterialsDirty = true;
        }
    }

    /** @return True if anything changed, false otherwise */
    public boolean tick() {
        if (tile.getLevel().isClientSide) {
            throw new IllegalStateException("Never call this on the client side!");
        }
        if (isBlueprintDirty) {
            currentRecipe = CraftingUtil.findMatchingRecipe(this, tile.getLevel());
            if (currentRecipe == null) {
                assumedResult = ItemStack.EMPTY;
                recipeType = null;
            } else {
                assumedResult = currentRecipe.assemble(toCraftingInput(), tile.getLevel().registryAccess());
                NonNullList<Ingredient> ingredients = currentRecipe.getIngredients();
                if (ingredients.isEmpty()) {
                    recipeType = EnumRecipeType.EXACT_STACKS;
                } else {
                    recipeType = EnumRecipeType.INGREDIENTS;
                }
            }
            isBlueprintDirty = false;
            return true;
        }
        return false;
    }

    /** @return True if {@link #craft()} might return true, or false if {@link #craft()} will definitely return
     *         false. */
    public boolean canCraft() {
        if (currentRecipe == null || isBlueprintDirty) {
            return false;
        }
        if (!invResult.canFullyAccept(assumedResult)) {
            return false;
        }
        if (areMaterialsDirty) {
            areMaterialsDirty = false;
            switch (recipeType) {
                case INGREDIENTS:
                    // cachedHasRequirements = hasIngredients();
                    // break;
                case EXACT_STACKS: {
                    cachedHasRequirements = hasExactStacks();
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown recipe type " + recipeType);
                }
            }
        }
        return cachedHasRequirements;
    }

    /** Attempts to craft a single item. Assumes that {@link #canCraft()} has been called in the same tick, without any
     * modifications happening to the
     *
     * @return True if the crafting happened, false otherwise. *
     * @throws IllegalStateException if {@link #canCraft()} hasn't been called before, or something changed in the
     *             meantime. */
    public boolean craft() throws IllegalStateException {
        if (isBlueprintDirty) {
            return false;
        }

        switch (recipeType) {
            case INGREDIENTS:
                // return craftByIngredients();
            case EXACT_STACKS: {
                return craftExact();
            }
            default: {
                throw new IllegalStateException("Unknown recipe type " + recipeType);
            }
        }
    }

    private boolean hasExactStacks() {
        TObjectIntMap<ItemStackKey> required = new TObjectIntHashMap<>(getContainerSize());
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack req = invBlueprint.getStackInSlot(s);
            if (!req.isEmpty()) {
                int count = req.getCount();
                if (count != 1) {
                    req = req.copy();
                    req.setCount(1);
                }
                ItemStackKey key = new ItemStackKey(req);
                required.adjustOrPutValue(key, count, count);
            }
        }
        return required.forEachEntry((stack, count) -> {
            ArrayStackFilter filter = new ArrayStackFilter(stack.baseStack);
            ItemStack inInventory = invMaterials.extract(filter, count, count, true);
            return !inInventory.isEmpty() && inInventory.getCount() == count;
        });
    }

    /** Implementation of {@link #craft()}, assuming nothing about the current recipe. */
    private boolean craftExact() {
        // 4 steps:
        // - Move everything out of this inventory (Just to check: state correction operation)
        // - Attempt to move every exact item from invMaterials to this inventory
        // - Call normal crafting stuffs
        // - Move everything from the inventory back to materials

        // Step 1
        clearInventory();

        // Step 2
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack bpt = invBlueprint.getStackInSlot(s);
            if (!bpt.isEmpty()) {
                ItemStack stack = invMaterials.extract(new ArrayStackFilter(bpt), 1, 1, false);
                if (stack.isEmpty()) {
                    clearInventory();
                    return false;
                }
                setItem(s, stack);
            }
        }

        // Step 3
        // Some recipes (for example vanilla fireworks) require calling
        // matches before calling assemble, as they store the
        // result of matches for assemble and getResult.
        CraftingInput input = toCraftingInput();
        if (!currentRecipe.matches(input, tile.getLevel())) {
            return false;
        }
        ItemStack result = currentRecipe.assemble(input, tile.getLevel().registryAccess());
        if (result.isEmpty()) {
            // what?
            clearInventory();
            return false;
        }
        ItemStack leftover = invResult.insert(result, false, false);
        if (!leftover.isEmpty()) {
            InventoryUtil.addToBestAcceptor(tile.getLevel(), tile.getBlockPos(), null, leftover);
        }
        NonNullList<ItemStack> remainingStacks = currentRecipe.getRemainingItems(input);
        for (int s = 0; s < remainingStacks.size(); s++) {
            ItemStack inSlot = getItem(s);
            ItemStack remaining = remainingStacks.get(s);

            if (!inSlot.isEmpty()) {
                removeItem(s, 1);
                inSlot = getItem(s);
            }

            if (!remaining.isEmpty()) {
                if (inSlot.isEmpty()) {
                    setItem(s, remaining);
                } else if (ItemStack.isSameItem(inSlot, remaining)
                    && ItemStack.isSameItemSameComponents(inSlot, remaining)) {
                    remaining.grow(inSlot.getCount());
                    setItem(s, remaining);
                } else {
                    leftover = invMaterials.insert(remaining, false, false);
                    if (!leftover.isEmpty()) {
                        InventoryUtil.addToBestAcceptor(tile.getLevel(), tile.getBlockPos(), null, leftover);
                    }
                }
            }
        }

        // Step 4
        // Some ingredients really need to be removed (like empty buckets)
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack inSlot = removeItemNoUpdate(s);
            if (!inSlot.isEmpty()) {
                leftover = invMaterials.insert(inSlot, false, false);
                if (!leftover.isEmpty()) {
                    InventoryUtil.addToBestAcceptor(tile.getLevel(), tile.getBlockPos(), null, leftover);
                }
            }
        }
        return true;
    }

    /** @return True if this inventory is now clear, false otherwise. */
    private boolean clearInventory() {
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack inSlot = items.get(s);
            if (!inSlot.isEmpty()) {
                ItemStack leftover = invMaterials.insert(inSlot, false, false);
                removeItem(s, inSlot.getCount() - (leftover.isEmpty() ? 0 : leftover.getCount()));
                if (!leftover.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
