package buildcraft.transport.plug;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;

public class PluggablePowerAdaptor extends PipePluggable {

    private static final AABB[] BOXES = new AABB[6];

    static {
        double ll = 0 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 16 / 16.0;

        double min = 3 / 16.0;
        double max = 13 / 16.0;

        BOXES[Direction.DOWN.getIndex()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.getIndex()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.getIndex()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.getIndex()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.getIndex()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.getIndex()] = new AABB(ul, min, min, uu, max, max);
    }

    private long storedMJ = 0;

    public PluggablePowerAdaptor(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    public PluggablePowerAdaptor(PluggableDefinition definition, IPipeHolder holder, Direction side, CompoundTag nbt) {
        super(definition, holder, side);
        storedMJ = nbt.getLong("storedMJ");
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.setLong("storedMJ", storedMJ);
        return nbt;
    }

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.getIndex()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCTransportItems.plugPowerAdaptor);
    }

    @Override
    @Nullable
    public PluggableModelKey getModelRenderKey(BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.CUTOUT) {
            return new KeyPlugPowerAdaptor(side);
        }
        return null;
    }

    @Override
    public <T> T getCapability(@Nonnull BlockCapability<T, Direction> cap) {
        if (cap == MjAPI.CAP_CONNECTOR || cap == MjAPI.CAP_RECEIVER || cap == MjAPI.CAP_REDSTONE_RECEIVER) {
            return holder.getPipe().getBehaviour().getCapability(cap, side);
        }
        if (MjAPI.isRfAutoConversionEnabled() && cap == Capabilities.EnergyStorage.BLOCK) {
            IMjReceiver receiver = holder.getPipe().getBehaviour().getCapability(MjAPI.CAP_RECEIVER, side);
            if (receiver == null) {
                return null;
            }
            return (T) (new IEnergyStorage() {

                @Override
                public boolean canReceive() {
                    return receiver.canReceive();
                }

                @Override
                public boolean canExtract() {
                    return false;
                }

                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {

                    if (maxReceive <= 0) {
                        return 0;
                    }

                    if (!receiver.canReceive()) {
                        return 0;
                    }

                    // TODO: Validate!

                    long mjPerRf = MjAPI.getRfConversion().mjPerRf;
                    long maxReceiveMj = maxReceive * mjPerRf + storedMJ;
                    long excess = receiver.receivePower(maxReceiveMj, simulate);

                    // Actual MJ that was accepted
                    long acceptedMj = maxReceiveMj - excess;

                    if (acceptedMj <= 0) {
                        return 0;
                    }

                    long acceptedRF = acceptedMj / mjPerRf;
                    long mjExcess = acceptedMj % mjPerRf;

                    if (mjExcess > 0) {
                        acceptedRF++;

                        if (!simulate) {
                            storedMJ = mjPerRf - mjExcess;
                        }
                    }

                    return (int) acceptedRF;
                }

                @Override
                public int getMaxEnergyStored() {
                    if (receiver instanceof IMjReadable) {
                        long mjPerRf = MjAPI.getRfConversion().mjPerRf;
                        return (int) (((IMjReadable) receiver).getCapacity() / mjPerRf);
                    } else {
                        return 0;
                    }
                }

                @Override
                public int getEnergyStored() {
                    return 0;
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    return 0;
                }

            });
        }
        return null;
    }
}
