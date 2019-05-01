package net.nawaman.curry.extra.type_enum;

import java.util.HashSet;

import net.nawaman.curry.CurryError;

public final class TEMS_Grouping extends Abstract_TEMS_Deriving {
	
	public TEMS_Grouping(String pName, String pDerivedName, String[] pGroupedNames) {
		super(pName, pDerivedName);
		
		if((pDerivedName != null) && pName.equals(pDerivedName))
			throw new CurryError("Driving enum members cannot have the same name with its derived one.");
		
		if(pGroupedNames        == null) throw new NullPointerException();
		if(pGroupedNames.length ==    0) throw new IllegalArgumentException("Empty group nams.");
		this.GroupedNames = pGroupedNames.clone();
		
		HashSet<String> GNs = new HashSet<String>();
		for(String GN : pGroupedNames) {
			if(GNs.contains(GN)) throw new CurryError("Grouped enum members has repeated name.");
			GNs.add(GN);
			if(pName.equals(GN)) throw new CurryError("Grouping enum members cannot have the same name with its grouped one.");
		}
	}
	
	/**{@inheritDoc}*/ @Override 
	boolean canDNBeNull() {
		return true;
	}
	
	/**{@inheritDoc}*/ @Override 
	public boolean isGrouping() {
		return true;
	}
	/**{@inheritDoc}*/ @Override 
	public TEMS_Grouping asGrouping() {
		return this;
	}
	
	String[] GroupedNames;
	       
	public int getMemberCount() {
		return this.GroupedNames.length;
	}
	public String getMember(int pPos) {
		return ((pPos < 0)||(pPos >= this.GroupedNames.length))?null:this.GroupedNames[pPos];
	}
}