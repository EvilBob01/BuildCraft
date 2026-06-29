/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.item;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.world.item.Item;

import net.minecraftforge.client.model.ModelLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.core.BCLog;

import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;

public interface IItemBuildCraft {
    String id();

    default void init() {
        Item thisItem = (Item) this;
        thisItem/* setUnlocalizedName removed in 1.21 */, EnumTagType.UNLOCALIZED_NAME));
        thisItem/* setRegistryName removed - use registry directly */, EnumTagType.REGISTRY_NAME));
        thisItem/* setCreativeTab removed - use CreativeModeTab */, EnumTagType.CREATIVE_TAB)));
    }

    /** Sets up all of the model information for this item. This is called multiple times, and you *must* make sure that
     * you add all the same values each time. Use {@link #addVariant(TIntObjectHashMap, int, String)} to help get
     * everything correct. */
    @OnlyIn(Dist.CLIENT)
    default void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, 0, "");
    }

    default void addVariant(TIntObjectHashMap<ModelResourceLocation> variants, int meta, String suffix) {
        String tag = TagManager.getTag(id(), EnumTagType.MODEL_LOCATION);
        variants.put(meta, new ModelResourceLocation(tag + suffix, "inventory"));
    }

    @OnlyIn(Dist.CLIENT)
    default void registerVariants() {
        Item thisItem = (Item) this;
        TIntObjectHashMap<ModelResourceLocation> variants = new TIntObjectHashMap<>();
        addModelVariants(variants);
        for (int key : variants.keys()) {
            ModelResourceLocation variant = variants.get(key);
            if (RegistryConfig.DEBUG) {
                BCLog.logger.info("[lib.registry][" + thisItem.builtInRegistryHolder().key().location() + "] Registering a variant " + variant
                    + " for damage " + key);
            }
            ModelLoader.setCustomModelResourceLocation(thisItem, key, variant);
        }
    }
}
