package com.rwtema.funkylocomotion.movers;

import com.rwtema.funkylocomotion.api.IMoveFactory;
import com.rwtema.funkylocomotion.blocks.BlockMoving;
import com.rwtema.funkylocomotion.blocks.TileMovingServer;
import com.rwtema.funkylocomotion.description.Describer;
import com.rwtema.funkylocomotion.factory.FactoryRegistry;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.helper.BlockStates;
import com.rwtema.funkylocomotion.network.FLNetwork;
import com.rwtema.funkylocomotion.network.MessageClearTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.*;

public class MoveManager {
	public static final NBTTagCompound airBlockTag;
	public static final NBTTagCompound airDescTag;
	final static LinkedHashMap<String, Object> vars = new LinkedHashMap<>(10, 0.2F);
	private static final Object BLANK = new Object();

	static {
		airBlockTag = new NBTTagCompound();
		airDescTag = new NBTTagCompound();
		if (Block.getIdFromBlock(Blocks.AIR) != 0)
			airDescTag.setInteger("Block", Block.getIdFromBlock(Blocks.AIR));
	}

	public static void startMoving(World world, List<BlockPos> posList, EnumFacing dir, int maxTime) {
		ArrayList<BlockLink> links = new ArrayList<>(posList.size());
		for (BlockPos blockPos : posList) {
			links.add(new BlockLink(blockPos, blockPos.offset(dir)));
		}

		startMoving(world, world, links, dir, maxTime);
	}

