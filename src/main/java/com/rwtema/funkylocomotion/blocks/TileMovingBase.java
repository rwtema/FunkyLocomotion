package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.EntityMovingEventHandler;
import com.rwtema.funkylocomotion.Proxy;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TileMovingBase extends TileEntity implements ITickable {
	private static final AxisAlignedBB[] blank = new AxisAlignedBB[0];
	public final Side side;
	public AxisAlignedBB[] collisions = blank;
	public boolean isAir = true;
	public int time = 0;
	public int maxTime = 0;
	public NBTTagCompound block;
	public NBTTagCompound desc;
	public int dir = -1;
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
		return new AxisAlignedBB(
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
		dir = tag.getByte("Dir");

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
	@Nonnull
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if (block != null)
			tag.setTag("BlockTag", block);
		if (desc != null)
			tag.setTag("DescTag", desc);
		tag.setInteger("Time", time);
		tag.setInteger("MaxTime", maxTime);
		tag.setByte("Dir", (byte) dir);

		if (collisions.length > 0)
			tag.setTag("Collisions", TagsAxis(collisions));

		if (lightLevel != 0) tag.setByte("Light", (byte) lightLevel);
		if (lightOpacity != 0) tag.setShort("Opacity", (short) lightOpacity);

		if (scheduledTickTime != -1) {
			tag.setInteger("TickTime", scheduledTickTime);
			tag.setInteger("TickPriority", scheduledTickPriority);
		}
		return tag;
	}

	@Nullable
	public EnumFacing getDir() {
		if (dir < 0 || dir >= 6)
			return null;

		return EnumFacing.values()[this.dir];
	}

	public Vec3d getMovVec() {
		double d = 1.0 / maxTime;
		if (dir < 0 || dir >= 6)
			return Vec3d.ZERO;

		EnumFacing dir = EnumFacing.values()[this.dir];
		return new Vec3d(dir.getFrontOffsetX() * d, dir.getFrontOffsetY() * d, dir.getFrontOffsetZ() * d);
	}

	@Override
	public void update() {
		if (maxTime == 0)
			return;

		if (getWorld().isRemote) {
			time = time + 1 - 1;
		}

		Vec3d mov = getMovVec();

		Set<Entity> entityList = new HashSet<>();

		time++;

		for (AxisAlignedBB bb : getTransformedColisions()) {
			List<Entity> entities = getWorld().getEntitiesWithinAABB(Entity.class, bb.expand(0, 0.1, 0));
			for (Entity entity : entities) {
				entityList.add(entity);
			}
		}

		for (Entity a : entityList) {
			if (!a.isDead) {
				Map<Entity, Vec3d> map = EntityMovingEventHandler.getMovementMap(side);
				if (!map.containsKey(a)) {
					for (AxisAlignedBB bb : getTransformedColisions()) {
						AxisAlignedBB boundingBox = a.getEntityBoundingBox();
						if (boundingBox.intersectsWith(bb)) {
							if (boundingBox.minY > bb.maxY - 0.2) {
								a.setEntityBoundingBox(boundingBox.offset(0, bb.maxY - boundingBox.minY, 0));
							}
						} else if (dir == 0 && a.motionY <= 0 && boundingBox.intersectsWith(bb.offset(0, 0.2, 0))) {
							a.setEntityBoundingBox(boundingBox.offset(0, bb.maxY - boundingBox.minY, 0));
						}
					}

					EntityMovingEventHandler.moveEntity(a, mov.xCoord, mov.yCoord, mov.zCoord);

					map.put(a, null);
				}

			}
		}
	}

	public AxisAlignedBB getCombinedCollisions(boolean renderOffset, boolean shrink) {
		if (isAir) return Block.NULL_AABB;

		AxisAlignedBB bb = null;
		for (AxisAlignedBB collision : collisions) {
			if (bb == null) {
				bb = collision;
			} else
				bb = bb.union(collision);
		}

		EnumFacing dir = this.getDir();
		if (dir != null) {
			TileEntity other = getWorld().getTileEntity(pos.offset(dir));
			if (other instanceof TileMovingBase) {
				AxisAlignedBB[] bbs1 = ((TileMovingBase) other).collisions;
				for (AxisAlignedBB bb1 : bbs1) {
					if (bb == null)
						bb = bb1.offset(dir.getFrontOffsetX(), dir.getFrontOffsetY(), dir.getFrontOffsetZ());
					else
						bb = bb.union(bb1.offset(dir.getFrontOffsetX(), dir.getFrontOffsetY(), dir.getFrontOffsetZ()));
				}
			}
		}

		if (bb == null)
			return null;

		double h = offset(renderOffset);
		if (dir != null) {
			bb = bb.offset(h * dir.getFrontOffsetX(), h * dir.getFrontOffsetY(), h * dir.getFrontOffsetZ());
		} else if (shrink) {
			double mult = this.dir == 6 ? h + 1 : -h;
			bb = new AxisAlignedBB(
					0.5 + mult * (bb.minX - 0.5),
					0.5 + mult * (bb.minY - 0.5),
					0.5 + mult * (bb.minZ - 0.5),
					0.5 + mult * (bb.maxX - 0.5),
					0.5 + mult * (bb.maxY - 0.5),
					0.5 + mult * (bb.maxZ - 0.5));
		}

		return bb;
	}

	public AxisAlignedBB[] getTransformedColisions() {
		double h = offset(false);
		EnumFacing dir = getDir();
		if (dir != null && h != 0) {
			AxisAlignedBB[] tbbs = new AxisAlignedBB[collisions.length];
			for (int i = 0; i < collisions.length; i++) {
				tbbs[i] = collisions[i].offset(h * dir.getFrontOffsetX(), h * dir.getFrontOffsetY(), h * dir.getFrontOffsetZ()).offset(pos);
			}
			return tbbs;
		} else {
			return collisions;
		}
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


	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}


}
