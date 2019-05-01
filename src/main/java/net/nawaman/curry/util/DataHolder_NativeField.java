package net.nawaman.curry.util;

import java.lang.reflect.Field;

import net.nawaman.curry.Engine;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;

abstract class DataHolder_NativeField implements DataHolder {

	abstract protected Engine getEngine();
	abstract protected Object getTheObject();
	abstract protected Field  getTheField();
	
	/** Sets the value to the holder and return true if success. */
	public Object setData(Object pData) {
		Field F = this.getTheField();
		if(F == null) throw new RuntimeException("Internal Error: The native-bean dataholder has no method.");
		try {
			F.set(this.getTheObject(), pData);
			return pData;
		} catch(Exception E) {
			throw new RuntimeException("There is an error setting value of a dataholder field.", E);
		}
	}
	/** Returns the value that this data holder holds. */
	public Object getData() {
		Field F = this.getTheField();
		if(F == null) throw new RuntimeException("Internal Error: The native-bean dataholder has no method.");
		try {
			return F.get(this.getTheObject());
		} catch(Exception E) {
			throw new RuntimeException("There is an error getting value of a dataholder field.", E);
		}
	}

	/** Checks if the DataHolder is readable. */
	public boolean isReadable() { return true; }
	/** Checks if the DataHolder is writable. */
	public boolean isWritable() { return true; }
	
	/** Checks if type checking MUST NOT be done when set. */
	public boolean isNoTypeCheck() { return false; }

	/** Returns the data type of the DataHolder. */
	public Type getType() {
		Field F = this.getTheField();
		if(F == null) return TKJava.TVoid;
		if(this.getEngine() == null) return TKJava.TAny;
		else return this.getEngine().getTypeManager().getTypeOfTheInstanceOf(F.getType());
	}
	
	@Override abstract public DataHolder clone();

	/** Performs advance configuration to the data holder. */
	public Object config(String pName, Object[] pParams) { return null; }
	/** Returns more information about the DataHolder. */
	public Object getMoreInfo(String pName)              { return null; }
}