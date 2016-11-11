package com.rwtema.funkylocomotion.items;

import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemBlockFrame extends ItemBlockMetadata {
	public final int index;

	public ItemBlockFrame(Block block) {
		super(block);
		if (!(block instanceof BlockStickyFrame))
			throw new IllegalArgumentException("Frame Block/Item mismatch");

		index = ((BlockStickyFrame) block).getRawIndex();
	}

	@Nonnull
	@Override
	public String getItemStackDisplayName(@Nonnull ItemStack itemstack) {
		if (index == 0 && itemstack.getItemDamage() == 0)
			return super.getItemStackDisplayName(itemstack);

		StringBuilder builder = new StringBuilder(super.getItemStackDisplayName(itemstack));

		builder.append(" (");

		int meta = itemstack.getItemDamage() + index;
		for (int i = 0; i < 6; i++)
			if (((meta) & (1 << i)) != 0)
				builder.append(I18n.translateToLocal("frame.dir.abbreviations." + i));

		builder.append(")");
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack item, @Nonnull EntityPlayer player, @Nonnull List list, boolean debug) {
		super.addInformation(item, player, list, debug);
		if (index == 0 && item.getItemDamage() == 0)
			return;
		list.add(I18n.translateToLocal("frame.dir.start"));
		String s = "";
		boolean flag = false;
		for (int i = 0; i < 6; i++) {
			if (((index + item.getItemDamage()) & (1 << i)) != 0) {
				if (flag)
					s = s + ", ";
				s = s + I18n.translateToLocal("frame.dir.name." + i);
				flag = true;
			}
		}

		list.addAll(Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(s, 60));
	}
}
