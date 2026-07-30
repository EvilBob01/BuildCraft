package buildcraft.api.transport.pluggable;

import java.util.Objects;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;

public abstract class PluggableModelKey {
    public final BlockRenderLayer layer;
    public final Direction side;
    private final int hash;

    public PluggableModelKey(BlockRenderLayer layer, Direction side) {
        if (layer != BlockRenderLayer.CUTOUT && layer != BlockRenderLayer.TRANSLUCENT) {
            throw new IllegalArgumentException("Can only use CUTOUT or TRANSLUCENT at the moment (was " + layer + ")");
        }
        if (side == null) throw new NullPointerException("side");
        this.layer = layer;
        this.side = side;
        this.hash = Objects.hash(layer, side);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PluggableModelKey other = (PluggableModelKey) obj;
        if (layer != other.layer) return false;
        if (side != other.side) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
