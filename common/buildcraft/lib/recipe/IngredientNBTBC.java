package buildcraft.lib.recipe;

import java.util.stream.Stream;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

// TODO (Phase 8 — Recipes): IngredientNBT (Forge 1.12) removed.
// In NeoForge 1.21 the correct approach is ICustomIngredient + a registered IngredientType with a Codec.
// This stub extends Ingredient directly (still permitted in NeoForge 1.21.1) and overrides test() for
// exact-item matching. It will not survive serialization; replace when assembly recipes are ported.
public class IngredientNBTBC extends Ingredient {

    private final ItemStack matchStack;

    @SuppressWarnings("deprecation")
    public IngredientNBTBC(ItemStack stack) {
        super(Stream.of(new Ingredient.ItemValue(stack)));
        this.matchStack = stack;
    }

    @Override
    public boolean test(ItemStack input) {
        return ItemStack.isSameItemSameTags(matchStack, input);
    }
}
