package com.rwtema.funkylocomotion.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rwtema.funkylocomotion.api.IAdvStickyBlock;
import com.rwtema.funkylocomotion.helper.NullHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlockMassFrameEdge extends BlockFLMultiState implements IAdvStickyBlock {
	public static final HashMap<EnumFacing.Axis, AxisOrNone> reverse = new HashMap<>();
	public static PropertyEnum<AxisOrNone> ORIENTATION = PropertyEnum.create("orientation", AxisOrNone.class);

	static {
		for (AxisOrNone axisOrNone : AxisOrNone.values()) {
			reverse.put(axisOrNone.axis, axisOrNone);
		}
	}

	private final float s = 5 / 16F;
	private final float e = 1 - s;
	Map<AxisOrNone, AxisAlignedBB> bounds = ImmutableMap.<AxisOrNone, AxisAlignedBB>builder()
			.put(AxisOrNone.NONE, new AxisAlignedBB(s, s, s, e, e, e))
			.put(AxisOrNone.X, new AxisAlignedBB(0, s, s, 1, e, e))
			.put(AxisOrNone.Y, new AxisAlignedBB(s, 0, s, e, 1, e))
			.put(AxisOrNone.Z, new AxisAlignedBB(s, s, 0, e, e, 1))
			.build();

	public BlockMassFrameEdge() {
		super(Material.ROCK);
		this.setRegistryName("funkylocomotion:mass_frame_edge");
		this.setUnlocalizedName("funkylocomotion:mass_frame_edge");
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return bounds.get(getActualState(state, source, pos).getValue(ORIENTATION));
	}

	@SuppressWarnings("deprecation")
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ORIENTATION);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		EnumFacing.Axis axis = null;
		for (EnumFacing facing : EnumFacing.values()) {
			IBlockState blockState = worldIn.getBlockState(pos.offset(facing));
			Block block = blockState.getBlock();
			if (block == this || block == NullHelper.notNull(FLBlocks.MASS_FRAME_CORNER)) {
				if (axis == null) {
					axis = facing.getAxis();
				} else if (axis != facing.getAxis()) {
					axis = null;
					break;
				}
			}
		}

		return state.withProperty(ORIENTATION, reverse.get(axis));
	}

	@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		super.breakBlock(worldIn, pos, state);

	}

	@Override
	public Iterable<BlockPos> getBlocksToMove(World world, BlockPos pos) {
		ImmutableList.Builder<BlockPos> builder = ImmutableList.builder();
		EnumFacing.Axis axis = null;
		for (EnumFacing facing : EnumFacing.values()) {
			BlockPos offset = pos.offset(facing);
			Block block = world.getBlockState(offset).getBlock();
			if (block == this || block == NullHelper.notNull(FLBlocks.MASS_FRAME_CORNER)) {
				if (axis == null || axis == facing.getAxis()) {
					builder.add(offset);
					axis = facing.getAxis();
				} else {
					return ImmutableList.of();
				}
			}
		}

		return builder.build();
	}

	public enum AxisOrNone implements IStringSerializable {
		X(EnumFacing.Axis.X),
		Y(EnumFacing.Axis.Y),
		Z(EnumFacing.Axis.Z),
		NONE(null);


		@Nullable
		public EnumFacing.Axis axis;

		AxisOrNone(@Nullable EnumFacing.Axis axis) {
			this.axis = axis;

		}

		@Nonnull
		@Override
		public String getName() {
			return axis != null ? axis.getName() : "none";
		}
	}
}
