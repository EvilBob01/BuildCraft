package buildcraft.transport.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** BlockHitResult subclass that carries a pipe sub-hit index identifying which
 *  sub-part of the pipe block was targeted (center, face connector, pluggable, wire). */
public class PipeHitResult extends BlockHitResult {
    public final int subHit;

    public PipeHitResult(BlockHitResult base, int subHit) {
        super(base.getLocation(), base.getDirection(), base.getBlockPos(), base.isInside());
        this.subHit = subHit;
    }
}
