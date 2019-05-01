package net.nawaman.curry.util;

import net.nawaman.curry.Context;
import net.nawaman.curry.Engine;
import net.nawaman.curry.Type;

/** Factory for creating DataHolders */
public interface DataHolderFactory {
	
	/** Returns the name of the factory. */
	public String getName();
	
	// Assigned Initial Value
	public DataHolder newDataHolder(Context pContext, Engine pEngine, Type pType, Object pData, boolean pIsReadable,
			boolean pIsWritable, MoreData pMoreInfo, DataHolderInfo pDHInfo);
	
	// Default Initial Value
	public DataHolder newDataHolder(Context pContext, Engine pEngine, Type pType, boolean pIsReadable,
			boolean pIsWritable, MoreData pMoreInfo, DataHolderInfo pDHInfo);
	
	/** Checks if the given data holder is compatible with this DataHolder Factory*/
	public boolean isInstance(DataHolder DH);

}
