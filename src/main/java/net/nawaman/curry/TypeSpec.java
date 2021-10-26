package net.nawaman.curry;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import net.nawaman.curry.TKJava.TSJava;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLNoName.TRNoName;
import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.Objectable;
import net.nawaman.util.UArray;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

//Should explain here about required type and used type
// For the moment, TypeSpec can only required and used other types
/** TypeSpec contains information about a Type. */
abstract public class TypeSpec implements Serializable, Cloneable, Objectable {

	static final private long serialVersionUID = -6546546876546543516L;
	
	static final public TypeSpec[] EmptyTypeSpecArray = new TypeSpec[0];
	
	/** A flag indicate if the elements of this TypeSpec can be further changed (in the build process) */
	boolean IsSaved = false;
	
	/** Constructs a type spec. */
	protected TypeSpec(TypeRef pTRef) {		
		this.Ref = pTRef;
		if(this.Ref == null) this.Ref = new TLNoName.TRNoName(this);
	}
	
	/** Constructs a new type spec. */
	protected TypeSpec(TypeRef pRef, Serializable[] pDatas, TypeRef[] pRequiredTypes, TypeRef[] pUsedTypes) {
		this(pRef);
		this.Datas         = pDatas;
		this.RequiredTypes = pRequiredTypes;
		this.UsedTypes     = pUsedTypes;
	}
	
	// Constructor helper ----------------------------------------------------------------
	
	/** Creates TypeSpec data array with an optional documenation */
	static protected Serializable[] newDataWithOptionalDocumentation(Documentation pDoc, Serializable ... pDatas) {
		if(pDoc == null) return pDatas;
		MoreData MD = new MoreData(Documentation.MIName_Documentation, pDoc);
		
		if(pDatas == null) return new Serializable[] { MD };
		
		Serializable[] Datas = new Serializable[pDatas.length + 1];
		System.arraycopy(pDatas, 0, Datas, 0, pDatas.length);
		Datas[pDatas.length] = MD;
		
		return Datas;
	}
	
	// Classification --------------------------------------------------------------------
	
	/** Returns the kind name of this type. */
	abstract public String getKindName();
	
	// Reference ---------------------------------------------------------------
	
	/** The type reference. */
	TypeRef Ref;
	
	/** Returns the type reference */
	final public TypeRef getTypeRef() {
		return this.Ref;
	}
	
	// Type --------------------------------------------------------------------
	
	/** Ensure the type spec is initialize to a type and returns the type */
	final protected Type getThisType(Engine pEngine) {
		pEngine.getTypeManager().ensureTypeInitialized(this.Ref);
		return this.Ref.getTheType();
	}
	
	// Clone --------------------------------------------------------------------
	
	/** Clone this type spec. */ @Override
	public TypeSpec clone() throws CloneNotSupportedException {
		return (TypeSpec)super.clone();
	}
	
	// Location ----------------------------------------------------------------
	
	/** Returns the locations of this type */
	public Location getLocation() {
		Engine E = null;
		Type T = this.getTypeRef().getTheType();
		if(T != null) E = T.getEngine();
		return this.getLocation(E);
	}
	
	/** Returns the locations of this type */
	public Location getLocation(Engine pEngine) {
		TypeRef TR = this.getTypeRef();
		
		if(TR instanceof TREnclosed)    return ((TREnclosed)TR).getLocation();
		if(TR instanceof TRParametered) return ((TRParametered)TR).getTargetTypeRef().getLocation(pEngine);
		
		return null;
	}
	
	// Information -------------------------------------------------------------
	
	// Resolution and Initialization -----------------------------------------------------

	/** Status of type */
	static public enum Status { Unloaded, Loaded, Resolved, Validated, Initialized }

	/** Status of this type */
	transient Status TypeStatus	= Status.Unloaded;

	/** Returns the status of this type */
	final protected Status getStatus() {
		return this.TypeStatus;
	}
	/** Checks if this type is unloaded */
	final public boolean isUnloaded() {
		return (this.TypeStatus == Status.Unloaded) || (this.Ref.getTheType() == null);
	}
	/** Checks if this type is loaded */
	final public boolean isLoaded() {
		return (this.Ref.getTheType() != null) && (this.TypeStatus != Status.Unloaded);
	}
	/** Checks if this type is resolved */
	final public boolean isResolved() {
		return (this.Ref.getTheType() != null) && ((this.TypeStatus != Status.Unloaded) && (this.TypeStatus != Status.Loaded));
	}
	/** Checks if this type is validated */
	final public boolean isValidated() {
		return (this.Ref.getTheType() != null) && ((this.TypeStatus == Status.Validated) || (this.TypeStatus == Status.Initialized));
	}
	/** Checks if this type is initialized */
	final public boolean isInitialized() {
		return (this.Ref.getTheType() != null) && (this.TypeStatus == Status.Initialized);
	}
	
