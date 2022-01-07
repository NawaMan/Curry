package net.nawaman.curry;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import net.nawaman.curry.Executable.*;
import net.nawaman.curry.Instructions_Core.*;
import net.nawaman.curry.util.DataHolderFactory;
import net.nawaman.curry.util.Objectable_Curry;
import net.nawaman.curry.util.UCurry;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.script.Function;
import net.nawaman.script.ScriptManager;
import net.nawaman.util.Objectable;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

/** Engine of the virtual machine */
final public class Engine {
	
	/** The file extension for Engine spec */
	static public final String ENGINE_SPEC_FILE_EXTENSION    = "ces";
	/** The default Engine name */
	static public final String DEFAULT_ENGINE_NAME           = "Curry";
	/** The file name for default Engine spec file */
	static public final String DEFAULT_ENGINE_SPEC_FILE_NAME = DEFAULT_ENGINE_NAME + "." + ENGINE_SPEC_FILE_EXTENSION;
	
	/** A flag to indicate whether or not instruction hash should be checked (compare with its registered hash value) */
	static public boolean IsInstructionHashValueChecking = true;
	
	/** A lock to limit the interface to be used only internally */
	static class LocalLock {}
	
	static HashMap<Engine, Comparator<? extends Object>> Comparators
	        = new HashMap<Engine, Comparator<? extends Object>>();
	
    static HashMap<Engine, Comparator<? extends Object>> HashComparators
            = new HashMap<Engine, Comparator<? extends Object>>();
	
	// Constructors --------------------------------------------------------------------------------
	
	/** Construct an engine. */
	static public Engine newEngine(EngineSpec pEngineSpec, boolean pVocal) {
		Engine E = new Engine(pEngineSpec);
		return E.initializeEngine(pVocal)?E:null;
	}
	/** Construct an engine. */
	static public Engine newEngine(EngineSpec pEngineSpec) {
		Engine E = new Engine(pEngineSpec);
		return E.initializeEngine(false)?E:null;
	}
	
	// Engine form SpecFile ------------------------------------------------------------------------
	
	static HashMap<String, Engine> EnginesFromSpecFile = new HashMap<String, Engine>();
	
	/** Will all engine created be vocal */
	static public boolean IsToVocal = true;
	
	/**
	 * Construct the default engine.
	 **/
	static public Engine loadEngine() {
		return loadEngine(null, false);
	}
	/**
	 * Construct an engine by loading it from an ESFileName (relative to current dir).
	 * The file have ".ces" extension.
	 **/
	static public Engine loadEngine(String ESFileName) {
		return loadEngine(ESFileName, false);
	}
	/**
	 * Construct an engine by loading it from an ESFileName (relative to current dir).
	 * The file have ".ces" extension.
	 **/
	static public Engine loadEngine(String ESFileName, boolean IsForceCreate) {
		
		if(ESFileName == null) ESFileName = DEFAULT_ENGINE_NAME;

		Engine $Engine = null;
		
		// Try to get from the cache
		if(!IsForceCreate && (($Engine = EnginesFromSpecFile.get(ESFileName)) != null))
			return $Engine;
		
		// Try to load --------------------------------------------------------
		
		PTypePackage.EnsureEngineRegisted();
		
		ParserTypeProvider TPackage = null;
		try { 
			Function F = (Function)ScriptManager.Use("EngineSpec");
			TPackage = (PTypePackage)F.run();
		} catch(Exception E) {
			String Message =
				"There is a problem loading the EngineSpec Parser/Compiler please check if `EngineSpec.tpt` is " +
				"available in the class path";
			System.err.println(Message + ": \n" + E);
			throw new RuntimeException(Message, E);
		}
		
		// Default engine - When the name is "Curry"
		if(ESFileName.equals(DEFAULT_ENGINE_NAME) || ESFileName.equals(DEFAULT_ENGINE_SPEC_FILE_NAME)) {
			
			// Engine with default spec and all known Extensions
			$Engine = Engine.newEngine(net.nawaman.curry.EngineSpec.newSimpleEngineSpec(DEFAULT_ENGINE_NAME));
			
		} else {
			
			try {
				CompilationContext CC = new CompilationContext.Simple();
				
				if(!(new File(ESFileName)).exists()) {
					String FileName = ESFileName + "." + ENGINE_SPEC_FILE_EXTENSION;
					if(!(new File(FileName)).exists()) {
						String Message =
							"The curry language parse file `"+ESFileName+"` is not found. Parse files are not " +
							"searched as part of Usepath but using direct path reference (relative to working " +
							"directory)";
						System.err.println(Message);
						throw new RuntimeException(Message);
					}
					ESFileName = FileName;
				}
				
				String     ESpec = UString.loadTextFile(ESFileName);
				ParserType      PT    = TPackage.type("EngineSpecFile");
				EngineSpec ES    = (EngineSpec)PT.compile(ESpec, ESFileName, CC, TPackage);
				
				if(ES == null) {
					String Msg = "Problem loading an engine from the file `"+ESFileName+"`: \n" + CC;
					System.err.println(Msg);
					throw new RuntimeException(Msg);
				}

				$Engine = Engine.newEngine(ES, Engine.IsToVocal);
				
			} catch (Exception E) {
				System.err.println(E);
				throw new RuntimeException(E);
			}
		}

		// Add if not there
		if(!EnginesFromSpecFile.containsKey(ESFileName)) EnginesFromSpecFile.put(ESFileName, $Engine);
		
		return $Engine;
	}
	
	static Engine An_Engine = null;
	
	/** Construct an uninitialized engine. */
	Engine(EngineSpec pEngineSpec) {
		this.EngineSpec = pEngineSpec;
		if(Engine.An_Engine == null) Engine.An_Engine = this;
	}
	
	// EngineSpec ----------------------------------------------------------------------------------
	private EngineSpec  EngineSpec = null;
	/** Returns the engine specification. */
	public EngineSpec   getEngineSpec() { return this.EngineSpec; }
	/** Returns the engine name. */
	public String       getName()       { return this.EngineSpec.getEngineName(); }
	/** Returns the engine signature. */
	public Serializable getSignature()  { return this.EngineSpec.getSignature(); }

