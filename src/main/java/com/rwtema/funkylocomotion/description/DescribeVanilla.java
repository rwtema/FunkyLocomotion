package com.rwtema.funkylocomotion.description;

import framesapi.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.*;
import net.minecraft.world.World;

public class DescribeVanilla extends DescribeBase {
    @Override
    public String getID() {
        return "";
    }

    @Override
    public boolean canHandleTile(TileEntity tile) {
        final Packet packet = tile.getDescriptionPacket();
        return packet == null || packet instanceof S35PacketUpdateTileEntity;
    }

    @Override
    public void addDescriptionToTags(NBTTagCompound descriptor, TileEntity tile) {
        Packet packet = tile.getDescriptionPacket();

        if (packet instanceof S35PacketUpdateTileEntity) {
            S35PacketUpdateTileEntity pkt_TE = (S35PacketUpdateTileEntity) packet;
            descriptor.setTag("Tile", pkt_TE.func_148857_g());
        }
    }

    @Override
    public TileEntity recreateTileEntity(NetworkManager net, NBTTagCompound tag, Block block, int meta, BlockPos pos, World world) {
        TileEntity tile = super.recreateTileEntity(net, tag, block, meta, pos, world);
        if (tile != null) {
            if (tag.hasKey("Tile", 10)) {
                NBTTagCompound tileTag = tag.getCompoundTag("Tile");

                if (tile instanceof TileEntityMobSpawner ||
                        tile instanceof TileEntityCommandBlock ||
                        tile instanceof TileEntityBeacon ||
                        tile instanceof TileEntitySkull ||
                        tile instanceof TileEntityFlowerPot) {
                    tile.readFromNBT(tileTag);
                } else {
                    S35PacketUpdateTileEntity newpkt = new S35PacketUpdateTileEntity(pos.x, pos.y, pos.z, 0, tileTag);
                    tile.onDataPacket(net, newpkt);
                }
            }
        }
        return tile;
    }
}
