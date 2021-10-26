package net.nawaman.curry;

import net.nawaman.util.UObject;

/** Type loader for Type of TypeRef */
public class TLType extends TypeLoader {

	/** TypeRef of Type */
	static public final TypeTypeRef TypeRefOfType = new TypeTypeRef(TKJava.TType.getTypeRef());
	
	/** KindName of this Type Loader */
	static final public String KindName = "Type";
	
	/** Constructs a new no-name type loader. */
	TLType(Engine pEngine) {
		super(pEngine);
	}
	
	// To Satisfy TypeLoader -------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return KindName;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Object loadTypeSpec(Context pContext, TypeRef pTRef) {
		// Check if the TypeRef is the correct kind
		if(!(pTRef instanceof TypeTypeRef)) {
			// Returns the error
			return new CurryError(
				"Type Loading Error: Wrong TypeRef Class. nawa.curry.TLType.TRType " +
				"expected but "+pTRef.getClass().getCanonicalName()+" found.(TLType.java#29)",
				pContext
			);
		}
		
		// Return the TypeSpec
		return pTRef.getTypeSpec(pContext.getEngine(), pContext);
	}
	
	/**
	 * The default TypeRef for Type of Type.
	 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
	 */
	static public class TypeTypeRef extends TypeRef {
		
		static private final long serialVersionUID = 5165465465165178932L;
		
		TypeRef TheRef = null;

		public TypeTypeRef(TypeRef pTheRef) {
			this.TheRef = pTheRef;
		}
		
		/**{@inheritDoc}*/ @Override
		public String getRefKindName() {
			return KindName;
		}
		
		/** Returns the TypeRef of the type that this TypeRef is refering. */
		public TypeRef getTheRef() {
			return this.TheRef;
		}

		/**{@inheritDoc}*/ @Override
		public void resetTypeRefForCompilation() {
			super.resetTypeRefForCompilation();
			if(this.TheRef != null)
				this.TheRef.resetForCompilation();
		}

		/**{@inheritDoc}*/ @Override
		public TypeRef clone() {
			return new TypeTypeRef(this.getTheRef());
		}
		
		/**{@inheritDoc}*/ @Override
		public String  toString() {
			return "Type<" + ((this.TheRef == null)?TKJava.TAny.getTypeRef().toString():this.TheRef.toString()) + ">";
		}
		/**{@inheritDoc}*/ @Override
		public String  toDetail() {
			return this.toString();
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean equals(Object O) {
			if(O instanceof TypeTypeRef) return UObject.equal(this.TheRef, ((TypeTypeRef)O).TheRef);
			return false;
		}
		
		/**{@inheritDoc}*/ @Override
		public int hash() {
			return UObject.hash(this.toString());
		}
		/**{@inheritDoc}*/ @Override
		public int hashCode() {
			return this.hash();
		}
	}
}
