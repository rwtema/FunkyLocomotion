package com.rwtema.funkylocomotion.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.WeakHashMap;

public class FLNetwork {
    public static SimpleNetworkWrapper net;
    private static final WeakHashMap<World, PlayerManager> cache = new WeakHashMap<World, PlayerManager>();

    public static void init() {
        net = new SimpleNetworkWrapper("FLoco");
        net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.SERVER);
        net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.CLIENT);
    }

    public static void sendToAllWatchingChunk(World world, int x, int y, int z, IMessage message) {

        if (!cache.containsKey(world)) {
            if (!(world instanceof WorldServer)) {
                cache.put(world, null);
            } else
                cache.put(world, ((WorldServer) world).getPlayerManager());
        }

        PlayerManager playerManager = cache.get(world);
        if (playerManager == null)
            return;

        PlayerManager.PlayerInstance watcher = playerManager.getOrCreateChunkWatcher(x >> 4, z >> 4, false);
        if (watcher != null)
            watcher.sendToAllPlayersWatchingChunk(net.getPacketFrom(message));
    }
}
