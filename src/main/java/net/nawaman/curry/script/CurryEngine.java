package net.nawaman.curry.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import net.nawaman.curry.CurryInputStream;
import net.nawaman.curry.CurryOutputStream;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Location;
import net.nawaman.curry.Executable.Fragment;
import net.nawaman.curry.Executable.SubRoutine;
import net.nawaman.curry.compiler.CompileProductContainer;
import net.nawaman.curry.compiler.CurryLanguage;
import net.nawaman.curry.compiler.GetCurryLanguage;
import net.nawaman.curry.script.CurryProblem.CurryProblemContainer;
import net.nawaman.script.CompileOption;
import net.nawaman.script.CompiledCode;
import net.nawaman.script.Executable;
import net.nawaman.script.ExecutableInfo;
import net.nawaman.script.Function;
import net.nawaman.script.Macro;
import net.nawaman.script.ProblemContainer;
import net.nawaman.script.Scope;
import net.nawaman.script.Script;
import net.nawaman.script.ScriptEngine;
import net.nawaman.script.ScriptEngineOption;
import net.nawaman.script.ScriptManager;
import net.nawaman.script.Signature;
import net.nawaman.script.Utils;
import net.nawaman.usepath.FileExtFilter;
import net.nawaman.usepath.FileExtUsableFilter;
import net.nawaman.usepath.UsableFilter;
import net.nawaman.util.UString;

/** ScriptEngine for Curry */
public class CurryEngine implements ScriptEngine {
	
	/** The short Signature Spec */
	static public final String         DEFAULT_ENGINE_NAME = "Curry";
	
	/** The short Signature Spec */
	static public final String         SIMPLE_KIND_NAME = "CurryLanguage";
	/** The short Signature Spec */
	static public final String         SIMPLE_KIND_SPEC = "{ "+SIMPLE_KIND_NAME+" }";
	/** The full Signature Spec */
	static public final String         SIGNATURE_SPEC   = String.format("{ function ():%s }", GetCurryLanguage.class.getCanonicalName());
	/** The default Signature for TypeDefinition */
	static public final Signature      CLANG_SIGNATURE  = new Signature.Simple("getCurryLanguage", GetCurryLanguage.class);
	/** The default ExecutableInfo for TypeDefinition */
	static public final ExecutableInfo CLANG_EXECINFO   = new ExecutableInfo(null, "function", CLANG_SIGNATURE, SIGNATURE_SPEC, null);

	
	// Static attributes and operations --------------------------------------------------------------------------------
	// These are utility methods used for Customed CurryEngine as name/Engine/Language is selected by itself (with
	//     getName(...) and getShortName(...)).
	
	/** Register a given curry engine */
	static public boolean registerCurryEngine(CurryEngine pCurryEngine) {
		if(pCurryEngine == null) return false;
		ScriptManager.Instance.registerEngine(pCurryEngine);
		return true;
	}

	/** Register a new current engine form the curry language (as the language contains a target curry engine) */
	static public boolean registerCurryEngine(CurryLanguage pCLanguage) {
		if(pCLanguage == null) return false;
		ScriptManager.Instance.registerEngine(
				new CurryEngine(pCLanguage.getName(), null, pCLanguage.getTargetEngine(), pCLanguage));
		return true;
	}
	
	// ---------------------------------------------------------------------------------------------
	// These are utilities method to creates/loads/caches generic CurryEngine or the engine that are the result of
	//    the combination of Engine and Language. Another curry-based ScriptEngine that does not support these
	//    modulation (for whatever reason) should take care of itsown registration

	static HashMap<String, CurryEngine> CurryEngines = new HashMap<String, CurryEngine>();

	/** Ensure that TPackageScriptEngine is registed. */
	static public void EnsureEngineRegisted() {
		ScriptManager.Instance.getDefaultEngineOf(CurryEngine.class.getCanonicalName());
	}
	
