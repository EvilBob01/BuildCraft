/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import io.netty.buffer.ByteBuf;

/** Local replacement for the removed {@code net.minecraftforge.fml.common.network.simpleimpl.IMessage}. Kept with
 * the exact same shape so the ~15 existing BuildCraft message classes only need their import updated, not their
 * body rewritten. {@link MessageManager} adapts instances of this to real NeoForge {@code CustomPacketPayload}s. */
public interface IMessage {
    void fromBytes(ByteBuf buf);

    void toBytes(ByteBuf buf);
}
