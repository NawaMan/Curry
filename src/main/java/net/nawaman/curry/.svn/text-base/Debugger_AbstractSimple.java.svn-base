package net.nawaman.curry;

import java.io.PrintStream;

abstract public class Debugger_AbstractSimple implements Debugger {

	public Debugger_AbstractSimple() {}
	
	// Setting ---------------------------------------------------------------------------
	
	boolean IsEnable = true;
	public void enable()  { this.IsEnable =  true; }
	public void disable() { this.IsEnable = false; }
	
	boolean IsEachExpr = true;
	public void toMornitorExpression()    { this.IsEachExpr =  true; }
	public void toNotMornitorExpression() { this.IsEachExpr = false; }
	
	// Switch ----------------------------------------------------------------------------
	public boolean isEnable()                       { return this.IsEnable;   }
	public boolean isMornitorExecutableInvocation() { return  true; }
	public boolean isMornitorExpressionExecution()  { return this.IsEachExpr; }
	public boolean isMornitorLineChange()           { return false; }
	
	// Messaging -------------------------------------------------------------------------
	public Object sendMessage(Context pContext, Object ID, Object ... Params) {
		PrintStream O = pContext.getEngine().getDebugPrinter();
		return this.sendMessage(O, ID, Params);
	}
	abstract protected Object sendMessage(PrintStream O, Object ID, Object ... Params);
	
	// Event -----------------------------------------------------------------------------
	public void onStart(Context pContext) {
		PrintStream O = pContext.getEngine().getDebugPrinter();
		this.onStart(pContext.getEngine(), pContext.getGlobalScope(), O);
	}
	abstract protected void onStart(Engine Engine, Scope GlobalScope, PrintStream O);
	
	@Override public Object onQuit(Context pContext, Object pReturn) {
		PrintStream O = pContext.getEngine().getDebugPrinter();
		return this.onQuit(pContext.getEngine(), pContext.getGlobalScope(), O, pReturn);
	}
	abstract protected Object onQuit(Engine Engine, Scope GlobalScope, PrintStream O, Object Return);
	
	// Executable ------------------------------------------------------------------------
	@Override public Object onInvokeExecutable(Context pContext, Executable pExec) {
		PrintStream O = pContext.getEngine().getDebugPrinter();
		return this.onInvokeExecutable(pContext.getEngine(), pContext.getGlobalScope(), O, pExec);
	}
	abstract protected Object onInvokeExecutable(Engine Engine, Scope GlobalScope, PrintStream O, Executable Exec);
	
	// Expression ----------------------------------------------------------------------------------
	@Override public Object onExecuteExpression(Context pContext, Expression Expr, Object[] pParams) {
		PrintStream O = pContext.getEngine().getDebugPrinter();
		// Get Level
		int Level = 0;
		Context C = pContext;
		while((C != null) && !(C instanceof Context.ContextGlobal)) { C = (Context)C.Parent; Level++; }
		Level--;
		// More Data
		Executable Exec = pContext.getInitiator();
		Object     SO   = pContext.getStackOwner();
		Location   L    = pContext.getStackLocation();
		String     ID   = pContext.getStackIdentification();
		int        Col  = pContext.getCurrentColumn();
		int        Row  = pContext.getCurrentLineNumber();
		return this.onExecuteExpression(pContext.getEngine(), pContext.getGlobalScope(), O, Expr, pParams, Level, L,
                Col, Row, Exec, SO, ID);
	}
	abstract protected Object onExecuteExpression(Engine Engine, Scope GlobalScope, PrintStream O,
	                                       Expression Expr, Object[] pParams, int Level, Location Location,
	                                       int pCurrentColumn, int CurrentLineNumber, Executable Exec, Object SO,
	                                       String Identification);
	
	// Line Number ---------------------------------------------------------------------------------
	@Override public Object onLineChanged(Context pContext) { return null; }
}
