package com.rwtema.funkylocomotion.description;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DescribeVanilla extends DescribeBase {
	@Override
	public String getID() {
		return "";
	}

	@Override
	public boolean canHandleTile(TileEntity tile) {
		return true;
	}

	@Override
	public void addDescriptionToTags(NBTTagCompound descriptor, TileEntity tile) {
		NBTTagCompound updateTag = tile.getUpdateTag();
		updateTag.removeTag("x");
		updateTag.removeTag("y");
		updateTag.removeTag("z");
		descriptor.setTag("Tile", updateTag);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TileEntity recreateTileEntity(NBTTagCompound tag, IBlockState state, BlockPos pos, World world) {
		TileEntity tile = super.recreateTileEntity(tag, state, pos, world);
		if (tile != null && tag.hasKey("Tile", 10)) {
			NBTTagCompound tileTag = tag.getCompoundTag("Tile");
			tileTag.setInteger("x", pos.getX());
			tileTag.setInteger("y", pos.getY());
			tileTag.setInteger("z", pos.getZ());
			tile.handleUpdateTag(tileTag);
		}
		return tile;
	}
}
