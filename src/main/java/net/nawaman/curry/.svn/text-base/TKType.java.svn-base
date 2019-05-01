package net.nawaman.curry;

import java.io.Serializable;
import java.util.Vector;

import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.compiler.TypeSpecCreator;

// TypeInfo structure of Type
// Data    {}
// TypeRef { TheType }

/**
 * Type of a type
 * 
 * This type has another type as its only instance. It allows an access to the referred type static elements as this type
 *     non-static elements. This is very useful in the the compilation mechanism as the compiler can treat the type of
 *     type as it is just another type.
 **/
final public class TKType extends TypeKind {
	
	// Constants ------------------------------------------------------------------------
	
	static final public String KindName = "Type";
	static final public String ParamName = "T";

	static final int IndexParameterizedTypeInfo = 0;
	static final int ParameterIndex_TheTypeRef  = 0;

		// Type is engine independent
	TKType(Engine pEngine) {
		super(pEngine);
		this.BaseSpec     = new TSType();
		this.BaseSpec.TypeStatus = TypeSpec.Status.Loaded;
		
		this.BASE_TYPEREF = this.BaseSpec.getTypeRef();
		this.BASE_TYPEREF.setTheType(new TType(this, BaseSpec));
	}
	
	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return KindName;
	}
	
	/** Type for Any */
	private final TSType BaseSpec;
	
	/** TypeRef of the base Type */
	private final TypeRef BASE_TYPEREF;
	
	/** Returns the TypeSpec of the Type of The given TypeRef */
	TSType getTypeSpecOfTypeOf(TypeRef pTRef) {
		if((pTRef == null) || pTRef.equals(TKJava.TAny.getTypeRef()))
			return (TSType)this.BaseSpec;
		
		TSType TST = (TSType)this.BaseSpec.getParameteredTypeSpec(null, this.getEngine(), pTRef);
		
		if(!TST.getTypeRef().isLoaded()) {
			TST.getTypeRef().setTheType(new TType(this, TST));
			if(TST.TypeStatus == TypeSpec.Status.Unloaded) TST.TypeStatus = TypeSpec.Status.Loaded;
		}
		
		return TST;
	}
	

	// TypeSpec -------------------------------------------------------------------------
	// TypeInfo structure of Type
	// Data    { ParameterizedTypeInfo }
	// TypeRef {}
	
	static final class TSType extends TypeSpec {

		/** Constructs a new type spec. */
		private TSType() {
			super(null,
				new Serializable[] {
					new ParameterizedTypeInfo(new TypeParameterInfo(ParamName, TKJava.TAny.getTypeRef()))
				},
				null, null);
		}

		// Classification ----------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public String getKindName() {
			return KindName;
		}
		
		// Services ----------------------------------------------------------------------

		/** Returns the contain TypeRef of this array */
		public TypeRef getTheTypeRef() {
			return this.getParameterTypeRef(ParameterIndex_TheTypeRef);
		}

		/** {@inheritDoc} */ @Override
		protected int getParameterizationInfoIndex() {
			return IndexParameterizedTypeInfo;
		}

		/**{@inheritDoc}*/ @Override
		protected boolean isToShowNoName() {
			return false;
		}
		/**{@inheritDoc}*/ @Override
		protected boolean isToShowParameteredInfo() {
			return false;
		}

		// Serializable ---------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForCompilation() {}

		// Parameterization ------------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForParameterization() {}
		
		// Objectable -----------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected String getToString() {
			return this.getToString(null);
		}
		/**{@inheritDoc}*/ @Override
		protected String getToString(TRParametered pParameteredTypeRef) {
			// Get the contain type
			TypeRef TR = (pParameteredTypeRef == null)
			                 ? this.getTheTypeRef()
			                 : pParameteredTypeRef.getParameterTypeRef(ParameterIndex_TheTypeRef);
			// Default one
			if(TR == null) TR = TKJava.TAny.getTypeRef();

			return "Type:<" + TR + ">";
		}
	}
	
	// Type  -----------------------------------------------------------------------------

	/** The type of type */
	static final public class TType extends Type {
		protected TType(TypeKind pKind, TSType pTypeInfo) {
			super(pKind, pTypeInfo);
		}
		
		/** Returns the TypeRef of the type that this type is goven */
		public TypeRef getTypeTypeRef() {
			return ((TSType)this.getTypeSpec()).getTheTypeRef();
		}
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		return false;
	}
	
	// For unnamed TType -----------------------------------------------------
	
	/** Returns a TypeRef of a Type */
	public TypeRef newTypeRef(Type pTheType) {
		if(pTheType == null)             return null;
		if(pTheType.equals(TKJava.TAny)) return this.BASE_TYPEREF;
		return new TLParametered.TRParametered(this.Engine, this.BASE_TYPEREF, pTheType.getTypeRef());
	}
	/** Returns a TypeRef of a TypeRef */
	public TypeRef newTypeRef(TypeRef pTheTypeRef) {
		if(pTheTypeRef == null)                          return null;
		if(pTheTypeRef.equals(TKJava.TAny.getTypeRef())) return this.BASE_TYPEREF;
		return new TLParametered.TRParametered(this.Engine, this.BASE_TYPEREF, pTheTypeRef);
	}

	/** Returns a TypeSpec Creator of a TypeRef */
	public TypeSpecCreator getTypeSpecCreator(final TypeRef pTheTypeRef) {
		return new TypeSpecCreator() {
			public TypeSpec newTypeSpec(Engine pEngine, TypeRef pTRef, boolean pIsVerify, Documentation pDocument) {
				TypeRef TRef = newTypeRef(pTheTypeRef);
				return TRef.getTypeSpec(pEngine);
			}
		}; 
	}
	
	// To Satisfy TypeFactory ---------------------------------------------------------------------
	
	// Typing --------------------------------------------------------------------------------------

	/**
	 * Returns type associated with Spec or create one if not exist. In case of error, return an
	 *    the exception.
	 **/ @Override
	 protected Type getType(Engine pEngine, Context pContext, TypeSpec pTypeSpec) {
		// Precondition
		if(!(pTypeSpec instanceof TSType)) {
			String SpecStr = (pTypeSpec == null)?"null":pTypeSpec.getKindName();
			throw new CurryError("Internal Error: Wrong type kind ("+ SpecStr +" in " + KindName + " ).(TKType.java#205)", pContext);
		}
		
		// The type is ready to use, just return it out
		if((pTypeSpec.getTypeRef() != null) && (pTypeSpec.getTypeRef().getTheType() != null)) 
			return pTypeSpec.getTypeRef().getTheType();
		
		TSType TST = (TSType)pTypeSpec;
		
		// Try to find by Type
		TypeRef TheTRef = TST.getTheTypeRef();
		if((TheTRef == null) || TheTRef.equals(TKJava.TAny.getTypeRef())) {
			Type T = this.BASE_TYPEREF.getTheType();
			if(T == null) T = new TType(this, TST);
			return T;
		}
		
		// Ensure that the type is Resolved
		if(!TheTRef.isLoaded()) {
			Engine E = this.getEngine();
			if(((E = pEngine) == null) && (pContext != null)) E = pContext.getEngine();
			E.getTypeManager().ensureTypeExist(pContext, TheTRef);
		}
		
		return new TType(this, TST);
	}

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TType.class;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getDefaultType(Context pContext) {
		return this.BASE_TYPEREF.getTheType();
	}
	
	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pTS) {
		return Type.class;
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec, Object pByObject) {
		if(pByObject == null)                                      return true;
		if((pTheTypeSpec == null) || !(pByObject instanceof Type)) return false;

		// Ensure this type is properly loaded
		TypeRef ThisRef = ((TSType)pTheTypeSpec).getTheTypeRef();
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, ThisRef, ((Type)pByObject).getTypeRef());
	}
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec,
			TypeSpec BySpec) {
		// Ensure this type is properly loaded
		TypeRef TheRef = ((TSType)TheSpec).getTheTypeRef();
		TypeRef ByRef  = ((TSType)BySpec) .getTheTypeRef();
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheRef, ByRef);
	}
	
	// Revert type checking ------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanTypeBeAssignedByTypeWith_Revert(Context pContext, Engine pEngine, TypeSpec TheSpec,
				TypeSpec BySpec) {
		if(!(BySpec instanceof TSType)) return false;
		// If the type of TheSpec can be assigned by TPackage then it can be assigned by this object
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec.getTypeRef(), TKJava.TType.getTypeRef());
	}
	
	// Get Type -------------------------------------------------------------------------
	
	private TypeKind[] TypeKinds  = null;
	
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOf(Context pContext, Object pObj) {
		if(!(pObj instanceof Type)) return null;
		Type T = (Type)pObj;
		
		TypeSpec TS = this.BaseSpec.findParametedTypeSpecInCache(T.getTypeRef());
		TypeRef  TR = (TS == null)?null:TS.getTypeRef();
		if(TR == null) TR = new TLParametered.TRParametered(this.Engine, this.BASE_TYPEREF, T.getTypeRef());
		if(!TR.isLoaded()) {
			Engine E = this.getEngine();
			if(E == null) {
				if(pContext != null) E = pContext.getEngine();
				if(E == null)        E = T.getEngine();
			}
			E.getTypeManager().ensureTypeExist(pContext, TR);
		}
		return TR.getTheType();
	}
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		// Ensure the TypeKinds are cached
		if(this.TypeKinds == null) {
			// Loop all the type kind and see what call the type getTypeClass
			Engine E = this.getEngine();
			if(E == null) return null;
			
			MType TManager = E.getTypeManager();
			
			String[] TKindNames = TManager.getTypeKindNames();
			this.TypeKinds  = new TypeKind[TKindNames.length];
			for(int i = 0; i < TKindNames.length; i++)
				this.TypeKinds[i] = TManager.getTypeKind(TKindNames[i]);
		}
		
		// Get the default type of that type kind
		for(int i = 0; i < this.TypeKinds.length; i++) {
			TypeKind TK = this.TypeKinds[i];
			if(TK == null) continue;
			Class<?> Cls = TK.getTypeClass(pContext);
			if(Cls == null) continue;
			if(Cls.isAssignableFrom(pCls))
				return this.getTypeOf(pContext, TK.getDefaultType(pContext));
		}
		return null;
	}
	
	// Instantiation ---------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isTypeAbstract(Context pContext, Type pThisType) {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	protected Object getTypeDefaultValue(Context pContext, Type pThisType) {
		if(!(pThisType instanceof TType)) return null;
		TypeRef TRef = ((TType)pThisType).getTypeTypeRef();
		this.getEngine().getTypeManager().ensureTypeInitialized(TRef);
		return TRef.getTheType();
	}
	
	/** Returns the initializers for initializing newly created instance. */
	@Override protected ConstructorInfo[] getConstructorInfos(Context pContext, Engine pEngine, Type pTheType) {
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isNeedInitialization() {
		return false;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Object createNewTypeInstance(Context pContext, Executable pInitiator, Type pThisType, Object pSearchKey,
			Object[] pParams) {
		return null;
	}

	// Elements --------------------------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindFields(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<AttributeInfo> AIs) {
		
		if(pIsStatic || !(pTheType instanceof TType)) return;
		
		// Get the Type of Type
		TType   TT   = ((TType)pTheType);
		TypeRef TRef = TT.getTypeTypeRef();
		Engine  E    = this.getEngine();
		if(E == null) E = (pContext == null)?null:pContext.getEngine();
		if(E == null) E = pEngine;
		E.getTypeManager().ensureTypeInitialized(TRef);
		
		// Get the Type
		Type T = TRef.getTheType();
		
		// Add static elements of the type as object's elements of this type
		AttributeInfo[] Type_AIs = T.getAllNonDynamicAttributeInfo(null);
		if((Type_AIs == null) || (Type_AIs.length == 0)) return;
		
		for(int i = 0; i < Type_AIs.length; i++) {
			// Add AIs as Delegate Object
			AttributeInfo AI = Type_AIs[i];
			if(AI == null) continue;
			
			// Repeat with the one higher priority, ignore this one
			if(getAIsByName(AIs, AI.getName()) != null)
				continue;
			
			AIs.add(
				this.doType_newAIDlgObject(
					pTheType,
					AI.getReadAccessibility(),
					AI.getWriteAccessibility(),
					AI.getConfigAccessibility(),
					AI.getName(),
					AI.isNotNull(),
					T,
					AI.getMoreData()
				)
			);
		}
		return;
	}

	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {

		if(pIsStatic || !(pTheType instanceof TType)) return;
		
		// Get the Type of Type
		TType   TT   = ((TType)pTheType);
		TypeRef TRef = TT.getTypeTypeRef();
		Engine  E    = this.getEngine();	
		if(E == null) E = (pContext == null)?null:pContext.getEngine();
		if(E == null) E = pEngine;
		E.getTypeManager().ensureTypeInitialized(TRef);
		
		// Get the Type
		Type T = TRef.getTheType();
		
		OperationInfo[] Type_OIs = T.getAllNonDynamicOperationInfo(null);
		if((Type_OIs == null) || (Type_OIs.length == 0)) return;
		
		for(int i = 0; i < Type_OIs.length; i++) {
			// Add OIs as Delegate Object
			OperationInfo OI = Type_OIs[i];
			if(OI == null) continue;

			this.addTypeOperationToOperationList(pContext, pEngine, OIs,
				this.doType_newOIDlgObject(
					pTheType,
					OI.getAccessibility(),
					OI.getSignature(),
					T,
					OI.getMoreData()
				)
			);
		}
		return;
	}
}
