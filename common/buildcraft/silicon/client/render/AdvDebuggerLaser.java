/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.debug.DebugRenderHelper;
import buildcraft.lib.misc.VolumeUtil;

import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.tile.TileLaser;

public class AdvDebuggerLaser implements DetachedRenderer.IDetachedRenderer {
    private static final int COLOUR_VISIBLE = 0xFF_99_FF_99;
    private static final int COLOUR_NOT_VISIBLE = 0xFF_11_11_99;

    private final BlockPos pos;
    private final Direction face;

    public AdvDebuggerLaser(TileLaser tile) {
        pos = tile.getPos();
        BlockState state = tile.getWorld().getBlockState(pos);
        face = state.getBlock() == BCSiliconBlocks.laser
            ? state.getValue(BuildCraftProperties.BLOCK_FACING_6)
            : null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(Player player, float partialTicks) {
        if (pos == null || face == null) {
            return;
        }
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        VolumeUtil.iterateCone(player.world, pos, face, 6, true, (world, start, p, visible) -> {
            int colour = visible ? COLOUR_VISIBLE : COLOUR_NOT_VISIBLE;
            DebugRenderHelper.renderSmallCuboid(bb, p, colour);
        });
        Tessellator.getInstance().draw();
    }
}
