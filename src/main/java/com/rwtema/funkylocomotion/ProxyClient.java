package com.rwtema.funkylocomotion;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import com.rwtema.funkylocomotion.blocks.TileFrameProjector;
import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import com.rwtema.funkylocomotion.eventhandler.ClientTimer;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import com.rwtema.funkylocomotion.rendering.ChunkRerenderer;
import com.rwtema.funkylocomotion.rendering.TESRMoving;
import com.rwtema.funkylocomotion.rendering.TESRProjector;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ProxyClient extends Proxy {
	@Override
	public void registerRendering() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileMovingClient.class, TESRMoving.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFrameProjector.class, TESRProjector.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new ClientTimer());
		MinecraftForge.EVENT_BUS.register(new ChunkRerenderer());

		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(resourceManager -> FunkyLocomotion.slider.init());

		FakeWorldClient.register();
		ModelLoader.setCustomStateMapper(FunkyLocomotion.moving, blockIn -> ImmutableMap.of());


		for (final BlockStickyFrame frame : FunkyLocomotion.frame) {
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
						Collections.sort(list, (o1, o2) -> o1.getName2().compareTo(o2.getName2()));
						for (EnumFacing facing : list) {
							values.put(BlockStickyFrame.DIR_OPEN[facing.ordinal()], state.getValue(BlockStickyFrame.DIR_OPEN[facing.ordinal()]));
						}

						Validate.isTrue(frame.getBlockState().getValidStates().contains(state));

						this.mapStateModelLocations.put(state,
								new ModelResourceLocation(
										"funkylocomotion:frame",
										mapper.getPropertyString(values))
						);
					}

					return mapStateModelLocations;
				}
			});

			for (int i = 0; i < 16; i++) {
				ModelLoader.setCustomModelResourceLocation(Validate.notNull(Item.getItemFromBlock(frame)), i, new ModelResourceLocation("funkylocomotion:frame", "inventory"));
			}

		}

		registerBlockItemModel(FunkyLocomotion.booster);
		registerBlockItemModel(FunkyLocomotion.slider);
		registerBlockItemModel(FunkyLocomotion.teleporter);
		registerBlockItemModel(FunkyLocomotion.frameProjector);
		ModelLoader.setCustomModelResourceLocation(Validate.notNull(Item.getItemFromBlock(FunkyLocomotion.pusher)), 0, new ModelResourceLocation("funkylocomotion:pusher", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Validate.notNull(Item.getItemFromBlock(FunkyLocomotion.pusher)), 1, new ModelResourceLocation("funkylocomotion:puller", "inventory"));
		ModelLoader.setCustomModelResourceLocation(FunkyLocomotion.wrench, 0, new ModelResourceLocation("funkylocomotion:wrench", "inventory"));
		ModelLoader.setCustomModelResourceLocation(FunkyLocomotion.wrench, 1, new ModelResourceLocation("funkylocomotion:wrench_eye", "inventory"));
		ModelLoader.setCustomModelResourceLocation(FunkyLocomotion.wrench, 2, new ModelResourceLocation("funkylocomotion:wrench_hammer", "inventory"));
	}

	private void registerBlockItemModel(Block block) {
		Item item = Validate.notNull(Item.getItemFromBlock(block));
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}

	@Override
	public void sendUsePacket(BlockPos pos, EnumFacing face, EnumHand hand, float hitX, float hitY, float hitZ) {
		NetHandlerPlayClient connection = Minecraft.getMinecraft().getConnection();
		if (connection != null) {
			connection.sendPacket(
					new CPacketPlayerTryUseItemOnBlock(pos, face, hand, hitX, hitY, hitZ));
		}
	}
}
