package buildcraft.transport.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import buildcraft.lib.net.MessageManager;

public class PipeItemMessageQueue {

    private static final Map<ServerPlayer, MessageMultiPipeItem> cachedPlayerPackets = new WeakHashMap<>();

    public static void serverTick() {
        for (Entry<ServerPlayer, MessageMultiPipeItem> entry : cachedPlayerPackets.entrySet()) {
            MessageManager.sendTo(entry.getValue(), entry.getKey());
        }
        cachedPlayerPackets.clear();
    }

    public static void appendTravellingItem(Level world, BlockPos pos, int stackId, byte stackCount, boolean toCenter,
        Direction side, @Nullable DyeColor colour, byte timeToDest) {
        ServerLevel server = (ServerLevel) world;
        List<ServerPlayer> players = server.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false);
        if (players.isEmpty()) {
            // No-one was watching this chunk.
            return;
        }
        for (ServerPlayer player : players) {
            cachedPlayerPackets.computeIfAbsent(player, pl -> new MessageMultiPipeItem()).append(pos, stackId,
                stackCount, toCenter, side, colour, timeToDest);
        }
    }
}
