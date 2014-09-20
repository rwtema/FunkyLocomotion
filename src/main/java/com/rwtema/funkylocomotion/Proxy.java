package com.rwtema.funkylocomotion;

import net.minecraft.world.World;

public class Proxy {
    public int pusherRendererId = 0;
    public int sliderRendererId = 0;
    public static float renderTimeOffset = 0;

    public void registerRendering(){

    }

    public World getClientWorld(){
        throw  new RuntimeException("Err loading client world on server");
    }
}
