package buildcraft.energy.event;

import java.time.Month;
import java.time.MonthDay;

import buildcraft.api.core.BCLog;

import buildcraft.lib.fluid.BCFluid;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.BCEnergyFluids;

/** Used for automatically changing lang entries, fluid colours, and a few other things around christmas time. This is
 * in energy rather than lib because no other module does anything at christmas. */
public class ChristmasHandler {

    private static Boolean enabled;

    public static boolean isEnabled() {
        if (enabled == null) {
            throw new IllegalStateException("Unknown until init!");
        }
        return enabled;
    }

    private static void fmlPreInit() {
        enabled = BCEnergyConfig.christmasEventStatus.isEnabled(MonthDay.of(Month.DECEMBER, 25));
        if (isEnabled()) {
            setColours(0xC0_75_34, 0x5A_1D_0c, BCEnergyFluids.crudeOil);
            setColours(0xD4_82_39, 0xD8_7D_33, BCEnergyFluids.oilResidue);
            setColours(0xD4_82_39, 0x5A_1D_0C, BCEnergyFluids.oilHeavy);
            setColours(0xD4_82_39, 0x30_0E_05, BCEnergyFluids.oilDense);
            setColours(0xC0_75_34, 0x8a_3D_1C, BCEnergyFluids.oilDistilled);
            setColours(0x4F_33_2F, 0x30_0E_05, BCEnergyFluids.fuelDense);
            setColours(0x88_44_2D, 0x5A_1d_0C, BCEnergyFluids.fuelMixedHeavy);
            setColours(0x9B_61_39, 0x94_59_31, BCEnergyFluids.fuelLight);
            setColours(0xC0_75_34, 0xB3_68_2C, BCEnergyFluids.fuelMixedLight);
            setColours(0xD6_C9_90, 0xCF_BF_8E, BCEnergyFluids.fuelGaseous);
        }
    }

    public static void fmlPreInitDedicatedServer() {
        fmlPreInit();
        // TODO (Phase 7): Christmas lang-entry replacement disabled — LanguageMap removed in 1.21
        // In 1.21 add christmas translations as a bundled resource pack instead.
    }

    public static void fmlPreInitClient() {
        fmlPreInit();
        // TODO (Phase 7): IReloadableResourceManager removed in 1.21; use RegisterClientReloadListenersEvent instead.
        // Christmas lang-entry override is disabled until Phase 7 render/resource overhaul.
        if (isEnabled()) {
            BCLog.logger.info("[energy.christmas] Christmas colours applied; lang overrides require Phase 7 port.");
        }
    }

    private static void setColours(int lightColour, int darkColour, BCFluid[] fluids) {
        if (fluids != null) {
            for (BCFluid fluid : fluids) {
                fluid.setColour(lightColour, darkColour);
                // TODO (Phase 9): gaseous/density override no longer possible via BCFluid;
                // FluidType properties are immutable after registration in 1.21.
            }
        }
    }
}
