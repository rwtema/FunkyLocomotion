package com.rwtema.funkylocomotion.network;

import com.rwtema.funkylocomotion.blocks.TileMovingBase;
import com.rwtema.funkylocomotion.entity.EntityAirShip;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.WeakHashMap;

public class FLNetwork {
	private static final WeakHashMap<World, PlayerChunkMap> cache = new WeakHashMap<>();
	public static SimpleNetworkWrapper net;

	public static void init() {
		net = new SimpleNetworkWrapper("FLoco");
		net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.SERVER);
		net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.CLIENT);
		net.registerMessage(MessageObstruction.Handler.class, MessageObstruction.class, 1, Side.SERVER);
		net.registerMessage(MessageObstruction.Handler.class, MessageObstruction.class, 1, Side.CLIENT);
		net.registerMessage(MessageOneTimeChat.Handler.class, MessageOneTimeChat.class, 2, Side.SERVER);
		net.registerMessage(MessageOneTimeChat.Handler.class, MessageOneTimeChat.class, 2, Side.CLIENT);
		net.registerMessage(EntityAirShip.MessageCustomPositionSpeed.ServerHandler.class, EntityAirShip.MessageCustomPositionSpeed.class, 3, Side.SERVER);
		net.registerMessage(EntityAirShip.MessageCustomPositionSpeed.ClientHandler.class, EntityAirShip.MessageCustomPositionSpeed.class, 3, Side.CLIENT);
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
		World world = chunk.getWorld();
		PlayerChunkMapEntry watcher = getChunkWatcher(chunk, world);
		if (watcher != null) watcher.update();
	}

	public static PlayerChunkMapEntry getChunkWatcher(Chunk chunk) {
		return getChunkWatcher(chunk, chunk.getWorld());
	}

	public static PlayerChunkMapEntry getChunkWatcher(Chunk chunk, World world) {
		PlayerChunkMap playerManager = getPlayerManager(world);
		return playerManager != null ? playerManager.getEntry(chunk.x, chunk.z) : null;
	}

	public static PlayerChunkMapEntry getChunkWatcher(World world, BlockPos pos) {
		return getChunkWatcher(world, pos.getX(), pos.getZ());
	}

	public static PlayerChunkMapEntry getChunkWatcher(World world, int x, int z) {
		PlayerChunkMap playerManager = getPlayerManager(world);
		return playerManager != null ? playerManager.getEntry(x >> 4, z >> 4) : null;
	}

	public static IBlockState readState(PacketBuffer buf) {
		Block block = Block.REGISTRY.getObjectById(buf.readVarInt());
		int meta = buf.readUnsignedByte();
		return block.getStateFromMeta(meta);
	}


	public static void writeBlockState(PacketBuffer buffer, IBlockState state) {
		buffer.writeVarInt(Block.REGISTRY.getIDForObject(state.getBlock()));
		buffer.writeByte(state.getBlock().getMetaFromState(state));
	}

	public static void writeAxisArray(AxisAlignedBB[] bbs, PacketBuffer buffer) {
		int n = Math.min(bbs.length, 255);
		buffer.writeByte((byte) n);
		for (int i = 0; i < n; i++) {
			AxisAlignedBB bb = bbs[i];
			buffer.writeFloat((float) bb.minX);
			buffer.writeFloat((float) bb.minY);
			buffer.writeFloat((float) bb.minZ);
			buffer.writeFloat((float) bb.maxX);
			buffer.writeFloat((float) bb.maxY);
			buffer.writeFloat((float) bb.maxZ);
		}
	}

	public static AxisAlignedBB[] readAxisArray(PacketBuffer buffer) {
		int i = buffer.readUnsignedByte();
		if (i <= 0) return TileMovingBase.BLANK;

		AxisAlignedBB[] bbs = new AxisAlignedBB[i];
		for (int j = 0; j < i; j++) {
			float x1 = buffer.readFloat();
			float y1 = buffer.readFloat();
			float z1 = buffer.readFloat();
			float x2 = buffer.readFloat();
			float y2 = buffer.readFloat();
			float z2 = buffer.readFloat();
			if (x1 == 0 && y1 == 0 && z1 == 0 && x2 == 1 && y2 == 1 && z2 == 1) {
				bbs[j] = Block.FULL_BLOCK_AABB;
			} else
				bbs[j] = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
		}
		return bbs;
	}

	public static Vec3d readVec3d(ByteBuf buf){
		return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	public static void writeVec3d(ByteBuf buf, Vec3d vec3d){
		buf.writeDouble(vec3d.x);
		buf.writeDouble(vec3d.y);
		buf.writeDouble(vec3d.z);
	}

}
