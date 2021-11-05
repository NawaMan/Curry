package net.nawaman.curry.compiler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import net.nawaman.compiler.Code;
import net.nawaman.compiler.CodeFeeders;
import net.nawaman.curry.Context;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Instruction;
import net.nawaman.curry.Location;
import net.nawaman.curry.MType;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.Scope;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TKPackage;
import net.nawaman.curry.TLParameter;
import net.nawaman.curry.TLPrimitive;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.Instructions_Core.Inst_Type;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLCurrent.TRCurrent;
import net.nawaman.curry.TLParameter.TRParameter;
import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.curry.TLPrimitive.TRPrimitive;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.result.ParseResult;

/** Compile Product */
public class CompileProduct extends net.nawaman.compiler.CompileProduct {
	
	/** Name in MoreData for IsLocal:boolean */
	static public final String MDName_IsLocal = "IsLocal";
	
	/** Name in MoreData for IsTransparentDH:boolean */
	static public final String MDName_IsTransparentDH = "IsTransparentDH";

	protected CompileProduct(CurryCompiler pCCompiler, CodeFeeders pTheInput) {
		super(pTheInput);
		this.CCompiler = pCCompiler;
	}
	
	// General Services ----------------------------------------------------------------------------
	
	CurryCompiler CCompiler;

	/** Returns the curry language of this compilation */
	final public CurryLanguage getCurryLanguage() { return this.CCompiler.getCurryLanguage(); }
	/** Returns the curry compiler of this compilation */
	final public CurryCompiler getCurryCompiler() { return this.CCompiler; }
	/** Returns the target engine of this compilation */
	final public Engine        getEngine()        { return this.CCompiler.getEngine(); }
	
	// Compile time replacement for information and services ---------------------------------------
	
	/** Returns a type ref from a string name TName let the current package name is PName */
	public TypeRef getReturnTypeRefOf(Object O) {
		if(O == null)
			return null;
		
		if(!(O instanceof Expression))
			return this.getEngine().getTypeManager().getTypeOf(O).getTypeRef();
		
		TypeRef TRef = null;
		if(     ((Expression)O).isExpr())   TRef = TKJava.TExpression.getTypeRef();
		else if(((Expression)O).isData())   TRef = this.getEngine().getTypeManager().getTypeOf(((Expression)O).getData()).getTypeRef();
		else {
			Instruction I = this.getEngine().getInstruction(((Expression)O), ((Expression)O).getInstructionNameHash());
			if(I == null) return null;
			
			TRef = I.getReturnTypeRef(((Expression)O), this);
		}
		
		return TRef;
	}

	// Current structure ---------------------------------------------------------------------------
	
	public PackageBuilder getOwnerPackageBuilder() {
		if(this.OwnerPackageName == null) return null;
		return this.getEngine().getUnitManager().getPackageBuilder(null, this.OwnerPackageName);
	}
	
	protected boolean               IsOwnerObject    = false;
	protected TypeRef               OwnerTypeRef     = null;
	protected String                OwnerPackageName = null;
	protected ParameterizedTypeInfo PTInfo           = null;
	
	private boolean IsConstructor                       = false;
	private boolean SuperHaveDefaultConstructor         = false;
	private boolean HasCalledSuper                      = false;
	private boolean IsToAddDefaultConstructorRevokation = false;
	
	
	static Random TheRandom = new Random();
	private int TempVarNumber = Math.abs(TheRandom.nextInt(Short.MAX_VALUE));
	
	/** Checks if the owner is an object */
	public boolean isOwnerObject() {
		return this.IsOwnerObject;
	}
	/** Returns the type of the StackOwner */
	public TypeRef getOwnerTypeRef() {
		return this.OwnerTypeRef;
	}
	/** Returns the package of this stack */
	public String  getOwnerPackageName() {
		return this.OwnerPackageName;
	}
	
	/** Returns a number used for temporary variable */
	public int getTempVarNumber() {
		return this.TempVarNumber++;
	}
	
	/** Creates a Curry Compilation Option that allows StackOnwer information to be shared */
	public CurryCompilationOptions createOptionsForShareStackOwner(Object SecretID) {
		CurryCompilationOptions CCOptions = new CurryCompilationOptions();
		if(!this.CCompiler.isMacthID(SecretID)) return CCOptions;
		
		CCOptions.CFName        = this.getCurrentFeederName();
		CCOptions.IsOwnerObject = this.isOwnerObject();
		CCOptions.OwnerTypeRef  = this.getOwnerTypeRef();
		CCOptions.OwnerPackage  = this.getOwnerPackageName();
		return CCOptions;
	}

	/** Is the compilation is currently in a constructor */
	public boolean isConstructor() {
		return this.IsConstructor;
	}
	/** Is this is in a constructor and default constructor of the type exist */
	public boolean doesSuperHaveDefaultConstructor() {
		return this.SuperHaveDefaultConstructor;
	}
	/** Is this is in a constructor and super constructor have been called. */
	public boolean hasCalledSuper() {
		return this.HasCalledSuper;
	}
	/** Is this is in a constructor and a default constructor should be added to the construtor body. */
	boolean isToAddDefaultConstructorRevocation() {
		return this.IsToAddDefaultConstructorRevokation;
	}
	
	/** Notify the context that there is an access to a current type's element */
	public boolean notifyAccessingElement(Expression pExpr) {
		if(!this.IsConstructor || this.HasCalledSuper) return true;
		if(!this.SuperHaveDefaultConstructor) {
			this.reportError(
					"Super constructor must be called before an element can be accessed <CompileProduct:138>",
					null, this.getPosition(pExpr));
			return false;
		}
		// If the super type have a default constructor, the first access to this type element will autotically access 
		// to refer to this default constructor
		this.IsToAddDefaultConstructorRevokation = true;
		this.HasCalledSuper                      = true;
		return true;
	}
	
	/** Notify the context that there is an access to a super constructor */
	public boolean notifyInvokeSuperConstructor(Expression pExpr) {
		if(!this.IsConstructor) {
			this.reportError(
				"Super/This constructor can only be invoked from a constructor <CompileProduct:153>",
				null, this.getPosition(pExpr));
			return false;
		}
		if(this.HasCalledSuper) {
			this.reportError(
				"Super/This constructor can only be invoked once from a constructor <CompileProduct:159>",
				null, this.getPosition(pExpr));
			return false;
		}
		this.HasCalledSuper = true;
		return true;
	}
	
	// Simulating scope ------------------------------------------------------------------------------------------------
	
	// Variable created in this fake scope will always be typed as TypeRef and the value is always the type ref of that
	//    variable type
	
	/** Scope information */
	static public class ScopeInfo {
		String                    Name;
		Scope                     Scope;
		TypeRef                   ReturnTypeRef;
		boolean                   IsLoop;
		boolean                   IsSwitch;
		Set<String>               Imports;
		HashMap<String, MoreData> VarMoreData;
		
		ScopeInfo(String pName, Scope pScope, TypeRef pReturnTypeRef, boolean pIsLoop, boolean pIsSwitch) {
			this.Name          = pName;
			this.Scope         = pScope;
			this.ReturnTypeRef = pReturnTypeRef;
			this.IsLoop        = pIsLoop;
			this.IsSwitch      = pIsSwitch;
			
			if(this.ReturnTypeRef == null) this.ReturnTypeRef = TKJava.TAny.getTypeRef();
		}
		
