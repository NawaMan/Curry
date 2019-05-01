package net.nawaman.curry;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import net.nawaman.curry.StackOwner.OperationSearchKind;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.Objectable;

/** The reference to a type */
abstract public class TypeRef implements Serializable, Comparable<TypeRef>, Objectable, Cloneable {
	
	static private final long serialVersionUID = 5693154654651654354L;
	
	static public final TypeRef[] EmptyTypeRefArray = new TypeRef[0];

	// Classification --------------------------------------------------------------------
	
	/** Returns the kind of type reference of this type ref. */	
	abstract public String getRefKindName();
	
	// Resolution ------------------------------------------------------------------------
	
	/** The Type */
	private transient Type TheType = null;
	// Set the result type
	void setTheType(Type pTheType) {
		this.TheType = pTheType;
	}

	/** Resets this type ref compilation */
	protected void resetTypeRefForCompilation() {}
	
	/** Resets the type compilation */
	final public void resetForCompilation() {
		this.TheType = null;
		this.resetTypeRefForCompilation();
	}
	
	// Get the result type
	final Type getTheType() {
		return this.TheType;
	}
	
	/** Checks if the ref has been resolved */
	final public boolean isLoaded() {
		return this.TheType != null;
	}
	
	/** Create the clone of this TypeRef */ @Override
	abstract public TypeRef clone();
	
	// Objectable -----------------------------------------------------------------------
	
	/** Checks if parameterized TypeRef has target this TypeRef, its should not display the parameterized infomation */
	protected boolean isToShowNoParametered() {
		return false;
	}
	
	/** Checks if O is the same or consider to be the same object with this object. */
	@Override abstract public String  toString();
	/** Returns the long string representation of the object. */
	          abstract public String  toDetail();
	/** Checks if O is the same or consider to be the same object with this object. */ 
	                   public boolean is(Object O) { return this.equals(O); }
	/** Checks if O equals to this object. */
	@Override abstract public boolean equals(Object O);
	/** Returns the integer representation of the object based on its value. */
	          abstract public int     hash();
	/** Returns the integer representation of the object. */  
	@Override          public int     hashCode() { return super.hashCode(); }

	/** Compare to another TypeRef */
	final public int compareTo(TypeRef TR) {
		return this.toString().compareTo((TR == null)?null:TR.toString());
	}
	
	// Serializable ----------------------------------------------------------------------------------------------------
	
	static boolean ShowWarning = false;
	
	/** Perform some operatin just before saving */
	private void writeObject(ObjectOutputStream out) throws IOException {
		// Reset the type
		this.resetForCompilation();
		
		if(!(out instanceof CurryOutputStream)) {
			if(!ShowWarning) {
				System.err.println(
					"TypeRef should be saved by `CurryOutputStream` or its refernce to type enclosed by a package " +
					"may not be properly resolved.");
				ShowWarning = true;
			}
		
		} else {
			CurryOutputStream POS = (CurryOutputStream)out;
			
			// Notify the CurryOutputStream that it is being written
			POS.notifyTypeRefWritten(this);
		} 
		
		// Save the rest
		out.defaultWriteObject();
	}
	
	// Dynamic ---------------------------------------------------------------------------------------------------------

	/** Checks if this TypeRef is Dynamic */
	public boolean isDynamic() {
		return false;
	}
	
	/** Checks if the given TypeRef is a Dynamic TypeRef */
	static public boolean isTypeRefDynamic(TypeRef pTRef) {
		if(pTRef == null) return false;
		return pTRef.isDynamic();
	} 

	// Delegate to type ------------------------------------------------------------------------------------------------
	
	/** Returns the type kind of this type. */
	final public TypeKind getTypeKind(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeExist(null, this);
		return this.getTheType().getTypeKind();
	}
	/** Returns the name of the kind this type is. */
	final public String getTypeKindName(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeExist(null, this);
		return this.getTheType().getTypeKindName();
	}

