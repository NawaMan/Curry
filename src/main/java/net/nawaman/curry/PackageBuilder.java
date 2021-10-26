package net.nawaman.curry;

import java.io.Serializable;
import java.util.HashSet;

import net.nawaman.compiler.Code;
import net.nawaman.compiler.CodeFeeders;
import net.nawaman.curry.EngineExtensions.EE_DefaultPackage;
import net.nawaman.curry.Package.Required;
import net.nawaman.curry.Package.Required_Func;
import net.nawaman.curry.Package.Required_PVar;
import net.nawaman.curry.Package.Required_Type;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLPackage.TRPackage;
import net.nawaman.curry.TLPackage.TRPackage_Internal;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;

/** A builder for a pacakge */
final public class PackageBuilder extends StackOwnerBuilder {
	
	/** Constructs a package builder */
	PackageBuilder(String pName) {
		super(pName);
	}
	
	UnitBuilder UB = null;

	Serializable EngineSpecSignature = null;
	
	HashSet<String>   FriendNames       = null;
	HashSet<Required> RequiredArtifacts = null;
	
	/** Returns the Engine that package builder is attaching on */ @Override
	final public Engine getEngine() {
		return this.UB.getEngine();
	}
	
	// Parent and Package ----------------------------------------------------------------------------------------------

	/** Returns the parent StackOwner builder that this stackowner builder is attaching on */ @Override
	public StackOwnerBuilderEncloseObject getStackOwnerBuilderEncloseObject() {
		return this.UB;
	}

	/** Returns the package name of that this builder is taking care of */ @Override
	final public String getPackageName() {
		return this.Name;
	}
	
	/** Returns the package builder that this stackowner builder is attaching on */ @Override
	public PackageBuilder getPackageBuilder() {
		return this;
	}
	
	/** Returns the unit builder that package builder is attaching on */
	final public UnitBuilder getUnitBuilder() {
		return this.UB;
	}
	
	/**{@inheritDoc}*/ @Override
	public TREnclosed ensureTypeRefValid(Accessibility pPAccess, TypeSpec TS, Location pLocation) {
		if((TS == null) || !(TS.getTypeRef() instanceof TRPackage)) return null;
		
		TRPackage TRef = (TRPackage)TS.getTypeRef();
		
		String PName = TRef.getPackageName();
		if(!this.getPackageName().equals(PName)) return TRef;
		if(TRef instanceof TRPackage_Internal)   return TRef;
		
		return new TRPackage_Internal(TS, PName, TRef.getTypeName(), pPAccess, pLocation); 
	}
	
	// Others Data -----------------------------------------------------------------------------------------------------
	
	/** A dummy package that has elements just like the real one but no elements' body (just signatures) */
	Package ThePackage = null;
	/**
	 * Returns the simulated package that duplicate the actual one (can be use for elements investigation for
	 *    compile-time type checking)
	 **/
	final public Package getPackage() {
		return this.ThePackage;
	}
	
	/**{@inheritDoc}*/ @Override
	public String toString() {
		return "Package " + this.getName();
	}

   	/** Checks if the ID is a valid ID */ @Override
   	public boolean isValidID(Object pID) {
   		return (this.UB != null) && this.UB.isValidID(pID);
   	}
   	
	// Package related -------------------------------------------------------------------------------------------------
	
	/** Add a friend package to this package */
	public boolean addFriendPackage(String pPName) {
		if(!this.isActive())                   return false;
		if(pPName == null)                     return false;
		if(this.Name.equals(pPName))           return  true;
		if(!this.UB.UM.isPackageExist(pPName)) return false;
		// Default package cannot be added as a friend
		if((this.UB.UM.getEngine().getExtension(EE_DefaultPackage.DefaultPackageName) != null)
			&&  EE_DefaultPackage.DefaultPackageName.equals(pPName)) return false;
			
		if(this.FriendNames == null) this.FriendNames = new HashSet<String>();
		if(this.FriendNames.contains(pPName)) this.FriendNames.add(pPName);
		return true;
	} 
	
	// Add Required Package, PVar, Func, Type (Ensure non of those are default package)
	
