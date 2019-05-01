package net.nawaman.curry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import java.util.Vector;

import net.nawaman.curry.AttributeInfo.AIDirect;
import net.nawaman.curry.AttributeInfo.AIDlgAttr;
import net.nawaman.curry.AttributeInfo.AIDlgObject;
import net.nawaman.curry.AttributeInfo.AIDynamic;
import net.nawaman.curry.AttributeInfo.AINative;
import net.nawaman.curry.ConstructorInfo.CIMacro;
import net.nawaman.curry.OperationInfo.OIDirect;
import net.nawaman.curry.OperationInfo.OIDlgAttr;
import net.nawaman.curry.OperationInfo.OIDlgObject;
import net.nawaman.curry.OperationInfo.OIDynamic;
import net.nawaman.curry.StackOwner.OperationSearchKind;
import net.nawaman.curry.TKJava.TSJava;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.DataArray;
import net.nawaman.util.DataArray_Proxy;
import net.nawaman.util.Objectable;
import net.nawaman.util.UArray;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

/** Kind of Data Type */
abstract public class TypeKind implements Objectable {

	/** Constructs a new TypeKind. */
	protected TypeKind(Engine pEngine) {
		this.Engine = pEngine;
	}

	/** The engine that will recognize this kind. */
	final Engine Engine;

	/** Returns the engine that will recognize this type kind */
	final public Engine getEngine() {
		return this.Engine;
	}

	// Classification -------------------------------------------------------------------

	/** Returns the name of the kind of type */
	abstract public String getKindName();

	/** Checks if the type pTheType is final. */
	abstract protected boolean isTypeDerivable(Context pContext, Type pTheType);
	
	// Type Construction -----------------------------------------------------------------

	/** Returns type associated with Spec or create one if not exist. In case of error, return an the exception. */
	abstract protected Type getType(Engine pEngine, Context pContext, TypeSpec pSpec);
	
	/** Creates and returns a new type builder */
	final protected TypeBuilder newTypeBuilder(TypeSpec pTS, Accessibility pPAccess, Location pLocation,
			StackOwnerBuilderEncloseObject pEncloseObject) {
		
		if(pEncloseObject == null)
			throw new IllegalArgumentException("A type built from a type builder must be enclosed by an enclose object");
		
		if(!this.getKindName().equals(pTS.getKindName()))
			throw new IllegalArgumentException("The given type spec is not of the type kind '" + this.getKindName() +
					"' (It is of the '"+pTS.getKindName()+"').");
		
		// Create a new type builder from the information
		return this.createNewTypeBuilder(pTS, pPAccess, pLocation, pEncloseObject);
	}

	/** Creates and returns a new type builder */
	protected TypeBuilder createNewTypeBuilder(TypeSpec pTS, Accessibility pPAccess, Location pLocation,
			StackOwnerBuilderEncloseObject pEncloseObject) {
		// Create a new type builder from the information
		return new TypeBuilder(pTS, pPAccess, pLocation, pEncloseObject);
	}
	
	// Validation -----------------------------------------------------------------------

	// Validate the type
	/** Validates the type spec (alter loaded but just before marked as resolved - all required types are confirmed to exists). */
	protected Exception doValidateTypeSpec(Context pContext, TypeSpec pSpec) {
		return null;
	}

	// Validate the type 
	/** Validates the type (after resolved but just before validate - all required types are resolved). */
	protected Exception doValidateType(Context pContext, Type pType) {
		return null;
	}
	
	// Type Initialization ---------------------------------------------------------------

	// Initialize
	/**
	 * Initialize the type by creating necessary parts of the type.<br/> At this point the type is resolved and has been
	 * validated.
	 */
	protected Exception initializeType(Context pContext, Type pType) {
		return null;
	}

	/**
	 * Returns necessary expressions for initialization. <br />
	 * These expression allow separation of work in Java and Curry Scope.
	 */
	protected Expression[] getTypeInitializeExpressions(Context pContext, Type pType) {
		return null;
	}

	// PostInitialize
	/**
	 * Post-Initialization Validates the type<br/> At this point the type is loaded, resolved and initialized. All of
	 * its used types are resolved. All of its required types are also initialized. This method may check the type spec
	 * in the things that required the type to be initialized first. If the validation fail, The type and all that are
	 * initialized at the same time will be set to just resolved.
	 */
	protected Exception doValidateTypePostInitialization(Context pContext, Type pType) {
		return null;
	}

	/** Add interface into the list */
	static void AddInterface(Engine E, HashSet<TypeRef> Set, Type T) {
		if((T == null) || Set.contains(T.getTypeRef())) return;
		if(!TKInterface.isTypeInterface(T)) return;
		
		MType    MT = E.getTypeManager();
		TypeSpec TS = T.getTypeSpec();
		
		Set.add(T.getTypeRef());
		int ICount = TS.getInterfaceCount();
		for(int i = ICount; --i >= 0; ) {
			TypeRef TR = TS.getInterfaceRefAt(i);
			Type    TT = MT.getTypeFromRefNoCheck(null, TR);
			if(TT == null) continue;
			AddInterface(E, Set, TT);
		}
	}
	
	// Ensure that all the interface supoosibly implemented by the given type are properly implemented
	/** Process Interface implementation */
	final Exception doValidateTypeInterfaceImplementation(Context pContext, Type pType) {		
		if(pType == null) return new NullPointerException();
		
		Engine $Engine = this.getEngine();
		if($Engine == null) $Engine = pContext.getEngine();
		
		MType    MT   = $Engine.getTypeManager();
		TypeSpec Spec = pType.getTypeSpec();
		
		// No need to check the java type
		if(Spec instanceof TSJava) {
			// Except that it is an interface
			Class<?> ClsInterface = pType.getDataClass();
			if(ClsInterface.isInterface()) {
				Class<?>[] Interfaces =  ClsInterface.getInterfaces();
				if((Interfaces != null) && (Interfaces.length != 0)) {
					if(Spec.CBATo_TRUE == null) Spec.CBATo_TRUE = new HashSet<TypeRef>();
					for(Class<?> InterfaceCls : Interfaces) {
						Type Interface = MT.getTypeOfTheInstanceOf(pContext, InterfaceCls);
						Spec.CBATo_TRUE.add(Interface.getTypeRef());
					}
				}
			}
			return null;
		}
		
		// Prepare Super ------------------------------------------------------
		Type     Super    = null;
		TypeRef  SuperTR  = pType.getSuperRef();
		boolean  HasSuper = (SuperTR != null) && !TKJava.TAny.getTypeRef().equals(SuperTR);
		if(HasSuper) {
			Super = MT.getTypeFromRefNoCheck(pContext, SuperTR);
			if(TKJava.TAny.equals(Super.getTypeRef())) {
				Super    = null;
				SuperTR  = null;
				HasSuper = false;
			}
		}
		
		HashSet<TypeRef> Interfaces_ToCheck = new HashSet<TypeRef>();

		if(HasSuper) {
			TypeSpec Super_Spec = Super.getTypeSpec();

			if(Super_Spec.CBATo_TRUE != null) {
				// If super is not abstract, all types it can be assigned into will be included in this type
				if(!Super.isAbstract(pContext)) {
					if(Spec.CBATo_TRUE == null) Spec.CBATo_TRUE = new HashSet<TypeRef>();
					Spec.CBATo_TRUE.addAll(Super_Spec.CBATo_TRUE);
					
				// Otherwise, check the interface of those form super
				} else {
					// Extract interface from all the CBATo of the super
					for(TypeRef TRef : Super_Spec.CBATo_TRUE) {
						if(!TKInterface.isTypeInterface(MT.getTypeFromRefNoCheck(pContext, TRef))) continue;
						
						// Ensure this type implement the interface
						Interfaces_ToCheck.add(TRef);
					}
				}
			}
		}
		
		// Discover all interface from the data class and the interface if this type --------------
		
		// Add interfaces from Data class
		Class<?>[] IClss = pType.getDataClass().getInterfaces();
		if((IClss != null) && (IClss.length != 0)) {
			for(Class<?> ICls : IClss)
				AddInterface($Engine, Interfaces_ToCheck, MT.getTypeOfTheInstanceOfNoCheck(pContext, ICls));
		}
		
		// Add interface from what declared in TypeSpec
		int ICount = Spec.getInterfaceCount();
		for(int i = ICount; --i >= 0; ) {
			TypeRef TR = pType.getTypeSpec().getInterfaceRefAt(i);
			if(TR == null) continue;
			AddInterface($Engine, Interfaces_ToCheck, this.getTypeFromRef(pContext, TR));
		}
		
		// Process the collected interface
		if(Interfaces_ToCheck.size() != 0) {
			// If this is not abstract, check to ensure all interfaces are properly implemented.
			if(!pType.isAbstract(pContext)) {
				// Checks all of them before added
				for(TypeRef IntefaceToCheck : Interfaces_ToCheck) {
					Type Interface = MT.getTypeFromRefNoCheck(pContext, IntefaceToCheck);
					
					if(TKInterface.checkIfInterfaceImplementedBy(pContext, $Engine, pType, Interface, true, true))
						continue;

					throw new CurryError(
						String.format(
							"Construction Error: The type '%s' is said to implements the interface '%s' but it does not.",
							pType, Interface
						),
						pContext
					);
				}				
			}
			// If this type is abstract, just add the interface to the cache
			if(Spec.CBATo_TRUE == null) Spec.CBATo_TRUE = new HashSet<TypeRef>();
			Spec.CBATo_TRUE.addAll(Interfaces_ToCheck);
		}
		
		return null;
	}

