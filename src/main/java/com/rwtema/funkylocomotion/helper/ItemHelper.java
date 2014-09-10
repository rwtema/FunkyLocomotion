package com.rwtema.funkylocomotion.helper;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ItemHelper {
    public static final String[] wrenchInterfaces = {"cofh.api.item.IToolHammer", "mods.railcraft.api.core.items.IToolCrowbar", "buildcraft.api.tools.IToolWrench"};
    public static final Class<?>[] wrenchClasses;

    static {
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

        if (item.getItem() == Items.stick || item.getItem() == FunkyLocomotion.wrench)
            return true;

        for (Class<?> c : wrenchClasses) {
            if (c != null && c.isAssignableFrom(item.getItem().getClass()))
                return true;
        }

        return false;
    }
}
