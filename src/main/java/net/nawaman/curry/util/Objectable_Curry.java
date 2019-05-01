package net.nawaman.curry.util;

import net.nawaman.curry.*;
import net.nawaman.util.Objectable;

/** Objectable that is in curry scope. */
public interface Objectable_Curry extends Objectable {
	
	/** Checks if this Objectable_Curry can be used as a normal objectable */
	public boolean isAlsoNormalObjectable();
	
	/** Returns the short string representation of the object. */
	public Executable getExpr_toString(Engine pEngine);
	/** Returns the long string representation of the object. */
	public Executable getExpr_toDetail(Engine pEngine);
	
	/** Checks if O is the same or consider to be the same object with this object. */
	public Executable getExpr_is(Engine pEngine, Object O);
	/** Checks if O equals to this object. */
	public Executable getExpr_equals(Engine pEngine, Object O);
	/** Returns the integer representation of the object. */
	public Executable getExpr_hash(Engine pEngine);
	
}
