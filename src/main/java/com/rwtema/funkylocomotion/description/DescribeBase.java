package com.rwtema.funkylocomotion.description;

import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.BlockPos;
import framesapi.IDescriptionProxy;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class DescribeBase implements IDescriptionProxy {
    @Override
    @SideOnly(Side.CLIENT)
    public TileEntity recreateTileEntity(NetworkManager net, NBTTagCompound tag, Block block, int meta, BlockPos pos, World world) {
        if (!block.hasTileEntity(meta))
            return null;

        TileEntity tile = block.createTileEntity(world, meta);
        if (tile != null) {
            tile.setWorldObj(FakeWorldClient.getFakeWorldWrapper(world));
            tile.xCoord = pos.x;
            tile.yCoord = pos.y;
            tile.zCoord = pos.z;
            tile.blockType = block;
            tile.blockMetadata = meta;
        }
        return tile;
    }
}
