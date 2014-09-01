package com.rwtema.frames.helper;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.world.chunk.Chunk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BlockReflectionHelper {
    private static Method relight = ReflectionHelper.findMethod(Chunk.class, null, new String[]{"relightBlock", "func_76615_h"}, int.class, int.class, int.class);
    private static Method propogateSkyLight = ReflectionHelper.findMethod(Chunk.class, null, new String[]{"propagateSkylightOcclusion", "func_76595_e"}, int.class, int.class);

    private static void silentInvoke(Method m, Object o, Object... params) {
        try {
            m.invoke(o, params);
        } catch (IllegalAccessException ignore) {

        } catch (InvocationTargetException ignore) {

        }
    }

    public static void relightChunk(Chunk c, int x, int y, int z) {
        silentInvoke(relight, c, x, y, z);
    }

    public static void propogateSkylightChunk(Chunk c, int x, int z) {
        silentInvoke(propogateSkyLight, c, x, z);
    }


}
