package net.nawaman.curry.util;

import net.nawaman.util.Objectable;

/** Utilities for those that is in curry scope. */
public class UCurry {
	
	private UCurry() {}

	/** Checks if the object O is in curry scope. */
	static public boolean isObjectableNormal(Objectable O) {
		if(!(O instanceof Objectable_Curry)) return true;
		return ((Objectable_Curry)O).isAlsoNormalObjectable();
	}
	/** Checks if the object O is in curry scope. */
	static public boolean isObjectableCurry(Objectable O) {
		if(!(O instanceof Objectable_Curry)) return false;
		return !((Objectable_Curry)O).isAlsoNormalObjectable();
	}
	

	/** Checks if the objec O is in curry scope. */
	static public boolean isDataHolderNormal(DataHolder DH) {
		if(!(DH instanceof DataHolder_Curry)) return true;
		return ((DataHolder_Curry)DH).isAlsoNormalDataHolder();
	}
	/** Checks if the dataholder DH is in curry scope. */
	static public boolean isDataHolderCurry(DataHolder DH) {
		if(!(DH instanceof DataHolder_Curry)) return false;
		return !((DataHolder_Curry)DH).isAlsoNormalDataHolder();
	}

}
