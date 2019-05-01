package net.nawaman.curry;

import java.util.*;

import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.util.*;
import net.nawaman.util.UArray;

/** DataContext */
public class Context extends ScopePrivate {

	/** Name of StackOwner variable */
	static public final String StackOwner_VarName              = "$This$";
	/** Name of StackOwner as type variable */
	static public final String StackOwnerAsType_VarName        = "$Type$";
	/** Name of StackOwner as Current type variable */
	static public final String StackOwnerAsCurrentType_VarName = "$Current$";
	/** Name of StackOwner as package variable */
	static public final String StackOwnerAsPackage_VarName     = "$Package$";
	
	/** Checks if the given name is one of the StackOwner variablename */
	static public boolean isStackOwnerVariableNames(String VName) {
		if(VName == null) return false;
		if(VName.equals(StackOwner_VarName))              return true;
		if(VName.equals(StackOwnerAsType_VarName))        return true;
		if(VName.equals(StackOwnerAsCurrentType_VarName)) return true;
		if(VName.equals(StackOwnerAsPackage_VarName))     return true;
		return false;
	}

	/** Constructs a context */
	Context(Context pParent, Executor pExecutor, String pName, Executable pInitiator) {
		super(pName, pParent);
		this.Initiator   = pInitiator;
		this.ContextExec = (this instanceof ContextExecutable)
								?(ContextExecutable)this
								:((pParent instanceof Context)?((Context)this.Parent).ContextExec:null);
	
		if(     pExecutor != null) this.Executor = pExecutor;
		else if(pParent   != null) this.Executor = pParent.getExecutor();
		else                       throw new NullPointerException();
	}
	/** Constructs a context */
	Context(Context pParent, String pName, Executable pInitiator) {
		this(pParent, (pParent == null)?null:pParent.getExecutor(), pName, pInitiator);
	}

	// Services ------------------------------------------------------------------------------------

	final Executable Initiator;
	final Executor   Executor;

	final Executable getInitiator() { return this.Initiator;       }
	final Engine     getEngine()    { return this.Executor.Engine; }
	final Executor   getExecutor()  { return this.Executor;        }

	/** Checks if the context is one of the parent of this one */
	boolean isAlive(Context pCurrentContext) {
		if(pCurrentContext == this) return  true;
		if(pCurrentContext == null) return false;
		return pCurrentContext.isParent(this);
	}
	/** Checks if the context is one of the parent of this one */
	boolean isParent(Context pCurrentContext) {
		if(pCurrentContext == this) return  true;
		if(pCurrentContext == null) return false;
		return (this.Parent instanceof Context)?((Context)this.Parent).isParent(pCurrentContext):false;
	}
	
	// Root & Global Scope and Context  -------------------------------------------------------------

	ContextGlobal getRootContext() {
		if(this.Parent != null) return ((Context)this.Parent).getRootContext();
		return null;
	}

	final Scope getEngineScope() {
		return this.getEngine().getEngineScope();
	}
	final Scope getGlobalScope() {
		if(this instanceof ContextGlobal) return ((ContextGlobal)this).GlobalScope;
		
		ContextGlobal RC = this.getRootContext();
		return (RC != null) ? RC.GlobalScope : null;
	}

	ScopePrivate getActualParent() {
		return this.getParent();
	}

	final Context getActualParentContext() {
		ScopePrivate AParent = this.getActualParent();
		return (AParent instanceof Context) ? (Context)AParent : null;
	}
	
	
	
	// Executable ----------------------------------------------------------------------------------

	final ContextExecutable ContextExec;
	
	ContextExecutable getExecutableContext() {
		return this.ContextExec;
	}
	
	Executable getExecutable() {
		return (this.ContextExec == null)?null:this.ContextExec.getExecutable();
	}
	
	DataHolder getFrozenDataHolder(String pDHName) {
		return (this.ContextExec == null)?null:this.ContextExec.getFrozenDataHolder(pDHName);
	}

	DataHolder getParameterDataHolder_form_Executable(String pDHName) {
		if ((this.ContextExec == null)|| (this.ContextExec == this))
			return null;
		
		return this.ContextExec.getParameterDataHolder_form_Executable(pDHName);
	}

	// DataHolders -------------------------------------------------------------
	
	@Override
	DataHolder getLocalDataHolder(String pDHName) {
		DataHolder DH = this.getFrozenDataHolder(pDHName);
		return (DH != null)?DH:super.getLocalDataHolder(pDHName);
	}

	Hashtable<String, DataHolder> PDHs_Cahce;

	/// NOTE: Cache the parent. This is done because it takes long time to search for variables in all parent context. 
	@Override
	DataHolder getParentDataHolder(String pDHName) {
		if(this.PDHs_Cahce != null) {
			DataHolder DH = this.PDHs_Cahce.get(pDHName);
			if(DH != null) return DH;
		}
		// Get from the parent
		ScopePrivate TheParent = this.Parent;
		while(TheParent != null) {
			DataHolder DH = TheParent.getLocalDataHolder(pDHName);
			if(DH != null) {
				if(this.PDHs_Cahce == null) this.PDHs_Cahce = new Hashtable<String, DataHolder>();
				this.PDHs_Cahce.put(pDHName, DH);
				return DH;
			}
			TheParent = TheParent.Parent;
		}
		Context RC = this.getRootContext();
		if(RC != null) {
			if(this != RC) {
				DataHolder DH = RC.getDataHolder(pDHName);
				if(DH != null) {
					if(this.PDHs_Cahce == null)
						this.PDHs_Cahce = new Hashtable<String, DataHolder>();
					this.PDHs_Cahce.put(pDHName, DH);
					return DH;
				}
			}
		}
		return null;
	}

	/** Set the variable without given the engine (since Context has engine embedded) */
	Object setVariableValue(String pVarName, Object pNewValue) {
		return super.setValue(this.getEngine(), pVarName, pNewValue);
	}

	/** Get the variable without given the engine (since Context has engine embedded) */
	Object getVariableValue(String pVarName) {
		return super.getValue(this.getEngine(), pVarName);
	}
	
	// New Borrowed Variable (for Macro and Fragment) -------------------------------------
	
	Object newBorrowedVariable(String pVName, Type pType, Object pDefaultValue) {
		DataHolder DH = this.getLocalDataHolder(pVName);
		if(DH == null) {
			// If not exist, try the parent stack
			DH = this.getDataHolder(pVName);
			if(DH == null) return this.newVariable(this.getEngine(), pVName, pType, pDefaultValue);
		}
		// Found the variable
		if(!this.getEngine().getDataHolderManager().isDHWritable(pVName, DH)) // The original is not writable, so create one here
			return this.newVariable(this.getEngine(), pVName, pType, pDefaultValue);
		
		return this.addDataHolder(pVName, DH);
	}
	
	Object newBorrowedConstant(String pVName, Type pType, Object pDefaultValue) {
		DataHolder DH = this.getLocalDataHolder(pVName);
		if(DH == null) {
			// If not exist, try the parent stack
			DH = this.getDataHolder(pVName);
			if(DH == null) return this.newConstant(this.getEngine(), pVName, pType, pDefaultValue);
		}
		// Found the variable
		if(this.getEngine().getDataHolderManager().isDHWritable(pVName, DH)) // The original is not writable, so create one here
			return this.newConstant(this.getEngine(), pVName, pType, pDefaultValue);
		
		return this.addDataHolder(pVName, DH);
	}
	