	// Make it very sure that the engine is registed.
	static {
		EnsureEngineRegisted();
	}
	
	/** Returns the CurryEngine from the ScriptEngineOption */
	static public CurryEngine newInstance() {
		return newInstance(null);
	}	
	/** Returns the CurryEngine from the ScriptEngineOption */
	static public CurryEngine newInstance(ScriptEngineOption pOption) {
		String Param = (pOption instanceof ScriptEngineOption.Simple) ? pOption.toString() : null;
		return getEngine(Param, false);
	}

	/** Returns the CurryEngine from the Parameter Text */
	static public CurryEngine getEngine() {
		return getEngine(null, false);
	}
	/** Returns the CurryEngine from the Parameter Text */
	static public CurryEngine getEngine(String Param) {
		return getEngine(Param, false);
	}
	/** Returns the CurryEngine from the Parameter Text */
	static public CurryEngine getEngine(String Param, boolean IsForceCreate) {
		
		if((Param != null) && (Param.length() == 0)) Param = null;
		
		CurryEngine CEngine = null;
		
		if(!IsForceCreate) {
			CEngine = CurryEngines.get(Param);
			if(CEngine != null) return CEngine;
		}
		
		// Create a new one
		
		String LangName    = null;
		String EngineName  = null;

		// Split the parameter by ":"
		String[] Params = (Param == null) ? UString.EmptyStringArray : Param.split(":");

		// Get the Language name and Engine name
		if(Params.length > 1) EngineName = Params[1];
		
		// Get the language
		CurryLanguage CLanguage = CurryLanguage.Util.GetCurryLanguage(LangName, EngineName, false);
		if(CLanguage == null) return null;
		
		// Returns it
		CEngine = new CurryEngine("Curry", Param, CLanguage.getTargetEngine(), CLanguage);
		
		// Add if not there
		if(!CurryEngines.containsKey(Param)) CurryEngines.put(Param, CEngine);
		
		return CEngine;
	}
	
	// Non-static attributes and operations ----------------------------------------------------------------------------

	/** Constructs a curry engine */
	protected CurryEngine(String pName, String pParam, Engine pEngine, CurryLanguage pCLanguage) {
		if((pEngine == null) || (pCLanguage == null)) throw new NullPointerException();
		this.TheEngine   = pEngine;
		this.Name        = pName;
		this.TheLanguage = pCLanguage;
		this.TheOption   = (pParam == null) ? null : new ScriptEngineOption.Simple(pParam);
	}
	
	String             Name;
	Engine             TheEngine;
	CurryLanguage      TheLanguage;
	ScriptEngineOption TheOption;
	
	/** Returns the actual curry engine */
	final public Engine getTheEngine() {
		return this.TheEngine;
	}
	/** Returns the curry compile */
	final public CurryLanguage getTheLanguage() {
		return this.TheLanguage;
	}
	
	/**{@inheritDoc}*/@Override
	public String getName() {
		return CurryEngine.class.getCanonicalName();
	}
	/**{@inheritDoc}*/@Override
	public String getShortName() {
		return this.Name;
	}
	
	/**{@inheritDoc}*/@Override
	public ScriptEngineOption getOption() {
		return this.TheOption;
	}
	/**{@inheritDoc}*/@Override
	public ScriptEngineOption getOption(String pParam) {
		return new ScriptEngineOption.Simple(pParam);
	}
	/**{@inheritDoc}*/@Override
	public String getParameterString() {
		return (this.TheOption == null) ? "" : this.TheOption.toString();
	}

	/**{@inheritDoc}*/ @Override
	public ExecutableInfo getReplaceExecutableInfo(ExecutableInfo EInfo) {
		return null;
	}

	// Usable Filters (File filter) ------------------------------------------------------------------------------------

	/** Filter for CURRYFile only */
	static class CURRYFileFilter extends FileExtUsableFilter {
		public CURRYFileFilter() {
			super(new FileExtFilter.ExtListFileFilter("curry"));
		}
	}
	
