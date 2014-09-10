package com.rwtema.funkylocomotion.blocks;

import framesapi.BlockPos;
import framesapi.IStickyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockFrame extends Block implements IStickyBlock {
    @Override
    public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_) {
        return super.shouldSideBeRendered(p_149646_1_, p_149646_2_, p_149646_3_, p_149646_4_, p_149646_5_) && !(p_149646_1_.getBlock(p_149646_2_, p_149646_3_, p_149646_4_) instanceof BlockFrame);
    }

    public BlockFrame() {
        super(Material.rock);
        this.setBlockName("funkylocomotion:frame");
        this.setBlockTextureName("funkylocomotion:frame");
    }

    @Override
    public boolean isStickySide(World world, BlockPos pos, ForgeDirection side) {
        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }
}
