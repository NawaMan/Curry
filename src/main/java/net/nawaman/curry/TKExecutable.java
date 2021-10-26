package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.curry.OperationInfo.SimpleOperation;
import net.nawaman.curry.TKVariant.TVariant;
import net.nawaman.curry.TLParameter.TRParameter;
import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.compiler.TypeSpecCreator;
import net.nawaman.curry.util.MoreData;

public class TKExecutable extends TypeKind {
	
	// Constants ------------------------------------------------------------------------
	
	static final public String KindName = "Executable"; 
	
	// Constructor ---------------------------------------------------------------------------------
	
	/** Constructs a Executable TypeKind */ 
	protected TKExecutable(Engine pEngine) {
		super(pEngine);
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		return false;
	}

	// Services ---------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return true;
	}

	/** Creates a new type ref for a no name type from the given executable information */
	public TypeRef getNoNameTypeRef(Executable.ExecKind pExecKind, ExecInterface pInterface, MoreData pExtraData,
			StringBuffer pSB) {
		int L = (pSB == null)?0:pSB.length();
		TypeSpec TS = this.getTypeSpec(null, pExecKind, pInterface, pExtraData, pSB);
		if((TS == null) || ((pSB != null) && (L != pSB.length()))) return null;
		return TS.getTypeRef();
	}
	
	/** Creates a new type spec from the given executable information */
	public TypeSpec getTypeSpec(TypeRef pTypeRef, Executable.ExecKind pExecKind,
			ExecInterface pInterface, MoreData pExtraData, StringBuffer pSB) {
		return this.getTypeSpec(pTypeRef, pExecKind, pInterface, null, pExtraData, true, pSB);
	}
	
	/** Creates a new type spec from the given executable information */
	public TypeSpec getTypeSpec(TypeRef pTypeRef, Executable.ExecKind pExecKind, ExecInterface pInterface,
			ParameterizedTypeInfo pTPInfo, MoreData pExtraData, StringBuffer pSB) {
		return this.getTypeSpec(pTypeRef, pExecKind, pInterface, pTPInfo, pExtraData, true, pSB);
	}
	
	/** Creates a new type spec from the given executable information */
	protected TypeSpec getTypeSpec(TypeRef pTypeRef, Executable.ExecKind pExecKind, ExecInterface pInterface,
			ParameterizedTypeInfo pTPInfo, MoreData pExtraData, boolean pIsVerify, StringBuffer pSB) {
		
		if(pIsVerify) {
			String Error = ensureTypeSpecFormat(null, pTypeRef, pExecKind, pInterface);
			if(Error != null) { if(pSB != null) pSB.append(Error); return null; }
		}
		return new TSExecutable(pTypeRef, pExecKind, pInterface, pTPInfo, pExtraData);
	}

	public TypeSpecCreator getTypeSpecCreator(final Executable.ExecKind pExecKind, final ExecInterface pInterface,
			final ParameterizedTypeInfo pTPInfo) {
		return new TypeSpecCreator() {
			public TypeSpec newTypeSpec(Engine pEngine, TypeRef pTRef, boolean pIsVerify, Documentation pDocument) {
				StringBuffer SB = new StringBuffer();
				MoreData EI = (pDocument == null)?null:new MoreData(Documentation.MIName_Documentation, pDocument);
				TypeSpec TS = getTypeSpec(pTRef, pExecKind , pInterface, pTPInfo, EI, SB);
				if(SB.length() != 0) throw new IllegalArgumentException("Unable to create type specification for an executable type '"+pTRef+"': " + SB);
				return TS;
			}
		}; 
	}
	
	// Internal Services -------------------------------------------------------
	
	/** Checks to ensure that the TypeSpec for executable Type is in a valid form */
	String ensureTypeSpecFormat(Context pContext, TypeSpec pTypeSpec) {
		if(pTypeSpec == null)                    return "Null TypeSpec.";
		if(!(pTypeSpec instanceof TSExecutable)) return "TypeSpec is mal-form (TSExecutable is required).";
		TSExecutable TSE = (TSExecutable)pTypeSpec; 
		
		return this.ensureTypeSpecFormat(pContext, TSE.getTypeRef(), TSE.getExecKind(), TSE.getDeclaredSignature());
	}
	/** Checks to ensure that the TypeSpec for executable Type is in a valid form */
	String ensureTypeSpecFormat(Context pContext, TypeRef pTypeRef, Executable.ExecKind pExecKind,
			ExecInterface pInterface) {
		
		if((pExecKind != null) && pExecKind.isFragment() && (pInterface.getParamCount() != 0)) 
			return "Mal-form TypeSpec: A fragment should not have a parameter." +
					"(Executable type "+pTypeRef+").";
		
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
					pTypeSpec.getTypeRef().toString() + ": " + Err + ".(TKExecutable.java#77)",
					null);
		}
		return new TExecutable(this, (TSExecutable)pTypeSpec);
	}

	// Get Type -------------------------------------------------------------------------
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOf(Context pContext, Object pObj) {
		if(!(pObj instanceof Executable)) return null;
		if(pObj instanceof Expression)    return TKJava.TExpression;
		TypeSpec TS = new TSExecutable(null, ((Executable)pObj).getKind(), ((Executable)pObj).getSignature(), null, null);
		this.getEngine().getTypeManager().ensureTypeInitialized(pContext, TS.getTypeRef());
		return TS.getTypeRef().getTheType();
	}
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		if(pCls == Expression.class) return TKJava.TExpression;
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

	// Information and functionality -------------------------------------------
	
	// Return the class of the data object, this is used in Array and Collection
	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pTS) {
		return Executable.class;
	}
	
	// Check if the data object is a valid data of this type.
	// This method will be called only after the data class is checked
	//     so there is no need to check for the data class again.
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec, Object pByObject) {
		if(!(pByObject    instanceof   Executable)) return false;
		if(!(pTheTypeSpec instanceof TSExecutable)) return false;
		TSExecutable TSE = (TSExecutable)pTheTypeSpec;
		Executable   DE  = (Executable)pByObject;
		
		// Ensure ExecKind
		if((TSE.getExecKind() != null) && (TSE.getExecKind() != DE.getKind())) return false;
		
		// Ensure the interface.
		if(ExecInterface.Util.canBeAssignedBy_ByInterface(this.getEngine(), pContext, TSE.getSignature(),
				DE.getSignature()) == ExecInterface.NotMatch) return false;
		
		return true;
	}
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec, TypeSpec BySpec) {
		if(!(TheSpec instanceof TSExecutable)) return false;
		if(!(BySpec  instanceof TSExecutable)) return false;
		TSExecutable TheExec = (TSExecutable)TheSpec;
		TSExecutable ByExec  = (TSExecutable)BySpec;
		
		// Ensure ExecKind
		if((TheExec.getExecKind() != null) && (TheExec.getExecKind() != ByExec.getExecKind())) return false;
		
		// Ensure the interface.
		if(ExecInterface.Util.canBeAssignedBy_ByInterface(this.getEngine(), pContext, TheExec.getSignature(),
				ByExec.getSignature()) == ExecInterface.NotMatch) return false;
		
		return true;
	}

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanTypeBeAssignedByTypeWith_Revert(Context pContext, Engine pEngine,
			TypeSpec TheSpec, TypeSpec BySpec) {
		if(!(BySpec instanceof TSExecutable)) return false;
		switch(((TSExecutable)BySpec).getExecKind()) {
			// If the type of TheSpec can be assigned by TFragment then it can be assigned by this object
			case Fragment:   return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec.getTypeRef(), TKJava.TFragment  .getTypeRef());
			// If the type of TheSpec can be assigned by TMacro then it can be assigned by this object
			case Macro:      return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec.getTypeRef(), TKJava.TMacro     .getTypeRef());
			// If the type of TheSpec can be assigned by TSubRoutine then it can be assigned by this object
			case SubRoutine: return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec.getTypeRef(), TKJava.TSubRoutine.getTypeRef());
			
			/* 
			case Fragment:   return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TKJava.TFragment  .getTypeRef(), pTheTypeSpec.getTypeRef());
			case Macro:      return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TKJava.TMacro     .getTypeRef(), pTheTypeSpec.getTypeRef());
			case SubRoutine: return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TKJava.TSubRoutine.getTypeRef(), pTheTypeSpec.getTypeRef());
			 */
		}
		return false;
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
	protected Object createNewTypeInstance(Context pContext, Executable pInitiator,
			Type pTheType, Object pSearchKey, Object[] pParams) {
		// Returns null before this is an abstract
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected ConstructorInfo[] getConstructorInfos(Context pContext, Engine pEngine, Type pTheType) {
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Exception doValidateTypeSpec(Context pContext, TypeSpec pSpec) {
		TSExecutable TS = (TSExecutable)pSpec;
		String Err = this.ensureTypeSpecFormat(pContext, TS);
		if(Err != null) return ExternalContext.newCurryError(pContext, Err, null);
		return null;
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	// Other Classes ---------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------
	
	/** Executable TypeSpec */
	static public class TSExecutable extends TypeSpec {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		// Constants ------------------------------------------------------------------------
		
		static final public String KindName = "Executable";
		
		final static public int Index_ExecKind          = 0;
		final static public int Index_ExecSignature     = 1;
		final static public int Index_ParameterizedInfo = 2;
		final static public int Index_ExtraData         = 3;
		
		// Service ---------------------------------------------------------------------------
		
		/** Returns the TypeRef array of the given interface array */
		static public TypeRef[] getTypeRefArray(ExecInterface pInterface) {
			TypeRef[] TRefs = new TypeRef[pInterface.getParamCount()];
			for(int i = pInterface.getParamCount(); --i >= 0; ) { TRefs[i] = pInterface.getParamTypeRef(i); }
			return TRefs;
		}
		
		/** Constructs an Executable TypeSpec */
		protected TSExecutable(TypeRef pTypeRef, Executable.ExecKind pExecKind, ExecInterface pInterface,
				ParameterizedTypeInfo pTPInfo, MoreData pExtraData) {
			super(pTypeRef,
					new Serializable[] {
						pExecKind,
						ExecSignature.newSignature(
							((pExecKind == null)?"executable":pExecKind.toString()),
							pInterface, null, null
						),
						pTPInfo,
						pExtraData
					}, null,
					TSExecutable.getTypeRefArray(pInterface));
			
			if(pExtraData != null) pExtraData.toFreeze();
		}
		
		// Classification --------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public String getKindName() {
			return KindName;
		}
		
		// Parameterization -----------------------------------------------------------------
		
		transient ExecSignature Signature = null;

		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForParameterization() {
			this.Signature = null;
		}
		
		// Services --------------------------------------------------------------------------
		
		/** Returns the Required TypeKinds for this executable */
		public Executable.ExecKind getExecKind() {
			return (Executable.ExecKind)this.getData(Index_ExecKind);
		}
		/** Returns the Signature of this Executable */
		public ExecSignature getSignature() {
			if(this.Signature == null) {
				this.Signature = (ExecSignature)this.Datas[Index_ExecSignature];
				if(this.isParameterized() || this.isParametered()) {
					TypeRef TR = this.getTypeRef();
					Type    T  = TR.getTheType();
					Engine  E  = (T == null) ? null : T.getEngine();
					this.Signature = OperationInfo.SimpleOperation.ChangeBaseTypeSignature(E, TR, TR, this.Signature);
					this.Signature = OperationInfo.SimpleOperation.flatSignature(          E, TR,     this.Signature);
				}
			}
			
			return this.Signature;
		}
		/** Returns the Signature of this Executable */
		public ExecSignature getDeclaredSignature() {
			return (ExecSignature)this.Datas[Index_ExecSignature];
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
			return (this.getDataCount() == 4)?Index_ExtraData:-1;
		}

		// Resolve -----------------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resolveParameteredTypeSpec(Context pContext, Engine pEngine) {
			// Resolve the Signature here
			super.resolveParameteredTypeSpec(pContext, pEngine);
			TypeRef       TRef  = this.getTypeRef();
			ExecSignature OldES = (ExecSignature)this.Datas[Index_ExecSignature];
			ExecSignature NewES = SimpleOperation.ChangeBaseTypeSignature(pEngine, TRef, TRef, OldES);
			if(OldES != NewES) this.Signature = NewES;
			
			OldES = NewES;
			NewES = SimpleOperation.flatSignature(pEngine, TRef, OldES);
			if(OldES != NewES) this.Signature = NewES;
		}
		
		// For compilation only ----------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForCompilation() {
			Util.ResetExecSignature(this.getDeclaredSignature());
			Util.ResetExecSignature(this.getSignature());
		}
	
		// Objectable -----------------------------------------------------------------------
		
		private String getParameterizedInfo(TypeRef TR) {
			if(TR instanceof TRParameter)
				 return ((TRParameter)TR).getParameterName();
			else return (TR == null)?TKJava.TAny.getTypeRef().toString():TR.toString();
		}

		/**{@inheritDoc}*/ @Override
		protected boolean isToShowNoName() {
			return false;
		}
		
		/**{@inheritDoc}*/ @Override
		protected String getToString() {
			TypeRef TRef = this.getTypeRef();
			
			StringBuilder SB = new StringBuilder();
			if((TRef instanceof TLNoName.TRNoName) || (TRef instanceof TRParametered))
				SB.append("Executable:<");
			else return TRef.toString();
			
			ExecSignature Sign = this.getSignature();

			String Name = Sign.getName();
			if((this.getExecKind() != null) || !"executable".equals(Name))
				SB.append(Name);
			
			
			if(!(this.getTypeRef() instanceof TRParametered) && this.isParameterized()) {
				Sign = this.getDeclaredSignature();
				
				SB.append("(");
				for(int i = 0; i < Sign.getParamCount(); i++) {
					if(i != 0) SB.append(", ");
						
					SB.append(this.getParameterizedInfo(Sign.getParamTypeRef(i)));
				}
				SB.append("):");
				SB.append(this.getParameterizedInfo(Sign.getReturnTypeRef()));
			} else {
				SB.append(ExecInterface.Util.toString(this.getSignature().getInterface(), ""));
			}
			SB.append(">");
			
			if((this.getTypeRef() instanceof TRParametered) || this.isParameterized())
				SB.append(" ").append(this.getParameterizedTypeInfo());
			
			return SB.toString();
		}
	}
	
	/** Executable Type */
	static public class TExecutable extends Type {

		/** Constructs an executable Type */
		TExecutable(TypeKind pTKind, TSExecutable pTSpec) {
			super(pTKind, pTSpec);
		}

		/** Returns the Executable Kind required by this type */
		public Executable.ExecKind getExecKind() {
			return ((TSExecutable)this.getTypeSpec()).getExecKind();
		}
		
		/** Returns the signature of the executable */
		public ExecSignature getSignature() {
			return ((TSExecutable)this.getTypeSpec()).getSignature();
		}

		/**{@inheritDoc}*/ @Override
		public String toString() {
			StringBuilder SB = new StringBuilder("Executable:<");
			ExecSignature Sign = this.getSignature();

			String Name = Sign.getName();
			if((this.getExecKind() != null) || !"executable".equals(Name))
				SB.append(Name);

			SB.append(ExecInterface.Util.getParametersToString(this.getSignature(), false));
			SB.append(":").append(this.getSignature().getReturnTypeRef());
			
			SB.append(">");
			
			return SB.toString();
		}
	}
}
