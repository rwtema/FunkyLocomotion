package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBooster extends Block {
	public BlockBooster() {
		super(Material.ROCK);
		this.setRegistryName("funkylocomotion:booster");
		this.setUnlocalizedName("funkylocomotion:booster");
		this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
		this.setHardness(1);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			ItemStack item = playerIn.getHeldItem(hand);
			if (!(ItemHelper.isWrench(item)))
				return false;

			IBlockState blockState = worldIn.getBlockState(pos);

			EnumFacing face = blockState.getValue(BlockDirectional.FACING);
			if (side == face)
				side = face.getOpposite();

			worldIn.setBlockState(pos, state.withProperty(BlockDirectional.FACING, side), 3);
		}
		return true;
	}


	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileBooster();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockDirectional.FACING);
	}


	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockDirectional.FACING).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = getDefaultState();
		state = state.withProperty(BlockDirectional.FACING, EnumFacing.values()[meta % 6]);
		return state;
	}
}
