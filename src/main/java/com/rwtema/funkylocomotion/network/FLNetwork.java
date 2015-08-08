package com.rwtema.funkylocomotion.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import framesapi.BlockPos;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.WeakHashMap;

public class FLNetwork {
    public static SimpleNetworkWrapper net;
    private static final WeakHashMap<World, PlayerManager> cache = new WeakHashMap<World, PlayerManager>();

    public static void init() {
        net = new SimpleNetworkWrapper("FLoco");
        net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.SERVER);
        net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.CLIENT);
        net.registerMessage(MessageObstruction.Handler.class, MessageObstruction.class, 1, Side.SERVER);
        net.registerMessage(MessageObstruction.Handler.class, MessageObstruction.class, 1, Side.CLIENT);
    }

    public static void sendToAllWatchingChunk(World world, int x, int y, int z, IMessage message) {
        PlayerManager.PlayerInstance watcher = getChunkWatcher(world, x, z);

        if (watcher != null)
            watcher.sendToAllPlayersWatchingChunk(net.getPacketFrom(message));
    }

    private static PlayerManager getPlayerManager(World world) {
        if (!cache.containsKey(world)) {
            if (!(world instanceof WorldServer)) {
                cache.put(world, null);
            } else
                cache.put(world, ((WorldServer) world).getPlayerManager());
        }

        return cache.get(world);
    }

    public static void updateChunk(Chunk chunk) {
        World world = chunk.worldObj;
        PlayerManager.PlayerInstance watcher = getChunkWatcher(chunk, world);
        if (watcher != null) watcher.sendChunkUpdate();
    }

	public static PlayerManager.PlayerInstance getChunkWatcher(Chunk chunk) {
		return getChunkWatcher(chunk, chunk.worldObj);
	}

    public static PlayerManager.PlayerInstance getChunkWatcher(Chunk chunk, World world) {
        PlayerManager playerManager = getPlayerManager(world);
        return playerManager != null ? playerManager.getOrCreateChunkWatcher(chunk.xPosition, chunk.zPosition, false) : null;
    }

    public static PlayerManager.PlayerInstance getChunkWatcher(World world, BlockPos pos) {
        return getChunkWatcher(world, pos.x, pos.z);
    }

    public static PlayerManager.PlayerInstance getChunkWatcher(World world, int x, int z) {
        PlayerManager playerManager = getPlayerManager(world);
        return playerManager != null ? playerManager.getOrCreateChunkWatcher(x >> 4, z >> 4, false) : null;
    }
}
