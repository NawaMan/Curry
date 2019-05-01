package net.nawaman.curry;

/** Provide information about a DObject. */
public class DObjectInfo extends StackOwnerInfo {
	
	public DObjectInfo(DObject pDObject) {
		super(pDObject);
	}
	
	/** Returns the DObject that this object is helping */
	final public DObject getDObject() {
		return (DObject)this.SO;
	}
	
	/**
	 * Returns the type of this object.
	 * 
	 * The default value is no-type
	 **/
	public Type getTheType() {
		return null;
	}

	/**
	 * Returns the Engine for the DObject
	 **/
	public Engine getEngine() {
		return ((DObject)this.SO).getEngine();
	}
}
