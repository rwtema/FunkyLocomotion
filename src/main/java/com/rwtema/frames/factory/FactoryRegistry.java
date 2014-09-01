package com.rwtema.frames.factory;

import framesapi.BlockPos;
import framesapi.IMoveFactory;
import net.minecraft.world.World;

public class FactoryRegistry {
    public static IMoveFactory getDefaultFactory() {
        return defaultFactory;
    }

    private static DefaultMoveFactory defaultFactory = new DefaultMoveFactory();

    public static IMoveFactory getFactory(String id) {
        return defaultFactory;
    }

    public static IMoveFactory getFactory(World world, BlockPos pos) {
        return defaultFactory;
    }
}
