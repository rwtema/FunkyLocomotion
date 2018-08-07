package com.rwtema.funkylocomotion.compat;

import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import com.rwtema.funkylocomotion.api.FunkyCapabilities;
import com.rwtema.funkylocomotion.api.FunkyRegistry;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.Validate;

@ModCompat(modid = "buildcrafttransport")
public class BCCompat extends CompatHandler {

	@Override
	public void init() {
		Validate.notNull(FunkyRegistry.INSTANCE).registerProxy(IPipeHolder.class, FunkyCapabilities.STICKY_BLOCK, (world, pos, side) -> {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof IPipeHolder) {
				IPipeHolder pipeHolder = (IPipeHolder) tile;
				PipePluggable pluggable = pipeHolder.getPluggable(side);
				if (pluggable instanceof IFacade) {
					IFacade facade = (IFacade) pluggable;
					for (IFacadePhasedState phasedState : facade.getPhasedStates()) {
						IBlockState facadeState = phasedState.getState().getBlockState();
						Block facadeBlock = facadeState.getBlock();
						if (facadeBlock instanceof BlockStickyFrame) {
							return BlockStickyFrame.isStickySide(facadeState, side);
						}
					}
				}
			}
			return false;
		});
	}
}
