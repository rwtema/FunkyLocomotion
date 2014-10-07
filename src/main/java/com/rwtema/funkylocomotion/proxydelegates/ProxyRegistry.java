package com.rwtema.funkylocomotion.proxydelegates;

import java.util.HashMap;

public class ProxyRegistry {
    public static final HashMap<Class<?>, HashMap<Object, Object>> proxies
            = new HashMap<Class<?>, HashMap<Object, Object>>();

    public static <T> T register(Object a, T proxy, Class<? extends T> iface) {
        assert (proxy != null);
        assert (a != null);
        assert (iface.isAssignableFrom(proxy.getClass()));

        if (iface.isAssignableFrom(a.getClass()))
            return iface.cast(a);

        HashMap<Object, Object> h = proxies.get(iface);
        if (h == null) {
            h = new HashMap<Object, Object>();
            proxies.put(iface, h);
        }

        h.put(a, iface.cast(proxy));

        return iface.cast(proxy);
    }

    public static <T> T getInterface(Object a, Class<? extends T> iface) {
        if (a == null)
            return null;

        Class<?> aClass = a.getClass();
        if (iface.isAssignableFrom(aClass)) {
            return iface.cast(a);
        }

        if (!proxies.containsKey(iface))
            return null;

        HashMap<Object, Object> h = proxies.get(iface);
        Object obj = h.get(a);
        if (obj == null)
            obj = h.get(aClass);

        if (obj == null)
            return null;
        else
            return iface.cast(obj);
    }
}
