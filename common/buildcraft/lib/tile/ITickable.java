package buildcraft.lib.tile;

/** Replacement for the removed net.minecraft.util.ITickable (MC 1.12). Tiles that implement this
 *  are ticked every server tick via the block's getTicker() hook in {@link buildcraft.lib.block.BlockBCTile_Neptune}. */
public interface ITickable {
    void update();
}