	// The data ------------------------------------------------------
	/** Spec Datas */
	protected Serializable[] Datas         = null;
	/** Required types that must be initialized before this type is initialized. */
	protected TypeRef[]      RequiredTypes = null;
	/** Used types that must be resolved before this type is initialized. */
	protected TypeRef[]      UsedTypes     = null;

	// The access ----------------------------------------------------
			
	/** Returns the number of data this spec contains. */
	final protected int getDataCount() {
		if(this.Datas == null) return 0;
		return this.Datas.length;
	}
	/** Returns the data at the position pPos. */
	final protected Serializable getData(int pPos) {
		if((this.Datas == null) || (pPos < 0) || (pPos >= this.Datas.length)) return null;
		return this.Datas[pPos];
	}

	/** Returns the number of required type ref this spec contains. */
	final protected int getRequiredTypeRefCount() {
		if(this.RequiredTypes == null) return 0;
		return this.RequiredTypes.length;
	}
	/** Returns the required type ref at the position pPos. */
	final protected TypeRef getRequiredTypeRef(int pPos) {
		if((this.RequiredTypes == null) || (pPos < 0) || (pPos >= this.RequiredTypes.length)) return null;
		return this.RequiredTypes[pPos];
	}
	/** Appends the given TypeRef as a required required */
	final protected void addRequiredTypeRef(TypeRef pRequiredTypeRef) {
		if(this.IsSaved || (pRequiredTypeRef == null) || (pRequiredTypeRef.equals(this.getTypeRef()))) return;
		
		if(this.RequiredTypes != null) {
			// Checks if the given type 
			for(int i = this.RequiredTypes.length; --i >= 0; )
				if(pRequiredTypeRef.equals(this.RequiredTypes[i]))
					return;
			
			TypeRef[] TRefs = new TypeRef[this.RequiredTypes.length + 1];
			System.arraycopy(this.RequiredTypes, 0, TRefs, 0, this.RequiredTypes.length);
			TRefs[this.RequiredTypes.length] = pRequiredTypeRef;
			
			this.RequiredTypes = TRefs;
			
		} else this.RequiredTypes = new TypeRef[] { pRequiredTypeRef }; 
	}
	
	/** Returns the number of type that this spec will use. */
	final protected int getUsedTypeRefCount()    {
		if(this.UsedTypes == null) return 0;
		return  this.UsedTypes.length;
	}
	/** Returns the used type at the position pPos */
	final protected TypeRef getUsedTypeRef(int pPos) {
		if((this.UsedTypes == null) || (pPos < 0) || (pPos >= this.UsedTypes.length)) return null;
		return this.UsedTypes[pPos];
	}
	/** Appends the given TypeRef as a used required */
	final protected void addUsedTypeRef(TypeRef pUsedTypeRef) {
		if(this.IsSaved || (pUsedTypeRef == null) || (pUsedTypeRef.equals(this.getTypeRef()))) return;
		
		if(this.UsedTypes != null) {
			// Checks if the given type 
			for(int i = this.UsedTypes.length; --i >= 0; )
				if(pUsedTypeRef.equals(this.UsedTypes[i]))
					return;
			
			TypeRef[] TRefs = new TypeRef[this.UsedTypes.length + 1];
			System.arraycopy(this.UsedTypes, 0, TRefs, 0, this.UsedTypes.length);
			TRefs[this.UsedTypes.length] = pUsedTypeRef;
			
			this.UsedTypes = TRefs;
			
		} else this.UsedTypes = new TypeRef[] { pUsedTypeRef }; 
	}

	/** Returns the index in the Datas of MoreData */
	protected int getMoreDataIndex()  { return -1; }
	/** Returns the index in the Datas of ExtraData */
	protected int getExtraInfoIndex() { return -1; }
	
	/**
	 * Return MoreData of this type. <br />
	 * MoreData is an information that is added later but it is an important information that will
	 *     distinguish a type from others.  
	 **/
	protected MoreData getMoreData() {
		MoreData MD = (MoreData)this.getData(this.getMoreDataIndex());
		if(MD == null) return MoreData.Empty;
		return MD;
	}

	/** Returns ExtraInfo of this type. */
	protected MoreData getExtraInfo() {
		MoreData MD = (MoreData)this.getData(this.getExtraInfoIndex());
		if(MD == null) return MoreData.Empty;
		return MD;
	}
	
