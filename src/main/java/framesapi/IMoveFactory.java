package framesapi;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMoveFactory {
	NBTTagCompound destroyBlock(World world, BlockPos pos);

	boolean recreateBlock(World world, BlockPos pos, NBTTagCompound tag);

}
