package com.rwtema.funkylocomotion.rendering;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import com.rwtema.funkylocomotion.blocks.FLBlocks;
import com.rwtema.funkylocomotion.blocks.TileFrameProjector;
import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import com.rwtema.funkylocomotion.eventhandler.ClientTimer;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import com.rwtema.funkylocomotion.helper.NullHelper;
import com.rwtema.funkylocomotion.items.FLItems;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.*;

@Mod.EventBusSubscriber(Side.CLIENT)
public class FLRenderingRegistration {
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		registerBlockItemModel(FLBlocks.BOOSTER);
		registerBlockItemModel(FLBlocks.FRAME_PROJECTOR);
		registerBlockItemModel(FLBlocks.SLIDER);
		registerBlockItemModel(FLBlocks.TELEPORTER);
		registerBlockItemModel(FLBlocks.MASS_FRAME_CORNER);
		registerBlockItemModel(FLBlocks.MASS_FRAME_EDGE);

		ModelLoader.setCustomModelResourceLocation(Validate.notNull(Item.getItemFromBlock(FLBlocks.PUSHER)), 0, new ModelResourceLocation("funkylocomotion:pusher", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Validate.notNull(Item.getItemFromBlock(FLBlocks.PUSHER)), 1, new ModelResourceLocation("funkylocomotion:puller", "inventory"));
		ModelLoader.setCustomModelResourceLocation(FLItems.WRENCH, 0, new ModelResourceLocation("funkylocomotion:wrench", "inventory"));
		ModelLoader.setCustomModelResourceLocation(FLItems.WRENCH, 1, new ModelResourceLocation("funkylocomotion:wrench_eye", "inventory"));
		ModelLoader.setCustomModelResourceLocation(FLItems.WRENCH, 2, new ModelResourceLocation("funkylocomotion:wrench_hammer", "inventory"));

		for (final BlockStickyFrame frame : FLBlocks.FRAMES) {
			for (int i = 0; i < 16; i++) {
				ModelLoader.setCustomModelResourceLocation(Validate.notNull(Item.getItemFromBlock(frame)), i, new ModelResourceLocation("funkylocomotion:frame", "inventory"));
			}
		}

		registerRenderers();
	}

	private static void registerBlockItemModel(Block block) {
		Item item = Validate.notNull(Item.getItemFromBlock(block));
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	private static void registerRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileMovingClient.class, TESRMoving.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFrameProjector.class, TESRProjector.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new ClientTimer());
		MinecraftForge.EVENT_BUS.register(new ChunkRerenderer());

		/* What was the point of this?!
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(
				new IResourceManagerReloadListener() {
					@Override public void onResourceManagerReload(IResourceManager resourceManager) { FLBlocks.SLIDER.init(); } });
		*/

		FakeWorldClient.register();
		ModelLoader.setCustomStateMapper(NullHelper.notNull(FLBlocks.MOVING), blockIn -> ImmutableMap.of());


		for (final BlockStickyFrame frame : FLBlocks.FRAMES) {
			ModelLoader.setCustomStateMapper(frame, new IStateMapper() {
				Map<IBlockState, ModelResourceLocation> mapStateModelLocations = Maps.newLinkedHashMap();
				DefaultStateMapper mapper = new DefaultStateMapper();

				@Nonnull
				@Override
				public Map<IBlockState, ModelResourceLocation> putStateModelLocations(@Nonnull Block blockIn) {

					for (int i = 0; i < 16; i++) {
						IBlockState state = frame.getStateFromMeta(i);
						Map<IProperty<?>, Comparable<?>> values = new LinkedHashMap<>();
						ArrayList<EnumFacing> list = Lists.newArrayList(EnumFacing.values());
						list.sort(Comparator.comparing(EnumFacing::getName2));
						for (EnumFacing facing : list) {
							values.put(BlockStickyFrame.DIR_OPEN[facing.ordinal()], state.getValue(BlockStickyFrame.DIR_OPEN[facing.ordinal()]));
						}

						Validate.isTrue(frame.getBlockState().getValidStates().contains(state));

						this.mapStateModelLocations.put(
								state, new ModelResourceLocation("funkylocomotion:frame", mapper.getPropertyString(values)));
					}

					return mapStateModelLocations;
				}
			});
		}
	}
}
