package net.nawaman.curry;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

import net.nawaman.curry.TKArray.TSArray;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UString;

/** Interface for execution in the form of `<return> name(<paramtype0>,<paramtype1>, ...)` */
final public class ExecSignature implements ExecInterface, HasSignature, HasLocation, Cloneable {
	
	static private final long serialVersionUID = -6583248189746116688L;
	
	/** The empty ExecSignature */
	static public final ExecSignature[] EmptyExecSignatureArray = new ExecSignature[0];
	
	// Constructor -----------------------------------------------------------------------------------------------------
	
	ExecSignature(String pName, ExecInterface pEI, Location pLocation, MoreData pExtraData) {
		this.Name      = pName;
		this.Interface = pEI;
		this.Location  = pLocation;
		this.ExtraData = pExtraData;
		
		if(this.ExtraData != null) this.ExtraData.toFreeze(); 
	}
	
	       String    Name;
	/** Returns the name of the signature */
	public String getName() {
		return this.Name;
	}
	
    	   Location    Location;
    /** Returns the name of the signature */
    public Location getLocation() { return this.Location; }
    
           MoreData    ExtraData = null;
    /** Returns the extra data of the signature */
    public MoreData getExtraData() { return (this.ExtraData == null)?MoreData.Empty:this.ExtraData; }

           ExecInterface    Interface; 
    /** Returns the interface of this signature */
    public ExecInterface getInterface() {
    	return this.Interface;
    }
    
    /**{@inheritDoc}*/@Override
    public ExecSignature getSignature() {
    	return this;
    }

    /**{@inheritDoc}*/@Override 
	public int getParamCount() {
    	return this.getInterface().getParamCount();
    }
    /**{@inheritDoc}*/@Override
	public TypeRef getParamTypeRef(int pPos) {
    	return this.getInterface().getParamTypeRef(pPos);
    }
    /**{@inheritDoc}*/@Override
	public TypeRef getLastVarArgParamTypeRef_As_NonArray() {
    	return this.getInterface().getLastVarArgParamTypeRef_As_NonArray();
    }
    /**{@inheritDoc}*/@Override
	public String  getParamName(int pPos) {
    	return this.getInterface().getParamName(pPos);
    }
	public boolean isVarArgs() {
		return this.getInterface().isVarArgs();
	}
	public TypeRef getReturnTypeRef() {
		return this.getInterface().getReturnTypeRef();
	}
	
	public String getLocationString() {
		return this.toString() + net.nawaman.curry.Location.ToString(this.getLocation()); 
	}
	
	/**{@inheritDoc}*/@Override
	public ExecSignature clone() {
		String[]  PNames = new String[ this.getParamCount()];
		TypeRef[] PTRefs = new TypeRef[this.getParamCount()];
		for(int i = this.getParamCount(); --i >= 0; ) {
			PNames[i] = this.getParamName(i);
			PTRefs[i] = this.getParamTypeRef(i).clone();
		}
		ExecSignature ES = ExecSignature.newSignature(
				this.getName(), PTRefs, PNames,
				this.isVarArgs(), this.getReturnTypeRef().clone(),
				(this.getLocation()  == null)?null:this.getLocation().clone(),
				(this.getExtraData() == null)?null:this.getExtraData().clone());
		return ES;
	}
	public ExecSignature clone(MoreData pExtraData) {
		String[]  PNames = new String[ this.getParamCount()];
		TypeRef[] PTRefs = new TypeRef[this.getParamCount()];
		for(int i = this.getParamCount(); --i >= 0; ) {
			PNames[i] = this.getParamName(i);
			PTRefs[i] = this.getParamTypeRef(i).clone();
		}
		ExecSignature ES = ExecSignature.newSignature(
				this.getName(), PTRefs, PNames,
				this.isVarArgs(), this.getReturnTypeRef().clone(),
				(this.getLocation()  == null)?null:this.getLocation().clone(),
				MoreData.combineMoreData(pExtraData, this.getExtraData()));
		return ES;
	}
	
	// Objectable -------------------------------------------------------------
	
