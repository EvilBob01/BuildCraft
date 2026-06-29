/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.neoforged.api.distmarker.Dist;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventStatement;

import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.collect.OrderedEnumMap;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionPipeDirection;

public abstract class PipeBehaviourDirectional extends PipeBehaviour {
    public static final OrderedEnumMap<Direction> ROTATION_ORDER = VanillaRotationHandlers.ROTATE_FACING;

    protected EnumPipePart currentDir = EnumPipePart.CENTER;

    public PipeBehaviourDirectional(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDirectional(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        setCurrentDir(NBTUtilBC.readEnum(nbt.getTag("currentDir"), Direction.class));
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.setTag("currentDir", NBTUtilBC.writeEnum(getCurrentDir()));
        return nbt;
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, Side side) {
        super.writePayload(buffer, side);
        PacketBufferBC bufBc = PacketBufferBC.asPacketBufferBc(buffer);
        bufBc.writeEnumValue(currentDir);
    }

    @Override
    public void readPayload(FriendlyByteBuf buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        currentDir = PacketBufferBC.asPacketBufferBc(buffer).readEnumValue(EnumPipePart.class);
    }

    @Override
    public boolean onPipeActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            EntityUtil.activateWrench(player, trace);

            if (part == EnumPipePart.CENTER) {
                return advanceFacing();
            } else if (part.face != getCurrentDir() && canFaceDirection(part.face)) {
                setCurrentDir(part.face);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isClientSide) {
            return;
        }

        if (!canFaceDirection(getCurrentDir())) {
            if (!advanceFacing()) {
                setCurrentDir(null);
            }
        }
    }

    protected abstract boolean canFaceDirection(Direction dir);

    /** @return True if the facing direction changed. */
    public boolean advanceFacing() {
        Direction current = currentDir.face;
        for (int i = 0; i < 6; i++) {
            current = ROTATION_ORDER.next(current);
            if (canFaceDirection(current)) {
                setCurrentDir(current);
                return true;
            }
        }
        return false;
    }

    @Nullable
    protected Direction getCurrentDir() {
        return currentDir.face;
    }

    protected void setCurrentDir(Direction setTo) {
        if (this.currentDir.face == setTo) {
            return;
        }
        this.currentDir = EnumPipePart.fromFacing(setTo);
        if (!pipe.getHolder().getPipeWorld().isClientSide) {
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @PipeEventHandler
    public void addActions(PipeEventStatement.AddActionInternal event) {
        for (Direction face : Direction.VALUES) {
            if (canFaceDirection(face)) {
                event.actions.add(BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]);
            }
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        if (event.action instanceof ActionPipeDirection) {
            ActionPipeDirection action = (ActionPipeDirection) event.action;
            if (canFaceDirection(action.direction)) {
                setCurrentDir(action.direction);
            }
        }
    }
}
