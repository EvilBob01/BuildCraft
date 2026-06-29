/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.plug;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;

import buildcraft.silicon.BCSiliconModels;
import buildcraft.silicon.item.ItemPluggableLens.LensData;

public enum ModelLensItem implements IBakedModel {
    INSTANCE;

    private static final List<List<BakedQuad>> cached = new ArrayList<>(34);

    public static void onModelBake() {
        cached.clear();
    }

    private static List<BakedQuad> getQuads(int damage) {
        if (damage < 0 || damage >= 34) damage = 0;
        if (cached.isEmpty()) {
            for (int i = 0; i < 34; i++) {
                List<BakedQuad> list = new ArrayList<>();
                LensData data = new LensData(i);
                MutableQuad[] cutout, translucent;
                Direction side = Direction.WEST;
                if (data.isFilter) {
                    cutout = BCSiliconModels.getFilterCutoutQuads(side, data.colour);
                    translucent = BCSiliconModels.getFilterTranslucentQuads(side, data.colour);
                } else {
                    cutout = BCSiliconModels.getLensCutoutQuads(side, data.colour);
                    translucent = BCSiliconModels.getLensTranslucentQuads(side, data.colour);
                }
                for (MutableQuad q : cutout) {
                    list.add(q.toBakedItem());
                }
                for (MutableQuad q : translucent) {
                    list.add(q.toBakedItem());
                }
                cached.add(list);
            }
        }
        return cached.get(damage);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, long rand) {
        return ImmutableList.of();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return LensOverride.LENS_OVERRIDE;
    }

    public static class LensOverride extends ItemOverrideList {
        public static final LensOverride LENS_OVERRIDE = new LensOverride();

        private LensOverride() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, Level world, LivingEntity entity) {
            return new ModelItemSimple(getQuads(stack.getItemDamage()), ModelItemSimple.TRANSFORM_PLUG_AS_ITEM, false);
        }
    }
}
