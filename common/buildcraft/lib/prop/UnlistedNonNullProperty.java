/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.prop;

// TODO (Phase 7 — Rendering): replaces IUnlistedProperty<V> (removed in 1.21).
// In NeoForge 1.21, tile-to-model data is passed via BlockEntity.getModelData() / ModelData / ModelProperty<T>.
public class UnlistedNonNullProperty<V> {
    public final String name;

    public UnlistedNonNullProperty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isValid(V value) {
        return value != null;
    }

    @SuppressWarnings("rawtypes")
    public Class getType() {
        return Object.class;
    }

    public String valueToString(V value) {
        return value.toString();
    }
}