	// Extension related services ------------------------------------------------------------------
	final Vector<String>          ExtensionNames = new Vector<String>();
	final Vector<EngineExtension> Extensions     = new Vector<EngineExtension>();
	/** Registers an engine extension. */
	private boolean regExt(EngineExtension pExtension) {
		// Precondition
		if(this.IsInitialized) {
			this.showErr(this.EngineSpec.getEngineAlreadyInitializedMsg());
			return false;
		}
		if(pExtension == null) {
			this.showErr(this.EngineSpec.getNullExtRegErr());
			return false;
		}
		if(pExtension.getEngine() != null) {
			this.showErr(this.EngineSpec.getExtAlreadyInUseErr(pExtension));
			return false;
		}
				
		if(this.ExtensionNames.indexOf(pExtension.getExtName()) != -1) {
			this.showErr(this.EngineSpec.getExtAlreadyInUseErr(pExtension));
			return false;
		}
		
		this.ExtensionNames.add(pExtension.getExtName());
		this.Extensions.add(pExtension);
		pExtension.Engine = this;
		return true;
	}
	/** Returns the extension that is associated with pName. */
	public EngineExtension getExtension(String pName) {
		if(pName == null) return null;
		int Ind = this.ExtensionNames.indexOf(pName);
		if(Ind ==  -1) return null;
		return this.Extensions.get(Ind);
	}
	
	// Initialization ------------------------------------------------------------------------------
	boolean IsInitialized = false;
	/** Checks if the engine has been initialized. */
	public boolean isInitialized() { return this.IsInitialized; }
	
	/** Initialize this engine. */
	protected boolean initializeEngine(boolean pVocal) {
		// Returns if this is already been initialized.
		if(this.IsInitialized) return true;
		
		if(this.EngineSpec.Engine != null)
			throw new RuntimeException("The Engine spec is already used to create an engtine.");
		
		// Assign the Engine in to the EnginesSpec
		this.EngineSpec.Engine = this;
		
		// Prepare engine objects ------------------------------------------------------------------
		// These will prepare (including registering) all types of engine that required to be there regardless to the
		//    engine extension mentioned in the spec.
		if(!this.getTypeManager().preparePrimitiveTypes()) return false;
		if(!this.getTypeManager().prepareLockedTypes())    return false;
		if(!this.getTypeManager().prepareTypeKinds())      return false;
		if(!this.getTypeManager().prepareTypeLoaders())    return false;
		if(!this.prepareDataHolderFactories())             return false;
		if(!this.prepareEngineInstructions())              return false;
				
		// Initialize the Extension ----------------------------------------------------------------
		if(pVocal) this.showMsg(this.EngineSpec.getEngineInitializingMsg());
		// Register
		int NameLength = 0;
		EngineExtension[] EEs = this.EngineSpec.getExtensions();
		for(EngineExtension EE : EEs) {
			if(NameLength < EE.getExtName().length()) NameLength = EE.getExtName().length();
			if(this.regExt(EE)) {
				if(pVocal) this.showMsg(this.EngineSpec.getEngineExtRegistrationSuccessMsg(EE));
				else       this.showMsg(this.EngineSpec.getEngineExtRegistrationSuccessMsg_NonVocal(EE));
				continue;
			}
			this.showErr(this.EngineSpec.getEngineExtRegistrationFailMsg(EE));
			return false;
		}
		
		// Ensure all Required
		for(EngineExtension EE : this.Extensions) {
			String[] REENs = EE.getRequiredExtensionNames();
			if(REENs == null) continue;
			for(int i = 0; i < REENs.length; i++) {
				boolean found = false;
				for(EngineExtension EE_R : EEs) {
					if(UString.equal(REENs[i], EE_R.getExtName())) {
						found = true;
						break;
					}
				}
				if(found) continue;
				this.showErr(this.getEngineSpec().getRequiredEngineExtDoesNotExistMsg(EE, REENs[i]));
				return false;
			}
		}
		
		// Prepare EngineSpec component
		this.EngineSpec.preparePrimitiveTypes();
		this.EngineSpec.prepareLockedTypes();
		this.EngineSpec.prepareTypeKinds();
		this.EngineSpec.prepareTypeLoaders();
		this.EngineSpec.prepareDataHolderFactories();
		this.EngineSpec.prepareInstructions();
		
		// Initialized
		for(int i = 0; i < this.Extensions.size(); i++) {
			EngineExtension EE = this.Extensions.get(i);
			String Err = EE.initialize();
			if(Err == null) {
				if(pVocal) this.showMsg(this.EngineSpec.getEngineExtInitializationSuccessMsg(         EE, NameLength));
				else       this.showMsg(this.EngineSpec.getEngineExtInitializationSuccessMsg_NonVocal(EE, NameLength));
				continue;
			}
			this.showErr(this.EngineSpec.getEngineExtInitializationFailMsg(EE, NameLength));
			return false;
		}
		
		// Configure centralized objects by selecting the first one ---------------------
		
		// Set up default DataHolder factory ----------------------------------
		for(int i = 0; i < this.Extensions.size(); i++) {
			DataHolderFactory DHF = this.Extensions.get(i).getDefaultDataHolderFactory();
			if(DHF == null) continue;
			this.getDataHolderManager().DefaultDataHolderFactory = DHF;
			break;
		}

		// Predefined types -------------------------------------------------------------
		{
			String Err = this.EngineSpec.preparePredefinedTypes();
			if(Err != null) {
				this.showErr(this.EngineSpec.getEngineExtInitializationFailMsg(null, NameLength));
				return false;
			}
		}
		
		// Perform post initialization action -------------------------------------------
		for(int i = 0; i < this.Extensions.size(); i++) {
			EngineExtension EE = this.Extensions.get(i);
			String Err = EE.performPostInitializeAction();
			if(Err == null) continue;
			this.showErr(this.EngineSpec.getEngineExtInitializationFailMsg(EE, NameLength));
			return false;
		}
		
		// Prepare the hash array of the instruction ------------------------------------
		this.prepareInstructionHash();
		
		// Finalize the initialization --------------------------------------------------------
		if(pVocal) this.showMsg(this.EngineSpec.getEngineInitializationSuccessMsg());
		else       this.showMsg(this.EngineSpec.getEngineInitializationSuccessMsg_NonVocal());
		this.IsInitialized     = true;
		this.EngineSpec.Engine = this;
		
		return true;
	}
	
