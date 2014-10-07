package com.rwtema.funkylocomotion.helper;

import com.rwtema.funkylocomotion.movepermissions.MoveCheckReflector;
import framesapi.BlockPos;
import framesapi.IMoveCheck;
import framesapi.ISlipperyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockHelper {
    public static boolean silentSetBlock(Chunk chunk, BlockPos pos, Block block, int meta) {
        int dx = pos.x & 15;
        int dz = pos.z & 15;
        int y = pos.y;

        int i1 = dz << 4 | dx;

        if (y >= chunk.precipitationHeightMap[i1] - 1) {
            chunk.precipitationHeightMap[i1] = -999;
        }

        Block block1 = chunk.getBlock(dx, y, dz);
        int k1 = chunk.getBlockMetadata(dx, y, dz);

        if (block1 == block && k1 == meta) {
            return false;
        } else {
            ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[y >> 4];

            if (extendedblockstorage == null) {
                if (block == Blocks.air) {
                    return false;
                }

                extendedblockstorage = chunk.getBlockStorageArray()[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !chunk.worldObj.provider.hasNoSky);
            }

            extendedblockstorage.func_150818_a(dx, y & 15, dz, block);
            extendedblockstorage.setExtBlockMetadata(dx, y & 15, dz, meta);

            chunk.isModified = true;
            return true;
        }

    }

    public static void silentClear(Chunk chunk, BlockPos pos) {
        silentSetBlock(chunk, pos, Blocks.air, 0);
    }

    public static void postUpdateBlock(World world, BlockPos pos) {
        int i1 = (pos.z & 15) << 4 | (pos.x & 15);


        Chunk chunk = getChunk(world, pos);

        if (pos.y >= chunk.precipitationHeightMap[i1] - 1) {
            chunk.precipitationHeightMap[i1] = -999;
        }

        int j1 = chunk.heightMap[i1];
        boolean flag = pos.y >= j1;
        Block newBlock = chunk.getBlock(pos.x & 15, pos.y, pos.z & 15);
        int k2 =  255;


        if (flag) {
            chunk.generateSkylightMap();
        } else {
            int j2 = newBlock.getLightOpacity(world, pos.x, pos.y, pos.z);

            if (j2 > 0) {
                if (pos.y >= j1) {
                    chunk.relightBlock(pos.x & 15, pos.y + 1, pos.z & 15);
                }
            } else if (pos.y == j1 - 1) {

                chunk.relightBlock(pos.x & 15, pos.y, pos.z & 15);
            }

            if (j2 != k2 && (j2 < k2 || chunk.getSavedLightValue(EnumSkyBlock.Sky, pos.x & 15, pos.y, pos.z & 15) > 0 || chunk.getSavedLightValue(EnumSkyBlock.Block, pos.x & 15, pos.y, pos.z & 15) > 0)) {
                chunk.propagateSkylightOcclusion(pos.x & 15, pos.z & 15);
            }
        }

        world.func_147451_t(pos.x, pos.y, pos.z);
        world.markBlockForUpdate(pos.x, pos.y, pos.z);

        world.notifyBlockChange(pos.x, pos.y, pos.z, Blocks.air);

        world.notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, Blocks.air);

        if (newBlock.hasComparatorInputOverride()) {
            world.func_147453_f(pos.x, pos.y, pos.z, newBlock);
        }
    }

    public static Chunk getChunk(World world, BlockPos pos) {
        return world.getChunkFromBlockCoords(pos.x, pos.z);
    }

    public static Block getBlock(World world, BlockPos pos) {
        return world.getBlock(pos.x, pos.y, pos.z);
    }

    public static Block getBlock(Chunk chunk, BlockPos pos) {
        return chunk.getBlock(pos.x & 15, pos.y, pos.z & 15);
    }

    public static boolean canMoveBlock(World world, BlockPos pos) {
        Block b = getBlock(world, pos);
        if (b == Blocks.air || b.isAir(world, pos.x, pos.y, pos.z))
            return false;

        if (b instanceof IMoveCheck)
            return ((IMoveCheck) b).canMove(world, pos.x, pos.y, pos.z);

        if (b.getBlockHardness(world, pos.x, pos.y, pos.z) < 0)
            return false;

        TileEntity tile = world.getTileEntity(pos.x, pos.y, pos.z);

        if (tile != null) {
            if (tile instanceof IMoveCheck)
                return ((IMoveCheck) tile).canMove(world, pos.x, pos.y, pos.z);
            else if (!MoveCheckReflector.canMoveClass(tile.getClass()))
                return false;
        }

        return MoveCheckReflector.canMoveClass(b.getClass());
    }

    public static void breakBlockWithDrop(World world, BlockPos pos) {
        Block block = getBlock(world, pos);

        if (block.getMaterial() != Material.air) {
            int l = world.getBlockMetadata(pos.x, pos.y, pos.z);
            world.playAuxSFX(2001, pos.x, pos.y, pos.z, Block.getIdFromBlock(block) + (l << 12));
            block.dropBlockAsItem(world, pos.x, pos.y, pos.z, l, 0);
        }

        world.setBlock(pos.x, pos.y, pos.z, Blocks.air, 0, 2);
    }

    public static boolean isValid(World world, BlockPos pos) {
        return world.blockExists(pos.x, pos.y, pos.z);
    }

    public static boolean canStick(World world, BlockPos pos, ForgeDirection dir) {
        if (!isValid(world, pos))
            return false;

        if (!canMoveBlock(world, pos))
            return false;

        Block b = getBlock(world, pos);
        return !(b instanceof ISlipperyBlock) || ((ISlipperyBlock) b).canStickTo(world, pos, dir);
    }

    public static boolean canReplace(World world, BlockPos pos) {
        return isValid(world, pos) && (world.isAirBlock(pos.x, pos.y, pos.z) || getBlock(world, pos).isReplaceable(world, pos.x, pos.y, pos.z));

    }
}
