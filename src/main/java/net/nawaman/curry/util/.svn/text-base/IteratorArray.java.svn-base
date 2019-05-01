package net.nawaman.curry.util;

import net.nawaman.curry.Type;
import net.nawaman.util.Resetable;
import net.nawaman.util.UArray;

public class IteratorArray<T> implements net.nawaman.curry.util.Iterator<T>, Resetable {
		
	public IteratorArray(Type pType, Object pArray) {
		if(!UArray.isArrayInstance(pArray)) throw new IllegalArgumentException("An array is needed.");
		this.Type  = pType; this.Array = pArray;
	}

	int    Index =   -1;
	Type   Type  = null;
	Object Array = null;
	
	public Type getType() { return this.Type; }
	
	public boolean reset() { this.Index = -1; return true; }
	
    public boolean hasNext() { return (this.Index < (UArray.getLength(Array) - 1)); }
    @SuppressWarnings("unchecked")
    public T       next() {
    	if(!this.hasNext()) return null;
    	this.Index++;
    	return (T)UArray.get(Array, Index);
    }
    
    public void remove() {}
    
    @Override public String toString() { return UArray.toString(this.Array); }
	
}