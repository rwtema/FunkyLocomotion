package com.rwtema.funkylocomotion.compat;

import cofh.core.render.IBlockAppearance;
import com.rwtema.funkylocomotion.api.FunkyCapabilities;
import com.rwtema.funkylocomotion.api.FunkyRegistry;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.Validate;

@ModCompat(modid = "cofhcore")
public class CoFHCompat extends CompatHandler {

	@Override
	public void init() {
		Validate.notNull(FunkyRegistry.INSTANCE).registerProxy(IBlockAppearance.class, FunkyCapabilities.STICKY_BLOCK, (world, pos, side) -> {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if (block instanceof IBlockAppearance) {
				IBlockState facade = ((IBlockAppearance) block).getVisualState(world, pos, side);

				Block facadeBlock = facade.getBlock();
				if (facadeBlock instanceof BlockStickyFrame) {
					return BlockStickyFrame.isStickySide(facade, side);
				}
			}
			return false;
		});
	}
}
