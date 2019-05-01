package net.nawaman.curry;

import java.util.Vector;

import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.DataHolderFactory;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.DataHolder_Curry;
import net.nawaman.curry.util.MoreData;
import net.nawaman.curry.util.UCurry;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

/** DataHolder Manager */
public class MDataHolder extends EnginePart {
	
	protected MDataHolder(Engine pEngine) {
		super(pEngine);
	}
	
	/**{@inheritDoc}*/ @Override public MDataHolder getDataHolderManager() {
		return this.TheEngine.getDataHolderManager();
	}
	
	// DataHolder Services ---------------------------------------------------------------------------------------------
	
	/** DataHolder factories. */
	final Vector<DataHolderFactory> DHFs = new Vector<DataHolderFactory>();
	
	       DataHolderFactory DefaultDataHolderFactory = null;
	/** Returns DefaultDataHolderFactory */
	public DataHolderFactory getDefaultDataHolderFactory() {
		return this.DefaultDataHolderFactory;
	}

	/** Register a DataHolder factory and returns an error string (null if no error). */
	boolean regDataHolderFactory(DataHolderFactory pDHF) {
		// Precondition
		if(this.TheEngine.IsInitialized) { this.TheEngine.showErr(this.TheEngine.getEngineSpec().getEngineAlreadyInitializedMsg()); return false; }
		if(pDHF == null)                 { this.TheEngine.showErr(this.TheEngine.getEngineSpec().getNullDataHolderFactoryRegErr()); return false; }
		// Register
		this.DHFs.add(pDHF);
		return true;
	}
	
	/** Returns the index associated with the DataHolder factory named pName. */
	int getDataHolderFactoryIndex(String pName) {
		if(pName == null) return -1;
		for(int i = this.DHFs.size(); --i >= 0; ) {
			if(UString.equal(pName,this.DHFs.get(i).getName())) return i;
		}
		return -1;
	}
	/** Checks if the DataHolder factory named pName exists. */
	public boolean isDataHolderFactoryExist(String pName) {
		int Index = this.getDataHolderFactoryIndex(pName);
		return (Index != -1);
	}
	/** Returns the DataHolder factory named pName */
	public DataHolderFactory getDataHolderFactory(String pName) {
		int Index = this.getDataHolderFactoryIndex(pName);
		if(Index == -1) return this.DefaultDataHolderFactory;
		return this.DHFs.get(Index);
	}
	
	// Without Context -----------------------------------------------

	/** Create a new DataHolder from the DataHolder info */
	public DataHolder newDH(DataHolderInfo DHI) {
		return this.newDH(null, DHI);
	}
	/** Set value to the DataHolder */
	public Object setDHData(String pName, DataHolder DH, Object pData) {
		return this.setDHData(null, pName, DH, pData);
	}
	/** Get value from the DataHolder */
	public Object getDHData(String pName, DataHolder DH) {
		return this.getDHData(null, pName, DH);
	}	
	/** Check if the DataHolder is readable */
	public boolean isDHReadable(String pName, DataHolder DH) {
		return this.isDHReadable(null, pName, DH);
	}
	/** Check if the DataHolder is writable */
	public boolean isDHWritable(String pName, DataHolder DH) {
		return this.isDHWritable(null, pName, DH);
	}
	/** Get the DataHolder type */
	public Type getDHType(String pName, DataHolder DH) {
		return this.getDHType(null, pName, DH);
	}	
	/** Returns a clone of this DataHolder. */
	public DataHolder cloneDH(String pName, DataHolder DH) {
		return this.cloneDH(null, pName, DH);
	}
	/** Performs advance configuration to the data holder. */
	public Object configDH(DataHolder DH, String pCName, Object[] pParams) {
		return this.configDH(null, DH, pCName, pParams);
	}
	/** Performs advance configuration to the data holder. */
	public Object getDHMoreInfo(DataHolder DH, String pMIName) {
		return this.getDHMoreInfo(null, DH, pMIName);
	}
	
	// With Context --------------------------------------------------
	
	// DataHolder --------------------------------------------

