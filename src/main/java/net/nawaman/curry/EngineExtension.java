package net.nawaman.curry;

import net.nawaman.curry.util.DataHolderFactory;
import net.nawaman.curry.util.MoreData;

// Add Engine Scope

/** Extension of engine. */
abstract public class EngineExtension {
	
	/** Constructs an engine extension. */
	protected EngineExtension() {}
	
	/** Returns the name of this engine extension. */
	abstract protected String getExtName();

	// Engine -----------------------------------------------------------------
	transient Engine Engine = null;
	/** Returns the engine that this extension extends. */
	final protected Engine getEngine() {
		return this.Engine;
	}
	
	// Other Extension --------------------------------------------------------
	/** Returns engine extension by name. */
	final protected EngineExtension getExtension(String pName) {
		if(pName       == null) return null;
		if(this.Engine == null) return null;
		return this.Engine.getExtension(pName);
	}
	
	// Required Extension -----------------------------------------------------
	/** Returns names of extension required by this extension. */
	abstract protected String[] getRequiredExtensionNames();
	
	// Initialization ---------------------------------------------------------
	                boolean IsInitialized = false;
	/** Checks if this extension is initialized. */
	final protected boolean isInitialized() { return this.IsInitialized; }
	/** Initialize this extension and return error if occurs. */
	final String initialize() {
		if(this.IsInitialized) return null;
		String Err = this.initializeThis();
		this.IsInitialized = true;
		return Err;
	}
	
	/** Perform the initialization of the extension and return error string if occurs. */
	// If this extension needs other extension to be initialized first, feel free to do it here.
	abstract protected String initializeThis();

	/** Create an instruction that has previously registered but have not yet initialized */
	protected Instruction getNewInstruction(int hSearch) {
		return null;
	}

	/** Perform any actions needed to be done after the initialization and return error string if occurs. */
	protected String performPostInitializeAction() {
		return null;
	}

	/** Perform any actions needed to be done after the initialization and return error string if occurs. */
	protected String performPredefinedTypes() {
		return null;
	}
	
	// Possible interaction to the engine -----------------------------------------------------------
	/** Register a type factory in to the engine and return true if success. */
	final protected boolean regTypeKind(TypeKind pTypeKind) {
		return this.Engine.getTypeManager().regTypeKind(this, pTypeKind);
	}
	
	/** Registers a primitive type. */
	final protected boolean regPrimitiveType(TKJava.TJava pType) {
		if(this.Engine.getTypeManager().isPrimitiveType(pType.getDataClass())) return true;
		return this.Engine.getTypeManager().regPrimitiveType(pType);
	}
	/** Registers a primitive type by the alias and the class. */
	final protected boolean regPrimitiveType(String pAlias, Class<?> pCls) {
		if((pAlias == null) || (pAlias.length() == 0)) pAlias = pCls.getCanonicalName();
		if(this.Engine.getTypeManager().isPrimitiveType(pCls)) return true;
		return this.Engine.getTypeManager().regPrimitiveType((TKJava.TJava)TKJava.Instance.getTypeByClass(this.Engine, pAlias, pCls));
	}
	
	/** Registers an instruction into the engine. */
	final protected boolean regInst(Instruction pInstruction) {
		if(pInstruction == null) return false;
		this.Engine.regInst(pInstruction, -1, this);
		return true;
	}
	/** Registers an instruction into the engine. */
	final protected boolean regInst(int pInstHash) {
		this.Engine.regInst(null, pInstHash, this);
		return true;
	}
	/** Registers a type resolver into the engine. */
	final protected boolean regTypeLoader(TypeLoader pTypeLoader) {
		if(pTypeLoader == null) return false;
		this.Engine.getTypeManager().regTypeLoader(this, pTypeLoader);
		return true;
	}
	/** Registers a DataHolder factory into the engine. */
	final protected boolean regDataHolderFactory(DataHolderFactory pDHF) {
		if(pDHF           == null) return false;
		if(pDHF.getName() == null) return false;
		if(this.Engine.getDataHolderManager().getDataHolderFactoryIndex(pDHF.getName()) != -1) return false;
		this.Engine.getDataHolderManager().regDataHolderFactory(pDHF);
		return true;
	}

	/** Register a type spec as a pre-define type. The type must be in "curry" package */
	final protected String registerPredefineTypeSpec(TypeSpec TS) {
		return this.Engine.getTypeManager().registerPredefineTypeSpec(TS);
	}
	
	/** Returns the default DataHolder factory that the extension require. */
	protected DataHolderFactory getDefaultDataHolderFactory() {
		return null;
	}
	
	/** Create an action record. */
	final protected ActionRecord newActionRecord(StackOwner pActor, LocationSnapshot pLocationSnapshot) {
		return new ActionRecord(pActor, pLocationSnapshot);
	}
	/** Create an action record with extra-data. */
	final protected ActionRecord newActionRecord(StackOwner pActor, LocationSnapshot pLocationSnapshot, MoreData pMoreData) {
		return (pMoreData == null)
				?new ActionRecord(                          pActor, pLocationSnapshot)
				:new ActionRecord.ActionRecord_WithMoreData(pActor, pLocationSnapshot, pMoreData);
	}

}
