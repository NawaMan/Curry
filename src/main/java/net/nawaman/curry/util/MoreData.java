package net.nawaman.curry.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import net.nawaman.util.UString;

final public class MoreData implements Serializable, Cloneable {
	
    private static final long serialVersionUID = -5723439477084060243L;
    
    static public final MoreData Empty = new MoreData(); 
	
	public MoreData() {}
	
	public MoreData(String pName, Serializable pData) {
		this.setData(pName, pData);
	}
	
	public MoreData(Entry ... pEntries) {
		if(pEntries != null) { for(Entry E : pEntries) { if(E != null) this.setData(E.Name, E.Data); } }
	}
	
	public MoreData(Map<String, ? extends Serializable> pMap) {
		if(pMap != null) { for(String K : pMap.keySet()) { if(K != null) this.setData(K, pMap.get(K)); } }
	}
	
	static public class Entry implements Serializable, Cloneable {
		public Entry(String pName, Serializable pData) { this.Name = pName; this.Data = pData; }
		String       Name;
		Serializable Data;
		@Override public Entry clone() { return new Entry(this.Name, this.Data); }
	}
	
	boolean IsFrozen = false;
	Entry[] Datas    = null;
	
	Entry getEntry(String pName) {
		if(this.Datas == null) return null;
		for(int i = this.Datas.length; --i >= 0; ) {
			Entry E = this.Datas[i];
			if(E.Name.equals(pName)) return E;
		}
		return null;
	}
	/** Sets a data to an associated name */
	public Object setData(String pName, Serializable pValue) {
		if(this.IsFrozen)                throw new RuntimeException("This moredata has been frozen.");
		if(pName == null)                throw new NullPointerException();
		Entry E = this.getEntry(pName);
		if(E != null) {
			E.Data = pValue;
			return pValue;
		}
		if(this.Datas == null) this.Datas = new Entry[1];
		else {
			Entry[] NewDatas = new Entry[this.Datas.length + 1];
			System.arraycopy(this.Datas, 0, this.Datas, 0, this.Datas.length);
			this.Datas = NewDatas;
		}
		this.Datas[this.Datas.length - 1] = new Entry(pName, pValue);
		return pValue;
	}
	/** Returns the value associated name */
	public Serializable getData(String pName) {
		if(this.Datas == null) return null;
		Entry E = this.getEntry(pName);
		return (E == null)?null:E.Data;
	}
	/** Checks if the entry associated with pName exists */
	public boolean contains(String pName) {
		return (this.getData(pName) != null);
	}
	
	/** Freeze this MoreData */
	public void    toFreeze() { this.IsFrozen = true; }
	/** Checks if this more data is frozen */
	public boolean isFrozen() { return this.IsFrozen; }
	
	// All entry of add will be combined into the base (ignore if repeat) then return the result.
	static public MoreData combineMoreData(MoreData pBase, MoreData pAdd) {
		HashSet<String> DNames   = new HashSet<String>();
		Vector<Entry>   DEntries = new Vector<Entry>();
		if((pBase != null) && (pBase.Datas != null)) {
			for(Entry E : pBase.Datas) {
				DNames.add(  E.Name);
				DEntries.add(E.clone());
			}
		}
		if((pAdd != null) && (pAdd.Datas != null)) {
			for(Entry E : pAdd.Datas) {
			    if (E == null)
			        continue;
			    
				if(DNames.contains(E.Name))
				    continue;
				DNames.add(  E.Name);
				DEntries.add(E.clone());
			}
		}
		MoreData MD = new MoreData();
		MD.Datas = DEntries.toArray(new Entry[0]);
		return MD;
	}
	
	/** Creates a MoreData from array of Name and Value pair Like new Object[] { new Serializable[] { Name, Value } } */
	static public MoreData newMoreDataFromArray(Object pDatas, boolean pIsToFreeze) {
		if(pDatas == null) return null;
		if(!pDatas.getClass().isArray()) throw new IllegalArgumentException("Array of array is needed as a parameter.");
		
		MoreData MD = new MoreData();
		
		for(int i = 0; i < Array.getLength(pDatas); i++) {
			Object E = Array.get(pDatas, i);
			if((E == null) || !E.getClass().isArray() || (Array.getLength(E) < 2)) continue;
			String       N =               Array.get(E, 0).toString();
			Serializable V = (Serializable)Array.get(E, 1);
			MD.setData(N, V);
		}
		
		if(pIsToFreeze) MD.toFreeze();
		return MD;
	}
	
