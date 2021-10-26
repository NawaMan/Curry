package net.nawaman.curry;

/** Closure is a sub-routine that holds its own context. */
final public class Closure extends WrapperExecutable.Wrapper implements Executable.SubRoutine {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	/** Constructs a closure */
	Closure(Context pTheContext, Executable pExecutable) {
		while(pExecutable instanceof Closure) pExecutable = ((Closure)pExecutable).getSubRoutine();
		if(pExecutable == null) throw new NullPointerException();
		
		if(!(pExecutable instanceof SubRoutine)) {
			if(     pExecutable instanceof Macro)    pExecutable = new MacroToSubRoutineWrapper(   (Macro)   pExecutable);
			else if(pExecutable instanceof Fragment) pExecutable = new FragmentToSubRoutineWrapper((Fragment)pExecutable);
		}
		this.SubRoutine = (SubRoutine)pExecutable;
		this.TheContext = pTheContext;
	}
	
	final private Context               TheContext;
	final private Executable.SubRoutine SubRoutine;

	/** Returns the context for executing this closure */
	protected Context getTheContext() {
		return this.TheContext;
	}
	
	/** The wrapped executable */
	@Override protected Executable getWrapped() {
		return this.SubRoutine;
	}

	/**{@inheritDoc}*/ @Override
	public Closure clone() {
		throw new RuntimeException(new CloneNotSupportedException());
	}
	/**{@inheritDoc}*/ @Override
	public Closure reCreate(Engine pEngine, Scope pFrozenScope) {
		if((this.getFrozenVariableCount() == 0) || (pFrozenScope == null)) return this.clone();
		return new Closure(this.getTheContext(), this.getWrapped().reCreate(pEngine, pFrozenScope));
	}
	
	// Specially for closure ----------------------------------------------------------------------
	
	/** Checks if the closure is still alive */
	public boolean isAlive(Context pCurrentContext) {
		return (this.TheContext == null)?false:this.TheContext.isAlive(pCurrentContext);
	}
	
	/** Checks if the closure is still alive */
	public boolean isAlive(ExternalContext pCurrentContext) {
		return ((this.TheContext == null) || (pCurrentContext == null))
		        ?false
		        :this.TheContext.isAlive(pCurrentContext.pContext);
	}
	
	/** Returns the SubRoutine wrapped by this closure */
	public SubRoutine getSubRoutine() {
		return this.SubRoutine;
	}
	
	/** Returns the executable wrapped by this closure */
	public Executable getWrappedExecutable() {
		if(this.SubRoutine instanceof TransformWrapper<?, ?>)
			return ((TransformWrapper<?, ?>)this.SubRoutine).getWrapped();
		return this.SubRoutine;
	}
	
	public ExecKind getWrappedKind() {
		return this.getWrappedExecutable().getKind();
	}
}
