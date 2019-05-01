package net.nawaman.curry;

import net.nawaman.curry.StackOwner.OperationSearchKind;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.util.UString;

/** Provide information about a StackOwner */
public class StackOwnerInfo {
	
	StackOwner SO = null;
	
	public StackOwnerInfo(StackOwner pSO) {
		this.SO = pSO;
	}
	
	/** Returns the StackOwner that this object is helping */
	final public StackOwner getSO() {
		return this.SO;
	}
	
	/**
	 * Changes the StackOwner that this object is helping.
	 * Returns if the change success.
	 **/
	final public boolean changeSO(StackOwner pSO) {
		if(this.isImmutable() || (pSO == null)) return false;
		this.SO = pSO;
		return true;
	}
	
	/** Checks if the StackOwnerInfo is immutable (its StackOWner cannot be changed) **/
	public boolean isImmutable() {
		return false;
	}

	// Dynamic Handle --------------------------------------------------------------------------------------------------

	static private ExecSignature DynamicHandleSignature = null; 
	/** The default interface of the dynamic handler operation  */
	static public final ExecSignature getDynamicHandleSignature() {
		if(DynamicHandleSignature == null) {
			DynamicHandleSignature = ExecSignature.newSignature(
				"handleDynamic",
				new TypeRef[] { TKJava.TAny.getTypeRef(), TKJava.TAny.getTypeRef() },
				new String[]  { "pIdentity",              "pParameters"            },
				true,
				TKJava.TAny.getTypeRef(),
				null,
				null
			);
		}
		
		return DynamicHandleSignature;
	}

	// ---------------------------------------------------------------------------------------------
	// Handle A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	/** Returns an array of all the non-dynamic operation info */
	public OperationInfo[] getOperationInfos() {	
		OperationInfo[] OIs = this.SO.getAllNonDynamicOperationInfo(null);
		return (OIs == null) ? null : OIs.clone();
	}
	/** Returns an array of all the non-dynamic attribute info */
	public AttributeInfo[] getAttributeInfos() {
		AttributeInfo[] AIs = this.SO.getAllNonDynamicAttributeInfo(null);
		return (AIs == null) ? null : AIs.clone();
	}

	// Handle A/O ----------------------------------------------------------------------------------
	// NOTE: High level
	// For reliability (type checking of parameters) and Convenient for the users
	
	// With pIsSearchInDynamicDelegation for more flexibility
	
	/** Search attribute of this StackOwner as the type */
	public TypeRef searchAttribute(Engine pEngine, boolean pIsSearchInDynamicDelegation, Type pAsType, String pName) {
		return this.SO.searchAttribute(pEngine, pIsSearchInDynamicDelegation, pAsType, pName);
	}

	/** Search operation of this StackOwner as the type using name and parameters */
	final public ExecSignature searchOperation(Engine pEngine, boolean pIsSearchInDynamicDelegation, String pOName,
			Object[] pParams, Object[][] pAdjParams) {
		if(pEngine == null) pEngine = this.SO.getEngine();
		return this.SO.searchOperation(pEngine, OperationSearchKind.ByParams,
				pIsSearchInDynamicDelegation, pOName, pParams, pAdjParams);
	}

	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final public ExecSignature searchOperation(Engine pEngine, boolean pIsSearchInDynamicDelegation, String pOName,
			TypeRef[] pPTypeRefs) {
		if(pEngine == null) pEngine = this.SO.getEngine();
		return this.SO.searchOperation(pEngine, OperationSearchKind.ByTRefs,
				pIsSearchInDynamicDelegation, pOName,
				pPTypeRefs, null);
	}

	/** Search operation of this StackOwner as the type using name and interface */
	final public ExecSignature searchOperation(Engine pEngine, boolean pIsSearchInDynamicDelegation, String pOName,
			ExecInterface pExecInterface) {
		if(pEngine == null) pEngine = this.SO.getEngine();
		return this.SO.searchOperation(pEngine, OperationSearchKind.ByNameInterface,
				pIsSearchInDynamicDelegation, pOName, pExecInterface, null);
	}

	/**
	 * Search operation of this StackOwner as the type using name and signature (if the signature
	 * are exact match, you can use it to execute)
	 */
	final public ExecSignature searchOperation(Engine pEngine, boolean pIsSearchInDynamicDelegation,
			ExecSignature pExecSignature) {
		if(pEngine == null) pEngine = this.SO.getEngine();
		return this.SO.searchOperation(pEngine, OperationSearchKind.BySignature,
				pIsSearchInDynamicDelegation, pExecSignature, null, null);
	}