	/** Create a DataHolder from the DataHolder info */
	DataHolder newDH(Context pContext, DataHolderInfo DHI) {
		DataHolderFactory DHF = this.getDataHolderFactory(DHI.getDHFactoryName());
		if(DHF == null) throw new CurryError("Unknown DataHolder factory (" + DHI.getDHFactoryName() + ").", pContext);
		Type T = this.TheEngine.getTypeManager().getTypeFromRefNoCheck(pContext, DHI.getTypeRef());
		DataHolder DH = null;
		if(DHI.isSet()) {
			Object O = DHI.getIValue();
			if(DHI.isExpression() && (O instanceof Expression))
				O = (new Executor(this.TheEngine)).execExternal(pContext, (Expression)((Expression)O).clone());
			   DH = DHF.newDataHolder(pContext, this.getEngine(), T, O, DHI.isReadable(), DHI.isWritable(), DHI.getMoreInfo(), DHI);
		} else DH = DHF.newDataHolder(pContext, this.getEngine(), T,    DHI.isReadable(), DHI.isWritable(), DHI.getMoreInfo(), DHI);
		if(DH == null) throw new CurryError("There is a problem creating a DataHolder. Perhaps the default value is not"
				+ " compatible with the variable type.", pContext);
		return DH;
	}

	/** Create a DataHolder from the DataHolder info */
	DataHolder newDH(Context pContext, Engine pEngine, String DHFName, Type pType, boolean pIsReadable,
			boolean pIsWritable, MoreData pMoreInfo) {
		return this.newDH(pContext, pEngine, DHFName, pType, false, null, pIsReadable, pIsWritable, pMoreInfo);
	}

	/** Create a DataHolder from the DataHolder info */
	DataHolder newDH(Context pContext, Engine pEngine, String DHFName, Type pType, Object pData,
			boolean pIsReadable, boolean pIsWritable, MoreData pMoreInfo) {
		return this.newDH(pContext, pEngine, DHFName, pType, true, pData, pIsReadable, pIsWritable, pMoreInfo);
	}

