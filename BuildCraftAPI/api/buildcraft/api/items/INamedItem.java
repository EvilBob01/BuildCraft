package buildcraft.api.items;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

public interface INamedItem {
    String getName(@Nonnull ItemStack stack);

    boolean setName(@Nonnull ItemStack stack, String name);
}
