package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockMoving extends Block {
	public static BlockMoving instance;
	private final AxisAlignedBB ZERO_BOUNDS = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

	public BlockMoving() {
		super(Material.ROCK);
		this.setLightOpacity(0);
		this.setBlockUnbreakable();
		this.setRegistryName("funkylocomotion:moving");
		instance = this;
	}

	@SuppressWarnings("unused")
	public static boolean _Immoveable() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox,
									  @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (!(tile instanceof TileMovingBase))
			return;

		for (AxisAlignedBB bb : ((TileMovingBase) tile).getTransformedColisions())
			if (entityBox.intersects(bb))
				collidingBoxes.add(bb);

		EnumFacing d = ((TileMovingBase) tile).getDir();
		if (d != null) {
			TileEntity tile2 = worldIn.getTileEntity(pos.offset(d));
			if (!(tile2 instanceof TileMovingBase))
				return;

			for (AxisAlignedBB bb : ((TileMovingBase) tile2).getTransformedColisions())
				if (entityBox.intersects(bb))
					collidingBoxes.add(bb);
		}
	}

	@SuppressWarnings("deprecation")
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		return getBounds(worldIn, pos, false, false);
	}

	private AxisAlignedBB getBounds(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos, boolean renderOffset, boolean shrink) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileMovingBase) {
			TileMovingBase movingBase = (TileMovingBase) tile;
			return movingBase.getCombinedCollisions(renderOffset, shrink);
		}
		return FULL_BLOCK_AABB;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		if (world.isRemote || FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			return new TileMovingClient();
		else
			return new TileMovingServer();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean causesSuffocation(IBlockState state) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
		return ItemStack.EMPTY;
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos) {
		AxisAlignedBB bounds = getBounds(worldIn, pos, true, true);
		if (bounds == null) return ZERO_BOUNDS;
		return bounds.offset(pos);
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		AxisAlignedBB bounds = getBounds(source, pos, false, true);
		if (bounds == null) {
			return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
		}
		return bounds;
	}

	@Override
	public int getLightValue(@Nonnull IBlockState state, IBlockAccess world, @Nonnull BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileMovingBase ? ((TileMovingBase) tile).lightLevel : super.getLightValue(state, world, pos);
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess blockAccess, IBlockState stateIn, BlockPos pos, EnumFacing side) {
		return BlockFaceShape.UNDEFINED;
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof TileMovingBase ? ((TileMovingBase) tile).lightOpacity : super.getLightOpacity(state, world, pos);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
									EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote)
			return false;

		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileMovingServer) {
			((TileMovingServer) tile).cacheActivate(playerIn, side, hand, hitX, hitY, hitZ);
			return true;
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (!FakeWorldClient.isValid(worldIn)) return;
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileMovingClient) {
			TileMovingClient mover = (TileMovingClient) tile;
			FakeWorldClient fakeWorld = FakeWorldClient.getFakeWorldWrapper(worldIn);
			fakeWorld.offset = mover.offset(true);
			fakeWorld.dir_id = mover.dir;
			fakeWorld.dir = mover.getDir();
			@SuppressWarnings("deprecation")
			IBlockState state = mover.block.getStateFromMeta(mover.meta);
			mover.block.randomDisplayTick(state, fakeWorld, pos, rand);
			fakeWorld.offset = 0;
			fakeWorld.dir_id = -1;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}
}
