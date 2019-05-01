package net.nawaman.curry;

import net.nawaman.util.UObject;

/** TypeLoader of those type that has no name (use TypeRef contains TypeSpec). */
final public class TLNoName extends TypeLoader {
	
	/** KindName of this Type Loader */
	static final public String KindName = "NoName";
	
	/** Constructs a new no-name type loader. */
	TLNoName(Engine pEngine) { super(pEngine); }
	
	// To Satisfy TypeLoader -------------------------------------------------------------
	
	/** Returns the loader kind name */
	@Override public String getKindName() { return KindName; }
	
	/** Loads a type spec from the type reference. */
	@Override protected Object loadTypeSpec(Context pContext, TypeRef pTRef) {
		// Check if the TypeRef is the correct kind
		if(!(pTRef instanceof TRNoName)) {
			// Returns the error
			return new CurryError(
				"Type Loading Error: Wrong TypeRef Class. nawa.curry.TLNoName.TRNoName " +
				"expected but "+pTRef.getClass().getCanonicalName()+" found.(TLNoName.java#26)",
				pContext
			);
		}
		// Return the TypeSpec
		return ((TRNoName)pTRef).TypeSpec;
	}
	
	/**
	 * TypeRef that has no name.<br />
	 * The type ref itself contains the TypeSpec
	 **/
	static final public class TRNoName extends TypeRef {
		
		static private final long serialVersionUID = 1526354654635465788L;
		
		/** Constructs a no name type reference. */
		TRNoName(TypeSpec pTypeSpec) {
			if(pTypeSpec == null) throw new NullPointerException("A no-name type ref cannot be constucted without the type spec.");
			this.TypeSpec = pTypeSpec;
		}
		
		/**{@inheritDoc}*/ @Override
		public String getRefKindName() {
			return TLNoName.KindName;
		}
		
		/** TypeSpec of the type that this type reference is referring. */
		private TypeSpec TypeSpec = null;
		/** Returns the type spec of the type that this type reference is referring. */
		public TypeSpec getTypeSpec() {
			return this.TypeSpec;
		}
		
		/**{@inheritDoc}*/ @Override
		protected TypeSpec getTypeSpecWithoutEngine() {
			return this.TypeSpec;
		}
		
		/**{@inheritDoc}*/ @Override
		void setTheType(Type pTheType) {
			super.setTheType(pTheType);
			if(pTheType == null)
				return;
			
			this.TypeSpec = pTheType.getTypeSpec();
		}
		
		/**{@inheritDoc}*/ @Override 
		public TypeRef clone() {
			return new TRNoName(this.TypeSpec);
		}
		
		// Objectable -----------------------------------------------------------------------
				
		/**{@inheritDoc}*/ @Override
		public String toString() {				
			if(!this.TypeSpec.isToShowNoName()) return this.TypeSpec.getToString();
			return "NoName {" + this.TypeSpec.getToString() + "}";
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			return this.toString();
		}
		/**{@inheritDoc}*/ @Override
		public boolean equals(Object O) {
			if(this == O)                return  true;
			if(!(O instanceof TRNoName)) return false;
			// Both are no-name, compare type spec.
			return UObject.equal(this.TypeSpec, ((TRNoName)O).getTypeSpec());
		}
		/**{@inheritDoc}*/ @Override
		public int hash() {
			return UObject.hash("NoNameTypeRef") + UObject.hash(this.TypeSpec.toString());
		}
		
	}

}
