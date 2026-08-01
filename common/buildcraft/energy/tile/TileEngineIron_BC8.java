/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import net.minecraft.core.HolderLookup;
import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import buildcraft.lib.net.MessageContext;
import net.neoforged.api.distmarker.Dist;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager.IDirtyFuel;
import buildcraft.api.fuels.ISolidCoolant;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IItemPipe;

import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.energy.BCEnergyGuis;

public class TileEngineIron_BC8 extends TileEngineBase_BC8 {
    public static final int MAX_FLUID = 10_000;

    public static final double COOLDOWN_RATE = 0.05;
    public static final int MAX_COOLANT_PER_TICK = 40;

    public final Tank tankFuel = new Tank("fuel", MAX_FLUID, this, this::isValidFuel);
    public final Tank tankCoolant = new Tank("coolant", MAX_FLUID, this, this::isValidCoolant) {
        @Override
        protected FluidGetResult map(ItemStack stack, int space) {
            ISolidCoolant coolant = BuildcraftFuelRegistry.coolant.getSolidCoolant(stack);
            if (coolant == null) {
                return super.map(stack, space);
            }
            FluidStack fluidCoolant = coolant.getFluidFromSolidCoolant(stack);
            if (fluidCoolant == null || fluidCoolant.getAmount() <= 0 || fluidCoolant.getAmount() > space) {
                return super.map(stack, space);
            }
            return new FluidGetResult(StackUtil.EMPTY, fluidCoolant);
        }
    };
    public final Tank tankResidue = new Tank("residue", MAX_FLUID, this, this::isResidue);
    private final IFluidHandlerAdv fluidHandler = new InternalFluidHandler();

    private int penaltyCooling = 0;
    private boolean lastPowered = false;
    private double burnTime;
    private double residueAmount = 0;
    private IFuel currentFuel;

