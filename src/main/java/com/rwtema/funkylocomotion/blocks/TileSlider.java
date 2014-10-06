package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.helper.BlockHelper;
import framesapi.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

public class TileSlider extends TilePusher {

    private static Random rand = new Random();
    private ForgeDirection slideDir = ForgeDirection.UNKNOWN;

    public void rotateAboutAxis() {
        ForgeDirection dir = getFacing();
        ForgeDirection slide = getSlideDir();

        slideDir = slide.getRotation(dir);
    }

    private static final int[][] orthog = {
            {6, 6, 5, 4, 3, 2, 6},
            {6, 6, 4, 5, 2, 3, 6},
            {5, 4, 6, 6, 1, 0, 6},
            {4, 5, 6, 6, 0, 1, 6},
            {3, 2, 1, 0, 6, 6, 6},
            {2, 3, 0, 1, 6, 6, 6},
            {6, 6, 6, 6, 6, 6, 6}
    };

    public static ForgeDirection getOrthogonal(ForgeDirection a, ForgeDirection b) {
        return ForgeDirection.getOrientation(orthog[a.ordinal()][b.ordinal()]);
    }

    public ForgeDirection getSlideDir() {
        ForgeDirection ang = getFacing();

        if (getOrthogonal(ang, slideDir) == ForgeDirection.UNKNOWN) {
            int j = (xCoord + yCoord + zCoord) % 6;
            while (j >= 6 || getOrthogonal(ForgeDirection.getOrientation(j), ang) == ForgeDirection.UNKNOWN)
                j = (j + 1) % 6;

            slideDir = ForgeDirection.getOrientation(j);
        }


        return slideDir;
    }

    public ForgeDirection getFacing() {
        return ForgeDirection.getOrientation(getBlockMetadata() % 6);
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
