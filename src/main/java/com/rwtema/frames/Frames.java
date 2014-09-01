package com.rwtema.frames;

import com.rwtema.frames.blocks.*;
import com.rwtema.frames.rendering.TileEntityRenderMoving;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = Frames.MODID, version = Frames.VERSION)
public class Frames {
    public static final String MODID = "newframes";
    public static final String VERSION = "1.0";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerBlock(new BlockFrame(), "frame");
        GameRegistry.registerBlock(new BlockMoving(), "moving");
        GameRegistry.registerBlock(new BlockPusher(), "pusher");
        GameRegistry.registerTileEntity(TileMoving.class, "newframes:tileMover");

        ClientRegistry.bindTileEntitySpecialRenderer(TileMovingClient.class, new TileEntityRenderMoving());
    }
}
