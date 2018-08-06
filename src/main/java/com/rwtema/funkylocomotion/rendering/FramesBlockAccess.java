package com.rwtema.funkylocomotion.rendering;

import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import com.rwtema.funkylocomotion.helper.BlockStates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class FramesBlockAccess implements IBlockAccess {
	public final IBlockAccess world;

	public FramesBlockAccess(World world) {
		this.world = world;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Biome getBiome(@Nonnull BlockPos pos) {
		return world.getBiome(pos);
	}

	@Override
	public int getStrongPower(@Nonnull BlockPos pos, @Nonnull EnumFacing direction) {
		return world.getStrongPower(pos, direction);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public WorldType getWorldType() {
		return world.getWorldType();
	}

	public TileMovingClient getTile(BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return (tile != null && tile.getClass() == TileMovingClient.class) ? (TileMovingClient) tile : null;
	}

	@Override
	public TileEntity getTileEntity(@Nonnull BlockPos pos) {
		TileMovingClient tile = getTile(pos);
		return tile == null ? null : tile.tile;
	}

	@Override
	public int getCombinedLight(@Nonnull BlockPos pos, int lightValue) {
		return world.getCombinedLight(pos, lightValue);
	}

	@Nonnull
	@Override
	public IBlockState getBlockState(@Nonnull BlockPos pos) {
		TileMovingClient tile = getTile(pos);
		return tile == null ? BlockStates.AIR : tile.getState();
	}

	@Override
	public boolean isAirBlock(@Nonnull BlockPos pos) {
		TileMovingClient tile = getTile(pos);
		return tile == null || tile.block == Blocks.AIR;
	}

	@Override
	public boolean isSideSolid(@Nonnull BlockPos pos, @Nonnull EnumFacing side, boolean _default) {
		TileMovingClient tile = getTile(pos);
		return tile != null && tile.getState().isSideSolid(this, pos, side);
	}
}
