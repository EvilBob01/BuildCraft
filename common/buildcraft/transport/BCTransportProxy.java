/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.BCModules;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.api.transport.pipe.PipeBehaviour;

import buildcraft.lib.net.MessageManager;

import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.client.render.PipeWireRenderer;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiDiamondWoodPipe;
import buildcraft.transport.gui.GuiEmzuliPipe_BC8;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.net.MessageMultiPipeItem;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.tile.TileFilteredBuffer;
import buildcraft.transport.wire.MessageWireSystems;
import buildcraft.transport.wire.MessageWireSystemsPowered;

public abstract class BCTransportProxy implements IGuiHandler {
    @SidedProxy(modId = BCTransport.MODID)
    private static BCTransportProxy proxy;

    public static BCTransportProxy getProxy() {
        return proxy;
    }

    @Override
    public Object getServerGuiElement(int id, Player player, Level world, int x, int y, int z) {
        BCTransportGuis gui = BCTransportGuis.get(id);
        if (gui == null) return null;
        BlockEntity tile = world.getBlockEntity(new BlockPos(x, y, z));

        switch (gui) {
            case FILTERED_BUFFER: {
                if (tile instanceof TileFilteredBuffer) {
                    TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                    return new ContainerFilteredBuffer_BC8(player, filteredBuffer);
                }
                break;
            }
            case PIPE_DIAMOND: {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourDiamond) {
                        PipeBehaviourDiamond diaPipe = (PipeBehaviourDiamond) behaviour;
                        return new ContainerDiamondPipe(player, diaPipe);
                    }
                }
                break;
            }
            case PIPE_DIAMOND_WOOD: {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourWoodDiamond) {
                        PipeBehaviourWoodDiamond diaPipe = (PipeBehaviourWoodDiamond) behaviour;
                        return new ContainerDiamondWoodPipe(player, diaPipe);
                    }
                }
                break;
            }
            case PIPE_EMZULI: {
                if (tile instanceof IPipeHolder) {
                    IPipeHolder holder = (IPipeHolder) tile;
                    IPipe pipe = holder.getPipe();
                    if (pipe == null) return null;
                    PipeBehaviour behaviour = pipe.getBehaviour();
                    if (behaviour instanceof PipeBehaviourEmzuli) {
                        PipeBehaviourEmzuli emPipe = (PipeBehaviourEmzuli) behaviour;
                        return new ContainerEmzuliPipe_BC8(player, emPipe);
                    }
                }
                break;
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, Player player, Level world, int x, int y, int z) {
        return null;
    }

    public void fmlPreInit() {
        MessageManager.registerMessageClass(BCModules.TRANSPORT, MessageWireSystems.class, Dist.CLIENT);
        MessageManager.registerMessageClass(BCModules.TRANSPORT, MessageWireSystemsPowered.class, Dist.CLIENT);
        MessageManager.registerMessageClass(BCModules.TRANSPORT, MessageMultiPipeItem.class, Dist.CLIENT);
    }

    public void fmlInit() {}

    public void fmlPostInit() {}

    @SuppressWarnings("unused")
    @OnlyIn(Dist.DEDICATED_SERVER)
    public static class ServerProxy extends BCTransportProxy {}

    @SuppressWarnings("unused")
    @OnlyIn(Dist.CLIENT)
    public static class ClientProxy extends BCTransportProxy {
        @Override
        public void fmlPreInit() {
            super.fmlPreInit();
            BCTransportSprites.fmlPreInit();
            BCTransportModels.fmlPreInit();
            PipeApiClient.registry = PipeRegistryClient.INSTANCE;
            PipeWireRenderer.init();

            MessageManager.setHandler(MessageWireSystems.class, MessageWireSystems.HANDLER, Dist.CLIENT);
            MessageManager.setHandler(MessageWireSystemsPowered.class, MessageWireSystemsPowered.HANDLER, Dist.CLIENT);
            MessageManager.setHandler(MessageMultiPipeItem.class, MessageMultiPipeItem.HANDLER, Dist.CLIENT);
        }

        @Override
        public void fmlInit() {
            super.fmlInit();
            BCTransportModels.fmlInit();
        }

        @Override
        public void fmlPostInit() {
            super.fmlPostInit();
            BCTransportModels.fmlPostInit();
        }

        @Override
        public Object getClientGuiElement(int id, Player player, Level world, int x, int y, int z) {
            BCTransportGuis gui = BCTransportGuis.get(id);
            if (gui == null) {
                return null;
            }
            BlockEntity tile = world.getBlockEntity(new BlockPos(x, y, z));
            switch (gui) {
                case FILTERED_BUFFER: {
                    if (tile instanceof TileFilteredBuffer) {
                        TileFilteredBuffer filteredBuffer = (TileFilteredBuffer) tile;
                        return new GuiFilteredBuffer(new ContainerFilteredBuffer_BC8(player, filteredBuffer));
                    }
                    break;
                }
                case PIPE_DIAMOND: {
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IPipe pipe = holder.getPipe();
                        if (pipe == null) return null;
                        PipeBehaviour behaviour = pipe.getBehaviour();
                        if (behaviour instanceof PipeBehaviourDiamond) {
                            PipeBehaviourDiamond diaPipe = (PipeBehaviourDiamond) behaviour;
                            return new GuiDiamondPipe(player, diaPipe);
                        }
                    }
                    break;
                }
                case PIPE_DIAMOND_WOOD: {
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IPipe pipe = holder.getPipe();
                        if (pipe == null) return null;
                        PipeBehaviour behaviour = pipe.getBehaviour();
                        if (behaviour instanceof PipeBehaviourWoodDiamond) {
                            PipeBehaviourWoodDiamond diaPipe = (PipeBehaviourWoodDiamond) behaviour;
                            return new GuiDiamondWoodPipe(player, diaPipe);
                        }
                    }
                    break;
                }
                case PIPE_EMZULI: {
                    if (tile instanceof IPipeHolder) {
                        IPipeHolder holder = (IPipeHolder) tile;
                        IPipe pipe = holder.getPipe();
                        if (pipe == null) return null;
                        PipeBehaviour behaviour = pipe.getBehaviour();
                        if (behaviour instanceof PipeBehaviourEmzuli) {
                            PipeBehaviourEmzuli emzPipe = (PipeBehaviourEmzuli) behaviour;
                            return new GuiEmzuliPipe_BC8(player, emzPipe);
                        }
                    }
                    break;
                }
            }
            return null;
        }
    }
}