		/** Returns the name of the scope */
		public String getName() {
			return this.Name;
		}
		/** Returns the scopr */
		public Scope getScope() {
			return this.Scope;
		}
		/** Checks if this scope is a loop */
		public boolean isLoop() {
			return this.IsLoop;
		}
		/** Checks if this scope is a scwitch */
		public boolean isSwitch() {
			return this.IsSwitch;
		}
		/** Checks if this crope is a macro */
		public boolean isMacro() {
			return false;
		}
		/** Returns the return TypeRef of the scope */
		public TypeRef getReturnTypeRef() {
			return this.ReturnTypeRef;
		}
		/** Returns MoreData object for the given variable name */
		public MoreData getVariableMoreData(String VName, boolean IsToCreate) {
			if(!this.Scope.isLocalVariableExist(VName)) return null;
			
			// No to create, so return as is
			if(!IsToCreate) return (this.VarMoreData == null) ? null : this.VarMoreData.get(VName);
			// Enusre that MoreData object for the variable exists
			if(this.VarMoreData == null) this.VarMoreData = new HashMap<String, MoreData>();
			MoreData MD = this.VarMoreData.get(VName);
			if(MD == null) this.VarMoreData.put(VName, MD = new MoreData());
			return MD;
		}
	}
	
	/** Scope information for internal macro */
	static public final class MacroScopeInfo extends ScopeInfo {
		MacroScopeInfo(ExecSignature pMacroSignature, Scope pScope) {
			super(
				pMacroSignature.getName(),
				pScope,
				pMacroSignature.getReturnTypeRef(),
				false,
				false
			);
			this.Signature = pMacroSignature;
		}
		@Override public boolean isMacro() { return true; }
		
		ExecSignature Signature = null;
		/** Returns the current signature */
		public ExecSignature getSignature() {
			return this.Signature;
		}
	}
	
	protected Vector<ScopeInfo>          SInfos        = new Vector<ScopeInfo>();
	protected ExecSignature              Signature     = null;
	protected Executable.ExecKind        ExecKind      = null;
	protected Hashtable<String, TypeRef> Params        = null;   
	protected Hashtable<String, TypeRef> Frozens       = null;
	
	/** An actual scope for global scope */
	Scope RealGlobalScope = null;
	/** An actual top level scope */
	Scope TopScope        = null;
	
	/** A simulated scope for global scope */
	Scope GlobalScope = null;
	Scope getGlobalScope() {
		if(this.GlobalScope == null) this.GlobalScope = new Scope();
		return this.GlobalScope;
	}
	
	/** Clear all the scope */
	public void clearScope() {
		this.SInfos.clear();
		this.Imports = null;
		this.IsToUpdateImports = false;
	}
	
	public ScopeInfo getCurrentScopeInfo() {
		if(this.SInfos.size() == 0) return null;
		return this.SInfos.get(this.SInfos.size() - 1);
	}
	
	/** Returns the current scope */
	Scope getCurrentScope() {
		if(this.SInfos.size() == 0) return null;
		return this.SInfos.get(this.SInfos.size() - 1).getScope();
	}
	
	/** Returns the execution kind of this scope */
	public Executable.ExecKind getExecKind() {
		return this.ExecKind;
	}
	
	// Variable --------------------------------------------------------------------------------------------------------

	/** Creats a local variable */
	public boolean newVariable(String pName, TypeRef pTRref) {
		Scope S = this.getCurrentScope();
		if(S == null) return false;
		if(S.isLocalVariableExist(pName)) return false;
		S.newVariable(pName, TKJava.TTypeRef, pTRref);
		return true;
	}
	/** Creats a local constant */
	public boolean newConstant(String pName, TypeRef pTRref) {
		Scope S = this.getCurrentScope();
		if(S == null) return false;
		if(this.getCurrentScope().isLocalVariableExist(pName)) return false;
		this.getCurrentScope().newConstant(pName, TKJava.TTypeRef, pTRref);
		return true;
	}
	
	/** Returns the type of the variable */
	public TypeRef getVariableTypeRef(String pName) {
		if("this".equals(pName) || Context.StackOwner_VarName.equals(pName)) {
			if(!this.IsOwnerObject) {
				Type T = this.getEngine().getTypeManager().getTypeOf(this.getTypeAtCompileTime(this.getOwnerTypeRef()));
				return (T == null) ? TKJava.TAny.getTypeRef() : T.getTypeRef();
			}
			return this.getOwnerTypeRef();
		}

		if(Context.StackOwnerAsType_VarName       .equals(pName)) return this.getOwnerTypeRef();
		if(Context.StackOwnerAsCurrentType_VarName.equals(pName)) return this.getOwnerTypeRef();

		if(Context.StackOwnerAsPackage_VarName.equals(pName)) {
			return TKPackage.newTypeTypeRef(this.getOwnerPackageName());
		}
		
		if((this.Frozens != null) && (this.Frozens.get(pName) != null))
			return this.Frozens.get(pName);
		
		Scope S = null;
		if(((S = this.getCurrentScope()) != null) && S.isVariableExist(pName))          return (TypeRef)S.getValue(this.getEngine(), pName);
		if((this.Params                  != null) && (this.Params.get(pName)  != null)) return this.Params.get(pName);
		if(((S = this.TopScope)          != null) && S.isVariableExist(pName))          return S.getType(this.getEngine(), pName).getTypeRef();
		if(((S = this.GlobalScope)       != null) && S.isVariableExist(pName))          return S.getType(this.getEngine(), pName).getTypeRef();
		return this.isCompileTimeCheckingFull()?null:TKJava.TAny.getTypeRef();
	}
	
	/** Checks if a variable exists */
	public boolean isVariableExist(String pName) {
		if("this"                                 .equals(pName)) return true;
		if(Context.StackOwner_VarName             .equals(pName)) return true;
		if(Context.StackOwnerAsType_VarName       .equals(pName)) return true;
		if(Context.StackOwnerAsPackage_VarName    .equals(pName)) return true;
		if(Context.StackOwnerAsCurrentType_VarName.equals(pName)) return true; 
		
		if((this.Frozens != null) && (this.Frozens.get(pName) != null))
			return true;
		
		Scope S = null;
		if(((S = this.getCurrentScope()) != null) && S.isVariableExist(pName))          return true;
		if((this.Params                  != null) && (this.Params.get(pName)  != null)) return true;
		if(((S = this.TopScope)          != null) && S.isVariableExist(pName))          return true;
		if(((S = this.GlobalScope)       != null) && S.isVariableExist(pName))          return true;
		return false;
	}
	
	/** Checks if a variable exist in the current scope */
	public boolean isLocalVariableExist(String pName) {
		// Check the frozen variable first
		if((this.Frozens != null) && (this.Frozens.get(pName) != null)) return true;
		// Check the current scope
		Scope S = this.getCurrentScope();
		if(S == null) return false;
		return S.isLocalVariableExist(pName);
	}
	
	/** Checks if the variable is a constant */
	public boolean isConstant(String pName) {
		Scope S = null;
		if(((S = this.getCurrentScope()) != null) && S.isVariableExist(pName)) return S.isVariableConstant(pName);
		if(((S = this.TopScope)          != null) && S.isVariableExist(pName)) return S.isVariableConstant(pName);
		return false;
	}
	/** Returns MoreData object for the given variable name */
	public MoreData getVariableMoreData(String VName, boolean IsToCreate) {
		if((VName == null) || (VName.length() == 0)) return null;

		int Count = this.SInfos.size();
		for(int i = Count; --i >= 0; ) {
			ScopeInfo SI = this.SInfos.get(i);
			Scope     S  = SI.getScope();
			if(!S.isLocalVariableExist(VName))
				continue;
			
			return SI.getVariableMoreData(VName, IsToCreate);
		}
		// TODO - Think what to do with TopScope
		return null;
	}
	
	// Parent ----------------------------------------------------------------------------------------------------------
	
