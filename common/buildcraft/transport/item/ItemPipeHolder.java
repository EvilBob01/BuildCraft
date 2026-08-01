/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import java.util.List;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.client.render.font.SpecialColourFontRenderer;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.registry.TagManager;

import buildcraft.transport.BCTransportBlocks;

public class ItemPipeHolder extends BlockItem implements IItemBuildCraft, IItemPipe {
    public final PipeDefinition definition;
    private final String id;
    private String unlocalizedName;

    protected ItemPipeHolder(PipeDefinition definition, String tagId) {
        super(BCTransportBlocks.pipeHolder, new Item.Properties());
        this.definition = definition;
        this.id = tagId;
        if (!"".equals(id)) {
            init();
        }
    }

    /** Creates a new {@link ItemPipeHolder} without requiring a tag. */
    public static ItemPipeHolder create(PipeDefinition definition) {
        return new ItemPipeHolder(definition, "");
    }

    /** Creates a new {@link ItemPipeHolder} with a tag that will be taken from {@link TagManager}. */
    public static ItemPipeHolder createAndTag(PipeDefinition definition) {
        ResourceLocation reg = definition.identifier;
        String tagId = "item.pipe." + reg.getNamespace() + "." + reg.getPath();
        return new ItemPipeHolder(definition, tagId);
    }

    public ItemPipeHolder registerWithPipeApi() {
        PipeApi.pipeRegistry.setItemForPipe(definition, this);
        return this;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        for (int i = 0; i <= 16; i++) {
            variants.put(i, new ModelResourceLocation("buildcrafttransport:pipe_item#inventory"));
        }
    }

    // BlockItem overrides these to point to the block — keep as BC internal utilities

    public ItemPipeHolder setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = "item." + unlocalizedName;
        return this;
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return unlocalizedName != null ? unlocalizedName : super.getDescriptionId(stack);
    }

    // Misc usefulness

    @Override
    @OnlyIn(Dist.CLIENT)
    public Font getFontRenderer(ItemStack stack) {
        return SpecialColourFontRenderer.INSTANCE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (unlocalizedName != null) {
            String tipName = "tip." + unlocalizedName.replace(".name", "").replace("item.", "");
            String localised = I18n.get(tipName);
            if (!localised.equals(tipName)) {
                tooltip.add(Component.literal(localised).withStyle(ChatFormatting.GRAY));
            }
        }
        if (definition.flowType == PipeApi.flowFluids) {
            PipeApi.FluidTransferInfo fti = PipeApi.getFluidTransferInfo(definition);
            tooltip.add(Component.literal(LocaleUtil.localizeFluidFlow(fti.transferPerTick)));
        } else if (definition.flowType == PipeApi.flowPower) {
            PipeApi.PowerTransferInfo pti = PipeApi.getPowerTransferInfo(definition);
            tooltip.add(Component.literal(LocaleUtil.localizeMjFlow(pti.transferPerTick)));
        } else if (definition.flowType == PipeApi.flowRf && PipeApi.flowRf != null) {
            PipeApi.RedstoneFluxTransferInfo pti = PipeApi.getRfTransferInfo(definition);
            tooltip.add(Component.literal(pti.transferPerTick + " RF/t"));//TODO: Locale!
        }
    }
}
