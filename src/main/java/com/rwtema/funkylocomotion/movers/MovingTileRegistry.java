package com.rwtema.funkylocomotion.movers;

import com.google.common.collect.Lists;
import com.rwtema.funkylocomotion.blocks.TileMovingServer;

import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

public class MovingTileRegistry {
    private static final Object BLANK_ENTRY = new Object();
    public static WeakHashMap<TileMovingServer, Object> map = new WeakHashMap<TileMovingServer, Object>();

    public static void register(TileMovingServer moving) {
        map.put(moving, BLANK_ENTRY);
    }

    public static void deregister(TileMovingServer moving) {
        map.remove(moving);
    }

    public static List<TileMovingServer> getTilesFinishedMoving() {
        List<TileMovingServer> list = Lists.newArrayList();

        Iterator<TileMovingServer> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            TileMovingServer tile = iterator.next();
            if (tile.isInvalid())
                iterator.remove();
            else if (tile.hasWorldObj() && tile.time >= tile.maxTime
                    && tile.getWorldObj().blockExists(tile.xCoord, tile.yCoord, tile.zCoord) // ensure the tile isn't in mid-chunk-load
                    )
                list.add(tile);
        }

        return list;
    }
}
