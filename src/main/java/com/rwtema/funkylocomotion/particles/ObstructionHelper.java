package com.rwtema.funkylocomotion.particles;

import java.lang.reflect.Field;
import java.util.List;
import com.rwtema.funkylocomotion.items.FLItems;
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
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ObstructionHelper {
	private static final Field field_PlayerChunkMapEntry_players = ReflectionHelper.findField(PlayerChunkMapEntry.class, "field_187283_c", "players");

	@SideOnly(Side.CLIENT)
	public static boolean shouldRenderParticles() {
		return playerHoldingWrench(Minecraft.getMinecraft().player);
	}

	private static boolean playerHoldingWrench(EntityPlayer thePlayer) {
		return thePlayer != null && (isEyeWrench(thePlayer.getHeldItemMainhand()) || isEyeWrench(thePlayer.getHeldItemOffhand()));
	}

	private static boolean isEyeWrench(ItemStack heldItem) {
		return heldItem.isEmpty() == false &&
				heldItem.getItem() == FLItems.WRENCH && heldItem.getItemDamage() == ItemWrench.metaWrenchEye;
	}

	@SuppressWarnings("unchecked")
	public static boolean sendObstructionPacket(World world, BlockPos pos, EnumFacing dir) {
		PlayerChunkMapEntry chunkWatcher = FLNetwork.getChunkWatcher(world, pos);
		if (chunkWatcher == null) return false;

		Packet<?> packet = null;

		List<EntityPlayerMP> players;
		try
		{
			players = (List<EntityPlayerMP>) field_PlayerChunkMapEntry_players.get(chunkWatcher);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			return false;
		}

		//for (EntityPlayerMP player : chunkWatcher.players) {
		for (EntityPlayerMP player : players) {
			if (playerHoldingWrench(player)) {
				if (packet == null) packet = FLNetwork.net.getPacketFrom(new MessageObstruction(pos, dir));
				player.connection.sendPacket(packet);
			}

		}
		return packet != null;
	}
}
