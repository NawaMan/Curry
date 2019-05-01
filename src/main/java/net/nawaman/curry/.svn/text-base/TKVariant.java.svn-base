package net.nawaman.curry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import net.nawaman.curry.AttributeInfo.AIVariant;
import net.nawaman.curry.OperationInfo.OIVariant;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.compiler.TypeSpecCreator;
import net.nawaman.curry.util.MoreData;

final public class TKVariant extends TypeKind {
	
	final static public String KindName = "Variant";

	// Vector<TypeRef>	: NOTE: all of these types must be in Required type
	final static public int Index_Interfaces        = 0;
	final static public int Index_ParameterizedInfo = 1;
	final static public int DataOperationInfoIndex  = 2;
	final static public int TypeAttributeInfoIndex  = 3;
	final static public int TypeOperationInfoIndex  = 4;
	final static public int Index_ExtraInfo         = 5;
	
	final static public int Index_AsType    = 0;
	final static public int Index_TypeToNew = 1;

	/** Constructs a new Variant Type Kind of the Engine */
	protected TKVariant(Engine pEngine) {
		super(pEngine);
	}

	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return KindName;
	}
	
	/** Checks if the Variant type spec is a wrapper (variant of one) */
	static boolean isWrapper(TSVariant TSV) {
		if(TSV == null) return false;
		return TSV.isWrapper();
	}
	
	// TypeSpec -------------------------------------------------------------------------
	
	/** TypeSpec of a Variant Type */
	static final class TSVariant extends TypeSpec {
		
		/** Constructs a TypeSpec for Variant Type */
		protected TSVariant(TypeRef pTypeRef, TypeRef pAsType, TypeRef[] pTypes, TypeRef pTypeToNew,
				TypeRef[] pInterfaceRefs, ParameterizedTypeInfo pTPInfo, MoreData pExtraInfo) {
			super(
				pTypeRef,
				(pExtraInfo == null)?
					new Serializable[] { pInterfaceRefs, pTPInfo, null, null, null } :
					new Serializable[] { pInterfaceRefs, pTPInfo, null, null, null, pExtraInfo },
				new TypeRef[pTypes.length + 2 + ((pInterfaceRefs == null) ? 0 : pInterfaceRefs.length)],
				null
			);
			System.arraycopy(pTypes, 0, this.RequiredTypes, 2, pTypes.length);
			this.RequiredTypes[Index_AsType]    = pAsType;
			this.RequiredTypes[Index_TypeToNew] = pTypeToNew;
			
			if(pExtraInfo != null) pExtraInfo.toFreeze();
			
			this.AsType    = this.getRequiredTypeRef(Index_AsType);
			this.TypeAsNew = this.getRequiredTypeRef(Index_TypeToNew);

			this.TypeList = new TypeRef[pTypes.length];
			System.arraycopy(this.RequiredTypes, 2, this.TypeList, 0, this.TypeList.length);
			
			if((pInterfaceRefs != null) && (pInterfaceRefs.length != 0)) {
				int IRefCount = pInterfaceRefs.length;
				System.arraycopy(pInterfaceRefs, 0, this.RequiredTypes, 2 + this.TypeList.length, IRefCount);
			}
		}
		
		// Classification ----------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public String getKindName() {
			return KindName;
		}
		
		// Services ----------------------------------------------------------------------
		
		transient TypeRef   AsType    = null;
		transient TypeRef   TypeAsNew = null;
		transient TypeRef[] TypeList  = null;
		
		void clearData() {
			this.AsType    = null;
			this.TypeAsNew = null;
			this.TypeList  = null;
		}
		
		/** Returns the TypeRef of this type AsType */
		TypeRef getAsType() {	
			if(this.AsType == null)
				this.AsType = this.getRequiredTypeRef(Index_AsType);		
			return this.AsType;
		}

		/** Returns the TypeRef of the Type that will be used for creating a new object */
		TypeRef getTypeRefToNew() {
			if(this.TypeAsNew == null)
				this.TypeAsNew = this.getRequiredTypeRef(Index_TypeToNew);
			return this.TypeAsNew;
		}
		
		/** Returns an array of TypeRef as possible member of this variant type */
		TypeRef[] getTypeList() {
			if(this.TypeList == null) {
				this.TypeList = new TypeRef[this.RequiredTypes.length - 2 - this.getInterfaceCount()];
				System.arraycopy(this.RequiredTypes, 2, this.TypeList, 0, this.TypeList.length);
			}
			return this.TypeList;
		}
		
		/**{@inheritDoc}*/ @Override
		protected void resolveParameteredTypeSpec(Context pContext, Engine pEngine) {
			Engine $Engine = pEngine; if(($Engine == null) && (pContext != null)) $Engine = pContext.getEngine();
			
			// Flatten the AsType
			this.AsType    = this.getRequiredTypeRef(Index_AsType);
			this.TypeAsNew = this.getRequiredTypeRef(Index_TypeToNew);
			if(this.AsType    instanceof TRBasedOnType) this.AsType    = TLBasedOnType.flatBaseOnType($Engine, this.AsType,    this, null);
			if(this.TypeAsNew instanceof TRBasedOnType) this.TypeAsNew = TLBasedOnType.flatBaseOnType($Engine, this.TypeAsNew, this, null);

			this.TypeList = new TypeRef[this.RequiredTypes.length - 2];
			System.arraycopy(this.RequiredTypes, 2, this.TypeList, 0, this.TypeList.length);
			for(int i = 0; i < this.TypeList.length; i++)
				this.TypeList[i] = TLBasedOnType.flatBaseOnType($Engine, this.TypeList[i], this, null);
		}
		
		/** Returns the number of memeber of this Type */
		public int getMemberCount() {
			TypeRef[] TRs = this.getTypeList();
			return (TRs == null)?0:TRs.length;
		}
		
		/** Returns the TypeRef of the member type at the index */
		public TypeRef getMemberAt(int I) {
			if(I < 0) return null;
			TypeRef[] TRs = this.getTypeList();
			return ((TRs == null) || (I >= TRs.length))?null:TRs[I];
		}

		/**{@inheritDoc}*/ @Override
		protected int getInterfaceIndex() {
			return Index_Interfaces;
		}
		
		/**{@inheritDoc}*/ @Override
		protected int getParameterizationInfoIndex() {
			return Index_ParameterizedInfo;
		}
		/**{@inheritDoc}*/ @Override
		protected int getMoreDataIndex() {
			return -1;
		}
		/**{@inheritDoc}*/ @Override
		protected int getExtraInfoIndex() {
			return (this.getDataCount() == Index_ExtraInfo)?-1:Index_ExtraInfo;
		}
		
		/**{@inheritDoc}*/ @Override
		protected int getDataOperationInfoIndex() {
			return DataOperationInfoIndex;
		}
		/**{@inheritDoc}*/ @Override
		protected int getTypeAttributeInfoIndex() {
			return TypeAttributeInfoIndex;
		}
		/**{@inheritDoc}*/ @Override
		protected int getTypeOperationInfoIndex() {
			return TypeOperationInfoIndex;
		}
		
		/**{@inheritDoc}*/ @Override
		void resolveParameteredRequiredAndUsedTypeRefs(Context pContext, Engine pEngine) {
			super.resolveParameteredRequiredAndUsedTypeRefs(pContext, pEngine);
			this.clearData();
		}
		
		/** Checks if this variant type spec is a wrapper (a variant of one type) */
		public boolean isWrapper() {
			if(this.getMemberCount() >= 1) return false;

			TypeRef MT = this.getMemberAt(0);
			if(MT == null) return true;
			
			if(!MT.equals(this.getAsType())) return false;
			
			TypeRef TN =this.getTypeRefToNew();
			if((TN != null) && !MT.equals(TN)) return false;
			
			return true;
		}
		
		// For compilation only --------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForCompilation() {			
			Util.ResetTypeRefs(this.getAsType());
			Util.ResetTypeRefs(this.getTypeRefToNew());
		}

		// Parameterization --------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForParameterization() {}
		
		// Objectable --------------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected boolean isToShowNoName() {
			return false;
		}

		/**{@inheritDoc}*/ @Override
		protected String getToString() {
			TypeRef TRef = this.getTypeRef();

			StringBuilder SB = new StringBuilder();
			
			if((TRef instanceof TLNoName.TRNoName) || (TRef instanceof TRParametered))
				SB.append("Variant:");
			else return this.getTypeRef().toString();

			TypeRef AsTRef    = this.getAsType();
			TypeRef TRefToNew = this.getTypeRefToNew();
			
			SB.append("<");
			if((AsTRef != null) || !TKJava.TAny.getTypeRef().equals(AsTRef)) SB.append(AsTRef).append("||");
			int Count = this.getMemberCount();
			for(int i = 0; i < Count; i++) {
				if(i != 0) SB.append("|");
				TypeRef TR = this.getMemberAt(i);
				if((AsTRef != null) && TR.equals(TRefToNew))
					 SB.append(":").append(TR).append(":");
				else SB.append(TR);
			}
			SB.append(">");
			
			if(this.getParameterizedTypeInfo() != null)
				SB.append(" ").append(this.getParameterizedTypeInfo());
			
			return SB.toString();
		}
	}
	
	// Type ----------------------------------------------------------------------------------------
	
	/** Variant Type */
	final public class TVariant extends Type {
		
		/** Constructs a Variant Type */
		protected TVariant(TypeKind pTypeKind, TKVariant.TSVariant pTS) {
			super(pTypeKind, pTS);
		}
		
		/** Returns the TypeRef of the AsType of this Variant Type */
		public TypeRef getAsTypeRef() {
			TypeRef TR = ((TKVariant.TSVariant)this.getTypeSpec()).getAsType();
			return (TR == null)?TKJava.TAny.getTypeRef():TR;
		}
		/** Returns the AsType of this Variant Type */
		public Type getAsType() {
			return this.getAsTypeRef().getTheType();
		}
		
		// Members -------------------------------------------------------------------------------------
		
		/** Returns the TypeRef of the Memner type at the index */
		public TypeRef getMemberRef(int pIndex) {
			TypeRef[] TRs = ((TKVariant.TSVariant)this.getTypeSpec()).getTypeList();
			if((pIndex < 0) || (pIndex >= TRs.length)) return null;
			return TRs[pIndex];
		}
		/** Returns the number of the member type at the index */
		public int getMemberCount() {
			return ((TKVariant.TSVariant)this.getTypeSpec()).getTypeList().length;
		}
		
		/** Returns the TypeRef of the Type to be used in creating a new instance */
		public TypeRef getTypeRefToNew() {
			return ((TKVariant.TSVariant)this.getTypeSpec()).getTypeRefToNew();
		}
		/** Returns the Type to be used in creating a new instance */
		public Type getTypeToNew() {
			TypeRef TR = ((TKVariant.TSVariant)this.getTypeSpec()).getTypeRefToNew();
			if(TR == null) return null;
			return TR.getTheType();
		}
		
		/** Checks if the Variant type spec is a wrapper (variant of one) */
		public boolean isWrapper() {
			return ((TSVariant)this.getTypeSpec()).isWrapper();
		}
		
		// StackOwner --------------------------------------------------------------------------------------------------
	
		// Middle-Level search ---------------------------------------------------------------------

		// These are only use for information searching the above methods (the low-level is used for execution and access)

		/**{@inheritDoc}*/ @Override
		protected TypeRef searchTypeAttributeLocal(Engine pEngine, String pName) {
			return this.searchAttributeLocal(pEngine, null, pName);
		}

		/**{@inheritDoc}*/ @Override
		protected ExecSignature searchTypeOperationLocal(Engine pEngine, OperationSearchKind pOSKind, Object pParam1,
				Object pParam2, Object pParam3) {
			return this.searchOperationLocal(pEngine, pOSKind, pParam1, pParam2, pParam3);
		}
	
		// Middle-Level search ---------------------------------------------------------------------------------------------

		// These are only use for information searching the above methods (the low-level is used for execution and access)

		/**{@inheritDoc}*/ @Override
		protected ExecSignature searchObjectOperation(Object pTheObject, Engine pEngine,
				OperationSearchKind pOSKind, boolean pIsSearchInDynamicDelegation, Object pParam1, Object pParam2,
				Object pParam3) {

			// Try the old way first
			ExecSignature ES = super.searchObjectOperation(pTheObject, pEngine, pOSKind, pIsSearchInDynamicDelegation,
					pParam1, pParam2, pParam3);
			if(ES != null) return ES;
			
			// See if we can find the operation in all member type
			for(int i = this.getMemberCount(); --i >= 0; ) {
				TypeRef TR = this.getMemberRef(i);
				this.getEngine().getTypeManager().ensureTypeInitialized(TR);
					
				if(ES == null) {
					ES = TR.getTheType().searchObjectOperation(pTheObject, pEngine, pOSKind, pIsSearchInDynamicDelegation,
							pParam1, pParam2, pParam3);
					
					if(ES == null) return null;
					continue;
				}
				
				ExecSignature ThisES = TR.getTheType().searchObjectOperation(pTheObject, pEngine, pOSKind,
							pIsSearchInDynamicDelegation, pParam1, pParam2, pParam3);
				
				if(ThisES == null) return null;
				
				if(ES.getParamCount() != ThisES.getParamCount()) return null;
				if(ES.isVarArgs()     != ThisES.isVarArgs())     return null;
				
				boolean IsSame = true;
				
				TypeRef TReturn = null;
				if(ES.getReturnTypeRef().equals(ThisES.getReturnTypeRef())) {
					TReturn = ES.getReturnTypeRef();
				} else if(MType.CanTypeRefByAssignableByInstanceOf(null, pEngine,
						ES.getReturnTypeRef(), ThisES.getReturnTypeRef())) {
					// contravariant
					TReturn = ES.getReturnTypeRef();
				} else if(MType.CanTypeRefByAssignableByInstanceOf(null, pEngine,
						ThisES.getReturnTypeRef(), ES.getReturnTypeRef())) {
					// contravariant
					TReturn = ThisES.getReturnTypeRef();
					IsSame = false;
				} else return null;
				
				TypeRef[] TParams = new TypeRef[ES.getParamCount()];
				String[]  TPNames = new String[ ES.getParamCount()];
				for(int p = TParams.length; --p >= 0; ) {
					TypeRef     ESTR =     ES.getParamTypeRef(p);
					TypeRef ThisESTR = ThisES.getParamTypeRef(p);
					if(ESTR.equals(ThisESTR)) {
						TParams[p] = ESTR;
					} else if(MType.CanTypeRefByAssignableByInstanceOf(null, pEngine, ESTR, ThisESTR)) {
						// covariant
						TParams[p] = ThisES.getParamTypeRef(p);
						IsSame     = false;
					} else if(MType.CanTypeRefByAssignableByInstanceOf(null, pEngine, ThisESTR, ESTR)) {
						// covariant
						TParams[p] = ES.getParamTypeRef(p);
					} else return null;
					
					TPNames[p] = ExecInterface.AutoParamNamePrifix + p;
				}
				
				if(IsSame) continue;
				ES = ExecSignature.newSignature(ES.getName(),
						TParams, TPNames,
						ES.isVarArgs(),
						TReturn,
						null, null);
			}
			
			return ES;
		}

		/**{@inheritDoc}*/ @Override
		protected TypeRef searchObjectAttribute(Object pTheObject, Engine pEngine, boolean pIsSearchInDynamicDelegation,
				Type pAsType, String pName) {

			// Try the old way first
			TypeRef TRef = super.searchObjectAttribute(pTheObject, pEngine, pIsSearchInDynamicDelegation, pAsType, pName);
			if(TRef != null) return TRef;

			// See if we can find the operation in all member type
			for(int i = this.getMemberCount(); --i >= 0; ) {
				TypeRef TR = this.getMemberRef(i);
				this.getEngine().getTypeManager().ensureTypeInitialized(TR);
					
				if(TRef == null) {
					TRef = TR.getTheType().searchObjectAttribute(pTheObject, pEngine, pIsSearchInDynamicDelegation, pAsType, pName);
					
					if(TRef == null) return null;
					continue;
				}
				
				TypeRef ThisTRef = TR.getTheType().searchObjectAttribute(pTheObject, pEngine, pIsSearchInDynamicDelegation, pAsType, pName);
				
				if(ThisTRef == null) return null;
				
				if(MType.CanTypeRefByAssignableByInstanceOf(null, pEngine, TRef, ThisTRef)) {
					// contravariant
					//TRef = TRef;
				} else if(MType.CanTypeRefByAssignableByInstanceOf(null, pEngine, ThisTRef, TRef)) {
					// contravariant
					TRef = ThisTRef;
				} else return null;
			}
			return TRef;
		}
		/**{@inheritDoc}*/ @Override
		public String toString() {
			TypeRef TRef = this.getTypeRef();
			
			// Other ways of showing
			if(!(TRef instanceof TLNoName.TRNoName) && !(TRef instanceof TLParametered.TRParametered))
				return TRef.toString();

			TypeRef AsTRef    = this.getAsTypeRef();
			TypeRef TRefToNew = this.getTypeRefToNew();
			
			StringBuilder SB = new StringBuilder();
			SB.append("Variant:<");
			if((AsTRef != null) || !TKJava.TAny.getTypeRef().equals(AsTRef)) SB.append(AsTRef).append("||");
			int Count = this.getMemberCount();
			for(int i = 0; i < Count; i++) {
				if(i != 0) SB.append("|");
				TypeRef TR = this.getMemberRef(i);
				if((AsTRef != null) && TR.equals(TRefToNew))
					 SB.append(":").append(TR).append(":");
				else SB.append(TR);
			}
			SB.append(">");

			return SB.toString();
		}
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		return false;
	}
	
	// ----------------------------------------------------------------------------------
	
	/** Creates a Noname TypeRef for a variant type */
	static public TypeRef newNoNameVariantTypeRef(TypeRef pAsType, TypeRef[] pTypes, TypeRef pTypeToNew,
			ParameterizedTypeInfo pTPInfo) {
		return newNoNameVariantTypeRef(pAsType, pTypes, pTypeToNew, null, pTPInfo);
	}
	
	/** Creates a Noname TypeRef for a variant type */
	static public TypeRef newNoNameVariantTypeRef(TypeRef pAsType, TypeRef[] pTypes, TypeRef pTypeToNew,
			TypeRef[] pInterfaceRefs, ParameterizedTypeInfo pTPInfo) {
		// Default as Type
		if(pAsType == null) pAsType = TKJava.TAny.getTypeRef();
		// Creates the TypeSpec and return the Ref
		return (new TSVariant(null, pAsType, pTypes, pTypeToNew, pInterfaceRefs, pTPInfo, null)).getTypeRef();
	}

	/** Creates a TypeSpec for Variant Type */
	public TypeSpec getTypeSpec(TypeRef pTypeRef, TypeRef pAsType, TypeRef[] pTypes, TypeRef pTypeToNew,
			ParameterizedTypeInfo pTPInfo, Documentation pDocument, StringBuffer pSB) {
		return this.getTypeSpec(pTypeRef, pAsType, pTypes, pTypeToNew, null, pTPInfo, pDocument, pSB);
	}
	
	/** Creates a TypeSpec for Variant Type */
	public TypeSpec getTypeSpec(TypeRef pTypeRef, TypeRef pAsType, TypeRef[] pTypes, TypeRef pTypeToNew,
			TypeRef[] pInterfaceRefs, ParameterizedTypeInfo pTPInfo, Documentation pDocument, StringBuffer pSB) {
		return this.getTypeSpec(pTypeRef, pAsType, pTypes, pTypeToNew, pInterfaceRefs, pTPInfo, false, pDocument, pSB);
	}
	
	/** Creates a TypeSpec for Variant Type */
	protected TypeSpec getTypeSpec(TypeRef pTypeRef, TypeRef pAsType, TypeRef[] pTypes, TypeRef pTypeToNew,
			TypeRef[] pInterfaceRefs, ParameterizedTypeInfo pTPInfo, boolean pIsVerify, Documentation pDocument, StringBuffer pSB) {
		if(pAsType == null) pAsType = TKJava.TAny.getTypeRef();
		
		TSVariant TSV = new TSVariant(pTypeRef, pAsType, pTypes, pTypeToNew, pInterfaceRefs, pTPInfo,
				(pDocument == null)?null:new MoreData(Documentation.MIName_Documentation, pDocument));
		
		if(pIsVerify) {
			String Error = ensureTypeSpecFormat(this.getEngine(), TSV);
			if(Error != null) {
				if(pSB != null) pSB.append(Error);
				return null;
			}
		}
		
		return TSV;
	}

	/** Creates a TypeSpec Creator for a Variant Type */
	public TypeSpecCreator getTypeSpecCreator(final TypeRef pAsType, final TypeRef[] pTypes, final TypeRef pTypeToNew,
			final TypeRef[] pInterfaceRefs, final ParameterizedTypeInfo pTPInfo) {
		return new TypeSpecCreator() {
			public TypeSpec newTypeSpec(Engine pEngine, TypeRef pTRef, boolean pIsVerify, Documentation pDocument) {
				StringBuffer SB = new StringBuffer();
				TypeSpec TS = getTypeSpec(pTRef, pAsType, pTypes, pTypeToNew, pInterfaceRefs, pTPInfo, pDocument, SB);
				if(SB.length() != 0)
					throw new IllegalArgumentException("Unable to create type specification for a variant type '"+pTRef+"': " + SB);
				return TS;
			}
		}; 
	}
	
	// -----------------------------------------------------------------------------------
	
	/** Ensure the format of TypeSpec for a Variant Type Spec */
	static String ensureTypeSpecFormat(Engine pEngine, TypeSpec pTypeSpec) {
		if(pTypeSpec == null)                 return "Null TypeSpec.";
		if(!(pTypeSpec instanceof TSVariant)) return "TypeInfo is mal-form (TS_Variant is required).";
		TSVariant TSV = (TSVariant)pTypeSpec;
		// TODELETE - Delete it when sure
		// TSV.clearData();
		return ensureTypeSpecFormat(pEngine, TSV, TSV.getTypeRef(), TSV.getAsType(), TSV.getTypeList(), TSV.getTypeRefToNew());
	}
	/** Ensure the format of TypeSpec for a Variant Type Spec */
	static String ensureTypeSpecFormat(Engine pEngine, TSVariant TSV, TypeRef pRef, TypeRef pAsType, TypeRef[] pTypes, TypeRef pTypeToNew) {
		if(pTypes        == null) return "Mal-formed TypeSpec: Type list is null (Variant type "+pRef+").";
		if(pTypes.length ==    0) return "Mal-formed TypeSpec: Type list is empty (Variant type "+pRef+").";
		
		boolean IsTypeAsNewValue = false;
		for(int i = pTypes.length; --i >= 0; ) {
			TypeRef TRef = pTypes[i];
			
			if(TRef != null) {
				if((pTypeToNew != null) && TRef.canBeAssignedByInstanceOf(pEngine, pTypeToNew))
					IsTypeAsNewValue = true;
				
				if(!TKJava.TVoid.getTypeRef().equals(TRef) &&
				   ((pAsType == null) || pAsType.canBeAssignedByInstanceOf(pEngine, TRef)))
					continue;
			}
			return "Mal-formed TypeSpec: the element #"+i+" in the type list is null or void (Variant type "+pRef+").";
		}
		if(!IsTypeAsNewValue && (pTypeToNew != null))
			return "Mal-formed TypeSpec: the type for new instance is not compatible with on the member type (Variant type "+pRef+").";
		return null;
	}
	
	// ----------------------------------------------------------------------------------
	
	// It is very important to remember that Required Types in pTypeInfo may not be resolved
	//     and initialized. Therefore, only use them as TypeRefs
	/**{@inheritDoc}*/ @Override
	protected Type getType(Engine pEngine, Context pContext, TypeSpec pTypeSpec) {
		return new TVariant(this, (TSVariant)pTypeSpec);
	}

	// Get Type -------------------------------------------------------------------------
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOf(Context pContext, Object pObj) {
		return null;
	}
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		return null;
	}
	
	// Typing --------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TVariant.class;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getDefaultType(Context pContext) {
		return null;
	}
	
	// Information and functionality ------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pTS) {
		if(!(pTS instanceof TSVariant)) return null;
		Engine E = this.getEngine();
		if((E == null) && (pContext != null)) E = pContext.getEngine();
		if(E == null) return Object.class;
		
		E.getTypeManager().ensureTypeInitialized(pContext, pTS.getTypeRef());
		Type T = pTS.getTypeRef().getTheType();
		if(T instanceof TVariant)
			return ((TVariant)T).getAsTypeRef().getDataClass(E);
		
		return E.getTypeManager().getDataClassOf(((TSVariant)pTS).getAsType());
	}
	
	// Check if the data object is a valid data of this type.
	// This method will be called only after the data class is checked
	//     so there is no need to check for the data class again.
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec, Object pByObject) {
		// This is in an assumption that all members of pType are initialized in the initialization process
		TSVariant TSV = (TSVariant)pTheTypeSpec;
		for(int i = TSV.getMemberCount(); --i >= 0; ) {
			if(MType.CanTypeRefByAssignableBy(pContext, pEngine, TSV.getMemberAt(i), pByObject))
				return true;
		}
		return false;
	}
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec, TypeSpec BySpec) {
		TSVariant TheVariant = (TSVariant)TheSpec;
		for(TypeRef TR : TheVariant.getTypeList()) {
			// Any match return true
			if(MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TR, BySpec.getTypeRef()))
				return true;
		}
		return false;
	}
	
	// Revert type checking ------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanTypeBeAssignedByTypeWith_Revert(Context pContext, Engine pEngine, TypeSpec TheSpec,
			TypeSpec BySpec) {
		if(!(BySpec instanceof TSVariant)) return false;
		
		TypeRef AsTypeRef = ((TSVariant)BySpec).getAsType();
		if(AsTypeRef == null) AsTypeRef = TKJava.TAny.getTypeRef();
		// If the type of TheSpec can be assigned by the AsType of the BySpec then it can be assigned by this object
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec.getTypeRef(), AsTypeRef);
	}
	
	// Instantiation ----------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected boolean isNeedInitialization() {
		return false;
	}

	/**{@inheritDoc}*/ @Override
	protected Object getTypeDefaultValue(Context pContext, Type pThisType) {
		Type T = ((TVariant)pThisType).getTypeToNew();
		if(T == null) return null;
		return T.getDefaultValue(pContext);
	}
	/**{@inheritDoc}*/ @Override
	protected Object getTypeNoNullDefaultValue(Context pContext, Type pThisType) {
		Type T = ((TVariant)pThisType).getTypeToNew();
		if(T == null) return null;
		return T.getNoNullDefaultValue(pContext);
	}
	
	/** Checks if the type pThisType is an abstract. */
	@Override
	protected boolean isTypeAbstract(Context pContext, Type pThisType) {
		Type T = ((TVariant)pThisType).getTypeToNew();
		if(T == null) return true;
		return T.isAbstract(pContext);
	}
	/**{@inheritDoc}*/ @Override
	protected Object createNewTypeInstance(Context pContext, Executable pInitiator, Type pThisType,
				Object pSearchKey, Object[] pParams) {
		Type T = ((TVariant)pThisType).getTypeToNew();
		if(T == null) return null;
		return T.newInstance(pContext, pInitiator, pSearchKey, pParams);
	}
	
	/**{@inheritDoc}*/ @Override
	protected ConstructorInfo[] getConstructorInfos(Context pContext, Engine pEngine, Type pTheType) {
		// Precondition
		if(pTheType == null) throw new NullPointerException();
		if(!(pTheType instanceof TVariant))
			throw new CurryError("Internal Error: Wrong Type Kind ("+pTheType.getTypeKindName()+").(TKVariant.java#331)", pContext);
		
		if(this.isTypeAbstract(pContext, pTheType)) return null;
		
		TypeRef TR = ((TVariant)pTheType).getTypeRefToNew();
		this.getEngine().getTypeManager().ensureTypeInitialized(pContext, TR);
		
		return TR.getTheType().getConstructorInfos(pContext);
	}
		
	/**{@inheritDoc}*/ @Override
	protected Exception initializeType(Context pContext, Type pThisType) {
		if(pThisType ==  null)               return new NullPointerException();
		if(!(pThisType instanceof TVariant)) return new IllegalArgumentException("Wrong type.(TKVariant.java#176)");
		
		TypeRef AsTR = ((TSVariant)pThisType.getTypeSpec()).getAsType();
		if((AsTR == null) || TKJava.TAny.getTypeRef().equals(AsTR)) return null;
		
		for(TypeRef TR : ((TSVariant)pThisType.getTypeSpec()).getTypeList()) {
			if(!AsTR.canBeAssignedByInstanceOf(this.getEngine(), TR)) {
				return new CurryError("All members of a variant type must be assignable to its 'AsType' but "+TR.toString()+" is not.", pContext);
			}
		}
		return null;
	}

	// PostInitialize --------------------------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected Exception doValidateType(Context pContext, Type pType) {
		String Err = ensureTypeSpecFormat(this.getEngine(), pType.getTypeSpec());
		if(Err != null) {
			throw new CurryError("Type Creation Error: " +
					"The following error occur while trying to create a type " +
					pType.getTypeSpec().getTypeRef().toString() + ": " + Err + ".(TKVariant.java#92)",
					pContext
				);
		}
		return null;
	}
	
	// Elements from TypeKind ----------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindFields(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<AttributeInfo> AIs) {
		// Find all attributes that are common to all the member.
		
		// Prepare the often used
		Engine $Engine = this.getEngine(); if((($Engine = pEngine) == null) && (pContext != null)) $Engine = pContext.getEngine();
		MType  $MT     = $Engine.getTypeManager();
		
		
		HashMap<String, TypeRef> CommonAttributes = new HashMap<String, TypeRef>();
		
		TVariant TheType     = (TVariant)pTheType;
		int      MCount      = TheType.getMemberCount();
		boolean  HaveHadAttr = false;
		
		for(int i = 0; i < MCount; i++) {
			TypeRef MemberRef = TheType.getMemberRef(i);
			Type    Member    = $MT.getTypeFromRefNoCheck(pContext, MemberRef);

			$MT.ensureTypeValidated(pContext, Member, null);
			AttributeInfo[] MemberAIs = pIsStatic ? Member.getAttributeInfos() : Member.getObjectAttributeInfos();
			if((MemberAIs == null) ||( MemberAIs.length == 0)) continue;
			
			if(CommonAttributes.size() == 0) {
				// There were some elements added but all are not intersected
				if(HaveHadAttr) return;
				
				// The first member to have this
				for(AttributeInfo AI : MemberAIs) {
					if(AI == null) continue;
					if(!Accessibility.Public.equals(AI.getReadAccessibility())) continue;
					
					// Add the Attribute in to the collection
					String  AName = AI.getName();
					TypeRef ATRef = AI.getDeclaredTypeRef();
					CommonAttributes.put(AName, ATRef);
				}
				// Have had elements added into the collections
				HaveHadAttr = true;
				
			} else {
				// The later member
				Set<String> ANames_NotIntersect = new HashSet<String>(CommonAttributes.keySet());
				for(AttributeInfo AI : MemberAIs) {
					if((AI == null) || Accessibility.Public.equals(AI.getReadAccessibility())) continue;
					// Remove the attribute name from the list that is not intersect
					ANames_NotIntersect.remove(AI.getName());
				}
				
				// Remove all the list that is not intersect
				for(String ANameNotIntersect : ANames_NotIntersect)
					CommonAttributes.remove(ANameNotIntersect);
				
				// Ensure that the rest of the attribute have a compatible tyepref
				for(AttributeInfo AI : MemberAIs) {
					if(AI == null) continue;
					
					String  AName = AI.getName();
					TypeRef ATRef = AI.getDeclaredTypeRef();
					TypeRef ARef  = CommonAttributes.get(AName);
					
					if       (MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, ATRef, ARef )) {
						// Already the preferred one
						continue; 
					} else if(MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, ARef , ATRef)) {
						// The new one is a smaller one, use this one
						CommonAttributes.put(AName, ATRef);
					} else {
						// Not compatible so cannot be used
						CommonAttributes.remove(AName);
					}
				}
			}
		}
		
		// Add all common Attributes as variant
		for(String AName : CommonAttributes.keySet()) {
			AIVariant AIV = new AttributeInfo.AIVariant(AName, CommonAttributes.get(AName));
			
			if(pIsStatic)
				 this.addTypeAttributeToAttributeList(pContext, pEngine, AIs, AIV);
			else this.addDataAttributeToAttributeList(pContext, pEngine, AIs, AIV);
		}
	}
	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {

		// Find all attributes that are common to all the member.
		
		// Prepare the often used
		Engine $Engine = this.getEngine(); if((($Engine = pEngine) == null) && (pContext != null)) $Engine = pContext.getEngine();
		MType  $MT     = $Engine.getTypeManager();
		
		
		HashSet<ExecSignature> CommonOperations = new HashSet<ExecSignature>();
		
		TVariant TheType     = (TVariant)pTheType;
		int      MCount      = TheType.getMemberCount();
		boolean  HaveHadOper = false;

		for(int i = 0; i < MCount; i++) {
			TypeRef MemberRef = TheType.getMemberRef(i);
			Type    Member    = $MT.getTypeFromRefNoCheck(pContext, MemberRef);

			$MT.ensureTypeValidated(pContext, Member, null);
			
			OperationInfo[] MemberOIs = pIsStatic ? Member.getOperationInfos() : Member.getObjectOperationInfos();
			if((MemberOIs == null) ||( MemberOIs.length == 0)) continue;

			HashSet<ExecSignature> OSigns = new HashSet<ExecSignature>();
			if(CommonOperations.size() == 0) {
				// There were some elements added but all are not intersected
				if(HaveHadOper) return;
				
				// The first member to have this
				for(OperationInfo OI : MemberOIs) {
					if(OI == null) continue;
					if(!Accessibility.Public.equals(OI.getAccessibility())) continue;
					
					// Add the Operation in to the collection
					ExecSignature OSign = OI.getDeclaredSignature();
					OSigns.add(OSign);
				}
				// Have had elements added into the collections
				HaveHadOper = true;
				
			} else {
				// The later member
				ExecSignature[] CommonOIESs = CommonOperations.toArray(new ExecSignature[CommonOperations.size()]);
				MOI: for(OperationInfo OI : MemberOIs) {
					if((OI == null) || !Accessibility.Public.equals(OI.getAccessibility())) continue;
					
					// See if we can find a compatible exectuable in the list
					ExecSignature ES = OI.getDeclaredSignature();
					
					// 1. Search Regularly
					int Score = ExecInterface.Util.searchSignatureBySignature(pEngine, pContext, CommonOIESs, ES, false);
					if(Score >= ExecSignature.ExactMatch) {
						OSigns.add(ES);
						continue MOI;
					}
					
					// 2. See if there an OI from the common that can fit in this OI
					String OIName     = ES.getName();
					int    COIESCount = CommonOIESs.length;
					for(int o = 0; o < COIESCount; o++) {
						ExecSignature CES = CommonOIESs[o];
						if(CES == null) continue;
						
						if(!OIName.equals(CES.getName())) continue;
						
						Score = ExecInterface.Util.isCompatibleWith(pEngine, pContext, ES, CES);
						if(Score >= ExecSignature.ExactMatch) {
							OSigns.add(CES);
							continue MOI;
						} 
					}
				}
			}
			
			CommonOperations = OSigns;
		}
		
		// Add all common Operations as variant
		for(ExecSignature OSign : CommonOperations) {
			OIVariant OIV = new OperationInfo.OIVariant(OSign, Executable.ExecKind.SubRoutine);
			
			if(pIsStatic)
				 this.addTypeOperationToOperationList(pContext, pEngine, OIs, OIV);
			else this.addDataOperationToOperationList(pContext, pEngine, OIs, OIV);
		}
	}
}