	// Documentation -----------------------------------------------------------
	
	/** Returns the Documentation of this type */
	public Documentation getDocumentation() {
		MoreData MD = this.getExtraInfo();
		Serializable S = (MD == null)?null:MD.getData(Documentation.MIName_Documentation);
		return (S instanceof Documentation)?(Documentation)S:null;
	}
	
	// Super --- -------------------------------------------------------------------------------------------------------

	/** The index to the position in data for the super type ref */
	protected int getSuperIndex() {
		return -1;
	}
	
	/** Returns the RypeRef of the super type */
	public TypeRef getSuperRef() {
		return (TypeRef)this.getData(this.getSuperIndex());
	}
	
	// Interface -------------------------------------------------------------------------------------------------------

	/** The index to the position in data for the interface type ref */
	protected int getInterfaceIndex() {
		return -1;
	}
	
	/** Returns the array of intefaces */
	protected TypeRef[] getInterfaces() {
		int Index = this.getInterfaceIndex();
		if(Index == -1) return null;
		
		return (TypeRef[])this.getData(Index);
	}
	
	/** Returns the number of interface this type is defined to have */
	final public int getInterfaceCount() {
		TypeRef[] Interfaces = this.getInterfaces();
		if(Interfaces == null) return 0;
		return  Interfaces.length;
	}

	/** Returns the type reference of the interface at the index */
	final public TypeRef getInterfaceRefAt(int I) {
		if(I < 0) return null;
		TypeRef[] Interfaces = this.getInterfaces();
		if(Interfaces == null) return null;
		
		if(I >= Interfaces.length) return null;
		return Interfaces[I];
	}
	
	// StackOwner ------------------------------------------------------------------------------------------------------
	
	/** Returns the index in Datas of the ConstructorInfo */
	protected int getConstructorInfoIndex()   { return -1; }
	/** Returns the index in Datas of the Data AttributeInfo */
	protected int getDataAttributeInfoIndex() { return -1; }
	/** Returns the index in Datas of the Data OperationInfo */
	protected int getDataOperationInfoIndex() { return -1; }
	/** Returns the index in Datas of the Type AttributeInfo */
	protected int getTypeAttributeInfoIndex() { return -1; }
	/** Returns the index in Datas of the Type OperationInfo */
	protected int getTypeOperationInfoIndex() { return -1; }
	
	/** Returns the index in Datas of the Data ConstructorInfo */ @SuppressWarnings("unchecked")
	protected Vector<ConstructorInfo> getConstructorInfo() {
		int Index = this.getConstructorInfoIndex();
		if(Index == -1) return null;
		Vector<ConstructorInfo> CIs = (Vector<ConstructorInfo>)this.getData(Index);
		if(CIs == null) {
			CIs = new Vector<ConstructorInfo>();
			this.Datas[Index] = CIs;
		}
		return CIs;
	}
	
	/** Returns the index in Datas of the Data AttributeInfo */ @SuppressWarnings("unchecked")
	protected Vector<AttributeInfo> getDataAttributeInfo() {
		int Index = this.getDataAttributeInfoIndex();
		if(Index == -1) return null;
		Vector<AttributeInfo> AIs = (Vector<AttributeInfo>)this.getData(Index);
		if(AIs == null) {
			AIs = new Vector<AttributeInfo>();
			this.Datas[Index] = AIs;
		}
		return AIs;
	}
	/** Returns the index in Datas of the Data OperationInfo */ @SuppressWarnings("unchecked")
	protected Vector<OperationInfo> getDataOperationInfo() {
		int Index = this.getDataOperationInfoIndex();
		if(Index == -1) return null;
		Vector<OperationInfo> OIs = (Vector<OperationInfo>)this.getData(Index);
		if(OIs == null) {
			OIs = new Vector<OperationInfo>();
			this.Datas[Index] = OIs;
		}
		return OIs;
	}
	/** Returns the index in Datas of the Type AttributeInfo */ @SuppressWarnings("unchecked")
	protected Vector<AttributeInfo> getTypeAttributeInfo() {
		int Index = this.getTypeAttributeInfoIndex();
		if(Index == -1) return null;
		Vector<AttributeInfo> AIs = (Vector<AttributeInfo>)this.getData(Index);
		if(AIs == null) {
			AIs = new Vector<AttributeInfo>();
			this.Datas[Index] = AIs;
		}
		return AIs;
	}
	/** Returns the index in Datas of the Type OperationInfo */ @SuppressWarnings("unchecked")
	protected Vector<OperationInfo> getTypeOperationInfo() {
		int Index = this.getTypeOperationInfoIndex();
		if(Index == -1) return null;
		Vector<OperationInfo> OIs = (Vector<OperationInfo>)this.getData(Index);
		if(OIs == null) {
			OIs = new Vector<OperationInfo>();
			this.Datas[Index] = OIs;
		}
		return OIs;
	}
	
