package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.Proxy;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileMovingBase extends TileEntity {
    private static AxisAlignedBB[] blank = new AxisAlignedBB[0];

    public int time = 0;
    public int maxTime = 0;
    public NBTTagCompound block;
    public NBTTagCompound desc;
    public ForgeDirection dir = ForgeDirection.UNKNOWN;
    public AxisAlignedBB[] collisions = blank;

    public int lightLevel = 0;
    public int lightOpacity = 255;

    public int scheduledTickTime = -1;
    public int scheduledTickPriority;

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        block = tag.getCompoundTag("BlockTag");
        desc = tag.getCompoundTag("DescTag");
        time = tag.getInteger("Time");
        maxTime = tag.getInteger("MaxTime");
        dir = ForgeDirection.getOrientation(tag.getByte("Dir"));


        if (tag.hasKey("Collisions", 10))
            collisions = AxisTags(tag.getTagList("Collisions", 10));

        lightLevel = tag.getByte("Light");
        lightOpacity = tag.getShort("Opacity");


        if (tag.hasKey("TimeTime", 3)) {
            scheduledTickTime = tag.getInteger("TickTime");
            scheduledTickPriority = tag.getInteger("TickPriority");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("BlockTag", block);
        tag.setTag("DescTag", desc);
        tag.setInteger("Time", time);
        tag.setInteger("MaxTime", maxTime);
        tag.setByte("Dir", (byte) dir.ordinal());

        if (collisions.length > 0)
            tag.setTag("Collisions", TagsAxis(collisions));

        if (lightLevel != 0) tag.setByte("Light", (byte) lightLevel);
        if (lightOpacity != 0) tag.setShort("Opacity", (short) lightOpacity);

        if (scheduledTickTime != -1) {
            tag.setInteger("TickTime", scheduledTickTime);
            tag.setInteger("TickPriority", scheduledTickPriority);
        }
    }

    public static boolean _Immovable() {
        return true;
    }


    public abstract void updateEntity();

    public AxisAlignedBB getCombinedCollisions() {
        AxisAlignedBB bb = null;
        for (int i = 0; i < collisions.length; i++) {
            if (bb == null)
                bb = collisions[i].copy();
            else
                bb.func_111270_a(collisions[i]);
        }

        TileEntity other = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
        if (other instanceof TileMovingBase) {
            AxisAlignedBB[] bbs1 = ((TileMovingBase) other).collisions;
            for (AxisAlignedBB bb1 : bbs1) {
                if (bb == null)
                    bb = bb1.getOffsetBoundingBox(dir.offsetX, dir.offsetY, dir.offsetZ);
                else
                    bb.func_111270_a(bb1.getOffsetBoundingBox(dir.offsetX, dir.offsetY, dir.offsetZ));
            }
        }

        if (bb == null)
            return null;

        double h = offset(0);
        bb.offset(h * dir.offsetX, h * dir.offsetY, h * dir.offsetZ);
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
        final float v = Math.abs(f - Proxy.renderTimeOffset);
        if((time + f) / (maxTime + 1) > 1){
            f = 0;
        }
        if(time == maxTime)
            return 0;
        f = Proxy.renderTimeOffset;
        return (time + f) / (maxTime) - 1;
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
