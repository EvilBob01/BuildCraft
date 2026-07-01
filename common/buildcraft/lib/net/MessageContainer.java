/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.world.entity.player.Player;

import net.neoforged.api.distmarker.Dist;

public class MessageContainer implements IMessage {

    private int windowId;
    private int msgId;
    private PacketBufferBC payload;

    @SuppressWarnings("unused")
    public MessageContainer() {}

    public MessageContainer(int windowId, int msgId, PacketBufferBC payload) {
        this.windowId = windowId;
        this.msgId = msgId;
        this.payload = payload;
    }

    // Packet breakdown:
    // INT - WindowId
    // USHORT - PAYLOAD_SIZE->"size"
    // BYTE[size] - PAYLOAD

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = buf.readInt();
        msgId = buf.readUnsignedShort();
        int payloadSize = buf.readUnsignedShort();
        ByteBuf read = buf.readBytes(payloadSize);
        payload = new PacketBufferBC(read);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(windowId);
        buf.writeShort(msgId);
        int length = payload.readableBytes();
        buf.writeShort(length);
        buf.writeBytes(payload, 0, length);
    }

    /** TODO (Phase 6.5 — see ROADMAP.md): dispatches into {@code ContainerBC_Neptune}, which still extends
     * the removed 1.12.2 {@code Container} class (menus are {@code AbstractContainerMenu} now, and
     * {@code Player.openContainer} is {@code Player.containerMenu}). Stubbed to a no-op until the container
     * subsystem is ported. */
    public static final IMessageHandler<MessageContainer, IMessage> HANDLER = (message, ctx) -> {
        try {
            return null;
        } finally {
            message.payload.release();
        }
    };
}
