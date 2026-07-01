/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

/** TODO (Phase 8 — see ROADMAP.md): the 1.12.2 fluid API ({@code BlockFluidBase}, {@code FluidRegistry},
 * legacy {@code Material}, {@code StateMap}) was removed entirely in favor of NeoForge's {@code FluidType} +
 * {@code FluidStack} system. This class is stubbed to a no-op that preserves the call signature used by
 * {@code BCEnergyFluids} and friends until fluid registration is rewritten against the new API. */
public class FluidManager {

    public static void init(net.neoforged.bus.api.IEventBus modEventBus) {
        // no-op until the fluid system is ported (Phase 8)
    }

    /** Should only ever be called during common setup. Currently a no-op passthrough. */
    public static <F extends BCFluid> F register(F fluid) {
        return fluid;
    }
}
