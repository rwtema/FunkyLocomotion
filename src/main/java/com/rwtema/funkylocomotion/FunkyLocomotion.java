package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.asm.WrenchFactory;
import com.rwtema.funkylocomotion.blocks.*;
import com.rwtema.funkylocomotion.items.ItemBlockFrame;
import com.rwtema.funkylocomotion.items.ItemBlockPusher;
import com.rwtema.funkylocomotion.items.ItemBlockTeleporter;
import com.rwtema.funkylocomotion.items.ItemWrench;
import com.rwtema.funkylocomotion.movers.MoverEventHandler;
import com.rwtema.funkylocomotion.network.FLNetwork;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = FunkyLocomotion.MODID, version = FunkyLocomotion.VERSION, dependencies = "after:ThermalExpansion;after:ThermalFoundation;required-after:Forge@[10.13.1.1217,)")
public class FunkyLocomotion {
	public static final String MODID = "funkylocomotion";
	public static final String VERSION = "1.0";
	public static final CreativeTabFrames creativeTabFrames = new CreativeTabFrames();
	public static final BlockStickyFrame[] frame = new BlockStickyFrame[4];
	@SidedProxy(serverSide = "com.rwtema.funkylocomotion.Proxy", clientSide = "com.rwtema.funkylocomotion.ProxyClient")
	public static Proxy proxy;
	public static ItemWrench wrench;
	public static BlockPusher pusher;
	public static BlockMoving moving;
	public static BlockSlider slider;
	public static BlockBooster booster;
	public static BlockTeleport teleporter;
	public static boolean redrawChunksInstantly;


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
		redrawChunksInstantly = config.get("client", "redrawChunksInstantly", true).getBoolean(true);
		if (config.hasChanged())
			config.save();

		EntityMovingEventHandler.init();
		MoverEventHandler.init();

		for (int i = 0; i < 4; i++) {
			BlockStickyFrame.curLoadingIndex = i;
			GameRegistry.register(frame[i] = new BlockStickyFrame());
			GameRegistry.register(new ItemBlockFrame(frame[i]).setRegistryName(frame[i].getRegistryName()));
		}
		GameRegistry.register(moving = new BlockMoving());
		pusher = new BlockPusher();
		pusher.setUnlocalizedName("funkylocomotion:pusher");
		pusher.setRegistryName("funkylocomotion:pusher");
		GameRegistry.register(pusher);
		GameRegistry.register(new ItemBlockPusher(pusher).setRegistryName(pusher.getRegistryName()));
		GameRegistry.registerBlock(slider = new BlockSlider());
		GameRegistry.register(teleporter = new BlockTeleport());
		GameRegistry.register(new ItemBlockTeleporter(teleporter).setRegistryName(teleporter.getRegistryName()));
		GameRegistry.registerBlock(booster = new BlockBooster());

		GameRegistry.register(wrench = WrenchFactory.makeMeAWrench());

		GameRegistry.registerTileEntity(TileMovingServer.class, "funkylocomotion:tileMover");
		GameRegistry.registerTileEntity(TilePusher.class, "funkylocomotion:tilePusher");
		GameRegistry.registerTileEntity(TileSlider.class, "funkylocomotion:tileSlider");
		GameRegistry.registerTileEntity(TileBooster.class, "funkylocomotion:tileBooster");
		GameRegistry.registerTileEntity(TileTeleport.class, "funkylocomotion:tileTeleporter");

		proxy.registerRendering();

//		for (Block f : frame)
//			BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(f), new FrameDispenserAcion());
//		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(wrench, new WrenchDispenserAction());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {

		try {
			Class.forName("cofh.api.block.IBlockAppearance");

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
