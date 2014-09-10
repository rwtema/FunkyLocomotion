package com.rwtema.funkylocomotion.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockPusher extends ItemBlockMetadata {

    public ItemBlockPusher(Block p_i45328_1_) {
        super(p_i45328_1_);
    }

    @Override
    public int getMetadata(int p_77647_1_) {
        return p_77647_1_ < 6 ? 0 : 6;
    }

    @Override
    public String getUnlocalizedName(ItemStack p_77667_1_) {
        return super.getUnlocalizedName(p_77667_1_) + "." + getMetadata(p_77667_1_.getItemDamage());
    }
}
