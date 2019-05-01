package net.nawaman.curry.extra.type_enum;

import net.nawaman.curry.CurryError;

public class TEMemberSpec {
	TEMemberSpec(String pName) {
		this.Name = pName; if(pName == null) throw new NullPointerException();
	}
	       String Name;
	public String getName() { return this.Name; }
	
	// Characteristics -----------------------------------------------------------------------------
	
	public boolean isIndependent() { return false; }
	public boolean isBorrowing()   { return false; }
	public boolean isDeriving()    { return false; }
	public boolean isGrouping()    { return false; }
	
	public TEMS_Independent asIndependent() { return null; }
	public TEMS_Borrowing   asBorrowing()   { return null; }
	public TEMS_Deriving    asDeriving()    { return null; }
	public TEMS_Grouping    asGrouping()    { return null; }
}

abstract class Abstract_TEMS_Deriving extends TEMemberSpec {
	Abstract_TEMS_Deriving(String pName, String pDerivedName) {
		super(pName);
		this.DerivedName = pDerivedName;
		if(!this.canDNBeNull() && (pDerivedName == null))        throw new NullPointerException();
		if((pDerivedName != null) && pName.equals(pDerivedName)) throw new CurryError("Driving enum members cannot have the same name with its derived one.");
	}
	private String DerivedName;
	public  String getDerivedName() { return this.DerivedName; }
	
	abstract boolean canDNBeNull();
}