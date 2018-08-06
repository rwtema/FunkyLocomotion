package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.items.ItemBlockTeleporter;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTeleport extends BlockPusher {
	public BlockTeleport() {
		super();
		this.setRegistryName("funkylocomotion:teleporter");
		this.setUnlocalizedName("funkylocomotion:teleporter");
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 0));
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return new TileTeleport();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, @Nonnull IBlockState state) {
		@SuppressWarnings("deprecation")
		ItemStack item = super.getItem(worldIn, pos, state);
		if (!item.isEmpty()) {
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			if (tileEntity instanceof TileTeleport) {
				int teleportId = ((TileTeleport) tileEntity).teleportId;

				if (teleportId != 0) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setInteger(ItemBlockTeleporter.NBT_TELEPORTER_ID, teleportId);
					item.setTagCompound(tag);
				}
			}
		}
		return item;
	}

	@Override
	public void harvestBlock(@Nonnull World worldIn, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state,
							 @Nullable TileEntity te, @Nonnull ItemStack stack) {
		if (te instanceof TileTeleport) {
			ItemStack itemstack = new ItemStack(this, 1);
			int teleportId = ((TileTeleport) te).teleportId;

			if (teleportId != 0) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger(ItemBlockTeleporter.NBT_TELEPORTER_ID, teleportId);
				itemstack.setTagCompound(tag);
			}

			spawnAsEntity(worldIn, pos, itemstack);
		} else {
			super.harvestBlock(worldIn, player, pos, state, null, stack);
		}
	}


	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		if (!stack.hasTagCompound())
			return;

		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof TileTeleport) {
			tile.invalidate();
			((TileTeleport) tile).teleportId = Validate.notNull(stack.getTagCompound()).getInteger(ItemBlockTeleporter.NBT_TELEPORTER_ID);
			tile.validate();
		}
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
		IBlockState state = getDefaultState();
		state = state.withProperty(BlockDirectional.FACING, EnumFacing.values()[meta % 6]);
		return state;
	}
}
