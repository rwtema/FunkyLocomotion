package framesapi;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISlipperyBlock {
	public boolean canStickTo(World world, BlockPos pos, EnumFacing dir);
}
