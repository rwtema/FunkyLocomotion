package com.rwtema.frames;

import com.rwtema.frames.blocks.*;
import com.rwtema.frames.debug.DebugEventHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = Frames.MODID, version = Frames.VERSION)
public class Frames {
    public static final String MODID = "newframes";
    public static final String VERSION = "1.0";

    @SidedProxy(serverSide = "com.rwtema.frames.Proxy", clientSide = "com.rwtema.frames.ProxyClient")
    public static Proxy proxy;

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        LogHelper.info("Let's Move!");
//        FMLCommonHandler.instance().bus().register(new DebugEventHandler());

        GameRegistry.registerBlock(new BlockStickyFrame(0), "frame");
        GameRegistry.registerBlock(new BlockStickyFrame(1), "frame2");
        GameRegistry.registerBlock(new BlockStickyFrame(2), "frame3");
        GameRegistry.registerBlock(new BlockStickyFrame(3), "frame4");
        GameRegistry.registerBlock(new BlockMoving(), "moving");
        GameRegistry.registerBlock(new BlockPusher(), "pusher");
        GameRegistry.registerTileEntity(TileMoving.class, "newframes:tileMover");

        proxy.registerRendering();
    }
}
