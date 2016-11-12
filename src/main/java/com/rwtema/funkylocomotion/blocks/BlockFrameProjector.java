package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockFrameProjector extends BlockFLMultiState {

	public BlockFrameProjector() {
		super(Material.ROCK);
		this.setRegistryName("funkylocomotion:frame_projector");
		this.setUnlocalizedName("funkylocomotion:frame_projector");
		this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
		this.setHardness(1);
	}

	@Nonnull
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
		return getDefaultState().withProperty(BlockDirectional.FACING, EnumFacing.values()[meta % 6]);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileFrameProjector(state.getValue(BlockDirectional.FACING));
	}

	@Nonnull
	@Override
	public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, ItemStack stack) {
		EnumFacing facingFromEntity = BlockPistonBase.getFacingFromEntity(pos, placer);
		return getDefaultState().withProperty(BlockDirectional.FACING, facingFromEntity);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof TileFrameProjector) {
			((TileFrameProjector) tileEntity).range = 1;
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			ItemStack item = playerIn.getHeldItem(hand);
			if (!(ItemHelper.isWrench(item)))
				return false;

			if (playerIn.isSneaking()) {
				TileEntity tileEntity = worldIn.getTileEntity(pos);
				if (tileEntity instanceof TileFrameProjector) {
					TileFrameProjector projector = (TileFrameProjector) tileEntity;
					projector.range++;
					if (projector.range > TileFrameProjector.MAX_RANGE) {
						projector.range = 1;
					}
					BlockHelper.markBlockForUpdate(worldIn, pos);
				}
			} else {
				IBlockState blockState = worldIn.getBlockState(pos);

				EnumFacing face = blockState.getValue(BlockDirectional.FACING);
				if (side == face)
					side = face.getOpposite();

				worldIn.setBlockState(pos, state.withProperty(BlockDirectional.FACING, side), 3);
				TileEntity tileEntity = worldIn.getTileEntity(pos);
				if (tileEntity instanceof TileFrameProjector) {
					TileFrameProjector projector = (TileFrameProjector) tileEntity;
					projector.facing = side;
					BlockHelper.markBlockForUpdate(worldIn, pos);
				}
			}
		}
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileFrameProjector))
			return;

		TileFrameProjector tileFrameProjector = (TileFrameProjector) tile;

		if (!(world instanceof WorldServer)) {
			return;
		}
		boolean prevPowered = tileFrameProjector.powered;
		tileFrameProjector.powered = world.isBlockIndirectlyGettingPowered(pos) > 0;
		if (prevPowered != tileFrameProjector.powered) {
			BlockHelper.markBlockForUpdate(world, pos);
		}
	}
}
