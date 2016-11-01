package com.rwtema.funkylocomotion.proxydelegates;

import java.util.HashMap;

public class ProxyRegistry {
	public static final HashMap<Class<?>, HashMap<Object, Object>> proxies
			= new HashMap<>();

	public static <T> T register(Object a, T proxy, Class<? extends T> iface) {
		assert (proxy != null);
		assert (a != null);
		assert (iface.isAssignableFrom(proxy.getClass()));

		if (iface.isAssignableFrom(a.getClass()))
			return iface.cast(a);

		HashMap<Object, Object> h = proxies.get(iface);
		if (h == null) {
			h = new HashMap<>();
			proxies.put(iface, h);
		}

		h.put(a, iface.cast(proxy));

		return iface.cast(proxy);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInterface(Object a, Class<? extends T> iface) {
		if (a == null)
			return null;

		Class<?> aClass = a.getClass();
		if (iface.isAssignableFrom(aClass)) {
			return (T) a;
		}

		HashMap<Object, Object> h = proxies.get(iface);

		if (h == null) return null;

		Object obj = h.get(a);
		if (obj == null) {
			if (h.containsKey(aClass)) {
				obj = h.get(aClass);
			} else {
				for (Class<?> interfaces : aClass.getInterfaces()) {
					obj = h.get(interfaces);
					if (obj != null) {
						h.put(aClass, obj);
						break;
					}
				}
				if (obj == null)
					h.put(aClass, null);
			}
		}

		if (obj == null)
			return null;
		else
			return (T) obj;
	}
}
