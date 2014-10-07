package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.EntityMovingEventHandler;
import com.rwtema.funkylocomotion.Proxy;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TileMovingBase extends TileEntity {
    private static final AxisAlignedBB[] blank = new AxisAlignedBB[0];
    public AxisAlignedBB[] collisions = blank;
    public final Side side;
    public boolean isAir = true;
    public int time = 0;
    public int maxTime = 0;
    public NBTTagCompound block;
    public NBTTagCompound desc;
    public ForgeDirection dir = ForgeDirection.UNKNOWN;
    public int lightLevel = 0;
    public int lightOpacity = 255;
    public int scheduledTickTime = -1;
    public int scheduledTickPriority;

    public TileMovingBase(Side side) {
        this.side = side;
    }

    public static boolean _Immovable() {
        return true;
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

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        block = tag.getCompoundTag("BlockTag");
        desc = tag.getCompoundTag("DescTag");
        time = tag.getInteger("Time");
        maxTime = tag.getInteger("MaxTime");
        dir = ForgeDirection.getOrientation(tag.getByte("Dir"));

        isAir = block.hasNoTags();

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

    public Vec3 getMovVec() {
        double d = 1.0 / maxTime;
        return Vec3.createVectorHelper(dir.offsetX * d, dir.offsetY * d, dir.offsetZ * d);
    }

    @SuppressWarnings("unchecked")
    public void updateEntity() {
        if (maxTime == 0)
            return;

        if (worldObj.isRemote) {
            time = time + 1 - 1;
        }

        Vec3 mov = getMovVec();

        Set<Entity> entityList = new HashSet<Entity>();


        time++;

        for (AxisAlignedBB bb : getTransformedColisions()) {
            List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, bb.expand(0, 0.1, 0));
            for (Entity entity : entities) {
                entityList.add(entity);
            }
        }

        for (Entity a : entityList) {

            if (!a.isDead) {
                Map<Entity, Vec3> map = EntityMovingEventHandler.getMovementMap(side);
                if (!map.containsKey(a)) {
                    for (AxisAlignedBB bb : getTransformedColisions()) {
                        if (a.boundingBox.intersectsWith(bb)) {
                            if (a.boundingBox.minY > bb.maxY - 0.2) {
                                a.boundingBox.offset(0, bb.maxY - a.boundingBox.minY, 0);
                            }
                        } else if (dir == ForgeDirection.DOWN && a.motionY <= 0 && a.boundingBox.intersectsWith(bb.offset(0, 0.2, 0))) {
                            a.boundingBox.offset(0, bb.maxY - a.boundingBox.minY, 0);
                        }
                    }

                    EntityMovingEventHandler.moveEntity(a, mov.xCoord, mov.yCoord, mov.zCoord);

                    map.put(a, null);
                }

            }
        }

    }

    public AxisAlignedBB getCombinedCollisions() {
        AxisAlignedBB bb = null;
        for (AxisAlignedBB collision : collisions) {
            if (bb == null)
                bb = collision.copy();
            else
                bb.func_111270_a(collision);
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

        double h = offset(false);
        bb.offset(h * dir.offsetX, h * dir.offsetY, h * dir.offsetZ);
        return bb;
    }

    public AxisAlignedBB[] getTransformedColisions() {
        AxisAlignedBB[] tbbs = new AxisAlignedBB[collisions.length];
        double h = offset(false);
        for (int i = 0; i < collisions.length; i++) {
            tbbs[i] = collisions[i].getOffsetBoundingBox(h * dir.offsetX, h * dir.offsetY, h * dir.offsetZ).offset(xCoord, yCoord, zCoord);
        }
        return tbbs;
    }

    public float progress() {
        return time >= maxTime ? 1 : (time + Proxy.renderTimeOffset) / (maxTime);
    }

    public double offset(boolean t) {
        if (time >= maxTime)
            return 0;
        float f = t ? Proxy.renderTimeOffset : 0;
        return (time + f) / (maxTime) - 1;
    }


}
