package com.rwtema.funkylocomotion.blocks;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import com.rwtema.funkylocomotion.api.IAdvStickyBlock;
import com.rwtema.funkylocomotion.api.IMoveCheck;
import com.rwtema.funkylocomotion.energy.EnergyStorageSerializable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.EnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TileMassFrame extends TileEntity implements IAdvStickyBlock, IMoveCheck {
	public static final int MAX_RANGE = 64;
	@Nonnull
	BlockPos controllerPos = BlockPos.ORIGIN;

	public TileMassFrame() {
		super();
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		controllerPos = new BlockPos(
				tag.getInteger("offset_x"),
				tag.getInteger("offset_y"),
				tag.getInteger("offset_z"));

	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setInteger("offset_x", controllerPos.getX());
		tag.setInteger("offset_y", controllerPos.getY());
		tag.setInteger("offset_z", controllerPos.getZ());
		return super.writeToNBT(tag);
	}

	private Optional<TileMassFrameController> getController() {
		if (world == null || BlockPos.ORIGIN.equals(controllerPos)) {
			return Optional.empty();
		}
		TileEntity tileEntity = world.getTileEntity(pos.add(controllerPos));
		if (tileEntity instanceof TileMassFrameController) {
			return Optional.of((TileMassFrameController) tileEntity);
		}
		return Optional.empty();
	}

	@Override
	public Iterable<BlockPos> getBlocksToMove(World world, BlockPos pos) {
		return getController().map(t -> t.getBlocksToMove(world, pos)).orElse(ImmutableSet.of());
	}

	@Override
	public EnumActionResult canMove(World worldObj, BlockPos pos, @Nullable GameProfile profile) {
		return getController().map(t -> t.canMove(worldObj, pos, profile)).orElse(EnumActionResult.FAIL);
	}
}
