package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.helper.WeakSet;
import com.rwtema.funkylocomotion.movers.MoveManager;
import com.rwtema.funkylocomotion.particles.ObstructionHelper;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class TileTeleport extends TilePusher {
	static TLongObjectHashMap<WeakSet<TileTeleport>> cache = new TLongObjectHashMap<>();
	int teleportId;

	@Override
	public List<BlockPos> getBlocks(World world, BlockPos home, EnumFacing dir, boolean push) {
		BlockPos advance = home.offset(dir);
		if (BlockHelper.canStick(world, advance, dir.getOpposite(), profile))
			return getBlocks(world, home, advance, null);

		return null;
	}

	@Override
	public List<BlockPos> checkPositions(World srcWorld, EnumFacing moveDir, ArrayList<BlockPos> posList, HashSet<BlockPos> posSet) {
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

			if (tile.isInvalid())
				iterator.remove();

			if (tile == this || !tile.hasWorldObj())
				continue;

			World world = tile.getWorld();

			if (world == null || world.isRemote)
				iterator.remove();

			if (DimensionManager.getWorld(world.provider.getDimension()) != world) {
				iterator.remove();
			}

			if (!world.isBlockLoaded(tile.getPos()))
				continue;

			return tile;
		}
		return null;
	}

	private BlockPos getDestinationPos(TileTeleport tile, BlockPos pos) {

		BlockPos srcPos = this.pos.offset(EnumFacing.values()[getBlockMetadata()]);
		BlockPos dstPos = tile.pos.offset(EnumFacing.values()[tile.getBlockMetadata()]);

		return new BlockPos(
				pos.getX() - srcPos.getX() + dstPos.getX(),
				pos.getY() - srcPos.getY() + dstPos.getY(),
				pos.getZ() - srcPos.getZ() + dstPos.getZ()
		);
	}

	public void startMoving() {
		cooldown = -1;
		TileTeleport tileTeleport = getTileTeleport();
		if (tileTeleport == null) return;

		int meta = getBlockMetadata();
		EnumFacing dir = EnumFacing.values()[meta % 6];
		boolean push = meta < 6;
		if (dir == null)
			return;

		List<BlockPos> posList = getBlocks(worldObj, pos, dir, push);
		if (posList != null) {
			final int energy = posList.size() * powerPerTile;
			if (this.energy.extractEnergy(energy, true) != energy)
				return;

			if (tileTeleport.energy.extractEnergy(energy, true) != energy)
				return;

			ArrayList<TileBooster> boosters = new ArrayList<>(6);
			for (EnumFacing d : EnumFacing.values()) {
				if (d != dir) {
					BlockPos p = pos.offset(d);
					IBlockState state = worldObj.getBlockState(p);
					if (state.getBlock() == FunkyLocomotion.booster) {
						if (state.getValue(BlockDirectional.FACING) != d.getOpposite())
							continue;

						TileEntity tile = BlockHelper.getTile(worldObj, p);
						if (tile instanceof TileBooster) {
							TileBooster booster = (TileBooster) tile;
							if (booster.energy.extractEnergy(energy, true) != energy)
								continue;

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

			ArrayList<MoveManager.BlockLink> links = new ArrayList<>(posList.size());
			for (BlockPos blockPos : posList) {
				links.add(new MoveManager.BlockLink(blockPos, getDestinationPos(tileTeleport, blockPos)));
			}

			MoveManager.startMoving(worldObj, tileTeleport.worldObj, links, null, moveTime[boosters.size()] * 2);
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
		if (teleportId != 0 && (worldObj == null || !worldObj.isRemote)) {
			WeakSet<TileTeleport> tileTeleports = cache.get(teleportId);
			if (tileTeleports != null) {
				tileTeleports.remove(this);
			}
		}
	}

	@Override
	public void validate() {
		super.validate();
		if (teleportId != 0 && (worldObj == null || !worldObj.isRemote)) {

			WeakSet<TileTeleport> tileTeleports = cache.get(teleportId);
			if (tileTeleports == null) {
				tileTeleports = new WeakSet<>();
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
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("ID", teleportId);
		return tag;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("ID", teleportId);
		return tag;
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		teleportId = tag.getInteger("ID");
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

}
