package buildcraft.core.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.items.IItemFluidShard;

import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;

public class ItemFragileFluidContainer extends ItemBC_Neptune implements IItemFluidShard {

    // Half of a bucket
    public static final int MAX_FLUID_HELD = 500;

    public ItemFragileFluidContainer(String id) {
        super(id);
        setMaxStackSize(1);
    }

    /* TODO (Phase 6 — see ROADMAP.md): NeoForge removed Item#initCapabilities. Item capabilities are now registered
     * externally, once, from a RegisterCapabilitiesEvent listener on the mod bus:
     *
     *   event.registerItem(Capabilities.FluidHandler.ITEM,
     *       (stack, ctx) -> new FragileFluidHandler(stack), BCCoreItems.fragileFluidShard);
     *
     * FragileFluidHandler below is unchanged and ready to be used as that factory's return value; only the
     * registration call site still needs writing. */

    @Override
    protected void addSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        // Never allow this to be displayed in a creative tab -- we don't want to list every single fluid...
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluid = getFluid(stack);

        String localized;

        if (fluid == null) {
            localized = "ERROR! NULL FLUID!";
        } else if (fluid.getFluid() instanceof BCFluid) {
            BCFluid bcFluid = (BCFluid) fluid.getFluid();
            if (bcFluid.isHeatable()) {
                // Add the heatable bit to the end of the name
                localized = bcFluid.getBareLocalizedName(fluid);
                String whole = LocaleUtil.localize(getUnlocalizedName() + ".name", localized);
                return LocaleUtil.localize("buildcraft.fluid.heat_" + bcFluid.getHeatValue(), whole);
            } else {
                localized = fluid.getLocalizedName();
            }
        } else {
            localized = fluid.getLocalizedName();
        }
        return LocaleUtil.localize(getUnlocalizedName() + ".name", localized);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundTag fluidTag = stack.getSubCompound("fluid");
        if (fluidTag != null) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidTag);
            if (fluid != null && fluid.amount > 0) {
                tooltip.add(LocaleUtil.localizeFluidStaticAmount(fluid.amount, MAX_FLUID_HELD));
            }
        }
    }

    @Override
    public void addFluidDrops(NonNullList<ItemStack> toDrop, FluidStack fluid) {
        if (fluid == null) {
            return;
        }
        int amount = fluid.amount;
        if (amount >= MAX_FLUID_HELD) {
            FluidStack fluid2 = fluid.copy();
            fluid2.amount = MAX_FLUID_HELD;
            while (amount >= MAX_FLUID_HELD) {
                ItemStack stack = new ItemStack(this);
                setFluid(stack, fluid2);
                amount -= MAX_FLUID_HELD;
                toDrop.add(stack);
            }
        }
        if (amount > 0) {
            ItemStack stack = new ItemStack(this);
            setFluid(stack, new FluidStack(fluid, amount));
            toDrop.add(stack);
        }
    }

    static void setFluid(ItemStack container, FluidStack fluid) {
        CompoundTag nbt = NBTUtilBC.getItemData(container);
        nbt.setTag("fluid", fluid.saveAdditional(new CompoundTag()));
    }

    @Nullable
    static FluidStack getFluid(ItemStack container) {
        if (container.isEmpty()) {
            return null;
        }
        CompoundTag fluidNbt = container.getSubCompound("fluid");
        if (fluidNbt == null) {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(fluidNbt);
    }

    public class FragileFluidHandler implements IFluidHandlerItem {

        @Nonnull
        private ItemStack container;

        public FragileFluidHandler(@Nonnull ItemStack container) {
            this.container = container;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[] {
                new FluidTankProperties(getFluid(container), MAX_FLUID_HELD, false, true) };
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || resource == null) {
                return null;
            }
            if (!fluid.isFluidEqual(resource)) {
                return null;
            }
            return drain(resource.amount, doDrain);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || maxDrain <= 0) {
                return null;
            }
            int toDrain = Math.min(maxDrain, fluid.amount);
            FluidStack f = new FluidStack(fluid, toDrain);
            if (doDrain) {
                fluid.amount -= toDrain;
                if (fluid.amount <= 0) {
                    fluid = null;
                    container = StackUtil.EMPTY;
                } else {
                    setFluid(container, fluid);
                }
            }
            return f;
        }

        @Override
        public ItemStack getContainer() {
            return container;
        }
    }
}
