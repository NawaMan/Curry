package net.nawaman.curry;

import net.nawaman.curry.TKArray.TSArray;
import net.nawaman.curry.TLNoName.TRNoName;
import net.nawaman.util.UArray;
import net.nawaman.util.UObject;

/** TypeLoader of a Parametered type reference*/
public class TLParametered extends TLBasedOnType {
	
	/** KindName of this Type Loader */
	static final public String KindName = "Parametered";
	
	/** Constructs a new parametered type loader. */
	TLParametered(Engine pEngine) {
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
		if(!(pTRef instanceof TRParametered)) {
			// Returns the error
			return new CurryError(
				"Type Loading Error: Wrong TypeRef Class. net.nawaman.curry.TLParametered.TRParametered " +
				"expected but "+pTRef.getClass().getCanonicalName()+" found.(TLParametered.java#31)",
				pContext
			);
		}
		try {
			Engine        E     = pContext.getEngine();
			TRParametered TRPed = (TRParametered)pTRef;
			
			// Get the target type spec
			TypeRef  TargetRef  = TRPed.getTargetTypeRef();
			TypeSpec TargetSpec = TargetRef.getTypeSpec(E, pContext);
			
			// Obtain the type spec
			return TargetSpec.getParameteredTypeSpec(pContext, E, TRPed.ParamTypeRefs);
			
		} catch (Throwable T) {
			return new CurryError(
					"Type Loading Error: Unable to create parametered type ("+pTRef+"). <TLParametered.java#47>",
					pContext, T);
		}
	}
	
	static void throwUnmatchTypeParameter(TypeSpec pTargetTypeSpec, TypeRef ... pParameterTypeRefs) {
		throw new IllegalArgumentException(String.format(
				"Unmatch type parameter for %s (%s).",
				UArray.toString(pParameterTypeRefs, "<", ">", ","),
				pTargetTypeSpec));
	} 
	
	/** TypeRef that refer to the parameter of a parametered type */
	static public final class TRParametered extends TRBasedOnType {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		TypeRef   TargetTypeRef;
		TypeRef[] ParamTypeRefs;

		/** Constructs a parametered type reference. */
		public TRParametered(Engine pEngine, TypeRef pTargetTypeRef, TypeRef ... pParameterTypeRefs) {
			this(pTargetTypeRef.getTypeSpec(pEngine), pParameterTypeRefs);
		}
		
		/** Constructs a parameterized type . */
		public TRParametered(TypeRef pTargetType, TypeRef ... pParameterTypeRef) {
			this.TargetTypeRef = pTargetType.clone();
			this.ParamTypeRefs = (TypeRef[])UArray.deepClone(pParameterTypeRef);
		}
		
		/** Constructs a parameterized type . */
		TRParametered(Type pTargetType, TypeRef ... pParameterTypeRef) {
			this(pTargetType.getTypeSpec(), pParameterTypeRef);
		}
		/** Constructs a parameterized types. */
		TRParametered(TypeSpec pTargetTypeSpec, TypeRef ... pParameterTypeRefs) {
			this.TargetTypeRef = pTargetTypeSpec.getTypeRef();
			
			// Get the ParameterizationInfo and ensure it exists.
			TypeParameterInfo.TypeParameterInfos TPI = pTargetTypeSpec.getTypeParameterInfos();
			if(TPI == null)
				throw new IllegalArgumentException(
						"The target type is not a parameterized or paretered type: " +
						pTargetTypeSpec.getTypeRef().toString());

			if(pParameterTypeRefs == null) pParameterTypeRefs = TypeRef.EmptyTypeRefArray;
			if(TPI.getParameterTypeCount() != pParameterTypeRefs.length)
				TLParametered.throwUnmatchTypeParameter(pTargetTypeSpec, pParameterTypeRefs);
			
			// Save the parameter type reference
			this.ParamTypeRefs = (pParameterTypeRefs.length != 0)?pParameterTypeRefs.clone():pParameterTypeRefs;
		}

		/** Returns the type-reference kind name. */ @Override
		public String getRefKindName() {
			return TLParametered.KindName;
		}
		
		/** Returns the type reference of the target type */
		public TypeRef getTargetTypeRef() {
			return this.TargetTypeRef;
		}

