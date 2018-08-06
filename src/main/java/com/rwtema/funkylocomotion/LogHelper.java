package com.rwtema.funkylocomotion;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogHelper {
	public static final Logger logger = LogManager.getLogger("newframes");

	public static final boolean isDeObf;

	static {
		boolean deObftemp;
		try {
			World.class.getMethod("getBlockState", BlockPos.class);
			deObftemp = true;
		} catch (Throwable ex) {
			deObftemp = false;
		}

		isDeObf = deObftemp;
	}

	public static void debug(Object info, Object... info2) {
		if (isDeObf) {
			StringBuilder temp = new StringBuilder("Debug: " + info);
			for (Object t : info2)
				temp.append(" ").append(t);

			logger.info(temp.toString());
		}
	}


	public static void info(Object info, Object... info2) {
		StringBuilder temp = new StringBuilder("" + info);
		for (Object t : info2)
			temp.append(" ").append(t);

		logger.info(temp.toString());
	}

	public static void errorThrowable(String message, Throwable t) {
		logger.error(message, t);
	}

	public static void error(Object info, Object... info2) {
		StringBuilder temp = new StringBuilder("" + info);
		for (Object t : info2)
			temp.append(" ").append(t);

		logger.error(temp.toString());
	}
}
