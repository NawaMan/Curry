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

import java.util.Vector;

import net.nawaman.curry.AttributeInfo;
import net.nawaman.curry.Context;
import net.nawaman.curry.DObject;
import net.nawaman.curry.Engine;
import net.nawaman.curry.Executable;
import net.nawaman.curry.MType;
import net.nawaman.curry.OperationInfo;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeKind;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;

/**
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class TKObject extends TypeKind {

	/** Constructs a new TypeKind. */
	protected TKObject(Engine pEngine) { super(pEngine); }


	// Classification -------------------------------------------------------------------

	/** Returns the name of the kind of type */
	abstract @Override public String getKindName();

	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		if(!(pTheType instanceof TObject)) return false;
		return !((TObject)pTheType).isFinal();
	}
	
	// Type Construction -----------------------------------------------------------------

	/**{@inheritDoc}*/ @Override 
	protected Type getType(Engine pEngine, Context pContext, TypeSpec pSpec) {
		if(!(pSpec instanceof TSObject)) return null;
		return new TObject(this, (TSObject)pSpec);
	}

	// Type Initialization ---------------------------------------------------------------

	// PreInitialize
	/**{@inheritDoc}*/ @Override
	protected Exception doValidateTypeSpec(Context pContext, TypeSpec pSpec) {
		return null;
	}

	// Initialize
	/**{@inheritDoc}*/ @Override
	protected Exception initializeType(Context pContext, Type pType) {
		return super.initializeType(pContext, pType);
	}

	// Typing --------------------------------------------------------------------------------------

	// Type checking ---------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TObject.class;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getDefaultType(Context pContext) {
		return null;
	}

	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pSpec) {
		return DObject.class;
	}

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec, Object pByObject) {
		if(pByObject    == null)            return true;
		if(pTheTypeSpec == null)            return false;
		if(!(pByObject instanceof DObject)) return false;
		// Checks if the data is an instance of the type class
		if(!this.getTypeDataClass(pContext, pTheTypeSpec).isInstance(pByObject)) return false;
		
		// Get the Type of the object
		Type OT = ((DObject)pByObject).getTheType();
		if(!(OT instanceof TObject)) return false;
		
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, pTheTypeSpec.getTypeRef(), OT.getTypeRef());
	}

	/** Checks if an instance of the type with the spec pSpec can be assigned to a variable of the type pTheType */ @Override 
	abstract protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec, TypeSpec BySpec);

	// Get Type -------------------------------------------------------------------------
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOf(Context pContext, Object pObject) {
		if(!(pObject instanceof DObject)) return null;
		return ((DObject)pObject).getTheType();
	}

	/**{@inheritDoc}*/ @Override
	protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		return null;
	}

	// Instantiation ---------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isTypeAbstract(Context pContext, Type pTheType) {
		if(!(pTheType instanceof TObject)) return false;
		return ((TObject)pTheType).getTSObject().isAbstract();
	}

	/**{@inheritDoc}*/ @Override
	protected Object getTypeDefaultValue(Context pContext, Type pTheType) {
		return null;
	}

	/**
	 * Creates a new instance of the type.<br />
	 * If isAbstract() returns true, this method will never be called.
	 * @param pSearch is a search key to find the right constructor. It can be 1) null for seach with the parameter, 2)
	 *            Type[] for searching with type, 3) TypeRef[] for searching with type name and 4) ExecInterface for
	 *            searching with an interface.
	 */ @Override
	protected Object createNewTypeInstance(Context pContext, Executable pInitiator, Type pTheType,
				Object pSearchKey, Object[] pParams) {
		if(!(pTheType instanceof TObject)) return false;
		return this.doType_newDObject(pContext, pTheType);
	}
	
	// StackOwner ----------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindFields(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<AttributeInfo> AIs) {
		
		// Only an object type to be process here, the rest ... nothing happen
		if(!(pTheType instanceof TObject) || !(pTheType.getTypeSpec() instanceof TSObject)) return;
		
		// Get the list from the super
		TObject Super   = null;
		TypeRef SuperTR = ((TClass)pTheType).getSuperRef();
		if((SuperTR != null) && (SuperTR != TKJava.TAny.getTypeRef())) {
			if(pEngine == null) pEngine = this.getEngine();
			
			// This line only just in case as it should already be initialized 
			pEngine.getTypeManager().ensureTypeInitialized(SuperTR);

			Super = (TClass)((TClass)pTheType).getSuper();
		}
		
		if(Super != null) {
			if(pIsStatic) {
				// Get only the type attributes
				AttributeInfo[] Super_AIs = Super.getTypeInfo().getAttributeInfos();
				if(Super_AIs != null) {
					for(int i = Super_AIs.length; --i >= 0; ) {
						AttributeInfo AI = Super_AIs[i];
						if(AI == null) continue;
						
						// Repeat with the one higher priority, ignore this one
						if(getAIsByName(AIs, AI.getName()) != null)
							continue;
						
						// Create object delegates to the super type
						this.addTypeAttributeToAttributeList(pContext, pEngine, AIs,
							this.doType_newAIDlgObject(
								pTheType,
								AI.getReadAccessibility(),
								AI.getWriteAccessibility(),
								AI.getConfigAccessibility(),
								AI.getName(),
								AI.isNotNull(),
								AI.getOwner(),
								AI.getMoreData()
							)
						);
					}
				}
				
			} else {
				// Get only the object attributes
				AttributeInfo[] Super_AIs = Super.getTypeInfo().getObjectAttributeInfos();
				if(Super_AIs != null) {
					for(int i = Super_AIs.length; --i >= 0; ) {
						AttributeInfo AI = Super_AIs[i];
						if(AI == null) continue;
						
						// Repeat with the one higher priority, ignore this one
						if(getAIsByName(AIs, AI.getName()) != null)
							continue;
						
						// Add the clone here - NOTE that the owner will be preserved
						this.addTypeAttributeToAttributeList(pContext, pEngine, AIs, AI.makeClone());
					}
				}
			}
		}
	}

	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {
		
		// Only an object type to be process here, the rest ... nothing happen
		if(!(pTheType instanceof TObject) || !(pTheType.getTypeSpec() instanceof TSObject)) return;
		
		// Get the list from the super
		TObject Super   = null;
		TypeRef SuperTR = ((TClass)pTheType).getSuperRef();
		if((SuperTR != null) && (SuperTR != TKJava.TAny.getTypeRef())) {
			if(pEngine == null) pEngine = this.getEngine();
			pEngine.getTypeManager().ensureTypeInitialized(SuperTR);

			Super = (TClass)((TClass)pTheType).getSuper();
		}
		
		if(Super != null) {
			if(pIsStatic) {
				// Get only the type operations
				OperationInfo[] Super_OIs = Super.getTypeInfo().getOperationInfos();
				if(Super_OIs != null) {
					for(int i = Super_OIs.length; --i >= 0; ) {
						OperationInfo OI = Super_OIs[i];
						if(OI == null) continue;

						// Create object delegates to the super type
						this.addTypeOperationToOperationList(pContext, pEngine, OIs, 
							this.doType_newOIDlgObject(
								pTheType,
								OI.getAccessibility(),
								OI.getSignature(),
								OI.getOwner(),
								OI.getMoreData()
							)
						);
					}
				}
			} else {
				// Get only the object operations
				OperationInfo[] Super_OIs = Super.getTypeInfo().getObjectOperationInfos();
				if(Super_OIs != null) {
					for(int i = Super_OIs.length; --i >= 0; ) {
						OperationInfo OI = Super_OIs[i];
						if(OI == null) continue;

						this.addDataOperationToOperationList(pContext, pEngine, OIs, OI.makeClone());
					}
				}
			}
		}
	}
	
	// Dynamic Delegation --------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected int doType_getDynamicDelegationCount(Context pContext, Type pTheType) {
		return 0;
	}
	/**{@inheritDoc}*/ @Override
	protected String doType_getDynamicDelegation(Context pContext, Type pTheType, int I) {
		return null;
	}
	/**{@inheritDoc}*/ @Override
	protected TypeRef doType_getDynamicDelegationAsType(Context pContext, Type pTheType, int I) {
		return null;
	}
}
