package framesapi;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IStickyBlock {
    public boolean isStickySide(World world, int x,int y,int z, ForgeDirection side);
}