	// For compilation only --------------------------------------------------------------------------------------------
	
	/** Resets TypeRef that this TypeSpec holds */
	abstract protected void resetTypeSpecForCompilation();
	
	/** Clears the list of Good and Fail Interface of the type */
	final public void resetForCompilation() {
		this.TypeStatus = Status.Unloaded;
		this.resetTypeSpecForCompilation();
		
		Util.ResetTypeRefs(false, this.Ref);
		Util.ResetTypeRefs(this.RequiredTypes);
		Util.ResetTypeRefs(this.UsedTypes);
		
		Util.ResetTypeParameterInfos(this.getParameteredTypeInfo());
		Util.ResetTypeParameterInfos(this.getParameterizedTypeInfo());

		Util.ResetOperationInfos(this.getDataOperationInfo());
		Util.ResetOperationInfos(this.getTypeOperationInfo());
		Util.ResetAttributeInfos(this.getDataAttributeInfo());
		Util.ResetAttributeInfos(this.getTypeAttributeInfo());
	}
	
	// Parameterization ------------------------------------------------------------------------------------------------
	
	/** Resets TypeSpec so that a parametered one can be properly separated from its target */
	abstract protected void resetTypeSpecForParameterization();

	/** Returns the index in Datas of the TypeParameterizationInfo */
	protected int getParameterizationInfoIndex() {
		return -1;
	}
	
	/** Checks if the type is parameterized */
	final public boolean isParameterized() {
		return (this.getData(this.getParameterizationInfoIndex()) instanceof ParameterizedTypeInfo);
	}
	/** Checks if the type is parametered */
	final public boolean isParametered() {
		return (this.getData(this.getParameterizationInfoIndex()) instanceof ParameteredTypeInfo);
	}
	
	final protected TypeParameterInfo.TypeParameterInfos getTypeParameterInfos() {
		return (TypeParameterInfo.TypeParameterInfos)this.getData(this.getParameterizationInfoIndex());
	}
	
	/** Returns the ParameterizationInfo of this type */
	final public ParameterizedTypeInfo getParameterizedTypeInfo() {
		TypeParameterInfo.TypeParameterInfos TPIs = this.getTypeParameterInfos();
		if(!(TPIs instanceof ParameterizedTypeInfo)) return null;
		return (ParameterizedTypeInfo)TPIs;
	}
	/** Returns the TypeParameteredInfo of this type */
	final public ParameteredTypeInfo getParameteredTypeInfo() {
		TypeParameterInfo.TypeParameterInfos TPIs = this.getTypeParameterInfos();
		if(!(TPIs instanceof ParameteredTypeInfo)) return null;
		return (ParameteredTypeInfo)TPIs;
	}
	
	/** Returns the Parameter TypeRef of this type at the given index the  with the given name */
	final public TypeRef getParameterTypeRef(int pIndex) {
		if(pIndex < 0) return null;
		TypeParameterInfo.TypeParameterInfos TPIs = this.getTypeParameterInfos();
		if(TPIs == null) return null;
		return TPIs.getParameterTypeRef(pIndex);
	}
	/** Returns the Parameter TypeRef of this type associated with the given name */
	final public TypeRef getParameterTypeRef(String pPName) {
		if(pPName == null) return null;
		TypeParameterInfo.TypeParameterInfos TPIs = this.getTypeParameterInfos();
		if(TPIs == null) return null;
		return TPIs.getParameterTypeRef(pPName);
	}
	
	// A cache for parametered spec of this type spec (in case it is a parameterized)
	transient Hashtable<Integer, TypeSpec> ParameteredSpecs = null;
	
	/** Looks for a Parametered TypeSpec of this TypeSpec in the cache */
	final protected TypeSpec findParametedTypeSpecInCache(TypeRef ... pPTypeRefs) {
		if(this.ParameteredSpecs != null) {
			TypeSpec TS = this.ParameteredSpecs.get(UObject.toDetail(pPTypeRefs).hashCode());
			if(TS != null) return TS;
		}
		return null;
	}
	/** Looks for a Parametered TypeSpec of this TypeSpec in the cache */
	final protected void saveParametedTypeSpecInCache(TypeSpec pTSpec, TypeRef ... pPTypeRefs) {
		if(this.ParameteredSpecs == null) this.ParameteredSpecs = new Hashtable<Integer, TypeSpec>();
		this.ParameteredSpecs.put(UObject.toString(pPTypeRefs).hashCode(), pTSpec);
	}
	
