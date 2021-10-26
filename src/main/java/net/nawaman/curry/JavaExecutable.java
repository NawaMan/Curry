package net.nawaman.curry;

import net.nawaman.curry.util.MoreData;

/** Java executable. **/
abstract public class JavaExecutable extends AbstractExecutable {
    
    private static final long serialVersionUID = -3009836901006887137L;
	
	/** Constructs this a normal way */
	protected JavaExecutable(Engine pEngine, ExecSignature pSignature, String[] pFVNames, Scope pFrozenScope) {
		super(pEngine, pFVNames, pFrozenScope);
		this.Signature = pSignature;
		if(this.Signature == null) throw new NullPointerException();
	}
	// Information -------------------------------------------------------------
	ExecSignature Signature;
	/**{@inheritDoc}*/ @Override
	public ExecSignature getSignature() {
		return this.Signature;
	}
	/**{@inheritDoc}*/ @Override
	final public Location getLocation() {
		if(this.Signature == null) return null;
		return this.Signature.getLocation();
	}
	// Body -------------------------------------------------------------------- 
	// For display
	/**{@inheritDoc}*/ @Override
	protected int getBodyHash() {
		return "...".hashCode();
	}
	/**{@inheritDoc}*/ @Override
	protected String getBodyStr(Engine pEngine) {
		return "<< Java >>";
	}
	// Clone -------------------------------------------------------------------
	/**{@inheritDoc}*/ @Override public JavaExecutable clone() {
		return this;
	}
	// Executing ---------------------------------------------------------------
	/** Executing this -  For internal to change */
	abstract Object run(Context pContext, Object[] pParams);
	
	/** Actually run the executable - do check  */
	final Object runRaw(Context pContext, Object[] pParams) {
		// NOTE - There is no need to check the parameter the function that call it should do.
		
		Object R = this.run(pContext, pParams);
		if(R instanceof SpecialResult) return R; // Special result
		return R;
	}
	
	// Simple native -----------------------------------------------------------
	
	/** Simple Java Fragment */
	static public abstract class JavaFragment_Simple extends JavaExecutable implements Executable.Fragment {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		JavaFragment_Simple(Engine pEngine, ExecSignature pSignature, String[] pFVNames, Scope pFrozenScope) {
			super(pEngine, pSignature, pFVNames, pFrozenScope);
			if(pSignature.getParamCount() != 0)
				throw new IllegalArgumentException("Signature of a fragment must has no parameter."); 
		}
		/** Construct a new native fragment */
		public JavaFragment_Simple(String pName, TypeRef pReturnTypeRef, Location pLocation, MoreData pExtraData) {
			this(null, pName, pReturnTypeRef, pLocation, pExtraData, null, null);
		}
		/** Construct a new native fragment */
		public JavaFragment_Simple(Engine pEngine, String pName, TypeRef pReturnTypeRef, Location pLocation,
				MoreData pExtraData, String[] pFVNames, Scope pFrozenScope) {
			this(pEngine, ExecSignature.newProcedureSignature(pName,
				 (pReturnTypeRef == null)?TKJava.TVoid.getTypeRef():pReturnTypeRef, pLocation, pExtraData),
				 pFVNames, pFrozenScope);
		}
		// Executing -----------------------------------------------------------
		/** Executing this -  For external*/
		abstract protected Object run();
		/** Executing this -  For internal to change */
		@Override Object run(Context pContext, Object[] pParams) {
			return this.run();
		}
	}
	
	/** Simple Java Macro */
	static public abstract class JavaMacro_Simple extends JavaExecutable implements Executable.Macro {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		/** Construct a new native macro */
		public JavaMacro_Simple(Engine pEngine, ExecSignature pSignature, String[] pFVNames, Scope pFrozenScope) {
			super(pEngine, pSignature, pFVNames, pFrozenScope);
		}
		/** Construct a new native macro */
		public JavaMacro_Simple(ExecSignature pSignature) {
			super(null, pSignature, null, null);
		}
		// Executing -----------------------------------------------------------
		/** Executing this -  For external*/
		abstract protected Object run(Object[] pParams);
		/** Executing this -  For internal to change */
		@Override Object run(Context pContext, Object[] pParams) {
			return this.run(pParams);
		}
	}
	
