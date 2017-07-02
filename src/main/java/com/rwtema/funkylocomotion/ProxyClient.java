package com.rwtema.funkylocomotion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ProxyClient extends Proxy {

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().world;
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