	// Engine Scope --------------------------------------------------------------------
	final Scope EngineScope = new Scope(); 
	/** Returns the engine scope of this engine. */
	public Scope getEngineScope() {
		return this.EngineScope;
	}
	
	/** Returns the name of the engine context */
	public String getEngineContextName() {
		return this.EngineSpec.getEngineContextName();
	}
	
	/** A default context to used with type kind/load and name space loading related */
	public Context newRootContext() {
		return (new Executor(this)).newRootContext(null);
	}
	/** A default context to used with type kind/load and name space loading related */
	public Context newRootContext(Scope pGlobalScope, Debugger pDebugger) {
		return (new Executor(this, pDebugger)).newRootContext(pGlobalScope);
	}
	
	/** A default context to used with type kind/load and name space loading related */
	public Context newRootContext(Scope pGlobalScope, Scope pTopScope, Executable pExecutable, Debugger pDebugger) {
		return (new Executor(this, pDebugger)).newRootContext(pGlobalScope, pTopScope, pExecutable);
	}
	
	// Type Management -------------------------------------------------------------------------------------------------
	
	final private MType TypeManager = new MType(this);
	
	final public MType getTypeManager() {
		return this.TypeManager;
	}
	
	// ClassPaths ------------------------------------------------------------------------------------------------------
	
	private MClassPaths ClassPaths = null;
	
	final public MClassPaths getClassPaths() {
		if(this.ClassPaths == null) this.ClassPaths = new MClassPaths(this);
		return this.ClassPaths;
	}
	
	// StackOwner Related Services -----------------------------------------------------------------
	
	/** Returns default stack owner. */
	public StackOwner getDefaultStackOwner() {
		return this.getDefaultPackage();
	}
	
	// Unit and Default Package --------------------------------------------------------------------
	/** Manager of all units */
	MUnit TheUnitManager = null;
	
	final public MUnit getUnitManager() {
		return this.TheUnitManager;
	}
	
	       Package DefaultPackage = null;
	/** Returns the default package */
	public Package getDefaultPackage() {
		return this.DefaultPackage;
	}
	
           DefaultPackageBuilder DefaultPackageBuilder = null;
    /** Returns the default package builder */
    public DefaultPackageBuilder getDefaultPackageBuilder() {
    	return this.DefaultPackageBuilder;
    }
	
	// Package -------------------------------------------------------------------------------------
	
	/** Returns the package of the given object O */
	public Package getPackageOf(Object O) {
		if(O == null)            return this.getDefaultPackage();
		if(O instanceof Package) return (Package)O;
		TypeRef TR = null;
		if(     O instanceof TypeRef)  TR = (TypeRef)O;
		else if(O instanceof Type)     TR = ((Type)O).getTypeRef();
		else if(O instanceof TypeSpec) TR = ((TypeSpec)O).getTypeRef();
		else {
			Type T = this.getTypeManager().getTypeOf(O);
			if(T != null) TR = T.getTypeRef();
		}
		if(!(TR instanceof TLPackage.TRPackage)) return this.getDefaultPackage();
		return this.getUnitManager().getPackage(((TLPackage.TRPackage)TR).PName);
	}
	
	// Default Printer -----------------------------------------------------------------------------
	
	PrintStream DefaultPrinter = null;
	PrintStream DebugPrinter   = null;
	
	/** Returns the default printer */
	public PrintStream getDefaultPrinter() {
		return (this.DefaultPrinter == null)?System.out:this.DefaultPrinter;
	}
	/** Returns the debug printer */
	public PrintStream getDebugPrinter() {
		return (this.DebugPrinter   == null)?System.out:this.DebugPrinter;
	}
	
	/** Set the default printer */
	public void setDefaultPrinter(PrintStream PS) {
		this.DefaultPrinter = PS;
	}
	/** Set the debug printer */
	public void setDebugPrinter(PrintStream PS) {
		this.DebugPrinter = PS;
	}
	
	// DataHolderFactory related services ----------------------------------------------------------
	
	final private MDataHolder DataHolderManager = new MDataHolder(this);
	
	final public MDataHolder getDataHolderManager() { 
		return this.DataHolderManager;
	}
	
	// Instruction --------------------------------------------------
	int                           InstructionCount = 0;
	int[]                         InstructionHashs = null;
	final Vector<Instruction>     Instructions     = new Vector<Instruction>();
	final Vector<EngineExtension> InstOwners       = new Vector<EngineExtension>();
	
	/** Returns the has from the given instruction name */
	static public int calculateHash(String pInstName) {
		int H = UString.hash(pInstName);
		if(H >= 0) H = -(H + 1);
		while(H >= 0) { H = H >> 1; H = -(H + 1); }	// Ensure is negative and unique
		return H;
	} 
	
	/** Check if an expression is an expression of instruction of the given name */
	public boolean isExpressionOf(String pInstName, Expression Expr) {
		Instruction Inst = this.getInstruction(pInstName);
		if(Inst == null) return (Expr == null);
		if(Expr == null) return false;
		Instruction InstE = this.getInstruction(null, Expr.getInstructionNameHash());
		return (Inst == InstE);
	}

