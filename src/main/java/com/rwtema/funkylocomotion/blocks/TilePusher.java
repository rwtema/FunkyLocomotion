package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.energy.EnergyStorageSerializable;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.movers.IMover;
import com.rwtema.funkylocomotion.movers.MoveManager;
import com.rwtema.funkylocomotion.particles.ObstructionHelper;
import com.rwtema.funkylocomotion.proxydelegates.ProxyRegistry;
import framesapi.IStickyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class TilePusher extends TileEntity implements IMover {
	public static final int[] moveTime = new int[]{
			20,
			10,
			7,
			5,
			4,
			3
	};
	public static int maxTiles = 256;
	public static int powerPerTile = 1000;
	public final EnergyStorageSerializable energy = new EnergyStorageSerializable(maxTiles * powerPerTile);
	public boolean powered;

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);

		energy.readFromNBT(tag);
		powered = tag.getBoolean("Powered");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		energy.writeToNBT(tag);
		tag.setBoolean("powered", powered);
		return tag;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	public List<BlockPos> getBlocks(World world, BlockPos home, EnumFacing dir, boolean push) {
		BlockPos advance = home.offset(dir);
		if (push) {
			if (BlockHelper.canStick(world, advance, dir.getOpposite()))
				return getBlocks(world, home, advance, dir);
		} else {
			if (!world.isAirBlock(advance))
				return null;

			BlockPos advance2 = advance.offset(dir);
			if (BlockHelper.canStick(world, advance2, dir.getOpposite()))
				return getBlocks(world, home, advance2, dir.getOpposite());
		}

		return null;
	}

	public List<BlockPos> getBlocks(World world, BlockPos home, BlockPos start, EnumFacing moveDir) {

		ArrayList<BlockPos> posList = new ArrayList<>();
		HashSet<BlockPos> posSet = new HashSet<>();
		getBlockPosIterate(world, home, start, posList, posSet);

		return checkPositions(world, moveDir, posList, posSet);
	}

	public List<BlockPos> checkPositions(World world, EnumFacing moveDir, ArrayList<BlockPos> posList, HashSet<BlockPos> posSet) {
		boolean fail = false;
		for (BlockPos pos : posList) {
			BlockPos adv = pos.offset(moveDir);
			if (!posSet.contains(adv) && !BlockHelper.canReplace(world, adv)) {
				if (!ObstructionHelper.sendObstructionPacket(world, pos, moveDir))
					return null;
				fail = true;
			}
		}

		return fail ? null : posList;
	}

	private void getBlockPosIterate(World world, BlockPos home, BlockPos start, ArrayList<BlockPos> posList, HashSet<BlockPos> posSet) {
		LinkedList<BlockPos> toIterate = new LinkedList<>();
		HashSet<BlockPos> toIterateSet = new HashSet<>();

		toIterate.add(start);
		toIterateSet.add(start);


//		for (int i = 0; i < toIterate.size(); i++) {
		while (!toIterate.isEmpty()) {
			BlockPos pos = toIterate.poll();

			posList.add(pos);
			posSet.add(pos);

			Block b = world.getBlockState(pos).getBlock();

			IStickyBlock stickyBlock = ProxyRegistry.getInterface(b, IStickyBlock.class);

			if (stickyBlock != null) {
				for (EnumFacing side : EnumFacing.values()) {
					if (stickyBlock.isStickySide(world, pos, side)) {
						BlockPos newPos = pos.offset(side);

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
	}

	public void startMoving() {

		int meta = getBlockMetadata();
		EnumFacing dir1 = EnumFacing.values()[meta % 6].getOpposite();
		boolean push1 = meta < 6;
		EnumFacing d2 = push1 ? dir1 : dir1.getOpposite();

		EnumFacing dir = d2.getOpposite();
		boolean push = meta < 6;

		List<BlockPos> posList = getBlocks(worldObj, pos, dir, push);
		if (posList != null) {
			final int energy = posList.size() * powerPerTile;
			if (this.energy.extractEnergy(energy, true) != energy)
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

			MoveManager.startMoving(worldObj, posList, getDirection(), moveTime[boosters.size()]);
		}
	}

	@Override
	public boolean stillExists() {
		return !tileEntityInvalid && worldObj != null && worldObj.isBlockLoaded(pos) && worldObj.getTileEntity(pos) == this;
	}

	@Override
	public void onChunkUnload() {
		tileEntityInvalid = true;
	}

	public EnumFacing getDirection() {
		int meta = getBlockMetadata();
		EnumFacing dir = EnumFacing.values()[meta % 6].getOpposite();
		boolean push = meta < 6;
		return push ? dir.getOpposite() : dir;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(energy);
		}
		return super.getCapability(capability, facing);
	}
}
