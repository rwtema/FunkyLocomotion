package com.rwtema.funkylocomotion.helper;

import com.mojang.authlib.GameProfile;
import com.rwtema.funkylocomotion.api.FunkyCapabilities;
import com.rwtema.funkylocomotion.api.IMoveCheck;
import com.rwtema.funkylocomotion.api.ISlipperyBlock;
import com.rwtema.funkylocomotion.movepermissions.MoveCheckReflector;
import com.rwtema.funkylocomotion.proxydelegates.ProxyRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

public class BlockHelper {
	private static final MethodHandle methodHandle_Chunk_relightBlock =
			MethodHandleUtils.getMethodHandleVirtual(Chunk.class, new String[]{"func_76615_h", "relightBlock"}, int.class, int.class, int.class);
	private static final MethodHandle methodHandle_Chunk_propagateSkylightOcclusion =
			MethodHandleUtils.getMethodHandleVirtual(Chunk.class, new String[]{"func_76595_e", "propagateSkylightOcclusion"}, int.class, int.class);
	private static final Field field_Chunk_precipitationHeightMap = ReflectionHelper.findField(Chunk.class, "field_76638_b", "precipitationHeightMap");

	@SuppressWarnings("deprecation")
	public static boolean silentSetBlock(Chunk chunk, BlockPos pos, Block block, int meta) {
		int dx = pos.getX() & 15;
		int dz = pos.getZ() & 15;
		int y = pos.getY();

		int i1 = dz << 4 | dx;

		try {
			int[] precMap = (int[]) field_Chunk_precipitationHeightMap.get(chunk);
			if (y >= precMap[i1] - 1) {
				precMap[i1] = -999;
			}
			//if (y >= chunk.precipitationHeightMap[i1] - 1) {
			//	chunk.precipitationHeightMap[i1] = -999;
			//}
		} catch (Exception e) {
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

				extendedblockstorage = chunk.getBlockStorageArray()[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !chunk.getWorld().provider.isNether());
			}

			extendedblockstorage.set(dx, y & 15, dz, block.getStateFromMeta(meta));
			chunk.setModified(true);
			return true;
		}

	}

	public static void silentClear(Chunk chunk, BlockPos pos) {
		silentSetBlock(chunk, pos, Blocks.AIR, 0);
	}

	public static void postUpdateBlock(World world, BlockPos pos) {
		int i1 = (pos.getZ() & 15) << 4 | (pos.getX() & 15);


		Chunk chunk = world.getChunkFromBlockCoords(pos);

		try {
			int[] precMap = (int[]) field_Chunk_precipitationHeightMap.get(chunk);
			if (pos.getY() >= precMap[i1] - 1) {
				precMap[i1] = -999;
			}
			//if (pos.getY() >= chunk.precipitationHeightMap[i1] - 1) {
			//	chunk.precipitationHeightMap[i1] = -999;
			//}
		} catch (Exception e) {
		}

		int j1 = chunk.getHeightMap()[i1];
		boolean flag = pos.getY() >= j1;
		IBlockState newState = chunk.getBlockState(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		Block newBlock = newState.getBlock();
		int k2 = 255;

		if (flag) {
			chunk.generateSkylightMap();
		} else {
			int j2 = newBlock.getLightOpacity(newState, world, pos);

			try {
				if (j2 > 0) {
					if (pos.getY() >= j1) {
						methodHandle_Chunk_relightBlock.invokeExact(chunk, pos.getX() & 15, pos.getY() + 1, pos.getZ() & 15);
						//chunk.relightBlock(pos.getX() & 15, pos.getY() + 1, pos.getZ() & 15);
					}
				} else if (pos.getY() == j1 - 1) {
					methodHandle_Chunk_relightBlock.invokeExact(chunk, pos.getX() & 15, pos.getY(), pos.getZ() & 15);
					//chunk.relightBlock(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
				}


				if (j2 != k2 && (j2 < k2 || chunk.getLightFor(EnumSkyBlock.SKY, pos) > 0 || chunk.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
					methodHandle_Chunk_propagateSkylightOcclusion.invokeExact(chunk, pos.getX() & 15, pos.getZ() & 15);
					//chunk.propagateSkylightOcclusion(pos.getX() & 15, pos.getZ() & 15);
				}
			} catch (Throwable t) {
			}
		}

		world.checkLight(pos);
		markBlockForUpdate(world, pos);

		if (!world.isRemote) {
			// TODO: Is this correct in 1.11?
			world.neighborChanged(pos, Blocks.AIR, pos);
			world.neighborChanged(pos, newBlock, pos);
			// It used to be this in 1.10:
			//world.notifyBlockOfStateChange(pos, Blocks.AIR);
			//world.notifyBlockOfStateChange(pos, newBlock);
			world.notifyNeighborsOfStateChange(pos, newBlock, false);

			if (newState.hasComparatorInputOverride()) {
				world.updateComparatorOutputLevel(pos, newBlock);
			}
		}
	}

	public static boolean canMoveBlock(World world, BlockPos pos, @Nullable GameProfile profile) {
		IBlockState state = world.getBlockState(pos);
		Block b = state.getBlock();
		if (b == Blocks.AIR || b.isAir(state, world, pos))
			return false;

		IMoveCheck check = ProxyRegistry.getInterface(b, IMoveCheck.class, FunkyCapabilities.MOVE_CHECK);
		if (check != null) {
			EnumActionResult result = check.canMove(world, pos, profile);
			if (result != EnumActionResult.PASS)
				return result == EnumActionResult.SUCCESS;
		}

		if (state.getBlockHardness(world, pos) < 0)
			return false;

		TileEntity tile = world.getTileEntity(pos);

		if (tile != null) {
			check = ProxyRegistry.getInterface(tile, IMoveCheck.class, FunkyCapabilities.MOVE_CHECK);
			if (check != null) {
				EnumActionResult result = check.canMove(world, pos, profile);
				if (result != EnumActionResult.PASS)
					return result == EnumActionResult.SUCCESS;
			} else {
				EnumActionResult result = MoveCheckReflector.canMoveClass(tile.getClass(), world, pos, profile);
				if (result != EnumActionResult.PASS)
					return result == EnumActionResult.SUCCESS;
			}
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

	public static boolean canStick(World world, BlockPos pos, EnumFacing dir, @Nullable GameProfile profile) {
		if (!isValid(world, pos))
			return false;

		if (!canMoveBlock(world, pos, profile))
			return false;

		Block b = world.getBlockState(pos).getBlock();
		ISlipperyBlock slip = ProxyRegistry.getInterface(b, ISlipperyBlock.class, FunkyCapabilities.SLIPPERY_BLOCK);
		return slip == null || slip.canStickTo(world, pos, dir);
	}

	public static boolean canReplace(World world, BlockPos pos) {
		return isValid(world, pos) && (world.isAirBlock(pos) || world.getBlockState(pos).getBlock().isReplaceable(world, pos));
	}

	public static TileEntity getTile(World world, BlockPos pos) {
		return world.getTileEntity(pos);
	}

	public static void markBlockForUpdate(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 0);
	}

	@Nullable
	public static <T extends TileEntity> T getTileEntitySafely(IBlockAccess world, BlockPos pos, Class<T> tileClass) {
		TileEntity te;

		if (world instanceof ChunkCache) {
			ChunkCache chunkCache = (ChunkCache) world;
			te = chunkCache.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
		} else {
			te = world.getTileEntity(pos);
		}

		if (tileClass.isInstance(te)) {
			return tileClass.cast(te);
		} else {
			return null;
		}
	}
}
