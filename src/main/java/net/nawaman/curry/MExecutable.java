package net.nawaman.curry;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.Instructions_Core.Inst_Group;
import net.nawaman.curry.Instructions_Core.Inst_RunOnce;
import net.nawaman.curry.Instructions_Core.Inst_Stack;
import net.nawaman.curry.Instructions_Core.Inst_Type;
import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.result.Coordinate;

public class MExecutable extends EnginePart {
	
	protected MExecutable(Engine pEngine) {
		super(pEngine);
	}
		
	// Execute Executable --------------------------------------------
	/** Execute an executable */
	public Object executeExecutable(Executable pExec, Object ... pParams) {
		return this.debugExecutable(null, null, pExec, pParams);
	}
	/** Execute an executable with a global scope */
	public Object executeExecutable(Scope pGlobalScope, Scope pScope, Executable pExec, Object ... pParams) {
		return this.debugExecutable(pGlobalScope, pScope, null, pExec, pParams);
	}
	
	// Debug Expression ----------------------------------------------
	/** Debug an executable */
	public Object debugExecutable(Debugger pDebugger, Executable pExec, Object ... pParams) {
		return this.debugExecutable(null, null, pDebugger, pExec, pParams);
	}
	/** Debug an executable with a global scope */
	public Object debugExecutable(Scope pGlobalScope, Scope pScope, Debugger pDebugger, Executable pExec, Object ... pParams) {
		if(pExec == null) return null;
		if(pExec instanceof Executable.Fragment)   return this.debugFragment  (pGlobalScope, pScope, pDebugger, pExec);
		if(pExec instanceof Executable.Macro)      return this.debugMacro(     pGlobalScope, pScope, pDebugger, pExec, pParams);
		if(pExec instanceof Executable.SubRoutine) return this.debugSubRoutine(pGlobalScope,         pDebugger, pExec, pParams);
		
		Context C = this.getEngine().newRootContext(pGlobalScope, pDebugger);
		Object  R = C.getExecutor().execExecutable(C, pExec, pExec, null, false, null, pParams, false, true);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}
	
	// Fragment ----------------------------------------------------------------
	
	// Execute a fragment -------------------------------------------
	/** Execute an executable as a fragment. */
	public Object runFragment(Executable pExec) {
		return this.debugFragment(null, null, null, pExec);
	}
	
	/** Execute an executable as a fragment in this context. */
	public Object runFragment(Scope pScope, Executable pExec) {
		return this.debugFragment(null, pScope, null, pExec);
	}
	
	/** Execute an executable as a fragment in this context. */
	public Object runFragment(Scope pGlobalScope, Scope pScope, Executable pExec) {
		return this.debugFragment(pGlobalScope, pScope, null, pExec);
	}
	
	// Debug an executable as a fragment ----------------------------
	/** Debug an executable as a fragment. */
	public Object debugFragment(Debugger pDebugger, Executable pExec) {
		return this.debugFragment(null, null, pDebugger, pExec);
	}
	/** Run an executable as a fragment in this context. */
	public Object debugFragment(Scope pScope, Debugger pDebugger, Executable pExec) {
		return this.debugFragment(null, pScope, pDebugger, pExec);
	}
	/** Run an executable as a fragment in this context. */
	public Object debugFragment(Scope pGlobalScope, Scope pScope, Debugger pDebugger, Executable pExec) {
		if(pExec == null) return null;
		Context C = this.getEngine().newRootContext(pGlobalScope, pScope, (Executable.Fragment)pExec, pDebugger);
		Object  R = C.getExecutor().execExecutable(C, pExec, pExec, ExecKind.Fragment, false, null, null, false, true);
		if(R instanceof SpecialResult) { 
			if(R instanceof SpecialResult.ResultResult) return ((SpecialResult.ResultResult)R).getResult();
			throw ((SpecialResult)R).getException(C);
		}
		return R;
	}
	
	// Macro -------------------------------------------------------------------
	
	// Execute an executable as a macro ------------------------------
	/** Execute an executable as a macro. */
	public Object execMacro(Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		return this.debugMacro(null, null, pExec, Params);
	}
	/** Execute an executable as a macro with in the global scope. */
	public Object execMacro(Scope pScope, Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		return this.debugMacro(null, pScope, null, pExec, Params);
	}	
	/** Execute an executable as a macro with in the global scope. */
	public Object execMacro(Scope pGlobalScope, Scope pScope, Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		return this.debugMacro(null, pScope, null, pExec, Params);
	}
	
