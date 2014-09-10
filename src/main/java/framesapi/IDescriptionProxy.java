package framesapi;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IDescriptionProxy {
    public String getID();

    public boolean canHandleTile(TileEntity tile);

    public void addDescriptionToTags(NBTTagCompound descriptor, TileEntity tile);


    public TileEntity recreateTileEntity(NetworkManager net, NBTTagCompound tag, Block block, int meta, BlockPos pos, World world);

}
