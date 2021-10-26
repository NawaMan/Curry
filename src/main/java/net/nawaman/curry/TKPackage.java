package net.nawaman.curry;

import java.io.Serializable;
import java.util.Vector;

import net.nawaman.curry.compiler.TypeSpecCreator;

//TypeInfo structure of Type
//Data    { Name of the package }
//TypeRef {}

/**
 * Type of a package
 * 
 * This type has a package as its only instance. It allows an access to the referred package elements as this type
 *     non-static elements. This is very useful in the the compilation mechanism as the compiler can treat the type of
 *     type as it is just another type.
 **/
public class TKPackage extends TypeKind {
	
	// Constants ------------------------------------------------------------------------
	
	static final public String KindName = "Package";
	
	static final int IndexThePackageName = 0;
	static final int IndexExtraData      = 1;

	// Type is engine independent
	TKPackage(Engine pEngine) {
		super(pEngine);
	}
	
	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return KindName;
	}
	
	/** Create a no-name type reference of a package type */
	static public TypeRef newTypeTypeRef(String pPackageName) {
		return new TLNoName.TRNoName(new TSPackage(pPackageName));
	}
	/** Create a no-name type reference of a package type */
	static public TypeRef newTypeTypeRef(Package pPackage) {
		return new TLNoName.TRNoName(new TSPackage(pPackage.getName()));
	}
	/** Create a no-name type reference of a package type */
	static public TypeRef newTypeTypeRef(PackageBuilder pPackageBuilder) {
		return new TLNoName.TRNoName(new TSPackage(pPackageBuilder.getName()));
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		return false;
	}
	
	// For unnamed TPackage -----------------------------------------------------
	
	private Vector<TPackage> PackageTypes = new Vector<TPackage>();
	
	protected TypeSpec getTypeSpec(String pThePackageName, boolean pIsVerify) {
		if(pThePackageName == null) return null;
		
		// If unnamed and already exist, return the old one (sharing for better memory usage)
		if(this.PackageTypes != null) {
			for(int i = 0; i < this.PackageTypes.size(); i++) {
				TPackage  TP  = this.PackageTypes.get(i);
				TSPackage TSP = (TSPackage)TP.getTypeSpec();
				if(TSP.getThePackageName().equals(pThePackageName)) return TP.getTypeSpec();
			}
		}
		return new TSPackage(pThePackageName);
	}
	public TypeSpec getTypeSpec(String pThePackageName) {
		return this.getTypeSpec(pThePackageName, true);
	}

	public TPackage newPackageType(String pThePackageName) {
		if(pThePackageName == null) return null;
		
		if(this.PackageTypes != null) {
			// Try to find it first
			for(int i = 0; i < this.PackageTypes.size(); i++) {
				TPackage TP = this.PackageTypes.get(i);
				// Check type ref
				String PName = TP.getThePackageName();
				if((PName == pThePackageName) || PName.equals(pThePackageName)) return TP;
			}
		} else this.PackageTypes =  new Vector<TPackage>();
		
		// Cannot find so create a new one and saved it 
		TypeSpec TS = this.getTypeSpec(pThePackageName);
		TPackage TP = new TPackage(this, (TSPackage)TS);
		TS.getTypeRef().setTheType(TP);
		TP.TSpec.TypeStatus = TypeSpec.Status.Loaded;
		
		this.PackageTypes.add(TP);
		
		return TP;
	}
	
	public TypeSpecCreator getTypeSpecCreator(final String pThePackageName) {
		return new TypeSpecCreator() {
			public TypeSpec newTypeSpec(Engine pEngine, TypeRef pTRef, boolean pIsVerify, Documentation pDocument) {
				return getTypeSpec(pThePackageName);
			}
		}; 
	}
	
	// To Satisfy TypeFactory ---------------------------------------------------------------------
	
	// Typing --------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected Type getType(Engine pEngine, Context pContext, TypeSpec pTypeSpec) {
		// Precondition
		if(!(pTypeSpec instanceof TSPackage)) {
			String SpecStr = (pTypeSpec == null)?"null":pTypeSpec.getKindName();
			throw new CurryError("Internal Error: Wrong type kind ("+ SpecStr +" in " + KindName + " ).(TKPackage.java#188)", pContext);
		}
		
		// The type is ready to use, just return it out
		if((pTypeSpec.getTypeRef() != null) && (pTypeSpec.getTypeRef().getTheType() != null)) 
			return pTypeSpec.getTypeRef().getTheType();
		
		TSPackage TSP = (TSPackage)pTypeSpec;
		
		// Try to find by Type
		String PName = TSP.getThePackageName();
		
		// Find it from the cached - only for NoName
		if(TSP.getTypeRef() == null)   TSP.Ref = new TLNoName.TRNoName(TSP);
		if(TSP.getTypeRef() instanceof TLNoName.TRNoName) {
			if(this.PackageTypes != null) {
				// Try to find it first
				for(TPackage TP : this.PackageTypes) {
					if(!PName.equals(TP.getThePackageName())) continue;
					// Found it so return
					return TP;
				}
				// Cannot find so create a new one and saved it
				TPackage TP = new TPackage(this, TSP);
				this.PackageTypes.add(TP);
				return TP;
				
			} else this.PackageTypes =  new Vector<TPackage>();
			
			// Cannot find so create a new one and saved it
			TPackage TP = new TPackage(this, TSP);
			this.PackageTypes.add(TP);
			return TP;
		}
		// Create a new one
		return new TPackage(this, TSP);
	}

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TPackage.class;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getDefaultType(Context pContext) {
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pTS) {
		return Package.class;
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec, Object pByObject) {
		if(pByObject == null)                                         return  true;
		if((pTheTypeSpec == null) || !(pByObject instanceof Package)) return false;
		return pEngine.getUnitManager().getPackage(((TSPackage)pTheTypeSpec).getThePackageName()) == pByObject;
	}
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec,
			TypeSpec BySpec) {
		if(!(BySpec instanceof TSPackage)) return false;
		// Check the package name
		return ((TSPackage)TheSpec).getThePackageName().equals(((TSPackage)BySpec).getThePackageName());
	}
	
	// Revert type checking ------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanTypeBeAssignedByTypeWith_Revert(Context pContext, Engine pEngine,
			TypeSpec TheSpec, TypeSpec BySpec) {
		if(!(BySpec instanceof TSPackage)) return false;
		// If the type of TheSpec can be assigned by TPackage then it can be assigned by this object
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec.getTypeRef(), TKJava.TPackage.getTypeRef());
	}
	
	// Get Type -------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOf(Context pContext, Object pObj) {
		if(!(pObj instanceof Package)) return null;
		return this.newPackageType(((Package)pObj).getName());
	}
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		return null;
	}
	
	// Instantiation ---------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isTypeAbstract(Context pContext, Type pThisType) {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	protected Object getTypeDefaultValue(Context pContext, Type pThisType) {
		return null;
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
		
		if(pIsStatic || !(pTheType instanceof TPackage)) return;
		
		// Default package will have varied elements so it cannot be determined
		// TODO - Find the way to still get elements information from default elements
		if(((TPackage)pTheType).isDefaultPackage())
			return;
		
		// Get the Package
		Package P = ((TPackage)pTheType).getThePackage();
		
		AttributeInfo[] Type_AIs = P.getAllNonDynamicAttributeInfo(null);
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
					P,
					AI.getMoreData()
				)
			);
		}
		return;
	}

	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {

		if(pIsStatic || !(pTheType instanceof TPackage)) return;
		
		// Default package will have varied elements so it cannot be determined
		// TODO - Find the way to still get elements information from default elements
		if(((TPackage)pTheType).isDefaultPackage())
			return;
		
		// Get the Package
		Package P = ((TPackage)pTheType).getThePackage();
		
		OperationInfo[] Package_OIs = P.getAllNonDynamicOperationInfo(null);
		if((Package_OIs == null) || (Package_OIs.length == 0)) return;
		
		for(int i = 0; i < Package_OIs.length; i++) {
			// Add OIs as Delegate Object
			OperationInfo OI = Package_OIs[i];
			if(OI == null) continue;
			
			this.addTypeOperationToOperationList(pContext, pEngine, OIs,
				this.doType_newOIDlgObject(
					pTheType,
					OI.getAccessibility(),
					OI.getSignature(),
					P,
					OI.getMoreData()
				)
			);
		}
		return;
	}
		
	// -----------------------------------------------------------------------------------------------------------------
	// Other Classes ---------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	// TypeSpec -------------------------------------------------------------------------
	// TypeInfo structure of Type
	// Data    { Name of the package }
	// TypeRef {}
	
	static final class TSPackage extends TypeSpec {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		/** Constructs a new type spec. */
		TSPackage(String pPackageName) {
			super(
				null,
				new Serializable[] { pPackageName },
				new TypeRef[] {},
				null
			);
			if(pPackageName == null) throw new NullPointerException();
		}

		// Classification ----------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public String getKindName() {
			return KindName;
		}
		
		// Services ----------------------------------------------------------------------
		
		/** Returns the name of the package this type is referring to */
		public String getThePackageName() {
			return (String)this.getData(IndexThePackageName);
		}
		
		/** Returns the index of the extra data (in the data array) */
		public int getExtraDataIndex() {
			return IndexExtraData;
		}
		
		/**{@inheritDoc}*/ @Override
		protected boolean isToShowNoName() {
			return false;
		}
		
		// For compilation only ----------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForCompilation() {}

		// Parameterization --------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForParameterization() {}

		// Representation ----------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected String getToString() {
			return "Type:<Package::" + this.getThePackageName() + ">";
		}
	}
	
	// Type  -----------------------------------------------------------------------------

	/** The type of package */
	static final public class TPackage extends Type {
		protected TPackage(TypeKind pKind, TSPackage pTypeInfo) {
			super(pKind, pTypeInfo);
			this.ThePackage = pKind.getEngine().getUnitManager().getPackage(pTypeInfo.getThePackageName());
			
			if(this.ThePackage == null)
				throw new NullPointerException();
			
			this.IsDefault = this.ThePackage == pKind.getEngine().getDefaultPackage();
		}
		
		boolean IsDefault;
		Package ThePackage;
		
		/** Checks if the package held by this type is the default package */
		public boolean isDefaultPackage() {
			return this.IsDefault;
		}
		
		/** Returns the name of the package this type is referring to */
		public String getThePackageName() {
			return ((TSPackage)this.getTypeSpec()).getKindName();
		}
		/** Returns the package this type is referring to */
		public Package getThePackage() {
			return this.ThePackage;
		}
	}
}
