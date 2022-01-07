/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2021 Nawapunth Manusitthipol. Implements with and for Java 11 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's Curry.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawa<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.curry;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Vector;

import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLNoName.TRNoName;
import net.nawaman.curry.TLParameter.TRParameter;
import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.TypeParameterInfo.TypeParameterInfos;
import net.nawaman.curry.compiler.TypeSpecCreator;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UClass;

/**
 * TypeKind for interface
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class TKInterface extends TypeKind {

	static public final String KindName = "Interface";

	// Constants ----------------------------------------------------------------------------------

	final static public int IndexCount = 10;
	// DATA
	final static public int Index_Kind       = 0;	// String
	final static public int Index_Interfaces = 1;	// Vector<TypeRef>	: NOTE: all of these types must be in Required type
	
	final static public int Index_TypeAttrs   = 2;	// Vector<OperationInfo>
	final static public int Index_ObjectAttrs = 3;	// Vector<OperationInfo>
	
	final static public int Index_TypeOpers   = 4;	// Vector<OperationInfo>
	final static public int Index_ObjectOpers = 5;	// Vector<OperationInfo>

	final static public int Index_Parameterization = 6;	// MoreData	: NOTE: all types in here must be in the used types
	
	final static public int Index_Target   = 7; // TypeRef
	final static public int Index_IsStrict = 8; // Boolean

	final static public int Index_ExtraInfo = 9;	// Extra Info
	
	// Constructor -----------------------------------------------------------------------------------------------------
	
	/** Constructs a new TypeKind. */
	protected TKInterface(Engine pEngine) {
		super(pEngine);
	}

	// Classification ------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return TKInterface.KindName;
	}
	
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec getTypeSpec(TypeRef pTRef, TypeRef[] pInterfaceRefs, final ParameterizedTypeInfo pTPInfo,
			TypeRef pTargetRef, boolean pIsStrict, MoreData pExtraInfo) {
		return new TSInterface(pTRef, pInterfaceRefs, pTargetRef, pTPInfo, pIsStrict, pExtraInfo);
	}
	
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newDuckTypeSpec(ExecSignature ... pMethodsSignature) {
		return this.newDuckTypeSpec(null, null, null, null, null, pMethodsSignature);
	}
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newDuckTypeSpec(TypeRef[] pInterfaceRefs, TypeRef pTargetRef, final ParameterizedTypeInfo pTPInfo,
			ExecSignature ... pMethodsSignature) {
		return this.newDuckTypeSpec(null, pInterfaceRefs, pTargetRef, pTPInfo, null, pMethodsSignature);
	}
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newDuckTypeSpec(TypeRef TRef, ExecSignature ... pMethodsSignature) {
		return this.newDuckTypeSpec(TRef, null, null, null, null, pMethodsSignature);
	}
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newDuckTypeSpec(TypeRef TRef, TypeRef[] pInterfaceRefs, TypeRef pTargetRef, final ParameterizedTypeInfo pTPInfo,
			ExecSignature ... pMethodsSignature) {
		return this.newDuckTypeSpec(TRef, pInterfaceRefs, pTargetRef, pTPInfo, null, pMethodsSignature);
	}
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newDuckTypeSpec(TypeRef TRef, TypeRef[] pInterfaceRefs, TypeRef pTargetRef, final ParameterizedTypeInfo pTPInfo,
			MoreData pExtraInfo, ExecSignature ... pMethodsSignature) {
		return this.newTypeSpec(TRef, pInterfaceRefs, pTargetRef, pTPInfo, pExtraInfo, false, pMethodsSignature);
	}
	
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newInterfaceTypeSpec(ExecSignature ... pMethodsSignature) {
		return this.newInterfaceTypeSpec(null, null, null, null, null, pMethodsSignature);
	}
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newInterfaceTypeSpec(TypeRef[] pInterfaceRefs, TypeRef pTargetRef, final ParameterizedTypeInfo pTPInfo,
			ExecSignature ... pMethodsSignature) {
		return this.newInterfaceTypeSpec(null, pInterfaceRefs, pTargetRef, pTPInfo, null, pMethodsSignature);
	}
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newInterfaceTypeSpec(TypeRef TRef, ExecSignature ... pMethodsSignature) {
		return this.newInterfaceTypeSpec(TRef, null, null, null, null, pMethodsSignature);
	}
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newInterfaceTypeSpec(TypeRef TRef, TypeRef[] pInterfaceRefs, TypeRef pTargetRef, final ParameterizedTypeInfo pTPInfo,
			ExecSignature ... pMethodsSignature) {
		return this.newInterfaceTypeSpec(TRef, pInterfaceRefs, pTargetRef, pTPInfo, null, pMethodsSignature);
	}
	/** Creats a TypeSpec for Interface Type */
	public TypeSpec newInterfaceTypeSpec(TypeRef TRef, TypeRef[] pInterfaceRefs, TypeRef pTargetRef, final ParameterizedTypeInfo pTPInfo,
			MoreData pExtraInfo, ExecSignature ... pMethodsSignature) {
		return this.newTypeSpec(TRef, pInterfaceRefs, pTargetRef, pTPInfo, pExtraInfo, true, pMethodsSignature);
	}
	
	/** Creates a TypeSpec for Interface Type */
	@SuppressWarnings("unlikely-arg-type")
    public TypeSpec newTypeSpec(TypeRef TRef, TypeRef[] pInterfaceRefs, TypeRef pTargetRef, final ParameterizedTypeInfo pTPInfo,
			MoreData pExtraInfo, boolean pIsStrict, ExecSignature ... pMethodsSignature) {
		
		TSInterface TSI = new TSInterface(TRef, pInterfaceRefs, pTargetRef, pTPInfo, pIsStrict, pExtraInfo);
		
		Vector<OperationInfo> Methods = new Vector<OperationInfo>();
		for(int i = 0; i < pMethodsSignature.length; i++) {
			ExecSignature MethodsSignature = pMethodsSignature[i];
			if(MethodsSignature == null)           continue;
			if(Methods.contains(MethodsSignature)) continue;
			Methods.add(
				// Add an abstract methods
				new OperationInfo.OIDirect(
					Type.Public,
					MethodsSignature,
					ExecKind.SubRoutine,
					null
				)
			);
		}
		
		// Assign the methods
		TSI.Datas[Index_ObjectOpers] = Methods;
		
		return TSI;
	}
	
	/** Creates a aTypeSpec creator for Interface type */
	public TypeSpecCreator getTypeSpecCreator(final ParameterizedTypeInfo pTPInfo,
			final TypeRef[] pInterfaceRefs, final TypeRef pTargetRef, final boolean pIsStrict) {
		return new TypeSpecCreator() {
			public TypeSpec newTypeSpec(Engine pEngine, TypeRef pTRef, boolean pIsVerify, Documentation pDocument) {
				MoreData EI = (pDocument == null)?null:new MoreData(Documentation.MIName_Documentation, pDocument);
				return getTypeSpec(pTRef, pInterfaceRefs, pTPInfo, pTargetRef, pIsStrict, EI);
			}
		};
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	// TypeKind --------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		return true;
	}
	
	// Type Construction -----------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected Type getType(Engine pEngine, Context pContext, TypeSpec pSpec) {
		return new TInterface(this, (TSInterface)pSpec);
	}

	// Type Initialization ---------------------------------------------------------------

	// PreInitialize
	/**{@inheritDoc}*/ @Override
	protected Exception doValidateType(Context pContext, Type pType) {
		
		// TODO - Ensure all interface target is valid
		
		// Make sure Super is the same kind
		
		// Make sure all interfaces are ... interfaces
		
		return null;
	}

	// Initialize
	
	/**{@inheritDoc}*/ @Override
	protected Exception initializeType(Context pContext, Type pType) {
		// Interface are added without checking
		Engine E = this.getEngine();
		if(E == null) E = pContext.getEngine();
		
		MType    MType  = E.getTypeManager();
		TypeSpec TSpec  = pType.getTypeSpec();
		int      ICount = TSpec.getInterfaceCount();
		for(int i = ICount; --i >= 0; ) {
			TypeRef TR = TSpec.getInterfaceRefAt(i);            if(TR == null) continue;
			Type T = MType.getTypeFromRefNoCheck(pContext, TR); if(T  == null) continue;
			if(!TKInterface.isTypeInterface(T))                                continue;
			
			// TOFIX
			// pType.addGoodInterfacesToType(T);
		}
		return null;
	}
	
	// Type and Class -----------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOf(Context pContext, Object pObject) {
		return null;
	}
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		return null;
	}

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TInterface.class;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getDefaultType(Context pContext) {
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pSpec) {
		// The data class is the class of the target
		TypeRef TR = ((TSInterface)pSpec).getTargetRef();
		if(TR == null) return Object.class;
		Type T = this.getEngine().getTypeManager().getTypeFromRefNoCheck(pContext, TR);
		return T.getDataClass();
	}

	// Compatibility ------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec, Object pByObject) {
		if(pByObject    == null) return true;
		if(pTheTypeSpec == null) return false;
		Type Type = this.getEngine().getTypeManager().getTypeOfNoCheck(pContext, pByObject);
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, pTheTypeSpec.getTypeRef(), Type.getTypeRef());
	}
		
	HashMap<Integer, Integer> Pairs = new HashMap<Integer, Integer>();

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec, TypeSpec BySpec) {
		TypeSpec SInterface = TheSpec;
		TypeSpec SType      = BySpec;
		if(SType      == null) return true;
		if(SInterface == null) return false;
		
		// NO Need to check other as it already checked by the TypeKind
		TypeRef TRInterface = SInterface.getTypeRef();
		TypeRef TRType      = SType     .getTypeRef();
		
		Engine $Engine = this.getEngine();
		MType  MT      = $Engine.getTypeManager();
		if(!SInterface.isResolved()) MT.ensureTypeExist(pContext, TRInterface);
		if(!SType     .isResolved()) MT.ensureTypeExist(pContext, TRType);
		return TKInterface.checkIfInterfaceImplementedBy(pContext, $Engine, TRType.getTheType(), TRInterface.getTheType(), false, false);
	}
	
	// Virtual Compatibility -------------------------------------------------------------------------------------------

	// Revert type checking ------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanTypeBeAssignedByTypeWith_Revert(Context pContext, Engine pEngine,
			TypeSpec TheSpec, TypeSpec BySpec) {
		if(!(BySpec instanceof TSInterface)) return false;
		
		TypeRef TargetRef = ((TSInterface)BySpec).getTargetRef();
		if(TargetRef == null) TargetRef = TKJava.TAny.getTypeRef();
		// If the type of TheSpec can be assigned by the Target of the BySpec then it can be assigned by this object
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TheSpec.getTypeRef(), TargetRef);
	}

	// Instantiation ---------------------------------------------------------------------

	/** Checks if the type pTheType is abstract. */
	@Override protected boolean isTypeAbstract(Context pContext, Type pTheType) { return true; }

	/**{@inheritDoc}*/ @Override
	protected Object getTypeDefaultValue(Context pContext, Type pTheType) { return null; }

	/**
	 * Creates a new instance of the type.<br />
	 * If isAbstract() returns true, this method will never be called.
	 * @param pSearch is a search key to find the right constructor. It can be 1) null for seach with the parameter, 2)
	 *            Type[] for searching with type, 3) TypeRef[] for searching with type name and 4) ExecInterface for
	 *            searching with an interface.
	 */
	@Override protected Object createNewTypeInstance(Context pContext, Executable pInitiator, Type pTheType,
				Object pSearchKey, Object[] pParams) { return null;	}
	
	// Dynamic Delegation --------------------------------------------------------------------------

	/** Returns the number of Dynamic Delegation */
	@Override protected int doType_getDynamicDelegationCount(Context pContext, Type pTheType) {
		return 0;
	}
	/** Returns the name of the Attribute for the delegation */
	@Override protected String doType_getDynamicDelegation(Context pContext, Type pTheType, int I) {
		return null;
	}
	/** Returns the type that this StackOwner need to be seen as to get the Delegation Object */
	@Override protected TypeRef doType_getDynamicDelegationAsType(Context pContext, Type pTheType, int I) {
		return null;
	}
	
	// Utilities -------------------------------------------------------------------------------------------------------

	/** Checks if a type is an interface */
	static final public boolean isTypeInterface(Type pType) {
		if(pType == null)               return false;
		if(pType instanceof TInterface) return true;
		Class<?> DClass = pType.getDataClass();
		if(DClass == null) return false;
		return DClass.isInterface();
	}
	static final public boolean isTypeStrictInterface(Type pType) {
		if(pType == null)               return false;
		if(pType instanceof TInterface) return ((TInterface)pType).isStrict();
		Class<?> DClass = pType.getDataClass();
		if(DClass == null) return false;
		return pType.getDataClass().isInterface();
	}
	/** Returns the target of the given interface type */
	static final public TypeRef getInterfaceTarget(Type pType) {
		if(pType instanceof TInterface) return ((TInterface)pType).getTargetRef();
		return TKJava.TAny.getTypeRef();
	}

	/** Checks if a type is an interface */
	static final public boolean isTypeRefInterface(Engine pEngine, TypeRef pTRef) {
		if((pTRef == null) || (pEngine == null)) return false;
		try { pEngine.getTypeManager().ensureTypeExist(null, pTRef); } catch(Exception E) {}
		if(!pTRef.isLoaded()) return false;
		return isTypeInterface(pTRef.getTheType());
	}
	static final public boolean isTypeRefStrictInterface(Engine pEngine, TypeRef pTRef) {
		if((pTRef == null) || (pEngine == null)) return false;
		try { pEngine.getTypeManager().ensureTypeExist(null, pTRef); } catch(Exception E) {}
		if(!pTRef.isLoaded()) return false;
		return isTypeStrictInterface(pTRef.getTheType());
	}
	/** Returns the target of the given interface type ref */
	static final public TypeRef getInterfaceTarget(Engine pEngine, TypeRef pTRef) {
		if((pTRef == null) || (pEngine == null)) return TKJava.TAny.getTypeRef();
		try { pEngine.getTypeManager().ensureTypeExist(null, pTRef); } catch(Exception E) {}
		if(!pTRef.isLoaded()) return TKJava.TAny.getTypeRef();
		return getInterfaceTarget(pTRef.getTheType());
	}
	
	// Check type -----------------------------------------------------------------------------

	/** Checks if the type implement the interface */
	static public final boolean isInterfaceImplementedBy(Engine pEngine, Type pTheType, Type pInterface) {
		
		// Check regularly first as the type may already be in the cache
		Boolean IsSpecMatch = MType.MightTypeSpecByAssignableByInstanceOf(null, pEngine, pTheType.getTypeSpec(), pInterface.getTypeSpec());
		if(IsSpecMatch != null) return IsSpecMatch;
		
		return TKInterface.checkIfInterfaceImplementedBy(null, pEngine, pTheType, pInterface, false, false);
	}	

	/** Checks if the given type can implements this interface and return error message if not. */
	final static boolean checkIfInterfaceImplementedBy(Context pContext, Engine pEngine, Type pTheType, Type pInterface,
			boolean pIsIgnoreStrict, boolean pIsThrowError) {

		// Precondition - Just in case
		if((pTheType == null) || (pInterface == null) || !isTypeInterface(pInterface)) return false;
		if((pTheType == pInterface) || pTheType.equals(pInterface))                    return true;

		// Both are Java - Check in Java style
		if((pTheType instanceof TKJava.TJava) && (pInterface instanceof TKJava.TJava))
			return pInterface.getDataClass().isAssignableFrom(pTheType.getDataClass());

		
		// TODELETE - Delete when sure
		//if(pTheType.isAbstract(pContext) && !TKInterface.isTypeStrictInterface(pInterface)) return true;
		

		// Ensure Engine is not null
		if((pEngine == null) && ((pEngine = pInterface.getEngine()) == null)) pEngine = pTheType.getEngine();

		// Ensure both types are validated 
		MType MT = pEngine.getTypeManager();
		if(pIsThrowError) {
			if(!pInterface.isValidated()) MT.ensureTypeValidated(pContext, pInterface, null);
			if(!pTheType  .isValidated()) MT.ensureTypeValidated(pContext, pTheType,   null);
		} else {
			try {
				if(!pInterface.isValidated()) MT.ensureTypeValidated(pContext, pInterface, null);
				if(!pTheType  .isValidated()) MT.ensureTypeValidated(pContext, pTheType,   null);
			} catch (Exception E) {}
		}
		

		// The Target Type must match -----------------------------------------------
		TypeRef TTRef = getInterfaceTarget(pInterface);
		if((TTRef != null) && !MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TTRef, pTheType.getTypeRef()))
			return false;
		
		
		// All interface of this type must match ------------------------------------
		TypeSpec ISpec = pInterface.getTypeSpec();
		
		// Parameterized interface type cannot be assigned by a Java type
		if(ISpec.isParameterized() && (pTheType instanceof TKJava.TJava))
			return false;
		
		int ICount = ISpec.getInterfaceCount();
		for(int i = 0; i < ICount; i++) {
			TypeRef TR = ISpec.getInterfaceRefAt(i);
			if(TR == null) continue;

			Type T = MT.getTypeFromRefNoCheck(pContext, TR);
			//if(!MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, TR, pTheType.getTypeRef()))
			//	return false;
			if(TKInterface.checkIfInterfaceImplementedBy(pContext, pEngine, T, pTheType, false, pIsThrowError))
				return false;
		}
		
		if(!pIsIgnoreStrict && (!(ISpec instanceof TSInterface) || ((TSInterface)ISpec).isStrict()))
			return false;

		// Check each elements ------------------------------------------------------
		// Prepare Declared Signature
		OperationInfo[] TheTypeOIs         = pTheType.getObjectOperationInfos();
		ExecSignature[] TheTypeDeclaredESs = (TheTypeOIs == null)
		                                         ? ExecSignature.EmptyExecSignatureArray
		                                         : new ExecSignature[TheTypeOIs.length];
		for(int i = 0; i < TheTypeDeclaredESs.length; i++) {
			OperationInfo TheTypeOI = TheTypeOIs[i];
			if(TheTypeOI == null) continue;
			TheTypeDeclaredESs[i] = TheTypeOI.getDeclaredSignature();
		}
		
		
		// Check Operation
		OperationInfo[] OIs = pInterface.getObjectOperationInfos();
		var notFounds = new ArrayList<OperationInfo>();
		for(OperationInfo OI : OIs) {
			if(OI == null) continue;
			// Operation from Object are everywhere so there is no need to check
			if(OI.getRKind().isNative() && (OI.asNative().getMethod().getDeclaringClass() == Object.class)) continue;
			ExecSignature Required_ES = OI.getDeclaredSignature();
			
			OperationInfo Found____OI = null; {				
				int hashI = Required_ES.hash_WithoutParamNamesReturnType();
				for(int i = 0; i < TheTypeDeclaredESs.length; i++) {
					ExecSignature TypeOIES = TheTypeDeclaredESs[i];
					int           hashT    = TypeOIES.hash_WithoutParamNamesReturnType();
					if(hashI == hashT) {
						Found____OI = TheTypeOIs[i];
						break;
					}
				}
			}
			
			boolean IsFound = (Found____OI != null);
						
			if(IsFound) {
				// Ensure it is public
				if(!Found____OI.getAccessibility().isPublic()) {
					throw new CurryError("The type " + pTheType + " must implement the public operation `" +
							Found____OI.getAccessibility() + " " + Found____OI.getSignature().toString() +
							"` in order to implement the interface "+pInterface.toString()+".");
				}
			}
			
			if(!IsFound) {
			    notFounds.add(OI);
			}
		}
		if (!notFounds.isEmpty()) {
	        if(!pIsThrowError) return false;
	        
            var oiList
                    = notFounds
                    .stream()
                    .map(OperationInfo::getDeclaredSignature)
                    .map(String::valueOf)
                    .map("        "::concat)
                    .collect(joining("\n"));
            var errMsg = format(
                "The type '%s' must implement the following operations in order to implement the interface '%s': \n%s",
                pTheType, pInterface, oiList
            );
            throw new CurryError(errMsg, pContext);
		}

		// Check Attribute
		AttributeInfo[] AIs = pInterface.getObjectAttributeInfos();
		for(AttributeInfo AI : AIs) {
			if(AI == null) continue;
			if(AI.getRKind().isNative() && (AI.asNative().getField().getDeclaringClass() == Object.class)) continue;
			
			TypeRef TR = pTheType.searchObjectAttribute(pEngine, AI.getName());
			if(TR == null) {
				if(!pIsThrowError) return false;
				throw new CurryError("The type " + pTheType + " must implement the operation " +
						AI.getName() + " in order to implement the interface "+pInterface.toString()+".");
			}
			AttributeInfo LocalAI = pTheType.getTypeInfo().getObjectAttributeInfo(AI.getName(), null);
			if(!LocalAI.getReadAccessibility().isPublic())
				throw new CurryError("The type " + pTheType + " must implement the public operation `" +
						LocalAI.getReadAccessibility() + " " + LocalAI.getName() + "` in order to implement the interface " +
						pInterface.toString()+".");
		}
		
		return true;
	}
	
	// Cache for java interface ----------------------------------------------------------------------------------------
	
	static private final HashMap<Type, Class<?>[]> JavaInterfaces = new HashMap<Type, Class<?>[]>();
		
	/** Get the array of Java Interfaces of a class */
	static Class<?>[] getJavaInterfaces(Engine pEngine, Type T) {
		if(T instanceof TKJava.TJava) {
			Class<?>   C  = ((TKJava.TJava)T).getDataClass();
			Class<?>[] Is = C.getInterfaces();
			if(C.isInterface()) {
				HashSet<Class<?>> JIs = new HashSet<Class<?>>();
				JIs.add(C);
				if((Is != null) && (Is.length != 0))
					JIs.addAll(Arrays.asList(Is));
				return JIs.toArray(UClass.EmptyClassArray);
			}
			return Is;
		}
		
		Class<?>[] Is = JavaInterfaces.get(T);
		if(Is != null) return Is;
		
		HashSet<Class<?>> JIs = new HashSet<Class<?>>();
		MType             MT  = pEngine.getTypeManager();
		
		if(isTypeInterface(T) && (T instanceof TKJava.TJava))
			JIs.add(T.getDataClass());
		
		TypeRef SuperRef = T.getTypeSpec().getSuperRef();
		if(SuperRef != null) {
			Type Super = MT.getTypeFromRefNoCheck(null, SuperRef);
			
			Is = getJavaInterfaces(pEngine, Super);
			if((Is != null) && (Is.length != 0))
				JIs.addAll(Arrays.asList(Is));
			/*
			for(int i = (Is == null)?0:Is.length; --i >= 0; ) {
				TypeRef TR = Is[i];
				if(TR == null)
					continue;
				
				Type TI = MT.getTypeFromRefNoCheck(null, TR);
				if(TI == null) continue;
				
				Class<?>[] Cs = getJavaInterfaces(pEngine, TI);
				if(Cs != null) JIs.addAll(Arrays.asList(Cs));
				
				if(!(TI instanceof TKJava.TJava))
					continue;

				Class<?> C = TI.getDataClass();
				if(!C.isInterface()) continue;
				JIs.add(C);
			}*/
		}
		
		TypeSpec TS = T.getTypeSpec();
		for(int i = TS.getInterfaceCount(); --i >= 0; ) {
			Type TI = MT.getTypeFromRefNoCheck(null, TS.getInterfaceRefAt(i));
			
			Class<?>[] Cs = getJavaInterfaces(pEngine, TI);
			if(Cs != null) JIs.addAll(Arrays.asList(Cs));
			
			if(!(TI instanceof TKJava.TJava))
				continue;

			Class<?> C = TI.getDataClass();
			if(!C.isInterface()) continue;
			JIs.add(C);
		}
		
		Is = JIs.toArray(UClass.EmptyClassArray);
		JavaInterfaces.put(T, Is);
		return Is;
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	// Other Classes ---------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * TypeSpec for Interface
	 * 
	 * In curry, interface can can have Target (type that is indented to use)
	 * 
	 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
	 */
	static public class TSInterface extends TypeSpec {

        private static final long serialVersionUID = 7010193114051097113L;

		// Constructor and verification ---------------------------------------------------------------

		protected TSInterface(TypeRef pTRef, TypeRef[] pInterfaceRefs, TypeRef pTargetRef, ParameterizedTypeInfo pTPInfo,
				boolean pIsStrict, MoreData pExtraInfo) {
			super(pTRef);

			// Datas -----------------------------------------------------------------------------------
			Serializable[] TheDatas = new Serializable[IndexCount];

			TheDatas[Index_Kind]       = TKInterface.KindName;
			TheDatas[Index_Interfaces] = (pInterfaceRefs == null)?null:pInterfaceRefs.clone();
			
			TheDatas[Index_Parameterization] = pTPInfo;
			
			TheDatas[Index_Target]   = pTargetRef;
			TheDatas[Index_IsStrict] = pIsStrict;

			// The rest of the data should be added or set via TypeBuilder

			TheDatas[Index_ExtraInfo] = pExtraInfo;

			this.Datas = TheDatas;

			// Required and Used Types -----------------------------------------------------------------

			Vector<TypeRef> RTs = new Vector<TypeRef>();
			RTs.add(pTargetRef);
			if(pInterfaceRefs != null)
				RTs.addAll(Arrays.asList((TypeRef[])(pInterfaceRefs.clone())));
			
			Vector<TypeRef> UTs = new Vector<TypeRef>();
			if(pTPInfo != null) {
				for(int i = pTPInfo.getParameterTypeCount(); --i >= 0; )
					UTs.add(pTPInfo.getParameterTypeRef(i));
				UTs.toArray(this.UsedTypes = new TypeRef[UTs.size()]);
			}
			
			this.RequiredTypes = (RTs.size() == 0) ? null : RTs.toArray(new TypeRef[RTs.size()]);
			this.UsedTypes     = (UTs.size() == 0) ? null : UTs.toArray(new TypeRef[UTs.size()]);
		}
	
		// Classification -----------------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		public String  getKindName() {
			return (String)this.getData(Index_Kind);
		}

		/** Returns the TypeRef of the target type */
		final public TypeRef getTargetRef() {
			return (TypeRef)this.getData(Index_Target);
		}
		/** Checks if the interface required explicit declaration */
		final public boolean isStrict() {
			return Boolean.TRUE.equals(this.getData(Index_IsStrict));
		}

		// Interface ----------------------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		protected int getInterfaceIndex() {
			return Index_Interfaces;
		}
		
		// Parameterization related ----------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		protected int getParameterizationInfoIndex() {
			return Index_Parameterization;
		}
		
		/**{@inheritDoc}*/ @Override
		protected void resolveParameteredTypeSpec(Context pContext, Engine pEngine) {
			super.resolveParameteredTypeSpec(pContext, pEngine);

			boolean IsThisAParameterized = this.isParameterized();
			boolean IsThisAParametered   = this.isParametered();
			boolean IsNoFlat             = false;
			
			if(IsThisAParametered) {
				TypeRef[] PTRefs = ((TRParametered)this.getTypeRef()).ParamTypeRefs;
				for(TypeRef PTRef : PTRefs) {
					if((PTRef instanceof TRParameter) && !Objects.equals(((TRParameter)PTRef).getBaseTypeRef(),this.getTypeRef())) {
						IsNoFlat = true;
						break;
					}
				}
			}
			
			TypeRef TargetRef = (TypeRef)this.Datas[Index_Target];
			if(TargetRef instanceof TRBasedOnType) {
				this.Datas[Index_Target] = (TargetRef = this.newBaseOnTypeTypeRef(pEngine, (TRBasedOnType)TargetRef));
				
				if(!IsNoFlat && (TargetRef instanceof TRBasedOnType) && !IsThisAParameterized)
					this.Datas[Index_Target] = (TargetRef = ((TRBasedOnType)TargetRef).flatType(pEngine, null, null));
			}
			
			TypeRef[] Interfaces = (TypeRef[])this.Datas[Index_Interfaces];
			if(Interfaces != null) {
				Interfaces = IsThisAParametered ? (TypeRef[])Interfaces : (TypeRef[])Interfaces.clone();
				for(int i = 0; i < Interfaces.length; i++) {
					TypeRef IRef = Interfaces[i];
					if(!(IRef instanceof TRBasedOnType)) continue;
					Interfaces[i] = (IRef = this.newBaseOnTypeTypeRef(pEngine, (TRBasedOnType)IRef));
					
					if(!IsNoFlat && (IRef instanceof TRBasedOnType) && !IsThisAParameterized)
						Interfaces[i] = (IRef = ((TRBasedOnType)IRef).flatType(pEngine, null, null));
				}
			}
		}
		
		// MoreData and ExtraInfo ---------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		final protected int getExtraInfoIndex() {
			return Index_ExtraInfo;
		}

		// StackOwner ---------------------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		final protected int getDataOperationInfoIndex() {
			return Index_ObjectOpers;
		}
		/**{@inheritDoc}*/ @Override
		final protected int getTypeOperationInfoIndex() {
			return Index_TypeOpers;
		}
		
		/**{@inheritDoc}*/ @Override
		final protected int getDataAttributeInfoIndex() {
			return Index_ObjectAttrs;
		}
		/**{@inheritDoc}*/ @Override
		final protected int getTypeAttributeInfoIndex() {
			return Index_TypeAttrs;
		}
		
		// For compilation only --------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForCompilation() {
			Util.ResetTypeRefs(this.getTargetRef());
			Util.ResetTypeRefs(this.getInterfaces());
		}

		// Parameterization --------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForParameterization() {}

		// Representation -------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		protected boolean isToShowNoName() {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		/**{@inheritDoc}*/ @Override
		protected String getToString() {
			if(this.getTypeRef() instanceof TRNoName) {
				StringBuilder SB = new StringBuilder();
				if(this.isStrict())
					 SB.append("Interface");
				else SB.append("Duck");
				
				SB.append(":<");
				
				if(this.getTargetRef() != null) {
					SB.append("(");
					SB.append(this.getTargetRef());
					SB.append(")");
				}
				
				if(this.getInterfaceCount() != 0) {
					SB.append("[");
					for(int i = 0; i < this.getInterfaceCount(); i++) {
						if(SB.length() != 0) SB.append(",");
						SB.append(this.getInterfaceRefAt(i));
					}

					SB.append("]");
				}
				
				SB.append("{");
				Vector<OperationInfo> OIs = (Vector<OperationInfo>)this.getData(Index_ObjectOpers);
				int OICount   = ((OIs == null)?0:OIs.size());
				int LastIndex = OICount - 1;
				for(int i = 0; i < OICount; i++) {
					if(i != 0) SB.append(" ");
					SB.append(OIs.get(i).getDeclaredSignature());
					if(i != LastIndex) SB.append(";");
				}
				SB.append("}");
				
				SB.append(">");
				
				return SB.toString();
			}
			
			return this.getKindName() + ":=:"+ this.hashCode();
		}

		/**{@inheritDoc}*/ @Override
		protected String getDescriptionDetail(Engine pEngine) {			
			StringBuffer SB = new StringBuffer();
			
			if(this.isStrict())
				 SB.append("Interface");
			else SB.append("Duck");
			
			SB.append(":=:").append(this.hashCode());
			
			if(this.getTargetRef() != null) {
				SB.append("(");
				SB.append(this.getTargetRef());
				SB.append(")");
			}
			
			if(this.getInterfaceCount() != 0) {
				SB.append("[");
				for(int i = 0; i < this.getInterfaceCount(); i++) {
					if(i != 0) SB.append(",");
					SB.append(this.getInterfaceRefAt(i));
				}

				SB.append("]");
			}
			
			TypeParameterInfos PTInfo = this.getTypeParameterInfos();
			if(PTInfo != null) SB.append(PTInfo);
			
			return SB.toString();
		}
	}
	
	/**
	 * Type of Interface
	 * 
	 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
	 */
	static public class TInterface extends Type {

		// Construction --------------------------------------------------------------------------------

		protected TInterface(TKInterface pTKind, TSInterface pTSpec) {
			super(pTKind, pTSpec);
		}
	
		// Classification -----------------------------------------------------------------------------

		/** Returns the kind name of this type. */
		final public String  getKindName()  {
			return ((TSInterface)this.getTypeSpec()).getKindName();
		}
		/** Returns the spec of this type. */
		final public TSInterface getTSInterface() {
			return ((TSInterface)this.getTypeSpec());
		}

		/** Returns the TypeRef of the target type */
		final public TypeRef getTargetRef() {
			TypeRef TRS = this.getTSInterface().getTargetRef();
			if(TRS == null) return TKJava.TAny.getTypeRef();
			return TRS;
		}
		/** Returns the type of the target type */
		final public Type getTarget() {
			TypeRef TRS = this.getTSInterface().getTargetRef();
			if(TRS == null) return TKJava.TAny;
			this.getEngine().getTypeManager().ensureTypeInitialized(TRS);
			return TRS.getTheType();
		}

		/** Checks if the interface is strict (must be explicitly defined as implementation in order to be compatible) */
		final public boolean isStrict() {
			return this.getTSInterface().isStrict();
		}

		/** Checks if the given type implements this interface (if this interface is not-strict, check if the type can implement) */
		final public boolean isImplementedBy(Type pTheType) {
			return TKInterface.checkIfInterfaceImplementedBy(null, null, pTheType, this, false, false);
		}
	}
}
