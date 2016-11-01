package com.rwtema.funkylocomotion;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTabFrames extends CreativeTabs {
	public CreativeTabFrames() {
		super(FunkyLocomotion.MODID);
	}

	@Override
	public Item getTabIconItem() {
		return Item.getItemFromBlock(FunkyLocomotion.pusher);
	}
}
