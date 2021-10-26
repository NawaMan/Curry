package net.nawaman.curry;

/** Executable that wrap other executable */
abstract public class WrapperExecutable implements Executable {
    
    private static final long serialVersionUID = -3009836901006887137L;
	
	// Only class in size Curry package can implement this class
	WrapperExecutable() {}

	/** The wrapped executable */
	abstract protected Executable getWrapped();
	
	// Cache of the signature (signature must not be changing over time)
	ExecSignature Signature = null;
	/** Returns the signature of the executable */
	final public ExecSignature getSignature() {
		if(this.Signature == null) this.Signature = this.getWrapped().getSignature();
		return this.Signature;
	}
	/**{@inheritDoc}*/ @Override
	public Location getLocation() {
		ExecSignature ES = this.getSignature();
		if(ES == null) return null;
		return ES.getLocation();
	}
	// Kind --------------------------------------------------------------------
	/** Returns the kind of this executable */
	abstract Executable.ExecKind getExecKind();
	/** Returns the kind of this executable */
	final public Executable.ExecKind getKind() {
		return this.getExecKind();
	}
	/** Checks if the executable is a fragment. **/
	final public boolean isFragment()      { return this.getWrapped().getKind().isFragment();   }
	/** Checks if the executable is a macro. **/
	final public boolean isMacro()         { return this.getWrapped().getKind().isMacro();      }
	/** Checks if the executable is a sub-routine. **/
	final public boolean isSubRoutine()    { return this.getWrapped().getKind().isSubRoutine(); }
	// Cast --------------------------------------------------------------------
	/** Returns the executable as a fragment. **/
	final public Fragment   asFragment()   { return this.getWrapped().asFragment();   }
	/** Returns the executable as a macro. **/
	final public Macro      asMacro()      { return this.getWrapped().asMacro();      }
	/** Returns the executable as a sub-routine. **/
	final public SubRoutine asSubRoutine() { return this.getWrapped().asSubRoutine(); }
	// Curry -------------------------------------------------------------------
	/** Checks if the executable is a curry executable */
	final public boolean isCurry() { return this.getWrapped().isCurry(); }
	/** Returns the executable as a Curry if it is or null if it is not */
	final public Curry   asCurry() { return this.getWrapped().asCurry(); }
	// Java --------------------------------------------------------------------
	/** Checks if the executable is a Java executable */
	final public boolean        isJava() { return this.getWrapped().isJava(); }
	/** Returns the executable as a Java if it is or null if it is not */
	final public JavaExecutable asJava() { return this.getWrapped().asJava(); }
	
	// Frozen variables and Recreation ----------------------------------------
	
	/**{@inheritDoc}*/ @Override
	final public String[] getFrozenVariableNames() {
		return this.getWrapped().getFrozenVariableNames();
	}
	/**{@inheritDoc}*/ @Override
	final public int getFrozenVariableCount() {
		return this.getWrapped().getFrozenVariableCount();
	}
	/**{@inheritDoc}*/ @Override
	final public String getFrozenVariableName(int I) {
		return this.getWrapped().getFrozenVariableName(I);
	}
	/**{@inheritDoc}*/ @Override
	public TypeRef getFrozenVariableTypeRef(Engine pEngine, int I) {
		return this.getWrapped().getFrozenVariableTypeRef(pEngine, I);
	}
	
	/** Recreate the executable based on the newly given frozen scope */
	abstract public Executable reCreate(Engine pEngine, Scope pFrozenScope);
	