	/**
	 * Creates a TypeSpec for its Parametered Type with the given parameter TypeRef
	 * 
	 * Implement this method if the Type of this TypeSpec kind may still be parameterized even if it does not have
	 *    TypeParameterizedInfo or its have a special way to create the TypeSpec.
	 **/
	protected TypeSpec newTypeSpecOfParameteredTypeSpec(Context pContext, Engine pEngine, TypeRef ... pPTypeRefs) {
		throw new IllegalArgumentException(
				String.format(
					"Type Parameterization error: the type '%s' cannot be parametered.",
					this.getTypeRef()
				)
			);
	}
	
	/**
	 * Indicator that this TypeSpec will provide a way to create TypeSpec for its Parameted Type by itself.
	 * 
	 * If this method returns true, ensure to implements TypeSpec.newTypeSpecOfParameteredTypeSpec(...) above.
	 **/ 
	protected boolean isSelfProvide_TypeSpecOfParameteredTypeSpec(Engine pEngine) {
		return false;
	}
	
	/**
	 * Returns type specification of the parametered type of this type.
	 * NOTE: This method will throw IllegalArgumentException when something go wrong.
	 **/
	TypeSpec getParameteredTypeSpec(Context pContext, Engine pEngine, TypeRef ... pPTypeRefs) {
		if((pPTypeRefs == null) || (pPTypeRefs.length == 0)) return null;
		
		// This type must be resolved before doing anything
		if(!this.getTypeRef().isLoaded())
			pEngine.getTypeManager().ensureTypeValidated(pContext, this.getTypeRef(), null);
		
		ParameterizedTypeInfo TPI = null;
		int Index = this.getParameterizationInfoIndex();
		// See if this type is parameterized
		if(Index != -1) {
			// Get from the cache
			TypeSpec TS = this.findParametedTypeSpecInCache(pPTypeRefs);
			if(TS != null) return TS;
			
			TPI = this.getParameterizedTypeInfo();
			if(TPI.getParameterTypeCount() != ((pPTypeRefs == null) ? 0 : pPTypeRefs.length))
				throw new CurryError("Invalid type parameteization `" + UArray.toString(pPTypeRefs) + "` for " + this + " <TypeSpec:482>.");
			
			for(int i = TPI.getParameterTypeCount(); --i >= 0; ) {
				TypeRef PTRef = TPI.getParameterTypeRef(i);
				if(!TKJava.TAny.getTypeRef().equals(PTRef) && !PTRef.canBeAssignedByInstanceOf(pEngine, pPTypeRefs[i]))
					throw new CurryError("Invalid type parameteization `" + UArray.toString(pPTypeRefs) + "` for " + this + " <TypeSpec:482>.");
			}
		}

		if((TPI == null) || this.isSelfProvide_TypeSpecOfParameteredTypeSpec(pEngine))
			return this.newTypeSpecOfParameteredTypeSpec(pContext, pEngine, pPTypeRefs);

		TypeSpec TS = null;		
		try { TS = (TypeSpec)this.clone(); }
		catch (CloneNotSupportedException E) {
			throw new IllegalArgumentException("Type Parameterization error: " + E);
		}
		
		// Ensure the TS is correct
		if(TS == null)
			throw new IllegalArgumentException(
					"Type Parameterization error: unable to produce a clone of the type '"+this.getTypeRef()+"'.");
		
		// Change the type status
		TS.TypeStatus = TypeSpec.Status.Unloaded;
		
		// Change the TypeRef of the Spec to a Parameterized TypeRef 
		TS.Ref = new TLParametered.TRParametered(this, pPTypeRefs);
		((TLParametered.TRParametered)TS.Ref).TheSpec = TS;
		
		// Ensure that this clone is a proper one
		if(this.Datas         == TS.Datas)         { TS.Datas         = (Serializable[])UArray.deepClone(this.Datas);         }
		if(this.RequiredTypes == TS.RequiredTypes) { TS.RequiredTypes = (TypeRef[])     UArray.deepClone(this.RequiredTypes); }
		if(this.UsedTypes     == TS.UsedTypes)     { TS.UsedTypes     = (TypeRef[])     UArray.deepClone(this.UsedTypes);     } 
		
		// Change from Parameterized to Parametered
		ParameteredTypeInfo PTI = TPI.getParamteredInfo(pEngine, this.getTypeRef(), pPTypeRefs);
		TS.Datas[Index] = PTI;

		// Clear all the caches
		TS.CBATo_TRUE  = null;
		TS.CBATo_FALSE = null;
		TS.resetTypeSpecForParameterization();
		
		// Resolve the parameters
		TS.resolveParameters(pContext, pEngine);
	
		// Save to the cache
		this.saveParametedTypeSpecInCache(TS, pPTypeRefs);
		
		return TS;
	}

