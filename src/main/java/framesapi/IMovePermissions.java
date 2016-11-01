package framesapi;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IMovePermissions {
	boolean canMove(World worldObj, BlockPos pos, @Nullable GameProfile profile);
}