	/** Returns the TypeRef of a parent variable */
	public TypeRef getParentVariableTypeRef(int pCount, String pName) {
		if(pName != null) {
			int Count = pCount;
			if(Count >= 0) {
				if(Count == 0) return this.getVariableTypeRef(pName);

				if(Count >= this.SInfos.size()) {
					if(Count == this.SInfos.size()) {
						// From TopScope
						if((this.TopScope != null) && this.TopScope.isVariableExist(pName))
							return this.TopScope.getType(this.getEngine(), pName).getTypeRef();
					}
				} else {
					ScopeInfo SI = this.SInfos.get(this.SInfos.size() - Count - 1);
					if(SI != null) {
						Scope S = SI.getScope();
						if(S != null) {
							Object O = S.getValue(this.getEngine(), pName);
							if(O instanceof TypeRef) return (TypeRef)O;
						}
					}
				}
			}
		}
		return this.isCompileTimeCheckingFull()?null:TKJava.TAny.getTypeRef();
	}
	/** Checks if a parent variable exist */
	public boolean isParentVariableExist(int pCount, String pName) {
		int Count = pCount;
		if(Count < 0) return false;
		if(Count == 0) return this.isVariableExist(pName);
		
		if(Count >= this.SInfos.size()) {
			if(Count == this.SInfos.size()) {
				// From TopScope
				if((this.TopScope != null) && this.TopScope.isVariableExist(pName))
					return true;
			}
			return false;
		}
		
		ScopeInfo SI = this.SInfos.get(this.SInfos.size() - Count - 1);
		if(SI == null) return false;
		Scope S = SI.getScope();
		if(S == null) return false;
		return S.isVariableExist(pName);
	}
	/** Checks if a parent variable is a constant */
	public boolean isParentVariableConstant(int pCount, String VName) {
		if((VName == null) || (VName.length() == 0)) return false;
		
		int Count = pCount;
		if(Count <  0) return false;
		if(Count == 0) return this.isConstant(VName);
		
		if(Count >= this.SInfos.size()) {
			if(Count == this.SInfos.size()) {
				// From TopScope
				if((this.TopScope != null) && this.TopScope.isVariableExist(VName))
					return this.TopScope.isVariableConstant(VName);
			}
			return false;
		}
		
		ScopeInfo SI = this.SInfos.get(this.SInfos.size() - Count - 1);
		if(SI == null) return false;
		
		Scope S = SI.getScope();
		if(S == null) return false;
		
		return S.isVariableConstant(VName);
	}
	/** Returns MoreData object for the given parent variable name */
	public MoreData getParentVariableMoreData(int pCount, String VName, boolean IsToCreate) {
		int Count = pCount;
		if(Count <  0) return null;
		if(Count == 0) return this.getVariableMoreData(VName, IsToCreate);
		
		if((VName == null) || (VName.length() == 0)) return null;
		
		if(Count >= this.SInfos.size())
			// TODO - Think what to do with TopScope
			return null;
		
		for(int i = Count; --i >= 0; ) {
			ScopeInfo SI = this.SInfos.get(i);
			Scope     S  = SI.getScope();
			if(!S.isLocalVariableExist(VName))
				continue;
			
			return SI.getVariableMoreData(VName, IsToCreate);
		}
		return null;
	}
	
	/** Returns the index of parent stack by its name */
	int getParentIndex(String pStackName) {
		if(pStackName == null) return 0;
		for(int i = this.SInfos.size(); --i >= 0; ) {
			ScopeInfo SI = this.SInfos.get(i);
			if(SI == null) continue;
			if(!pStackName.equals(SI.getName())) continue;
			return (this.SInfos.size() - 1) - i;
		}
		return -1;
	}

	/** Returns the TypeRef of a parent variable */
	public TypeRef getParentVariableTypeRef(String pStackName, String pName) {
		return this.getParentVariableTypeRef(this.getParentIndex(pStackName), pName);
	}
	/** Checks if a parent variable exist */
	public boolean isParentVariableExist(String pStackName, String pName) {
		return this.isParentVariableExist(this.getParentIndex(pStackName), pName);
	}
	/** Checks if a parent variable is a constant */
	public boolean isParentVariableConstant(String pStackName, String pName) {
		return this.isParentVariableConstant(this.getParentIndex(pStackName), pName);
	}
	/** Returns MoreData object for the given parent variable name */
	public MoreData getParentVariableMoreData(String pStackName, String pName, boolean IsToCreate) {
		return this.getParentVariableMoreData(this.getParentIndex(pStackName), pName, IsToCreate);
	}
	
	// Global ----------------------------------------------------------------------------------------------------------
	
	/** Creates a new Global variable */
	public TypeRef newGlobalVariable(String pName, TypeRef pTRef, boolean pIsConstant) {
		if(pName == null) return TKJava.TType.getTypeRef();
		Scope S = null;
		// real global variable
		if((S = this.RealGlobalScope) != null) {
			// Check in the real global scope
			if(S.isLocalVariableExist(pName)) return this.getGlobalVariableTypeRef(pName);
			// But created in the simulated scope
			if(!pIsConstant) this.getGlobalScope().newVariable(pName, TKJava.TTypeRef, pTRef);
			else             this.getGlobalScope().newConstant(pName, TKJava.TTypeRef, pTRef);
			return pTRef;
		}
		// Simulated global variable
		if((S = this.getGlobalScope()) != null) {
			if(S.isLocalVariableExist(pName)) return this.getGlobalVariableTypeRef(pName);
			if(!pIsConstant) this.getGlobalScope().newVariable(pName, TKJava.TTypeRef, pTRef);
			else             this.getGlobalScope().newConstant(pName, TKJava.TTypeRef, pTRef);
			return pTRef;
		}
		return this.getGlobalVariableTypeRef(pName);
	}
	/** Returns the type of a global variable */
	public TypeRef getGlobalVariableTypeRef(String pName) {
		Scope S = null;
		if(((S = this.RealGlobalScope)  != null) && S.isVariableExist(pName)) return S.getType(this.getEngine(), pName).getTypeRef();
		if(((S = this.getGlobalScope()) != null) && S.isVariableExist(pName)) return (TypeRef)S.getValue(this.getEngine(), pName);
		return this.isCompileTimeCheckingFull()?null:TKJava.TAny.getTypeRef();
	}
	/** Checks if a global variable exist */
	public boolean isGlobalVariableExist(String pName) {
		Scope S = null;
		if(((S = this.RealGlobalScope)  != null) && S.isVariableExist(pName)) return true;
		if(((S = this.getGlobalScope()) != null) && S.isVariableExist(pName)) return true;
		return false;
	}
	/** Checks if a global variable is a constant */
	public boolean isGlobalVariableConstant(String pName) {
		Scope S = null;
		if(((S = this.RealGlobalScope)  != null) && S.isVariableExist(pName)) return S.isVariableConstant(pName);
		if(((S = this.getGlobalScope()) != null) && S.isVariableExist(pName)) return S.isVariableConstant(pName);
		return false;
	}
	/** Returns MoreData object for the given global variable name */
	public MoreData getGlobalVariableMoreData(String pStackName, String pName, boolean IsToCreate) {
		// TODO - Do something
		return null;
	}
	
	// Engine ----------------------------------------------------------------------------------------------------------
	
	/** Returns the TypeRef of an engine variable */
	public TypeRef getEngineVariableTypeRef(String pName) {
		if(this.getEngine().getEngineScope().isVariableExist(pName)) {
			Type T = this.getEngine().getEngineScope().getType(this.getEngine(), pName);
			if(T != null) return T.getTypeRef();
		}
		return this.isCompileTimeCheckingFull()?null:TKJava.TAny.getTypeRef();
	}
	/** Checks if an engine variable exist */
	public boolean isEngineVariableExist(String pName) {
		return this.getEngine().getEngineScope().isVariableExist(pName);
	}
	/** Checks if an engine variable is a constant */
	public boolean isEngineVariableConstant(String pName) {
		return this.getEngine().getEngineScope().isVariableConstant(pName);
	}
	/** Returns MoreData object for the given engine variable name */
	public MoreData getEngineVariableMoreData(String pStackName, String pName, boolean IsToCreate) {
		// TODO - Do something
		return null;
	}
	
