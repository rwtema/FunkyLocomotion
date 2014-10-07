package com.rwtema.funkylocomotion.debug;

import com.rwtema.funkylocomotion.blocks.TileMovingBase;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class DebugEventHandler {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void renderTiles(TickEvent.WorldTickEvent event) {
        WorldClient clientWorld = Minecraft.getMinecraft().theWorld;

        if (event.world == null || clientWorld == null)
            return;

        if (event.world.provider.dimensionId == clientWorld.provider.dimensionId) {
            List<TileEntity> tiles = event.world.loadedTileEntityList;
            double r, g, b;
            if (event.world.isRemote) {
                r = 0.25;
                g = 1;
                b = 0.25;
            } else {
                r = 0.25;
                g = 0.25;
                b = 1;
            }

            for (TileEntity tile : tiles) {
                if (!(tile instanceof TileMovingBase)) {
                    double x = tile.xCoord + 0.5, y = tile.yCoord + 0.5, z = tile.zCoord + 0.5;
                    if (tile.isInvalid())
                        clientWorld.spawnParticle("reddust", x, y, z, 1, 1, 1);
                    else
                        clientWorld.spawnParticle("reddust", x, y, z, r, g, b);
                }

            }
        }
    }
}