	/** Creates a new TRBasedOnType from another TypeRef */
	final protected TRBasedOnType newBaseOnTypeTypeRef(Engine pEngine, TRBasedOnType pTRef) {
		if(pTRef == null) return pTRef;
		if(this.getTypeRef().equals(pTRef.TheBaseTypeRef)) return pTRef;
		
		TRBasedOnType TRBOT = pTRef.createNewTypeRef(pEngine, this.Ref, this);
		TRBOT.TheBaseTypeRef = this.Ref;
		return TRBOT;
	}
	
	/**
	 * Process the type parameters
	 * If there is a problem that makes the TypeSpec unusable, an exception should be thrown.
	 **/
	protected void resolveParameters(Context pContext, Engine pEngine) {
		if(!(this.getTypeRef() instanceof TRParametered) && !this.isParameterized()) return;
		this.resolveParameteredRequiredAndUsedTypeRefs(pContext, pEngine);
		this.resolveParameteredTypeSpec               (pContext, pEngine);
	}
	
	/**
	 * Process the type parameters
	 * 
	 * If there is a problem that makes the TypeSpec unusable, an exception should be thrown.
	 **/
	void resolveParameteredRequiredAndUsedTypeRefs(Context pContext, Engine pEngine) {
		boolean IsThisAParameterized = this.isParameterized();
		
		// Loop all Required and Used type --------------------------------------------------------
		for(int A = 2; --A >= 0; ) {
			TypeRef[] TRefs = (A == 0) ? this.RequiredTypes : this.UsedTypes;
			if(TRefs == null) continue;
			for(int i = TRefs.length; --i >= 0; ) {
				TypeRef TRef = TRefs[i];
				if(!(TRef instanceof TRBasedOnType)) continue;
				
				TRefs[i] = (TRef = this.newBaseOnTypeTypeRef(pEngine, (TRBasedOnType)TRefs[i]));
				
				if((TRef instanceof TRBasedOnType) && !IsThisAParameterized)
					TRefs[i] = (TRef = ((TRBasedOnType)TRef).flatType(pEngine, null, null));
			}
		}
	}
	
	/**
	 * Process the type parameters
	 * If there is a problem that makes the TypeSpec unusable, an exception should be thrown.
	 **/
	protected void resolveParameteredTypeSpec(Context pContext, Engine pEngine) {}
	
	// Current and Parameterized ------------------------------------------------------------------

	/**
	 * Returns type reference of the parametered type of this type.
	 * 
	 * NOTE: This method will throw IllegalArgumentException when something go wrong.
	 *           Use TLParametered.TRParameter constructors if exception (at the create time) is not wanted.
	 **/
	final public TypeRef getParameteredTypeRef(Engine pEngine, TypeRef ... pPTypeRefs) {
		return this.getParameteredTypeRef(null, pEngine, pPTypeRefs);
	}

	/**
	 * Returns type reference of the parametered type of this type.
	 * 
	 * NOTE: This method will throw IllegalArgumentException when something go wrong.
	 *           Use TLParametered.TRParameter constructors if exception (at the create time) is not wanted.
	 **/
	final protected TypeRef getParameteredTypeRef(Context pContext, Engine pEngine, TypeRef ... pPTypeRefs) {
		TypeSpec TheSpec = this.getParameteredTypeSpec(pContext, pEngine, pPTypeRefs);
		if(this == TheSpec) return TheSpec.getTypeRef();
		
		TypeRef TheRef;
		if(!((TheRef = TheSpec.getTypeRef()) instanceof TLParametered.TRParametered)) {
			throw new IllegalArgumentException("The type spec `"+TheSpec+"` cannot be parameted.");
		}
		
		return TheRef; 
	}
	
	// Cache of compatible types and Interface ----------------------------------------------------
	
	// 'CanBeAssignedTo' cache
	HashSet<TypeRef> CBATo_TRUE  = null;
	HashSet<TypeRef> CBATo_FALSE = null;
	
