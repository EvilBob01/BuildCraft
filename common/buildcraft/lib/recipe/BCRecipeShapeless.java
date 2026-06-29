package buildcraft.lib.recipe;

import javax.annotation.Nonnull;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import net.minecraftforge.oredict.ShapelessOreRecipe;

public class BCRecipeShapeless extends ShapelessOreRecipe {
    private final boolean enabled;

    public BCRecipeShapeless(ResourceLocation group, NonNullList<Ingredient> input, @Nonnull ItemStack result, boolean enabled) {
        super(group, input, enabled ? result : ItemStack.EMPTY);
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