	/** Register an instruction and returns an error string (null if no error). */
	boolean regInst(Instruction pInst, int pInstHash, EngineExtension pEE) {
		// Precondition
		if(this.IsInitialized) { this.showErr(this.EngineSpec.getEngineAlreadyInitializedMsg()); return false; }
		if((pInst != null) && (pInst.Engine != null)) {
			this.showErr(this.EngineSpec.getInstAlreadyInUseErr(pInst));
			return false;
		}
		
		// Ensure hash array is initialized
		if(this.InstructionHashs == null) this.InstructionHashs = new int[256];
		// Ensure enough hash space
		if(this.InstructionCount >= this.InstructionHashs.length) {
			int[] NewIHs = new int[this.InstructionHashs.length + 256];
			System.arraycopy(this.InstructionHashs, 0, NewIHs, 0, this.InstructionHashs.length);
			this.InstructionHashs = NewIHs;
		}
		
		// Create hash of the input instruction
		int hSearch = (pInst != null)?calculateHash(pInst.getName()):pInstHash;
		int Count   = this.InstructionCount;
		int Ind     = Count;
		// Find the position to insert that will automatically sort by the hash.
		for(int i = 0; i < Count; i++) {
			int hEach = this.InstructionHashs[i];
			if(hSearch == hEach) {
				if(pInst != null) pInst.Engine = this;
				this.showErr(this.EngineSpec.getInstAlreadyInUseErr(hSearch));
				return false;
			}
			if(hSearch < hEach) { Ind = i; break; }
		}
		// Insert the instruction
		this.Instructions.insertElementAt(pInst, Ind);
		// Insert Owner
		this.InstOwners.insertElementAt(  pEE,   Ind);
		// Insert Hash
		System.arraycopy(this.InstructionHashs, Ind, this.InstructionHashs, Ind + 1, this.InstructionCount - Ind);
		this.InstructionHashs[Ind] = hSearch;
		//this.InstructionCount = this.Instructions.size();
		this.InstructionCount++;
		
		if(pInst != null) pInst.Engine = this;
		return true;
	}
	/** Register an instruction and returns an error string (null if no error). */
	boolean regInst(Instruction pInst) {
		return this.regInst(pInst, -1, null);
	}
	/** Register an instruction and returns an error string (null if no error). */
	boolean regInst(int pInstHash) {
		return this.regInst(null, pInstHash, null);
	}
	/** Register an instruction and returns an error string (null if no error). */
	boolean regInst(Instruction pInst, EngineExtension pEE) {
		return this.regInst(pInst, -1, pEE);
	}
	/** Register an instruction and returns an error string (null if no error). */
	boolean regInst(int pInstHash, EngineExtension pEE) {
		return this.regInst(null, pInstHash, pEE);
	}
	
	/** Prepares Instruction Hash. */
	void prepareInstructionHash() {
		// Precondition
		if(this.IsInitialized) this.showErr(this.EngineSpec.getEngineAlreadyInitializedMsg());
		
		this.InstOwners.insertElementAt(null, 0);
		this.Instructions.insertElementAt(null, 0);
		// Append 0 or null at the first item as 0 index means no-instruction  
		int[] NewInstHash = new int[this.InstructionCount + 1];
		System.arraycopy(this.InstructionHashs, 0, NewInstHash, 1, this.InstructionCount);
		this.InstructionHashs = NewInstHash;
		this.InstructionHashs[0] = 0;
		this.InstructionCount++;
	}
	
	/** Returns the instructions by its name. */
	public Instruction getInstruction(String pName) {
		if(pName == null) return null;
		return this.getInstruction(null, calculateHash(pName));
	}
	/** Returns the instructions by its hash (negative value) or index (positive value) **/
	public Instruction getInstruction(int pNameHashOrIndex) {
		return this.getInstruction(null, pNameHashOrIndex);
	}
	/** Returns the instructions by its hash (negative value) or index (positive value) **/
	public Instruction getInstruction(Expression pExpr, int pNameHashOrIndex) {
		int hSearch = (pExpr != null)?pExpr.getInstructionNameHash():pNameHashOrIndex;
		if(hSearch == 0)
			return null;
		if(hSearch > 0) { // Get by index ------------------------------------------------------------------------------
			int Index = hSearch;
			Instruction Inst = this.Instructions.get(Index);
			if(Inst == null) {
				// Get the Hash
				hSearch = this.InstructionHashs[Index];
				// Get the Owner
				EngineExtension EE = this.InstOwners.get(Index);
				// Get the Instruction
				if(EE == null) {
					Inst = this.EngineSpec.getNewInstruction(hSearch);
					if(Inst == null) {
						Inst = this.EngineSpec.getNewEngineInstruction(hSearch);

						/* */ // FORTEST
						if(IsInstructionHashValueChecking) {
							// Check if the name hash is correct
							if((Inst != null) && (Inst.getNameHash() != hSearch))
								this.showErr("Invalid Name Hash: " + Inst.toDetail() + "[" + Inst.getNameHash() + "]");
							
							// Checks if there is such instruction with the name hash  
							if(Inst == null)
								this.showErr(this.EngineSpec.getNullInstRegErr(hSearch));
							
						}
						/* */
					}
				} else {
					Inst = EE.getNewInstruction(hSearch);
				}
				if(Inst == null) {
					throw new CurryError("Extension Error: A registered instruction does not exist (hash = "
							+hSearch+"; Owner = "+((EE == null)?"the engine or the engine spec":EE.getExtName())+").");
				}
				Inst.Engine = this;
				this.Instructions.set(Index, Inst);
			}
			return Inst;
		}
		
		// Get by hash - using quick search
		int Low  = 0;
		int High = this.InstructionCount - 1;
		while(Low <= High) {
			int Mid = (Low + High) / 2;
			int hEach = this.InstructionHashs[Mid];
			if(hEach == hSearch) {
				if(pExpr != null) pExpr.updateInstructionSingatureHash(Mid);
				int Index = Mid;
				Instruction Inst = this.Instructions.get(Index);
				if(Inst == null) {
					// Get the Owner
					EngineExtension EE = this.InstOwners.get(Index);
					// Get the Instruction
					if(EE == null) {
						Inst = this.EngineSpec.getNewInstruction(hSearch);
						if(Inst == null) Inst = this.EngineSpec.getNewEngineInstruction(hSearch);
					} else {
						Inst = EE.getNewInstruction(hSearch);
					}
					if(Inst == null) {
						throw new CurryError("Extension Error: A registered instruction does not exist (hash = "
							+hSearch+"; Owner = "+((EE == null)?"the engine or the engine spec":EE.getExtName())+").");
					}
					Inst.Engine = this;
					this.Instructions.set(Index, Inst);
				}
				return Inst;
			}
			
			if(High == Low) break;
			
			if(hEach > hSearch) High = Mid;
			else                Low  = Mid + 1;
		}
		return null;
	}
	/** Returns the index of the instruction name hash **/
	public int getIndexOfInstruction(int pNameHash) {
		int hSearch = pNameHash;
		// Get by hash - using quick search
		int Low  = 0;
		int High = this.InstructionCount - 1;
		while(Low <= High) {
			int Mid = (Low + High) / 2;
			int hEach = this.InstructionHashs[Mid];
			if(hEach == hSearch) return Mid;
			
			if(this.InstructionHashs[Mid] > hSearch) High = Mid - 1;
			else                                     Low  = Mid + 1;
		}
		return -1;
	}
	/**
	 * Returns the Extension that register the instruction or null if not found or the instruction is part of the core
	 *   instructions.
	 **/
	public EngineExtension getOwnerOfInstruction(int pNameHashOrIndex) {
		Instruction Inst = this.getInstruction(pNameHashOrIndex);
		if(Inst == null)
			throw new NullPointerException("Unknown instruction: " + pNameHashOrIndex);
		int Index = this.getIndexOfInstruction(Inst.getNameHash());
		if((Index < 0) || (Index >= this.InstOwners.size())) return null;
		return this.InstOwners.get(Index);
	}

