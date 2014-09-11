package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.blocks.*;
import com.rwtema.funkylocomotion.factory.FactoryRegistry;
import com.rwtema.funkylocomotion.fmp.FMPMover;
import com.rwtema.funkylocomotion.fmp.FMPStickness;
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

@Mod(modid = FunkyLocomotion.MODID, version = FunkyLocomotion.VERSION)
public class FunkyLocomotion {
    public static final String MODID = "funkylocomotion";
    public static final String VERSION = "1.0";

    @SidedProxy(serverSide = "com.rwtema.funkylocomotion.Proxy", clientSide = "com.rwtema.funkylocomotion.ProxyClient")
    public static Proxy proxy;

    public static CreativeTabFrames creativeTabFrames = new CreativeTabFrames();

    public static ItemWrench wrench;
    public static BlockStickyFrame[] frame = new BlockStickyFrame[4];
    public static BlockPusher pusher;

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        LogHelper.info("Let's Move!");
//        FMLCommonHandler.instance().bus().register(new DebugEventHandler());

        GameRegistry.registerBlock(frame[0] = new BlockStickyFrame(0), ItemBlockFrame.class, "frame");
        GameRegistry.registerBlock(frame[1] = new BlockStickyFrame(1), ItemBlockFrame.class, "frame2");
        GameRegistry.registerBlock(frame[2] = new BlockStickyFrame(2), ItemBlockFrame.class, "frame3");
        GameRegistry.registerBlock(frame[3] = new BlockStickyFrame(3), ItemBlockFrame.class, "frame4");
        GameRegistry.registerBlock(new BlockMoving(), "moving");
        GameRegistry.registerBlock(pusher = new BlockPusher(), ItemBlockPusher.class, "pusher");
        GameRegistry.registerItem(wrench = new ItemWrench(), "wrench");
        GameRegistry.registerTileEntity(TileMoving.class, "funkylocomotion:tileMover");
        GameRegistry.registerTileEntity(TilePusher.class, "funkylocomotion:tilePusher");

        proxy.registerRendering();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Recipes.addRecipes();
        if (Loader.isModLoaded("ForgeMultipart")) {
            Block b = (Block) Block.blockRegistry.getObject("ForgeMultipart:block");
            FactoryRegistry.moveFactoryMapBlock.put(b, new FMPMover());
            FMPStickness.init(b);
//            DescriptorRegistry.register(new FMPDescriber(), true);
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
