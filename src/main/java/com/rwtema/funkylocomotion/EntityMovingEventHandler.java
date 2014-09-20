package com.rwtema.funkylocomotion;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.WeakHashMap;

public class EntityMovingEventHandler {
    public static WeakHashMap<Entity, Vec3> client = new WeakHashMap<Entity, Vec3>();
    public static WeakHashMap<Entity, Vec3> server = new WeakHashMap<Entity, Vec3>();

    private EntityMovingEventHandler() {

    }

    public static void init() {
        FMLCommonHandler.instance().bus().register(new EntityMovingEventHandler());
    }

    public static WeakHashMap<Entity, Vec3> getMovementMap(Side side) {
        return side == Side.CLIENT ? client : server;
    }

    public static void moveEntity(Entity entity, double dx, double dy, double dz) {
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;

        entity.posX = (entity.boundingBox.minX + entity.boundingBox.maxX) / 2.0D;
        entity.posY = entity.boundingBox.minY + (double) entity.yOffset - (double) entity.ySize;
        entity.posZ = (entity.boundingBox.minZ + entity.boundingBox.maxZ) / 2.0D;

        double kx, ky, kz;


        if (entity.noClip) {
            entity.boundingBox.offset(dx, dy, dz);
            entity.posX = (entity.boundingBox.minX + entity.boundingBox.maxX) / 2.0D;
            entity.posY = entity.boundingBox.minY + (double) entity.yOffset - (double) entity.ySize;
            entity.posZ = (entity.boundingBox.minZ + entity.boundingBox.maxZ) / 2.0D;
        } else {
            entity.worldObj.theProfiler.startSection("move");
            entity.ySize *= 0.4F;

            double dx_original = dx;
            double dy_original = dy;
            double dz_original = dz;
            AxisAlignedBB bb = entity.boundingBox.copy();
            boolean flag = entity.onGround && entity.isSneaking() && entity instanceof EntityPlayer;

            //make sure player do not fall off if sneaking
            if (flag) {
                double d9;

                for (d9 = 0.05D; dx != 0.0D && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox.getOffsetBoundingBox(dx, -1.0D, 0.0D)).isEmpty(); dx_original = dx) {
                    if (dx < d9 && dx >= -d9) {
                        dx = 0.0D;
                    } else if (dx > 0.0D) {
                        dx -= d9;
                    } else {
                        dx += d9;
                    }
                }

                for (; dz != 0.0D && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox.getOffsetBoundingBox(0.0D, -1.0D, dz)).isEmpty(); dz_original = dz) {
                    if (dz < d9 && dz >= -d9) {
                        dz = 0.0D;
                    } else if (dz > 0.0D) {
                        dz -= d9;
                    } else {
                        dz += d9;
                    }
                }

                while (dx != 0.0D && dz != 0.0D && entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox.getOffsetBoundingBox(dx, -1.0D, dz)).isEmpty()) {
                    if (dx < d9 && dx >= -d9) {
                        dx = 0.0D;
                    } else if (dx > 0.0D) {
                        dx -= d9;
                    } else {
                        dx += d9;
                    }

                    if (dz < d9 && dz >= -d9) {
                        dz = 0.0D;
                    } else if (dz > 0.0D) {
                        dz -= d9;
                    } else {
                        dz += d9;
                    }

                    dx_original = dx;
                    dz_original = dz;
                }
            }

            List list = entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox.addCoord(dx, dy, dz));

            for (int i = 0; i < list.size(); ++i) {
                dy = ((AxisAlignedBB) list.get(i)).calculateYOffset(entity.boundingBox, dy);
            }

            entity.boundingBox.offset(0.0D, dy, 0.0D);

            if (!entity.field_70135_K && dy_original != dy) {
                dz = 0.0D;
                dy = 0.0D;
                dx = 0.0D;
            }

            boolean flag1 = entity.onGround || dy_original != dy && dy_original < 0.0D;
            int j;

            for (j = 0; j < list.size(); ++j) {
                dx = ((AxisAlignedBB) list.get(j)).calculateXOffset(entity.boundingBox, dx);
            }

            entity.boundingBox.offset(dx, 0.0D, 0.0D);

            if (!entity.field_70135_K && dx_original != dx) {
                dz = 0.0D;
                dy = 0.0D;
                dx = 0.0D;
            }

            for (j = 0; j < list.size(); ++j) {
                dz = ((AxisAlignedBB) list.get(j)).calculateZOffset(entity.boundingBox, dz);
            }

            entity.boundingBox.offset(0.0D, 0.0D, dz);

            if (!entity.field_70135_K && dz_original != dz) {
                dz = 0.0D;
                dy = 0.0D;
                dx = 0.0D;
            }


            int k;


            if (entity.stepHeight > 0.0F && flag1 && (flag || entity.ySize < 0.05F) && (dx_original != dx || dz_original != dz)) {
                kz = dx;
                kx = dy;
                ky = dz;
                dx = dx_original;
                dy = dy_original + (double) entity.stepHeight;
                dz = dz_original;
                AxisAlignedBB axisalignedbb1 = entity.boundingBox.copy();
                entity.boundingBox.setBB(bb);
                list = entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox.addCoord(dx_original, dy, dz_original));

                for (k = 0; k < list.size(); ++k) {
                    dy = ((AxisAlignedBB) list.get(k)).calculateYOffset(entity.boundingBox, dy);
                }

                entity.boundingBox.offset(0.0D, dy, 0.0D);

                if (!entity.field_70135_K && dy_original != dy) {
                    dz = 0.0D;
                    dy = 0.0D;
                    dx = 0.0D;
                }

                for (k = 0; k < list.size(); ++k) {
                    dx = ((AxisAlignedBB) list.get(k)).calculateXOffset(entity.boundingBox, dx);
                }

                entity.boundingBox.offset(dx, 0.0D, 0.0D);

                if (!entity.field_70135_K && dx_original != dx) {
                    dz = 0.0D;
                    dy = 0.0D;
                    dx = 0.0D;
                }

                for (k = 0; k < list.size(); ++k) {
                    dz = ((AxisAlignedBB) list.get(k)).calculateZOffset(entity.boundingBox, dz);
                }

                entity.boundingBox.offset(0.0D, 0.0D, dz);

                if (!entity.field_70135_K && dz_original != dz) {
                    dz = 0.0D;
                    dy = 0.0D;
                    dx = 0.0D;
                }

                if (!entity.field_70135_K && dy_original != dy) {
                    dz = 0.0D;
                    dy = 0.0D;
                    dx = 0.0D;
                } else {
                    dy = (double) (-entity.stepHeight);

                    for (k = 0; k < list.size(); ++k) {
                        dy = ((AxisAlignedBB) list.get(k)).calculateYOffset(entity.boundingBox, dy);
                    }

                    entity.boundingBox.offset(0.0D, dy, 0.0D);
                }

                if (kz * kz + ky * ky >= dx * dx + dz * dz) {
                    dx = kz;
                    dy = kx;
                    dz = ky;
                    entity.boundingBox.setBB(axisalignedbb1);
                }
            }

            entity.worldObj.theProfiler.endSection();
            entity.worldObj.theProfiler.startSection("rest");
            entity.posX = (entity.boundingBox.minX + entity.boundingBox.maxX) / 2.0D;
            entity.posY = entity.boundingBox.minY + (double) entity.yOffset - (double) entity.ySize;
            entity.posZ = (entity.boundingBox.minZ + entity.boundingBox.maxZ) / 2.0D;

            entity.worldObj.theProfiler.endSection();
        }

        kx = entity.posX - x;
        ky = entity.posY - y;
        kz = entity.posZ - z;

        entity.prevPosX += kx;
        entity.prevPosY += ky;
        entity.prevPosZ += kz;


    }

    @SubscribeEvent
    public void moveEntities(TickEvent.ServerTickEvent event) {
        move(event);
    }

    @SubscribeEvent
    public void moveEntities(TickEvent.ClientTickEvent event) {
        move(event);
    }

    public void move(TickEvent event) {
        final WeakHashMap<Entity, Vec3> map = getMovementMap(event.side);

        map.clear();
    }

}
