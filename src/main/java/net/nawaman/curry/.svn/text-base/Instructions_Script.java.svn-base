package net.nawaman.curry;

import net.nawaman.script.CompileOption;
import net.nawaman.script.ProblemContainer;
import net.nawaman.script.Script;
import net.nawaman.script.ScriptEngine;
import net.nawaman.script.ScriptEngineOption;
import net.nawaman.script.ScriptManager;
import net.nawaman.script.SimpleScriptExecutionExceptionWrapper;

// TODO Finish this

public class Instructions_Script {

	static public final class Inst_GetScriptEngine extends Inst_AbstractSimple {
		static public final String Name = "getScriptEngine";	// -420481
		Inst_GetScriptEngine(Engine pEngine) { super(pEngine, "=" + Name + "(+$):"+ScriptEngine.class.getCanonicalName()); }
		@Override protected Object run(Context pContext, Object[] pParams) {
			return ScriptManager.Instance.getDefaultEngineOf((String)pParams[0]);
		}
	}

	static public final class Inst_NewScriptEngine extends Inst_AbstractSimple {
		static public final String Name = "newScriptEngine";	// -42422
		Inst_NewScriptEngine(Engine pEngine) {
			super(pEngine, "=" + Name + "(+$,"+ScriptEngineOption.class.getCanonicalName()+"):"+ScriptEngine.class.getCanonicalName());
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return ScriptManager.Instance.newEngine((String)pParams[0], (ScriptEngineOption)pParams[1]);
		}
	}

	static public final class Inst_CompileScript extends Inst_AbstractSimple {
		static public final String Name = "compileScript";	// -350785
		Inst_CompileScript(Engine pEngine) {
			super(pEngine, "=" + Name + "(+"+ScriptEngine.class.getCanonicalName()+",+$,$[],"
					+CompileOption.class.getCanonicalName()+"):"+Script.class.getCanonicalName());
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			ScriptEngine  SE      = (ScriptEngine) pParams[0];
			String        Code    = (String)       pParams[1];
			String[]      FVNames = (String[])     pParams[2];
			CompileOption COption = (CompileOption)pParams[3];
			ProblemContainer PC = SE.newCompileProblemContainer();
			// Frozen scope is null because we want the 
			Script S = null;
			
			try {
				S = SE.newScript(Code, null, FVNames, COption, PC);
				if(PC.hasProblem())
					return new SpecialResult.ResultError(new CurryError("There is a problem compiling a script:\n" + PC.toString()));
			} catch(SimpleScriptExecutionExceptionWrapper SSEEW) {
				return new SpecialResult.ResultError(new CurryError("There is a problem compiling a script.", SSEEW.getCause()));
			}
			
			// Returns the result
			return S;
		}
	}
	
	/*
	/** Create a new Script object from the code * /
	public Script newScript(String pCode, Scope pFrozen, String[] pFrozenVNames,
			CompileOption pOption, ProblemContainer pResult);
	
	/** Creates a new macro * /
	public Macro newMacro(Signature pSignature, String[] pParamNames, String pCode, Scope pFrozen, String[] pFrozenVNames,
			CompileOption pOption, ProblemContainer pResult);
	
	/** Creates a new function * /
	public Function newFunction(Signature pSignature, String[] pParamNames, String pCode, Scope pFrozen, String[] pFrozenVNames,
			CompileOption pOption, ProblemContainer pResult);
	 */
	
}
