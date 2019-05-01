/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's Curry.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.curry;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;

/**
 * Abstract class for StackOwner builder (such as package and type)
 * @author Nawapunth Manusitthipol
 */
abstract public class StackOwnerBuilder implements StackOwnerBuilderEncloseObject {
	
	/** Constructs a package builder */
	StackOwnerBuilder(String pName) {
		this.Name = pName;
	}
	
	String Name;
	
	/** Returns the name of that this builder */
	final public String getName() {
		return this.Name;
	}
   	
   	/** Returns the Engine for this StackOwnerBuilder */ @Override
   	abstract public Engine getEngine();
   	
   	// Parent and package ----------------------------------------------------------------------------------------------
	
	/** Returns the parent StackOwner builder that this stackowner builder is attaching on */
	abstract public StackOwnerBuilderEncloseObject getStackOwnerBuilderEncloseObject();

	/** Returns the name of package this type is in */ @Override
	public String getPackageName() {
		PackageBuilder PB = this.getPackageBuilder();
		if(PB != null) return PB.getPackageName();
		return null;
	}
	
	/** Returns the package builder that this stackowner builder is attaching on */
	public PackageBuilder getPackageBuilder() {
		if(this instanceof PackageBuilder) return (PackageBuilder)this;
		
		String PName = this.getPackageName();
		if(PName == null) return null;
		
		StackOwnerBuilderEncloseObject SOBEO = this.getStackOwnerBuilderEncloseObject();
		if(SOBEO != null) return this.getEngine().getUnitManager().getPackageBuilder(null, this.getPackageName());
		return null;
	}
	
   	// Activeness ------------------------------------------------------------------------------------------------------

    private boolean IsActive = true;
    
   	/** Checks if this package builder is still active (have not saved yet) */ @Override
   	final public boolean isActive() {
		StackOwnerBuilderEncloseObject SOBEO = this.getStackOwnerBuilderEncloseObject();
		return (SOBEO == null)?this.IsActive:SOBEO.isActive();
	}
   	
	/** Make all type builders that this StackOwnerBuilder owns to in-active */
	final protected void toInactive() {
		if(!this.IsActive) return;
		
		this.doJustBeforeToInactive();

		this.IsActive = false;		
		if(this.TypeBuilders != null) {
			for(String TheName : this.TypeBuilders.keySet()) {
				this.TypeBuilders.get(TheName).toInactive();
			}
		}
		
		this.doJustAfterToInactive();
	}

	/** This method will be run just after all Sub-TypeBuilder is in-actived */
	protected void doJustBeforeToInactive() {}

	/** This method will be run just after all Sub-TypeBuilder is in-actived */
	protected void doJustAfterToInactive() {}
   	
   	/** Checks if the ID is a valid ID */
   	public boolean isValidID(Object pID) {
		StackOwnerBuilderEncloseObject SOBEO = this.getStackOwnerBuilderEncloseObject();
		return (SOBEO == null)?true:SOBEO.isValidID(pID);
   	}
	
   	// The elements ----------------------------------------------------------------------------------------------------
   	
   	HashSet<AttributeInfo> AttrInfos = null;
	HashSet<OperationInfo> OperInfos = null;
	HashSet<TypeSpec>      TypeInfos = null;
	
	protected HashSet<AttributeInfo> getAttrInfos() {
		if(this.AttrInfos == null) this.AttrInfos = new HashSet<AttributeInfo>();
		return this.AttrInfos;
	}
	protected HashSet<OperationInfo> getOperInfos() {
		if(this.OperInfos == null) this.OperInfos = new HashSet<OperationInfo>();
		return this.OperInfos;
	}
	protected HashSet<TypeSpec> getTypeInfos() {
		if(this.TypeInfos == null) this.TypeInfos = new HashSet<TypeSpec>();
		return this.TypeInfos;
	}

	/** Returns the set of type specs */
	@SuppressWarnings("unchecked")
	public HashSet<TypeSpec> getTypeSpecs() {
		if(this.TypeInfos == null) return new HashSet<TypeSpec>();
		return (HashSet<TypeSpec>)this.TypeInfos.clone();
	}

	// Add Elements --------------------------------------------------------------------------------

	// Dynamic --------------------------------------------------------
	/** Creates a new attribute info */
	final public void addAttrDynamic(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
			String pAName, TypeRef pTRef, MoreData pMoreData) {
		if(!this.isActive())         throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pAName == null)           throw new NullPointerException();
		if(this.isAttrExist(pAName)) throw new IllegalArgumentException("The attribute with the same signature is already exist ("+pAName+" in "+this.getName()+").");
		
