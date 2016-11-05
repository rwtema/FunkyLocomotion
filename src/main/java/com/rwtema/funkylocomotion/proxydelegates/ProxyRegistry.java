package com.rwtema.funkylocomotion.proxydelegates;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class ProxyRegistry {
	public static final HashMap<Class<?>, HashMap<Object, Object>> proxies
			= new HashMap<>();


	public static <T> T register(Object a, Class<? extends T> iface, T proxy) {
		Validate.isTrue(proxy != null);
		Validate.isTrue(a != null);
		Validate.isTrue(iface.isAssignableFrom(proxy.getClass()));

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
	public static <T> T getInterface(Object a, Class<? extends T> iface, Capability<T> capability) {
		if (a == null)
			return null;

		if (a instanceof ICapabilityProvider) {
			ICapabilityProvider capabilityProvider = (ICapabilityProvider) a;
			if (capabilityProvider.hasCapability(capability, null)) {
				T t = capabilityProvider.getCapability(capability, null);
				if (t != null)
					return t;
			}
		}

		Class<?> aClass = a.getClass();
		if (iface.isAssignableFrom(aClass)) {
			return (T) a;
		}

		HashMap<Object, Object> proxyMap = proxies.get(iface);

		if (proxyMap == null) return null;

		Object obj = proxyMap.get(a);
		if (obj == null) {
			if (proxyMap.containsKey(aClass)) {
				obj = proxyMap.get(aClass);
			} else {
				LinkedList<Class<?>> toCheck = new LinkedList<>();
				Collections.addAll(toCheck, aClass.getInterfaces());
				Class<?> poll;
				while ((poll = toCheck.poll()) != null) {
					obj = proxyMap.get(poll);
					if (obj != null) {
						proxyMap.put(aClass, obj);
						break;
					}
					Collections.addAll(toCheck, poll.getInterfaces());
				}
				if (obj == null)
					proxyMap.put(aClass, null);
			}
		}

		if (obj == null)
			return null;
		else
			return (T) obj;
	}
}
