package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.curry.StackOwner.OperationSearchKind;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UString;

/**
 * Provide an information about a Type
 */
public class TypeInfo extends StackOwnerInfo {
	
	public TypeInfo(Type pType) {
		super(pType);
	}
	
	/** Returns the Type that this object is helping */
	final public Type getType() {
		return (Type)this.SO;
	}

	/** Returns the type kind of this type. */
	final public TypeKind getTypeKind() {
		return this.getType().getTypeKind();
	}

	/** Returns the name of the kind this type is. */
	final public String getTypeKindName() {
		return this.getType().getTypeKindName();
	}

	// Spec and Ref ----------------------------------------------------------------------

	/** Returns the spec of this type. */
	final public TypeSpec getTypeSpec() {
		return this.getType().getTypeSpec();
	}

	/** Returns the type ref of this type */
	final public TypeRef getTypeRef() {
		return this.getType().getTypeRef();
	}

	/** Returns the NameSpace that hold this type */
	final public Package getPackage() {
		Engine E = this.getType().getEngine();
		if(E == null) return null;
		return E.getPackageOf(this);
	}

	/** Returns the code location of this type */
	final public Location getLocation() {
		return this.getLocation(null);
	}

	/** Returns the code location of this type */
	final public Location getLocation(Engine pEngine) {
		return this.getType().TSpec.getLocation(pEngine);
	}

	/** Returns the documentation of this type */
	final public Documentation getDocumentation() {
		return this.getType().TSpec.getDocumentation();
	}

	// Current and Parameterized ------------------------------------------------------------------

	/**
	 * Returns type reference of the parametered type of this type.
	 * 
	 * NOTE: This method will throw IllegalArgumentException when something go wrong.
	 *           Use TLParametered.TRParameter constructors if exception (at the create time) is not wanted.
	 **/
	final public TypeRef getParameteredTypeRef(Engine pEngine, TypeRef ... pPTypeRefs) {
		Engine E = pEngine;
		if(E == null) E = this.getType().getEngine();
		
		if(!this.isLoaded()) E.getTypeManager().ensureTypeInitialized(this.getType());
		return this.getTypeSpec().getParameteredTypeRef(pEngine, pPTypeRefs);
	}
	/**
	 * Returns type reference of the parametered type of this type.
	 * 
	 * NOTE: This method will not throw any error. If the parameter is not compatible, the error will be thrown at
	 *           initialization time.
	 **/
	final public TLParametered.TRParametered getParameteredTypeRef(TypeRef ... pPTypeRefs) {
		return new TLParametered.TRParametered(this.getType(), pPTypeRefs);
	}

	// Current and Parameterized ------------------------------------------------------------------
	
	/** Checks if the type is parameterized */
	final public boolean isParameterized(Engine pEngine) {
		if(!this.isLoaded()) {
			Engine E = pEngine; if(E == null) E = this.getType().getEngine();
			E.getTypeManager().ensureTypeInitialized(this.getType());
		}
		return this.getTypeSpec().isParameterized();
	}
	/** Checks if the type is parametered */
	final public boolean isParametered(Engine pEngine) {
		if(!this.isLoaded()) {
			Engine E = pEngine; if(E == null) E = this.getType().getEngine();
			E.getTypeManager().ensureTypeInitialized(this.getType());
		}
		return this.getTypeSpec().isParametered();
	}
	
	/** Returns the ParameterizationInfo of this type */
	final public ParameterizedTypeInfo getParameterizedTypeInfo(Engine pEngine) {
		if(!this.isLoaded()) {
			Engine E = pEngine; if(E == null) E = this.getType().getEngine();
			E.getTypeManager().ensureTypeInitialized(this.getType());
		}
		return this.getTypeSpec().getParameterizedTypeInfo();
	}
	/** Returns the TypeParameteredInfo of this type */
	final public ParameteredTypeInfo getParameteredTypeInfo(Engine pEngine) {
		if(!this.isLoaded()) {
			Engine E = pEngine; if(E == null) E = this.getType().getEngine();
			E.getTypeManager().ensureTypeInitialized(this.getType());
		}
		return this.getTypeSpec().getParameteredTypeInfo();
	}

	// Resolution and Initialization -----------------------------------------------------

