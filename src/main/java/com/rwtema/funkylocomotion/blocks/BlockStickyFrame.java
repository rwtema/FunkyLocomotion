package com.rwtema.funkylocomotion.blocks;

import java.util.EnumMap;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.Validate;
import com.google.common.collect.ImmutableList;
import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStickyFrame extends BlockFrame {
	public static final BlockStickyFrame[] blocks = new BlockStickyFrame[4];
	public static final PropertyBool[] DIR_OPEN;
	public static final EnumMap<EnumFacing, PropertyBool> DIR_OPEN_MAP;
	public static int curLoadingIndex = -1;

	static {
		DIR_OPEN_MAP = new EnumMap<>(EnumFacing.class);
		DIR_OPEN = new PropertyBool[6];
		for (int i = 0; i < EnumFacing.values().length; i++) {
			EnumFacing facing = EnumFacing.values()[i];
			DIR_OPEN[i] = PropertyBool.create("open_" + facing.getName().toLowerCase());
			DIR_OPEN_MAP.put(facing, DIR_OPEN[i]);
		}
	}

	public int index;

	public BlockStickyFrame() {
		super();
		index = curLoadingIndex;
		blocks[index] = this;
		this.setUnlocalizedName("funkylocomotion:frame");
		this.setRegistryName("funkylocomotion:frame_" + index);
		if (index == 0)
			this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
		this.setLightOpacity(0);

		for (IBlockState state : blockState.getValidStates()) {
			int metaFromState = getMetaFromState(state);
			IBlockState state2 = getStateFromMeta(metaFromState);
			Validate.isTrue(state == state2);
		}
	}

	public static boolean isRawMetaSticky(int i, EnumFacing side) {
		return (i & (1 << side.ordinal())) == 0;
	}

	public static int getRawMeta(IBlockState state) {
		int t = 0;
		for (int i = 0; i < DIR_OPEN.length; i++) {
			if (state.getValue(DIR_OPEN[i])) {
				t |= 1 << i;
			}
		}
		return t;
	}

	public static boolean isStickySide(IBlockState state, EnumFacing side) {
		return isRawMetaSticky(getRawMeta(state), side);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Nonnull
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	public int getRawIndex() {
		return index * 16;
	}

	@Override
	public boolean isStickySide(World world, BlockPos pos, EnumFacing side) {
		return isRawMetaSticky(getRawMeta(world.getBlockState(pos)), side);
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		int i = getRawIndex() + meta;

		IBlockState state = getDefaultState();
		for (int k = 0; k < DIR_OPEN.length; k++) {
			state = state.withProperty(DIR_OPEN[k], (i & (1 << k)) != 0);
		}
		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return getRawMeta(state) & 15;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack item = playerIn.getHeldItem(hand);
		if (!(ItemHelper.isWrench(item)))
			return false;

		int i = (getRawMeta(state)) ^ (1 << side.ordinal());

		if (i > 63 || i < 0)
			i = 0;

		int meta = i % 16;
		Block block = blocks[(i - meta) / 16];

		worldIn.setBlockState(pos, block.getStateFromMeta(meta), 2);
		return true;
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		index = curLoadingIndex;
		return new BlockStateContainer(this, DIR_OPEN[0], DIR_OPEN[1], DIR_OPEN[2], DIR_OPEN[3], DIR_OPEN[4], DIR_OPEN[5]) {
			final ImmutableList<IBlockState> myValidStates;

			{
				ImmutableList.Builder<IBlockState> builder = ImmutableList.builder();
				for (IBlockState state : super.getValidStates()) {
					int rawMeta = getRawMeta(state);
					if (rawMeta >= getRawIndex()
							&& (rawMeta < 16 + getRawIndex())
							) {
						builder.add(state);
					}
				}
				myValidStates = builder.build();
			}

			@Nonnull
			@Override
			public ImmutableList<IBlockState> getValidStates() {
				return myValidStates;
			}
		};
	}

	@Override
	public void getSubBlocks(@Nonnull Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
		if (index == 0) {
			list.add(new ItemStack(itemIn));
		}
	}
}
