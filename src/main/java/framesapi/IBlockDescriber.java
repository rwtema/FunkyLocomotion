package framesapi;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IBlockDescriber<V> {
    public NBTTagCompound getDescriptor(World world, BlockPos pos);

    @SideOnly(Side.CLIENT)
    public V createRenderCache(NBTTagCompound tagCompound);

    @SideOnly(Side.CLIENT)
    public void render(IBlockAccess fakeWorld, BlockPos pos, Block block, int meta, V cache);
}
