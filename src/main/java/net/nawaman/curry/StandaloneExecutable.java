package net.nawaman.curry;

/** Executable that embedded the engine and ready to be execute. */
abstract public class StandaloneExecutable extends WrapperExecutable.Wrapper {
	
	Engine     Engine;
	Executable Exec;
	
	static public class Fragment extends StandaloneExecutable implements Executable.Fragment {
		Fragment(Engine pEngine, Executable pExec) {
			super(pEngine, pExec);
		}
		
		/**{@inheritDoc}*/ @Override
		public StandaloneExecutable.Fragment clone() {
			return new StandaloneExecutable.Fragment(this.Engine, this.Exec);
		}
		/**{@inheritDoc}*/ @Override
		public StandaloneExecutable.Fragment reCreate(Engine pEngine, Scope pFrozenScope) {
			if((this.getFrozenVariableCount() == 0) || (pFrozenScope == null)) return this.clone();
			return new StandaloneExecutable.Fragment(this.Engine, this.Exec.reCreate(pEngine, pFrozenScope));
		}
	}
	static public class Macro extends StandaloneExecutable implements Executable.Macro {
		Macro(Engine pEngine, Executable pExec) {
			super(pEngine, pExec);
		}
		
		/**{@inheritDoc}*/ @Override
		public StandaloneExecutable.Macro clone() {
			return new StandaloneExecutable.Macro(this.Engine, this.Exec);
		}
		/**{@inheritDoc}*/ @Override
		public StandaloneExecutable.Macro reCreate(Engine pEngine, Scope pFrozenScope) {
			if((this.getFrozenVariableCount() == 0) || (pFrozenScope == null)) return this.clone();
			return new StandaloneExecutable.Macro(this.Engine, this.Exec.reCreate(pEngine, pFrozenScope));
		}
	}
	static public class SubRoutine extends StandaloneExecutable implements Executable.SubRoutine {
		SubRoutine(Engine pEngine, Executable pExec) {
			super(pEngine, pExec);
		}
		
		/**{@inheritDoc}*/ @Override
		public StandaloneExecutable.SubRoutine clone() {
			return new StandaloneExecutable.SubRoutine(this.Engine, this.Exec);
		}
		/**{@inheritDoc}*/ @Override
		public StandaloneExecutable.SubRoutine reCreate(Engine pEngine, Scope pFrozenScope) {
			if((this.getFrozenVariableCount() == 0) || (pFrozenScope == null)) return this.clone();
			return new StandaloneExecutable.SubRoutine(this.Engine, this.Exec.reCreate(pEngine, pFrozenScope));
		}
	}
	
	// Factory Methods -------------------------------------------------------------------
	
	/** Create a new StandAlone */
	static public Fragment newFragment(Engine pEngine, Executable.Fragment pExec) {
		return new Fragment(pEngine, pExec);
	}
	/** Create a new StandAlone */
	static public Macro newMacro(Engine pEngine, Executable.Macro pExec) {
		return new Macro(pEngine, pExec);
	}
	/** Create a new StandAlone */
	static public SubRoutine newSubRoutine(Engine pEngine, Executable.SubRoutine pExec) {
		return new SubRoutine(pEngine, pExec);
	}
	
	/** Create a new StandAlone */
	static public Fragment newSE(Engine pEngine, Executable.Fragment pExec) {
		return new Fragment(pEngine, pExec);
	}
	/** Create a new StandAlone */
	static public Macro newSE(Engine pEngine, Executable.Macro pExec) {
		return new Macro(pEngine, pExec);
	}
	/** Create a new StandAlone */
	static public SubRoutine newSE(Engine pEngine, Executable.SubRoutine pExec) {
		return new SubRoutine(pEngine, pExec);
	}
	
	// Constructor -----------------------------------------------------------------------
	
	/** Constructs a Stand-Alone executable */
	StandaloneExecutable(Engine pEngine, Executable pExec) {
		if(pEngine == null) throw new NullPointerException();
		this.Engine = pEngine;
		this.Exec   = pExec;
	}
	
	/** Returns the engine that will run this executable */
	public Engine getEngine() { return this.Engine; }
	
	/** Checks if this Stand-Alone executable does not need a parameter */
	public boolean isProcedure() {
		if(this.getSignature() == null)              return true;
		if(this.getSignature().getParamCount() == 0) return true;
		return false;
	}
	
	/** Returns the wrapped executable */
	@Override protected Executable getWrapped() { return this.Exec; }
	
	/** Returns the cloned of this Stand-Alone executable */
	@Override abstract public StandaloneExecutable clone();

	/** Recreate the executable based on the newly given frozen scope */
	@Override abstract public Executable reCreate(Engine pEngine, Scope pFrozenScope);
	
	// Executing -----------------------------------------------------------------------------------

	/** Execute with the parameters */
	public Object exec(Object ... Params) { return this.execute(null, Params); }
	
	/** Execute */
	public Object execute() { return this.execute((Scope)null); }
	/** Execute with the parameters and the global scope */
	public Object execute(Scope pGlobalScope) {
		if(this.Exec == null)      return null;
		
		if(this.Exec.isFragment())   return this.Engine.getExecutableManager().runFragment(pGlobalScope, this.Exec.asFragment());
		if(this.Exec.isMacro())      return this.Engine.getExecutableManager().execMacro(  pGlobalScope, this.Exec.asMacro());
		if(this.Exec.isSubRoutine()) return this.Engine.getExecutableManager().callSubRoutine(this.Exec.asSubRoutine());
		return null;
	}
	/** Execute with the parameters */
	public Object execute(Object[] Params) {
		return this.execute(null, Params);
	}
	/** Execute with the parameters and the global scope */
	public Object execute(Scope pGlobalScope, Object[] Params) {
		if(this.Exec == null)        return null;
		if(this.Exec.isFragment())   return this.Engine.getExecutableManager().runFragment(   pGlobalScope, this.Exec.asFragment());
		if(this.Exec.isMacro())      return this.Engine.getExecutableManager().execMacro(     pGlobalScope, this.Exec.asMacro(), Params);
		if(this.Exec.isSubRoutine()) return this.Engine.getExecutableManager().callSubRoutine(this.Exec.asSubRoutine(), Params);
		return null;
	}
	
	// Debugging -----------------------------------------------------------------------------------

	/** Debug */
	public Object debug(Debugger pDebugger) { return this.debug((Scope)null, pDebugger); }
	/** Debug with the parameters and the global scope */
	public Object debug(Scope pGlobalScope, Debugger pDebugger) {
		if(this.Exec == null)      return null;
		if(this.Exec.isFragment()) return this.Engine.getExecutableManager().debugFragment(pGlobalScope, pDebugger, this.Exec.asFragment());
		if(this.Exec.isMacro())    return this.Engine.getExecutableManager().debugMacro(   pGlobalScope, pDebugger, this.Exec.asMacro());
		return null;
	}
	/** Debug with the parameters */
	public Object debug(Debugger pDebugger, Object[] Params) {
		return this.debug(null, pDebugger, Params);
	}
	/** Debug with the parameters and the global scope */
	public Object debug(Scope pGlobalScope, Debugger pDebugger, Object[] Params) {
		if(this.Exec == null)      return null;
		if(this.Exec.isFragment()) return this.Engine.getExecutableManager().debugFragment(pGlobalScope, pDebugger, this.Exec.asFragment());
		if(this.Exec.isMacro())    return this.Engine.getExecutableManager().debugMacro(   pGlobalScope, pDebugger, this.Exec.asMacro(), Params);
		return null;
	}
}