	// Typing --------------------------------------------------------------------------------------

	/** Returns the class of this type. */
	abstract protected Class<? extends Type> getTypeClass(Context pContext);

	/** Returns the class of this type. */
	abstract protected Type getDefaultType(Context pContext);

	// Type checking ---------------------------------------------------------------------

	/** Returns the class of the data of this type */
	abstract protected Class<?> getTypeDataClass(Context pContext, TypeSpec pSpec);

	/** Checks if the object pObject can be assigned to a variable of the type pTheType */
	final protected boolean canTypeBeAssignedBy(Context pContext, Type TheType, Object ByObject) {
		TypeRef TheRef = (TheType == null) ? null : TheType.getTypeRef();
		return MType.CanTypeRefByAssignableBy(pContext, this.getEngine(), TheRef, ByObject);
	}

	/** Checks if an instance of the type pType can be assigned to a variable of the type pTheType */
	final protected boolean canTypeBeAssignedByInstanceOf(Context pContext, Type TheType, Type ByType) {
		TypeRef TheRef = (TheType == null) ? null : TheType.getTypeRef();
		TypeRef ByRef  = (ByType  == null) ? null : ByType .getTypeRef();
		return MType.CanTypeRefByAssignableByInstanceOf(pContext, this.getEngine(), TheRef, ByRef);
	}
	
	// The actual code for checking the type compatibility -------------------------------------------------------------

	// ByObject -------------------------------------------------------------------------
	
