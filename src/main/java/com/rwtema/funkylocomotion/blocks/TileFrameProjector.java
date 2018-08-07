package com.rwtema.funkylocomotion.blocks;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.rwtema.funkylocomotion.api.IAdvStickyBlock;
import com.rwtema.funkylocomotion.api.IMoveCheck;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

public class TileFrameProjector extends TileEntity implements IAdvStickyBlock, IMoveCheck {
	public final static int MAX_RANGE = 16;

	public int range;
	public EnumFacing facing;
	public boolean powered = true;

	public TileFrameProjector(EnumFacing facing) {
		this();
		this.facing = facing;
	}

	public TileFrameProjector() {
		super();
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setInteger("Facing", facing.ordinal());
		tag.setInteger("Range", range);
		tag.setBoolean("Powered", powered);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		facing = EnumFacing.values()[tag.getInteger("Facing")];
		range = tag.getInteger("Range");
		powered = tag.getBoolean("Powered");
		super.readFromNBT(tag);
	}

	@Override
	public Iterable<BlockPos> getBlocksToMove(World world, BlockPos pos) {
		if (!powered) return ImmutableList.of();
		final int range = this.range;
		if (range <= 0) return ImmutableList.of();
		final BlockPos center = this.pos;
		final EnumFacing facing = this.facing;
		if (center == null || facing == null) return ImmutableList.of();
		final EnumFacing.Axis axis = facing.getAxis();

		return () -> new Iterator<BlockPos>() {
			int r = range;
			Iterator<BlockPos.MutableBlockPos> curIterator;

			@Override
			public boolean hasNext() {
				if (curIterator != null) {
					if (curIterator.hasNext()) {
						return true;
					}
				}

				if (r == 0) return false;
				BlockPos a = center.offset(facing, r);

				int dx = axis != EnumFacing.Axis.X ? r : 0;
				int dy = axis != EnumFacing.Axis.Y ? r : 0;
				int dz = axis != EnumFacing.Axis.Z ? r : 0;

				curIterator = BlockPos.getAllInBoxMutable(
						a.add(-dx, -dy, -dz), a.add(dx, dy, dz)

				).iterator();
				r--;
				return true;
			}

			@Override
			public BlockPos next() {
				return curIterator.next();
			}
		};
	}


	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tag = super.getUpdateTag();
		tag.setInteger("Range", range);
		if (facing != null)
			tag.setInteger("Facing", facing.ordinal());
		tag.setBoolean("Powered", powered);
		return tag;
	}

	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		range = tag.getInteger("Range");
		facing = EnumFacing.values()[tag.getInteger("Facing")];
		powered = tag.getBoolean("Powered");
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
	public EnumActionResult canMove(World worldObj, BlockPos pos, @Nullable GameProfile profile) {
		return range == 0 ? EnumActionResult.FAIL : EnumActionResult.PASS;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return powered && super.shouldRenderInPass(pass);
	}
}
