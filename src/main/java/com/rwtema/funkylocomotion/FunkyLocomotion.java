package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.blocks.*;
import com.rwtema.funkylocomotion.dispenser.FrameDispenserAcion;
import com.rwtema.funkylocomotion.dispenser.WrenchDispenserAction;
import com.rwtema.funkylocomotion.factory.FactoryRegistry;
import com.rwtema.funkylocomotion.fmp.FMPMover;
import com.rwtema.funkylocomotion.fmp.FMPStickness;
import com.rwtema.funkylocomotion.items.ItemBlockFrame;
import com.rwtema.funkylocomotion.items.ItemBlockPusher;
import com.rwtema.funkylocomotion.items.ItemWrench;
import com.rwtema.funkylocomotion.movers.MoverEventHandler;
import com.rwtema.funkylocomotion.network.FLNetwork;
import com.rwtema.funkylocomotion.proxydelegates.COFHStickiness;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = FunkyLocomotion.MODID, version = FunkyLocomotion.VERSION, dependencies = "after:ThermalExpansion;after:ThermalFoundation;required-after:Forge@[10.13.1.1217,)")
public class FunkyLocomotion {
    public static final String MODID = "funkylocomotion";
    public static final String VERSION = "1.0";

    @SidedProxy(serverSide = "com.rwtema.funkylocomotion.Proxy", clientSide = "com.rwtema.funkylocomotion.ProxyClient")
    public static Proxy proxy;

    public static final CreativeTabFrames creativeTabFrames = new CreativeTabFrames();

    public static ItemWrench wrench;
    public static final BlockStickyFrame[] frame = new BlockStickyFrame[4];
    public static BlockPusher pusher;
    public static BlockMoving moving;
    public static BlockSlider slider;



    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        LogHelper.info("Let's Move!");
        FLNetwork.init();

        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        TilePusher.maxTiles = config.get(Configuration.CATEGORY_GENERAL, "maximumBlocksPushed", 1024).getInt(1024);
        TilePusher.powerPerTile = config.get(Configuration.CATEGORY_GENERAL, "energyPerBlock", 250).getInt(250);
        Recipes.shouldAddRecipes = config.get(Configuration.CATEGORY_GENERAL, "addRecipes", true).getBoolean(true);
        Recipes.shouldAddFrameCopyResetRecipes = config.get(Configuration.CATEGORY_GENERAL, "addFrameCopyResetRecipes", true).getBoolean(true);
        if (config.hasChanged())
            config.save();

        EntityMovingEventHandler.init();
        MoverEventHandler.init();

        GameRegistry.registerBlock(frame[0] = new BlockStickyFrame(0), ItemBlockFrame.class, "frame");
        GameRegistry.registerBlock(frame[1] = new BlockStickyFrame(1), ItemBlockFrame.class, "frame2");
        GameRegistry.registerBlock(frame[2] = new BlockStickyFrame(2), ItemBlockFrame.class, "frame3");
        GameRegistry.registerBlock(frame[3] = new BlockStickyFrame(3), ItemBlockFrame.class, "frame4");
        GameRegistry.registerBlock(moving = new BlockMoving(), "moving");
        GameRegistry.registerBlock(pusher = new BlockPusher(), ItemBlockPusher.class, "pusher");
        GameRegistry.registerBlock(slider = new BlockSlider(), "slider");
        GameRegistry.registerItem(wrench = new ItemWrench(), "wrench");
        GameRegistry.registerTileEntity(TileMovingServer.class, "funkylocomotion:tileMover");
        GameRegistry.registerTileEntity(TilePusher.class, "funkylocomotion:tilePusher");
        GameRegistry.registerTileEntity(TileSlider.class, "funkylocomotion:tileSlider");

        proxy.registerRendering();

        for (Block f : frame)
            BlockDispenser.dispenseBehaviorRegistry.putObject(Item.getItemFromBlock(f), new FrameDispenserAcion());
        BlockDispenser.dispenseBehaviorRegistry.putObject(wrench, new WrenchDispenserAction());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (Loader.isModLoaded("ForgeMultipart")) {
            Block b = (Block) Block.blockRegistry.getObject("ForgeMultipart:block");
            FactoryRegistry.moveFactoryMapBlock.put(b, new FMPMover());
            FMPStickness.init(b);
        }

        try{
            Class.forName("cofh.api.block.IBlockAppearance");
            COFHStickiness.register();
        } catch (ClassNotFoundException ignore) {

        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Recipes.addRecipes();
    }


    public void handleIMC(FMLInterModComms.IMCEvent event) {
        for (FMLInterModComms.IMCMessage msg : event.getMessages()) {
            if ("blacklist".equals(msg.key) && msg.isStringMessage()) {
                String s = msg.getStringValue();
                //TODO
            }
        }
    }


}