    @SuppressWarnings("unlikely-arg-type")
	/** Add a required package to this package */
	public boolean addRequiredPackage(String pPName) {
		if(!this.isActive())                   return false;
		if(pPName == null)                     return false;
		if(this.Name.equals(pPName))           return  true;
		if(!this.UB.UM.isPackageExist(pPName)) return false;
		// Default package cannot be added as a friend
		if((this.UB.UM.getEngine().getExtension(EE_DefaultPackage.DefaultPackageName) != null)
			&&  EE_DefaultPackage.DefaultPackageName.equals(pPName)) return false;
			
		if(this.RequiredArtifacts == null) this.RequiredArtifacts = new HashSet<Required>();
		if(this.RequiredArtifacts.contains(pPName)) this.RequiredArtifacts.add(new Required(pPName));
		return true;
	}
    @SuppressWarnings("unlikely-arg-type")
	/** Add a required type to this package */
	public boolean addRequiredType(String pPName, String pTName) {
		if(!this.isActive())                   return false;
		if(pPName == null)                     return false;
		if(pTName == null)                     return false;
		if(this.Name.equals(pPName))           return  true;
		if(!this.UB.UM.isPackageExist(pPName)) return false;
		// Default package cannot be added as a friend
		if((this.UB.UM.getEngine().getExtension(EE_DefaultPackage.DefaultPackageName) != null)
			&&  EE_DefaultPackage.DefaultPackageName.equals(pPName)) return false;
		if(!this.UB.UM.isTypeExist(null, pPName, pTName)) return false;
			
		if(this.RequiredArtifacts == null) this.RequiredArtifacts = new HashSet<Required>();
		if(this.RequiredArtifacts.contains(pPName)) this.RequiredArtifacts.add(new Required_Type(pPName, pTName));
		return true;
	}
    @SuppressWarnings("unlikely-arg-type")
	/** Add a required package variable to this package */
	public boolean addRequiredPackageVariable(String pPName, String pVName) {
		if(!this.isActive())                   return false;
		if(pPName == null)                     return false;
		if(pVName == null)                     return false;
		if(this.Name.equals(pPName))           return  true;
		if(!this.UB.UM.isPackageExist(pPName)) return false;
		// Default package cannot be added as a friend
		if((this.UB.UM.getEngine().getExtension(EE_DefaultPackage.DefaultPackageName) != null)
			&&  EE_DefaultPackage.DefaultPackageName.equals(pPName)) return false;
		if(!this.UB.UM.isTypeExist(null, pPName, pVName)) return false;
			
		if(this.RequiredArtifacts == null) this.RequiredArtifacts = new HashSet<Required>();
		if(this.RequiredArtifacts.contains(pPName)) this.RequiredArtifacts.add(new Required_PVar(pPName, pVName));
		return true;
	}
    @SuppressWarnings("unlikely-arg-type")
	/** Add a required package variable to this package */
	public boolean addRequiredFunction(String pPName, ExecSignature pES) {
		if(!this.isActive())                   return false;
		if(pPName == null)                     return false;
		if(pES    == null)                     return false;
		if(this.Name.equals(pPName))           return  true;
		if(!this.UB.UM.isPackageExist(pPName)) return false;
		// Default package cannot be added as a friend
		if((this.UB.UM.getEngine().getExtension(EE_DefaultPackage.DefaultPackageName) != null)
			&&  EE_DefaultPackage.DefaultPackageName.equals(pPName)) return false;
		if(!this.UB.UM.isFunctionExist(null, pPName, pES)) return false;
			
		if(this.RequiredArtifacts == null) this.RequiredArtifacts = new HashSet<Required>();
		if(this.RequiredArtifacts.contains(pPName)) this.RequiredArtifacts.add(new Required_Func(pPName, pES));
		return true;
	}
	
	// Add Elements --------------------------------------------------------------------------------

	// Dynamic --------------------------------------------------------
	/** Creates a new attribute info */
	public void addPVarDynamic(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig, String pVName,
			TypeRef pTRef, MoreData pMoreData) {
		this.addAttrDynamic(pPARead, pPAWrite, pPAConfig, pVName, pTRef, pMoreData);
	}
	/** Creates a new operation info */
	public void addFunctDynamic(Accessibility pPAccess, ExecSignature pES, MoreData pMoreData) {	
		this.addOperDynamic(pPAccess, pES, pMoreData);
	}
	// Field --------------------------------------------------------
	/** Creates a new attribute info */
	public void addPVarDlgAttr(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig, String pVName,
			String pTName, MoreData pMoreData) {
		this.addAttrDlgAttr(pPARead, pPAWrite, pPAConfig, pVName, pTName, pMoreData);
	}
	/** Creates a new operation info */
	public void addFunctDlgAttr(Accessibility pPAccess, ExecSignature pES, String pTName, MoreData pMoreData) {
		this.addOperDlgAttr(pPAccess, pES, pTName, pMoreData);
	}
	