	// Without pIsSearchInDynamicDelegation for convenient

	/** Search operation of this StackOwner as the type using name and parameters */
	final public ExecSignature searchOperation(Engine pEngine, String pOName, Object[] pParams, Object[][] pAdjParams) {
		return this.SO.searchOperation(pEngine, pOName, pParams, pAdjParams);
	}

	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final public ExecSignature searchOperation(Engine pEngine, String pOName, TypeRef[] pPTypeRefs) {
		return this.SO.searchOperation(pEngine, pOName, pPTypeRefs);
	}

	/** Search operation of this StackOwner as the type using name and interface */
	final public ExecSignature searchOperation(Engine pEngine, String pOName, ExecInterface pExecInterface) {
		return this.SO.searchOperation(pEngine, pOName, pExecInterface);
	}

	/**
	 * Search operation of this StackOwner as the type using name and signature (if the signature
	 * are exact match, you can use it to execute)
	 */
	final public ExecSignature searchOperation(Engine pEngine, ExecSignature pExecSignature) {
		return this.SO.searchOperation(pEngine, pExecSignature);
	}

	// Without Engine and pIsSearchInDynamicDelegation for convenient

	/** Search operation of this StackOwner as the type using name and parameters */
	final public ExecSignature searchOperation(String pOName, Object[] pParams, Object[][] pAdjParams) {
		return this.SO.searchOperation(null, OperationSearchKind.ByParams, true, pOName, pParams, pAdjParams);
	}

	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final public ExecSignature searchOperation(String pOName, TypeRef[] pPTypeRefs) {
		return this.SO.searchOperation(null, OperationSearchKind.ByTRefs, true, pOName, pPTypeRefs, null);
	}

	/** Search operation of this StackOwner as the type using name and interface */
	final public ExecSignature searchOperation(String pOName, ExecInterface pExecInterface) {
		return this.SO.searchOperation(null, OperationSearchKind.ByNameInterface, true, pOName, pExecInterface, null);
	}

	/**
	 * Search operation of this StackOwner as the type using name and signature (if the signature
	 * are exact match, you can use it to execute)
	 */
	final public ExecSignature searchOperation(ExecSignature pExecSignature) {
		return this.SO.searchOperation(null, OperationSearchKind.BySignature, true, pExecSignature, null, null);
	}
	
	// Specification ------------------------------------------------------------------------------
	
	/** Atrtibute Specification */
	final public AttributeInfo getAttributeInfo(String pVName, Type pAsType) {
		if(pVName == null) return null;
		AttributeInfo[] AIs = (this.SO instanceof StackOwner_Simple)
                                  ? ((StackOwner_Simple)this.SO).getAttributeInfos()
                                  : this.SO.getAllNonDynamicAttributeInfo(null);
		if(AIs    == null) return null;
		int hSearch = UString.hash(pVName);
		for(int i = AIs.length; --i >= 0; ) {
			if(hSearch != AIs[i].getNameHash()) continue;
			return AIs[i];
		}
		return null;
	}
	/** Operation Specification */
	final public OperationInfo getOperationInfo(ExecSignature pSignature) {
		if(pSignature == null) return null;
		OperationInfo[] OIs = (this.SO instanceof StackOwner_Simple)
		                          ? ((StackOwner_Simple)this.SO).getOperationInfos()
		                          : this.SO.getAllNonDynamicOperationInfo(null);
		if(OIs == null) return null;
		int hSearch = pSignature.hash_WithoutParamNamesReturnType();
		for(int i = OIs.length; --i >= 0; ) {
			if(hSearch != OIs[i].getSignatureHash()) continue;
			return OIs[i];
		}
		return null;
	}

	// Display information ------------------------------------------------------------------------

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final protected String getAttributeAccessToString(DataHolder.AccessKind pAKind, Type pAsType, 
			String pAttrName, Object pParam1, Object pParam2) {
		return this.SO.getAttributeAccessToString(pAKind, pAsType, pAttrName);
	}

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final protected String getAttributeAccessToString(DataHolder.AccessKind pAKind, Type pAsType, String pAttrName) {
		return this.SO.getAttributeAccessToString(pAKind, pAsType, pAttrName);
	}

	/** Display the operation access as a string */
	final protected String getOperationAccessToString(OperationSearchKind pOSKind, Type pAsType, Object pParam1, Object pParam2) {
		return this.SO.getOperationAccessToString(pOSKind, pAsType, pParam1, pParam2);
	}
}