	/** Checks if the type kind of this type ref is named as TKName. */
	final public boolean isTypeKind(Engine pEngine, String TKName) {
		if(TKName  == null) return false;
		
		if(pEngine != null) pEngine.getTypeManager().ensureTypeExist(null, this);
		Type T = null;
		if((T = this.getTheType()) == null) return false;
		
		return TKName.equals(T.getTypeKindName());
	}
	/** Checks if the type kind of this type ref is named as TKName. */
	final public boolean isTypeKind(Engine pEngine, Class<? extends TypeKind> TKClass) {
		if(TKClass  == null) return false;
		
		if(pEngine != null) pEngine.getTypeManager().ensureTypeExist(null, this);
		Type T = null;
		if((T = this.getTheType()) == null) return false;
		
		return TKClass.isInstance(T.getTypeKind());
	}
	
	// TypeSpec --------------------------------------------------------------------------------------------------------
	
	/** Returns the type spec without a help from Engine or Returns null if that is not possible */
	protected TypeSpec getTypeSpecWithoutEngine() {
		if(this.isLoaded()) return this.getTheType().getTypeSpec();
		return null;
	}

	/** Returns the spec of this type. */
	final public TypeSpec getTypeSpec(Engine pEngine) {
		if(this.isLoaded()) return this.getTheType().getTypeSpec();
		TypeSpec TS = this.getTypeSpecWithoutEngine();
		if(TS != null) return TS;
		
		if(pEngine != null) {
			pEngine.getTypeManager().ensureTypeExist(null, this);
			return this.getTheType().getTypeSpec();
		}
		return this.getTypeSpecWithoutEngine();
	}
	/** Returns TypeSpec from a type ref */
	final TypeSpec getTypeSpec(Engine pEngine, Context pContext) {
		// TODO - Improve this, this may be moved or join with other
	
		// Already resolve so just return it
		if(this.isLoaded()) return this.TheType.getTypeSpec();
		
		// Not yet Result, try to find the quick way to do without resolving it
		
		if(this instanceof TLNoName.TRNoName) {
			TypeSpec TS = ((TLNoName.TRNoName)this).getTypeSpec();
			if(TS != null) return TS;
		}
		
		if(this instanceof TLPackage.TRPackage) {
			MUnit UM = pEngine.getUnitManager();
			if(UM == null) return null;
			String PName = ((TLPackage.TRPackage)this).getPackageName();
			String TName = ((TLPackage.TRPackage)this).getTypeName();

			TypeSpec TS = null;
			// Predefined type
			if(MType.PREDEFINEDTYPE_PACKAGENAME.equals(PName)) {
				if((TS = pEngine.getTypeManager().getPrefineTypeSpec(TName)) != null)
					return TS;
			}

			PackageBuilder PB = UM.getPackageBuilder(null, PName);
			TypeBuilder    TB;
			if((PB != null) && ((TB = PB.getTypeBuilder(TName)) != null)) TS = TB.getTypeSpec();
			else {
				Package  P  = UM.getPackage(PName); 
				if(P != null) TS = P.getTypeSpec(TName);
			}
			
			if(TS != null) {
				// Resolve it first
				pEngine.getTypeManager().ensureTypeExist(pContext, this);
				return TS;
			}
		}
		
		if(this instanceof TLCurrent.TRCurrent) {
			TypeRef TRef = ((TLCurrent.TRCurrent)this).getBaseTypeRef();
			if(TRef != null) return TRef.getTypeSpec(pEngine, pContext);
		}

		// No Quick way - Just try to resolve it
		try {
			pEngine.getTypeManager().ensureTypeExist(pContext, this);
			return this.TheType.getTypeSpec();
		} catch (Exception e) {
			return null;
		}
	}

	/** Returns the spec of this type. */
	static final public TypeSpec getTypeSpecOf(Engine pEngine, TypeRef pTRef) {
		return TypeRef.getTypeSpecOf(pEngine, null, pTRef);
	}
	/** Returns TypeSpec from a type ref */
	static final TypeSpec getTypeSpecOf(Engine pEngine, Context pContext, TypeRef pTRef) {
		if(pTRef == null) return null;
		return pTRef.getTypeSpec(pEngine, pContext);
	}
	
