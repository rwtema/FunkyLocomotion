package com.rwtema.frames.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.Random;

public class BlockEmptyMoving extends Block {
    protected BlockEmptyMoving() {
        super(Material.rock);
        this.setBlockUnbreakable();
        this.setBlockName("newframes:emptymoving");
        this.setBlockTextureName("stone");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p_149727_5_, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        world.scheduleBlockUpdate(x, y, z, this, 1);
        return true;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random p_149674_5_) {
        world.setBlockToAir(x, y, z);
    }
}
