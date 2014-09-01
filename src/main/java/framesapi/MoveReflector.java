package framesapi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class MoveReflector {

    private static HashMap<Class<?>, Boolean> cache = new HashMap<Class<?>, Boolean>();

    public static boolean canMoveClass(Class<?> clazz) {
        Boolean b = cache.get(clazz);
        if (b == null) {
            b = _canMoveClass(clazz);
            cache.put(clazz, b);
        }
        return b;
    }

    private static boolean _canMoveClass(Class<?> clazz) {
        try {
            Method method = clazz.getMethod("_Immoveable");
            if (method.getReturnType() == boolean.class) {
                Boolean b = (Boolean) method.invoke(null);
                return b == null || !b;
            }
            return true;
        } catch (NoSuchMethodException e) {
            return true;
        } catch (InvocationTargetException e) {
            return true;
        } catch (IllegalAccessException e) {
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return true;
        }
    }
}
