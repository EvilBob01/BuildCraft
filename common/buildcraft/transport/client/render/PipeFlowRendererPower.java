/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import javax.vecmath.Point3f;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;

import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowPower.Section;

@OnlyIn(Dist.CLIENT)
public enum PipeFlowRendererPower implements IPipeFlowRenderer<PipeFlowPower> {
    INSTANCE;

    @Override
    public void render(PipeFlowPower flow, double x, double y, double z, float partialTicks, BufferBuilder bb) {
        double centrePower = 0;
        double[] power = new double[6];
        for (Direction side : Direction.values()) {
            Section s = flow.getSection(side);
            int i = side.ordinal();
            power[i] = s.displayPower / (double) MjAPI.MJ;
            centrePower = Math.max(centrePower, power[i]);
        }

        bb.setTranslation(x, y, z);

        if (centrePower > 0) {
            for (Direction side : Direction.values()) {
                if (!flow.pipe.isConnected(side)) {
                    continue;
                }
                int i = side.ordinal();
                Section s = flow.getSection(side);
                double offset = computeOffset(s.clientDisplayFlowLast, s.clientDisplayFlow, partialTicks);
                renderSidePower(side, power[i], centrePower, offset, bb);
            }

            Vec3 offsetLast = flow.clientDisplayFlowCentreLast;
            Vec3 offsetThis = flow.clientDisplayFlowCentre;
            double offsetX = computeOffset(offsetLast.x, offsetThis.x, partialTicks);
            double offsetY = computeOffset(offsetLast.y, offsetThis.y, partialTicks);
            double offsetZ = computeOffset(offsetLast.z, offsetThis.z, partialTicks);

            renderCentrePower(centrePower, offsetX, offsetY, offsetZ, bb);
        }

        bb.setTranslation(0, 0, 0);
    }

    private static double computeOffset(double tick0, double tick1, float partialTicks) {
        if (tick0 + 8 < tick1) {
            // overflow upwards
            tick0 += 16;
        } else if (tick1 + 8 < tick0) {
            // overflow downwards
            tick1 += 16;
        }
        double offset = MathUtil.interp(partialTicks, tick0, tick1);
        if (offset >= 16) {
            offset -= 16;
        }
        return offset;
    }

    private static void renderSidePower(Direction side, double power, double centrePower, double offset,
        BufferBuilder bb) {
        if (power < 0) {
            return;
        }
        boolean overload = false;
        double radius = 0.248 * power;
        if (radius >= 0.248) {
            // overload = true;
            radius = 0.248;
        }

        TextureAtlasSprite sprite = (overload ? BCTransportSprites.POWER_FLOW_OVERLOAD : BCTransportSprites.POWER_FLOW)
            .getSprite();

        double centreRadius = 0.252 - (0.248 * centrePower);

        Vec3 centre = VecUtil.offset(VecUtil.VEC_HALF, side, 0.25 + 0.125 - centreRadius / 2);
        Vec3 radiusV = new Vec3(radius, radius, radius);
        radiusV = VecUtil.replaceValue(radiusV, side.getAxis(), 0.125 + centreRadius / 2);

        Point3f centreF = new Point3f((float) centre.x, (float) centre.y, (float) centre.z);
        Point3f radiusF = new Point3f((float) radiusV.x, (float) radiusV.y, (float) radiusV.z);

        UvFaceData uvs = new UvFaceData();
        for (Direction face : Direction.values()) {
            if (face == side.getOpposite()) {
                continue;
            }

            AABB box = new AABB(centre.subtract(radiusV).scale(0.5), centre.add(radiusV).scale(0.5));
            box = box.offset(VecUtil.offset(Vec3.ZERO, side, offset * side.getAxisDirection().getOffset() / 32));
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centreF, radiusF, uvs);
            quad.texFromSprite(sprite);
            quad.lighti(15, 15);
            quad.render(bb);
        }
    }

    private static void renderCentrePower(double power, double offsetX, double offsetY, double offsetZ, BufferBuilder bb) {
        boolean overload = false;
        float radius = 0.248f * (float) power;
        if (radius > 0.248f) {
            // overload = true;
            radius = 0.248f;
        }
        TextureAtlasSprite sprite = (overload ? BCTransportSprites.POWER_FLOW_OVERLOAD : BCTransportSprites.POWER_FLOW)
            .getSprite();

        Point3f centre = new Point3f(0.5f, 0.5f, 0.5f);
        Point3f radiusP = new Point3f(radius, radius, radius);

        UvFaceData uvs = new UvFaceData();

        for (Direction face : Direction.values()) {

            AABB box = new AABB(
                new Vec3(0.5 - radius, 0.5 - radius, 0.5 - radius).scale(0.5), //
                new Vec3(0.5 + radius, 0.5 + radius, 0.5 + radius).scale(0.5)//
            );
            box = box.offset(offsetX / 32.0, offsetY / 32.0, offsetZ / 32.0);
            ModelUtil.mapBoxToUvs(box, face, uvs);

            MutableQuad quad = ModelUtil.createFace(face, centre, radiusP, uvs);
            quad.texFromSprite(sprite);
            quad.lighti(15, 15);
            quad.render(bb);
        }
    }
}
