package net.nawaman.curry;

import java.util.HashSet;
import java.util.Set;

import net.nawaman.curry.util.DataHolder;

/** Public scope is a scope that can be used outside curry environment */
final public class Scope extends ScopePrivate {

	/** Constructs a scope */
	public Scope()                                 { super(null, null);          }
	/** Constructs a scope with a delegated parent */
	public Scope(Scope pParent)                    { super(null, pParent);       }
	/** Constructs a scope */
	public Scope(String pScopeName)                { super(pScopeName, null);    }
	/** Constructs a scope with a delegated parent */
	public Scope(String pScopeName, Scope pParent) { super(pScopeName, pParent); }

	/** Constructs a scope */
	public Scope(                                  boolean pIsNewVarAllowed) { super(null, null);          this.IsNewVarAllowed = pIsNewVarAllowed; }
	/** Constructs a scope with a delegated parent */
	public Scope(Scope pParent,                    boolean pIsNewVarAllowed) { super(null, pParent);       this.IsNewVarAllowed = pIsNewVarAllowed; }
	/** Constructs a scope */
	public Scope(String pScopeName,                boolean pIsNewVarAllowed) { super(pScopeName, null);    this.IsNewVarAllowed = pIsNewVarAllowed; }
	/** Constructs a scope with a delegated parent */
	public Scope(String pScopeName, Scope pParent, boolean pIsNewVarAllowed) { super(pScopeName, pParent); this.IsNewVarAllowed = pIsNewVarAllowed; }
		
	// Is new variable is allow in this context
	                       boolean IsFixed         = false;
	                       boolean IsNewVarAllowed = true;
	@Override final public boolean isNewVarAllowed() { return this.IsNewVarAllowed; }	// Every one
	          final public boolean enableNewVar()    { if(this.IsFixed) return false; this.IsNewVarAllowed =  true; return true; }  // Only in the global context
	          final public boolean disableNewVar()   { if(this.IsFixed) return false; this.IsNewVarAllowed = false; return true; }	// Only in the global context
	
	// Is allow to all context
                 boolean IsToAll = true;
	final public boolean isToAll()      { return this.IsToAll;  }	// Every one
	final public void    enableToAll()  { this.IsToAll =  true; }	// Only in the global context
	final public void    disableToAll() { this.IsToAll = false; }	// Only in the global context
	
	// Variable ------------------------------------------------------------------------------------
	
	/** Create a new variable */ @Override
	public Object newVariable(Engine pEngine, String pVName, Type pType, Object pDefaultValue) {
		return super.newVariable(pEngine, pVName, pType, pDefaultValue);
	}
	
	/** Create a new constant */ @Override
	public Object newConstant(Engine pEngine, String pVName, Type pType, Object pDefaultValue) {
		return super.newConstant(pEngine, pVName, pType, pDefaultValue);
	}
	
	/** Create a new variable */
	public Object newVariable(String pVName, Type pType, Object pDefaultValue) {
		return super.newVariable(pType.getEngine(), pVName, pType, pDefaultValue);
	}
	
	/** Create a new constant */
	public Object newConstant(String pVName, Type pType, Object pDefaultValue) {
		return super.newConstant(pType.getEngine(), pVName, pType, pDefaultValue);
	}
	
	/** Remove a variable */
	@Override public boolean removeVariable(String pVarName) {
		return super.removeVariable(pVarName);
	}

	/** Check if the variable named pName exist */
	@Override public boolean isVariableExist(String pName) {
		return super.isVariableExist(pName);
	}
	/** Check if the variable named pName exist in the immediate scope */
	@Override public boolean isLocalVariableExist(String pName) {
		return super.isLocalVariableExist(pName);
	}
	/** Check if the variable named pName exist in the immediate scope */
	@Override public boolean isVariableConstant(String pName) {
		return super.isVariableConstant(pName);
	}
	
	/** Add a data-holder in to this scope */
	@Override public Object addDataHolder(String pBName, DataHolder pDH) {
		return super.addDataHolder(pBName, pDH);
	}
	
	/** Set the value of the variable named pName */
	@Override public Object setValue(Engine pEngine, String pName, Object pValue) {
		return super.setValue(pEngine, pName, pValue);
	}
	
	/** Get the value of the variable named pName */
	@Override public Object getValue(Engine pEngine, String pName) {
		return super.getValue(pEngine, pName);
	}
	
	/** Returns the type of the variable */
	@Override public Type getType(Engine pEngine, String pDHName) {
		return super.getType(pEngine, pDHName);
	}

	/** List names of all the variables in this scope */
	static Set<String> getVariableNamesOf(ScopePrivate pPScope) {
		if(pPScope == null) return null;
		HashSet<String> VNames = new HashSet<String>();
		
		ScopePrivate SP = pPScope;
		while(SP != null) {
			if(SP.DataHolders != null) VNames.addAll(new HashSet<String>(SP.DataHolders.keySet()));
			SP = SP.Parent;
		}
		return (VNames.size() == 0)?null:VNames;
	}

	/** List names of all the variables in this scope */
	static public Set<String> getVariableNamesOf(Scope pScope) {
		return Scope.getVariableNamesOf((ScopePrivate)pScope);
	}
}
