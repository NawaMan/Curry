package net.nawaman.curry;

import java.io.Serializable;
import java.util.HashMap;

import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.TypeParameterInfo.TypeParameterInfos;
import net.nawaman.curry.compiler.TypeSpecCreator;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.MoreData;

/** TypeKind of DataHolder Type */
final public class TKDataHolder extends TypeKind {
	
	// Constants ------------------------------------------------------------------------
	
	static final public String KindName  = "DataHolder";
	static final public String ParamName = "T";

	static final int IndexParameterizedTypeInfo = 0;
	static final int ParameterIndex_TheTypeRef  = 0;
	
	// Constructor ---------------------------------------------------------------------------------
	
	TKDataHolder(Engine pEngine) {
		super(pEngine);
	}

	/** Type for Any */
	private HashMap<Integer, TSDataHolder> BaseTypeSpecs = null;
	
	/** Returns the hash of the basic non-type value of the TSDataHolder properties */
	final int getHash(Boolean pIsReadable, Boolean pIsWritable) {
		int H = 0;
		if(      Boolean.TRUE.equals(pIsReadable)) H += 1;
		else if(!Boolean.TRUE.equals(pIsReadable)) H += 2;
		if(      Boolean.TRUE.equals(pIsWritable)) H += 10;
		else if(!Boolean.TRUE.equals(pIsWritable)) H += 20;
		return H;
	}
	
	/** Returns a base TypeSpec for DataHoler with the given property */
	final TSDataHolder getTSDataHolder(Boolean pIsReadable, Boolean pIsWritable) {
		if(this.BaseTypeSpecs == null) this.BaseTypeSpecs = new HashMap<Integer, TSDataHolder>(5);
		
		int H = this.getHash(pIsReadable, pIsWritable);
		if(H == 22) { //if(IsNotReadable && IsNotWritable) 
			throw new RuntimeException("Mal-form TypeSpec: A dataholder's cannot be both unreadable and unwritable. " +
					"(DataHolder type) <TKDataHolder:249>.");
		}
		
		TSDataHolder    TSD = this.BaseTypeSpecs.get(H);
		if(TSD != null) return TSD;
			
		TSD = new TSDataHolder(pIsReadable, pIsWritable);
		
		this.BaseTypeSpecs.put(H, TSD);
		return TSD;
	}
	
	/** Returns a base TypeSpec for DataHoler with the given property */
	final TSDataHolder getTSDataHolder(TypeRef pDataTypeRef, Boolean pIsReadable, Boolean pIsWritable) {
		TSDataHolder TSD = this.getTSDataHolder(pIsReadable, pIsWritable);
		if((pDataTypeRef == null) || pDataTypeRef.equals(TKJava.TAny.getTypeRef())) return TSD;
		
		return (TSDataHolder)TSD.getParameteredTypeSpec(null, this.getEngine(), pDataTypeRef);
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		return false;
	}

	// Services ---------------------------------------------------------------
	
	/** Checks if the TypeRef is a DataHolder Type */
	static public boolean isDataHolder(Engine pEngine, TypeRef pTRef) {
		if((pEngine == null) || (pTRef == null))          return false;
		if(pTRef.equals(TKJava.TDataHolder.getTypeRef())) return  true;
		
		try { pEngine.getTypeManager().ensureTypeExist(null, pTRef); }
		catch(Exception E) {}
		
		Type T = pTRef.getTheType();
		if(T == null) return false;
		
		return (T instanceof TDataHolder);
	}
	
	/** Checks if the TypeRef is a DataHolder Type */
	static public TypeRef getDataHolderDataTypeRef(Engine pEngine, TypeRef pTRef) {
		if((pEngine == null) || (pTRef == null))          return null;
		if(pTRef.equals(TKJava.TDataHolder.getTypeRef())) return TKJava.TAny.getTypeRef();
		
		try { pEngine.getTypeManager().ensureTypeExist(null, pTRef); }
		catch(Exception E) {}
		
		Type T = pTRef.getTheType();
		if(!(T instanceof TDataHolder))
			return null;
		
		return ((TDataHolder)T).getDataTypeRef();
	}

