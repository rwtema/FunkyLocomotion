package com.rwtema.funkylocomotion.blocks;

import com.google.common.collect.Streams;
import com.rwtema.funkylocomotion.helper.NullHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockMassFrameCorner extends BlockFLMultiState {
	private static final double s = 2 / 16F;
	private static final boolean[] booleanValues = {false, true};
	public static Map<EnumFacing, PropertyBool> PROPERTY_MAP = Stream.of(EnumFacing.values()).collect(Collectors.toMap(s -> s, s -> PropertyBool.create(s.getName2())));
	public static PropertyBool ACTIVE = PropertyBool.create("active");
	public static PropertyEnum<State> STATE = PropertyEnum.create("state", State.class);
	AxisAlignedBB bounds = new AxisAlignedBB(s, s, s, 1 - s, 1 - s, 1 - s);


	public BlockMassFrameCorner() {
		super(Material.ROCK);
		setRegistryName("funkylocomotion:mass_frame_corner");
		setUnlocalizedName("funkylocomotion:mass_frame_corner");
	}

	public static boolean checkEdgeFrame(World worldIn, @Nullable EntityPlayer playerIn, int x0, int y0, int z0, int x1, int y1, int z1) {
		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
		BlockMassFrameCorner blockMassFrameCorner = NullHelper.notNull(FLBlocks.MASS_FRAME_CORNER);
		for (boolean x : booleanValues) {
			for (boolean y : booleanValues) {
				for (boolean z : booleanValues) {
					blockPos.setPos(x ? x1 : x0, y ? y1 : y0, z ? z1 : z0);

					IBlockState blockState = worldIn.getBlockState(blockPos);
					if (blockState.getBlock() != blockMassFrameCorner) {
						if (playerIn != null) {
							playerIn.sendMessage(new TextComponentTranslation("frame.mass.err.nocornerfound", blockMassFrameCorner.getBlockPosText(blockPos)));
						}
						return false;
					}
				}
			}
		}

		Block edgeBlock = NullHelper.notNull(FLBlocks.MASS_FRAME_EDGE);
		for (boolean x : booleanValues) {
			for (boolean y : booleanValues) {
				for (int z = z0 + 1; z <= (z1 - 1); z++) {
					blockPos.setPos(x ? x1 : x0, y ? y1 : y0, z);
					if (worldIn.getBlockState(blockPos).getBlock() != edgeBlock) {
						if (playerIn != null) {
							playerIn.sendMessage(new TextComponentTranslation("frame.mass.err.noedgefound", blockMassFrameCorner.getBlockPosText(blockPos)));
						}
						return false;
					}
				}
			}
		}

		for (boolean x : booleanValues) {
			for (boolean z : booleanValues) {
				for (int y = y0 + 1; y <= (y1 - 1); y++) {
					blockPos.setPos(x ? x1 : x0, y, z ? z1 : z0);
					if (worldIn.getBlockState(blockPos).getBlock() != edgeBlock) {
						if (playerIn != null) {
							playerIn.sendMessage(new TextComponentTranslation("frame.mass.err.noedgefound", blockMassFrameCorner.getBlockPosText(blockPos)));
						}
						return false;
					}
				}
			}
		}

		for (boolean z : booleanValues) {
			for (boolean y : booleanValues) {
				for (int x = x0 + 1; x <= (x1 - 1); x++) {
					blockPos.setPos(x, y ? y1 : y0, z ? z1 : z0);
					if (worldIn.getBlockState(blockPos).getBlock() != edgeBlock) {
						if (playerIn != null) {
							playerIn.sendMessage(new TextComponentTranslation("frame.mass.err.noedgefound", blockMassFrameCorner.getBlockPosText(blockPos)));
						}
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
//		if (state.getValue(STATE) == State.EMPTY) {
		if (worldIn.isRemote) return true;
		processPosition(worldIn, pos, facing, playerIn);
		return true;
//		}
//
//		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		super.breakBlock(worldIn, pos, state);

	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return bounds;
	}

	public void processPosition(World worldIn, BlockPos pos, EnumFacing facing, @Nullable EntityPlayer playerIn) {
		BlockMassFrameEdge edgeBlock = NullHelper.notNull(FLBlocks.MASS_FRAME_EDGE);
		HashMap<EnumFacing.Axis, BlockPos> boundTypes = new HashMap<>();

		mainLoop:
		for (EnumFacing enumFacing : EnumFacing.values()) {
			if (boundTypes.get(enumFacing.getAxis()) != null) {
				Block block = worldIn.getBlockState(pos.offset(enumFacing)).getBlock();
				if (block == edgeBlock) {
					if (playerIn != null) {
						playerIn.sendMessage(new TextComponentTranslation("frame.mass.err.axis", enumFacing.getAxis().getName2()));
					}
					return;
				}
			}

			BlockPos.MutableBlockPos npos = new BlockPos.MutableBlockPos(pos);
			for (int i = 0; i < TileMassFrame.MAX_RANGE; i++) {
				npos.move(enumFacing);
				Block block = worldIn.getBlockState(npos).getBlock();
				if (block != edgeBlock) {
					if (block == this) {
						if (i != 0) {
							boundTypes.put(enumFacing.getAxis(), npos);
						}
					} else {
						if (i != 0) {
							if (playerIn != null) {
								playerIn.sendMessage(new TextComponentTranslation("frame.mass.err.nomatchingcorner", getBlockPosText(npos)));
							}
							return;
						}
					}
					continue mainLoop;
				}
			}

			if (playerIn != null) {
				playerIn.sendMessage(new TextComponentTranslation("frame.mass.err.toolong", getFacingTranslation(enumFacing), TileMassFrame.MAX_RANGE));
			}
			return;
		}

		if (boundTypes.size() != 3) {
			if (playerIn != null) {
				playerIn.sendMessage(new TextComponentTranslation("frame.mass.err.corner"));
			}
			return;
		}

		int x0 = boundTypes.values().stream().mapToInt(BlockPos::getX).min().orElseThrow(RuntimeException::new);
		int y0 = boundTypes.values().stream().mapToInt(BlockPos::getY).min().orElseThrow(RuntimeException::new);
		int z0 = boundTypes.values().stream().mapToInt(BlockPos::getZ).min().orElseThrow(RuntimeException::new);

		int x1 = boundTypes.values().stream().mapToInt(BlockPos::getX).max().orElseThrow(RuntimeException::new);
		int y1 = boundTypes.values().stream().mapToInt(BlockPos::getY).max().orElseThrow(RuntimeException::new);
		int z1 = boundTypes.values().stream().mapToInt(BlockPos::getZ).max().orElseThrow(RuntimeException::new);


		if (!checkEdgeFrame(worldIn, playerIn, x0, y0, z0, x1, y1, z1)) return;

		TileMassFrameController controller = null;
		List<TileMassFrame> slaves = new ArrayList<>();
		for (boolean x : booleanValues) {
			for (boolean y : booleanValues) {
				for (boolean z : booleanValues) {
					BlockPos blockPos = new BlockPos(x ? x1 : x0, y ? y1 : y0, z ? z1 : z0);
					if (x && y && z) {
						worldIn.setBlockState(blockPos, getDefaultState().withProperty(STATE, State.CONTROLLER));
						controller = (TileMassFrameController) worldIn.getTileEntity(blockPos);
					} else {
						worldIn.setBlockState(blockPos, getDefaultState().withProperty(STATE, State.SLAVE));
						slaves.add((TileMassFrame) worldIn.getTileEntity(blockPos));
					}
				}
			}
		}

		if (controller == null) {
			throw new RuntimeException();
		}

		for (TileMassFrame slave : slaves) {
			slave.controllerPos = controller.getPos().subtract(slave.getPos());
		}

		controller.start = new BlockPos(x0, y0, z0).subtract(controller.getPos());
		controller.end = new BlockPos(x1, y1, z1).subtract(controller.getPos());
		if (playerIn != null) {
			playerIn.sendMessage(new TextComponentTranslation("frame.mass.success", 1 + x1 - x0, 1 + y1 - y0, 1 + z1 - z0));
		}
	}

	@Nonnull
	private String getBlockPosText(BlockPos blockPos) {
		return "[" + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ() + "]";
	}

	@Nonnull
	private TextComponentTranslation getFacingTranslation(EnumFacing enumFacing) {
		return new TextComponentTranslation("frame.dir.name." + enumFacing.ordinal());
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this,
				Streams.concat(Stream.of(STATE), Stream.of(ACTIVE), PROPERTY_MAP.values().stream())
						.toArray(IProperty[]::new));
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		for (EnumFacing facing : EnumFacing.values()) {
			state = state.withProperty(PROPERTY_MAP.get(facing), worldIn.getBlockState(pos.offset(facing)).getBlock() == NullHelper.notNull(FLBlocks.MASS_FRAME_EDGE));
		}
		if (state.getValue(STATE) == State.EMPTY) {
			state = state.withProperty(ACTIVE, false);
		} else {
			state = state.withProperty(ACTIVE, true);
		}
		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(STATE).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(STATE, State.values()[meta % 3]);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(STATE) != State.EMPTY;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		switch (state.getValue(STATE)) {
			case SLAVE:
				return new TileMassFrame();
			case CONTROLLER:
				return new TileMassFrameController();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	public enum State implements IStringSerializable {
		EMPTY,
		SLAVE,
		CONTROLLER;

		@Override
		public String getName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}