    public TileEngineIron_BC8() {
        tankManager.addAll(tankFuel, tankCoolant, tankResidue);

        // TODO: Auto list of example fuels!
        tankFuel.helpInfo = new ElementHelpInfo(tankFuel.helpInfo.title, 0xFF_FF_33_33, Tank.DEFAULT_HELP_KEY, null,
            "buildcraft.help.tank.fuel");

        // TODO: Auto list of example coolants!
        tankCoolant.helpInfo = new ElementHelpInfo(tankCoolant.helpInfo.title, 0xFF_55_55_FF, Tank.DEFAULT_HELP_KEY,
            null, "buildcraft.help.tank.coolant");

        tankResidue.helpInfo = new ElementHelpInfo(tankResidue.helpInfo.title, 0xFF_AA_33_AA, Tank.DEFAULT_HELP_KEY,
            null, "buildcraft.help.tank.residue");

        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, fluidHandler, EnumPipePart.VALUES);
    }

    // BlockEntity overrides

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putInt("penaltyCooling", penaltyCooling);
        nbt.putDouble("burnTime", burnTime);
        nbt.putDouble("residueAmount", residueAmount);
        return nbt;
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        penaltyCooling = nbt.getInt("penaltyCooling");
        burnTime = nbt.getDouble("burnTime");
        residueAmount = Math.max(0, nbt.getDouble("residueAmount"));
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Dist.CLIENT) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.readData(buffer);
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.writeData(buffer);
            }
        }
    }

    // TileEngineBase overrides

    @Override
    public boolean onActivated(Player player, InteractionHand hand, Direction side, float hitX, float hitY,
        float hitZ) {
        ItemStack current = player.getItemInHand(hand).copy();
        if (super.onActivated(player, hand, side, hitX, hitY, hitZ)) {
            return true;
        }
        if (!current.isEmpty()) {
            if (EntityUtil.getWrenchHand(player) != null) {
                return false;
            }
            if (current.getItem() instanceof IItemPipe) {
                return false;
            }
        }
        if (!level.isClientSide) {
            BCEnergyGuis.ENGINE_IRON.openGUI(player, getBlockPos());
        }
        return true;
    }

    @Override
    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:
                return 0.04;
            case GREEN:
                return 0.05;
            case YELLOW:
                return 0.06;
            case RED:
                return 0.07;
            default:
                return 0;
        }
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        FluidStack fuel = tankFuel.getFluid();
        return fuel != null && fuel.getAmount() > 0 && penaltyCooling == 0 && isRedstonePowered;
    }

    @Override
    protected void burn() {
        final FluidStack fuel = this.tankFuel.getFluid();
        if (currentFuel == null || !currentFuel.getFluid().isFluidEqual(fuel)) {
            currentFuel = BuildcraftFuelRegistry.fuel.getFuel(fuel);
        }

        if (fuel == null || currentFuel == null) {
            return;
        }

        if (penaltyCooling <= 0) {
            if (isRedstonePowered) {
                lastPowered = true;

                if (burnTime > 0 || fuel.getAmount() > 0) {
                    if (burnTime > 0) {
                        burnTime--;
                    }
                    if (burnTime <= 0) {
                        if (fuel.getAmount() > 0) {
                            fuel.setAmount(fuel.getAmount() - 1);
                            burnTime += currentFuel.getTotalBurningTime() / 1000.0;

                            // If we also produce residue then put it out too
                            if (currentFuel instanceof IDirtyFuel) {
                                IDirtyFuel dirtyFuel = (IDirtyFuel) currentFuel;
                                FluidStack residueFluid = dirtyFuel.getResidue().copy();
                                residueAmount += residueFluid.getAmount() / 1000.0;
                                if (residueAmount >= 1) {
                                    residueFluid.setAmount(Mth.floor(residueAmount));
                                    residueAmount -= tankResidue.fill(residueFluid, true);
                                } else if (tankResidue.getFluid() == null) {
                                    residueFluid.setAmount(0);
                                    tankResidue.setFluid(residueFluid);
                                }
                            }
                        } else {
                            tankFuel.setFluid(null);
                            currentFuel = null;
                            currentOutput = 0;
                            return;
                        }
                    }
                    currentOutput = currentFuel.getPowerPerCycle(); // Comment out for constant power
                    addPower(currentFuel.getPowerPerCycle());
                    heat += currentFuel.getPowerPerCycle() * HEAT_PER_MJ / MjAPI.MJ;// * getBiomeTempScalar();
                }
            } else if (lastPowered) {
                lastPowered = false;
                penaltyCooling = 10;
                // 10 tick of penalty on top of the cooling
            }
        }

        if (burnTime <= 0 && fuel.getAmount() <= 0) {
            tankFuel.setFluid(null);
        }
    }

    @Override
    public void updateHeatLevel() {
        double target;
        if (heat > MIN_HEAT && (penaltyCooling > 0 || !isRedstonePowered)) {
            heat -= COOLDOWN_RATE;
            target = MIN_HEAT;
        } else if (heat > IDEAL_HEAT) {
            target = IDEAL_HEAT;
        } else {
            target = heat;
        }

        if (target != heat) {
            // coolEngine(target)
            {
                double coolingBuffer = 0;
                double extraHeat = heat - target;

                if (extraHeat > 0) {
                    // fillCoolingBuffer();
                    {
                        if (tankCoolant.getFluidAmount() > 0) {
                            float coolPerMb =
                                BuildcraftFuelRegistry.coolant.getDegreesPerMb(tankCoolant.getFluid(), (float) heat);
                            if (coolPerMb > 0) {
                                int coolantAmount = Math.min(MAX_COOLANT_PER_TICK, tankCoolant.getFluidAmount());
                                float cooling = coolPerMb;
                                // cooling /= getBiomeTempScalar();
                                coolingBuffer += coolantAmount * cooling;
                                tankCoolant.drain(coolantAmount, true);
                            }
                        }
                    }
                    // end
                }

                // if (coolingBuffer >= extraHeat) {
                // coolingBuffer -= extraHeat;
                // heat -= extraHeat;
                // return;
                // }

                heat -= coolingBuffer;
                coolingBuffer = 0.0f;
            }
            // end
            getPowerStage();
        }

        if (heat <= MIN_HEAT && penaltyCooling > 0) {
            penaltyCooling--;
        }

        if (heat <= MIN_HEAT) {
            heat = MIN_HEAT;
        }
    }

    @Override
    public boolean isActive() {
        return penaltyCooling <= 0;
    }

    @Override
    public long getMaxPower() {
        return 10_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerReceived() {
        return 2_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 500 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 4;
    }

    @Override
    protected int getMaxChainLength() {
        return 4;
    }

    @Override
    public long getCurrentOutput() {
        if (currentFuel == null) {
            return 0;
        } else {
            return currentFuel.getPowerPerCycle();
        }
    }

    // Fluid related

    private boolean isValidFuel(FluidStack fluid) {
        return BuildcraftFuelRegistry.fuel.getFuel(fluid) != null;
    }

    private boolean isValidCoolant(FluidStack fluid) {
        return BuildcraftFuelRegistry.coolant.getCoolant(fluid) != null;
    }

    private boolean isResidue(FluidStack fluid) {
        // If this is the client then we don't have a current fuel- just trust the server that its correct
        if (world != null && level.isClientSide) {
            return true;
        }
        if (currentFuel instanceof IDirtyFuel) {
            return fluid.isFluidEqual(((IDirtyFuel) currentFuel).getResidue());
        }
        return false;
    }

    private class InternalFluidHandler implements IFluidHandlerAdv {

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = tankFuel.fill(resource, action);
            if (filled == 0) {
                filled = tankCoolant.fill(resource, action);
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return tankResidue.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return tankResidue.drain(maxDrain, action);
        }

        @Override
        public FluidStack drain(IFluidFilter filter, int maxDrain, boolean doDrain) {
            return tankResidue.drain(filter, maxDrain, doDrain);
        }

        @Override
        public int getTanks() { return 3; }

        @Override
        public FluidStack getFluidInTank(int tank) {
            switch (tank) {
                case 0: return tankFuel.getFluidInTank(0);
                case 1: return tankCoolant.getFluidInTank(0);
                case 2: return tankResidue.getFluidInTank(0);
                default: return FluidStack.EMPTY;
            }
        }

        @Override
        public int getTankCapacity(int tank) {
            switch (tank) {
                case 0: return tankFuel.getTankCapacity(0);
                case 1: return tankCoolant.getTankCapacity(0);
                case 2: return tankResidue.getTankCapacity(0);
                default: return 0;
            }
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            switch (tank) {
                case 0: return tankFuel.isFluidValid(0, stack);
                case 1: return tankCoolant.isFluidValid(0, stack);
                case 2: return tankResidue.isFluidValid(0, stack);
                default: return false;
            }
        }
    }
}
