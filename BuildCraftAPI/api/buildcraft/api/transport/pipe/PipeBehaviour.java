package buildcraft.api.transport.pipe;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.capabilities.BlockCapability;
import buildcraft.lib.net.MessageContext;
import net.neoforged.api.distmarker.Dist;

import buildcraft.api.core.EnumPipePart;

public abstract class PipeBehaviour {
    public final IPipe pipe;

    public PipeBehaviour(IPipe pipe) {
        this.pipe = pipe;
    }

    public PipeBehaviour(IPipe pipe, CompoundTag nbt) {
        this.pipe = pipe;
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();

        return nbt;
    }

    public void writePayload(FriendlyByteBuf buffer, Dist side) {}

    public void readPayload(FriendlyByteBuf buffer, Dist side, MessageContext ctx) throws IOException {}

    /** @deprecated Replaced by {@link #getTextureData(Direction)}. */
    @Deprecated
    public int getTextureIndex(Direction face) {
        return 0;
    }

    /** Gets the texture data to use for the specified face. This may return null for the center, which indicates that
     * the center of the pipe will use the face texture instead.
     * 
     * @param face Null indicates the center of the pipe.
     * @return The texture data for the given face. This may be null, but only for the center! */
    public PipeFaceTex getTextureData(Direction face) {
        return PipeFaceTex.get(getTextureIndex(face));
    }

    // Event handling

    public boolean canConnect(Direction face, PipeBehaviour other) {
        return true;
    }

    public boolean canConnect(Direction face, BlockEntity oTile) {
        return true;
    }

    /** Used to force a connection to a given tile, even if the {@link PipeFlow} wouldn't normally connect to it. */
    public boolean shouldForceConnection(Direction face, BlockEntity oTile) {
        return false;
    }

    public boolean onPipeActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        return false;
    }

    public void onEntityCollide(Entity entity) {}

    public void onTick() {}

    @Override
    public boolean hasCapability(@Nonnull BlockCapability<?, Direction> capability, Direction facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    public <T> T getCapability(@Nonnull BlockCapability<T, Direction> capability, Direction facing) {
        return null;
    }

    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {}
}
