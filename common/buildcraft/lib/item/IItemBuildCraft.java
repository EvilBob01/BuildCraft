/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.item;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;

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

    /** Sets up all of the model information for this item. Phase 7 (rendering) stub — model registration
     * is handled via data providers in 1.21.1, not ModelLoader.setCustomModelResourceLocation. */
    @OnlyIn(Dist.CLIENT)
    default void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        // TODO Phase 7: port to NeoForge 1.21.1 model provider system
    }

    default void addVariant(TIntObjectHashMap<ModelResourceLocation> variants, int meta, String suffix) {
        // TODO Phase 7: port to NeoForge 1.21.1 model provider system
    }

    @OnlyIn(Dist.CLIENT)
    default void registerVariants() {
        // TODO Phase 7 (rendering): ModelLoader.setCustomModelResourceLocation removed in NeoForge 1.21.1;
        // model registration needs to move to a RegisterClientReloadListenersEvent / model provider.
    }
}
