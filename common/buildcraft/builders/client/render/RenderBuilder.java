/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;

import buildcraft.builders.tile.TileBuilder;
import buildcraft.core.client.BuildCraftLaserManager;

public class RenderBuilder extends FastTESR<TileBuilder> {
    private static final double OFFSET = 0.1;

    @Override
    public void renderTileEntityFast(@Nonnull TileBuilder tile, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer) {
        Minecraft.getInstance().mcProfiler.push("bc");
        Minecraft.getInstance().mcProfiler.push("builder");

        buffer.setTranslation(x - tile.getBlockPos().getX(), y - tile.getBlockPos().getY(), z - tile.getBlockPos().getZ());

        Minecraft.getInstance().mcProfiler.push("box");
        Box box = tile.getBox();
        LaserBoxRenderer.renderLaserBoxDynamic(box, BuildCraftLaserManager.STRIPES_WRITE, buffer, true);

        Minecraft.getInstance().mcProfiler.endStartSection("path");

        List<BlockPos> path = tile.path;
        if (path != null) {
            BlockPos last = null;
            for (BlockPos p : path) {
                if (last != null) {
                    Vec3 from = new Vec3(last.getX(), last.getY(), last.getZ()).add(VecUtil.VEC_HALF);
                    Vec3 to = new Vec3(p.getX(), p.getY(), p.getZ()).add(VecUtil.VEC_HALF);
                    Vec3 one = offset(from, to);
                    Vec3 two = offset(to, from);
                    LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE_DIRECTION, one, two, 1 / 16.1);
                    LaserRenderer_BC8.renderLaserDynamic(data, buffer);
                }
                last = p;
            }
        }

        Minecraft.getInstance().mcProfiler.pop();

        buffer.setTranslation(0, 0, 0);

        if (tile.getBuilder() != null) {
            RenderSnapshotBuilder.render(tile.getBuilder(), tile.getLevel(), tile.getBlockPos(), x, y, z, partialTicks, buffer);
        }

        Minecraft.getInstance().mcProfiler.pop();
        Minecraft.getInstance().mcProfiler.pop();
    }

    private static Vec3 offset(Vec3 from, Vec3 to) {
        Vec3 dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, OFFSET));
    }

    @Override
    public boolean isGlobalRenderer(TileBuilder te) {
        return true;
    }
}
