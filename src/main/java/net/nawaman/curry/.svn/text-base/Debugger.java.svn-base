package net.nawaman.curry;

/** Debugger that mornitor the execution. */
public interface Debugger {
	
	// Switch --------------------------------------------------------------------------------------
	
	/** Checks if the debugger is running at all */
	public boolean isEnable();
	/** Checks if this debugger is mornitoring a context change. */
	public boolean isMornitorExecutableInvocation();
	/** Checks if this debugger is mornitoring an expression execution. */
	public boolean isMornitorExpressionExecution();
	/** Checks if this debugger is mornitoring a changing of line number. */
	public boolean isMornitorLineChange();
	
	// Messaging -----------------------------------------------------------------------------------
	
	/** Send message to the debugger. **/
	public Object sendMessage(Context pContext, Object ID, Object ... Params);
	
	// Mornitoring ---------------------------------------------------------------------------------
	
	/**
	 * This method will be run when the execution first start.
	 **/
	public void onStart(Context pContext);

	/**
	 * This method will be run when the execution is just about to end. The return value of this
	 *     method will be returned as the result of the execution. 
	 **/
	public Object onQuit(Context pContext, Object pReturn);
	
	/**
	 * This method will be run when an expression Expr is about to be executed (but after the line
	 *    skipping is process so the line number will not be effected). Unless the method 
	 *    isMornitorExecutableInvokation() of this class that is run just before this method return 
	 *    `false`, this method will always be executed.<br />
	 * <br />
	 * This method can return the replacement of the execution either in value or the replacement
	 *    expression. The special result (both return and replace) can be created using
	 *    createDebuggerResult() and createDebuggerReplace() in ExternalContext. Or simple return
	 *    null if there is not replace or return.<br />
	 *  <br />
	 * If the executable is null, the execute will not execute this method.
	 **/
	public Object onInvokeExecutable(Context pContext, Executable pExec);
	
	/**
	 * This method will be run when an expression Expr is about to be executed (but after the line
	 *    skipping is process so the line number will not be effected). Unless the method 
	 *    isMornitorExprExecution() of this class that is run just before this method return `false`,
	 *    this method will always be executed.<br />
	 * <br />
	 * This method can return the replacement of the execution either in value or the replacement
	 *    expression. The special result (both return and replace) can be created using
	 *    createDebuggerResult() and createDebuggerReplace() in Context. Or simple return
	 *    null if there is not replace or return.<br />
	 *  <br />
	 * If the expression is null, the execute will not execute this method.
	 **/
	public Object onExecuteExpression(Context pContext, Expression Expr, Object[] pParams);
	
	/**
	 * This method will be run when a line number of the excuting code has changed. Unless the method
	 *    isLineChange() of this class that is run just before this method return `false`,
	 *    this method will always be executed.<br />
	 * <br />
	 * If the executable is null, the execute will not execute this method.
	 **/
	public Object onLineChanged(Context pContext);


	// Special use -------------------------------------------------------------------------------------

	/** Special result used to replace the execution with a result. */
	static class DebuggerResult {
		DebuggerResult()               {}
		DebuggerResult(Object pResult) { this.Result = pResult; }
		Object Result = null;
	}
	/** Special result used to replace the execution with another expression. */
	static class DebuggerReplace {
		DebuggerReplace()                 {}
		DebuggerReplace(Expression pExpr) { this.Expr = pExpr; }
		Expression Expr = null;
	}
}