	/** Checks in the cache if the type of this type spec can be assigned to the given type ref */
	final protected Boolean checkCanBeAssignedTo_InCache(TypeRef ToRef) {
		if((this.CBATo_TRUE  != null) && this.CBATo_TRUE .contains(ToRef)) return true;
		if((this.CBATo_FALSE != null) && this.CBATo_FALSE.contains(ToRef)) return false;
		return null;
	}
	
	/** Add a TypeRef into the cache */
	final void addToCanBeAssignedToCache(TypeRef ToRef, boolean IsTRUE) {
		if((ToRef == null) || this.getTypeRef().equals(ToRef)) return;		
		// Add to the cache
		if(IsTRUE) { if(this.CBATo_TRUE  == null) this.CBATo_TRUE  = new HashSet<TypeRef>(); this.CBATo_TRUE .add(ToRef); }
		else       { if(this.CBATo_FALSE == null) this.CBATo_FALSE = new HashSet<TypeRef>(); this.CBATo_FALSE.add(ToRef); }
	}
	
	// Representation -------------------------------------------------------------------

	/** Checks if this type will not display "NoName" when toString() (when the TypeRef is NoName). */
	protected boolean isToShowNoName() {
		return true;
	}
	/**
	 * Checks if parameterized TypeRef has target a no-name TypeRef with this TypeSpec, its should display the
	 * parameterized infomation
	 **/
	protected boolean isToShowParameteredInfo() {
		return true;
	}
	
	/**
	 * Returns toString for this TypeSpec.
	 * 
	 * Implement this and make this.isToShowParameteredInfo() to return false, to display TypeSpec with customed way to
	 *    display the TypeSpec and ParameteredInfo.
	 **/
	protected String getToString(TRParametered pParameteredTypeRef) {
		return this.getToString();
	}
	
	/** Returns toString for this TypeSpec */
	abstract protected String getToString();

	/** Returns the toString for the type */
	String Type_toString() {
		TypeRef TRef = this.getTypeRef();
		
		StringBuffer SB = new StringBuffer();
		
		// Show the TypeRef (in case of NoName, it will get to getToString())
		if(TRef instanceof TRNoName)
			 SB.append(this.getToString());
		else SB.append(TRef.toString()); 
		
		// Show the parameterization information
		if(this.isToShowParameteredInfo() && this.isParameterized())
			SB.append(this.getParameterizedTypeInfo());
			
		return SB.toString();
	}
	
	/** Returns Description for this TypeSpec */
	protected String getDescriptionDetail(Engine pEngine) {
		return this.toString();
	}
	
	/** Returns Description for this TypeSpec */
	final public String getDescription(Engine pEngine) {
		StringBuilder SB = new StringBuilder();
		
		// If this is not a NoName TypeSpec, show the Ref first. 
		if(!(this.Ref instanceof TRNoName)) SB.append(this.Ref).append("::").append(this.getKindName());

		String S = this.getDescriptionDetail(pEngine);
		if(S != null) SB.append(S);
		return SB.toString();
	}
	
	// Objectable -----------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	final public String toString() {
		StringBuffer SB = new StringBuffer(this.Type_toString());
		
		// Show the Kind name of the type
		SB.append(":").append(this.getKindName());
			
