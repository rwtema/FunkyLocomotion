package com.rwtema.funkylocomotion.movers;

import com.rwtema.funkylocomotion.blocks.BlockMoving;
import com.rwtema.funkylocomotion.blocks.TileMovingServer;
import com.rwtema.funkylocomotion.description.DescriptorRegistry;
import com.rwtema.funkylocomotion.factory.FactoryRegistry;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.network.FLNetwork;
import com.rwtema.funkylocomotion.network.MessageClearTile;
import framesapi.BlockPos;
import framesapi.IDescriptionProxy;
import framesapi.IMoveFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

public class MoveManager {
	public static final NBTTagCompound airBlockTag;
	public static final NBTTagCompound airDescTag;

	static {
		airBlockTag = new NBTTagCompound();
		airDescTag = new NBTTagCompound();
		if (Block.getIdFromBlock(Blocks.air) != 0)
			airDescTag.setInteger("Block", Block.getIdFromBlock(Blocks.air));
	}

	public static void startMoving(World world, List<BlockPos> posList, ForgeDirection dir, int maxTime) {
		ArrayList<BlockLink> links = new ArrayList<BlockLink>(posList.size());
		for (BlockPos blockPos : posList) {
			links.add(new BlockLink(blockPos, blockPos.advance(dir)));
		}

		startMoving(world, world, links, dir, maxTime);
	}

