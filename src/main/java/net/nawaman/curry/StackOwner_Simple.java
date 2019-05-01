package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.curry.OperationInfo.OIDirect;
import net.nawaman.curry.OperationInfo.SimpleOperation;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UString;

/** StackOwner that looks for elements in itself only */
abstract public class StackOwner_Simple extends StackOwner_Lite {
	
	protected StackOwner_Simple() {}
	
	/** Local attribute informations **/
	AttributeInfo[] AttrInfos = null;
	/** Local operation informations **/
	OperationInfo[] OperInfos = null;
	
	transient int MaxDHIndex = 0;
	
	/** Prepare attributes (is called by initialize Elements) */
	protected void prepareAttrs(Context pContext, Engine pEngine) {}
	/** Prepare operations (is called by initialize Elements) */
	protected void prepareOpers(Context pContext, Engine pEngine) {}
	/** Prepare more elements that are not listed in AttrInfos and OperInfos  (is called by initialize Elements) */
	protected void initializeMoreElements(Context pContext, Engine pEngine) {}

	/** Ensure that the attributes are prepared */
	protected void ensureAttributesPrepared(Context pContext, Engine pEngine) {
		if(this.AttrInfos != null) return;
		this.prepareAttrs(pContext, pEngine);
	}
	/** Ensure that the operations are prepared */
	protected void ensureOperationsPrepared(Context pContext, Engine pEngine) {
		if(this.OperInfos == null) return;
		this.prepareOpers(pContext, pEngine);
	}
	
	/** Returns the attributes info of this StackOwner */
	final protected AttributeInfo[] getAttributeInfos() {
		this.ensureAttributesPrepared(null, null);
		return this.AttrInfos;
	}
	/** Returns the attributes info of this StackOwner */
	final protected AttributeInfo[] getAttributeInfos(Context pContext, Engine pEngine) {
		this.ensureAttributesPrepared(pContext, pEngine);
		return this.AttrInfos;
	}
	
	/** Returns the operations info of this StackOwner */
	final protected OperationInfo[] getOperationInfos() {
		this.ensureOperationsPrepared(null, null);
		return this.OperInfos;
	}
	/** Returns the operations info of this StackOwner */
	final protected OperationInfo[] getOperationInfos(Context pContext, Engine pEngine) {
		this.ensureOperationsPrepared(pContext, pEngine);
		return this.OperInfos;
	}
	
	// DataHolder ----------------------------------------------------------------------------------
	
	transient       boolean IsElementInitialized = false;
	/** Checks if the element has been initialized */
	final protected boolean isElementInitialized() {
		return this.IsElementInitialized;
	}
	/** Initialize all the elements */
	final protected void initializeElements(Context pContext, Engine pEngine) {
		if(this.IsElementInitialized) return;
		// Prepare the element
		this.ensureAttributesPrepared(pContext, pEngine);
		this.ensureOperationsPrepared(pContext, pEngine);

		// Set DHIndex (for Direct) and Owner (if not yet)
		if(this.AttrInfos != null) {
			for(int i = this.AttrInfos.length; --i >= 0; ) {
				AttributeInfo AI = this.AttrInfos[i];
				if(AI == null) continue;
				
				AI.changeCurrentHolder(this);
				if(AI instanceof AttributeInfo.AIDirect) {
					((AttributeInfo.AIDirect)AI).DHIndex = MaxDHIndex;
					MaxDHIndex++;
				}
			}
		}

		// Set Owner (if not yet)
		if(this.OperInfos != null) {
			for(OperationInfo OI : this.OperInfos) {
				if(OI == null) continue;
				if(     OI instanceof SimpleOperation) { ((SimpleOperation)OI).changeDeclaredOwner(this); continue; }
				else if(OI instanceof OIDirect)        { ((OIDirect)       OI).changeDeclaredOwner(this); continue; }
			}
		}
		
		// Initialize more Elements
		this.initializeMoreElements(pContext, pEngine);
		
		// Initialize the Attribute
		this.initializeAttributes(pContext, pEngine);
		
		// Set to be initialized
		this.IsElementInitialized = true;
	}
	
	// Abstract services -----------------------------------------------------------------
	
	/** Returns the current Engine (This method must not require null) */
	//abstract protected Engine getEngine();
	