	/** Creates a new type ref for a no name type from the given executable information */
	public TypeRef getNoNameTypeRef(TypeRef pDataTypeRef, Boolean pIsReadable, Boolean pIsWritable, StringBuffer pSB) {
		int L = (pSB == null)?0:pSB.length();
		TypeSpec TS = this.getTypeSpec(pDataTypeRef, pIsReadable, pIsWritable, true, pSB);
		if((TS == null) || ((pSB != null) && (L != pSB.length()))) return null;
		return TS.getTypeRef();
	}
	
	/** Creates a new type spec from the given executable information */
	protected TypeSpec getTypeSpec(TypeRef pDataTypeRef, Boolean pIsReadable, Boolean pIsWritable, boolean pIsVerify,
			StringBuffer pSB) {

		if(pIsVerify && (Boolean.FALSE.equals(pIsReadable)) && (Boolean.FALSE.equals(pIsWritable))) { 
			pSB.append("Mal-form TypeSpec: A dataholder's cannot be both unreadable and unwritable. (DataHolder type)" +
					" <TKDataHolder:249>.");
			return null;
		}
		return this.getTSDataHolder(pDataTypeRef, pIsReadable, pIsWritable);
	}

	/** Returns a TypeSpec creator for costructing a DataHolder Type */
	public TypeSpecCreator getTypeSpecCreator(final TypeRef pDataTypeRef,
			final Boolean pIsReadable, final Boolean pIsWritable, final MoreData pExtraData) {
		return new TypeSpecCreator() {
			public TypeSpec newTypeSpec(Engine pEngine, TypeRef pTRef, boolean pIsVerify, Documentation pDocument) {
				StringBuffer SB = new StringBuffer();
				TypeSpec TS = getTypeSpec(pDataTypeRef, pIsReadable, pIsWritable, true, SB);
				if(SB.length() != 0)
					throw new IllegalArgumentException("Unable to create type specification for an executable type '"+
							pTRef+"': " + SB);
				return TS;
			}
		}; 
	}
	
	// Internal Services -------------------------------------------------------
	
	String ensureTypeSpecFormat(Context pContext, TypeSpec pTypeSpec) {
		if(pTypeSpec == null)                    return "Null TypeSpec <TKDataHolder:131>.";
		if(!(pTypeSpec instanceof TSDataHolder)) return "TypeSpec is mal-form (TSDataHolder is required) <TKDataHolder:132>.";
		TSDataHolder TSD = (TSDataHolder)pTypeSpec; 

		boolean IsNotReadable = Boolean.FALSE.equals(TSD.isReadable());
		boolean IsNotWritable = Boolean.FALSE.equals(TSD.isWritable()); 
		if(IsNotReadable && IsNotWritable) 
			return "Mal-form TypeSpec: A dataholder's cannot be both unreadable and unwritable. (DataHolder type " +
					TSD.toString() + ") <TKDataHolder:249>.";
		
		if((TSD.getDataTypeRef() == null) || TSD.getDataTypeRef().equals(TKJava.TVoid)) 
			return "Mal-form TypeSpec: A dataholder's data type must not be null or void. (DataHolder type " +
					TSD.toString() + ") <TKDataHolder:146>.";
		
		return null;
	}
	
