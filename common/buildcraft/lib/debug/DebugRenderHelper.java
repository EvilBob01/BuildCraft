/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import net.minecraftforge.client.model.ModelLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.DetachedRenderer.IDetachedRenderer;

@OnlyIn(Dist.CLIENT)
public enum DebugRenderHelper implements IDetachedRenderer {
    INSTANCE;

    private static final MutableQuad[] smallCuboid;

    static {
        smallCuboid = new MutableQuad[6];
        Tuple3f center = new Point3f(0.5f, 0.5f, 0.5f);
        Tuple3f radius = new Point3f(0.25f, 0.25f, 0.25f);

        for (Direction face : Direction.VALUES) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, null);
            quad.lightf(1, 1);
            smallCuboid[face.ordinal()] = quad;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(Player player, float partialTicks) {
        IAdvDebugTarget target = BCAdvDebugging.INSTANCE.targetClient;
        if (target == null) {
            return;
        } else if (!target.doesExistInWorld()) {
            // targetClient = null;
            // return;
        }
        IDetachedRenderer renderer = target.getDebugRenderer();
        if (renderer != null) {
            renderer.render(player, partialTicks);
        }
    }

    public static void renderAABB(BufferBuilder bb, AABB aabb, int colour) {
        bb.setTranslation(0, 0, 0);
        for (Direction face : Direction.VALUES) {
            MutableQuad quad = ModelUtil.createFace(
                face,
                new Point3f(
                    (float) aabb.getCenter().x,
                    (float) aabb.getCenter().y,
                    (float) aabb.getCenter().z
                ),
                new Point3f(
                    (float) (aabb.maxX - aabb.minX) / 2,
                    (float) (aabb.maxY - aabb.minY) / 2,
                    (float) (aabb.maxZ - aabb.minZ) / 2
                ),
                null
            );
            quad.lightf(1, 1);
            quad.texFromSprite(ModelLoader.White.INSTANCE);
            quad.colouri(colour);
            quad.render(bb);
        }
    }

    public static void renderSmallCuboid(BufferBuilder bb, BlockPos pos, int colour) {
        bb.setTranslation(pos.getX(), pos.getY(), pos.getZ());
        for (MutableQuad q : smallCuboid) {
            q.texFromSprite(ModelLoader.White.INSTANCE);
            q.colouri(colour);
            q.render(bb);
        }
        bb.setTranslation(0, 0, 0);
    }
}
