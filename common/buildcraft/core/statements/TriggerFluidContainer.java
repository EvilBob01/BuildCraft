/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidTypeUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;

public class TriggerFluidContainer extends BCStatement implements ITriggerExternal {
    public State state;

    public TriggerFluidContainer(State state) {
        super(
            "buildcraft:fluid." + state.name().toLowerCase(Locale.ROOT),
            "buildcraft.fluid." + state.name().toLowerCase(Locale.ROOT)
        );
        this.state = state;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCCoreSprites.TRIGGER_FLUID.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.CONTAINS || state == State.SPACE ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        IFluidHandler handler = CapUtil.getCapability(tile, CapUtil.CAP_FLUIDS, side.getOpposite());

        if (handler != null) {
            FluidStack searchedFluid = null;

            if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack().isEmpty()) {
                searchedFluid = FluidUtil.getFluidContained(parameters[0].getItemStack());
            }

            if (searchedFluid != null) {
                searchedFluid.setAmount(1);
            }

            int tankCount = handler.getTanks();
            if (tankCount == 0) {
                return false;
            }

            switch (state) {
                case EMPTY:
                    FluidStack drained = handler.drain(1, IFluidHandler.FluidAction.SIMULATE);
                    return drained == null || drained.getAmount() <= 0;
                case CONTAINS:
                    for (int i = 0; i < tankCount; i++) {
                        FluidStack fluid = handler.getFluidInTank(i);
                        if (!fluid.isEmpty() && fluid.getAmount() > 0 && (searchedFluid == null || searchedFluid.isFluidEqual(fluid))) {
                            return true;
                        }
                    }
                    return false;
                case SPACE:
                    if (searchedFluid == null) {
                        for (int i = 0; i < tankCount; i++) {
                            FluidStack fluid = handler.getFluidInTank(i);
                            if (fluid.isEmpty() || fluid.getAmount() < handler.getTankCapacity(i)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return handler.fill(searchedFluid, IFluidHandler.FluidAction.SIMULATE) > 0;
                case FULL:
                    if (searchedFluid == null) {
                        for (int i = 0; i < tankCount; i++) {
                            FluidStack fluid = handler.getFluidInTank(i);
                            if (fluid.isEmpty() || fluid.getAmount() < handler.getTankCapacity(i)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return handler.fill(searchedFluid, IFluidHandler.FluidAction.SIMULATE) <= 0;
            }
        }

        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_FLUID_ALL;
    }

    public enum State {
        EMPTY,
        CONTAINS,
        SPACE,
        FULL;

        public static final State[] VALUES = values();
    }
}
