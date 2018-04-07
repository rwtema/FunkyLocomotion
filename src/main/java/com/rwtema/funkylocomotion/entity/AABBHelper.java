package com.rwtema.funkylocomotion.entity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

public class AABBHelper {

	@Nonnull
	public static AxisAlignedBB transform(AxisAlignedBB bb, Function<Vec3d, Vec3d> function) {
		double x0 = Double.POSITIVE_INFINITY, y0 = Double.POSITIVE_INFINITY, z0 = Double.POSITIVE_INFINITY;
		double x1 = Double.NEGATIVE_INFINITY, y1 = Double.NEGATIVE_INFINITY, z1 = Double.NEGATIVE_INFINITY;

		for (byte a = 0; a <= 1; a++) {
			double x = a == 0 ? bb.minX : bb.maxX;
			for (byte b = 0; b <= 1; b++) {
				double y = b == 0 ? bb.minY : bb.maxY;
				for (byte c = 0; c <= 1; c++) {
					double z = c == 0 ? bb.minZ : bb.maxZ;

					Vec3d v = new Vec3d(x, y, z);
					Vec3d apply = function.apply(v);
					x0 = Math.min(x0, apply.x);
					x1 = Math.max(x1, apply.x);
					y0 = Math.min(y0, apply.y);
					y1 = Math.max(y1, apply.y);
					z0 = Math.min(z0, apply.z);
					z1 = Math.max(z1, apply.z);

				}
			}
		}
		return new AxisAlignedBB(closeRound(x0), closeRound(y0), closeRound(z0),
				closeRound(x1), closeRound(y1), closeRound(z1));
	}

	public static double closeRound(double t) {
		int tr = (int) Math.round(t);
		if (Math.abs(t - tr) < 1 / 512F) {
			return tr;
		}
		return t;
	}


	public AxisAlignedBB moveAndRotate(AxisAlignedBB bounds) {
		return null;
	}

	public static Vec3d rotate(Vec3d v, double yaw, double pitch, double roll) {
		Vec3d r = v;
		r = rotate(r, EnumFacing.Axis.Z, roll);
		r = rotate(r, EnumFacing.Axis.X, pitch);
		r = rotate(r, EnumFacing.Axis.Y, yaw);
		return r;
	}

	public static Vec3d rotate(Vec3d v, EnumFacing.Axis axis, double amount) {
		if (amount == 0) return v;
		double c = Math.cos(amount);
		double s = Math.sin(amount);
		switch (axis) {
			case X:
				return new Vec3d(
						v.x,
						c * v.y - s * v.z,
						s * v.y + c * v.z
				);
			case Y:
				return new Vec3d(
						c * v.x + s * v.z,
						v.y,
						-s * v.x + c * v.z
				);
			case Z:
				return new Vec3d(
						c * v.x - s * v.y,
						s * v.x + c * v.y,
						v.z
				);
			default:
				throw new IllegalArgumentException(Objects.toString(axis));
		}
	}


	public static double volume(AxisAlignedBB aabb){
		return (aabb.maxX - aabb.minX) * (aabb.maxY - aabb.minY) * (aabb.maxZ - aabb.minZ) ;
	}
}
