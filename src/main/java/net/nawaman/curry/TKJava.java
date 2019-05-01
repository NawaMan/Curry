package net.nawaman.curry;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;

import java.util.Vector;

import net.nawaman.curry.TLPrimitive.TRPrimitive;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.MoreData;
import net.nawaman.script.Function;
import net.nawaman.script.Macro;
import net.nawaman.script.Script;
import net.nawaman.util.DataArray;
import net.nawaman.util.UArray;
import net.nawaman.util.UClass;

final public class TKJava extends TypeKind {
	
	static public final String KindName = "Java"; 
	
	// Singleton ------------------------------------------------------------------------
	
	final static public TKJava Instance = new TKJava();

	// Native is engine independent
	TKJava() { super(null); }
	
	
	// Default native type --------------------------------------------------------------
	
	static private Hashtable<String, TJava> Types = new Hashtable<String, TJava>();
	
	// To Be Primitive ---------------------------------------------------------
	final static public TJava TAny  = (TJava)Instance.getTypeByClass(null,  "any", Object.class);
	final static public TJava TVoid = (TJava)Instance.getTypeByClass(null, "void",   Void.class);
	
	final static public TJava TBoolean   = (TJava)Instance.getTypeByClass(null, "boolean", Boolean.class);
	final static public TJava TCharacter = (TJava)Instance.getTypeByClass(null, "char",    Character.class);

	final static public TJava TByte       = (TJava)Instance.getTypeByClass(null,       "byte", Byte.class);
	final static public TJava TShort      = (TJava)Instance.getTypeByClass(null,      "short", Short.class);
	final static public TJava TInteger    = (TJava)Instance.getTypeByClass(null,        "int", Integer.class);
	final static public TJava TLong       = (TJava)Instance.getTypeByClass(null,       "long", Long.class);
	final static public TJava TFloat      = (TJava)Instance.getTypeByClass(null,      "float", Float.class);
	final static public TJava TDouble     = (TJava)Instance.getTypeByClass(null,     "double", Double.class);
	final static public TJava TNumber     = (TJava)Instance.getTypeByClass(null,     "Number", Number.class);
	final static public TJava TBigInteger = (TJava)Instance.getTypeByClass(null, "BigInteger", BigInteger.class);
	final static public TJava TBigDecimal = (TJava)Instance.getTypeByClass(null, "BigDecimal", BigDecimal.class);
	
	final static public TJava TEngine     = (TJava)Instance.getTypeByClass(null,      "Engine", Engine.class);
	final static public TJava TExecutor   = (TJava)Instance.getTypeByClass(null,    "Executor", Executor.class);
	final static public TJava TContext    = (TJava)Instance.getTypeByClass(null,     "Context", Context.class);
	final static public TJava TType       = (TJava)Instance.getTypeByClass(null,        "Type", Type.class);
	final static public TJava TTypeSpec   = (TJava)Instance.getTypeByClass(null,    "TypeSpec", TypeSpec.class);
	final static public TJava TTypeKind   = (TJava)Instance.getTypeByClass(null,    "TypeKind", TypeKind.class);
	final static public TJava TTypeRef    = (TJava)Instance.getTypeByClass(null,     "TypeRef", TypeRef.class);
	final static public TJava TTypeLoader = (TJava)Instance.getTypeByClass(null,  "TypeLoader", TypeLoader.class);
	
	// This one is not a primitive
	final static public TJava TString = (TJava)Instance.getTypeByClass(null, "String", String.class);
	
	// For Easy Access -----------------------------------------------------------------------------
	final static public TJava TExpression = (TJava)Instance.getTypeByClass(null,  "Expression",  Expression.class);
	

