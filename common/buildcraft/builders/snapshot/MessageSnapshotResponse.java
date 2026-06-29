/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import net.minecraft.nbt.CompressedStreamTools;

import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class MessageSnapshotResponse implements IMessage {
    private Snapshot snapshot;

    @SuppressWarnings("unused")
    public MessageSnapshotResponse() {
    }

    public MessageSnapshotResponse(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void toBytes(ByteBuf buf) {

//        byte[] bytes = NbtSquisher.squishBuildCraftV1(Snapshot.saveAdditional(snapshot));
//        buf.writeInt(bytes.length);
//        buf.writeBytes(bytes);
//        try {
//            CompressedStreamTools.write(Snapshot.saveAdditional(snapshot), new ByteBufOutputStream(buf));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        try {
            CompressedStreamTools.writeCompressed(Snapshot.saveAdditional(snapshot), new ByteBufOutputStream(buf));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
//            snapshot = Snapshot.loadAdditional(NbtSquisher.expand(buf.readBytes(buf.readInt()).array()));
//            snapshot = Snapshot.loadAdditional(CompressedStreamTools.read(new ByteBufInputStream(buf), NBTSizeTracker.INFINITE));
            snapshot = Snapshot.loadAdditional(CompressedStreamTools.readCompressed(new ByteBufInputStream(buf)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final IMessageHandler<MessageSnapshotResponse, IMessage> HANDLER = (message, ctx) -> {
        ClientSnapshots.INSTANCE.onSnapshotReceived(message.snapshot);
        return null;
    };
}