	// Reset context --------------------------------------------------------------------------------------------------- 
	
	// NOTE - This reserved for those who construct the curry compiler
	
	/** Create a new root scope */
	public boolean clearContext(Object pCompilerSecretID) {
		// Checks if the caller have the authority to do so
		if(!this.CCompiler.isMacthID(pCompilerSecretID)) return false;
		
		this.RealGlobalScope = null;
		this.TopScope        = null;
		
		this.clearScope();
		this.ExecKind         = null;
		this.IsOwnerObject    = false;
		this.OwnerTypeRef     = null;
		this.OwnerPackageName = null;
		this.Checking         = this.getResetValueCompileTimeChecking();
		
		this.IsConstructor                       = false;
		this.SuperHaveDefaultConstructor         = false;
		this.HasCalledSuper                      = false;
		this.IsToAddDefaultConstructorRevokation = false;
		
		return true;
	}
	
	void resetForExecutable(ExecSignature pSignature, Scope pGlobalScope, Scope pTopScope, String[] pFVNames) {
		this.Params = null;
		if((pSignature != null) && (pSignature.getParamCount() != 0)) {
			this.Params = new Hashtable<String, TypeRef>();
			for(int i = pSignature.getParamCount(); --i >= 0; )
				this.Params.put(pSignature.getParamName(i), pSignature.getParamTypeRef(i));
		}
		this.Frozens = null;
		if(((pGlobalScope != null) || (pTopScope != null)) && (pFVNames != null) && (pFVNames.length != 0)) {
			this.Frozens = new Hashtable<String, TypeRef>();
			for(int i = pFVNames.length; --i >= 0; ) {
				String FVName = pFVNames[i];
				if((FVName == null) || (FVName.length() == 0)) continue;
				Type          T = (pTopScope    == null)?null:pTopScope.   getType(this.getEngine(), FVName);
				if(T == null) T = (pGlobalScope == null)?null:pGlobalScope.getType(this.getEngine(), FVName);
				this.Frozens.put(FVName, (T == null)?TKJava.TAny.getTypeRef():T.getTypeRef());
			}
			if(this.Frozens.size() == 0) this.Frozens = null;
		}
	}
	
	/** Create a new root scope */
	public Scope resetContextForFragment(Object pCompilerSecretID, TypeRef pReturnTypeRef, boolean pIsOwnerObject,
			TypeRef pOwnerTypeRef, String pOwnerPackageName, Scope pGlobalScope, Scope pTopScope, String[] pFVNames,
			boolean pIsLocal) {
		
		// Checks if the caller have the authority to do so
		if(!this.CCompiler.isMacthID(pCompilerSecretID)) return null;
		
		this.clearContext(pCompilerSecretID);
		
		this.RealGlobalScope = pGlobalScope;
		this.TopScope        = pIsLocal?pTopScope:null;
		Scope NewScope = new Scope();
		this.SInfos.add(
				new ScopeInfo(
					null, NewScope,
					(pReturnTypeRef == null)?TKJava.TAny.getTypeRef():pReturnTypeRef,
					false, false));
		this.ExecKind         = Executable.ExecKind.Fragment;
		this.IsOwnerObject    = pIsOwnerObject;
		this.OwnerTypeRef     = pOwnerTypeRef;
		this.OwnerPackageName = pOwnerPackageName;
		this.Checking         = this.getResetValueCompileTimeChecking();
		
		this.resetForExecutable(null, pGlobalScope, pTopScope, pFVNames);
		
		this.toStopTry(false);
		return NewScope;
	}
	
	/** Create a new root scope */
	Scope resetContextForConstructor(Object pCompilerSecretID, ExecSignature pSignature, TypeRef pOwnerTypeRef,
			String pOwnerPackageName, boolean pDoesSuperHaveDefaultConstructor) {
		Scope S = this.resetContextForMacro(
				pCompilerSecretID, pSignature, true, pOwnerTypeRef, pOwnerPackageName, null, null, null, false);
		this.IsConstructor               = true;
		this.SuperHaveDefaultConstructor = pDoesSuperHaveDefaultConstructor;
		return S;
	}
	
	/** Create a new root scope */
	public Scope resetContextForMacro(Object pCompilerSecretID, ExecSignature pSignature, boolean pIsOwnerObject,
			TypeRef pOwnerTypeRef, String pOwnerPackageName, Scope pGlobalScope, Scope pTopScope, String[] pFVNames,
			boolean pIsLocal) {
		
		// Checks if the caller have the authority to do so
		if(!this.CCompiler.isMacthID(pCompilerSecretID)) return null;
		
		this.clearContext(pCompilerSecretID);
		
		this.RealGlobalScope = pGlobalScope;
		this.TopScope        = pIsLocal?pTopScope:null;
		
		Scope NewScope = new Scope();
		this.SInfos.add(new MacroScopeInfo(pSignature, NewScope));
		this.ExecKind         = Executable.ExecKind.Macro;
		this.IsOwnerObject    = pIsOwnerObject;
		this.OwnerTypeRef     = pOwnerTypeRef;
		this.OwnerPackageName = pOwnerPackageName;
		this.Checking         = this.getResetValueCompileTimeChecking();
		
		this.resetForExecutable(pSignature, pGlobalScope, pTopScope, pFVNames);
		
		this.toStopTry(false);
		return NewScope;
	}
	
	/** Create a new root scope */
	public Scope resetContextForSubRoutine(Object pCompilerSecretID, ExecSignature pSignature, boolean pIsOwnerObject,
			TypeRef pOwnerTypeRef, String pOwnerPackageName, Scope pGlobalScope, Scope pTopScope, String[] pFVNames) {
		
		// Checks if the caller have the authority to do so
		if(!this.CCompiler.isMacthID(pCompilerSecretID)) return null;
		
		this.clearContext(pCompilerSecretID);
		
		this.RealGlobalScope = pGlobalScope;
		this.TopScope        = null;
		
		Scope NewScope = new Scope();
		this.SInfos.add(new ScopeInfo(pSignature.getName(), NewScope, pSignature.getReturnTypeRef(), false, false));
		this.ExecKind         = Executable.ExecKind.SubRoutine;
		this.Signature        = pSignature;
		this.IsOwnerObject    = pIsOwnerObject;
		this.OwnerTypeRef     = pOwnerTypeRef;
		this.OwnerPackageName = pOwnerPackageName;
		this.Checking         = this.getCompileTimeChecking();
		
		this.resetForExecutable(pSignature, pGlobalScope, pTopScope, pFVNames);
		
		this.toStopTry(false);
		return NewScope;
	}
	
	// New Scope -------------------------------------------------------------------------------------------------------
	
	/** Creates a new scope that is depend to the current scope */
	public Scope newScope(String pName, TypeRef pReturnType) {
		if(this.SInfos.size() == 0) return null;
		Scope NewScope = new Scope(this.getCurrentScope());
		this.SInfos.add(new ScopeInfo(pName, NewScope, pReturnType, false, false));
		return NewScope;
	}
	/** Creates a new scope for a loop */
	public Scope newLoopScope(String pName, TypeRef pReturnType) {
		if(this.SInfos.size() == 0) return null;
		Scope NewScope = new Scope(this.getCurrentScope());
		this.SInfos.add(new ScopeInfo(pName, NewScope, pReturnType, true, false));
		return NewScope;
	}
	/** Creates a new scope for a switch */
	public Scope newSwitchScope(String pName, TypeRef pReturnType) {
		if(this.SInfos.size() == 0) return null;
		Scope NewScope = new Scope(this.getCurrentScope());
		this.SInfos.add(new ScopeInfo(pName, NewScope, pReturnType, false, true));
		return NewScope;
	}
	/** Creates a new macro scope */
	public Scope newMacroScope(ExecSignature pSignature) {
		if(this.SInfos.size() ==    0) return null;
		if(pSignature         == null) return null;
		Scope NewScope = new Scope(this.getCurrentScope());
		this.SInfos.add(new MacroScopeInfo(pSignature, NewScope));
		
		// Prepare parameter
		for(int i = pSignature.getParamCount(); --i >= 0; )
			NewScope.newVariable(pSignature.getParamName(i), TKJava.TTypeRef, pSignature.getParamTypeRef(i));

		return NewScope;
	}

