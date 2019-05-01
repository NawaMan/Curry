package net.nawaman.curry;

/** Part of the engine - separate works (perhaps allows modular in the future) */
abstract public class EnginePart {
	
	protected EnginePart(Engine pTheEngine) {
		if(pTheEngine == null) throw new NullPointerException();
		this.TheEngine = pTheEngine;
	}
	
	public final Engine TheEngine;
	
	final Engine getEngine() {
		return this.TheEngine;
	}
	
	// Quick access to other parts -------------------------------------------------------------------------------------
	
	public MClassPaths getClassPaths()        { return this.TheEngine.getClassPaths();  }
	public MType       getTypeManager()       { return this.TheEngine.getTypeManager(); }
	public MUnit       getUnitManager()       { return this.TheEngine.getUnitManager(); }
	public MDataHolder getDataHolderManager() { return this.TheEngine.getDataHolderManager(); }

}
