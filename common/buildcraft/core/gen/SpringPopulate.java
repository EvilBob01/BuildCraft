/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.gen;

import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.neoforged.bus.api.Event.Result;
import net.neoforged.bus.api.SubscribeEvent;

import buildcraft.api.enums.EnumSpring;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.core.BCCoreBlocks;

public class SpringPopulate {

    @SubscribeEvent
    public void populate(PopulateChunkEvent.Post event) {

        Level world = event.getLevel();
        Random rand = event.getRand();
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();
        boolean doGen = TerrainGen.populate(event.getGen(), world, rand, chunkX, chunkZ, event.isHasVillageGenerated(), PopulateChunkEvent.Populate.EventType.CUSTOM);

        if (!doGen || !EnumSpring.WATER.canGen) {
            event.setResult(Result.ALLOW);
            return;
        }

        // shift to world coordinates
        int worldX = chunkX << 4;
        int worldZ = chunkZ << 4;

        doPopulate(world, rand, worldX, worldZ);
    }

    private static void doPopulate(Level world, Random random, int x, int z) {
        int dimId = world.provider.getDimension();
        // No water springs will generate in the Nether or End.
        if (dimId == -1 || dimId == 1) {
            return;
        }

        // A spring will be generated every 40th chunk.
        if (random.nextFloat() > 0.025f) {
            return;
        }

        int posX = x + random.nextInt(16);
        int posZ = z + random.nextInt(16);

        for (int i = 0; i < 5; i++) {
            BlockPos pos = new BlockPos(posX, i, posZ);
            Block candidate = world.getBlockState(pos).getBlock();

            if (candidate != Blocks.BEDROCK) {
                continue;
            }

            // Handle flat bedrock maps
            int y = i > 0 ? i : i - 1;

            BlockState springState = BCCoreBlocks.spring.defaultBlockState();
            springState = springState.setValue(BuildCraftProperties.SPRING_TYPE, EnumSpring.WATER);

            world.setBlock(new BlockPos(posX, y, posZ), springState, 3);

            for (int j = y + 2; j < world.getHeight(); j++) {
                if (world.isEmptyBlock(new BlockPos(posX, j, posZ))) {
                    break;
                } else {
                    world.setBlock(new BlockPos(posX, j, posZ), Blocks.WATER.defaultBlockState(), 3);
                }
            }

            break;
        }
    }
}
