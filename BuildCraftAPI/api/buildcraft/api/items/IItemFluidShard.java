package buildcraft.api.items;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IItemFluidShard {
    void addFluidDrops(NonNullList<ItemStack> toDrop, @Nullable FluidStack fluid);
}
