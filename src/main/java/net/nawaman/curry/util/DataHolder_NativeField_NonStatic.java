package net.nawaman.curry.util;

import java.lang.reflect.Field;

import net.nawaman.curry.Engine;

public class DataHolder_NativeField_NonStatic extends DataHolder_NativeField_Static {
	
	public DataHolder_NativeField_NonStatic(Engine pEngine, Object pObject, Field pField) {
		super(pEngine, pField);
		this.TheObject = pObject;
	}

	Object TheObject = null;

	@Override protected Object getTheObject() { return this.TheObject; }
	
	@Override public DataHolder clone() { return new DataHolder_NativeField_NonStatic(this.Engine, this.TheObject, this.TheField); }

}
