package com.rwtema.funkylocomotion.factory;

import com.rwtema.funkylocomotion.helper.BlockHelper;
import framesapi.IMoveFactory;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class DefaultMoveFactory implements IMoveFactory {
	public static NBTTagCompound getBBTag(AxisAlignedBB bb, BlockPos pos) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setDouble("x1", bb.minX - pos.getX());
		tag.setDouble("y1", bb.minY - pos.getY());
		tag.setDouble("z1", bb.minZ - pos.getZ());
		tag.setDouble("x2", bb.maxX - pos.getX());
		tag.setDouble("y2", bb.maxY - pos.getY());
		tag.setDouble("z2", bb.maxZ - pos.getZ());
		return tag;
	}

	@Override
	public NBTTagCompound destroyBlock(World world, BlockPos pos) {
		Chunk chunk = world.getChunkFromBlockCoords(pos);
		IBlockState state = chunk.getBlockState(pos);

		Block b = state.getBlock();
		int meta = b.getMetaFromState(state);

		NBTTagCompound tag = new NBTTagCompound();

		if (b == Blocks.AIR) return tag;

		String name = (Block.REGISTRY.getNameForObject(b)).toString();

		tag.setString("Block", name);
		if (meta != 0)
			tag.setByte("Meta", (byte) meta);

		saveTile(pos, chunk, tag);

		BlockHelper.silentClear(chunk, pos);

		return tag;
	}

	protected NBTTagCompound saveTile(BlockPos pos, Chunk chunk, NBTTagCompound tag) {
		TileEntity tile = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
		if (tile != null) {
			NBTTagCompound tileTag = new NBTTagCompound();
			tile.writeToNBT(tileTag);
			tag.setTag("Tile", tileTag);
			chunk.removeTileEntity(pos);
			return tileTag;
		}
		return null;
	}

	@Override
	public boolean recreateBlock(World world, BlockPos pos, NBTTagCompound tag) {
		Block block = Block.getBlockFromName(tag.getString("Block"));

		if (block == null)
			block = Blocks.AIR;

		int meta = tag.getByte("Meta");

		Chunk chunk = world.getChunkFromBlockCoords(pos);

		BlockHelper.silentSetBlock(chunk, pos, block, meta);

		loadTile(pos, tag, chunk);

		return true;
	}

	protected TileEntity loadTile(BlockPos pos, NBTTagCompound tag, Chunk chunk) {
		if (tag.hasKey("Tile", 10)) {
			NBTTagCompound tileTag = tag.getCompoundTag("Tile");
			tileTag.setInteger("x", pos.getX());
			tileTag.setInteger("y", pos.getY());
			tileTag.setInteger("z", pos.getZ());

			TileEntity tile = TileEntity.create(chunk.getWorld(), tileTag);
			if (tile != null) {
				chunk.addTileEntity(tile);
				return tile;
			}
		}

		return null;
	}


}
