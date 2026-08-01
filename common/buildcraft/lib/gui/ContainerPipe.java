package buildcraft.lib.gui;

import net.minecraft.world.entity.player.Player;

import buildcraft.api.transport.pipe.IPipeHolder;

public abstract class ContainerPipe extends ContainerBC_Neptune {

    public final IPipeHolder pipeHolder;

    public ContainerPipe(Player player, IPipeHolder pipeHolder) {
        super(player);
        this.pipeHolder = pipeHolder;
    }

    @Override
    public final boolean stillValid(Player player) {
        return pipeHolder.canPlayerInteract(player);
    }
}
