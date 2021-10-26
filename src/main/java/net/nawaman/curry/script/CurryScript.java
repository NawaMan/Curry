package net.nawaman.curry.script;

import net.nawaman.curry.Executable;
import net.nawaman.curry.AbstractExecutable;
import net.nawaman.script.CompiledCode;
import net.nawaman.script.FrozenVariableInfos;
import net.nawaman.script.Scope;
import net.nawaman.script.Script;

/** Script written in Curry */
public class CurryScript extends CurryExecutable implements Script {
    
    private static final long serialVersionUID = -3009836901006887137L;
	
	public CurryScript(CurryEngine pCEngine, String pCode, CurryCompiledCode pCCode) {
		super(pCEngine, pCode, (pCCode == null)?null:pCCode.TheFragment);
		
		this.CCode = pCCode;
	}

	CurryCompiledCode CCode;

	/**{@inheritDoc}*/ @Override
	public CurryScript reCreate(net.nawaman.curry.Scope pNewFrozenScope) {
		return this.reCreate(new CurryScope(this.getTheEngine(), pNewFrozenScope));
	}
	/**{@inheritDoc}*/ @Override
	public CurryScript reCreate(net.nawaman.script.Scope pNewFrozenScope) {
		if((pNewFrozenScope == null) || (this.TheExecutable == null)) return this;
		pNewFrozenScope = this.getNewFrozenScope(pNewFrozenScope);
		
		Executable E = this.TheExecutable;
		E = ((AbstractExecutable)E).reCreate(
										((CurryEngine)this.getEngine()).getTheEngine(),
										((CurryScope)pNewFrozenScope).TheScope);
		return new CurryScript(
					(CurryEngine)this.getEngine(), this.Code,
					new CurryCompiledCode((CurryEngine)this.getEngine(), (Executable.Fragment)E));
	}
	
	/**{@inheritDoc}*/@Override
	public CompiledCode getCompiledCode() {
		FrozenVariableInfos FVInfos = this.getFVInfos();
		if((this.CCode == null) && (FVInfos == null) && (this.getFVInfos().getFrozenVariableCount() == 0))
			this.CCode = (CurryCompiledCode)this.getEngine().compile(this.getCode(), null, null, null, null);
		return this.CCode;
	}
	
	/**{@inheritDoc}*/@Override
	public Object run() {
		return this.run(null);
	}
	/**{@inheritDoc}*/@Override
	public Object run(Scope pScope) {
		if(this.getCompiledCode() == null) return null;
		return this.getEngine().eval(this.getCompiledCode(), pScope, null);
	}

}
