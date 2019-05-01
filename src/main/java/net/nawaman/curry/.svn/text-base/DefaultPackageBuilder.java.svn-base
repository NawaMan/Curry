package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.curry.EngineExtensions.EE_DefaultPackage;
import net.nawaman.curry.Executable.SubRoutine;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;

/** Interface that allow access to the default package */
public interface DefaultPackageBuilder {

	// Dynamic ---------------------------------------------------------
	
	/** Creates a new attribute info */
	public boolean addPVarDynamic(Accessibility pARead, Accessibility pAWrite, Accessibility pAConfig,
			String pVName, TypeRef pTRef, MoreData pMoreData);
	
	/** Creates a new operation info */
	public boolean addFuncDynamic(Accessibility pAccess, ExecSignature pES, MoreData pMoreData);
	
	// DlgAttr -----------------------------------------------------------
	
	/** Creates a new attribute info */
	public boolean addPVarDlgAttr(Accessibility pARead, Accessibility pAWrite, Accessibility pAConfig,
			String pVName, String pTName, MoreData pMoreData);
	
	/** Creates a new operation info */
	public boolean addFuncDlgAttr(Accessibility pAccess, ExecSignature pES, String pTName, MoreData pMoreData);
	
	// Direct ----------------------------------------------------------
	/**
	 * Add a package constant to the default package. The variable will be public and its 
	 *     factory will be net.nawaman.curry.Variable.<br />
	 * @param pVName       is the name of the variable.
	 * @param pValue       is the variable default value.
	 * @param pIsValueExpr is the flag to indicate if the default value should consider an expression (run
	 * 						before assign).
	 * @return `true` if adding success or `false` if failed.
	 **/
	public boolean addPVarConst(String pVName, boolean pIsNotNull, Serializable pValue, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData);
	
	/**
	 * Add a package variable to the default package. The variable will be public and its factory will
	 *     be net.nawaman.curry.Variable.Factory.<br />
	 * @param pVName       is the of the variable.
	 * @param pType        is the variable type.
	 * @param pValue       is the variable default value.
	 * @param pIsValueExpr is the flag to indicate if the default value should consider an expression (run
	 * 						before assign).
	 * @return `true` if adding success or `false` if failed.
	 **/
	public boolean addPVar(String pVName, boolean pIsNotNull, Type pType, Serializable pValue,
            boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData);
	
	/**
	 * Add a package variable to the default package.<br />
	 * @param pPARead   is the accessibility for reading the value and other info from the variable.
	 * @param pPAWrite  is the accessibility for writing the value to the variable.
	 * @param pPAConfig is the accessibility for changing configuration of the variable.
	 * @param pVName    is the name of the variable.
	 * @param pDHI      is the DataHolder info of the variable.
	 * @return `true` if adding success or `false` if failed.
	 **/
	public boolean addPDataHolder(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
            String pVName, boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData);
	
	/**
	 * Add a package variable to the default package.<br />
	 * @param pPAccess     is the accessibility for reading the value and other info from the variable.
	 * @param pVName       is the name of the variable.
	 * @param pType        is the variable type.
	 * @param pValue       is the variable default value.
	 * @param pIsValueExpr is the flag to indicate if the default value should consider an expression (run before assign).
	 * @return `true` if adding success or `false` if failed.
	 **/
	public boolean addPVar(Accessibility pPAccess, String pVName, boolean pIsNotNull, Type pType,
			Serializable pValue, boolean IsValueExpr, MoreData pMoreInfo, Location pLocation,
			MoreData pMoreData);
	
	/**
	 * Add a package variable to the default package.<br />
	 * @param pPARead   is the accessibility for reading the value and other info from the variable.
	 * @param pPAWrite  is the accessibility for writing the value to the variable.
	 * @param pPAConfig is the accessibility for changing configuration of the variable.
	 * @param pVName    is the name of the variable.
	 * @param pType        is the variable type.
	 * @param pValue       is the variable default value.
	 * @param pIsValueExpr is the flag to indicate if the default value should consider an expression (run before assign).
	 * @return `true` if adding success or `false` if failed.
	 **/
	public boolean addPVar(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
			String pVName, boolean pIsNotNull, Type pType, Serializable pValue,
			boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData);
	
	/**
	 * Bind a data holder as a package variable to the default package.<br />
	 * @param pPARead     is the accessibility for reading the value and other info from the variable.
	 * @param pPAWrite    is the accessibility for writing the value to the variable.
	 * @param pPAConfig   is the accessibility for changing configuration of the variable.
	 * @param pVName      is the name of the variable.
	 * @param pDataHolder is the variable type.
	 * @return `true` if adding success or `false` if failed.
	 **/
	public boolean bindPDataHolder(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
            String pVName, boolean pIsNotNull, DataHolder DH, Location pLocation, MoreData pMoreData);
	
	/**
	 * Add a package function to this package<br />
	 * @param pPAccess is the accessibility of this function.
	 * @param pExec    is a sun-routine to be the function. pExec must not be a wrapper. It must
	 *                     also not be an external/java sub-routine that is not truly serializable
	 *                     (link to a non-serializable object).
	 * @return `true` if adding success or `false` if failed. 
	 **/
	public boolean addFunction(Accessibility pPAccess, SubRoutine pExec, MoreData pMoreData);
	
	/**
	 * Add a package type to this package.<br />
	 * @param pPAccess is the accessibility of this function.
	 * @param pTName   is the name of the type as it to be referred to.
	 * @param TS       is the type spec of the type. The type ref must be given as null or a
	 *                      no-name ref.
	 * @return `true` if adding success or `false` if failed.
	 **/
	public boolean addType(Accessibility pPAccess, String pTName, TypeSpec TS, Location pLocation);
	
