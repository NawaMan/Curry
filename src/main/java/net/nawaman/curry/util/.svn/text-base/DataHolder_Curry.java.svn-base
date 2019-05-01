package net.nawaman.curry.util;

import net.nawaman.curry.*;

public interface DataHolder_Curry extends DataHolder, Cloneable {

	/** Checks if this DataHolder_Curry can also be run as a normal DataHolder */
	public boolean isAlsoNormalDataHolder();

	/** Sets the value to the holder and return true if success. */
	public Executable getExpr_setData(Engine pEngine, Object pData);
	/** Returns the value that this data holder holds. */
	public Executable getExpr_getData(Engine pEngine);
	
	/** Returns the data type of the dataholder. */
	public Executable getExpr_getType(Engine pEngine);

	/** Checks if the dataholder is readable. */
	public Executable getExpr_isReadable(Engine pEngine);
	/** Checks if the dataholder is writable. */
	public Executable getExpr_isWritable(Engine pEngine);
	
	/** Checks if type cheching MUST NOT be done when set. */
	public Executable getExpr_isNoTypeCheck(Engine pEngine);
	
	/** Returns a clone of this dataholder. */
	public Executable getExpr_clone(Engine pEngine);

	/** Performs advance configuration to the data holder. */
	public Executable getExpr_config(Engine pEngine, String pName, Object[] pParams);
	/** Returns more information about the dataholder. */
	public Executable getExpr_getMoreInfo(Engine pEngine, String pName);
	
}