	/** Returns the all instruction list as string */
	public Instruction[] getInstructions() {
		return this.getInstructions(null, true);
	}
	/**
	 * Returns the instruction list of the given extension as string
	 * 
	 * If EE is null and IsAll is false, only the core instructions are included.
	 * If EE is null and IsAll is true,  all instructions are included.
	 * If EE is not null, all instructions of that engine extension are included.
	 **/
	public Instruction[] getInstructions(EngineExtension EE, boolean pIsAll) {
		Vector<Instruction> Insts = new Vector<Instruction>();
		for(int i = this.InstructionCount; --i >= 1; ) {
			Instruction     Inst = this.getInstruction(i);
			EngineExtension IEE  = this.getOwnerOfInstruction(i);
			if(EE != null) { if(EE != IEE) continue;                }
			else           { if(!pIsAll && (IEE != null)) continue; }
			Insts.add(Inst);
		}
		return Insts.toArray(new Instruction[Insts.size()]);
	}

	/** Returns the all instruction list as string */
	public String getInstructionsAsString() {
		return this.getInstructionsAsString(null, true);
	}

	/** Returns the all instruction list as string */
	public String getInstructionsAsString(EngineExtension EE) {
		return this.getInstructionsAsString(EE, false);
	}
	/**
	 * Returns the instruction list of the given extension as string
	 * 
	 * If EE is null and IsAll is false, only the core instructions are included.
	 * If EE is null and IsAll is true,  all instructions are included.
	 * If EE is not null, all instructions of that engine extension are included.
	 **/
	public String getInstructionsAsString(EngineExtension EE, boolean pIsAll) {
		StringBuilder SB = new StringBuilder();
		int Length = 0;
		if(EE == null) Length = pIsAll?25:"Engine".length();
		else           Length = EE.getExtName().length();
		
		SB.append(String.format("%"+Length+"s => %8s: %s\n", "Owner", "Hash", "Specification"));
		for(int i = this.InstructionCount; --i >= 1; ) {
			Instruction Inst = this.getInstruction(i);
			if(this.getInstruction(Inst.getNameHash()) == null)
				throw new NullPointerException(
					"Unmatch instruction hash " + Inst.getNameHash() + " for '" + Inst.getName() + "'.");
			
			EngineExtension IEE  = this.getOwnerOfInstruction(Inst.getNameHash());
			if(EE != null) { if(EE != IEE) continue;                }
			else           { if(!pIsAll && (IEE != null)) continue; }

			String Owner = (IEE == null)?"Engine":IEE.getExtName();
			SB.append(String.format(
					"%"+Length+"s => %8d: %s\n",
					Owner, Inst.getNameHash(), Inst.getSpecification().toDetail()));
		}
		return SB.toString();
	}

	// Preparation of Default Engine Specification -------------------------------------------------
	// These methods will prepare (including registering) all types of engine that required to be there regardless to
	//    the engine extension mentioned in the spec.
	
	/** Prepares the engine DataHolder factories.<br/>This method must display error message if needed.**/
	boolean prepareDataHolderFactories() {
		if(!this.getDataHolderManager().regDataHolderFactory(Variable.getFactory())) return false;
		return true;
	}
	
	/** Prepares the engine instructions.<br/>This method must display error message if needed. **/	
	boolean prepareEngineInstructions() {
		// Core ----------------------------------------------------------------
		if(!this.regInst(new Inst_GetEngine(this)))  return false;
		if(!this.regInst(new Inst_Data(this)))       return false;
		if(!this.regInst(new Inst_Doc(this)))        return false;
		if(!this.regInst(new Inst_Type(this)))       return false;
		if(!this.regInst(new Inst_Cast(this)))       return false;
		if(!this.regInst(new Inst_CastOrElse(this))) return false;
		if(!this.regInst(new Inst_Group(this)))      return false;
		if(!this.regInst(new Inst_Stack(this)))      return false;
		
		// Is A ... type
		if(!this.regInst(new Inst_IsKindOf(  this, null))) return false;
		if(!this.regInst(new Inst_InstanceOf(this, null))) return false;
		
		if(!this.regInst(new Inst_HashCode(this))) return false;
		if(!this.regInst(new Inst_Hash(this)))     return false;
		if(!this.regInst(new Inst_ToString(this))) return false;
		if(!this.regInst(new Inst_ToDetail(this))) return false;
		if(!this.regInst(new Inst_Compare(this)))  return false;
		
		if(!this.regInst(new Inst_Check(this, Inst_Check.CheckMethod.IS)))  return false;
		if(!this.regInst(new Inst_Check(this, Inst_Check.CheckMethod.EQs))) return false;
		
		// Prepare the engine instructions
		this.EngineSpec.prepareEngineInstructions();
		
		return true;
	}

