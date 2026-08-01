package buildcraft.api.items;

import javax.annotation.Nonnull;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface INamedItem {
    Component getName(@Nonnull ItemStack stack);

    boolean setName(@Nonnull ItemStack stack, String name);
}
