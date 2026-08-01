package buildcraft.api;

import net.minecraft.world.item.Item;

// TODO (Phase 10 — Registry): @ObjectHolder injection removed in NeoForge 1.21.
// Replace with DeferredHolder<Item, Item> fields registered via DeferredRegister.ITEMS in each module.
import buildcraft.api.items.FluidItemDrops;

public class BCItems {

    public static class Lib {
        public static final Item GUIDE = null;
        public static final Item GUIDE_NOTE = null;
        public static final Item DEBUGGER = null;
    }

    public static class Core {
        public static final Item GEAR_WOOD = null;
        public static final Item GEAR_STONE = null;
        public static final Item GEAR_IRON = null;
        public static final Item GEAR_GOLD = null;
        public static final Item GEAR_DIAMOND = null;
        public static final Item WRENCH = null;
        public static final Item PAINTBRUSH = null;
        public static final Item LIST = null;
        public static final Item MAP_LOCATION = null;
        public static final Item MARKER_CONNECTOR = null;
        public static final Item VOLUME_BOX = null;
        public static final Item GOGGLES = null;

        /** It is recommended that you refer to {@link FluidItemDrops#item} when creating fluid drops rather than
         * this. */
        public static final Item FRAGILE_FLUID_SHARD = null;
    }

    public static class Builders {

    }

    public static class Energy {
        public static final Item GLOB_OF_OIL = null;
    }

    public static class Factory {
        public static final Item PLASTIC_SHEET = null;
        public static final Item WATER_GEL = null;
        public static final Item GELLED_WATER = null;
    }

    public static class Transport {
        public static final Item PLUG_BLOCKER = null;
        public static final Item PLUG_POWER_ADAPTOR = null;

        public static final Item PIPE_STRUCTURE = null;
        public static final Item PIPE_WOOD_ITEM = null;
        public static final Item PIPE_EMZULI_ITEM = null;
        public static final Item PIPE_DIAMOND_WOOD_ITEM = null;
        public static final Item PIPE_WOOD_FLUID = null;
        public static final Item PIPE_DIAMOND_WOOD_FLUID = null;
    }

    public static class Silicon {
        public static final Item REDSTONE_CHIPSET = null;

        public static final Item PLUG_PULSAR = null;
    }

    public static class Robotics {

    }
}
