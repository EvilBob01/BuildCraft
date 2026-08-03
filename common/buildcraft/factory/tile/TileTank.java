/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.io.IOException;
import net.minecraft.world.level.material.Fluid;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import buildcraft.lib.fluid.IFluidTankProperties;
import buildcraft.lib.fluid.TankProperties;
import buildcraft.lib.net.MessageContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.ITickable;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryGuis;

public class TileTank extends TileBC_Neptune implements ITickable, IDebuggable, IFluidHandlerAdv {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("tank");
    public static final int NET_FLUID_DELTA = IDS.allocId("FLUID_DELTA");

    private static final ResourceLocation ADVANCEMENT_STORE_FLUIDS = ResourceLocation.parse(
        "buildcraftfactory:fluid_storage"
    );

    private static boolean isPlayerInteracting = false;

    public final Tank tank;
    public final FluidSmoother smoothedTank;

    private int lastComparatorLevel;

    public TileTank(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 16 * FluidType.BUCKET_VOLUME);
    }

    protected TileTank(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity) {
        this(type, pos, state, new Tank("tank", capacity, null));
    }

    protected TileTank(BlockEntityType<?> type, BlockPos pos, BlockState state, Tank tank) {
        super(type, pos, state);
        tank.setBlockEntity(this);
        this.tank = tank;
        tankManager.add(tank);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, this, EnumPipePart.VALUES);
        smoothedTank = new FluidSmoother(w -> createAndSendMessage(NET_FLUID_DELTA, w), tank);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public int getComparatorLevel() {
        int amount = tank.getFluidAmount();
        int cap = tank.getCapacity();
        return amount * 14 / cap + (amount > 0 ? 1 : 0);
    }

    // ITickable

    @Override
    public void update() {
        smoothedTank.tick(level);

        if (!level.isClientSide) {
            int compLevel = getComparatorLevel();
            if (compLevel != lastComparatorLevel) {
                lastComparatorLevel = compLevel;
                setChanged();
            }
        }
    }

    // BlockEntity

    @Override
    public void onPlacedBy(LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (!placer.level().isClientSide) {
            isPlayerInteracting = true;
            balanceTankFluids();
            isPlayerInteracting = false;
        }
    }

    /** Moves fluids around to their preferred positions. (For gaseous fluids this will move everything as high as
     * possible, for liquid fluids this will move everything as low as possible.) */
    public void balanceTankFluids() {
        List<TileTank> tanks = getConnectedTanks();
        FluidStack fluid = null;
        for (TileTank tile : tanks) {
            FluidStack held = tile.tank.getFluid();
            if (held == null) {
                continue;
            }
            if (fluid == null) {
                fluid = held;
            } else if (!fluid.isFluidEqual(held)) {
                return;
            }
        }
        if (fluid == null) {
            return;
        }
        if (fluid.getFluidType().isLighterThanAir()) {
            Collections.reverse(tanks);
        }
        TileTank prev = null;
        for (TileTank tile : tanks) {
            if (prev != null) {
                FluidUtilBC.move(tile.tank, prev.tank);
            }
            prev = tile;
        }
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand, Direction facing, float hitX, float hitY,
        float hitZ) {
        int amountBefore = tank.getFluidAmount();
        isPlayerInteracting = true;
        boolean didChange = FluidUtilBC.onTankActivated(player, pos, hand, this);
        isPlayerInteracting = false;
        if (didChange && !player.level().isClientSide && amountBefore < tank.getFluidAmount()) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_STORE_FLUIDS);
        }
        if (!didChange) {
            if (!level.isClientSide) {
                BCFactoryGuis.TANK.openGUI(player, worldPosition);
            }
        }
        return true;
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_FLUID_DELTA, buffer, side);
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.writeInit(buffer);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.writeData(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Dist.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_FLUID_DELTA, buffer, side, ctx);
                smoothedTank.resetSmoothing(getLevel());
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.handleMessage(getLevel(), buffer);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.readData(buffer);
            }
        }
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("fluid = " + tank.getDebugString());
        smoothedTank.getDebugInfo(left, right, side);
    }

    // Rendering

    @OnlyIn(Dist.CLIENT)
    public FluidStackInterp getFluidForRender(float partialTicks) {
        return smoothedTank.getFluidForRender(partialTicks);
    }

    // Tank helper methods

    /** Tests to see if this tank can connect to the other one, in the given direction. BuildCraft itself only calls
     * with {@link Direction#UP} or {@link Direction#DOWN}, however addons are free to call with any of the other 4
     * non-null faces. (Although an addon calling from other faces must provide some way of transferring fluids around).
     * 
     * @param other The other tank.
     * @param direction The direction that the other tank is, from this tank.
     * @return True if this can connect, false otherwise. */
    public boolean canConnectTo(TileTank other, Direction direction) {
        return true;
    }

    /** Helper for {@link #canConnectTo(TileTank, Direction)} that only returns true if both tanks can connect to each
     * other.
     * 
     * @param from
     * @param to
     * @param direction The direction from the "from" tank, to the "to" tank, such that
     *            {@link Objects#equals(Object, Object) Objects.equals(}{@link TileTank#getBlockPos()
     *            from.getBlockPos()}.{@link BlockPos#offset(Direction) offset(direction)}, {@link TileTank#getBlockPos()
     *            to.getBlockPos()}) returns true.
     * @return True if both could connect, false otherwise. */
    public static boolean canTanksConnect(TileTank from, TileTank to, Direction direction) {
        return from.canConnectTo(to, direction) && to.canConnectTo(from, direction.getOpposite());
    }

    /** @return A list of all connected tanks around this block, ordered by position from bottom to top. */
    private List<TileTank> getConnectedTanks() {
        // double-ended queue rather than array list to avoid
        // the copy operation when we search downwards
        Deque<TileTank> tanks = new ArrayDeque<>();
        tanks.add(this);
        TileTank prevTank = this;
        while (true) {
            BlockEntity tileAbove = prevTank.getNeighbourTile(Direction.UP);
            if (!(tileAbove instanceof TileTank)) {
                break;
            }
            TileTank tankUp = (TileTank) tileAbove;
            if (tankUp != null && canTanksConnect(prevTank, tankUp, Direction.UP)) {
                tanks.addLast(tankUp);
            } else {
                break;
            }
            prevTank = tankUp;
        }
        prevTank = this;
        while (true) {
            BlockEntity tileBelow = prevTank.getNeighbourTile(Direction.DOWN);
            if (!(tileBelow instanceof TileTank)) {
                break;
            }
            TileTank tankBelow = (TileTank) tileBelow;
            if (tankBelow != null && canTanksConnect(prevTank, tankBelow, Direction.DOWN)) {
                tanks.addFirst(tankBelow);
            } else {
                break;
            }
            prevTank = tankBelow;
        }
        return new ArrayList<>(tanks);
    }

    // IFluidHandler

    /** Returns the combined fluid across all connected tanks, or null if all tanks are empty. */
    private FluidStack getCombinedFluid() {
        List<TileTank> tanks = getConnectedTanks();
        TileTank bottom = tanks.get(0);
        TileTank top = tanks.get(tanks.size() - 1);
        FluidStack total = bottom.tank.getFluid();
        if (total == null) {
            total = top.tank.getFluid();
        }
        if (total != null) {
            total = total.copy();
            total.setAmount(0);
            for (TileTank t : tanks) {
                FluidStack other = t.tank.getFluid();
                if (other != null) {
                    total.setAmount(total.getAmount() + other.getAmount());
                }
            }
        }
        return total;
    }

    /** Returns the combined capacity across all connected tanks. */
    private int getCombinedCapacity() {
        int capacity = 0;
        for (TileTank t : getConnectedTanks()) {
            capacity += t.tank.getCapacity();
        }
        return capacity;
    }

    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[] { new TankProperties(getCombinedFluid(), getCombinedCapacity()) };
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int slot) {
        FluidStack combined = getCombinedFluid();
        return combined != null ? combined : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int slot) {
        return getCombinedCapacity();
    }

    @Override
    public boolean isFluidValid(int slot, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource == null || resource.getAmount() <= 0) {
            return 0;
        }
        int filled = 0;
        List<TileTank> tanks = getConnectedTanks();
        for (TileTank t : tanks) {
            FluidStack current = t.tank.getFluid();
            if (current != null && !current.isFluidEqual(resource)) {
                return 0;
            }
        }
        boolean gas = resource.getFluidType().isLighterThanAir();
        if (gas) {
            Collections.reverse(tanks);
        }
        resource = resource.copy();
        for (TileTank t : tanks) {
            int tankFilled = t.tank.fill(resource, action);
            if (tankFilled > 0) {
                if (isPlayerInteracting & action.execute()) {
                    t.sendNetworkUpdate(NET_RENDER_DATA);
                }
                resource.setAmount(resource.getAmount() - tankFilled);
                filled += tankFilled;
                if (resource.getAmount() == 0) {
                    break;
                }
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        return drain((fluid) -> true, maxDrain, action.execute());
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource == null) {
            return null;
        }
        return drain(resource::isFluidEqual, resource.getAmount(), action.execute());
    }

    // IFluidHandlerAdv

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        }
        List<TileTank> tanks = getConnectedTanks();
        boolean gas = false;
        for (TileTank tile : tanks) {
            FluidStack fluid = tile.tank.getFluid();
            if (fluid != null) {
                gas = fluid.getFluid().isGaseous(fluid);
                break;
            }
        }
        if (!gas) {
            Collections.reverse(tanks);
        }
        FluidStack total = null;
        for (TileTank t : tanks) {
            int realMax = maxDrain - (total == null ? 0 : total.getAmount());
            if (realMax <= 0) {
                break;
            }
            FluidStack drained = t.tank.drain(filter, realMax, doDrain);
            if (drained == null) continue;
            if (isPlayerInteracting & doDrain) {
                t.sendNetworkUpdate(NET_RENDER_DATA);
            }
            if (total == null) {
                total = drained.copy();
                total.setAmount(0);
            }
            total.setAmount(total.getAmount() + drained.getAmount());
        }
        return total;
    }
}
