package com.rwtema.funkylocomotion.helper;

import com.rwtema.funkylocomotion.movepermissions.MoveCheckReflector;
import com.rwtema.funkylocomotion.api.IMoveCheck;
import com.rwtema.funkylocomotion.api.ISlipperyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class BlockHelper {
	public static boolean silentSetBlock(Chunk chunk, BlockPos pos, Block block, int meta) {
		int dx = pos.getX() & 15;
		int dz = pos.getZ() & 15;
		int y = pos.getY();

		int i1 = dz << 4 | dx;

		if (y >= chunk.precipitationHeightMap[i1] - 1) {
			chunk.precipitationHeightMap[i1] = -999;
		}

		IBlockState state1 = chunk.getBlockState(dx, y, dz);
		Block block1 = state1.getBlock();
		int k1 = block1.getMetaFromState(state1);

		if (block1 == block && k1 == meta) {
			return false;
		} else {
			ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[y >> 4];

			if (extendedblockstorage == Chunk.NULL_BLOCK_STORAGE) {
				if (block == Blocks.AIR) {
					return false;
				}

				extendedblockstorage = chunk.getBlockStorageArray()[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !chunk.worldObj.provider.getHasNoSky());
			}

			extendedblockstorage.set(dx, y & 15, dz, block.getStateFromMeta(meta));
			chunk.isModified = true;
			return true;
		}

	}

	public static void silentClear(Chunk chunk, BlockPos pos) {
		silentSetBlock(chunk, pos, Blocks.AIR, 0);
	}

	public static void postUpdateBlock(World world, BlockPos pos) {
		int i1 = (pos.getZ() & 15) << 4 | (pos.getX() & 15);


		Chunk chunk = world.getChunkFromBlockCoords(pos);

		if (pos.getY() >= chunk.precipitationHeightMap[i1] - 1) {
			chunk.precipitationHeightMap[i1] = -999;
		}

		int j1 = chunk.heightMap[i1];
		boolean flag = pos.getY() >= j1;
		IBlockState newState = chunk.getBlockState(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		Block newBlock = newState.getBlock();
		int k2 = 255;

		if (flag) {
			chunk.generateSkylightMap();
		} else {
			int j2 = newBlock.getLightOpacity(newState, world, pos);

			if (j2 > 0) {
				if (pos.getY() >= j1) {
					chunk.relightBlock(pos.getX() & 15, pos.getY() + 1, pos.getZ() & 15);
				}
			} else if (pos.getY() == j1 - 1) {

				chunk.relightBlock(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
			}


			if (j2 != k2 && (j2 < k2 || chunk.getLightFor(EnumSkyBlock.SKY, pos) > 0 || chunk.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
				chunk.propagateSkylightOcclusion(pos.getX() & 15, pos.getZ() & 15);
			}
		}

		world.checkLight(pos);
		markBlockForUpdate(world, pos);

		world.notifyBlockOfStateChange(pos, Blocks.AIR);
		world.notifyBlockOfStateChange(pos, newBlock);
		world.notifyNeighborsOfStateChange(pos, newBlock);

		if (newState.hasComparatorInputOverride()) {
			world.updateComparatorOutputLevel(pos, newBlock);
		}
	}

	public static boolean canMoveBlock(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		Block b = state.getBlock();
		if (b == Blocks.AIR || b.isAir(state, world, pos))
			return false;

		if (b instanceof IMoveCheck)
			return ((IMoveCheck) b).canMove(state, world, pos);

		if (b.getBlockHardness(state, world, pos) < 0)
			return false;

		TileEntity tile = world.getTileEntity(pos);

		if (tile != null) {
			if (tile instanceof IMoveCheck)
				return ((IMoveCheck) tile).canMove(state, world, pos);
			else if (!MoveCheckReflector.canMoveClass(tile.getClass()))
				return false;
		}

		return MoveCheckReflector.canMoveClass(b.getClass());
	}

	public static void breakBlockWithDrop(World world, BlockPos pos) {
		IBlockState iblockstate = world.getBlockState(pos);
		if (iblockstate.getMaterial().isLiquid()) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
		} else {
			world.destroyBlock(pos, true);
		}
	}

	public static boolean isValid(World world, BlockPos pos) {
		return world.isBlockLoaded(pos);
	}

	public static boolean canStick(World world, BlockPos pos, EnumFacing dir) {
		if (!isValid(world, pos))
			return false;

		if (!canMoveBlock(world, pos))
			return false;

		Block b = world.getBlockState(pos).getBlock();
		return !(b instanceof ISlipperyBlock) || ((ISlipperyBlock) b).canStickTo(world, pos, dir);
	}

	public static boolean canReplace(World world, BlockPos pos) {
		return isValid(world, pos) && (world.isAirBlock(pos) || world.getBlockState(pos).getBlock().isReplaceable(world, pos));
	}

	public static TileEntity getTile(World world, BlockPos pos) {
		return world.getTileEntity(pos);
	}

	public static int getMeta(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getBlock().getMetaFromState(state);
	}

	public static void markBlockForUpdate(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 0);
	}
}
