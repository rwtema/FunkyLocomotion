package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.helper.WeakSet;
import com.rwtema.funkylocomotion.movers.MoveManager;
import com.rwtema.funkylocomotion.particles.ObstructionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.BlockPos;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

public class TileTeleport extends TilePusher {
	int teleportId;

	static TLongObjectHashMap<WeakSet<TileTeleport>> cache = new TLongObjectHashMap<WeakSet<TileTeleport>>();

	@Override
	public List<BlockPos> getBlocks(World world, BlockPos home, ForgeDirection dir, boolean push) {
		BlockPos advance = home.advance(dir);
		if (BlockHelper.canStick(world, advance, dir.getOpposite()))
			return getBlocks(world, home, advance, ForgeDirection.UNKNOWN);

		return null;
	}

	@Override
	public List<BlockPos> checkPositions(World srcWorld, ForgeDirection moveDir, ArrayList<BlockPos> posList, HashSet<BlockPos> posSet) {
		TileTeleport tile = getTileTeleport();
		if (tile == null) return null;

		World dstWorld = tile.worldObj;

		boolean fail = false;
		for (BlockPos pos : posList) {

			BlockPos adv = getDestinationPos(tile, pos);

			if ((dstWorld == srcWorld && posSet.contains(adv)) || !BlockHelper.canReplace(dstWorld, adv)) {
				if (!ObstructionHelper.sendObstructionPacket(srcWorld, pos, moveDir))
					return null;
				fail = true;
			}
		}

		return fail ? null : posList;
	}

	private TileTeleport getTileTeleport() {
		if (teleportId == 0) return null;

		WeakSet<TileTeleport> tileTeleports = cache.get(teleportId);

		if (tileTeleports == null) return null; // should never happen

		for (Iterator<TileTeleport> iterator = tileTeleports.iterator(); iterator.hasNext(); ) {
			TileTeleport tile = iterator.next();

			if(tile.isInvalid())
				iterator.remove();

			if (tile == this || !tile.hasWorldObj())
				continue;

			World world = tile.getWorldObj();

			if (world == null || world.isRemote)
				iterator.remove();

			if(DimensionManager.getWorld(world.provider.dimensionId) != world){
				iterator.remove();
			}

			if (!world.blockExists(tile.xCoord, tile.yCoord, tile.zCoord))
				continue;

			return tile;
		}
		return null;
	}

	private BlockPos getDestinationPos(TileTeleport tile, BlockPos pos) {

		BlockPos srcPos = new BlockPos(this).advance(getBlockMetadata() ^ 1);
		BlockPos dstPos = new BlockPos(tile).advance(tile.getBlockMetadata() ^ 1);

		return new BlockPos(
				pos.x - srcPos.x + dstPos.x,
				pos.y - srcPos.y + dstPos.y,
				pos.z - srcPos.z + dstPos.z
		);
	}

	public void startMoving() {

		TileTeleport tileTeleport = getTileTeleport();
		if (tileTeleport == null) return;

		int meta = getBlockMetadata();
		ForgeDirection dir = ForgeDirection.getOrientation(meta % 6).getOpposite();
		boolean push = meta < 6;
		if (dir == ForgeDirection.UNKNOWN)
			return;

		BlockPos pos = new BlockPos(xCoord, yCoord, zCoord);
		List<BlockPos> posList = getBlocks(worldObj, pos, dir, push);
		if (posList != null) {
			final int energy = posList.size() * powerPerTile;
			if (this.energy.extractEnergy(energy, true) != energy)
				return;

			if(tileTeleport.energy.extractEnergy(energy, true) != energy)
				return;

			ArrayList<TileBooster> boosters = new ArrayList<TileBooster>(6);
			for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
				if (d != dir) {
					BlockPos p = pos.advance(d);
					if (BlockHelper.getBlock(worldObj, p) == FunkyLocomotion.booster) {
						if (ForgeDirection.getOrientation(BlockHelper.getMeta(worldObj, p) % 6) != d)
							continue;

						TileEntity tile = BlockHelper.getTile(worldObj, p);
						if (tile instanceof TileBooster) {
							TileBooster booster = (TileBooster) tile;
							if (booster.energy.extractEnergy(energy, true) != energy)
								return;

							boosters.add(booster);
						}
					}
				}
			}

			if (!boosters.isEmpty()) {
				for (TileBooster booster : boosters) {
					booster.energy.extractEnergy(energy, false);
				}
			}

			this.energy.extractEnergy(energy, false);
			tileTeleport.energy.extractEnergy(energy, false);

			ArrayList<MoveManager.BlockLink> links = new ArrayList<MoveManager.BlockLink>(posList.size());
			for (BlockPos blockPos : posList) {
				links.add(new MoveManager.BlockLink(blockPos, getDestinationPos(tileTeleport, blockPos)));
			}

			MoveManager.startMoving(worldObj, tileTeleport.worldObj, links, ForgeDirection.UNKNOWN, moveTime[boosters.size()] * 2);
		}
	}


	@Override
	public void invalidate() {
		super.invalidate();
		unCache();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		unCache();
	}

	private void unCache() {
		if(teleportId != 0 && (worldObj == null || !worldObj.isRemote)) {
			WeakSet<TileTeleport> tileTeleports = cache.get(teleportId);
			if (tileTeleports != null) {
				tileTeleports.remove(this);
			}
		}
	}

	@Override
	public void validate() {
		super.validate();
		if(teleportId != 0 && (worldObj == null || !worldObj.isRemote)) {

			WeakSet<TileTeleport> tileTeleports = cache.get(teleportId);
			if (tileTeleports == null) {
				tileTeleports = new WeakSet<TileTeleport>();
				cache.put(teleportId, tileTeleports);
			}

			tileTeleports.add(this);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		teleportId = tag.getInteger("ID");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("ID", teleportId);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		teleportId = pkt.func_148857_g().getInteger("ID");
		if (worldObj.blockExists(xCoord, yCoord, zCoord)) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("ID", teleportId);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}
}
