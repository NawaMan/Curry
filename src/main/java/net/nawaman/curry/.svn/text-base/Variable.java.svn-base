package net.nawaman.curry;

import net.nawaman.curry.util.*;
import net.nawaman.util.UObject;

public class Variable implements DataHolder {
	
	static final public String FactoryName = "Variable";
	
	static final public String ConfigName_ToNotWritable = "ToNotWritable";
	
	static public class VariableFactory implements DataHolderFactory {
		
		public String getName() { return FactoryName; }
				
		public Variable newDataHolder(Context pContext, Engine pEngine, Type pType, Object pData, boolean pIsReadable,
				boolean pIsWritable, MoreData pMoreInfo, DataHolderInfo pDHInfo) {
			if(pType == null) pType = TKJava.TAny;
			
			if(!MType.CanTypeRefByAssignableBy(pContext, pEngine, pType.getTypeRef(), pData))
				return null;
			
			return new Variable(pType, pData, pIsReadable, pIsWritable);		
		}
		public Variable newDataHolder(Context pContext, Engine pEngine, Type pType, boolean pIsReadable,
				boolean pIsWritable, MoreData pMoreInfo, DataHolderInfo pDHInfo) {
			if(pType == null) pType = TKJava.TAny;
			return new Variable(pType, pIsReadable, pIsWritable);
		}
		
		/** Checks if the given data holder is compatible with this DataHolder Factory*/
		public boolean isInstance(DataHolder DH) {
			return DH instanceof Variable;
		}
	}

	static private VariableFactory Factory = null;
	static public  VariableFactory getFactory() {
		if(Variable.Factory == null) Variable.Factory = new VariableFactory();
		return Variable.Factory;
	}

	Variable(Type pType, Object pData, boolean pIsReadable, boolean pIsWritable) {
		this.Type     = (pType == null)?TKJava.TAny:pType;
		this.TheData  = pData;
        this.Flags[0] = pIsReadable;
        this.Flags[1] = pIsWritable;
	}
	Variable(Type pType, boolean pIsReadable, boolean pIsWritable) {
		this(pType, null, pIsReadable, pIsWritable);
	}
	
	Type      Type    = null;
	Object    TheData = null;
    boolean[] Flags   = new boolean[2];
	
	public Type getType() {
		return this.Type;
	}
	
	/** Checks if type checking MUST NOT be done when set. */
	public boolean isNoTypeCheck() { return true; }	// True - because setData will check it.

	public boolean isReadable() { return this.Flags[0]; }
	public boolean isWritable() { return this.Flags[1]; }

	public Object setData(Object pData) {
		if(this.Flags[1]) {
			if(this.getType().canBeAssignedBy(pData)) {
				this.TheData  = pData;
				return pData;
			} else {
				Object O = TKJava.tryToCastTo(pData, this.getType());
				if(O != null) {
					this.TheData  = O;
					return O;
				}
				throw new CurryError("Incompatible data `"+UObject.toString(pData)+"` to this variable of the type `"+
						UObject.toString(this.Type)+"`.");
			}
		}
		throw new CurryError("A variable is not writable.");
	}
	public Object getData() {
		if(this.Flags[0]) return this.TheData;
		throw new CurryError("A variable is not readable.");
	}
	
	@Override public Variable clone() {
		Variable V = new Variable(this.Type, this.TheData, this.Flags[0], this.Flags[1]);
		return V;
	}
	
	/** Performs advance configuration to the data holder. */
	public Object config(String pName, Object[] pParams) {
		if(ConfigName_ToNotWritable.equals(pName)) this.Flags[1] = false;
		return null;
	}
	
	/** Returns more information about the DataHolder. */
	public Object getMoreInfo(String pName) { return null; }
	
}
