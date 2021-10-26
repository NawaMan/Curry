package net.nawaman.curry;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.TLPrimitive.TRPrimitive;
import net.nawaman.curry.compiler.TypeSpecCreator;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.util.UArray;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

// TypeInfo structure of Array
// Data    { Length, ParameterizedTypeInfo, [Documents] }
// TypeRef { BaseContainTypeRef }

final public class TKArray extends TypeKind {
	
	// Constants ------------------------------------------------------------------------
	
	static final public String KindName  = "Array";
	static final public String ParamName = "T";
	
	static final int IndexDimension             = 0;
	static final int IndexParameterizedTypeInfo = 1;
	static final int IndexExtraData             = 2;

	static final int ParameterIndex_ContainTypeRef = 0;
	
	// Predefine TypeRefs ---------------------------------------------------------------- 
	
	/** Any[]          */ static final public TypeRef AnyArrayRef          = getTypeSpec(TKJava.TAny         ).getTypeRef();

	/** String[]       */ static final public TypeRef StringArrayRef       = getTypeSpec(TKJava.TString      ).getTypeRef();
	/** boolean[]      */ static final public TypeRef BooleanArrayRef      = getTypeSpec(TKJava.TBoolean     ).getTypeRef();
	/** char[]         */ static final public TypeRef CharacterArrayRef    = getTypeSpec(TKJava.TCharacter   ).getTypeRef();

	/** byte[]         */ static final public TypeRef ByteArrayRef         = getTypeSpec(TKJava.TByte        ).getTypeRef();
	/** short[]        */ static final public TypeRef ShortArrayRef        = getTypeSpec(TKJava.TShort       ).getTypeRef();
	/** integer[]      */ static final public TypeRef IntegerArrayRef      = getTypeSpec(TKJava.TInteger     ).getTypeRef();
	/** long[]         */ static final public TypeRef LongArrayRef         = getTypeSpec(TKJava.TLong        ).getTypeRef();
	/** float[]        */ static final public TypeRef FloatArrayRef        = getTypeSpec(TKJava.TFloat       ).getTypeRef();
	/** double[]       */ static final public TypeRef DoubleArrayRef       = getTypeSpec(TKJava.TDouble      ).getTypeRef();
	/** Number[]       */ static final public TypeRef NumberArrayRef       = getTypeSpec(TKJava.TNumber      ).getTypeRef();

	/** Type[]         */ static final public TypeRef TypeArrayRef         = getTypeSpec(TKJava.TType        ).getTypeRef();
	/** TypeRef[]      */ static final public TypeRef TypeRefArrayRef      = getTypeSpec(TKJava.TTypeRef     ).getTypeRef();

	/** Expression[]   */ static final public TypeRef ExpressionArrayRef   = getTypeSpec(TKJava.TExpression  ).getTypeRef();

	/** Class[]        */ static final public TypeRef ClassArrayRef        = getTypeSpec(TKJava.TClass       ).getTypeRef();
	/** Serializable[] */ static final public TypeRef SerializableArrayRef = getTypeSpec(TKJava.TSerializable).getTypeRef();

	/** CaseEntry[]    */ static final public TypeRef CaseEntryArrayRef    = getTypeSpec(TKJava.TCaseEntry   ).getTypeRef();
	/** CatchEntry[]   */ static final public TypeRef CatchEntryArrayRef   = getTypeSpec(TKJava.TCatchEntry  ).getTypeRef();
	
	// Constructor ---------------------------------------------------------------------------------

