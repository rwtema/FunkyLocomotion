package com.rwtema.funkylocomotion;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import com.rwtema.funkylocomotion.eventhandler.ClientTimer;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import com.rwtema.funkylocomotion.rendering.ChunkRerenderer;
import com.rwtema.funkylocomotion.rendering.TESRMoving;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
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

import java.util.*;

@SideOnly(Side.CLIENT)
public class ProxyClient extends Proxy {
	@Override
	public void registerRendering() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileMovingClient.class, new TESRMoving());
		MinecraftForge.EVENT_BUS.register(new ClientTimer());
		MinecraftForge.EVENT_BUS.register(new ChunkRerenderer());

		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new IResourceManagerReloadListener() {
			@Override
			public void onResourceManagerReload(IResourceManager resourceManager) {
				FunkyLocomotion.slider.init();
			}
		});

		FakeWorldClient.register();
		ModelLoader.setCustomStateMapper(FunkyLocomotion.moving, new IStateMapper() {
			@Override
			public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
				return ImmutableMap.of();
			}
		});


		for (final BlockStickyFrame frame : FunkyLocomotion.frame) {
			ModelLoader.setCustomStateMapper(frame, new IStateMapper() {
				Map<IBlockState, ModelResourceLocation> mapStateModelLocations = Maps.newLinkedHashMap();
				DefaultStateMapper mapper = new DefaultStateMapper();

				@Override
				public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {

					for (int i = 0; i < 16; i++) {
						IBlockState state = frame.getStateFromMeta(i);
						Map<IProperty<?>, Comparable<?>> values = new LinkedHashMap<IProperty<?>, Comparable<?>>();
						ArrayList<EnumFacing> list = Lists.newArrayList(EnumFacing.values());
						Collections.sort(list, new Comparator<EnumFacing>() {
							@Override
							public int compare(EnumFacing o1, EnumFacing o2) {
								return o1.getName2().compareTo(o2.getName2());
							}
						});
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
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(frame), 0, new ModelResourceLocation("funkylocomotion:frame", "inventory"));
		}

		registerBlock(FunkyLocomotion.booster);
		registerBlock(FunkyLocomotion.slider);
		registerBlock(FunkyLocomotion.teleporter);
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(FunkyLocomotion.pusher), 0, new ModelResourceLocation("funkylocomotion:pusher", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(FunkyLocomotion.pusher), 1, new ModelResourceLocation("funkylocomotion:puller", "inventory"));
		ModelLoader.setCustomModelResourceLocation(FunkyLocomotion.wrench, 0, new ModelResourceLocation("funkylocomotion:wrench", "inventory"));
		ModelLoader.setCustomModelResourceLocation(FunkyLocomotion.wrench, 1, new ModelResourceLocation("funkylocomotion:wrench", "inventory"));
		ModelLoader.setCustomModelResourceLocation(FunkyLocomotion.wrench, 2, new ModelResourceLocation("funkylocomotion:wrench", "inventory"));
	}

	private void registerBlock(Block booster) {
		Item item = Item.getItemFromBlock(booster);
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