	// Internal services ---------------------------------------------------------------------------
	/** Display engine message */
	void showMsg(String pMsg) {
		this.EngineSpec.showMsg(pMsg);
	}
	/** Display engine error */
	void showErr(String pErr) {
		this.EngineSpec.showMsg(pErr);
		if(!this.IsInitialized) throw new RuntimeException("Error Initializing Engine: " + pErr);
	}
	
	// Execution and Debugging services --------------------------------------------------
	
	final private MExecutable ExecutableManager = new MExecutable(this);
	
	/** Returns the executable manager */
	public MExecutable getExecutableManager() {
		return this.ExecutableManager;
	}

	// Run directly from the instruction name ----------------------------------
	
	// Without scope -------------------------------------------------
	/** Create and execute an expression */
	public Object execute(String pInstName, Object ... Params) {
		return this.execute(null, pInstName, Params);
	}
	/** Create and execute an expression */
	public Object execute(String pInstName, Object[] Params, Expression ... Body) {
		return this.execute(null, pInstName, Params, Body);
	}
	
	// With scope ----------------------------------------------------
	/** Create and execute an expression with a scope */
	public Object execute(Scope pGlobalScope, String pInstName, Object ... Params) {
		Instruction Inst = this.getInstruction(pInstName);
		Expression  Expr = Inst.newExpression(Params);
		if(Expr == null) return null;
		Context C = this.newRootContext(pGlobalScope, null);
		Object R = C.getExecutor().execInternal(C, Expr);
		if(R instanceof SpecialResult) {
			if(R instanceof SpecialResult.ResultResult)
				return ((SpecialResult.ResultResult)R).getResult();
			throw ((SpecialResult)R).getException(C);
		}
		return R;
	}
	/** Create and execute an expression with a scope */
	public Object execute(Scope pGlobalScope, String pInstName, Object[] Params, Expression ... Body) {
		Instruction Inst = this.getInstruction(pInstName);
		Expression  Expr = Inst.newExprSubs(Params, Body);
		if(Expr == null) return null;
		Context C = this.newRootContext(pGlobalScope, null);	
		Object R = C.getExecutor().execInternal(C, Expr);
		if(R instanceof SpecialResult) {
			if(R instanceof SpecialResult.ResultResult)
				return ((SpecialResult.ResultResult)R).getResult();
			throw ((SpecialResult)R).getException(C);
		}
		return R;
	}
	
	// Without scope -------------------------------------------------
	/** Create and execute an expression */
	public Object debug(Debugger pDebugger, String pInstName, Object ... Params) {
		return this.debug(pDebugger, null, pInstName, Params);
	}
	/** Create and execute an expression */
	public Object debug(Debugger pDebugger, String pInstName, Object[] Params, Expression ... Body) {
		return this.debug(pDebugger, null, pInstName, Params, Body);
	}
	
	// With scope ----------------------------------------------------
	/** Create and execute an expression with a scope */
	public Object debug(Debugger pDebugger, Scope pGlobalScope, String pInstName, Object ... Params) {
		Instruction Inst = this.getInstruction(pInstName);
		Expression  Expr = Inst.newExpression(Params);
		if(Expr == null) return null;
		Context C = this.newRootContext(pGlobalScope, null);	
		Object R = (new Executor(this, pDebugger)).exec(pGlobalScope, Expr, false);
		if(R instanceof SpecialResult) {
			if(R instanceof SpecialResult.ResultResult)
				return ((SpecialResult.ResultResult)R).getResult();
			throw ((SpecialResult)R).getException(C);
		}
		return R;
	}
	/** Create and execute an expression with a scope */
	public Object debug(Debugger pDebugger, Scope pGlobalScope, String pInstName, Object[] Params, Expression ... Body) {
		Instruction Inst = this.getInstruction(pInstName);
		Expression  Expr = Inst.newExprSubs(Params, Body);
		if(Expr == null) return null;
		Context C = this.newRootContext(pGlobalScope, null);	
		Object R = (new Executor(this, pDebugger)).exec(pGlobalScope, Expr, false);
		if(R instanceof SpecialResult) {
			if(R instanceof SpecialResult.ResultResult)
				return ((SpecialResult.ResultResult)R).getResult();
			throw ((SpecialResult)R).getException(C);
		}
		return R;
	}

	// Expression --------------------------------------------------------------
	
	// Execute Expression --------------------------------------------
	/** Execute an expression */
	public Object execute(Object pExpr) {
		if(!(pExpr instanceof Expression)) return pExpr;
		if(((Expression)pExpr).isData())   return ((Expression)pExpr).getData();
		if(pExpr instanceof Fragment)      return this.getExecutableManager().debugFragment(null, null, (Fragment)pExpr);
		
		return this.debugRAW(null, null, pExpr, false);
	}
	/** Execute an expression with a global scope */
	public Object execute(Scope pGlobalScope, Object pExpr) {
		if(!(pExpr instanceof Expression)) return pExpr;
		if(((Expression)pExpr).isData())   return ((Expression)pExpr).getData();
		if(pExpr instanceof Fragment)      return this.getExecutableManager().debugFragment(pGlobalScope, null, (Fragment)pExpr);
		
		return this.debugRAW(pGlobalScope, null, pExpr, false);
	}
	/**
	 * Execute an expression with a scope and the option to wrap it with a Context so that the scope is not altered).
	 **/
	public Object execute(Scope pGlobalScope, Object pExpr, boolean IsWrapped) {
		if(!(pExpr instanceof Expression)) return pExpr;
		if(((Expression)pExpr).isData())   return ((Expression)pExpr).getData();
		if(pExpr instanceof Fragment)      return this.getExecutableManager().debugFragment(pGlobalScope, null, (Fragment)pExpr);
		
		return this.debugRAW(pGlobalScope, null, pExpr, IsWrapped);
	}
	
