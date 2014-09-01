package framesapi;

import net.minecraft.world.World;

public interface IMoveCheck {
    public boolean canMove(World worldObj, int x, int y, int z);
}
