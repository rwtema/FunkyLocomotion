package com.rwtema.funkylocomotion.dispenser;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.blocks.BlockFrame;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import framesapi.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class WrenchDispenserAction extends BehaviorDefaultDispenseItem {

    @Override
    public ItemStack dispenseStack(IBlockSource pos, ItemStack stack) {
        if (stack.getItem() == FunkyLocomotion.wrench) {
            EnumFacing facing = BlockDispenser.func_149937_b(pos.getBlockMetadata());
            World world = pos.getWorld();
            BlockPos p = new BlockPos(
                    pos.getXInt() + facing.getFrontOffsetX(),
                    pos.getYInt() + facing.getFrontOffsetY(),
                    pos.getZInt() + facing.getFrontOffsetZ());
            Block block = BlockHelper.getBlock(world, p);
            if ( block instanceof BlockFrame) {
                BlockHelper.breakBlockWithDrop(world, p);
            }
        }

        return stack;
    }
}
