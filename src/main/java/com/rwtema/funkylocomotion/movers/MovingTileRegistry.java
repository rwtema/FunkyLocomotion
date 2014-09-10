package com.rwtema.funkylocomotion.movers;

import com.google.common.collect.Lists;
import com.rwtema.funkylocomotion.blocks.TileMoving;

import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

public class MovingTileRegistry {
    private static final Object BLANK_ENTRY = new Object();
    public static WeakHashMap<TileMoving, Object> map = new WeakHashMap<TileMoving, Object>();

    public static void register(TileMoving moving) {
        map.put(moving, BLANK_ENTRY);
    }

    public static void deregister(TileMoving moving) {
        map.remove(moving);
    }

    public static List<TileMoving> getTilesFinishedMoving() {
        List<TileMoving> list = Lists.newArrayList();

        Iterator<TileMoving> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            TileMoving tile = iterator.next();
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