	@SuppressWarnings("unchecked")
	public static void startMoving(World srcWorld, World dstWorld, List<BlockLink> links, ForgeDirection dir, int maxTime) {

		if (dir != ForgeDirection.UNKNOWN && srcWorld != dstWorld) {
			throw new IllegalArgumentException("Trying to regular shift between worlds");
		}

		HashSet<BlockPos> srcBlocks = new HashSet<BlockPos>();
		HashSet<BlockPos> dstBlocks = new HashSet<BlockPos>();

		for (BlockLink link : links) {
			srcBlocks.add(link.srcPos);
			dstBlocks.add(link.dstPos);
		}

		HashSet<BlockPos> dstBlocksToBeDestroyed = new HashSet<BlockPos>();
		HashSet<BlockPos> srcBlocksToBecomeAir = new HashSet<BlockPos>();
		srcBlocksToBecomeAir.addAll(srcBlocks);
		dstBlocksToBeDestroyed.addAll(dstBlocks);

		if(srcWorld == dstWorld){
			srcBlocksToBecomeAir.removeAll(dstBlocks);
			dstBlocksToBeDestroyed.removeAll(srcBlocks);
		}

		Map<BlockPos, Entry> dstTileEntries = new HashMap<BlockPos, Entry>();

		for (BlockPos pos : dstBlocksToBeDestroyed) {
			BlockHelper.breakBlockWithDrop(dstWorld, pos);
		}

		Set<Chunk> srcChunks = new HashSet<Chunk>();
		HashSet<EntityPlayer> srcWatchingPlayers = new HashSet<EntityPlayer>();
		HashSet inventories = new HashSet();

		for (BlockLink link : links) {
			BlockPos dstPos = link.dstPos;
			BlockPos srcPos = link.srcPos;

			Entry e = new Entry(dstPos);

			e.block = srcWorld.getBlock(srcPos.x, srcPos.y, srcPos.z);
			e.meta = srcWorld.getBlockMetadata(srcPos.x, srcPos.y, srcPos.z);

			e.lightopacity = e.block.getLightOpacity(srcWorld, srcPos.x, srcPos.y, srcPos.z);

			e.lightlevel = e.block.getLightValue(srcWorld, srcPos.x, srcPos.y, srcPos.z);

			List<AxisAlignedBB> axes = new ArrayList<AxisAlignedBB>();
			e.block.addCollisionBoxesToList(srcWorld, srcPos.x, srcPos.y, srcPos.z, TileEntity.INFINITE_EXTENT_AABB, axes, null);

			if (axes.size() > 0) {
				e.bb = new ArrayList<AxisAlignedBB>();
				for (AxisAlignedBB bb : axes) {
					e.bb.add(AxisAlignedBB.getBoundingBox(
							bb.minX - srcPos.x,
							bb.minY - srcPos.y,
							bb.minZ - srcPos.z,
							bb.maxX - srcPos.x,
							bb.maxY - srcPos.y,
							bb.maxZ - srcPos.z
					));
				}
			}

			NBTTagCompound descriptor = new NBTTagCompound();

			descriptor.setInteger("Block", Block.getIdFromBlock(e.block));
			if (e.meta != 0)
				descriptor.setByte("Meta", (byte) e.meta);

			TileEntity tile = srcWorld.getTileEntity(srcPos.x, srcPos.y, srcPos.z);
			if (tile != null) {
				boolean flag = false;
				for (IDescriptionProxy d : DescriptorRegistry.getProxyList()) {
					if (d.canHandleTile(tile)) {
						d.addDescriptionToTags(descriptor, tile);
						if (!"".equals("DescID"))
							descriptor.setString("DescID", d.getID());
						flag = true;
						break;
					}
				}
				if (!flag) {
					descriptor.setBoolean("DNR", true);
				}

				if (tile instanceof IInventory) {
					inventories.add(tile);
				}
			}

			e.description = descriptor;

			srcChunks.add(BlockHelper.getChunk(srcWorld, srcPos));

			dstTileEntries.put(dstPos, e);
		}


		for (Chunk c : srcChunks) {
			List<NextTickListEntry> ticks = srcWorld.getPendingBlockUpdates(c, false);
			if (ticks != null) {
				long k = srcWorld.getTotalWorldTime();
				for (NextTickListEntry tick : ticks) {
					BlockPos p = (new BlockPos(tick.xCoord, tick.yCoord, tick.zCoord));

					if (BlockHelper.getBlock(c, p) != tick.func_151351_a())
						continue;

					p = p.advance(dir);

					if (!dstTileEntries.containsKey(p))
						continue;

					Entry e = dstTileEntries.get(p);

					e.scheduledTickTime = (int) (tick.scheduledTime - k);
					e.scheduledTickPriority = tick.priority;
				}
			}

			PlayerManager.PlayerInstance chunkWatcher = FLNetwork.getChunkWatcher(c, srcWorld);
			if (chunkWatcher != null)
				srcWatchingPlayers.addAll(chunkWatcher.playersWatchingChunk);
		}

		// from now on - NO BLOCK UPDATES


		for (BlockLink link : links) {
			IMoveFactory factory = FactoryRegistry.getFactory(srcWorld, link.srcPos);
			dstTileEntries.get(link.dstPos).blockTag = factory.destroyBlock(srcWorld, link.srcPos);
		}

		// let there be updates;

		for (EntityPlayer watchingPlayer : srcWatchingPlayers) {
			if (watchingPlayer.openContainer != watchingPlayer.inventoryContainer && watchingPlayer.openContainer != null) {
				for (Object o : watchingPlayer.openContainer.inventorySlots) {
					Slot s = (Slot) o;
					if (inventories.contains(s.inventory)) {
						watchingPlayer.closeScreen();
						break;
					}
				}
			}
		}

		for(BlockLink link : links){
			BlockPos dstPos = link.dstPos;
			BlockPos srcPos = link.srcPos;
			BlockHelper.silentClear(BlockHelper.getChunk(dstWorld, dstPos), dstPos);
			if(dir != ForgeDirection.UNKNOWN)
				FLNetwork.sendToAllWatchingChunk(srcWorld, srcPos.x, srcPos.y, srcPos.z, new MessageClearTile(srcPos));
			dstWorld.removeTileEntity(dstPos.x, dstPos.y, dstPos.z);
		}

		for (BlockPos pos : srcBlocksToBecomeAir) {
			BlockHelper.postUpdateBlock(dstWorld, pos);
		}

		ArrayList<TileMovingServer> tiles = new ArrayList<TileMovingServer>();

		for (Entry e : dstTileEntries.values()) {
			dstWorld.setBlock(e.pos.x, e.pos.y, e.pos.z, BlockMoving.instance, 0, 1);
			TileMovingServer tile = (TileMovingServer) dstWorld.getTileEntity(e.pos.x, e.pos.y, e.pos.z);
			tile.block = e.blockTag;
			tile.desc = e.description;
			tile.dir = dir.ordinal();
			tile.maxTime = maxTime;

			tile.lightLevel = e.lightlevel;
			tile.lightOpacity = e.lightopacity;

			tile.scheduledTickTime = e.scheduledTickTime;
			tile.scheduledTickPriority = e.scheduledTickPriority;
			if (e.bb != null)
				tile.collisions = e.bb.toArray(new AxisAlignedBB[e.bb.size()]);

			tile.isAir = false;

			tiles.add(tile);
		}

		for(BlockLink link : links){
			BlockPos srcPos = link.srcPos;
			if (srcWorld != dstWorld || !dstTileEntries.containsKey(srcPos)) {
				srcWorld.setBlock(srcPos.x, srcPos.y, srcPos.z, BlockMoving.instance, 0, 1);
				TileMovingServer tile = (TileMovingServer) srcWorld.getTileEntity(srcPos.x, srcPos.y, srcPos.z);

				if (dir != ForgeDirection.UNKNOWN) {
					tile.block = (NBTTagCompound) airBlockTag.copy();
					tile.desc = (NBTTagCompound) airDescTag.copy();
					tile.dir = dir.ordinal();
					tile.maxTime = maxTime;

					tile.lightLevel = 0;
					tile.lightOpacity = 0;
					tile.isAir = true;
				} else {
					FLNetwork.sendToAllWatchingChunk(srcWorld, srcPos.x, srcPos.y, srcPos.z, new MessageClearTile(srcPos));
					Entry e = dstTileEntries.get(link.dstPos);
					tile.block = (NBTTagCompound) airBlockTag.copy();
					if (e.description != null)
						tile.desc = (NBTTagCompound) e.description.copy();
					else
						tile.desc = (NBTTagCompound) airDescTag.copy();
					tile.dir = 7;
					tile.maxTime = maxTime;

					tile.lightLevel = e.lightlevel;
					tile.lightOpacity = e.lightopacity;

					if (e.bb != null)
						tile.collisions = e.bb.toArray(new AxisAlignedBB[e.bb.size()]);

					tile.isAir = true;
				}

				tiles.add(tile);
			}
		}


		for (TileMovingServer tile : tiles) {
			PlayerManager.PlayerInstance watcher = FLNetwork.getChunkWatcher(tile.getWorldObj(), tile.xCoord, tile.zCoord);
			if(watcher != null) {
				S23PacketBlockChange pkt = new S23PacketBlockChange(tile.xCoord, tile.yCoord, tile.zCoord, tile.getWorldObj());
				watcher.sendToAllPlayersWatchingChunk(pkt);
			}
		}

		for (TileMovingServer tile : tiles) {
			PlayerManager.PlayerInstance watcher = FLNetwork.getChunkWatcher(tile.getWorldObj(), tile.xCoord, tile.zCoord);
			if(watcher != null) {
				Packet packet = tile.getDescriptionPacket();
				if (packet != null)
					watcher.sendToAllPlayersWatchingChunk(packet);
			}
		}
	}


