package com.rwtema.funkylocomotion.debug;

import com.rwtema.funkylocomotion.blocks.TileMovingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class DebugEventHandler {
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderTiles(TickEvent.WorldTickEvent event) {
		WorldClient clientWorld = Minecraft.getMinecraft().world;

		if (event.world == null || clientWorld == null)
			return;

		if (event.world.provider.getDimension() == clientWorld.provider.getDimension()) {
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
					BlockPos pos = tile.getPos();
					double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
					if (tile.isInvalid())
						clientWorld.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 1, 1, 1);
					else
						clientWorld.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, r, g, b);
				}

			}
		}
	}
}
