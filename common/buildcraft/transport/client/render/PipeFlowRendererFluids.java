/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import java.util.Arrays;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.api.transport.pipe.IPipeHolder;

import buildcraft.lib.client.render.fluid.FluidRenderer;
import buildcraft.lib.client.render.fluid.FluidSpriteType;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.RenderUtil.AutoTessellator;
import buildcraft.lib.misc.VecUtil;

import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.flow.PipeFlowFluids;

@OnlyIn(Dist.CLIENT)
public enum PipeFlowRendererFluids implements IPipeFlowRenderer<PipeFlowFluids> {
    INSTANCE;

    @Override
    public void render(PipeFlowFluids flow, double x, double y, double z, float partialTicks, BufferBuilder vb) {
        FluidStack forRender = flow.getFluidStackForRender();
        if (forRender == null) {
            return;
        }

        Profiler prof = Minecraft.getInstance().mcProfiler;
        prof.push("calc");

        boolean[] sides = new boolean[6];
        Arrays.fill(sides, IFluidHandler.FluidAction.EXECUTE);

        double[] amounts = flow.getAmountsForRender(partialTicks);
        Vec3[] offsets = flow.getOffsetsForRender(partialTicks);

        int blocklight = forRender.getFluid().getLuminosity(forRender);
        IPipeHolder holder = flow.pipe.getHolder();
        int combinedLight = holder.getPipeWorld().getCombinedLight(holder.getPipePos(), blocklight);

        FluidRenderer.vertex.lighti(combinedLight);

        try (AutoTessellator tess = RenderUtil.getThreadLocalUnusedTessellator()) {
            BufferBuilder fluidBuffer = tess.tessellator.getBuffer();
            fluidBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            fluidBuffer.setTranslation(x, y, z);

            boolean gas = forRender.getFluid().isGaseous(forRender);
            boolean horizontal = false;
            boolean vertical = flow.pipe.isConnected(gas ? Direction.DOWN : Direction.UP);

            prof.endStartSection("build");
            for (Direction face : Direction.values()) {
                double size = ((Pipe) flow.pipe).getConnectedDist(face);
                double amount = amounts[face.get3DDataValue()];
                if (face.getAxis() != Axis.Y) {
                    horizontal |= flow.pipe.isConnected(face) && amount > 0;
                }

                Vec3 center = VecUtil.offset(new Vec3(0.5, 0.5, 0.5), face, 0.245 + size / 2);
                Vec3 radius = new Vec3(0.24, 0.24, 0.24);
                radius = VecUtil.replaceValue(radius, face.getAxis(), 0.005 + size / 2);

                if (face.getAxis() == Axis.Y) {
                    double perc = amount / flow.capacity;
                    perc = Math.sqrt(perc);
                    radius = new Vec3(perc * 0.24, radius.y, perc * 0.24);
                }

                Vec3 offset = offsets[face.get3DDataValue()];
                if (offset == null) offset = Vec3.ZERO;
                center = center.add(offset);
                fluidBuffer.setTranslation(x - offset.x, y - offset.y, z - offset.z);

                Vec3 min = center.subtract(radius);
                Vec3 max = center.add(radius);

                if (face.getAxis() == Axis.Y) {
                    FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, fluidBuffer, sides);
                } else {
                    FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max,
                        fluidBuffer, sides);
                }
            }

            double amount = amounts[EnumPipePart.CENTER.get3DDataValue()];

            double horizPos = 0.26;

            Vec3 offset = offsets[EnumPipePart.CENTER.get3DDataValue()];
            if (offset == null) offset = Vec3.ZERO;
            fluidBuffer.setTranslation(x - offset.x, y - offset.y, z - offset.z);

            if (horizontal | !vertical) {
                Vec3 min = new Vec3(0.26, 0.26, 0.26);
                Vec3 max = new Vec3(0.74, 0.74, 0.74);

                min = min.add(offset);
                max = max.add(offset);

                FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, amount, flow.capacity, min, max,
                    fluidBuffer, sides);
                horizPos += (max.y - min.y) * amount / flow.capacity;
            }

            if (vertical && horizPos < 0.74) {
                double perc = amount / flow.capacity;
                perc = Math.sqrt(perc);
                double minXZ = 0.5 - 0.24 * perc;
                double maxXZ = 0.5 + 0.24 * perc;

                double yMin = gas ? 0.26 : horizPos;
                double yMax = gas ? 1 - horizPos : 0.74;

                Vec3 min = new Vec3(minXZ, yMin, minXZ);
                Vec3 max = new Vec3(maxXZ, yMax, maxXZ);
                min = min.add(offset);
                max = max.add(offset);

                FluidRenderer.renderFluid(FluidSpriteType.FROZEN, forRender, 1, 1, min, max, fluidBuffer, sides);
            }

            // gl state setup
            RenderHelper.disableStandardItemLighting();
            Minecraft.getInstance().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableCull();

            prof.endStartSection("draw");
            fluidBuffer.setTranslation(0, 0, 0);
            tess.tessellator.draw();
        }

        RenderHelper.enableStandardItemLighting();

        FluidRenderer.vertex.lighti(0xF, 0xF);
        prof.pop();

    }
}
