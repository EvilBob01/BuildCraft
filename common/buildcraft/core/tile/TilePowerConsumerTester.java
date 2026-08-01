package buildcraft.core.tile;

import net.minecraft.core.HolderLookup;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.tile.ITickable;
import buildcraft.lib.tile.TileBC_Neptune;

public class TilePowerConsumerTester extends TileBC_Neptune implements IMjReceiver, ITickable, IDebuggable {

    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);
    private long lastReceived;
    private long nextTickReceived;
    private long lastTickReceived;
    private long totalReceived;

    public TilePowerConsumerTester(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        caps.addProvider(mjCaps);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        lastReceived = nbt.getLong("last");
        nextTickReceived = nbt.getLong("nt");
        lastTickReceived = nbt.getLong("lt");
        totalReceived = nbt.getLong("total");
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putLong("last", lastReceived);
        nbt.putLong("nt", nextTickReceived);
        nbt.putLong("lt", lastTickReceived);
        nbt.putLong("total", totalReceived);
    }

    // ITickable

    @Override
    public void update() {
        lastTickReceived = nextTickReceived;
        nextTickReceived = 0;
    }

    // IMjReceiver

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return 100000 * MjAPI.MJ;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        if (!simulate) {
            lastReceived = microJoules;
            nextTickReceived += microJoules;
            totalReceived += microJoules;
        }
        return 0;
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("");
        left.add("Last received = " + LocaleUtil.localizeMj(lastReceived));
        left.add("Tick received = " + LocaleUtil.localizeMj(lastTickReceived));
        left.add("Total received = " + LocaleUtil.localizeMj(totalReceived));
    }
}
