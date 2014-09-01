package com.rwtema.frames.blocks;

import com.rwtema.frames.helper.BlockHelper;
import com.rwtema.frames.rendering.FakeWorldClient;
import framesapi.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.*;
import net.minecraftforge.common.util.ForgeDirection;

public class TileMovingClient extends TileMovingBase {
    public Block block = Blocks.air;
    public int meta = 0;
    public TileEntity tile = null;

    @Override
    public void updateEntity() {
        if (time < maxTime) {
            time++;
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound tag = pkt.func_148857_g();
        block = Block.getBlockById(tag.getInteger("Block"));
        meta = tag.getInteger("Meta");

        time = tag.getInteger("Time");
        maxTime = tag.getInteger("MaxTime");

        lightLevel = tag.getInteger("Light");
        lightOpacity = tag.getShort("Opacity");

        BlockHelper.postUpdateBlock(worldObj, new BlockPos(this));

        dir = ForgeDirection.getOrientation(tag.getByte("Dir"));

        if (block.hasTileEntity(meta)) {
            tile = block.createTileEntity(getWorldObj(), meta);
            if (tile != null) {
                tile.setWorldObj(FakeWorldClient.getFakeWorldWrapper(worldObj));
                tile.xCoord = xCoord;
                tile.yCoord = yCoord;
                tile.zCoord = zCoord;

                if (tag.hasKey("Tile", 10)) {
                    NBTTagCompound tileTag = tag.getCompoundTag("Tile");

                    if (tile instanceof TileEntityMobSpawner ||
                            tile instanceof TileEntityCommandBlock ||
                            tile instanceof TileEntityBeacon ||
                            tile instanceof TileEntitySkull ||
                            tile instanceof TileEntityFlowerPot) {
                        tile.readFromNBT(tileTag);
                    } else if (tile instanceof TileEntitySign) {
                        for (int i = 0; i < 4; i++)
                            ((TileEntitySign) tile).signText[i] = tileTag.getString(Integer.toString(i));
                    } else {
                        S35PacketUpdateTileEntity newpkt = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tileTag);
                        tile.onDataPacket(net, newpkt);
                    }
                }
            }
        } else
            tile = null;
    }

    public double offset(float f) {
        return (time + f) / (maxTime + 1) - 1;
    }
}
