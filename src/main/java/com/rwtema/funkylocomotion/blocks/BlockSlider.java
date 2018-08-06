package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockSlider extends BlockPusher {
	public static final PropertyInteger SUB_ROTATION = PropertyInteger.create("sub_rot", 0, 3);

	private int[][] map;

	public BlockSlider() {
		super();
		init();
		this.setUnlocalizedName("funkylocomotion:slider");
		this.setRegistryName("funkylocomotion:slider");
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
	}

	private void init() {
		map = new int[][]{
				{9, 9, 2, 0, 3, 1},
				{9, 9, 0, 2, 3, 1},
				{0, 2, 9, 9, 3, 1},
				{0, 2, 9, 9, 1, 3},
				{0, 2, 1, 3, 9, 9},
				{0, 2, 3, 1, 9, 9},
		};
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		if (state.getBlock() != this) return state;
		EnumFacing facing = state.getValue(BlockDirectional.FACING);

		TileSlider tile = BlockHelper.getTileEntitySafely(worldIn, pos, TileSlider.class);
		if (tile != null) {
			EnumFacing slideDir = tile.getSlideDir();
			int value = map[facing.ordinal()][slideDir.ordinal()];

			if (value != 9)
				state = state.withProperty(SUB_ROTATION, value);
		}

		return state;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
									EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return false;
		}
		ItemStack item = playerIn.getHeldItem(hand);
		if (!(ItemHelper.isWrench(item)))
			return false;

		final int meta = getMetaFromState(state);
		TileEntity tile = worldIn.getTileEntity(pos);
		if (playerIn.isSneaking()) {
			if (tile != null && tile.getClass() == TileSlider.class) {
				((TileSlider) tile).rotateAboutAxis();
				BlockHelper.markBlockForUpdate(worldIn, pos);
				return true;
			}
		} else {
			if (side.ordinal() == meta)
				side = side.getOpposite();

			worldIn.setBlockState(pos, state.withProperty(BlockDirectional.FACING, side), 3);

			if (tile != null && tile.getClass() == TileSlider.class) {
				((TileSlider) tile).getSlideDir();
				BlockHelper.markBlockForUpdate(worldIn, pos);
			}
			return true;
		}
		return false;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileSlider();
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockDirectional.FACING, SUB_ROTATION);
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
