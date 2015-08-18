package com.rwtema.funkylocomotion;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class Proxy {
    public static float renderTimeOffset = 0;
    public int pusherRendererId = 0;
    public int sliderRendererId = 0;

    public void registerRendering() {

    }

    public World getClientWorld() {
        throw new RuntimeException("Err loading client world on server");
    }

	public void sendUsePacket(int x, int y, int z, int face, ItemStack item, float hitX, float hitY, float hitZ) {

	}
}
