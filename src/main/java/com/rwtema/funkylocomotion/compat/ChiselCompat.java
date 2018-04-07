package com.rwtema.funkylocomotion.compat;

import com.rwtema.funkylocomotion.api.FunkyCapabilities;
import com.rwtema.funkylocomotion.api.FunkyRegistry;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.Validate;
import team.chisel.ctm.api.IFacade;

@ModCompat(modid = "ctm-api")
public class ChiselCompat extends CompatHandler {
	@Override
	public void init() {
		Validate.notNull(FunkyRegistry.INSTANCE).registerProxy(IFacade.class, FunkyCapabilities.STICKY_BLOCK, (world, pos, side) -> {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if (block instanceof IFacade) {
				IBlockState facade = ((IFacade) block).getFacade(world, pos, side);

				Block facadeBlock = facade.getBlock();
				if (facadeBlock instanceof BlockStickyFrame) {
					return BlockStickyFrame.isStickySide(facade, side);
				}
			}
			return false;
		});
	}
}
