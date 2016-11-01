package com.rwtema.funkylocomotion.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class BlockFLDirectional extends Block {
	public BlockFLDirectional(Material materialIn) {
		super(materialIn);
	}


	@Override
	@Nonnull
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BlockDirectional.FACING, EnumFacing.values()[meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockDirectional.FACING).getIndex();
	}

	@Override
	@Nonnull
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockDirectional.FACING);
	}
}
