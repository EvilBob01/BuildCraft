/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.io.IOException;

import buildcraft.lib.net.MessageContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.lib.net.IPayloadReceiver;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;

/** Defines some sort of separate element that exists on both the server and client. Doesn't draw directly. */
public abstract class Widget_Neptune<C extends ContainerBC_Neptune> implements IPayloadReceiver {
    public final C container;

    public Widget_Neptune(C container) {
        this.container = container;
    }

    public boolean isRemote() {
        return container.player.world.isClientSide;
    }

    // Net updating

    protected final void sendWidgetData(IPayloadWriter writer) {
        container.sendWidgetData(this, writer);
    }

    public IMessage handleWidgetDataServer(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public IMessage handleWidgetDataClient(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        return null;
    }

    @Override
    public IMessage receivePayload(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        if (ctx.side == Dist.CLIENT) {
            return handleWidgetDataClient(ctx, buffer);
        } else {
            return handleWidgetDataServer(ctx, buffer);
        }
    }
}
