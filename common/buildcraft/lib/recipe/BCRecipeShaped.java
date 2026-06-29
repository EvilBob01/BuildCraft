package buildcraft.lib.recipe;

import javax.annotation.Nonnull;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class BCRecipeShaped extends ShapedOreRecipe {
    private final boolean enabled;

    public BCRecipeShaped(ResourceLocation group, @Nonnull ItemStack result, CraftingHelper.ShapedPrimer primer, boolean enabled) {
        super(group, enabled ? result : ItemStack.EMPTY, primer);
        this.enabled = enabled;
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inventory, @Nonnull Level world) {
        return enabled && super.matches(inventory, world);
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return enabled ? super.getIngredients() : NonNullList.create();
    }

    @Override
    public boolean isDynamic() {
        return !enabled;
    }
}
