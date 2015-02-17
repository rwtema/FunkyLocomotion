package com.rwtema.funkylocomotion;

import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogHelper {
    public static final Logger logger = LogManager.getLogger("newframes");

    public static final boolean isDeObf;

    static {
        boolean deObftemp;
        try {
            World.class.getMethod("getBlock", int.class, int.class, int.class);
            deObftemp = true;
        } catch (Throwable ex) {
            deObftemp = false;
        }

        isDeObf = deObftemp;
    }

    public static void debug(Object info, Object... info2) {
        if (isDeObf) {
            String temp = "Debug: " + info;
            for (Object t : info2)
                temp = temp + " " + t;

            logger.info(info);
        }
    }


    public static void info(Object info, Object... info2) {
        String temp = "" + info;
        for (Object t : info2)
            temp = temp + " " + t;

        logger.info(info);
    }

    public static void errorThrowable(String message, Throwable t) {
        logger.error(message, t);
    }

    public static void error(Object info, Object... info2) {
        String temp = "" + info;
        for (Object t : info2)
            temp = temp + " " + t;

        logger.error(info);
    }
}