	/** Returns the status of this type */
	final public TypeSpec.Status getStatus() {
		return this.getType().getStatus();
	}
	/** Checks if this type is unloaded */
	final public boolean isUnloaded() {
		return this.getType().isUnloaded();
	}
	/** Checks if this type is loaded */
	final public boolean isLoaded() {
		return this.getType().isLoaded();
	}
	/** Checks if this type is resolved */
	final public boolean isResolved() {
		return this.getType().isResolved();
	}
	/** Checks if this type is initialized */
	final public boolean isInitialized() {
		return this.getType().isInitialized();
	}
	
	// Interface ----------------------------------------------------------------------------------

	/** Returns the number of interface this type is defined to have */
	final public int     getInterfaceCount()      { return this.getType().getTypeSpec().getInterfaceCount();  }
	/** Returns the type of the interface at the index */
	final public TypeRef getInterfaceRefAt(int I) { return this.getType().getTypeSpec().getInterfaceRefAt(I); }

	// Typing ---------------------------------------------------------------------------

	// Type Checking -----------------------------------------------------------
	/**
	 * Checks if a variable of this type can be assigned by the object pObject.
	 * 
	 * @param pObject the object to check
	 * @return true if the input object is allowed to be assigned to a variable of this type
	 */
	final public boolean canBeAssignedBy(Object pObject) {
		return this.getType().canBeAssignedBy(null, pObject);
	}

	/**
	 * Checks if a variable of this type can be assigned by an instance of the type pType.
	 * 
	 * @param pObject the type to check
	 * @return true if an instance of the input type is allowed to be assigned to a variable of this type
	 */
	final public boolean canBeAssignedByInstanceOf(Type pType) {
		return this.getType().canBeAssignedByInstanceOf(null, pType);
	}

	/** Returns the data class of this type or null if there is no specific class for the type. */
	final public Class<?> getDataClass() {
		return this.getType().TKind.getTypeDataClass(null, this.getTypeSpec());
	}
	/** Returns the class of this type. */
	final public Class<? extends Type> getTypeClass() {
		return this.getType().getClass();
	}
	
	// Initializers ------------------------------------------------------------
	
	/** Returns the interfaces of ConstructorInfos in this types */
	final public ConstructorInfo[] getConstructorInfos() {
		ConstructorInfo[] Ins = this.getType().getConstructorInfos(null);
		if(Ins == null) return null;
		return Ins.clone();
	}

	// Instantiation -----------------------------------------------------------

	/** Checks if this type is final (cannot inherit). */
	final public boolean isDerivable() {
		return this.getTypeKind().isTypeDerivable(null, this.getType());
	}

	/** Checks if this type is abstract (cannot construct an instance). */
	final boolean isDerivable(Context pContext) {
		return this.getTypeKind().isTypeDerivable(pContext, this.getType());
	}
		
	/** Checks if this type is abstract (cannot construct an instance). */
	final public boolean isAbstract() {
		return this.getTypeKind().isTypeAbstract(null, this.getType());
	}

	/**
	 * Returns the default value of this type.<br/> The default value will be used when a variable is created and when
	 * an array of the type is created.
	 */
	final public Object getDefaultValue() {
		return this.getType().getDefaultValue(null);
	}
	/** Returns the default value that is not null if such value exist */
	final public Object getNoNullDefaultValue() {
		return this.getType().getNoNullDefaultValue(null);
	}
	
	// Search for constructor --------------------------------------------------
	
	/** Creates a new instance of this type in the Context using the parameter. */
	final public ExecInterface searchConstructor(Object[] pSearchKey) {
		return this.searchConstructor(((Type)this.SO).getEngine(), pSearchKey);
	}
	/** Creates a new instance of this type in the Context using the parameter. */
	final public ExecInterface searchConstructor(TypeRef[] pSearchKey) {
		return this.searchConstructor(((Type)this.SO).getEngine(), pSearchKey);
	}
	/** Creates a new instance of this type in the Context using the parameter. */
	final public ExecInterface searchConstructor(ExecInterface pSearchKey)  {
		return this.searchConstructor(((Type)this.SO).getEngine(), pSearchKey);
	}
	
