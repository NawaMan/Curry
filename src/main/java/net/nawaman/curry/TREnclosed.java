package net.nawaman.curry;

/** TypeRef for a type that is owned by something */
abstract public class TREnclosed extends TypeRef {

	/** Returns the name of the type that this type reference is referring. */
	abstract public String   getTypeName(); 
	
	/** Returns the enclosed object */
	abstract Object getEncloseObject(Context pContext);
	
	/** Returns the accessibility of the TypeRef */
	abstract public Accessibility getAccessibility();
	
	/** Returns the location of the TypeRef */
	abstract public Location getLocation();
	
	/** Returns a new TypeRef for internal referencing */
	abstract public TREnclosed newInternalTypeRef(Engine pEngine, Accessibility pAccessibility, Location pLocation);
	
}
