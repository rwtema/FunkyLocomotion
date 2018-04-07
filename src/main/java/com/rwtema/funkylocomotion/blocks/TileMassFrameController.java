package com.rwtema.funkylocomotion.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.mojang.authlib.GameProfile;
import com.rwtema.funkylocomotion.helper.NullHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

public class TileMassFrameController extends TileMassFrame {
	public BlockPos start = BlockPos.ORIGIN;
	public BlockPos end = BlockPos.ORIGIN;
	int energyRequirements = -1;

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		start = new BlockPos(
				tag.getInteger("start_x"),
				tag.getInteger("start_y"),
				tag.getInteger("start_z"));
		end = new BlockPos(
				tag.getInteger("end_x"),
				tag.getInteger("end_y"),
				tag.getInteger("end_z"));
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setInteger("start_x", start.getX());
		tag.setInteger("start_y", start.getY());
		tag.setInteger("start_z", start.getZ());
		tag.setInteger("end_x", end.getX());
		tag.setInteger("end_y", end.getY());
		tag.setInteger("end_z", end.getZ());
		return super.writeToNBT(tag);
	}

	public void recalcEnergy() {
		energyRequirements = 0;
		if (start.equals(end) || TilePusher.powerPerTile == 0) return;
		int numBlocks = 0;
		for (BlockPos.MutableBlockPos blockPos : BlockPos.getAllInBoxMutable(pos.add(start), pos.add(end))) {
			IBlockState blockState = world.getBlockState(blockPos);
			Block block = blockState.getBlock();
			if (!(block == NullHelper.notNull(FLBlocks.MASS_FRAME_CORNER) ||
					block == NullHelper.notNull(FLBlocks.MASS_FRAME_EDGE) ||
					block.isAir(blockState, world, blockPos))) {
				numBlocks++;
				if (numBlocks > TilePusher.maxTiles) {
					break;
				}
			}
		}
		energyRequirements = numBlocks * TilePusher.powerPerTile;
	}

	@Override
	public EnumActionResult canMove(World worldObj, BlockPos pos, @Nullable GameProfile profile) {
		BlockPos end = getPos().add(this.end);
		BlockPos start = getPos().add(this.start);
		if (!BlockMassFrameCorner.checkEdgeFrame(world, null, start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ())) {
			return EnumActionResult.FAIL;
		}
		return EnumActionResult.SUCCESS;
	}

	@Override
	public Iterable<BlockPos> getBlocksToMove(World world, BlockPos pos) {
		recalcEnergy();
		if (start.equals(end)) return ImmutableSet.of();

		BlockPos end = getPos().add(this.end);
		BlockPos start = getPos().add(this.start);

		if (!BlockMassFrameCorner.checkEdgeFrame(world, null, start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ())) {
			return ImmutableList.of();
		}

		Stream<BlockPos> posStream = Streams.stream(BlockPos.getAllInBox(start, end));
		return (posStream::iterator);
	}
}
