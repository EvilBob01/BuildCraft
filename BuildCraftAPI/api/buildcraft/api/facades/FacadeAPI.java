package buildcraft.api.facades;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import net.neoforged.fml.common.event.FMLInterModComms;

public final class FacadeAPI {
    public static final String IMC_MOD_TARGET = "buildcraftsilicon";
    public static final String IMC_FACADE_DISABLE = "facade_disable_block";
    public static final String IMC_FACADE_CUSTOM = "facade_custom_map_block_item";
    public static final String NBT_CUSTOM_BLOCK_REG_KEY = "block_registry_name";
    public static final String NBT_CUSTOM_BLOCK_META = "block_meta";
    public static final String NBT_CUSTOM_ITEM_STACK = "item_stack";

    public static IFacadeItem facadeItem;
    public static IFacadeRegistry registry;

    private FacadeAPI() {

    }

    public static void disableBlock(Block block) {
        FMLInterModComms.sendMessage(IMC_MOD_TARGET, IMC_FACADE_DISABLE, block.builtInRegistryHolder().key().location());
    }

    public static void mapStateToStack(BlockState state, ItemStack stack) {
        CompoundTag nbt = new CompoundTag();
        nbt.setString(NBT_CUSTOM_BLOCK_REG_KEY, state.getBlock().builtInRegistryHolder().key().location().toString());
        nbt.setInteger(NBT_CUSTOM_BLOCK_META, state.getBlock().getMetaFromState(state));
        nbt.setTag(NBT_CUSTOM_ITEM_STACK, stack.serializeNBT());
        FMLInterModComms.sendMessage(IMC_MOD_TARGET, IMC_FACADE_CUSTOM, nbt);
    }

    public static boolean isFacadeMessageId(String id) {
        return IMC_FACADE_CUSTOM.equals(id) //
            || IMC_FACADE_DISABLE.equals(id);
    }
}