	/** Simple Java SubRoutine */
	static public abstract class JavaSubRoutine_Simple extends JavaExecutable implements Executable.SubRoutine {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		/** Construct a new native sub-routine */
		public JavaSubRoutine_Simple(Engine pEngine, ExecSignature pSignature, String[] pFVNames, Scope pFrozenScope) {
			super(pEngine, pSignature, pFVNames, pFrozenScope);
		}
		/** Construct a new native sub-routine */
		public JavaSubRoutine_Simple(ExecSignature pSignature) {
			super(null, pSignature, null, null);
		}
		// Executing -----------------------------------------------------------
		/** Executing this -  For external*/
		abstract protected Object run(Object[] pParams);
		/** Executing this -  For internal to change */
		@Override Object run(Context pContext, Object[] pParams) {
			return this.run(pParams);
		}
	}
	
	// Complex native ----------------------------------------------------------
	
	/** Complex Java Fragment */
	static public abstract class JavaFragment_Complex extends JavaExecutable implements Executable.Fragment {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		/** Construct a new native fragment */
		public JavaFragment_Complex(Engine pEngine, ExecSignature pSignature, String[] pFVNames, Scope pFrozenScope) {
			super(pEngine, pSignature, pFVNames, pFrozenScope);
		}
		/** Construct a new native fragment */
		public JavaFragment_Complex(Engine pEngine, String pName, TypeRef pReturnTypeRef, Location pLocation,
				MoreData pExtraData, String[] pFVNames, Scope pFrozenScope) {
			this(pEngine,
				 ExecSignature.newProcedureSignature(pName,
					(pReturnTypeRef == null)?TKJava.TAny.getTypeRef():pReturnTypeRef, pLocation, pExtraData),
				 pFVNames, pFrozenScope);
		}
		/** Construct a new native fragment */
		public JavaFragment_Complex(ExecSignature pSignature) {
			super(null, pSignature, null, null);
		}
		/** Construct a new native fragment */
		public JavaFragment_Complex(String pName, TypeRef pReturnTypeRef, Location pLocation, MoreData pExtraData) {
			this(null,
				 ExecSignature.newProcedureSignature(pName,
					(pReturnTypeRef == null)?TKJava.TAny.getTypeRef():pReturnTypeRef, pLocation, pExtraData),
				 null, null);
		}
		// Executing -----------------------------------------------------------
		/** Executing this */ @Override
		final protected Object run(Context pContext, Object[] pParams) {
			return this.run(pContext);
		}
		/** Executing this */
		abstract protected Object run(Context pContext);
	}

	/** Complex Java Macro */
	static public abstract class JavaMacro_Complex extends JavaExecutable implements Executable.Macro {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		/** Construct a new native macro */
		public JavaMacro_Complex(Engine pEngine, ExecSignature pSignature, String[] pFVNames, Scope pFrozenScope) {
			super(pEngine, pSignature, pFVNames, pFrozenScope);
		}
		/** Construct a new native macro */
		public JavaMacro_Complex(ExecSignature pSignature) {
			super(null, pSignature, null, null);
		}
		// Executing -----------------------------------------------------------
		/** Executing this */ @Override
		abstract protected Object run(Context pContext, Object[] pParams);
	}

	/** Complex Java SubRoutine */
	static public abstract class JavaSubRoutine_Complex extends JavaExecutable implements Executable.SubRoutine {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		/** Construct a new native sub-routine */
		public JavaSubRoutine_Complex(Engine pEngine, ExecSignature pSignature, String[] pFVNames, Scope pFrozenScope) {
			super(pEngine, pSignature, pFVNames, pFrozenScope);
		}
		/** Construct a new native sub-routine */
		public JavaSubRoutine_Complex(ExecSignature pSignature) {
			super(null, pSignature, null, null);
		}
		// Executing -----------------------------------------------------------
		// TO Override
		/** Executing this -  For external*/ @Override
		abstract protected Object run(Context pContext, Object[] pParams);
	}
}