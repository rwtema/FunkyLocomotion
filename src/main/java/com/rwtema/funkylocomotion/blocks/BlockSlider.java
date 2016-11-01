package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockSlider extends BlockPusher {
	public static final PropertyInteger SUB_ROTATION = PropertyInteger.create("sub_rot", 0, 3);


	int[][] map;

	public BlockSlider() {
		super();
		init();
		this.setUnlocalizedName("funkylocomotion:slider");
		this.setRegistryName("funkylocomotion:slider");
	}

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		list.add(new ItemStack(itemIn, 1, 0));
	}

	public void init() {
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
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		if (state.getBlock() != this) return state;
		EnumFacing facing = state.getValue(BlockDirectional.FACING);

		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileSlider) {
			EnumFacing slideDir = ((TileSlider) tile).getSlideDir();
			init();
			int value = map[facing.ordinal()][slideDir.ordinal()];

			if (value != 9)
				state = state.withProperty(SUB_ROTATION, value);
		}

		return state;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		}
		ItemStack item = playerIn.getHeldItem(hand);
		if (!(ItemHelper.isWrench(item)))
			return false;

		final int meta = getMetaFromState(state);
		TileEntity tile = worldIn.getTileEntity(pos);
		if (playerIn.isSneaking()) {
			if (tile != null && tile.getClass() == TileSlider.class) {
				((TileSlider) tile).rotateAboutAxis();
				IBlockState actualState = getActualState(state, worldIn, pos);
				EnumFacing slideDir = ((TileSlider) tile).getSlideDir();
				playerIn.addChatComponentMessage(new TextComponentString(
						slideDir.toString() + " " + actualState.getValue(BlockDirectional.FACING) + " " + actualState.getValue(SUB_ROTATION)
				));
				BlockHelper.markBlockForUpdate(worldIn, pos);
			}
		} else {
			if (side.ordinal() == meta)
				side = side.getOpposite();

			worldIn.setBlockState(pos, state.withProperty(BlockDirectional.FACING, side), 3);

			if (tile != null && tile.getClass() == TileSlider.class) {
				((TileSlider) tile).getSlideDir();
				IBlockState actualState = getActualState(state, worldIn, pos);
				EnumFacing slideDir = ((TileSlider) tile).getSlideDir();
				playerIn.addChatComponentMessage(new TextComponentString(
						slideDir.toString() + " " + actualState.getValue(BlockDirectional.FACING) + " " + actualState.getValue(SUB_ROTATION)
				));
				BlockHelper.markBlockForUpdate(worldIn, pos);
			}
		}
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileSlider();
	}

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
