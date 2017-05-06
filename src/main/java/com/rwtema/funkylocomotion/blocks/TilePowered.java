package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.energy.EnergyStorageSerializable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public class TilePowered extends TileEntity {
	public final EnergyStorageSerializable energy;
	private final IEnergyStorage public_energy_wrapper;

	public TilePowered(int capacity) {
		energy = new EnergyStorageSerializable(capacity, capacity, capacity) {
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
				int i = super.extractEnergy(maxExtract, simulate);
				if (!simulate && i != 0) {
					markDirty();
				}
				return i;
			}
		};

		if (TilePusher.powerPerTile > 0) {
			public_energy_wrapper = new IEnergyStorage() {
				public int receiveEnergy(int maxReceive, boolean simulate) {
					return energy.receiveEnergy(maxReceive, simulate);
				}

				public int extractEnergy(int maxExtract, boolean simulate) {
					return 0;
				}

				public int getEnergyStored() {
					return energy.getEnergyStored();
				}

				public int getMaxEnergyStored() {
					return energy.getMaxEnergyStored();
				}

				public boolean canExtract() {
					return false;
				}

				public boolean canReceive() {
					return true;
				}
			};
		} else {
			public_energy_wrapper = null;
		}
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
		return (capability == CapabilityEnergy.ENERGY && TilePusher.powerPerTile > 0) || super.hasCapability(capability, facing);
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY && TilePusher.powerPerTile > 0) {
			return CapabilityEnergy.ENERGY.cast(public_energy_wrapper);
		}
		return super.getCapability(capability, facing);
	}
}
