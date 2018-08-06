package com.rwtema.funkylocomotion.dispenser;

import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class WrenchDispenserAction extends BehaviorDefaultDispenseItem {

	@Nonnull
	@Override
	public ItemStack dispenseStack(IBlockSource pos, ItemStack stack) {
//        if (stack.getItem() == FunkyLocomotion.wrench) {
//            EnumFacing enumfacing = (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING);
//
//            World world = pos.getWorld();
//            BlockPos p = new BlockPos(
//                    pos.getX() + facing.getFrontOffsetX(),
//                    pos.getY() + facing.getFrontOffsetY(),
//                    pos.getZ() + facing.getFrontOffsetZ());
//			Block block = world.getBlockState(p).getBlock();
//
//        }

		return stack;
	}
}
