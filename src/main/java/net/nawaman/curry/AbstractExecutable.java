package net.nawaman.curry;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

import net.nawaman.script.Signature;
import net.nawaman.util.Objectable;
import net.nawaman.util.UObject;

/** Default implementation of the executable */
abstract public class AbstractExecutable implements Executable, Serializable, Objectable {
    
    private static final long serialVersionUID = -3009836901006887137L;
	
	static final Scope EmptyScope = new Scope(); {
		// Do not allow this scope to be modifiable
		AbstractExecutable.EmptyScope.IsFixed         = true;
		AbstractExecutable.EmptyScope.IsNewVarAllowed = false;
	}
	
	AbstractExecutable(Engine pEngine, String[] pFVNames, Scope pFrozenScope) {
		// Set Executable Kind
		this.Kind = null;
		if(     this instanceof Executable.SubRoutine) this.Kind = Executable.ExecKind.SubRoutine;
		else if(this instanceof Executable.Macro     ) this.Kind = Executable.ExecKind.Macro;
		else if(this instanceof Executable.Fragment  ) this.Kind = Executable.ExecKind.Fragment;
		else throw new IllegalArgumentException("At least a kind of exeutable is required when implement an executable.");
		this.FrozenScope = this.newFrozenScope(pEngine, pFVNames, pFrozenScope);
	}
	// Kind ----------------------------------------------------------------
	private Executable.ExecKind Kind;
	/**{@inheritDoc}*/@Override final public Executable.ExecKind getKind() { return this.Kind; }		
	/**{@inheritDoc}*/@Override final public boolean isFragment()   { return (this instanceof   Fragment); }
	/**{@inheritDoc}*/@Override final public boolean isMacro()      { return (this instanceof      Macro); }
	/**{@inheritDoc}*/@Override final public boolean isSubRoutine() { return (this instanceof SubRoutine); }
	// Cast ----------------------------------------------------------------
	final public Fragment   asFragment()   { return (this instanceof   Fragment)?(Fragment)  this:null; }
	final public Macro      asMacro()      { return (this instanceof      Macro)?(Macro)     this:null; }
	final public SubRoutine asSubRoutine() { return (this instanceof SubRoutine)?(SubRoutine)this:null; }
	// Script --------------------------------------------------------------
	protected String[]  FVNames = null;
	protected TypeRef[] FVTRefs = null;
	
	/** Returns all the names of variables */
	final public String[] getFrozenVariableNames() {
		return (this.FVNames == null)?null:this.FVNames.clone();
	}
	/** Returns the number of the frozen variables */
	final public int getFrozenVariableCount() {
		return (this.FVNames == null)?0:this.FVNames.length;
	}
	/** Returns the name of the frozen variable at the index I */
	final public String getFrozenVariableName(int I) {
		return ((I < 0)||(I >= this.getFrozenVariableCount())?null:this.FVNames[I]);
	}
	/** Returns the type of the frozen variable at the index I */
	final public TypeRef getFrozenVariableTypeRef(Engine pEngine, int I) {
		if(this.FrozenScope == null) {
			if(this.FVTRefs != null) {
				TypeRef TRef = ((I < 0) || (I >= this.FVTRefs.length)) ? TKJava.TAny.getTypeRef() : this.FVTRefs[I];
				if(TRef == null) TRef = TKJava.TAny.getTypeRef();
				
				return TRef;
			}
			return null;
		}
		String Name = this.getFrozenVariableName(I);
		if(Name == null) return null;
		if(!this.FrozenScope.isVariableExist(Name)) return null;
		
		Type T = this.FrozenScope.getType(pEngine, Name);
		return (T == null) ? null : T.getTypeRef();
	}
	