	/** Returns the short string representation of the object. */
	public boolean is(Object O) {
		return (this == O);
	}
	/**{@inheritDoc}*/ @Override
	public boolean isAllParameterTypeEquals(Object O) {
		if(this == O) return true;
		if(O == null) return false;
		if(!(O instanceof ExecSignature)) return false;
		ExecSignature ES = (ExecSignature)O;
		if(!this.getName().equals(ES.getName())) return false;
		return this.Interface.isAllParameterTypeEquals(ES.getInterface());
	}
	/**{@inheritDoc}*/ @Override
	public boolean equivalents(Object O) {
		if(this == O) return true;
		if(O == null) return false;
		if(!(O instanceof ExecSignature)) return false;
		ExecSignature ES = (ExecSignature)O;
		if(!this.getName().equals(ES.getName())) return false;
		return this.Interface.equivalents(ES.getInterface());
	}
	/**{@inheritDoc}*/ @Override
	public boolean equals(Object O) {
		if(this == O) return true;
		if(O == null) return false;
		if(!(O instanceof ExecSignature)) return false;
		ExecSignature ES = (ExecSignature)O;
		if(!this.getName().equals(ES.getName())) return false;
		return this.Interface.equals(ES.getInterface());
	}
	/**{@inheritDoc}*/ @Override
	public int hash() {
		return UString.hash(this.Name) + this.getInterface().hash();
	}
	/** Calculates and returns the hash value of the exec-inteface. */
	public int hash_WithoutParamNamesReturnType() {
		return UString.hash(this.Name) + this.getInterface().hash_WithoutParamNamesReturnType();
	}
	/**{@inheritDoc}*/ @Override
	public int hashCode() {
		return this.hash_WithoutParamNamesReturnType();
	}
	/**{@inheritDoc}*/ @Override
	public String toString() {
		StringBuffer SB = new StringBuffer();
		SB.append(this.Name);
		SB.append(ExecInterface.Util.getParametersToString(this, false));
		SB.append(":");
		SB.append(this.getInterface().getReturnTypeRef().toString());
		return SB.toString();
	}
	/** Returns the integer representation of the object. */
	public String toDetail() {
		StringBuffer SB = new StringBuffer();
		SB.append(this.Name);
		SB.append(ExecInterface.Util.getParametersToDetail(this, false));
		SB.append(":");
		SB.append(this.getInterface().getReturnTypeRef().toDetail());
		return SB.toString();
	}
	
	// Utilities -----------------------------------------------------------------------------------

	static ExecSignature ES_ToString = null;
	static ExecSignature ES_ToDetail = null;
	static ExecSignature ES_Hash     = null;
	static ExecSignature ES_HashCode = null;
	static ExecSignature ES_Equals   = null;
	static ExecSignature ES_Is       = null;
	
	// Often Used Signatures -------------------------------------------------------------------------------------------
	
	/** Signature for toString() */
	static public final ExecSignature getES_ToString() {
		if(ES_ToString == null) ES_ToString = newProcedureSignature("toString", TKJava.TString.getTypeRef(),  null, null);
		return ES_ToString;
	}
	/** Signature for toDetail() */
	static public final ExecSignature getES_ToDetail() {
		if(ES_ToDetail == null) ES_ToDetail = newProcedureSignature("toDetail", TKJava.TString.getTypeRef(),  null, null);
		return ES_ToDetail;
	}
	/** Signature for hash() */
	static public final ExecSignature getES_Hash() {
		if(ES_Hash == null) ES_Hash = newProcedureSignature("hash", TKJava.TInteger.getTypeRef(), null, null);
		return ES_Hash;
	}
	/** Signature for hashCode() */
	static public final ExecSignature getES_HashCode() {
		if(ES_HashCode == null) ES_HashCode = newProcedureSignature("hashCode", TKJava.TInteger.getTypeRef(), null, null);
		return ES_HashCode;
	}

	/** Signature for equal(Object) */
	static public final ExecSignature getES_Equal() {
		if(ES_Equals == null)
			ES_Equals = ExecSignature.newSignature("equals",
					new TypeRef[] { new TLPrimitive.TRPrimitive("any", Object.class.getCanonicalName()) },
					new String[]  { "O" }, false, TKJava.TBoolean.getTypeRef(),  null, null);
		return ES_Equals;
	}
	/** Signature for is(Object) */
	static public final ExecSignature getES_Is() {
		if(ES_Is == null) ES_Is = ExecSignature.newSignature("is",
				new TypeRef[] { new TLPrimitive.TRPrimitive("any", Object.class.getCanonicalName()) },
				new String[] { "O" }, false, TKJava.TBoolean.getTypeRef(),  null, null);
		return ES_Is;
	}
	
