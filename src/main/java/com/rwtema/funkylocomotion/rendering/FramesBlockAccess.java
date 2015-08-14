package com.rwtema.funkylocomotion.rendering;

import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public class FramesBlockAccess implements IBlockAccess {
    public final IBlockAccess world;

    public FramesBlockAccess(World world) {
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
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int minBrightness) {
        return world.getLightBrightnessForSkyBlocks(x, y, z, minBrightness);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null ? 0 : tile.meta;
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
        return world.isBlockProvidingPowerTo(x, y, z, side);
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null || tile.block == Blocks.air;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return world.getBiomeGenForCoords(x, z);
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
