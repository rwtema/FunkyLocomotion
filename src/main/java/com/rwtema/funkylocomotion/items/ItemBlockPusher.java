package com.rwtema.funkylocomotion.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBlockPusher extends ItemBlockMetadata {

	public ItemBlockPusher(Block block) {
		super(block);
	}

	@Override
	public int getMetadata(int meta) {
		return meta == 0 ? 0 : 6;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return super.getUnlocalizedName(itemstack) + "." + getMetadata(itemstack.getItemDamage());
	}
}