	/** Creates a new instance of this type in the Context using the parameter. */
	final public ExecInterface searchConstructor(Engine pEngine, Object[] pSearchKey) {
		return this.getType().searchConstructorLocal(pEngine, pSearchKey);
	}
	/** Creates a new instance of this type in the Context using the parameter. */
	final public ExecInterface searchConstructor(Engine pEngine, TypeRef[] pSearchKey) {
		return this.getType().searchConstructorLocal(pEngine, pSearchKey);
	}
	/** Creates a new instance of this type in the Context using the parameter. */
	final public ExecInterface searchConstructor(Engine pEngine, ExecInterface pSearchKey)  {
		return this.getType().searchConstructorLocal(pEngine, pSearchKey);
	}
	
	// Representation and other -----------------------------------------------
	/** Returns a description of this type. */
	final public String getDescription() {
		return this.getType().getDescription();
	}
	
	/** Return MoreData of this type.  */
	final public Serializable getMoreData(String pName) {
		MoreData MD = this.getTypeSpec().getMoreData();
		return (MD == null)?null:MD.getData(pName);
	}

	/** Returns ExtraInfo of this type. */
	final public Serializable getExtraInfo(String pName) {
		MoreData MD = this.getTypeSpec().getExtraInfo();
		return (MD == null)?null:MD.getData(pName);
	}
	
	// Super -----------------------------------------------------------------------------------------------------------
	
	/** Checks if a type has a super */
	static public boolean isTypeHasSuper(Engine pEngine, TypeRef TRef) {
		if(TRef == null) return false;
		try { pEngine.getTypeManager().ensureTypeInitialized(TRef); } catch(Exception E) { return false; }
		Type    T  = TRef.getTheType();
		TypeRef TR = T.getTypeSpec().getSuperRef();
		return (TR != null) && !TR.equals(TKJava.TAny.getTypeRef());
	}
	/** Checks if a type referred by the given TypeRef has a super */
	static public boolean isTypeHasSuper(Engine pEngine, Type T) {
		if(T == null) return false;
		TypeRef TR = T.getTypeSpec().getSuperRef();
		return (TR != null) && !TR.equals(TKJava.TAny.getTypeRef());
	}
	
	/** Return the type ref of the super of the given type */
	static public TypeRef getSuperRefOf(Engine pEngine, TypeRef TRef) {
		if(TRef == null) return null;
		try { pEngine.getTypeManager().ensureTypeInitialized(TRef); } catch(Exception E) { return null; }
		Type T = TRef.getTheType();
		return T.getTypeSpec().getSuperRef();
	}
	/** Return the type ref of the super of the given type */
	static public TypeRef getSuperRefOf(Engine pEngine, Type T) {
		if(T == null) return null;
		return T.getTypeSpec().getSuperRef();
	}
	
	// Not Null Attribute --------------------------------------------------------------------------
	
	/** Checks if the 'NotNull' features is now on */
	public boolean isEnforceNotNull() {
		return this.getType().isEnforceNotNull();
	}

	// ---------------------------------------------------------------------------------------------
	// Handle A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	/** Checks if the package variable `pVName` exist. */
	public boolean isTypeFieldExist(String pVName) {
		return this.getType().isTypeFieldExist(pVName);
	}

	/** Checks if the function `pES` exist. */
	public boolean isTypeMethodExist(ExecSignature pES) {
		return this.getType().isTypeMethodExist(pES);
	}
	
	// High-Level search to object element information ----------------------------------------------

	/** Search attribute of this StackOwner as the type */
	final public TypeRef searchTypeAttribute(Engine pEngine, String pName) {
		return this.getType().searchTypeAttribute(pEngine, pName);
	}
	
	/** Search operation of this StackOwner as the type using name and parameters */
	final public ExecSignature searchTypeOperation(Engine pEngine, String pOName, Object[] pParams) {
		return this.getType().searchTypeOperation(pEngine, pOName, pParams);
	}

	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final public ExecSignature searchTypeOperation(Engine pEngine, String pOName, TypeRef[] pPTypeRefs) {
		return this.getType().searchTypeOperation(pEngine, pOName, pPTypeRefs);
	}

	/** Search operation of this StackOwner as the type using name and interface */
	final public ExecSignature searchTypeOperation(Engine pEngine, String pOName, ExecInterface pExecInterface) {
		return this.getType().searchTypeOperation(pEngine, pOName, pExecInterface);
	}

