package com.rwtema.funkylocomotion.items;

import com.rwtema.funkylocomotion.asm.WrenchFactory;
import com.rwtema.funkylocomotion.blocks.FLBlocks;
import com.rwtema.funkylocomotion.helper.NullHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber
public class FLItems {
	@GameRegistry.ObjectHolder("funkylocomotion:wrench")
	public static final ItemWrench WRENCH = NullHelper.notNull();

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();

		registry.register(generateItemBlock(FLBlocks.BOOSTER));
		registry.register(generateItemBlock(FLBlocks.FRAME_PROJECTOR));
		registry.register(new ItemBlockPusher(FLBlocks.PUSHER).setRegistryName(Validate.notNull(FLBlocks.PUSHER.getRegistryName())));
		registry.register(generateItemBlock(FLBlocks.SLIDER));
		registry.register(new ItemBlockTeleporter(FLBlocks.TELEPORTER).setRegistryName(Validate.notNull(FLBlocks.TELEPORTER.getRegistryName())));
		registry.register(generateItemBlock(FLBlocks.MASS_FRAME_EDGE));
		registry.register(generateItemBlock(FLBlocks.MASS_FRAME_CORNER));

		for (int i = 0; i < 4; i++) {
			registry.register(new ItemBlockFrame(FLBlocks.FRAMES[i]).setRegistryName(Validate.notNull(FLBlocks.FRAMES[i].getRegistryName())));
		}

		registry.register(WrenchFactory.makeMeAWrench());
	}

	@Nonnull
	private static Item generateItemBlock(Block block) {
		return new ItemBlock(block).setRegistryName(Validate.notNull(block.getRegistryName()));
	}
}
