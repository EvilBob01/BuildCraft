/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListRegistry;

public class VanillaListHandlers {
    public static void fmlInit() {
        ListRegistry.registerHandler(new ListMatchHandlerClass());
        ListRegistry.registerHandler(new ListMatchHandlerFluid());
        ListRegistry.registerHandler(new ListMatchHandlerTools());
        ListRegistry.registerHandler(new ListMatchHandlerArmor());
        // ItemFood class removed in 1.21; food matching via item class is stubbed
    }

    public static void fmlPostInit() {
        // OreDictionary removed in 1.21; tag-based matching is stubbed in ListMatchHandlerOreDictionary
        ListRegistry.registerHandler(new ListMatchHandlerOreDictionary());
    }
}
