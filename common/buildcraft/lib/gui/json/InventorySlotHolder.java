package buildcraft.lib.gui.json;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class InventorySlotHolder {

    public final Slot[] slots;

    public InventorySlotHolder(AbstractContainerMenu container, Container inventory) {
        List<Slot> list = new ArrayList<>();
        for (Slot s : container.slots) {
            if (s.container == inventory) {
                list.add(s);
            }
        }
        slots = list.toArray(new Slot[0]);
    }

    public InventorySlotHolder(AbstractContainerMenu container, IItemHandler inventory) {
        List<Slot> list = new ArrayList<>();
        for (Slot s : container.slots) {
            if (s instanceof SlotItemHandler && ((SlotItemHandler) s).getItemHandler() == inventory) {
                list.add(s);
            }
        }
        slots = list.toArray(new Slot[0]);
    }
}
