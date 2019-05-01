package net.nawaman.curry.util;

import java.lang.reflect.Array;

import net.nawaman.curry.Engine;
import net.nawaman.curry.Type;
import net.nawaman.util.UArray;

public class DataHolderOfArrayElement implements net.nawaman.curry.util.DataHolder {
	
	static final public int IsReadablePos = 0;
	static final public int IsWritablePos = 1;
	
	static public DataHolderOfArrayElement newDataHolderOfArrayElement(
			Engine pEngine, Object pArrayObj, int pPos, boolean pIsReadable, boolean pIsWritable) {
		if(pArrayObj == null) return null;
		if(pEngine   == null) return null;
		
		if(!UArray.isArrayInstance(pArrayObj))  return null;
		if(pPos >= UArray.getLength(pArrayObj)) return null;
		if(!pIsReadable && !pIsWritable)        return null;
		
		Type T = pEngine.getTypeManager().getTypeOfTheInstanceOf(UArray.getComponentType_OfInstance(pArrayObj));
		
		return new DataHolderOfArrayElement(pArrayObj, T, pPos, pIsReadable, pIsWritable); 
	}
	
	DataHolderOfArrayElement(Object pArrayObj, Type pType, int pPos, boolean pIsReadable, boolean pIsWritable) {
		this.ArrayObj = pArrayObj;
		this.Type     = pType;
		this.Pos      = pPos;
		this.setFlag(IsReadablePos, pIsReadable);
		this.setFlag(IsWritablePos, pIsWritable);
	}
	
	Object ArrayObj = null;
	Type   Type     = null;
	int    Pos      =    0;

	int    Flags =    0;
	void setFlag(int pPos, boolean pValue) {
		if((pPos < 0) || (pPos >= 32)) return;
		if(pValue) this.Flags = this.Flags |  (1 << pPos);
		else       this.Flags = this.Flags & ~(1 << pPos);
	}
	boolean isFlag(int pPos) {
		if((pPos < 0) || (pPos >= 32)) return false;
		return (this.Flags & (1 << pPos)) != 0;
	}
	
	public boolean isReadable() { return this.isFlag(IsReadablePos); }
	public boolean isWritable() { return this.isFlag(IsWritablePos); }
	
	public Type getType() { return this.Type; }

	public boolean isNoTypeCheck() { return true; }	// True - because setData will check it.

	public Object setData(Object pData) {
		if(!this.isWritable())
			throw new RuntimeException("The dataholder is not writable");
		if(!this.Type.getTypeInfo().canBeAssignedBy(pData))
			throw new RuntimeException("The dataholder cannot be assigned by the value '"+pData+"'.");
		
		Array.set(this.ArrayObj, this.Pos, pData);
		return pData;
	}
	public Object getData() {
		if(this.isReadable()) return Array.get(this.ArrayObj, this.Pos);
		return null;
	}
	
	@Override public DataHolderOfArrayElement clone() {
		Object CopyOfArray = (this.ArrayObj==null)?null:Array.newInstance(this.ArrayObj.getClass().getComponentType(), Array.getLength(this.ArrayObj));
		return new DataHolderOfArrayElement(CopyOfArray, this.Type, this.Pos, this.isReadable(), this.isWritable());
	}
	
	/** Performs advance configuration to the data holder. */
	public Object config(String pName, Object[] pParams) { return false; }
	
	/** Returns more information about the dataholder. */
	public Object getMoreInfo(String pName) { return null; }
}