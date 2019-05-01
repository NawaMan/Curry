package net.nawaman.curry;

import java.io.PrintStream;

import net.nawaman.util.UObject;
import net.nawaman.util.UString;

public class Debugger_Simple extends Debugger_AbstractSimple {
	
	static final int Default_ScreenWidth = 180;
	static final int Default_ExprWidth   =  50;
	
	public Debugger_Simple() {}
	public Debugger_Simple(int pScreenWidth, int pExprWidth) {
		this.ScreenWidth = pScreenWidth;
		this.ExprWidth   = pExprWidth;
	}

	public Debugger_Simple(PrintStream pPS) {
		this.PS = pPS;
	}
	public Debugger_Simple(PrintStream pPS, int pScreenWidth, int pExprWidth) {
		this(pScreenWidth, pExprWidth);
		this.PS = pPS;
	}
	
	PrintStream PS          = null;
	int         ScreenWidth = Default_ScreenWidth;
	int         ExprWidth   = Default_ExprWidth;

	public void setScreenWidth(int W) { if(W >= 0) this.ScreenWidth = W; }
	public void setExprWidth(int W)   { if(W >= 0) this.ExprWidth   = W; }
	public int  getScreenWidth()      { return this.ScreenWidth;         }
	public int  getExprWidth()        { return this.ExprWidth;           }
	
	/** Selects a print stream */
	final protected PrintStream getPrintStream(PrintStream O) {
		if(this.PS != null) return PS;
		if(O       != null) return  O;
		return System.out;
	}
	
	// Messaging -------------------------------------------------------------------------
	@Override protected Object sendMessage(PrintStream O, Object ID, Object ... Params) {
		O = this.getPrintStream(O);
		
		O.println("Debug Message { ID: " + UObject.toString(ID) + "; Params: " + UObject.toString(Params) + "; }");
		return null;
	}
	
	// Event -----------------------------------------------------------------------------
	@Override protected void onStart(Engine Engine, Scope GlobalScope, PrintStream O) {
		O = this.getPrintStream(O);
		
		O.println();
		O.println(UString.tc("", '*', this.getScreenWidth()));
		String S = " BEGIN ";
		O.println(UString.tc(UString.lc(S, '*', (this.getScreenWidth() + S.length())/2), '*', this.getScreenWidth()));
		O.println(UString.tc("", '*', this.getScreenWidth()));
	}
	@Override protected Object onQuit(Engine Engine, Scope GlobalScope, PrintStream O, Object Return) {
		O = this.getPrintStream(O);
		
		O.println();
		O.println(UString.tc("", '*', this.getScreenWidth()));
		String S = " DONE!!! ";
		O.println(UString.tc(UString.lc(S, '*', (this.getScreenWidth() + S.length())/2), '*', this.getScreenWidth()));
		O.print(  UString.tc("", '*', this.getScreenWidth()));
		return Return;
	}
	
	// Executable ------------------------------------------------------------------------
	@Override protected Object onInvokeExecutable(Engine Engine, Scope GlobalScope, PrintStream O, Executable pExec) {
		O = this.getPrintStream(O);
		
		O.println();
		O.print(
			UString.tc(
				"---- " +
				((pExec instanceof OperationInfo)?((OperationInfo)pExec).getCurrentAsType() + ".":"") +
				pExec.getSignature().toString() + " ", '-', this.getScreenWidth()
			)
		);
		if(!this.IsEachExpr) System.out.println();
		return null;
	}
	
	// Expression ----------------------------------------------------------------------------------
	@Override protected Object onExecuteExpression(Engine Engine, Scope GlobalScope, PrintStream O, Expression Expr,
				Object[] pParams, int Level, Location Location, int pCurrentColumn, int pCurrentLineNumber,
				Executable Exec, Object SO, String Identification) {
		O = this.getPrintStream(O);
		
		String S = String.format("\n(%3d,%3d): %s%s", pCurrentColumn, pCurrentLineNumber, UString.repeat("--> ",Level), Expr.toString(Engine));
		O.print(UString.tc(S,' ', this.getExprWidth() - 1) + "|");
		return null;
	}
	
}