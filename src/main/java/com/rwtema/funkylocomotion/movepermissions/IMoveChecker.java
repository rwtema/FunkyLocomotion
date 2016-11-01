package com.rwtema.funkylocomotion.movepermissions;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IMoveChecker {
	public boolean preventMovement(World world, int x, int y, int z, Block block, int meta, TileEntity tile);
}
