package com.rwtema.funkylocomotion.compat;

import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import com.rwtema.funkylocomotion.api.FunkyCapabilities;
import com.rwtema.funkylocomotion.api.FunkyRegistry;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.Validate;

@ModCompat(modid = "appliedenergistics2")
public class AE2Compat extends CompatHandler {

	@Override
	public void init() {
		Validate.notNull(FunkyRegistry.INSTANCE).registerProxy(IPartHost.class, FunkyCapabilities.STICKY_BLOCK, (world, pos, side) -> {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof IPartHost) {
				IPartHost partHost = (IPartHost) tile;
				IFacadeContainer facadeContainer = partHost.getFacadeContainer();
				AEPartLocation loc = AEPartLocation.fromFacing(side);
				IFacadePart facade = facadeContainer.getFacade(loc);
				if(facade != null) {
					IBlockState facadeState = facade.getBlockState();
					Block facadeBlock = facadeState.getBlock();
					if (facadeBlock instanceof BlockStickyFrame) {
						return BlockStickyFrame.isStickySide(facadeState, side);
					}
				}
			}
			return false;
		});
	}
}