	// Lock --------------------------------------------------------------------
	
	/** This method will help limiting the implementation of this interface to be within this package. */
	public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface);
	
	// Simple Implementation -------------------------------------------------------------------------------------------
	
	static class Simple implements DefaultPackageBuilder {
		
		final EE_DefaultPackage EEDP;
		
		Simple(EE_DefaultPackage pEEDP) {
			this.EEDP = pEEDP;
		}
		
		// Dynamic --------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public boolean addPVarDynamic(Accessibility pARead, Accessibility pAWrite,
				Accessibility pAConfig, String pVName, TypeRef pTRef, MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addAttrDynamic(pARead, pAWrite, pAConfig, pVName, pTRef, pMoreData);
		}
		/**{@inheritDoc}*/ @Override
		public boolean addFuncDynamic(Accessibility pAccess, ExecSignature pES,
				MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addOperDynamic(pAccess, pES, pMoreData);
		}
		// DlgAttr --------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public boolean addPVarDlgAttr(Accessibility pARead, Accessibility pAWrite, Accessibility pAConfig,
				String pVName, String pTName, MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addAttrDlgAttr(pARead, pAWrite, pAConfig, pVName, pTName, pMoreData);
		}
		/**{@inheritDoc}*/ @Override
		public boolean addFuncDlgAttr(Accessibility pAccess, ExecSignature pES,
				String pTName, MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addOperDlgAttr(pAccess, pES, pTName, pMoreData);
		}
		
		// Direct --------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public boolean addPVarConst(String pVName, boolean pIsNotNull, Serializable pValue,
				boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addConstant(Package.Access.Public, pVName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation,
					pMoreData);
		}
		/**{@inheritDoc}*/ @Override
		public boolean addPVar(String pVName, boolean pIsNotNull, Type pType,
				Serializable pValue, boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addVariable(Package.Access.Public, pVName, pIsNotNull, pType.getTypeRef(), pValue, IsValueExpr, pMoreInfo,
					pLocation, pMoreData);
		}
		/**{@inheritDoc}*/ @Override
		public boolean addPDataHolder(Accessibility pPARead, Accessibility pPAWrite,
				Accessibility pPAConfig, String pVName, boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation,
				MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addDataHolder(pPARead, pPAWrite, pPAConfig, pVName, pIsNotNull, pDHI, pLocation, pMoreData);
		}
		/**{@inheritDoc}*/ @Override
		public boolean addPVar(Accessibility pPAccess, String pVName, boolean pIsNotNull,
				Type pType, Serializable pValue, boolean IsValueExpr, MoreData pMoreInfo, Location pLocation,
				MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addVariable(pPAccess, pVName, pIsNotNull, pType.getTypeRef(), pValue, IsValueExpr, pMoreInfo, pLocation,
					pMoreData);
		}
		/**{@inheritDoc}*/ @Override
		public boolean addPVar(Accessibility pPARead, Accessibility pPAWrite,
				Accessibility pPAConfig, String pVName, boolean pIsNotNull, Type pType, Serializable pValue,
				boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addVariable(pPARead, pPAWrite, pPAConfig, pVName, pIsNotNull, pType.getTypeRef(), pValue, IsValueExpr,
					pMoreInfo, pLocation, pMoreData);
		}
		/**{@inheritDoc}*/ @Override
		public boolean bindPDataHolder(Accessibility pPARead, Accessibility pPAWrite,
				Accessibility pPAConfig, String pVName, boolean pIsNotNull, DataHolder DH, Location pLocation,
				MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.bindDataHolder(pPARead, pPAWrite, pPAConfig, pVName, pIsNotNull, DH, pLocation, pMoreData);
		}
		/**{@inheritDoc}*/ @Override
		public boolean addFunction(Accessibility pPAccess, SubRoutine pExec,
				MoreData pMoreData) {
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP == null) return false;
			return DP.addOperation(pPAccess, pExec, pMoreData);
		}
		/**{@inheritDoc}*/ @Override
		public boolean addType(Accessibility pPAccess, String pTName, TypeSpec TS,
				Location pLocation) {
			if(pTName == null) return false;
			if(TS     == null) return false;
			if((TS.Ref != null) && !(TS.Ref instanceof TLNoName.TRNoName) && 
				!((TS.Ref instanceof TLPackage.TRPackage)
					&& EngineExtensions.EE_DefaultPackage.DefaultPackageName.equals(((TLPackage.TRPackage)TS.Ref).PName))) {
				return false;
			}
			
			Package DP = this.EEDP.Engine.DefaultPackage;
			if(DP.isTypeExist(pTName)) return false;
						
			// Re assign the type ref
			TS.Ref = new TLPackage.TRPackage_Internal(TS, DP.Name, pTName, pPAccess, pLocation);
			
			if(DP.TypeInfos == null) DP.TypeInfos = new TypeSpec[1];
			else {
				TypeSpec[] NTypeInfos = new TypeSpec[DP.TypeInfos.length + 1];
				System.arraycopy(DP.TypeInfos, 0, NTypeInfos, 0, DP.TypeInfos.length);
				DP.TypeInfos = NTypeInfos;
			}
			DP.TypeInfos[DP.TypeInfos.length - 1] = TS;
			return true;
		}
		
		/** This method will help limiting the implementation of this interface to be within this package. */
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
}