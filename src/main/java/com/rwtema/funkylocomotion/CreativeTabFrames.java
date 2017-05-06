package com.rwtema.funkylocomotion;

import javax.annotation.Nonnull;
import org.apache.commons.lang3.Validate;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabFrames extends CreativeTabs {
	public CreativeTabFrames() {
		super(FunkyLocomotion.MODID);
	}

	@Nonnull
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Validate.notNull(Item.getItemFromBlock(FunkyLocomotion.pusher)));
	}
}
