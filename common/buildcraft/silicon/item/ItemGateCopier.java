package buildcraft.silicon.item;

import java.util.List;

import javax.annotation.Nonnull;

import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;

public class ItemGateCopier extends ItemBC_Neptune {
    private static final String NBT_DATA = "gate_data";

    public ItemGateCopier(String id) {
        super(id);
        setMaxStackSize(1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addModelVariants(TIntObjectHashMap<ModelResourceLocation> variants) {
        addVariant(variants, 0, "empty");
        addVariant(variants, 1, "full");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, Level world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (getMetadata(stack) != 0) {
            tooltip.add(LocaleUtil.localize("buildcraft.item.nonclean.usage"));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        if (player.isSneaking()) {
            return clearData(StackUtil.asNonNull(stack));
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    private InteractionResultHolder<ItemStack> clearData(@Nonnull ItemStack stack) {
        if (getMetadata(stack) == 0) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        CompoundTag nbt = NBTUtilBC.getItemData(stack);
        nbt.removeTag(NBT_DATA);
        if (nbt.hasNoTags()) {
            stack.setTagCompound(null);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public int getMetadata(ItemStack stack) {
        return getCopiedGateData(stack) != null ? 1 : 0;
    }

    public static CompoundTag getCopiedGateData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(NBT_DATA)) ? tag.getCompound(NBT_DATA) : null;
    }

    public static void setCopiedGateData(ItemStack stack, CompoundTag nbt) {
        NBTUtilBC.getItemData(stack).put(NBT_DATA, nbt);
    }
}
