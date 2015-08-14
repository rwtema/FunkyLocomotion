package com.rwtema.funkylocomotion.blocks;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.movers.IMover;
import com.rwtema.funkylocomotion.movers.MoveManager;
import com.rwtema.funkylocomotion.particles.ObstructionHelper;
import com.rwtema.funkylocomotion.proxydelegates.ProxyRegistry;
import framesapi.BlockPos;
import framesapi.IStickyBlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TilePusher extends TileEntity implements IEnergyHandler, IMover {
	public static int maxTiles = 256;
	public static int powerPerTile = 1000;
	public final EnergyStorage energy = new EnergyStorage(maxTiles * powerPerTile);
	public boolean powered;


	public static final int[] moveTime = new int[]{
			20,
			10,
			7,
			5,
			4,
			3
	};

	@Override
	public boolean canUpdate() {
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		energy.readFromNBT(tag);
		powered = tag.getBoolean("Powered");
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		energy.writeToNBT(tag);
		tag.setBoolean("powered", powered);
	}

	@Override
	public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
		return oldBlock != newBlock;
	}

	public List<BlockPos> getBlocks(World world, BlockPos home, ForgeDirection dir, boolean push) {
		BlockPos advance = home.advance(dir);
		if (push) {
			if (BlockHelper.canStick(world, advance, dir.getOpposite()))
				return getBlocks(world, home, advance, dir);
		} else {
			if (!world.isAirBlock(advance.x, advance.y, advance.z))
				return null;

			advance = advance.advance(dir);
			if (BlockHelper.canStick(world, advance, dir.getOpposite()))
				return getBlocks(world, home, advance, dir.getOpposite());
		}

		return null;
	}

	public List<BlockPos> getBlocks(World world, BlockPos home, BlockPos start, ForgeDirection moveDir) {

		ArrayList<BlockPos> posList = new ArrayList<BlockPos>();
		HashSet<BlockPos> posSet = new HashSet<BlockPos>();
		ArrayList<BlockPos> toIterate = new ArrayList<BlockPos>();
		HashSet<BlockPos> toIterateSet = new HashSet<BlockPos>();

		toIterate.add(start);
		toIterateSet.add(start);


		for (int i = 0; i < toIterate.size(); i++) {
			BlockPos pos = toIterate.get(i);

			posList.add(pos);
			posSet.add(pos);

			Block b = BlockHelper.getBlock(world, pos);

			IStickyBlock stickyBlock = ProxyRegistry.getInterface(b, IStickyBlock.class);

			if (stickyBlock != null) {
				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
					if (stickyBlock.isStickySide(world, pos.x, pos.y, pos.z, side)) {
						BlockPos newPos = pos.advance(side);

						if (home.equals(newPos))
							continue;

						if (toIterateSet.contains(newPos))
							continue;

						if (BlockHelper.canStick(world, newPos, side.getOpposite())) {
							toIterate.add(newPos);
							toIterateSet.add(newPos);
						}
					}
				}
			}
		}

		boolean fail = false;
		for (BlockPos pos : posList) {
			BlockPos adv = pos.advance(moveDir);
			if (!posSet.contains(adv) && !BlockHelper.canReplace(world, adv)) {
				if (!ObstructionHelper.sendObstructionPacket(world, pos, moveDir))
					return null;
				fail = true;
			}
		}

		return fail ? null : posList;
	}

	public void startMoving() {
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

			MoveManager.startMoving(worldObj, posList, getDirection(), moveTime[boosters.size()]);
		}
	}

	@Override
	public boolean stillExists() {
		return !tileEntityInvalid && worldObj != null && worldObj.blockExists(xCoord, yCoord, zCoord) && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void onChunkUnload() {
		tileEntityInvalid = true;
	}

	public ForgeDirection getDirection() {
		int meta = getBlockMetadata();
		ForgeDirection dir = ForgeDirection.getOrientation(meta % 6).getOpposite();
		boolean push = meta < 6;
		return push ? dir : dir.getOpposite();
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		return energy.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return energy.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return energy.getMaxEnergyStored();
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}
}