	/** Checks if there is a scope with the given name */
	public boolean hasScopeNamed(String pName) {
		for(int i = this.SInfos.size(); --i >= 0; ) {
			String SName = this.SInfos.get(i).getName();
			if((pName == SName) || (pName == null) || ((pName != null) && (pName.equals(SName)))) return true;
		}
		return false;
	}
	
	// Information -----------------------------------------------------------------------------------------------------
	
	/** Returns the closest macro signature */
	public ExecSignature getClosestMacroSignature() {
		if(this.SInfos.size() == 0) return null;
		for(int i = this.SInfos.size(); --i >= 0; ) {
			ScopeInfo SI = this.SInfos.get(i);
			// Found a macro or the root of the scope
			if(SI.isMacro()) return ((MacroScopeInfo)SI).getSignature();
		}
		if(this.ExecKind.isMacro()) return this.Signature;
		return null;
	}
	
	/** Returns the closest sub-routine signature */
	public ExecSignature getSubRoutineSignature() {
		if(this.SInfos.size() == 0) return null;
		if(this.ExecKind.isSubRoutine()) return this.Signature;
		return null;
	}
	
	/** Returns the closest macro signature */
	public ExecSignature getClosestSignature() {
		if(this.SInfos.size() == 0) return null;
		for(int i = this.SInfos.size(); --i >= 0; ) {
			ScopeInfo SI = this.SInfos.get(i);
			// Found a macro or the root of the scope
			if(SI.isMacro()) return ((MacroScopeInfo)SI).getSignature();
		}
		if(this.ExecKind.isSubRoutine()) return this.Signature;
		return null;
	}
	
	/** Checks if the current scope is not under a macro or sub-routine */
	public boolean isFragment() {
		if(this.SInfos.size() == 0)      return false;
		for(int i = this.SInfos.size(); --i >= 0; ) {
			ScopeInfo SI = this.SInfos.get(i);
			// Found a macro or the root of the scope
			if(SI.isMacro()) return false;
		}
		if(this.ExecKind.isMacro())      return false;
		if(this.ExecKind.isSubRoutine()) return false;
		return true;
	}
	/** Checks if the current scope is under an macro */
	public boolean isMacro() {
		if(this.SInfos.size() == 0) return false;
		for(int i = this.SInfos.size(); --i >= 0; ) {
			ScopeInfo SI = this.SInfos.get(i);
			// Found a macro or the root of the scope
			if(SI.isMacro()) return true;
		}
		if(this.ExecKind.isMacro()) return true;
		return false;
	}
	/** Checks if the current scope is under a sub-routine */
	public boolean isSubRoutine() {
		if(this.SInfos.size() == 0) return false;
		for(int i = this.SInfos.size(); --i >= 0; ) {
			ScopeInfo SI = this.SInfos.get(i);
			// Found a macro or the root of the scope
			if(SI.isMacro()) return false;
		}
		if(this.ExecKind.isSubRoutine()) return true;
		return false;
	}
	
	// Out of the loop -------------------------------------------------------------------------------------------------
	
	/** Check if the current stack, well, is a stack */
	public boolean isInsideScope() {
		if(this.SInfos.size() == 0) return false;
		ScopeInfo SI = this.SInfos.get(this.SInfos.size() - 1);
		if(SI == null) return false;
		return true;
	}
	
	/** Check if the current stack, well, is a stack */
	public boolean isInsideScope(String pName) {
		if(this.SInfos.size() == 0) return false;
		for(int i = this.SInfos.size(); --i >= 0; ) {
			String SName = this.SInfos.get(i).getName();
			if((pName == SName) || (pName == null) || ((pName != null) && (pName.equals(SName)))) return true;
		}
		return false;
	}
	/** Exit a scope */
	public boolean canExitScope(String pName, Object pReturnValue) {
		TypeRef ValueTypeRef = (pReturnValue == null)?null:this.getReturnTypeRefOf(pReturnValue);
		
		if(this.SInfos.size() == 0) return false;
		for(int i = this.SInfos.size(); --i >= 0; ) {
			String SName = this.SInfos.get(i).getName();
			if((pName == SName) || (pName == null) || ((pName != null) && (pName.equals(SName)))) {
				TypeRef TR = this.SInfos.get(i).getReturnTypeRef();
				if(TR != null) {
					try {
						Type T = this.getTypeAtCompileTime(TR);
						if(T.equals(TKJava.TAny)) return true;
					} catch(Exception E) { return false; }
				}
				if(ValueTypeRef != null) {
					try {
						Type T = this.getTypeAtCompileTime(TR);
						if(T.getTypeInfo().canBeAssignedByInstanceOf(this.getTypeAtCompileTime(ValueTypeRef))) return true;
					} catch(Exception E) { return false; }
				}
				return true;
			}
		}
		return false;
	}
	/** Exit a scope */
	public boolean exitScope() {
		if(this.SInfos.size() == 0) return false;

		int I = this.SInfos.size() - 1;
		// Mark to update the import
		if((this.SInfos.get(I).Imports != null) && (this.SInfos.get(I).Imports.size() != 0))
			this.IsToUpdateImports = true;
		// Remove the stack
		this.SInfos.remove(I);
		return true;
	}

	/** Check if the current stack, well, is a stack */
	public boolean isInsideMacroOrSubRoutine() {
		return (this.isMacro() || this.isSubRoutine());
	}
	/** Returns from an sub-routine and macro (whichever closer) scope */
	public boolean canReturnExecutableScope(Object pReturnValue) {
		TypeRef ValueTypeRef = (pReturnValue == null)?null:this.getReturnTypeRefOf(pReturnValue);
		
		for(int i = this.SInfos.size(); --i >= 0; ) {
			ScopeInfo SI = this.SInfos.get(i);
			// Found a macro or the root of the scope
			if(SI.isMacro() || (i == 0)) {
				TypeRef TR = this.SInfos.get(i).getReturnTypeRef();
				Type    T  = null; 
				if(TR != null) {
					try {
						T = this.getTypeAtCompileTime(TR);
						if(T.equals(TKJava.TAny))  return true;
						if(T.equals(TKJava.TVoid)) return (pReturnValue == null);
					} catch(Exception E) { return false; }
				}
				if(ValueTypeRef != null) {
					try { return T.getTypeInfo().canBeAssignedByInstanceOf(this.getTypeAtCompileTime(ValueTypeRef)); }
					catch(Exception E) { return false; }
				}
				return true;
			}
		}
		return false;
	}

