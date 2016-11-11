package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.energy.EnergyStorageSerializable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public class TilePowered extends TileEntity {
	public final EnergyStorageSerializable energy;

	public TilePowered(int capacity) {
		energy = new EnergyStorageSerializable(capacity, capacity, 0) {
			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {
				int i = super.receiveEnergy(maxReceive, simulate);
				if (!simulate && i != 0) {
					markDirty();
				}
				return i;
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) {
				return 0;
			}
		};
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		energy.readFromNBT(tag);
	}

	@Nonnull
	@Override
	@OverridingMethodsMustInvokeSuper
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		energy.writeToNBT(tag);
		return tag;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(energy);
		}
		return super.getCapability(capability, facing);
	}
}
