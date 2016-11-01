package com.rwtema.funkylocomotion.movers;

import com.google.common.collect.Lists;
import com.rwtema.funkylocomotion.blocks.TileMovingServer;
import com.rwtema.funkylocomotion.helper.WeakSet;

import java.util.Iterator;
import java.util.List;

public class MovingTileRegistry {

	public static final WeakSet<TileMovingServer> movingTilesSet = new WeakSet<>();

	public static void register(TileMovingServer moving) {
		movingTilesSet.add(moving);
	}

	public static void deregister(TileMovingServer moving) {
		movingTilesSet.remove(moving);
	}

	public static List<TileMovingServer> getTilesFinishedMoving() {
		List<TileMovingServer> list = Lists.newArrayList();

		Iterator<TileMovingServer> iterator = movingTilesSet.iterator();
		while (iterator.hasNext()) {
			TileMovingServer tile = iterator.next();
			if (tile.isInvalid())
				iterator.remove();
			else if (tile.hasWorldObj() && tile.time >= tile.maxTime
					&& tile.getWorld().isBlockLoaded(tile.getPos()) // ensure the tile isn't in mid-chunk-load
					)
				list.add(tile);
		}

		return list;
	}
}
