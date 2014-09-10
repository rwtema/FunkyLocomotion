package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.blocks.BlockMoving;
import com.rwtema.funkylocomotion.blocks.BlockPusher;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import com.rwtema.funkylocomotion.blocks.TileMoving;
import com.rwtema.funkylocomotion.factory.FMPMover;
import com.rwtema.funkylocomotion.factory.FactoryRegistry;
import com.rwtema.funkylocomotion.items.ItemBlockFrame;
import com.rwtema.funkylocomotion.items.ItemBlockPusher;
import com.rwtema.funkylocomotion.items.ItemWrench;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

@Mod(modid = FunkyLocomotion.MODID, version = FunkyLocomotion.VERSION)
public class FunkyLocomotion {
    public static final String MODID = "funkylocomotion";
    public static final String VERSION = "1.0";

    @SidedProxy(serverSide = "com.rwtema.funkylocomotion.Proxy", clientSide = "com.rwtema.funkylocomotion.ProxyClient")
    public static Proxy proxy;

    public static CreativeTabFrames creativeTabFrames = new CreativeTabFrames();

    public static ItemWrench wrench;

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        LogHelper.info("Let's Move!");
//        FMLCommonHandler.instance().bus().register(new DebugEventHandler());


        GameRegistry.registerBlock(new BlockStickyFrame(0), ItemBlockFrame.class, "frame");
        GameRegistry.registerBlock(new BlockStickyFrame(1), ItemBlockFrame.class, "frame2");
        GameRegistry.registerBlock(new BlockStickyFrame(2), ItemBlockFrame.class, "frame3");
        GameRegistry.registerBlock(new BlockStickyFrame(3), ItemBlockFrame.class, "frame4");
        GameRegistry.registerBlock(new BlockMoving(), "moving");
        GameRegistry.registerBlock(new BlockPusher(), ItemBlockPusher.class, "pusher");
        GameRegistry.registerItem(wrench = new ItemWrench(), "wrench");
        GameRegistry.registerTileEntity(TileMoving.class, "funkylocomotion:tileMover");

        proxy.registerRendering();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("ForgeMultipart")) {
            Block b = (Block) Block.blockRegistry.getObject("ForgeMultipart:block");
            FactoryRegistry.moveFactoryMapBlock.put(b, new FMPMover());
        }
    }


    public void handleIMC(FMLInterModComms.IMCEvent event) {
        for (FMLInterModComms.IMCMessage msg : event.getMessages()) {
            if ("blacklist".equals(msg.key) && msg.isStringMessage()) {
                String s = msg.getStringValue();

            }
        }
    }


}
