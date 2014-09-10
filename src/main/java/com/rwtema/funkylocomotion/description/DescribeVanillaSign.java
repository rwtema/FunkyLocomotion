package com.rwtema.funkylocomotion.description;

import framesapi.BlockPos;
import framesapi.IDescriptionProxy;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.World;

public class DescribeVanillaSign extends DescribeVanilla implements IDescriptionProxy {
    @Override
    public String getID() {
        return "sign";
    }

    @Override
    public boolean canHandleTile(TileEntity tile) {
        return tile instanceof TileEntitySign;
    }

    @Override
    public void addDescriptionToTags(NBTTagCompound descriptor, TileEntity tile) {
        Packet packet = tile.getDescriptionPacket();
        if (packet instanceof S33PacketUpdateSign) {
            final String[] strings = ((S33PacketUpdateSign) packet).func_149347_f();
            NBTTagCompound signTag = new NBTTagCompound();
            for (int i = 0; i < 4; i++)
                signTag.setString(Integer.toString(i), strings[i]);
            descriptor.setTag("Tile", signTag);
        }
    }

    @Override
    public TileEntity recreateTileEntity(NetworkManager net, NBTTagCompound tag, Block block, int meta, BlockPos pos, World world) {
        TileEntity tile = super.recreateTileEntity(net, tag, block, meta, pos, world);

        if (tile == null)
            return null;

        if (tag.hasKey("Tile", 10)) {
            NBTTagCompound tileTag = tag.getCompoundTag("Tile");
            if (tile instanceof TileEntitySign) {
                for (int i = 0; i < 4; i++)
                    ((TileEntitySign) tile).signText[i] = tileTag.getString(Integer.toString(i));
            }
        }
        return tile;
    }
}
