package com.rwtema.funkylocomotion.blocks;

import framesapi.BlockPos;
import framesapi.IStickyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockFrame extends Block implements IStickyBlock {
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