	@Override
	boolean isVariableConstant(String pDHName) {
		DataHolder DH = this.getDataHolder(pDHName);
		if(DH == null) return false;
		return !this.getEngine().getDataHolderManager().isDHWritable(this, pDHName, DH);
	}
	
	boolean isVariableDefaultDataHolder(String pDHName) {
		DataHolder DH = this.getDataHolder(pDHName);
		if(DH == null) return false;
		return this.getEngine().getDataHolderManager().getDefaultDataHolderFactory().isInstance(DH);
	}
	
	boolean checkVariableDataHolderFactory(String DHFactoryName, String pDHName) {
		if(DHFactoryName == null) return false;
		DataHolderFactory DHF = this.getEngine().getDataHolderManager().getDataHolderFactory(DHFactoryName);
		if(DHF == null) return false;
		DataHolder DH = this.getDataHolder(pDHName);
		if(DH == null) return false;
		return DHF.isInstance(DH);
	}
	
	// Parent ----------------------------------------------------------------------------------------------------------
	
	int getParentContextIndex(String pName) {
		if(pName == null) return 0;
		Context P = this;
		int i = 0;
		while(P != null) {
			if(pName.equals(P.getName())) return i;
			i++;
			if(!(P.Parent instanceof Context)) break;
			P = (Context)P.Parent;
		}
		return -1;
	}
	
	// By Count -----------------------------------------------------------------------------------

	DataHolder getParentDataHolder(int Count, String pVarName) {
		if(Count < 0) throw new CurryError("Parent variable does not exist (" + pVarName + ").");
		Context P = this;
		for(; Count > 0; --Count) {
			P = (Context)P.Parent;
			if(P == null) break;
		}
		DataHolder DH = (P == null)?null:P.getDataHolder(pVarName);
		if(DH == null) throw new CurryError("Parent variable does not exist (" + pVarName + ").");
		return DH;
	}
	
	Object setParentVariableValue(int Count, String pVarName, Object pNewValue) {
		DataHolder DH = this.getParentDataHolder(Count, pVarName);
		return this.getEngine().getDataHolderManager().setDHData(this, pVarName, DH, pNewValue);
	}

	Object getParentVariableValue(int Count, String pVarName) {
		DataHolder DH = this.getParentDataHolder(Count, pVarName);
		return this.getEngine().getDataHolderManager().getDHData(this, pVarName, DH);
	}

	boolean isParentVariableExist(int Count, String pVarName) {
		DataHolder DH = this.getParentDataHolder(Count, pVarName);
		return (DH != null);
	}

	boolean isParentVariableConstant(int Count, String pVarName) {
		DataHolder DH = this.getParentDataHolder(Count, pVarName);
		return !this.getEngine().getDataHolderManager().isDHWritable(this, pVarName, DH);
	}

	Type getParentVariableType(int Count, String pVarName) {
		DataHolder DH = this.getParentDataHolder(Count, pVarName);
		return this.getEngine().getDataHolderManager().getDHType(this, pVarName, DH);
	}
	
	// By name ------------------------------------------------------------------------------------

	DataHolder getParentDataHolder(String pStackName, String pVarName) {
		int Index = this.getParentContextIndex(pStackName);
		return this.getParentDataHolder(Index, pVarName);
	}

	Object setParentVariableValue(String pStackName, String pVarName, Object pNewValue) {
		int Index = this.getParentContextIndex(pStackName);
		return this.setParentVariableValue(Index, pVarName, pNewValue);
	}
	Object getParentVariableValue(String pStackName, String pVarName) {
		int Index = this.getParentContextIndex(pStackName);
		return this.getParentVariableValue(Index, pVarName);
	}
	boolean isParentVariableExist(String pStackName, String pDHName) {
		int Index = this.getParentContextIndex(pStackName);
		return this.isParentVariableExist(Index, pDHName);
	}
	boolean isParentVariableConstant(String pStackName, String pDHName) {
		int Index = this.getParentContextIndex(pStackName);
		return this.isParentVariableConstant(Index, pDHName);
	}
	
	Type getParentVariableType(String pStackName, String pDHName) {
		int Index = this.getParentContextIndex(pStackName);
		return this.getParentVariableType(Index, pDHName);
	}

	// Engine Scope ----------------------------------------------------------------------------------------------------
	
	Object setEngineVariableValue(String pVarName, Object pNewValue) {
		ScopePrivate SP = this.getEngineScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pVarName);
		if(DH == null)
			throw new CurryError("Engine variable \'" + pVarName + "\' does not exist.");

		// No need to check for writability and compatibility

