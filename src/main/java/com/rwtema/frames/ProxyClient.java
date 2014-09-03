package com.rwtema.frames;

import com.rwtema.frames.blocks.TileMovingClient;
import com.rwtema.frames.rendering.TileEntityRenderMoving;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProxyClient extends Proxy {
    @Override
    public void registerRendering() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMovingClient.class, new TileEntityRenderMoving());
    }
}
