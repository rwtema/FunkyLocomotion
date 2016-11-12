package com.rwtema.funkylocomotion.description;

import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class Describer {

	public static void addDescriptionToTags(NBTTagCompound descriptor, TileEntity tile) {
		NBTTagCompound updateTag = tile.getUpdateTag();
		updateTag.removeTag("x");
		updateTag.removeTag("y");
		updateTag.removeTag("z");
		if (!updateTag.hasNoTags()) {
			descriptor.setTag("Tile", updateTag);
		}
	}

	@SideOnly(Side.CLIENT)
	public static TileEntity recreateTileEntity(NBTTagCompound tag, IBlockState state, BlockPos pos, World world) {
		if (!FakeWorldClient.isValid(world)) return null;
		Block block = state.getBlock();
		if (!block.hasTileEntity(state))
			return null;

		FakeWorldClient fakeWorldWrapper = FakeWorldClient.getFakeWorldWrapper(world);
		TileEntity tile = block.createTileEntity(fakeWorldWrapper, state);
		if (tile != null) {
			fakeWorldWrapper.blockstateOverides.put(pos, state);
			fakeWorldWrapper.tileOverides.put(pos, tile);

			tile.setWorldObj(fakeWorldWrapper);
			tile.setPos(pos);
			tile.updateContainingBlockInfo();

			if (tag.hasKey("Tile", 10)) {
				NBTTagCompound tileTag = tag.getCompoundTag("Tile");
				tileTag.setInteger("x", pos.getX());
				tileTag.setInteger("y", pos.getY());
				tileTag.setInteger("z", pos.getZ());
				tile.handleUpdateTag(tileTag);
			}
			fakeWorldWrapper.blockstateOverides.remove(pos);
			fakeWorldWrapper.tileOverides.remove(pos);
		}

		return tile;
	}
}
