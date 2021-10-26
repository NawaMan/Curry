package net.nawaman.curry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import net.nawaman.curry.OperationInfo.OIDirect;
import net.nawaman.curry.OperationInfo.OIDlgObject;
import net.nawaman.curry.OperationInfo.OINative;
import net.nawaman.curry.OperationInfo.SimpleOperation;
import net.nawaman.curry.TKArray.TArray;
import net.nawaman.curry.TKJava.TJava;
import net.nawaman.curry.TKPackage.TPackage;
import net.nawaman.curry.TKType.TType;
import net.nawaman.curry.TKVariant.TVariant;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;
import net.nawaman.javacompiler.JavaCompiler;
import net.nawaman.util.Objectable;
import net.nawaman.util.UClass;
import net.nawaman.util.UNumber;
import net.nawaman.util.UString;

/** Data type in Curry scope **/
abstract public class Type extends StackOwner_Simple implements Objectable {
	
	static public final Type[] EmptyTypeArray = new Type[0];
	
	/** Construct a new type **/
	protected Type(TypeKind pTKind, TypeSpec pTSpec) {
		{// Precondition //
			if(pTKind == null) {
				if(pTSpec == null)
					 throw new NullPointerException("Type kind and spec of a type must be null.");
				else throw new NullPointerException("Type kind of a type must not be null (Spec = "+pTSpec.toString()+").");
			}
				
			if(pTSpec == null)
				throw new NullPointerException("Type spec of a type must not be null (Kind = "+pTKind.toString()+").");
		}
		
		this.TKind = pTKind;
		this.TSpec = pTSpec;
	}
	
	// Classification --------------------------------------------------------------------
	
	final TypeKind TKind;

	/** Returns the type kind of this type. */
	final public TypeKind getTypeKind() {
		return this.TKind;
	}

	/** Returns the name of the kind this type is. */
	final public String getTypeKindName() {
		return this.TSpec.getKindName();
	}

	// Spec and Ref ----------------------------------------------------------------------
	/** The spec of this type */
	final TypeSpec TSpec;

	/** Returns the spec of this type. */
	final public TypeSpec getTypeSpec() {
		return this.TSpec;
	}

	/** Returns the type ref of this type */
	final public TypeRef getTypeRef() {
		return this.TSpec.getTypeRef();
	}
	
	// Info ---------------------------------------------------------------------------------------

	// Make this object immutable
	static class ImmutableTypeInfo extends TypeInfo {
		ImmutableTypeInfo(Type pSO)            { super(pSO);  }
		public @Override boolean isImmutable() { return true; }
	}
	
	/** {@inheritDoc}*/ @Override
	public TypeInfo getSOInfo() {
		if(this.SOInfo == null) this.SOInfo = new ImmutableTypeInfo(this);
		return (ImmutableTypeInfo)this.SOInfo;
	}
	/** Returns the TypeInfo object for this type */
	public TypeInfo getTypeInfo() {
		if(this.SOInfo == null) this.SOInfo = new ImmutableTypeInfo(this);
		return (ImmutableTypeInfo)this.SOInfo;
	}
	
	// Resolution and Initialization -----------------------------------------------------

	/** Returns the status of this type */
	final protected TypeSpec.Status getStatus() {
		return this.TSpec.TypeStatus;
	}
	/** Checks if this type is unloaded */
	final protected boolean isUnloaded() {
		return this.TSpec.isUnloaded();
	}
	/** Checks if this type is loaded */
	final protected boolean isLoaded() {
		return this.TSpec.isLoaded();
	}
	/** Checks if this type is resolved */
	final protected boolean isResolved() {
		return this.TSpec.isResolved();
	}
	/** Checks if this type is validated */
	final protected boolean isValidated() {
		return this.TSpec.isValidated();
	}
	/** Checks if this type is initialized */
	final protected boolean isInitialized() {
		return this.TSpec.isInitialized();
	}

	// Internal Utilities ----------------------------------------------------------------

	// Engine
	/**{@inheritDoc}*/ @Override
	final public Engine getEngine() {
		return this.TKind.getEngine();
	}
	
	/** This is for all types to be able to access Type of a ref without effect of content */
	final protected Type getTypeFromRef(TypeRef pTRef) {
		if(pTRef == null) return null;
		return pTRef.getTheType();
	}
	/** Returns the data class of this type or null if there is no specific class for the type. */
	final protected Class<?> getDataClass() {
		return this.TKind.getTypeDataClass(null, this.getTypeSpec());
	}
	/** Returns the class of this type. */
	final protected Class<? extends Type> getTypeClass() {
		return this.getClass();
	}
	
	// AsNative --------------------------------------------------------------------------------------------------------

	static private final String MethodCode =
		"	public %s %s(%s) {\n"+ // <?=Method Return Class?>, <?=Method Name>, <?=List of Method Class and parameter name?>
		"		if((this.ESs == null) || (this.ESs[%s] == null)) {\n"+	//<?=MIndex?>
		"			this.ESs = new %s[%s];\n"+
		"			%s MT = this.DO.getDObjectInfo().getEngine().getTypeManager();\n"+		//<?=Name of MType?>
		"			this.ESs[%s] = this.DO.getDObjectInfo().searchOperation(\"%s\", %s);\n"+ // <?MIndex?>, <?=Method Name>, <?=Array of Method class?>
		"		}\n"+
		"		%s;\n"+
		"	}\n";
	
	static private final String ClassCode = 
		"public class %s implements %s%s {\n"+	// TypeName, net.nawaman.curry.DObjectStandalone, List of Java Interface
		"	private %s DO = null;\n"+	// net.nawaman.curry.DObject
		"	static private %s[] ESs = null;\n"+	// net.nawaman.curry.ExecSignature
		"	final public void setAsDObject(%s pDO) { if(this.DO == null) this.DO = pDO; }\n"+	// net.nawaman.curry.DObject
		"	@Override final public Object getAsNative()  { return this;    }\n"+
		"	@Override final public Object getAsDObject() { return this.DO; }\n"+
		"%s"+
		"}";
	
	static private final String TypeRefName = TypeRef.class.getCanonicalName();
	
	/** Returns the code for a native method of a AsNative  */
	final String getAsNativeMethod(Method M, int MIndex, int MCount) {
		Class<?> Return      = M.getReturnType();
		String   ReturnClass = Return.getCanonicalName();
		String   MethodName  = M.getName();

		StringBuilder ReturnExpr = new StringBuilder();
		if(Return != void.class) {
			ReturnExpr.append(String.format("return ((%s)(%s))%s",
				UClass.getCLASS(M.getReturnType()).getCanonicalName(),
				"%s",
				((Return != void.class) && (Return != UClass.getCLASS(Return)))
					?"."+Return.getName()+"Value()"
					:""
			));
		} else ReturnExpr.append("%s");
			
		
		StringBuilder PLists = new StringBuilder();
		StringBuilder PClses = new StringBuilder();
		StringBuilder Params = new StringBuilder();
		Class<?>[]    PCls   = M.getParameterTypes();
		for(int i = 0; i < PCls.length; i++) {
			if(i != 0) {
				PLists.append(", ");
				PClses.append(", ");
				Params.append(", ");
			}
			
			String   PName  = "P"+i;
			Class<?> PClass = PCls[i];
			
			boolean IsVarArgIndex = M.isVarArgs() && (i == (PCls.length - 1));
			if(!IsVarArgIndex)
				 PLists.append(PClass.getCanonicalName()                   ).append(" "    ).append(PName);
			else PLists.append(PClass.getComponentType().getCanonicalName()).append(" ... ").append(PName);
			
			PClses.append("MT.getTypeOfTheInstanceOf(").append(PClass.getCanonicalName()).append(".class).getTypeRef()");
			
			if(IsVarArgIndex) Params.append("(").append(PClass.getCanonicalName()).append(")");
			Params.append(PName);
		}
		
		ReturnExpr = new StringBuilder(String.format(ReturnExpr.toString(),
			String.format(
				"this.DO.invokeDirect(this.ESs[%s], %s)",
				MIndex,
				(Params.length() == 0)?"(Object[])null, true":"new Object[] { " + Params + " }, true"
			)
		));
		
		return String.format(MethodCode,
				ReturnClass, MethodName, PLists,
				MIndex,
				ExecSignature.class.getCanonicalName(), MCount,
				MType.class.getCanonicalName(),
				MIndex, MethodName, (PClses.length() == 0)?"("+TypeRefName+"[])null":"new "+TypeRefName+"[] {" + PClses + " } ",
				ReturnExpr.toString()
			);
	}
	
	/** Returns the class of AsNative object if this type has a DObject object */
	final Class<? extends DObjectStandalone> getAsNativeClass(Engine pEngine) {
		MType    MT     = pEngine.getTypeManager();
		TypeSpec TS     = this.getTypeSpec();
		int      ICount = TS.getInterfaceCount();
		
		StringBuilder            ItfCode = new StringBuilder();
		HashMap<Integer, Method> IMds    = new HashMap<Integer, Method>();
		for(int i = ICount; --i >= 0;) {
			TypeRef IRef  = TS.getInterfaceRefAt(i);
			Type    IType = MT.getTypeFromRefNoCheck(null, IRef);
			if(!(IType instanceof TKJava.TJava))    continue;
			if(!TKInterface.isTypeInterface(IType)) continue;
			
			if( (IType.getDataClass() == net.nawaman.curry.DObjectStandalone.class) ||
				(IType.getDataClass() == net.nawaman.curry.TypedData        .class) ||
				(IType.getDataClass() == java.lang.reflect.InvocationHandler.class)
			) continue;
			
			Class<?> Cls = IType.getDataClass();
			ItfCode.append(", ").append(Cls.getCanonicalName());
			
			Method[] Ms = Cls.getMethods();
			for(int m = 0; m < Ms.length; m++)
				IMds.put(Ms[m].toString().hashCode(), Ms[m]);
		}
		
		// No interface so do nothing
		if(ItfCode.length() == 0) return null;

		StringBuilder MtdCode = new StringBuilder();
		int m      = 0;
		int mCount = IMds.size();
		for(Method M : IMds.values()) {
			if(MtdCode.length() != 0) MtdCode.append("\n");
			MtdCode.append(this.getAsNativeMethod(M, m++, mCount));
		}
		
		String TypeName = "$ClsOf_"+UNumber.abs(this.toString().hashCode());
		
		if(this.getTypeRef() instanceof TLPackage.TRPackage)
			TypeName += ((TLPackage.TRPackage)this.getTypeRef()).PName+"_"+((TLPackage.TRPackage)this.getTypeRef()).TName;
		
		else if(this.getTypeRef() instanceof TLPrimitive.TRPrimitive) {
			String Alias = ((TLPrimitive.TRPrimitive)this.getTypeRef()).Alias;
			if(Alias != null) TypeName += "_" + Alias;
			else {
				String Name  = ((TLPrimitive.TRPrimitive)this.getTypeRef()).Name;
				TypeName += "_" + ((Name == null)?"":Name.replace('.', '_'));
			}
		}
		
		String ClsCode = String.format(ClassCode,
				TypeName, DObjectStandalone.class.getCanonicalName(), ItfCode,
				DObject.class.getCanonicalName(),
				ExecSignature.class.getCanonicalName(),
				DObject.class.getCanonicalName(),
				MtdCode
			);
		
		Engine E = this.getEngine();
		String    Error = null;
		Exception Excpt = null;
		try {
			JavaCompiler JC = E.getClassPaths().getJavaCompiler();
			JC.addCode(TypeName+".java", "", ClsCode.toString());
			Error = JC.compile();
			if(Error == null) return JC.getClassByName(TypeName).asSubclass(DObjectStandalone.class);
		} catch(Exception Ec) { Excpt = Ec; }
		throw new CurryError("Unable to create a Native class for the Type `"+this+"`"+((Error == null)?"":": " + Error)+".", Excpt);
	}
	
	// Super --- -------------------------------------------------------------------------------------------------------
	
	/** Returns the RypeRef of the super type */
	final public TypeRef getSuperRef() {
		TypeRef TR = this.getTypeSpec().getSuperRef();
		return (TR != null) ? TR : TKJava.TAny.getTypeRef();
	}

	// Typing ---------------------------------------------------------------------------

	// Type Checking -----------------------------------------------------------
	
	/**
	 * Checks if a variable of this type can be assigned by the object pObject.
	 * 
	 * @param pObject the object to check
	 * @return true if the input object is allowed to be assigned to a variable of this type
	 */
	final protected boolean canBeAssignedBy(Object pObject) {
		return this.canBeAssignedBy(null, pObject);
	}