		return SB.toString();
	}
	/**{@inheritDoc}*/ @Override
	final public String toDetail() {
		StringBuffer SB = new StringBuffer();
		SB.append(this.toString()).append("{ ");
		SB.append("Data:").    append(UObject.toString(this.Datas)).        append("; ");
		SB.append("Required:").append(UObject.toString(this.RequiredTypes)).append("; ");
		SB.append("Used:")    .append(UObject.toString(this.UsedTypes)).    append("; ");
		SB.append(" }");
		return SB.toString();
	}
	
	/**{@inheritDoc}*/ @Override	
	public boolean is(Object O) {
		return this == O;
	}
	
	/**{@inheritDoc}*/ @Override
	public boolean equals(Object O) {
		if(O == null) return false;
		if(!(O instanceof TypeSpec)) return false;

		TypeSpec ES = (TypeSpec)O;

		if(!(this.Ref instanceof TLNoName.TRNoName) && !(ES.Ref instanceof TLNoName.TRNoName))
			// Both are not NoName, so check by name or other info
			return this.Ref.equals(ES.Ref);
		
		if((this.Ref instanceof TLNoName.TRNoName) != (ES.Ref instanceof TLNoName.TRNoName))
			// One no name but one is not so they are not equals.
			return false;
		
		// In case of no name ref, check each item
			
		if(!UString.equal(this.getKindName(), ES.getKindName()))           return false;
		if(this.getDataCount()            != ES.getDataCount())            return false;
		if(this.getRequiredTypeRefCount() != ES.getRequiredTypeRefCount()) return false;
		if(this.getUsedTypeRefCount()     != ES.getUsedTypeRefCount())     return false;
			
		int EDIndex = this.getExtraInfoIndex();
		for(int i = 0; i < this.getDataCount(); i++) {
			if(i == EDIndex) continue;	// Skip ExtraInfo
			if(!UObject.equal(this.getData(i), ES.getData(i))) return false;
		}
		for(int i = 0; i < this.getRequiredTypeRefCount(); i++) {
			if(!UObject.equal(this.getRequiredTypeRef(i), ES.getRequiredTypeRef(i))) return false;
		}
		for(int i = 0; i < this.getUsedTypeRefCount(); i++) {
			if(!UObject.equal(this.getUsedTypeRef(i), ES.getUsedTypeRef(i))) return false;
		}
		return true;
		
	}
	
	/**{@inheritDoc}*/ @Override
	public int hash() {
		int hash = UString.hash(this.getKindName());
		hash += this.getDataCount();
		hash += UObject.hash(this.Datas);
		hash += UObject.hash(this.RequiredTypes);
		hash += UObject.hash(this.UsedTypes);
		return hash;
	}
	
	/**{@inheritDoc}*/ @Override
	public int hashCode() {
		return this.hash();
	}
	
	// Serializable ----------------------------------------------------------------------------------------------------
	
	/** Perform some operatin just before saving */
	private void writeObject(ObjectOutputStream out) throws IOException {
		// Reset the type
		this.resetForCompilation();
		
		// Save the rest
		out.defaultWriteObject();
	}
	
	// Generic ---------------------------------------------------------------------------------------------------------
	
	/** Extract the parameters of the Type that target the Parameterized */
	static public TypeRef[] ExtractParameterFrom(Context pContext, Engine pEngine, TypeRef TheType, TypeRef Parameterized) {
		if(!MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, Parameterized, TheType))
			return null;
		
		Engine $Engine = pEngine; if(($Engine == null) && (pContext != null)) $Engine = pContext.getEngine();
		MType  MT      = $Engine.getTypeManager(); 

		MT.ensureTypeValidated(pContext, TheType,       null);
		MT.ensureTypeValidated(pContext, Parameterized, null);
		if(!Parameterized.getTheType().getTypeSpec().isParameterized()) return null;
		
		TypeParameterInfo.TypeParameterInfos TPInfos = Parameterized.getTypeSpecWithoutEngine().getTypeParameterInfos();
		if(TPInfos == null) return null;
		
		int PCount = TPInfos.getParameterTypeCount();
		if(PCount == 0) return TypeRef.EmptyTypeRefArray;
		
		// Try both and see the smallest ValueType
		Type     OT      = MT.getTypeFromRefNoCheck(pContext, TheType);
		TypeSpec OT_Spec = OT.getTypeSpec();
		
		// Java Type
		if(OT_Spec instanceof TSJava) return null;
		
		TypeRef[] ParamTypes = new TypeRef[PCount];
		boolean   HasFound   = false;
		if(OT_Spec.CBATo_TRUE != null) {
			for(TypeRef OTCompatibleRef : OT_Spec.CBATo_TRUE) {
				TypeRef GIRef = OTCompatibleRef;
				if(GIRef == null) continue;
					
				if(GIRef.equals(Parameterized)) {
					if(!HasFound) {
						HasFound = true;
						for(int i = PCount; --i >= 0; )
							ParamTypes[i] = TPInfos.getParameterTypeRef(i);
					}
					continue;
				}
				// Not a parametered or the target is not match
				else if(!(GIRef instanceof TRParametered) || !Parameterized.equals(((TRParametered)GIRef).getTargetTypeRef())) {
					continue;
				}

				HasFound = true;
				for(int i = PCount; --i >= 0; ) {
					TypeRef PRef = ((TRParametered)GIRef).getParameterTypeRef(i);
					
					TypeRef ParamType = ParamTypes[i];
					// Found a parameted of CompareValueType one
					if(PRef.equals(ParamType)) continue;

					// We try to use the largest type --------------------------------------------------- vvvvvvvvv  vvvv
					if((ParamType == null) || MType.CanTypeRefByAssignableByInstanceOf(pContext, pEngine, ParamType, PRef))
						ParamType = PRef;
					
					// Assign the parameter types
					ParamTypes[i] = ParamType;
				}
			}
		}
		
		return ParamTypes;
	}
}
