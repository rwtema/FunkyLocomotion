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
    public Block getBlock(int p_147439_1_, int p_147439_2_, int p_147439_3_) {
        return access.getBlock(p_147439_1_ + dx, p_147439_2_ + dy, p_147439_3_ + dz);
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return access.getTileEntity(x + dx, y + dy, z + dz);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int p_72802_4_) {
        return access.getLightBrightnessForSkyBlocks(x + dx, y + dy, z + dz, p_72802_4_);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        return access.getBlockMetadata(x + dx, y + dy, z + dz);
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int p_72879_4_) {
        return access.isBlockProvidingPowerTo(x + dx, y + dy, z + dz, p_72879_4_);
    }

    @Override
    public boolean isAirBlock(int x, int y, int p_147437_3_) {
        return access.isAirBlock(x + dx, y + dy, p_147437_3_ + dz);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BiomeGenBase getBiomeGenForCoords(int p_72807_1_, int p_72807_2_) {
        return access.getBiomeGenForCoords(p_72807_1_ + dx, p_72807_2_ + dz);
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