	// Package ---------------------------------------------------------------------------------------------------------

	/** Returns the NameSpace that hold this type */
	final public Package getPackage(Engine pEngine) {
		if(pEngine != null) {
			pEngine.getTypeManager().ensureTypeInitialized(this);
			return pEngine.getPackageOf(this.getTheType());
		}
		return null;
	}

	/** Returns the code location of this type */
	final public Location getLocation(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getTypeInfo().getLocation(pEngine);
	}

	// Current and Parameterized ------------------------------------------------------------------

	/**
	 * Returns type reference of the parametered type of this type.
	 * 
	 * NOTE: This method will throw IllegalArgumentException when something go wrong.
	 *           Use TLParametered.TRParameter constructors if exception (at the create time) is not wanted.
	 **/
	final public TypeRef getParameteredTypeRef(Engine pEngine, TypeRef ... pPTypeRefs) {
		try {
			TypeSpec TSpec = this.getTypeSpec(pEngine);
			if(TSpec != null) return TSpec.getParameteredTypeRef(pEngine, pPTypeRefs);
		}
		catch (CurryError CE) { throw CE; }
		catch (Exception   E) { }
		return new TLParametered.TRParametered(pEngine,this, pPTypeRefs);
	}

	/** Checks if the type is parameterized */
	final public boolean isParameterized(Engine pEngine) {
		TypeSpec TS = this.getTypeSpec(pEngine);
		return TS.isParameterized();
	}
	/** Checks if the type is parametered */
	final public boolean isParametered(Engine pEngine) {
		TypeSpec TS = this.getTypeSpec(pEngine);
		return TS.isParametered();
	}
	
	/** Returns the ParameterizationInfo of this type */
	final public ParameterizedTypeInfo getParameterizedTypeInfo(Engine pEngine) {
		TypeSpec TS = this.getTypeSpec(pEngine);
		return TS.getParameterizedTypeInfo();
	}
	/** Returns the TypeParameteredInfo of this type */
	final public ParameteredTypeInfo getParameteredTypeInfo(Engine pEngine) {
		TypeSpec TS = this.getTypeSpec(pEngine);
		return TS.getParameteredTypeInfo();
	}
    /** Returns the array of Parameter types of this type */
    final public TypeRef[] getParameters(Engine pEngine) {
        final ParameteredTypeInfo aPTInfo = this.getParameteredTypeInfo(pEngine);
        if (aPTInfo == null)
	        return null;
        
        final TypeRef[] aPTRefs = aPTInfo.getParameterTypeRefs();
        return aPTRefs;
    }
	
	// Interface and super ------------------------------------------------------------------------

