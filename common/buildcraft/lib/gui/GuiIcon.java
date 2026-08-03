/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.core.render.ISprite;

import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;

@OnlyIn(Dist.CLIENT)
public class GuiIcon implements ISimpleDrawable {
    public final ISprite sprite;
    public final int textureSize;
    public final int width, height;

    public GuiIcon(ISprite sprite, int textureSize) {
        this.sprite = sprite;
        this.textureSize = textureSize;
        this.width = (int) (Math.abs(sprite.getInterpU(1) - sprite.getInterpU(0)) * textureSize);
        this.height = (int) (Math.abs(sprite.getInterpV(1) - sprite.getInterpV(0)) * textureSize);
    }

    public GuiIcon(ResourceLocation texture, double u, double v, double width, double height, int texSize) {
        this(new SpriteRaw(texture, u, v, width, height, texSize), texSize);
    }

    public GuiIcon(ResourceLocation texture, double u, double v, double width, double height) {
        this(texture, u, v, width, height, 256);
    }

    public GuiIcon offset(double u, double v) {
        SpriteRaw raw = (SpriteRaw) sprite;
        double uMin = raw.uMin + u / textureSize;
        double vMin = raw.vMin + v / textureSize;
        return new GuiIcon(new SpriteRaw(raw.location, uMin, vMin, raw.width, raw.height), textureSize);
    }

    public boolean containsGuiPos(double x, double y, IGuiPosition pos) {
        return new GuiRectangle(x, y, width, height).contains(pos);
    }

    public DynamicTexture createDynamicTexture(int scale) {
        return new DynamicTexture(width * scale, height * scale, true);
    }

    @Override
    public void drawAt(double x, double y) {
        this.drawScaledInside(x, y, this.width, this.height);
    }

    public void drawScaledInside(IGuiArea element) {
        drawScaledInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawScaledInside(double x, double y, double drawnWidth, double drawnHeight) {
        draw(sprite, x, y, x + drawnWidth, y + drawnHeight);
    }

    // TODO Phase 7: The old fixed-function-pipeline "arbitrary quadrilateral" trick (perspective-correct
    // texturing via a 4-component (s, t, r, q) texture coordinate, exploiting glBegin(GL_QUADS)'s
    // perspective divide) has no equivalent in the modern (core-profile, shader based) rendering
    // pipeline. This needs a dedicated shader to be reimplemented properly; for now it falls back to a
    // simple (non-perspective-correct) straight blit using the bounding rectangle of the 4 points.
    public void drawCustomQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double xMin = Math.min(Math.min(x1, x2), Math.min(x3, x4));
        double xMax = Math.max(Math.max(x1, x2), Math.max(x3, x4));
        double yMin = Math.min(Math.min(y1, y2), Math.min(y3, y4));
        double yMax = Math.max(Math.max(y1, y2), Math.max(y3, y4));
        draw(sprite, xMin, yMin, xMax, yMax);
    }

    private static double[] calcQ(double x1, double y1, double x2, double y2, double x3, double y3, double x4,
        double y4) {
        // Method contents taken from http://www.bitlush.com/posts/arbitrary-quadrilaterals-in-opengl-es-2-0
        // (or github https://github.com/bitlush/android-arbitrary-quadrilaterals-in-opengl-es-2-0 if the site is down)
        // this code is by Keith Wood

        double ax = x3 - x1;
        double ay = y3 - y1;
        double bx = x4 - x2;
        double by = y4 - y2;

        double cross = ax * by - ay * bx;

        if (cross != 0) {
            double cy = y1 - y2;
            double cx = x1 - x2;

            double s = (ax * cy - ay * cx) / cross;

            if (s > 0 && s < 1) {
                double t = (bx * cy - by * cx) / cross;

                if (t > 0 && t < 1) {
                    double q0 = 1 / (1 - t);
                    double q1 = 1 / (1 - s);
                    double q2 = 1 / t;
                    double q3 = 1 / s;
                    return new double[] { q0, q1, q2, q3 };
                }
            }
        }
        // in case (for some reason) some of the input was wrong then we will fail back to default rendering
        return new double[] { 1, 1, 1, 1 };
    }

