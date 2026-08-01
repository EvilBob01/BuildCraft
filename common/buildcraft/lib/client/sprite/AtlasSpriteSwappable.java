/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.fml.client.FMLClientHandler;

import buildcraft.lib.BCLibConfig;

/** Provides the basic implementation for */
public abstract class AtlasSpriteSwappable extends TextureAtlasSprite {
    private TextureAtlasSprite current;
    private boolean needsSwapping = true;

    public AtlasSpriteSwappable(String baseName) {
        super(baseName);
        if (!BCLibConfig.useSwappableSprites) {
            throw new IllegalStateException(
                "The user has disabled swappable sprites but some code still called it's constructor anyway!\n"
                    + "(Note that this is a *mod* bug, not a user configuration issue - there are legitimate reasons\n"
                    + "to disabled swappable sprites normally, like for optifine compat)");
        }
    }

    @Override
    public boolean hasAnimationMetadata() {
        return true;
    }

    @Override
    public void updateAnimation() {
        if (current == null) {
            copyFrom(Minecraft.getInstance().getTextureMapBlocks().getMissingSprite());
            return;
        }
        Profiler p = Minecraft.getInstance().mcProfiler;
        // MAPPING: func_194340_a: Profiler.startSection
        p.func_194340_a(getClass()::getSimpleName);
        if (needsSwapping) {
            p.push("copy");
            current.copyFrom(this);
            p.pop();
        }
        if (current.hasAnimationMetadata() && BCLibConfig.enableAnimatedSprites) {
            p.push("update");
            p.push(getIconName());
            current.updateAnimation();
            p.pop();
            p.pop();
        } else if (needsSwapping) {
            p.push("swap");
            TextureUtil.uploadTextureMipmap(current.getFrameTextureData(0), current.getIconWidth(),
                current.getIconHeight(), current.getOriginX(), current.getOriginY(), false, false);
            p.pop();
        }
        needsSwapping = false;
        p.pop();
    }

    public boolean swapWith(TextureAtlasSprite other) {
        if (current != other && (current == null || other != null)) {
            current = other;
            if (width == 0) {
                this.width = other.getIconWidth();
                this.height = other.getIconHeight();
            }
            generateMipmaps(Minecraft.getInstance().gameSettings.mipmapLevels);
            needsSwapping = true;
            return true;
        }
        return false;
    }

    /** Actually loads the given location. Note that subclasses should override this, and possibly call
     * {@link #loadSprite(IResourceManager, String, ResourceLocation, boolean)} to load all of the possible variants. */
    @Override
    public boolean load(IResourceManager manager, ResourceLocation location,
        Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        TextureAtlasSprite sprite = loadSprite(manager, super.getIconName(), location, true);
        if (sprite != null) {
            swapWith(sprite);
        }
        return false;
    }

    public static TextureAtlasSprite loadSprite(String name, ResourceLocation location, boolean careIfMissing) {
        return loadSprite(Minecraft.getInstance().getResourceManager(), name, location, careIfMissing);
    }

    public static TextureAtlasSprite loadSprite(IResourceManager manager, String name, ResourceLocation location,
        boolean careIfMissing) {
        // Load the initial variant
        TextureAtlasSprite sprite = makeAtlasSprite(ResourceLocation.parse(name));
        try {
            // Copied almost directly from TextureMap.
            PngSizeInfo pngsizeinfo = PngSizeInfo.makeFromResource(manager.getResource(location));
            try (IResource iresource = manager.getResource(location)) {
                boolean flag = iresource.getMetadata("animation") != null;
                sprite.loadSprite(pngsizeinfo, flag);
                sprite.loadSpriteFrames(iresource, Minecraft.getInstance().gameSettings.mipmapLevels + 1);
                return sprite;
            }
        } catch (IOException io) {
            if (careIfMissing) {
                // Do the same as forge - track the missing texture for later rather than printing out the error.
                FMLClientHandler.instance().trackMissingTexture(location);
            }
            return null;
        }
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public void generateMipmaps(int level) {
        if (current != null) {
            current.generateMipmaps(level);
        }
    }

    // Overrides

    @Override
    public int getFrameCount() {
        if (current == null) {
            return 0;
        }
        return current.getFrameCount();
    }

    @Override
    public void copyFrom(TextureAtlasSprite from) {
        super.copyFrom(from);
        if (current == null) {
            current = from;
        } else {
            current.copyFrom(from);
        }
    }

    @Override
    public int[][] getFrameTextureData(int index) {
        if (current == null) {
            return new int[1][1];
        }
        return current.getFrameTextureData(index);
    }

    @Override
    public void setFramesTextureData(List<int[][]> newFramesTextureData) {
        // NO-OP
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + super.toString();
    }
}
