package net.nawaman.curry.util;

import net.nawaman.curry.Type;

public interface Iterator<T> extends java.util.Iterator<T> {
	
	public Type getType();
	
    public boolean hasNext();
    public T       next();
    
}