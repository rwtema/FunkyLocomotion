package com.rwtema.funkylocomotion.helper;

import javax.annotation.Nonnull;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakSet<E> extends AbstractSet<E> implements Set<E> {
	private static final Object BLANK = new Object();
	private final WeakHashMap<E, Object> map = new WeakHashMap<>();

	@Override
	public boolean add(E e) {
		return !map.containsKey(e) && map.put(e, BLANK) == null;
	}

	@Nonnull
	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public boolean remove(Object o) {
		return map.remove(o) == BLANK;
	}

	@Override
	public void clear() {
		map.clear();
	}
}
