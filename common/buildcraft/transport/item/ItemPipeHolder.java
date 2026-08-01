/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import java.util.List;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.client.render.font.SpecialColourFontRenderer;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.registry.TagManager;

import buildcraft.transport.BCTransportBlocks;

public class ItemPipeHolder extends BlockItem implements IItemBuildCraft, IItemPipe {
    public final PipeDefinition definition;
    private final String id;
    private String unlocalizedName;
    private CreativeModeTab creativeTab;

    protected ItemPipeHolder(PipeDefinition definition, String tagId) {
        super(BCTransportBlocks.pipeHolder);
        this.definition = definition;
        this.id = tagId;
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
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
        String tagId = "item.pipe." + reg.getResourceDomain() + "." + reg.getResourcePath();
        return new ItemPipeHolder(definition, tagId);
    }

    public ItemPipeHolder registerWithPipeApi() {
        PipeApi.pipeRegistry.setItemForPipe(definition, this);
        return this;
    }

    @Override
    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
        }
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

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String colourComponent = "";
        int meta = stack.getMetadata();
        if (meta > 0 && meta <= 16) {
            DyeColor colour = DyeColor.byMetadata(meta - 1);
            colourComponent = ColourUtil.getTextFullTooltipSpecial(colour) + " ";
        }
        return colourComponent + super.getItemStackDisplayName(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public FontRenderer getFontRenderer(ItemStack stack) {
        return SpecialColourFontRenderer.INSTANCE;
    }

    // BlockItem overrides these to point to the block

    @Override
    public BlockItem setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = "item." + unlocalizedName;
        return this;
    }

    @Override
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return unlocalizedName;
    }

    @Override
    public Item setCreativeTab(CreativeModeTab tab) {
        creativeTab = tab;
        return this;
    }

    @Override
    public CreativeModeTab getCreativeTab() {
        return creativeTab;
    }

    // Misc usefulness

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, Level world, List<String> tooltip, ITooltipFlag flag) {
        String tipName = "tip." + unlocalizedName.replace(".name", "").replace("item.", "");
        String localised = I18n.format(tipName);
        if (!localised.equals(tipName)) {
            tooltip.add(ChatFormatting.GRAY + localised);
        }
        if (definition.flowType == PipeApi.flowFluids) {
            PipeApi.FluidTransferInfo fti = PipeApi.getFluidTransferInfo(definition);
            tooltip.add(LocaleUtil.localizeFluidFlow(fti.transferPerTick));
        } else if (definition.flowType == PipeApi.flowPower) {
            PipeApi.PowerTransferInfo pti = PipeApi.getPowerTransferInfo(definition);
            tooltip.add(LocaleUtil.localizeMjFlow(pti.transferPerTick));
        } else if (definition.flowType == PipeApi.flowRf && PipeApi.flowRf != null) {
            PipeApi.RedstoneFluxTransferInfo pti = PipeApi.getRfTransferInfo(definition);
            tooltip.add(pti.transferPerTick + " RF/t");//TODO: Locale!
        }
    }
}
