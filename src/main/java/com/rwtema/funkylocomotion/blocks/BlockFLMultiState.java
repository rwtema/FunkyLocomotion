package com.rwtema.funkylocomotion.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public abstract class BlockFLMultiState extends Block {
	public BlockFLMultiState(Material blockMaterialIn, MapColor blockMapColorIn) {
		super(blockMaterialIn, blockMapColorIn);
	}

	public BlockFLMultiState(Material materialIn) {
		super(materialIn);
	}

	@Override
	protected abstract BlockStateContainer createBlockState();

	@Override
	public abstract int getMetaFromState(IBlockState state);

	@SuppressWarnings("NullableProblems")
	@Override
	public abstract IBlockState getStateFromMeta(int meta);
}
