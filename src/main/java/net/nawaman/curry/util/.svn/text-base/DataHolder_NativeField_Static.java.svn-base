package net.nawaman.curry.util;

import java.lang.reflect.Field;

import net.nawaman.curry.Engine;

public class DataHolder_NativeField_Static extends DataHolder_NativeField {
	
	public DataHolder_NativeField_Static(Engine pEngine, Field pField) {
		this.Engine   = pEngine;
		this.TheField = pField;
	}

	Engine Engine   = null;
	Field  TheField = null;

	@Override protected Engine getEngine()    { return this.Engine;   }
	@Override protected Object getTheObject() { return null;          }
	@Override protected Field  getTheField()  { return this.TheField; }
	
	@Override public DataHolder clone() { return new DataHolder_NativeField_Static(this.Engine, this.TheField); }

}
