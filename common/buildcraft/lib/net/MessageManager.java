/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;

import buildcraft.api.IBuildCraftMod;
import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.MessageUtil;

/** Routes BuildCraft's legacy {@link IMessage}-shaped packets over a single NeoForge {@link BCPayload}
 * channel. Message classes keep their old {@code toBytes}/{@code fromBytes} bodies; only the transport
 * layer (this class) changed. See {@link BCPayload} for the wire format. */
public class MessageManager {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.messages");

    private static final List<Class<? extends IMessage>> ORDERED_CLASSES = new ArrayList<>();
    private static final Map<Class<? extends IMessage>, MessageInfo<?>> MESSAGE_HANDLERS = new HashMap<>();

    private static class MessageInfo<I extends IMessage> {
        final IBuildCraftMod module;
        final Class<I> messageClass;
        int id = -1;

        @Nullable
        IMessageHandler<I, ?> clientHandler, serverHandler;

        MessageInfo(IBuildCraftMod module, Class<I> messageClass) {
            this.module = module;
            this.messageClass = messageClass;
        }
    }

    /** Registers a message as one that will not be received, but will be sent. */
    public static <I extends IMessage> void registerMessageClass(IBuildCraftMod module, Class<I> clazz, Dist... sides) {
        registerMessageClass(module, clazz, null, sides);
    }

    @SuppressWarnings("unchecked")
    public static <I extends IMessage> void registerMessageClass(IBuildCraftMod module, Class<I> messageClass,
        IMessageHandler<I, ?> messageHandler, Dist... sides) {
        MessageInfo<I> messageInfo = (MessageInfo<I>) MESSAGE_HANDLERS.get(messageClass);
        if (messageInfo == null) {
            messageInfo = new MessageInfo<>(module, messageClass);
            MESSAGE_HANDLERS.put(messageClass, messageInfo);
            ORDERED_CLASSES.add(messageClass);
        }
        if (messageHandler == null) {
            if (DEBUG) {
                BCLog.logger.info("[lib.messages] Registered message " + messageClass + " for " + module.getModId());
            }
            return;
        }
        Dist specificSide = sides != null && sides.length == 1 ? sides[0] : null;
        if (specificSide == null || specificSide == Dist.CLIENT) {
            messageInfo.clientHandler = messageHandler;
        }
        if (specificSide == null || specificSide == Dist.DEDICATED_SERVER) {
            messageInfo.serverHandler = messageHandler;
        }
    }

    /** Sets the handler for the specified message class.
     *
     * @param side The side that the given handler will receive messages on. */
    @SuppressWarnings("unchecked")
    public static <I extends IMessage> void setHandler(Class<I> messageClass, IMessageHandler<I, ?> messageHandler,
        Dist side) {
        MessageInfo<I> messageInfo = (MessageInfo<I>) MESSAGE_HANDLERS.get(messageClass);
        if (messageInfo == null) {
            throw new IllegalArgumentException("Cannot set handler for unregistered message: " + messageClass);
        }
        registerMessageClass(messageInfo.module, messageClass, messageHandler, side);
    }

    /** Called by {@link BCLib} to hook up payload registration on the mod event bus. */
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(MessageManager::registerPayloads);
    }

    /** Called by {@link BCLib} once all modules have finished registering their message classes. Assigns final,
     * stable IDs in registration order. */
    public static void fmlPostInit() {
        for (int i = 0; i < ORDERED_CLASSES.size(); i++) {
            MESSAGE_HANDLERS.get(ORDERED_CLASSES.get(i)).id = i;
        }
        if (DEBUG) {
            BCLog.logger.info("[lib.messages] Finalized " + ORDERED_CLASSES.size() + " message classes:");
            for (int i = 0; i < ORDERED_CLASSES.size(); i++) {
                BCLog.logger.info("[lib.messages]   " + i + ": " + ORDERED_CLASSES.get(i));
            }
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(BCLib.MODID);
        registrar.playBidirectional(BCPayload.TYPE, BCPayload.STREAM_CODEC, MessageManager::handlePayload);
    }

    @SuppressWarnings("unchecked")
    private static void handlePayload(BCPayload payload, IPayloadContext context) {
        if (payload.msgId() < 0 || payload.msgId() >= ORDERED_CLASSES.size()) {
            BCLog.logger.warn("[lib.messages] Received an unknown message id " + payload.msgId());
            return;
        }
        Class<? extends IMessage> msgClass = ORDERED_CLASSES.get(payload.msgId());
        MessageInfo<IMessage> info = (MessageInfo<IMessage>) MESSAGE_HANDLERS.get(msgClass);
        Dist side = context.flow() == PacketFlow.CLIENTBOUND ? Dist.CLIENT : Dist.DEDICATED_SERVER;
        IMessageHandler<IMessage, IMessage> handler =
            (IMessageHandler<IMessage, IMessage>) (side == Dist.CLIENT ? info.clientHandler : info.serverHandler);

        context.enqueueWork(() -> {
            try {
                IMessage message = msgClass.getDeclaredConstructor().newInstance();
                message.fromBytes(Unpooled.wrappedBuffer(payload.data()));
                if (handler == null) {
                    if (side == Dist.DEDICATED_SERVER) {
                        BCLog.logger.warn("[lib.messages] The server received " + msgClass
                            + " but has no handler registered for it (bad/buggy client?)");
                    }
                    return;
                }
                MessageContext ctx = new MessageContext(side, context);
                IMessage reply = handler.onMessage(message, ctx);
                if (reply != null) {
                    MessageUtil.sendReturnMessage(ctx, reply);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to deserialize " + msgClass, e);
            }
        });
    }

    private static BCPayload toPayload(IMessage message) {
        MessageInfo<?> info = MESSAGE_HANDLERS.get(message.getClass());
        if (info == null) {
            throw new IllegalArgumentException("Cannot send unregistered message " + message.getClass());
        }
        ByteBuf buf = Unpooled.buffer();
        message.toBytes(buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new BCPayload(info.id, data);
    }

    /** Send this message to everyone. The {@link IMessageHandler} for this message type should be on the CLIENT
     * side. */
    public static void sendToAll(IMessage message) {
        PacketDistributor.sendToAllPlayers(toPayload(message));
    }

    /** Send this message to the specified player. The {@link IMessageHandler} for this message type should be on
     * the CLIENT side. */
    public static void sendTo(IMessage message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, toPayload(message));
    }

    /** Send this message to the server. The {@link IMessageHandler} for this message type should be on the SERVER
     * side. */
    public static void sendToServer(IMessage message) {
        PacketDistributor.sendToServer(toPayload(message));
    }

    /** TODO (Phase 5 — see ROADMAP.md): dimensions are no longer identified by {@code int} (they're
     * {@code ResourceKey<Level>} now), and resolving one to a live {@code ServerLevel} to enumerate its
     * players requires a {@code MinecraftServer} reference this class doesn't have. Callers of this method
     * ({@code WorldSavedDataVolumeBoxes}, {@code MarkerSubCache}) also still reference the removed
     * {@code Level.provider} field, so they need their own follow-up regardless. Stubbed to a no-op for
     * now so the call sites keep compiling. */
    public static void sendToDimension(IMessage message, int dimensionId) {
        // no-op: see TODO above
    }
}
