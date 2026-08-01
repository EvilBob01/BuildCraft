package buildcraft.builders.container;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.world.entity.player.Player;

import buildcraft.lib.net.MessageContext;
import net.neoforged.api.distmarker.Dist;

import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.builders.filler.FillerType;
import buildcraft.core.BCCoreProxy;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class ContainerFillerPlanner extends ContainerBC_Neptune implements IContainerFilling {
    public final AddonFillerPlanner addon;
    private final FullStatement<IFillerPattern> patternStatementClient = new FullStatement<>(
        FillerType.INSTANCE,
        4,
        (statement, paramIndex) -> onStatementChange()
    );

    public ContainerFillerPlanner(Player player) {
        super(player);
        Pair<VolumeBox, EnumAddonSlot> selectingVolumeBoxAndSlot = EnumAddonSlot.getSelectingVolumeBoxAndSlot(
            player,
            BCCoreProxy.getProxy().getVolumeBoxes(player.level())
        );
        addon = Optional.ofNullable(selectingVolumeBoxAndSlot.getLeft())
            .map(volumeBox -> volumeBox.addons.get(selectingVolumeBoxAndSlot.getRight()))
            .map(AddonFillerPlanner.class::cast)
            .orElseThrow(IllegalStateException::new);
        init();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatementClient() {
        return patternStatementClient;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatement() {
        return addon.patternStatement;
    }

    @Override
    public boolean isInverted() {
        return addon.inverted;
    }

    @Override
    public void setInverted(boolean value) {
        addon.inverted = value;
    }

    @Override
    public void valuesChanged() {
        addon.updateBuildingInfo();
        if (!player.level().isClientSide) {
            WorldSavedDataVolumeBoxes.get(getPlayer().world).setChanged();
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        IContainerFilling.super.readMessage(id, buffer, side, ctx);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean canInteractWith(Player player) {
        return true;
    }
}
