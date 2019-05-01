package net.nawaman.curry.script;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.nawaman.curry.Engine;
import net.nawaman.curry.Executable;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.WrapperExecutable.Wrapper;
import net.nawaman.script.FrozenVariableInfos;
import net.nawaman.script.Scope;
import net.nawaman.script.ScriptEngine;
import net.nawaman.script.ScriptEngineOption;
import net.nawaman.script.ScriptManager;

/** Script Executable wrapper for Curry Executable */
abstract public class CurryExecutable implements net.nawaman.script.Executable {

	/** Constructs CurryExecutable from curry executable and the code*/
	protected CurryExecutable(CurryEngine pCEngine, String pCode, Executable pExec) {
		this.CEngine = pCEngine;
		this.Code    = pCode;

		this.TheExecutable = Wrapper.getDeepWrappedExecutable(pExec);
		if(this.TheExecutable == null) return;
	}

	transient CurryEngine CEngine;
	
	String      Code;
	Executable  TheExecutable; // The Wrapped one (in case of wrapper)
	
	/** Returns the name of the Engine */
	public String getEngineName() {
		return this.CEngine.getShortName();
	}
	/** Return the script engine used to run this executable */
	public ScriptEngine getEngine() {
		return this.CEngine;
	}

	/** Returns the actual curry engine */
	protected Engine getTheEngine() {
		return ((CurryEngine)this.getEngine()).TheEngine;
	}
	
	private FrozenVariableInfos FVInfos = null;

	/** Returns the frozen variable informations */
	public FrozenVariableInfos getFVInfos() {
		final CurryExecutable This = this;
		if(this.FVInfos == null) {
			this.FVInfos = new FrozenVariableInfos() {
				/**{@inheritDoc}*/ @Override 
				public String[] getFrozenVariableNames() {
					return This.TheExecutable.getFrozenVariableNames();
				}
				/**{@inheritDoc}*/ @Override 
				public int getFrozenVariableCount() {
					return This.TheExecutable.getFrozenVariableCount();
				}
				/**{@inheritDoc}*/ @Override
				public String getFrozenVariableName(int I) {
					return This.TheExecutable.getFrozenVariableName(I);
				}
				/**{@inheritDoc}*/ @Override
				public Class<?> getFrozenVariableType(int I) {
					TypeRef TRef = This.TheExecutable.getFrozenVariableTypeRef(This.getTheEngine(), I);
					if(TRef == null) return Object.class;
					
					return TRef.getDataClass(CEngine.getTheEngine());
				}
			};
		} 
		return this.FVInfos;
	}
	
	/** Recreate the macro that hold a new set of frozen variable */
	abstract public CurryExecutable reCreate(net.nawaman.curry.Scope pNewFrozenScope);
	/** Recreate the macro that hold a new set of frozen variable */
	abstract public CurryExecutable reCreate(net.nawaman.script.Scope pNewFrozenScope);
	
	/** Returns the code of the executable */
	public String getCode() {
		return this.Code;
	}

	/** Create a new Frozen Scope to be used in the reCreate */
	final protected Scope getNewFrozenScope(Scope pNewFrozenScope) {
		if(pNewFrozenScope == null) return null;
		
		Engine TheEngine = ((CurryEngine)this.getEngine()).getTheEngine();
		
		if(!(pNewFrozenScope instanceof CurryScope) && (pNewFrozenScope != null)) {
			Scope NewScope = new CurryScope(TheEngine, new net.nawaman.curry.Scope());
			Scope.Simple.duplicate(pNewFrozenScope, NewScope);
			pNewFrozenScope = NewScope;
		}
		return pNewFrozenScope;
	}
	
	// Serializable ----------------------------------------------------------------------------------------------------

	
	/** Custom deserialization is needed. */
	private void readObject(ObjectInputStream aStream) throws IOException, ClassNotFoundException {
		// Save the rest
		aStream.defaultReadObject();
		
		CurryEngine.EnsureEngineRegisted();
		
		// Load the ShortName
		String ShortName = (String)aStream.readObject();
		this.CEngine = (CurryEngine)ScriptManager.Instance.getDefaultEngineOf(ShortName);
		
		
		if(this.CEngine == CurryEngine.getEngine()) {
			String Parameter = (String)aStream.readObject();
			this.CEngine = CurryEngine.getEngine(Parameter);
		}

		if(this.CEngine == null) this.CEngine = CurryEngine.getEngine(); 
	}

	/** Custom serialization is needed. */
	private void writeObject(ObjectOutputStream aStream) throws IOException {
		// Save the rest
		aStream.defaultWriteObject();

		// Write the Engine short name (so that we can get the Default Curry Engine)
		aStream.writeObject(this.CEngine.getShortName());
		
		// Save the Parameter
		ScriptEngineOption SEO = this.CEngine.getOption();
		aStream.writeObject((SEO == null) ? null : SEO.toString());

	}
}
