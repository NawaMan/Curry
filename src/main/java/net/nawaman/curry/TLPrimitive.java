package net.nawaman.curry;

import net.nawaman.util.UString;

/** TypeLoader of those type that has no name (use TypeRef contains TypeSpec). */
public class TLPrimitive extends TypeLoader {
	
	/** KindName of this Type Loader */
	static final public String KindName = "Alias";
	
	/** Constructs a new no-name type loader. */
	TLPrimitive(Engine pEngine) { super(pEngine); }
	
	/** Returns type from the TRPrimitive */
	static public Type getTypeFromTRPrimitive(Engine pEngine, TRPrimitive pTRPrimitive) {
		if(pTRPrimitive == null) return null;
		if(!pTRPrimitive.isLoaded()) 
			pEngine.getTypeManager().ensureTypeValidated(pTRPrimitive);
		return pTRPrimitive.getTheType();
	}
	
	// To Satisfy TypeLoader -------------------------------------------------------------
	
	/** Returns the loader kind name */ @Override
	public String getKindName() {
		return KindName;
	}
	
	/** Loads a type spec from the type reference. */ @Override
	protected Object loadTypeSpec(Context pContext, TypeRef pTRef) {
		// Check if the TypeRef is the correct kind
		if(!(pTRef instanceof TRPrimitive)) {
			// Returns the error
			return new CurryError(
				"Type Loading Error: Wrong TypeRef Class. nawa.curry.TLAlias.TRAlias " +
				"expected but "+pTRef.getClass().getCanonicalName()+" found.(TLPrimitive.java#24)",
				pContext
			);
		}
		
		// Looks for it in TKJava
		return TKJava.Instance.getTypeByClassName(pContext.getEngine(),
				((TRPrimitive)pTRef).getAlias(), ((TRPrimitive)pTRef).getClassCanonicalName()
			).getTypeSpec();
	}
	
	/**
	 * TypeRef that refer to aliased type (TJava type).<br />
	 **/
	static final public class TRPrimitive extends TypeRef {
		
		static private final long serialVersionUID = 3112544568798465165L;
		
		/** Constructs a alias type reference. */
		public TRPrimitive(String pClassCanonicalName) {
			this(null, pClassCanonicalName);
		}
		
		/** Constructs a alias type reference. */
		TRPrimitive(String pAlias, String pClassCanonicalName) {
			if((pAlias == null) && (pClassCanonicalName == null))
				throw new NullPointerException("An primitive type ref (a.k.a. Alias TypeRef) cannot be constucted without the alias.");
			this.Alias = pAlias;
			this.Name  = pClassCanonicalName;
		}

		/** Returns the type-reference kind name. */ @Override
		public String getRefKindName() {
			return TLPrimitive.KindName;
		}
		
		/**{@inheritDoc}*/ @Override 
		public TypeRef clone() {
			return new TRPrimitive(this.Alias, this.Name);
		}
		
		/** Alias of the type that this type reference is referering. */
		final  String Alias;
		/** Returns the alias of the type that this type reference is referering. */
		public String getAlias() {
			return this.Alias;
		}
		
		/** Name of the Java class this type reference is referering. */
		final  String Name;
		/** Returns the canonical name of the class this Java type is referering. */
		public String getClassCanonicalName() {
			return this.Name;
		}
		
		// Objectable -----------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override 
		public String toString() {
			return (this.Alias != null) ? this.Alias : this.Name;
		}
		/**{@inheritDoc}*/ @Override 
		public String toDetail() {
			return this.toString();
		}
		/**{@inheritDoc}*/ @Override 
		public boolean equals(Object O) {
			if(this == O)               return  true;
			if(!(O instanceof TRPrimitive)) return false;
			// Both are no-name, compare type spec.
			if(this.Alias != null) return this.Alias.equals(((TRPrimitive)O).Alias);
			return this.Name.equals(((TRPrimitive)O).Name);
		}
		/**{@inheritDoc}*/ @Override 
		public int hash() {
			return UString.hash("AliasTypeRef") + UString.hash(this.toString());
		}
	}
}
