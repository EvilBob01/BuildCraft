/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.key;

import java.util.Objects;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class KeyPlugFacade extends PluggableModelKey {
    public final BlockState state;
    public final boolean isHollow;
    private final int hash;

    public KeyPlugFacade(Direction side, BlockState state, boolean isHollow) {
        super(side);
        this.state = state;
        this.isHollow = isHollow;
        this.hash = Objects.hash(side, state, isHollow);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        KeyPlugFacade other = (KeyPlugFacade) obj;
        return other.isHollow == isHollow//
                && other.side == side//
                && other.state == state;
    }
}