	@SuppressWarnings("unchecked")
	public synchronized static void startMoving(World srcWorld, World dstWorld, List<BlockLink> links, @Nullable EnumFacing dir, int maxTime) {
		String section = "Start";

		clearVars();

		try {
			vars.put("srcWorld", srcWorld);
			vars.put("dstWorld", dstWorld);
			vars.put("links", links);
			vars.put("dir", dir);
			vars.put("maxTime", maxTime);

			if (dir != null && srcWorld != dstWorld) {
				throw new IllegalArgumentException("Trying to regular shift between worlds");
			}

			HashSet<BlockPos> srcBlocks = new HashSet<>();
			HashSet<BlockPos> dstBlocks = new HashSet<>();

			vars.put("srcBlocks", srcBlocks);
			vars.put("dstBlocks", dstBlocks);

			for (BlockLink link : links) {
				srcBlocks.add(link.srcPos);
				dstBlocks.add(link.dstPos);
			}

			HashSet<BlockPos> dstBlocksToBeDestroyed = new HashSet<>();
			HashSet<BlockPos> srcBlocksToBecomeAir = new HashSet<>();

			vars.put("dstBlocksToBeDestroyed", dstBlocksToBeDestroyed);
			vars.put("srcBlocksToBecomeAir", srcBlocksToBecomeAir);

			srcBlocksToBecomeAir.addAll(srcBlocks);
			dstBlocksToBeDestroyed.addAll(dstBlocks);

			if (srcWorld == dstWorld) {
				srcBlocksToBecomeAir.removeAll(dstBlocks);
				dstBlocksToBeDestroyed.removeAll(srcBlocks);
			}

			Map<BlockPos, Entry> dstTileEntries = new HashMap<>();

			vars.put("dstTileEntries", dstTileEntries);

			section = "BreakBlockWithDrop";

			for (BlockPos pos : dstBlocksToBeDestroyed) {
				BlockHelper.breakBlockWithDrop(dstWorld, pos);
			}

			Set<Chunk> srcChunks = new HashSet<>();
//			HashSet<EntityPlayer> srcWatchingPlayers = new HashSet<>();
			HashSet inventories = new HashSet();

			vars.put("srcChunks", srcChunks);
//			vars.put("srcWatchingPlayers", srcWatchingPlayers);
			vars.put("inventories", inventories);

			section = "Read Data";

			for (BlockLink link : links) {
				vars.put("Iterator", link);
				BlockPos dstPos = link.dstPos;
				BlockPos srcPos = link.srcPos;

				Entry e = new Entry(dstPos);

				IBlockState state = srcWorld.getBlockState(srcPos);
				e.block = state.getBlock();
				e.meta = e.block.getMetaFromState(state);

				e.lightopacity = state.getLightOpacity(srcWorld, srcPos);

				e.lightlevel = state.getLightValue(srcWorld, srcPos);

				List<AxisAlignedBB> axes = new ArrayList<>();
				state.addCollisionBoxToList(srcWorld, srcPos, TileEntity.INFINITE_EXTENT_AABB, axes, null);

				if (axes.size() > 0) {
					e.bb = new ArrayList<>();
					for (AxisAlignedBB bb : axes) {
						e.bb.add(new AxisAlignedBB(
								bb.minX - srcPos.getX(),
								bb.minY - srcPos.getY(),
								bb.minZ - srcPos.getZ(),
								bb.maxX - srcPos.getX(),
								bb.maxY - srcPos.getY(),
								bb.maxZ - srcPos.getZ()
						));
					}
				}

				NBTTagCompound descriptor = new NBTTagCompound();

				descriptor.setInteger("Block", Block.getIdFromBlock(e.block));
				if (e.meta != 0)
					descriptor.setByte("Meta", (byte) e.meta);

				TileEntity tile = srcWorld.getTileEntity(srcPos);
				if (tile != null) {
					Describer.addDescriptionToTags(descriptor, tile);

					if (tile instanceof IInventory) {
						inventories.add(tile);
					}
				}

				e.description = descriptor;

				srcChunks.add(srcWorld.getChunkFromBlockCoords(srcPos));

				dstTileEntries.put(dstPos, e);
			}
			vars.put("Iterator", BLANK);

			section = "LoadTicks";
			for (Chunk chunk : srcChunks) {
				vars.put("Iterator", chunk);
				List<NextTickListEntry> ticks = srcWorld.getPendingBlockUpdates(chunk, false);
				if (ticks != null) {
					long k = srcWorld.getTotalWorldTime();
					for (NextTickListEntry tick : ticks) {
						vars.put("Iterator2", tick);
						BlockPos pos = tick.position;

						if (chunk.getBlockState(pos).getBlock() != tick.getBlock())
							continue;

						if (dir != null) pos = pos.offset(dir);

						if (!dstTileEntries.containsKey(pos))
							continue;

						Entry e = dstTileEntries.get(pos);

						e.scheduledTickTime = (int) (tick.scheduledTime - k);
						e.scheduledTickPriority = tick.priority;
					}
				}

//				PlayerChunkMapEntry chunkWatcher = FLNetwork.getChunkWatcher(chunk, srcWorld);
//				if (chunkWatcher != null) {
//					srcWatchingPlayers.addAll(chunkWatcher.addPlayer(););
//				}
			}
			vars.put("Iterator", BLANK);
			vars.put("Iterator2", BLANK);

			// from now on - NO BLOCK UPDATES
			section = "destroyBlock";
			for (BlockLink link : links) {
				vars.put("Iterator", link);
				IMoveFactory factory = FactoryRegistry.getFactory(srcWorld, link.srcPos);
				dstTileEntries.get(link.dstPos).blockTag = factory.destroyBlock(srcWorld, link.srcPos);
			}
			vars.put("Iterator", BLANK);

			// let there be updates;
//			section = "closeInventories";
//			for (EntityPlayer watchingPlayer : srcWatchingPlayers) {
//				vars.put("Iterator", watchingPlayer);
//				if (watchingPlayer.openContainer != watchingPlayer.inventoryContainer && watchingPlayer.openContainer != null) {
//					for (Object o : watchingPlayer.openContainer.inventorySlots) {
//						Slot s = (Slot) o;
//						if (inventories.contains(s.inventory)) {
//							watchingPlayer.closeScreen();
//							break;
//						}
//					}
//				}
//			}
			vars.put("Iterator", BLANK);

			section = "clearTilesSilent";
			for (BlockLink link : links) {
				vars.put("Iterator", link);
				BlockPos dstPos = link.dstPos;
				BlockPos srcPos = link.srcPos;
				BlockHelper.silentClear(dstWorld.getChunkFromBlockCoords(dstPos), dstPos);
//				if (dir != null)
				FLNetwork.sendToAllWatchingChunk(srcWorld, srcPos, new MessageClearTile(srcPos));
				dstWorld.removeTileEntity(dstPos);
			}
			vars.put("Iterator", BLANK);

			section = "postUpdateBlock";
			for (BlockPos pos : srcBlocksToBecomeAir) {
				vars.put("Iterator", pos);
				BlockHelper.postUpdateBlock(dstWorld, pos);
			}
			vars.put("Iterator", BLANK);

			ArrayList<TileMovingServer> tiles = new ArrayList<>();

			vars.put("tiles", tiles);

			section = "createTiles";
			for (Entry e : dstTileEntries.values()) {
				vars.put("Iterator", e);
				dstWorld.setBlockState(e.pos, BlockMoving.instance.getDefaultState(), 1);
				TileMovingServer tile = (TileMovingServer) Validate.notNull(dstWorld.getTileEntity(e.pos));
				vars.put("Iterator2", tile);
				tile.block = e.blockTag;
				tile.desc = e.description;
				tile.dir = dir == null ? 6 : dir.ordinal();
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
			vars.put("Iterator", BLANK);
			vars.put("Iterator2", BLANK);

			section = "createBlankTiles";
			for (BlockLink link : links) {
				vars.put("Iterator", BLANK);
				BlockPos srcPos = link.srcPos;
				if (srcWorld != dstWorld || !dstTileEntries.containsKey(srcPos)) {
					srcWorld.setBlockState(srcPos, BlockMoving.instance.getDefaultState(), 1);
					TileMovingServer tile = (TileMovingServer) Validate.notNull(srcWorld.getTileEntity(srcPos));
					vars.put("Iterator2", tile);

					if (dir != null) {
						tile.block = airBlockTag.copy();
						tile.desc = airDescTag.copy();
						tile.dir = dir.ordinal();
						tile.maxTime = maxTime;

						tile.lightLevel = 0;
						tile.lightOpacity = 0;
						tile.isAir = true;
					} else {
						FLNetwork.sendToAllWatchingChunk(srcWorld, srcPos, new MessageClearTile(srcPos));
						Entry e = dstTileEntries.get(link.dstPos);
						tile.block = airBlockTag.copy();
						if (e.description != null)
							tile.desc = e.description.copy();
						else
							tile.desc = airDescTag.copy();
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
			vars.put("Iterator", BLANK);


			section = "networkUpdateBlocks";
			for (TileMovingServer tile : tiles) {
				vars.put("Iterator", tile);
				PlayerChunkMapEntry watcher = FLNetwork.getChunkWatcher(tile.getWorld(), tile.getPos());
				if (watcher != null) {
					SPacketBlockChange pkt = new SPacketBlockChange(tile.getWorld(), tile.getPos());
					watcher.sendPacket(pkt);
				}
			}
			vars.put("Iterator", BLANK);

			section = "networkUpdateTile";
			for (TileMovingServer tile : tiles) {
				vars.put("Iterator", tile);
				PlayerChunkMapEntry watcher = FLNetwork.getChunkWatcher(tile.getWorld(), tile.getPos());
				if (watcher != null) {
					Packet packet = tile.getUpdatePacket();
					vars.put("Iterator2", packet);
					if (packet != null)
						watcher.sendPacket(packet);
				}
			}
			vars.put("Iterator", BLANK);
			vars.put("Iterator2", BLANK);

			section = "DoneIfWeCrashNowThenWhatTheHell";

			clearVars();
		} catch (Throwable err) {
			try {
				CrashReport crashReport = buildCrashReport(section, err);
				throw new ReportedException(crashReport);
			} finally {
				clearVars();
			}
		}
	}

	private static CrashReport buildCrashReport(String section, Throwable err) {
		CrashReport crashReport = CrashReport.makeCrashReport(err, "FunkyLocomotionMoveCrash");
		CrashReportCategory moveCode = crashReport.makeCategory("MoveCode");

		moveCode.addCrashSection("Section", "\"" + section + "\"");

		for (Map.Entry<String, Object> e : vars.entrySet()) {
			Object value = e.getValue();
			if (value != BLANK)
				moveCode.addCrashSection("var_" + e.getKey(), makeString(value, 0));
		}
		return crashReport;
	}

	private static void clearVars() {
		for (Map.Entry<String, Object> entry : vars.entrySet()) {
			entry.setValue(BLANK);
		}
	}


	public static void finishMoving() {
		clearVars();
		String section = "start";

		try {
			List<TileMovingServer> tiles = MovingTileRegistry.getTilesFinishedMoving();
			if (tiles.isEmpty()) return;

			HashSet<Chunk> chunks = new HashSet<>();

			vars.put("tiles", tiles);
			vars.put("chunks", tiles);

			// Clear Tiles
			section = "Clear Tiles";
			for (TileMovingServer tile : tiles) {
				vars.put("tile", tile);
				World worldObj = tile.getWorld();
				BlockPos pos = tile.getPos();
				chunks.add(worldObj.getChunkFromBlockCoords(pos));
				worldObj.setBlockState(pos, BlockStates.AIR, 0);
				worldObj.setBlockState(pos, BlockStates.STONE, 0);
			}
			vars.put("tile", BLANK);

			// Set Block/Tile
			section = "Set Block/Tile";
			for (TileMovingServer tile : tiles) {
				BlockPos pos = tile.getPos();
				vars.put("tile", tile);
				if (tile.block != null) {
					BlockHelper.silentClear(tile.getWorld().getChunkFromBlockCoords(pos), pos);
					Block block = Block.getBlockFromName(tile.block.getString("Block"));
					vars.put("block", block);
					if (block == null) block = Blocks.AIR;
					IMoveFactory factory = FactoryRegistry.getFactory(block);
					vars.put("factory", factory);
					factory.recreateBlock(tile.getWorld(), pos, tile.block);
				}
			}
			vars.put("tile", BLANK);
			vars.put("block", BLANK);
			vars.put("factory", BLANK);

			// Update Blocks
			section = "Update Blocks";
			for (TileMovingServer tile : tiles) {
				vars.put("tile", tile);
				BlockPos pos = tile.getPos();
				BlockHelper.postUpdateBlock(tile.getWorld(), pos);
				if (tile.scheduledTickTime != -1) {
					int time = tile.scheduledTickTime - 1;
					if (time <= 0) {
						World world = tile.getWorld();
						IBlockState state = world.getBlockState(pos);
						state.getBlock().updateTick(tile.getWorld(), tile.getPos(), state, world.rand);
					} else {
						tile.getWorld().scheduleBlockUpdate(
								tile.getPos(),
								tile.getWorld().getBlockState(pos).getBlock(),
								time, tile.scheduledTickPriority);
					}

				}

			}
			vars.put("tile", BLANK);

			// Send Update Packets
			section = "Send Update Packets";
			for (Chunk chunk : chunks) {
				vars.put("chunk", chunk);
				chunk.isModified = true;
				FLNetwork.updateChunk(chunk);
			}
			vars.put("chunk", BLANK);

			// Redocached Activation
			section = "Redo Activation";
			for (TileMovingServer tile : tiles) {
				vars.put("tile", tile);
				if (tile.activatingPlayer != null) {
					EntityPlayer player = tile.activatingPlayer.get();
					if (player != null) {
						IBlockState state = tile.getWorld().getBlockState(tile.getPos());
						Block b = state.getBlock();
						b.onBlockActivated(tile.getWorld(),
								tile.getPos(),
								state,
								player,
								tile.activatingHand,
								player.getHeldItem(tile.activatingHand),
								tile.activatingSide,
								tile.activatingHitX, tile.activatingHitY, tile.activatingHitZ);
					}
				}
			}
			vars.put("tile", BLANK);

			section = "Fin";
			clearVars();
		} catch (Throwable err) {
			try {
				CrashReport crashReport = buildCrashReport(section, err);
				throw new ReportedException(crashReport);
			} finally {
				clearVars();
			}
		}
	}


	private static String makeString(Object o, int n) {
		if (o == null) return "null";
		if (o instanceof String) return (String) o;

		StringBuilder builder = new StringBuilder();
		tabs(n, builder);
		builder.append(o.getClass().getSimpleName());
		builder.append("{");
		if (o instanceof Block) {
			String nameForObject = "" + Block.REGISTRY.getNameForObject((Block) o);
			builder.append(nameForObject);
			builder.append(",");
			builder.append(o.toString());
		} else if (o instanceof TileEntity) {
			try {
				builder.append("tag=");
				NBTTagCompound tag = new NBTTagCompound();
				((TileEntity) o).writeToNBT(tag);
				builder.append(tag.toString());
			} catch (Exception err) {
				builder.append("TE WriteToNBT Crash\n");
				builder.append(err.toString());
			}
		} else if (o instanceof Collection) {
			int i = 0;
			Iterator iterator = ((Iterable) o).iterator();
			builder.append('\n');
			tabs(n, builder);
			while (iterator.hasNext()) {
				builder.append("\t\t").append(i).append("=");
				i++;
				builder.append(makeString(iterator.next(), n + 1));
				builder.append("\n");
				tabs(n, builder);
			}
		} else {
			builder.append(o.toString());
		}
		builder.append("}");
		return builder.toString();
	}

	private static void tabs(int n, StringBuilder builder) {
		for (int j = 0; j < n; j++) {
			builder.append('\t');
		}
	}


	private static class Entry {
		final BlockPos pos;
		public int scheduledTickTime = -1;
		public int scheduledTickPriority;
		NBTTagCompound blockTag;
		NBTTagCompound description;
		Block block;
		int meta;
		List<AxisAlignedBB> bb = null;
		int lightlevel;
		int lightopacity;

		public Entry(BlockPos pos) {
			this.pos = pos;
		}

		@Override
		public String toString() {
			return "Entry{" +
					"scheduledTickTime=" + scheduledTickTime +
					", scheduledTickPriority=" + scheduledTickPriority +
					", blockTag=" + blockTag +
					", description=" + description +
					", pos=" + pos +
					", block=" + makeString(block, 0) +
					", meta=" + meta +
					", bb=" + makeString(bb, 0) +
					", lightlevel=" + lightlevel +
					", lightopacity=" + lightopacity +
					'}';
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

			return dstPos.equals(blockLink.dstPos) && srcPos.equals(blockLink.srcPos);

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
