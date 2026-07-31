/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.lib.misc.CapUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;

public class PipeBehaviourWoodPower extends PipeBehaviour {

    public PipeBehaviourWoodPower(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWoodPower(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWoodPower);
    }

    @Override
    public int getTextureIndex(Direction face) {
        if (face == null) {
            return 0;
        }
        if (pipe.getConnectedPipe(face) != null) {
            return 0;
        }
        BlockEntity tile = pipe.getConnectedTile(face);
        if (tile == null) {
            return 0;
        }
        if (pipe.getFlow() instanceof PipeFlowRedstoneFlux) {
            IEnergyStorage recv = CapUtil.getCapability(tile, Capabilities.EnergyStorage.BLOCK, face.getOpposite());
            return recv == null ? 1 : recv.canReceive() ? 0 : 1;
        } else {
            IMjReceiver recv = CapUtil.getCapability(tile, MjAPI.CAP_RECEIVER, face.getOpposite());
            return recv == null ? 1 : recv.canReceive() ? 0 : 1;
        }
    }
}
