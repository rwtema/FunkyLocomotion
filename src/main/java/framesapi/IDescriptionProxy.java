package framesapi;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IDescriptionProxy {
	String getID();

	boolean canHandleTile(TileEntity tile);

	void addDescriptionToTags(NBTTagCompound descriptor, TileEntity tile);

	@SideOnly(Side.CLIENT)
	TileEntity recreateTileEntity(NBTTagCompound tag, IBlockState state, BlockPos pos, World world);
}
