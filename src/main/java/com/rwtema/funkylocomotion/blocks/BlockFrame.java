package com.rwtema.funkylocomotion.blocks;

import javax.annotation.Nonnull;
import com.rwtema.funkylocomotion.api.IStickyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFrame extends Block implements IStickyBlock {
	public BlockFrame() {
		super(Material.ROCK);
		this.setUnlocalizedName("funkylocomotion:frame");
		this.setHardness(1);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, EnumFacing side) {
		return super.shouldSideBeRendered(blockState, blockAccess, pos, side) &&
				!(blockAccess.getBlockState(pos.offset(side)).getBlock() instanceof BlockFrame);
	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return true;
	}

	@Override
	public boolean causesSuffocation(IBlockState state) {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public boolean isStickySide(World world, BlockPos pos, EnumFacing side) {
		return true;
	}
}
