package com.rwtema.frames.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileMovingBase extends TileEntity {
    public int time = 0;
    public int maxTime = 0;
    public NBTTagCompound block;
    public NBTTagCompound desc;
    public ForgeDirection dir = ForgeDirection.UNKNOWN;

    private static AxisAlignedBB[] blank = new AxisAlignedBB[0];
    public AxisAlignedBB[] collisions = blank;


    public int lightLevel = 0;
    public int lightOpacity = 255;

    public int scheduledTickTime = -1;
    public int scheduledTickPriority;

    public static boolean _Immovable() {
        return true;
    }


    public abstract void updateEntity();

    public AxisAlignedBB getCombinedCollisions() {
        if (collisions.length == 0)
            return null;

        AxisAlignedBB bb = collisions[0].copy();
        for (int i = 1; i < collisions.length; i++)
            bb.func_111270_a(collisions[i]);

        double h = offset(0);
        bb.offset(h * dir.offsetX, h * dir.offsetY, h * dir.offsetZ).offset(xCoord, yCoord, zCoord);
        return bb;
    }

    public AxisAlignedBB[] getTransformedColisions() {
        AxisAlignedBB[] tbbs = new AxisAlignedBB[collisions.length];
        double h = offset(0);
        for (int i = 0; i < collisions.length; i++) {
            tbbs[i] = collisions[i].getOffsetBoundingBox(h * dir.offsetX, h * dir.offsetY, h * dir.offsetZ).offset(xCoord, yCoord, zCoord);
        }
        return tbbs;
    }


    public double offset(float f) {
        return (time + f) / (maxTime + 1) - 1;
    }


    private static NBTTagCompound NBTAxis(AxisAlignedBB bb) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("a", (float) bb.minX);
        tag.setFloat("b", (float) bb.minY);
        tag.setFloat("c", (float) bb.minZ);
        tag.setFloat("d", (float) bb.maxX);
        tag.setFloat("e", (float) bb.maxY);
        tag.setFloat("f", (float) bb.maxZ);
        return tag;
    }

    private static AxisAlignedBB AxisNBT(NBTTagCompound tag) {
        return AxisAlignedBB.getBoundingBox(
                tag.getFloat("a"),
                tag.getFloat("b"),
                tag.getFloat("c"),
                tag.getFloat("d"),
                tag.getFloat("e"),
                tag.getFloat("f")
        );
    }

    protected static AxisAlignedBB[] AxisTags(NBTTagList tagList) {
        final int n = tagList.tagCount();
        AxisAlignedBB[] bbs = new AxisAlignedBB[n];
        for (int i = 0; i < n; i++)
            bbs[i] = AxisNBT(tagList.getCompoundTagAt(i));

        return bbs;
    }

    protected static NBTTagList TagsAxis(AxisAlignedBB[] bbs) {
        NBTTagList tagList = new NBTTagList();
        for (AxisAlignedBB bb : bbs)
            tagList.appendTag(NBTAxis(bb));
        return tagList;
    }


}
