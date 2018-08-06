package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.blocks.FLBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public class CreativeTabFrames extends CreativeTabs {
	public CreativeTabFrames() {
		super(FunkyLocomotion.MODID);
	}

	@Nonnull
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Validate.notNull(Item.getItemFromBlock(FLBlocks.PUSHER)));
	}
}
