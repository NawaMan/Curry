package net.nawaman.curry;

import net.nawaman.util.UObject;

/** TypeLoader of a Parameter type reference*/
public class TLParameter extends TLBasedOnType {
	
	/** KindName of this Type Loader */
	static final public String KindName = "Parameter";
	
	/** Constructs a new Current type loader. */
	TLParameter(Engine pEngine) {
		super(pEngine);
	}
	
	// To Satisfy TypeLoader -------------------------------------------------------------
	
	/** Returns the loader kind name */ @Override
	public String getKindName() {
		return KindName;
	}
	
	/** Loads a type spec from the type reference. */ @Override
	protected Object loadTypeSpec(Context pContext, TypeRef pTRef) {
		Engine E = pContext.getEngine();
				
		// Check if the TypeRef is the correct kind
		if(!(pTRef instanceof TRParameter)) {
			// Returns the error
			return new CurryError(
				"Type Loading Error: Wrong TypeRef Class. net.nawaman.curry.TLParameter.TRParameter " +
				"expected but "+pTRef.getClass().getCanonicalName()+" found.(TLParameter.java#31)",
				pContext
			);
		}
		try {
			TRParameter TRParam = (TRParameter)pTRef;
			String      PName   = TRParam.getParameterName();			
			TypeRef     BTRef   = TRParam.TheBaseTypeRef;
			Type        BType   = null;
			// If the base type is null or this Context have a stackowner that is not a package 
			if((BTRef == null) || (pContext.getStackOwner() != pContext.getStackOwnerAsPackage())) {
				BType = pContext.getStackOwnerAsType();
				BTRef = (BType == null) ? null : BType.getTypeRef();
				
				if(BTRef == null) {
					return new CurryError(
							"Type Loading Error: Unable to create parameter type ("+pTRef+") because the current " +
							"context does not belong to a type. <TLParameter.java#45>",
							pContext);
				}
			}
			
			TypeParameterInfo.TypeParameterInfos TPIs = BTRef.getTypeSpec(E).getTypeParameterInfos();
			
			if(TPIs == null) {
				// Returns the error
				return new CurryError(
					"Type Loading Error: The type "+BTRef+" is not a parameterized or parameter type.(TLParameter.java#31)",
					pContext
				);
			}
			
			TypeRef ParamTRef = TPIs.getParameterTypeRef(PName);
			if(ParamTRef == null) {
				// Returns the error
				return new CurryError(
					"Type Loading Error: The type "+BTRef+" does not have a parameter named \""+PName+"\".<TLParameter.java#53>",
					pContext
				);
			}
			return ParamTRef.getTypeSpec(pContext.getEngine());
			
		} catch (Throwable T) {
			return new CurryError(
					"Type Loading Error: Unable to create parameter type ("+pTRef+"). <TLParameter.java#62>",
					pContext, T);
		}
	}
	
	/** TypeRef that refer to the parameter of a parametered type */
	static public final class TRParameter extends TRBasedOnType {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		String ParameterName;

		/** Constructs a type based on another type. */
		public TRParameter(String pName) {
			super();
			this.ParameterName = pName;
		}
		/** Constructs a type based on another type. */
		public TRParameter(TypeRef pBaseTypeRef, String pName) {
			super(pBaseTypeRef);
			this.ParameterName = pName;
		}

		/** Returns the type-reference kind name. */ @Override
		public String getRefKindName() {
			return TLParameter.KindName;
		}

		/** Returns the parameter name that this type ref refers to. */
		public String getParameterName() {
			return this.ParameterName;
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean isToUseOwnerAndNotCurrent() {
			return false;
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean isDynamic() {
			// return TypeRef.isTypeRefDynamic(this.TheBaseTypeRef);
			return true;
		}

		/**{@inheritDoc}*/ @Override 
		public TypeRef flatType(Engine pEngine, TypeSpec pNewBaseTypeSpec, TypeRef pParameterBaseTypeToIgnore) {
			if(pParameterBaseTypeToIgnore != null) {
				TypeRef BTRef;
				if(((BTRef = ((TRParameter)this).getBaseTypeRef()) == null) || BTRef.equals(pParameterBaseTypeToIgnore))
					return this;
			}
			
			TypeSpec TS = (pNewBaseTypeSpec != null) ? pNewBaseTypeSpec : this.getBaseTypeRef().getTypeSpec(pEngine);
			TypeParameterInfo.TypeParameterInfos TPIs = TS.getTypeParameterInfos();
			return TPIs.getParameterTypeRef(this.getParameterName());
		}
			
		/** Creates a new TRBasedOnType from another TypeRef */ @Override
		protected TRParameter createNewTypeRef(Engine pEngine, TypeRef pNewBaseType, TypeSpec pNewBaseTypeSpec) {
			if(pNewBaseType == null) return new TRParameter(this.getParameterName());
			
			// If the given TypeSpec is not for the given TypeRef, get it 
			if((pNewBaseTypeSpec == null) || (pNewBaseTypeSpec.getTypeRef() != pNewBaseType))
				pNewBaseTypeSpec = pNewBaseType.getTypeSpec(pEngine);
			
			TypeSpec TS  = pNewBaseTypeSpec;
			TypeParameterInfo.TypeParameterInfos TPIs = TS.getTypeParameterInfos();
			if(TPIs == null)
				throw new CurryError(String.format(
					"The base type of TRParameter must be either parameterized or parametered type (%s is not). <TLParameter#101>",
					pNewBaseType
				));
			if(!TPIs.containParameterTypeRef(this.getParameterName()))
				throw new CurryError(String.format(
						"The base type of TRParameter does not have a parameter named %s (%s). <TLParameter#101>",
						this.getParameterName(), pNewBaseType
					));
			return new TRParameter(pNewBaseType, this.getParameterName());
		}
		
		/**{@inheritDoc}*/ @Override 
		public TypeRef clone() {
			if(this.TheBaseTypeRef == null)
				 return new TRParameter(                     this.ParameterName);
			else return new TRParameter(this.TheBaseTypeRef, this.ParameterName);
		}
		
		// Objectable -----------------------------------------------------------------------
		
		/** Returns the short string representation of the object. */ @Override
		public String toString() {
			return "Parameter{" + this.getParameterName() + "}";
		}
		/** Returns the long string representation of the object. */ @Override
		public String toDetail() {			
			if(this.getTheType() == null) {
				String B = (this.getBaseTypeRef() == null) ? "" : this.getBaseTypeRef().toString();
				return "Parameter{" + B + "::" + this.getParameterName() + "}";
			} else return this.getTheType().toDetail();
		}
		/** Checks if O equals to this object. */ @Override
		public boolean equals(Object O) {
			if(this == O)                   return  true;
			if(!(O instanceof TRParameter)) return false;
			// Both are no-name, compare type spec.
			TypeRef TRef1 = this.getBaseTypeRef();
			if(TRef1 == null) TRef1 = TKJava.TAny.getTypeRef();
			TypeRef TRef2 = ((TRParameter)O).getBaseTypeRef();
			if(TRef2 == null) TRef2 = TKJava.TAny.getTypeRef();
			return UObject.equal(TRef1, TRef2) && UObject.equal(this.getParameterName(), ((TRParameter)O).getParameterName());
		}
		
		static private final int ParameterTypeRef_Hash = UObject.hash("ParameterTypeRef");
		
		/** Returns the integer representation of the object. */ @Override
		public int hash() {
			return ParameterTypeRef_Hash + UObject.hash(this.toString());
		}
	}

}
