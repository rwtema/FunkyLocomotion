package com.rwtema.funkylocomotion.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockPusher extends ItemBlockMetadata {

    public ItemBlockPusher(Block block) {
        super(block);
    }

    @Override
    public int getMetadata(int meta) {
        return meta < 6 ? 0 : 6;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        return super.getUnlocalizedName(itemstack) + "." + getMetadata(itemstack.getItemDamage());
    }
}