	// Debug an executable as a macro --------------------------------
	/** Debug an executable as a macro with in the global scope. */
	public Object debugMacro(Debugger pDebugger, Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		return this.debugMacro(null, pDebugger, pExec, Params);
	}
	/** Debug an executable as a macro with in the global scope. */
	public Object debugMacro(Scope pScope, Debugger pDebugger, Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		return this.debugMacro(null, pScope, pDebugger, pExec, Params);
	}
	/** Debug an executable as a macro with in the global scope. */
	public Object debugMacro(Scope pGlobalScope, Scope pScope, Debugger pDebugger, Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		Context C = this.getEngine().newRootContext(pGlobalScope, pScope, pExec, pDebugger);
		Object  R = C.getExecutor().execExecutable(C, pExec, pExec, ExecKind.Macro, true, null, Params, false, true);
		if(R instanceof SpecialResult) { 
			if(R instanceof SpecialResult.ResultResult) return ((SpecialResult.ResultResult)R).getResult();
			throw ((SpecialResult)R).getException(C);
		}
		return R;
	}
	
	// SubRoutine --------------------------------------------------------------
	
	// Execute an executable as a sub-routine ------------------------
	/** Execute an executable as a sub-routine. */
	public Object callSubRoutine(Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		return this.debugSubRoutine(null, null, pExec, Params);
	}
	// Execute an executable as a sub-routine ------------------------
	/** Execute an executable as a sub-routine. */
	public Object callSubRoutine(Scope pGlobalScope, Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		return this.debugSubRoutine(pGlobalScope, null, pExec, Params);
	}

	// Debug an executable as a sub-routine --------------------------
	/** Debug an executable as a sub-routine. */
	public Object debugSubRoutine(Debugger pDebugger, Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		return this.debugSubRoutine(null, pDebugger, pExec, Params);
	}
	// Debug an executable as a sub-routine --------------------------
	/** Debug an executable as a sub-routine. */
	public Object debugSubRoutine(Scope pGlobalScope, Debugger pDebugger, Executable pExec, Object ... Params) {
		if(pExec == null) return null;
		Context C = this.getEngine().newRootContext(pGlobalScope, pDebugger);
		Object  R = C.getExecutor().execExecutable(C, pExec, pExec, ExecKind.SubRoutine, true, null, Params, false, true);
		if(R instanceof SpecialResult) { 
			if(R instanceof SpecialResult.ResultResult) return ((SpecialResult.ResultResult)R).getResult();
			throw ((SpecialResult)R).getException(C);
		}
		return R;
	}
	
	// Construct Executable services ----------------------------------------------------
	
	/** Create an expression */
	public Expression newExpr(String InstName, Object ... pParams) {
		return this.getEngine().getInstruction(InstName).newExpression_Coordinate((Coordinate)null, pParams);
	}
	/** Create an expression */
	public Expression newExpr(Coordinate coordinate, String InstName, Object ... pParams) {
		Instruction Inst = this.getEngine().getInstruction(InstName);
		if(Inst == null) throw new CurryError("Unknown instruction: " + InstName);
		return Inst.newExpression_Coordinate(coordinate, pParams);
	}
	/** Create an expression */
	public Expression newExpr(int[] pCR, String InstName, Object ... pParams) {
		Instruction Inst = this.getEngine().getInstruction(InstName);
		if(Inst == null) throw new CurryError("Unknown instruction: " + InstName);
		return Inst.newExpression_Coordinate(pCR, pParams);
	}
	/** Create an expression */
	public Expression newExpr(int pCol, int pRow, String InstName, Object ... pParams) {
		return this.getEngine().getInstruction(InstName).newExpression_Coordinate(pCol, pRow, pParams);
	}
	
	/** Create an expression with sub-expressions*/
	public Expression newExprSub(String InstName, Object[] pParams, Expression ... pSubExprs) {
		return this.getEngine().getInstruction(InstName).newExprSubs_Coordinate((Coordinate)null, pParams, pSubExprs);
	}
	/** Create an expression with sub-expressions*/
	public Expression newExprSub(Coordinate coordinate, String InstName, Object[] pParams, Expression ... pSubExprs) {
		int col = Coordinate.colOf(coordinate);
		int row = Coordinate.rowOf(coordinate);
		return this.getEngine().getInstruction(InstName).newExprSubs_Coordinate(col, row, pParams, pSubExprs);
	}
	/** Create an expression with sub-expressions*/
	public Expression newExprSub(int[] pCR, String InstName, Object[] pParams, Expression ... pSubExprs) {
		return this.getEngine().getInstruction(InstName).newExprSubs_Coordinate(pCR, pParams, pSubExprs);
	}
	/** Create an expression with sub-expressions*/
	public Expression newExprSub(int pCol, int pRow, String InstName, Object[] pParams, Expression ... pSubExprs) {
		return this.getEngine().getInstruction(InstName).newExprSubs_Coordinate(pCol, pRow, pParams, pSubExprs);
	}
	
