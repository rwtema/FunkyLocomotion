package com.rwtema.funkylocomotion.network;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class FLNetwork {
    public static SimpleNetworkWrapper net;

    public static void init() {
        net = new SimpleNetworkWrapper("FLoco");
        net.registerMessage(MessageMoveBlock.Handler.class, MessageMoveBlock.class, 0, Side.SERVER);
        net.registerMessage(MessageMoveBlock.Handler.class, MessageMoveBlock.class, 0, Side.CLIENT);
    }
}