	String ToString(Object O) {
		if(O == null) return "null";
		if(O instanceof String)
			return "STRING START ------------------------------------------------------------\n"+
				   ((String)O)+
				 "\n-------------------------------------------------------------------------\n";
		if(O instanceof Character) return "'"+UString.escapeText((String)O)+"'";
		if(O instanceof Number) {
			if(O instanceof Integer) return "(int)"    + O.toString();
			if(O instanceof Double)  return "(double)" + O.toString();
			if(O instanceof Byte)    return "(byte)"   + O.toString();
			if(O instanceof Short)   return "(short)"  + O.toString();
			if(O instanceof Long)    return "(long)"   + O.toString();
			if(O instanceof Float)   return "(float)"  + O.toString();
		}
		if(O.getClass().isArray()) {
			StringBuffer SB = new StringBuffer();
			String CName = O.getClass().getComponentType().getCanonicalName();
			if(CName.startsWith("java.lang.") && (CName.substring("java.lang.".length()).indexOf(".") == -1))
				CName = CName.substring("java.lang.".length());
			SB.append(CName).append("[] {");
			for(int i = 0; i < java.lang.reflect.Array.getLength(O); i++) {
				if(i != 0) SB.append(",");
				String EToString = ToString(java.lang.reflect.Array.get(O, i));
				if(EToString.contains("\n")) {
					String[] Lines = EToString.split("\n");
					StringBuffer LinesSB = new StringBuffer();
					for(int l = 0; l < Lines.length; l++) {
						if(l != 0) LinesSB.append("\n\t");
						LinesSB.append(Lines[l]);
					}
					EToString = LinesSB.toString();
				}
				SB.append("\n\t").append(EToString);
			}
			SB.append("\n}");
			return SB.toString();
		}
		return O.toString() + ":" + O.getClass().getCanonicalName();
	}
	
	@Override public MoreData clone() {
		MoreData MD = new MoreData();
		if(this.Datas != null) {
			MD.Datas = new Entry[this.Datas.length];
			for(int i = 0; i < this.Datas.length; i++) MD.Datas[i] = this.Datas[i].clone(); 
		}
		return MD;
	}
	
	@Override public String toString() {
		StringBuffer SB = new StringBuffer();
		if((this.Datas != null) && (this.Datas.length != 0)) {
			SB.append("\n\n");
			for(Entry E : this.Datas) {
				String       NDataName = E.Name;
				Serializable D         = E.Data;
				String DToString = ToString(D);

				if(DToString.indexOf('\n') != -1) {
					String[] Lines = DToString.split("\n");
					StringBuffer LinesSB = new StringBuffer();
					for(int i = 0; i < Lines.length; i++) LinesSB.append("\n\t").append(Lines[i]);
					DToString = LinesSB.toString();
				} else {
					DToString = " = " + DToString + ";";
				}

				SB.append("\tdata: ").append(NDataName).append(DToString).append("\n\n");
			}
		}
		return "MoreData {" + SB.toString() + "}";
	}
	
	@Override public boolean equals(Object O) {
        if (this == O)
            return true;
        
	    if (!(O instanceof MoreData))
	        return false;
	    
	    final MoreData MD = (MoreData)O;
	    
	    if (((MD  .Datas == null) || (MD  .Datas.length == 0))
	     && ((this.Datas == null) || (this.Datas.length == 0)))
	        return true;
	    
        if ((MD.Datas == null) || (this.Datas == null))
            return false;
        
        if (this.Datas.length != MD.Datas.length)
            return false;
        
        for(final Entry aEntry : this.Datas) {
            final String       aName   = aEntry.Name;
            final Serializable aData   = this.getData(aName);
            
            final boolean aIsMDContainName = MD.contains(aName);
            if (!aIsMDContainName)
                return false;
            
            final Serializable aMDData = MD.getData(aName);
            
            if (aData == aMDData)
                continue;
            
            if (aData == null)
                return false;
            
            if (aData.equals(aMDData))
                return false;
        }
        
        return true;
	}
}
