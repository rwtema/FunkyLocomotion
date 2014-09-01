package framesapi;

import net.minecraft.world.World;

import java.util.List;

public interface IAdvStickyBlock {
    public List<BlockPos> getBlocksToMove(World world, BlockPos pos );
}
