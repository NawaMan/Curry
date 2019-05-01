package net.nawaman.curry;

public interface Accessible {
	
	/** Returns the owner of this accessible */
	public StackOwner getOwner();
	
	/** Returns the type that own this accessible (and that attribute access will be done under it) */
	public Type getOwnerAsType();
	
}
