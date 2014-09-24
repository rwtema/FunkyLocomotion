package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.description.DescriptorRegistry;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.BlockPos;
import framesapi.IDescriptionProxy;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.HashSet;
import java.util.WeakHashMap;

public class TileMovingClient extends TileMovingBase {
    public static WeakHashMap<ChunkCoordinates, TileEntity> cachedTiles = new WeakHashMap<ChunkCoordinates, TileEntity>();
    public static HashSet<Class> renderBlackList = new HashSet<Class>();
    public static HashSet<Class> renderErrorList = new HashSet<Class>();
    public Block block = Blocks.air;
    public int meta = 0;
    public TileEntity tile = null;
    public boolean render;
    public boolean error = false;
    public boolean rawTile = false;
    public boolean init = false;
    public boolean failedToRenderInFirstPass = false;

    public TileMovingClient() {
        super(Side.CLIENT);
    }

    @Override
    public void updateEntity() {
        if (time < maxTime) {
            super.updateEntity();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (rawTile && tile != null)
            tile.invalidate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        init = true;
        NBTTagCompound tag = pkt.func_148857_g();
        block = Block.getBlockById(tag.getInteger("Block"));
        meta = tag.getInteger("Meta");

        time = tag.getInteger("Time");
        maxTime = tag.getInteger("MaxTime");

        lightLevel = tag.getInteger("Light");
        lightOpacity = tag.getShort("Opacity");

        isAir = block == Blocks.air;

        if (tag.hasKey("Collisions", 9)) {
            collisions = AxisTags(tag.getTagList("Collisions", 10));
        }

        BlockHelper.postUpdateBlock(worldObj, new BlockPos(this));

        dir = ForgeDirection.getOrientation(tag.getByte("Dir"));

        ChunkCoordinates key = new ChunkCoordinates(xCoord - dir.offsetX, yCoord - dir.offsetY, zCoord - dir.offsetZ);

        TileEntity tile = cachedTiles.get(key);
        if (tile != null) {
            cachedTiles.remove(key);
            if (tile.getWorldObj() == this.worldObj) {
                rawTile = true;
                tile.xCoord = this.xCoord;
                tile.yCoord = this.yCoord;
                tile.zCoord = this.zCoord;
                tile.blockType = this.block;
                tile.blockMetadata = this.meta;

                tile.setWorldObj(FakeWorldClient.getFakeWorldWrapper(this.worldObj));
                this.tile = tile;
                render = true;
            }
        } else {
            render = !tag.getBoolean("DNR");

            if (render) {
                IDescriptionProxy d = DescriptorRegistry.getDescriptor(tag.getString("DescID"));
                if (d != null)
                    this.tile = d.recreateTileEntity(net, tag, block, meta, new BlockPos(this), getWorldObj());
            }
        }

        checkClass(this.block);

        if (checkClass(this.tile))
            this.tile = null;
    }

    public boolean checkClass(Object o) {
        if (o == null)
            return false;
        if (renderBlackList.contains(o.getClass())) {
            this.render = false;
            return true;
        }

        if (renderErrorList.contains(o.getClass())) {
            this.render = false;
            this.error = true;
            return true;
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
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

        double h = offset(true);
        return other.getOffsetBoundingBox(h * dir.offsetX, h * dir.offsetY, h * dir.offsetZ);
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        if (maxTime == 0)
            return false;

        if (!render || error)
            return pass == 0;

        if (block == Blocks.air || block.getRenderType() == -1)
            return false;

        if (pass == 1 && failedToRenderInFirstPass)
            return true;

        if (tile != null) {
            if (tile.shouldRenderInPass(pass))
                return true;
        }

        return block.canRenderInPass(pass);
    }
}