	// Direct ----------------------------------------------------------------------------------------------------------
	
	/**
	 * Add a package constant to this package. The variable will be public and its factory will
	 *     be nawa.curry.Variable.<br />
	 * @param pVName       is the name of the variable.
	 * @param pValue       is the variable default value.
	 * @param pIsValueExpr is the flag to indicate if the default value should consider an expression (run before assign).
	 * @return `true` if adding success or `false` if failed.
	 **/
	public Object addPConst(String pVName, boolean pIsNotNull, Serializable pValue, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		return this.addAttrConst(pVName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData);
	}
	/**
	 * Add a package variable to this package. The variable will be public and its factory will
	 *     be nawa.curry.Variable.Factory.<br />
	 * @param pVName       is the name of the variable.
	 * @param pType        is the variable type.
	 * @param pValue       is the variable default value.
	 * @param pIsValueExpr is the flag to indicate if the default value should consider an expression (run before assign).
	 * @return `true` if adding success or `false` if failed.
	 **/
	public Object addPVarDirect(String pVName, boolean pIsNotNull, TypeRef pTypeRef, Serializable pValue,
			boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		return this.addAttrDirect(pVName, pIsNotNull, pTypeRef, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData);
	}
	/**
	 * Add a package variable to this package.<br />
	 * @param pPARead   is the accessibility for reading the value and other info from the variable.
	 * @param pPAWrite  is the accessibility for writing the value to the variable.
	 * @param pPAConfig is the accessibility for changing configuration of the variable.
	 * @param pVName    is the name of the variable.
	 * @param pDHI      is the dataholder info of the variable.
	 * @return `true` if adding success or `false` if failed.
	 **/
	public Object addPVarDirect(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig, String pVName,
			boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData) {
		return this.addAttrDirect(pPARead, pPAWrite, pPAConfig, pVName, pIsNotNull, pDHI, pLocation, pMoreData);
	}
	/**
	 * Add a package function to this package<br />
	 * @param pPAccess is the accessibility of this function.
	 * @param pExec    is a sun-routine to be the function. pExec must not be a wrapper. It must
	 *                     also not be an external/java sub-routine that is not truely serializable
	 *                     (link to a non-serializable object).
	 * @return `true` if adding success or `false` if failed. 
	 **/
	public Object addFunctDirect(Accessibility pPAccess, Executable pExec, MoreData pMoreData) {
		return this.addOperDirect(pPAccess, pExec, pMoreData);
	}
	
	// Temp Element ----------------------------------------------------------------------------------------------------
	
	/**
	 * Add a package constant to this package. The variable will be public and its factory will
	 *     be nawa.curry.Variable.<br />
	 * @param pVName       is the name of the variable.
	 * @param pValue       is the variable default value.
	 * @param pIsValueExpr is the flag to indicate if the default value should consider an expression (run before assign).
	 * @return `true` if adding success or `false` if failed.
	 **/
	public Object addTempPConst(String pVName, boolean pIsNotNull, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData, Object pTempData) {
		return this.addTempAttrConst(pVName, pIsNotNull, IsValueExpr, pMoreInfo, pLocation, pMoreData, pTempData);
	}
	/**
	 * Add a package variable to this package. The variable will be public and its factory will
	 *     be nawa.curry.Variable.Factory.<br />
	 * @paran pVName       is the name of the variable.
	 * @param pType        is the variable type.
	 * @param pValue       is the variable default value.
	 * @param pIsValueExpr is the flag to indicate if the default value should consider an expression (run before assign).
	 * @return `true` if adding success or `false` if failed.
	 **/
	public Object addTempPVarDirect(String pVName, boolean pIsNotNull, TypeRef pTypeRef, boolean IsValueExpr, MoreData pMoreInfo,
			Location pLocation, MoreData pMoreData, Object pTempData) {
		return this.addTempAttrDirect(pVName, pIsNotNull, pTypeRef, IsValueExpr, pMoreInfo, pLocation, pMoreData, pTempData);
	}
	/**
	 * Add a package variable to this package.<br />
	 * @param pPARead   is the accessibility for reading the value and other info from the variable.
	 * @param pPAWrite  is the accessibility for writing the value to the variable.
	 * @param pPAConfig is the accessibility for changing configuration of the variable.
	 * @paran pVName    is the name of the variable.
	 * @param pDHI      is the dataholder info of the variable.
	 * @return `true` if adding success or `false` if failed.
	 **/
	public Object addTempPVarDirect(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig, String pVName,
			boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData, Object pTempData) {
		return this.addTempAttrDirect(pPARead, pPAWrite, pPAConfig, pVName, pIsNotNull, pDHI, pLocation, pMoreData, pTempData);
	}
	/**
	 * Add a package function to this package<br />
	 * @param pPAccess is the accessibility of this function.
	 * @param pExec    is a executable. pExec must not be a wrapper. It must
	 *                     also not be an external/java sub-routine that is not truely serializable
	 *                     (link to a non-serializable object).
	 * @return `true` if adding success or `false` if failed. 
	 **/
	public void addTempFunctDirect(Accessibility pPAccess, Executable pExec, MoreData pMoreData, Object pTempData) {
		this.addTempOperDirect(pPAccess, pExec, pMoreData, pTempData);
	}
	
