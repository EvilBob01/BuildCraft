/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileAutoWorkbenchItems extends TileAutoWorkbenchBase {
    public TileAutoWorkbenchItems(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 3, 3);
    }
}
