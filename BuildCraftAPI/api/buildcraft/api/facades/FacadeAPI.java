package buildcraft.api.facades;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public final class FacadeAPI {
    public static final String IMC_MOD_TARGET = "buildcraftsilicon";
    public static final String IMC_FACADE_DISABLE = "facade_disable_block";
    public static final String IMC_FACADE_CUSTOM = "facade_custom_map_block_item";
    public static final String NBT_CUSTOM_BLOCK_REG_KEY = "block_registry_name";
    // NBT_CUSTOM_BLOCK_META is unused in 1.21 (block metadata removed in 1.13)
    public static final String NBT_CUSTOM_ITEM_STACK = "item_stack";

    public static IFacadeItem facadeItem;
    public static IFacadeRegistry registry;

    private FacadeAPI() {}

    // TODO (Phase 8 — Registry): FMLInterModComms was removed in NeoForge 1.21.
    // Facade block exclusion must be rewritten using a capability or event-based API.
    public static void disableBlock(Block block) {
        // stub — FMLInterModComms removed
    }

    // TODO (Phase 8 — Registry): getMetaFromState removed in 1.13; ItemStack.serializeNBT() removed in 1.21.
    // mapStateToStack must be rewritten for NeoForge: use BlockState's registry key and DataComponents.
    public static void mapStateToStack(BlockState state, ItemStack stack) {
        // stub — see TODO above
    }

    public static boolean isFacadeMessageId(String id) {
        return IMC_FACADE_CUSTOM.equals(id)
            || IMC_FACADE_DISABLE.equals(id);
    }
}
