package com.maicard.base;

import java.util.Map.Entry;

public class Pair<K, V> implements Entry<K,V>{

	public  K key;

	public  V value;
	
	public Pair(){}

	public Pair(K key, V value)
	{
		this.key = key;
		this.value = value;
	}

	public String toString()
	{
		return "Pair[" + key + "," + value + "]";
	}

	private static boolean equals(Object x, Object y)
	{
		return (x == null && y == null) || (x != null && x.equals(y));
	}

	public boolean equals(Object other)
	{
		return other instanceof Pair<?, ?> && equals(key, ((Pair<?, ?>)other).key)
				&& equals(value, ((Pair<?, ?>)other).value);
	}

	public int hashCode()
	{
		if (key == null)
			return (value == null) ? 0 : value.hashCode() + 1;
		else if (value == null)
			return key.hashCode() + 2;
		else
			return key.hashCode() * 17 + value.hashCode();
	}

	public static <A, B> Pair<A, B> of(A a, B b)
	{
		return new Pair<A, B>(a, b);
	}

	@Override
	public K getKey() {
 		return key;
	}

	@Override
	public V getValue() {
 		return value;
	}

	@Override
	public V setValue(V v) {
		value = v;
 		return v;
	}
}