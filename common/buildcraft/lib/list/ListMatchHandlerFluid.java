/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import java.util.ArrayList;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import buildcraft.api.lists.ListMatchHandler;

import buildcraft.lib.misc.StackUtil;

public class ListMatchHandlerFluid extends ListMatchHandler {
    private static final List<ItemStack> clientExampleHolders = new ArrayList<>();
    private static boolean isBuilt = false;

    private static void buildClientExampleList() {
        if (isBuilt) {
            return;
        }
        isBuilt = true;
        for (Item item : Item.REGISTRY) {
            NonNullList<ItemStack> stacks = NonNullList.create();
            item.getSubItems(CreativeModeTab.SEARCH, stacks);
            for (ItemStack toTry : stacks) {
                IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(toTry).orElse(null);
                if (fluidHandler != null && fluidHandler.drain(1, IFluidHandler.FluidAction.SIMULATE) == null) {
                    clientExampleHolders.add(toTry);
                }
            }
        }
    }

    @Override
    public boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            IFluidHandlerItem fluidHandlerStack = FluidUtil.getFluidHandler(stack.copy().orElse(null));
            IFluidHandlerItem fluidHandlerTarget = FluidUtil.getFluidHandler(target.copy().orElse(null));

            if (fluidHandlerStack != null && fluidHandlerTarget != null) {
                // check to make sure that both of the stacks can contain fluid
                fluidHandlerStack.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                fluidHandlerTarget.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                ItemStack emptyStack = fluidHandlerStack.getContainer();
                ItemStack emptyTarget = fluidHandlerTarget.getContainer();
                if (StackUtil.isMatchingItem(emptyStack, emptyTarget, true, true)) {
                    return true;
                }
            }
        } else if (type == Type.MATERIAL) {
            FluidStack fStack = FluidUtil.getFluidContained(stack).orElse(null);
            FluidStack fTarget = FluidUtil.getFluidContained(target).orElse(null);
            if (fStack != null && fTarget != null) {
                return fStack.isFluidEqual(fTarget);
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        if (type == Type.TYPE) {
            return FluidUtil.getFluidHandler(stack).orElse(null) != null;
        } else if (type == Type.MATERIAL) {
            return FluidUtil.getFluidContained(stack).orElse(null) != null;
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getClientExamples(Type type, @Nonnull ItemStack stack) {
        buildClientExampleList();
        if (type == Type.MATERIAL) {
            FluidStack fStack = FluidUtil.getFluidContained(stack).orElse(null);
            if (fStack != null) {
                NonNullList<ItemStack> examples = NonNullList.create();

                for (ItemStack potentialHolder : clientExampleHolders) {
                    potentialHolder = potentialHolder.copy();
                    IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(potentialHolder).orElse(null);
                    if (fluidHandler != null
                        && (fluidHandler.fill(fStack, IFluidHandler.FluidAction.EXECUTE) > 0 || fluidHandler.drain(fStack, IFluidHandler.FluidAction.SIMULATE) != null)) {
                        examples.add(fluidHandler.getContainer());
                    }
                }
                return examples;
            }
        } else if (type == Type.TYPE) {
            IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(stack.copy().orElse(null));

            if (fluidHandler != null) {
                NonNullList<ItemStack> examples = NonNullList.create();
                examples.add(stack);
                FluidStack contained = fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                if (contained != null) {
                    examples.add(fluidHandler.getContainer());
                    for (ItemStack potential : clientExampleHolders) {
                        IFluidHandlerItem potentialHolder = FluidUtil.getFluidHandler(potential).orElse(null);
                        if (potentialHolder.fill(contained, IFluidHandler.FluidAction.EXECUTE) > 0) {
                            examples.add(potentialHolder.getContainer());
                        }
                    }
                }
                return examples;
            }
        }
        return null;
    }
}
