package net.nawaman.curry;

/*** Object that can be parent of a StackOwnerBuilder */
public interface StackOwnerBuilderEncloseObject {

   	/** Returns the Engine for the StackOwnerBuilder */
   	public Engine getEngine();

	/** Returns the name of package the StackOwnerBuilderis in */   	
	public String getPackageName();

   	/** Checks if this package builder is still active (have not saved yet) */
	public boolean isActive();

   	/** Checks if the ID is a valid ID */
	boolean isValidID(Object pID);
	
	/** Ensure the TypeRef of the given TypeSpec valid and returns the valid one or null if that is not possible */
	abstract public TREnclosed ensureTypeRefValid(Accessibility pPAccess, TypeSpec TS, Location pLocation);
	
	// Lock --------------------------------------------------------------------
	
	/** This method will help limiting the implementation of this interface to be within this package. */
	public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface);
}
