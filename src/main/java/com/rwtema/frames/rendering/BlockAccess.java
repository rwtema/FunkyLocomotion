package com.rwtema.frames.rendering;

import com.rwtema.frames.blocks.TileMovingClient;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockAccess implements IBlockAccess {
    public IBlockAccess world;

    public BlockAccess(World world) {
        this.world = world;
    }

    public TileMovingClient getTile(int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return (tile != null && tile.getClass() == TileMovingClient.class) ? (TileMovingClient) tile : null;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null ? Blocks.air : tile.block;
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null ? null : tile.tile;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLightBrightnessForSkyBlocks(int p_72802_1_, int p_72802_2_, int p_72802_3_, int p_72802_4_) {
        return world.getLightBrightnessForSkyBlocks(p_72802_1_, p_72802_2_, p_72802_3_, p_72802_4_);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null ? 0 : tile.meta;
    }

    @Override
    public int isBlockProvidingPowerTo(int p_72879_1_, int p_72879_2_, int p_72879_3_, int p_72879_4_) {
        return world.isBlockProvidingPowerTo(p_72879_1_, p_72879_2_, p_72879_3_, p_72879_4_);
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null || tile.block == Blocks.air;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BiomeGenBase getBiomeGenForCoords(int p_72807_1_, int p_72807_2_) {
        return world.getBiomeGenForCoords(p_72807_1_, p_72807_2_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getHeight() {
        return world.getHeight();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean extendedLevelsInChunkCache() {
        return world.extendedLevelsInChunkCache();
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        TileMovingClient tile = getTile(x, y, z);
        return tile != null && tile.block.isSideSolid(this, x, y, z, side);
    }
}
