/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.InteractionResult;

import buildcraft.api.blocks.CustomPaintHelper;
import buildcraft.api.blocks.ICustomPaintHandler;

public class VanillaPaintHandlers {

    public static void fmlInit() {
        registerDoubleTypedHandler(Blocks.GLASS, Blocks.STAINED_GLASS, BlockStainedGlass.COLOR);
        registerDoubleTypedHandler(Blocks.GLASS_PANE, Blocks.STAINED_GLASS_PANE, BlockStainedGlassPane.COLOR);
        registerDoubleTypedHandler(Blocks.HARDENED_CLAY, Blocks.STAINED_HARDENED_CLAY, BlockColored.COLOR);
    }

    private static void registerDoubleTypedHandler(Block clear, Block dyed, Property<DyeColor> colourProp) {
        ICustomPaintHandler handler = createDoubleTypedPainter(clear, dyed, colourProp);
        CustomPaintHelper.INSTANCE.registerHandler(clear, handler);
        CustomPaintHelper.INSTANCE.registerHandler(dyed, handler);
    }

    public static ICustomPaintHandler createDoubleTypedPainter(Block clear, Block dyed, Property<DyeColor> colourProp) {
        return (world, pos, state, hitPos, hitSide, to) -> {
            if (state.getBlock() == clear) {
                // We are currently clear
                if (to == null) {
                    return InteractionResult.FAIL;
                }
                BlockState painted = dyed.getDefaultState().withProperty(colourProp, to);
                world.setBlock(pos, painted);
                return InteractionResult.SUCCESS;
            } else if (state.getBlock() == dyed) {
                if (to == state.getValue(colourProp)) {
                    return InteractionResult.FAIL;
                }
                if (to == null) {
                    state = clear.getDefaultState();
                } else {
                    state = state.withProperty(colourProp, to);
                }
                world.setBlock(pos, state);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        };
    }
}
