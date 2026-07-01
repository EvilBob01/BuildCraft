/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import buildcraft.lib.BCLib;

/** A single NeoForge {@link CustomPacketPayload} used to carry every BuildCraft {@link IMessage}. Rather than
 * defining one payload type per legacy message class (which would mean touching all ~15 of them plus every
 * call site), this wraps the message's own {@code toBytes}/{@code fromBytes} serialization behind a
 * (message-class-id, raw-bytes) envelope. {@link MessageManager} does the routing. */
public record BCPayload(int msgId, byte[] data) implements CustomPacketPayload {
    public static final Type<BCPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BCLib.MODID, "msg"));

    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, BCPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.msgId);
            buf.writeByteArray(payload.data);
        },
        (buf) -> {
            int msgId = buf.readVarInt();
            byte[] data = buf.readByteArray();
            return new BCPayload(msgId, data);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
