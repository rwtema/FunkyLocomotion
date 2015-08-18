package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import com.rwtema.funkylocomotion.eventhandler.ClientTimer;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import com.rwtema.funkylocomotion.rendering.ChunkRerenderer;
import com.rwtema.funkylocomotion.rendering.RenderBlockPusher;
import com.rwtema.funkylocomotion.rendering.RenderBlockSlider;
import com.rwtema.funkylocomotion.rendering.RenderItemWrench;
import com.rwtema.funkylocomotion.rendering.TileEntityRenderMoving;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

@SideOnly(Side.CLIENT)
public class ProxyClient extends Proxy {
    @Override
    public void registerRendering() {
        pusherRendererId = RenderingRegistry.getNextAvailableRenderId();
        sliderRendererId = RenderingRegistry.getNextAvailableRenderId();

        RenderingRegistry.registerBlockHandler(new RenderBlockPusher());
        RenderingRegistry.registerBlockHandler(new RenderBlockSlider());
        MinecraftForgeClient.registerItemRenderer(FunkyLocomotion.wrench, new RenderItemWrench());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMovingClient.class, new TileEntityRenderMoving());
        FMLCommonHandler.instance().bus().register(new ClientTimer());
		FMLCommonHandler.instance().bus().register(new ChunkRerenderer());

        FakeWorldClient.register();
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }

	@Override
	public void sendUsePacket(int x, int y, int z, int face, ItemStack item, float hitX, float hitY, float hitZ) {
			Minecraft.getMinecraft().getNetHandler().addToSendQueue(
					new C08PacketPlayerBlockPlacement(x, y, z, face, item, hitX, hitY, hitZ));
	}
}
