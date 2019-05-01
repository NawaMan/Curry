/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
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
import java.util.Set;
import java.util.Vector;

import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;

/**
 * TypeBuilder is a builder of type where developers can add detail of type before it is built.
 * TypeBuilder produce type spec.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class TypeBuilder extends StackOwnerBuilder {
	
	protected TypeBuilder(TypeSpec pTSpec, Accessibility pAccess, Location pLocation,
			StackOwnerBuilderEncloseObject pEncloseObject) {
		
		super(((TREnclosed)pTSpec.getTypeRef()).getTypeName());
		if(pEncloseObject == null) throw new NullPointerException("A type must be enclosed by something.");

		Engine $Engine     = pEncloseObject.getEngine();
		this.EncloseObject = pEncloseObject;
		this.TKind         = $Engine.getTypeManager().getTypeKind(pTSpec.getKindName());
		this.Access        = (pAccess == null)?Accessibility.Private:pAccess;
		this.Location      = pLocation;
		this.TSpec         = pTSpec;
		this.TSpec.Ref     = ((TREnclosed)this.TSpec.Ref).newInternalTypeRef($Engine, this.Access, this.Location);
	}
	
	final StackOwnerBuilderEncloseObject EncloseObject;
	final TypeKind                       TKind;
	final Accessibility                  Access;
	final Location                       Location;

	TypeSpec TSpec;
	
	/**{@inheritDoc}*/ @Override
	final public StackOwnerBuilderEncloseObject getStackOwnerBuilderEncloseObject() {
		return this.EncloseObject;
	}

	/**{@inheritDoc}*/ @Override
   	final public Engine getEngine() {
   		return this.EncloseObject.getEngine();
   	}
	
	/**{@inheritDoc}*/ @Override
	final public String toString() {
		return "Type " + this.getTypeRef().toString();
	}
	
	/** Returns the type spec */
	final public TypeSpec getSpec() {
		return this.TSpec;
	}

	/**{@inheritDoc}*/ @Override
	public PackageBuilder getPackageBuilder() {
		if(this.EncloseObject instanceof PackageBuilder)
			return (PackageBuilder)this.EncloseObject;

		if(this.EncloseObject instanceof StackOwnerBuilder)
			return ((StackOwnerBuilder)this).getPackageBuilder();
		
		return null;
	}
	
	// StackOwnerBuilderEncloseObject ----------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public TREnclosed ensureTypeRefValid(Accessibility pPAccess, TypeSpec TS, Location pLocation) {
		// It cannot hold a type directly, NOT YET!!!
		return null;
	}
	
	// Activeness ------------------------------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected void doJustAfterToInactive() {
		super.toInactive();
		
		// Update the TSpec
		
		// Constructor
		if(this.TSpec.getConstructorInfoIndex() != -1) {
			Vector<ConstructorInfo> CIs = this.TSpec.getConstructorInfo();
			try { for(ConstructorInfo CI : this.getConstructorInfos()) CIs.add(CI); }
			catch (RuntimeException E) {
				if((E.getMessage() == null) || !E.getMessage().contains("does not accept static constructors")) throw E;
			}
		}
		// Non-static
		if(this.TSpec.getDataAttributeInfoIndex() != -1) {
			Vector<AttributeInfo> AIs = this.TSpec.getDataAttributeInfo();
			try { for(AttributeInfo AI : this.getAttrInfos()) AIs.add(AI); }
			catch (RuntimeException E) {
				if((E.getMessage() == null) || !E.getMessage().contains("does not accept attribute")) throw E;
			}
		}
		if(this.TSpec.getDataOperationInfoIndex() != -1) {
			Vector<OperationInfo> OIs = this.TSpec.getDataOperationInfo();
			try { for(OperationInfo OI : this.getOperInfos()) OIs.add(OI); }
			catch (RuntimeException E) {
				if((E.getMessage() == null) || !E.getMessage().contains("does not accept operation")) throw E;
			}
		}
		// Static
		if(this.TSpec.getTypeAttributeInfoIndex() != -1) {
			Vector<AttributeInfo> SAIs = this.TSpec.getTypeAttributeInfo();
			try { for(AttributeInfo AI : this.getStaticAttrInfos()) SAIs.add(AI); }
			catch (RuntimeException E) {
				if((E.getMessage() == null) || !E.getMessage().contains("does not accept static attribute")) throw E;
			}
		}
		if(this.TSpec.getTypeOperationInfoIndex() != -1) {
			Vector<OperationInfo> SOIs = this.TSpec.getTypeOperationInfo();
			try { for(OperationInfo OI : this.getStaticOperInfos()) SOIs.add(OI); }
			catch (RuntimeException E) {
				if((E.getMessage() == null) || !E.getMessage().contains("does not accept static operation")) throw E;
			}
		}
		
		// Freeze MoreData and ExtraInfo
		MoreData MD = this.TSpec.getMoreData();
		MoreData EI = this.TSpec.getExtraInfo();
		if(MD != null) MD.toFreeze();
		if(EI != null) EI.toFreeze();
		
		// Clear all the Parametered TypeSpec
		if(this.TSpec.ParameteredSpecs != null)
			this.TSpec.ParameteredSpecs.clear();
		
		// Marked as no more changed
		this.TSpec.IsSaved = true;
	}
   	
   	// Elements --------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected HashSet<AttributeInfo> getAttrInfos() {
		if(this.TSpec.getDataAttributeInfo() == null)
			throw new RuntimeException("This type " + this.getName() + " of the kind " + this.TSpec.getKindName() +
					" does not accept attribute.");
		return super.getAttrInfos();
	}
	/**{@inheritDoc}*/ @Override
	protected HashSet<OperationInfo> getOperInfos() {
		if(this.TSpec.getDataOperationInfo() == null)
			throw new RuntimeException("This type " + this.getName() + " of the kind " + this.TSpec.getKindName() +
					" does not accept operation.");
		return super.getOperInfos();
	}
	/**{@inheritDoc}*/ @Override
	protected HashSet<TypeSpec> getTypeInfos() {
		throw new RuntimeException("This type " + this.getName() + " of the kind " + this.TSpec.getKindName() +
				"does not accept sub type.");
	}
	
	/** Returns the type ref */       final public    TypeRef       getTypeRef()      { return this.TSpec.Ref; }
	/** Returns the type spec */      final protected TypeSpec      getTypeSpec()     { return this.TSpec; }
	/** Returns the type kind */      final public    TypeKind      getTypeKind()     { return this.TKind; }
	/** Returns the type kind name */ final public    String        getTypeKindName() { return (this.TKind == null)?null:this.TKind.getKindName(); }
	/** Returns the accessibility */  final public    Accessibility getAccessiblity() { return this.Access;   }
	/** Returns the location */       final public    Location      getLocation()     { return this.Location; }
		
	/** Returns the number of data slot of the type-spec data */
	final protected int getTSpecDataCount() {
		return this.TSpec.getDataCount();
	}
	
	/** Returns the number of data slot of the type-spec data */
	final protected Serializable getTSpecDataAt(int pPos) {
		return this.TSpec.getData(pPos);
	}
	
	final protected boolean setTSpecDataAt(int pPos, Serializable pValue) {
		if(!this.isActive())
			return false;
		if((pPos < 0) || (pPos >= this.TSpec.Datas.length))
			return false;
		
		this.TSpec.Datas[pPos] = pValue;
		return true;
	}
	
	// Services --------------------------------------------------------------------------------------------------------
	
	// Add Required type
	public boolean addRequiredType(Object pID, TypeRef pTRef) {
		if(!this.isValidID(pID)) return false;
		if(pTRef == null)        return false;
		
		if(this.TSpec.RequiredTypes == null) {
			this.TSpec.RequiredTypes = new TypeRef[1];
			this.TSpec.RequiredTypes[0] = pTRef;
		} else {
			TypeRef[] TRs = new TypeRef[this.TSpec.RequiredTypes.length + 1];
			System.arraycopy(this.TSpec.RequiredTypes, 0, TRs, 0, this.TSpec.RequiredTypes.length);
			TRs[this.TSpec.RequiredTypes.length] = pTRef;
			this.TSpec.RequiredTypes = TRs;
		}
		return true;
	}
	
	// Add Used Type
	public boolean addUsedType(Object pID, TypeRef pTRef) {
		if(!this.isValidID(pID)) return false;
		if(pTRef == null)        return false;
		
		if(this.TSpec.UsedTypes == null) {
			this.TSpec.UsedTypes = new TypeRef[1];
			this.TSpec.UsedTypes[0] = pTRef;
		} else {
			TypeRef[] TRs = new TypeRef[this.TSpec.UsedTypes.length + 1];
			System.arraycopy(this.TSpec.UsedTypes, 0, TRs, 0, this.TSpec.UsedTypes.length);
			TRs[this.TSpec.UsedTypes.length] = pTRef;
			this.TSpec.UsedTypes = TRs;
		}
		return true;
	}
	
	// Set ExtraInfo
	public boolean setExtraInfo(Object pID, String pEIName, Serializable pValue) {
		if(!this.isValidID(pID)) return false;
		MoreData EI = this.TSpec.getExtraInfo();
		if((EI == null) || (EI == MoreData.Empty)) {
			int EIIndex = this.TSpec.getExtraInfoIndex();
			if((EIIndex < 0) || (EIIndex >= this.TSpec.Datas.length)) return false; 
			EI = new MoreData();
			this.TSpec.Datas[this.TSpec.getExtraInfoIndex()] = EI;
		}
		
		EI.setData(pEIName, pValue);
		return true;
	}
	// Get ExtraInfo
	public Object getExtraInfo(Object pID, String pEIName) {
		if(!this.isValidID(pID)) return null;
		MoreData EI = this.TSpec.getExtraInfo();
		if((EI == null) || (EI == MoreData.Empty)) return null;
		return EI.getData(pEIName);
	}
	
	// Set MoreData
	public boolean setMoreData(Object pID, String pEIName, Serializable pValue) {
		if(!this.isValidID(pID)) return false;
		MoreData MD = this.TSpec.getMoreData();
		if((MD == null) || (MD == MoreData.Empty)) {
			int EIIndex = this.TSpec.getMoreDataIndex();
			if((EIIndex < 0) || (EIIndex >= this.TSpec.Datas.length)) return false;
			MD = new MoreData();
			this.TSpec.Datas[this.TSpec.getMoreDataIndex()] = MD; 
		}
		
		MD.setData(pEIName, pValue);
		return true;
	}
	// Get MoreData
	public Object getMoreData(Object pID, String pEIName) {
		if(!this.isValidID(pID)) return null;
		MoreData MD = this.TSpec.getMoreData();
		if((MD == null) || (MD == MoreData.Empty)) return null;
		return MD.getData(pEIName);
	}
	
	// Temporary Constructor -------------------------------------------------------------------------------------------
	
	
	static final class TempCIDirectEntry {
		TempCIDirectEntry(TypeBuilder pTB, ConstructorInfo pCID, Object pTempData) {
			this.TB       = pTB;
			this.CID      = pCID;
			this.TempData = pTempData;
			if(this.TB == null)     throw new NullPointerException(    "The given TypeBuilder is null.");
			if(!this.TB.isActive()) throw new IllegalArgumentException("The given TypeBuilder is no longer active.");
		}
		private TypeBuilder     TB;
		private ConstructorInfo CID;
		private Object          TempData;
		
		Object getTempData() {
			return this.TempData;
		}
		boolean resolve(Object pID, Expression pBody) {
			if(!this.TB.isValidID(pID)) return false;
			Executable.Macro Macro = this.CID.DclExec;
			if(!(Macro instanceof CurryExecutable.CurryMacro)) return false;
			((CurryExecutable.CurryMacro)Macro).Body = pBody;
			return true;
		}
		boolean resolve(Object pID, ExternalExecutor pEE, Object pEEID, Object pEESC) {
			if(!this.TB.isValidID(pID)) return false;
			Executable.Macro Macro = this.CID.DclExec;
			if(Macro == null) return false;
			((ExternalExecutable.ExternalMacro)Macro).EE = pEE;
			((ExternalExecutable.ExternalMacro)Macro).ID = pEEID;
			((ExternalExecutable.ExternalMacro)Macro).SC = pEESC;
			return true;
		}
		boolean resolve(Object pID, Executable.Macro pMacro) {
			if(!this.TB.isValidID(pID)) return false;
			if(pMacro  == null) return false;
			Executable.Macro Macro = this.CID.DclExec;
			if(!Macro.getSignature().equals(pMacro.getSignature())) return false;
			this.CID.DclExec = pMacro;
			return true;
		}
		
		// Reset -------------------------------------------------------------------
		
		/** Resets TypeRefs and TypeSpecs for the compilation */
		final protected void resetForCompilation() {
			Util.ResetConstructorInfo(this.CID);
		}
	}
	
	// Static (type scope) element -------------------------------------------------------------------------------------
	
	HashSet<ConstructorInfo> ConstructorInfos = null;
	HashSet<AttributeInfo>   StaticAttrInfos = null;
	HashSet<OperationInfo>   StaticOperInfos = null;
	
	protected HashSet<ConstructorInfo> getConstructorInfos() {
		if(this.TSpec.getConstructorInfo() == null)
			throw new RuntimeException("This type " + this.getName() + " of the kind " + this.TSpec.getKindName() +
					" does not accept constructors.");
		
		if(this.ConstructorInfos == null) this.ConstructorInfos = new HashSet<ConstructorInfo>();
		return this.ConstructorInfos;
	}
	
	protected HashSet<AttributeInfo> getStaticAttrInfos() {
		if(this.TSpec.getTypeAttributeInfo() == null)
			throw new RuntimeException("This type " + this.getName() + " of the kind " + this.TSpec.getKindName() +
					" does not accept static attribute.");
		
		if(this.StaticAttrInfos == null) this.StaticAttrInfos = new HashSet<AttributeInfo>();
		return this.StaticAttrInfos;
	}
	protected HashSet<OperationInfo> getStaticOperInfos() {
		if(this.TSpec.getTypeAttributeInfo() == null)
			throw new RuntimeException("This type " + this.getName() + " of the kind " + this.TSpec.getKindName() +
					" does not accept static operation.");
		
		if(this.StaticOperInfos == null) this.StaticOperInfos = new HashSet<OperationInfo>();
		return this.StaticOperInfos;
	}
	
	// Add Elements --------------------------------------------------------------------------------

	// Dynamic --------------------------------------------------------
	/** Creates a new attribute info */
	final public Object addStaticAttrDynamic(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
			String pAName, TypeRef pTRef, MoreData pMoreData) {
		if(!this.isActive())               throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pAName == null)                 throw new NullPointerException();
		if(this.isStaticAttrExist(pAName)) throw new IllegalArgumentException("The static attribute with the same signature is already exist ("+pAName+" in "+this.getName()+").");
		
		this.getStaticAttrInfos().add(new AttributeInfo.AIDynamic(pPARead, pPAWrite, pPAConfig, pAName, pTRef, pMoreData));
		return true;
	}
	/** Creates a new operation info */
	final public Object addStaticOperDynamic(Accessibility pPAccess, ExecSignature pES, MoreData pMoreData) {
		if(!this.isActive())      throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pES == null)           throw new NullPointerException();
		if(this.isStaticOperExist(pES)) throw new IllegalArgumentException("The static operation with the same signature is already exist ("+pES.toString()+" in "+this.getName()+").");
				
		this.getStaticOperInfos().add(new OperationInfo.OIDynamic(pPAccess, pES, pMoreData));
		return true;
	}
	// Field --------------------------------------------------------
	/** Creates a new attribute info */
	final public Object addStaticAttrDlgAttr(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
			String pAName, String pTName, MoreData pMoreData) {
		if(!this.isActive()) throw new RuntimeException(String.format("The builder is inactive (%s).", this.getName()));
		if(pAName == null)   throw new NullPointerException();
		if(this.isStaticAttrExist(pAName))
			throw new IllegalArgumentException(String.format(
				"The static attribute with the same signature is already exist (%s in %s).", pAName, this.getName()));
		
		this.getStaticAttrInfos().add(new AttributeInfo.AIDlgAttr(pPARead, pPAWrite, pPAConfig, pAName, pTName, pMoreData));
		return true;
	}
	/** Creates a new operation info */
	final public Object addStaticOperDlgAttr(Accessibility pPAccess, ExecSignature pES, String pTName, MoreData pMoreData) {
		if(!this.isActive())            throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pES == null)                 throw new NullPointerException();
		if(this.isStaticOperExist(pES)) throw new IllegalArgumentException("The static operation with the same signature is already exist ("+pES.toString()+" in "+this.getName()+").");
		
		this.getStaticOperInfos().add(new OperationInfo.OIDlgAttr(pPAccess, pES, pTName, pMoreData));
		return true;
	}
	
	// Temp related ----------------------------------------------------------------------------------------------------
	
	Hashtable<ExecSignature, TempCIDirectEntry> TempConstructors = null;
	Hashtable<String,        TempAIDirectEntry> StaticTempAttrs  = null;
	Hashtable<ExecSignature, TempOIDirectEntry> StaticTempOpers  = null;
	
	// Constructors ---------------------------------------------------------------------
	
	/** Returns the signatures of the temp constructor */
	final public Set<ExecSignature> getTempConsSignatures() {
		return (this.TempConstructors == null)?null:this.TempConstructors.keySet();
	}
	/** Returns the temp-data of the temporary operation */
	final public Object getTempConsTempData(ExecSignature pES) {
		if(this.TempConstructors == null) return null;
		TempCIDirectEntry TCIDE = this.TempConstructors.get(pES);
		if(TCIDE == null) return null;
		return TCIDE.getTempData();
	}
	final public boolean resolveTempCons(ExecSignature pES, Object pID, Expression pBody) {
		if(this.TempConstructors == null) return false;
		TempCIDirectEntry TCIDE = this.TempConstructors.get(pES);
		if(TCIDE == null) return false;
		boolean Result = TCIDE.resolve(pID, pBody);
		if(Result) this.TempConstructors.remove(TCIDE);
		return Result;
	}
	final public boolean resolveTempCons(ExecSignature pES, Object pID, ExternalExecutor pEE, Object pEEID, Object pEESC) {
		if(this.TempConstructors == null) return false;
		TempCIDirectEntry TCIDE = this.TempConstructors.get(pES);
		if(TCIDE == null) return false;
		boolean Result = TCIDE.resolve(pID, pEE, pEEID, pEESC);
		if(Result) this.TempConstructors.remove(TCIDE);
		return Result;
	}
	final public boolean resolveTempCons(ExecSignature pES, Object pID, Executable.Macro pMacro) {
		if(this.TempConstructors == null) return false;
		TempCIDirectEntry TCIDE = this.TempConstructors.get(pES);
		if(TCIDE == null) return false;
		boolean Result = TCIDE.resolve(pID, pMacro);
		if(Result) this.TempConstructors.remove(TCIDE);
		return Result;
	}
	
	// Static Attribute -----------------------------------------------------------------
	
	/** Returns the names of the temp attribute */
	final public Set<String> getStaticTempAttrNames() {
		return (this.StaticTempAttrs == null)?null:this.StaticTempAttrs.keySet();
	}
	/** Returns the temp-data of the temporary attribute */
	final public Object getStaticTempAttrTempData(String pName) {
		if(this.StaticTempAttrs == null) return null;
		TempAIDirectEntry TAIDE = this.StaticTempAttrs.get(pName);
		if(TAIDE == null) return null;
		return TAIDE.getTempData();
	}
	/** Resolve the temporary attribute */
	final public boolean resolveStaticTempAttr(String pName, Object pID, TypeRef pDValueTypeRef, Serializable pDValue) {
		if(this.StaticTempAttrs == null) return false;
		TempAIDirectEntry TAIDE = this.StaticTempAttrs.get(pName);
		if(TAIDE == null) return false;
		boolean Result = TAIDE.resolve(this.getEngine(), pID, pDValueTypeRef, pDValue);
		if(Result) this.StaticTempAttrs.remove(TAIDE);
		return Result;
	}
	/** Resolve the temporary attribute */
	final public AttributeInfo getStaticTempAttrAttributeInfo(String pName) {
		if(this.StaticTempAttrs == null) return null;
		TempAIDirectEntry TAIDE = this.StaticTempAttrs.get(pName);
		if(TAIDE == null) return null;
		return TAIDE.AID;
	}
	/** Resolve the temporary attribute */
	final public Location getStaticTempAttrLocation(String pName) {
		if(this.StaticTempAttrs == null) return null;
		TempAIDirectEntry TAIDE = this.StaticTempAttrs.get(pName);
		if(TAIDE == null) return null;
		return TAIDE.AID.Location;
	}
	
	// Static Operations ----------------------------------------------------------------
	
	/** Returns the signatures of the temp attribute */
	final public Set<ExecSignature> getStaticTempOperSignatures() {
		return (this.StaticTempOpers == null)?null:this.StaticTempOpers.keySet();
	}
	/** Returns the temp-data of the temporary operation */
	final public Object getStaticTempOperTempData(ExecSignature pES) {
		if(this.StaticTempOpers == null) return null;
		TempOIDirectEntry TOIDE = this.StaticTempOpers.get(pES);
		if(TOIDE == null) return null;
		return TOIDE.getTempData();
	}
	final public boolean resolveStaticTempOper(ExecSignature pES, Object pID, Expression pBody) {
		if(this.StaticTempOpers == null) return false;
		TempOIDirectEntry TOIDE = this.StaticTempOpers.get(pES);
		if(TOIDE == null) return false;
		boolean Result = TOIDE.resolve(pID, pBody);
		if(Result) this.StaticTempOpers.remove(TOIDE);
		return Result;
	}
	final public boolean resolveStaticTempOper(ExecSignature pES, Object pID, ExternalExecutor pEE, Object pEEID, Object pEESC) {
		if(this.StaticTempOpers == null) return false;
		TempOIDirectEntry TOIDE = this.StaticTempOpers.get(pES);
		if(TOIDE == null) return false;
		boolean Result = TOIDE.resolve(pID, pEE, pEEID, pEESC);
		if(Result) this.StaticTempOpers.remove(TOIDE);
		return Result;
	}
	final public boolean resolveStaticTempOper(ExecSignature pES, Object pID, Executable pExec) {
		if(this.StaticTempOpers == null) return false;
		TempOIDirectEntry TOIDE = this.StaticTempOpers.get(pES);
		if(TOIDE == null) return false;
		boolean Result = TOIDE.resolve(pID, pExec);
		if(Result) this.StaticTempOpers.remove(TOIDE);
		return Result;
	}
	/** Resolve the temporary constructor */
	final public Location getTempConstLocation(ExecSignature pES) {
		if(this.ConstructorInfos == null) return null;
		TempCIDirectEntry TCIDE = this.TempConstructors.get(pES);
		if(TCIDE == null) return null;
		return TCIDE.CID.getSignature().getLocation();
	}
	/** Resolve the temporary attribute */
	final public Location getStaticTempOperLocation(ExecSignature pES) {
		if(this.StaticTempOpers == null) return null;
		TempOIDirectEntry TOIDE = this.StaticTempOpers.get(pES);
		if(TOIDE == null) return null;
		return TOIDE.OID.getSignature().getLocation();
	}
	
	// Direct not temp -------------------------------------------------------------------------------------------------
	
	final public Object addConstructor(Accessibility pPAccess, Executable.Macro pMacro, MoreData pMoreData) {
		return this.addConstructor(pPAccess, pMacro, pMoreData, null, false);
	}

	final public Object addStaticAttrConst(Accessibility PARead, String pVName, boolean pIsNotNull, Serializable pValue,
			boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		return this.addStaticAttrConst(PARead, pVName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, null, false);
	}
	final public Object addStaticAttrConst(String pVName, boolean pIsNotNull, Serializable pValue, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		return this.addStaticAttrConst(Accessibility.Public, pVName, pIsNotNull, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, null, false);
	}
	final public Object addStaticAttrDirect(String pVName, boolean pIsNotNull, TypeRef pTypeRef, Serializable pValue,
			boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData) {
		return this.addStaticAttrDirect(pVName, pIsNotNull, pTypeRef, pValue, IsValueExpr, pMoreInfo, pLocation, pMoreData, null, false);
	}
	final public Object addStaticAttrDirect(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig, String pAName,
			boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData) {
		return this.addStaticAttrDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, pMoreData, null, false);
	}

	
	final public Object addStaticOperDirect(Accessibility pPAccess, Executable pExec, MoreData pMoreData) {
		return this.addStaticOperDirect(pPAccess, pExec, pMoreData, null, false);
	}
	
	// Direct temp -----------------------------------------------------------------------------------------------------
	
	final public Object addTempConstructor(Accessibility pPAccess, MoreData pMoreData, Executable.Macro pMacro, Object pTempData) {
		return this.addConstructor(pPAccess, pMacro, pMoreData, pTempData, true);
	}
	
	final public Object addTempStaticAttrConst(Accessibility pAccess, String pVName, boolean pIsNotNull,
			boolean IsValueExpr, MoreData pMoreInfo, Location pLocation, MoreData pMoreData, Object pTempData) {
		return this.addStaticAttrConst(pAccess, pVName, pIsNotNull, null, IsValueExpr, pMoreInfo, pLocation, pMoreData, pTempData, true);
	}
	final public Object addTempStaticAttrDirect(String pVName, boolean pIsNotNull, TypeRef pTypeRef, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData, Object pTempData) {
		return this.addStaticAttrDirect(pVName, pIsNotNull, pTypeRef, null, IsValueExpr, pMoreInfo, pLocation, pMoreData,
				pTempData, true);
	}
	final public Object addTempStaticAttrDirect(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig,
			String pAName, boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData,
			Object pTempData) {
		return this.addStaticAttrDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI, pLocation, pMoreData,
				pTempData, true);
	}

	final public Object addTempStaticOperDirect(Accessibility pPAccess, Executable pExec, MoreData pMoreData, Object pTempData) {
		return this.addStaticOperDirect(pPAccess, pExec, pMoreData, pTempData, true);
	}
	
	// Direct Implement ------------------------------------------------------------------------------------------------
	
	final boolean addConstructor(Accessibility pPAccess, Executable.Macro pMacro, MoreData pMoreData, Object pTempData,
			boolean pIsTemp) {
		if(!this.isActive()) throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pMacro == null)   throw new NullPointerException();
		
		// Check repeat
		if(this.isConstructorExist(pMacro.getSignature()))
			throw new IllegalArgumentException(
				"The constructor with the same signature is already exist ("+pMacro.toString()+" in "+this.getName()+").");
		
		// This will ensure all the constrain of Constructor
		ConstructorInfo CID = new ConstructorInfo.CIMacro(this.getEngine(), pPAccess, this.getTypeRef(), pMacro, pMoreData);
		this.getConstructorInfos().add(CID);
		
		if(!pIsTemp) return true;
		
		// Add the temp to the list
		if(this.TempConstructors == null) this.TempConstructors = new Hashtable<ExecSignature, TempCIDirectEntry>();
		this.TempConstructors.put(pMacro.getSignature(), new TempCIDirectEntry(this, CID, pTempData));
		return true;
	}
	
	final public Object addStaticAttrConst(Accessibility PA, String pVName, boolean pIsNotNull, Serializable pValue, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData, Object pTempData, boolean pIsTemp) {
		
		if(!this.isActive())               return false;
		if(pVName == null)                 return false;
		if(this.isStaticAttrExist(pVName)) return false;
		
		DataHolderInfo DHI = new DataHolderInfo(null, pValue, Variable.FactoryName, true, false, true, IsValueExpr, pMoreInfo);
		
		return this.addStaticAttrDirect(PA, PA, PA, pVName, pIsNotNull, DHI, pLocation, pMoreData, pTempData, pIsTemp);
	}
	final Object addStaticAttrDirect(String pAName, boolean pIsNotNull, TypeRef pTypeRef, Serializable pValue, boolean IsValueExpr,
			MoreData pMoreInfo, Location pLocation, MoreData pMoreData, Object pTempData, boolean pIsTemp) {
		
		if(!this.isActive()) throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pAName   == null) throw new NullPointerException();
		if(pTypeRef == null) throw new NullPointerException();

		DataHolderInfo DHI = new DataHolderInfo(pTypeRef, pValue, Variable.FactoryName, true, true, true, IsValueExpr, pMoreInfo);
		Accessibility  PA  = Accessibility.Public;
		return this.addStaticAttrDirect(PA, PA, PA, pAName, pIsNotNull, DHI, pLocation, pMoreData, pTempData, pIsTemp);
	}
	final boolean addStaticAttrDirect(Accessibility pPARead, Accessibility pPAWrite, Accessibility pPAConfig, String pAName,
			boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation, MoreData pMoreData, Object pTempData,
			boolean pIsTemp) {
		if(!this.isActive()) throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pAName == null)   throw new NullPointerException();
		if(pDHI   == null)   throw new NullPointerException();

		// Check repeat
		if(this.isStaticAttrExist(pAName))
			throw new IllegalArgumentException(
					"The static attribute with the same signature is already exist ("+pAName+" in "+this.getName()+").");
		
		// Static attribute cannot be BaseOnType.
		if((pDHI.getTypeRef() instanceof TLBasedOnType.TRBasedOnType))
			throw new CurryError("Static attribute cannot be BasedOnType TypeRef ("+this+"."+pAName+"). <TypeBuilder:600>");
		
		AttributeInfo.AIDirect AID = new AttributeInfo.AIDirect(pPARead, pPAWrite, pPAConfig, pAName, pIsNotNull, pDHI,
											pLocation, pMoreData);
		this.getStaticAttrInfos().add(AID);
		
		if(!pIsTemp) return true;
		
		// Add the temp to the list
		if(this.StaticTempAttrs == null) this.StaticTempAttrs = new Hashtable<String, StackOwnerBuilder.TempAIDirectEntry>();
		this.StaticTempAttrs.put(pAName, new TempAIDirectEntry(this, AID, pTempData));
		return true;
		
	}

	final boolean addStaticOperDirect(Accessibility pPAccess, Executable pExec, MoreData pMoreData, Object pTempData,
			boolean pIsTemp) {
		if(!this.isActive())                             throw new RuntimeException("The builder is inactive ("+this.getName()+").");
		if(pExec == null)                                throw new NullPointerException();
		if(pExec instanceof WrapperExecutable.Wrapper)   throw new IllegalArgumentException("Wrapper cannot be used as operation ("+pExec.toString()+").");
		if(this.isStaticOperExist(pExec.getSignature())) throw new IllegalArgumentException("The static operation with the same signature is already exist ("+pExec.toString()+" in "+this.getName()+").");
		
		OperationInfo.OIDirect OID = new OperationInfo.OIDirect(pPAccess, pExec, pMoreData);
		this.getStaticOperInfos().add(OID);
		
		if(!pIsTemp) return true;
		
		// Add the temp to the list
		if(this.StaticTempOpers == null) this.StaticTempOpers = new Hashtable<ExecSignature, StackOwnerBuilder.TempOIDirectEntry>();
		this.StaticTempOpers.put(pExec.getSignature(), new TempOIDirectEntry(this, OID, pTempData));
		return true;
	}
	
	// Artifacts ---------------------------------------------------------------
	
	/** Checks if the function `pES` exist. */
	final public boolean isConstructorExist(ExecSignature pES) {
		if(pES == null) return false;
		for(ConstructorInfo CI : this.getConstructorInfos()) {
			if(pES.isAllParameterTypeEquals(CI.getSignature())) return true;
		}
		return false;
	}
	/** Checks if the package variable `pVName` exist. */
	final public boolean isStaticAttrExist(String pVName) {
		if(pVName == null) return false;
		for(AttributeInfo PVI : this.getStaticAttrInfos()) {
			if(pVName.equals(PVI.getName())) return true;
		}
		return false;
	}
	/** Checks if the function `pES` exist. */
	final public boolean isStaticOperExist(ExecSignature pES) {
		if(pES == null) return false;
		for(OperationInfo FI : this.getStaticOperInfos()) {
			if(pES.isAllParameterTypeEquals(FI.getSignature())) return true;
		}
		return false;
	}
	
	// Reset --------------------------------------------------------------------------------------
	
	/** Resets an iterable of ConstructorInfos */
	final static public void ResetTempConstructorInfos(Hashtable<ExecSignature, TempCIDirectEntry> TCIDEs) {
		if(TCIDEs == null) return;
		for(ExecSignature Signature : TCIDEs.keySet()) {
			if(Signature != null) Util.ResetExecSignature(Signature);
			TempCIDirectEntry TCIDE = TCIDEs.get(Signature);
			if(TCIDE != null) TCIDE.resetForCompilation();
		}
	}
	
	/**{@inheritDoc}*/ @Override
	protected void resetBuilderCompilation() {
		Util.ResetConstructorInfos(this.ConstructorInfos);
		Util.ResetAttributeInfos(  this.StaticAttrInfos);
		Util.ResetOperationInfos(  this.StaticOperInfos);
		ResetTempConstructorInfos( this.TempConstructors);
		ResetTempAttributeInfos(   this.StaticTempAttrs);
		ResetTempOperationInfos(   this.StaticTempOpers);
		this.TSpec.resetForCompilation();
	}
}
