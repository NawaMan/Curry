package net.nawaman.curry;

import net.nawaman.curry.Context.ContextStackOwner;
import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.MoreData;

final public class ExternalContext {

	// General -----------------------------------------------------------------

	/** Returns the engine of that this context is operating on */
	static public Engine getEngine(Context pContext) {
		return pContext.getEngine();
	}

	// Type --------------------------------------------------------------------

	/** Ensure that the type referred by pTypeRef exists */
	static public void ensureTypeExist(Context pContext, TypeRef TR) {
		pContext.getEngine().getTypeManager().ensureTypeExist(pContext, TR);
	}

	/** Ensure that the type referred by pTypeRef is loaded */
	static public void ensureTypeValidated(Context pContext, TypeRef TR, TypeRef pParameterBaseTypeToIgnore) {
		pContext.getEngine().getTypeManager().ensureTypeValidated(pContext, TR, pParameterBaseTypeToIgnore);
	}

	/** Ensure that the type is initialized */
	static public void ensureTypeInitialized(Context pContext, Type pType) {
		pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, pType);
	}

	/** Ensure that the type referred by pTypeRef is initialized */
	static public void ensureTypeInitialized(Context pContext, TypeRef pTypeRef, TypeRef pParameterBaseTypeToIgnore) {
		pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, pTypeRef, pParameterBaseTypeToIgnore);
	}
	
	/** Ensure that the type referred by pTypeRef is initialized */
	static public void ensureTypeInitialized(Context pContext, TypeRef pTypeRef) {
		pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, pTypeRef, null);
	}

	/** Returns the type from the ref */
	static public Type getTypeFromRef(Context pContext, TypeRef TR) {
		return pContext.getEngine().getTypeManager().getTypeFromRef(pContext, TR);
	}

	// Execute -----------------------------------------------------------------
	/** Create and run an expression */
	static public Object run(Context pContext, String pInstName, Object... Params) {
		Instruction Inst = pContext.getEngine().getInstruction(pInstName);
		Expression Expr = Inst.newExpression(Params);
		if (Expr == null)
			return null;
		Object R = pContext.getExecutor().execInternal(pContext, Expr);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Create and run an expression */
	static public Object run(Context pContext, String pInstName, Object[] Params,
			Expression... Body) {
		Instruction Inst = pContext.getEngine().getInstruction(pInstName);
		Expression Expr = Inst.newExprSubs(Params, Body);
		if (Expr == null)
			return null;
		Object R = pContext.getExecutor().execInternal(pContext, Expr);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an expression in this context. */
	static public Object execute(Context pContext, Expression pExpr) {
		Object R = pContext.getExecutor().execInternal(pContext, pExpr);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Run an executable as a fragment in this context. */
	static public Object runFragment(Context pContext, Executable pExec) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec,
				ExecKind.Fragment, false, null, null, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an executable as a macro in this context. */
	static public Object execMacro(Context pContext, Executable pExec, Object ... Params) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec, ExecKind.Macro,
				false, null, Params, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an executable as a macro in this context. */
	static public Object execMacro(Context pContext, Executable pExec, boolean IsBlindCaller,
			Object ... Params) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec, ExecKind.Macro,
				IsBlindCaller, null, Params, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an executable as a sub-routine in this context. */
	static public Object callSubRoutine(Context pContext, Executable pExec, Object ... Params) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec,
				ExecKind.SubRoutine, false, null, Params, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an executable as a sub-routine in this context. */
	static public Object callSubRoutine(Context pContext, Executable pExec, boolean IsBlindCaller,
			Object ... Params) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec,
				ExecKind.SubRoutine, IsBlindCaller, null, Params, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Checks if an object is a special result and should not process. */
	static public boolean isSpecialResult(Context pContext, Object O) {
		return O instanceof SpecialResult;
	}
	
	// Context info ------------------------------------------------------------
	
	static public String getName(Context pContext) {
		return pContext.getName();
	}

	// Variable access ---------------------------------------------------------

	// Local ---------------------------------------------------------
	/** Create a new variable */
	static public Object newVariable(Context pContext, String pVName, Type pType, Object pDefaultValue) {
		return pContext.newVariable(pContext.getEngine(), pVName, pType, pDefaultValue);
	}

	/** Create a new constant */
	static public Object newConstant(Context pContext, String pVName, Type pType, Object pDefaultValue) {
		return pContext.newConstant(pContext.getEngine(), pVName, pType, pDefaultValue);
	}

	/** Set a value to a local variable. */
	static public Object setVarValue(Context pContext, String pVName, Object pValue) {
		return pContext.setVariableValue(pVName, pValue);
	}

	/** Get a value of a local variable. */
	static public Object getVarValue(Context pContext, String pVName) {
		return pContext.getVariableValue(pVName);
	}

	/** Check if the variable named pName exist */
	static public boolean isVariableExist(Context pContext, String pName) {
		return pContext.isVariableExist(pName);
	}

	/** Check if the local variable named pName exist in the immediate scope */
	static public boolean isLocalVariableExist(Context pContext, String pName) {
		return pContext.isLocalVariableExist(pName);
	}

	/** Check if the variable is writable */	
	static public boolean isConstant(Context pContext, String pDHName) {
		return pContext.isVariableConstant(pDHName);
	}
	

	/** Check if the variable is an instance of the default DataHolderFactory */
	static public boolean isVariableDefaultDataHolder(Context pContext, String pDHName) {
		return pContext.isVariableDefaultDataHolder(pDHName);
	}
	/** Check if the variable is an instance of the DataHolderFactory of the given name */	
	static public boolean checkVariableDataHolderFactory(Context pContext, String DHFactoryName, String pDHName) {
		return pContext.checkVariableDataHolderFactory(DHFactoryName, pDHName);
	}

	/** Add a DataHolder in to this scope */
	static public Object addDataHolder(Context pContext, String pBName, DataHolder pDH) {
		return pContext.addDataHolder(pBName, pDH);
	}

	// Parent --------------------------------------------------------
	
	// By count -------------------------------------------
	
	/** Set a value to a parent variable */
	static public Object setParentValue(Context pContext, int pCount, String pVarName,
			Object pNewValue) {
		return pContext.setParentVariableValue(pCount, pVarName, pNewValue);
	}

	/** Get a value of a parent variable. */
	static public Object getParentValue(Context pContext, int pCount, String pVarName) {
		return pContext.getParentVariableValue(pCount, pVarName);
	}

	/** Check if the parent variable named pName exist */
	static public boolean isParentVariableExist(Context pContext, int pCount, String pDHName) {
		return pContext.isParentVariableExist(pCount, pDHName);
	}

	/** Check if the parent variable named pName exist */
	static public boolean isParentConstant(Context pContext, int pCount, String pDHName) {
		return pContext.isParentVariableConstant(pCount, pDHName);
	}
	
	// By name --------------------------------------------
	
	/** Set a value to a parent variable */
	static public Object setParentValue(Context pContext, String pStackName, String pVarName,
			Object pNewValue) {
		return pContext.setParentVariableValue(pStackName, pVarName, pNewValue);
	}

	/** Get a value of a parent variable. */
	static public Object getParentValue(Context pContext, String pStackName, String pVarName) {
		return pContext.getParentVariableValue(pStackName, pVarName);
	}

	/** Check if the parent variable named pName exist */
	static public boolean isParentVariableExist(Context pContext, String pStackName, String pDHName) {
		return pContext.isParentVariableExist(pStackName, pDHName);
	}

	/** Check if the parent variable named pName exist */
	static public boolean isParentConstant(Context pContext, String pStackName, String pDHName) {
		return pContext.isParentVariableConstant(pStackName, pDHName);
	}

	// Engine Context ----------------------------------------------------------
	/** Set a value to an engine variable */
	static public Object setEngineValue(Context pContext, String pVarName, Object pNewValue) {
		return pContext.setEngineVariableValue(pVarName, pNewValue);
	}

	/** Get a value of an engine variable. */
	static public Object getEngineValue(Context pContext, String pDHName) {
		return pContext.getEngineVariableValue(pDHName);
	}

	/** Check if the engine variable named pName exist */
	static public boolean isEngineVariableExist(Context pContext, String pDHName) {
		return pContext.isEngineVariableExist(pDHName);
	}

	/** Check if the parent variable named pName exist */
	static public boolean isEngineVariableConstant(Context pContext, String pDHName) {
		return pContext.isEngineVariableConstant(pDHName);
	}

	// Global Scope ------------------------------------------------------------

	// Global scope control ------------------------------------------
	
	/** Checks if the global context allow to add a variable. */
	static public boolean isGlobalStack(Context pContext) {
		return pContext.isGlobalStack();
	}
	
	/** Checks if the global context allow to add a variable. */
	static public boolean isNewGlobalVarEnabled(Context pContext) {
		return pContext.isNewGlobalVariableEnabled();
	}

	// Access --------------------------------------------------------
	/** Create a new variable */
	static public Object newGlobalVariable(Context pContext, String pVName, Type pType,
			Object pDefaultValue, boolean pIsConstant) {
		return pContext.newGlobalVariable(pVName, pType, pDefaultValue, pIsConstant);
	}

	/** Add a DataHolder in to this scope */
	static public Object addGlobalDataHolder(Context pContext, String pDHName, DataHolder pDH) {
		return pContext.addGlobalDataHolder(pDHName, pDH);
	}

	/** Set a value to an engine variable */
	static public Object setGlobalValue(Context pContext, String pVarName, Object pNewValue) {
		return pContext.setGlobalVariableValue(pVarName, pNewValue);
	}

	/** Set a value to an engine variable */
	static public Object getGlobalValue(Context pContext, String pDHName) {
		return pContext.getGlobalVariableValue(pDHName);
	}

	/** Check if the engine variable named pName exist */
	static public boolean isGlobalVariableExist(Context pContext, String pDHName) {
		return pContext.isGlobalVariableExist(pDHName);
	}

	/** Check if the parent variable named pName exist */
	static public boolean isGlobalVariableConstant(Context pContext, String pDHName) {
		return pContext.isGlobalVariableConstant(pDHName);
	}

	// Report Error with Location ----------------------------------------------

	/** Creates a new curry error that contains the location information */
	static public CurryError newCurryError(Context pContext, String pMessage, Throwable pCause) {
		return new CurryError(pMessage, pContext, pCause);
	}

	// Do Array Operation

	// Do DataArray_Curry

	// Do DataHolder_Curry -----------------------------------------------------
	/** Set value to the DataHolder */
	static public Object setDHData(Context pContext, String pName, DataHolder DH, Object pData) {
		return pContext.getEngine().getDataHolderManager().setDHData(pContext, pName, DH, pData);
	}

	/** Get value from the DataHolder */
	static public Object getDHData(Context pContext, String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().getDHData(pContext, pName, DH);
	}

	/** Check if the DataHolder is readable */
	static public boolean isDHReadable(Context pContext, String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().isDHReadable(pContext, pName, DH);
	}

	/** Check if the DataHolder is writable */
	static public boolean isDHWritable(Context pContext, String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().isDHWritable(pContext, pName, DH);
	}

	/** Get the DataHolder type */
	static public Type getDHType(Context pContext, String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().getDHType(pContext, pName, DH);
	}

	/** Returns a clone of this DataHolder. */
	static public DataHolder cloneDH(Context pContext, String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().cloneDH(pContext, pName, DH);
	}

	/** Performs advance configuration to the data holder. */
	static public Object configDH(Context pContext, String pName, DataHolder DH, String pMIName,
			Object[] pParams) {
		return pContext.getEngine().getDataHolderManager().configDH(pContext, DH, pMIName, pParams);
	}

	// Get StackOwner & StackCaller info ---------------------------------------
	
	/** Returns the owner of this stack */
	static public Object getStackOwner(Context pContext) {
		return pContext.getStackOwner();
	}
	/** Returns the owner of this stack */
	static public Type getStackOwnerAsType(Context pContext) {
		return pContext.getStackOwnerAsType();
	}
	/** Returns the owner of this stack */
	static public Type getStackOwnerAsCurrentType(Context pContext) {
		return pContext.getStackOwnerAsCurrentType();
	}
	/** Returns the owner of this stack */
	static public Package getStackOwnerAsPackage(Context pContext) {
		return pContext.getStackOwnerAsPackage();
	}

	/** Returns the caller of this stack */
	static public StackOwner getStackCaller(Context pContext) {
		return pContext.getStackCaller();
	}
	/** Returns the caller of this stack */
	static public Type getStackCallerAsType(Context pContext) {
		return pContext.getStackCallerAsType();
	}
	/** Returns the caller of this stack */
	static public Package getStackCallerAsPackage(Context pContext) {
		return pContext.getStackCallerAsPackage();
	}

	/** Returns the current stack delegate source */
	static public Object getDelegateSource(Context pContext) {
		return pContext.getStackCallerAsPackage();
	}

	/** Returns the caller of this stack */
	static public boolean isStackConstructor(Context pContext) {
		return pContext.isConstructor();
	}

	/** Returns the current executable */
	static public Executable getExecutable(Context pContext) {
		return pContext.getExecutable();
	}

	// Get Location info -------------------------------------------------------

	/** Get the current location */
	static public Location getStackLocation(Context pContext) {
		return pContext.getStackLocation();
	}

	/** Get the current coordinate */
	static public int getCurrentCoordinate(Context pContext) {
		return pContext.getCurrentCoordinate();
	}
	/** Get the current coordinate */
	static public int getCurrentColumn(Context pContext) {
		return pContext.getCurrentColumn();
	}

	/** Get the current line number */
	static public int getCurrentLineNumber(Context pContext) {
		return pContext.getCurrentLineNumber();
	}

	/** Return the identification of the current stack like "function()" */
	static public String getStackIdentification(Context pContext) {
		return pContext.getStackIdentification();
	}

	/** Returns the local snapshort of this location */
	static public LocationSnapshot getCurrentLocationSnapshot(Context pContext) {
		return pContext.getCurrentLocationSnapshot();
	}

	/** Returns the current expression */
	static public Expression getCurrentExpression(Context pContext) {
		return pContext.getCurrentExpression();
	}
	/** Returns the current documentation */
	static public Documentation getCurrentDocumentation(Context pContext) {
		return pContext.getCurrentDocumentation();
	}

	// Locations --------------------------------------------------------------

	/** Snapshot of the stacktrace */
	static public LocationSnapshot[] getLocations(Context pContext) {
		return Context.getLocationsOf(pContext);
	}

	/** Returns the location snapshot as a string */
	static public String getLocationsToString(Context pContext) {
		return pContext.getLocationsToString();
	}

	// Create ActionRecord -----------------------------------------------------
	
	/** Create an action record. */
	static public ActionRecord newActionRecord(Context pContext) {
		Object Owner = pContext.getStackOwner();
		return new ActionRecord((Owner instanceof StackOwner)?(StackOwner)Owner:null, pContext.getCurrentLocationSnapshot());
	}

	/** Create an action record with extra data. */
	static public ActionRecord newActionRecord(Context pContext, MoreData pExtraData) {
		Object Owner = pContext.getStackOwner();
		return (pExtraData == null)
			? new ActionRecord                          ((Owner instanceof StackOwner)?(StackOwner)Owner:null, pContext.getCurrentLocationSnapshot())
			: new ActionRecord.ActionRecord_WithMoreData((Owner instanceof StackOwner)?(StackOwner)Owner:null, pContext.getCurrentLocationSnapshot(), pExtraData);
	}
	
	/** Create an action record. */
	static public ActionRecord newCallerActionRecord(Context pContext) {
		pContext = pContext.getExecutableContext();
		
		ScopePrivate SP = pContext.getActualParent();
		while(SP instanceof ContextStackOwner)
			SP = ((ContextStackOwner)SP).getActualParent();
		if(!(SP instanceof Context)) return null;
		
		return ExternalContext.newActionRecord((Context)SP);
	}

	/** Create an action record with extra data. */
	static public ActionRecord newCallerActionRecord(Context pContext, MoreData pExtraData) {
		pContext = pContext.getExecutableContext();
		
		ScopePrivate SP = pContext.getActualParent();
		while(SP instanceof ContextStackOwner)
			SP = ((ContextStackOwner)SP).getActualParent();
		if(!(SP instanceof Context)) return null;
		
		return ExternalContext.newActionRecord((Context)SP, pExtraData);
	}
	
	// TODO - This is a Hack
	static public ActionRecord newCallerActionRecord_Hack(Context pContext) {
		if(pContext instanceof ContextStackOwner) {
			boolean IsInitializer     = ((ContextStackOwner)pContext).IsInitializer;
			boolean IsAttributeAccess = ((ContextStackOwner)pContext).IsAttributeAccess;
			
			ScopePrivate SP = pContext;
			while(SP instanceof ContextStackOwner) {
				if(IsInitializer     != ((ContextStackOwner)SP).IsInitializer)     break;
				if(IsAttributeAccess != ((ContextStackOwner)SP).IsAttributeAccess) break;

				IsInitializer     = ((ContextStackOwner)SP).IsInitializer;
				IsAttributeAccess = ((ContextStackOwner)SP).IsAttributeAccess;
				SP                = ((ContextStackOwner)SP).getActualParent();
			}
			return ExternalContext.newActionRecord((Context)SP);
		}
		
		pContext = pContext.getExecutableContext();
		
		ScopePrivate SP = pContext.getActualParent();
		while(SP instanceof ContextStackOwner)
			SP = ((ContextStackOwner)SP).getActualParent();
		if(!(SP instanceof Context)) return null;
		
		return ExternalContext.newActionRecord((Context)SP);
	}

	// Objectable --------------------------------------------------------------

	/** Returns a string representation of an object with type */
	static public String getDisplayObject(Context pContext, Object O) {
		return pContext.getEngine().getDisplayObject(pContext, O);
	}

	/** Returns a short string representation of an object. */
	static public String toString(Context pContext, Object O) {
		return pContext.getEngine().toString(pContext, O);
	}

	/** Returns a long string representation of an object. */
	static public String toDetail(Context pContext, Object O) {
		return pContext.getEngine().toDetail(pContext, O);
	}

	/** Checks if the object O is the same with AnotherO. */
	static public boolean is(Context pContext, Object O, Object AnotherO) {
		return pContext.getEngine().is(pContext, O, AnotherO);
	}

	/** Checks if the object O equals to AnotherO. */
	static public boolean equals(Context pContext, Object O, Object AnotherO) {
		return pContext.getEngine().equals(pContext, O, AnotherO);
	}

	/** Returns the hash value of the object O. */
	static public int hash(Context pContext, Object O) {
		return pContext.getEngine().hash(pContext, O);
	}

	/** Compare the object O equals to AnotherO. */
	static public int compares(Context pContext, Object O, Object AnotherO) {
		return pContext.getEngine().compares(pContext, O, AnotherO);
	}

	// For Debugger only -----------------------------------------------------------------

	/** A service to create a result that will replace the current execution */
	static public Object createDebuggerResult(Context pContext, Object pResult) {
		return new Debugger.DebuggerResult(pResult);
	}

	/** A service to create a result that will replace the current execution */
	static public Object createDebuggerReplace(Context pContext, Expression pExpr) {
		return new Debugger.DebuggerResult(pExpr);
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	// Embedded
	// -----------------------------------------------------------------------------------------------------------------
	
	Context pContext = null;
	
	/** Creates an instance of ExternalContext */
	public ExternalContext(Context pC) {
		this.pContext = pC;
	}
	/** Unhook the Context from this ExternalContext and make this object unusable */
	final public void detach() {
		this.pContext = null;
	}

	// General -----------------------------------------------------------------

	/** Returns the engine of that this context is operating on */
	public Engine getEngine() {
		return pContext.getEngine();
	}

	// Type --------------------------------------------------------------------

	/** Ensure that the type referred by pTypeRef exists */
	public void ensureTypeExist(TypeRef TR) {
		pContext.getEngine().getTypeManager().ensureTypeExist(pContext, TR);
	}

	/** Ensure that the type referred by pTypeRef is validated */
	public void ensureTypeValidated(TypeRef TR, TypeRef pParameterBaseTypeToIgnore) {
		pContext.getEngine().getTypeManager().ensureTypeValidated(pContext, TR, pParameterBaseTypeToIgnore);
	}

	/** Ensure that the type is initialized */
	public void ensureTypeInitialized(Type pType) {
		pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, pType);
	}

	/** Ensure that the type referred by pTypeRef is initialized */
	public void ensureTypeInitialized(TypeRef pTypeRef, TypeRef pParameterBaseTypeToIgnore) {
		pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, pTypeRef, pParameterBaseTypeToIgnore);
	}
	
	/** Ensure that the type referred by pTypeRef is initialized */
	public void ensureTypeInitialized(TypeRef pTypeRef) {
		pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, pTypeRef, null);
	}

	/** Returns the type from the ref */
	public Type getTypeFromRef(TypeRef TR) {
		return pContext.getEngine().getTypeManager().getTypeFromRef(pContext, TR);
	}

	// Execute -----------------------------------------------------------------
	/** Create and run an expression */
	public Object run(String pInstName, Object... Params) {
		Instruction Inst = pContext.getEngine().getInstruction(pInstName);
		Expression Expr = Inst.newExpression(Params);
		if (Expr == null)
			return null;
		Object R = pContext.getExecutor().execInternal(pContext, Expr);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Create and run an expression */
	public Object run(String pInstName, Object[] Params, Expression... Body) {
		Instruction Inst = pContext.getEngine().getInstruction(pInstName);
		Expression Expr = Inst.newExprSubs(Params, Body);
		if (Expr == null)
			return null;
		Object R = pContext.getExecutor().execInternal(pContext, Expr);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an expression in this context. */
	public Object execute(Expression pExpr) {
		Object R = pContext.getExecutor().execInternal(pContext, pExpr);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Run an executable as a fragment in this context. */
	public Object runFragment(Executable pExec) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec,
				ExecKind.Fragment, false, null, null, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an executable as a macro in this context. */
	public Object execMacro(Executable pExec, Object[] Params) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec, ExecKind.Macro,
				false, null, Params, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an executable as a macro in this context. */
	public Object execMacro(Executable pExec, boolean IsBlindCaller,
			Object[] Params) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec, ExecKind.Macro,
				IsBlindCaller, null, Params, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an executable as a sub-routine in this context. */
	public Object callSubRoutine(Executable pExec, Object[] Params) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec,
				ExecKind.SubRoutine, false, null, Params, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Execute an executable as a sub-routine in this context. */
	public Object callSubRoutine(Executable pExec, boolean IsBlindCaller,
			Object[] Params) {
		Object R = pContext.getExecutor().execExecutable(pContext, pExec, pExec,
				ExecKind.SubRoutine, IsBlindCaller, null, Params, false, true);
		if (R instanceof SpecialResult)
			throw ((SpecialResult) R).getException(pContext);
		return R;
	}

	/** Checks if an object is a special result and should not process. */
	public boolean isSpecialResult(Object O) {
		return O instanceof SpecialResult;
	}
	
	// Context info ------------------------------------------------------------
	
	public String getName() {
		return pContext.getName();
	}

	// Variable access ---------------------------------------------------------

	// Local ---------------------------------------------------------
	/** Create a new variable */
	public Object newVariable(String pVName, Type pType, Object pDefaultValue) {
		return pContext.newVariable(pContext.getEngine(), pVName, pType, pDefaultValue);
	}

	/** Create a new constant */
	public Object newConstant(String pVName, Type pType, Object pDefaultValue) {
		return pContext.newConstant(pContext.getEngine(), pVName, pType, pDefaultValue);
	}

	/** Set a value to a local variable. */
	public Object setVarValue(String pVName, Object pValue) {
		return pContext.setVariableValue(pVName, pValue);
	}

	/** Get a value of a local variable. */
	public Object getVarValue(String pVName) {
		return pContext.getVariableValue(pVName);
	}

	/** Check if the variable named pName exist */
	public boolean isVariableExist(String pName) {
		return pContext.isVariableExist(pName);
	}

	/** Check if the local variable named pName exist in the immediate scope */
	public boolean isLocalVariableExist(String pName) {
		return pContext.isLocalVariableExist(pName);
	}

	/** Check if the variable is writable */	
	public boolean isConstant(String pDHName) {
		DataHolder DH = pContext.getDataHolder(pDHName);
		if(DH == null) return false;
		return !pContext.getEngine().getDataHolderManager().isDHWritable(pContext, pDHName, DH);
	}

	/** Add a DataHolder in to this scope */
	public Object addDataHolder(String pBName, DataHolder pDH) {
		return pContext.addDataHolder(pBName, pDH);
	}

	// Parent --------------------------------------------------------
	
	// By count -------------------------------------------
	
	/** Set a value to a parent variable */
	public Object setParentValue(int pCount, String pVarName,
			Object pNewValue) {
		return pContext.setParentVariableValue(pCount, pVarName, pNewValue);
	}

	/** Get a value of a parent variable. */
	public Object getParentValue(int pCount, String pVarName) {
		return pContext.getParentVariableValue(pCount, pVarName);
	}

	/** Check if the parent variable named pName exist */
	public boolean isParentVariableExist(int pCount, String pDHName) {
		return pContext.isParentVariableExist(pCount, pDHName);
	}

	/** Check if the parent variable named pName exist */
	public boolean isParentConstant(int pCount, String pDHName) {
		return pContext.isParentVariableConstant(pCount, pDHName);
	}
	
	// By name --------------------------------------------
	
	/** Set a value to a parent variable */
	public Object setParentValue(String pStackName, String pVarName,
			Object pNewValue) {
		return pContext.setParentVariableValue(pStackName, pVarName, pNewValue);
	}

	/** Get a value of a parent variable. */
	public Object getParentValue(String pStackName, String pVarName) {
		return pContext.getParentVariableValue(pStackName, pVarName);
	}

	/** Check if the parent variable named pName exist */
	public boolean isParentVariableExist(String pStackName, String pDHName) {
		return pContext.isParentVariableExist(pStackName, pDHName);
	}

	/** Check if the parent variable named pName exist */
	public boolean isParentConstant(String pStackName, String pDHName) {
		return pContext.isParentVariableConstant(pStackName, pDHName);
	}

	// Engine Context ----------------------------------------------------------
	/** Set a value to an engine variable */
	public Object setEngineValue(String pVarName, Object pNewValue) {
		return pContext.setEngineVariableValue(pVarName, pNewValue);
	}

	/** Get a value of an engine variable. */
	public Object getEngineValue(String pDHName) {
		return pContext.getEngineVariableValue(pDHName);
	}

	/** Check if the engine variable named pName exist */
	public boolean isEngineVariableExist(String pDHName) {
		return pContext.isEngineVariableExist(pDHName);
	}

	/** Check if the parent variable named pName exist */
	public boolean isEngineVariableConstant(String pDHName) {
		return pContext.isEngineVariableConstant(pDHName);
	}

	// Global Scope ------------------------------------------------------------

	// Global scope control ------------------------------------------
	
	/** Checks if the global context allow to add a variable. */
	public boolean isGlobalStack() {
		return pContext.isGlobalStack();
	}
	
	/** Checks if the global context allow to add a variable. */
	public boolean isNewGlobalVarEnabled() {
		return pContext.isNewGlobalVariableEnabled();
	}

	// Access --------------------------------------------------------
	/** Create a new variable */
	public Object newGlobalVariable(String pVName, Type pType,
			Object pDefaultValue, boolean pIsConstant) {
		return pContext.newGlobalVariable(pVName, pType, pDefaultValue, pIsConstant);
	}

	/** Add a DataHolder in to this scope */
	public Object addGlobalDataHolder(String pDHName, DataHolder pDH) {
		return pContext.addGlobalDataHolder(pDHName, pDH);
	}

	/** Set a value to an engine variable */
	public Object setGlobalValue(String pVarName, Object pNewValue) {
		return pContext.setGlobalVariableValue(pVarName, pNewValue);
	}

	/** Set a value to an engine variable */
	public Object getGlobalValue(String pDHName) {
		return pContext.getGlobalVariableValue(pDHName);
	}

	/** Check if the engine variable named pName exist */
	public boolean isGlobalVariableExist(String pDHName) {
		return pContext.isGlobalVariableExist(pDHName);
	}

	/** Check if the parent variable named pName exist */
	public boolean isGlobalVariableConstant(String pDHName) {
		return pContext.isGlobalVariableConstant(pDHName);
	}

	// Report Error with Location ----------------------------------------------

	/** Creates a new curry error that contains the location information */
	public CurryError newCurryError(String pMessage, Throwable pCause) {
		return new CurryError(pMessage, pContext, pCause);
	}

	// Do Array Operation

	// Do DataArray_Curry

	// Do DataHolder_Curry -----------------------------------------------------
	/** Set value to the DataHolder */
	public Object setDHData(String pName, DataHolder DH, Object pData) {
		return pContext.getEngine().getDataHolderManager().setDHData(pContext, pName, DH, pData);
	}

	/** Get value from the DataHolder */
	public Object getDHData(String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().getDHData(pContext, pName, DH);
	}

	/** Check if the DataHolder is readable */
	public boolean isDHReadable(String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().isDHReadable(pContext, pName, DH);
	}

	/** Check if the DataHolder is writtable */
	public boolean isDHWritable(String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().isDHWritable(pContext, pName, DH);
	}

	/** Get the DataHolder type */
	public Type getDHType(String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().getDHType(pContext, pName, DH);
	}

	/** Returns a clone of this DataHolder. */
	public DataHolder cloneDH(String pName, DataHolder DH) {
		return pContext.getEngine().getDataHolderManager().cloneDH(pContext, pName, DH);
	}

	/** Performs advance configuration to the data holder. */
	public Object configDH(String pName, DataHolder DH, String pMIName,
			Object[] pParams) {
		return pContext.getEngine().getDataHolderManager().configDH(pContext, DH, pMIName, pParams);
	}

	// Get StackOwner & StackCaller info ---------------------------------------
	
	/** Returns the owner of this stack */
	public Object getStackOwner() {
		return pContext.getStackOwner();
	}
	/** Returns the owner of this stack */
	public Type getStackOwnerAsType() {
		return pContext.getStackOwnerAsType();
	}
	/** Returns the owner of this stack */
	public Type getStackOwnerAsCurrentType() {
		return pContext.getStackOwnerAsCurrentType();
	}
	/** Returns the owner of this stack */
	public Package getStackOwnerAsPackage() {
		return pContext.getStackOwnerAsPackage();
	}

	/** Returns the caller of this stack */
	public StackOwner getStackCaller() {
		return pContext.getStackCaller();
	}
	/** Returns the caller of this stack */
	public Type getStackCallerAsType() {
		return pContext.getStackCallerAsType();
	}
	/** Returns the caller of this stack */
	public Package getStackCallerAsPackage() {
		return pContext.getStackCallerAsPackage();
	}

	/** Returns the current stack delegate source */
	public Object getDelegateSource() {
		return pContext.getStackCallerAsPackage();
	}

	/** Returns the caller of this stack */
	public boolean isStackConstructor() {
		return pContext.isConstructor();
	}

	/** Returns the current executable */
	public Executable getExecutable() {
		return pContext.getExecutable();
	}

	// Get Location info -------------------------------------------------------

	/** Get the current location */
	public Location getStackLocation() {
		return pContext.getStackLocation();
	}

	/** Get the current coordinate */
	public int getCurrentCoordinate() {
		return pContext.getCurrentCoordinate();
	}
	/** Get the current coordinate */
	public int getCurrentColumn() {
		return pContext.getCurrentColumn();
	}

	/** Get the current line number */
	public int getCurrentLineNumber() {
		return pContext.getCurrentLineNumber();
	}

	/** Return the identification of the current stack like "function()" */
	public String getStackIdentification() {
		return pContext.getStackIdentification();
	}

	/** Returns the local Snapshot of this location */
	public LocationSnapshot getCurrentLocationSnapshot() {
		return pContext.getCurrentLocationSnapshot();
	}

	/** Returns the current expression */
	public Expression getCurrentExpression() {
		return pContext.getCurrentExpression();
	}
	/** Returns the current documentation */
	public Documentation getCurrentDocumentation() {
		return pContext.getCurrentDocumentation();
	}

	// Locations --------------------------------------------------------------

	/** Snapshot of the StackTrace */
	public LocationSnapshot[] getLocations() {
		return Context.getLocationsOf(pContext);
	}

	/** Returns the location snapshot as a string */
	public String getLocationsToString() {
		return pContext.getLocationsToString();
	}

	// Create ActionRecord -----------------------------------------------------
	/** Create an action record. */
	public ActionRecord newActionRecord() {
		Object Owner = pContext.getStackOwner();
		return new ActionRecord((Owner instanceof StackOwner)?(StackOwner)Owner:null, pContext.getCurrentLocationSnapshot());
	}

	/** Create an action record with extra data. */
	public ActionRecord newActionRecord(MoreData pExtraData) {
		Object Owner = pContext.getStackOwner();
		return (pExtraData == null)
			? new ActionRecord                          ((Owner instanceof StackOwner)?(StackOwner)Owner:null, pContext.getCurrentLocationSnapshot())
			: new ActionRecord.ActionRecord_WithMoreData((Owner instanceof StackOwner)?(StackOwner)Owner:null, pContext.getCurrentLocationSnapshot(), pExtraData);
	}
	/** Create an action record. */
	final protected ActionRecord newActionRecord(Documentation pDocument) {
		return this.newActionRecord((pDocument == null) ? null : Documentation.Util.NewMoreData(pDocument));
	}
	/** Create an action record. */
	final protected ActionRecord newActionRecord(String pDocumentText) {
		return this.newActionRecord((pDocumentText == null) ? null : Documentation.Util.NewMoreData(pDocumentText));
	}

	// Objectable --------------------------------------------------------------

	/** Returns a string representation of an object with type */
	public String getDisplayObject(Object O) {
		return pContext.getEngine().getDisplayObject(pContext, O);
	}

	/** Returns a short string representation of an object. */
	public String toString(Object O) {
		return pContext.getEngine().toString(pContext, O);
	}

	/** Returns a long string representation of an object. */
	public String toDetail(Object O) {
		return pContext.getEngine().toDetail(pContext, O);
	}

	/** Checks if the object O is the same with AnotherO. */
	public boolean is(Object O, Object AnotherO) {
		return pContext.getEngine().is(pContext, O, AnotherO);
	}

	/** Checks if the object O equals to AnotherO. */
	public boolean equals(Object O, Object AnotherO) {
		return pContext.getEngine().equals(pContext, O, AnotherO);
	}

	/** Returns the hash value of the object O. */
	public int hash(Object O) {
		return pContext.getEngine().hash(pContext, O);
	}

	/** Compare the object O equals to AnotherO. */
	public int compares(Object O, Object AnotherO) {
		return pContext.getEngine().compares(pContext, O, AnotherO);
	}

	// For Debugger only -----------------------------------------------------------------

	/** A service to create a result that will replace the current execution */
	public Object createDebuggerResult(Object pResult) {
		return new Debugger.DebuggerResult(pResult);
	}

	/** A service to create a result that will replace the current execution */
	public Object createDebuggerReplace(Expression pExpr) {
		return new Debugger.DebuggerResult(pExpr);
	}
}