	/** Checks if the current stack is in a switch */
	public boolean isInsideSwitchScope() {
		for(int i = this.SInfos.size(); --i >= 0; ) { if(this.SInfos.get(i).isSwitch()) return true; }
		return false;
	}
	/** Checks if the current stack is in a switch */
	public boolean isInsideSwitchScope(String pName) {
		for(int i = this.SInfos.size(); --i >= 0; ) {
			String SName = this.SInfos.get(i).getName();
			if(!this.SInfos.get(i).isSwitch()) continue;
			if((pName == SName) || (pName == null) || ((pName != null) && (pName.equals(SName)))) return true;
		}
		return false;
	}
	/** Finish and go out of a switch scope */
	public boolean canDoneSwitchScope(String pName, Object pReturnValue) {
		TypeRef ValueTypeRef = (pReturnValue == null)?null:this.getReturnTypeRefOf(pReturnValue);
		
		for(int i = this.SInfos.size(); --i >= 0; ) {
			String SName = this.SInfos.get(i).getName();
			if(!this.SInfos.get(i).isSwitch()) continue;
			if((pName == SName) || (pName == null) || ((pName != null) && (pName.equals(SName)))) {
				TypeRef TR = this.SInfos.get(i).getReturnTypeRef();
				Type    T  = null;
				if(TR != null) {
					try {
						T = this.getTypeAtCompileTime(TR);
						if(T.equals(TKJava.TAny)) return true;
					} catch(Exception E) { return false; }
				}
				if(ValueTypeRef != null) {
					try { return T.getTypeInfo().canBeAssignedByInstanceOf(this.getTypeAtCompileTime(ValueTypeRef)); }
					catch(Exception E) { return false; }
				}
				return true;
			}
		}
		return false;
	}
	/** Finish and go out of a switch scope */
	public boolean doneSwitchScope() {
		for(int i = this.SInfos.size(); --i >= 0; ) {
			if(!this.SInfos.get(i).isSwitch()) continue;
			for(int j = this.SInfos.size(); --j >= i;) {
				ScopeInfo SI = this.SInfos.get(j);
				// Mark to update the import
				if((SI.Imports != null) && (SI.Imports.size() != 0))
					this.IsToUpdateImports = true;
				// Remove the stack
				this.SInfos.remove(j);
			}
			return true;
		}
		return false;
	}

	/** Checks if the current stack is in a loop */
	public boolean isInsideLoopScope() {
		for(int i = this.SInfos.size(); --i >= 0; ) { if(this.SInfos.get(i).isLoop()) return true; }
		return false;
	}
	/** Checks if the current stack is in a loop */
	public boolean isInsideLoopScope(String pName) {
		for(int i = this.SInfos.size(); --i >= 0; ) {
			String SName = this.SInfos.get(i).getName();
			if(!this.SInfos.get(i).isLoop()) continue;
			if((pName == SName) || (pName == null) || ((pName != null) && (pName.equals(SName)))) return true;
		}
		return false;
	}
	
	/** Check if continue loop can be done here */
	public boolean canContinueLoopScope(String pName, Object pReturnValue) {
		//TypeRef ValueTypeRef = (pReturnValue == null)?null:this.getReturnTypeRefOf(pReturnValue);
		
		for(int i = this.SInfos.size(); --i >= 0; ) {
			String SName = this.SInfos.get(i).getName();
			if(!this.SInfos.get(i).isLoop()) continue;
			if((pName == SName) || (pName == null) || ((pName != null) && (pName.equals(SName)))) {
				/* TODO: Not sure what type should be checking against (for the moment) 
				TypeRef TR = this.SInfos.get(i).getReturnTypeRef();
				Type    T  = null;
				if(TR != null) {
					try {
						T = this.getTypeAtCompileTime(TR);
						if(T.equals(TKJava.TAny)) return true;
					} catch(Exception E) { return false; }
				}
				if(ValueTypeRef != null) {
					try { return T.getTypeInfo().canBeAssignedByInstanceOf(this.getTypeAtCompileTime(ValueTypeRef)); }
					catch(Exception E) { return false; }
				}*/
				return true;
			}
		}
		return false;
	}
	
	/** Finish and go out of a switch scope */
	public boolean canStopLoopScope(String pName, Object pReturnValue) {
		TypeRef ValueTypeRef = (pReturnValue == null)?null:this.getReturnTypeRefOf(pReturnValue);
		
		for(int i = this.SInfos.size(); --i >= 0; ) {
			String SName = this.SInfos.get(i).getName();
			if(!this.SInfos.get(i).isLoop()) continue;
			if((pName == SName) || (pName == null) || ((pName != null) && (pName.equals(SName)))) {
				TypeRef TR = this.SInfos.get(i).getReturnTypeRef();
				Type    T  = null;
				if(TR != null) {
					try {
						T = this.getTypeAtCompileTime(TR);
						if(T.equals(TKJava.TAny)) return true;
					} catch(Exception E) { return false; }
				}
				if(ValueTypeRef != null) {
					try { return T.getTypeInfo().canBeAssignedByInstanceOf(this.getTypeAtCompileTime(ValueTypeRef)); }
					catch(Exception E) { return false; }
				}
				return true;
			}
		}
		return false;
	}
	
	/** Finish and go out of a switch scope */
	public boolean stopLoopScope() {
		for(int i = this.SInfos.size(); --i >= 0; ) {
			if(!this.SInfos.get(i).isLoop()) continue;
			for(int j = this.SInfos.size(); --j >= i;) {
				ScopeInfo SI = this.SInfos.get(j);
				// Mark to update the import
				if((SI.Imports != null) && (SI.Imports.size() != 0))
					this.IsToUpdateImports = true;
				// Remove the stack
				this.SInfos.remove(j);
			}
			return true;
		}
		return false;
	}
	
	// Import and Types ----------------------------------------------------------------------------
	
	private boolean        IsToUpdateImports = false;
	private Vector<String> Imports           = null;
	
	protected List<String> getImportList() {
		if(!this.IsToUpdateImports) return this.Imports;
		if(this.SInfos == null) return null;
		HashSet<String> ImportSet = new HashSet<String>();
		for(int i = this.SInfos.size(); --i >= 0; ){
			ScopeInfo SI = this.SInfos.get(i);
			if((SI == null) || (SI.Imports == null) || (SI.Imports.size() == 0)) continue;
			ImportSet.addAll(SI.Imports);
		}
		if(ImportSet.size() == 0) return null;
		this.Imports = new Vector<String>(ImportSet);
		this.IsToUpdateImports = false;
		return this.Imports;
	}
	
	public String[] getImports() {
		List<String> Is = this.getImportList();
		return (Is == null)?null:Is.toArray(new String[Is.size()]);
	}
	
	/** Add import to the current stack */
	public void addImport(String ... pImports) {
		if(pImports == null) return;
		ScopeInfo SI = this.getCurrentScopeInfo();
		if(SI == null) {
			if(this.Imports == null) this.Imports = new Vector<String>();
			this.Imports.addAll(Arrays.asList(pImports));
			return;
		}
		// Add it in to the set
		for(String Import : pImports) {
			if((this.Imports == null) || !this.Imports.contains(Import)) {
				if(SI.Imports == null) SI.Imports = new HashSet<String>();
				SI.Imports.add(Import);
				this.IsToUpdateImports = true;
			}
		}
	}
	
	/** Use the given ParameterizedTypeInfo when finding a TypeRef */
	public void useParameterizedTypeInfos(ParameterizedTypeInfo pPTInfo) {
		this.PTInfo = pPTInfo;
	}

	/** Disable the current ParameterizedTypeInfo */
	public void clearParameterizedTypeInfos() {
		this.PTInfo = null;
	}
	
