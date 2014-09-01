package com.rwtema.frames.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileMovingBase extends TileEntity {
    public int time = 0;
    public int maxTime = 0;
    public NBTTagCompound block;
    public NBTTagCompound desc;
    public ForgeDirection dir = ForgeDirection.UNKNOWN;

    public int lightLevel;
    public int lightOpacity;

    public abstract void updateEntity();

}
