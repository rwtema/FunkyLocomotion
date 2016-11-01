package com.rwtema.funkylocomotion.network;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.WeakHashMap;

public class FLNetwork {
	private static final WeakHashMap<World, PlayerChunkMap> cache = new WeakHashMap<World, PlayerChunkMap>();
	public static SimpleNetworkWrapper net;

	public static void init() {
		net = new SimpleNetworkWrapper("FLoco");
		net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.SERVER);
		net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.CLIENT);
		net.registerMessage(MessageObstruction.Handler.class, MessageObstruction.class, 1, Side.SERVER);
		net.registerMessage(MessageObstruction.Handler.class, MessageObstruction.class, 1, Side.CLIENT);
	}

	public static void sendToAllWatchingChunk(World world, BlockPos pos, IMessage message) {
		PlayerChunkMapEntry watcher = getChunkWatcher(world, pos);

		if (watcher != null)
			watcher.sendPacket(net.getPacketFrom(message));
	}

	private static PlayerChunkMap getPlayerManager(World world) {
		if (!cache.containsKey(world)) {
			if (!(world instanceof WorldServer)) {
				cache.put(world, null);
			} else
				cache.put(world, ((WorldServer) world).getPlayerChunkMap());
		}

		return cache.get(world);
	}

	public static void updateChunk(Chunk chunk) {
		World world = chunk.worldObj;
		PlayerChunkMapEntry watcher = getChunkWatcher(chunk, world);
		if (watcher != null) watcher.update();
	}

	public static PlayerChunkMapEntry getChunkWatcher(Chunk chunk) {
		return getChunkWatcher(chunk, chunk.worldObj);
	}

	public static PlayerChunkMapEntry getChunkWatcher(Chunk chunk, World world) {
		PlayerChunkMap playerManager = getPlayerManager(world);
		return playerManager != null ? playerManager.getEntry(chunk.xPosition >> 4, chunk.zPosition >> 4) : null;
	}

	public static PlayerChunkMapEntry getChunkWatcher(World world, BlockPos pos) {
		return getChunkWatcher(world, pos.getX(), pos.getZ());
	}

	public static PlayerChunkMapEntry getChunkWatcher(World world, int x, int z) {
		PlayerChunkMap playerManager = getPlayerManager(world);
		return playerManager != null ? playerManager.getEntry(x >> 4, z >> 4) : null;
	}
}
