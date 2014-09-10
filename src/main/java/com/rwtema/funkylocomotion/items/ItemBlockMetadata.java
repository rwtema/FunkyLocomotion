package com.rwtema.funkylocomotion.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockMetadata extends ItemBlock {
    public ItemBlockMetadata(Block p_i45328_1_) {
        super(p_i45328_1_);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int p_77647_1_) {
        return p_77647_1_;
    }
}
