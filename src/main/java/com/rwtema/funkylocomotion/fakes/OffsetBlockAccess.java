package com.rwtema.funkylocomotion.fakes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class OffsetBlockAccess implements IBlockAccess {
	public final IBlockAccess access;
	public BlockPos offset = BlockPos.ORIGIN;

	public OffsetBlockAccess(IBlockAccess world) {
		access = world;
	}

	@Override
	@Nullable
	public TileEntity getTileEntity(BlockPos pos) {
		return access.getTileEntity(pos.add(offset));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return access.getCombinedLight(pos.add(offset), lightValue);
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return access.getBlockState(pos.add(offset));
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return access.isAirBlock(pos.add(offset));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Biome getBiome(BlockPos pos) {
		return access.getBiome(pos.add(offset));
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return access.getStrongPower(pos.add(offset), direction);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public WorldType getWorldType() {
		return access.getWorldType();
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return access.isSideSolid(pos.add(offset), side, _default);
	}

	public OffsetBlockAccess setOffset(EnumFacing dir) {
		return setOffset(dir.getFrontOffsetX(), dir.getFrontOffsetY(), dir.getFrontOffsetZ());
	}

	public OffsetBlockAccess setOffset(int x, int y, int z) {
		offset = new BlockPos(x, y, z);
		return this;
	}


}
