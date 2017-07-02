package com.rwtema.funkylocomotion.items;

import com.rwtema.funkylocomotion.asm.WrenchFactory;
import com.rwtema.funkylocomotion.blocks.FLBlocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class FLItems
{
	@GameRegistry.ObjectHolder("funkylocomotion:wrench")
	public static final ItemWrench WRENCH = null;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();

		registry.register(new ItemBlock(FLBlocks.BOOSTER).setRegistryName(FLBlocks.BOOSTER.getRegistryName()));
		registry.register(new ItemBlock(FLBlocks.FRAME_PROJECTOR).setRegistryName(FLBlocks.FRAME_PROJECTOR.getRegistryName()));
		registry.register(new ItemBlockPusher(FLBlocks.PUSHER).setRegistryName(FLBlocks.PUSHER.getRegistryName()));
		registry.register(new ItemBlock(FLBlocks.SLIDER).setRegistryName(FLBlocks.SLIDER.getRegistryName()));
		registry.register(new ItemBlockTeleporter(FLBlocks.TELEPORTER).setRegistryName(FLBlocks.TELEPORTER.getRegistryName()));

		for (int i = 0; i < 4; i++) {
			registry.register(new ItemBlockFrame(FLBlocks.FRAMES[i]).setRegistryName(FLBlocks.FRAMES[i].getRegistryName()));
		}

		registry.register(WrenchFactory.makeMeAWrench());
	}
}
