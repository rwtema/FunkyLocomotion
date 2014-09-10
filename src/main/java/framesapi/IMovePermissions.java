package framesapi;

import net.minecraft.world.World;

import java.util.UUID;

public interface IMovePermissions {
    public boolean canMove(World worldObj, int x, int y,int z, String username, UUID id);
}
