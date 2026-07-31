package buildcraft.api.mj;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;

import net.neoforged.neoforge.capabilities.BlockCapability;

import buildcraft.api.core.ICapabilityAccessor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Provides a quick way to return all types of a single {@link IMjConnector} for all the different capabilities.
 * <p>
 * Under NeoForge 1.21.1 this is no longer an {@code ICapabilityProvider} that the game polls on the block entity.
 * That interface, and the whole poll-the-tile model, was removed. Instead this is a plain holder: register the
 * owning block entity type against each MJ capability in a
 * {@code net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent} listener on the mod bus, and have the
 * factory delegate to {@link #getCapability(BlockCapability, Direction)}. For example:
 *
 * <pre>{@code
 * event.registerBlockEntity(MjAPI.CAP_RECEIVER, MyBlockEntityType.INSTANCE,
 *     (be, side) -> be.mjCaps.getCapability(MjAPI.CAP_RECEIVER, side));
 * }</pre>
 *
 * This mirrors {@code buildcraft.lib.cap.CapabilityHelper} on the non-API side. */
public class MjCapabilityHelper implements ICapabilityAccessor {

    @Nonnull
    private final IMjConnector connector;

    @Nullable
    private final IMjReceiver receiver;

    @Nullable
    private final IMjRedstoneReceiver rsReceiver;

    @Nullable
    private final IMjReadable readable;

    @Nullable
    private final IMjPassiveProvider provider;

    private final IEnergyStorage rfAutoConvert;

    public MjCapabilityHelper(@Nonnull IMjConnector mj) {
        this.connector = mj;
        this.receiver = mj instanceof IMjReceiver ? (IMjReceiver) mj : null;
        this.rsReceiver = mj instanceof IMjRedstoneReceiver ? (IMjRedstoneReceiver) mj : null;
        this.readable = mj instanceof IMjReadable ? (IMjReadable) mj : null;
        this.provider = mj instanceof IMjPassiveProvider ? (IMjPassiveProvider) mj : null;

        if (MjAPI.isRfAutoConversionEnabled()) {
            rfAutoConvert = new IEnergyStorage() {

                @Override
                public int getEnergyStored() {
                    IMjReadable read = readable;
                    if (read != null) {
                        long mjPerRf = MjAPI.getRfConversion().mjPerRf;
                        return (int) (read.getStored() / mjPerRf);
                    } else {
                        return 0;
                    }
                }

                @Override
                public int getMaxEnergyStored() {
                    IMjReadable read = readable;
                    if (read != null) {
                        long mjPerRf = MjAPI.getRfConversion().mjPerRf;
                        return (int) (read.getCapacity() / mjPerRf);
                    } else {
                        return 0;
                    }
                }

                @Override
                public boolean canReceive() {
                    return receiver != null && receiver.canReceive();
                }

                /** @return Amount of energy that was (or would have been, if simulated) accepted by the storage. */
                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {

                    if (maxReceive <= 0) {
                        return 0;
                    }

                    IMjReceiver recv = receiver;
                    if (recv == null || !recv.canReceive()) {
                        return 0;
                    }

                    long mjPerRf = MjAPI.getRfConversion().mjPerRf;
                    long maxReceiveMj = maxReceive * mjPerRf;
                    long excess = recv.receivePower(maxReceiveMj, true);

                    // Actual MJ that was accepted
                    long acceptedMj = maxReceiveMj - excess;

                    if (acceptedMj < mjPerRf) {
                        return 0;
                    }

                    // MJ that was accepted but cannot be converted back to RF
                    // (We need to actual accepted MJ to be some integer multiple of mjPerRf)
                    long excessMj = acceptedMj % mjPerRf;
                    // An MJ value that is an integer multiple of mjPerRf
                    long exactAcceptableMj = acceptedMj - excessMj;

                    if (exactAcceptableMj <= 0) {
                        return 0;
                    }

                    int rf = (int) (exactAcceptableMj / mjPerRf);
                    if (rf * mjPerRf != exactAcceptableMj) {
                        // Sanity check
                        throw new IllegalStateException(
                            "Programmer made a mistake?? mjPerRf=" + mjPerRf + ", rf=" + rf + ", exactAcceptableMJ="
                                + exactAcceptableMj
                        );
                    }

                    long excess2 = recv.receivePower(exactAcceptableMj, true);

                    if (excess2 != 0) {
                        // Odd. This means we can't actually accept the exact amount
                        // not actually a crash
                        return 0;
                    }

                    if (!simulate) {
                        long excess3 = recv.receivePower(exactAcceptableMj, simulate);

                        if (excess3 != excess2) {
                            throw new IllegalStateException("Bad impl: " + recv.getClass() + " of receivePower");
                        }
                    }

                    return rf;
                }

                @Override
                public boolean canExtract() {
                    return provider != null;
                }

                /** @return Amount of energy that was (or would have been, if simulated) extracted from the storage. */
                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    long mjPerRf = MjAPI.getRfConversion().mjPerRf;
                    // TODO!
                    // (Nothing in buildcraft supports this at the moment)
                    return 0;
                }

            };
        } else {
            rfAutoConvert = null;
        }
    }

    public boolean hasCapability(@Nonnull BlockCapability<?, Direction> capability, @Nullable Direction facing) {
        return getCapability(capability, facing) != null;
    }

    /** Returns this holder's instance for the given capability, or null if it doesn't provide one.
     * <p>
     * The old Forge {@code Capability#cast(Object)} helper does not exist on {@link BlockCapability}, so the
     * reference comparison against the known MJ capabilities is what establishes type safety here — each branch can
     * only be reached when {@code T} matches that capability's type. */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull BlockCapability<T, Direction> capability, @Nullable Direction facing) {
        if (capability == MjAPI.CAP_CONNECTOR) {
            return (T) connector;
        }
        if (capability == MjAPI.CAP_RECEIVER) {
            return (T) receiver;
        }
        if (capability == MjAPI.CAP_REDSTONE_RECEIVER) {
            return (T) rsReceiver;
        }
        if (capability == MjAPI.CAP_READABLE) {
            return (T) readable;
        }
        if (capability == MjAPI.CAP_PASSIVE_PROVIDER) {
            return (T) provider;
        }
        if (capability == Capabilities.EnergyStorage.BLOCK) {
            return (T) rfAutoConvert;
        }
        return null;
    }
}
