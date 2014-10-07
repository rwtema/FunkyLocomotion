package com.rwtema.funkylocomotion.factory;

import com.rwtema.funkylocomotion.helper.BlockHelper;
import framesapi.BlockPos;
import framesapi.IMoveFactory;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class FactoryRegistry {
    public static final Map<Block, IMoveFactory> moveFactoryMapBlock = new HashMap<Block, IMoveFactory>();
    public static final Map<Class<? extends Block>, IMoveFactory> moveFactoryMapBlockClass = new HashMap<Class<? extends Block>, IMoveFactory>();
    private static final DefaultMoveFactory defaultFactory = new DefaultMoveFactory();

    public static IMoveFactory getDefaultFactory() {
        return defaultFactory;
    }

    public static IMoveFactory getFactory(Block b) {
        if (b instanceof IMoveFactory)
            return (IMoveFactory) b;

        IMoveFactory f;
        f = moveFactoryMapBlock.get(b);
        if (f != null)
            return (f);

        f = moveFactoryMapBlockClass.get(b.getClass());
        if (f != null)
            return (f);

        return defaultFactory;
    }

    public static IMoveFactory getFactory(World world, BlockPos pos) {
        Block b = BlockHelper.getBlock(world, pos);
        return getFactory(b);
    }


}