		return this.getEngine().getDataHolderManager().setDHData(this, pVarName, DH, pNewValue);
	}

	DataHolder getEngineDataHolder(String pDHName) {
		ScopePrivate SP = this.getEngineScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pDHName);
		return DH;
	}

	Object getEngineVariableValue(String pDHName) {
		DataHolder DH = this.getEngineDataHolder(pDHName);
		return this.getEngine().getDataHolderManager().getDHData(this, pDHName, DH);
	}

	boolean isEngineVariableExist(String pDHName) {
		ScopePrivate SP = this.getEngineScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pDHName);
		return (DH != null);
	}

	boolean isEngineVariableConstant(String pDHName) {
		ScopePrivate SP = this.getEngineScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pDHName);
		return (DH != null)?!this.getEngine().getDataHolderManager().isDHWritable(pDHName, DH):false;
	}

	Type getEngineVariableType(String pDHName) {
		ScopePrivate SP = this.getEngineScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pDHName);
		return (DH != null)?this.getEngine().getDataHolderManager().getDHType(pDHName, DH):null;
	}

	// Global Scope ----------------------------------------------------------------------------------------------------

	// Global scope control ------------------------------------------------------------------------

	boolean isGlobalStack() {
		return false;
	}
	
	boolean isNewGlobalVariableEnabled() { // Every one
		ContextGlobal GC = this.getRootContext();
		if(GC == null) return false;
		Scope S = GC.getGlobalScope();
		if(S == null) return false;
		// Not enable
		if(!S.isNewVarAllowed()) return false;
		// Not enable to all
		if(!S.isToAll() && (GC.getStackOwner() != this.getStackOwner())) return false;
		return true;
	}

	boolean enableGlobalNewVariable()       { return false; }
	boolean disableGlobalNewVariable()      { return false; }
	boolean enableGlobalNewVariableToAll()  { return false; }
	boolean disableGlobalNewVariableToAll() { return false; }

	Object newGlobalVariable(String pVName, Type pType, Object pDefaultValue, boolean pIsConstant) {
		if(!this.isNewGlobalVariableEnabled())
			throw new CurryError("Global scope does not allow to create a variable (" + pVName + ").");
		ScopePrivate SP = this.getGlobalScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pVName);
		else           throw new CurryError("Global scope does not exist (" + pVName + ").");
		if(DH != null) throw new CurryError("Global variable is already exist (" + pVName + ").");
		if(pIsConstant) return SP.newConstant(this.getEngine(), pVName, pType, pDefaultValue);
		else            return SP.newVariable(this.getEngine(), pVName, pType, pDefaultValue);
	}

	Object addGlobalDataHolder(String pDHName, DataHolder pDH) {
		if(!this.isNewGlobalVariableEnabled())
			throw new CurryError("Global scope does not allow to create a variable (" + pDHName + ").");
		ScopePrivate SP = this.getGlobalScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pDHName);
		else           throw new CurryError("Global scope does not exist (" + pDHName + ").");
		if(DH != null) throw new CurryError("Global variable is already exist (" + pDHName + ").");
		if(!SP.isNewVarAllowed())
			throw new CurryError("Global scope does not allow to create a variable (" + pDHName + ").");
		return SP.addDataHolder(pDHName, pDH);
	}

	Object setGlobalVariableValue(String pVarName, Object pNewValue) {
		ScopePrivate SP = this.getGlobalScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pVarName);
		else           throw new CurryError("Global scope does not exist (" + pVarName + ").");
		if(DH == null) throw new CurryError("Global variable \'" + pVarName + "\' does not exist.");
		return this.getEngine().getDataHolderManager().setDHData(this, pVarName, DH, pNewValue);
	}

	DataHolder getGlobalDataHolder(String pDHName) {
		ScopePrivate SP = this.getGlobalScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pDHName);
		else           throw new CurryError("Global scope does not exist (" + pDHName + ").");
		return DH;
	}

	Object getGlobalVariableValue(String pDHName) {
		DataHolder DH = this.getGlobalDataHolder(pDHName);
		if(DH == null) throw new CurryError("Global variable \'" + pDHName + "\' does not exist.");
		return this.getEngine().getDataHolderManager().getDHData(this, pDHName, DH);
	}

	boolean isGlobalVariableExist(String pDHName) {
		ScopePrivate SP = this.getGlobalScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pDHName);
		return (DH != null);
	}

	boolean isGlobalVariableConstant(String pDHName) {
		ScopePrivate SP = this.getGlobalScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pDHName);
		return (DH != null)?!this.getEngine().getDataHolderManager().isDHWritable(pDHName, DH):false;
	}

	Type getGlobalVariableType(String pDHName) {
		ScopePrivate SP = this.getGlobalScope();
		DataHolder DH = null;
		if(SP != null) DH = SP.getDataHolder(pDHName);
		return (DH != null)?this.getEngine().getDataHolderManager().getDHType(pDHName, DH):null;
	}

	// StackOwner and StackCaller ------------------------------------------------------------------

	Object getStackOwner() {
		if(this.Parent != null) return ((Context)this.Parent).getStackOwner();
		return this.getEngine().getDefaultStackOwner();
	}

	Type getStackOwnerAsType() {
		if(this.Parent != null) return ((Context)this.Parent).getStackOwnerAsType();
		return this.getEngine().getTypeManager().getTypeOfNoCheck(this, this.getStackOwner());
	}
	Type getStackOwnerAsCurrentType() {
		if(this.Parent != null) return ((Context)this.Parent).getStackOwnerAsCurrentType();
		return this.getEngine().getTypeManager().getTypeOfNoCheck(this, this.getStackOwner());
	}

	Package getStackOwnerAsPackage() {
		if(this.Parent != null) return ((Context)this.Parent).getStackOwnerAsPackage();
		return this.getEngine().getDefaultPackage();
	}

	StackOwner getStackCaller() {
		Object Caller = (this.Parent != null) ? ((Context)this.Parent).getStackOwner() : this.getStackOwner();
		return (Caller instanceof StackOwner)?(StackOwner)Caller:null;
	}

	Type getStackCallerAsType() {
		return (this.Parent != null) ? ((Context)this.Parent).getStackCallerAsType() : this.getStackOwnerAsType();
	}

	Package getStackCallerAsPackage() {
		return (this.Parent != null) ? ((Context)this.Parent).getStackCallerAsPackage() : this.getStackOwnerAsPackage();
	}

	Object getDelegateSource() {
		if(this.Parent != null) return ((Context)this.Parent).getDelegateSource();
		return null;
	}
	
	/** Checks if this stack is run during the construction of the owner. */
	boolean isConstructor() {
		return (this.Parent != null) ? ((Context)this.Parent).isConstructor() : this.isConstructor();
	}

	// Location ------------------------------------------------------------------------------------

	Expression    CurrentExpression    = null;
	int           CurrentCoordinate    =   -1;
	Documentation CurrentDocumentation = null;
	
	/** Change the coordinate */
	void setCurrentExpression(Expression Expr) {
		if(Expr == null) return;
		this.CurrentExpression = Expr;
		
		// Set the current coordinate if the expr's coordinate is not -1
		int Coordinate = Expr.getCoordinate();
		if(Coordinate != -1) this.CurrentCoordinate = Coordinate; 
	}
	void setCurrentDocumentation(Documentation pDocumentation) {
		this.CurrentDocumentation = pDocumentation;
	}

	/** Get the current line number */
	int getCurrentCoordinate() {
		if(this.CurrentExpression != null) return this.CurrentCoordinate;
		return ((Context)this.Parent).getCurrentCoordinate();
	}
	/** Get the current co-ordinate */
	Expression getCurrentExpression() {
		if(this.CurrentExpression != null)
			return this.CurrentExpression;

		// If case that the executable is an expression (no location), the location is of the parent
		if(this.Parent instanceof Context) {
			this.CurrentExpression = ((Context)this.Parent).getCurrentExpression();
			return this.CurrentExpression;
		}
		
		return null;
	}
	/** Get the current documenation */
	Documentation getCurrentDocumentation() {
		if(this.CurrentDocumentation != null)
			return this.CurrentDocumentation;
		
		if(this.Parent instanceof Context) {
			Documentation Doc = ((Context)this.Parent).getCurrentDocumentation();
			if(Doc != null) return Doc;
		}

		return Documentation.Util.getDocumentationOf(this.getExecutable());
	}

	/** Get the current location */
	Location getStackLocation() {
		return ((Context)this.Parent).getStackLocation();
	}
	/** Get the current line number */
	int getCurrentLineNumber() {
		return Location.getRow(this.getCurrentCoordinate());
	}
	/** Get the current column number */
	int getCurrentColumn() {
		return Location.getCol(this.getCurrentCoordinate());
	}
	/** Return the identification of the current stack like "function()" */
	String getStackIdentification() {
		return ((Context)this.Parent).getStackIdentification();
	}

	/** Returns the local snapshot of this location */
	final LocationSnapshot getCurrentLocationSnapshot() {
		String  Identity    = null;
		Context ExecContext = this.getExecutableContext();
		if(ExecContext == null) ExecContext = this;
		
		if      (ExecContext.getStackOwner() instanceof Package) Identity = ExecContext.getStackOwnerAsPackage().getName();
		else if (ExecContext.getStackOwnerAsType() != null)      Identity = ExecContext.getStackOwnerAsType().getTypeRef().toString();
		else                                                     Identity = ExecContext.getStackOwner()                   .toString();
		
		ExecSignature Signature = null;
		if(this.getExecutable() != null) Signature = this.getExecutable().getSignature();
		
		return LocationSnapshot.create(
				Identity,
				Signature,
				this.getCurrentExpression(),
				this.getCurrentCoordinate(),
				this.getCurrentDocumentation()
			);
	}
	
	// TODO - This is a Hack
	/** Returns the Caller ActionRecord that call Intializer */
	ActionRecord getInitializerCaller() {
		return ExternalContext.newActionRecord(this);
	}

	// Locations -----------------------------------------------------------------------------------

	static final String        ROOT_IDENTITY    = "<<-- ROOT -->>";
	static final String        UNNAMED_CODE     = "<<Umnamed Code>>";
	static final Location      UNNAMED_LOCATION = new Location(ROOT_IDENTITY);
	static final ExecSignature ROOT_SIGNATURE   = ExecSignature.newEmptySignature("root", UNNAMED_LOCATION, null);
	
	static LocationSnapshot[] ABSOLUTE_ROOT_LOCATION = null;

	static private final LocationSnapshot[] getAbsoluteRootLocations() {
		if(ABSOLUTE_ROOT_LOCATION == null) {
			ABSOLUTE_ROOT_LOCATION = new LocationSnapshot[] {
				LocationSnapshot.create(
					ROOT_IDENTITY,
					ROOT_SIGNATURE,
					null,
					-1,
					null
				)
			};
		}
		return ABSOLUTE_ROOT_LOCATION.clone();
	}
	final private LocationSnapshot[] getRootLocations() {
		return new LocationSnapshot[] {
			LocationSnapshot.create(
				ROOT_IDENTITY,
				ROOT_SIGNATURE,
				this.getCurrentExpression(),
				this.getCurrentCoordinate(),
				this.getCurrentDocumentation()
			)
		};
	}
	
	/** Snapshot of the stack trace */
	final private LocationSnapshot[] getLocations() {
		if((this instanceof ContextGlobal) || (this instanceof ContextRoot)) {
			
			if((this.getCurrentExpression()    == null) &&
			   (this.getCurrentCoordinate()    ==   -1) &&
			   (this.getCurrentDocumentation() == null))
				 return LocationSnapshot.EmptyLocationSnapshots;
			else return this.getRootLocations();
		}
			

		// Get the closest executable-context location
		LocationSnapshot[] Ls = null;
		Context ExecParent;
		if((ExecParent = this.getExecutableContext()) != null) {
			Context TheParent;
			if((TheParent = ExecParent.getActualParentContext()) != null) {
				// Ignore the root
				if((TheParent instanceof ContextGlobal) || (TheParent instanceof ContextRoot))
					 Ls = LocationSnapshot.EmptyLocationSnapshots;
				else Ls = TheParent.getLocations();
			}
		}

		// Get this location
		if(this.getStackLocation() == null) {
			if((Ls == null) || (Ls.length == 0))
				return this.getRootLocations();

			return Ls;
		}

		// Originate here
		if((Ls == null) || (Ls.length == 0))
			return new LocationSnapshot[] { this.getCurrentLocationSnapshot() };

		// Extends
		Ls = UArray.resizeArray(Ls, Ls.length + 1);
		Ls[Ls.length - 1] = this.getCurrentLocationSnapshot();
		return Ls;
	}

	static public LocationSnapshot[] getLocationsOf(Context pContext) {
		if(pContext == null) return Context.getAbsoluteRootLocations();
		
		LocationSnapshot[] Ls = pContext.getLocations();

		if((Ls == null) || (Ls.length == 0))
			return pContext.getRootLocations();
		
		return Ls;
	}

	static public String getLocationsToString(LocationSnapshot[] Locations) {
		if(Locations        == null) return "";
		if(Locations.length ==    0) return ROOT_IDENTITY;
		
		String[] Names = new String[Locations.length];
		String[] LCRs  = new String[Locations.length];
		int Width_At = 0;
		int Width_RC = 0;
		for(int i = Locations.length; --i >= 0;) {
			LocationSnapshot LS = Locations[i];
			Location         L  = LS.getLocation();
			
			String CN = L.isCode() ? UNNAMED_CODE : L.getCodeName();
			
			int W = (Names[i] = CN).length();
			if(W > Width_At) Width_At = W;
			
			W = (LCRs[i] = Location.getCoordinateAsString(LS.getCoordinate())).length();
			if(W > Width_RC) Width_RC = W;
		}
		
		// Print them in the invert order (so it will be the same as in Java)
		StringBuffer SB = new StringBuffer();
		for(int i = Locations.length; --i >= 0;) {
			SB.append("\n\t");
			
			LocationSnapshot LS = Locations[i];

			String Name = Names[i];
			String LCR  = LCRs[i];
			SB.append(Name)             .append(String.format("%" + (1 + Width_At - Name.length()) + "s", ""));
			SB.append("at ").append(LCR).append(String.format("%" + (1 + Width_RC - LCR .length()) + "s", ""));
			
			String ON = LS.OwnerName;
			String SN = (LS.getSignature() == null) ? null : LS.getSignature().toString();

			SB.append("=> ");
			if((ON != null) && !EngineExtensions.EE_DefaultPackage.DefaultPackageName.equals(ON)) {
				if(ON != null) {
					SB.append(ON);
					if(SN != null) SB.append(".");
				}
			}
			if(SN != null) SB.append(SN);
		}
		return SB.toString();
	}

	static String getLocationsToString(Context pContext) {
		LocationSnapshot[] Locations = Context.getLocationsOf(pContext);
		if(Locations == null) return "";
		return "\n\t" + UArray.toString(Locations, "", "", "\n\t");
	}

	String getLocationsToString() {
		return getLocationsToString(Context.getLocationsOf(this));
	}

	// Subclasses ------------------------------------------------------------------------------------------------------
	
	/** Stack context that has no parent and has global variable */
	static class ContextGlobal extends Context {

		/** Constructs a context */
		ContextGlobal(Executor pExecutor, Scope pGlobalScope) {
			super(null, pExecutor, (pGlobalScope == null) ? null : pGlobalScope.ScopeName, null);
			this.GlobalScope = pGlobalScope;
			this.CurrentExpression = null;
		}

		// Stack Caller and Owner
		// ----------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override StackOwner getStackOwner()              { return this.getEngine().getDefaultStackOwner(); }
		/**{@inheritDoc}*/ @Override Type       getStackOwnerAsType()        { return TKJava.TPackage; }
		/**{@inheritDoc}*/ @Override Type       getStackOwnerAsCurrentType() { return TKJava.TPackage; }
		/**{@inheritDoc}*/ @Override Package    getStackOwnerAsPackage()     { return this.getEngine().getDefaultPackage(); }

		/**{@inheritDoc}*/ @Override StackOwner getStackCaller()           { return null; }
		/**{@inheritDoc}*/ @Override Type       getStackCallerAsType()     { return null; }
		/**{@inheritDoc}*/ @Override Package    getStackCallerAsPackage()  { return null; }

		/**{@inheritDoc}*/ @Override
		boolean isConstructor() {
			return false;
		}

		// Global Context and scope
		// --------------------------------------------------------------------

		final Scope GlobalScope;

		/**{@inheritDoc}*/ @Override
		ContextGlobal getRootContext() {
			return this;
		}

		// Global scope control ------------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		boolean isGlobalStack() {
			return true;
		}
		/**{@inheritDoc}*/ @Override
		boolean isNewGlobalVariableEnabled() {
			return true;
		}
		
		/**{@inheritDoc}*/ @Override
		boolean enableGlobalNewVariable()    {
			if(this.GlobalScope != null) { this.GlobalScope.enableNewVar();	return true; }
			return false;
		}

		/**{@inheritDoc}*/ @Override
		boolean disableGlobalNewVariable() {
			if(this.GlobalScope != null) { this.GlobalScope.disableNewVar(); return true; }
			return false;
		}

		/**{@inheritDoc}*/ @Override
		boolean enableGlobalNewVariableToAll() {
			if(this.GlobalScope != null) { this.GlobalScope.enableToAll(); return true;	}
			return false;
		}

		/**{@inheritDoc}*/ @Override
		boolean disableGlobalNewVariableToAll() {
			if(this.GlobalScope != null) { this.GlobalScope.disableToAll(); return true; }
			return false;
		}

		// Variables -----------------------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		DataHolder getParentDataHolder(String pDHName) {
			// Get form cache
			if(this.PDHs_Cahce != null) {
				DataHolder DH = this.PDHs_Cahce.get(pDHName);
				if(DH != null) return DH;
			}
			// Access to global scope
			if(this.GlobalScope != null) {
				DataHolder DH = this.GlobalScope.getDataHolder(pDHName);
				if(DH != null) {
					if(this.PDHs_Cahce == null) this.PDHs_Cahce = new Hashtable<String, DataHolder>();
					this.PDHs_Cahce.put(pDHName, DH);
					return DH;
				}
			}
			// Access to engine scope
			if(this.getEngineScope() != null) {
				DataHolder DH = this.getEngineScope().getDataHolder(pDHName);
				if(DH != null) {
					if(this.PDHs_Cahce == null) this.PDHs_Cahce = new Hashtable<String, DataHolder>();
					this.PDHs_Cahce.put(pDHName, DH);
					return DH;
				}
			}
			return super.getParentDataHolder(pDHName);
		}

		// Locations -----------------------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override Location getStackLocation()       { return null; }
		/**{@inheritDoc}*/ @Override String   getStackIdentification() { return null; }
		
		/**{@inheritDoc}*/ @Override
		int getCurrentCoordinate() {
			return this.CurrentCoordinate;
		}
		/**{@inheritDoc}*/ @Override
		Expression getCurrentExpression() {
			return this.CurrentExpression;
		}
		/**{@inheritDoc}*/ @Override
		Documentation getCurrentDocumentation() {
			return this.CurrentDocumentation;
		}
	}

	/** Stack context that has executable that own it and so the identity and location */
	static class ContextExecutable extends Context {

		ContextExecutable(Context pParent, Executor pExecutor, Executable pInitiator, String pName, Executable pExec) {
			super(pParent, pExecutor, pName, pInitiator);
			this.Exec = pExec;

			this.RAWContextExec = ((pParent instanceof Context)?((Context)this.Parent).ContextExec:null);
		}

		ContextExecutable(Context pParent, Executable pInitiator, String pName, Executable pExec) {
			this(pParent, (pParent == null)?null:pParent.getExecutor(), pInitiator, pName, pExec);
		}

		// Variable ---------------------------------------------------------------------------

		final Executable        Exec;
		final ContextExecutable RAWContextExec;
		
		/**{@inheritDoc}*/ @Override
		Executable getExecutable() {
			return this.Exec;
		}
		
		private HashMap<String, DataHolder> FrozenVariableCache = null;
		
		/**{@inheritDoc}*/ @Override
		DataHolder getFrozenDataHolder(String pDHName) {
			Executable E = this.Exec;
			while(E instanceof WrapperExecutable) E = ((WrapperExecutable)E).getWrapped();
				
			if(!(E instanceof AbstractExecutable)) return null;
			if(this.FrozenVariableCache == null) {
				AbstractExecutable AE = (AbstractExecutable)E;
				if(AE.getFrozenVariableCount() != 0) {
					this.FrozenVariableCache = new HashMap<String, DataHolder>();
					for(int i = AE.getFrozenVariableCount(); --i >= 0; ) {
						String FVName = AE.getFrozenVariableName(i);
						this.FrozenVariableCache.put(FVName, AE.getFrozenScope().getDataHolder(FVName));
					}
				}
			}
			DataHolder DH = (this.FrozenVariableCache == null)?null:this.FrozenVariableCache.get(pDHName);
			
			if((DH == null) && !(this instanceof ContextSubRoutine)) {
				// TODO - This is a Hack
				if((pDHName.charAt(0) == 't') && "this".equals(pDHName)) return null;
				if(pDHName.charAt(0) == '$') {
					if(Context.StackOwnerAsType_VarName       .equals(pDHName)) return null;
					if(Context.StackOwnerAsPackage_VarName    .equals(pDHName)) return null;
					if(Context.StackOwnerAsCurrentType_VarName.equals(pDHName)) return null;
				}
					
				DH = (this.RAWContextExec == null)?null:this.RAWContextExec.getFrozenDataHolder(pDHName);
			}
			
			return DH; 
		}
		
		// Location ------------------------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		String getStackIdentification() {
			if(this.Exec                == null) return null;
			if(this.Exec.getSignature() == null) return null;
			return this.Exec.getSignature().toString();
		}
		/**{@inheritDoc}*/ @Override
		Location getStackLocation() {
			if(this.Exec                == null) return null;
			if(this.Exec.getSignature() == null) return null;
			return this.Exec.getSignature().getLocation();
		}
		/**{@inheritDoc}*/ @Override
		int getCurrentCoordinate() {
			if(this.CurrentExpression != null)
				return this.CurrentCoordinate;

			this.CurrentExpression = this.getCurrentExpression();
			
			if(this.CurrentExpression != null)
				return this.CurrentCoordinate;
			
			Location L = this.getStackLocation();
			if(L == null) { if(this.Exec instanceof Expression) return ((Expression) this.Exec).getCoordinate(); }
			else          {                                     return L.getCoordinate();                        }

			return -1;
		}
	}

	/** Stack context that are on the top of an execution - If delegate all creation of variable to its Scope */
	static class ContextRoot extends ContextExecutable {
		ContextRoot(Executor pExecutor, Scope pGlobalScope, Scope pTopScope, Executable pExecutable) {
			// The parent is a global context from the global scope
			super(new ContextGlobal(pExecutor, pGlobalScope), pExecutor, pExecutable, "Root", pExecutable);
			Scope S = ((pTopScope == null)?new Scope():pTopScope);
			if(S.DataHolders == null) S.DataHolders = new Hashtable<String, DataHolder>();
			this.DataHolders = S.DataHolders;
		}
	}

	/** Stack context that has executable that own it and so the identity and location */
	static class ContextFragment extends ContextExecutable {
		ContextFragment(Context pParent, Executable pInitiator, String pName, Executable.Fragment pFragment) {
			this(pParent, pInitiator, null, pName, pFragment);
		}
		ContextFragment(Context pParent, Executable pInitiator, Object pOwner,  String pName,
				Executable.Fragment pFragment) {
			super(pParent, pInitiator, pName, pFragment);

			this.Parent    = null;
			this.RawParent = pParent;
			if(this.RawParent != null) {	// Hijack the properties
				this.Parent      = this.RawParent.Parent;
				this.DataHolders = this.RawParent.DataHolders;
				this.PDHs_Cahce  = this.RawParent.PDHs_Cahce;
			}
			
			this.Owner = (pOwner != null) ? pOwner : pParent.getEngine().getDefaultStackOwner();
		}

		final Object  Owner;
		final Context RawParent;

		/** {@inheritDoc} */ @Override
		boolean isParent(Context pCurrentContext) {
			if(pCurrentContext == this) return  true;
			if(pCurrentContext == null) return false;
			return (this.RawParent instanceof Context)?((Context)this.RawParent).isParent(pCurrentContext):false;
		}

		/**{@inheritDoc}*/ @Override
		ScopePrivate getActualParent() {
			return this.RawParent;
		}

		// StackOwner and Caller  -----------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		Object getStackOwner() {
			if(this.Owner == null) {
				if(this.RawParent instanceof Context) return ((Context)this.RawParent).getStackOwner();
				return this.getEngine().getDefaultStackOwner();
			}
			return this.Owner;
		}
		/**{@inheritDoc}*/ @Override
		Type getStackOwnerAsType() {
			if(this.Owner == null) {
				if(this.RawParent instanceof Context) return ((Context)this.RawParent).getStackOwnerAsType();
				StackOwner SO = this.getEngine().getDefaultStackOwner();
				if(SO instanceof Package) return null;
				if(SO instanceof    Type) return (Type)SO;
				return this.getEngine().getTypeManager().getTypeOf(SO);
			}
			// If the owner is not a type, see if the exec is a method and have a type, return type
			if(this.Exec  instanceof OperationInfo) return ((OperationInfo)this.Exec).getOwnerAsType();
			
			if (this.isVariableExist(StackOwnerAsType_VarName)) {
				Type T = (Type)this.getVariableValue(StackOwnerAsType_VarName);
				if (T != null) return T;
			}
			
			// Else return the type of the object
			return this.getEngine().getTypeManager().getTypeOf(this.Owner);
		}
		/**{@inheritDoc}*/ @Override
		Type getStackOwnerAsCurrentType() {
			if(this.Owner instanceof Type) return ((Type)this.Owner);
			// Else return the type of the object
			return this.getEngine().getTypeManager().getTypeOf(this.Owner);
		}
		/**{@inheritDoc}*/ @Override
		Package getStackOwnerAsPackage() {
			if(this.Owner == null) {
				if(this.RawParent instanceof Context) return ((Context)this.RawParent).getStackOwnerAsPackage();
				StackOwner SO = this.getEngine().getDefaultStackOwner();
				if(SO instanceof Package) return (Package)SO;
				if(SO instanceof    Type) return this.getEngine().getPackageOf((Type)SO);
				return this.getEngine().getPackageOf(SO);
			}
			// If the owner is a package, return the package
			if(  this.Owner instanceof Package)        return ((Package)this.Owner);
			if(!(this.Exec  instanceof OperationInfo)) return null;
			return this.getEngine().getPackageOf(((OperationInfo)this.Exec).getOwnerAsType());
		}
		
		/**{@inheritDoc}*/ @Override
		StackOwner getStackCaller() {
			Object Caller = (this.RawParent != null) ? this.RawParent.getStackOwner() : this.getStackOwner();
			return (Caller instanceof StackOwner)?(StackOwner)Caller:null;
		}
		/**{@inheritDoc}*/ @Override
		Type getStackCallerAsType() {
			return (this.RawParent != null) ? this.RawParent.getStackOwnerAsType() : this.getStackOwnerAsType();
		}
		/**{@inheritDoc}*/ @Override
		Package getStackCallerAsPackage() {
			return (this.RawParent != null) ? this.RawParent.getStackOwnerAsPackage() : this.getStackOwnerAsPackage();
		}

		/**{@inheritDoc}*/ @Override
		Object getDelegateSource() {
			if(this.RawParent != null) return this.RawParent.getDelegateSource();
			return null;
		}

		/** Checks if this stack is run during the construction of the owner. */
		/**{@inheritDoc}*/ @Override
		boolean isConstructor() { return false; }

		// RootContext  ---------------------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		ContextGlobal getRootContext() {
			return (this.RawParent != null) ? this.RawParent.getRootContext() : null;
		}
	}

	/** Executable Context that comes with Param and ability to hide the caller */
	static class ContextMacro extends ContextExecutable {

		// TODO - This is a hack
		static boolean IsFrozenVariableExist(Executable Exec, String FName) {
			if(Exec  == null) return false;
			if(FName == null) return false;
			for(int i = 0; i < Exec.getFrozenVariableCount(); i++)
				if(FName.equals(Exec.getFrozenVariableName(i)))
					return true;
			return false;
		}
		
		ContextMacro(Context pParent, Executable pInitiator, Executable pExec, Object[] pParams,
				boolean pIsCheckParam, boolean pIsBlindCaller) {
			this(pParent, pInitiator, null, pExec, pParams, pIsCheckParam, pIsBlindCaller);
		}

		ContextMacro(Context pParent, Executable pInitiator, Object pOwner, Executable pExec, Object[] pParams,
				boolean pIsCheckParam, boolean pIsBlindCaller) {
			super(pParent, (pInitiator == null) ? pExec : pInitiator, ((pExec == null) ? null : pExec.getSignature().getName()), pExec);

			// TODO - This is a hack, we should not let this happen but Pattern it to have this in effect
			Object SO;
			if(IsFrozenVariableExist(pExec, "this") && ((SO = this.getVariableValue("this")) != null)) 
				pOwner = SO;
			
			this.Params = pParams;
			this.Owner  = (pOwner != null) ? pOwner : pParent.getEngine().getDefaultStackOwner();
			
			this.adjustParent();
			
			// Temporarily set this back
			this.Parent = pParent;
			
			Engine  E  = this.getEngine();
			MType   MT = E.getTypeManager();

			Object  O  = this.getStackOwner();
			Type    T  = this.getStackOwnerAsType();
			Type    S  = this.getStackOwnerAsCurrentType();
			Package P  = this.getStackOwnerAsPackage();
			Type    TP = MT.getTypeOf(P);
				
			if(T == null) {
				this.newConstant(E, "this",                                  TKJava.TAny,  O, true);
				this.newConstant(E, Context.StackOwner_VarName,              TKJava.TAny,  O, true);
				this.newConstant(E, Context.StackOwnerAsType_VarName,        TKJava.TType, T, true);
				this.newConstant(E, Context.StackOwnerAsCurrentType_VarName, TKJava.TType, S, true);
				this.newConstant(E, Context.StackOwnerAsPackage_VarName,     TP,           P, true);
					
			} else {
				Type TO = MT.getTypeOf(O);
				Type TT = MT.getTypeOf(T);
				Type TS = MT.getTypeOf(S);
				this.newConstant(E, "this",                                  TO, O, true);
				this.newConstant(E, Context.StackOwner_VarName,              TO, O, true);
				this.newConstant(E, Context.StackOwnerAsType_VarName,        TT, T, true);
				this.newConstant(E, Context.StackOwnerAsCurrentType_VarName, TS, S, true);
				this.newConstant(E, Context.StackOwnerAsPackage_VarName,     TP, P, true);				
			}
			
			this.adjustParent();
		}

		final Object   Owner;
		final Object[] Params;
		final boolean  IsBlindCaller = false;
		

		// DELETE WHEN SURE
		// boolean IsCheckParam  = false;
		
		void adjustParent() {
			if((this.Exec != null) && this.Exec.getKind().isSubRoutine()) {
			   if(!(this.Exec instanceof Closure) || ((Closure)this.Exec).getWrappedKind().isSubRoutine())
				   this.Parent = null;
			}
		}

		// Context ---------------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		DataHolder getLocalDataHolder(String pDHName) {
			DataHolder DH = (this.DataHolders == null) ? null : this.DataHolders.get(pDHName);
			if(DH != null) return DH;
			
			DH = this.getParameterDataHolder_form_Executable(pDHName);
			if(DH != null) return DH;
			
			if(!this.Exec.isSubRoutine()) {
				MoreData MD = this.Exec.getSignature().getExtraData();
				if((MD != null) && Boolean.TRUE.equals(MD.getData(CompileProduct.MDName_IsLocal))) {
					ScopePrivate SP = this.getExecutableContext().getActualParent();
					if(SP instanceof Context) {
						Context theParent = (Context)SP;
						
						DH = theParent.getParameterDataHolder_form_Executable(pDHName);
						if(DH != null) return DH;
					}
				}
			}
			
			// Get from this context
			DH = super.getLocalDataHolder(pDHName);
			return DH;
		}
		
		@Override
		DataHolder getParameterDataHolder_form_Executable(String pDHName) {
			// Get from Params
			ExecInterface EI = this.Exec.getSignature();
			for (int p = EI.getParamCount(); --p >= 0;) {
				String PName = EI.getParamName(p);
				if (PName.equals(pDHName)) {
					Type   PType = EI.getParamTypeRef(p).getTheType();
					Object Param = this.Params[p];
					
					// The type is an array of it isVarArgs
					if(EI.isVarArgs() && (p == (EI.getParamCount() - 1)))
						PType = this.getEngine().getTypeManager().getAnnonymousArrayTypeOf(PType);

					// Add the param as a variable
					DataHolder DH = Variable.getFactory().newDataHolder(
										this, this.getEngine(), PType, Param, true, true, null, null);
					
					if (DH == null) {
						final String aMessage = String.format(
								"Executable-parameter dataholder-creation error: `%s:%s = '%s'`",
								PName, PType, Param);
						
						throw new CurryError(aMessage, this);
					}
					
					if (this.DataHolders == null)
						this.DataHolders = new Hashtable<String, DataHolder>();
					
					this.DataHolders.put(PName, DH);
					
					if (this.ScopeName != null)
						this.DataHolders.put(this.ScopeName + "." + PName, DH);

					return DH;
				}
			}
			return null;
		}

		// StackOwner and Caller  -----------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		Object getStackOwner() {
			if(this.Owner == null) {
				if(this.Parent instanceof Context) return ((Context)this.Parent).getStackOwner();
				return this.getEngine().getDefaultStackOwner();
			}
			return this.Owner;
		}
		/**{@inheritDoc}*/ @Override
		Type getStackOwnerAsType() {
			if(this.Owner == null) {
				if(this.Parent instanceof Context) return ((Context)this.Parent).getStackOwnerAsType();
				StackOwner SO = this.getEngine().getDefaultStackOwner();
				if(SO instanceof Package) return null;
				if(SO instanceof    Type) return (Type)SO;
				return this.getEngine().getTypeManager().getTypeOf(SO);
			}
			// If the owner is not a type, see if the exec is a method and have a type, return type
			if(this.Exec  instanceof OperationInfo)   return ((OperationInfo)  this.Exec).getOwnerAsType();
			if(this.Exec  instanceof ConstructorInfo){
				TypeRef TRef = ((ConstructorInfo)this.Exec).getOwnerAsTypeRef();
				return this.getEngine().getTypeManager().getTypeFromRefNoCheck(this, TRef);
			}
			// Else return the type of the object
			return this.getEngine().getTypeManager().getTypeOf(this.Owner);
		}
		/**{@inheritDoc}*/ @Override
		Type getStackOwnerAsCurrentType() {
			if(this.Owner instanceof Type) return ((Type)this.Owner);
			// Else return the type of the object
			return this.getEngine().getTypeManager().getTypeOfNoCheck(this, this.Owner);
		}
		/**{@inheritDoc}*/ @Override
		Package getStackOwnerAsPackage() {
			if(this.Owner == null) {
				if(this.Parent instanceof Context) return ((Context)this.Parent).getStackOwnerAsPackage();
				StackOwner SO = this.getEngine().getDefaultStackOwner();
				if(SO instanceof Package) return (Package)SO;
				if(SO instanceof    Type) return this.getEngine().getPackageOf((Type)SO);
				return this.getEngine().getPackageOf(SO);
			}
			// If the owner is a package, return the package
			if(  this.Owner instanceof Package)        return ((Package)this.Owner);
			if(!(this.Exec  instanceof OperationInfo)) return null;
			return this.getEngine().getPackageOf(((OperationInfo)this.Exec).getOwnerAsType());
		}
		
		/**{@inheritDoc}*/ @Override
		Expression getCurrentExpression() {
			if(this.CurrentExpression != null)
				return this.CurrentExpression;
			
			return null;
		}
	}

	/** Macro Context that block the above context and have a new StackOwner */
	static class ContextSubRoutine extends ContextMacro {

		ContextSubRoutine(Context pParent, Executable pInitiator, Object pOwner, Executable pExec, Object[] pParams,
				boolean pIsCheckParam, boolean pIsBlindCaller) {
			super(pParent, pInitiator, pOwner, pExec, pParams, pIsCheckParam, pIsBlindCaller);
			this.Parent    = null;
			this.RawParent = pParent;
		}

		final Context RawParent;

		/**{@inheritDoc}*/ @Override
		boolean isParent(Context pCurrentContext) {
			if(pCurrentContext == this) return  true;
			if(pCurrentContext == null) return false;
			return (this.RawParent instanceof Context)?((Context)this.RawParent).isParent(pCurrentContext):false;
		}

		/**{@inheritDoc}*/ @Override
		ScopePrivate getActualParent() {
			return this.RawParent;
		}
		
		/**{@inheritDoc}*/ @Override
		StackOwner getStackCaller() {
			Object Caller = (this.RawParent != null) ? this.RawParent.getStackOwner() : this.getStackOwner();
			return (Caller instanceof StackOwner)?(StackOwner)Caller:null;
		}
		/**{@inheritDoc}*/ @Override
		Type getStackCallerAsType() {
			return (this.RawParent != null) ? this.RawParent.getStackOwnerAsType() : this.getStackOwnerAsType();
		}
		/**{@inheritDoc}*/ @Override
		Package getStackCallerAsPackage() {
			return (this.RawParent != null) ? this.RawParent.getStackOwnerAsPackage() : this.getStackOwnerAsPackage();
		}

		/**{@inheritDoc}*/ @Override
		Object getDelegateSource() {
			if(this.RawParent instanceof ContextDelegate) return this.RawParent.getDelegateSource();
			return null;
		}
		/**{@inheritDoc}*/ @Override
		Documentation getCurrentDocumentation() {
			if(this.CurrentDocumentation != null) return this.CurrentDocumentation;
			return Documentation.Util.getDocumentationOf(this.Exec);
		}

		// RootContext  ---------------------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		ContextGlobal getRootContext() {
			return (this.RawParent != null) ? this.RawParent.getRootContext() : null;
		}

		/**{@inheritDoc}*/ @Override
		Expression getCurrentExpression() {
			if(this.CurrentExpression != null)
				return this.CurrentExpression;
			
			return null;
		}
	}
	
	/** Stack context that has executable that own it and so the identity and location */
	static class ContextStackOwner extends Context {

		/** Constructs a context */
		ContextStackOwner(Context pParent, String pName, boolean pIsAttributeAccess, Executable pInitiator, Object pOwner,
			Location pLocation) {
			
			this(pParent, pName, pIsAttributeAccess, pInitiator,
					(pOwner != null) ? pOwner : pParent.getEngine().getDefaultStackOwner(),
					null, false, pLocation);
		}

		/** Constructs a context */
		ContextStackOwner(Context pParent, String pName, boolean pIsAttributeAccess, Executable pInitiator, Object pOwner,
			Type pAsType, Location pLocation) {
			
			this(pParent, pName, pIsAttributeAccess, pInitiator, pOwner, pAsType, false, pLocation);
		}

		/** Constructs a context */
		ContextStackOwner(Context pParent, String pName, boolean pIsAttributeAccess, Executable pInitiator, Object pOwner,
				Type pAsType, boolean pIsInitializer, Location pLocation) {
			
			super(pParent, pName, pInitiator);
			this.Owner             = (pOwner != null) ? pOwner : pParent.getEngine().getDefaultStackOwner();
			this.Location          = pLocation;
			this.AsType            = pAsType;
			this.IsInitializer     = pIsInitializer;
			this.IsAttributeAccess = pIsAttributeAccess;
			
			this.RawParent = pParent;
			if(this.RawParent != null) {	// Hijack the properties
				this.Parent      = this.RawParent.Parent;
				this.DataHolders = this.RawParent.DataHolders;
				this.PDHs_Cahce  = this.RawParent.PDHs_Cahce;
			}
		}

		final Object   Owner;
		final Type     AsType;
		final Context  RawParent;
		final boolean  IsInitializer;
		final boolean  IsAttributeAccess;
		final Location Location;

		/**{@inheritDoc}*/ @Override
		boolean isParent(Context pCurrentContext) {
			if(pCurrentContext == this) return  true;
			if(pCurrentContext == null) return false;
			return (this.RawParent instanceof Context)?((Context)this.RawParent).isParent(pCurrentContext):false;
		}

		/**{@inheritDoc}*/ @Override
		ScopePrivate getActualParent() {
			return this.RawParent;
		}

		// StackOwner and Caller -------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		Object getStackOwner() {
			if(this.Owner == null) {
				if(this.RawParent instanceof Context) return ((Context)this.RawParent).getStackOwner();
				return this.getEngine().getDefaultStackOwner();
			}
			return this.Owner;
		}

		/**{@inheritDoc}*/ @Override
		Type getStackOwnerAsType() {
			if(this.Owner == null) {
				if(this.RawParent instanceof Context) return ((Context)this.RawParent).getStackOwnerAsType();
				StackOwner SO = this.getEngine().getDefaultStackOwner();
				if(SO instanceof Package) return null;
				if(SO instanceof    Type) return (Type)SO;
				return this.getEngine().getTypeManager().getTypeOf(SO);
			}
			// If AsType is not null, return it as the type
			if(this.AsType != null)           return this.AsType;
			// If the owner is a type, return the type
			if(this.Owner instanceof Type)    return ((Type)this.Owner);
			// Else return the type of the object
			return this.getEngine().getTypeManager().getTypeOf(this.Owner);
		}
		/**{@inheritDoc}*/ @Override
		Package getStackOwnerAsPackage() {
			if(this.Owner == null) {
				if(this.RawParent instanceof Context) return ((Context)this.RawParent).getStackOwnerAsPackage();
				StackOwner SO = this.getEngine().getDefaultStackOwner();
				if(SO instanceof Package) return (Package)SO;
				if(SO instanceof    Type) return this.getEngine().getPackageOf((Type)SO);
				return this.getEngine().getPackageOf(SO);
			}
			// If the owner is a package, return the package
			if(  this.Owner instanceof Package) return ((Package)this.Owner);
			return this.getEngine().getPackageOf(this.Owner);
		}
		
		/**{@inheritDoc}*/ @Override
		StackOwner getStackCaller() {
			Object Caller = (this.RawParent != null) ? this.RawParent.getStackOwner() : this.getStackOwner();
			return (Caller instanceof StackOwner)?(StackOwner)Caller:null;
		}
		/**{@inheritDoc}*/ @Override
		Type getStackCallerAsType() {
			return (this.RawParent != null) ? this.RawParent.getStackOwnerAsType() : this.getStackOwnerAsType();
		}
		/**{@inheritDoc}*/ @Override
		Package getStackCallerAsPackage() {
			return (this.RawParent != null) ? this.RawParent.getStackOwnerAsPackage() : this.getStackOwnerAsPackage();
		}

		/**{@inheritDoc}*/ @Override
		Object getDelegateSource() {
			if(this.RawParent instanceof ContextDelegate) return this.RawParent.getDelegateSource();
			return null;
		}

		/**{@inheritDoc}*/ @Override
		boolean isConstructor() {
			return this.IsInitializer;
		}

		// RootContext  ---------------------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		ContextGlobal getRootContext() {
			return (this.RawParent != null) ? this.RawParent.getRootContext() : null;
		}

		// Location  ------------------------------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		Location getStackLocation() {
			return this.Location;
		}
		/**{@inheritDoc}*/ @Override
		int getCurrentCoordinate() {
			if(this.CurrentExpression != null)
				return this.CurrentCoordinate;

			this.CurrentExpression = this.getCurrentExpression();
			
			if(this.CurrentExpression != null)
				return this.CurrentCoordinate;
			
			Location L = this.getStackLocation();
			if(L != null) return L.getCoordinate();

			return -1;
		}
		/**{@inheritDoc}*/ @Override
		Expression getCurrentExpression() {
			if(this.CurrentExpression != null)
				return this.CurrentExpression;

			// If case that the executable is an expression (no location), the location is of the parent
			if(this.RawParent instanceof Context) {
				this.CurrentExpression = ((Context)this.RawParent).getCurrentExpression();
				return this.CurrentExpression;
			}
			
			return null;
		}
		/**{@inheritDoc}*/ @Override
		Documentation getCurrentDocumentation() {
			if(this.CurrentDocumentation != null)
				return this.CurrentDocumentation;

			// If case that the executable is an expression (no location), the location is of the parent
			if(this.RawParent instanceof Context) {
				this.CurrentDocumentation = ((Context)this.RawParent).getCurrentDocumentation();
				return this.CurrentDocumentation;
			}
			
			return Documentation.Util.getDocumentationOf(this.getExecutable());
		}

		/**{@inheritDoc}*/ @Override
		ActionRecord getInitializerCaller() {
			ContextStackOwner CSO = this;
			while(CSO.IsInitializer) {
				ScopePrivate P = CSO.Parent;
				if(!(P instanceof Context)) return null;
				
				if(!(P instanceof ContextStackOwner))
					break;
				
				CSO = (ContextStackOwner)P; 
			}

			ScopePrivate P = CSO.Parent;
			Object       O = (P instanceof Context) ? ((Context)P).getStackOwner() : this.getStackCaller();
			return new ActionRecord(O, CSO.getCurrentLocationSnapshot());
		}
	}
	
	/** Stack context for delegation */
	static class ContextDelegate extends ContextStackOwner {

		/** Constructs a context */
		ContextDelegate(Context pParent, String pName, Executable pInitiator, Object pOwner, Location pLocation) {
			super(pParent, pName, false, pInitiator, pOwner, pLocation);
		}

		/** Constructs a context */
		ContextDelegate(Context pParent, String pName, Executable pInitiator, Object pOwner, Type pAsType,
				Location pLocation) {
			super(pParent, pName, false, pInitiator, pOwner, pAsType, pLocation);
		}

		/**{@inheritDoc}*/ @Override
		Object getDelegateSource() {
			return this.getStackOwner();
		}
	}
}