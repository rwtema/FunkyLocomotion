package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.helper.BlockHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileSlider extends TilePusher {

	private static final int[][] orthog = {
			{6, 6, 5, 4, 3, 2, 6},
			{6, 6, 4, 5, 2, 3, 6},
			{5, 4, 6, 6, 1, 0, 6},
			{4, 5, 6, 6, 0, 1, 6},
			{3, 2, 1, 0, 6, 6, 6},
			{2, 3, 0, 1, 6, 6, 6},
			{6, 6, 6, 6, 6, 6, 6}
	};

	private EnumFacing slideDir = null;

	public static EnumFacing getOrthogonal(EnumFacing a, EnumFacing b) {
		int i = orthog[a.ordinal()][b.ordinal()];
		return i == 6 ? null : EnumFacing.values()[i];
	}

	public void rotateAboutAxis() {
		EnumFacing dir = getFacing();
		EnumFacing slide = getSlideDir();

		slideDir = slide.rotateAround(dir.getAxis());
	}

	public EnumFacing getSlideDir() {
		EnumFacing ang = getFacing();

		if (slideDir == null || getOrthogonal(ang, slideDir) == null) {
			int j = 0;
			while (j >= 6 || getOrthogonal(EnumFacing.values()[j], ang) == null)
				j = (j + 1) % 6;

			slideDir = EnumFacing.values()[j];
		}


		return slideDir;
	}

	public void setSlideDir(EnumFacing dir) {
		slideDir = dir;
	}

	public EnumFacing getFacing() {
		return EnumFacing.values()[getBlockMetadata() % 6];
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		slideDir = EnumFacing.values()[tag.getByte("SlideDirection")];
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("SlideDirection", (byte) slideDir.ordinal());
		return tag;
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

	@Override
	@Nonnull
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		tag.setByte("dir", (byte) getSlideDir().ordinal());
		return tag;
	}

	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		slideDir = EnumFacing.values()[tag.getByte("dir")];
		worldObj.markBlockRangeForRenderUpdate(pos, pos);
	}


	@Override
	public EnumFacing getDirection() {
		return getSlideDir();
	}

	@Override
	public List<BlockPos> getBlocks(World world, BlockPos home, EnumFacing dir, boolean push) {
		EnumFacing slide = getSlideDir();
		BlockPos advance = home.offset(dir);

		if (BlockHelper.canStick(world, advance, dir.getOpposite(), profile))
			return getBlocks(world, home, advance, slide);

		return null;
	}
}
