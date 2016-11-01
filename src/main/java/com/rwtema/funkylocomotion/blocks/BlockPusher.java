package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import com.rwtema.funkylocomotion.movers.MoverEventHandler;
import framesapi.ISlipperyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;

public class BlockPusher extends BlockFLMultiState implements ISlipperyBlock {
	public static final PropertyEnum<PushPullType> PUSH_PULL_TYPE = PropertyEnum.create("push_pull", PushPullType.class);

	public BlockPusher() {
		super(Material.ROCK);
		this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
		this.setHardness(1);
	}

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		list.add(new ItemStack(itemIn, 1, 0));
		list.add(new ItemStack(itemIn, 1, 1));
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
	public boolean canStickTo(World world, BlockPos pos, EnumFacing dir) {
		return dir != null && world.getBlockState(pos).getValue(BlockDirectional.FACING) != dir;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TilePusher))
			return;

		TilePusher tilePush = (TilePusher) tile;

		if (!(world instanceof WorldServer)) {
			return;
		}

		tilePush.powered = ((World) world).isBlockIndirectlyGettingPowered(pos) > 0;

		if (tilePush.powered)
			MoverEventHandler.registerMover(tilePush);
	}

	@Override
	public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TilePusher();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockDirectional.FACING, PUSH_PULL_TYPE);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockDirectional.FACING).ordinal() + state.getValue(PUSH_PULL_TYPE).metaMask;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = getDefaultState();
		state = state.withProperty(BlockDirectional.FACING, EnumFacing.values()[meta % 6]);
		state = state.withProperty(PUSH_PULL_TYPE, meta < 6 ? PushPullType.PUSHER : PushPullType.PULLER);
		return state;
	}

	enum PushPullType implements IStringSerializable {
		PUSHER(0),
		PULLER(6);

		public final int metaMask;

		PushPullType(int metaMask) {
			this.metaMask = metaMask;
		}

		@Override
		public String getName() {
			return toString().toLowerCase();
		}
	}
}
