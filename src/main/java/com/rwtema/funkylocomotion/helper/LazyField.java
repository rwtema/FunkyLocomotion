package com.rwtema.funkylocomotion.helper;

import java.util.function.Supplier;

public class LazyField<T> implements Supplier<T> {
	private final static Object NULL = new Object();
	private final Supplier<T> supplier;
	Object value;

	public LazyField(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get() {
		Object value = this.value;
		if (value == null) {
			T t = supplier.get();
			value = this.value = (t != null ? t : NULL);
		}
		return value != NULL ? (T) value : null;
	}

	@SuppressWarnings("unchecked")
	public T compute() {
		T t = supplier.get();
		Object value = this.value = (t != null ? t : NULL);
		return value != NULL ? (T) value : null;
	}

	public void clearCache() {
		this.value = null;
	}
}
