package net.nawaman.curry;

/** TypeLoader load a type spec from type ref */
abstract public class TypeLoader {
	
	/** Constructs a TypeLoader. */
	TypeLoader(Engine pEngine) {
		if(pEngine == null) throw new NullPointerException();
		this.Engine = pEngine;
	}
	
	/** The Engine that will recognize this type loader. */
	final Engine Engine;
	
	/** Returns the engine that will recognize this type loader */
	final protected Engine getEngine() {
		return this.Engine;
	}
	
	// Classification --------------------------------------------------------------------
	
	/** Returns the kind name of this type. */
	abstract public String getKindName();
	
	// Services --------------------------------------------------------------------------

	/**
	 * Load a type spec from the given type reference.<br />
	 * @return Type spec if success or Throwable if fail.
	 ***/
	protected Object loadTypeSpec(Context pContext, TypeRef pTRef) { return null; }
	
	// Utilities ------------------------------------------------------------------------

	/** Ensure that the type referred by pTypeRef exist (resolved) */
	final protected void ensureTypeExist(ExternalContext EC, TypeRef pRef) {
		this.Engine.getTypeManager().ensureTypeExist(EC.pContext, pRef);
	}
}
