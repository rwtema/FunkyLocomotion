package com.rwtema.funkylocomotion.particles;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.items.ItemWrench;
import com.rwtema.funkylocomotion.network.FLNetwork;
import com.rwtema.funkylocomotion.network.MessageObstruction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ObstructionHelper {
    @SideOnly(Side.CLIENT)
    public static boolean shouldRenderParticles() {
        return playerHoldingWrench(Minecraft.getMinecraft().thePlayer);
    }

    private static boolean playerHoldingWrench(EntityPlayer thePlayer) {
        return !(thePlayer == null || thePlayer.getHeldItem() == null) &&
                thePlayer.getHeldItem().getItem() == FunkyLocomotion.wrench && thePlayer.getHeldItem().getItemDamage() == ItemWrench.metaWrenchEye;
    }

    public static boolean sendObstructionPacket(World world, BlockPos pos, ForgeDirection dir) {
        PlayerManager.PlayerInstance chunkWatcher = FLNetwork.getChunkWatcher(world, pos);
        if (chunkWatcher == null) return false;

        Packet packet = null;

        for (int i = 0; i < chunkWatcher.playersWatchingChunk.size(); ++i) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) chunkWatcher.playersWatchingChunk.get(i);

            if (!playerHoldingWrench(entityplayermp)) continue;

            if (!entityplayermp.loadedChunks.contains(chunkWatcher.chunkLocation)) {
                if(packet == null) packet = FLNetwork.net.getPacketFrom(new MessageObstruction(pos, dir));
                entityplayermp.playerNetServerHandler.sendPacket(packet);
            }
        }
        return packet != null;
    }
}