	/** Returns a type ref from a string representation of it (type name only) */
	public TypeRef getTypeRefFromString(String TName) {
		// Early returns
		if((TName == null) || ((TName = TName.trim()).length() == 0))
			return null;

		// Ensure paraterized-type information
		if(this.PTInfo == null) {
			try {
				Type     T  = this.getTypeAtCompileTime(this.getOwnerTypeRef());
				TypeSpec TS = (T != null) ? T.getTypeSpec() : null;
				if((TS != null) && TS.isParameterized())
					this.PTInfo = TS.getParameterizedTypeInfo();
			} catch (Exception e) {}
		}

		// Parameterized Type		
		if((this.PTInfo != null) && (this.PTInfo.getParameterTypeRef(TName) != null)) {
			TypeRef OTypeRef = this.getOwnerTypeRef();
			
			if(OTypeRef == null) return new TLParameter.TRParameter(TName);
			else                 return new TLParameter.TRParameter(OTypeRef, TName);
		}
		
		MType        MT  = this.getEngine().getTypeManager();
		List<String> Is  = this.getImportList();
		TypeRef      OTR = this.getOwnerTypeRef();
		String       OPN = this.getOwnerPackageName();
		// Ask the TypeManager to search for it
		return MT.searchTypeRef(Is, OPN, OTR, TName);
	}
	/** Try the type at compile time. The private type of the current package can be access via this method */
	public Type getTypeAtCompileTime(TypeRef pTRef) {
		return this.getTypeAtCompileTime(pTRef, false);
	}
	/** Try the type at compile time. The private type of the current package can be access via this method */
	public Type getTypeAtCompileTime(TypeRef pTRef, boolean IsForceInitialized) {
		if(pTRef == null) return null;

		Type TheType = null;
		if(!pTRef.isLoaded()) {
			if(pTRef instanceof TRPrimitive)
				return TLPrimitive.getTypeFromTRPrimitive(this.getEngine(), (TRPrimitive)pTRef);
			
			if(pTRef instanceof TRParameter) {
				String PName = ((TRParameter)pTRef).getParameterName();
				
				// Use base if exists
				TypeRef BTRef;
				if((BTRef = ((TRParameter)pTRef).getBaseTypeRef()) != null) {
					TypeSpec TS = BTRef.getTypeSpec(getEngine());
					TypeRef  TR;
					if((TS != null) && ((TR = TS.getParameterTypeRef(PName)) != null)) {
						Type T = this.getTypeAtCompileTime(TR);
						if(T != null) return T;
					}
				}
				
				// Use the owner typeref
				Type ThisType = this.getTypeAtCompileTime(this.getOwnerTypeRef());
				if(ThisType == null) ThisType = this.getTypeAtCompileTime(this.getOwnerTypeRef());
				
				TypeRef TR = ThisType.getTypeInfo().getTypeSpec().getParameterTypeRef(PName);
				return this.getTypeAtCompileTime(TR);
			}
			
			if(pTRef instanceof TRCurrent)
				return this.getTypeAtCompileTime(this.getOwnerTypeRef());
			
			// Ensure all parameter types are resolved here (Engine may not know the type)
			if(pTRef instanceof TRParametered) {
				TRParametered TRP = (TRParametered)pTRef;
				Type TT = this.getTypeAtCompileTime(TRP.getTargetTypeRef(), IsForceInitialized);
				TypeRef[] TRs = new TypeRef[TRP.getParameterCount()];
				for(int i = TRs.length; --i >= 0; ) {
					Type T = this.getTypeAtCompileTime(TRP.getParameterTypeRef(i), IsForceInitialized);
					if(T == null) continue;
					TRs[i] = T.getTypeRef();
				}
				
				pTRef = new TRParametered(this.getEngine(), (TT == null) ? TRP.getTargetTypeRef() : TT.getTypeRef(), TRs);
			}
			
			if(pTRef instanceof TypeTypeRef) {
				TypeRef OldInside = ((TypeTypeRef)pTRef).getTheRef();
				Type T = this.getTypeAtCompileTime(OldInside);
				TypeRef Inside = (T == null) ? TKJava.TAny.getTypeRef() : T.getTypeRef();
				if(OldInside != Inside) pTRef = new TypeTypeRef(Inside);
			}
		}
		
		// Type to get the type so we can create the frozen scope with the type name.
		try {
			PackageBuilder PBuilder = this.getOwnerPackageBuilder();
			if(PBuilder != null)
				return PBuilder.tryToGetTypeAtCompileTime(this, pTRef, IsForceInitialized);
		} catch(Throwable T) {}
		
		// This is the last resource.
		try { TheType = (Type)this.getEngine().execute(Inst_Type.Name, pTRef); }
		catch (Throwable T) {}
		
		if((TheType != null) && IsForceInitialized) {
			this.getEngine().getTypeManager().ensureTypeInitialized(
					pTRef,
					(pTRef instanceof TRBasedOnType)
						? ((TRBasedOnType)pTRef).getBaseTypeRef()
						: null);
		}
		
		return TheType;
	}
	
	// Compile time checking -----------------------------------------------------------------------
	
	/** The level of the compile time checking */
	static public enum CompileTimeChecking {
		// Report as error when an incompatible parameter type is found
		Full,
		// No check so no report
		None;
				
		public boolean isFull() { return this == Full; }
		public boolean isNone() { return this == None; }
	}
	
	private CompileTimeChecking Checking = CompileTimeChecking.Full;
	
	/** Returns the reset value of the compile-time type checking flag' */
	public CompileTimeChecking getResetValueCompileTimeChecking() {
		return this.CCompiler.getResetValueCompileTimeChecking();
	}
	/** Checks if the compile-time expression parameter-type checking */
	public CompileTimeChecking getCompileTimeChecking() {
		return this.Checking;
	}
	/** Changes the the compile-time expression parameter-type checking */
	public CompileTimeChecking setCompileTimeChecking(CompileTimeChecking pChecking) {
		return (this.Checking = pChecking);
	}
	
	/** Checks if the compile checking in full */
	public boolean isCompileTimeCheckingFull() {
		return this.getCompileTimeChecking().isFull();
	}
	/** Checks if the compiler will not do any checking */
	public boolean isCompileTimeCheckingNone() {
		return this.getCompileTimeChecking().isNone();
	}
	
	// Error Report ----------------------------------------------------------------------------------------------------
	
	/** Temporary message kind */
	static private enum  TMKind { Message, Warning, Error, FatalError }
	/** Temporary message */
	static private class TempMessage {
		TempMessage(TMKind pKind, String pMessage, Throwable pCause, int pPosition) {
			this.Kind = pKind; this.Message = pMessage; this.Cause = pCause; this.Position = pPosition;
		}
		
		TMKind    Kind     = null;
		String    Message  = null;
		Throwable Cause    = null;
		int       Position = -1;
	}
	/** Messages container */
	static private class TryStatus {
		boolean             isFoundProblem = false;
		CompileTimeChecking SavedCC        = null;
		Vector<TempMessage> TMessages      = new Vector<CompileProduct.TempMessage>();
	}
	
	private boolean   isTryChecking = false;
	private TryStatus TryStatus   = null;
	
	/** Start trying */
	public boolean toStartTry() {
		if(this.isTryChecking) return false;
		this.isTryChecking            = true;
		this.TryStatus                = new TryStatus();
		this.TryStatus.isFoundProblem = false;
		this.TryStatus.SavedCC        = this.getCompileTimeChecking();
		this.setCompileTimeChecking(CompileTimeChecking.Full);
		return true;
	}
	
	/** Save the try state and start trying another one */
	public Object toReStartTry() {
		if(!this.isTryChecking) return false;
		Object Saved = this.TryStatus;
		this.toStopTry(false);
		return Saved;
	}
	
	/** Save the try state and start trying another one */
	public boolean toRestoreTry(Object pSaved) {
		if(!this.isTryChecking)           return false;
		if((pSaved instanceof TryStatus)) return false;
		this.TryStatus = (TryStatus)pSaved;
		if(this.TryStatus == null) this.TryStatus = new TryStatus();
		return true;
	}
	