	/**
	 * Search operation of this StackOwner as the type using name and signature (if the signature are exact match, you
	 * can use it to execute)
	 **/
	final public ExecSignature searchTypeOperation(Engine pEngine, ExecSignature pExecSignature) {
		return this.getType().searchTypeOperation(pEngine, pExecSignature);
	}
	
	/** Returns an array of all the non-dynamic operation info */
	final public OperationInfo[] getObjectOperationInfos() {
		OperationInfo[] OIs = this.getType().getObjectOperationInfos();
		return (OIs != null) ? OIs.clone() : null;
	}
	/** Returns an array of all the non-dynamic attribute info */
	final public AttributeInfo[] getObjectAttributeInfos() {
		AttributeInfo[] AIs = this.getType().getObjectAttributeInfos();
		return (AIs != null) ? AIs.clone() : null;
	}
	
	// High-Level search to object element information ----------------------------------------------

	/** Search attribute of this StackOwner as the type */
	final public TypeRef searchObjectAttribute(Engine pEngine, String pAName) {
		return this.getType().searchObjectAttribute(pEngine, pAName);
	}
	
	/** Search operation of this StackOwner as the type using name and parameters */
	final public ExecSignature searchObjectOperation(Engine pEngine, String pOName, Object[] pParams,
			Object[][] pAdjParams) {
		return this.getType().searchObjectOperation(pEngine, pOName, pParams, pAdjParams);
	}

	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final public ExecSignature searchObjectOperation(Engine pEngine, String pOName, TypeRef[] pPTypeRefs) {
		return this.getType().searchObjectOperation(pEngine, pOName, pPTypeRefs);
	}

	/** Search operation of this StackOwner as the type using name and interface */
	final public ExecSignature searchObjectOperation(Engine pEngine, String pOName, ExecInterface pExecInterface) {
		return this.getType().searchObjectOperation(pEngine, pOName, pExecInterface);
	}

	/**
	 * Search operation of this StackOwner as the type using name and signature (if the signature
	 * are exact match, you can use it to execute)
	 */
	final public ExecSignature searchObjectOperation(Engine pEngine, ExecSignature pExecSignature) {
		return this.getType().searchObjectOperation(pEngine, pExecSignature);
	}
	
	// Specification ------------------------------------------------------------------------------
	
	/** Atrtibute Specification */
	final public AttributeInfo getObjectAttributeInfo(String pVName, Type pAsType) {
		if(pVName == null) return null;
		
		AttributeInfo[] AIs = ((Type)this.SO).getObjectAttributeInfos();
		if(AIs == null) return null;
		
		int hSearch = UString.hash(pVName);
		for(int i = AIs.length; --i >= 0; ) {
			if(hSearch != AIs[i].getNameHash()) continue;
			return AIs[i];
		}
		return null;
	}
	/** Operation Specification */
	final public OperationInfo getObjectOperationInfo(ExecSignature pSignature) {
		if(pSignature == null) return null;
		OperationInfo[] OIs = ((Type)this.SO).ObjOperInfos;
		if(OIs        == null) return null;
		int hSearch = pSignature.hash_WithoutParamNamesReturnType();
		for(int i = OIs.length; --i >= 0; ) {
			ExecSignature S = OIs[i].getSignature();
			int           h = S.hash_WithoutParamNamesReturnType();
			if(hSearch != h) continue;
			return OIs[i];
		}
		return null;
	}

	// Display information ---------------------------------------------------------------

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final public String doData_getAttributeAccessToString(DObject pTheObject,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName, Object pParam1, Object pParam2) {
		return this.getType().doData_getAttributeAccessToString(pTheObject, pAKind, pAsType, pAttrName, pParam1, pParam2);
	}

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final public String doData_getAttributeAccessToString(DObject pTheObject,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName) {
		return this.getType().doData_getAttributeAccessToString(pTheObject, pAKind, pAsType, pAttrName);
	}

	/** Display the operation access as a string */
	final public String doData_getOperationAccessToString(DObject pTheObject,
			OperationSearchKind pOSKind, Type pAsType, Object pParam1, Object pParam2) {
		return this.getType().doData_getOperationAccessToString(pTheObject, pOSKind, pAsType, pParam1, pParam2);
	}
}
