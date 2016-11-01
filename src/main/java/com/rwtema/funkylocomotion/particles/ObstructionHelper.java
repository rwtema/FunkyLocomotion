package com.rwtema.funkylocomotion.particles;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.items.ItemWrench;
import com.rwtema.funkylocomotion.network.FLNetwork;
import com.rwtema.funkylocomotion.network.MessageObstruction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ObstructionHelper {
	@SideOnly(Side.CLIENT)
	public static boolean shouldRenderParticles() {
		return playerHoldingWrench(Minecraft.getMinecraft().thePlayer);
	}

	private static boolean playerHoldingWrench(EntityPlayer thePlayer) {
		return thePlayer != null && (isEyeWrench(thePlayer.getHeldItemMainhand()) || isEyeWrench(thePlayer.getHeldItemOffhand()));
	}

	private static boolean isEyeWrench(ItemStack heldItem) {
		return heldItem != null &&
				heldItem.getItem() == FunkyLocomotion.wrench && heldItem.getItemDamage() == ItemWrench.metaWrenchEye;
	}

	public static boolean sendObstructionPacket(World world, BlockPos pos, EnumFacing dir) {
		PlayerChunkMapEntry chunkWatcher = FLNetwork.getChunkWatcher(world, pos);
		if (chunkWatcher == null) return false;

		Packet packet = null;

		for (EntityPlayerMP player : chunkWatcher.players) {
			if (playerHoldingWrench(player)) {
				if (packet == null) packet = FLNetwork.net.getPacketFrom(new MessageObstruction(pos, dir));
				player.connection.sendPacket(packet);
			}

		}
		return packet != null;
	}
}
