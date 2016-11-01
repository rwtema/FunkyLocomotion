package com.rwtema.funkylocomotion.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockMetadata extends ItemBlock {
	public ItemBlockMetadata(Block block) {
		super(block);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}
}
