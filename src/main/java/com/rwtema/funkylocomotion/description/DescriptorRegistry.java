package com.rwtema.funkylocomotion.description;

import framesapi.IDescriptionProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptorRegistry {
	private static final Map<String, IDescriptionProxy> proxyMap = new HashMap<>();
	private static final List<IDescriptionProxy> proxyList = new ArrayList<>();

	static {
		register(new DescribeVanilla(), false);
	}

	public static void register(IDescriptionProxy d, boolean priority) {
		if (proxyMap.containsKey(d.getID()))
			throw new RuntimeException(d.getID() + " already registered");

		proxyMap.put(d.getID(), d);
		if (priority || proxyList.isEmpty())
			proxyList.add(d);
		else
			proxyList.add(0, d);
	}

	public static List<IDescriptionProxy> getProxyList() {
		return proxyList;
	}

	public static IDescriptionProxy getDescriptor(String s) {
		return proxyMap.get(s);
	}


}
