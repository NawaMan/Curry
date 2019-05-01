package net.nawaman.curry.script;

import java.io.Serializable;

import net.nawaman.curry.Executable;
import net.nawaman.curry.AbstractExecutable;
import net.nawaman.curry.Executable.Fragment;
import net.nawaman.curry.Executable.Macro;
import net.nawaman.curry.Executable.SubRoutine;
import net.nawaman.curry.WrapperExecutable;
import net.nawaman.script.Signature;

public class CurryFunction extends CurryExecutable implements net.nawaman.script.Function, Serializable {
	
	public CurryFunction(CurryEngine pCEngine, String pCode, SubRoutine pSubRoutine) {
		super(pCEngine, pCode, pSubRoutine);
		
		this.TheSubRoutine = pSubRoutine;
		this.TheSignature  = new CurrySignature(pCEngine, this.TheSubRoutine.getSignature());
	}
	
	CurrySignature TheSignature;
	SubRoutine     TheSubRoutine;	// The Wrapper one (in case of wrapper)

	/**{@inheritDoc}*/@Override
	public Signature getSignature() {
		return this.TheSignature;
	}
	
	/** The wrapped sub-routine */
	final public SubRoutine getSubRoutine() {
		return this.TheSubRoutine;
	}
	
	/**{@inheritDoc}*/ @Override
	public CurryFunction reCreate(net.nawaman.curry.Scope pNewFrozenScope) {
		return this.reCreate(new CurryScope(this.getTheEngine(), pNewFrozenScope));
	}
	/**{@inheritDoc}*/ @Override
	public CurryFunction reCreate(net.nawaman.script.Scope pNewFrozenScope) {
		if((pNewFrozenScope == null) || (this.TheExecutable == null)) return this;
		pNewFrozenScope = this.getNewFrozenScope(pNewFrozenScope);
		
		Executable E = this.TheExecutable;
		E = ((AbstractExecutable)E).reCreate(((CurryEngine)this.getEngine()).getTheEngine(), ((CurryScope)pNewFrozenScope).TheScope);
		if(     E instanceof Fragment) E = new WrapperExecutable.FragmentToSubRoutineWrapper((Fragment)E);
		else if(E instanceof Macro)    E = new WrapperExecutable.MacroToSubRoutineWrapper(   (Macro)E);
		return new CurryFunction((CurryEngine)this.getEngine(), this.getCode(), (Executable.SubRoutine)E);
	}
	
	/** Execute the function */
	public Object run(Object ... pParams) {
		return this.getTheEngine().getExecutableManager().callSubRoutine(this.TheSubRoutine, pParams);
	}

}