	// ----------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return KindName;
	}
	
	// It is very important to remember that Required Types in pTypeInfo may not be resolved
	//     and initialized. Therefore, only use them as TypeRefs
	/**{@inheritDoc}*/ @Override
	protected Type getType(Engine pEngine, Context pContext, TypeSpec pTypeSpec) {
		String Err = this.ensureTypeSpecFormat(pContext, pTypeSpec);
		if(Err != null) {
			throw ExternalContext.newCurryError(pContext,
					"Type Creation Error: " +
					"The following error occur while trying to create a type " +
					pTypeSpec.getTypeRef().toString() + ": " + Err + ".(TKDataHolder.java#77)",
					null);
		}
		return new TDataHolder(this, (TSDataHolder)pTypeSpec);
	}

	// Get Type -------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOf(Context pContext, Object pObj) {
		if(!(pObj instanceof DataHolder)) return null;
		DataHolder DH = (DataHolder)pObj;
		Type     T  = this.getEngine().getDataHolderManager().getDHType(   pContext, null, DH);
		Boolean  IR = this.getEngine().getDataHolderManager().isDHReadable(pContext, null, DH);
		Boolean  IW = this.getEngine().getDataHolderManager().isDHWritable(pContext, null, DH);
		TypeSpec TS = this.getTSDataHolder((T == null)?null:T.getTypeRef(), IR, IW);

		if(TS.getTypeRef().isLoaded()) return TS.getTypeRef().getTheType();
		
		MType MT = this.getEngine().getTypeManager();
		MT.ensureTypeInitialized(pContext, TS.getTypeRef());
		return TS.getTypeRef().getTheType();
	}
	
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		return null;
	}
	
	// Typing --------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TDataHolder.class;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getDefaultType(Context pContext) {
		return null;
	}

	// Information and functionality -------------------------------------------
	
	// Return the class of the data object, this is used in Array and Collection
	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pTS) {
		return DataHolder.class;
	}
	
	// Check if the data object is a valid data of this type.
	// This method will be called only after the data class is checked
	//     so there is no need to check for the data class again.
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec, Object pByObject) {
		if(!(pByObject    instanceof   DataHolder)) return false;
		if(!(pTheTypeSpec instanceof TSDataHolder)) return false;
		TSDataHolder TSD = (TSDataHolder)pTheTypeSpec;
		DataHolder   DH  = (DataHolder)  pByObject;
		
		if(!MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TSD.getDataTypeRef(), DH.getType().getTypeRef()))
			return false;
		
		MDataHolder $MD = this.getEngine().getDataHolderManager();
		boolean TSDIsReadable = TSD.isReadable();
		boolean TSDIsWritable = TSD.isWritable();
		boolean DHIsReadable  = $MD.isDHReadable(pContext, null, DH);
		boolean DHIsWritable  = $MD.isDHWritable(pContext, null, DH);
		if(Boolean.TRUE .equals(TSDIsReadable) && !DHIsReadable) return false;
		if(Boolean.TRUE .equals( DHIsWritable) && !DHIsWritable) return false;
		if(Boolean.FALSE.equals(TSDIsWritable) &&  DHIsReadable) return false;
		if(Boolean.FALSE.equals( DHIsWritable) &&  DHIsWritable) return false;
		return true;
	}
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec, TypeSpec BySpec) {
		
		if(!(TheSpec instanceof TSDataHolder)) return false;
		if(!(BySpec  instanceof TSDataHolder)) return false;
		TSDataHolder TheD = (TSDataHolder)TheSpec;
		TSDataHolder ByD  = (TSDataHolder)BySpec;
		if(TheD == ByD) return true;
		if(TheD.getDataTypeRef().canBeAssignedByInstanceOf(this.getEngine(), ByD.getDataTypeRef())) return false;
		
		if((TheD.isReadable() == null) || (TheD.isWritable() == null)) return true;

		boolean TheDIsReadable = TheD.isReadable();
		boolean TheDIsWritable = TheD.isWritable();
		boolean ByDIsReadable  = ByD.isReadable();
		boolean ByDIsWritable  = ByD.isWritable();
		if(Boolean.TRUE .equals(TheDIsReadable) && !Boolean.TRUE .equals(ByDIsReadable)) return false;
		if(Boolean.TRUE .equals(TheDIsWritable) && !Boolean.TRUE .equals(ByDIsWritable)) return false;
		if(Boolean.FALSE.equals(TheDIsReadable) && !Boolean.FALSE.equals(ByDIsReadable)) return false;
		if(Boolean.FALSE.equals(TheDIsWritable) && !Boolean.FALSE.equals(ByDIsWritable)) return false;
		return true;
	}
	
	// Revert type checking ------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanTypeBeAssignedByTypeWith_Revert(Context pContext, Engine pEngine,
			TypeSpec TheSpec, TypeSpec BySpec) {
		if(!(BySpec instanceof TSDataHolder)) return false;
		// If the type of TheSpec can be assigned by TDataHolder then it can be assigned by this object
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec.getTypeRef(), TKJava.TDataHolder.getTypeRef());
	}
	
	// Initialization ------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected boolean isNeedInitialization() {
		return false;
	}

	/**{@inheritDoc}*/ @Override
	protected boolean isTypeAbstract(Context pContext, Type pTheType) {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	protected Object getTypeDefaultValue(Context pContext, Type pTheType) {
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Object createNewTypeInstance(Context pContext, Executable pInitiator, Type pTheType, Object pSearchKey,
			Object[] pParams) {
		// Returns null before this is an abstract
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected ConstructorInfo[] getConstructorInfos(Context pContext, Engine pEngine, Type pTheType) {
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Exception doValidateTypeSpec(Context pContext, TypeSpec pSpec) {
		if(pSpec ==  null)                   return new NullPointerException();
		if(!(pSpec instanceof TSDataHolder)) return new IllegalArgumentException("Wrong type.(TKDataHolder.java#221)");
		
		TSDataHolder TS = ((TSDataHolder)pSpec);
		String Err = this.ensureTypeSpecFormat(pContext, TS);
		if(Err != null) return ExternalContext.newCurryError(pContext, Err, null);
		return null;
	}
		
	// -----------------------------------------------------------------------------------------------------------------
	// Other Classes ---------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------
		
	/** TypeSpec of DataHolder Type */
	static public class TSDataHolder extends TypeSpec {
		
		// Constants ------------------------------------------------------------------------
		
		@SuppressWarnings("hiding")
		static final public String KindName = TKDataHolder.KindName;
		
		final static public int Index_ParameterizedTypeInfo = 0;
		final static public int Index_IsReadable            = 1;
		final static public int Index_IsWritable            = 2;
		final static public int Index_ExtraData             = 3;

		@SuppressWarnings("hiding")
		static final int ParameterIndex_TheTypeRef  = 0;
		
		// Service ---------------------------------------------------------------------------

		/** Constructs a TSDataHolder */
		private TSDataHolder(Boolean pIsReadable, Boolean pIsWritable) {
			super(null,
				new Serializable[] {
					new ParameterizedTypeInfo(new TypeParameterInfo(ParamName, TKJava.TAny.getTypeRef())),
					pIsReadable, pIsWritable
				},
				null, null
			);
		}
		
		// Classification --------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public String getKindName() {
			return KindName;
		}
		
		// Services --------------------------------------------------------------------------

		/** {@inheritDoc} */ @Override
		protected int getParameterizationInfoIndex() {
			return IndexParameterizedTypeInfo;
		}
		
		/** Checks if the object is a boolean */
		private Boolean getValidBoolean(Object O) {
			return (O instanceof Boolean)?(Boolean)O:null;
		}
		
		/** Returns the TypeRef of the DataHolder Type */
		public TypeRef getDataTypeRef() {
			TypeParameterInfos TPI = (TypeParameterInfos)this.getData(ParameterIndex_TheTypeRef);
			if(TPI instanceof ParameteredTypeInfo)
				return ((ParameteredTypeInfo)TPI).getParameterTypeRef(0);
			
			TypeRef TRef = this.getTypeRef();
			// Get the type from the parameter of the TypeRef
			if(TRef instanceof TRParametered)
				return ((TRParametered)TRef).getParameterTypeRef(0);

			TRef = this.getParameterTypeRef(ParameterIndex_TheTypeRef);
			return (TRef == null) ? TKJava.TAny.getTypeRef() : TRef;
		}

		/** Checks if this data holder is readable */
		public Boolean isReadable() {
			return this.getValidBoolean(this.getData(Index_IsReadable));
		}

		/** Checks if this data holder is writable */
		public Boolean isWritable() {
			return this.getValidBoolean(this.getData(Index_IsWritable));
		}
		
		/**{@inheritDoc}*/ @Override
		protected int getMoreDataIndex() {
			return -1;
		}
		/**{@inheritDoc}*/ @Override
		protected int getExtraInfoIndex() {
			return (this.getDataCount() == (Index_ExtraData - 1))?Index_ExtraData:-1;
		}
		
		// Parameterization --------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected TypeSpec newTypeSpecOfParameteredTypeSpec(Context pContext, Engine pEngine, TypeRef ... pPTypeRefs) {
			if((pPTypeRefs == null) || (pPTypeRefs.length != 1)) {
				throw new IllegalArgumentException(
					String.format(
						"Type Parameterization error: Invalid type parameterization '%s' <TKDataHolder:392>.",
						this.getTypeRef()
					)
				);
			}
			
			TypeRef TRef = this.getTypeRef();
			TypeRef DRef = pPTypeRefs[0];
			
			if((DRef == null) || TKJava.TAny.getTypeRef().equals(DRef))
				return this;
			
			TypeRef DHRef = new TLParametered.TRParametered(TRef, DRef);
			return DHRef.getTypeSpec(pEngine, pContext);
		}
		
		// For compilation only --------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForCompilation() {}

		// Parameterization --------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForParameterization() {}
		
		// Objectable -----------------------------------------------------------------------
		
		private String getAccessibilityString(Boolean B) {
			if(B == null) return ":";
			if(Boolean.TRUE .equals(B)) return "=";
			if(Boolean.FALSE.equals(B)) return "!";
			return "";
		}

		/**{@inheritDoc}*/ @Override
		protected boolean isToShowNoName() {
			return false;
		}
		/**{@inheritDoc}*/ @Override
		protected boolean isToShowParameteredInfo() {
			return false;
		}
		
		/**{@inheritDoc}*/ @Override
		protected String getToString() {
			return this.getToString(null);
		}
		/**{@inheritDoc}*/ @Override
		protected String getToString(TRParametered pParameteredTypeRef) {
			TypeSpec TS = pParameteredTypeRef.getTypeSpecWithoutEngine();
			if(TS instanceof TSDataHolder) {
				TSDataHolder TSDH = (TSDataHolder)TS;
				return String.format(
					"DataHolder<%s%s%s>",
					TSDH.getAccessibilityString(TSDH.isReadable()),
					TSDH.getDataTypeRef(),
					TSDH.getAccessibilityString(TSDH.isWritable())
				);
			}
			
			// Get the contain type
			TypeRef TR = (pParameteredTypeRef == null)
			                 ? this.getDataTypeRef()
			                 : pParameteredTypeRef.getParameterTypeRef(ParameterIndex_TheTypeRef);
			// Default one
			if(TR == null) TR = TKJava.TAny.getTypeRef();

			return String.format(
					"DataHolder<%s%s%s>",
					this.getAccessibilityString(this.isReadable()),
					this.getDataTypeRef(),
					this.getAccessibilityString(this.isWritable())
				);
		}
	}
	
	/** Type of DataHolder */
	static public class TDataHolder extends Type {
		TDataHolder(TypeKind pTKind, TSDataHolder pTSpec) {
			super(pTKind, pTSpec);
		}
		
		/** Returns the Type Ref of the DataHolder Type */
		public TypeRef getDataTypeRef() {
			return ((TSDataHolder)this.getTypeSpec()).getDataTypeRef();
		}
		/** Checks if this data holder is readable */
		public Boolean isReadable() {
			return ((TSDataHolder)this.getTypeSpec()).isReadable();
		}
		/** Checks if this data holder is writable */
		public Boolean isWritable() {
			return ((TSDataHolder)this.getTypeSpec()).isWritable();
		}
	}
}