	/** Checks if this StackOwner is appendable */
	abstract protected boolean isElementAppendable();
	
	// Implemented Services --------------------------------------------------------------
	
	// Attribute Info ----------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected int getAttrInfoCount() {
		this.ensureAttributesPrepared(null, null);
		if(this.AttrInfos == null) return 0;
		
		return this.AttrInfos.length;
	}
	
	/**{@inheritDoc}*/ @Override
	protected AttributeInfo getAttrInfoAt(int pIndex) {
		this.ensureAttributesPrepared(null, null);
		if(this.AttrInfos == null) return null;
		
		if((pIndex < 0) || (pIndex >= this.AttrInfos.length)) return null;
		return this.AttrInfos[pIndex];
	}

	/**{@inheritDoc}*/ @Override
	final protected int  getMaxDHIndex() {
		return this.MaxDHIndex;
	}
	/** Increase the current max-DHIndex (the maximum number of the data-holder index) by one */
	final protected void incMaxDHIndex() {
		this.MaxDHIndex++;
	}
	
	// Existing Check ----------------------------------------------------------
	// Exact search locally
	
	/** Checks if the attribute `pVName` exist. */
	protected boolean isAttrExist(String pVName) {
		if(pVName == null) return false;

		this.ensureAttributesPrepared(null, null);
		if(this.AttrInfos == null) return false;
		
		int hSearch = UString.hash(pVName);
		for(int i = this.AttrInfos.length; --i >= 0; ) {
			if(hSearch != this.AttrInfos[i].getNameHash()) continue;
			return true;
		}
		return false;
	}
	/** Checks if the operation `pES` exist. */
	protected boolean isOperExist(ExecSignature pSignature) {
		if(pSignature == null) return false;

		this.ensureOperationsPrepared(null, null);
		if(this.OperInfos == null) return false;
		
		int hSearch = pSignature.hashCode();
		for(int i = this.OperInfos.length; --i >= 0; ) {
			if(hSearch != this.OperInfos[i].getSignatureHash()) continue;
			return true;
		}
		return false;
	}
	
	// Respond -----------------------------------------------------------------
	// NOTE - Ignore all AsType

	/**{@inheritDoc}*/ @Override
	 protected OperationInfo getOperationLocal(Context pContext, Type pAsType, ExecSignature pSignature) {
		if(pSignature == null) return null;

		this.ensureOperationsPrepared(pContext, ((pAsType == null) ? null : pAsType.getEngine()));
		if(this.OperInfos == null) return null;
		
		OperationInfo OI = null;
		int hSearch = pSignature.hashCode();
		for(int i = 0; i < this.OperInfos.length; i++) {
			OperationInfo OI_InList = this.OperInfos[i];
			if(OI_InList == null) return null;
				
			if(hSearch != this.OperInfos[i].getSignatureHash()) continue;
			OI = this.OperInfos[i];
			break;
		}
		
		// Not Found
		if(OI == null)
			return null;
		
		if(!this.isOperAllowed(pContext, OI))
			return OperationInfo.NoPermission;
			
		return OI;
	}
	/**{@inheritDoc}*/ @Override
	protected AttributeInfo getAttributeLocal(Context pContext, DataHolder.AccessKind DHAK, Type pAsType, String pName) {
		if(pName == null) return null;

		this.ensureAttributesPrepared(pContext, ((pAsType == null) ? null : pAsType.getEngine()));
		if(this.AttrInfos == null) return null;
		
		AttributeInfo AI = null;
		int hSearch = UString.hash(pName);
		for(int i = 0; i < this.AttrInfos.length; i++) {
			AttributeInfo AI_InList = this.AttrInfos[i];
			if(AI_InList == null) continue;
			
			if(hSearch != AI_InList.getNameHash()) continue;
			AI = AI_InList;
			break;
		}
		if(AI == null) return null;	// Not Found
		if(!this.isAttrAllowed(pContext, AI, DHAK)) return AttributeInfo.NoPermission;
		return AI;
	}
	
	/**{@inheritDoc}*/ @Override
	protected OperationInfo[] getAllNonDynamicOperationInfo(Type pAsType) {
		this.ensureOperationsPrepared(null, ((pAsType == null) ? null : pAsType.getEngine()));
		if(this.OperInfos == null) return null;
		
		return this.OperInfos.clone();
	}
	/**{@inheritDoc}*/ @Override
	protected AttributeInfo[] getAllNonDynamicAttributeInfo(Type pAsType) {
		this.ensureAttributesPrepared(null, ((pAsType == null) ? null : pAsType.getEngine()));
		if(this.AttrInfos == null) return null;
		
		return this.AttrInfos.clone();
	}
	
	// Searches ----------------------------------------------------------------
	// NOTE - Ignore all AsType

	/** Search attribute of this StackOwner as the type */ @Override
	protected TypeRef searchAttributeLocal(Engine pEngine, Type pAsType, String pName) {
		if(pName == null) return null;

		this.ensureAttributesPrepared(null, ((pEngine == null) ? ((pAsType == null) ? null : pAsType.getEngine()) : pEngine));
		if(this.AttrInfos == null) return null;
		
		for(int i = 0; i < this.AttrInfos.length; i++) {
			AttributeInfo AI = this.AttrInfos[i];
			if(AI == null) continue;
			
			if(!pName.equals(this.AttrInfos[i].Name))
				continue;
			
			// Ensure the attribute is resolved
			AI.resolve(pEngine);
			// Then returns
			return AI.getTypeRef();
		}
		return null;
	}
	
	/**
	 * Search operation of this StackOwner as the type using name and parameters. <br />
	 *    If pOSKind is ByParams and the pAdjParams is Object[1][], pAdjParams[1] should be the
	 *        adjusted value or null (adjust not support).
	 **/ @Override
	 protected ExecSignature searchOperationLocal(Engine pEngine,
			OperationSearchKind pOSKind, Object pParam1, Object pParam2, Object pParam3) {
		if(pParam1 == null) throw new NullPointerException();
		if(pOSKind == null) throw new NullPointerException();

		this.ensureOperationsPrepared(null, pEngine);
		if(this.OperInfos == null) return null;
		
		int Index = -1;
		switch(pOSKind) {
			case Direct: return (ExecSignature)pParam1;
			case BySignature: {
				Index = ExecInterface.Util.searchExecutableBySignature(pEngine, null, 
						(HasSignature[])this.OperInfos, (ExecSignature)pParam1, false);
				break;
			}
			case ByParams: {
				Index = ExecInterface.Util.searchExecutableByParams(pEngine, null,
						(HasSignature[])this.OperInfos, (String)pParam1, (Object[]) pParam2,
						(Object[][])pParam3, false);
				break;
			}
			case ByTRefs: {
				Index = ExecInterface.Util.searchExecutableByTRefs(pEngine, null,
						(HasSignature[])this.OperInfos, (String)pParam1, (TypeRef[])pParam2, false);
				break;
			}
			case ByNameInterface: {
				Index = ExecInterface.Util.searchExecutableByInterface(pEngine, null,
						(HasSignature[])this.OperInfos, (String)pParam1, (ExecInterface)pParam2, false);
				break;
			}
		}
		if(Index == ExecInterface.NotMatch) return null;
		return this.OperInfos[Index].getSignature();
	}
	
	// Utilities -------------------------------------------------------------------------
	
	// Add more elements at run-time -------------------------------------------
	
	 /** Add a new attribute at run-time */
	boolean addAttrInfo(AttributeInfo pAttrInfo) {
		if(!this.isElementAppendable()) return false;
		
		if(this.AttrInfos == null) {
			this.AttrInfos = new AttributeInfo[1];
		} else {
			AttributeInfo[] NAttrInfo = new AttributeInfo[this.AttrInfos.length + 1];
			System.arraycopy(this.AttrInfos, 0, NAttrInfo, 0, this.AttrInfos.length);
			this.AttrInfos = NAttrInfo;
		}
		this.AttrInfos[this.AttrInfos.length - 1] = pAttrInfo;
		if(pAttrInfo instanceof AttributeInfo.AIDirect) {
			((AttributeInfo.AIDirect)pAttrInfo).DHIndex = this.MaxDHIndex;
			this.MaxDHIndex++;
		}
		return true;
	}
	 /** Add a new operation at run-time */
	boolean addOperInfo(OperationInfo pOperInfo) {
		if(!this.isElementAppendable()) return false;
		
		if(this.OperInfos == null) this.OperInfos = new OperationInfo[1];
		else {
			OperationInfo[] NOperInfos = new OperationInfo[this.OperInfos.length + 1];
			System.arraycopy(this.OperInfos, 0, NOperInfos, 0, this.OperInfos.length);
			this.OperInfos = NOperInfos;
		}
		this.OperInfos[this.OperInfos.length - 1] = pOperInfo;
		return true;
	}

	// Dynamic --------------------------------------------------------
	/** Creates a new attribute info */
	protected boolean addAttrDynamic(Accessibility pARead, Accessibility pAWrite, Accessibility pAConfig, String pVName,
			TypeRef pTRef, MoreData pMoreData) {
		if(!this.isElementAppendable()) return false;
		if(pVName == null)              return false;
		if(this.isAttrExist(pVName))    return false;
		if(pARead   == null) pARead   = Accessibility.Private;
		if(pAWrite  == null) pAWrite  = Accessibility.Private;
		if(pAConfig == null) pAConfig = Accessibility.Private;
		return this.addAttrInfo(this.newAIDynamic(pARead, pAWrite, pAConfig, pVName, pTRef, pMoreData));
	}
	/** Creates a new operation info */
	protected boolean addOperDynamic(Accessibility pAccess, ExecSignature pES, MoreData pMoreData) {
		if(!this.isElementAppendable()) return false;
		if(pES == null)                 return false;
		if(this.isOperExist(pES))       return false;
		if(pAccess == null) pAccess = Accessibility.Private;
		return this.addOperInfo(this.newOIDynamic(pAccess, pES, pMoreData));
	}
	// Field --------------------------------------------------------
	/** Creates a new attribute info */
	protected boolean addAttrDlgAttr(Accessibility pARead, Accessibility pAWrite, Accessibility pAConfig, String pVName,
			String pDlgAttr, MoreData pMoreData) {
		if(!this.isElementAppendable()) return false;
		if(pVName == null)              return false;
		if(this.isAttrExist(pVName))    return false;
		return this.addAttrInfo(this.newAIDlgAttr(pARead, pAWrite, pAConfig, pVName, pDlgAttr, pMoreData));
	}
	/** Creates a new operation info */
	protected boolean addOperDlgAttr(Accessibility pAccess, ExecSignature pES, String pDlgAttr, MoreData pMoreData) {
		if(!this.isElementAppendable())  return false;
		if(pES == null)           return false;
		if(this.isOperExist(pES)) return false;
		return this.addOperInfo(this.newOIDlgAttr(pAccess, pES, pDlgAttr, pMoreData));
	}
	// Object --------------------------------------------------------
	/** Creates a new attribute info */
	protected boolean addAttrDlgObject(Accessibility pARead, Accessibility pAWrite, Accessibility pAConfig,
			String pVName, boolean pIsNotNull, Object pDlgObject, MoreData pMoreData) {
		if(!this.isElementAppendable()) return false;
		if(pVName == null)              return false;
		if(this.isAttrExist(pVName))    return false;
		return this.addAttrInfo(this.newAIDlgObject(pARead, pAWrite, pAConfig, pVName, pIsNotNull, pDlgObject,
				pMoreData));
	}
	/** Creates a new operation info */
	protected boolean addOperDlgObject(Accessibility pAccess, ExecSignature pES, Object pDlgObject, MoreData pMoreData) {
		if(!this.isElementAppendable()) return false;
		if(pES == null)                 return false;
		if(this.isOperExist(pES))       return false;
		return this.addOperInfo(this.newOIDlgObject(pAccess, pES, pDlgObject, pMoreData));
	}

	// Direct --------------------------------------------------------
	protected boolean addConstant(String pVName, boolean pIsNotNull, Serializable pValue, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		if(!this.isElementAppendable()) return false;
		return this.addConstant(Accessibility.Public, pVName, pIsNotNull, pValue, IsValueExpr,
				pMoreInfo, pLocation, pMoreData);
	}
	protected boolean addConstant(Accessibility pAccess, String pVName, boolean pIsNotNull, Serializable pValue,
			boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		if(!this.isElementAppendable())     return false;
		if(pVName == null)           return false;
		if(this.isAttrExist(pVName)) return false;
		DataHolderInfo DHI = new DataHolderInfo(
		                             TKJava.TAny.getTypeRef(),
		                             pValue, Variable.FactoryName,
		                             true, false, true, IsValueExpr, pMoreInfo);
		return this.addDataHolder(pAccess, pAccess, pAccess, pVName, pIsNotNull, DHI, pLocation, pMoreData);
	}
	protected boolean addVariable(String pVName, boolean pIsNotNull, TypeRef pTypeRef,
			Serializable pValue, boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		return this.addVariable(Accessibility.Public, pVName, pIsNotNull, pTypeRef, pValue,
				IsValueExpr, pMoreInfo, pLocation, pMoreData);
	}
	protected boolean addVariable(Accessibility pAccess, String pVName, boolean pIsNotNull, TypeRef pTypeRef,
			Serializable pValue, boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		if(!this.isElementAppendable()) return false;
		if(pVName == null)              return false;
		if(this.isAttrExist(pVName))    return false;
		if(pTypeRef == null) throw new NullPointerException("Variable type must not be null. <StackOwner_Simple:357>");
		DataHolderInfo DHI = new DataHolderInfo(pTypeRef, pValue, Variable.FactoryName,
		                            true, true, true, IsValueExpr, pMoreInfo);
		return this.addDataHolder(pAccess, pAccess, pAccess, pVName, pIsNotNull, DHI, pLocation, pMoreData);
	}
	protected boolean addVariable(Accessibility pARead, Accessibility pAWrite, Accessibility pAConfig, String pVName,
			boolean pIsNotNull, TypeRef pTypeRef, Serializable pValue, boolean IsValueExpr, MoreData pMoreInfo,
			Location pLocation, MoreData pMoreData) {
		if(!this.isElementAppendable()) return false;
		if(pVName == null)              return false;
		if(this.isAttrExist(pVName))    return false;
		if(pTypeRef == null) throw new NullPointerException("Variable type must not be null. <StackOwner_Simple:367>");
		DataHolderInfo DHI = new DataHolderInfo(pTypeRef, pValue, Variable.FactoryName,
		                            true, true, true, IsValueExpr, pMoreInfo);
		
		return this.addDataHolder(pARead, pAWrite, pAConfig, pVName, pIsNotNull, DHI, pLocation,
				pMoreData);
	}
	protected boolean addDataHolder(Accessibility pARead, Accessibility pAWrite, Accessibility pAConfig, String pVName,
			boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData) {
		if(!this.isElementAppendable())     return false;
		if(pVName == null)           return false;
		if(this.isAttrExist(pVName)) return false;
		return this.addAttrInfo(this.newAIDirect(pARead, pAWrite, pAConfig, pVName, pIsNotNull, pDHI, pLocation,
				pMoreData));
	}
	
	protected boolean bindDataHolder(Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, boolean pIsNotNull, DataHolder DH,
			Location pLocation, MoreData pMoreData) {
		
		if(!this.isElementAppendable())     return false;
		if(pVName == null)           return false;
		if(this.isAttrExist(pVName)) return false;

		AttributeInfo NewAI = this.newAIDirect(pARead, pAWrite, pAConfig, pVName, pIsNotNull, null, pLocation,
				pMoreData);
		if(!this.addAttrInfo(NewAI)) return false;
		this.ensureDHSpace(null);
		this.Attrs[((AttributeInfo.AIDirect)NewAI).DHIndex] = DH;
		return true;
	}
	
	protected boolean addOperation(Accessibility pAccess, Executable pExec, MoreData pMoreData) {
		if(!this.isElementAppendable())                   return false;
		if(pExec == null)                          return false;
		if(pExec instanceof OperationInfo)         return false;
		if(StandaloneOperation.isThereClosure(pExec))          return false;
		if(this.isOperExist(pExec.getSignature())) return false;
		return this.addOperInfo(this.newOIDirect(pAccess, pExec, pMoreData));
	}
	
	protected boolean addAbstractOperation(Accessibility pAccess, ExecSignature pSignature, Executable.ExecKind pKind,
			MoreData pMoreData) {
		if(!this.isElementAppendable())  return false;
		if(pSignature == null)           return false;
		if(this.isOperExist(pSignature)) return false;
		return this.addOperInfo(this.newOIDirect(pAccess, pSignature, pKind, pMoreData));
	}
}