	/**
	 * Checks if a variable of this type can be assigned by the object pObject.
	 * 
	 * @param pObject the object to check
	 * @return true if the input object is allowed to be assigned to a variable of this type
	 */
	final boolean canBeAssignedBy(Context pContext, Object pObject) {
		return this.TKind.canTypeBeAssignedBy(pContext, this, pObject);
	}

	/**
	 * Checks if a variable of this type can be assigned by an instance of the type pType.
	 * 
	 * @param pObject the type to check
	 * @return true if an instance of the input type is allowed to be assigned to a variable of this type
	 */
	final protected boolean canBeAssignedByInstanceOf(Type pType) {
		return this.canBeAssignedByInstanceOf(null, pType);
	}
	
	/**
	 * Checks if a variable of this type can be assigned by an instance of the type pType.
	 * 
	 * @param pObject the type to check
	 * @return true if an instance of the input type is allowed to be assigned to a variable of this type
	 */
	final boolean canBeAssignedByInstanceOf(Context pContext, Type pType) {
		return this.TKind.canTypeBeAssignedByInstanceOf(pContext, this, pType);
	}

	/** Returns the data class of this type or null if there is no specific class for the type. */
	final Class<?> getDataClass(Context pContext) {
		TypeRef TRef = this.TSpec.Ref;
		if(!TRef.isLoaded()) TRef.setTheType(this);
		return this.TKind.getTypeDataClass(pContext, this.TSpec);
	}
	
	// Initializers ------------------------------------------------------------

	protected ConstructorInfo[] ConstructorInfos = null;
	
	/** Returns the initializers for initializing newly created instance. */
	final protected ConstructorInfo[] getConstructorInfos(Context pContext) {
		if(this.ConstructorInfos == null) {
			// Try the best to get the engine
			Engine $Engine;
			if((($Engine = this.getEngine()) == null) &&		
			   ((pContext == null) || ($Engine = pContext.getEngine()) == null))
			   $Engine = Engine.An_Engine;
			
			// Ensure that the type is valid
			$Engine.getTypeManager().ensureTypeValidated(pContext, this, null);
				
			// Get the constractor from the TypeKind
			this.ConstructorInfos = this.TKind.getConstructorInfos(pContext, $Engine, this);
			
			// Ensure there a constructor ------------------------------------------------------------------------------
			if((this.ConstructorInfos == null) || (this.ConstructorInfos.length == 0)) {
				boolean HasSuper = TypeInfo.isTypeHasSuper($Engine, this);
				if(HasSuper) {	// A constructor is required if the super has no default constructor - this ensure that 
					TypeRef SuperTR = TypeInfo.getSuperRefOf($Engine, this);
					Type    SuperT  = SuperTR.getTheType();
					if((SuperT == null) || !SuperT.isValidated()) {
						$Engine.getTypeManager().ensureTypeValidated(pContext, SuperTR, null);
						SuperT = SuperTR.getTheType();
					}
					
					ConstructorInfo CI = TypeKind.SearchConstructorInfo(pContext, $Engine, SuperT, TypeRef.EmptyTypeRefArray);
					if(CI == null) {
						// Check if this type have no constructor and super type also does not.
						throw new CurryError("The type `"+this+"` and its super type does not have a default " +
								"constructor ("+this+") <Type:388>.", pContext);
					}
				}
				
				// Get the executable manager
				MExecutable ME = $Engine.getExecutableManager();
				
				this.ConstructorInfos =
					new ConstructorInfo[] {
						// Create a default constructor ----------------------------------------------------------------
						HasSuper
							? new ConstructorInfo.CIMacro(this.getEngine(), Public, this.getTypeRef(),
								new CurryExecutable.CurryMacro($Engine, 
									ExecSignature.newProcedureSignature(ConstructorInfo.ConstructorSignatureName,
										TKJava.TVoid.getTypeRef(),
										null, null),
									ME.newExpr("super_initialize_ByParams"),
									null, null
								),
								null
							)
							: new ConstructorInfo.CIRoot($Engine, Public, this.getTypeRef())
					};
			}
			
			boolean IsNeedToCheckParam = this.getTypeSpec().isParametered() || this.getTypeSpec().isParameterized();
			for(int i = 0; i < this.ConstructorInfos.length; i++) {
				ConstructorInfo C = this.ConstructorInfos[i];
				
				// If it is not Native or Root, clone
				if(C instanceof ConstructorInfo.CIMacro)
					this.ConstructorInfos[i] = (C = ((ConstructorInfo.CIMacro)C).clone()); 
				
				// Resolve the constructor signature
				C.resolve($Engine, this, IsNeedToCheckParam);
			}
		}
		return this.ConstructorInfos;
	}

	// Instantiation -----------------------------------------------------------

	/** Checks if this type is abstract (cannot construct an instance). */
	final boolean isAbstract(Context pContext) {
		return this.TKind.isTypeAbstract(pContext, this);
	}
	
	// Default Value ----------------------------------------------------------------------------------------

	static private HashMap<Type, OperationInfo> OIs_DefaultValue       = new HashMap<Type, OperationInfo>();
	static private HashMap<Type, OperationInfo> OIs_NoNullDefaultValue = new HashMap<Type, OperationInfo>();
	
	final private OperationInfo getDefaultValueOI(Context pContext, boolean IsNoNull) {
		HashMap<Type, OperationInfo> Cached = IsNoNull ? OIs_NoNullDefaultValue : OIs_DefaultValue;
		OperationInfo OI = Cached.get(this);
		
		// See if the operation is this method (native and belong to Type)
		if((OI == null) && (pContext != null)) {
			ExecSignature ES = this.searchTypeOperation(pContext.getEngine(),
			                       IsNoNull ? "getNoNullDefaultValue" : "getDefaultValue",
			                       (TypeRef[])null
			                   );
			
			OI = this.getOperation(pContext, null, null, ES);
			if(OI == null)
				OI = OperationInfo.NoPermission;
			
			Cached.put(this, OI);
		}
		return OI;
	}
	
	/** Do the default toString */
	final private Object do_getDefaultValue(Context pContext, boolean IsNoNull) {
		OperationInfo OI = null;
		
		Object O = null; 
		if((pContext == null) || ((OI = this.getDefaultValueOI(pContext, IsNoNull)) == OperationInfo.NoPermission))
			// If so, run the native
			 O = IsNoNull
			     ? this.doDefault_getNoNullDefaultValue(pContext)
			     : this.doDefault_getDefaultValue(pContext);
			// Else, run the StackOwner's one
		else O = this.invokeDirect(pContext, null, true, null, OI.getSignature(), (Object[])null);
		
		if((O != null) && !this.canBeAssignedBy(O))
			throw new CurryError("Invalid return type for "+this+"::"+
			             (IsNoNull ? "getNoNullDefaultValue" : "getDefaultValue")+
			             "(). <Type:509>");
		return O;
	}
	
	/** Do the default getDefaultValue */
	protected Object doDefault_getDefaultValue(Context pContext) {
		// Should call the one int the TypeKind
		return this.TKind.getTypeDefaultValue(pContext, this);
	}
	
	/**
	 * Returns the default value of this type.<br/> The default value will be used when a variable is created and when
	 * an array of the type is created.
	 */
	final Object getDefaultValue(Context pContext) {
		return this.do_getDefaultValue(pContext, false);
	}
	
	// NoNull Default Value ---------------------------------------------------------------------------------

	/** Do the default toString */
	protected Object doDefault_getNoNullDefaultValue(Context pContext) {
		// Should call the one int the TypeKind
		return this.TKind.getTypeNoNullDefaultValue(pContext, this);
	}
	
	/** Do the default toString */
	final Object getNoNullDefaultValue(Context pContext) {
		return this.do_getDefaultValue(pContext, true);
	}
	
	// New Instance -----------------------------------------------------------------------------------------

	/** Creates a new instance of this type in the Context using the parameter. */
	final Object newInstance(Context pContext, Executable pInitiator, Object pSearchKey, Object[] pParams) {
		if(this.isAbstract(pContext)) {
			throw new CurryError(
				String.format(
					"Instance Error: The type '%s' is abstract and unable to create a new instance. <Type:519>",
					this
				),
				pContext);
		}
	
		Engine E = this.getEngine();
		if((E == null) && (pContext != null)) E = pContext.getEngine();
		
		// If this type is a BasedOnType, flaten it first
		if(this.getTypeRef() instanceof TRBasedOnType) {
			TypeRef TRef = TLBasedOnType.flatBaseOnType(E, this.getTypeRef(), null, null);
			if((TRef != null) && !TRef.equals(this.getTypeRef())) {
				Type Type = E.getTypeManager().getTypeFromRefNoCheck(pContext, TRef);
				if((Type != null) && (Type != this))
					return Type.newInstance(pContext, pInitiator, pSearchKey, pParams); 
			}
		}
		
		// The type must be initialized to create a new instance
		E.getTypeManager().ensureTypeInitialized(this);	// -> This will ensure the type attribute is prepared
		
		// The object field list should be prepared before any instance is created.
		this.ensureObjectAttributesPrepared(pContext, E);
		return this.TKind.newTypeInstance(pContext, pInitiator, this, pSearchKey, pParams);
	}
	
	/** Creates a new instance of this type in the Context using the parameter. */
	final public Object newInstance(Object[] pParams) {
		return this.newInstance(null, null, null, pParams);
	}

	/** Creates a new instance of this type in the Context using the parameter. */
	final public Object newInstance(TypeRef[] pSearchKey, Object[] pParams) {
		return this.newInstance(null, null, pSearchKey, pParams);
	}

	/** Creates a new instance of this type in the Context using the parameter. */
	final public Object newInstance(ExecInterface pSearchKey, Object[] pParams) {
		return this.newInstance(null, null, pSearchKey, pParams);
	}
	
	// Search for constructor --------------------------------------------------
	
	/** Creates a new instance of this type in the Context using the parameter. */
	final protected ExecInterface searchConstructorLocal(Object pSearchKey) {
		return this.searchConstructorLocal(this.getEngine(), pSearchKey);
	}
	
	/** Creates a new instance of this type in the Context using the parameter. */
	final protected ExecInterface searchConstructorLocal(Engine pEngine, Object pSearchKey) {
		ConstructorInfo CI = this.TKind.searchConstructor(null, pEngine, this, pSearchKey);
		if(CI == null) return null;
		return CI.getSignature().getInterface();
	}
	
	// Representation and other -----------------------------------------------

	/** Returns a description of this type. */
	final protected String getDescription() {
		return this.getDescription(this.getEngine());
	}
	
	/** Returns a description of this type. */
	final protected String getDescription(Engine pEngine) {
		return this.getTypeSpec().getDescription(pEngine);
	}
	
	/** Return MoreData of this type.  */
	final protected MoreData getMoreData() {
		MoreData MD = this.getTypeSpec().getMoreData();
		return (MD == null)?MoreData.Empty:MD;
	}

	/** Returns ExtraInfo of this type. */
	final protected MoreData getExtraInfo() {
		MoreData MD = this.getTypeSpec().getExtraInfo();
		return (MD == null)?MoreData.Empty:MD;
	}

