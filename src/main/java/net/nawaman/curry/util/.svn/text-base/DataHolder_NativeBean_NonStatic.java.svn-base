package net.nawaman.curry.util;

import java.lang.reflect.*;

import net.nawaman.curry.Engine;

public class DataHolder_NativeBean_NonStatic extends DataHolder_NativeBean_Static {

	public DataHolder_NativeBean_NonStatic(Engine pEngine, Object pObject, Method pReadMethod, Method pWriteMethod) {
		super(pEngine, pReadMethod, pWriteMethod);
		this.TheObject   = pObject;
	}
	
	Object TheObject   = null;

	/**{@inheritDoc}*/ @Override
	protected Object getTheObject() {
		return this.TheObject;
	}
	
	/**{@inheritDoc}*/ @Override
	public DataHolder clone() {
		return new DataHolder_NativeBean_NonStatic(this.Engine, this.TheObject, this.ReadMethod, this.WriteMethod);
	}

}