	/**
	 * Checks if an instance of the type with the spec pSpec can be assigned to a variable of the type pTheType.
	 * 
	 * This method is indented to be inherit for customization only and not for use. It will not guarantee to handle
	 * error properly and it does not need to handle Type Parameterization as TypeKind.canTypeBeAssignedByTypeWith(...)
	 * will handle that. Moreover, the type checking will not be cached so it will check logically every execution.
	 **/
	abstract protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec,
			Object pByObject);
	
	// ByInstanceof ---------------------------------------------------------------------
	
	/**
	 * Checks if the given object can be assigned into a variable of the given TypeSpec .
	 * 
	 * This method is indented to be inherit for customization only and not for use. It will not guarantee to handle
	 * error properly and it does not need to handle Type Parameterization as TypeKind.canTypeBeAssignedBy(...)
	 * will handle that. Moreover, the type checking will not be cached so it will check logically every execution.
	 **/
	abstract protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec,
			TypeSpec BySpec);
	
	final HashSet<String> RevertTypeCheckingInProgress = new HashSet<String>();

	// Engine is needed when pTheTypeSpec and pSpec are not for TJava and pSpec (or its parameters) required to be
	//    resolved or initialized.
	/** Checks if an instance of the type with the spec pSpec can be assigned to a variable of the type pTheType */
	final boolean performCheckIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine,
			TypeSpec TheSpec, TypeSpec BySpec) {
		
		// Well no need to check it
		if(TheSpec == null)   return false;
		if(TheSpec == BySpec) return true;
		
		// Precondition
		if(!TheSpec.getKindName().equals(this.getKindName())) {
			if(TheSpec == null) throw new NullPointerException();
			throw new CurryError("Internal Error: Wrong Type Kind ("+TheSpec+").(TKTypeKind#458)", pContext);
		}
		if(BySpec == null) return false;

		// Check compatible
		Engine $Engine = this.getEngine();
		
		// Attemp at all cost to get the Engine
		if($Engine == null) {
			if(pContext != null) $Engine = pContext.getEngine();
			
			if($Engine == null) {
				Type TheSpec_Type = TheSpec.getTypeRef().getTheType();
				if(TheSpec_Type != null) $Engine = TheSpec_Type.getEngine();
				
				if($Engine == null) {
					Type BySpec_Type = BySpec.getTypeRef().getTheType();
					if(BySpec_Type != null) $Engine = BySpec_Type.getEngine();
					
					if($Engine == null) $Engine = net.nawaman.curry.Engine.An_Engine;
				}
			}
		}
		
		// Perform the Type checking
		if(this.checkIfTypeCanBeAssignedByTypeWith(pContext, $Engine, TheSpec, BySpec))
			return true;
		
		// In case of Virtual, check the revert
		if(this.isVirtual(pContext)) {
			
			String S = String.format("%s-%s", BySpec, TheSpec);
			if(this.RevertTypeCheckingInProgress.contains(S)) return false;	// Check the recursive cache
			else {
				// Add in the cache - to prevent recursive
				this.RevertTypeCheckingInProgress.add(S);
					
				try {
					// Check revert
					TypeKind TK = null;
					TypeRef  TR = BySpec.getTypeRef();
					if(TR.isLoaded()) {
						TK = TR.getTheType().getTypeKind();
						return TK.checkIfTypeCanTypeBeAssignedByTypeWith_Revert(pContext, $Engine, TheSpec, BySpec);
							
					} else {
						TK = $Engine.getTypeManager().getTypeKind(BySpec.getKindName());
						return TK.checkIfTypeCanTypeBeAssignedByTypeWith_Revert(pContext, $Engine, TheSpec, BySpec);
							
					}
				} finally {
					this.RevertTypeCheckingInProgress.remove(S);
				}
			}
		}
		
		return false;
	}

	// Revert type checking ------------------------------------------------------------------------

	/** Checks if this type is virtual so it need a revert type checking. */
	abstract protected boolean isVirtual(Context pContext);

	/**
	 * Checks if an instance of the type with the spec pSpec can be assigned to a variable of the
	 * type pTheType. This is only works when this type is virtual.
	 */
	protected boolean checkIfTypeCanTypeBeAssignedByTypeWith_Revert(Context pContext, Engine pEngine, TypeSpec TheSpec,
			TypeSpec BySpec) {
		return false;
	}

	// Get Type -------------------------------------------------------------------------
	/** Returns the type of the object pObj */
	abstract protected Type getTypeOf(Context pContext, Object pObj);

	/** Returns the type of the class pCls */
	abstract protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls);

	// Instantiation ---------------------------------------------------------------------

	/** Checks if the type pTheType is abstract. */
	abstract protected boolean isTypeAbstract(Context pContext, Type pTheType);

	/** Returns the default value of the type pTheType. */
	abstract protected Object getTypeDefaultValue(Context pContext, Type pTheType);
	/** Returns the non-null default value of the type pTheType if such value exist. */
	protected Object getTypeNoNullDefaultValue(Context pContext, Type pTheType) {
		return this.getTypeDefaultValue(pContext, pTheType);
	}
	
	/** Returns the initializers for initializing newly created instance. */
	protected ConstructorInfo[] getConstructorInfos(Context pContext, Engine pEngine, Type pTheType) {
		Vector<ConstructorInfo> VCIs = pTheType.getTypeSpec().getConstructorInfo();
		if(VCIs == null) return ConstructorInfo.EmptyConstructorInfos;
		return VCIs.toArray(new ConstructorInfo[VCIs.size()]);
	}

	/** Searches and returns constructor of this type */
	protected ConstructorInfo searchConstructor(Context pContext, Type pTheType, Object pSearchKey) {
		return this.searchConstructor(pContext, null, pTheType, pSearchKey);
	}
	/** Searches and returns constructor of this type */
	protected ConstructorInfo searchConstructor(Context pContext, Engine pEngine, Type pTheType, Object pSearchKey) {
		if(pEngine == null) pEngine = this.getEngine();
		ConstructorInfo C = TypeKind.SearchConstructorInfo(pContext, pEngine, pTheType, pSearchKey);
		if(C == null) return null;
		return C;
	}
	
	/**
	 * Creates a new instance of the type.<br />
	 * If isAbstract() returns true, this method will never be called.
	 */
	final Object newTypeInstance(Context pContext, Executable pInitiator, Type pTheType, Object pSearchKey,
			Object[] pParams) {
		
		if((pTheType == null) || (pTheType == TKJava.TVoid))
			throw new NullPointerException("Unable to construct an instance of a null/void type.");
		if(pTheType.isAbstract(pContext))
			throw new IllegalArgumentException(
					"Unable to construct an instance of an abstract type (" + pTheType.toString() + ").");
		
		Engine E = this.Engine;
		if(E == null) E = pContext.getEngine();
		if(E != null) {
			MType MT = E.getTypeManager();
			MT.ensureTypeInitialized(pContext, pTheType);
			MT.checkPermissionOfType(pContext, pTheType);
			
			if(pContext == null) pContext = E.newRootContext();
			// Ensure that the access to the type is valid
			Accessibility A = Package.getAccessibilityOf(pTheType);
			if((A != null) && !A.isAllowed(pContext, pTheType, null))
				throw new CurryError("Insufficient permission for invoking the constructor "+UObject.toString(pSearchKey)+
						" of the type '"+pTheType.toString()+"'.",pContext);
			
		} else if (pContext != null) E = pContext.getEngine();
		
		ConstructorInfo CI = this.searchConstructor(pContext, pTheType, (pSearchKey == null)?pParams:pSearchKey);
		if(CI == null) {
			String SearchKeyStr = (pSearchKey == null)
			                          ? ((pParams == null) ? "()" : UObject.toString(pParams))
			                          : UObject.toString(pSearchKey);
			throw new CurryError("Unknown constructor " + SearchKeyStr + " of the type '" +
					pTheType.toString() + "'.", pContext);
		}
		
		// Ensure the owner of the context have access to the constructors. 
		Accessibility A = CI.getAccessibility();
		if((A != null) && !A.isAllowed(pContext, pTheType, CI))
			throw new CurryError("Insufficient permission for invoking the constructor "+UObject.toString(pSearchKey)+
					" of the type '"+pTheType.toString()+"'.",pContext);
		
		// Prepare a new context for the creation of the instance
		Context NewContext = new Context.ContextStackOwner(pContext, "new", false, pInitiator, pTheType, pTheType, true, null);
		Object O = this.createNewTypeInstance(NewContext, pInitiator, pTheType, pSearchKey, pParams);		
		
		if((O instanceof StackOwner) && this.isNeedInitialization()) {			
			// Do the initialize
			this.initializeNewTypeInstance(NewContext, pInitiator, pTheType, (StackOwner)O, pSearchKey, pParams);
			
			if((O instanceof DObject) && (((DObject)O).Attrs == null)) {
				throw new CurryError("The object attributes have not been initialized.", NewContext,
						new InstantiationError("Make sure the constructor is properly delegated up its super: " + pTheType));
			}
		}
		
		// Check for sure that the return result is valid value of the type
		if(!pTheType.canBeAssignedBy(NewContext, O))
			throw new CurryError("Invalid constructor return type ("+CI.getSignature()+") for "+this.toString()+".", pContext);
		
		return O;
	}

	/**
	 * Creates a new instance of the type.<br />
	 * If isAbstract() returns true, this method will never be called.
	 * @param pSearch is a search key to find the right constructor. It can be 1) null for search with the parameter, 2)
	 *            Type[] for searching with type, 3) TypeRef[] for searching with type name and 4) ExecInterface for
	 *            searching with an interface.
	 */
	abstract protected Object createNewTypeInstance(Context pContext, Executable pInitiator, Type pTheType, Object pSearchKey,
			Object[] pParams);
	
	/** Checks if this type kind required initializing */
	protected boolean isNeedInitialization() { return true; }
	
	/** Search for an constructor */
	static final protected ConstructorInfo SearchConstructorInfo(Context pContext, Engine pEngine,
			Type pTheType, Object pSearchKey) {
		
		ConstructorInfo[] Cs = pTheType.getConstructorInfos(pContext);
		if(Cs == null) return null;
		
		ConstructorInfo C = null;
		int             I = ExecInterface.NotMatch;
		Engine          E = pEngine; if((E == null) && (pContext != null)) E = pContext.getEngine();
		// Get the constructor
		if(pSearchKey instanceof TypeRef[])
			I = ExecInterface.Util.searchExecutableByTRefs(    E, pContext, Cs, null, (TypeRef[])pSearchKey,     true);
		else if(pSearchKey instanceof ExecInterface)
			I = ExecInterface.Util.searchExecutableByInterface(E, pContext, Cs, null, (ExecInterface)pSearchKey, true);
		else if((pSearchKey == null) || (pSearchKey instanceof Object[]))
			I = ExecInterface.Util.searchExecutableByParams(   E, pContext, Cs, null, (Object[])pSearchKey,      true);
		else throw new CurryError("Invalid initializer search key '"+E.toString(pSearchKey)+"'.", pContext);
		
		if((I == ExecInterface.NotMatch) || ((C = (ConstructorInfo)UArray.get(Cs, I)) == null)) return null;
		return C;
	}
	
	/** Initialize attribute of DObject instance */
	final void initializeDObjectAttribute(DObject DO, Context NewContext) {
		// Do all the initialization of all direct attributes
		AttributeInfo[] AIs = DO.getAllNonDynamicAttributeInfo(null);
			
		// First create the attribute array
		if(DO.Attrs == null) DO.Attrs = new DataHolder[DO.getMaxDHIndex()];
		else System.out.println("Somthing is wrong.");
			
		// Ensure the DataHolder is initialized.
		for(AttributeInfo AI : AIs) {
			if(!AI.getRKind().isDirect()) continue;
			// Get the value so that it will be initialized
			DO.initializeDH(NewContext, NewContext.getEngine(), DataHolder.AccessKind.Get, AI.asDirect());
		}
	}

	/** Initialize the newly created instance */
	final protected void doInitializeNewInstance(Context pContext, Executable pInitiator, Type pTheType,
			Object pNewInstance, Object pSearchKey, Object[] pParams) {
		if(pNewInstance == null) throw new NullPointerException();

		ConstructorInfo C = this.searchConstructor(pContext, pTheType, (pSearchKey == null)?pParams:pSearchKey);
		if(C == null)
			throw new CurryError("There is no such initializer "+pTheType+UArray.toString(pParams, "(", ")", ",")+".", pContext);
		
		// Prepare a new context for the initialization of the instance
		Context NewContext = new Context.ContextStackOwner(pContext, "new", false, pInitiator, pNewInstance, pTheType, true, null);
		
		if(C instanceof CIMacro) {
			// If this is a implicit root initializer
			if((pNewInstance instanceof DObject) && (((DObject)pNewInstance).Attrs == null) &&
					TKJava.TAny.getTypeRef().equals(pTheType.getSuperRef()))
				this.initializeDObjectAttribute((DObject)pNewInstance, NewContext);
			
			// Call the initializer
			NewContext.getExecutor().execExecutable(NewContext, pInitiator, ((CIMacro)C), Executable.ExecKind.Macro,
					false, pNewInstance, pParams, false, false);
			
		} else {			
			// Initialize all attributes
			if(!(pNewInstance instanceof DObject)) return;
			this.initializeDObjectAttribute((DObject)pNewInstance, NewContext);
		}
	}
	
	/**
	 * Initialize the newly created instance of the type.
	 * @param pSearch is a search key to find the right constructor. It can be 1) null for search with the parameter, 2)
	 *            Type[] for searching with type, 3) TypeRef[] for searching with type name and 4) ExecInterface for
	 *            searching with an interface.
	 **/
	protected void initializeNewTypeInstance(Context pContext, Executable pInitiator, Type pTheType,
			StackOwner pNewInstance, Object pSearchKey, Object[] pParams) {
		if(pNewInstance == null) return;
		this.doInitializeNewInstance(pContext, pInitiator, pTheType, pNewInstance, pSearchKey, pParams);
	}

	// Objectable ----------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	final public String toString() {
		return "TypeKind:" + this.getKindName();
	}

	/**{@inheritDoc}*/ @Override
	final public String toDetail() {
		return this.toString();
	}

	/**{@inheritDoc}*/ @Override
	final public boolean is(Object O) {
		return (this == O);
	}

	/**{@inheritDoc}*/ @Override
	final public boolean equals(Object O) {
		return this.is(O);
	}

	/**{@inheritDoc}*/ @Override
	final public int hashCode() {
		return super.hashCode();
	}

	/**{@inheritDoc}*/ @Override
	final public int hash() {
		return super.hashCode() + UString.hash(this.toString());
	}

	// *********************************************************************************************
	// Type StackOwner *****************************************************************************
	// *********************************************************************************************

	// ---------------------------------------------------------------------------------------------
	// General Behaviors --------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Customize of StackOwner Kind ----------------------------------------------------------------

	/** Returns the name of the operation kind of the given type */
	protected String doType_getOperKindName(Type pTheType) {
		return "static method";
	}

	/** Returns the name of the attribute kind of the given type */
	protected String doType_getAttrKindName(Type pTheType) {
		return "static field";
	}

	/** Validate the given accessibility and throw an error if the valiation fail. */
	protected void doType_validateAccessibility(Type pTheType, Accessibility pAccess) {
		if(pAccess.isOther()
				&& !(pAccess instanceof Package.Access)
				&& !(pAccess instanceof Type.Access))
			throw new IllegalArgumentException("The accessiblity must be a Type.Access or Package.Access.");
	}
	
	// ---------------------------------------------------------------------------------------------
	// General A/O related -------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	// Not Null Attribute --------------------------------------------------------------------------
	
	/** Checks if the 'NotNull' features is now on */
	public boolean doType_isEnforceNotNull(Type pTheType) { return false; }
	
	// Dynamic Handling ----------------------------------------------------------------------------
	
	/** Check if dynamic handling is allowed */
	protected boolean doType_isHandleDynamically(Type pTheType) { return false; }
	
	// Dynamic Delegation --------------------------------------------------------------------------

	/** Returns the number of Dynamic Delegation */
	protected int doType_getDynamicDelegationCount(Context pContext, Type pTheType) {
		return 0;
	}
	/** Returns the name of the attribute for the delegation */
	protected String doType_getDynamicDelegation(Context pContext, Type pTheType, int I) {
		return null;
	}
	/** Returns the type that this StackOwner need to be seen as to get the Delegation Object */
	protected TypeRef doType_getDynamicDelegationAsType(Context pContext, Type pTheType, int I) {
		return null;
	}

	// ---------------------------------------------------------------------------------------------
	// Handle A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	// Attribute -----------------------------------------------------------------------------------
	
	/** Returns all the attribute in the given list that have the same name as the given name */
	static final public Vector<AttributeInfo> getAIsByName(Vector<AttributeInfo> pAIs, String pName) {
		if(pName == null) return null;
		if(pAIs  == null) return null;
		Vector<AttributeInfo> AIs = null;
		for(int i = pAIs.size(); --i >= 0; ) {
			AttributeInfo AI = pAIs.get(i);
			if(AI == null) continue;
			if(!pName.equals(AI.getName())) continue;
			// Found the one with the same name
			if(AIs == null) AIs = new Vector<AttributeInfo>();
			AIs.add(AI);
		}
		return AIs;
	}
	
	/** Adds a lower level attribute into the list of attribute info */
	static final public void Regular_addAttributeToAttributeList(Context pContext, Engine pEngine,
			Vector<AttributeInfo> AIs, AttributeInfo AI) {
		
		if((AI == null) || (AIs == null)) return;
		
		String AName = AI.getName();
		for(int i = AIs.size(); --i >= 0; ) {
			AttributeInfo AI_InList = AIs.get(i);
			if(AI_InList == null) continue;
			if(!AName.equals(AI_InList.getName())) continue;
			
			// TODO - Check override permission

			// Repeat!! the owner is the same
			// TODO - Fix this as this is not the correct way to find a repeat
			if((AI.getOwner() != AI_InList.getOwner()) || !AI_InList.getRKind().isDirect())
				// If same one is found but not the same owner, we can be sure it 's OK
				break;

			
			if     ( AI       .getMoreData().contains(Respond.MDName_IgnoreIfRepeat)) return;
			else if(!AI_InList.getMoreData().contains(Respond.MDName_IgnoreIfRepeat)) {
				if((AI instanceof AINative) == (AI_InList instanceof AINative)) {
					throw new CurryError(
						String.format(
							"Attribute '%s' of the type '%s' already exists.",
							AName, AI_InList.getOwner()
						),
						pContext);
				}
			}
		}
		
		AIs.add(AI);
	}
	/** Adds a lower level attribute into the list of attribute info */
	protected void addTypeAttributeToAttributeList(Context pContext, Engine pEngine, Vector<AttributeInfo> AIs,
			AttributeInfo AI) {
		Regular_addAttributeToAttributeList(pContext, pEngine, AIs, AI);
	}
	/** Adds a lower level attribute into the list of attribute info */
	protected void addDataAttributeToAttributeList(Context pContext, Engine pEngine, Vector<AttributeInfo> AIs,
			AttributeInfo AI) {
		Regular_addAttributeToAttributeList(pContext, pEngine, AIs, AI);
	}
	
	// Operation -----------------------------------------------------------------------------------
	
	static int WeightConstant = 4;
	
	/** Returns the hash value of the signature name and parameter types */
	static final protected int getLeanHashOfSignature(ExecSignature ES) {
		if(ES == null) return 0;
		int h = UString.hash(ES.getName());
		h += ES.getParamCount();
		for(int i = 0; i < ES.getParamCount(); i++)
			h += (WeightConstant + i + 1)*UObject.hash(ES.getParamTypeRef(i));
		if(ES.isVarArgs())
			h += WeightConstant*WeightConstant*ExecInterface.EInterface.IsVarArgsHash;
		return h;
	}
	/** Returns the first found operation info with the same signature name and parameter types */
	static final public OperationInfo getOIByLeanSignature(Vector<OperationInfo> pOIs, ExecSignature pES) {
		
		if(pES   == null) return null;
		
		int hSearch = getLeanHashOfSignature(pES);
		for(int i = pOIs.size(); --i >= 0; ) {
			int OIH = getLeanHashOfSignature(pES);
			// If the hash equals, return it.
			if(hSearch == OIH) return pOIs.get(i);
		}
		return null;
	}
	
	/** Adds a lower level operation into the list of attribute info */
	static final public void Regular_addOperationToOperationList(Context pContext, Engine pEngine,
			Vector<OperationInfo> OIs, OperationInfo OI) {
		
		if((OI == null) || (OIs == null))
			return;
		
		ExecSignature ES = OI.getDeclaredSignature();
		
		int hSearch = getLeanHashOfSignature(ES);
		for(int i = OIs.size(); --i >= 0; ) {
			OperationInfo OOI = OIs.get(i);
			if(OOI == null)
				continue;
			
			if(getLeanHashOfSignature(OOI.getDeclaredSignature()) != hSearch) 
				continue;
			
			if((OOI.getOwner() == OI.getOwner()) && OI.getRKind().isDirect()) {		
				if     (  OI.getMoreData().contains(Respond.MDName_IgnoreIfRepeat)) return;
				else if(!OOI.getMoreData().contains(Respond.MDName_IgnoreIfRepeat)) {
					Type T = OOI.getOwnerAsType();
					if(!TKInterface.isTypeInterface(T)) {
						throw new CurryError(
							String.format("Operation '%s' of the type '%s' already exists.", ES, T),
							pContext
						);
					}
				}
			}
				
				
			// TODO - Check override permission
				
				
			// Remove the old one if it was an abstract
			int RemoveIndex = -1;
			if(OOI.getRKind().isDirect() && OOI.asDirect().isAbstract()) RemoveIndex = i;
			if(OOI.getRKind().isNative() && OOI.asNative().isAbstract()) RemoveIndex = i;
			if(RemoveIndex != -1) {
				// Replace
				OIs .remove(RemoveIndex);
			} else {
				// No replace
				return;
			}
		}
		
		OIs.add(OI);
	}
	/** Adds a lower level operation into the list of operation info */
	protected void addTypeOperationToOperationList(Context pContext, Engine pEngine, Vector<OperationInfo> OIs,
			OperationInfo OI) {
		Regular_addOperationToOperationList(pContext, pEngine, OIs, OI);
	}
	/** Adds a lower level operation into the list of operation info */
	protected void addDataOperationToOperationList(Context pContext, Engine pEngine, Vector<OperationInfo> OIs,
			OperationInfo OI) {
		Regular_addOperationToOperationList(pContext, pEngine, OIs, OI);
	}
	
	// Elements from spec --------------------------------------------------------------------------
	
	/** Returns the index in Data of the Data AttributeInfo */
	protected Vector<AttributeInfo> getTSpecDataAttributeInfo(TypeSpec TS) {
		return TS.getDataAttributeInfo();
	}
	/** Returns the index in Data of the Type AttributeInfo */
	protected Vector<AttributeInfo> getTSpecTypeAttributeInfo(TypeSpec TS) {
		return TS.getTypeAttributeInfo();
	}
	
	/** Returns the index in Data of the Data OperationInfo */
	protected Vector<OperationInfo> getTSpecDataOperationInfo(TypeSpec TS) {
		return TS.getDataOperationInfo();
	}
	/** Returns the index in Data of the Type OperationInfo */
	protected Vector<OperationInfo> getTSpecTypeOperationInfo(TypeSpec TS) {
		return TS.getTypeOperationInfo();
	}
	
	// Elements from TypeKind ----------------------------------------------------------------------

	/** Prepare fields for the type - The default is do nothing */
	protected void doType_prepareTypeKindFields(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<AttributeInfo> AIs) {}
	/** Prepare operations for the type - The default is do nothing */
	protected void doType_prepareTypeKindMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {}
	
	// Native Elements -----------------------------------------------------------------------------

	/** Prepare low priority fields for the type (It may be overridden by Native) */
	protected void doType_prepareNativeFields(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<AttributeInfo> AIs) {
		List<AttributeInfo> NativeAIs = pTheType.prepareNativeFields(pContext, pEngine, pIsStatic);
		if(NativeAIs == null) return;
		
		// Append them all
		int Count = NativeAIs.size();
		for(int i = 0; i < Count; i++) {
			AttributeInfo AI = NativeAIs.get(i);
			if(AI == null) continue;
				
			if(pIsStatic) this.addTypeAttributeToAttributeList(pContext, pEngine, AIs, AI);
			else          this.addDataAttributeToAttributeList(pContext, pEngine, AIs, AI);
		}
	}
	/** Prepare low priority operations for the type (It may be overriden by Native) */
	protected void doType_prepareNativeMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {
		// NOTE: The reason why we pass the hash on to prepareNativeMethods before ES must be created from Method anyway
		//       and that is likely to make no different in the speed
		List<OperationInfo> NativeOIs = pTheType.prepareNativeMethods(pContext, pEngine, pIsStatic);
		if(NativeOIs == null) return;

		// Append them all
		int Count = NativeOIs.size();
		for(int i = 0; i < Count; i++) {
			OperationInfo OI = NativeOIs.get(i);
			if(OI == null) continue;
			
			if(pIsStatic) this.addTypeOperationToOperationList(pContext, pEngine, OIs, OI);
			else          this.addDataOperationToOperationList(pContext, pEngine, OIs, OI);
		}
	}
	
	// Low-Priority Elements -----------------------------------------------------------------------

	/** Prepare low priority fields for the type (It may be overridden by Native) */
	protected void doType_prepareLowPriorityFields(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<AttributeInfo> AIs) {

		// Loop all default implementation of interface fields
		if(pIsStatic || (pTheType.getTypeSpec().getInterfaceCount() == 0) || (pTheType instanceof TKJava.TJava)) return;
				
		int ICount = pTheType.getTypeSpec().getInterfaceCount();
		for(int i = 0; i < ICount; i++) {
			Type T = this.getTypeFromRef(pContext, pTheType.getTypeSpec().getInterfaceRefAt(i));
			if(!(T instanceof TKInterface.TInterface)) continue;
			
			List<AttributeInfo> DefaultImplementation_Interface_AIs = Arrays.asList(T.getObjectAttributeInfos());
			if(DefaultImplementation_Interface_AIs == null) continue;
			
			// Create a delegate attribute to all attribute in DefaultImplementation_Interface_AIs
			for(AttributeInfo AI : DefaultImplementation_Interface_AIs) {
				if(AI == null) continue;
				
				// Repeat with the one higher priority, ignore this one
				if(getAIsByName(AIs, AI.getName()) != null)
					continue;
				
				// Add the newly create delegate attribute to the list
				this.addDataAttributeToAttributeList(pContext, pEngine, AIs, AI.makeClone());
			}
		}
	}
	/** Prepare low priority operations for the type (It may be overridden by Native) */
	protected void doType_prepareLowPriorityMethods(Context pContext, Engine pEngine, Type pTheType, boolean pIsStatic,
			Vector<OperationInfo> OIs) {
		
		// Only non-static and non-java
		if(pIsStatic || (pTheType.getTypeSpec().getInterfaceCount() == 0) || (pTheType instanceof TKJava.TJava)) return;
		
		// Loop all default implementation of interface methods
		HashSet<Integer> LOIESHs = new HashSet<Integer>();
		
		TypeSpec TSpec = pTheType.getTypeSpec();
		int ICount = TSpec.getInterfaceCount();
		for(int i = 0; i < ICount; i++) {
			TypeRef TR = TSpec.getInterfaceRefAt(i);
			Type    T  = this.getTypeFromRef(pContext, TR);
			// Only TKInterface.TInterface, the rest (native interface) will be checked later 
			if(!(T instanceof TKInterface.TInterface) || (T == pTheType) || (T.getTypeRef().equals(pTheType.getTypeRef())))
				continue;
			
			List<OperationInfo> DefaultImplementation_Interface_OIs = Arrays.asList(T.getObjectOperationInfos());
			if(DefaultImplementation_Interface_OIs == null) continue;
			
			// Create a delegate attribute to all operation in SOIs
			for(OperationInfo OI : DefaultImplementation_Interface_OIs) {
				if(!(OI instanceof OIDirect)) continue;
				
				// The signature of the operation repeat what previously added (from the earlier static delegation)
				int Hash = getLeanHashOfSignature(OI.getDeclaredSignature());
				// Check if already exist
				if(LOIESHs.contains(Hash)) continue;

				// Add the newly create delegate attribute to the list 
				this.addDataOperationToOperationList(pContext, pEngine, OIs, OI.makeClone());
			}
		}
	}

	// *********************************************************************************************
	// StackOwner behavior for Type ****************************************************************
	// *********************************************************************************************
	
	/** Checks if this StackOwner is appendable */
	protected boolean doType_isElementAppendable(Type pTheType) { return false; }

	// *********************************************************************************************
	// Object Behaviors ***************************************************************************
	// *********************************************************************************************
	
	// ---------------------------------------------------------------------------------------------
	// General Behaviors --------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Customize of StackOwner Kind ----------------------------------------------------------------	
	
	/** Returns the name of the operation kind */
	protected String doData_getOperKindName(Type pTheType, DObject pTheObject) { return "method"; }
	/** Returns the name of the attribute kind */
	protected String doData_getAttrKindName(Type pTheType, DObject pTheObject) { return "field"; }

	// Accessibility ------------------------------------------------------------------------------
	
	/** Validate the given accessibility and throw an error if the validation fail. */
	protected void doData_validateAccessibility(Type pTheType, DObject pTheObject,
			Accessibility pAccess) {
		this.doType_validateAccessibility(pTheType, pAccess);
		return;
	}
	
	// ---------------------------------------------------------------------------------------------
	// General A/O related -------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
		
	// Not Null Attribute --------------------------------------------------------------------------
	
	/** Checks if the 'NotNull' features is now on */
	public boolean doData_isEnforceNotNull(Type pTheType, DObject pTheObject) { return false; }

	// Dynamic Handling ----------------------------------------------------------------------------
	
	/** Check if dynamic handling is allowed */
	protected boolean doData_isHandleDynamically(Type pTheType, DObject pTheObject) { return false; }
	
	// Dynamic Delegation --------------------------------------------------------------------------

	/** Returns the number of Dynamic Delegation */
	protected int doData_getDynamicDelegationCount(Type pTheType, DObject pTheObject, Context pContext) {
		return 0;
	}
	/** Returns the name of the attribute for the delegation */
	protected String doData_getDynamicDelegation(Type pTheType, DObject pTheObject, Context pContext, int I) {
		return null;
	}
	/** Returns the type that this StackOwner need to be seen as to get the Delegation Object */
	protected TypeRef doData_getDynamicDelegationAsType(Type pTheType, DObject pTheObject, Context pContext, int I) {
		return null;
	}

	// *********************************************************************************************
	// Utilities ***********************************************************************************
	// *********************************************************************************************

	/** This is for all types to be able to access Type of a ref without effect of context */
	final protected Type getTypeFromRef(Context pContext, TypeRef pTRef) {
		if(pTRef == null) return null;
		try {
			Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
			return E.getTypeManager().getTypeFromRefNoCheck(pContext, pTRef);
		} catch (Exception E) { return null; }
	}
	
	// General Type Utilities ------------------------------------------------------------

	final protected boolean isTypeLoaded(Context pContext, TypeRef pTRef) {
		Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
		Type T = pTRef.getTheType();
		if(T == null) return false;
		return T.isLoaded();
	}

	final protected boolean isTypeResolved(Context pContext, TypeRef pTRef) {
		Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
		Type T = pTRef.getTheType();
		if(T == null) return false;
		return T.isResolved();
	}
	final protected void ensureTypeResolved(Context pContext, TypeRef pTRef) {
		Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
		E.getTypeManager().ensureTypeExist(pContext, pTRef);
	}

	final protected boolean isTypeValided(Context pContext, TypeRef pTRef) {
		Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
		Type T = pTRef.getTheType();
		if(T == null) return false;
		return T.isValidated();
	}
	final protected void ensureTypeValided(Context pContext, TypeRef pTRef) {
		Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
		E.getTypeManager().ensureTypeValidated(pContext, pTRef, null);
	}
	final protected void ensureTypeValided(Context pContext, TypeRef pTRef, TypeRef pParameterBaseTypeToIgnore) {
		Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
		E.getTypeManager().ensureTypeValidated(pContext, pTRef, pParameterBaseTypeToIgnore);
	}

	final protected boolean isTypeInitialized(Context pContext, TypeRef pTRef) {
		Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
		Type T = pTRef.getTheType();
		if(T == null) return false;
		return T.isInitialized();
	}
	final protected void ensureTypeInitialized(Context pContext, TypeRef pTRef) {
		Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
		E.getTypeManager().ensureTypeInitialized(pContext, pTRef);
	}

	// Delegate to type -----------------------------------------------------------------

	/** Returns the type kind of this type. */
	final protected TypeKind getTypeKind(TypeRef pTRef) {
		return (pTRef == null)?null:pTRef.getTypeKind(this.getEngine());
	}

	/** Returns the name of the kind this type is. */
	final protected String getTypeKindName(TypeRef pTRef) {
		return (pTRef == null)?null:pTRef.getTypeKindName(this.getEngine());
	}
	
	// TypeSpec --------------------------------------------------------------------------------------------------------

	/** Returns the spec of this type. */
	final protected TypeSpec getTypeSpec(Context pContext, TypeRef pTRef) {
		Engine E = this.getEngine(); if(E == null) E = pContext.getEngine();
		return pTRef.getTypeSpec(E, pContext);
	}
	
	// Constructor info ----------------------------------------------------------------------------
	
	/** Creates a constructor */
	final protected ConstructorInfo newConstructorInfo(TypeRef pTheTypeRef, Accessibility pAccess, Executable.Macro pMacro, MoreData pMoreData) {
		return new ConstructorInfo.CIMacro(this.Engine, pAccess, pTheTypeRef, pMacro, pMoreData);
	}
	
	// ---------------------------------------------------------------------------------------------
	// Type ----------------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Invalid AsType ----------------------------------------------------------

	/** Throw an InvalidOperationRespondObject error */
	final protected void doType_throwInvalidOperationAsType(Type pTheType, Context pContext,
			Type pAsType, ExecSignature pSignature) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		pTheType.throwOperation("Operation Invocation Error: The "
				+ pTheType.getOperKindName() + " cannot be access via the type '"
				+ pAsType.toString() + "'", pContext, pAsType, pSignature);
	}
	/** Throw an InvalidOperationRespondObject error */
	final protected void doType_throwInvalidOperationAsType(Type pTheType, Context pContext, Type pAsType,
			String pSignatureStr) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		pTheType.throwOperation("Operation Invocation Error: The "
				+ pTheType.getOperKindName() + " cannot be access via the type '"
				+ pAsType.toString() + "'", pContext, pAsType, pSignatureStr);
	}

	final protected void doType_throwInvalidAttributeAsType(Type pTheType, Context pContext,
			Type pAsType, DataHolder.AccessKind pAKind, String pAttrName) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		pTheType.throwAttribute("Attribute Access Error: The " + pTheType.getAttrKindName()
				+ " cannot be access via the type '" + pAsType.toString() + "'",
				pContext, pAKind, pAsType, pAttrName);
	}

	// Internal utilities (for this SO to use) -----------------------------------------------------
	
	// Access others element -------------------------------------------------------------
	
	/** Returns the attribute info of the given type */
	final protected DataArray<AttributeInfo> getTypeAttributeInfo(Type pType) {
		return new DataArray_Proxy<AttributeInfo>(pType.getAttributeInfos(), false); 
	}
	
	/** Returns the operation info of the given type */
	final protected DataArray<OperationInfo> getTypeOperationInfo(Type pType) {
		return new DataArray_Proxy<OperationInfo>(pType.getOperationInfos(), false); 
	}
	
	/** Returns the attribute info of the given type */
	final protected DataArray<AttributeInfo> getObjectAttributeInfo(Type pType) {
		return new DataArray_Proxy<AttributeInfo>(pType.getObjectAttributeInfos(), false); 
	}
	
	/** Returns the operation info of the given type */
	final protected DataArray<OperationInfo> getObjectOperationInfo(Type pType) {
		return new DataArray_Proxy<OperationInfo>(pType.getObjectOperationInfos(), false); 
	}
	
	// For prepare element ---------------------------------------------------------------

	/** Prepare Type Field Information */
	final protected void doType_prepareTypeFields(Type pTheType, Context pContext, Engine pEngine) {
		pTheType.prepareTypeFields(pContext, pEngine);
	}

	/** Prepare Object Field Information */
	final protected void doType_prepareObjectFields(Type pTheType, Context pContext, Engine pEngine) {
		pTheType.prepareObjectFields(pContext, pEngine);
	}

	/** Prepare Type Method Information */
	final protected void doType_prepareTypeMethods(Type pTheType, Context pContext, Engine pEngine) {
		pTheType.prepareTypeMethods(pContext, pEngine);
	}

	/** Prepare Object Method Information */
	final protected void doType_prepareObjectMethods(Type pTheType, Context pContext, Engine pEngine) {
		pTheType.prepareObjectMethods(pContext, pEngine);
	}

	// Report Error ----------------------------------------------------------------------

	/** Throw an error message that involve operation */
	final protected void doType_throwOperation(Type pTheType, String pErrMsg, Context pContext,
			Type pAsType, ExecSignature pSignature) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		pTheType.throwOperation(pErrMsg, pContext, pAsType, pSignature);
	}

	/** Throw an error message that involve operation */
	final protected void doType_throwOperation(Type pTheType, String pErrMsg, Context pContext,
			Type pAsType, String pSignatureStr) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		pTheType.throwOperation(pErrMsg, pContext, pAsType, pSignatureStr);
	}

	/** Throw an error message that involve attribute */
	final protected void doType_throwAttribute(Type pTheType, String pErrMsg, Context pContext,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName, Object pParam1,
			Object pParam2) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		pTheType.throwAttribute(pErrMsg, pContext, pAKind, pAsType, pAttrName, pParam1, pParam2);
	}

	/** Throw an error message that involve attribute */
	final protected void doType_throwAttribute(Type pTheType, String pErrMsg, Context pContext,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		pTheType.throwAttribute(pErrMsg, pContext, pAKind, pAsType, pAttrName);
	}
	
	// ---------------------------------------------------------------------------------------------
	// Utilities -----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// DObject ---------------------------------------------------------------------------
	
	/** Create a new data object of the given type */
	protected DObject doType_newDObject(Context pContext, Type pTheType) {
		return new DObject(pTheType);
	}
	
	// ActionRecord ----------------------------------------------------------------------

	/** Create an action record. */
	final protected ActionRecord doType_newActionRecord(Type pTheType, Context pContext) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newActionRecord(pContext);
	}

	// Access to Data ------------------------------------------------------------------------------

	/** Returns the number of attribute info this stackowner contains */
	final protected int doType_getAttrInfoCount(Type pTheType, DObject pTheObject) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.getAttrInfoCount();
	}
	/** Returns the attribute info at the index */
	final protected AttributeInfo doType_getAttrInfoAt(Type pTheType, DObject pTheObject, int pIndex) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.getAttrInfoAt(pIndex);
	}
	/** Returns the data-holder by the index */
	final protected DataHolder doType_getDHByIndex(Type pTheType, Context pContext, DataHolder.AccessKind DHAK,
			int Index) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.getDHByIndex(pContext, DHAK, Index);
	}
	
	// Duplicate and Creating responds -------------------------------------------------------------
	
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected AttributeInfo doType_borrowAttributeInfo(Type pTheType, AttributeInfo pAI) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.borrowAttributeInfo(pAI);
	}
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected OperationInfo doType_borrowOperationInfo(Type pTheType, OperationInfo pOI) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.borrowOperationInfo(pOI);
	}
	
	/** Make a clone of the attribute info */
	final protected AttributeInfo doType_cloneAttributeInfo(Type pTheType, AttributeInfo pAI) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.cloneAttributeInfo(pAI);
	}
	/** Make a clone of the attribute info */
	final protected OperationInfo doType_cloneOperationInfo(Type pTheType, OperationInfo pOI) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.cloneOperationInfo(pOI);
	}

	// Dynamic --------------------------------------------------------
	/** Creates a new operation info */
	final protected OIDynamic doType_newOIDynamic(Type pTheType, Accessibility pAccess, ExecSignature pES,
			MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newOIDynamic(pAccess, pES, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AIDynamic doType_newAIDynamic(Type pTheType, Accessibility pARead,
			Accessibility pAWrite, Accessibility pAConfig, String pVName, TypeRef pTRef, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newAIDynamic(pARead, pAWrite, pAConfig, pVName, pTRef, pMoreData);
	}

	// Field --------------------------------------------------------
	/** Creates a new operation info */
	final protected OIDlgAttr doType_newOIDlgAttr(Type pTheType, Accessibility pAccess, ExecSignature pES,
			String pDlgAttr, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newOIDlgAttr(pAccess, pES, pDlgAttr, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AIDlgAttr doType_newAIDlgAttr(Type pTheType, Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, String pDlgAttr, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newAIDlgAttr(pARead, pAWrite, pAConfig, pVName, pDlgAttr, pMoreData);
	}
	
	// Object --------------------------------------------------------
	/** Creates a new operation info */
	final protected OIDlgObject doType_newOIDlgObject(Type pTheType, Accessibility pAccess, ExecSignature pES,
			Object pDlgObject, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newOIDlgObject(pAccess, pES, pDlgObject, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AIDlgObject doType_newAIDlgObject(Type pTheType, Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, boolean pIsNotNull, Object pDlgObject, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newAIDlgObject(pARead, pAWrite, pAConfig, pVName, pIsNotNull, pDlgObject, pMoreData);
	}

	// Direct --------------------------------------------------------
	/** Creates a new operation info */
	final protected OIDirect doType_newOIDirect(Type pTheType, Accessibility pAccess, Executable pExec,
			MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newOIDirect(pAccess, pExec, pMoreData);
	}
	/** Creates a new abstract operation info */
	final protected OIDirect doType_newOIDirect(Type pTheType, Engine pEngine, Accessibility pAccess,
			ExecSignature pSignature, Executable.ExecKind pKind, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newOIDirect(pAccess, pSignature, pKind, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AIDirect doType_newAIDirect(Type pTheType, Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, boolean pIsNotNull, DataHolderInfo pDHI, Location pLocation,
			MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.newAIDirect(pARead, pAWrite, pAConfig, pVName, pIsNotNull, pDHI, pLocation, pMoreData);
	}

	// Display information ---------------------------------------------------------------

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final public String doType_getAttributeAccessToString(Type pTheType,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName,
			Object pParam1, Object pParam2) {
		return pTheType.getAttributeAccessToString(pAKind, pAsType, pAttrName, pParam1, pParam2);
	}

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final public String doType_getAttributeAccessToString(Type pTheType,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName) {
		return pTheType.getAttributeAccessToString(pAKind, pAsType, pAttrName);
	}

	/** Display the operation access as a string */
	final public String doType_getOperationAccessToString(Type pTheType,
			OperationSearchKind pOSKind, Type pAsType, Object pParam1, Object pParam2) {
		return pTheType.getOperationAccessToString(pOSKind, pAsType, pParam1, pParam2);
	}

	// ---------------------------------------------------------------------------------------------
	// Object --------------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// ActionRecord ----------------------------------------------------------------------

	/** Create an action record. */
	final protected ActionRecord doData_newActionRecord(Type pTheType, DObject pTheObject, Context pContext) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newActionRecord(pTheObject, pContext);
	}
	
	// Access to Data ------------------------------------------------------------------------------
	
	/** Returns the number of attribute info this stackowner contains */
	final protected int doData_getAttrInfoCount(Type pTheType, DObject pTheObject) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_getAttrInfoCount(pTheObject);
	}
	/** Returns the attribute info at the index */
	final protected AttributeInfo doData_getAttrInfoAt(Type pTheType, DObject pTheObject, int pIndex) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_getAttrInfoAt(pTheObject, pIndex);
	}
	/** Returns the data-holder by the index */
	final protected DataHolder doData_getDHByIndex(Type pTheType, DObject pTheObject, Context pContext,
			DataHolder.AccessKind DHAK, int Index) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_getDHByIndex(pTheObject, pContext, DHAK, Index);
	}
	
	// Duplicate and Creating responds -------------------------------------------------------------
	
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected AttributeInfo doData_borrowAttributeInfo(Type pTheType, DObject pTheObject, AttributeInfo pAI) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_borrowAttributeInfo(pTheObject, pAI);
	}
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected OperationInfo doData_borrowOperationInfo(Type pTheType, DObject pTheObject, OperationInfo pOI) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_borrowOperationInfo(pTheObject, pOI);
	}
	
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected AttributeInfo doData_cloneAttributeInfo(Type pTheType, DObject pTheObject, AttributeInfo pAI) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_cloneAttributeInfo(pTheObject, pAI);
	}
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected OperationInfo doData_cloneOperationInfo(Type pTheType, DObject pTheObject, OperationInfo pOI) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_cloneOperationInfo(pTheObject, pOI);
	}

	// Dynamic --------------------------------------------------------
	/** Creates a new operation info */
	final protected OperationInfo doData_newOIDynamic(Type pTheType, DObject pTheObject, Accessibility pAccess,
			ExecSignature pES, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newOIDynamic(pTheObject, pAccess, pES, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AttributeInfo doData_newAIDynamic(Type pTheType, DObject pTheObject, Accessibility pARead,
			Accessibility pAWrite, Accessibility pAConfig, String pVName, TypeRef pTRef, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newAIDynamic(pTheObject, pARead, pAWrite, pAConfig, pVName, pTRef, pMoreData);
	}

	// Field --------------------------------------------------------
	/** Creates a new operation info */
	final protected OperationInfo doData_newOIDlgAttr(Type pTheType, DObject pTheObject, Accessibility pAccess,
			ExecSignature pES, String pDlgAttr, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newOIDlgAttr(pTheObject, pAccess, pES, pDlgAttr, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AttributeInfo doData_newAIDlgAttr(Type pTheType, DObject pTheObject, Accessibility pARead,
			Accessibility pAWrite, Accessibility pAConfig, String pVName, String pDlgAttr, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newAIDlgAttr(pTheObject, pARead, pAWrite, pAConfig, pVName, pDlgAttr, pMoreData);
	}
	
	// Object --------------------------------------------------------
	/** Creates a new operation info */
	final protected OperationInfo doData_newOIDlgObject(Type pTheType, DObject pTheObject, Accessibility pAccess,
			ExecSignature pES, Object pDlgObject, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newOIDlgObject(pTheObject, pAccess, pES, pDlgObject, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AttributeInfo doData_newAIDlgObject(Type pTheType, DObject pTheObject, Accessibility pARead,
			Accessibility pAWrite, Accessibility pAConfig, String pVName, boolean pIsNotNull, Object pDlgObject,
			MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newAIDlgObject(pTheObject, pARead, pAWrite, pAConfig, pVName, pIsNotNull, pDlgObject,
				pMoreData);
	}

	// Direct --------------------------------------------------------
	/** Creates a new operation info */
	final protected OperationInfo doData_newOIDirect(Type pTheType, DObject pTheObject, Accessibility pAccess,
			Executable pExec, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newOIDirect(pTheObject, pAccess, pExec, pMoreData);
	}
	/** Creates a new abstract operation info */
	final protected OperationInfo doData_newOIDirect(Type pTheType, DObject pTheObject, Engine pEngine,
			Accessibility pAccess, ExecSignature pSignature, Executable.ExecKind pKind, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newOIDirect(pTheObject, pEngine, pAccess, pSignature, pKind, pMoreData);
	}
	/** Creates a new attribute info */
	final protected AttributeInfo doData_newAIDirect(Type pTheType, DObject pTheObject, Accessibility pARead,
			Accessibility pAWrite, Accessibility pAConfig, String pVName, boolean pIsNotNull, DataHolderInfo pDHI,
			Location pLocation, MoreData pMoreData) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_newAIDirect(pTheObject, pARead, pAWrite, pAConfig, pVName, pIsNotNull, pDHI, pLocation,
				pMoreData);
	}

	// Display information ---------------------------------------------------------------

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final public String doData_getAttributeAccessToString(Type pTheType, DObject pTheObject,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName, Object pParam1,
			Object pParam2) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_getAttributeAccessToString(pTheObject, pAKind, pAsType, pAttrName);
	}

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final public String doData_getAttributeAccessToString(Type pTheType, DObject pTheObject,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_getAttributeAccessToString(pTheObject, pAKind, pAsType, pAttrName);
	}

	/** Display the operation access as a string */
	final public String doData_getOperationAccessToString(Type pTheType, DObject pTheObject,
			OperationSearchKind pOSKind, Type pAsType, Object pParam1, Object pParam2) {
		if(pTheType.TKind != this) throw new IllegalArgumentException();
		return pTheType.doData_getOperationAccessToString(pTheObject, pOSKind, pAsType, pParam1,
				pParam2);
	}
}
