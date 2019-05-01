package net.nawaman.curry;

import net.nawaman.curry.util.*;
import net.nawaman.util.*;

/**
 * A record of an action - encomposing actor (StackOwner) and the location in which the action occurs.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
public class ActionRecord implements Objectable {
		
	/** Create an action record with parameter. */
	ActionRecord(Object pActor, LocationSnapshot pLocationSnapshot) {
		this.Actor            = pActor;
		this.LocationSnapshot = pLocationSnapshot;
	}
	
   	Object           Actor            = null;
   	LocationSnapshot LocationSnapshot = null;
	
	/** Returns the actor of this action. */
   	public Object getActor() {
   		return this.Actor;
   	}
   	/** Returns the location that the action occurs. */
   	public LocationSnapshot getLocationSnapshot() {
   		return this.LocationSnapshot;
   	}
   	/** Returns the extra-data that the action occurs. */
   	public MoreData getMoreData() {
   		return null;
   	}

   	// Objectable ----------------------------------------------------------------------------------
   	
   	/** Returns the string representation of this action record. */
   	@Override
   	public String toString() {
   		return this.toDetail();
   	}
   	
   	/** Returns the long string representation of this action record. */
   	public String toDetail() {
   		return ((this.Actor == null)?"":(UObject.toString(this.Actor)))
   				+ ((this.LocationSnapshot == null)?"":" given at '" + this.LocationSnapshot+"'");
   	}
   	
   	/** Checks if O is the same or consider to be the same with this action record. */
   	public boolean is(Object O) { return this == O; }
   	
   	/** Checks if O equals to this action record. */
   	@Override public boolean equals(Object O) {
   		if(O == null)                      return false;
   		if(!(O instanceof ActionRecord)) return false;
   		if(!UObject.equal(((ActionRecord)O).Actor,            this.Actor))            return false;
   		if(!UObject.equal(((ActionRecord)O).LocationSnapshot, this.LocationSnapshot)) return false; // ???
   		return true;
   	}
   	/** Returns hash code of this action record. */
   	@Override public int hash() { return UObject.hash(this.Actor) + UObject.hash(this.LocationSnapshot); }

	// Sub calsses -----------------------------------------------------------------------------------------------------
	static class ActionRecord_WithMoreData extends ActionRecord {
	
		/** Create an action record with parameter. */
		ActionRecord_WithMoreData(StackOwner pActor, LocationSnapshot pLocationSnapshot, MoreData pMoreData) {
			super(pActor, pLocationSnapshot);
			this.TheMoreData = pMoreData;
			if(this.TheMoreData != null) this.TheMoreData.toFreeze();
		}

		MoreData TheMoreData  = null;

		/** Returns the extra-data that the action occurs. */
		@Override public MoreData getMoreData() {
			return (this.TheMoreData == null)?MoreData.Empty:this.TheMoreData;
		}

		// Objectable ----------------------------------------------------------------------------------

		/** Returns the string representation of this action record. */
		@Override public String toString() {
			return super.toDetail();
		}
		/** Returns the long string representation of this action record. */
		@Override public String toDetail() {
			return super.toDetail() + ((this.TheMoreData == null)?"":" {"+ UObject.toString(this.TheMoreData) +"}");
		}

		/** Checks if O equals to this action record. */
		@Override public boolean equals(Object O) {
			if(O == null)                    return false;
			if(!(O instanceof ActionRecord)) return false;
			if(!UObject.equal(((ActionRecord)O).Actor,            this.Actor))     return false;
			if(!UObject.equal(((ActionRecord)O).LocationSnapshot, this.LocationSnapshot)) return false; // ???
			Object ED = ((O instanceof ActionRecord_WithMoreData)
					?((ActionRecord_WithMoreData)O).TheMoreData
					:null);
			if(!UObject.equal(ED, this.TheMoreData)) return false;
			return true;
		}
		/** Returns hash code of this action record. */
		@Override public int hash() {
			return UObject.hash(this.Actor)+UObject.hash(this.LocationSnapshot)+UObject.hash(this.TheMoreData);
		}
	}
}
