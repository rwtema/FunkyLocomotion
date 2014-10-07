package com.rwtema.funkylocomotion.blocks;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.movers.IMover;
import com.rwtema.funkylocomotion.movers.MoveManager;
import com.rwtema.funkylocomotion.proxydelegates.ProxyRegistry;
import framesapi.BlockPos;
import framesapi.IStickyBlock;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class TilePusher extends TileEntity implements IEnergyHandler, IMover {
    public static int maxTiles = 64 * 4;
    public static int powerPerTile = 1000;
    public final EnergyStorage energy = new EnergyStorage(maxTiles * powerPerTile);
    public boolean powered;
    public int countDown = 4;

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energy.readFromNBT(tag);
        powered = tag.getBoolean("Powered");
        countDown = tag.getInteger("Countdown");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        energy.writeToNBT(tag);
        tag.setBoolean("powered", powered);
        tag.setInteger("Countdown", countDown);
    }

    @Override
    public void updateEntity() {
        if (powered) {
            if (countDown > 0) {
                countDown--;
                if (countDown == 0)
                    startMoving();
            }
        }


    }

    @Override
    public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
        return oldBlock != newBlock;
    }

    public List<BlockPos> getBlocks(World world, BlockPos home, ForgeDirection dir, boolean push) {
        BlockPos advance = home.advance(dir);
        if (push) {
            if (BlockHelper.canStick(world, advance, dir.getOpposite()))
                return getBlocks(world, home, advance, dir);
        } else {
            if (!world.isAirBlock(advance.x, advance.y, advance.z))
                return null;

            advance = advance.advance(dir);
            if (BlockHelper.canStick(world, advance, dir.getOpposite()))
                return getBlocks(world, home, advance, dir.getOpposite());

        }

        return null;
    }

    public List<BlockPos> getBlocks(World world, BlockPos home, BlockPos start, ForgeDirection moveDir) {

        ArrayList<BlockPos> posList = new ArrayList<BlockPos>();
        ArrayList<BlockPos> toIterate = new ArrayList<BlockPos>();

        toIterate.add(start);


        for (int i = 0; i < toIterate.size(); i++) {
            BlockPos pos = toIterate.get(i);

            posList.add(pos);

            Block b = BlockHelper.getBlock(world, pos);

            IStickyBlock stickyBlock = ProxyRegistry.getInterface(b, IStickyBlock.class);

            if (stickyBlock != null) {
                for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                    if (stickyBlock.isStickySide(world, pos.x, pos.y, pos.z, side)) {
                        BlockPos newPos = pos.advance(side);

                        if (home.equals(newPos))
                            continue;

                        if (toIterate.contains(newPos))
                            continue;

                        if (BlockHelper.canStick(world, newPos, side.getOpposite()))
                            toIterate.add(pos.advance(side));
                    }
                }
            }
        }

        for (BlockPos pos : posList) {
            BlockPos adv = pos.advance(moveDir);
            if (!posList.contains(adv) && !BlockHelper.canReplace(world, adv)) {
                return null;
            }
        }

        return posList;
    }

    public void startMoving() {
        int meta = getBlockMetadata();
        ForgeDirection dir = ForgeDirection.getOrientation(meta % 6).getOpposite();
        boolean push = meta < 6;
        if (dir == ForgeDirection.UNKNOWN)
            return;

        List<BlockPos> posList = getBlocks(worldObj, new BlockPos(xCoord, yCoord, zCoord), dir, push);
        if (posList != null) {
            final int energy = posList.size() * powerPerTile;
            if (this.energy.extractEnergy(energy, true) == energy) {
                this.energy.extractEnergy(energy, false);

                MoveManager.startMoving(worldObj, posList, getDirection());
            }
        }
    }

    @Override
    public boolean stillExists() {
        return !tileEntityInvalid && worldObj != null && worldObj.blockExists(xCoord, yCoord, zCoord) && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
    }

    @Override
    public void onChunkUnload() {
        tileEntityInvalid = true;
    }

    public ForgeDirection getDirection() {
        int meta = getBlockMetadata();
        ForgeDirection dir = ForgeDirection.getOrientation(meta % 6).getOpposite();
        boolean push = meta < 6;
        return push ? dir : dir.getOpposite();
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        return energy.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return energy.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return energy.getMaxEnergyStored();
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return true;
    }
}
