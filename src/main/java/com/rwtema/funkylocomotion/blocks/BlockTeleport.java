package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.items.ItemBlockTeleporter;
import java.util.ArrayList;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockTeleport extends BlockPusher {
	public static IIcon iconFront;
	public static IIcon iconSide;

	public BlockTeleport() {
		super();
		this.setBlockName("funkylocomotion:teleporter");
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		iconFront = register.registerIcon("funkylocomotion:teleporterFront");
		iconSide = register.registerIcon("funkylocomotion:teleporterSide");
		super.registerBlockIcons(register);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		final int dir = Facing.oppositeSide[meta % 6];
		return side == dir ? iconFront : side == Facing.oppositeSide[dir] ? blockIcon : iconSide;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		return getDrops(world, x, y, z, 0, 0).get(0);
	}

	@Override
	public int damageDropped(int meta) {
		return 0;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileTeleport();
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {

		if (!player.capabilities.isCreativeMode && this.canHarvestBlock(player, world.getBlockMetadata(x, y, z))) {
			ArrayList<ItemStack> items = this.getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0);

			if (world.setBlockToAir(x, y, z)) {
				if (!world.isRemote) {
					for (ItemStack item : items) {
						this.dropBlockAsItem(world, x, y, z, item);
					}
				}

				return true;
			} else {
				return false;
			}
		} else {
			return super.removedByPlayer(world, player, x, y, z, willHarvest);
		}
	}


	@Override
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
		player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
		player.addExhaustion(0.025F);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ItemStack item = new ItemStack(this, 1, damageDropped(metadata));

		if (world.getTileEntity(x, y, z) instanceof TileTeleport) {
			int teleportId = ((TileTeleport) world.getTileEntity(x, y, z)).teleportId;

			if (teleportId != 0) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger(ItemBlockTeleporter.NBT_TELEPORTER_ID, teleportId);
				item.setTagCompound(tag);
			}
		}

		ret.add(item);
		return ret;
	}


	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack item) {
		if (!item.hasTagCompound())
			return;

		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileTeleport) {
			tile.invalidate();
			((TileTeleport) tile).teleportId = item.getTagCompound().getInteger(ItemBlockTeleporter.NBT_TELEPORTER_ID);
			tile.validate();
		}
	}
}
