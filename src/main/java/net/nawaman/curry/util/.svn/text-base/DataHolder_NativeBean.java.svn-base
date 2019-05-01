package net.nawaman.curry.util;

import java.lang.reflect.Method;

import net.nawaman.curry.*;
import net.nawaman.util.UClass;
import net.nawaman.util.UObject;

abstract public class DataHolder_NativeBean implements DataHolder {

	abstract protected Engine getEngine();
	abstract protected Object getTheObject();
	abstract protected Method getReadMethod();
	abstract protected Method getWriteMethod();
	
	/** Sets the value to the holder and return true if success. */
	public Object setData(Object pValue) {
		Method M = this.getWriteMethod();
		if(M == null) throw new RuntimeException("Internal Error: The native-bean dataholder has no method.");
		try {
			UClass.invokeMethod(M, this.getTheObject(), new Object[] { pValue });
			return pValue;
		} catch(Exception E) {
			throw new RuntimeException("There is an error setting value of a dataholder field.", E);
		}
	}
	/** Returns the value that this data holder holds. */
	public Object getData() {
		Method M = this.getReadMethod();
		if(M == null) throw new RuntimeException("Internal Error: The native-bean dataholder has no method.");
		try {
			return UClass.invokeMethod(M, this.getTheObject(), UObject.EmptyObjectArray);
		} catch(Exception E) {
			throw new RuntimeException("There is an error getting value of a dataholder field.", E);
		}
	}

	/** Returns the data type of the DataHolder. */
	public Type getType() {
		Method RM = this.getReadMethod();
		Method WM = this.getWriteMethod();
		if((RM == null) && (WM == null)) return TKJava.TVoid;
		if(this.getEngine() == null)     return TKJava.TAny;
		if (RM != null) return this.getEngine().getTypeManager().getTypeOfTheInstanceOf(RM.getReturnType());
		else {
			if(WM.getParameterTypes().length == 0) return TKJava.TVoid;
			return this.getEngine().getTypeManager().getTypeOfTheInstanceOf(WM.getParameterTypes()[0]);
		}
	}
	
	/** Checks if the DataHolder is readable. */
	public boolean isReadable() { return this.getReadMethod()  != null; }
	/** Checks if the DataHolder is writable. */
	public boolean isWritable() { return this.getWriteMethod() != null; }
	
	/** Checks if type checking MUST NOT be done when set. */
	public boolean isNoTypeCheck() { return false; }
	
	/** Performs advance configuration to the data holder. */
	public Object config(String pName, Object[] pParams) { return null; }
	/** Returns more information about the DataHolder. */
	public Object getMoreInfo(String pName)              { return null; }
	
	/** Returns a clone of this DataHolder. */
	@Override abstract public DataHolder clone();
	
}