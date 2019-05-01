package net.nawaman.curry.util;

import net.nawaman.util.Objectable;
import net.nawaman.util.UObject;

/** A wrapper of a normal object to an objectable object. */
public class Objectable_Adaptor implements Objectable {
	
	/** Constructs an Objectable_Adaptor. */
	public Objectable_Adaptor(Object pData) { this.TheData = pData; }
	
	Object TheData = null;

	/** Returns the short string representation of the object. */
	@Override public String toString() { if(this.TheData instanceof Objectable) return ((Objectable)this.TheData).toString(); return UObject.toString(this.TheData); }
	/** Returns the long string representation of the object. */
 	          public String toDetail() { if(this.TheData instanceof Objectable) return ((Objectable)this.TheData).toDetail(); return UObject.toString(this.TheData); }
	
 	/** Checks if O is the same or consider to be the same object with this object. */
	          public boolean is(Object O)     { if(this.TheData instanceof Objectable) return ((Objectable)this.TheData).is(O);     return UObject.is(this.TheData, O);    }
	/** Checks if O equals to this object. */
	@Override public boolean equals(Object O) { if(this.TheData instanceof Objectable) return ((Objectable)this.TheData).equals(O); return UObject.equal(this.TheData, O); }
	/** Returns the integer representation of the object. */
	@Override public int     hash()           { if(this.TheData instanceof Objectable) return ((Objectable)this.TheData).hash();    return UObject.hash(this.TheData);     }

}
