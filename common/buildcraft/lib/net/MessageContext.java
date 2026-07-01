/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import net.minecraft.server.level.ServerPlayer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Local replacement for the removed Forge {@code simpleimpl.MessageContext}, wrapping a NeoForge
 * {@link IPayloadContext}. Kept API-compatible with the old class ({@code ctx.side},
 * {@code ctx.getServerHandler().player}) so existing message handler code needs minimal changes. */
public class MessageContext {
    public final Dist side;
    private final IPayloadContext payloadContext;

    public MessageContext(Dist side, IPayloadContext payloadContext) {
        this.side = side;
        this.payloadContext = payloadContext;
    }

    public IPayloadContext getPayloadContext() {
        return payloadContext;
    }

    /** @return A thin wrapper exposing {@code .player} for server-received messages, matching the old
     *         {@code NetHandlerPlayServer}-shaped accessor used throughout BuildCraft's message handlers. */
    public ServerHandlerView getServerHandler() {
        return new ServerHandlerView((ServerPlayer) payloadContext.player());
    }

    public static final class ServerHandlerView {
        public final ServerPlayer player;

        ServerHandlerView(ServerPlayer player) {
            this.player = player;
        }
    }
}
