package com.rwtema.funkylocomotion.description;

import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import framesapi.IDescriptionProxy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class DescribeBase implements IDescriptionProxy {
	@Override
	@SideOnly(Side.CLIENT)
	public TileEntity recreateTileEntity(NBTTagCompound tag, IBlockState state, BlockPos pos, World world) {
		if (!FakeWorldClient.isValid(world)) return null;
		Block block = state.getBlock();
		if (!block.hasTileEntity(state))
			return null;

		FakeWorldClient fakeWorldWrapper = FakeWorldClient.getFakeWorldWrapper(world);
		TileEntity tile = block.createTileEntity(fakeWorldWrapper, state);
		if (tile != null) {
			tile.setWorldObj(fakeWorldWrapper);
			tile.setPos(pos);
			tile.updateContainingBlockInfo();
//			tile.blockType = block;
//			tile.blockMetadata = meta;
		}
		return tile;
	}
}
