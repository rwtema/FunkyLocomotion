package framesapi;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface ISlipperyBlock {
    public boolean canStickTo(World world, BlockPos pos, ForgeDirection dir);
}
