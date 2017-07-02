package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.api.FunkyRegistry;
import com.rwtema.funkylocomotion.api.IMoveCheck;
import com.rwtema.funkylocomotion.blocks.TilePusher;
import com.rwtema.funkylocomotion.compat.CompatHandler;
import com.rwtema.funkylocomotion.compat.FunkyRegistryImpl;
import com.rwtema.funkylocomotion.movers.MoverEventHandler;
import com.rwtema.funkylocomotion.network.FLNetwork;
import com.rwtema.funkylocomotion.proxydelegates.ProxyRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = FunkyLocomotion.MODID, version = FunkyLocomotion.VERSION)
public class FunkyLocomotion {
	public static final String MODID = "funkylocomotion";
	public static final String VERSION = "1.0";
	public static final CreativeTabFrames creativeTabFrames = new CreativeTabFrames();
	@SidedProxy(serverSide = "com.rwtema.funkylocomotion.Proxy", clientSide = "com.rwtema.funkylocomotion.ProxyClient")
	public static Proxy proxy;
	public static boolean redrawChunksInstantly;

	static {
		FunkyRegistry.INSTANCE = new FunkyRegistryImpl();
	}

	final IMoveCheck BLACKLIST = (worldObj, pos, profile) -> EnumActionResult.FAIL;

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

		CompatHandler.initCompat(event.getAsmData());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {

		try {
			Class.forName("cofh.api.block.IBlockAppearance");

		} catch (ClassNotFoundException ignore) {

		}
	}

	@EventHandler
	public void handleIMC(FMLInterModComms.IMCEvent event) {
		for (FMLInterModComms.IMCMessage msg : event.getMessages()) {
			if ("blacklist".equals(msg.key) && msg.isStringMessage()) {
				String s = msg.getStringValue();
				ResourceLocation location = new ResourceLocation(s);
				Block object = Block.REGISTRY.getObject(location);
				if (object != Blocks.AIR) {
					ProxyRegistry.register(object, IMoveCheck.class, BLACKLIST);
				} else {
					try {
						Class<?> aClass = Class.forName(s);
						ProxyRegistry.register(aClass, IMoveCheck.class, BLACKLIST);
					} catch (ClassNotFoundException ignore) {

					}
				}
			}
		}
	}

}
