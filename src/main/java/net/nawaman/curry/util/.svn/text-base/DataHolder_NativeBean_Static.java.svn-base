package net.nawaman.curry.util;

import java.lang.reflect.Method;

import net.nawaman.curry.Engine;

public class DataHolder_NativeBean_Static extends DataHolder_NativeBean {

	public DataHolder_NativeBean_Static(Engine pEngine, Method pReadMethod, Method pWriteMethod) {
		this.Engine      = pEngine;
		this.ReadMethod  = pReadMethod;
		this.WriteMethod = pWriteMethod;
	}
	
	Engine Engine      = null;
	Method ReadMethod  = null;
	Method WriteMethod = null;
	
	@Override protected Engine getEngine()      { return this.Engine;      }
	@Override protected Object getTheObject()   { return null;             }
	@Override protected Method getReadMethod()  { return this.ReadMethod;  }
	@Override protected Method getWriteMethod() { return this.WriteMethod; }
	
	@Override public DataHolder clone() { return new DataHolder_NativeBean_Static(this.Engine, this.ReadMethod, this.WriteMethod); }

}