	public static void finishMoving() {
		List<TileMovingServer> tiles = MovingTileRegistry.getTilesFinishedMoving();
		HashSet<Chunk> chunks = new HashSet<Chunk>();

		// Clear Tiles
		for (TileMovingServer tile : tiles) {
			chunks.add(tile.getWorldObj().getChunkFromBlockCoords(tile.xCoord, tile.zCoord));
			tile.getWorldObj().setBlock(tile.xCoord, tile.yCoord, tile.zCoord, Blocks.air, 0, 0);
			tile.getWorldObj().setBlock(tile.xCoord, tile.yCoord, tile.zCoord, Blocks.stone, 0, 0);
		}

		// Set Block/Tile
		for (TileMovingServer tile : tiles) {
			BlockPos pos = new BlockPos(tile);
			if (tile.block != null) {
				BlockHelper.silentClear(BlockHelper.getChunk(tile.getWorldObj(), pos), pos);
				Block block = Block.getBlockFromName(tile.block.getString("Block"));
				if (block == null) block = Blocks.air;
				IMoveFactory factory = FactoryRegistry.getFactory(block);
				factory.recreateBlock(tile.getWorldObj(), pos, tile.block);
			}
		}

		// Update Blocks
		for (TileMovingServer tile : tiles) {
			BlockPos pos = new BlockPos(tile);
			BlockHelper.postUpdateBlock(tile.getWorldObj(), pos);
			if (tile.scheduledTickTime != -1)
				tile.getWorldObj().scheduleBlockUpdateWithPriority(
						tile.xCoord, tile.yCoord, tile.zCoord,
						BlockHelper.getBlock(tile.getWorldObj(), pos),
						tile.scheduledTickTime - tile.maxTime, tile.scheduledTickPriority);

		}

		// Send Update Packets
		for (Chunk chunk : chunks) {
			chunk.isModified = true;
			FLNetwork.updateChunk(chunk);
		}

		// Redocached Activation
		for (TileMovingServer tile : tiles) {
			if (tile.activatingPlayer != null) {
				EntityPlayer player = tile.activatingPlayer.get();
				if (player != null) {
					Block b = BlockHelper.getBlock(tile.getWorldObj(), new BlockPos(tile));
					b.onBlockActivated(tile.getWorldObj(),
							tile.xCoord, tile.yCoord, tile.zCoord,
							player, tile.activatingSide,
							tile.activatingHitX, tile.activatingHitY, tile.activatingHitZ);
				}
			}
		}
	}




	private static class Entry {
		public int scheduledTickTime = -1;
		public int scheduledTickPriority;
		NBTTagCompound blockTag;
		NBTTagCompound description;
		final BlockPos pos;
		Block block;
		int meta;
		List<AxisAlignedBB> bb = null;
		int lightlevel;
		int lightopacity;

		public Entry(BlockPos pos) {
			this.pos = pos;
		}
	}

	public static class BlockLink {
		BlockPos srcPos;
		BlockPos dstPos;

		public BlockLink(BlockPos srcPos, BlockPos dstPos) {
			this.srcPos = srcPos;
			this.dstPos = dstPos;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			BlockLink blockLink = (BlockLink) o;

			if (!dstPos.equals(blockLink.dstPos)) return false;
			if (!srcPos.equals(blockLink.srcPos)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = srcPos.hashCode();
			result = 31 * result + dstPos.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return "BlockLink{" + srcPos.toString() +
					", " + dstPos.toString() +
					'}';
		}
	}

}
