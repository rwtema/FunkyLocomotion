package framesapi;

import net.minecraft.world.World;

import java.util.UUID;

public interface IMovePermissions {
    public boolean canMove(World worldObj, BlockPos pos, String username, UUID id);
}
