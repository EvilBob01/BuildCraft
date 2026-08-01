/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

import buildcraft.lib.client.render.laser.LaserBoxRenderer;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderArchitectTable extends TileEntitySpecialRenderer<TileArchitectTable> {
    @Override
    public void render(TileArchitectTable tile, double x, double y, double z, float partialTicks, int destroyStage,
        float partial) {
        if (!tile.markerBox) {
            return;
        }
        Minecraft.getInstance().mcProfiler.startSection("bc");
        Minecraft.getInstance().mcProfiler.startSection("architect_table");

        GL11.glPushMatrix();
        GL11.glTranslated(x - tile.getBlockPos().getX(), y - tile.getBlockPos().getY(), z - tile.getBlockPos().getZ());
        RenderHelper.disableStandardItemLighting();

        Minecraft.getInstance().mcProfiler.startSection("box");
        LaserBoxRenderer.renderLaserBoxStatic(tile.box, BuildCraftLaserManager.STRIPES_READ, true);
        Minecraft.getInstance().mcProfiler.endSection();

        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();

        Minecraft.getInstance().mcProfiler.endSection();
        Minecraft.getInstance().mcProfiler.endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileArchitectTable te) {
        return true;
    }
}
