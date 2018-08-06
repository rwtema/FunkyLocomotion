package com.rwtema.funkylocomotion;

import java.lang.reflect.Field;
import java.util.List;
import java.util.WeakHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class EntityMovingEventHandler {
	public static final WeakHashMap<Entity, Vec3d> client = new WeakHashMap<>();
	public static final WeakHashMap<Entity, Vec3d> server = new WeakHashMap<>();

	private EntityMovingEventHandler() {

	}

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new EntityMovingEventHandler());
	}

	public static WeakHashMap<Entity, Vec3d> getMovementMap(Side side) {
		return side == Side.CLIENT ? client : server;
	}

	public static void moveEntity(Entity entity, double dx, double dy, double dz) {

		double x = entity.posX;
		double y = entity.posY;
		double z = entity.posZ;

		entity.resetPositionToBB();

		double kx, ky, kz;
		if (entity.noClip) {
			entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(dx, dy, dz));
			entity.resetPositionToBB();
		} else {
			double xspeed = dx;
			double yspeed = dy;
			double zpeed = dz;
			boolean flag = entity.onGround && entity.isSneaking() && entity instanceof EntityPlayer;

			if (flag) {
				for ( ; dx != 0.0D && entity.getEntityWorld().getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(dx, -1.0D, 0.0D)).isEmpty(); xspeed = dx) {
					if (dx < 0.05D && dx >= -0.05D) {
						dx = 0.0D;
					} else if (dx > 0.0D) {
						dx -= 0.05D;
					} else {
						dx += 0.05D;
					}
				}

				for (; dz != 0.0D && entity.getEntityWorld().getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(0.0D, -1.0D, dz)).isEmpty(); zpeed = dz) {
					if (dz < 0.05D && dz >= -0.05D) {
						dz = 0.0D;
					} else if (dz > 0.0D) {
						dz -= 0.05D;
					} else {
						dz += 0.05D;
					}
				}

				for (; dx != 0.0D && dz != 0.0D && entity.getEntityWorld().getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(dx, -1.0D, dz)).isEmpty(); zpeed = dz) {
					if (dx < 0.05D && dx >= -0.05D) {
						dx = 0.0D;
					} else if (dx > 0.0D) {
						dx -= 0.05D;
					} else {
						dx += 0.05D;
					}

					xspeed = dx;

					if (dz < 0.05D && dz >= -0.05D) {
						dz = 0.0D;
					} else if (dz > 0.0D) {
						dz -= 0.05D;
					} else {
						dz += 0.05D;
					}
				}
			}

			List<AxisAlignedBB> list1 = entity.getEntityWorld().getCollisionBoxes(entity, entity.getEntityBoundingBox().expand(dx, dy, dz));
			AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();
			int i = 0;

			for (int j = list1.size(); i < j; ++i) {
				dy = list1.get(i).calculateYOffset(entity.getEntityBoundingBox(), dy);
			}

			entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(0.0D, dy, 0.0D));
			boolean i_ = entity.onGround || yspeed != dy && yspeed < 0.0D;
			int j4 = 0;

			for (int k = list1.size(); j4 < k; ++j4) {
				dx = list1.get(j4).calculateXOffset(entity.getEntityBoundingBox(), dx);
			}

			entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(dx, 0.0D, 0.0D));
			j4 = 0;

			for (int k4 = list1.size(); j4 < k4; ++j4) {
				dz = list1.get(j4).calculateZOffset(entity.getEntityBoundingBox(), dz);
			}

			entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(0.0D, 0.0D, dz));

			if (entity.stepHeight > 0.0F && i_ && (xspeed != dx || zpeed != dz)) {
				double d11 = dx;
				double d7 = dy;
				double d8 = dz;
				AxisAlignedBB axisalignedbb1 = entity.getEntityBoundingBox();
				entity.setEntityBoundingBox(axisalignedbb);
				dy = (double) entity.stepHeight;
				List<AxisAlignedBB> list = entity.getEntityWorld().getCollisionBoxes(entity, entity.getEntityBoundingBox().expand(xspeed, dy, zpeed));
				AxisAlignedBB axisalignedbb2 = entity.getEntityBoundingBox();
				AxisAlignedBB axisalignedbb3 = axisalignedbb2.expand(xspeed, 0.0D, zpeed);
				double d9 = dy;
				int l = 0;

				for (int i1 = list.size(); l < i1; ++l) {
					d9 = list.get(l).calculateYOffset(axisalignedbb3, d9);
				}

				axisalignedbb2 = axisalignedbb2.offset(0.0D, d9, 0.0D);
				double d15 = xspeed;
				int j1 = 0;

				for (int k1 = list.size(); j1 < k1; ++j1) {
					d15 = list.get(j1).calculateXOffset(axisalignedbb2, d15);
				}

				axisalignedbb2 = axisalignedbb2.offset(d15, 0.0D, 0.0D);
				double d16 = zpeed;
				int l1 = 0;

				for (int i2 = list.size(); l1 < i2; ++l1) {
					d16 = list.get(l1).calculateZOffset(axisalignedbb2, d16);
				}

				axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d16);
				AxisAlignedBB axisalignedbb4 = entity.getEntityBoundingBox();
				double d17 = dy;
				int j2 = 0;

				for (int k2 = list.size(); j2 < k2; ++j2) {
					d17 = list.get(j2).calculateYOffset(axisalignedbb4, d17);
				}

				axisalignedbb4 = axisalignedbb4.offset(0.0D, d17, 0.0D);
				double d18 = xspeed;
				int l2 = 0;

				for (int i3 = list.size(); l2 < i3; ++l2) {
					d18 = list.get(l2).calculateXOffset(axisalignedbb4, d18);
				}

				axisalignedbb4 = axisalignedbb4.offset(d18, 0.0D, 0.0D);
				double d19 = zpeed;
				int j3 = 0;

				for (int k3 = list.size(); j3 < k3; ++j3) {
					d19 = list.get(j3).calculateZOffset(axisalignedbb4, d19);
				}

				axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d19);
				double d20 = d15 * d15 + d16 * d16;
				double d10 = d18 * d18 + d19 * d19;

				if (d20 > d10) {
					dx = d15;
					dz = d16;
					dy = -d9;
					entity.setEntityBoundingBox(axisalignedbb2);
				} else {
					dx = d18;
					dz = d19;
					dy = -d17;
					entity.setEntityBoundingBox(axisalignedbb4);
				}

				int l3 = 0;

				for (int i4 = list.size(); l3 < i4; ++l3) {
					dy = list.get(l3).calculateYOffset(entity.getEntityBoundingBox(), dy);
				}

				entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(0.0D, dy, 0.0D));

				if (d11 * d11 + d8 * d8 >= dx * dx + dz * dz) {
					dx = d11;
					dy = d7;
					dz = d8;
					entity.setEntityBoundingBox(axisalignedbb1);
				}
			}

			entity.resetPositionToBB();
			Class<?> entityClass = entity.getClass();
			// isCollidedHorizontally/isCollidedVertically/isCollided seem to be missing the is on some versions
			try {
				boolean collidedHorizontally = xspeed != dx || zpeed != dz;
				boolean collidedVertically = yspeed != dy;
				boolean collided = collidedHorizontally || collidedVertically;
				Field fCollidedHorizontally = entityClass.getField("isCollidedHorizontally");
				Field fCollidedVertically = entityClass.getField("isCollidedVertically");
				Field fCollided = entityClass.getField("isCollided");
				
				fCollidedHorizontally.setBoolean(entity, collidedHorizontally);
				fCollidedVertically.setBoolean(entity, collidedVertically);
				if (dy != 0) {
					entity.onGround = collidedVertically && yspeed < 0.0D;
				}
				fCollided.setBoolean(entityClass, collided);
			}
			catch (NoSuchFieldException e)
			{
				entity.collidedHorizontally = xspeed != dx || zpeed != dz;
				entity.collidedVertically = yspeed != dy;
				if (dy != 0) {
					entity.onGround = entity.collidedVertically && yspeed < 0.0D;
				}
				entity.collided = entity.collidedHorizontally || entity.collidedVertically;
			}
			catch (IllegalArgumentException|IllegalAccessException e)
			{
				e.printStackTrace();
			}

			if (xspeed != dx) {
				entity.motionX = 0.0D;
			}

			if (zpeed != dz) {
				entity.motionZ = 0.0D;
			}
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
		final WeakHashMap<Entity, Vec3d> map = getMovementMap(event.side);

		map.clear();
	}

}