    public void drawCutInside(IGuiArea element) {
        drawCutInside(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawCutInside(double x, double y, double displayWidth, double displayHeight) {
        sprite.bindTexture();

        displayWidth = Math.min(this.width, displayWidth);
        displayHeight = Math.min(this.height, displayHeight);

        double xMin = x;
        double yMin = y;

        double xMax = x + displayWidth;
        double yMax = y + displayHeight;

        double uMin = sprite.getInterpU(0);
        double vMin = sprite.getInterpV(0);

        double uMax = sprite.getInterpU(displayWidth / width);
        double vMax = sprite.getInterpV(displayHeight / height);

        drawQuad(xMin, yMin, xMax, yMax, uMin, vMin, uMax, vMax);
    }

    public void drawCustomScaledAt(IGuiArea area, double texU0, double texV0, double texU1, double texV1) {
        double xMin = area.getX();
        double yMin = area.getY();
        double xMax = xMin + area.getWidth();
        double yMax = yMin + area.getHeight();
        drawCustomScaledAt(xMin, yMin, xMax, yMax, texU0, texV0, texU1, texV1);
    }

    /** @param xMin Absolute horizontal position to start rendering at
     * @param yMin Absolute vertical position to start rendering at
     * @param xMax Absolute horizontal width to end rendering at
     * @param yMax Absolute vertical width to end rendering at
     * @param texU0 Minimum texture U co-ordinate, always between 0 and 1.
     * @param texV0 Minimum texture V co-ordinate, always between 0 and 1.
     * @param texU1 Maximum texture U co-ordinate, always between 0 and 1.
     * @param texV1 Maximum texture V co-ordinate, always between 0 and 1. */
    public void drawCustomScaledAt(double xMin, double yMin, double xMax, double yMax, double texU0, double texV0, double texU1, double texV1) {
        drawCustomScaledAt(sprite, xMin, yMin, xMax, yMax, texU0, texV0, texU1, texV1);
    }

    /** @param xMin Absolute horizontal position to start rendering at
     * @param yMin Absolute vertical position to start rendering at
     * @param xMax Absolute horizontal width to end rendering at
     * @param yMax Absolute vertical width to end rendering at
     * @param texU0 Minimum texture U co-ordinate, always between 0 and 1.
     * @param texV0 Minimum texture V co-ordinate, always between 0 and 1.
     * @param texU1 Maximum texture U co-ordinate, always between 0 and 1.
     * @param texV1 Maximum texture V co-ordinate, always between 0 and 1. */
    public static void drawCustomScaledAt(ISprite sprite, double xMin, double yMin, double xMax, double yMax, double texU0, double texV0, double texU1, double texV1) {
        sprite.bindTexture();

        double uMin = sprite.getInterpU(texU0);
        double vMin = sprite.getInterpV(texV0);

        double uMax = sprite.getInterpU(texU1);
        double vMax = sprite.getInterpV(texV1);

        drawQuad(xMin, yMin, xMax, yMax, uMin, vMin, uMax, vMax);
    }

    public static void drawAt(ISprite sprite, double x, double y, double size) {
        drawAt(sprite, x, y, size, size);
    }

    public static void drawAt(ISprite sprite, double x, double y, double width, double height) {
        draw(sprite, x, y, x + width, y + height);
    }

    public static void draw(ISprite sprite, double xMin, double yMin, double xMax, double yMax) {
        sprite.bindTexture();

        double uMin = sprite.getInterpU(0);
        double vMin = sprite.getInterpV(0);

        double uMax = sprite.getInterpU(1);
        double vMax = sprite.getInterpV(1);

        drawQuad(xMin, yMin, xMax, yMax, uMin, vMin, uMax, vMax);
    }

    /** Draws a simple axis-aligned textured quad using the modern (shader based) immediate-mode rendering path.
     * <p>
     * Note: {@link ISprite#bindTexture()} must have already been called by the caller before this is invoked. */
    private static void drawQuad(double xMin, double yMin, double xMax, double yMax, double uMin, double vMin,
        double uMax, double vMax) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder vb = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        vertex(vb, xMin, yMax, uMin, vMax);
        vertex(vb, xMax, yMax, uMax, vMax);
        vertex(vb, xMax, yMin, uMax, vMin);
        vertex(vb, xMin, yMin, uMin, vMin);

        BufferUploader.drawWithShader(vb.buildOrThrow());
    }

    private static void vertex(BufferBuilder vb, double x, double y, double u, double v) {
        vb.addVertex((float) x, (float) y, 0).setUv((float) u, (float) v);
    }
}
