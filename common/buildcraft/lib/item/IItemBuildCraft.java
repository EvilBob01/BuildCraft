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

    /** TODO (Phase 4 — see ROADMAP.md): in 1.12.2 this set the unlocalized name, registry name, and
     * creative tab post-construction. In 1.21.1 the translation key and registry name are implicit from
     * the {@code DeferredRegister} entry, and creative tab membership is declared via a
     * {@code BuildCreativeModeTabContentsEvent} listener instead of a per-item setter. Left as a no-op
     * until that listener is wired up. */
    default void init() {
        // no-op: see TODO above
    }

    /** Sets up all of the model information for this item. This is called multiple times, and you *must* make sure that
     * you add all the same values each time. Use {@link #addVariant(TIntObjectHashMap, int, String)} to help get
     * everything correct. */
    @OnlyIn(Dist.CLIENT)
    default void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, 0, "");
    }

    default void addVariant(TIntObjectHashMap<ModelResourceLocation> variants, int meta, String suffix) {
        String tag = TagManager.get(id(), EnumTagType.MODEL_LOCATION);
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