	/** Constructs a new simple ExecSignature */
	static public ExecSignature newSignature(String pName, TypeRef pReturnType) {
		return newSignature(pName, TypeRef.EmptyTypeRefArray, UString.EmptyStringArray, false, pReturnType, null, null);
	}
	
	/** Constructs a new simple ExecSignature */
	static public ExecSignature newSignature(String pName, TypeRef pReturnType, Location pLocation, MoreData pExtraData) {
		return newSignature(pName, TypeRef.EmptyTypeRefArray, UString.EmptyStringArray, false, pReturnType, pLocation, pExtraData);
	}
	
	/** Constructs a new simple ExecSignature */
	static public ExecSignature newSignature(String pName, TypeRef[] pParamTypes, String[] pParamNames, TypeRef pReturnType) {
		return newSignature(pName, pParamTypes, pParamNames, false, pReturnType, null, null);
	}
	
	/** Constructs a new ExecSignature */
	static public ExecSignature newSignature(
			String   pName,      TypeRef[] pParamTypes, String[] pParamNames,
			boolean  pIsVarArgs, TypeRef  pReturnType,
			Location pLocation,  MoreData pExtraData) {
		
		// Name can be null but can't be empty
		if((pName != null) && (pName.length() == 0)) return null;
		
		ExecInterface EI = ExecInterface.Util.newInterface(pParamTypes, pParamNames, pIsVarArgs, pReturnType);
		if(EI == null) return null;
		ExecSignature ES = new ExecSignature(pName, EI, pLocation, pExtraData);
		return ES;
	}
	/** Constructs a new ExecSignature */
	static public ExecSignature newSignature(String pName, ExecInterface pInteface, Location pLocation,
			MoreData pExtraData) {
		// Name can be null but can't be empty
		if((pName != null) && (pName.length() == 0)) return null;
		
		ExecInterface EI = pInteface;
		if(EI == null) return null;
		if(pInteface instanceof ExecSignature) pInteface = ((ExecSignature)pInteface).Interface;
		ExecSignature ES = new ExecSignature(pName, EI, pLocation, pExtraData);
		return ES;
	}
	/** Create an empty execution signature */
	static public ExecSignature newEmptySignature(String pName, Location pLocation, MoreData pExtraData) {
		if(pName == null) return null;
		ExecInterface EI = new EInterface();
		return new ExecSignature(pName, EI, pLocation, pExtraData);
	}
	/** Create a procedure signature */
	static public ExecSignature newProcedureSignature(String pName, TypeRef pReturnTypeRef, Location pLocation,
			MoreData pExtraData) {
		if(pName == null) return null;
		return new ExecSignature(pName, new EInterface(pReturnTypeRef), pLocation, pExtraData);
	}
	/** Create a procedure signature */
	static public ExecSignature newProcedureSignature(String pName, TypeRef pReturnTypeRef) {
		if(pName == null) return null;
		return new ExecSignature(pName, new EInterface(pReturnTypeRef), null, null);
	}
	/** Create the interface of the given Java constructor */
	static public ExecSignature newSignature(Engine pEngine, Constructor<?> pConstructor) {
		return newSignature(pEngine, null, pConstructor);
	}
	/** Create the interface of the given Java constructor and replace the name with something else */
	static public ExecSignature newSignature(Engine pEngine, String pName, Constructor<?> pConstructor) {
		if(pConstructor == null) throw new NullPointerException();
		if(pEngine      == null) throw new NullPointerException();

		
		Class<?>[] ParamClasses = pConstructor.getParameterTypes();
		String[]   ParamNames = new String[ ParamClasses.length];
		TypeRef[]  ParamTypes = new TypeRef[ParamClasses.length];
		
		boolean IsVarArg = pConstructor.isVarArgs();
		
		int PCount = ParamTypes.length;
		int LIndex = PCount - 1;
		for(int i = ParamTypes.length; --i >= 0;) {
			Class<?> C = ParamClasses[i];
			ParamNames[i] = AutoParamNamePrifix + i;
			ParamTypes[i] = TKJava.Instance.getTypeByClass(pEngine, C.getCanonicalName(), C).getTypeRef();

			// If it is an array, extract them
			if(IsVarArg && (i == LIndex)) {
				TypeRef  TRef = ParamTypes[i];
				TypeSpec TS   = TRef.getTypeSpec(pEngine);
				if(TS instanceof TSArray) ParamTypes[i] = ((TSArray)TS).getContainTypeRef();
			}
		}
		
		EInterface EI = new EInterface(
					ParamTypes,
					ParamNames,
					IsVarArg,
					TKJava.TVoid.getTypeRef()
				);
		
		if(pName == null) pName = pConstructor.getName();
		return new ExecSignature(pName,
				EI,
				new Location(pConstructor.getDeclaringClass().getName().replace(".", File.separator) + ".class"), null);
	}
	
