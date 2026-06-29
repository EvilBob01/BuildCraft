package buildcraft.transport.client.model.key;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class KeyPlugPowerAdaptor extends PluggableModelKey {
    public KeyPlugPowerAdaptor(Direction side) {
        super(BlockRenderLayer.CUTOUT, side);
    }
}
