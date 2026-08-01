/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import com.google.common.collect.ImmutableSet;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.common.Tags;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.IngredientStack;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;

import buildcraft.transport.item.ItemPipeHolder;

@Mod.EventBusSubscriber(modid = BCTransport.MODID)
public class BCTransportRecipes {
    @GameRegistry.ObjectHolder("buildcraftsilicon:assembly_table")
    private static final Block SILICON_TABLE_ASSEMBLY = null;

    @SubscribeEvent
    public static void registerRecipes(RegisterEvent<IRecipe> event) {
        addPipeRecipe(BCTransportItems.pipeItemWood, "plankWood");
        addPipeRecipe(BCTransportItems.pipeItemCobble, "cobblestone");
        addPipeRecipe(BCTransportItems.pipeItemStone, "stone");
        addPipeRecipe(BCTransportItems.pipeItemQuartz, "blockQuartz");
        addPipeRecipe(BCTransportItems.pipeItemIron, "ingotIron");
        addPipeRecipe(BCTransportItems.pipeItemGold, "ingotGold");
        addPipeRecipe(BCTransportItems.pipeItemClay, Blocks.CLAY);
        addPipeRecipe(BCTransportItems.pipeItemSandstone,
            new ItemStack(Blocks.SANDSTONE, 1, OreDictionary.WILDCARD_VALUE));
        addPipeRecipe(BCTransportItems.pipeItemVoid, new ItemStack(Items.DYE, 1, DyeColor.BLACK.getDyeDamage()),
            "dustRedstone");
        addPipeRecipe(BCTransportItems.pipeItemObsidian, Blocks.OBSIDIAN);
        addPipeRecipe(BCTransportItems.pipeItemDiamond, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemLapis, Blocks.LAPIS_BLOCK);
        addPipeRecipe(BCTransportItems.pipeItemDaizuli, Blocks.LAPIS_BLOCK, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemDiaWood, "plankWood", Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemStripes, "gearGold");
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeItemEmzuli, Blocks.LAPIS_BLOCK);

        Item waterproof = BCTransportItems.waterproof;
        if (waterproof == null) {
            waterproof = Items.SLIME_BALL;
        }
        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipeFluidWood, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipeFluidCobble, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipeFluidStone, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipeFluidQuartz, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipeFluidIron, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipeFluidGold, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemClay, BCTransportItems.pipeFluidClay, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipeFluidSandstone, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemVoid, BCTransportItems.pipeFluidVoid, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemObsidian, BCTransportItems.pipeFluidObsidian, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipeFluidDiamond, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeFluidDiaWood, waterproof);

        String upgrade = "dustRedstone";
        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipePowerWood, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipePowerCobble, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipePowerStone, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipePowerQuartz, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipePowerIron, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipePowerGold, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipePowerSandstone, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipePowerDiamond, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipePowerDiaWood, upgrade);

        if (!BCTransportConfig.disableRfPipe) {
            addPipeUpgradeRecipe(BCTransportItems.pipePowerWood, BCTransportItems.pipeRfWood, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerCobble, BCTransportItems.pipeRfCobble, upgrade);
            addPipeUpgradeRecipe(BCTransportItems.pipePowerStone, BCTransportItems.pipeRfStone, upgrade);
        }

        if (BCTransportItems.wire != null) {
            for (DyeColor color : ColourUtil.COLOURS) {
                String name = StringUtilBC.formatDirect("wire-%s", color.getUnlocalizedName());
                ImmutableSet<IngredientStack> input = ImmutableSet.of(IngredientStack.of("dustRedstone"),
                    IngredientStack.of(ColourUtil.getDyeName(color)));
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 10_000 * MjAPI.MJ, input,
                    new ItemStack(BCTransportItems.wire, 8, color.getMetadata())));
            }
        }
    }

    private static void addPipeRecipe(ItemPipeHolder pipe, Object material) {
        addPipeRecipe(pipe, material, material);
    }

    /** TODO (Phase 8 — see ROADMAP.md): pipe crafting recipes used meta-based {@code ItemStack} variants
     * plus {@code ShapedOreRecipe}/{@code ForgeRegistries.RECIPES}, none of which exist in 1.21.1. Pipe
     * recipes must be re-authored as data pack JSON (one file per colour variant, since metadata subtypes
     * no longer exist). Stubbed to a no-op until that conversion happens. */
    private static void addPipeRecipe(ItemPipeHolder pipe, Object left, Object right) {
        // no-op: see TODO above
    }

    /** TODO (Phase 8 — see ROADMAP.md): same as {@link #addPipeRecipe}; upgrade recipes used
     * {@code ShapelessOreRecipe}/{@code ShapelessRecipes}, neither of which exist in 1.21.1. */
    private static void addPipeUpgradeRecipe(ItemPipeHolder from, ItemPipeHolder to, Object additional) {
        // no-op: see TODO above
    }
}