	/** Create a type expression (type from TypeRef) */
	public Expression newType(Object pTRef) {
		return this.getEngine().getInstruction(Inst_Type.Name).newExpression_Coordinate((Coordinate)null, pTRef);
	}
	/** Create a type expression (type from TypeRef) */
	public Expression newType(Coordinate coordinate, Object pTRef) {
		return this.getEngine().getInstruction(Inst_Type.Name).newExpression_Coordinate(Coordinate.colOf(coordinate), Coordinate.rowOf(coordinate), pTRef);
	}
	/** Create a type expression (type from TypeRef) */
	public Expression newType(int[] pCR, Object pTRef) {
		return this.getEngine().getInstruction(Inst_Type.Name).newExpression_Coordinate(pCR, pTRef);
	}
	/** Create a type expression (type from TypeRef) */
	public Expression newType(int pCol, int pRow, Object pTRef) {
		return this.getEngine().getInstruction(Inst_Type.Name).newExpression_Coordinate(pCol, pRow, pTRef);
	}
	
	/** Create a group expression */
	public Expression newGroup(Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_Group.Name).newExprSubs_Coordinate((Coordinate)null, null, pExprs);
	}
	/** Create a group expression */
	public Expression newGroup(Coordinate coordinate, Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_Group.Name).newExprSubs_Coordinate(coordinate.toArray(), null, pExprs);
	}
	/** Create a group expression */
	public Expression newGroup(int[] pCR, Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_Group.Name).newExprSubs_Coordinate(pCR, null, pExprs);
	}
	/** Create a group expression */
	public Expression newGroup(int pCol, int pRow, Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_Group.Name).newExprSubs_Coordinate(pCol, pRow, null, pExprs);
	}
	
	/** Create a run once expression */
	public Expression newRunOnce(Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_RunOnce.Name).newExprSubs_Coordinate((Coordinate)null, null, pExprs);
	}
	/** Create a run once expression */
	public Expression newRunOnce(int[] pCR, Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_RunOnce.Name).newExprSubs_Coordinate(pCR, null, pExprs);
	}
	/** Create a run once expression */
	public Expression newRunOnce(int pCol, int pRow, Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_RunOnce.Name).newExprSubs_Coordinate(pCol, pRow, null, pExprs);
	}
	
	/** Create a stack expression */
	public Expression newStack(Expression ... pExprs) {
		return this.newStack((Coordinate)null, pExprs);
	}
	/** Create a stack expression */
	public Expression newStack(int[] pCR, Expression ... pExprs) {
		return this.newStack(pCR, null, pExprs);
	}
	/** Create a stack expression */
	public Expression newStack(Coordinate pCR, Expression ... pExprs) {
		return this.newStack(Coordinate.colOf(pCR), Coordinate.rowOf(pCR), null, pExprs);
	}
	/** Create a stack expression */
	public Expression newStack(int pCol, int pRow, Expression ... pExprs) {
		return this.newStack(pCol, pRow, null, pExprs);
	}
	
	/** Create a stack expression */
	public Expression newStack(Object pName, Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_Stack.Name).newExprSubs_Coordinate((Coordinate)null, new Object[] { pName }, pExprs);
	}
	/** Create a stack expression */
	public Expression newStack(Coordinate pCR, Object pName, Expression ... pExprs) {
		return newStack(Coordinate.colOf(pCR), Coordinate.rowOf(pCR), pName, pExprs);
	}
	/** Create a stack expression */
	public Expression newStack(int[] pCR, Object pName, Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_Stack.Name).newExprSubs_Coordinate(pCR, new Object[] { pName }, pExprs);
	}
	/** Create a stack expression */
	public Expression newStack(int pCol, int pRow, Object pName, Expression ... pExprs) {
		if((pExprs == null) || (pExprs.length == 0)) return null;
		return this.getEngine().getInstruction(Inst_Stack.Name).newExprSubs_Coordinate(pCol, pRow, new Object[] { pName }, pExprs);
	}
	
	// Fragment ----------------------------------------------------------------
	/** Create a new curry fragment */
	public CurryExecutable.CurryFragment newFragment(String pName, TypeRef pReturnTypeRef, Location pLocation,
			MoreData pExtraData, Serializable pBody) {
		return new CurryExecutable.CurryFragment(this.getEngine(), pName, pReturnTypeRef, pLocation, pExtraData,
				pBody, null, null);
	}
	/** Create a new curry fragment */
	public CurryExecutable.CurryFragment newFragment(String pName, TypeRef pReturnTypeRef, Location pLocation,
			MoreData pExtraData,
			Serializable pBody, String[] pFVNames, Scope pFrozenScope) {
		return new CurryExecutable.CurryFragment(this.getEngine(), pName, pReturnTypeRef, pLocation, pExtraData, pBody,
				pFVNames, pFrozenScope);
	}
	/** Create a new external fragment */
	public ExternalExecutable.ExternalFragment newFragment(ExternalExecutor pEE, Object pID, Object pSC) {
		return new ExternalExecutable.ExternalFragment(pEE, pID, pSC);
	}
	// Macro -------------------------------------------------------------------
	/** Create a new curry macro */
	public CurryExecutable.CurryMacro newMacro(ExecSignature pSignature, Serializable pBody, String[] pFVNames,
			Scope pFrozenScope) {
		return new CurryExecutable.CurryMacro(this.getEngine(), pSignature, pBody, pFVNames, pFrozenScope);
	}
	/** Create a new curry macro */
	public CurryExecutable.CurryMacro newMacro(ExecSignature pSignature, Serializable pBody) {
		return new CurryExecutable.CurryMacro(this.getEngine(), pSignature, pBody, null, null);
	}
	/** Create a new external block */
	public ExternalExecutable.ExternalMacro newMacro(ExternalExecutor pEE, Object pID, Object pSC) {
		return new ExternalExecutable.ExternalMacro(pEE, pID, pSC);
	}
	// SubRoutine --------------------------------------------------------------
	/** Create a new curry sub-routine */
	public CurryExecutable.CurrySubRoutine newSubRoutine(ExecSignature pSignature, Serializable pBody, String[] pFVNames,
			Scope pFrozenScope) {
		return new CurryExecutable.CurrySubRoutine(this.getEngine(), pSignature, pBody, pFVNames, pFrozenScope);
	}
	/** Create a new curry sub-routine */
	public CurryExecutable.CurrySubRoutine newSubRoutine(ExecSignature pSignature, Serializable pBody) {
		return new CurryExecutable.CurrySubRoutine(this.getEngine(), pSignature, pBody, null, null);
	}
	/** Create a new external sub-routine */
	public ExternalExecutable.ExternalSubRoutine newSubRoutine(ExternalExecutor pEE, Object pID, Object pSC) {
		return new ExternalExecutable.ExternalSubRoutine(pEE, pID, pSC);
	}
	
	// Field -------------------------------------------------------------------
	
	// Read ----------------------------------------------------------
	/** Create an executable as object field read operation */
	public NativeExecutable.JavaObjectFieldRead newObjectFieldRead(Object pObj, Field pField) {
		return new NativeExecutable.JavaObjectFieldRead(this.getEngine(), pObj, pField);
	}
	/** Create an executable as type field read operation */
	public NativeExecutable.NativeClassFieldRead newTypeFieldRead(Field pField) {
		return new NativeExecutable.NativeClassFieldRead(this.getEngine(), pField);
	}
	
	// Write ---------------------------------------------------------
	/** Create an executable as object field write operation */
	public NativeExecutable.JavaObjectFieldWrite newObjectFieldWrite(Object pObj, Field pField) {
		return new NativeExecutable.JavaObjectFieldWrite(this.getEngine(), pObj, pField);
	}
	/** Create an executable as object field write operation */
	public NativeExecutable.JavaClassFieldWrite newTypeFieldWrite(Field pField) {
		return new NativeExecutable.JavaClassFieldWrite(this.getEngine(),  pField);
	}
	// Method with Param ---------------------------------------------
	/** Create an executable as object method invocation */
	public NativeExecutable.JavaObjectMethodInvoke newObjectMethod(Object pObj, Method M) {
		return new NativeExecutable.JavaObjectMethodInvoke(this.getEngine(), pObj, M);
	}
	/** Create an executable as type method invocation */
	public NativeExecutable.JavaClassMethodInvoke newTypeMethod(Method M) {
		return new NativeExecutable.JavaClassMethodInvoke(this.getEngine(), M);
	}
	// Method without Param ------------------------------------------
	/** Create an executable as object method invocation that has no parameter */
	public NativeExecutable.JavaObjectMethodInvoke_NoParam newObjectMethod_NoParams(Object pObj, Method M) {
		return new NativeExecutable.JavaObjectMethodInvoke_NoParam(this.getEngine(), pObj, M);
	}
	/** Create an executable as type method invocation that has no parameter */
	public NativeExecutable.JavaClassMethodInvoke_NoParam newTypeMethod_NoParams(Method M) {
		return new NativeExecutable.JavaClassMethodInvoke_NoParam(this.getEngine(), M);
	}
	// Constructor with Param ---------------------------------------------
	/** Create an executable as constructor invocation */
	public NativeExecutable.JavaConstructorInvoke newConstructor(Constructor<?> C) {
		return new NativeExecutable.JavaConstructorInvoke(this.getEngine(), C);
	}
	// Method without Param ------------------------------------------
	/** Create an executable as constructor invocation that has no parameter */
	public NativeExecutable.JavaConstructorInvoke_NoParam newConstructor_NoParams(Constructor<?> C) {
		return new NativeExecutable.JavaConstructorInvoke_NoParam(this.getEngine(), C);
	}
}
