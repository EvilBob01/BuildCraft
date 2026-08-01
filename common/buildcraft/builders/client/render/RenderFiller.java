/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;

import net.minecraftforge.client.model.animation.FastTESR;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.lib.client.render.laser.LaserBoxRenderer;

import buildcraft.builders.tile.TileFiller;
import buildcraft.core.client.BuildCraftLaserManager;

@OnlyIn(Dist.CLIENT)
public class RenderFiller extends FastTESR<TileFiller> {

    @Override
    public void renderTileEntityFast(TileFiller tile, double x, double y, double z, float partialTicks,
        int destroyStage, float partial, BufferBuilder bb) {
        Minecraft.getInstance().mcProfiler.push("bc");
        Minecraft.getInstance().mcProfiler.push("filler");

        Minecraft.getInstance().mcProfiler.push("main");
        if (tile.getBuilder() != null) {
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getLevel(), tile.getBlockPos(), x, y, z, partialTicks, bb);
        }
        Minecraft.getInstance().mcProfiler.pop();

        Minecraft.getInstance().mcProfiler.push("box");
        if (tile.markerBox) {
            bb.setTranslation(x - tile.getBlockPos().getX(), y - tile.getBlockPos().getY(), z - tile.getBlockPos().getZ());
            LaserBoxRenderer.renderLaserBoxDynamic(tile.box, BuildCraftLaserManager.STRIPES_WRITE, bb, true);
            bb.setTranslation(0, 0, 0);
        }
        Minecraft.getInstance().mcProfiler.pop();

        Minecraft.getInstance().mcProfiler.pop();
        Minecraft.getInstance().mcProfiler.pop();
    }

    @Override
    public boolean isGlobalRenderer(TileFiller te) {
        return true;
    }
}