		this.getAttrInfos().add(new AttributeInfo.AIDynamic(pPARead, pPAWrite, pPAConfig, pAName, pTRef, pMoreData));
	}
	/** Creates a new operation info */
	final public void addOperDynamic(Accessibility pPAccess, ExecSignature pES, MoreData pMoreData) {
		if(!this.isActive())      throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pES == null)           throw new NullPointerException();
		if(this.isOperExist(pES)) throw new IllegalArgumentException("The operation with the same signature is already exist ("+pES.toString()+" in "+this.getName()+").");
				
		this.getOperInfos().add(new OperationInfo.OIDynamic(pPAccess, pES, pMoreData));
	}
	// Field --------------------------------------------------------
	/** Creates a new attribute info */
	final public Object addAttrDlgAttr(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
			String pAName, String pTName, MoreData pMoreData) {
		if(!this.isActive()) throw new RuntimeException(String.format("The builder is inactive (%s).", this.getName()));
		if(pAName == null)   throw new NullPointerException();
		if(this.isAttrExist(pAName))
			throw new IllegalArgumentException(String.format(
					"The attribute with the same signature is already exist (%s in %s).", pAName, this.getName()));
		
		this.getAttrInfos().add(new AttributeInfo.AIDlgAttr(pPARead, pPAWrite, pPAConfig, pAName, pTName, pMoreData));
		return true;
	}
	/** Creates a new operation info */
	final public Object addOperDlgAttr(Accessibility pPAccess, ExecSignature pES, String pTName, MoreData pMoreData) {
		if(!this.isActive())      throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pES == null)           throw new NullPointerException();
		if(this.isOperExist(pES)) throw new IllegalArgumentException("The operation with the same signature is already exist ("+pES.toString()+" in "+this.getName()+").");
		
		this.getOperInfos().add(new OperationInfo.OIDlgAttr(pPAccess, pES, pTName, pMoreData));
		return true;
	}
	
	// Direct ----------------------------------------------------------------------------------------------------------
	
	static final class TempAIDirectEntry {
		TempAIDirectEntry(StackOwnerBuilder pSOB, AttributeInfo.AIDirect pAID, Object pTempData) {
			this.SOB      = pSOB;
			this.AID      = pAID;
			this.TempData = pTempData;
			if(this.SOB == null)     throw new NullPointerException(    "The given StackOwnerBuilder is null.");
			if(!this.SOB.isActive()) throw new IllegalArgumentException("The given StackOwnerBuilder is no longer active.");
		}
		
		StackOwnerBuilder      SOB;
		AttributeInfo.AIDirect AID;
		Object                 TempData;
		
		Object getTempData() {
			return this.TempData;
		}
		boolean resolve(Engine $Engine, Object pID, TypeRef pDValueTypeRef, Serializable pDValue) {
			if(!this.SOB.isValidID(pID)) return false;
			DataHolderInfo DHI = AID.DHInfo;
			if(DHI == null) return false;
			AID.DHInfo  = DHI.resolve($Engine, pDValueTypeRef, pDValue);
			AID.TypeRef = AID.DHInfo.getTypeRef();
			return true;
		}
		
		// Reset -------------------------------------------------------------------
		
		/** Resets TypeRefs and TypeSpecs for the compilation */
		final protected void resetForCompilation() {
			Util.ResetAttributeInfo(this.AID);
		}
	}
	
	static final class TempOIDirectEntry {
		TempOIDirectEntry(StackOwnerBuilder pSOB, OperationInfo.OIDirect pOID, Object pTempData) {
			this.SOB      = pSOB;
			this.OID      = pOID;
			this.TempData = pTempData;
			if(this.SOB == null)     throw new NullPointerException(    "The given StackOwnerBuilder is null.");
			if(!this.SOB.isActive()) throw new IllegalArgumentException("The given StackOwnerBuilder is no longer active.");
		}
		
		StackOwnerBuilder      SOB;
		OperationInfo.OIDirect OID;
		Object                 TempData;
		
		Object getTempData() {
			return this.TempData;
		}
		boolean resolve(Object pID, Expression pBody) {
			if(!this.SOB.isValidID(pID)) return false;
			Executable Exec = this.OID.getDeclaredExecutable();
			if(!(Exec instanceof CurryExecutable)) return false;
			((CurryExecutable)Exec).Body = pBody;
			return true;
		}
		boolean resolve(Object pID, ExternalExecutor pEE, Object pEEID, Object pEESC) {
			if(!this.SOB.isValidID(pID)) return false;
			Executable Exec = this.OID.getDeclaredExecutable();
			if(!(Exec instanceof ExternalExecutable)) return false;
			((ExternalExecutable)Exec).EE = pEE;
			((ExternalExecutable)Exec).ID = pEEID;
			((ExternalExecutable)Exec).SC = pEESC;
			return true;
		}
		boolean resolve(Object pID, Executable pExec) {
			if(!this.SOB.isValidID(pID)) return false;
			if((pExec == null) || StandaloneOperation.isThereClosure(pExec))           return false;
			if(!this.OID.getSignature().equals(pExec.getSignature())) return false;
			this.OID.setDeclaredExecutable(pExec);
			return true;
		}
		
		// Reset -------------------------------------------------------------------
		
		/** Resets TypeRefs and TypeSpecs for the compilation */
		final protected void resetForCompilation() {
			Util.ResetOperationInfo(this.OID);
		}
	}
	
	Hashtable<String,        TempAIDirectEntry> TempAttrs = null;
	Hashtable<ExecSignature, TempOIDirectEntry> TempOpers = null;
	
	// Attributes -----------------------------------------------------------------------
	
	/** Returns the names of the temp attribute */
	final public Set<String> getTempAttrNames() {
		return (this.TempAttrs == null)?null:this.TempAttrs.keySet();
	}
	/** Returns the temp-data of the temporary attribute */
	final public Object getTempAttrTempData(String pName) {
		if(this.TempAttrs == null) return null;
		TempAIDirectEntry TAIDE = this.TempAttrs.get(pName);
		if(TAIDE == null) return null;
		return TAIDE.getTempData();
	}
	/** Resolve the temporary attribute */
	final public boolean resolveTempAttr(String pName, Object pID, TypeRef pDValueTypeRef, Serializable pDValue) {
		if(this.TempAttrs == null) return false;
		TempAIDirectEntry TAIDE = this.TempAttrs.get(pName);
		if(TAIDE == null) return false;
		
		boolean Result = TAIDE.resolve(this.getEngine(), pID, pDValueTypeRef, pDValue);
		if(Result) this.TempAttrs.remove(TAIDE);
		return Result;
	}
	/** Resolve the temporary attribute */
	final public AttributeInfo getTempAttrAttributeInfo(String pName) {
		if(this.TempAttrs == null) return null;
		TempAIDirectEntry TAIDE = this.TempAttrs.get(pName);
		if(TAIDE == null) return null;
		return TAIDE.AID;
	}
	/** Resolve the temporary attribute */
	final public MoreData getTempAttrMoreData(String pName) {
		if(this.TempAttrs == null) return null;
		TempAIDirectEntry TAIDE = this.TempAttrs.get(pName);
		if(TAIDE == null) return null;
		return TAIDE.AID.MoreData;
	}
	/** Resolve the temporary attribute */
	final public Location getTempAttrLocation(String pName) {
		if(this.TempAttrs == null) return null;
		TempAIDirectEntry TAIDE = this.TempAttrs.get(pName);
		if(TAIDE == null) return null;
		return TAIDE.AID.Location;
	}
	
	// Operations -----------------------------------------------------------------------
	
	/** Returns the signatures of the temp operation */
	final public Set<ExecSignature> getTempOperSignatures() {
		return (this.TempOpers == null)?null:this.TempOpers.keySet();
	}
	/** Returns the temp-data of the temporary operation */
	final public Object getTempOperTempData(ExecSignature pES) {
		if(this.TempOpers == null) return null;
		TempOIDirectEntry TOIDE = this.TempOpers.get(pES);
		if(TOIDE == null) return null;
		return TOIDE.getTempData();
	}
	final public boolean resolveTempOper(ExecSignature pES, Object pID, Expression pBody) {
		if(this.TempOpers == null) return false;
		TempOIDirectEntry TOIDE = this.TempOpers.get(pES);
		if(TOIDE == null) return false;
		boolean Result = TOIDE.resolve(pID, pBody);
		if(Result) this.TempOpers.remove(TOIDE);
		return Result;
	}
	final public boolean resolveTempOper(ExecSignature pES, Object pID, ExternalExecutor pEE, Object pEEID, Object pEESC) {
		if(this.TempOpers == null) return false;
		TempOIDirectEntry TOIDE = this.TempOpers.get(pES);
		if(TOIDE == null) return false;
		boolean Result = TOIDE.resolve(pID, pEE, pEEID, pEESC);
		if(Result) this.TempOpers.remove(TOIDE);
		return Result;
	}
	final public boolean resolveTempOper(ExecSignature pES, Object pID, Executable pExec) {
		if(this.TempOpers == null) return false;
		TempOIDirectEntry TOIDE = this.TempOpers.get(pES);
		if(TOIDE == null) return false;
		boolean Result = TOIDE.resolve(pID, pExec);
		if(Result) this.TempOpers.remove(TOIDE);
		return Result;
	}
	/** Resolve the temporary attribute */
	final public MoreData getTempOperExtraData(ExecSignature pES) {
		if(this.TempOpers == null) return null;
		TempOIDirectEntry TOIDE = this.TempOpers.get(pES);
		if(TOIDE == null) return null;
		return TOIDE.OID.getSignature().getExtraData();
	}
	/** Resolve the temporary attribute */
	final public Location getTempOperLocation(ExecSignature pES) {
		if(this.TempOpers == null) return null;
		TempOIDirectEntry TOIDE = this.TempOpers.get(pES);
		if(TOIDE == null) return null;
		return TOIDE.OID.getSignature().getLocation();
	}
	
	// Direct not temp -------------------------------------------------------------------------------------------------

	final public Object addAttrConst(Accessibility PARead, String pVName, boolean pIsNotNull, Serializable pValue,
			boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		return this.addAttrConst(PARead, pVName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, null, false);
	}
	final public Object addAttrConst(String pVName, boolean pIsNotNull, Serializable pValue, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		return this.addAttrConst(Accessibility.Public, pVName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, null, false);
	}
	final public Object addAttrDirect(String pVName, boolean pIsNotNull, TypeRef pTypeRef, Serializable pValue,
			boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		return this.addAttrDirect(pVName, pIsNotNull, pTypeRef, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, null, false);
	}
	final public Object addAttrDirect(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig, String pAName,
			boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData) {
		return this.addAttrDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, pMoreData, null, false);
	}
	
	final public Object addOperDirect(Accessibility pPAccess, Executable pExec, MoreData pMoreData) {
		return this.addOperDirect(pPAccess, pExec, pMoreData, null, false);
	}
	
	// Abstract --------------------------------------------------------------------------------------------------------
	
	final public Object addAbstractAttrDirect(String pAName, boolean pIsNotNull, TypeRef pTypeRef, MoreData pMoreInfo,
			Location pLocation, MoreData pMoreData) {
		return this.addAbstractAttrDirect(null, pAName, pIsNotNull, pTypeRef, pMoreInfo, pLocation, pMoreData);
	}
	
	final public Object addAbstractAttrDirect(Accessibility Access, String pAName, boolean pIsNotNull, TypeRef pTypeRef,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		
		if(!this.isActive())         throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pAName   == null)         throw new NullPointerException();
		if(pTypeRef == null)         throw new NullPointerException();
		if(this.isAttrExist(pAName)) throw new IllegalArgumentException("The attribute with the same signature is already exist ("+pAName+" in "+this.getName()+").");

		DataHolderInfo DHI = new DataHolderInfo(pTypeRef, null, null, true, true, false, false, pMoreInfo);
		if(Access == null) Access = Accessibility.Public;
		
		AttributeInfo.AIDirect AID = new AttributeInfo.AIDirect(Access, Access, Access, pAName, pIsNotNull, DHI, pLocation, pMoreData);
		this.getAttrInfos().add(AID);
		return null;
	}
	final public Object addAbstractAttrDirect(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
			String pAName, boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData) {		
		if(!this.isActive())                throw new RuntimeException("The builder is inactive ("+this.getName()+"."+pAName+").");
		if(pAName == null)                  throw new NullPointerException();
		if(pDHI   == null)                  throw new NullPointerException();
		if(pDHI.getDHFactoryName() != null) throw new IllegalArgumentException("Abstract attribute must have dataholder factory as null ("+this.getName()+"."+pAName+").");
		if(this.isAttrExist(pAName)) throw new IllegalArgumentException("The attribute with the same signature is already exist ("+this.getName()+"."+pAName+").");
		
		AttributeInfo.AIDirect AID = new AttributeInfo.AIDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI,
											pLocation, pMoreData);
		this.getAttrInfos().add(AID);
		return null;
	}
	
	final public Object addAbstractOperDirect(Accessibility pPAccess, ExecSignature pSignature, Executable.ExecKind pKind, MoreData pMoreData) {
		if(!this.isActive())             throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pSignature == null)           throw new NullPointerException();
		if(this.isOperExist(pSignature)) throw new IllegalArgumentException("The operation with the same signature is already exist ("+pSignature.toString()+" in "+this.getName()+").");
		
		OperationInfo.OIDirect OID = new OperationInfo.OIDirect(pPAccess, pSignature, pKind, pMoreData);
		this.getOperInfos().add(OID);
		return null;
	}
	
	// Direct temp -----------------------------------------------------------------------------------------------------

	final public Object addTempAttrConst(Accessibility PARead, String pVName, boolean pIsNotNull, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData, Object pTempData) {
		return this.addAttrConst(PARead, pVName, pIsNotNull, null, IsValueExpr, pMoreInfo, pLocation, pMoreData, pTempData, true);
	}
	final public Object addTempAttrConst(String pVName, boolean pIsNotNull, boolean IsValueExpr, MoreData pMoreInfo,
			Location pLocation, MoreData pMoreData, Object pTempData) {
		return this.addAttrConst(Accessibility.Public, pVName, pIsNotNull, null, IsValueExpr, pMoreInfo, pLocation, pMoreData, pTempData, true);
	}
	final public Object addTempAttrDirect(String pVName, boolean pIsNotNull, TypeRef pTypeRef, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData, Object pTempData) {
		return this.addAttrDirect(pVName, pIsNotNull, pTypeRef, null, IsValueExpr, pMoreInfo, pLocation, pMoreData,
				pTempData, true);
	}
	final public Object addTempAttrDirect(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
			String pAName, boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData,
			Object pTempData) {
		return this.addAttrDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, pMoreData,
				pTempData, true);
	}
	
	final public Object addTempOperDirect(Accessibility pPAccess, Executable pESub,MoreData pMoreData,
			Object pTempData) {
		return this.addOperDirect(pPAccess, pESub, pMoreData, pTempData, true);
	}
	
	// Direct Implement ------------------------------------------------------------------------------------------------
	
	final Object addAttrConst(Accessibility PA, String pVName, boolean pIsNotNull, Serializable pValue, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData, Object pTempData, boolean pIsTemp) {
		
		if(!this.isActive())         return false;
		if(pVName == null)           return false;
		if(this.isAttrExist(pVName)) return false;
		DataHolderInfo DHI = new DataHolderInfo(null, pValue, Variable.FactoryName, true, false, true, IsValueExpr, pMoreInfo);

		return this.addAttrDirect(PA, PA, PA, pVName, pIsNotNull, DHI, pLocation, pMoreData, pTempData, pIsTemp);
	}
	final Object addAttrDirect(String pAName, boolean pIsNotNull, TypeRef pTypeRef, Serializable pValue, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData, Object pTempData, boolean pIsTemp) {
		
		if(!this.isActive())         throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pAName   == null)         throw new NullPointerException();
		if(pTypeRef == null)         throw new NullPointerException();
		if(this.isAttrExist(pAName)) throw new IllegalArgumentException("The attribute with the same signature is already exist ("+pAName+" in "+this.getName()+").");

		DataHolderInfo DHI = new DataHolderInfo(pTypeRef, pValue, Variable.FactoryName, true, true, true, IsValueExpr, pMoreInfo);
		Accessibility PA = Accessibility.Public;
		return this.addAttrDirect(PA, PA, PA, pAName, pIsNotNull, DHI, pLocation, pMoreData, pTempData, pIsTemp);
	}
	final boolean addAttrDirect(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig, String pAName,
			boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData, Object pTempData,
			boolean pIsTemp) {
		if(!this.isActive())         throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pAName == null)           throw new NullPointerException();
		if(pDHI   == null)           throw new NullPointerException();
		if(this.isAttrExist(pAName)) throw new IllegalArgumentException("The attribute with the same signature is already exist ("+pAName+" in "+this.getName()+").");
		
		AttributeInfo.AIDirect AID = new AttributeInfo.AIDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI,
											pLocation, pMoreData);
		this.getAttrInfos().add(AID);
		
		if(!pIsTemp) return true;
		
		// Add the temp to the list
		if(this.TempAttrs == null) this.TempAttrs = new Hashtable<String, StackOwnerBuilder.TempAIDirectEntry>();
		this.TempAttrs.put(pAName, new TempAIDirectEntry(this, AID, pTempData));
		return true;
		
	}
	
	final boolean addOperDirect(Accessibility pPAccess, Executable pExec, MoreData pMoreData, Object pTempData,
			boolean pIsTemp) {
		if(!this.isActive())                           throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pExec == null)                              throw new NullPointerException();
		if(pExec instanceof WrapperExecutable.Wrapper) throw new IllegalArgumentException("Wrapper cannot be used as operation ("+pExec.toString()+").");
		if(this.isOperExist(pExec.getSignature()))     throw new IllegalArgumentException("The operation with the same signature is already exist ("+pExec.toString()+" in "+this.getName()+").");
		
		OperationInfo.OIDirect OID = new OperationInfo.OIDirect(pPAccess, pExec, pMoreData);
		this.getOperInfos().add(OID);
		
		if(!pIsTemp) return true;
		
		// Add the temp to the list
		if(this.TempOpers == null) this.TempOpers = new Hashtable<ExecSignature, StackOwnerBuilder.TempOIDirectEntry>();
		this.TempOpers.put(pExec.getSignature(), new TempOIDirectEntry(this, OID, pTempData));
		return true;
	}
	
	// Type ------------------------------------------------------------------------------------------------------------
	
	/**
	 * Add a package type to this package.<br />
	 * @param pPAccess is the accessibility of this function.
	 * @param pTName   is the name of the type as it to be refereed to.
	 * @param TS       is the type spec of the type. The type ref must be given as null or a
	 *                      no-name ref.
	 * @return `true` if adding success or `false` if failed.
	 **/
	final public boolean addType(Accessibility pPAccess, TypeSpec TS, Location pLocation) {
		if(!this.isActive()) return false;
		if(TS     == null)   return false;
		
		TREnclosed TheValidTypeRef = this.ensureTypeRefValid(pPAccess, TS, pLocation);
		if(TheValidTypeRef == null)                         return false;
		if(this.isTypeExist(TheValidTypeRef.getTypeName())) return false;
		
		TS.Ref = TheValidTypeRef;
		
		// Marked as no more changed
		TS.IsSaved = true;
		
		this.getTypeInfos().add(TS);
		return true;
	}
	
	Hashtable<String, TypeBuilder> TypeBuilders = null;
	
	/** Returns the name of all the type builder held by this builder */
	final public Set<String> getTypeBuilderNames() {
		if(this.TypeBuilders == null) return null;
		return this.TypeBuilders.keySet();
	}
	
	final public TypeBuilder getTypeBuilder(String pName) {
		if(this.TypeBuilders == null) return null;
		return this.TypeBuilders.get(pName);
	}
	
	/** Create and add a type builder */
	final public TypeBuilder newTypeBuilder(Accessibility pPAccess, TypeSpec TS, Location pLocation) {
		if(!this.isActive()) return null;
		if(TS     == null)   return null;
		
		TypeKind TK = this.getEngine().getTypeManager().getTypeKind(TS.getKindName());
		if(TK == null) return null;
		
		TREnclosed TheValidTypeRef = this.ensureTypeRefValid(pPAccess, TS, pLocation);
		if(TheValidTypeRef == null)                         return null;
		if(this.isTypeExist(TheValidTypeRef.getTypeName())) return null;
		
		TS.Ref = TheValidTypeRef;
		
		// Add the type spec
		this.getTypeInfos().add(TS);
		
		// Create the type builder and remember it
		TypeBuilder TB = TK.newTypeBuilder(TS, pPAccess, pLocation, this);
		if(this.TypeBuilders == null) this.TypeBuilders = new Hashtable<String, TypeBuilder>();
		this.TypeBuilders.put(TheValidTypeRef.getTypeName(), TB);
		return TB;
	}
	
	// Replace the type spec -------------------------------------------------------------------------------------------

	final public boolean replaceTypeSpec(Object pSecretID, Accessibility pPAccess, TypeSpec TS,
			Location pLocation) {
		if(!this.isActive())           return false;
		if(TS     == null)             return false;
		if(!this.isValidID(pSecretID)) return false;

		TREnclosed TheValidTypeRef = this.ensureTypeRefValid(pPAccess, TS, pLocation);
		if(TheValidTypeRef == null)                          return false;
		if(!this.isTypeExist(TheValidTypeRef.getTypeName())) return false;
		
		String TName = TheValidTypeRef.getTypeName();

		// Remove the old one (this is done by copy all that is not the same type name)
		HashSet<TypeSpec> NewTSpecs = new HashSet<TypeSpec>();
		Iterator<TypeSpec> I = this.getTypeInfos().iterator();
		while(I.hasNext()) {
			TypeSpec aTS = I.next();
			if(TName.equals(((TLPackage.TRPackage)aTS.getTypeRef()).TName)) continue;
			NewTSpecs.add(aTS);
		}
		this.TypeInfos = NewTSpecs;
		
		// Add this one
		this.getTypeInfos().add(TS);
		
		TypeBuilder TB = this.getTypeBuilder(TName);
		if(TB != null) {
			TS.Ref   = TB.TSpec.Ref;
			TB.TSpec = TS;
		}
		
		return true;
	}
	
	// Artifacts ---------------------------------------------------------------
	
	/** Checks if the package variable `pVName` exist. */
	final public boolean isAttrExist(String pVName) {
		if(pVName == null) return false;
		for(AttributeInfo PVI : this.getAttrInfos()) {
			if(pVName.equals(PVI.getName())) return true;
		}
		return false;
	}
	/** Checks if the function `pES` exist. */
	final public boolean isOperExist(ExecSignature pES) {
		if(pES == null) return false;
		for(OperationInfo FI : this.getOperInfos()) {
			if(pES.isAllParameterTypeEquals(FI.getSignature())) return true;
		}
		return false;
	}
	/** Checks if the package type `pTName` exist. */
	final public boolean isTypeExist(String pTName) {
		if(pTName == null) return false;
		for(TypeSpec TS : this.getTypeInfos()) {
			if(!pTName.equals(((TLPackage.TRPackage)TS.getTypeRef()).TName)) continue;
			return true;
		}
		return false;
	}
	
	// Reset -------------------------------------------------------------------

	/** Resets an iterable of AttributeInfos */
	final static public void ResetTempAttributeInfos(Hashtable<String, TempAIDirectEntry> TAIDEs) {
		if(TAIDEs == null) return;
		for(String Name : TAIDEs.keySet()) {
			TempAIDirectEntry TAIDE = TAIDEs.get(Name);
			if(TAIDE != null) TAIDE.resetForCompilation();
		}
	}
	/** Resets an iterable of OperationInfos */
	final static public void ResetTempOperationInfos(Hashtable<ExecSignature, TempOIDirectEntry> TOIDEs) {
		if(TOIDEs == null) return;
		for(ExecSignature Signature : TOIDEs.keySet()) {
			if(Signature != null) Util.ResetExecSignature(Signature);
			TempOIDirectEntry TOIDE = TOIDEs.get(Signature);
			if(TOIDE     != null) TOIDE.resetForCompilation();
		}
	}

	/** Resets an iterable of TypeBuilders */
	final static public void ResetTypeBuilders(Hashtable<String, TypeBuilder> TBs) {
		if(TBs == null) return;
		for(String TName : TBs.keySet()) {
			TypeBuilder TB;
			if((TB = TBs.get(TName)) == null) continue;
			TB.resetForCompilation();
		}
	}
	/** Resets an iterable of TypeInfos */
	final static public void ResetTypeInfos(HashSet<TypeSpec> TSs) {
		if(TSs == null) return;
		for(TypeSpec TS : TSs) {
			if(TS == null) continue;
			TS.resetForCompilation();
		}
	}
	
	/** Resets TypeRefs and TypeSpecs for the compilation */
	protected void resetBuilderCompilation() {}
	
	/** Resets TypeRefs and TypeSpecs for the compilation */
	final public void resetForCompilation() {
		Util.ResetAttributeInfos(this.AttrInfos);
		Util.ResetOperationInfos(this.OperInfos);
		ResetTempAttributeInfos( this.TempAttrs);
		ResetTempOperationInfos( this.TempOpers);
		ResetTypeBuilders(       this.TypeBuilders);
		ResetTypeInfos(          this.TypeInfos);
		this.resetBuilderCompilation();
	}

	// Lock --------------------------------------------------------------------
	
	/** This method will help limiting the implementation of this interface to be within this package. */
	public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
		return pLocalInterface;
	}
}
