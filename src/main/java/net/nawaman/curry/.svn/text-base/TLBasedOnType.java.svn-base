package net.nawaman.curry;

import net.nawaman.curry.TLParameter.TRParameter;
import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.TypeParameterInfo.TypeParameterInfos;

/** Type loader of type reference that requires another type to resolve it. */
abstract public class TLBasedOnType extends TypeLoader {
	
	/** Constructs a new no-name type loader. */
	TLBasedOnType(Engine pEngine) {
		super(pEngine);
	}
	
	// TypeRef ---------------------------------------------------------------------------------------------------------
	
	/** TypeRef that a type must be consulted in other to resolve this type */
	static abstract public class TRBasedOnType extends TypeRef {
		
		static private final long serialVersionUID = 5956145354645265566L;

		/** Constructs a type based on another type. */
		TRBasedOnType() {}
		/** Constructs a type based on another type. */
		TRBasedOnType(TypeRef pBaseTypeRef) {
			// Ensure not null
			if(pBaseTypeRef == null)
				throw new NullPointerException(
						"A "+this.getRefKindName()+" type ref cannot be constucted without the base type reference.");
			
			this.TheBaseTypeRef = pBaseTypeRef;
		}
		
		/** Type reference of the base type */
		       TypeRef TheBaseTypeRef = null;
		/** Returns the type reference of the base type that this type reference needs */
		public TypeRef getBaseTypeRef() {
			return this.TheBaseTypeRef;
		}

		/**{@inheritDoc}*/ @Override
		public void resetTypeRefForCompilation() {
			super.resetTypeRefForCompilation();
			if(this.TheBaseTypeRef != null) this.TheBaseTypeRef.resetForCompilation();
		}
		
		/**
		 * Checks if owner type should be used as base type instead of current when this type appear in element
		 *     declation.
		 **/
		public boolean isToUseOwnerAndNotCurrent() {
			return false;
		}
		
		/** Flatten the BaseOnType type ref to the actual type it refers to */
		public TypeRef flatType(Engine pEngine, TypeSpec pNewBaseTypeSpec, TypeRef pParameterBaseTypeToIgnore) {
			try { pEngine.getTypeManager().ensureTypeInitialized(this); } catch (Exception e) {}
			if(!this.isLoaded()) return null;
			
			return this.getTheType().getTypeRef();
		}
		
		// NOTE: pNewBaseTypeSpec may be given so there is no need to retrieve it (which may result in recursive error) 
		/** Creates a new TRBasedOnType from another TypeRef */
		abstract protected TRBasedOnType createNewTypeRef(Engine pEngine, TypeRef pNewBaseType, TypeSpec pNewBaseTypeSpec);
		
		final protected TypeRef newTypeRef(
									Engine   pEngine,
									TypeRef  pNewCurentType,     TypeRef  pOwnerType,
									TypeSpec pNewCurentTypeSpec, TypeSpec pOwnerTypeSpec) {
			
			// Pick an appropriate Base
			TypeRef  NewBaseType  = this.isToUseOwnerAndNotCurrent()?pOwnerType    :pNewCurentType;
			TypeSpec NewBaseTSpec = this.isToUseOwnerAndNotCurrent()?pOwnerTypeSpec:pNewCurentTypeSpec;
			TypeRef  TBRef        = this.getBaseTypeRef();
			if((TBRef == NewBaseType) || ((TBRef != null) && (TBRef.equals(NewBaseType)))) return this;
			
			// Replace with a base that is a parameted so the base is from its param and not itself
			if((this instanceof TRParameter) && (NewBaseType instanceof TRParametered)) {
				TypeRef            TargetRef  = ((TRParametered)NewBaseType).getTargetTypeRef();
				TypeSpec           TargetSpec = TargetRef.getTypeSpec(pEngine);
				TypeParameterInfos TPInfos    = (TargetSpec != null) ? TargetSpec.getTypeParameterInfos() : null;
				if(TPInfos != null) {
					int PIndex = TPInfos.getParameterIndex(((TRParameter)this).getParameterName());
					if(PIndex != -1) {
						TypeRef TRef = ((TRParametered)NewBaseType).getParameterTypeRef(PIndex);
						if(TRef != null) return TRef;
					}
				}
			}
			
			// If the old base is a BOT, resolve it first
			if(TBRef instanceof TRBasedOnType) {
				TypeRef  NBR  = NewBaseType;
				TypeSpec NBS  = NewBaseTSpec;
				TypeRef  NNBT = ((TRBasedOnType)TBRef).newTypeRef(pEngine, NBR, NBR, NBS, NBS);
				
				// The BOT base does not base on this new base so there is no need to create a new one for this BOT
				if(TBRef == NNBT) return this;
				
				// The new of the new base :D
				NewBaseType  = NNBT;
				NewBaseTSpec = null;
			}
			
			// Ensure the type is compatible
			if(!pEngine.getTypeManager().canReplaceBaseOfBOT(this, NewBaseType)) {
				throw new IllegalArgumentException(String.format("Un-compatible new base type '%s' for '%s'.", NewBaseType, this));
 			}
			
			// Actually create a new type
			return this.createNewTypeRef(pEngine, NewBaseType, NewBaseTSpec);
		}
	}
	
	// Utilities ------------------------------------------------------------------------------

	/** Modified the object based on the owner type reference */
	static public TypeRef newTypeRef(
							Engine   pEngine,            TypeRef  pTRef,
							TypeRef  pCurrentTRef,       TypeRef  pOwnerTRef,
							TypeSpec pNewCurentTypeSpec, TypeSpec pOwnerTypeSpec) {
		
		if(!(pTRef instanceof TRBasedOnType)) return pTRef;		
		return ((TRBasedOnType)pTRef).newTypeRef(pEngine, pCurrentTRef, pOwnerTRef, pNewCurentTypeSpec, pOwnerTypeSpec);
	}

	/** Flatten the BaseOnType type ref to the actual type it refers to */
	static public TypeRef flatBaseOnType(
							Engine   pEngine,          TypeRef pTRef,
							TypeSpec pNewBaseTypeSpec, TypeRef pParameterBaseTypeToIgnore) {
		
		if(!(pTRef instanceof TRBasedOnType)) return pTRef;		
		return ((TRBasedOnType)pTRef).flatType(pEngine, pNewBaseTypeSpec, pParameterBaseTypeToIgnore);
	}
	
}
