package net.nawaman.curry.script;

import java.io.Serializable;

import net.nawaman.curry.Executable;
import net.nawaman.script.CompiledCode;

/** Compiled code of curry */
public class CurryCompiledCode implements Serializable, CompiledCode {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	/** Compiled code of curry */
	public CurryCompiledCode(CurryEngine pCEngine, Executable.Fragment pFragment) {
		this.EngineName  = pCEngine.getName();
		this.TheFragment = pFragment;
	}
	
	String              EngineName   = null;	// The curry engine name
	Executable.Fragment TheFragment  = null;
	
	/**{@inheritDoc}*/@Override
	public String getEngineName() {
		return this.EngineName;
	}
	
	/**{@inheritDoc}*/@Override
	public String getEngineOptionString() {
		return null;
	}
	
}
