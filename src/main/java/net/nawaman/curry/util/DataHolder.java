package net.nawaman.curry.util;

import net.nawaman.curry.*;

/** An advance version of DataHolder */
public interface DataHolder extends net.nawaman.util.DataHolder, Cloneable {
	
	/** EnumType of access kind to data-holder */
	static public enum AccessKind { Set, Get, GetType, IsReadable, IsWritable, IsNotTypeCheck, Config, GetMoreInfo, Clone };
	
	/** Sets the value to the holder and return true if success. */
	public Object setData(Object pValue);
	/** Returns the value that this data holder holds. */
	public Object getData();

	/** Returns the data type of the DataHolder. */
	public Type getType();
	
	/** Checks if the DataHolder is readable. */
	public boolean isReadable();
	/** Checks if the DataHolder is writable. */
	public boolean isWritable();
	
	/** Checks if type checking MUST NOT be done when set. */
	public boolean isNoTypeCheck();
	
	/** Performs advance configuration to the data holder. */
	public Object config(String pName, Object[] pParams);
	/** Returns more information about the DataHolder. */
	public Object getMoreInfo(String pName);
	
	/** Returns a clone of this DataHolder. */
	public DataHolder clone();

}
