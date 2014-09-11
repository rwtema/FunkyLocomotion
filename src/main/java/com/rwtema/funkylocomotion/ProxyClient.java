package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import com.rwtema.funkylocomotion.eventhandler.ClientTimer;
import com.rwtema.funkylocomotion.rendering.RenderBlockPusher;
import com.rwtema.funkylocomotion.rendering.TileEntityRenderMoving;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ProxyClient extends Proxy {
    @Override
    public void registerRendering() {
        pusherRendererId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new RenderBlockPusher());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMovingClient.class, new TileEntityRenderMoving());
        FMLCommonHandler.instance().bus().register(new ClientTimer());
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }
}
