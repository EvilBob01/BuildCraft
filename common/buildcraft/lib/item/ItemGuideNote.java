package buildcraft.lib.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import buildcraft.lib.BCLib;
import buildcraft.lib.misc.NBTUtilBC;

public class ItemGuideNote extends ItemBC_Neptune {

    public ItemGuideNote(String id) {
        super(id);
    }

    public static String getNoteId(ItemStack stack) {
        return NBTUtilBC.getItemData(stack).getString("note_id");
    }

    public ItemStack storeNoteId(String noteId) {
        ItemStack stack = new ItemStack(this);
        NBTUtilBC.getItemData(stack).putString("note_id", noteId);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        // TODO (Phase 6 — GUI): player.openGui removed; open guide note screen via MenuProvider
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }
}
