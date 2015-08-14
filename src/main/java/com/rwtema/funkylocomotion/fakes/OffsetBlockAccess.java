package com.rwtema.funkylocomotion.fakes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public class OffsetBlockAccess implements IBlockAccess {
    public final IBlockAccess access;
    public int dx, dy, dz;

    public OffsetBlockAccess(IBlockAccess world) {
        access = world;
    }

    public OffsetBlockAccess setOffset(ForgeDirection dir) {
        return setOffset(dir.offsetX, dir.offsetY, dir.offsetZ);
    }

    public OffsetBlockAccess setOffset(int x, int y, int z) {
        dx = x;
        dy = y;
        dz = z;
        return this;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return access.getBlock(x + dx, y + dy, z + dz);
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return access.getTileEntity(x + dx, y + dy, z + dz);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int minBrightness) {
        return access.getLightBrightnessForSkyBlocks(x + dx, y + dy, z + dz, minBrightness);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        return access.getBlockMetadata(x + dx, y + dy, z + dz);
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
        return access.isBlockProvidingPowerTo(x + dx, y + dy, z + dz, side);
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        return access.isAirBlock(x + dx, y + dy, z + dz);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return access.getBiomeGenForCoords(x + dx, z + dz);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getHeight() {
        return access.getHeight();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean extendedLevelsInChunkCache() {
        return access.extendedLevelsInChunkCache();
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        return access.isSideSolid(x + dx, y + dy, z + dz, side, _default);
    }
}
