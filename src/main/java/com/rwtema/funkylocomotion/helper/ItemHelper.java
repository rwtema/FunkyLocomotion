package com.rwtema.funkylocomotion.helper;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ItemHelper {


	public static final String[] wrenchClassNames = new String[]{
			"buildcraft.api.tools.IToolWrench",
			"cofh.api.item.IToolHammer",
			"powercrystals.minefactoryreloaded.api.IMFRHammer",
			"appeng.api.implementations.items.IAEWrench",
			"crazypants.enderio.api.tool.ITool"
	};

	public static final Class<?>[] wrenchClasses;

	static {
		String[] wrenchInterfaces = wrenchClassNames;
		wrenchClasses = new Class[wrenchInterfaces.length];
		for (int i = 0; i < wrenchClasses.length; i++) {
			try {
				wrenchClasses[i] = Class.forName(wrenchInterfaces[i]);
			} catch (ClassNotFoundException ignore) {
				wrenchClasses[i] = null;
			}

		}
	}


	public static boolean isWrench(ItemStack item) {
		if (item == null || item.getItem() == null)
			return false;

		if (item.getItem() == Items.STICK || item.getItem() == FunkyLocomotion.wrench)
			return true;

		for (Class<?> c : wrenchClasses) {
			if (c != null && c.isAssignableFrom(item.getItem().getClass()))
				return true;
		}

		return false;
	}
}
