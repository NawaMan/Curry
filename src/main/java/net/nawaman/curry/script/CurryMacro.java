package net.nawaman.curry.script;

import java.io.Serializable;

import net.nawaman.curry.Executable;
import net.nawaman.curry.AbstractExecutable;
import net.nawaman.curry.Executable.Fragment;
import net.nawaman.curry.WrapperExecutable.FragmentToMacroWrapper;
import net.nawaman.curry.WrapperExecutable.SubRoutineToMacroWrapper;
import net.nawaman.curry.Executable.SubRoutine;
import net.nawaman.curry.Executable.Macro;
import net.nawaman.script.Scope;
import net.nawaman.script.Signature;

public class CurryMacro extends CurryExecutable implements net.nawaman.script.Macro, Serializable {
    
    private static final long serialVersionUID = -3009836901006887137L;
    
	
	public CurryMacro(CurryEngine pCEngine, String pCode, Macro pMacro) {
		super(pCEngine, pCode, pMacro);
		
		this.TheMacro     = pMacro;
		this.TheSignature = new CurrySignature(pCEngine, this.TheMacro.getSignature());
	}
	
	CurrySignature TheSignature;
	Macro          TheMacro;	  // The Wrapper one (in case of wrapper)

	/**{@inheritDoc}*/@Override
	public Signature getSignature() {
		return this.TheSignature;
	}
	
	/** The wrapped macro */
	final public Macro getMacro() {
		return this.TheMacro;
	}
	
	/**{@inheritDoc}*/ @Override
	public CurryMacro reCreate(net.nawaman.curry.Scope pNewFrozenScope) {
		return this.reCreate(new CurryScope(this.getTheEngine(), pNewFrozenScope));
	}
	/**{@inheritDoc}*/@Override
	public CurryMacro reCreate(net.nawaman.script.Scope pNewFrozenScope) {
		if((pNewFrozenScope == null) || (this.TheExecutable == null)) return this;
		pNewFrozenScope = this.getNewFrozenScope(pNewFrozenScope);
		
		Executable E = this.TheExecutable;
		E = ((AbstractExecutable)E).reCreate(((CurryEngine)this.getEngine()).getTheEngine(), ((CurryScope)pNewFrozenScope).TheScope);
		if(     E instanceof Fragment)   E = new FragmentToMacroWrapper(  (Fragment)E);
		else if(E instanceof SubRoutine) E = new SubRoutineToMacroWrapper((SubRoutine)E);
		return new CurryMacro((CurryEngine)this.getEngine(), this.getCode(), (Macro)E);
	}
	
	/** Execute the macro */
	public Object run(Object ... pParams) {
		return this.getTheEngine().getExecutableManager().execMacro(this.TheMacro, pParams);
	}
	
	/** Execute the macro */
	public Object run(Scope pScope, Object ... pParams) {
		CurryScope CScope = null;
		try {
			if((pScope == null) || (pScope instanceof CurryScope)) CScope = (CurryScope)pScope;
			else {
				CScope = (CurryScope)this.getEngine().newScope();
				Scope.Simple.duplicate(pScope, CScope);
			}
			
			net.nawaman.curry.Scope Scope = (CScope == null)?null:CScope.TheScope;
			return this.getTheEngine().getExecutableManager().execMacro(Scope, this.TheMacro, pParams);
			
		} finally {
			if((pScope != null) && !(pScope instanceof CurryScope))
				Scope.Simple.duplicate(CScope, pScope);
			
		}
	}
}