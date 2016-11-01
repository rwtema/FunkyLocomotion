package com.rwtema.funkylocomotion.movepermissions;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class MoveCheckReflector implements IMoveChecker {

	private static final HashMap<Class<?>, Boolean> cache = new HashMap<Class<?>, Boolean>();

	public static boolean canMoveClass(Class<?> clazz) {
		Boolean b = cache.get(clazz);
		if (b == null) {
			b = _canMoveClass(clazz);
			cache.put(clazz, b);
		}
		return b;
	}

	private static boolean _canMoveClass(Class<?> clazz) {
		try {
			Method method = clazz.getMethod("_Immovable");
			if (Modifier.isStatic(method.getModifiers()) &&
					Modifier.isPublic(method.getModifiers()))
				if (method.getReturnType() == boolean.class) {
					Boolean b = (Boolean) method.invoke(null);
					return b == null || !b;
				}
			return true;
		} catch (NoSuchMethodException e) {
			return true;
		} catch (InvocationTargetException e) {
			return true;
		} catch (IllegalAccessException e) {
			return true;
		} catch (RuntimeException e) {
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return true;
		}
	}


	@Override
	public boolean preventMovement(World world, int x, int y, int z, Block block, int meta, TileEntity tile) {
		return !(canMoveClass(block.getClass()) && (tile == null || canMoveClass(tile.getClass())));
	}
}
