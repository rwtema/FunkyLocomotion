package com.rwtema.funkylocomotion.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.EnergyStorage;

public class EnergyStorageSerializable extends EnergyStorage {
	public EnergyStorageSerializable(int capacity) {
		super(capacity);
	}

	public EnergyStorageSerializable(int capacity, int maxTransfer) {
		super(capacity, maxTransfer);
	}

	public EnergyStorageSerializable(int capacity, int maxReceive, int maxExtract) {
		super(capacity, maxReceive, maxExtract);
	}

	public void readFromNBT(NBTTagCompound tag) {
		energy = tag.getInteger("energy");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setInteger("energy", energy);
		return tag;
	}
}