	// Array is engine independent
	TKArray(Engine pEngine) {
		super(pEngine);
	}

	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return KindName;
	}

	/** Create a type reference of an array type */
	static public TypeRef newArrayTypeRef(TypeRef pContainTypeRef) {
		return newArrayTypeRef(pContainTypeRef, -1);
	}
	/** Create a type reference of an array type */
	static public TypeRef newArrayTypeRef(TypeRef pContainTypeRef, int pDimension) {
		return new TLParametered.TRParametered(TSArray.getTSNoNameBaseArray(pDimension), pContainTypeRef);
	}

	/** Returns the type of the given type-ref refers to an array type */
	static public Type getArrayTypeFromRef(Engine pEngine, TypeRef pTRef) {
		if(pTRef == null) return null;
		if(!pTRef.isLoaded() || !pTRef.getTheType().isInitialized()) pEngine.getTypeManager().ensureTypeInitialized(pTRef);
		if(pTRef.getTheType() instanceof TArray) return pTRef.getTheType();
		return null;
	}
	/** Checks if the given type ref refer to an array */
	static public boolean isArrayTypeRef(Engine pEngine, TypeRef pTRef) {
		if(pTRef == null) return false;
		TypeRef BTRef = pTRef;
		if(pTRef instanceof TRParametered) BTRef = ((TRParametered)BTRef).getTargetTypeRef();
		
		if(!BTRef.isLoaded()) pEngine.getTypeManager().ensureTypeExist(null, BTRef);
		if(BTRef.getTheType() instanceof TArray) return true;
		return false;
	}
	/** Checks if the given type ref refer to an array */
	static public TypeRef getArrayComponentTypeRef(Engine pEngine, TypeRef pTRef) {
		if(pTRef == null) return null;
		TypeRef BTRef = pTRef;
		if(pTRef instanceof TRParametered) BTRef = ((TRParametered)BTRef).getTargetTypeRef();
		
		if(!BTRef.isLoaded()) pEngine.getTypeManager().ensureTypeExist(null, BTRef);
		if(!(BTRef.getTheType() instanceof TArray)) return null;
		
		// Not a paramtered
		if(BTRef == pTRef) return TKJava.TAny.getTypeRef();
		
		return ((TRParametered)pTRef).getParameterTypeRef(0);
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		return false;
	}
	
	// Services ---------------------------------------------------------------
	
	static String generateArrayTypeName(Type pContainType, int pLength) {
		if(!(pContainType instanceof TKArray.TArray))
			return ((pContainType == null) ? "void" : pContainType.getTypeRef().toString()) + "[" + ((pLength < 0)?"":pLength) + "]";
		
		String TStr = pContainType.getTypeRef().toString();
		int I = TStr.indexOf("[");
		
		return TStr.substring(0, I) + "[" + ((pLength < 0)?"":pLength) + "]" + TStr.substring(I);
	}
	
	static String generateArrayTypeName(TypeRef pContainTypeRef, int pLength) {
		String TStr = pContainTypeRef.toString();
		int I = TStr.indexOf("[");
		if(I == -1) return String.format("%s[%s]", TStr, (pLength < 0)?"":pLength);
		
		return TStr.substring(0, I) + "[" + ((pLength < 0)?"":pLength) + "]" + TStr.substring(I);
	}
	
	public TSArray getTypeSpec(TypeRef pTypeRef, Type pContainType, int pLength) {
		return this.getTypeSpec(pTypeRef, pContainType.getTypeRef(), pLength, true, null);
	}
	public TSArray getTypeSpec(TypeRef pTypeRef, TypeRef pContainTypeRef, int pLength) {
		return this.getTypeSpec(pTypeRef, pContainTypeRef, pLength, true, null);
	}
	public TSArray getTypeSpec(TypeRef pTypeRef, Type pContainType, int pLength, Documentation pDoc) {
		return this.getTypeSpec(pTypeRef, pContainType.getTypeRef(), pLength, true, pDoc);
	}
	public TSArray getTypeSpec(TypeRef pTypeRef, TypeRef pContainType, int pLength, Documentation pDoc) {
		return this.getTypeSpec(pTypeRef, pContainType, pLength, true, pDoc);
	}
	
	protected TSArray getTypeSpec(TypeRef pTypeRef, Type pContainType, int pLength, boolean pIsVerify, Documentation pDoc) {
		return this.getTypeSpec(pTypeRef, pContainType.getTypeRef(), pLength, pIsVerify, pDoc);
	}
    protected TSArray getTypeSpec(TypeRef pTypeRef, TypeRef pContainTypeRef, int pLength, boolean pIsVerify,
			Documentation pDoc) {
		if((pContainTypeRef == null) || (pContainTypeRef.equals(TKJava.TVoid.getTypeRef()))) return null;
		
		// Adjust the value
		if(pLength < -1) pLength = -1;

		// If unnamed and already exist, return the old one (sharing for better memory usage)
		if((pTypeRef == null) && (pDoc == null)) {
			
			TSArray TSA = TSArray.getTSNoNameBaseArray(pLength);
			if(TKJava.TAny.getTypeRef().equals(pContainTypeRef))
				 return TSA;
			else return TSArray.getTSParameteredArray(this.getEngine(), TSA, new TypeRef[] { pContainTypeRef });
		}
		
		return TSArray.newTSNamedBaseArray(this.getEngine(), pTypeRef, pContainTypeRef, pLength, pDoc);
	}
	
	/** Returns the TypeSpec of Array with TJava as its component and -1 as its length */
	static protected TSArray getTypeSpec(TKJava.TJava T) {
		TSArray TSA = TSArray.getTSNoNameBaseArray(-1);
		if(T == TKJava.TAny) return TSA;
		// NOTE: Use null as the engine
		// NOTE: this is posible because the engine will only be used to engine T is resolved but since T is TJava is
		//   always resolved.
		else return TSArray.getTSParameteredArray(null, TSA, new TypeRef[] { T.getTypeRef() });
	}

	/**
	 * Creates a new array type
	 * 
	 * Array types will not be initialized as other type as it will need to be availble before the Engine is ready (used
	 *    in Instruction).
	 **/
	TArray newArrayType(TypeRef pTypeRef, TypeRef pContainTypeRef, int pLength) {
		if((pContainTypeRef == null) || (pContainTypeRef.equals(TKJava.TVoid.getTypeRef()))) return null;
		
		TSArray TSA = this.getTypeSpec(pTypeRef, pContainTypeRef, pLength);
		TArray  TA  = (TArray)TSA.getTypeRef().getTheType();
		if(TA == null) {
			TA = new TArray(this, TSA);
			TSA.getTypeRef().setTheType(TA);
		}
		return TA;
	}
	
	/** Creates a TypeSpec Creator so that the compiler can use */
	public TypeSpecCreator getTypeSpecCreator(final TypeRef pContainTypeRef, final int pLength) {
		return new TypeSpecCreator() {
			public TypeSpec newTypeSpec(Engine pEngine, TypeRef pTRef, boolean pIsVerify, Documentation pDocument) {
				return getTypeSpec(pTRef, pContainTypeRef, pLength, pDocument);
			}
		}; 
	}
	
	// To Satisfy TypeFactory ---------------------------------------------------------------------
	
	// Typing --------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return false;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getType(Engine pEngine, Context pContext, TypeSpec pTypeSpec) {
		// Precondition
		if(!(pTypeSpec instanceof TSArray)) {
			String SpecStr = (pTypeSpec == null)?"null":pTypeSpec.getKindName();
			throw new CurryError("Internal Error: Wrong type kind ("+ SpecStr +" in " + KindName + " ).(TKArray.java#113)", pContext);
		}
		
		// The type is ready to use, just return it out
		if((pTypeSpec.getTypeRef() != null) && (pTypeSpec.getTypeRef().getTheType() != null)) 
			return pTypeSpec.getTypeRef().getTheType();
		
		TArray TA = new TArray(this, (TSArray)pTypeSpec);
		TA.getTypeRef().setTheType(TA);
		TA.TSpec.TypeStatus = TypeSpec.Status.Loaded;
		
		// Create a new one
		return TA;
	}
	
	// Type checking ---------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TArray.class;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getDefaultType(Context pContext) {
		return this.newArrayType(null, TKJava.TAny.getTypeRef(), -1);
	}
	
	// Caching the array class so we do not need to create many of them.
	static Hashtable<Class<?>, Class<?>> ArrayClasses = null;
	
	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pTS) {
		if(pTS == null)               return null;
		if(!(pTS instanceof TSArray)) return null;
		TSArray TSA  = ((TSArray)pTS);
		Class<?> Cls = TSA.getContainTypeRef().getDataClass(this.getEngine());
		
		if(ArrayClasses == null) ArrayClasses = new Hashtable<Class<?>, Class<?>>();
		Class<?> ACls = ArrayClasses.get(Cls);
		if(ACls != null) return ACls;
		
		ACls = Array.newInstance(Cls, 0).getClass();
		ArrayClasses.put(Cls, ACls);
		return ACls;
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec, Object pByObject) {
		if(!(pTheTypeSpec instanceof TSArray))
			return false;
		
		// Pre-check the Data
		if(pByObject == null) return true;
		Class<?> C = pByObject.getClass();
		if(!UArray.isArrayType(C)) return false;

		// Checks the dimension
		TSArray TSA = ((TSArray)pTheTypeSpec);
		int TLength = TSA.getLength();
		int DLength = UArray.getLength(pByObject);
		if((TLength != -1) && (TLength != DLength))
			return false;
		
		// Pre-check the type
		TypeRef TR = TSA.getTypeRef();
		Type    TA = TR.getTheType();
		if(TA == null) TA = this.getEngine().getTypeManager().getTypeFromRefNoCheck(pContext, TR);
		if(!TA.getDataClass().isInstance(pByObject)) return false;
		
		Type T = ((TArray)TA).getContainType();
		if(T instanceof TKJava.TJava) return true;

		// Check inidividually as the class does not actually ensure type compatible, unless it is TJava (above if)
		for(int i = DLength; --i >= 0; ) 
			if(!T.canBeAssignedBy(UArray.get(pByObject, i))) return false;
		
		return true;
	}
	/**{@inheritDoc}**/ @Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec, TypeSpec BySpec) {
		if(!(BySpec instanceof TSArray)) return false;
		
		TSArray TheArray = (TSArray)TheSpec;
		TSArray ByArray  = (TSArray)BySpec;
		
		// Check if the dimension type are compatible
		int TD = TheArray.getLength();
		if((TD != -1) && (TD != ByArray.getLength())) return false;
		
		// Check if the contained type are compatible
		TypeRef  TS_RT = TheArray.getContainTypeRef();
		TypeRef pTS_RT = ByArray .getContainTypeRef();
		
		return TS_RT.canBeAssignedByInstanceOf(this.getEngine(), pTS_RT);
	}

	// Get Type -------------------------------------------------------------------------
	@Override protected Type getTypeOf(Context pContext, Object pObj) {
		if(pObj == null)    return null;
		if(!pObj.getClass().isArray()) return null;
		Class<?> CClass = pObj.getClass().getComponentType();
		int   Length = Array.getLength(pObj);
		Type T = this.Engine.getTypeManager().getTypeOfTheInstanceOfNoCheck(pContext, CClass);
		if((T == null) || (T.equals(TKJava.TVoid))) {
			throw new IllegalArgumentException("Unable to constructs an array of the type null/void.");
		}
		if(T instanceof TKArray.TArray) {
			int TLength = -1;
			for(int i = 0; i < Array.getLength(pObj); i++) {
				Object O = Array.get(pObj, i);
				if(O == null) continue;
				int L = Array.getLength(O);
				if(TLength == -1) TLength = L;
				else {
					if(TLength != L) {
						TLength = -1;
						break;
					}
				}
			}
			if(TLength != -1) T = this.newArrayType(null, ((TKArray.TArray)T).getContainTypeRef(), TLength);
		}
		return this.newArrayType(null, T.getTypeRef(), Length);
	}
	@Override protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		if(!pCls.isArray()) return null;

		Class<?> CClass = UArray.getComponentType_OfType(pCls);
		
		Type T = this.Engine.getTypeManager().getTypeOfTheInstanceOfNoCheck(pContext, CClass);
		if((T == null) || (T.equals(TKJava.TVoid))) {
			throw new IllegalArgumentException("Unable to constructs an array of the type null/void.");
		}
		return this.newArrayType(null, T.getTypeRef(), -1);
	}
	
	// Instantiation ---------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isTypeAbstract(Context pContext, Type pThisType) {
		return false;
	}

	/**{@inheritDoc}*/ @Override
	protected Object getTypeDefaultValue(Context pContext, Type pThisType) {
		return null;
	}
	
	/** Returns the initializers for initializing newly created instance. */
	@Override protected ConstructorInfo[] getConstructorInfos(Context pContext, Engine pEngine, Type pTheType) {
		Vector<ConstructorInfo> VInitializers = new Vector<ConstructorInfo>();
		
		// new()
		VInitializers.add(
			new ConstructorInfo.CIMacro(this.getEngine(), Accessibility.Public, pTheType.getTypeRef(),
				new CurryExecutable.CurryMacro(this.getEngine(),
					ExecSignature.newSignature(
						ConstructorInfo.ConstructorSignatureName,
						null, null, false,
						TKJava.TVoid.getTypeRef(),
						null, null
					),
					null, null, null
				),
				null
			));

		// new(Current)
		VInitializers.add(
			new ConstructorInfo.CIMacro(this.getEngine(), Accessibility.Public, pTheType.getTypeRef(),
				new CurryExecutable.CurryMacro(this.getEngine(),
					ExecSignature.newSignature(
						ConstructorInfo.ConstructorSignatureName,
						new TypeRef[] { pTheType.getTypeRef() }, new String[]  { "pElements" },
						TKJava.TVoid.getTypeRef()
					),
					null, null, null
				),
				null
			));
		
		// new(...)
		ExecSignature ES;
		int     Length = ((TArray)pTheType).getLength();
		TypeRef CTRef  = ((TArray)pTheType).getContainTypeRef();
		if(Length == -1) { 
			ES = ExecSignature.newSignature(
					ConstructorInfo.ConstructorSignatureName,
					new TypeRef[] { CTRef }, new String[]  { "pElements" }, true,
					TKJava.TVoid.getTypeRef(), null, null
				);
		} else {
			TypeRef[] PTRefs = new TypeRef[Length];
			String[]  PNames = new String[Length];
			for(int i = 0; i < Length; i++) {
				PTRefs[i] = CTRef;
				PNames[i] = "E" + i;
			}
			ES = ExecSignature.newSignature(ConstructorInfo.ConstructorSignatureName, PTRefs, PNames, TKJava.TVoid.getTypeRef());
		}
		
		// Add new array from object VarArg
		VInitializers.add(
			new ConstructorInfo.CIMacro(this.getEngine(), Accessibility.Public, pTheType.getTypeRef(),
				new CurryExecutable.CurryMacro(this.getEngine(), ES, null, null, null), null
			));
		
		return VInitializers.toArray(new ConstructorInfo[VInitializers.size()]);
	}
	
	/** Checks if this type kind required initializing */
	@Override protected boolean isNeedInitialization() {
		return false;
	}
	
	// This method is a directly (also means faster) way to create Type instance, use this methods if
	//   if you can
	/**
	 * Creates a new instance of the type.<br />
	 * 
	 * If isAbstract() returns true, this method will never be called.
	 **/ @Override
	 protected Object createNewTypeInstance(Context pContext, Executable pInitiator, Type pThisType,
			Object pSearchKey, Object[] pParams) {
		
		// Precondition
		if(pThisType == null) throw new NullPointerException();
		if(!(pThisType instanceof TArray))
			throw new CurryError("Internal Error: Wrong Type Kind ("+pThisType.getTypeKindName()+").(TKArray.java#282)",
					pContext);
		if((pSearchKey != null) && (pParams != null))
			throw new CurryError("Creating an array instance does not require any parameters ("
					+ pThisType.getTypeKindName()+").", pContext);
		
		TArray TA = (TArray)pThisType;
		
		// Prepare the component type and length
		Class<?> C = TA.getContainType().getDataClass();
		int Length = ((TSArray)TA.getTypeSpec()).getLength();
		if((pParams == null) || (pParams.length == 0)) {
			if(Length == -1)
				 return UArray.getEmptyArrayOf(C);
			else return UArray.newArray(C, Length);
		}
		else if((pParams.length == 1) && TA.canBeAssignedBy(pContext, pParams[0])) {
			     return UArray.extractArray(pParams[0], C, 0, true, true);
		} else   return UArray.extractArray(pParams,    C, 0, true, true);
	}

	// Elements --------------------------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindFields(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<AttributeInfo> AIs) {
		if(!pIsStatic || !(pTheType instanceof TArray)) return;
		
		TArray TA = ((TArray)pTheType);
		
		Accessibility Public = Accessibility.Public;
		
		// Add length
		this.addTypeAttributeToAttributeList(pContext, pEngine, AIs,
			this.doType_newAIDirect(pTheType, Public, Public, Public, "length", false, 
				new DataHolderInfo(
					TKJava.TInteger.getTypeRef(),
					TA.getLength(),
					Variable.FactoryName,
					true,
					false,
					true,
					false,
					null
				),
				null,
				null
			)
		);

		// Add containTypeRef
		this.addTypeAttributeToAttributeList(pContext, pEngine, AIs,
			this.doType_newAIDirect(pTheType, Public, Public, Public, "containTypeRef", false, 
				new DataHolderInfo(
					TKJava.TTypeRef.getTypeRef(),
					TA.getContainTypeRef(),
					Variable.FactoryName,
					true,
					false,
					true,
					false,
					null
				),
				null,
				null
			)
		);
	}

	/**{@inheritDoc}*/ @Override
	protected void doType_prepareTypeKindMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {
		if(!(pTheType instanceof TArray)) return;

		
		// Simulate DataArray here
		
		Accessibility Public = Accessibility.Public;
		Engine        E      = this.getEngine();
		MExecutable   ME     = E.getExecutableManager();
		TArray        TA     = (TArray)pTheType;
		TypeRef       TR     = TA.getContainTypeRef();
		
		if(pIsStatic) {
			TKType TKT = (TKType)this.getEngine().getTypeManager().getTypeKind(TKType.KindName);
			
			ExecSignature ES;
			
			// Get ContaintType ----------------------------------------------------------
			ES = ExecSignature.newSignature(
				"getContainType",
				TypeRef.EmptyTypeRefArray,
				UString.EmptyStringArray,
				TKT.newTypeRef(TA.getContainTypeRef())
			);
			this.addTypeOperationToOperationList(pContext, pEngine, OIs,
				this.doType_newOIDirect(
					pTheType,
					Public,
					ME.newSubRoutine(ES,
						ME.newType(TA.getContainTypeRef())
					),
					null
				)
			);

			// Get ContaintTypeRef -------------------------------------------------------
			ES = ExecSignature.newSignature(
				"getContainTypeRef",
				TypeRef.EmptyTypeRefArray,
				UString.EmptyStringArray,
				TKJava.TTypeRef.getTypeRef()
			);
			this.addTypeOperationToOperationList(pContext, pEngine, OIs,
				this.doType_newOIDirect(
					pTheType,
					Public,
					ME.newSubRoutine(ES,
						TA.getContainTypeRef()
					),
					null
				)
			);
			
			// Get Length ----------------------------------------------------------------
			ES = ExecSignature.newSignature(
				"getLength",
				TypeRef.EmptyTypeRefArray,
				UString.EmptyStringArray,
				TKJava.TInteger.getTypeRef()
			);
			this.addTypeOperationToOperationList(pContext, pEngine, OIs,
				this.doType_newOIDirect(
					pTheType,
					Public,
					ME.newSubRoutine(ES,
						TA.getLength()
					),
					null
				)
			);
			
			return;
		}
		
		ExecSignature ES;

		// Add GetLength of the Type
		ES = ExecSignature.newSignature(
			"getLength",
			TypeRef.EmptyTypeRefArray,
			UString.EmptyStringArray,
			TKJava.TInteger.getTypeRef()
		);
		this.addTypeOperationToOperationList(pContext, pEngine, OIs,
			this.doType_newOIDirect(
				pTheType,
				Public,
				ME.newSubRoutine(ES,
					ME.newExpr(Instructions_Array.Inst_GetLengthArrayObject.Name,
						ME.newExpr(
							Instructions_Context.Inst_GetContextInfo.Name,
							Instructions_Context.Inst_GetContextInfo.StackOwner
						)
					)
				),
				null
			)
		);
		
		// Add GetData
		ES = ExecSignature.newSignature(
			"getData",
			new TypeRef[] { TKJava.TNumber.getTypeRef() },
			new String[]  { "I" },
			TR
		);
		this.addTypeOperationToOperationList(pContext, pEngine, OIs,
			this.doType_newOIDirect(
				pTheType,
				Public,
				ME.newSubRoutine(ES,
					ME.newExpr(Instructions_Array.Inst_GetArrayElementAt.Name,
						ME.newExpr(
							Instructions_Context.Inst_GetContextInfo.Name,
							Instructions_Context.Inst_GetContextInfo.StackOwner),
						ME.newExpr(
							Instructions_Context.Inst_GetVarValue.Name,
							"I")
					)
				),
				null
			)
		);
		
		// Add SetData
		ES = ExecSignature.newSignature(
			"setData",
			new TypeRef[] { TKJava.TNumber.getTypeRef(), TR      },
			new String[]  { "I",                         "Value" },
			TR
		);
		this.addTypeOperationToOperationList(pContext, pEngine, OIs,
			this.doType_newOIDirect(
				pTheType,
				Public,
				ME.newSubRoutine(ES,
					ME.newExpr(Instructions_Array.Inst_SetArrayElementAt.Name,
						ME.newExpr(
							Instructions_Context.Inst_GetContextInfo.Name,
							Instructions_Context.Inst_GetContextInfo.StackOwner
						),
						ME.newExpr(
							Instructions_Context.Inst_GetVarValue.Name,
							"I"
						),
						ME.newExpr(
							Instructions_Context.Inst_GetVarValue.Name,
							"Value"
						)
					)
				),
				null
			)
		);
	
		// Add Clone
		ES = ExecSignature.newSignature(
			"clone",
			TypeRef.EmptyTypeRefArray,
			UString.EmptyStringArray,
			TA.getTypeRef()
		);
		this.addTypeOperationToOperationList(pContext, pEngine, OIs,
			this.doType_newOIDirect(
				pTheType,
				Public,
				new JavaExecutable.JavaSubRoutine_Complex(E, ES, null, null) {
		            
		            private static final long serialVersionUID = -3009836901006887137L;
		            
					/**{@inheritDoc}*/ @Override
					protected Object run(Context $Context, Object[] pParams) {
						Object O = $Context.getStackOwner();
						if(O == null) return null;
						
						if(!UArray.isArrayInstance(O))
							throw new CurryError(              "Wrong Type Kind: ", $Context,
							      new IllegalArgumentException("The stackowner of Array.clone() must be an array."));
						
						int    Length = UArray.getLength(O);
						Object NewO   = UArray.newArray(O.getClass().getComponentType(), Length);
						System.arraycopy(O, 0, NewO, 0, Length);
						return NewO;
					}
				},
				null
			)
		);
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	// Other Classes ---------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------
	
	// TypeSpec -------------------------------------------------------------------------
	// TypeInfo structure of Array
	// Data    { Length, ParameterizedTypeInfo, [Documents] }
	// TypeRef { BaseContainTypeRef }

	/** TypeSpec of array types */
	static final public class TSArray extends TypeSpec {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		/** Constructs a new type spec. */
		private TSArray(TypeRef pRef, Serializable[] pDatas, TypeRef[] pRequireTypeRefs) {
			super(pRef, pDatas, pRequireTypeRefs, null);
		}

		// NOTE: BaseArrayTypeSpecs and JavaArrayTypeSpecs are Engine Independent
		static HashMap<Integer, TSArray> BaseArrayTypeSpecs = new HashMap<Integer, TSArray>();	// Dimension
		static HashMap<Integer, TSArray> JavaArrayTypeSpecs = new HashMap<Integer, TSArray>();	// Hash of TypeRef

		
		/** Creates a no-name base array */
		static TSArray getTSNoNameBaseArray(int pDimension) {
			if(pDimension < -1) pDimension = -1;
			
			if((pDimension < 32) || ((pDimension % 100) == 0)) {
				TSArray TSA = BaseArrayTypeSpecs.get(pDimension);
				if(TSA != null) return TSA;
			} 
			
			TSArray TSA = new TSArray(
				null,
				newDataWithOptionalDocumentation(
					null,
					pDimension,
					new ParameterizedTypeInfo(new TypeParameterInfo(ParamName, TKJava.TAny.getTypeRef()))
				),
				TypeRef.EmptyTypeRefArray
			);
			
			
			if((pDimension < 32) || ((pDimension % 100) == 0)) BaseArrayTypeSpecs.put(pDimension, TSA);
			
			return TSA;
		}

		// NOTE: The Engine should only be used to ensure the contain TypeRef is resolved. There is some situation where
		//    pEngine will be given as null but that is when the TypeRef is already resolved (the ref that gets from
		//    TKJava).
		/** Creates a no-name base array */
		static TSArray newTSNamedBaseArray(Engine pEngine, TypeRef pRef, TypeRef pContainTypeRef, int pDimension,
				Documentation pDoc) {
			// Early returns
			if(pContainTypeRef == null) pContainTypeRef = TKJava.TAny.getTypeRef();
			if(pDimension      <    -1) pDimension      = -1;
			
			// Resolve the contain type ref
			if(!pContainTypeRef.isLoaded() && (pEngine != null) && (!(pContainTypeRef instanceof TRPrimitive)))
				pEngine.getTypeManager().ensureTypeExist(null, pContainTypeRef);
			
			// Creates the type spec
			return new TSArray(
					pRef,
					newDataWithOptionalDocumentation(
						pDoc,
						pDimension,
						new ParameterizedTypeInfo(new TypeParameterInfo(ParamName, TKJava.TAny.getTypeRef()))
					),
					TypeRef.EmptyTypeRefArray
				);
		}

		// NOTE: The Engine should only be used to ensure the contain TypeRef is resolved. There is some situation where
		//    pEngine will be given as null but that is when the TypeRef is already resolved (the ref that gets from
		//    TKJava).
		/** Creates a no-name base array */
		static private TSArray getTSParameteredArray(Engine pEngine, TSArray pTSArray, TypeRef[] pPTypeRefs) {
			// Early returns
			if((pTSArray == null) || (pPTypeRefs == null) || (pPTypeRefs.length != 1) || (pPTypeRefs[0] == null))
				return null;
			
			// Default value
			if(pPTypeRefs[0] == null) pPTypeRefs[0] = TKJava.TAny.getTypeRef();
			TypeRef pContainTypeRef = pPTypeRefs[0];

			// Resolve the contain TypeRef
			if(!pContainTypeRef.isLoaded() && (pEngine != null) && (!(pContainTypeRef instanceof TRPrimitive)))
				pEngine.getTypeManager().ensureTypeExist(null, pContainTypeRef);

			// Often used variable
			TypeRef TSACTR = pTSArray.getContainTypeRef();
			
			// Prepare the new TSArray
			TSArray TSA = null;
			TypeRef TRA = new TLParametered.TRParametered(pTSArray, pPTypeRefs);

			// Find in the cache
			if(TSACTR.isLoaded() && (TSACTR.getTheType() instanceof TKJava.TJava)) {
				// Cache for Engine independent (array of Java Types)
				TSA = JavaArrayTypeSpecs.get(UObject.toString(TRA).hashCode());
				if(TSA != null) {
					// TODELETE - Delete it when sure
					//Type T = TSA.getTypeRef().getTheType();
					//if((T != null) && (T instanceof TArray))
					//	TSA.getTypeRef().resetForCompilation();
					return TSA;
				}
			
			} else {
				// Get from the cache - Engine dependent
				TypeSpec TS = pTSArray.findParametedTypeSpecInCache(pPTypeRefs);
				if(TS instanceof TSArray) return (TSArray)TS;
				
			}
			
			// Create new one if needed
			if(TSA == null) {
				// Try to collapse Parametered of Parametered Array
				TypeRef BaseTR = pTSArray.getTypeRef();
				TSArray BaseTS = pTSArray;
				boolean IsNested = false;
				while((BaseTR instanceof TRParametered) && BaseTS.isParametered()) {
					// This works because the target type is always be initialized because its parametered.
					BaseTR   = pTSArray.getParameteredTypeInfo().getOriginalTypeRef();
					BaseTS   = (TSArray)BaseTR.getTheType().getTypeSpec();
					IsNested = true;
				}
				
				// Creates from the deepest nested array
				if(IsNested) return getTSParameteredArray(pEngine, BaseTS, pPTypeRefs);
				
				// Create one -------------------------------------------------------------------------------
				TSA = new TSArray(
						new TLParametered.TRParametered(BaseTS, pPTypeRefs),
						newDataWithOptionalDocumentation(
							null,
							pTSArray.getLength(),
							new ParameteredTypeInfo(BaseTR, new TypeParameterInfo(ParamName, pContainTypeRef))
						),
						TypeRef.EmptyTypeRefArray
					);
			}
			
			// Save the cache
			if(TSACTR.getTheType() instanceof TKJava.TJava) 	// Cache for Engine independent
				JavaArrayTypeSpecs.put(UObject.toString(TRA).hashCode(), TSA);
			
			else {
				// Save to the cache - Engine dependent
				pTSArray.saveParametedTypeSpecInCache(TSA, pPTypeRefs);
				
			}
			
			return TSA;
		}

		// Classification ----------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public String getKindName() {
			return KindName;
		}
		
		// Services ----------------------------------------------------------------------
		
		/** Returns the length of the array */
		public int getLength() {
			return ((Integer)this.getData(IndexDimension)).intValue();
		}
		/** Returns the contain TypeRef of this array */
		public TypeRef getContainTypeRef() {
			// Type NoName base type or Parametered one
			return this.getParameterTypeRef(ParameterIndex_ContainTypeRef);
		}
		
		/** Returns the index in the data array pointing to the ExtraData */
		public int getExtraDataIndex() {
			return IndexExtraData;
		}
		
		/** {@inheritDoc} */ @Override
		protected int getParameterizationInfoIndex() {
			return IndexParameterizedTypeInfo;
		}
		
		// Parameterization --------------------------------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForParameterization() {}

		/**{@inheritDoc}*/ @Override 
		protected boolean isSelfProvide_TypeSpecOfParameteredTypeSpec(Engine pEngine) {
			return true;
		}
		
		/**{@inheritDoc}*/ @Override
		protected TypeSpec newTypeSpecOfParameteredTypeSpec(Context pContext, Engine pEngine, TypeRef ... pPTypeRefs) {
			if((pPTypeRefs == null) || (pPTypeRefs.length != 1) || (pPTypeRefs[0] == null))
				throw new IllegalArgumentException(
					"Type Parameterization error: invalid parameter types `"+UArray.toString(pPTypeRefs, "<", ">", ",")+
					"`for the type '"+this.getTypeRef()+"'.");

			TypeRef CTRef = this.getContainTypeRef();
			
			// Early returns
			TypeRef TRef = pPTypeRefs[0];
			if(TRef.equals(CTRef)) return this;
			
			// Ensure that the new Types is compatible
			if(!MType.CanTypeRefByAssignableBy(pContext, pEngine, CTRef, TRef))
				TLParametered.throwUnmatchTypeParameter(this, pPTypeRefs);
			
			// Create one
			return getTSParameteredArray(pEngine, this, pPTypeRefs);
		}
		
		// For compilation only --------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForCompilation() {}
		
		// Representation ---------------------------------------------------------------------------------------------- 

		/**{@inheritDoc}*/ @Override
		protected boolean isToShowNoName() {
			return false;
		}
		/**{@inheritDoc}*/ @Override
		protected boolean isToShowParameteredInfo() {
			return false;
		}
		/**{@inheritDoc}*/ @Override
		protected String getToString() {
			return this.getToString(null);
		}
		/**{@inheritDoc}*/ @Override
		protected String getToString(TRParametered pParameteredTypeRef) {
			// Get the contain type
			TypeRef TR = (pParameteredTypeRef == null)
			                 ? this.getContainTypeRef()
			                 : pParameteredTypeRef.getParameterTypeRef(ParameterIndex_ContainTypeRef);
			// Default one
			if(TR == null) TR = TKJava.TAny.getTypeRef();
			
			// Default value
			if(TR == null) TR = TKJava.TVoid.getTypeRef();
			String Dimension = "[" + ((this.getLength() == -1)?"":"" + this.getLength()) + "]";
			
			String TStr = TR.toString();
			if(TR.getTheType() instanceof TArray) {
				int I = TStr.indexOf("[");
				if(I == -1) return TStr + Dimension;

				return TStr.substring(0, I) + Dimension + TStr.substring(I);
			}
			return TStr + Dimension;
		}
	}
	
	// Type  -----------------------------------------------------------------------------

	/** Array Types */
	static final public class TArray extends Type {
		protected TArray(TypeKind pKind, TypeSpec pTypeSpec) {
			super(pKind, pTypeSpec);
		}

		/** Returns the contains TypeRef */
		public TypeRef getContainTypeRef() {
			return ((TSArray)this.getTypeSpec()).getContainTypeRef();
		}
		/** Returns the contains type */
		Type getContainType()    {
			TypeRef TR = this.getContainTypeRef();
			if(TR == null) return null;
			
			if(!TR.isLoaded()) this.getEngine().getTypeManager().ensureTypeExist(null, TR);
			return TR.getTheType();
		}
		
		/** Returns the lenght of this Array Type or -1 for any length */
		public int getLength() {
			return ((TSArray)this.getTypeSpec()).getLength();
		}
		
		/** Returns the componet class */
		public Class<?> getComponentClass() {
			Type T = this.getContainType();
			if(T == null) {
				this.TKind.getEngine().getTypeManager().ensureTypeInitialized(this.getContainTypeRef());
				T = this.getContainType();
			}
			
			if(T == null) return Object.class;
			return T.getDataClass();
		}	
	}
}
