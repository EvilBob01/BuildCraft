/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

// TODO (Phase 8): net.minecraftforge.common.config.{Configuration,Property} and
// net.minecraftforge.fml.client.event.ConfigChangedEvent removed in NeoForge 1.21.1. Config
// now uses ModConfigSpec + the ModConfigEvent bus. This class needs a full rewrite; for now
// defaults are hardcoded and config load/save/reload are no-ops so dependent code compiles.

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.EnumPipeColourType;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.config.EnumRestartRequirement;

public class BCTransportConfig {
    public enum PowerLossMode {
        LOSSLESS,
        PERCENTAGE,
        ABSOLUTE;

        public static final PowerLossMode DEFAULT = LOSSLESS;
        public static final PowerLossMode[] VALUES = values();
    }

    private static final long MJ_REQ_MILLIBUCKET_MIN = 100;
    private static final long MJ_REQ_ITEM_MIN = 50_000;

    public static long mjPerMillibucket = 1_000;
    public static long mjPerItem = MjAPI.MJ;
    public static int baseFlowRate = 10;
    public static int basePowerRate = 4;
    public static int baseRfRate = 40;
    public static boolean fluidPipeColourBorder = true;
    public static boolean disableRfPipe = false;
    public static boolean powerPipeUseOldMjTexture = false;
    public static PowerLossMode lossMode = PowerLossMode.DEFAULT;

    public static void preInit() {
        // TODO (Phase 8): rewrite against ModConfigSpec.
        reloadConfig(EnumRestartRequirement.WORLD);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        // TODO (Phase 8): rewrite against ModConfigSpec — read values from config instead of hardcoding.
        if (EnumRestartRequirement.WORLD.hasBeenRestarted(restarted)) {
            mjPerMillibucket = 1_000;
            if (mjPerMillibucket < MJ_REQ_MILLIBUCKET_MIN) {
                mjPerMillibucket = MJ_REQ_MILLIBUCKET_MIN;
            }

            mjPerItem = MjAPI.MJ;
            if (mjPerItem < MJ_REQ_ITEM_MIN) {
                mjPerItem = MJ_REQ_ITEM_MIN;
            }

            baseFlowRate = 10;
            basePowerRate = 4;
            baseRfRate = 40;

            fluidPipeColourBorder = true;
            PipeApi.flowFluids.fallbackColourType =
                fluidPipeColourBorder ? EnumPipeColourType.BORDER_INNER : EnumPipeColourType.TRANSLUCENT;

            lossMode = PowerLossMode.DEFAULT;

            fluidTransfer(BCTransportPipes.cobbleFluid, baseFlowRate, 10);
            fluidTransfer(BCTransportPipes.woodFluid, baseFlowRate, 10);

            fluidTransfer(BCTransportPipes.stoneFluid, baseFlowRate * 2, 10);
            fluidTransfer(BCTransportPipes.sandstoneFluid, baseFlowRate * 2, 10);

            fluidTransfer(BCTransportPipes.clayFluid, baseFlowRate * 4, 10);
            fluidTransfer(BCTransportPipes.ironFluid, baseFlowRate * 4, 10);
            fluidTransfer(BCTransportPipes.quartzFluid, baseFlowRate * 4, 10);

            fluidTransfer(BCTransportPipes.diamondFluid, baseFlowRate * 8, 10);
            fluidTransfer(BCTransportPipes.diaWoodFluid, baseFlowRate * 8, 10);
            fluidTransfer(BCTransportPipes.goldFluid, baseFlowRate * 8, 2);
            fluidTransfer(BCTransportPipes.voidFluid, baseFlowRate * 8, 10);

            powerTransfer(BCTransportPipes.cobblePower, basePowerRate, 16, false);
            powerTransfer(BCTransportPipes.stonePower, basePowerRate * 2, 32, false);
            powerTransfer(BCTransportPipes.woodPower, basePowerRate * 4, 128, true);
            powerTransfer(BCTransportPipes.sandstonePower, basePowerRate * 4, 32, false);
            powerTransfer(BCTransportPipes.quartzPower, basePowerRate * 8, 32, false);
            powerTransfer(BCTransportPipes.ironPower, basePowerRate * 8, 32, false);
            powerTransfer(BCTransportPipes.goldPower, basePowerRate * 32, 32, false);
            powerTransfer(BCTransportPipes.diamondPower, basePowerRate * 64, 32, false);
            powerTransfer(BCTransportPipes.diaWoodPower, basePowerRate * 64, 32, true);

            if (!disableRfPipe) {
                rfTransfer(BCTransportPipes.cobbleRf, baseRfRate, false);
                rfTransfer(BCTransportPipes.stoneRf, baseRfRate * 2, false);
                rfTransfer(BCTransportPipes.woodRf, baseRfRate * 4, true);
                rfTransfer(BCTransportPipes.sandstoneRf, baseRfRate * 4, false);
                rfTransfer(BCTransportPipes.quartzRf, baseRfRate * 8, false);
                rfTransfer(BCTransportPipes.ironRf, baseRfRate * 8, false);
                rfTransfer(BCTransportPipes.goldRf, baseRfRate * 32, false);
                rfTransfer(BCTransportPipes.diamondRf, baseRfRate * 64, false);
                rfTransfer(BCTransportPipes.diaWoodRf, baseRfRate * 64, true);
            }
        }
    }

    private static void fluidTransfer(PipeDefinition def, int rate, int delay) {
        PipeApi.fluidTransferData.put(def, new PipeApi.FluidTransferInfo(rate, delay));
    }

    private static void powerTransfer(PipeDefinition def, int transferMultiplier, int resistanceDivisor, boolean recv) {
        long transfer = MjAPI.MJ * transferMultiplier;
        long resistance = MjAPI.MJ / resistanceDivisor;
        PipeApi.powerTransferData.put(def, PowerTransferInfo.createFromResistance(transfer, resistance, recv));
    }

    private static void rfTransfer(PipeDefinition def, int maxTransfer, boolean recv) {
        PipeApi.rfTransferData.put(def, new RedstoneFluxTransferInfo(maxTransfer, recv));
    }
}
