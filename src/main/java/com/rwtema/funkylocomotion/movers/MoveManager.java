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

	@SuppressWarnings("unchecked")
	public static void startMoving(World world, List<BlockPos> list, ForgeDirection dir, int maxTime) {
		if (dir == ForgeDirection.UNKNOWN)
			throw new IllegalArgumentException("Direction cannot be unknown.");

		Set<BlockPos> newBlocks = new HashSet<BlockPos>();
		Set<BlockPos> oldBlocks = new HashSet<BlockPos>();
		oldBlocks.addAll(list);

		for (BlockPos blockPos : list) {
			BlockPos advance = blockPos.advance(dir);
			if (!list.contains(advance)) newBlocks.add(advance);
			oldBlocks.remove(advance);
		}

		Map<BlockPos, Entry> movers = new HashMap<BlockPos, Entry>();

		for (BlockPos pos : newBlocks) {
			BlockHelper.breakBlockWithDrop(world, pos);
		}

		Set<Chunk> chunks = new HashSet<Chunk>();
		HashSet<EntityPlayer> watchingPlayers = new HashSet<EntityPlayer>();
		HashSet inventories = new HashSet();

		for (BlockPos pos : list) {
			BlockPos advance = pos.advance(dir);

			Entry e = new Entry(advance, dir, maxTime);

			e.block = world.getBlock(pos.x, pos.y, pos.z);
			e.meta = world.getBlockMetadata(pos.x, pos.y, pos.z);

			e.lightopacity = e.block.getLightOpacity(world, pos.x, pos.y, pos.z);

			e.lightlevel = e.block.getLightValue(world, pos.x, pos.y, pos.z);

			List<AxisAlignedBB> axes = new ArrayList<AxisAlignedBB>();
			e.block.addCollisionBoxesToList(world, pos.x, pos.y, pos.z, TileEntity.INFINITE_EXTENT_AABB, axes, null);

			if (axes.size() > 0) {
				e.bb = new ArrayList<AxisAlignedBB>();
				for (AxisAlignedBB bb : axes) {
					e.bb.add(AxisAlignedBB.getBoundingBox(
							bb.minX - pos.x,
							bb.minY - pos.y,
							bb.minZ - pos.z,
							bb.maxX - pos.x,
							bb.maxY - pos.y,
							bb.maxZ - pos.z
					));
				}
			}

			NBTTagCompound descriptor = new NBTTagCompound();

			descriptor.setInteger("Block", Block.getIdFromBlock(e.block));
			if (e.meta != 0)
				descriptor.setByte("Meta", (byte) e.meta);

			TileEntity tile = world.getTileEntity(pos.x, pos.y, pos.z);
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

			chunks.add(BlockHelper.getChunk(world, pos));

			movers.put(advance, e);
		}


		for (Chunk c : chunks) {
			List<NextTickListEntry> ticks = world.getPendingBlockUpdates(c, false);
			if (ticks != null) {
				long k = world.getTotalWorldTime();
				for (NextTickListEntry tick : ticks) {
					BlockPos p = (new BlockPos(tick.xCoord, tick.yCoord, tick.zCoord));

					if (BlockHelper.getBlock(c, p) != tick.func_151351_a())
						continue;

					p = p.advance(dir);

					if (!movers.containsKey(p))
						continue;

					Entry e = movers.get(p);

					e.scheduledTickTime = (int) (tick.scheduledTime - k);
					e.scheduledTickPriority = tick.priority;
				}
			}

			PlayerManager.PlayerInstance chunkWatcher = FLNetwork.getChunkWatcher(c, world);
			if (chunkWatcher != null)
				watchingPlayers.addAll(chunkWatcher.playersWatchingChunk);
		}

		// from now on - NO BLOCK UPDATES


		for (BlockPos pos : list) {
			IMoveFactory factory = FactoryRegistry.getFactory(world, pos);
			NBTTagCompound block = factory.destroyBlock(world, pos);
			BlockPos advance = pos.advance(dir);
			movers.get(advance).blockTag = block;
		}

		// let there be updates;

		for (EntityPlayer watchingPlayer : watchingPlayers) {
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

		for (BlockPos pos : list) {
			BlockHelper.silentClear(BlockHelper.getChunk(world, pos), pos);
			FLNetwork.sendToAllWatchingChunk(world, pos.x, pos.y, pos.z, new MessageClearTile(pos));
			world.removeTileEntity(pos.x, pos.y, pos.z);
		}

		for (BlockPos pos : oldBlocks) {
			BlockHelper.postUpdateBlock(world, pos);
		}

		ArrayList<TileMovingServer> tiles = new ArrayList<TileMovingServer>();

		for (Entry e : movers.values()) {
			world.setBlock(e.pos.x, e.pos.y, e.pos.z, BlockMoving.instance, 0, 1);
			TileMovingServer tile = (TileMovingServer) world.getTileEntity(e.pos.x, e.pos.y, e.pos.z);
			tile.block = e.blockTag;
			tile.desc = e.description;
			tile.dir = e.dir;
			tile.maxTime = e.time;

			tile.lightLevel = e.lightlevel;
			tile.lightOpacity = e.lightopacity;

			tile.scheduledTickTime = e.scheduledTickTime;
			tile.scheduledTickPriority = e.scheduledTickPriority;
			if (e.bb != null)
				tile.collisions = e.bb.toArray(new AxisAlignedBB[e.bb.size()]);

			tile.isAir = false;

			tiles.add(tile);
		}

		for (BlockPos pos : list) {
			if (!movers.containsKey(pos)) {
				world.setBlock(pos.x, pos.y, pos.z, BlockMoving.instance, 0, 1);
				TileMovingServer tile = (TileMovingServer) world.getTileEntity(pos.x, pos.y, pos.z);
				tile.block = (NBTTagCompound) airBlockTag.copy();
				tile.desc = (NBTTagCompound) airDescTag.copy();
				tile.dir = dir;
				tile.maxTime = maxTime;

				tile.lightLevel = 0;
				tile.lightOpacity = 0;
				tile.isAir = true;

				tiles.add(tile);
			}
		}


		for (TileMovingServer tile : tiles) {
			PlayerManager.PlayerInstance watcher = FLNetwork.getChunkWatcher(world, tile.xCoord, tile.zCoord);
			watcher.sendToAllPlayersWatchingChunk(new S23PacketBlockChange(tile.xCoord, tile.yCoord, tile.zCoord, world));
		}

		for (TileMovingServer tile : tiles) {
			PlayerManager.PlayerInstance watcher = FLNetwork.getChunkWatcher(world, tile.xCoord, tile.zCoord);
			Packet packet = tile.getDescriptionPacket();
			if (packet != null)
				watcher.sendToAllPlayersWatchingChunk(packet);
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
		final ForgeDirection dir;
		final int time;
		Block block;
		int meta;
		List<AxisAlignedBB> bb = null;
		int lightlevel;
		int lightopacity;

		public Entry(BlockPos pos, ForgeDirection dir, int time) {
			this.pos = pos;
			this.dir = dir;
			this.time = time;
		}
	}

}
