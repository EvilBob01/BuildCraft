package buildcraft.api.mj;

import java.text.DecimalFormat;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.capabilities.BlockCapability;

import buildcraft.api.core.CapabilitiesHelper;

public class MjAPI {

    // ################################
    //
    // Useful constants (Public API)
    //
    // ################################

    /** A single minecraft joule, in micro joules (the power system base unit) */
    public static final long ONE_MINECRAFT_JOULE = getMjValue();
    /** The same as {@link #ONE_MINECRAFT_JOULE}, but a shorter field name */
    public static final long MJ = ONE_MINECRAFT_JOULE;

    /** The decimal format used to display values of MJ to the player. Note that this */
    public static final DecimalFormat MJ_DISPLAY_FORMAT = new DecimalFormat("#,##0.##");

    public static IMjEffectManager EFFECT_MANAGER = NullaryEffectManager.INSTANCE;

    // ###############
    //
    // Helpful methods
    //
    // ###############

    /** Formats a given MJ value to a player-oriented string. Note that this does not append "MJ" to the value. */
    public static String formatMj(long microMj) {
        return formatMjInternal(microMj / (double) MJ);
    }

    private static String formatMjInternal(double val) {
        return MJ_DISPLAY_FORMAT.format(val);
    }

    public static MjRfConversion getRfConversion() {
        return IMjToRfStatus.get().getConversion();
    }

    public static boolean isRfAutoConversionEnabled() {
        return IMjToRfStatus.get().isAutoconvertEnabled();
    }

    // ########################################
    //
    // Null based classes
    //
    // ########################################

    public enum NullaryEffectManager implements IMjEffectManager {
        INSTANCE;
        @Override
        public void createPowerLossEffect(Level world, Vec3 center, long microJoulesLost) {}

        @Override
        public void createPowerLossEffect(Level world, Vec3 center, Direction direction, long microJoulesLost) {}

        @Override
        public void createPowerLossEffect(Level world, Vec3 center, Vec3 direction, long microJoulesLost) {}
    }
    // @formatter:on

    // ###############
    //
    // Capabilities
    //
    // ###############

    /* Under NeoForge 1.21.1 these are {@link BlockCapability} values rather than the old Forge {@code Capability}
     * instances: each is created once with a unique ResourceLocation (see CapabilitiesHelper) and then queried
     * externally via {@code level.getCapability(cap, pos, side)}. Providers are attached per block-entity-type from a
     * RegisterCapabilitiesEvent listener on the mod bus, not by implementing a provider interface on the tile. */

    @Nonnull
    public static final BlockCapability<IMjConnector, Direction> CAP_CONNECTOR;

    @Nonnull
    public static final BlockCapability<IMjReceiver, Direction> CAP_RECEIVER;

    @Nonnull
    public static final BlockCapability<IMjRedstoneReceiver, Direction> CAP_REDSTONE_RECEIVER;

    @Nonnull
    public static final BlockCapability<IMjReadable, Direction> CAP_READABLE;

    @Nonnull
    public static final BlockCapability<IMjPassiveProvider, Direction> CAP_PASSIVE_PROVIDER;

    static {
        CAP_CONNECTOR = CapabilitiesHelper.registerCapability(IMjConnector.class);
        CAP_RECEIVER = CapabilitiesHelper.registerCapability(IMjReceiver.class);
        CAP_REDSTONE_RECEIVER = CapabilitiesHelper.registerCapability(IMjRedstoneReceiver.class);
        CAP_READABLE = CapabilitiesHelper.registerCapability(IMjReadable.class);
        CAP_PASSIVE_PROVIDER = CapabilitiesHelper.registerCapability(IMjPassiveProvider.class);
    }

    private static long getMjValue() {
        return 1_000_000L;
    }
}
