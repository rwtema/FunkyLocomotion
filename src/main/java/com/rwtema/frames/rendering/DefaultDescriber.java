package com.rwtema.frames.rendering;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.BlockPos;
import framesapi.IBlockDescriber;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DefaultDescriber implements IBlockDescriber {
    public static final RenderBlocks renderBlocks = new RenderBlocks();

    @Override
    public NBTTagCompound getDescriptor(World world, BlockPos pos) {

        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object createRenderCache(NBTTagCompound tagCompound) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(IBlockAccess fakeWorld, BlockPos pos, Block block, int meta, Object cache) {
        renderBlocks.blockAccess = fakeWorld;

        renderBlocks.renderBlockByRenderType(block, pos.x, pos.y, pos.z);

        renderBlocks.blockAccess = null;

    }
}
