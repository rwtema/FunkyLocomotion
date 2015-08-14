package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockBooster extends Block {
	public static IIcon iconFront;
	public static IIcon iconSide;

	public BlockBooster() {
		super(Material.rock);
		this.setBlockName("funkylocomotion:booster");
		this.setBlockTextureName("funkylocomotion:pusher");
		this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
		this.setHardness(1);
	}

	@Override
	public void registerBlockIcons(IIconRegister p_149651_1_) {
		iconFront = p_149651_1_.registerIcon("funkylocomotion:boosterFront");
		iconSide = p_149651_1_.registerIcon("funkylocomotion:boosterSide");
		super.registerBlockIcons(p_149651_1_);
	}

	@Override
	public int damageDropped(int meta) {
		return 0;
	}


	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		if (!world.isRemote) {
			ItemStack item = player.getHeldItem();
			if (!(ItemHelper.isWrench(item)))
				return false;

			final int meta = world.getBlockMetadata(x, y, z);
			if ((meta < 6 ? 0 : 6) + side == meta)
				side = Facing.oppositeSide[side];

			world.setBlockMetadataWithNotify(x, y, z, (meta < 6 ? 0 : 6) + side, 3);
		}
		return true;
	}

	@Override
	public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
		final int dir = Facing.oppositeSide[p_149691_2_ % 6];
		return p_149691_1_ == dir ? iconFront : p_149691_1_ == Facing.oppositeSide[dir] ? blockIcon : iconSide;
	}

	@Override
	public int getRenderType() {
		return FunkyLocomotion.proxy.pusherRendererId;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileBooster();
	}
}
