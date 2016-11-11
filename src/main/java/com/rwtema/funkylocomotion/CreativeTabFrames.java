package com.rwtema.funkylocomotion;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public class CreativeTabFrames extends CreativeTabs {
	public CreativeTabFrames() {
		super(FunkyLocomotion.MODID);
	}

	@Nonnull
	@Override
	public Item getTabIconItem() {
		return Validate.notNull(Item.getItemFromBlock(FunkyLocomotion.pusher));
	}
}
