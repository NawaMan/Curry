package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.util.Objectable;

/**
 * An executable unit that can be executed in the curry context. <br />
 * 
 * This interface is designed specially in the way that it can be used outside this package but the implementation of
 *     its can only be done inside 'net.nawaman.curry' package; therefore, do not attempt to implement it.  
 **/
public interface Executable extends HasSignature, HasLocation, Serializable, Objectable, Cloneable {
	
	/** Returns the signature of the executable */
	public ExecSignature getSignature();
	
	// Kind --------------------------------------------------------------------
	
	/** Returns the kind of this executable */
	public Executable.ExecKind getKind();
	
	/** Checks if the executable is a fragment. **/
	public boolean isFragment();
	/** Checks if the executable is a macro. **/
	public boolean isMacro();
	/** Checks if the executable is a sub-routine. **/
	public boolean isSubRoutine();
	
	// Cast --------------------------------------------------------------------
	
	/** Returns the executable as a fragment. **/
	public Fragment   asFragment();
	/** Returns the executable as a macro. **/
	public Macro      asMacro();
	/** Returns the executable as a sub-routine. **/
	public SubRoutine asSubRoutine();
	
	// Curry -------------------------------------------------------------------
	
	/** Checks if the executable is a curry executable */
	public boolean isCurry();
	/** Returns the executable as a Curry if it is or null if it is not */
	public Curry asCurry();

	/** Checks if the executable is a Java executable */
	public boolean        isJava();
	/** Returns the executable as a Java if it is or null if it is not */
	public JavaExecutable asJava();
	
	// Frozen variables and Recreation ----------------------------------------
	
	/** Returns all the names of variables */
	public String[] getFrozenVariableNames();
	/** Returns the number of the frozen variables */
	public int getFrozenVariableCount();
	/** Returns the name of the frozen variable at the index I */
	public String getFrozenVariableName(int I);
	/** Returns the type of the frozen variable at the index I */
	public TypeRef getFrozenVariableTypeRef(Engine pEngine, int I);

	/** Recreate the executable based on the newly given frozen scope */
	public Executable reCreate(Engine pEngine, Scope pFrozenScope);
	
	// Display -----------------------------------------------------------------
	
	/** Returns the string representation of the executable */
	public String toString(Engine pEngine);          
	/** Returns the detail string representation of the executable */
	public String toDetail(Engine pEngine);
	
	// Cloneable ---------------------------------------------------------------

	/** Clone this executable (needed in some rare case of doing template) */
	public Executable clone();
	
	// Lock --------------------------------------------------------------------
	
	/** This method will help limiting the implementation of this interface to be within this package. */
	public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface);
	
	// Executable Kinds ----------------------------------------------------------------------------

	/** Kind of the executable */
	static public enum ExecKind {
		Fragment, Macro, SubRoutine;
		
		/** Checks if the executable is a fragment */
		public boolean isFragment()   { return this == Fragment;   }
		/** Checks if the executable is a macro */
		public boolean isMacro()      { return this == Macro;      }
		/** Checks if the executable is a sub-routine */
		public boolean isSubRoutine() { return this == SubRoutine; }
		
		@Override public String toString() {
			if(this == Fragment)   return "Fragment";
			if(this == Macro)      return "Macro";
			if(this == SubRoutine) return "SubRoutine";
			return null;
		}
	};
	
	/** An executable that shares the same context with the caller */
	static public interface Fragment extends Executable {}
	/**
	 * An executable that run on a separate context but linked with the caller's context.
	 * Macro has interface that allow parameters to be provided.
	 **/
	static public interface Macro extends Executable {}
	/**
	 * A subroutine is a macro that will run on a separated context that is caompletly separated
	 *    from the caller's context.
	 **/
	static public interface SubRoutine extends Executable {}

	// Is it Curry (has curry body) ----------------------------------------------------------------
	
	static public interface Curry extends Executable {
		/** Return the body of this curry executable */
		public Serializable getBody();
	}

}