		/**{@inheritDoc}*/ @Override
		void setTheType(Type pTheType) {
			super.setTheType(pTheType);
			this.TheSpec = (pTheType == null) ? null : pTheType.getTypeSpec();
		}
		/**{@inheritDoc}*/ @Override
		public void resetTypeRefForCompilation() {
			super.resetTypeRefForCompilation();
			this.TheSpec = null;
			if(this.TargetTypeRef != null) this.TargetTypeRef.resetForCompilation();
			if(this.ParamTypeRefs == null) return;
			for(int i = this.ParamTypeRefs.length; --i >= 0;) {
				TypeRef PTRef = this.ParamTypeRefs[i];
				if(PTRef == null) continue;
				PTRef.resetForCompilation();
			}
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean isDynamic() {
			if(TypeRef.isTypeRefDynamic(this.TargetTypeRef)) return true;

			for(int i = ((this.ParamTypeRefs == null) ? 0 : this.ParamTypeRefs.length); --i >= 0;) {
				TypeRef PTRef = this.ParamTypeRefs[i];
				if(TypeRef.isTypeRefDynamic(PTRef)) return true;
			}
			
			return false;
		}
		
		/** Returns the number of parameters of this type */
		public int getParameterCount() {
			return this.ParamTypeRefs.length;
		}

		/** Returns the parameter type at the index that this type ref refers to. */
		public TypeRef getParameterTypeRef(int Index) {
			return ((Index < 0) || (Index >= this.ParamTypeRefs.length))?null:this.ParamTypeRefs[Index];
		}
		
		/**{@inheritDoc}*/ @Override
		protected TRBasedOnType createNewTypeRef(Engine pEngine, TypeRef pNewBaseType, TypeSpec pNewBaseTypeSpec) {
			// If all the parameter is not BOT, return self
			boolean HasBOT = false;
			for(int i = this.ParamTypeRefs.length; (--i >= 0) && !HasBOT; )
				if(this.ParamTypeRefs[i] instanceof TRBasedOnType) HasBOT = true;
			if(!HasBOT) return this;
			
			HasBOT     = false;
			TypeRef TR = null;
			
			// Create a new TypeRef
			TRParametered TRef = (TRParametered)this.clone();
			
			// Try to resolve the Target
			TR = TLBasedOnType.newTypeRef(pEngine, TRef.TargetTypeRef, pNewBaseType, pNewBaseType,
					pNewBaseTypeSpec, pNewBaseTypeSpec);
			if(TR != TRef.TargetTypeRef) {
				TRef.TargetTypeRef = TR;
				HasBOT             = true;
			}

			for(int i = this.ParamTypeRefs.length; (--i >= 0) && !HasBOT; ) {
				TypeRef PTRef = TRef.ParamTypeRefs[i];
				TR = TLBasedOnType.newTypeRef(pEngine, PTRef, pNewBaseType, pNewBaseType, pNewBaseTypeSpec, pNewBaseTypeSpec);
				if(TR != TRef.ParamTypeRefs[i]) {
					TRef.ParamTypeRefs[i] = TR;
					HasBOT                = true;
				}
			}
			
			TRBasedOnType TRBOT = HasBOT ? TRef : this;
			
			// If there non of the parameters is recreated, no need to return the recreated.
			return TRBOT;
		}
		
		/**{@inheritDoc}*/ @Override
		public TypeRef clone() {
			// The cloning of each elements will be done inside
			return new TRParametered(this.getTargetTypeRef(), this.ParamTypeRefs);
		}
		
		/**{@inheritDoc}*/ @Override
		public TypeRef flatType(Engine pEngine, TypeSpec pNewBaseTypeSpec, TypeRef pParameterBaseTypeToIgnore) {
			// Flaten deeper
			TRParametered TRP = (TRParametered)this.clone();
			int     PCount    = TRP.ParamTypeRefs.length;
			boolean IsChanged = false;
			for(int i = 0; i < PCount; i++) {
				TypeRef TR = TLBasedOnType.flatBaseOnType(
								pEngine,
								TRP.ParamTypeRefs[i],
								pNewBaseTypeSpec,
								pParameterBaseTypeToIgnore);
				
				if(TR == TRP.ParamTypeRefs[i]) continue;
				
				TRP.ParamTypeRefs[i] = TR;
				IsChanged = true;
			}
			
			return IsChanged ? TRP : this;
		}
		
		// TypeSpec --------------------------------------------------------------------------------------------------------
		
		transient TypeSpec TheSpec = null;
		
		/**{@inheritDoc}*/ @Override
		protected TypeSpec getTypeSpecWithoutEngine() {
			return this.TheSpec;
		}
		
		// Objectable -----------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public String toString() {
			TypeSpec TS = this.getTargetTypeRef().getTypeSpecWithoutEngine();
			if((TS != null) && !TS.isToShowParameteredInfo())
				return TS.getToString(this);
			
			StringBuilder SB = new StringBuilder();
			SB.append(this.getTargetTypeRef());
			SB.append("<");
			for(int i = 0; i < this.getParameterCount(); i++) {
				if(i != 0) SB.append(",");
				SB.append(this.getParameterTypeRef(i));
			}
			SB.append(">");
			return SB.toString();
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			TypeSpec TS = this.getTargetTypeRef().getTypeSpecWithoutEngine();
			if((TS != null) && !TS.isToShowParameteredInfo())
				return TS.getToString(this);
			
			StringBuilder SB = new StringBuilder();
			SB.append(this.getTargetTypeRef().toDetail());
			SB.append("<");
			for(int i = 0; i < this.getParameterCount(); i++) {
				if(i != 0) SB.append(",");
				SB.append(this.getParameterTypeRef(i).toDetail());
			}
			SB.append(">");
			return SB.toString();
		}
		/**{@inheritDoc}*/ @Override
		public boolean equals(Object O) {
			if(this == O)                     return  true;
			if(!(O instanceof TRParametered)) return false;
			
			TRParametered TRP = (TRParametered)O;
			
			// Both are no-name, compare type spec.
			TypeRef TRef = TRP.getTargetTypeRef();
			if(!UObject.equal(this.getTargetTypeRef(), TRef))         return false;
			if(this.ParamTypeRefs        == TRP.ParamTypeRefs)        return  true;
			if(this.ParamTypeRefs.length != TRP.ParamTypeRefs.length) return false;
			return UObject.equal(this.ParamTypeRefs, TRP.ParamTypeRefs);
		}

		static private final int ParameterTypeRef_Hash = UObject.hash("ParameteredTypeRef");
		
		/**{@inheritDoc}*/ @Override
		public int hash() {
			TypeRef TargetRef = this.getTargetTypeRef();
			if(TargetRef instanceof TRNoName) {
				TypeSpec TargetSpec = TargetRef.getTypeSpecWithoutEngine();
				if(TargetSpec instanceof TSArray) {
					if(TKJava.TAny.getTypeRef().equals(this.getParameterTypeRef(0)))
						return UObject.hash("NoNameTypeRef") + UObject.hash(TargetSpec.toString());
				}
			} 
			
			return this.getTargetTypeRef().hash()*ParameterTypeRef_Hash + UObject.hash(this.ParamTypeRefs);
		}
	}

}
