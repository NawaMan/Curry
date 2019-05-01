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

package net.nawaman.curry.extra.type_object;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.nawaman.curry.Accessibility;
import net.nawaman.curry.AttributeInfo;
import net.nawaman.curry.AttributeInfo.AIDlgAttr;
import net.nawaman.curry.Context;
import net.nawaman.curry.CurryError;
import net.nawaman.curry.DObject;
import net.nawaman.curry.Documentation;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Location;
import net.nawaman.curry.OperationInfo;
import net.nawaman.curry.StackOwnerBuilderEncloseObject;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeBuilder;
import net.nawaman.curry.MType;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.compiler.TypeSpecCreator;
import net.nawaman.curry.util.MoreData;

/**
 * Type Kind for classed-Object type
 *
 * @author Nawapunth Manusitthipol
 */
public class TKClass extends TKObject {

	static public final String KindName = "Class";
	
	/** Constructs a new TypeKind. */
	protected TKClass(Engine pEngine) {
		super(pEngine);
	}

	// Classification -----------------------------------------------------------------------------

	/**{@inheritDoc} */ @Override
	public String getKindName() {
		return KindName;
	}
	
	// Type Initiation -----------------------------------------------------------------------------

	// Initialize
	/**
	 * Initialize the type by creating necessary parts of the type.<br/> At this point the type is resolved and has been
	 * validated.
	 */ @Override
	 protected Exception initializeType(Context pContext, Type pType) {
		// Checks interfaces and whatever needed to be done
		Exception Excp = super.initializeType(pContext, pType);
		if(Excp != null) return Excp;
		
		// Dynamic Delegation ==========================================================================================
		
		Vector<String> DNames   = new Vector<String>();
		Vector<Type>   DAsTypes = new Vector<Type>();
		
		// Add from type spec
		int C = ((TSClass)((TClass)pType).getTSObject()).getDynamicDelegationCount();
		for(int i = 0; i < C; i++) {
			String N = ((TSClass)((TClass)pType).getTSObject()).getDynamicDelegationAt(i);
			if(N == null) continue;
			DNames.add(  N);
			DAsTypes.add(pType);
		}
		
		// Prepare Dynamic Delegation
		Type T = ((TClass)pType).getSuper();
		if(T instanceof TClass) {
			TClass Super = (TClass)T;

			SuperLoop: for(;;) {
				String[] SDDlgNames   = Super.DDlgNames;
				Type[]   SDDlgAsTypes = Super.DDlgAsTypes;
				
				if((SDDlgNames != null) && (SDDlgNames.length != 0)) {
					for(int i = 0; i < SDDlgNames.length; i++) {
						if(SDDlgNames[i] == null) continue;
						DNames.add(  SDDlgNames[  i]);
						DAsTypes.add(SDDlgAsTypes[i]);
					}
				}
				
				T = Super.getSuper();
				if(!(T instanceof TClass)) break SuperLoop;	// No more super
				Super = (TClass)T;
			}
		}
		
		if(DNames.size() != 0) {
			((TClass)pType).DDlgNames   = DNames.  toArray(new String[DNames.size() ]);
			((TClass)pType).DDlgAsTypes = DAsTypes.toArray(new Type[ DAsTypes.size()]);
		}
		
		return null;
	}
	
