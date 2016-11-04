package com.rwtema.funkylocomotion.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class ChunkUnloadFactory extends DefaultMoveFactory {
	@Override
	protected NBTTagCompound saveTile(BlockPos pos, Chunk chunk, NBTTagCompound tag) {
		TileEntity tile = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
		if (tile != null) {
			NBTTagCompound tileTag = new NBTTagCompound();
			tile.writeToNBT(tileTag);
			tag.setTag("Tile", tileTag);
			chunk.removeTileEntity(pos);
			tile.onChunkUnload();
			return tileTag;
		}
		return null;
	}
}
