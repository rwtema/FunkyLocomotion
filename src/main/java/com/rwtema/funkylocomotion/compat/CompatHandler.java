package com.rwtema.funkylocomotion.compat;

import com.google.common.base.Throwables;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

public abstract class CompatHandler {
	public static void initCompat(ASMDataTable asmData) {
		for (ASMDataTable.ASMData data : asmData.getAll(ModCompat.class.getName())) {
			String modid = data.getAnnotationInfo().get("modid").toString();

			if (Loader.isModLoaded(modid) || ModAPIManager.INSTANCE.hasAPI(modid)) {
				try {
					Class<? extends CompatHandler> name = (Class<? extends CompatHandler>) Class.forName(data.getClassName());
					CompatHandler compatHandler = name.newInstance();
					compatHandler.init();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					throw Throwables.propagate(e);
				}
			}
		}

	}

	public abstract void init();
}
