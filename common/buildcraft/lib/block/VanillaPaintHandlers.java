/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import net.minecraft.world.level.block.Block;
// TODO (Phase 8): verify modern equivalent - since 1.13 colored blocks (glass, terracotta) are
// separate Block instances per color (e.g. Blocks.WHITE_STAINED_GLASS) rather than a single block
// with a COLOR BlockState property. This class's double-typed-paint-handler approach likely needs
// a full rework rather than a straight import rename.
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.InteractionResult;

import buildcraft.api.blocks.CustomPaintHelper;
import buildcraft.api.blocks.ICustomPaintHandler;

public class VanillaPaintHandlers {

    public static void fmlInit() {
        // TODO (Phase 8): since 1.13, colored glass/terracotta are separate Block instances per
        // color rather than one block with a COLOR BlockState property, so this double-typed paint
        // handler concept needs a full rework. Disabled for now to allow compilation.
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
