package com.rwtema.funkylocomotion;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Proxy {
	public static float renderTimeOffset = 0;

	public void registerRendering() {

	}

	public World getClientWorld() {
		throw new RuntimeException("Err loading client world on server");
	}

	public void sendUsePacket(BlockPos pos, EnumFacing face, EnumHand hand, float hitX, float hitY, float hitZ) {

	}
}