	// Debug Expression ----------------------------------------------
	/** Debug an expression */
	public Object debug(Debugger pDebugger, Object pExpr) {
		if(!(pExpr instanceof Expression)) return pExpr;
		if(((Expression)pExpr).isData())   return ((Expression)pExpr).getData();
		if(pExpr instanceof Fragment)      return this.getExecutableManager().debugFragment(null, pDebugger, (Fragment)pExpr);
		
		return this.debugRAW(null, pDebugger, pExpr, false);
	}
	/** Debug an expression with a global scope */
	public Object debug(Scope pGlobalScope, Debugger pDebugger, Object pExpr) {
		if(!(pExpr instanceof Expression)) return pExpr;
		if(((Expression)pExpr).isData())   return ((Expression)pExpr).getData();
		if(pExpr instanceof Fragment)      return this.getExecutableManager().debugFragment(pGlobalScope, pDebugger, (Fragment)pExpr);
		
		return this.debugRAW(pGlobalScope, pDebugger, pExpr, false);
	}
	/**
	 * Debug an expression with a scope and the option to wrap it with a Context so that the scope
	 * is not altered).
	 **/
	public Object debug(Scope pGlobalScope, Debugger pDebugger, Object pExpr, boolean IsWrapped) {
		if(!(pExpr instanceof Expression)) return pExpr;
		if(((Expression)pExpr).isData())   return ((Expression)pExpr).getData();
		if(pExpr instanceof Fragment)      return this.getExecutableManager().debugFragment(pGlobalScope, pDebugger, (Fragment)pExpr);
		
		return this.debugRAW(pGlobalScope, pDebugger, pExpr, IsWrapped);
	}
	
	Object debugRAW(Scope pGlobalScope, Debugger pDebugger, Object pExpr, boolean IsWrapped) {		
		Object R = (new Executor(this.getExecutableManager().getEngine(), pDebugger)).exec(pGlobalScope, pExpr, IsWrapped);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(null);
		return R;
	}
	
	// Objectable Services -----------------------------------------------------

	// Without Context -----------------------------------------------
	/** Returns a string representation of an object with type */
	public String getDisplayObject(Object O)            { return this.getDisplayObject(null, O);   }
	/** Returns a short string representation of an object. */
	public String toString(Object O)                    { return this.toString(null, O);           }	
	/** Returns a long string representation of an object. */
	public String toDetail(Object O)                    { return this.toDetail(null, O);           }
	/** Checks if the object O is the same with AnotherO. */
	public boolean is(Object O, Object AnotherO)        { return this.is(null, O, AnotherO);       }
	/** Checks if the object O equals to AnotherO. */
	public boolean equals(Object O, Object AnotherO)    { return this.equals(null, O, AnotherO);   }
	/** Returns the hash value of the object O. */
	public int hash(Object O)                           { return this.hash(null, O);               }
	/** Compare the object O equals to AnotherO. */
	public int compares(Object O, Object AnotherO)      { return this.compares(null, O, AnotherO); }
	
	// With Context --------------------------------------------------