	// Display -----------------------------------------------------------------
	public String toString(Engine pEngine) { return this.getWrapped().toString(pEngine); }          
	public String toDetail(Engine pEngine) { return this.getWrapped().toDetail(pEngine); }
	// Cloneable ---------------------------------------------------------------
	/** Clone this executable (needed in some rare case of templating) */
	abstract @Override public Executable clone();
	// Lock --------------------------------------------------------------------
	/** This method will help limiting the implementation of this interface to be within this package. */
	final public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) { return null; }
	// Objectable --------------------------------------------------------------
	@Override public String  toString()       { return this.getWrapped().toString(); }
	@Override public String  toDetail()       { return this.getWrapped().toDetail(); }
	@Override public boolean is(Object O)     { return this == O;                    }
	@Override public int     hash()           { return this.getWrapped().hash();     }
	@Override public int     hashCode()       { return this.getWrapped().hashCode(); }
	@Override public boolean equals(Object O) { return this.getWrapped().equals(O);  }
	
	// Expendable SubClasses -----------------------------------------------------------------------
	
	/** A wrapper of an executable */
	static abstract public class Wrapper extends WrapperExecutable {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		// Kind --------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override 
		final Executable.ExecKind getExecKind() {
			return this.getWrapped().getKind();
		}

		// Utilities ---------------------------------------------------------------------------------------------------
		
		/** Returns the wrapped */
		static public Executable getWrappedExecutable(Executable E) {
			if(!(E instanceof WrapperExecutable)) return E;
			return ((WrapperExecutable)E).getWrapped();
		}
		/** Returns the wrapped */
		static public Executable getDeepWrappedExecutable(Executable E) {
			if(!(E instanceof WrapperExecutable)) return E;
			while(E instanceof WrapperExecutable) E = ((WrapperExecutable)E).getWrapped();
			return E;
		}
		
		/** Returns the wrapped */
		static public AbstractExecutable getWrappedAbstractExecutable(Executable E) {
			if(E instanceof WrapperExecutable) E = ((WrapperExecutable)E).getWrapped();
			return (E instanceof AbstractExecutable)?(AbstractExecutable)E:null;
		}
		/** Returns the wrapped */
		static public AbstractExecutable getDeepWrappedAbstractExecutable(Executable E) { 
			while(E instanceof WrapperExecutable) E = ((WrapperExecutable)E).getWrapped();
			return (E instanceof AbstractExecutable)?(AbstractExecutable)E:null;
		}
	}
	
	// Transform Wrapper -----------------------------------------------------------------------------------------------

	/** A wrapper of that change the executable kind */
	static abstract class TransformWrapper<Source extends Executable, Target extends Executable> extends WrapperExecutable {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		Source SourceExec;
		public TransformWrapper(Source pSourceExec) {
			if(pSourceExec == null) throw new NullPointerException();
			this.SourceExec = pSourceExec;
		}
		/**{@inheritDoc}*/ @Override
		protected Executable getWrapped() {
			return this.SourceExec;
		}
	}

	/** A wrapper of fragment to a macro */
	static public class FragmentToMacroWrapper extends TransformWrapper<Fragment, Macro> implements Macro {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		public FragmentToMacroWrapper(Fragment pFragment) {
			super(pFragment);
		}
		/**{@inheritDoc}*/ @Override
		ExecKind getExecKind() {
			return ExecKind.Macro;
		}
		/**{@inheritDoc}*/ @Override
		public Executable clone() {
			return new FragmentToMacroWrapper((Fragment)this.getWrapped().clone());
		}
		
		/**{@inheritDoc}*/ @Override
		public Executable reCreate(Engine pEngine, Scope pFrozenScope) {
			return new FragmentToMacroWrapper((Fragment)(this.SourceExec.reCreate(pEngine, pFrozenScope)));
		}
	}

	/** A wrapper of fragment to a sub-routine */
	static public class FragmentToSubRoutineWrapper extends TransformWrapper<Fragment, SubRoutine> implements SubRoutine {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		public FragmentToSubRoutineWrapper(Fragment pFragment) {
			super(pFragment);
		}
		/**{@inheritDoc}*/ @Override ExecKind getExecKind() {
			return ExecKind.Macro;
		}
		/**{@inheritDoc}*/ @Override public Executable clone() {
			return new FragmentToSubRoutineWrapper((Fragment)this.getWrapped().clone());
		}
		
		/**{@inheritDoc}*/ @Override
		public Executable reCreate(Engine pEngine, Scope pFrozenScope) {
			return new FragmentToSubRoutineWrapper((Fragment)(this.SourceExec.reCreate(pEngine, pFrozenScope)));
		}
	}

	/** A wrapper of macro to a sub-routine */
	static public class MacroToSubRoutineWrapper extends TransformWrapper<Macro, SubRoutine> implements SubRoutine {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		public MacroToSubRoutineWrapper(Macro pMacro) {
			super(pMacro);
		}
		/**{@inheritDoc}*/ @Override ExecKind getExecKind() {
			return ExecKind.SubRoutine;
		}
		/**{@inheritDoc}*/ @Override public Executable clone() {
			return new MacroToSubRoutineWrapper((Macro)this.getWrapped().clone());
		}
		
		/**{@inheritDoc}*/ @Override
		public Executable reCreate(Engine pEngine, Scope pFrozenScope) {
			return new MacroToSubRoutineWrapper((Macro)(this.SourceExec.reCreate(pEngine, pFrozenScope)));
		}
	}

	/** A wrapper of a sub-routine to a macro */
	static public class SubRoutineToMacroWrapper extends TransformWrapper<SubRoutine, Macro> implements Macro {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		public SubRoutineToMacroWrapper(SubRoutine pSubRoutine) {
			super(pSubRoutine);
		}
		/**{@inheritDoc}*/ @Override Executable.ExecKind getExecKind() {
			return ExecKind.Macro;
		}
		/**{@inheritDoc}*/ @Override public Executable clone() {
			return new SubRoutineToMacroWrapper((SubRoutine)this.getWrapped().clone());
		}
		
		/**{@inheritDoc}*/ @Override
		public Executable reCreate(Engine pEngine, Scope pFrozenScope) {
			return new SubRoutineToMacroWrapper((SubRoutine)(this.SourceExec.reCreate(pEngine, pFrozenScope)));
		}
	}
}