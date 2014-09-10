package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.description.DescriptorRegistry;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import framesapi.BlockPos;
import framesapi.IDescriptionProxy;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileMovingClient extends TileMovingBase {
    public Block block = Blocks.air;
    public int meta = 0;
    public TileEntity tile = null;
    public boolean render;
    public boolean error = false;

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

        render = !tag.getBoolean("DNR");

        if (render) {
            IDescriptionProxy d = DescriptorRegistry.getDescriptor(tag.getString("DescID"));
            if (d != null)
                this.tile = d.recreateTileEntity(net, tag, block, meta, new BlockPos(this), getWorldObj());
        }
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