	/** Cache for Method Signatures */
	static WeakHashMap<Method, ExecSignature> MethodSignatures = new WeakHashMap<Method, ExecSignature>();
	
	/** Create the interface of the given Java method */
	static public ExecSignature newSignature(Engine pEngine, Method pMethod) {
		if(pMethod == null) throw new NullPointerException();
		
		ExecSignature ES = null;
		// Find in the cache
		if((ES = MethodSignatures.get(pMethod)) != null) return ES;
		
		TypeRef ReturnType = null;
		if(pMethod.getReturnType() == void.class) {
			ReturnType = TKJava.TVoid.getTypeRef();
		} else {
			Class<?> C = pMethod.getReturnType();
			ReturnType = TKJava.Instance.getTypeByClass(pEngine, C.getCanonicalName(), C).getTypeRef();
		}

		boolean IsVarArgs = pMethod.isVarArgs();
		
		Class<?>[] ParamClasses = pMethod.getParameterTypes();
		String[]   ParamNames = new String[ ParamClasses.length];
		TypeRef[]  ParamTypes = new TypeRef[ParamClasses.length];
		
		int PCount = ParamTypes.length;
		int LIndex = PCount - 1;
		for(int i = PCount; --i >= 0;) {
			Class<?> C = ParamClasses[i];
			ParamNames[i] = AutoParamNamePrifix + i;
			ParamTypes[i] = TKJava.Instance.getTypeByClass(null, C.getCanonicalName(), C).getTypeRef();
			
			// If it is an array, extract them
			if(IsVarArgs && (i == LIndex)) {
				TypeRef  TRef = ParamTypes[i];
				TypeSpec TS   = TRef.getTypeSpec(pEngine);
				if(TS instanceof TSArray) ParamTypes[i] = ((TSArray)TS).getContainTypeRef();
			}
		}

		ES = new ExecSignature(
				pMethod.getName(),
				new EInterface(ParamTypes, ParamNames, IsVarArgs, ReturnType),
				new Location(pMethod.getDeclaringClass().getName().replace(".", File.separator) + ".class"),
				null);
		
		MethodSignatures.put(pMethod, ES);
		return ES;
	}
	/** Create the interface for the action of reading the given Java method */
	static public ExecSignature newSignature_ReadField(Engine pEngine, Field pField) {
		if(pField  == null) throw new NullPointerException();
		if(pEngine == null) throw new NullPointerException();
		
		return new ExecSignature(pField.getName(),
				new EInterface(
					TypeRef.EmptyTypeRefArray,
					UString.EmptyStringArray,
					false,
					pEngine.getTypeManager().getTypeOfTheInstanceOf(pField.getType()).getTypeRef()
				),
				new Location(pField.getDeclaringClass().getName().replace(".", File.separator) + ".class"), null);
	}
	/** Create the interface for the action of writing the given Java method */
	static public ExecSignature newSignature_WriteField(Engine pEngine, Field pField) {
		if(pField  == null) throw new NullPointerException();
		if(pEngine == null) throw new NullPointerException();

		return new ExecSignature("set" + pField.getName().substring(0,1).toUpperCase() + pField.getName().substring(1),
				new EInterface(
					new TypeRef[] { pEngine.getTypeManager().getTypeOfTheInstanceOf(pField.getType()).getTypeRef() },
					new String[]  { AutoParamNamePrifix + 0 },
					false,
					TKJava.TVoid.getTypeRef()
				),
				new Location(pField.getDeclaringClass().getName().replace(".", File.separator) + ".class"), null);
	}
}
