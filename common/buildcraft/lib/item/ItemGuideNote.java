package buildcraft.lib.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ActionResult;
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
        NBTUtilBC.getItemData(stack).setString("note_id", noteId);
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        player.openGui(BCLib.INSTANCE, 1, world, 0, 0, 0);
        return new ActionResult<>(InteractionResult.SUCCESS, player.getHeldItem(hand));
    }
}