	final static public TJava TSystem        = (TJava)Instance.getTypeByClass(null,        "System", System.class);
	final static public TJava TClass         = (TJava)Instance.getTypeByClass(null,         "Class", Class.class);
	final static public TJava TSerializable  = (TJava)Instance.getTypeByClass(null,  "Serializable", Serializable.class);
	final static public TJava TIterator      = (TJava)Instance.getTypeByClass(null,      "Iterator", Iterator.class);
	final static public TJava TIterable      = (TJava)Instance.getTypeByClass(null,      "Iterable", Iterable.class);
	final static public TJava TDataHolder    = (TJava)Instance.getTypeByClass(null,    "DataHolder", DataHolder.class);
	final static public TJava TDataArray     = (TJava)Instance.getTypeByClass(null,     "DataArray", DataArray.class);
	final static public TJava TScopePrivate  = (TJava)Instance.getTypeByClass(null,  "ScopePrivate", ScopePrivate.class);
	final static public TJava TMoreData      = (TJava)Instance.getTypeByClass(null,      "MoreData", MoreData.class);
	final static public TJava TExecSignature = (TJava)Instance.getTypeByClass(null, "ExecSignature", ExecSignature.class);
	
	final static public TJava TCaseEntry  = (TJava)Instance.getTypeByClass(null,  "CaseEntry", Instructions_ControlFlow.Inst_Switch.CaseEntry.class);
	final static public TJava TCatchEntry = (TJava)Instance.getTypeByClass(null, "CatchEntry", Instructions_ControlFlow.Inst_TryCatch.CatchEntry.class);
	
	final static public TJava TCharSequence = (TJava)Instance.getTypeByClass(null, "CharSequence", CharSequence.class);
	final static public TJava TCloneable    = (TJava)Instance.getTypeByClass(null, "Cloneable",    Cloneable.class);
	final static public TJava TComparable   = (TJava)Instance.getTypeByClass(null, "Comparable",   Comparable.class);
	final static public TJava TComparator   = (TJava)Instance.getTypeByClass(null, "Comparator",   Comparator.class);
	final static public TJava TRunnable     = (TJava)Instance.getTypeByClass(null, "Runnable",     Runnable.class);
	final static public TJava TAppendable   = (TJava)Instance.getTypeByClass(null, "Appendable",   Appendable.class);
	final static public TJava TReadable     = (TJava)Instance.getTypeByClass(null, "Readable",     Readable.class);
	
	final static public TJava TMath              = (TJava)Instance.getTypeByClass(null, "Math",              Math.class);
	final static public TJava TClassLoader       = (TJava)Instance.getTypeByClass(null, "ClassLoader",       ClassLoader.class);
	final static public TJava TEnum              = (TJava)Instance.getTypeByClass(null, "Enum",              Enum.class);
	final static public TJava TPackage           = (TJava)Instance.getTypeByClass(null, "Package",           Package.class);
	final static public TJava TProcess           = (TJava)Instance.getTypeByClass(null, "Process",           Process.class);
	final static public TJava TProcessBuilder    = (TJava)Instance.getTypeByClass(null, "ProcessBuilder",    ProcessBuilder.class);
	final static public TJava TRuntime           = (TJava)Instance.getTypeByClass(null, "Runtime",           Runtime.class);
	final static public TJava TRuntimePermission = (TJava)Instance.getTypeByClass(null, "RuntimePermission", RuntimePermission.class);
	final static public TJava TSecurityManager   = (TJava)Instance.getTypeByClass(null, "SecurityManager",   SecurityManager.class);
	final static public TJava TStackTraceElement = (TJava)Instance.getTypeByClass(null, "StackTraceElement", StackTraceElement.class);
	final static public TJava TStrictMath        = (TJava)Instance.getTypeByClass(null, "StrictMath",        StrictMath.class);
	final static public TJava TStringBuilder     = (TJava)Instance.getTypeByClass(null, "StringBuilder",     StringBuilder.class);
	final static public TJava TStringBuffer      = (TJava)Instance.getTypeByClass(null, "StringBuffer",      StringBuffer.class);
	final static public TJava TThread            = (TJava)Instance.getTypeByClass(null, "Thread",            Thread.class);
	final static public TJava TThreadGroup       = (TJava)Instance.getTypeByClass(null, "ThreadGroup",       ThreadGroup.class);
	final static public TJava TThreadLocal       = (TJava)Instance.getTypeByClass(null, "ThreadLocal",       ThreadLocal.class);
	
