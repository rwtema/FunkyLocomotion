package com.rwtema.funkylocomotion.movers;

import com.rwtema.funkylocomotion.blocks.BlockMoving;
import com.rwtema.funkylocomotion.blocks.TileMoving;
import com.rwtema.funkylocomotion.description.DescriptorRegistry;
import com.rwtema.funkylocomotion.factory.FactoryRegistry;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import framesapi.BlockPos;
import framesapi.IDescriptionProxy;
import framesapi.IMoveFactory;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public class MoveManager {
    public static final NBTTagCompound airBlockTag;
    public static final NBTTagCompound airDescTag;

    static {
        airBlockTag = new NBTTagCompound();
        airDescTag = new NBTTagCompound();
        if (Block.getIdFromBlock(Blocks.air) != 0)
            airDescTag.setInteger("Block", Block.getIdFromBlock(Blocks.air));
    }

    public static void startMoving(World world, List<BlockPos> list, ForgeDirection dir) {
        if (dir == ForgeDirection.UNKNOWN)
            throw new IllegalArgumentException("Direction cannot be unknown.");

        final int time = 20;

        ArrayList<BlockPos> dests = new ArrayList<BlockPos>(list.size());
        Set<BlockPos> newBlocks = new HashSet<BlockPos>();
        Set<BlockPos> oldBlocks = new HashSet<BlockPos>();
        oldBlocks.addAll(list);

        for (BlockPos blockPos : list) {
            BlockPos advance = blockPos.advance(dir);
            dests.add(advance);
            if (!list.contains(advance)) newBlocks.add(advance);
            oldBlocks.remove(advance);
        }

        Map<BlockPos, Entry> movers = new HashMap<BlockPos, Entry>();

        for (BlockPos pos : newBlocks) {
            BlockHelper.breakBlockWithDrop(world, pos);
        }

        Set<Chunk> chunks = new HashSet<Chunk>();

        for (BlockPos pos : list) {
            BlockPos advance = pos.advance(dir);

            Entry e = new Entry(advance, dir, time);

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
        }


        //from now on - NO BLOCK UPDATES


        for (BlockPos pos : list) {
            IMoveFactory factory = FactoryRegistry.getFactory(world, pos);
            NBTTagCompound block = factory.destroyBlock(world, pos);
            BlockPos advance = pos.advance(dir);
            movers.get(advance).blockTag = block;
        }

        // let there be updates;

        for (BlockPos pos : oldBlocks) {
            BlockHelper.postUpdateBlock(world, pos);
        }

        for (Entry e : movers.values()) {
            BlockHelper.silentClear(BlockHelper.getChunk(world, e.pos), e.pos);
            world.setBlock(e.pos.x, e.pos.y, e.pos.z, BlockMoving.instance, 0, 3);
            TileMoving tile = (TileMoving) world.getTileEntity(e.pos.x, e.pos.y, e.pos.z);
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
        }
        for (BlockPos pos : list) {
            if (!movers.containsKey(pos)) {
                world.removeTileEntity(pos.x, pos.y, pos.z);
                world.setBlock(pos.x, pos.y, pos.z, BlockMoving.instance, 0, 3);
                TileMoving tile = (TileMoving) world.getTileEntity(pos.x, pos.y, pos.z);
                tile.block = (NBTTagCompound) airBlockTag.copy();
                tile.desc = (NBTTagCompound) airDescTag.copy();
                tile.dir = dir;
                tile.maxTime = time;

                tile.lightLevel = 0;
                tile.lightOpacity = 0;
            }
        }
    }


    public static void finishMoving() {
        List<TileMoving> tiles = MovingTileRegistry.getTilesFinishedMoving();

        // Clear Tiles
        for (TileMoving tile : tiles) {
            tile.getWorldObj().setBlock(tile.xCoord, tile.yCoord, tile.zCoord, Blocks.air, 0, 0);
            tile.getWorldObj().setBlock(tile.xCoord, tile.yCoord, tile.zCoord, Blocks.stone, 0, 0);
        }

        // Set Block/Tile
        for (TileMoving tile : tiles) {
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
        for (TileMoving tile : tiles) {
            BlockPos pos = new BlockPos(tile);
            BlockHelper.postUpdateBlock(tile.getWorldObj(), pos);
            if (tile.scheduledTickTime != -1)
                tile.getWorldObj().scheduleBlockUpdateWithPriority(
                        tile.xCoord, tile.yCoord, tile.zCoord,
                        BlockHelper.getBlock(tile.getWorldObj(), pos),
                        tile.scheduledTickTime - tile.maxTime, tile.scheduledTickPriority);

        }

        // Redocached Activation
        for (TileMoving tile : tiles) {
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
        NBTTagCompound blockTag;
        NBTTagCompound description;
        BlockPos pos;
        ForgeDirection dir;
        int time;
        Block block;
        int meta;

        List<AxisAlignedBB> bb = null;
        int lightlevel;
        int lightopacity;
        public int scheduledTickTime = -1;
        public int scheduledTickPriority;

        public Entry(BlockPos pos, ForgeDirection dir, int time) {
            this.pos = pos;
            this.dir = dir;
            this.time = time;
        }
    }

}