	/** Returns a string representation of an object with type */
	String getDisplayObject(Context pContext, Object O) {
		if(O == null) return "`null`:Void";
		if(O instanceof Expression) {
			return "`"+((Expression)O).toDetail(this)+"`:Expression";
		}
		String ToStr;
		if(!(O instanceof Objectable) || !UCurry.isObjectableCurry((Objectable)O)) ToStr = UObject.toString(O);
		else {                                                                      
			// Make a context
			if(pContext == null) pContext = (new Executor(this)).newRootContext(null);
			ToStr = this.toString(pContext, O);
		}
		Type T = this.getTypeManager().getTypeOfNoCheck(pContext, O);
		return "`" + ToStr + "`:" + UObject.toString(T);
	}
	/** Returns a short string representation of an object whether or not it is curry scope. */
	String toString(Context pContext, Object O) {		
		if(!(O instanceof Objectable) || !UCurry.isObjectableCurry((Objectable)O))
			return UObject.toString(O);
		
		
		// Make a context
		if(pContext == null) pContext = (new Executor(this)).newRootContext(null);
		Executable Exec   = ((Objectable_Curry)O).getExpr_toString(this);
		Object     Result = null;
		if(Exec != null) {
			// Execute with an appropriate function
			if(!(Exec instanceof Expression)) {
				   Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);
			} else Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
					UObject.EmptyObjectArray, false, true);
		}
		// Process result
		if(Result == null)                  return "null";
		if(Result instanceof String)        return (String)Result;
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);
		
		throw new CurryError(
				String.format(
					"Invalid expression result: `toString` expression of a DataHolder must return a string (%s)",
					this.getDisplayObject(Result)
				),
				pContext);
	}
	/** Returns a long string representation of an object whether or not it is curry scope. */
	String toDetail(Context pContext, Object O) {
		if(O instanceof Expression)
			return ((Expression)O).toDetail(this);
		
		if(O instanceof Executable) {
			String Detail = O.toString();
			
			if(((Executable)O).isCurry())
				Detail += "{ " + this.toDetail(((Executable)O).asCurry().getBody()) + " }";
			
			return Detail;
		}
		
		if(!(O instanceof Objectable) || !UCurry.isObjectableCurry((Objectable)O))
			return UObject.toDetail(O);
		
		// Make a context
		if(pContext == null) pContext = (new Executor(this)).newRootContext(null);
		Executable Exec   = ((Objectable_Curry)O).getExpr_toDetail(this);
		Object     Result = null;
		if(Exec != null) {
			// Execute with an appropriate function
			if(!(Exec instanceof Expression)) {
				   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
						   UObject.EmptyObjectArray, false, true);
			} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);		
		}
		// Process result
		if(Result == null)                  return "null";
		if(Result instanceof String)        return (String)Result;
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);

		throw new CurryError(
				String.format(
					"Invalid expression result: `toDetail` expression of a DataHolder must return a string (%s)",
					this.getDisplayObject(Result)
				),
				pContext);
	}
	/** Checks if the object O is the same with AnotherO whether or not they are curry scope. */
	boolean is(Context pContext, Object O, Object AnotherO) {
		if(O == AnotherO)
		    return true;
		
		final boolean aIsO_DObjectStandalone = (O instanceof DObjectStandalone);
		if (aIsO_DObjectStandalone)
		    O = ((DObjectStandalone)O).getAsDObject();
		
		final boolean aIsO_Objectable       = (O instanceof Objectable);
		final boolean aIsO_Objectable_Curry = aIsO_Objectable && UCurry.isObjectableCurry((Objectable)O);
		if(!aIsO_Objectable_Curry) {
		    final boolean aIs = UObject.is(O, AnotherO);
		    return aIs;
		}
		// Make a context
		if(pContext == null) pContext = (new Executor(this)).newRootContext(null);
		Executable Exec   = ((Objectable_Curry)O).getExpr_is(this, AnotherO);
		Object     Result = null;
		if(Exec != null) {
			// Execute with an appropriate function
			if(!(Exec instanceof Expression)) {
				   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
						   new Object[] { AnotherO }, false, true);
			} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);
		}		
		// Process result
		if(Result == null)                  return false;
		if(Result instanceof Boolean)       return ((Boolean)Result).booleanValue();
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);

		throw new CurryError(
				String.format(
					"Invalid expression result: `is` expression of a DataHolder must return a boolean (%s)",
					this.getDisplayObject(Result)
				),
				pContext);
	}
	/** Checks if the object O equals to AnotherO. */
	boolean equals(Context pContext, Object O, Object AnotherO) {
		if(O == AnotherO) return true;
		if(!(O instanceof Objectable) || !UCurry.isObjectableCurry((Objectable)O)) return UObject.equal(O, AnotherO);
		// Make a context
		if(pContext == null) pContext = (new Executor(this)).newRootContext(null);
		Executable Exec   = ((Objectable_Curry)O).getExpr_equals(this, AnotherO);
		Object     Result = null;
		if(Exec != null) {
			// Execute with an appropriate function
			if(!(Exec instanceof Expression)) {
				   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
						   new Object[] { AnotherO }, false, true);
			} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);
		}		
		// Process result
		if(Result == null)                  return false;
		if(Result instanceof Boolean)       return ((Boolean)Result).booleanValue();
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);
		
		throw new CurryError(
				String.format(
					"Invalid expression result: `equals` expression of a DataHolder must return a boolean (%s)",
					this.getDisplayObject(Result)
				),
				pContext);
	}
	/** Returns the hash value of the object O. */
	int hash(Context pContext, Object O) {
		if(!(O instanceof Objectable) || !UCurry.isObjectableCurry((Objectable)O)) return UObject.hash(O);
		// Make a context
		if(pContext == null) pContext = (new Executor(this)).newRootContext(null);
		Executable Exec   = ((Objectable_Curry)O).getExpr_hash(this);
		Object     Result = null;
		if(Exec != null) {
			// Execute with an appropriate function
			if(!(Exec instanceof Expression)) {
				   Result = pContext.getExecutor().execExecutable(pContext, Exec, Exec, null, true, null,
						   UObject.EmptyObjectArray, false, true);
			} else Result = pContext.getExecutor().execInternal(pContext, (Expression)Exec);	
		}	
		// Process result
		if(Result == null)                  return 0;
		if(Result instanceof Integer)       return ((Integer)Result).intValue();
		if(Result instanceof SpecialResult) throw ((SpecialResult)Result).getException(pContext);
		throw new CurryError(
				String.format(
					"Invalid expression result: `hashCode` expression of a DataHolder must return an integer (%s)",
					this.getDisplayObject(Result)
				),
				pContext);
	}
	/** Compare the object O equals to AnotherO. */
	int compares(Context pContext, Object O, Object AnotherO) {
		if(O == AnotherO) return 0;
		if((O instanceof Comparable<?>) && (AnotherO instanceof Comparable<?>) &&
					(O.getClass().isAssignableFrom(AnotherO.getClass()) ||
					AnotherO.getClass().isAssignableFrom(O.getClass()))) {
			return UObject.compare(O, AnotherO);
		}
		return this.hash(pContext, O) - this.hash(pContext, AnotherO);
	}
	
	public Comparator<? extends Object> getDefaultComparator() {
	    final Comparator<? extends Object> aComparator = Comparators.get(this);
	    if (aComparator == null) {
	        final Comparator<? extends Object> aNewComparator = new Comparator<Object>() {
	            final public @Override int compare(
	                    final Object pObj1,
	                    final Object pObj2) {
	                final int aCompareResult = compares(pObj1, pObj2);
	                return aCompareResult;
	            }
	            final public @Override boolean equals(final Object pObj) {
	                return (this == pObj);
	            }
            };
	        Comparators.put(this, aNewComparator);
	        return aNewComparator;
	    }
	    return aComparator;
	}
    
    public Comparator<? extends Object> getHashComparator() {
        final Comparator<? extends Object> aComparator = HashComparators.get(this);
        if (aComparator == null) {
            final Comparator<? extends Object> aNewComparator = new Comparator<Object>() {
                final public @Override int compare(
                        final Object pObj1,
                        final Object pObj2) {
                    final int aHash1 = hash(pObj1);
                    final int aHash2 = hash(pObj2);
                    return aHash1 - aHash2;
                }
                final public @Override boolean equals(final Object pObj) {
                    return (this == pObj);
                }
            };
            HashComparators.put(this, aNewComparator);
            return aNewComparator;
        }
        return aComparator;
    }
	
	static public void debugPoint() {
		System.err.println("Debug Point.");
	}
	static public void debugPoint(String pMsg) {
		System.err.println("Debug Point: " + pMsg);
	}
}