	final static public TJava TThrowable = (TJava)Instance.getTypeByClass(null, "Throwable", Throwable.class);
	final static public TJava TException = (TJava)Instance.getTypeByClass(null, "Exception", Exception.class);
	final static public TJava TError     = (TJava)Instance.getTypeByClass(null, "Error",     Error.class    );
	
	final static public TJava TFragment   = (TJava)Instance.getTypeByClass(null, "Fragment"  , Executable.Fragment  .class);
	final static public TJava TMacro      = (TJava)Instance.getTypeByClass(null, "Macro"     , Executable.Macro     .class);
	final static public TJava TSubRoutine = (TJava)Instance.getTypeByClass(null, "SubRoutine", Executable.SubRoutine.class);
	final static public TJava TClosure    = (TJava)Instance.getTypeByClass(null, "Closure"   , Closure              .class);
	final static public TJava TExecutable = (TJava)Instance.getTypeByClass(null, "Executable", Executable           .class);

	final static public TJava TScriptScript   = (TJava)Instance.getTypeByClass(null, "ScriptScript",   Script  .class);
	final static public TJava TScriptMacro    = (TJava)Instance.getTypeByClass(null, "ScriptMacro",    Macro   .class);
	final static public TJava TScriptFunction = (TJava)Instance.getTypeByClass(null, "ScriptFunction", Function.class);
	
	/** Returns the type of the given type-ref refers to a java type */
	static public Type getJavaTypeFromRef(Engine pEngine, TypeRef pTRef) {
		if(pTRef == null) return null;
		if(!pTRef.isLoaded() || !pTRef.getTheType().isInitialized()) pEngine.getTypeManager().ensureTypeInitialized(pTRef);
		if(pTRef.getTheType() instanceof TJava) return pTRef.getTheType();
		return null;
	}
			