	// Objectable --------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public String toString() {
		return this.getTypeSpec().Type_toString();
	}

	/**{@inheritDoc}*/ @Override
	final public String toDetail() {
		return this.getTypeSpec().toString();
	}

	/**{@inheritDoc}*/ @Override
	final public boolean is(Object O) {
		return this == O;
	}

	/**{@inheritDoc}*/ @Override
	final public boolean equals(Object O) {
		if(this == O)            return true;
		if(!(O instanceof Type)) return false;
		return this.getTypeSpec().equals(((Type)O).getTypeSpec());
	}

	/**{@inheritDoc}*/ @Override
	final public int hash() {
		return UString.hash("Type") + UString.hash(this.getTypeSpec().toString());
	}

	/**{@inheritDoc}*/ @Override
	final public int hashCode() {
		return super.hashCode();
	}
	
	// *****************************************************************************************************************
	// StackOwner Related **********************************************************************************************
	// *****************************************************************************************************************
	
	/** Predefined accessibility as Private */
	static final public Accessibility Public    = Accessibility.Public;
	static final public Accessibility Private   = Accessibility.Private;
	static final public Access        Protected = Access       .Protected;
	
	/** A type for object type artifact accessibility */
	static abstract public class Access extends Accessibility {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		protected Access() {}
		
		/** Predefine protected */
		static public final Access Protected = new Access() {
            
            private static final long serialVersionUID = -3009836901006887137L;
            
			@Override public    String  getName()     { return "Protected"; }
			@Override public    boolean isProtected() { return true;        }
			@Override protected boolean checkEqual(Accessibility pAcc) {
				if(pAcc instanceof Access) return ((Access)pAcc).isProtected();
				return false;
			}
			@Override
			public boolean isAllowed(Engine pEngine, Object pAccess, Type pAccessAsType, Package pAccessPackage,
					StackOwner pHost, Accessible pAccessible) {
					
				if(pHost == null)
					return false;
					
				StackOwner SO = pAccessAsType;
				if(SO ==  null)
					return false;
				
				if(SO == pHost)
					return true;
				
				if(pAccess == pHost)
					return true;

				// Same package
				if(pEngine.getPackageOf(pHost) == pAccessPackage)
					return true;

				// Derived type
				if(pHost instanceof Type) {
					TypeRef HostTRef   = ((Type)pHost).getTypeRef();
					TypeRef AccessTRef = pAccessAsType.getTypeRef();
					return MType.CanTypeRefByAssignableByInstanceOf(null, pEngine, HostTRef, AccessTRef);
				}

				return false;
			}
		};
		
		/** Checks if the permission is protected */
		public boolean isProtected() {
			return false;
		}
	}

	// *********************************************************************************************
	// A/O from Native *****************************************************************************
	// *********************************************************************************************

	// The following 3 methods of StackOwner_Simple is ignored by Type because Type has different
	// way to do it
	// protected void prepareFields(Engine pEngine) {}
	// protected void prepareMethods(Engine pEngine) {}
	// protected void initializeMoreElements(Engine pEngine) {}

	// Object Artifacts ----------------------------------------------

	/** Object attribute informations */ // Get prepared in newInstance()
	AttributeInfo[]	ObjAttrInfos = null;
	/** Object operation informations */
	OperationInfo[]	ObjOperInfos = null;

	/**{@inheritDoc}*/ @Override
	protected void prepareAttrs(Context pContext, Engine pEngine) {
		this.prepareTypeFields(pContext, pEngine);
	}
	/**{@inheritDoc}*/ @Override
	protected void prepareOpers(Context pContext, Engine pEngine) {
		this.prepareTypeMethods(pContext, pEngine);
	}
	
	// Ensure element prepared ---------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected void ensureAttributesPrepared(Context pContext, Engine pEngine) {
		boolean IsNull = (this.AttrInfos == null);
		
		if(!IsNull && ((this.AttrInfos.length == 0) || (this.AttrInfos[0] != null))) 
			return;

		// Prepare the object field
		if(IsNull)
			this.prepareAttrs(pContext, pEngine);
		
		// Only need to collapse when the first element is null
		if((this.AttrInfos != null) &&
		   (this.AttrInfos.length != 0) &&
		   (this.AttrInfos[0] == null)) {
			this.AttrInfos = this.collapseAttributes(pContext, pEngine, this.AttrInfos, true);
		}
	}
	/**{@inheritDoc}*/ @Override
	protected void ensureOperationsPrepared(Context pContext, Engine pEngine) {
		boolean IsNull = (this.OperInfos == null);
		
		// Is already prepared and collapsed
		if(!IsNull && ((this.OperInfos.length == 0) || (this.OperInfos[0] != null)))
			return;
		
		// Prepare the object method
		if(IsNull)
			this.prepareOpers(pContext, pEngine);

		// Only need to collapse when the first element is null
		if((this.OperInfos != null) &&
		   (this.OperInfos.length != 0) &&
		   (this.OperInfos[0] == null)) {
			this.OperInfos = this.collapseOperations(pContext, pEngine, this.OperInfos, true);
		}
	}
	
	/** Ensure that the object attributes are prepared */
	protected void ensureObjectAttributesPrepared(Context pContext, Engine pEngine) {
		boolean IsNull = (this.ObjAttrInfos == null);
		
		if(!IsNull && ((this.ObjAttrInfos.length == 0) || (this.ObjAttrInfos[0] != null))) 
			return;

		// Prepare the object field
		if(IsNull)
			this.prepareObjectFields(pContext, pEngine);
		
		// Only need to collapse when the first element is null
		if((this.ObjAttrInfos != null) &&
		   (this.ObjAttrInfos.length != 0) &&
		   (this.ObjAttrInfos[0] == null)) {
			this.ObjAttrInfos = this.collapseAttributes(pContext, pEngine, this.ObjAttrInfos, false);
		}
	}
	/** Ensure that the object operations are prepared */
	protected void ensureObjectOperationsPrepared(Context pContext, Engine pEngine) {
		boolean IsNull = (this.ObjOperInfos == null);
		
		// Is already prepared and collapsed
		if(!IsNull && ((this.ObjOperInfos.length == 0) || (this.ObjOperInfos[0] != null)))
			return;
		
		// Prepare the object method
		if(IsNull)
			this.prepareObjectMethods(pContext, pEngine);

		// Only need to collapse when the first element is null
		if((this.ObjOperInfos != null) &&
		   (this.ObjOperInfos.length != 0) &&
		   (this.ObjOperInfos[0] == null)) {
			this.ObjOperInfos = this.collapseOperations(pContext, pEngine, this.ObjOperInfos, false);
		}
	}
	
	// DataHolders ---------------------------------------------------------------------------------
	
	// This number is the number of DataHolder(s) (the direct attribute)
	// The number is used when creating object instance
	private int	MaxObjectDHIndex = 0;

	protected int  getMaxObjectDHIndex() { return this.MaxObjectDHIndex; }
	protected void incMaxObjectDHIndex() { this.MaxObjectDHIndex++;      }

	/**{@inheritDoc}*/ @Override 
	protected void doEnsureDHSpace(Context pContext) {
		if(this.getTypeInfo().isParametered(this.getEngine())) {
			MType MT = this.getEngine().getTypeManager();
			
			Type OType = MT.getTypeFromRefNoCheck(pContext, this.getTypeInfo().getParameteredTypeInfo(this.getEngine()).getOriginalTypeRef());
			if(!OType.isInitialized()) {
				MT.ensureTypeInitialized(OType);
				// Ensure the attribute is prepared
				OType.getTypeInfo().getAttributeInfos();
			}
			// Static attribute of the parametered type is got from the original parameterized type
			this.Attrs = OType.Attrs;
		}
		super.doEnsureDHSpace(pContext);
	}

	/**{@inheritDoc}*/ @Override
	boolean addAttrInfo(AttributeInfo pAttrInfo) {
		if(!this.isElementAppendable()) return false;
		if(this.getTypeInfo().isParametered(this.getEngine())) {
			Type OType = this.getTypeInfo().getParameteredTypeInfo(this.getEngine()).getOriginalTypeRef().getTheType();
			if(!OType.isInitialized()) this.getEngine().getTypeManager().ensureTypeInitialized(OType);
			// Calls the one from the original type
			return OType.addAttrInfo(pAttrInfo);
		}
		
		// Ensure the attributes have been prepared
		this.ensureAttributesPrepared(null, null);
		
		// Call super's
		return super.addAttrInfo(pAttrInfo);
	}
	
	// To be used --------------------------------------------------------------------------------- 

	/** Adds a lower level attribute into the list of attribute info */
	protected void addTypeAttributeToAttributeList(Context pContext, Engine pEngine, Vector<AttributeInfo> AIs,
			AttributeInfo AI) {
		TypeKind.Regular_addAttributeToAttributeList(pContext, pEngine, AIs, AI);
	}
	/** Adds a lower level attribute into the list of attribute info */
	protected void addDataAttributeToAttributeList(Context pContext, Engine pEngine, Vector<AttributeInfo> AIs,
			AttributeInfo AI) {
		TypeKind.Regular_addAttributeToAttributeList(pContext, pEngine, AIs, AI);
	}

	/** Adds a lower level operation into the list of operation info */
	protected void addTypeOperationToOperationList(Context pContext, Engine pEngine,
			Vector<OperationInfo> OIs, OperationInfo OI) {
		TypeKind.Regular_addOperationToOperationList(pContext, pEngine, OIs, OI);
	}
	/** Adds a lower level operation into the list of operation info */
	protected void addDataOperationToOperationList(Context pContext, Engine pEngine,
			Vector<OperationInfo> OIs, OperationInfo OI) {
		TypeKind.Regular_addOperationToOperationList(pContext, pEngine, OIs, OI);
	}
	
	// If there is a request for attribute or operation, prepare the type info.
	
	/** Prepare native methods so that native fields can be listed in the AttributeInfo array */
	final List<AttributeInfo> prepareNativeFields(Context pContext, Engine pEngine, boolean pIsStatic) {
		Vector<AttributeInfo> TAIs = new Vector<AttributeInfo>();
		Engine E = this.getEngine();
		if(E == null) E = pEngine;
		if(E == null) E = (pContext == null)?null:pContext.getEngine();

		// TODOLATER - Make it look more maintainable than this (now focus on accuracy)
		
		if(pIsStatic) {
			// Get both static and non-static element from type class
			{
				Field[] Fs = this.getClass().getFields();
				for(Field F : Fs) {
					if(!Modifier.isPublic(F.getModifiers())) continue;
					
					AttributeInfo AI = new AttributeInfo.AINative(E, F);
					AI.changeCurrentHolder(this);
					TAIs.add(AI);
				}
			}
					
			// Get static element from data class
			{
				Field[] Fs = this.getDataClass().getFields();
				for(Field F : Fs) {
					if(!Modifier.isPublic(F.getModifiers())) continue;
					if(!UClass.isMemberStatic(F))            continue;
					
					AttributeInfo AI = new AttributeInfo.AINative(E, F);
					AI.changeCurrentHolder(this);
					TAIs.add(AI);
				}
			}
		} else {	
			// Get non-static element from data class
			Field[] Fs = this.getDataClass().getFields();
			for(Field F : Fs) {
				if(!Modifier.isPublic(F.getModifiers())) continue;
				if(UClass.isMemberStatic(F))             continue;
					
				AttributeInfo AI = new AttributeInfo.AINative(E, F);
				AI.changeCurrentHolder(this);
				TAIs.add(AI);
			}
		}

		return TAIs;
	}
	/** Prepare native methods so that native methods can be listed in the OperationInfo array */
	final List<OperationInfo> prepareNativeMethods(Context pContext, Engine pEngine, boolean pIsStatic) {
		HashSet<Integer>      SOIs = new HashSet<Integer>();
		Vector<OperationInfo> LOIs = new Vector<OperationInfo>();

		// TODOLATER - Make it look more maintainable than this (no focus on accuracy)

		Class<?> TClass = this.getTypeClass();
		Class<?> DClass = this.getDataClass();
		
		if(pIsStatic) {
			// Get both static and non-static element from type class
			{	// For declare
				SOIs.clear();
				Method[] Ms = TClass.getDeclaredMethods();
				
				for(int i = Ms.length; --i >= 0;) {
					Method M = Ms[i];
					if(M == null)                            continue;
					if(!Modifier.isPublic(M.getModifiers())) continue;

					OperationInfo OI = new OperationInfo.OINative(pEngine, M);
					ExecSignature ES = OI.getSignature();
					// Calculate Hash of name and type
					int H = TypeKind.getLeanHashOfSignature(ES);

					// Found method with the same scope and the same name.
					if(SOIs.contains(H)) continue;

					// Add the method into the list.
					LOIs.add(OI);
					SOIs.add(H);
					if(((OperationInfo.OINative)OI).Owner == null) ((OperationInfo.OINative)OI).Owner = this;
				}
			}
			{	// For the rest (non-declare)
				SOIs.clear();
				Method[] Ms = TClass.getMethods();
				
				for(int i = Ms.length; --i >= 0;) {
					Method M = Ms[i];
					if(M == null)                            continue;
					if(M.getDeclaringClass() == DClass)      continue;
					if(!Modifier.isPublic(M.getModifiers())) continue;

					OperationInfo OI = new OperationInfo.OINative(pEngine, M);
					ExecSignature ES = OI.getSignature();
					// Calculate Hash of name and type
					int H = TypeKind.getLeanHashOfSignature(ES);

					// Found method with the same scope and the same name.
					if(SOIs.contains(H)) continue;

					// Add the method into the list.
					LOIs.add(OI);
					SOIs.add(H);
					if(((OperationInfo.OINative)OI).Owner == null) ((OperationInfo.OINative)OI).Owner = this;
				}
			}
			
			
			// Get static element from data class
			{	// For declare
				SOIs.clear();
				Method[] Ms = DClass.getDeclaredMethods();
				
				for(int i = Ms.length; --i >= 0;) {
					Method M = Ms[i];
					if(M == null)                            continue;
					if(!UClass.isMemberStatic(M))            continue;
					if(!Modifier.isPublic(M.getModifiers())) continue;

					OperationInfo OI = new OperationInfo.OINative(pEngine, M);
					ExecSignature ES = OI.getSignature();
					// Calculate Hash of name and type
					int H = TypeKind.getLeanHashOfSignature(ES);

					// Found method with the same scope and the same name.
					if(SOIs.contains(H)) continue;

					// Add the method into the list.
					LOIs.add(OI);
					SOIs.add(H);
					if(((OperationInfo.OINative)OI).Owner == null) ((OperationInfo.OINative)OI).Owner = this;
				}
			}
			{	// For the rest (non-declare)
				SOIs.clear();
				Method[] Ms = DClass.getMethods();
				
				for(int i = Ms.length; --i >= 0;) {
					Method M = Ms[i];
					if(M == null)                            continue;
					if(M.getDeclaringClass() == DClass)      continue;
					if(!UClass.isMemberStatic(M))            continue;
					if(!Modifier.isPublic(M.getModifiers())) continue;

					OperationInfo OI = new OperationInfo.OINative(pEngine, M);
					ExecSignature ES = OI.getSignature();
					// Calculate Hash of name and type
					int H = TypeKind.getLeanHashOfSignature(ES);

					// Found method with the same scope and the same name.
					if(SOIs.contains(H)) continue;

					// Add the method into the list.
					LOIs.add(OI);
					SOIs.add(H);
					if(((OperationInfo.OINative)OI).Owner == null) ((OperationInfo.OINative)OI).Owner = this;
				}
			}
		} else {
			// Get non-static element from data class
			{	// For declare
				SOIs.clear();
				Method[] Ms = this.getDataClass().getDeclaredMethods();
				
				for(int i = Ms.length; --i >= 0;) {
					Method M = Ms[i];
					if(M == null)                            continue;
					if(UClass.isMemberStatic(M))             continue;
					if(!Modifier.isPublic(M.getModifiers())) continue;

					OperationInfo OI = new OperationInfo.OINative(pEngine, M);
					ExecSignature ES = OI.getSignature();
					// Calculate Hash of name and type
					int H = TypeKind.getLeanHashOfSignature(ES);

					// Found method with the same scope and the same name.
					if(SOIs.contains(H)) continue;

					// Add the method into the list.
					LOIs.add(OI);
					SOIs.add(H);
					if(((OperationInfo.OINative)OI).Owner == null) ((OperationInfo.OINative)OI).Owner = this;
				}
			}
			{	// For the rest (non-declare)
				SOIs.clear();
				Method[] Ms = this.getDataClass().getMethods();
				
				for(int i = Ms.length; --i >= 0;) {
					Method M = Ms[i];
					if(M == null)                            continue;
					if(M.getDeclaringClass() == DClass)      continue;
					if(UClass.isMemberStatic(M))             continue;
					if(!Modifier.isPublic(M.getModifiers())) continue;

					OperationInfo OI = new OperationInfo.OINative(pEngine, M);
					ExecSignature ES = OI.getSignature();
					// Calculate Hash of name and type
					int H = TypeKind.getLeanHashOfSignature(ES);

					// Found method with the same scope and the same name.
					if(SOIs.contains(H)) continue;

					// Add the method into the list.
					LOIs.add(OI);
					SOIs.add(H);
					if(((OperationInfo.OINative)OI).Owner == null) ((OperationInfo.OINative)OI).Owner = this;
				}
			}
		}

		return LOIs;
	}

	/** Prepare Type Field Information */
	final void prepareTypeFields(Context pContext, Engine pEngine) {
		// Try the best to get the engine
		Engine E;
		if(((E = this.getEngine()) == null) &&
		   ((E = pEngine) == null)          &&		
		   ((pContext == null) || (E = pContext.getEngine()) == null))
		   E = Engine.An_Engine;
		
		if(this.AttrInfos != null) return;

		
		
		// Prepare the list of attribute info
		Vector<AttributeInfo> LAIs = new Vector<AttributeInfo>();
		
		// The first element to mark it is not yet collapse
		if(!(this instanceof TJava) || !(this instanceof TArray) || !(this instanceof TType) || !(this instanceof TPackage))
			LAIs.add(null);

		
		
		// Prepare the elements from spec
		List<AttributeInfo> TSpecLAIs = this.TKind.getTSpecTypeAttributeInfo(this.TSpec);
		if(TSpecLAIs != null) {
			int Count = TSpecLAIs.size();
			for(int i = 0; i < Count; i++) {
				AttributeInfo AI = TSpecLAIs.get(i);
				if(AI == null) continue;
				
				this.addTypeAttributeToAttributeList(pContext, pEngine, LAIs, AI);
			}
		}

		// Set the owner and increasing the DHIndex Max
		this.AttrInfos = new AttributeInfo[LAIs.size()];
		LAIs.toArray(this.AttrInfos);
		// Assign the owner and modified the base type (resolve first as the owner of attribute from Spec have no owner)
		this.modifyRespond(pContext, E, this.AttrInfos, true);
		
		
		
		// Prepare the elements from TypeKind
		this.TKind.doType_prepareTypeKindFields(   pContext, E, this, true, LAIs);
		// Prepare the elements from Native type
		this.TKind.doType_prepareNativeFields(     pContext, E, this, true, LAIs);
		// Prepare the elements from the low-priority elements
		this.TKind.doType_prepareLowPriorityFields(pContext, E, this, true, LAIs);
		
		
		
		// Set the owner and increasing the DHIndex Max
		this.AttrInfos = new AttributeInfo[LAIs.size()];
		LAIs.toArray(this.AttrInfos);
		
		// Post-process the operations
		this.AttrInfos = this.doAttributePostProcessing(pContext, this.AttrInfos, true);

		// Assign the owner and modified the base type
		this.modifyRespond(pContext, E, this.AttrInfos, true);
		
		// Validating the operations
		this.AttrInfos = this.validateAttributes(pContext, this.AttrInfos, true);
	}
	/** Prepare Object Field Information */
	final void prepareObjectFields(Context pContext, Engine pEngine) {
		// Try the best to get the engine
		Engine E;
		if(((E = this.getEngine()) == null) &&
		   ((E = pEngine) == null)          &&		
		   ((pContext == null) || (E = pContext.getEngine()) == null))
		   E = Engine.An_Engine;
		
		// Locked Type
		Class<?> Cls = this.getDataClass();
		if((Cls != Type.class) && E.getTypeManager().isSubClassOfLocked(Cls)) {
			this.ObjAttrInfos = new AttributeInfo[0];
			return;
		}

		if(this.ObjAttrInfos != null) return;

		
		
		// Prepare the list of attribute info
		Vector<AttributeInfo> LAIs = new Vector<AttributeInfo>();
		
		// The first element to mark it is not yet collapse
		if(!(this instanceof TJava) || !(this instanceof TArray) || !(this instanceof TType) || !(this instanceof TPackage))
			LAIs.add(null);

		
		
		// Prepare the elements from spec
		List<AttributeInfo> TSpecLAIs = this.TKind.getTSpecDataAttributeInfo(this.TSpec);
		if(TSpecLAIs != null) {
			int Count = TSpecLAIs.size();
			for(int i = 0; i < Count; i++) {
				AttributeInfo AI = TSpecLAIs.get(i);
				if(AI == null) continue;
				
				this.addDataAttributeToAttributeList(pContext, pEngine, LAIs, AI);
			}
		}

		// Set the owner and increasing the DHIndex Max
		this.ObjAttrInfos = new AttributeInfo[LAIs.size()];
		LAIs.toArray(this.ObjAttrInfos);
		// Assign the owner and modified the base type
		this.modifyRespond(pContext, E, this.ObjAttrInfos, false);
				
		
		
		// Prepare the elements from TypeKind
		this.TKind.doType_prepareTypeKindFields(   pContext, E, this, false, LAIs);
		// Prepare the elements from Native type
		this.TKind.doType_prepareNativeFields(     pContext, E, this, false, LAIs);
		// Prepare the elements from the low-priority elements
		this.TKind.doType_prepareLowPriorityFields(pContext, E, this, false, LAIs);
		
		
		
		// Set the owner and increasing the DHIndex Max
		this.ObjAttrInfos = new AttributeInfo[LAIs.size()];
		LAIs.toArray(this.ObjAttrInfos);
				
		// Post-process the operations
		this.ObjAttrInfos = this.doAttributePostProcessing(pContext, this.ObjAttrInfos, false);
		
		// Assign the owner and modified the base type
		this.modifyRespond(pContext, E, this.ObjAttrInfos, false);
		
		// Validating the operations
		this.ObjAttrInfos = this.validateAttributes(pContext, this.ObjAttrInfos, false);
	}

	/** Prepare Type Method Information */
	final void prepareTypeMethods(Context pContext, Engine pEngine) {
		// Try the best to get the engine
		Engine E;
		if(((E = this.getEngine()) == null) &&
		   ((E = pEngine) == null)          &&		
		   ((pContext == null) || (E = pContext.getEngine()) == null))
		   E = Engine.An_Engine;
		
		if(this.OperInfos != null) return;

		// Prepare the list of operation info
		Vector<OperationInfo> LOIs  = new Vector<OperationInfo>();
		
		// The first element to mark it is not yet collapse
		if(!(this instanceof TJava) || !(this instanceof TArray) || !(this instanceof TType) || !(this instanceof TPackage))
			LOIs.add(null);

		
		
		// Prepare the elements from spec
		List<OperationInfo> TSpecLOIs = this.TKind.getTSpecTypeOperationInfo(this.TSpec);
		if(TSpecLOIs != null) {
			int Count = TSpecLOIs.size();
			for(int i = 0; i < Count; i++) {
				OperationInfo OI = TSpecLOIs.get(i);
				if(OI == null) continue;
				
				this.addTypeOperationToOperationList(pContext, pEngine, LOIs, OI);
			}
		}

		// Set the owner
		this.OperInfos = new OperationInfo[LOIs.size()];
		LOIs.toArray(this.OperInfos);
		// Assign the owner and modified the base type
		this.modifyRespond(pContext, E, this.OperInfos, true);
		
		
		
		// Prepare the elements from TypeKind
		this.TKind.doType_prepareTypeKindMethods(   pContext, E, this, true, LOIs);
		// Prepare the elements from Native type
		this.TKind.doType_prepareNativeMethods(     pContext, E, this, true, LOIs);
		// Prepare the elements from the low-priority elements
		this.TKind.doType_prepareLowPriorityMethods(pContext, E, this, true, LOIs);

		// Set the owner
		this.OperInfos = new OperationInfo[LOIs.size()];
		LOIs.toArray(this.OperInfos);

		
		
		// Post-process the operations
		this.OperInfos = this.doOperationPostProcessing(pContext, this.OperInfos, true);

		// Assign the owner and modified the base type
		this.modifyRespond(pContext, E, this.OperInfos, true);
		
		// Validating the operations
		this.OperInfos = this.validateOperations(pContext, this.OperInfos, true);
	}
	/** Prepare Object Method Information */
	final void prepareObjectMethods(Context pContext, Engine pEngine) {
		// Try the best to get the engine
		Engine E;
		if(((E = this.getEngine()) == null) &&
		   ((E = pEngine) == null)          &&		
		   ((pContext == null) || (E = pContext.getEngine()) == null))
		   E = Engine.An_Engine;
		
		// Locked Type
		Class<?> Cls = this.getDataClass();
		if((Cls != Type.class) && E.getTypeManager().isSubClassOfLocked(Cls)) {
			this.ObjOperInfos = new OperationInfo[0];
			return;
		}

		if(this.ObjOperInfos != null) return;
		


		// Prepare the list of operation info
		Vector<OperationInfo> LOIs = new Vector<OperationInfo>();
		
		// The first element to mark it is not yet collapse
		if(!(this instanceof TJava) || !(this instanceof TArray) || !(this instanceof TType) || !(this instanceof TPackage))
			LOIs.add(null);

		
		
		// Prepare the elements from spec
		List<OperationInfo> TSpecLOIs = this.TKind.getTSpecDataOperationInfo(this.TSpec);
		if(TSpecLOIs != null) {
			int Count = TSpecLOIs.size();
			for(int i = 0; i < Count; i++) {
				OperationInfo OI = TSpecLOIs.get(i);
				if(OI == null) continue;
				
				this.addDataOperationToOperationList(pContext, pEngine, LOIs, OI);
			}
		}

		// Change to array
		this.ObjOperInfos = new OperationInfo[LOIs.size()];
		LOIs.toArray(this.ObjOperInfos);
		// Assign the owner and modified the base type
		this.modifyRespond(pContext, E, this.ObjOperInfos, false);
		
		
		
		// Prepare the elements from TypeKind
		this.TKind.doType_prepareTypeKindMethods(   pContext, E, this, false, LOIs);
		// Prepare the elements from Native type
		this.TKind.doType_prepareNativeMethods(     pContext, E, this, false, LOIs);
		// Prepare the elements from the low-priority elements
		this.TKind.doType_prepareLowPriorityMethods(pContext, E, this, false, LOIs);
		
		
		
		// Set the owner
		// Change to array
		this.ObjOperInfos = new OperationInfo[LOIs.size()];
		LOIs.toArray(this.ObjOperInfos);
		
		
		
		// Post-process the operations
		this.ObjOperInfos = this.doOperationPostProcessing(pContext, this.ObjOperInfos, false);
		
		// Assign the owner and modified the base type
		this.modifyRespond(pContext, E, this.ObjOperInfos, false);
		
		// Validating the operations
		this.ObjOperInfos = this.validateOperations(pContext, this.ObjOperInfos, false);
	}

	// BOT Resolution -----------------------------------------------------------------------------
	
	/**
	 * Changes the owner of the given operation to be this.
	 * This should only be used during the type initialization. It only works when the previous owner is null.
	 **/
	final protected void setOwnerToThisType(Respond Respond) {
		if(Respond instanceof AttributeInfo) {
			((AttributeInfo)Respond).changeCurrentHolder(this);
			
		} else if(Respond instanceof OperationInfo) {
			OperationInfo OI = (OperationInfo)Respond;
			if(     OI instanceof OperationInfo.SimpleOperation) ((OperationInfo.SimpleOperation)OI).changeCurrentHolder(this);
			else if(OI instanceof OperationInfo.OIDirect)        ((OperationInfo.OIDirect)       OI).changeCurrentHolder(this);
		}
	}

	/** Modifies the AttributeInfos */
	final void modifyRespond(Context pContext, Engine pEngine, Respond Respond, boolean pIsStatic) {
		if((Respond == null) || Respond.getRKind().isNative() || (!Respond.isAttributeInfo() && !Respond.isOperationInfo()))
			return;
		
		AttributeInfo AI = (Respond.isAttributeInfo())?Respond.asAttributeInfo():null;			
		
		// Set the owner
		this.setOwnerToThisType(Respond);
		
		// For Attribute only			
		// NOTE: Assign the DHIndex is what differs from the modified Respond for OperationInfo 
		if((AI != null) && AI.getRKind().isDirect()) {
			if(pIsStatic) { AI.asDirect().DHIndex = this.getMaxDHIndex();       this.incMaxDHIndex();       }
			else          { AI.asDirect().DHIndex = this.getMaxObjectDHIndex(); this.incMaxObjectDHIndex(); }
		}

		if     (AI != null)                                                 AI.resolve(pEngine);
		else if(Respond instanceof SimpleOperation) ((SimpleOperation)Respond).resolve(pEngine);
		else if(Respond instanceof OIDirect)        ((OIDirect)       Respond).resolve(pEngine); 
		
		// Static attribute cannot be BaseOnType.
		if((AI != null) && pIsStatic) {
			TypeRef ARef = AI.getDeclaredTypeRef();
			if(ARef == null) ARef = AI.getTypeRef();	// In the case of Delegate

			if(ARef.isDynamic())
				throw new CurryError("Static attribute cannot be dynamic (a kinf of BasedOnType) TypeRef ("+AI+"). <Type.modifyRespond:980>", pContext);
		}
	}
	
	/** Modifies the AttributeInfos */
	final void modifyRespond(Context pContext, Engine pEngine, AttributeInfo[] AIs, boolean pIsStatic) {
		if(AIs == null) return;
		for(int i = AIs.length; --i >= 0; ) {
			AttributeInfo AI = AIs[i];
			if(AI == null) continue;
			this.modifyRespond(pContext, pEngine, AIs[i], pIsStatic);
		}
	}	
	/** Modifies the OperationInfos */
	final void modifyRespond(Context pContext, Engine pEngine, OperationInfo[] OIs, boolean pIsStatic) {
		if(OIs == null) return;
		for(OperationInfo OI : OIs) {
			if(OI == null) continue;
			this.modifyRespond(pContext, pEngine, OI, pIsStatic);
		}
	}

	// PostProcess --------------------------------------------------------------------------------
	
	/** Ensure that non of the given attributes is abstract */
	final protected AttributeInfo[] ensureNoAbstractAttributes(Context pContext, AttributeInfo[] pAIs) {
		for(AttributeInfo AI : pAIs) {
			if(AI == null) continue;
			if((AI.getRKind().isNative() && AI.asNative().isAbstract()) ||
			   (AI.getRKind().isDirect() && AI.asDirect().isAbstract())) {
				throw new CurryError("Abstract attribute `"+AI+"` is not implemented in the type `"+this+"`.", pContext);
			}
		}
		return pAIs;
	}
	/** Ensure that non of the given operations is abstract */
	final protected OperationInfo[] ensureNoAbstractOperations(Context pContext, OperationInfo[] pOIs) {
		Vector<Integer>       OIHashes = new Vector<Integer>();
		Vector<OperationInfo> OIs      = new Vector<OperationInfo>();
		
		for(OperationInfo OI : pOIs) {
			if(OI == null) continue;

			int Hash = OI.getDeclaredSignature().hash_WithoutParamNamesReturnType();
			if(OIHashes.contains(Hash)) continue;
			
			OIHashes.add(Hash);
			OIs     .add(OI);
			
			if((OI.getRKind().isNative() && OI.asNative().isAbstract()) ||
			   (OI.getRKind().isDirect() && OI.asDirect().isAbstract())) {
				throw new CurryError("Abstract operation `"+OI+"` is not implemented in the type `"+this+"`.", pContext);
			}
		}
		return OIs.toArray(new OperationInfo[OIs.size()]);
	}
	
	/** Process the AttributeInfos */
	protected AttributeInfo[] doAttributePostProcessing(Context pContext, AttributeInfo[] pAIs, boolean pIsStatic) {
		return pAIs;
	}
	/** Process the OperationInfos */
	protected OperationInfo[] doOperationPostProcessing(Context pContext, OperationInfo[] pOIs, boolean pIsStatic) {
		return pOIs;
	}
	
	/** Process the AttributeInfos */
	protected AttributeInfo[] validateAttributes(Context pContext, AttributeInfo[] pAIs, boolean pIsStatic) {
		return pAIs;
	}
	/** Process the OperationInfos */
	protected OperationInfo[] validateOperations(Context pContext, OperationInfo[] pOIs, boolean pIsStatic) {
		return pOIs;
	}

	/** Collapse the AttributeInfos */
	protected AttributeInfo[] collapseAttributes(Context pContext, Engine pEngine, AttributeInfo[] pAIs, boolean pIsStatic) {	
		// Eliminate null value
		Vector<AttributeInfo> LAIs = new Vector<AttributeInfo>();
		for(int i = 0; i < pAIs.length; i++) {
			AttributeInfo AI = pAIs[i];
			if(AI == null) continue;
			
			LAIs.add(AI);
		}
		pAIs = LAIs.toArray(new AttributeInfo[LAIs.size()]);
		
		// No abstract element is allowed in non-abstract and non-variant type
		if(!pIsStatic && !this.isAbstract(pContext) && !(this instanceof TVariant))
			pAIs = this.ensureNoAbstractAttributes(pContext, pAIs);
		return pAIs;
	}
	/** Collapse the OperationInfos */
	protected OperationInfo[] collapseOperations(Context pContext, Engine pEngine, OperationInfo[] pOIs, boolean pIsStatic) {

		// If the type is not abstract, all non-static operation must not be abstract
		if(!pIsStatic && !this.isAbstract(pContext)) {
			Vector<OperationInfo> OIs_Info_Approved = new Vector<OperationInfo>();
			Vector<OperationInfo> OIs_Info_Abstract = new Vector<OperationInfo>();
			for(int i = 0; i < pOIs.length; i++) {
				OperationInfo OI = pOIs[i];
				if(OI == null) continue;
					
				// Only abstract is what we need to pay attention
				if((OI.getRKind().isDirect() && ((OIDirect)OI).isAbstract()) || (OI.getRKind().isNative() && ((OINative)OI).isAbstract()))
					 OIs_Info_Abstract.add(OI);
				else OIs_Info_Approved.add(OI);
			}
			
			if(OIs_Info_Abstract.size() != 0) {
				Engine $Engine = this.getEngine();
				if(($Engine == null) && (pContext != null)) {
					$Engine = pContext.getEngine();
					if($Engine == null) $Engine = pEngine;
				}
				
				OperationInfo[] Approveds = OIs_Info_Approved.toArray(new OperationInfo[OIs_Info_Approved.size()]);
				// All the abstract one must have an implementation
				for(int i = 0; i < OIs_Info_Abstract.size(); i++) {
					OperationInfo OI = OIs_Info_Abstract.get(i);
					int Score = ExecInterface.Util.searchExecutableBySignature($Engine, pContext, Approveds, OI.getDeclaredSignature(), false);
					if(Score != MType.NotMatch) continue;
					
					// The implementation is not found
					throw new CurryError(
						String.format(
							"An operation '%s' of the non-abstract type '%s' is abstract. <Type:1510>",
							OI.getSignature(), this
						),
						pContext
					);
				}
			}
		}
		
		// Eliminate null value
		Vector<OperationInfo> LOIs = new Vector<OperationInfo>();
		for(int i = 0; i < pOIs.length; i++) {
			OperationInfo OI = pOIs[i];
			if(OI == null) continue;
			
			LOIs.add(OI);
		}
		pOIs = LOIs.toArray(new OperationInfo[LOIs.size()]);
		
		// No abstract element is allowed in non-abstract and non-variant type
		if(!pIsStatic && !this.isAbstract(pContext) && !(this instanceof TVariant))
			pOIs = this.ensureNoAbstractOperations(pContext, pOIs);
		return pOIs;
	}
	
	// *********************************************************************************************
	// Type Behaviors ******************************************************************************
	// *********************************************************************************************

	// Customize of StackOwner Kind ----------------------------------------------------------------	

	/**{@inheritDoc}*/ @Override
	protected String getOperKindName() {
		return this.TKind.doType_getOperKindName(this);
	}

	/**{@inheritDoc}*/ @Override
	protected String getAttrKindName() {
		return this.TKind.doType_getAttrKindName(this);
	}

	/**{@inheritDoc}*/ @Override
	protected void validateAccessibility(Accessibility pAccess) {
		this.TKind.doType_validateAccessibility(this, pAccess);
	}
		
	// ---------------------------------------------------------------------------------------------
	// General A/O related -------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	// Data ----------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected net.nawaman.curry.util.DataHolder getDHAt(Context pContext, DataHolder.AccessKind DHAK,
			AttributeInfo.AIDirect AI) {
		Engine E = this.getEngine();
		if(E == null) E = pContext.getEngine();
		
		// DH of a type must only be accessed after the type is initialized.
		E.getTypeManager().ensureTypeInitialized(pContext, this);
		
		// Prepare the needed elements
		this.ensureObjectAttributesPrepared(pContext, E);
		
		// Returns the DataHolder
		return super.getDHAt(pContext, DHAK, AI);
	}
	
	// Not Null Attribute --------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected boolean isEnforceNotNull() {
		return this.TKind.doType_isEnforceNotNull(this);
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean toEnforceNotNull(Context pContext) {
		this.prepareTypeFields(pContext, pContext.getEngine());
		return super.toEnforceNotNull(pContext);
	}

	// Dynamic Handling ----------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isHandleDynamically() {
		return this.TKind.doType_isHandleDynamically(this);
	}
	
	// Dynamic Delegation --------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected int getDynamicDelegationCount(Context pContext) {
		return this.TKind.doType_getDynamicDelegationCount(pContext, this);
	}
	/**{@inheritDoc}*/ @Override
	protected String getDynamicDelegation(Context pContext, int I) {
		return this.TKind.doType_getDynamicDelegation(pContext, this, I);
	}
	/**{@inheritDoc}*/ @Override
	protected TypeRef getDynamicDelegationAsType(Context pContext, int I) {
		return this.TKind.doType_getDynamicDelegationAsType(pContext, this, I);
	}

	// ---------------------------------------------------------------------------------------------
	// Handle A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected boolean isElementAppendable() {
		return this.TKind.doType_isElementAppendable(this);
	}

	// Existing Check ----------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	final protected boolean isAttrExist(String pVName) {
		// Prepare the needed elements
		this.ensureAttributesPrepared(null, null);
		if(this.AttrInfos == null) return false;
		
		return super.isAttrExist(pVName);
	}

	/**{@inheritDoc}*/ @Override
	final protected boolean isOperExist(ExecSignature pES) {
		// Prepare the needed elements
		this.ensureOperationsPrepared(null, null);
		if(this.OperInfos == null) return false;
		
		return super.isOperExist(pES);
	}

	/** Checks if the type field exist. */
	final public boolean isTypeFieldExist(String pVName) {
		// Prepare the needed elements
		this.ensureAttributesPrepared(null, null);
		if(this.AttrInfos == null) return false;
		
		return super.isAttrExist(pVName);
	}
	/** Checks if the object method exist. */
	final public boolean isTypeMethodExist(ExecSignature pES) {
		// Prepare the needed elements
		this.ensureOperationsPrepared(null, null);
		if(this.OperInfos == null) return false;
		
		return super.isOperExist(pES);
	}

	/** Checks if the object field exist. */
	public boolean isOpjectFieldExist(String pVName) {
		if(pVName == null) return false;
		// Prepare the needed elements
		this.ensureObjectAttributesPrepared(null, null);
		if(this.ObjAttrInfos == null) return false;

		int hSearch = UString.hash(pVName);
		for(int i = this.ObjAttrInfos.length; --i >= 0; ) {
			AttributeInfo AI_InList = this.ObjAttrInfos[i];
			if(AI_InList == null) continue;
			
			if(hSearch != AI_InList.getNameHash()) continue;
			return true;
		}
		return false;
	}

	/** Checks if the object method exist. */
	public boolean isOpjectMethodExist(ExecSignature pES) {
		if(pES == null) return false;
		// Prepare the needed elements
		this.ensureObjectOperationsPrepared(null, null);
		if(this.ObjOperInfos == null) return false;

		int hSearch = pES.hashCode();
		for(int i = this.ObjOperInfos.length; --i >= 0; ) {
			OperationInfo OI_InList = this.ObjOperInfos[i];
			if(OI_InList == null) continue;
			
			if(hSearch != OI_InList.getSignatureHash()) continue;
			return true;
		}
		return false;
	}

	// Get Respond ---------------------------------------------------------------------------------
	
	// Get A/O Local ---------------------------------------------------------------------
	// NOTE: Low level
	// NOTE: pContext must not be null here
	
	/**{@inheritDoc}*/ @Override
	final protected OperationInfo getOperationLocal(Context pContext, Type pAsType, ExecSignature pSignature) {
		// Prepare the needed elements
		this.ensureOperationsPrepared(pContext, ((pAsType == null) ? null : pAsType.getEngine()));
		if(this.OperInfos == null) return null;
		
		return super.getOperationLocal(pContext, pAsType, pSignature);
	}
	/**{@inheritDoc}*/ @Override
	final protected AttributeInfo getAttributeLocal(Context pContext, DataHolder.AccessKind pDHAK, Type pAsType, String pName) {
		// Prepare the needed elements
		this.ensureAttributesPrepared(pContext, ((pAsType == null) ? null : pAsType.getEngine()));
		if(this.AttrInfos == null) return null;
		
		return super.getAttributeLocal(pContext, pDHAK, pAsType, pName);
	}
	
	/**{@inheritDoc}*/@Override
	final protected OperationInfo[] getAllNonDynamicOperationInfo(Type pAsType) {
		if(pAsType == this) pAsType = null;
		if(pAsType != null) return pAsType.getAllNonDynamicOperationInfo(null);
		
		// Prepare the needed elements
		this.ensureOperationsPrepared(null, null);
		if(this.OperInfos == null) return null;
		
		return this.OperInfos.clone();
	}
	/**{@inheritDoc}*/@Override
	final protected AttributeInfo[] getAllNonDynamicAttributeInfo(Type pAsType) {
		if(pAsType == this) pAsType = null;
		if(pAsType != null) return pAsType.getAllNonDynamicAttributeInfo(null);
		
		// Prepare the needed elements
		this.ensureAttributesPrepared(null, null);
		if(this.AttrInfos == null) return null;
		
		return this.AttrInfos.clone();
	}

	// Handle A/O ----------------------------------------------------------------------------------
	// NOTE: Middle level
	// NOTE: pContext must not be null here
	// Allow customizable to the high-level without having to override multiple method
	// Do appropriate search (for Operation)

	/** Execute an operation */ @Override
	protected Object invokeOperation(Context pContext, Expression pInitiator, boolean pIsBlindCaller,
								OperationSearchKind pOSKind, Type pAsType, Object pParam1, Object pParam2,
								Object[] pParams, boolean pIsAlreadyAdjusted) {
		if(pParam1 == null) throw new NullPointerException();
		// Process AsType
		if((pAsType != null) && (pAsType != this)) {// Forward to that type
			pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, pAsType);
			if(!pAsType.canBeAssignedByInstanceOf(pContext, this))
				this.throwInvalidOperationAsType(pContext, pAsType,
						this.getOperationAccessToString(pOSKind, pAsType, pParam1, pParam2));
			return pAsType.invokeOperation(pContext, pInitiator, pIsBlindCaller, pOSKind, pAsType,
					pParam1, pParam2, pParams, pIsAlreadyAdjusted);
		}

		return super.invokeOperation(pContext, pInitiator, pIsBlindCaller, pOSKind, pAsType, pParam1, pParam2, pParams,
				pIsAlreadyAdjusted);
	}
	/**{@inheritDoc}*/ @Override
	protected Object accessAttribute(Context pContext, Expression pInitiator, DataHolder.AccessKind pAKind,
								Type pAsType, String pAttrName, Object pParam1, Object pParam2, HashSet<Object> pObjects) {
		
		if(pAttrName == null) throw new NullPointerException();
		// Process AsType
		if((pAsType != null) && (pAsType != this)) {// Forward to that type
			pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, pAsType);
			if(!pAsType.canBeAssignedByInstanceOf(pContext, this))
				this.throwInvalidAttributeAsType(pContext, pAsType, DataHolder.AccessKind.Set, pAttrName);
			
			return pAsType.accessAttribute(pContext, pInitiator, pAKind, pAsType, pAttrName, pParam1, pParam2, pObjects);
		}
		
		return super.accessAttr(pContext, pInitiator, pAKind, pAsType, pAttrName, pParam1, pParam2, null);
	}

	/**{@inheritDoc}*/ @Override
	protected TypeRef searchAttributeLocal(Engine pEngine, Type pAsType, String pName) {
		// Prepare the needed elements
		this.ensureAttributesPrepared(null, pEngine);
		if(this.AttrInfos == null) return null;

		return super.searchAttributeLocal(pEngine, pAsType, pName);
	}


	/**
	 * Search operation of this StackOwner as the type using name and parameters. <br />
	 *    If pOSKind is ByParams and the pParam3 is Object[1][], the pParam3 is pAdjParams (the
	 *        adjusted values). The method should adjust the parameters and assign it as the first element
	 *        pAdjParams[0] or set it to null if the method does not support parameter adjustment.
	 **/ @Override
	 protected ExecSignature searchOperationLocal(Engine pEngine, OperationSearchKind pOSKind, Object pParam1,
										Object pParam2, Object pParam3) {
		// Prepare the needed elements
		this.ensureOperationsPrepared(null, pEngine);
		if(this.OperInfos == null) return null;

		return super.searchOperationLocal(pEngine, pOSKind, pParam1, pParam2, pParam3);
	}
	
	// Middle-Level search ---------------------------------------------------------------------------------------------

	// These are only use for information searching the above methods (the low-level is used for execution and access)

	/** Search attribute of this StackOwner as the type */
	protected TypeRef searchTypeAttributeLocal(Engine pEngine, String pName) {
		return this.searchAttributeLocal(pEngine, null, pName);
	}
	/** Do the search for Signature of an operation. */
	protected ExecSignature searchTypeOperationLocal(Engine pEngine, OperationSearchKind pOSKind, Object pParam1, Object pParam2,
			Object pParam3) {
		return this.searchOperationLocal(pEngine, pOSKind, pParam1, pParam2, pParam3);
	}
	
	// High-Level search to object element information ----------------------------------------------

	/** Search attribute of this StackOwner as the type */
	final protected TypeRef searchTypeAttribute(Engine pEngine, String pName) {
		return this.searchTypeAttributeLocal(pEngine, pName);
	}
	/** Search operation of this StackOwner as the type using name and parameters */
	final protected ExecSignature searchTypeOperation(Engine pEngine, String pOName, Object[] pParams) {
		return this.searchTypeOperationLocal(pEngine, OperationSearchKind.ByParams, pOName, pParams, null);
	}
	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final protected ExecSignature searchTypeOperation(Engine pEngine, String pOName, TypeRef[] pPTypeRefs) {
		return this.searchTypeOperationLocal(pEngine, OperationSearchKind.ByTRefs, pOName, pPTypeRefs, null);
	}
	/** Search operation of this StackOwner as the type using name and interface */
	final protected ExecSignature searchTypeOperation(Engine pEngine, String pOName, ExecInterface pExecInterface) {
		return this.searchTypeOperationLocal(pEngine, OperationSearchKind.ByNameInterface, pOName, pExecInterface, null);
	}
	/** Search operation of this StackOwner as the type using name and signature (if the signature are exact match, you can use it to execute) */
	final protected ExecSignature searchTypeOperation(Engine pEngine, ExecSignature pExecSignature) {
		return this.searchTypeOperationLocal(pEngine, OperationSearchKind.BySignature, pExecSignature, null, null);
	}
	
	// ---------------------------------------------------------------------------------------------
	// Utilities -----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	/** Creates a constructor */
	final protected ConstructorInfo newConstructorInfo(Accessibility pAccess, Executable.Macro pMacro, MoreData pMoreData) {
		return new ConstructorInfo.CIMacro(this.getEngine(), pAccess, this.getTypeRef(), pMacro, pMoreData);
	}

	// ---------------------------------------------------------------------------------------------
	// Handle Abnormality --------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Invalid AsType ----------------------------------------------------------

	/** Throw an InvalidOperationRespondObject error */
	final protected void throwInvalidOperationAsType(Context pContext, Type pAsType,
			ExecSignature pSignature) {
		this.throwOperation("Operation Invocation Error: The "
				+ this.getOperKindName() + " cannot be access via the type '"
				+ pAsType.toString() + "'", pContext, pAsType, pSignature);
	}
	/** Throw an InvalidOperationRespondObject error */
	final protected void throwInvalidOperationAsType(Context pContext, Type pAsType,
			String pSignatureStr) {
		this.throwOperation("Operation Invocation Error: The "
				+ this.getOperKindName() + " cannot be access via the type '"
				+ pAsType.toString() + "'", pContext, pAsType, pSignatureStr);
	}
	/** Throw an InvalidAttributeRespondObject error */
	final protected void throwInvalidAttributeAsType(Context pContext, Type pAsType,
			DataHolder.AccessKind pAKind, String pAttrName) {
		this.throwAttribute("Attribute Access Error: The " + this.getAttrKindName()
				+ " cannot be access via the type '" + pAsType.toString() + "'",
				pContext, pAKind, pAsType, pAttrName);
	}

	// *********************************************************************************************
	// Object Behaviors ***************************************************************************
	// *********************************************************************************************
	
	// ---------------------------------------------------------------------------------------------
	// General Behaviors ---------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Customize of StackOwner Kind ----------------------------------------------------------------	
	
	/** Returns the name of the operation kind */
	protected String doData_getOperKindName(DObject pTheObject) {
		return this.TKind.doData_getOperKindName(this, pTheObject);
	}
	/** Returns the name of the attribute kind */
	protected String doData_getAttrKindName(DObject pTheObject) {
		return this.TKind.doData_getAttrKindName(this, pTheObject);
	}

	// Accessibility -------------------------------------------------------------------------------
	
	/** Validate the given accessibility and throw an error if the validation fail. */
	protected void doData_validateAccessibility(DObject pTheObject, Accessibility pAccess) {
		this.TKind.doData_validateAccessibility(this, pTheObject, pAccess);
		return;
	}
	
	// ---------------------------------------------------------------------------------------------
	// General A/O related -------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
		
	// Not Null Attribute --------------------------------------------------------------------------
	
	/** Checks if the 'NotNull' features is now on */
	public boolean doData_isEnforceNotNull(DObject pTheObject) {
		return this.TKind.doData_isEnforceNotNull(this, pTheObject);
	}
	
	// Dynamic Handling ----------------------------------------------------------------------------
	
	/** Check if dynamic handling is allowed */
	protected boolean doData_isHandleDynamically(DObject pTheObject) {
		return this.TKind.doData_isHandleDynamically(this, pTheObject);
	}
	
	// Dynamic Delegation --------------------------------------------------------------------------

	/** Returns the number of Dynamic Delegation */
	protected int doData_getDynamicDelegationCount(DObject pTheObject, Context pContext) {
		return this.TKind.doData_getDynamicDelegationCount(this, pTheObject, pContext);
	}
	/** Returns the name of the Attr for the delegation */
	protected String doData_getDynamicDelegation(DObject pTheObject, Context pContext, int I) {
		return this.TKind.doData_getDynamicDelegation(this, pTheObject, pContext, I);
	}
	/** Returns the type that this StackOwner need to be seen as to get the Delegation Object */
	protected TypeRef doData_getDynamicDelegationAsType(DObject pTheObject, Context pContext, int I) {
		return this.TKind.doData_getDynamicDelegationAsType(this, pTheObject, pContext, I);
	}

	// ---------------------------------------------------------------------------------------------
	// Appended A/O --------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	// Attribute Info --------------------------------------------------------------------
	
	/** Returns the number of attribute info this StackOwner contains */
	protected int doData_getAttrInfoCount(DObject pTheObject) {
		this.ensureObjectAttributesPrepared(null, ((pTheObject == null) ? null : pTheObject.getEngine()));
		if(this.ObjAttrInfos == null) return 0;
		
		return this.ObjAttrInfos.length;
	}
	/** Returns the attribute info at the index */
	protected AttributeInfo doData_getAttrInfoAt(DObject pTheObject, int pIndex) {
		this.ensureObjectAttributesPrepared(null, ((pTheObject == null) ? null : pTheObject.getEngine()));
		if(this.ObjAttrInfos == null) return null;
		
		if((pIndex < 0) || (pIndex >= this.ObjAttrInfos.length)) return null;
		return this.ObjAttrInfos[pIndex];
	}
	/** Returns the maximum value of DataHolder index (or the maximum number of Direct AttrInfo) */
	protected int doData_getMaxDHIndex(DObject pTheObject) {
		return this.getMaxObjectDHIndex();
	}
	
	// ---------------------------------------------------------------------------------------------
	// Handle A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	// Get Respond ---------------------------------------------------------------------------------
	
	// Get A/O Local ---------------------------------------------------------------------
	// NOTE: Low level
	// NOTE: pContext must not be null here
	
	/** Get a respond to the requested operation associating with pExecSignature (pExecSignature must be exact match). */
	protected OperationInfo doData_getOperationLocal(DObject pTheObject, Context pContext, Type pAsType,
			ExecSignature pExecSignature) {
		
		if(pExecSignature == null) return null;
		if(pAsType == this) pAsType = null;
		// Do AsType by getting operation in that type
		if(pAsType != null) {
			if(pAsType.canBeAssignedBy(pContext, pTheObject))
				return pAsType.doData_getOperation(pTheObject, pContext, null, pAsType, pExecSignature);
			
			this.TKind.doType_throwInvalidOperationAsType(this, pContext, pAsType, pExecSignature.toString());
		}
		
		// Prepare the needed elements
		this.ensureObjectOperationsPrepared(pContext, ((pTheObject == null) ? null : pTheObject.getEngine()));
		if(this.ObjOperInfos == null) return null;
		
		OperationInfo OI = null;
		int hSearch = pExecSignature.hash_WithoutParamNamesReturnType();
		for(int i = this.ObjOperInfos.length; --i >= 0; ) {
			OperationInfo OI_InList = this.ObjOperInfos[i];
			if(OI_InList == null) continue;
			
			if(hSearch != OI_InList.getSignature().hash_WithoutParamNamesReturnType())
				continue;
			
			OI = OI_InList;
			break;
		}
		if(OI         == null) return null;	// Not Found
		if(pTheObject == null) {
			if(!this.isOperAllowed(pContext, OI))
				return OperationInfo.NoPermission;
		}
		else {
			if(!pTheObject.isOperAllowed(pContext, OI))
				return OperationInfo.NoPermission;
		}
		return OI;
	}

	// TODO - This is a Hack to allow Pattern to access to its port
	/** Get a respond to the attribute request that is associated with pName. **/
	final protected AttributeInfo doData_getAttributeLocal_RAW(DObject pTheObject, Context pContext,
			Type pAsType, String pName) {

		// NOTE: Search Locally for the attribute with the same name and the owner is 'AsType'
		// NOTE: If the given AsType is null, any attribute with the same name is accepted.
		
		// TODO - Make sure Abstract Attribute Handling is correct
		
		if(pName == null) return null;
		
		// Prepare the needed elements
		this.ensureObjectAttributesPrepared(pContext, ((pTheObject == null) ? null : pTheObject.getEngine()));
		if(this.ObjAttrInfos == null) return null;
		
		if(pAsType == this) pAsType = null;
		
		Engine $Engine = this.getEngine();
		if($Engine == null) $Engine = pContext.getEngine();
		MType MT = $Engine.getTypeManager();
		
		AttributeInfo AI            = null;
		int           AbstractIndex = -1;
		int hSearch = UString.hash(pName);
		for(int i = 0; i < this.ObjAttrInfos.length; i++) {
			if(hSearch != this.ObjAttrInfos[i].getNameHash()) continue;
			AttributeInfo AnAI = this.ObjAttrInfos[i];
			if(AnAI == null) continue;
			
			Type ANAIOwnerType = AnAI.getOwnerAsType();
			boolean HasAsType   = (pAsType != null);
			boolean IsTypeMatch = !HasAsType || pAsType.equals(ANAIOwnerType);
			if(!IsTypeMatch && HasAsType && (ANAIOwnerType.getTypeRef() instanceof TRParametered)) {
				// Get the non-parametered target
				while(ANAIOwnerType.getTypeRef() instanceof TRParametered) {
					TypeRef TargetRef = ((TRParametered)ANAIOwnerType.getTypeRef()).getTargetTypeRef();
					if(TargetRef == null) break;
					Type Target = MT.getTypeFromRefNoCheck(pContext, TargetRef);
					if(Target == null) break;
					ANAIOwnerType = Target;
				}
				
				Type AsType = pAsType;
				if(pAsType.getTypeRef() instanceof TRParametered) {
					// Both are Parametered
					while(AsType.getTypeRef() instanceof TRParametered) {
						TypeRef TargetRef = ((TRParametered)AsType.getTypeRef()).getTargetTypeRef();
						if(TargetRef == null) break;
						Type Target = MT.getTypeFromRefNoCheck(pContext, TargetRef);
						if(Target == null) break;
						AsType = Target;
					}
				}
				IsTypeMatch = AsType.equals(ANAIOwnerType);
			}
			
			// No AsType
			if((pAsType == null) ||
			  ( // Or exact as type and not abstract
				IsTypeMatch &&
				(!(AnAI instanceof AttributeInfo.AIDirect) || !((AttributeInfo.AIDirect)AnAI).isAbstract())
			  )) {
				// When No AsType the first found is chosen
				AI            = AnAI;
				AbstractIndex =   -1;
				break;
			}
			
			if(pAsType.canBeAssignedByInstanceOf((Type)AnAI.getOwner())) {
				// When AsType is not null, The one that is own by the closest above type is chosen
				// Unless it is an abstract attribute
				
				if((AnAI instanceof AttributeInfo.AIDirect) && ((AttributeInfo.AIDirect)AnAI).isAbstract()) {
					AbstractIndex = i;
					continue;
				}
				
				// Found the one matching the criteria
				AI            = AnAI;
				AbstractIndex =   -1;
				break; 
			}
		}
		
		// If the one found is the earliest abstract, get the closest one below
		if(AbstractIndex != -1) {
			for(int i = (AbstractIndex + 1); i < this.ObjAttrInfos.length; i++) {
				if(hSearch != this.ObjAttrInfos[i].getNameHash()) continue;
				AttributeInfo AnAI = this.ObjAttrInfos[i];
				if(AnAI == null) continue;
				
				if(!(AnAI instanceof AttributeInfo.AIDirect) || !((AttributeInfo.AIDirect)AnAI).isAbstract()) {
					// Found the closest from below
					AI = AnAI;
					break;
				}
			}
		}
		return AI;
	}
	/** Get a respond to the attribute request that is associated with pName. **/
	final protected AttributeInfo doData_getAttributeLocal(DObject pTheObject, Context pContext,
			DataHolder.AccessKind pDHAK, Type pAsType, String pName) {
		AttributeInfo AI = this.doData_getAttributeLocal_RAW(pTheObject, pContext, pAsType, pName);
		if(AI         == null) return null;	// Not Found
		if(pTheObject == null) { if(!this.      isAttrAllowed(pContext, AI, pDHAK)) return AttributeInfo.NoPermission; }
		else                   { if(!pTheObject.isAttrAllowed(pContext, AI, pDHAK)) return AttributeInfo.NoPermission; }
		return AI;
	}
	
	protected OperationInfo[] doData_getAllNonDynamicOperationInfo(DObject pTheObject, Type pAsType) {
		if(pAsType == this) pAsType = null;
		// Do AsType by getting operation in that type
		if(pAsType != null) {
			if(pAsType.canBeAssignedBy(null, pTheObject))
				return pAsType.doData_getAllNonDynamicOperationInfo(pTheObject, pAsType);
			return null;
		}
		
		// Prepare the needed elements
		this.ensureObjectOperationsPrepared(null, ((pTheObject == null) ? null : pTheObject.getEngine()));
		if(this.ObjOperInfos == null) return null;
		
		return this.ObjOperInfos;
	}
	protected AttributeInfo[] doData_getAllNonDynamicAttributeInfo(DObject pTheObject, Type pAsType) {
		if(pAsType == this) pAsType = null;
		// Do AsType by getting operation in that type
		if(pAsType != null) {
			if(pAsType.canBeAssignedBy(null, pTheObject))
				return pAsType.doData_getAllNonDynamicAttributeInfo(pTheObject, pAsType);
			return null;
		}
		
		// Prepare the needed elements
		this.ensureObjectAttributesPrepared(null, ((pTheObject == null) ? null : pTheObject.getEngine()));
		if(this.ObjAttrInfos == null) return null;
		
		return this.ObjAttrInfos;
	}
	
	/** Returns an array of all the non-dynamic operation info */
	final protected OperationInfo[] getObjectOperationInfos() {
		return this.doData_getAllNonDynamicOperationInfo(null, null);
	}
	/** Returns an array of all the non-dynamic attribute info */
	final protected AttributeInfo[] getObjectAttributeInfos() {
		return this.doData_getAllNonDynamicAttributeInfo(null, null);
	}
	
	// Middle Level ----------------------------------------------------------------------
	// This is to include the effect of Dynamic Delegation into the getOperaion so that getthing
	//     the operation (non-local) of an object from its type can be done.

	/** Get a respond to the operation request that is associated with pExecSignature (pExecSignature must be exact match). */
	final protected OperationInfo doData_getOperation(DObject pTheObject, Context pContext,
			Expression pInitiator, Type pAsType, ExecSignature pSignature) {
		if((pTheObject != null) && (pAsType == null))
			return pTheObject.getOperation(pContext, pInitiator, pAsType, pSignature);
		
		if(pAsType == this) pAsType = null;
		return this.doData_getOperationLocal(null, pContext, pAsType, pSignature);
	}
	/**
	 * Get a respond to the operation request that is associated with pExecSignature (pExecSignature
	 *    must be exact match).
	 **/
	final protected AttributeInfo doData_getAttribute(DObject pTheObject, Context pContext, 
			Expression pInitiator, DataHolder.AccessKind pDHAK, Type pAsType, String pName) {
		if(pAsType == this) pAsType = null;
		if((pTheObject != null) && (pAsType == null))
			return pTheObject.getAttribute(pContext, pInitiator, pDHAK, pAsType, pName);
		return this.doData_getAttributeLocal(null, pContext, pDHAK, pAsType, pName);
	}
	
	// ---------------------------------------------------------------------------------------------
	// Search A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Search A/O Local ------------------------------------------------------------------
	// NOTE: Low level
	// NOTE: pEngine must not be null here

	/** Search attribute of this StackOwner as the type */
	protected TypeRef doData_searchAttributeLocal(Object pTheObject, Engine pEngine, Type pAsType,
			String pName) {
		if(pName == null) return null;
		
		// Prepare the needed elements
		this.ensureObjectAttributesPrepared(null, (!(pTheObject instanceof DObject) ? null : ((DObject)pTheObject).getEngine()));
		if(this.ObjAttrInfos == null) return null;
		
		if(pAsType == this) pAsType = null;
		
		AttributeInfo AI = null;
		int hSearch = UString.hash(pName);
		for(int i = 0; i < this.ObjAttrInfos.length; i++) {
			AttributeInfo AI_InList = this.ObjAttrInfos[i];
			if(AI_InList == null) continue;
			
			if(hSearch != AI_InList.getNameHash()) continue;
			AI = AI_InList;
			
			// No AsType
			if((pAsType != null) && (pAsType.equals(AI.getOwner()))) return null;
			
			return AI.getTypeRef();
		}
		return null;
	}
	/**
	 * Search operation of this StackOwner as the type using name and parameters. <br />
	 *    If pOSKind is ByParams and the pParam3 is Object[1][], the pParam3 is pAdjParams (the
	 *        adjusted values). The method should adjust the parameters and assign it as the first element
	 *        pAdjParams[0] or set it to null if the method does not support parameter adjustment.
	 **/
	protected ExecSignature doData_searchOperationLocal(Object pTheObject, Engine pEngine, OperationSearchKind pOSKind,
			Object pParam1, Object pParam2, Object pParam3) {
		
		if(pParam1 == null) return null;
		if(pOSKind == null) throw new NullPointerException();
		
		// Prepare the needed elements
		this.ensureObjectOperationsPrepared(null, (!(pTheObject instanceof DObject) ? null : ((DObject)pTheObject).getEngine()));
		if(this.ObjOperInfos == null) return null;

		int Index = -1;
		switch(pOSKind) {
			case Direct: return (ExecSignature)pParam1;
			case BySignature:     { Index = ExecInterface.Util.searchExecutableBySignature(pEngine, null, (HasSignature[])this.ObjOperInfos, (ExecSignature)pParam1, false);                                       break; }
			case ByParams:        { Index = ExecInterface.Util.searchExecutableByParams(   pEngine, null, (HasSignature[])this.ObjOperInfos, (String)pParam1, (Object[])     pParam2, (Object[][])pParam3, false); break; }
			case ByTRefs:         { Index = ExecInterface.Util.searchExecutableByTRefs(    pEngine, null, (HasSignature[])this.ObjOperInfos, (String)pParam1, (TypeRef[])    pParam2, false);                      break; }
			case ByNameInterface: { Index = ExecInterface.Util.searchExecutableByInterface(pEngine, null, (HasSignature[])this.ObjOperInfos, (String)pParam1, (ExecInterface)pParam2, false);                      break; }
		}
		if(Index == ExecInterface.NotMatch) return null;
		return this.ObjOperInfos[Index].getSignature();
	}
	
	// Middle-Level search ---------------------------------------------------------------------------------------------

	// These are only use for information searching the above methods (the low-level is used for execution and access)
	
	/** Do the search for Signature of an operation. */
	protected ExecSignature searchObjectOperation(Object pTheObject, Engine pEngine,
			OperationSearchKind pOSKind, boolean pIsSearchInDynamicDelegation, Object pParam1,
			Object pParam2, Object pParam3) {
		
		// If the object is given , find in the object
		if(pTheObject instanceof DObject)
			return ((DObject)pTheObject).searchOperation(pEngine, pOSKind, pIsSearchInDynamicDelegation, pParam1, pParam2, pParam3);
		
		return this.doData_searchOperationLocal(pTheObject, pEngine, pOSKind, pParam1, pParam2, pParam3);
	}

	/** Search attribute of this StackOwner as the type */
	protected TypeRef searchObjectAttribute(Object pTheObject, Engine pEngine, boolean pIsSearchInDynamicDelegation,
			Type pAsType, String pName) {
		
		// If the object is given , find in the object
		if(pTheObject instanceof DObject)
			return ((DObject)pTheObject).searchAttribute(pEngine, pIsSearchInDynamicDelegation, pAsType, pName);
		
		return this.doData_searchAttributeLocal(pTheObject, pEngine, pAsType, pName);
	}
	
	// High-Level search to object element information ----------------------------------------------

	/** Search attribute of this StackOwner as the type */
	final protected TypeRef searchObjectAttribute(Engine pEngine, String pName) {
		return this.searchObjectAttribute(null, pEngine, false, null, pName);
	}
	
	/** Search operation of this StackOwner as the type using name and parameters */
	final protected ExecSignature searchObjectOperation(Engine pEngine, 
			String pOName, Object[] pParams, Object[][] pAdjParams) {
		return this.searchObjectOperation(null, pEngine, OperationSearchKind.ByParams, false,
				pOName, pParams, pAdjParams);
	}

	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final protected ExecSignature searchObjectOperation(Engine pEngine, String pOName, TypeRef[] pPTypeRefs) {
		return this.searchObjectOperation(null, pEngine, OperationSearchKind.ByTRefs, false, pOName, pPTypeRefs, null);
	}

	/** Search operation of this StackOwner as the type using name and interface */
	final protected ExecSignature searchObjectOperation(Engine pEngine, String pOName, ExecInterface pExecInterface) {
		return this.searchObjectOperation(null, pEngine, OperationSearchKind.ByNameInterface,
				false, pOName, pExecInterface, null);
	}

	/**
	 * Search operation of this StackOwner as the type using name and signature (if the signature
	 * are exact match, you can use it to execute)
	 */
	final protected ExecSignature searchObjectOperation(Engine pEngine, ExecSignature pExecSignature) {
		return this.searchObjectOperation(null, pEngine, OperationSearchKind.BySignature, false,
				pExecSignature, null, null);
	}
	
	// Internal utilities (for this SO to use) -----------------------------------------------------

	// Report Error ----------------------------------------------------------------------

	/** Throw an error message that involve operation */
	final protected void doData_throwOperation(DObject pTheObject, String pErrMsg, Context pContext,
			Type pAsType, ExecSignature pSignature) {
		pTheObject.throwOperation(pErrMsg, pContext, pAsType, pSignature);
	}

	/** Throw an error message that involve operation */
	final protected void doData_throwOperation(DObject pTheObject, String pErrMsg, Context pContext, Type pAsType,
			String pSignatureStr) {
		pTheObject.throwOperation(pErrMsg, pContext, pAsType, pSignatureStr);
	}

	/** Throw an error message that involve attribute */
	final protected void doData_throwAttribute(String pErrMsg, Context pContext, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName, Object pParam1, Object pParam2) {
		throw new CurryError(pErrMsg + " ("
				+ this.getAttributeAccessToString(pAKind, pAsType, pAttrName, pParam1, pParam2)
				+ ").", pContext);
	}

	/** Throw an error message that involve attribute */
	final protected void doData_throwAttribute(String pErrMsg, Context pContext, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName) {
		throw new CurryError(pErrMsg + " ("
				+ this.getAttributeAccessToString(pAKind, pAsType, pAttrName) + ").", pContext);
	}
	
	// ---------------------------------------------------------------------------------------------
	// Utilities -----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// ActionRecord ----------------------------------------------------------------------

	/** Create an action record. */
	protected ActionRecord doData_newActionRecord(DObject pTheObject, Context pContext) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.newActionRecord(pContext);
	}
	
	// Access to Data ------------------------------------------------------------------------------
	
	/** Returns the data-holder by the index */
	final protected DataHolder doData_getDHByIndex(DObject pTheObject, Context pContext, DataHolder.AccessKind DHAK,
			int Index) {
		if(!this.canBeAssignedBy(pContext, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.getDHByIndex(pContext, DHAK, Index);
	}
	
	// Duplicate and Creating responds -------------------------------------------------------------
	
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected AttributeInfo doData_borrowAttributeInfo(DObject pTheObject, AttributeInfo pAI) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.borrowAttributeInfo(pAI);
	}
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected OperationInfo doData_borrowOperationInfo(DObject pTheObject, OperationInfo pOI) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.borrowOperationInfo(pOI);
	}
	
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected AttributeInfo doData_cloneAttributeInfo(DObject pTheObject, AttributeInfo pAI) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.cloneAttributeInfo(pAI);
	}
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected OperationInfo doData_cloneOperationInfo(DObject pTheObject, OperationInfo pOI) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.cloneOperationInfo(pOI);
	}
	
	// Dynamic --------------------------------------------------------
	/** Creates a new operation info */
	final protected OperationInfo doData_newOIDynamic(DObject pTheObject, Accessibility pAccess, ExecSignature pES,
			MoreData pMoreData) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.newOIDynamic(pAccess, pES, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AttributeInfo doData_newAIDynamic(DObject pTheObject, Accessibility pARead,
			Accessibility pAWrite, Accessibility pAConfig, String pVName, TypeRef pTRef, MoreData pMoreData) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.newAIDynamic(pARead, pAWrite, pAConfig, pVName, pTRef, pMoreData);
	}

	// Field --------------------------------------------------------
	/** Creates a new operation info */
	final protected OperationInfo doData_newOIDlgAttr(DObject pTheObject, Accessibility pAccess, ExecSignature pES,
			String pDlgAttr, MoreData pMoreData) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.newOIDlgAttr(pAccess, pES, pDlgAttr, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AttributeInfo doData_newAIDlgAttr(DObject pTheObject, Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, String pDlgAttr, MoreData pMoreData) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.newAIDlgAttr(pARead, pAWrite, pAConfig, pVName, pDlgAttr, pMoreData);
	}
	
	// Object --------------------------------------------------------
	/** Creates a new operation info */
	final protected OperationInfo doData_newOIDlgObject(DObject pTheObject, Accessibility pAccess, ExecSignature pES,
			Object pDlgObject, MoreData pMoreData) {
		this.validateAccessibility(pAccess);
		OIDlgObject OID = new OIDlgObject(pAccess, pES, pDlgObject, pMoreData);
		OID.changeDeclaredOwner(this);
		return OID;
	}
	/** Creates a new attribute info */
	final protected AttributeInfo doData_newAIDlgObject(DObject pTheObject, Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, boolean pIsNotNull, Object pDlgObject, MoreData pMoreData) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.newAIDlgObject(pARead, pAWrite, pAConfig, pVName, pIsNotNull, pDlgObject, pMoreData);
	}

	// Direct --------------------------------------------------------
	/** Creates a new operation info */
	final protected OperationInfo doData_newOIDirect(DObject pTheObject, Accessibility pAccess, Executable pExec,
			MoreData pMoreData) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.newOIDirect(pAccess, pExec, pMoreData);
	}
	/** Creates a new abstract operation info */
	final protected OperationInfo doData_newOIDirect(DObject pTheObject, Engine pEngine, Accessibility pAccess,
			ExecSignature pSignature, Executable.ExecKind pKind, MoreData pMoreData) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.newOIDirect(pAccess, pSignature, pKind, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AttributeInfo doData_newAIDirect(DObject pTheObject, Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation,
			MoreData pMoreData) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.newAIDirect(pARead, pAWrite, pAConfig, pVName, pIsNotNull, pDHI, pLocation, pMoreData);
	}

	// Display information ---------------------------------------------------------------

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final protected String doData_getAttributeAccessToString(DObject pTheObject, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName, Object pParam1, Object pParam2) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.getAttributeAccessToString(pAKind, pAsType, pAttrName);
	}

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final protected String doData_getAttributeAccessToString(DObject pTheObject, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.getAttributeAccessToString(pAKind, pAsType, pAttrName);
	}

	/** Display the operation access as a string */
	final protected String doData_getOperationAccessToString(DObject pTheObject,
			OperationSearchKind pOSKind, Type pAsType, Object pParam1, Object pParam2) {
		if(!this.canBeAssignedBy(null, pTheObject)) throw new IllegalArgumentException();
		return pTheObject.getOperationAccessToString(pOSKind, pAsType, pParam1, pParam2);
	}
}