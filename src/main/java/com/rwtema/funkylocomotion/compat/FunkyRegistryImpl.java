package com.rwtema.funkylocomotion.compat;

import com.rwtema.funkylocomotion.api.FunkyRegistry;
import com.rwtema.funkylocomotion.api.IMoveFactory;
import com.rwtema.funkylocomotion.factory.FactoryRegistry;
import com.rwtema.funkylocomotion.proxydelegates.ProxyRegistry;
import net.minecraft.block.Block;
import net.minecraftforge.common.capabilities.Capability;

public class FunkyRegistryImpl extends FunkyRegistry {
	@Override
	public void registerMoveFactoryBlock(Block b, IMoveFactory factory) {
		FactoryRegistry.moveFactoryMapBlock.put(b, factory);
	}

	@Override
	public void registerMoveFactoryTileEntityClass(Class<?> tile, IMoveFactory factory) {
		FactoryRegistry.moveFactoryMapInheritanceClass.put(tile, factory);
	}

	@Override
	public void registerMoveFactoryBlockClass(Class<? extends Block> b, IMoveFactory factory) {
		FactoryRegistry.moveFactoryMapBlockClass.put(b, factory);
	}

	@Override
	public <T> void registerProxy(Object object, Capability<T> capability, T type) {
		try {
			ProxyRegistry.register(object, Class.forName(capability.getName()), type);
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException(e);
		}
	}
}