	/** Stop the trying */
	public boolean toStopTry(boolean pIsAddTryMessageBack) {
		if(!this.isTryChecking) return false;
		this.isTryChecking = false;
		
		if(this.TryStatus == null) return true;
		this.TryStatus.isFoundProblem = false;
		
		this.setCompileTimeChecking(
				(this.TryStatus.SavedCC == null)?this.getResetValueCompileTimeChecking():this.TryStatus.SavedCC);
		if(pIsAddTryMessageBack && (this.TryStatus != null) && (this.TryStatus.TMessages != null)) {
			for(int i = 0; i < this.TryStatus.TMessages.size(); i++) {
				TempMessage TM = this.TryStatus.TMessages.get(i);
				if(TM == null) continue;
				switch(TM.Kind) {
					case Message:    this.reportMessage(   TM.Message, TM.Cause, TM.Position); break;
					case Warning:    this.reportWarning(   TM.Message, TM.Cause, TM.Position); break;
					case Error:      this.reportError(     TM.Message, TM.Cause, TM.Position); break;
					case FatalError: this.reportFatalError(TM.Message, TM.Cause, TM.Position); break;
				}
			}
		}
		return true;
	}
	
	/** Checks if there is a problem or problems while trying */
	public boolean isProblemFoundWhileTrying() {
		return this.TryStatus.isFoundProblem;
	}

	/** Do the report (ensure it redirect to the trying collection) */
	protected void report(TMKind pKind, String pMessage, Throwable pCause, int pPos) {
		if(this.isTryChecking) {
			this.TryStatus.isFoundProblem = true;
			if((this.TryStatus != null) && (this.TryStatus.TMessages != null))
				this.TryStatus.TMessages.add(new TempMessage(pKind, pMessage, pCause, pPos));
		} else {
			switch (pKind) {
				case Message:    super.reportMessage(   pMessage, pCause, pPos); break;
				case Warning:    super.reportWarning(   pMessage, pCause, pPos); break;
				case Error:      super.reportError(     pMessage, pCause, pPos); break;
				case FatalError: super.reportFatalError(pMessage, pCause, pPos); break;
			}
		}
	}

	/** Do the report (ensure it redirect to the trying collection) */
	protected void report(TMKind pKind, String pMessage, Throwable pCause, int pCol, int pRow) {
		int Pos = -1;
		if((pCol == -1) && (pRow == -1)) Pos = -1;
		else {
			if(pCol <= -1) pCol = 0;
			if(pRow <= -1) pRow = 0;
			Code C = this.getCurrentCode();
			Pos = (C == null)?-1:C.getNearestValidPositionOf(pCol, pRow);
		}
		this.report(pKind, pMessage, pCause, Pos);
	}

	/**{@inheritDoc}*/ @Override public void reportMessage(   String pMessage, Throwable pCause)                     { this.report(TMKind.Message,    pMessage, pCause,   -1);       }
	/**{@inheritDoc}*/ @Override public void reportWarning(   String pMessage, Throwable pCause)                     { this.report(TMKind.Warning,    pMessage, pCause,   -1);       }
	/**{@inheritDoc}*/ @Override public void reportError(     String pMessage, Throwable pCause)                     { this.report(TMKind.Error,      pMessage, pCause,   -1);       }
	/**{@inheritDoc}*/ @Override public void reportFatalError(String pMessage, Throwable pCause)                     { this.report(TMKind.FatalError, pMessage, pCause,   -1);       }
	
	/**{@inheritDoc}*/ @Override public void reportMessage(   String pMessage, Throwable pCause, int pPos)           { this.report(TMKind.Message,    pMessage, pCause, pPos);       }
	/**{@inheritDoc}*/ @Override public void reportWarning(   String pMessage, Throwable pCause, int pPos)           { this.report(TMKind.Warning,    pMessage, pCause, pPos);       }
	/**{@inheritDoc}*/ @Override public void reportError(     String pMessage, Throwable pCause, int pPos)           { this.report(TMKind.Error,      pMessage, pCause, pPos);       }
	/**{@inheritDoc}*/ @Override public void reportFatalError(String pMessage, Throwable pCause, int pPos)           { this.report(TMKind.FatalError, pMessage, pCause, pPos);       }
	
	/**{@inheritDoc}*/ @Override public void reportMessage(   String pMessage, Throwable pCause, int pCol, int pRow) { this.report(TMKind.Message,    pMessage, pCause, pCol, pRow); }
	/**{@inheritDoc}*/ @Override public void reportWarning(   String pMessage, Throwable pCause, int pCol, int pRow) { this.report(TMKind.Warning,    pMessage, pCause, pCol, pRow); }
	/**{@inheritDoc}*/ @Override public void reportError(     String pMessage, Throwable pCause, int pCol, int pRow) { this.report(TMKind.Error,      pMessage, pCause, pCol, pRow); }
	/**{@inheritDoc}*/ @Override public void reportFatalError(String pMessage, Throwable pCause, int pCol, int pRow) { this.report(TMKind.FatalError, pMessage, pCause, pCol, pRow); }

	/** Returns the Current Curry location using Row+Col */
	public Location getCurrentLocation(ParseResult $Result) {
		if($Result == null) return null;
		return this.getCurrentLocation($Result.locationCROf(0));
	}
	/** Returns the Current Curry location using Row+Col */
	public Location getCurrentLocation(int[] CR) {
		if(CR == null) return null;
		return new Location(this.getCurrentFeederName(), this.getCurrentCodeName(), CR);
	}
	
	// Compile time state for file compilation -------------------------------------------------------------------------
	
	/** State of the compilation */
	static public enum CompilationState {
		// Normal compilation state
		Normal,
		// The time when types are registered (type ref is created)
		TypeRegistration,
		// The time when types are re-declared with access to other type (in case there is relationship)
		TypeRefinition,
		// The time when types' elements are declared (type spec is created)
		StructuralRegistration,
		// The time when types' elements are declared (type spec is created)
		TypeValidation,
		// The time when types' content are actually compiled.
		FullCompilation;

		public boolean isNormal()                 { return this == Normal;                 }
		public boolean isTypeRegistration()       { return this == TypeRegistration;       }
		public boolean isTypeRefinition()         { return this == TypeRefinition;         }
		public boolean isStructuralRegistration() { return this == StructuralRegistration; }
		public boolean isTypeValidation()         { return this == TypeValidation;         }
		public boolean isFullCompilation()        { return this == FullCompilation;        }
	}
	
	private CompilationState State = CompilationState.Normal;

	/** Changes the state of the compilation */
	public CompilationState setCompilationState(CompilationState pState, Object pCompilerSecretID) {
		// Checks if the caller have the authority to do so
		if(!this.CCompiler.isMacthID(pCompilerSecretID)) return null;
		
		return (this.State = pState);
	}
	/** Returns the current compilation state */
	public CompilationState getCompilationState() {
		if(this.State == null) this.State = CompilationState.Normal;
		return this.State;
	}

	public int getPosition(Expression pExpr) {
		int LN = pExpr.getLineNumber();
		int CL = pExpr.getColumn();
		return this.getPosition(CL, LN);
	}
	public int getPosition(int pColumn, int pLineNumber) {
		if((pLineNumber != -1) && (pColumn == -1)) pColumn = 0;
		return ((pLineNumber == -1) || (pColumn == -1)) ? -1 :
				this.getCurrentCode().getNearestValidPositionOf(pColumn, pLineNumber);
	}
	
	// Advance feature -------------------------------------------------------------------------------------------------
	
	/** Do something with another owner typeref */
	public void doWithAnotherOwnerTypeRef(TypeRef NewOwnerTypeRef, Runnable Runner) {
		if(Runner == null) return;

		// Save the old one
		TypeRef OldOwnerTypeRef = this.getOwnerTypeRef();
		
		try {
			// Use the new one
			this.OwnerTypeRef = NewOwnerTypeRef;
			// Run
			Runner.run();
		} finally {
			// Change back
			this.OwnerTypeRef = OldOwnerTypeRef;
		}
		
	}
	
	
	
}
