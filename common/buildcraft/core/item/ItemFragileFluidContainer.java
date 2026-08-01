package buildcraft.core.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
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
        CompoundTag stackTag = stack.getTag();
        CompoundTag fluidTag = (stackTag != null && stackTag.contains("fluid")) ? stackTag.getCompound("fluid") : null;
        if (fluidTag != null) {
            // TODO: FluidStack.loadFluidStackFromNBT was removed in NeoForge 1.21.1 — replace with new deserialization API
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidTag);
            if (fluid != null && fluid.getAmount() > 0) {
                tooltip.add(LocaleUtil.localizeFluidStaticAmount(fluid.getAmount(), MAX_FLUID_HELD));
            }
        }
    }

    @Override
    public void addFluidDrops(NonNullList<ItemStack> toDrop, FluidStack fluid) {
        if (fluid == null) {
            return;
        }
        int amount = fluid.getAmount();
        if (amount >= MAX_FLUID_HELD) {
            FluidStack fluid2 = fluid.copy();
            fluid2.setAmount(MAX_FLUID_HELD);
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
        nbt.put("fluid", fluid.saveAdditional(new CompoundTag()));
    }

    @Nullable
    static FluidStack getFluid(ItemStack container) {
        if (container.isEmpty()) {
            return null;
        }
        CompoundTag containerTag = container.getTag();
        CompoundTag fluidNbt = (containerTag != null && containerTag.contains("fluid")) ? containerTag.getCompound("fluid") : null;
        if (fluidNbt == null) {
            return null;
        }
        // TODO: FluidStack.loadFluidStackFromNBT was removed in NeoForge 1.21.1 — replace with new deserialization API
        return FluidStack.loadFluidStackFromNBT(fluidNbt);
    }

    public class FragileFluidHandler implements IFluidHandlerItem {

        @Nonnull
        private ItemStack container;

        public FragileFluidHandler(@Nonnull ItemStack container) {
            this.container = container;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || resource == null) {
                return null;
            }
            if (!fluid.isFluidEqual(resource)) {
                return null;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(container);
            if (fluid == null || maxDrain <= 0) {
                return null;
            }
            int toDrain = Math.min(maxDrain, fluid.getAmount());
            FluidStack f = new FluidStack(fluid, toDrain);
            if (action.execute()) {
                fluid.setAmount(fluid.getAmount() - toDrain);
                if (fluid.getAmount() <= 0) {
                    fluid = null;
                    container = StackUtil.EMPTY;
                } else {
                    setFluid(container, fluid);
                }
            }
            return f;
        }

        @Override
        public int getTanks() { return 1; }

        @Override
        public FluidStack getFluidInTank(int tank) {
            FluidStack f = ItemFragileFluidContainer.getFluid(container);
            return f != null ? f : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) { return MAX_FLUID_HELD; }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) { return false; }

        @Override
        public ItemStack getContainer() {
            return container;
        }
    }
}
