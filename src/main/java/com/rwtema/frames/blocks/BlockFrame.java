package com.rwtema.frames.blocks;

import framesapi.BlockPos;
import framesapi.IStickyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockFrame extends Block implements IStickyBlock {
    public BlockFrame() {
        super(Material.rock);
        this.setBlockName("newframes:frame");
        this.setBlockTextureName("newframes:frame");
    }

    @Override
    public boolean isStickySide(World world, BlockPos pos, ForgeDirection side) {
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
}
