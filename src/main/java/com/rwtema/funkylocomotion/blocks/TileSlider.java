package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.helper.BlockHelper;
import framesapi.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

public class TileSlider extends TilePusher {

    private static Random rand = new Random();
    private ForgeDirection slideDir = ForgeDirection.UNKNOWN;

    public void rotateAboutAxis() {
        ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata() % 6);
        ForgeDirection slide = getSlideDir();

        slideDir = slide.getRotation(dir);
    }

    public ForgeDirection getSlideDir() {
        int i = getBlockMetadata() % 6;
        int j = slideDir.ordinal();
        if (j == 6 || j == i || j == Facing.oppositeSide[i]) {
            j = (xCoord + yCoord + zCoord) % 6;
            while (j == 6 || j == i || j == Facing.oppositeSide[i])
                j = (j + 1) % 6;

            slideDir = ForgeDirection.getOrientation(j);
        }


        return slideDir;
    }

    public void setSlideDir(ForgeDirection dir) {
        slideDir = dir;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        slideDir = ForgeDirection.getOrientation(tag.getByte("SlideDirection"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setByte("SlideDirection", (byte) slideDir.ordinal());
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        slideDir = ForgeDirection.getOrientation(pkt.func_148857_g().getByte("dir"));
        if (worldObj.blockExists(xCoord, yCoord, zCoord)) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("dir", (byte) getSlideDir().ordinal());
        S35PacketUpdateTileEntity packet = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
        return packet;
    }

    @Override
    public ForgeDirection getDirection() {
        return getSlideDir();
    }

    @Override
    public List<BlockPos> getBlocks(World world, BlockPos home, ForgeDirection dir, boolean push) {
        ForgeDirection slide = getSlideDir();
        BlockPos advance = home.advance(dir);

        if (BlockHelper.canStick(world, advance, dir.getOpposite()))
            return getBlocks(world, home, advance, slide);

        return null;
    }
}
