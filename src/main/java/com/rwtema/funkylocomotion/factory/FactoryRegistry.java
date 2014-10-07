package com.rwtema.funkylocomotion.factory;

import com.rwtema.funkylocomotion.helper.BlockHelper;
import framesapi.BlockPos;
import framesapi.IMoveFactory;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class FactoryRegistry {
    public static final Map<Block, IMoveFactory> moveFactoryMapBlock = new HashMap<Block, IMoveFactory>();
    public static final Map<Class<? extends Block>, IMoveFactory> moveFactoryMapBlockClass = new HashMap<Class<? extends Block>, IMoveFactory>();
    public static final Map<Class<?>, IMoveFactory> moveFactoryMapInheritanceClass = new HashMap<Class<?>, IMoveFactory>();
    private static final DefaultMoveFactory defaultFactory = new DefaultMoveFactory();
    public static final ChunkUnloadFactory chunkUnloadFactory = new ChunkUnloadFactory();

    public static IMoveFactory getDefaultFactory() {
        return defaultFactory;
    }

    public static IMoveFactory getFactory(TileEntity tile) {
        if (tile == null)
            return null;

        if (tile instanceof IMoveFactory)
            return (IMoveFactory) tile;

        for (Map.Entry<Class<?>, IMoveFactory> clazz : moveFactoryMapInheritanceClass.entrySet()) {
            if (clazz.getKey().isAssignableFrom(tile.getClass()))
                return clazz.getValue();
        }

        return null;
    }

    public static IMoveFactory getFactory(Block b) {
        if (b instanceof IMoveFactory)
            return (IMoveFactory) b;

        IMoveFactory f = moveFactoryMapBlock.get(b);
        if (f != null)
            return (f);

        f = moveFactoryMapBlockClass.get(b.getClass());
        if (f != null)
            return (f);

        return getDefaultFactory();
    }

    public static IMoveFactory getFactory(World world, BlockPos pos) {
        IMoveFactory f = getFactory(BlockHelper.getTile(world, pos));
        if (f != null) return f;
        return getFactory(BlockHelper.getBlock(world, pos));
    }
}
