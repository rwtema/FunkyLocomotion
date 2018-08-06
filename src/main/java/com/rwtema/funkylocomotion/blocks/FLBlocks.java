package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.helper.NullHelper;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class FLBlocks {
	@GameRegistry.ObjectHolder("funkylocomotion:pusher")
	public static final BlockPusher PUSHER = NullHelper.notNull();

	@GameRegistry.ObjectHolder("funkylocomotion:moving")
	public static final BlockMoving MOVING = NullHelper.notNull();

	@GameRegistry.ObjectHolder("funkylocomotion:slider")
	public static final BlockSlider SLIDER = NullHelper.notNull();

	@GameRegistry.ObjectHolder("funkylocomotion:booster")
	public static final BlockBooster BOOSTER = NullHelper.notNull();

	@GameRegistry.ObjectHolder("funkylocomotion:teleporter")
	public static final BlockTeleport TELEPORTER = NullHelper.notNull();

	@GameRegistry.ObjectHolder("funkylocomotion:frame_projector")
	public static final BlockFrameProjector FRAME_PROJECTOR = NullHelper.notNull();

	@GameRegistry.ObjectHolder("funkylocomotion:mass_frame_corner")
	public static final BlockMassFrameCorner MASS_FRAME_CORNER = NullHelper.notNull();

	@GameRegistry.ObjectHolder("funkylocomotion:mass_frame_edge")
	public static final BlockMassFrameEdge MASS_FRAME_EDGE = NullHelper.notNull();


	public static final BlockStickyFrame[] FRAMES = new BlockStickyFrame[4];

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> registry = event.getRegistry();

		registry.register(new BlockBooster());
		registry.register(new BlockFrameProjector());
		registry.register(new BlockMoving());
		registry.register((new BlockPusher()).setUnlocalizedName("funkylocomotion:pusher").setRegistryName("funkylocomotion:pusher"));
		registry.register(new BlockSlider());
		registry.register(new BlockTeleport());
		registry.register(new BlockMassFrameEdge());
		registry.register(new BlockMassFrameCorner());

		for (int i = 0; i < 4; i++) {
			BlockStickyFrame.curLoadingIndex = i;
			registry.register(FRAMES[i] = new BlockStickyFrame());
		}

		registerTile(TileBooster.class, "funkylocomotion:tile_booster");
		registerTile(TileFrameProjector.class, "funkylocomotion:tile_frame_projector");
		registerTile(TileMovingClient.class, "funkylocomotion:tile_mover_client");
		registerTile(TileMovingServer.class, "funkylocomotion:tile_mover");
		registerTile(TilePusher.class, "funkylocomotion:tile_pusher");
		registerTile(TileSlider.class, "funkylocomotion:tile_slider");
		registerTile(TileTeleport.class, "funkylocomotion:tile_teleporter");
		registerTile(TileMassFrame.class, "funkylocomotion:tile_mass_frame");
		registerTile(TileMassFrameController.class, "funkylocomotion:tile_mass_frame_controller");
	}

	private static void registerTile(Class<? extends TileEntity> clazz, String key) {
		GameRegistry.registerTileEntity(clazz, new ResourceLocation(key));

	}
}
