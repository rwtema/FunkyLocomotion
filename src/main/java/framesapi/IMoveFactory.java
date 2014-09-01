package framesapi;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface IMoveFactory {
    public NBTTagCompound destroyBlock(World world, BlockPos pos);

    public boolean recreateBlock(World world, BlockPos pos, NBTTagCompound tag);

}
