package net.nawaman.curry;

import net.nawaman.util.UObject;

public class TLCurrent  extends TLBasedOnType {
	
	/** KindName of this Type Loader */
	static final public String KindName = "Current";
	
	/** Constructs a new Current type loader. */
	TLCurrent(Engine pEngine) {
		super(pEngine);
	}
	
	// To Satisfy TypeLoader -------------------------------------------------------------
	
	/** Returns the loader kind name */ @Override
	public String getKindName() {
		return KindName;
	}
	
	/** Loads a type spec from the type reference. */ @Override
	protected Object loadTypeSpec(Context pContext, TypeRef pTRef) {
		// Check if the TypeRef is the correct kind
		if(!(pTRef instanceof TRCurrent)) {
			// Returns the error
			return new CurryError(
				"Type Loading Error: Wrong TypeRef Class. net.nawaman.curry.TLCurrent.TRCurrent " +
				"expected but "+pTRef.getClass().getCanonicalName()+" found.(TLCurrent.java#31)",
				pContext
			);
		}
		try {
			TypeRef BTRef = ((TLCurrent.TRCurrent)pTRef).TheBaseTypeRef;
			if(BTRef == null) {
				Type T = pContext.getStackOwnerAsCurrentType(); 
				BTRef = (T != null)?T.getTypeRef():null;
				
				if(BTRef == null) {
					return new CurryError(
							"Type Loading Error: Unable to create current type ("+pTRef+") because the current " +
							"context does not belong to a type. <TLCurrent.java#42>",
							pContext);
				}
			}
			
			// Return the TypeSpec
			return BTRef.getTypeSpec(pContext.getEngine());
			
		} catch (Throwable T) {
			return new CurryError(
					"Type Loading Error: Unable to create current type ("+pTRef+"). <TLCurrent.java#40>",
					pContext, T);
		}
	}
	
	/** TypeRef that refer to the Current StackOwner type */
	static public class TRCurrent extends TRBasedOnType {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		/** Constructs a type based on another type. */
		public TRCurrent() {
			super();
		}
		/** Constructs a type based on another type. */
		private TRCurrent(TypeRef pBaseTypeRef) {
			super(pBaseTypeRef);
		}

		/**{@inheritDoc}*/ @Override
		public String getRefKindName() {
			return TLCurrent.KindName;
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean isDynamic() {
			return true;
		}
		
		/**{@inheritDoc}*/ @Override
		protected TypeSpec getTypeSpecWithoutEngine() {
			if(this.TheBaseTypeRef == null) return null;
			return this.TheBaseTypeRef.getTypeSpecWithoutEngine();
		}

		/**{@inheritDoc}*/ @Override 
		public TypeRef flatType(Engine pEngine, TypeSpec pNewBaseTypeSpec, TypeRef pParameterBaseTypeToIgnore) {
			if(pParameterBaseTypeToIgnore != null) {
				TypeRef BTRef;
				if(((BTRef = this.getBaseTypeRef()) == null) || BTRef.equals(pParameterBaseTypeToIgnore))
					return this;
			}
			// A direct return
			return this.getBaseTypeRef();
		}
			
		/**{@inheritDoc}*/ @Override
		protected TRCurrent createNewTypeRef(Engine pEngine, TypeRef pNewBaseType, TypeSpec pNewBaseTypeSpec) {
			return new TRCurrent(pNewBaseType);
		}
		
		/**{@inheritDoc}*/ @Override 
		public TypeRef clone() {
			return new TRCurrent(this.getBaseTypeRef().clone());
		}
		
		// Objectable -----------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public String toString() {
			TypeRef TRef = this.getBaseTypeRef();
			if(TRef == null) TRef = TKJava.TAny.getTypeRef();
			return "Current";
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			TypeRef TRef = this.getBaseTypeRef();
			if(TRef == null) TRef = TKJava.TAny.getTypeRef();
			return "Current { " + TRef.toDetail() + " }";
		}
		/**{@inheritDoc}*/ @Override
		public boolean equals(Object O) {
			if(this == O)              return true;
			if(!(O instanceof TRCurrent)) return O.equals(this.getBaseTypeRef());
			// Both are no-name, compare type spec.
			TypeRef TRef = this.getBaseTypeRef();
			if(TRef == null) TRef = TKJava.TAny.getTypeRef();
			return UObject.equal(TRef, ((TRCurrent)O).getBaseTypeRef());
		}
		/**{@inheritDoc}*/ @Override
		public int hash() {
			return UObject.hash(this.toDetail());
		}
	}
}