	// Instantiation ------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected TypeBuilder createNewTypeBuilder(TypeSpec pTS, Accessibility pPAccess, Location pLocation,
			StackOwnerBuilderEncloseObject pEncloseObject) {
		if(!(pTS instanceof TSClass)) throw new IllegalArgumentException("Invalid TypeSpec.");
		
		// Create a new type builder from the information
		return new TBClass((TSClass)pTS, pPAccess, pLocation, pEncloseObject);
	}
	
	/** Create a new TypeSpec of this type */
	public TSClass getTypeSpec(TypeRef pTRef, boolean pIsAbstract, boolean pIsFinal, TypeRef pSuperRef,
			TypeRef[] pInterfaces, ParameterizedTypeInfo pTPInfo, MoreData pMoreData, MoreData pExtraInfo) {
		return this.getTypeSpec(pTRef, pIsAbstract, pIsFinal, pSuperRef, pInterfaces, true, pTPInfo, pMoreData, pExtraInfo);
	}
	
	/** Create a new TypeSpec of this type */
	protected TSClass getTypeSpec(TypeRef pTRef, boolean pIsAbstract, boolean pIsFinal, TypeRef pSuperRef,
			TypeRef[] pInterfaces, boolean pIsVerify, ParameterizedTypeInfo pTPInfo, MoreData pMoreData,
			MoreData pExtraInfo) {
		return new TSClass(pTRef, this.getKindName(), pIsAbstract, pIsFinal, pSuperRef, pInterfaces, pTPInfo, pMoreData, pExtraInfo);
	}

	public TypeSpecCreator getTypeSpecCreator(final boolean pIsAbstract, final boolean pIsFinal, final TypeRef pSuperRef,
			final TypeRef[] pInterfaces, final MoreData pMoreData,
			final MoreData pExtraInfo) {
		return getTypeSpecCreator(pIsAbstract, pIsFinal, pSuperRef, pInterfaces, null, pMoreData, pExtraInfo);
	}

	public TypeSpecCreator getTypeSpecCreator(final boolean pIsAbstract, final boolean pIsFinal, final TypeRef pSuperRef,
			final TypeRef[] pInterfaces, final ParameterizedTypeInfo pTPInfo, final MoreData pMoreData,
			final MoreData pExtraInfo) {
		return new TypeSpecCreator() {
			public TypeSpec newTypeSpec(Engine pEngine, TypeRef pTRef, boolean pIsVerify, Documentation pDocument) {
				return getTypeSpec(pTRef, pIsAbstract, pIsFinal, pSuperRef, pInterfaces, pIsVerify, pTPInfo, pMoreData,
						MoreData.combineMoreData(
							(pDocument == null)?null:new MoreData(Documentation.MIName_Documentation, pDocument),
							pExtraInfo));
			}
		};
	}

	// Type checking ---------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TClass.class;
	}
	
	// Type Construction -----------------------------------------------------------------

	/** Returns type associated with Spec or create one if not exist. In case of error, return an the exception. */
	@Override protected Type getType(Engine pEngine, Context pContext, TypeSpec pSpec) {
		if(!(pSpec instanceof TSClass)) return null;
		return new TClass(this, (TSClass)pSpec);
	}

	/**{@inheritDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return false;
	}
	
	// Compatibility ------------------------------------------------------------------------------

	/** Checks if an instance of the type with the spec pSpec can be assigned to a variable of the type pTheType */
	@Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec, TypeSpec BySpec) {	
		if(!(TheSpec instanceof TSObject)) return false;
		if(!(BySpec        instanceof TSObject)) return false;
			
		TSObject TheObject = ((TSObject)TheSpec);
		TSObject ByObject  = ((TSObject)BySpec);
		
		// Check the super
		while(true) {
			if(TheObject == ByObject) return  true;
			
			// Get super then continue
			TypeRef TR = ByObject.getSuperRef();
			if(TR == null) return false;
			
			TypeSpec TS = this.getTypeSpec(pContext, TR);
			if(!(TS instanceof TSObject)) return false;
			
			ByObject = (TSObject)TS;
		}
	}
	
	// StackOwner ------------------------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected void doType_prepareLowPriorityFields(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<AttributeInfo> pAIsFromNative) {
		
		super.doType_prepareLowPriorityFields(pContext, pEngine, pTheType, pIsStatic, pAIsFromNative);
		
		// Only static and a class type to be process here, the rest ... nothing happen
		if(pIsStatic || !(pTheType instanceof TClass) || !(pTheType.getTypeSpec() instanceof TSClass)) return;
		
		// Get create fields that forward the access to all fields of those found in the static delegation type
		// The first static delegation gets the first priority
		
		// Prepare the type spec
		TClass  TC   = (TClass)pTheType;
		TSClass TSC  = (TSClass)TC.getTSObject();
		
		// No StaticDelegation
		if(TSC.getStaticDelegationCount() == 0) return;
		
		// Prepare a hash table to make it easy to search Type for the AttributeInfos
		Vector<AttributeInfo>      TAIs   = this.getTSpecDataAttributeInfo(TSC);
		Hashtable<String, TypeRef> TAITRs = new Hashtable<String, TypeRef>();
		for(AttributeInfo AI : TAIs) {
			AI.resolve(pEngine);
			TAITRs.put(AI.getName(), AI.getTypeRef());
		}
		
		HashSet<String> LAINs = new HashSet<String>();
		
		// Loop all 
		
		// Loop all static delegation
		for(int i = 0; i < TSC.getStaticDelegationCount(); i++) {
			
			String SDName = TSC.getStaticDelegationAt(i);
			// The delegate name is not found (since declaring static delegation must be done with the attribute itself,
			//   the attribute info is always local).
			if(!TAITRs.containsKey(SDName))
				throw new CurryError("Internal Error: Static delegated attribute named '"+SDName+"' does not exist.");
			
			TypeRef SDTR = TAITRs.get(SDName);
			try { this.getEngine().getTypeManager().ensureTypeInitialized(SDTR); }
			catch(Exception E) {
				if((E instanceof CurryError) &&
				   (((CurryError)E).getMessage() != null) &&
				   ((CurryError)E).getMessage().startsWith(MType.TypeInitializationErrorPrefix)) {
					throw new CurryError("Internal Error: The type of static delegated attribute '"+pTheType.toString()+
							"."+SDName+"' cannot be initialized (perhaps it reqiuires this type (recursive requiring)).", E);
				} else
					throw new CurryError("An error occur while preparing attribute from static delegated attribute " +
							"'"+pTheType.toString()+"."+SDName+"'.", E);
			}
			
			List<AttributeInfo> SAIs = Arrays.asList(this.getTypeFromRef(pContext, SDTR).getTypeInfo().getObjectAttributeInfos());
			if(SAIs == null) continue;
			
			// Create a delegate attribute to all attribute in SAIs
			for(AttributeInfo AI : SAIs) {
				if(AI == null) continue;
				
				// The name of the attribute repeat what defined in this type so no need to process (it will be overridden anyway)
				if(TAITRs.containsKey(AI.getName())) continue;
				// The name of the attribute repeat what previously added (from the earlier static delegation)
				if(LAINs.contains(AI.getName())) continue;
				
				// Repeat with the one higher priority, ignore this one
				if(getAIsByName(pAIsFromNative, AI.getName()) != null) continue;
				
				AI.resolve(pEngine);
				
				// HERE: When make parameterization type, here is where we change
				
				// Add the newly create delegate attribute to the list
				LAINs.add(AI.getName());
				
				AIDlgAttr NewAI = this.doType_newAIDlgAttr(pTheType,
						AI.getReadAccessibility(), AI.getWriteAccessibility(), AI.getConfigAccessibility(),
						AI.getName(), SDName, AI.getMoreData());

				pAIsFromNative.add(NewAI);
			}
		}
	}
	/**{@inheritDoc}*/ @Override
	protected void doType_prepareLowPriorityMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {
		
		super.doType_prepareLowPriorityMethods(pContext, pEngine, pTheType, pIsStatic, OIs);
		
		// Only static and a class type to be process here, the rest ... nothing happen
		if(pIsStatic || !(pTheType instanceof TClass) || !(pTheType.getTypeSpec() instanceof TSClass)) return;
		
		// Get create fields that forward the access to all fields of those found in the static delegation type
		// The first static delegation gets the first priority
		
		// Prepare the type spec
		TClass  TC   = (TClass)pTheType;
		TSClass TSC  = (TSClass)TC.getTSObject();
		
		// No StaticDelegation
		if(TSC.getStaticDelegationCount() == 0) return;
		
		// Prepare a hash table to make it easy to search Type from the AttributeInfos
		Vector<AttributeInfo>      TAIs   = this.getTSpecDataAttributeInfo(TSC);
		Hashtable<String, TypeRef> TAITRs = new Hashtable<String, TypeRef>();
		for(AttributeInfo AI : TAIs) {
			// Ensure the attribute is resolved
			AI.resolve(pEngine);
			TAITRs.put(AI.getName(), AI.getTypeRef());
		}
		
		// Loop all static delegation
		for(int i = 0; i < TSC.getStaticDelegationCount(); i++) {
			
			String SDName = TSC.getStaticDelegationAt(i);
			// The delegate name is not found (since declaring static delegation must be done with the attribute itself,
			//   the attribute info is always local).
			if(!TAITRs.containsKey(SDName))
				throw new CurryError("Internal Error: Static delegated attribute named '"+SDName+"' is not exist.");
			
			TypeRef SDTR = TAITRs.get(SDName);
			try { this.getEngine().getTypeManager().ensureTypeInitialized(SDTR); }
			catch(Exception E) {
				if((E instanceof CurryError) &&
				   (((CurryError)E).getMessage() != null) &&
				   ((CurryError)E).getMessage().startsWith(MType.TypeInitializationErrorPrefix)) {
					throw new CurryError("Internal Error: The type of static delegated attribute '"+pTheType.toString()+
							"."+SDName+"' cannot be initialized (perhaps it reqiuires this type (recursive requiring)).");
				} else
					throw new CurryError("An error occur while preparing attribute from static delegated attribute " +
							"'"+pTheType.toString()+"."+SDName+"'.");
			}
			
			List<OperationInfo> SOIs = Arrays.asList(this.getTypeFromRef(pContext, SDTR).getTypeInfo().getObjectOperationInfos());
			if(SOIs == null) continue;
			
			// Create a delegate operation to all operation in SOIs
			for(OperationInfo OI : SOIs) {
				if(OI == null) continue;
				
				// Get the TypeRef of the Attribute;
				ExecSignature ES = OI.getSignature();
				
				// HERE: When make parameterization type, here is where we change
				
				// Add the newly create delegate attribute to the list
				this.addDataOperationToOperationList(pContext, pEngine, OIs,
					this.doType_newOIDlgAttr(pTheType, OI.getAccessibility(), ES, SDName, OI.getMoreData())
				);
			}
		}
	}
	
	// Dynamic Delegation -------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected int doData_getDynamicDelegationCount(Type pTheType, DObject pTheObject, Context pContext) {
		if(!(pTheType instanceof TClass)) return 0;
		return ((TClass)pTheType).getDynamicDelegateeCount();
	}
	/**{@inheritDoc}*/ @Override
	protected String doData_getDynamicDelegation(Type pTheType, DObject pTheObject, Context pContext, int I) {
		if(!(pTheType instanceof TClass)) return null;
		return ((TClass)pTheType).getDynamicDelegateeName(I);
	}
	/**{@inheritDoc}*/ @Override
	protected TypeRef doData_getDynamicDelegationAsType(Type pTheType, DObject pTheObject, Context pContext, int I) {
		// Note: No AsType so it is completely dynamic (since at the moment, we don't use Dynamic Delegation in the type
		//           information)
		return ((TClass)pTheType).getDynamicDelegateeAsType(I).getTypeRef();
	}
	
}
