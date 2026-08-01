package buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;

import buildcraft.lib.net.MessageContext;
import net.neoforged.api.distmarker.Dist;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IFlowPowerLike;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventPower;
import buildcraft.api.transport.pipe.PipeEventRedstoneFlux;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.MathUtil;

import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import buildcraft.transport.statements.ActionPowerLimit;

public class PipeBehaviourLimiter extends PipeBehaviour {

    public static final int MAX_SHIFT = 6;

    private int limitShift = 0;

    public PipeBehaviourLimiter(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourLimiter(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        limitShift = MathUtil.clamp(nbt.getInt("limitShift"), 0, MAX_SHIFT);
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.putInt("limitShift", limitShift);
        return nbt;
    }

    @Override
    public void readPayload(FriendlyByteBuf buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        limitShift = buffer.readUnsignedByte();
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, Side side) {
        super.writePayload(buffer, side);
        buffer.writeByte(limitShift);
    }

    @PipeEventHandler
    public void configurePower(PipeEventPower.Configure event) {
        if (limitShift == MAX_SHIFT) {
            event.disableTransfer();
        } else {
            event.setMaxPower(event.getMaxPower() >> limitShift);
        }
    }

    @PipeEventHandler
    public void configurePower(PipeEventRedstoneFlux.Configure event) {
        if (limitShift == MAX_SHIFT) {
            event.disableTransfer();
        } else {
            event.setMaxPower(event.getMaxPower() >> limitShift);
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        if (event.action instanceof ActionPowerLimit) {
            limitShift = ((ActionPowerLimit) event.action).limitShift;

            requestReconfigure();
        }
    }

    @Override
    public boolean onPipeActivate(
        Player player, BlockHitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part
    ) {
        if (EntityUtil.getWrenchHand(player) == null) {
            return false;
        }

        if (!player.level().isClientSide) {
            EntityUtil.activateWrench(player, trace);
            limitShift++;
            if (limitShift > MAX_SHIFT) {
                limitShift = 0;
            }

            boolean isRf = pipe.getFlow() instanceof PipeFlowRedstoneFlux;
            final int limit;
            if (limitShift == MAX_SHIFT) {
                limit = 0;
            } else if (isRf) {
                RedstoneFluxTransferInfo transferInfo = PipeApi.getRfTransferInfo(pipe.getDefinition());
                limit = transferInfo.transferPerTick >> limitShift;
            } else {
                PowerTransferInfo transferInfo = PipeApi.getPowerTransferInfo(pipe.getDefinition());
                limit = (int) ((transferInfo.transferPerTick >> limitShift) / MjAPI.MJ);
            }
            String key = "chat.pipe." + (isRf ? "rf" : "power") + ".iron.mode";
            TextComponentTranslation chat = new TextComponentTranslation(key, limit);
            player.sendStatusMessage(chat, true);

            requestReconfigure();
        }
        return true;
    }

    private void requestReconfigure() {
        if (pipe.getFlow() instanceof IFlowPowerLike) {
            ((IFlowPowerLike) pipe.getFlow()).reconfigure();
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @Override
    public int getTextureIndex(Direction face) {
        return MAX_SHIFT - limitShift;
    }
}