	static CURRYFileFilter CURRYFileFilter = new CURRYFileFilter();   
	
	/**{@inheritDoc}*/ @Override
	public UsableFilter[] getUsableFilters() {
		if(CURRYFileFilter == null) CURRYFileFilter = new CURRYFileFilter(); 
		return new UsableFilter[] { CURRYFileFilter };
	}
	
	// Executable ------------------------------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/@Override
	public Scope newScope() {
		return new CurryScope(this.TheEngine, new net.nawaman.curry.Scope());
	}
	/**{@inheritDoc}*/@Override
	public Scope getCompatibleScope(Scope pOrg) {
		if(pOrg instanceof CurryScope) return (CurryScope)pOrg;
		CurryScope CS = new CurryScope(this.TheEngine, new net.nawaman.curry.Scope());
		if(pOrg == null) return CS;
		Scope.Simple.duplicate(pOrg, CS);
		return CS;
	}
	
	/**{@inheritDoc}*/ @Override
	public ProblemContainer newCompileProblemContainer() {
		return new CurryProblemContainer();
	}
	
	/**{@inheritDoc}*/ @Override
	public boolean isCompilable() {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	public boolean isCompiledCodeSerializable() {
		return true;
	}
	
	/**{@inheritDoc}*/ @Override
	public Object eval(String pCode, Scope pScope, ProblemContainer pResult) {
		CompiledCode CC = this.compile(pCode, null, null, null, pResult);
		if(CC == null) return null;
		if(!(CC instanceof CurryCompiledCode)) return null;

		return this.eval(CC, pScope, pResult);
	}
	
	/**{@inheritDoc}*/@Override
	public Object eval(CompiledCode pCode, Scope pScope, ProblemContainer pResult) {
		if(!(pCode instanceof CurryCompiledCode)) return null;
		
		// Get scrope
		pScope = this.getCompatibleScope(pScope);
		net.nawaman.curry.Scope Scope = (pScope == null) ? null : ((CurryScope)pScope).TheScope;
		
		// Execute the fragment
		return this.TheEngine.getExecutableManager().runFragment(Scope, ((CurryCompiledCode)pCode).TheFragment);
	}
	
	/**{@inheritDoc}*/@Override
	public Object eval(Script pScript, Scope pScope, ProblemContainer pResult) {
		if(!(pScript instanceof CurryScript))    return null;
		if(((CurryScript)pScript).CCode == null) return null;
		
		// Get scrope
		pScope = this.getCompatibleScope(pScope);
		net.nawaman.curry.Scope Scope = (pScope == null) ? null : ((CurryScope)pScope).TheScope;

		// Execute the fragment
		return this.TheEngine.getExecutableManager().runFragment(Scope, ((CurryScript)pScript).CCode.TheFragment);
	}
	
	/**{@inheritDoc}*/@Override
	public CompiledCode compile(String pCode, Scope pFrozen, String[] pFrozenVNames, CompileOption pOption,
			ProblemContainer pResult) {
		
		CurryCompiledOption CCOption = null;
		if(pOption instanceof CurryCompiledOption)
			CCOption = (CurryCompiledOption)pOption;
		
		if(pFrozenVNames != null) {
			if(CCOption == null) CCOption = new CurryCompiledOption();
			if(pFrozen  == null) pFrozen  = this.newScope();
			
			CCOption.setFrozens(pFrozenVNames);
			CCOption.setTopScope((net.nawaman.curry.Scope)((CurryScope)this.getCompatibleScope(pFrozen)).TheScope);
		}
		
		CompileProductContainer CPC = new CompileProductContainer();
		Fragment F = this.getTheLanguage().compileFragment("_noname_", pCode, CCOption, CPC);
		if(F == null) return null;

		// Set the problem if the result is given
		if(pResult instanceof CurryProblemContainer)
			CurryProblem.SetProblemContainer((CurryProblemContainer)pResult, CPC.getCompileProduct());
		// Print it out if no container is given
		else if(CPC.getCompileProduct().getMessageCount() != 0)
			 System.err.println(CPC.getCompileProduct().toString());
		
		return new CurryCompiledCode(this, F);
	}

	/**{@inheritDoc}*/@Override
	public void reCompile(Script pScript, Scope pFrozen, CompileOption pOption, ProblemContainer pResult) {
		if(pScript == null) return;
		
		if(!(pScript instanceof CurryScript)) {
			pScript.getEngine().reCompile(pScript, pFrozen, pOption, pResult);
			return;
		}
		
		CurryScript CS = (CurryScript)pScript;
		CS.CCode = (CurryCompiledCode)this.compile(CS.Code, pFrozen, CS.getFVInfos().getFrozenVariableNames(), pOption, pResult);
		
		return;
	}
	
	/**{@inheritDoc}*/@Override
	public Script newScript(String pCode, Scope pFrozen, String[] pFrozenVNames, CompileOption pOption,
			ProblemContainer pResult) {
				
		CurryCompiledCode CCC = (CurryCompiledCode)this.compile(pCode, pFrozen, pFrozenVNames, pOption, pResult);
		if(CCC == null) return null;
		
		return new CurryScript(this, pCode, CCC);
	}
	
	/**{@inheritDoc}*/@Override
	public Macro newMacro(Signature pSignature, String[] pParamNames, String pCode, Scope pFrozen, String[] pFrozenVNames,
			CompileOption pOption, ProblemContainer pResult) {

		CurryCompiledOption CCOption = null;
		if(pOption instanceof CurryCompiledOption)
			CCOption = (CurryCompiledOption)pOption;
		
		if(pFrozenVNames != null) {
			if(CCOption == null) CCOption = new CurryCompiledOption();
			if(pFrozen  == null) pFrozen  = this.newScope();
			
			CCOption.setFrozens(pFrozenVNames);
			CCOption.setTopScope((net.nawaman.curry.Scope)((CurryScope)this.getCompatibleScope(pFrozen)).TheScope);
		}

		String LName = ((CCOption == null) ? "" : (CCOption.getCodeName() + "::")) + pSignature.getName();
		
		CompileProductContainer            CPC   = new CompileProductContainer();
		CurryLanguage                      CLang = this.getTheLanguage();
		ExecSignature                      ES    = CurrySignature.toExecSignature(this.TheEngine, pSignature, pParamNames, new Location(LName));
		net.nawaman.curry.Executable.Macro Macro = CLang.compileMacro(ES, pCode, CCOption, CPC);

		// Set the problem if the result is given
		if(pResult instanceof CurryProblemContainer)
			CurryProblem.SetProblemContainer((CurryProblemContainer)pResult, CPC.getCompileProduct());		
		// Print it out if no container is given
		else if(CPC.getCompileProduct().getMessageCount() != 0)
			 System.err.println(CPC.getCompileProduct().toString());
		
		if(Macro == null) return null;
				
		return new CurryMacro(this, pCode, Macro);
	}
	
	/**{@inheritDoc}*/ @Override
	public Function newFunction(Signature pSignature, String[] pParamNames, String pCode, Scope pFrozen, String[] pFrozenVNames,
			CompileOption pOption, ProblemContainer pResult) {

		CurryCompiledOption CCOption = null;
		if(pOption instanceof CurryCompiledOption)
			CCOption = (CurryCompiledOption)pOption;
		
		if(pFrozenVNames != null) {
			if(CCOption == null) CCOption = new CurryCompiledOption();
			if(pFrozen  == null) pFrozen  = this.newScope();
			
			CCOption.setFrozens(pFrozenVNames);
			CCOption.setTopScope((net.nawaman.curry.Scope)((CurryScope)this.getCompatibleScope(pFrozen)).TheScope);
		}
		
		String LName = ((CCOption == null) ? "" : (CCOption.getCodeName() + "::")) + pSignature.getName();
		
		CompileProductContainer CPC   = new CompileProductContainer();
		CurryLanguage           CLang = this.getTheLanguage();
		ExecSignature           ES    = CurrySignature.toExecSignature(this.TheEngine, pSignature, pParamNames, new Location(LName));
		SubRoutine              SubR  = CLang.compileSubRoutine(ES, pCode, CCOption, CPC);
		
		// Set the problem if the result is given
		if(pResult instanceof CurryProblemContainer)
			CurryProblem.SetProblemContainer((CurryProblemContainer)pResult, CPC.getCompileProduct());		
		// Print it out if no container is given
		else if(CPC.getCompileProduct().getMessageCount() != 0)
			 System.err.println(CPC.getCompileProduct().toString());
		
		if(SubR == null) return null;
				
		return new CurryFunction(this, pCode, SubR);
	}

	/** Create an Executable using the ExecutableInfo and the Code */ @Override
	public Executable compileExecutable(ExecutableInfo pExecInfo, String pCode, CompileOption pOption,
			ProblemContainer pResult) {
		if(pExecInfo == null) pExecInfo = ExecutableInfo.DefaultExecutableInfo;
		
		// Unit
		//if("unit".equals(pExecInfo.Kind)) return Engine.newScript(pCode, null, null, pOption, pResult);
		
		if(!(pOption instanceof CurryCompiledOption)) pOption = new CurryCompiledOption();
		((CurryCompiledOption)pOption).setCodeName(pExecInfo.FileName);
		
		return Utils.compileExecutable(this, pExecInfo, pCode, pOption, pResult);
	}

	/** Returns the long comments of the given comment text */
	public String getLongComments(String Comment, int Width) {
		int WidthMinusOne = Width - 1;
		StringBuilder SB = new StringBuilder();
		while(SB.length() <  WidthMinusOne) SB.append("*");
		return String.format("/%1$s\n%2$s\n%1$s/", SB.toString(), Comment);
	}
	
	/**{@inheritDoc}*/ @Override
	public ObjectOutputStream newExecutableObjectOutputStream(OutputStream OS) {
		try { return CurryOutputStream.newCOS(this.getTheEngine(), OS); }
		catch (IOException E) { throw new RuntimeException(E); }
	}
	/**{@inheritDoc}*/ @Override
	public ObjectInputStream newExecutableObjectInputStream(InputStream IS) {
		try { return CurryInputStream.newCIS(this.getTheEngine(), IS); }
		catch (IOException            E) { throw new RuntimeException(E); }
		catch (ClassNotFoundException E) { throw new RuntimeException(E); }
	}

	static public void main(String[] args) {
		/*
		// Create Language
		PTypeProvider TPackage = null;
		try { TPackage = PTypePackage.loadAsPocketFromFile("/home/nawaman/Documents/Others/AppData/Eclipse/Curry/CurryCompiler.tpp"); }
		catch(Exception E) { System.err.println(E); throw new RuntimeException(E); }
		
		// Create Engine
		Engine        TheEngine   = net.nawaman.curry.Engine.newEngine(EngineSpec.newSimpleEngineSpec("Curry"));
		CurryLanguage TheLanguage = new CLRegParser("Curry", TheEngine, TPackage);
		CurryEngine.registerCurryEngine(TheLanguage);
		
		
		*/
		
		
		
		//ScriptManager.Instance.registerEngine(CurryEngine.getCurryEngineByName("Curry"));
		
		CurryEngine CEngine = CurryEngine.getEngine(); //(CurryEngine)ScriptManager.Instance.getDefaultEngineOf("Curry");
		
		//UseCurryEngine();
		
		//CurryEngine.registerCurryEngine(CEngine);
		
		Scope Scope = null;
		
		ProblemContainer PC = new ProblemContainer();

		System.out.println("Eval ------------------------------------------------------------------------------------");
		System.out.println(CEngine.eval("5;",           null, null));
		System.out.println(CEngine.eval("@:plus(5,7);", null, null));
		
		System.out.println();
		System.out.println("Compiled Code ---------------------------------------------------------------------------");
		CompiledCode CCode = CEngine.compile("@:plus(5,6);", null, null, null, null);
		System.out.println(CEngine.eval(CCode, null, null));
		
		Scope = CEngine.newScope();
		Scope.newVariable("I", Integer.class, 8);
		CCode = CEngine.compile("@:plus(5,@:getVarValue(`I`));", Scope, new String[] { "I" }, null, null);
		System.out.println(CEngine.eval(CCode, null, null));
		
		System.out.println();
		System.out.println("Script ----------------------------------------------------------------------------------");
		Script Script = CEngine.newScript("@:plus(5,9);", null, null, null, null);
		System.out.println(Script.run());

		Scope.setValue("I", 10);
		Script = CEngine.newScript("@:plus(5,@:getVarValue(`I`));", Scope, new String[] { "I" }, null, null);
		System.out.println(Script.run());

		Scope.setValue("I", 11);
		Script = CEngine.newScript("@:plus(5,(:Integer ? #:getVarValue(`I`)));", null, null, null, null);
		System.out.println(Script.run(Scope));
		
		System.out.println();
		System.out.println("Macro -----------------------------------------------------------------------------------");
		Macro Macro = CEngine.newMacro(
				new Signature.Simple("AddFive", Integer.class, false, Integer.class),
				new String[] { "I" }, "@:plus(5,@:getVarValue(`I`));", null, null, null, PC);
		
		if(PC.getProblemCount() != 0) System.out.println(PC);
		else                          System.out.println(Macro.run(12));

		Scope.newVariable("C", Integer.class, 5);
		Macro = CEngine.newMacro(
				new Signature.Simple("AddFive", Integer.class, false, Integer.class),
				new String[] { "I" }, "@:plus(@:getVarValue(`C`),@:getVarValue(`I`));", Scope, new String[] { "C" }, null, PC);
		
		if(PC.getProblemCount() != 0) System.out.println(PC);
		else                          System.out.println(Macro.run(13));

		Macro = CEngine.newMacro(
				new Signature.Simple("AddFive", Integer.class, false, Integer.class),
				new String[] { "I" }, "@:plus((:Integer ? #:getVarValue(`C`)),@:getVarValue(`I`));", null, null, null, PC);
		
		if(PC.getProblemCount() != 0) System.out.println(PC);
		else                          System.out.println(Macro.run(Scope, 14));
		
		System.out.println();
		System.out.println("Function ------------------------------------------------------------------------------");
		Function Function = CEngine.newFunction(
				new Signature.Simple("AddFive", Integer.class, false, Integer.class),
				new String[] { "I" }, "@:plus(5,@:getVarValue(`I`));", null, null, null, PC);
		
		if(PC.getProblemCount() != 0) System.out.println(PC);
		else                          System.out.println(Function.run(15));

		Scope.newVariable("C", Integer.class, 5);
		Function = CEngine.newFunction(
				new Signature.Simple("AddFive", Integer.class, false, Integer.class),
				new String[] { "I" }, "@:plus(@:getVarValue(`C`),@:getVarValue(`I`));", Scope, new String[] { "C" }, null, PC);
		
		if(PC.getProblemCount() != 0) System.out.println(PC);
		else                          System.out.println(Function.run(16));
		
		Function F = (Function)ScriptManager.Use("source/TestInSide");
		System.out.println(F);
		System.out.println(F.run((Object[])null));
		
		F = (Function)ScriptManager.Use("source/TestInSide");
		System.out.println(F);
		System.out.println(F.run((Object[])null));
	}
}