	// Artifacts ---------------------------------------------------------------
	
	/** Checks if the package variable `pVName` exist. */
	public boolean isVarExist(String pVName) {
		return this.isAttrExist(pVName);
	}
	/** Checks if the function `pES` exist. */
	public boolean isFunctionExist(ExecSignature pES) {
		return this.isOperExist(pES);
	}
	
	// Type -------------------------------------------------------------------
	
	/** Obtains the types */
	public Type tryToGetTypeAtCompileTime(CompileProduct pCProduct, TypeRef pTRef) {
		return this.tryToGetTypeAtCompileTime(pCProduct, pTRef, false);
	}
	
	/** Obtains the types */
	public Type tryToGetTypeAtCompileTime(CompileProduct pCProduct, TypeRef pTRef, boolean IsForceInitialized) {
		MType MT = pCProduct.getEngine().getTypeManager();
		
		if(pTRef.isLoaded()) {
			Type T = pTRef.getTheType();
			if(IsForceInitialized)
				MT.ensureTypeInitialized(
					null,
					T.getTypeRef(),
					(pTRef instanceof TRBasedOnType)
						? ((TRBasedOnType)pTRef).getBaseTypeRef()
						: null);
			return T;
		}
		
		boolean IsLocal = true;
		
		IsLocal &= ((pCProduct != null) && (this.getUnitBuilder().getName() != null));
		IsLocal &= this.getUnitBuilder().getName().equals(pCProduct.getCurrentFeederName());
		IsLocal &= this                 .getName().equals(pCProduct.getOwnerPackageName());
		IsLocal &= (pTRef instanceof TRPackage) && this.getName().equals(((TRPackage)pTRef).getPackageName());

		Type T = null;
		try {
			T = IsLocal
			         ? MT.getTypeFromRefNoCheck(null, pTRef)
			         : MT.getTypeFromRef(       null, pTRef);
			if(T != null) {
				if(IsForceInitialized)
					MT.ensureTypeInitialized(
						null,
						T.getTypeRef(),
						(pTRef instanceof TRBasedOnType)
							? ((TRBasedOnType)pTRef).getBaseTypeRef()
							: null);
				return T;
			}
		} catch (IllegalArgumentException E) {
			if(!pCProduct.getCompilationState().isTypeRefinition() && pCProduct.getCompilationState().isTypeRegistration()) {
				System.err.println("Exception <tryToGetTypeAtCompileTime:363>: " + E);
				E.printStackTrace(System.err);
			}
		} catch (Exception E) {
			if(IsForceInitialized && (T != null)) {				
				TypeBuilder TB = this.getTypeBuilder(((TRPackage)pTRef).getTypeName());
				Location    L  = (TB == null) ? null : TB.getLocation();
				// If the problem is from the other code file, no report
				if(!pCProduct.getCurrentCode().getCodeName().equals(L.getCodeName())) return null;
					
				Code C = (Code)pCProduct.getCodeData(pCProduct.getCurrentFeederIndex(), L.getCodeName(), CodeFeeders.DataName_Code);
				int  P = C.getStartPosOfLine(L.getLineNumber() - 1) + L.getColumn();
				
				pCProduct.reportError("Fail to initialize type `"+T.toString()+"` ", E, P);
				return null;
			}
		}
		return null;
	}
	
}