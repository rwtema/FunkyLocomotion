package com.rwtema.funkylocomotion.blocks;

import com.mojang.authlib.GameProfile;
import com.rwtema.funkylocomotion.api.FunkyCapabilities;
import com.rwtema.funkylocomotion.api.IAdvStickyBlock;
import com.rwtema.funkylocomotion.api.IStickyBlock;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.movers.IMover;
import com.rwtema.funkylocomotion.movers.MoveManager;
import com.rwtema.funkylocomotion.movers.MoverEventHandler;
import com.rwtema.funkylocomotion.particles.ObstructionHelper;
import com.rwtema.funkylocomotion.proxydelegates.ProxyRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TilePusher extends TilePowered implements IMover, ITickable {
	public static final int[] moveTime = new int[]{
			20,
			10,
			7,
			5,
			4,
			3
	};
	public static final int COOLDOWN_TIMER = 2;
	public static int maxTiles = 1024;
	public static int powerPerTile = 250;
	public boolean powered;
	@Nullable
	protected GameProfile profile;
	int cooldown = -1;

	public TilePusher() {
		super(maxTiles * powerPerTile);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		cooldown = tag.getInteger("cooldown");
		powered = tag.getBoolean("Powered");

		String name = tag.getString("Name");
		UUID uuid;
		if (tag.hasKey("UUIDL")) {
			uuid = new UUID(tag.getLong("UUIDU"), tag.getLong("UUIDL"));
			profile = new GameProfile(uuid, name);
		} else {
			if (StringUtils.isBlank(name))
				profile = null;
			else
				profile = new GameProfile(null, name);
		}

	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("cooldown", cooldown);
		tag.setBoolean("powered", powered);

		NBTTagCompound profileTag = new NBTTagCompound();
		if (profile != null) {
			profileTag.setString("Name", profile.getName());
			UUID id = profile.getId();
			if (id != null) {
				profileTag.setLong("UUIDL", id.getLeastSignificantBits());
				profileTag.setLong("UUIDU", id.getMostSignificantBits());
			}
			tag.setTag("profile", profileTag);
		}
		return tag;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	public List<BlockPos> getBlocks(World world, BlockPos home, EnumFacing dir, boolean push) {
		if (push) {
			BlockPos advance = home.offset(dir);
			if (BlockHelper.canStick(world, advance, dir.getOpposite(), profile))
				return getBlocks(world, home, advance, dir);
		} else {
			dir = dir.getOpposite();
			BlockPos advance = home.offset(dir);
			if (!world.isAirBlock(advance))
				return null;

			BlockPos advance2 = advance.offset(dir);
			if (BlockHelper.canStick(world, advance2, dir.getOpposite(), profile))
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

		while (!toIterate.isEmpty()) {
			BlockPos pos = toIterate.poll();

			posList.add(pos);
			posSet.add(pos);

			IBlockState state = world.getBlockState(pos);
			Block b = state.getBlock();
			TileEntity tile = world.getTileEntity(pos);

			IAdvStickyBlock advStickyBlock = ProxyRegistry.getInterface(b, IAdvStickyBlock.class, FunkyCapabilities.ADV_STICKY_BLOCK);
			if (tile != null && advStickyBlock == null)
				advStickyBlock = ProxyRegistry.getInterface(tile, IAdvStickyBlock.class, FunkyCapabilities.ADV_STICKY_BLOCK);

			if (advStickyBlock != null) {
				Iterable<BlockPos> blocksToMove = advStickyBlock.getBlocksToMove(world, pos);
				for (BlockPos blockPos : blocksToMove) {
					if (home.equals(blockPos)) continue;
					if (toIterateSet.contains(blockPos)) continue;
					if (!BlockHelper.isValid(world, blockPos) || !BlockHelper.canMoveBlock(world, blockPos, profile))
						continue;
					BlockPos immutableBlockPos = blockPos.toImmutable();
					toIterate.add(immutableBlockPos);
					toIterateSet.add(immutableBlockPos);
				}
			} else {
				IStickyBlock stickyBlock = ProxyRegistry.getInterface(b, IStickyBlock.class, FunkyCapabilities.STICKY_BLOCK);
				if (tile != null && stickyBlock == null)
					stickyBlock = ProxyRegistry.getInterface(tile, IStickyBlock.class, FunkyCapabilities.STICKY_BLOCK);

				if (stickyBlock != null) {
					for (EnumFacing side : EnumFacing.values()) {
						if (stickyBlock.isStickySide(world, pos, side)) {
							BlockPos newPos = pos.offset(side);

							if (home.equals(newPos))
								continue;

							if (toIterateSet.contains(newPos))
								continue;

							if (BlockHelper.canStick(world, newPos, side.getOpposite(), profile)) {
								toIterate.add(newPos);
								toIterateSet.add(newPos);
							}
						}
					}
				}
			}
		}
	}

	public void startMoving() {
		cooldown = -1;
		int meta = getBlockMetadata();
		EnumFacing dir1 = EnumFacing.values()[meta % 6].getOpposite();
		boolean push1 = meta < 6;
		EnumFacing d2 = push1 ? dir1 : dir1.getOpposite();

		EnumFacing dir = d2.getOpposite();
		boolean push = meta < 6;

		List<BlockPos> posList = getBlocks(getWorld(), pos, dir, push);
		if (posList != null) {
			final int energy = posList.size() * powerPerTile;
			if (this.energy.extractEnergy(energy, true) != energy)
				return;

			ArrayList<TileBooster> boosters = new ArrayList<>(6);
			for (EnumFacing d : EnumFacing.values()) {
				if (d != dir) {
					BlockPos p = pos.offset(d);
					IBlockState state = getWorld().getBlockState(p);
					if (state.getBlock() == FLBlocks.BOOSTER) {
						if (state.getValue(BlockDirectional.FACING) != d.getOpposite())
							continue;

						TileEntity tile = BlockHelper.getTile(getWorld(), p);
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

			MoveManager.startMoving(getWorld(), posList, getDirection(), moveTime[boosters.size()]);
		}
	}

	@Override
	public boolean stillExists() {
		return !tileEntityInvalid && getWorld() != null && getWorld().isBlockLoaded(pos) && getWorld().getTileEntity(pos) == this;
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
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(energy);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void update() {
		if (!this.getWorld().isRemote) {
			if (cooldown > 0) {
				cooldown--;
			}
			if (cooldown == 0) {
				MoverEventHandler.registerMover(this);
			}
		}
	}

	public void startCooldown() {
		if (cooldown == -1) {
			cooldown = COOLDOWN_TIMER;
		}
	}
}