	/** Create a DataHolder from the DataHolder info */
	private DataHolder newDH(Context pContext, Engine pEngine, String DHFName, Type pType, boolean IsSet, Object pData,
			boolean pIsReadable, boolean pIsWritable, MoreData pMoreInfo) {
		
		DataHolderFactory DHF = this.getDataHolderFactory(DHFName);
		if(DHF == null) throw new CurryError("Unknown DataHolder factory (" + DHFName + ").", pContext);
		
		Type T = this.TheEngine.getTypeManager().getTypeFromRefNoCheck(pContext, pType.getTypeRef());
		DataHolder DH = null;
		if(IsSet) {
			   DH = DHF.newDataHolder(pContext, this.getEngine(), T, pData, pIsReadable, pIsWritable, pMoreInfo, null);
		} else DH = DHF.newDataHolder(pContext, this.getEngine(), T,        pIsReadable, pIsWritable, pMoreInfo, null);
		
		if(DH == null) {
			throw new CurryError(
				"There is a problem creating a DataHolder. Perhaps the default value is not compatible with " +
				"the variable type.", pContext
			);
		}
		return DH;
	}
	/** Set value to the DataHolder */
	Object setDHData(Context pContext, String pName, DataHolder DH, Object pData) {
		if(DH == null) return new NullPointerException();
		if(UCurry.isDataHolderNormal(DH)) {
			if(!DH.isWritable()) {
				if(pName == null) pName = "The DataHolder";
				throw new CurryError(pName + " is not writable." , pContext);
			}
			// Check Type if needed
			if(!DH.isNoTypeCheck() && !DH.getType().canBeAssignedBy(pData)) {
				if(pName == null) pName = "The DataHolder";
				throw new CurryError("In compatible type when set "+pName.toLowerCase()+"." , pContext);
			}
			return DH.setData(pData);
		}

		// Make Context; Do it here so it does not need to make in every operation below
		if(pContext == null) pContext = (new Executor(this.TheEngine)).newRootContext(null);
		
		if(!this.isDHReadable(pContext, pName, DH)) {
			if(pName == null) pName = "The DataHolder";
			throw new CurryError(pName + " is not writable." , pContext);
		}
		// Check Type if needed
		if(!this.isDHNoTypeCheck(pContext, pName, DH) && !this.getDHType(pContext, pName, DH).canBeAssignedBy(pData)) {
			if(pName == null) pName = "The DataHolder";
			throw new CurryError("In compatible type when set "+pName.toLowerCase()+"." , pContext);
		}
		Executable Exec = ((DataHolder_Curry)DH).getExpr_setData(this.TheEngine, pData);
		if(Exec == null) return false;
		
		Object Result = null;
		// Execute with an appropriate function
		if(!(Exec instanceof Expression)) {	// So it is other kind of executable			
			   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
					   new Object[] { pData }, false, true);
		} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);		
		// Process result
		if(Result == null)                  return false;
		if(Result instanceof Boolean)       return ((Boolean)Result).booleanValue();
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);
		
		pName = (pName == null)?"":" (" + pName + ")";
		throw new CurryError("Invalid expression result: `setData` expression of a DataHolder must return a boolean"
				+ pName+".", pContext);
	}
	/** Get value from the DataHolder */
	Object getDHData(Context pContext, String pName, DataHolder DH) {
		if(DH == null) return false;
		if(UCurry.isDataHolderNormal(DH)) {
			if(!DH.isReadable()) {
				if(pName == null) pName = "The DataHolder";
				throw new CurryError(pName + " is not readable." , pContext);
			}
			return DH.getData();
		}

		// Make a context
		if(pContext == null) pContext = (new Executor(this.TheEngine)).newRootContext(null);
		
		// Is readable
		if(!this.isDHReadable(pContext, pName, DH)) {
			if(pName == null) pName = "The DataHolder";
			throw new CurryError(pName + " is not readable." , pContext);
		}
		Executable Exec = ((DataHolder_Curry)DH).getExpr_getData(this.TheEngine);
		if(Exec == null) return null;
		
		Object Result = null;
		// Execute with an appropriate function
		if(!(Exec instanceof Expression)) {	// So it is other kind of executable
			   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
					   UObject.EmptyObjectArray, false, true);
		} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);		
		// Process result
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);
		
		// Process result
		return Result;
	}
	
	/** Check if the DataHolder is readable */
	boolean isDHReadable(Context pContext, String pName, DataHolder DH) {
		if(DH == null) return false;
		if(UCurry.isDataHolderNormal(DH)) return DH.isReadable();
		
		Executable Exec = ((DataHolder_Curry)DH).getExpr_isReadable(this.TheEngine);
		if(Exec == null) return false;

		// Make a context
		if(pContext == null) pContext = (new Executor(this.TheEngine)).newRootContext(null);
		
		Object Result = null;
		// Execute with an appropriate function
		if((Exec instanceof Expression)) {	// So it is other kind of executable
			   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
					   UObject.EmptyObjectArray, false, true);
		} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);		
		// Process result
		if(Result == null)                  return false;
		if(Result instanceof Boolean)       return ((Boolean)Result).booleanValue();
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);

		pName = (pName == null)?"":" (" + pName + ")";
		throw new CurryError("Invalid expression result: `isReadable` expression of a DataHolder must return a boolean"
				+ pName+".", pContext);
	}
	/** Check if the DataHolder is writtable */
	boolean isDHWritable(Context pContext, String pName, DataHolder DH) {
		if(DH == null) return false;
		if(UCurry.isDataHolderNormal(DH)) return DH.isWritable();
		
		Executable Exec   = ((DataHolder_Curry)DH).getExpr_isWritable(this.TheEngine);
		if(Exec == null) return false;

		// Make a context
		if(pContext == null) pContext = (new Executor(this.TheEngine)).newRootContext(null);
		
		Object Result = null;
		// Execute with an appropriate function
		if(!(Exec instanceof Expression)) {	// So it is other kind of executable
			   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
					   UObject.EmptyObjectArray, false, true);
		} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);		
		// Process result
		if(Result == null)                  return false;
		if(Result instanceof Boolean)       return ((Boolean)Result).booleanValue();
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);
		
		pName = (pName == null)?"":" (" + pName + ")";
		throw new CurryError("Invalid expression result: `isWritable` expression of a DataHolder must return a boolean "
				+ pName+".", pContext);
	}

	/** Check if the DataHolder is writable */
	boolean isDHNoTypeCheck(Context pContext, String pName, DataHolder DH) {
		if(DH == null) return false;
		if(UCurry.isDataHolderNormal(DH)) return DH.isNoTypeCheck();
		
		Executable Exec = ((DataHolder_Curry)DH).getExpr_isNoTypeCheck(this.TheEngine);
		if(Exec == null) return false;

		// Make a context
		if(pContext == null) pContext = (new Executor(this.TheEngine)).newRootContext(null);
		
		Object Result = null;
		// Execute with an appropriate function
		if(!(Exec instanceof Expression)) {	// So it is other kind of executable
			   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
					   UObject.EmptyObjectArray, false, true);
		} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);		
		// Process result
		if(Result == null)                  return false;
		if(Result instanceof Boolean)       return ((Boolean)Result).booleanValue();
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);
		
		pName = (pName == null)?"":" (" + pName + ")";
		throw new CurryError("Invalid expression result: `isNoTypeCheck` expression of a DataHolder must return "
				+ "a boolean"+pName+".", pContext);
	}
	/** Get the DataHolder type */
	Type getDHType(Context pContext, String pName, DataHolder DH) {
		if(DH == null) return TKJava.TVoid;
		if(UCurry.isDataHolderNormal(DH)) return DH.getType();
		
		Executable Exec = ((DataHolder_Curry)DH).getExpr_getType(this.TheEngine);
		if(Exec == null) return TKJava.TAny;

		// Make a context
		if(pContext == null) pContext = (new Executor(this.TheEngine)).newRootContext(null);
		
		Object Result = null;
		// Execute with an appropriate function
		if(!(Exec instanceof Expression)) {	// So it is other kind of executable
			   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
					   UObject.EmptyObjectArray, false, true);
		} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);		
		// Process result
		if(Result == null)                  return TKJava.TVoid;
		if(Result instanceof Type)          return (Type)Result;
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);
		
		pName = (pName == null)?"":" (" + pName + ")";
		throw new CurryError("Invalid expression result: `getType` expression of a DataHolder must return a type "
				+ pName+".", pContext);
	}
	
	/** Returns a clone of this DataHolder. */
	DataHolder cloneDH(Context pContext, String pName, DataHolder DH) {
		if(DH == null) return null;
		if(UCurry.isDataHolderNormal(DH)) return DH.clone();
		
		Executable Exec = ((DataHolder_Curry)DH).getExpr_clone(this.TheEngine);
		
		// Make a context
		if(pContext == null) pContext = (new Executor(this.TheEngine)).newRootContext(null);
		
		if(Exec != null) {
			Object Result = null;
			// Execute with an appropriate function
			if(!(Exec instanceof Expression)) {	// So it is other kind of executable
				   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
						   UObject.EmptyObjectArray, false, true);
			} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);		
			// Process result
			if(Result == null)                  return null;
			if(Result instanceof DataHolder)    return (DataHolder)Result;
			if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);
		}
		pName = (pName == null)?"":" (" + pName + ")";
		throw new CurryError("Invalid expression result: `clone` expression of a DataHolder must return a DataHolder "
				+ pName + ".", pContext);
	}
	
	/** Performs advance configuration to the data holder. */
	Object configDH(Context pContext, DataHolder DH, String pCName, Object[] pParams) {
		if(DH == null) return null;
		if(UCurry.isDataHolderNormal(DH)) return DH.config(pCName, pParams);
		
		Executable Exec = ((DataHolder_Curry)DH).getExpr_config(this.TheEngine, pCName, pParams);
		if(Exec == null) return null;
			
		// Make a context
		if(pContext == null) pContext = (new Executor(this.TheEngine)).newRootContext(null);
		
		Object Result = null;
		 
		// Execute with an appropriate function
		if(!(Exec instanceof Expression)) {	// So it is other kind of executable
			   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
					   new Object[] { pCName, pParams }, false, true);
		} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);
		
		// Process result
		if(Result instanceof SpecialResult)
			throw ((SpecialResult)Result).getException(pContext);
				
		// Process result
		return Result;
	}
	
	/** Get more information of the data holder. */
	Object getDHMoreInfo(Context pContext, DataHolder DH, String pMIName) {
		if(DH == null) return null;
		if(UCurry.isDataHolderNormal(DH)) return DH.getMoreInfo(pMIName);
		
		Executable Exec   = ((DataHolder_Curry)DH).getExpr_getMoreInfo(this.TheEngine, pMIName);
		if(Exec == null) return null;
		
		// Make a context
		if(pContext == null) pContext = (new Executor(this.TheEngine)).newRootContext(null);
		
		Object Result = null;
		// Execute with an appropriate function
		if(!(Exec instanceof Expression)) {	// So it is other kind of executable
			   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
					   new Object[] { pMIName }, false, true);
		} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);		
		// Process result
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);

		// Process result
		return Result;
	}

}