	// Satisfy TypeFactory --------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return KindName;
	}

	// Create a new type from Spec
	/**{@inheritDoc}*/ @Override
	protected Type getType(Engine pEngine, Context pContext, TypeSpec pSpec) {
		// Precondition
		if(!(pSpec instanceof TSJava)) {
			String SpecStr = (pSpec == null)?"null":pSpec.getKindName();
			throw new CurryError("Internal Error: Wrong type kind ("+ SpecStr +" in " + KindName + " ).(TKJava.java#76)",
					pContext);
		}
		
		// Creates the class
		return this.getTypeByClassName(pEngine, ((TSJava)pSpec).getAlias(), ((TSJava)pSpec).getClassName());
	}

	/** Gets Java Type by the class */
	Type getTypeByClass(Engine pEngine, String pAlias, Class<?> pCls) {
		if((pAlias == null) && (pCls == null)) return null;
		if(pCls.isArray())  {
			Engine E = pEngine;
			if(E == null) E = net.nawaman.curry.Engine.An_Engine;
			if(E == null) throw new NullPointerException("No engine");
			return E.getTypeManager().getTypeFromRef(
						TKArray.newArrayTypeRef(
							this.getTypeByClass(pEngine, pAlias, pCls.getComponentType()).getTypeRef()));
		}
		
		// Look by Alias
		TJava T = (pAlias == null) ? null : TKJava.Types.get(pAlias);
		// Found it here
		if(T != null) return T;

		String ClsName = pCls.getCanonicalName(); if(ClsName == null) ClsName = pCls.getName();
		
		T = TKJava.Types.get(ClsName);
		// Found it here
		if(T != null) return T;
		
		// Create the type
		T = new TJava(pEngine, new TKJava.TSJava(pAlias, ClsName, false, false));
		
		// Add it to the collection
		if(pAlias != null) Types.put(pAlias, T);
		Types.put(ClsName, T);
		
		return T;
	}
	/** Gets Java Type by the class name */
	Type getTypeByClassName(Engine E, String pAlias, String pClassName) {
		if((pAlias == null) && (pClassName == null)) return null;
		// Look by Alias
		TJava T = (pAlias == null)?null:TKJava.Types.get(pAlias);
		// Found it here
		if(T != null) return T;

		T = TKJava.Types.get(pClassName);
		// Found it here
		if(T != null) return T;

		// Get the Class
		Class<?> Cls = UClass.getClassByName(pClassName, MClassPaths.getClassLoaderOf(E));
		if(Cls == null) return null;
		
		return this.getTypeByClass(E, pAlias, Cls);
	}

	/**{@inheritDoc}*/ @Override
	protected boolean isVirtual(Context pContext) {
		return false;
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isTypeDerivable(Context pContext, Type pTheType) {
		return false;
	}
	
	// Typing --------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected Class<? extends Type> getTypeClass(Context pContext) {
		return TJava.class;
	}

	/**{@inheritDoc}*/ @Override
	protected Type getDefaultType(Context pContext) {
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Class<?> getTypeDataClass(Context pContext, TypeSpec pTS) {
		TypeRef TR = ((TSJava)pTS).getTypeRef();
		Type T = TR.getTheType();
		
		if(T instanceof TJava) return ((TJava)T).getTheDataClass();
		Class<?> C = UClass.getClassByName(((TSJava)pTS).getClassName(), MClassPaths.getClassLoaderOf(pContext));
		if(T == null) {
			
			if(pContext != null) {
				Engine E = pContext.getEngine();
				E.getTypeManager().ensureTypeExist(pContext, TR);
				T = TR.getTheType();
			} else {
				return C;
			}
		}
		
		return (((TJava)T).DataClass = C);
	}

	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedBy(Context pContext, Engine pEngine, TypeSpec pTheTypeSpec,
			Object pByObject) {
		
		if(!(pTheTypeSpec instanceof TSJava)) {
			if(pTheTypeSpec == null) throw new NullPointerException();
			throw new CurryError(
				String.format(
					"Internal Error: Wrong Type Kind (%s).(TKJava.java#164)",
					pTheTypeSpec.getKindName()
				),
				pContext
			);
		}
		
		// Ensure that it is validated
		if(!pTheTypeSpec.isValidated()) {
			if((pEngine == null) && (pContext != null))
				pEngine = pContext.getEngine();

			if((pEngine != null) || !(pTheTypeSpec.getTypeRef() instanceof TRPrimitive))
				pEngine.getTypeManager().ensureTypeValidated(pContext, pTheTypeSpec.getTypeRef(), null);
			else {
				// There is a case that will have problem with null Engine and that is when TheTypeSpec is java
				TRPrimitive TheTRef = (TRPrimitive)pTheTypeSpec.getTypeRef();
				Type T = TKJava.Instance.getTypeByClassName(pEngine, TheTRef.getAlias(), TheTRef.getClassCanonicalName());
				TheTRef.setTheType(T);
			} 
		}
		
		Type     T = pTheTypeSpec.getTypeRef().getTheType();
		Class<?> C = ((TJava)T).DataClass;
		if(C.isInstance(pByObject)) return true;
		
		// If this is an interface and the object is DObject, as the object as native then check them
		if((pByObject instanceof DObject) && TKInterface.isTypeInterface(T)) {
			pByObject = ((DObject)pByObject).getAsNative();
			return C.isInstance(pByObject);
		}
		return false;
	}
	/**{@inheritDoc}*/ @Override
	protected boolean checkIfTypeCanBeAssignedByTypeWith(Context pContext, Engine pEngine, TypeSpec TheSpec, TypeSpec BySpec) {
		
		ClassLoader CL = null;
		// Null or not a TJava, return false;
		Type     ThisT = null;
		Class<?> ThisC;
		Class<?> TypeC;
		// Get the class
		if(!TheSpec.Ref.isLoaded() || !(ThisT = TheSpec.Ref.getTheType()).isInitialized()) {
			CL = MClassPaths.getClassLoaderOf(pContext);
			ThisC = UClass.getClassByName(((TSJava)TheSpec).getClassName(), CL);
		} else ThisC = ((TJava)TheSpec.Ref.getTheType()).getTheDataClass();
		
		if(ThisT == null) ThisT = TheSpec.Ref.getTheType();
		
		if(BySpec instanceof TSJava) {
			// Get the class of pSpec
			if(!BySpec.Ref.isLoaded()) {
				if(CL == null) CL = MClassPaths.getClassLoaderOf(pContext);
				TypeC = UClass.getClassByName(((TSJava)BySpec).getClassName(), CL);
			} else TypeC = ((TJava)BySpec.Ref.getTheType()).getTheDataClass();
			
		} else {

			// Possible circumstance
			// 1. This type is a Java interface and another type is an object that implement the interface
			
			Engine E = pEngine; 
			Type   T = BySpec.getTypeRef().getTheType();
			if(T == null) {
				if(E == null) E = pContext.getEngine();
				E.getTypeManager().ensureTypeExist(pContext, BySpec.getTypeRef());
				T = BySpec.getTypeRef().getTheType();
			}
			Class<?> C1 = UClass.getClassByName(((TSJava)TheSpec).getClassName());
			Class<?> C2 = T.getDataClass(pContext);
			
			// If this is assignable, return true
			if(C1.isAssignableFrom(C2)) return true;
			if(!C1.isInterface())       return false; // If this is not interface, no more investigation
			
			// Check the interface
			if((E == null) && ((E= pEngine) == null)) E = (pContext != null)?pContext.getEngine():T.getEngine();
			return TKInterface.checkIfInterfaceImplementedBy(pContext, E, T, ThisT, false, false);
		}
		return ThisC.isAssignableFrom(TypeC);
	}

	// Get Type -------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOf(Context pContext, Object pObj) {
		if(pObj == null) return TAny;
		return this.getTypeOfTheInstanceOf(pContext, pObj.getClass());
	}
	/**{@inheritDoc}*/ @Override
	protected Type getTypeOfTheInstanceOf(Context pContext, Class<?> pCls) {
		String ClsName = pCls.getCanonicalName(); if(ClsName == null) ClsName = pCls.getName();
		return this.getTypeByClass((pContext == null)?null:pContext.getEngine(), ClsName, pCls);
	}
	
	// Instantiation -------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected boolean isNeedInitialization() {
		return false;
	}
	
	/**{@inheritDoc}*/ @Override
	protected Object createNewTypeInstance(Context pContext, net.nawaman.curry.Executable pInitiator, Type pThisType,
			Object pSearchKey, Object[] pParams) {
		// Precondition
		if(pThisType == null) throw new NullPointerException();
		if(!(pThisType instanceof TJava))
			throw new CurryError("Internal Error: Wrong Type Kind ("+pThisType.getTypeKindName()+").(TKJava.java#238)", pContext);
				
		Class<?>       C     = ((TJava)pThisType).getDataClass();
		Constructor<?> Const = null;
		if(pSearchKey == null) Const = UClass.getConstructorByParams(C, pParams);
		else {
			// Prepare the search key
			Class<?>[] Cs = null;
			if(pSearchKey instanceof Type[]) {
				Cs = new Class<?>[((Type[])pSearchKey).length];
				for(int i = Cs.length; --i >= 0; ) {
					Type T = ((Type[])pSearchKey)[i];
					pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, T);
					Cs[i] = T.getDataClass();
					if(Cs[i] == null) Cs[i] = Object.class;
				}
			} else if(pSearchKey instanceof TypeRef[]) {
				Cs = new Class<?>[((TypeRef[])pSearchKey).length];
				for(int i = Cs.length; --i >= 0; ) {
					TypeRef TR = ((TypeRef[])pSearchKey)[i];
					pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, TR);
					Cs[i] = TR.getTheType().getDataClass();
					if(Cs[i] == null) Cs[i] = Object.class;
				}
			} else if(pSearchKey instanceof ExecInterface) {
				Cs = new Class<?>[((ExecInterface)pSearchKey).getParamCount()];
				for(int i = Cs.length; --i >= 0; ) {
					TypeRef TR = ((ExecInterface)pSearchKey).getParamTypeRef(i);
					pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, TR);
					Cs[i] = TR.getTheType().getDataClass();
					if(Cs[i] == null) Cs[i] = Object.class;
				}
			} else 
				throw new CurryError("Invalid constructor search key '"+pContext.getEngine().toString(pSearchKey)+"'.");
			// Get the constructor
			Const = UClass.getConstructorByParamClasses(C, Cs);
		}
		// Throw an error if not found
		if(Const == null)
			throw new CurryError("There is no such constructor "+pThisType
					+ UArray.toString((pSearchKey == null)?pParams:pSearchKey, "(", ")", ",")+".", pContext);
		
		// Actually run the constructor
		try {
			// TODO - Ensure this is what I want
			if(pParams != null) {
				for(int i = 0; i < pParams.length; i++) {
					if(pParams[i] instanceof DObject)
						pParams[i] = ((DObject)pParams[i]).getAsNative();
				}
			}
			return UClass.newInstance(Const, pParams);
		} catch(Exception E) {
			throw new CurryError("Instantiation Error: Error occurs while creating a new intance of "+
					pThisType.toString() +".(TKJava.java#462)", pContext, E);
		}
	}
	
	/**{@inheritDoc}*/ @Override
	protected boolean isTypeAbstract(Context pContext, Type pThisType) {
		// Precondition
		if(pThisType == null)             throw new NullPointerException();
		if(!(pThisType instanceof TJava)) {
			throw new CurryError("Internal Error: Wrong Type Kind ("+pThisType.getTypeKindName()+").(TKJava.java#256)", pContext);
		}
		Class<?> C = ((TJava)pThisType).getDataClass();
		return Modifier.isAbstract(C.getModifiers());
	}

	/**{@inheritDoc}*/ @Override
	protected Object getTypeDefaultValue(Context pContext, Type pThisType) {
		// Precondition
		if(pThisType == null)                                          throw new NullPointerException();
		if(pThisType.getTypeRef().equals(TKJava.TString.getTypeRef())) return "";
		
		if(!(pThisType instanceof TJava)) {
			throw new CurryError("Internal Error: Wrong Type Kind ("+pThisType.getTypeKindName()+").(TKJava.java#267)", pContext);
		}
		Class<?> C = ((TJava)pThisType).getDataClass();
		return (Serializable)UClass.getDefaultValue(C);
	}
	/**{@inheritDoc}*/ @Override
	protected Object getTypeNoNullDefaultValue(Context pContext, Type pTheType) {
		// TODO - Make it configurable by the EngineSpec
		if(pTheType == TKJava.TString)  return "";
		if(pTheType == TKJava.TClass)   return Object.class;
		if(pTheType == TKJava.TType)    return TKJava.TAny;
		if(pTheType == TKJava.TTypeRef) return TKJava.TAny.getTypeRef();
		
		return this.getTypeDefaultValue(pContext, pTheType);
	}
	
	/**{@inheritDoc}*/ @Override
	protected ConstructorInfo[] getConstructorInfos(Context pContext, Engine pEngine, Type pTheType) {
		// Precondition
		if(pTheType == null) throw new NullPointerException();
		if(!(pTheType instanceof TJava))
			throw new CurryError("Internal Error: Wrong Type Kind ("+pTheType.getTypeKindName()+").(TKJava.java#238)", pContext);
		
		if(this.isTypeAbstract(pContext, pTheType)) return null;
		
		if((pEngine == null) && (pContext != null)) pEngine = pContext.getEngine();
		
		Constructor<?>[] Cs = pTheType.getDataClass(pContext).getConstructors();
		
		Vector<ConstructorInfo> VInitializers = new Vector<ConstructorInfo>();
		
		for(Constructor<?> C : Cs)
			VInitializers.add(new ConstructorInfo.CINative(pEngine, Accessibility.Public, C, null));
		
		return VInitializers.toArray(new ConstructorInfo[VInitializers.size()]);
	}

	// Casting ----------------------------------------------------------------
	// Type to cast pData into the type pToType, return null if fail
	static public Object tryToCastTo(Object pData, Type pToType) {
		if(pData == null) {
			if(TKJava.TNumber.canBeAssignedByInstanceOf(pToType))    return tryToCastTo(0, pToType);
			if(TKJava.TBoolean.canBeAssignedByInstanceOf(pToType))   return false;
			if(TKJava.TCharacter.canBeAssignedByInstanceOf(pToType)) return (char)0;
			return null;
		}
		if(pToType == null)             return null;
		if(pToType.equals(TKJava.TAny)) return pData;
		
		Class<?> Cls = pToType.getDataClass();
		if((Cls == null) || (Cls == Object.class)) return null;
		// The same class so just return
		if(Cls == pData.getClass()) return pData;
		// Check if it can be casted
		if(Cls.isInstance(pData)) return Cls.cast(pData);
		// Some special operation
		// ToStirng
		if(Cls == String.class) return pData.toString();
		// Number
		if(Number.class.isAssignableFrom(Cls) || (Cls == Character.class)) {
			// Change char to int so it can be change to something else
			if(Character.class.isInstance(pData)) pData = (int)((Character)pData).charValue();
			
			// The data is not number, try to convert to number first
			if(!Number.class.isInstance(pData)) {
				if(Character.class.isInstance(pData)) {
					pData = (int)((Character)pData).charValue();
				}
			}
			// The data is number, just get the correct type of number
			if(Number.class.isInstance(pData)) {
				// Number to another number
				if(Cls ==   Integer.class) return       ((Number)pData).intValue();
				if(Cls ==    Double.class) return       ((Number)pData).doubleValue();
				if(Cls == Character.class) return (char)((Number)pData).intValue();
				if(Cls ==      Byte.class) return       ((Number)pData).byteValue();
				if(Cls ==      Long.class) return       ((Number)pData).longValue();
				if(Cls ==     Float.class) return       ((Number)pData).floatValue();
				if(Cls ==     Short.class) return       ((Number)pData).shortValue();
			}
		}
		
		if(UArray.isArrayType(Cls) && UArray.isArrayInstance(pData)) {
			try { return UArray.convertArray(pData, Cls, true); }
			catch (Exception e) {}
		}

		// For DObjectStandalone
		if(pToType.canBeAssignedBy(pData) && (pData instanceof DObjectStandalone) && (pToType instanceof TKJava.TJava)) {
			Class<?> C = ((TKJava.TJava)pToType).getDataClass();
			if(!C.isInstance(pData)) {
				pData = ((DObjectStandalone)pData).getAsNative();
				if(C.isInstance(pData)) return pData;
			}
		}
		
		return null;
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	// Other Classes ---------------------------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	// TypeSpec ------------------------------------------------------------------------------------
	
	// DataStructure
	// Datas: (String Alias, String ClassName, boolean IsPrimitive, IsLocked)
	/** Type spec of java type */
	static final public class TSJava extends TypeSpec {
		
        private static final long serialVersionUID = -1827779225647598914L;
        
        static final int Index_Alias       = 0;
		static final int Index_ClassName   = 1;
		static final int Index_Flag        = 2;
		
		static final int Index_Flag_IsPrimitive = 0;
		static final int Index_Flag_IsLocked    = 1;
		
		TSJava(String Alias, String ClassName, boolean IsPrimitive, boolean IsLocked) {
			super(new TLPrimitive.TRPrimitive(Alias, ClassName));
			this.Datas = new Serializable[] { Alias, ClassName, new boolean[] { IsPrimitive, IsLocked } };
		}
		
		/**{@inheritDoc}*/ @Override
		public String getKindName() {
			return TKJava.KindName;
		}
		
		/** Make this type a primitive */
		void setToPrimitive() {
			((boolean[])this.Datas[Index_Flag])[Index_Flag_IsPrimitive] = true;
		}
		/** Makes this type a locked one */
		void setToLocked() {
			((boolean[])this.Datas[Index_Flag])[Index_Flag_IsLocked] = true;
		}
		
		/** Returns the Alias of this type */
		String getAlias() {
			return (String)this.Datas[Index_Alias];
		}
		/** Returns the class name of the type */
		String getClassName() {
			return (String)this.Datas[Index_ClassName];
		}
		
		/** Checks if the type is primitive */
		boolean isPrimitive() {
			return ((boolean[])this.Datas[Index_Flag])[Index_Flag_IsPrimitive];
		}
		/** Chekcs if the type should not be accessible by curry */
		boolean isLocked() {
			return ((boolean[])this.Datas[Index_Flag])[Index_Flag_IsLocked];
		}

		/**{@inheritDoc}*/ @Override
		public TypeRef getSuperRef() {
			TJava T = (TJava)this.getTypeRef().getTheType();
			if (T == null) {
				System.err.println("T is null for `"+this.getTypeRef()+"`");
				return null;
			}
			
			if(T.DataClass == null)                            return null;
			if(T.DataClass.isArray())                          return null;
			if(T.DataClass == Object.class)                    return null;
			if(T.DataClass.getClass().getSuperclass() == null) return null;
			return TKJava.Instance.getTypeByClass(null, null, T.DataClass.getClass().getSuperclass()).getTypeRef();
		}
		
		/**{@inheritDoc}*/ @Override
		protected TypeRef[] getInterfaces() {
			String CN  = this.getClassName();
			if(CN == null) return null;
			Class<?> C = UClass.getClassByName(CN, MClassPaths.getClassLoaderOf(net.nawaman.curry.Engine.An_Engine));
			if(C == null) throw new CurryError("Interface is not found.", new ClassNotFoundException(CN));
			
			Class<?>[] Cls = C.getInterfaces();

			TypeRef[] TRs = new TypeRef[Cls.length];
			for(int i = Cls.length; --i >= 0; ) {
				Class<?> TheC = Cls[i];
				if(TheC == null) throw new CurryError("Interface is not found.", new ClassNotFoundException(CN));
				
				Type T = TKJava.Instance.getTypeByClass(null, null, TheC);
				if(T == null) return null;
				
				TRs[i] = T.getTypeRef();
			}

			return TRs;
		}

		// Serializable ---------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForCompilation() {}

		// Parameterization ------------------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void resetTypeSpecForParameterization() {}
		
		// Representation -------------------------------------------------------------------
			
		/**{@inheritDoc}*/ @Override
		protected String getToString() {
			// Get the name
			String Name = this.getAlias(); if(Name == null) Name = this.getClassName();
			
			// Simplify it
			if(Name.startsWith("java.lang.")) {
				String N = Name.substring("java.lang.".length());
				if(N.indexOf('.') == -1) return N;
			}
			if(Name.startsWith("net.nawaman.curry.")) {
				String N = Name.substring("net.nawaman.curry.".length());
				if(N.indexOf('.') == -1) return N;
			}
			
			return Name;
		}
	}
	
	// Type ----------------------------------------------------------------------------------------

	/** Type of Java Class */
	final public class TJava extends Type {
		
		/**  Constructs a new Java Type. **/
		TJava(Engine E, TypeSpec pTSpec) {
			super(TKJava.Instance, pTSpec);
			
			this.TSpec.Ref.setTheType(this);
			this.TSpec.TypeStatus = TypeSpec.Status.Initialized;
			this.DataClass        = UClass.getClassByName(
										this.getJavaSpec().getClassName(),
										MClassPaths.getClassLoaderOf(E));
		} // Java type is engine independent
		
		/** Returns the type factory in TFJava. */
		TKJava getTFJava() {
			return (TKJava)this.getTypeKind();
		}

		/** The Class Data */
		private Class<?> DataClass;
		/** Returns the Data Class that this type is pointing */
		public Class<?> getTheDataClass() {
			return this.DataClass;
		}

		/** Returns the spec in TSJava. */
		private TKJava.TSJava getJavaSpec() {
			return (TKJava.TSJava)this.getTypeSpec();
		}
		
		/** Returns the alias if this type or the class name if it has no alias */
		public String  getAlias() {
			String A = this.getJavaSpec().getAlias();
			return (A != null)?A:this.getTheDataClassName();
		}
		/** Returns the class name */
		public String  getTheDataClassName() { return this.getJavaSpec().getClassName(); }
		/** Checks if this type is primitive. */
		public boolean isPrimitive()         { return this.getJavaSpec().isPrimitive();  }
		/** Checks if this type is locked */
		public boolean isLocked()            { return this.getJavaSpec().isLocked();     }
	}
}