	transient private Scope FrozenScope = null;
	/** Returns the Frozen Scope */
	final protected Scope getFrozenScope() {
		if(this.FrozenScope == null) return AbstractExecutable.EmptyScope;
		return this.FrozenScope;
	}
	/** Recreate the executable based on the newly given frozen scope */
	final public Executable reCreate(Engine pEngine, Scope pFrozenScope) {
		AbstractExecutable TheClone = this.clone();
		// No Frozen so, just return the cloned
		if((this.FVNames == null) || (this.FVNames.length == 0)) return TheClone;
		
		if(this == TheClone) throw new RuntimeException("Unable to recreate the " + this.getKind() + ": Illegal cloned value.");
		TheClone.FVNames     = this.FVNames.clone();
		TheClone.FrozenScope = this.newFrozenScope(pEngine, this.FVNames, pFrozenScope);
		return TheClone;
	}
	private Scope newFrozenScope(Engine pEngine, String[] pFVNames, Scope pNewOrgFScope) {
		Scope FScope = null;
		if((pFVNames != null) && (pFVNames.length != 0)) {
			// Get the no-null no-repeat array of names
			HashSet<String> HS = new HashSet<String>(Arrays.asList(pFVNames));
			if(HS.contains(null)) HS.remove(null);
			this.FVNames = (String[])(HS).toArray(Signature.EmptyStringArray);
			if(this.FVNames.length == 0) { this.FVNames = null; return null; }
			
			this.FVTRefs = new TypeRef[this.FVNames.length];
			
			try {
				FScope = new Scope();
				for(int i = 0; i < this.FVNames.length; i++ ) {
					String Name = this.FVNames[i];
					// If the variable does not exist in FScope, create one (any ... = null), else copy from the Original
					Type  Type = null;
					if((pNewOrgFScope == null) || !pNewOrgFScope.isVariableExist(Name))
						 FScope.newConstant(Name, (Type = TKJava.TAny),                          null);
					else FScope.newConstant(Name, (Type = pNewOrgFScope.getType(pEngine, Name)), pNewOrgFScope.getValue(pEngine, Name));
					
					this.FVTRefs[i] = (Type == null) ? TKJava.TAny.getTypeRef() : Type.getTypeRef();
				}
			} finally {
				if(FScope != null) {
					// Do not allow this scope to be modifiable
					FScope.IsFixed         = true;
					FScope.IsNewVarAllowed = false;
				}
			}
		}
		return FScope;
	}
	// Curry ---------------------------------------------------------------
	/**{@inheritDoc}*/@Override
	final public boolean isCurry() {
		return this instanceof Curry;
	}
	/**{@inheritDoc}*/@Override
	final public Curry asCurry() {
		return (this instanceof Curry)?(Curry)this:null;
	}

	/**{@inheritDoc}*/@Override
	final public boolean isJava() {
		return this instanceof JavaExecutable;
	}
	/**{@inheritDoc}*/@Override
	final public JavaExecutable asJava() {
		return (this instanceof JavaExecutable)?(JavaExecutable)this:null;
	}
	
	// Clone ---------------------------------------------------------------
	@Override abstract public AbstractExecutable clone();
	
	// Objectable ----------------------------------------------------------
	abstract int    getBodyHash();
	abstract String getBodyStr(Engine pEngine);

	@Override public String toString() { return this.toString(null); }
	@Override public String toDetail() { return this.toDetail(null); }

	public String toString(Engine pEngine) { return this.Kind.toString() + " " + this.getSignature().getName()  +"(...){...}"; }          
	public String toDetail(Engine pEngine) { return this.Kind.toString() + " " + this.getSignature().toString() + "{ " + this.getBodyStr(pEngine) + " }"; }

	/**{@inheritDoc}*/@Override final public boolean is(Object O) { return this == O; }
	/**{@inheritDoc}*/@Override final public int     hash()       { return UObject.hash(this.getSignature()) + this.getBodyHash(); }
	/**{@inheritDoc}*/@Override final public int     hashCode()   { return super.hashCode(); }	// Lock so that can be used in Java collection
	/**{@inheritDoc}*/@Override final public boolean equals(Object O) {
		if(O == null)return false;
		if(this.getClass() != O.getClass()) return false;
		return this.hash() == UObject.hash(O);
	}

	// Constrains ------------------------------------------------------------------
	/** This method will help limiting the implementation of this interface to be within this package. */
	public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) { return null; }
}