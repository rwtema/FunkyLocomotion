package com.rwtema.frames.blocks;

import com.rwtema.frames.fakes.FakeWorldClient;
import com.rwtema.frames.helper.BlockHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileMovingClient extends TileMovingBase {
    public Block block = Blocks.air;
    public int meta = 0;
    public TileEntity tile = null;

    @Override
    public void updateEntity() {
        if (time < maxTime)
            time++;
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

        if (tag.hasKey("Collisions", 9)) {
            collisions = AxisTags(tag.getTagList("Collisions", 10));
        }

        BlockHelper.postUpdateBlock(worldObj, new BlockPos(this));

        dir = ForgeDirection.getOrientation(tag.getByte("Dir"));

        if (block.hasTileEntity(meta)) {
            tile = block.createTileEntity(getWorldObj(), meta);
            if (tile != null) {
                tile.setWorldObj(FakeWorldClient.getFakeWorldWrapper(worldObj));
                tile.xCoord = xCoord;
                tile.yCoord = yCoord;
                tile.zCoord = zCoord;
                tile.blockType = block;
                tile.blockMetadata = meta;

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

//    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB other;
        if (tile != null)
            other = tile.getRenderBoundingBox();
        else
            other = block.getCollisionBoundingBoxFromPool(FakeWorldClient.getFakeWorldWrapper(worldObj), xCoord, yCoord, zCoord);

        if (other == null)
            other = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
        else
            other = other.func_111270_a(AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1));

        double h = offset(0);
        return other.getOffsetBoundingBox(h * dir.offsetX, h * dir.offsetY, h * dir.offsetZ);
    }

}