	/** Returns the number of interface this type is defined to have */
	final public int getInterfaceCount(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getTypeSpec().getInterfaceCount();
	}
	/** Returns the type of the interface at the index */
	final public TypeRef getInterfaceRefAt(Engine pEngine, int I) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getTypeSpec().getInterfaceRefAt(I);
	}
	
	// Others data -----------------------------------------------------------------------------------------------------
	
	/** Returns the data class of this type or null if there is no specific class for the type. */
	final public Class<?> getDataClass(Engine pEngine) {
		if(pEngine != null)
			pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getDataClass();
	}

	/** Returns the data class of this type or null if there is no specific class for the type. */
	final Class<?> getDataClass(Context pContext) {
		return this.getDataClass((pContext == null)?null:pContext.getEngine());
	}

	/** Returns the class of this type. */
	final public Class<? extends Type> getTypeClass(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getTypeClass();
	}

	/** Checks if this type is final (cannot inherit). */
	final public boolean isDerivable(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getTypeInfo().isDerivable();
	}

	/** Checks if this type is abstract (cannot construct an instance). */
	final boolean isDerivable(Context pContext) {
		return this.isDerivable((pContext == null)?null:pContext.getEngine());
	}
		
	/** Checks if this type is abstract (cannot construct an instance). */
	final public boolean isAbstract(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getTypeInfo().isAbstract();
	}

	/** Checks if this type is abstract (cannot construct an instance). */
	final boolean isAbstract(Context pContext) {
		return this.isAbstract((pContext == null)?null:pContext.getEngine());
	}

	/**
	 * Returns the default value of this type.<br/> The default value will be used when a variable
	 * is created and when an array of the type is created.
	 */
	final public Object getDefaultValue(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getDefaultValue(null);
	}

	/**
	 * Returns the default value of this type.<br/> The default value will be used when a variable
	 * is created and when an array of the type is created.
	 */
	final Object getDefaultValue(Context pContext) {
		return this.getDefaultValue((pContext == null)?null:pContext.getEngine());
	}
	
	// Presentation and other -----------------------------------------------------
	
	/** Returns a description of this type. */
	final public String getDescription(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getDescription();
	}

	/** Returns a description of this type. */
	final String getDescription(Context pContext) {
		return this.getDescription((pContext == null)?null:pContext.getEngine());
	}
	
	/** Return MoreData of this type.  */
	final protected MoreData getMoreData(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getMoreData();
	}

	/** Returns ExtraInfo of this type. */
	final protected MoreData getExtraInfo(Engine pEngine) {
		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		return this.getTheType().getExtraInfo();
	}
	
	// Compatibility checking ------------------------------------------------------------------------------------------
	
	/** Checks if the object O is assignable into the type */
	final public boolean canBeAssignedBy(Engine pEngine, Object ByObj) {
		return MType.CanTypeRefByAssignableBy(null, pEngine, this, ByObj);
	}
	
	/** Checks if the object O is assignable into the type */
	final public boolean canBeAssignedByInstanceOf(Engine pEngine, Type ByType) {
		if(ByType == null)                         return false;
		if(this.equals(TKJava.TAny .getTypeRef())) return true;
		if(this.equals(TKJava.TVoid.getTypeRef())) return true;
		return MType.CanTypeRefByAssignableByInstanceOf(null, pEngine, this, ByType.getTypeRef());
	}
	
	/** Checks if the object O is assignable into the type */
	final public boolean canBeAssignedByInstanceOf(Engine pEngine, TypeRef ByRef) {
		return MType.CanTypeRefByAssignableByInstanceOf(null, pEngine, this, ByRef);
		
		/*
		if((this == TKJava.TAny.getTypeRef()) || (this.equals(TKJava.TAny.getTypeRef())) ||
		   (this == TR) || (this.equals(TR)))
			return true;
		
		if(TR == null) return false;
		
		if(TR.isLoaded()) {
			Type T = TR.getTheType();
			TypeRef Super = T.getSuperRef();
			if((Super != null) && !Super.equals(TKJava.TAny.getTypeRef()) && Super.equals(this))
				return true;
			
			TypeSpec TS = T.getTypeSpec();
			int ICount = TS.getInterfaceCount();
			for(int i = ICount; --i >= 0; ) {
				TypeRef ITRef = TS.getInterfaceRefAt(i);
				if((ITRef != null) && !ITRef.equals(TKJava.TAny.getTypeRef()) && ITRef.equals(this))
					return true;
			}
		}
				
		if(TR instanceof TLParametered.TRParametered) {
			TLParametered.TRParametered TR_TP = (TLParametered.TRParametered)TR;

			if(this.equals(TR_TP.getTargetTypeRef()))
				return true;
			
			if(this instanceof TLParametered.TRParametered) {
				TLParametered.TRParametered TR_this = (TLParametered.TRParametered)this;
				if(TR_this.getTargetTypeRef().equals(TR_TP.getTargetTypeRef())) {
					for(int i = TR_this.getParameterCount(); --i >= 0; ) {
						if(!TR_this.getParameterTypeRef(i).canBeAssignedByInstanceOf(pEngine, TR_TP.getParameterTypeRef(i)))
							return false;
					}
					return true;
				}
			}
			
			if(this.canBeAssignedByInstanceOf(pEngine, TR_TP.getTargetTypeRef()))
				return true;
		}

		if(pEngine != null) pEngine.getTypeManager().ensureTypeInitialized(this);
		if(this.isLoaded()) {			
			if((pEngine != null) && (!TR.isLoaded() || !TR.TheType.isInitialized()))
				pEngine.getTypeManager().ensureTypeInitialized(TR);
			
			if(TR.isLoaded()) {
				if(TR.TheType.isInitialized())
					return this.TheType.canBeAssignedByInstanceOf(null, TR.getTheType());
				throw new CurryError("The type "+TR+" cannot be initialized.");
			}
			throw new CurryError("The type "+TR+" cannot be resolved.");
		}
		throw new CurryError("The type "+this.toString()+" cannot be resolved.");
		*/
	}
	
	// Search for constructor -----------------------------------------------------------
	
	/** Looks for a constructor. */
	final public ExecInterface searchConstructor(Engine pEngine, Object[] pSearchKey) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.getTheType().searchConstructorLocal(pSearchKey);
	}
	/** Looks for a constructor. */
	final public ExecInterface searchConstructor(Engine pEngine, TypeRef[] pSearchKey) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.getTheType().searchConstructorLocal(pSearchKey);
	}
	/** Looks for a constructor. */
	final public ExecInterface searchConstructor(Engine pEngine, ExecInterface pSearchKey)  {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.getTheType().searchConstructorLocal(pSearchKey);
	}
	
	// Search attribute of Type (Delegate to Type) ---------------------------------------------------------------------

	/** Search attribute of this StackOwner as the type */
	final public TypeRef searchTypeAttribute(Engine pEngine, String pName) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchAttribute(pEngine, false, null, pName);
	}

	/** Search operation of this StackOwner as the type using name and parameters */
	final public ExecSignature searchTypeOperation(Engine pEngine, String pOName, Object[] pParams) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchOperation(pEngine, OperationSearchKind.ByParams, false, pOName, pParams, null);
	}

	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final public ExecSignature searchTypeOperation(Engine pEngine, String pOName, TypeRef[] pPTypeRefs) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchOperation(pEngine, OperationSearchKind.ByTRefs, false, pOName, pPTypeRefs, null);
	}

	/** Search operation of this StackOwner as the type using name and interface */
	final public ExecSignature searchTypeOperation(Engine pEngine, String pOName, ExecInterface pExecInterface) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchOperation(pEngine, OperationSearchKind.ByNameInterface, false, pOName, pExecInterface, null);
	}

	/**
	 * Search operation of this StackOwner as the type using name and signature (if the signature are exact match, you
	 * can use it to execute)
	 */
	final public ExecSignature searchTypeOperation(Engine pEngine, ExecSignature pExecSignature) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchOperation(pEngine, OperationSearchKind.BySignature, false, pExecSignature, null, null);
	}
	
	// Search attribute of instance of Type (Delegate to Type) ----------------------------------------------------------

	/** Search attribute of this StackOwner as the type */
	final public TypeRef searchObjectAttribute(Engine pEngine, String pName) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchObjectAttribute(null, pEngine, false, null, pName);
	}

	/** Search operation of this StackOwner as the type using name and parameters */
	final public ExecSignature searchObjectOperation(Engine pEngine, String pOName, Object[] pParams) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchObjectOperation(pEngine, pOName, pParams, null);
	}

	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final public ExecSignature searchObjectOperation(Engine pEngine, String pOName, TypeRef[] pPTypeRefs) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchObjectOperation(pEngine, pOName, pPTypeRefs);
	}

	/** Search operation of this StackOwner as the type using name and interface */
	final public ExecSignature searchObjectOperation(Engine pEngine, String pOName, ExecInterface pExecInterface) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchObjectOperation(pEngine, pOName, pExecInterface);
	}

	/**
	 * Search operation of this StackOwner as the type using name and signature (if the signature
	 * are exact match, you can use it to execute)
	 */
	final public ExecSignature searchObjectOperation(Engine pEngine, ExecSignature pExecSignature) {
		if((pEngine != null) && (!this.isLoaded() || !this.TheType.isValidated()))
			pEngine.getTypeManager().ensureTypeValidated(this);
		return this.TheType.searchObjectOperation(pEngine, pExecSignature);
	}
}
