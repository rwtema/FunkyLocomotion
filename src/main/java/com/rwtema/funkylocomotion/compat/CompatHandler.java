package com.rwtema.funkylocomotion.compat;

import com.google.common.base.Throwables;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

public abstract class CompatHandler {
	public static void initCompat(ASMDataTable asmData) {
		for (ASMDataTable.ASMData data : asmData.getAll(ModCompat.class.getName())) {
			String modid = data.getAnnotationInfo().get("modid").toString();
			String classname = data.getAnnotationInfo().get("classname").toString();

			boolean hasModID = !StringUtils.isNullOrEmpty(modid);
			boolean hasClassname = !StringUtils.isNullOrEmpty(classname);
			if (!hasModID && !hasClassname) {
				throw new IllegalStateException("Both Mod ID and classname are blank");
			}

			boolean flag = hasModID && (Loader.isModLoaded(modid) || ModAPIManager.INSTANCE.hasAPI(modid));
			if (!flag && hasClassname) {
				try {
					Class.forName(classname);
					flag = true;
				} catch (ClassNotFoundException ignore) {
					flag = false;
				}
			}

			if (flag) {
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
