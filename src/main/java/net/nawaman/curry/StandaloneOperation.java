package net.nawaman.curry;

import net.nawaman.curry.StackOwner.*;
import net.nawaman.curry.util.*;
import net.nawaman.util.UObject;

final public class StandaloneOperation extends JavaExecutable.JavaSubRoutine_Complex {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	static public final String DefaultOperationName = "Operation";
	
	StackOwner Owner = null;
	Object     Body;
	
	static private class OperationAccess {
		OperationSearchKind OSKind;
		Object   Param1;
		Object   Param2;
		OperationAccess(OperationSearchKind pOSKind, Object pParam1, Object pParam2) {
			this.OSKind = pOSKind; this.Param1 = pParam1; this.Param2 = pParam2;
		}
	}
	static private class AttributeAccess {
		DataHolder.AccessKind DHAKind;
		String AName;
		Type   AsType;
		AttributeAccess(DataHolder.AccessKind pDHAKind, Type pAsType, String pAName) {
			this.DHAKind = pDHAKind; this.AsType = pAsType; this.AName = pAName;
		}
	}
	
	/** Constructs a closure of a StackOwner */
	StandaloneOperation(Engine pEngine, StackOwner pOwner, String pName, Object pBody) {
		super(pEngine,
		      (pBody instanceof Executable)
				?((Executable)pBody).getSignature()
				:ExecSignature.newProcedureSignature(
					(pName == null)?DefaultOperationName:pName, pEngine.getTypeManager().getTypeOf(pBody).getTypeRef(),
					null, null),
		      null, null);
		if(pOwner  == null) throw new NullPointerException();
		this.Body  = pBody;
		this.Owner = pOwner;
		
		if((this.Body instanceof Executable) && isThereClosure((Executable)this.Body))
			throw new IllegalArgumentException("A closure cannot wrapped by a nother closure.");

	}
	/** Constructs a closure of a StackOwner */
	StandaloneOperation(Engine pEngine, StackOwner pOwner, Executable pBody) {
		this(pEngine, pOwner, null, pBody);
	}
	
	/** Creates a temporary default Closure */
	private StandaloneOperation() {
		super(null, ExecSignature.newEmptySignature("Temp", null, null), null, null);
	}
	
	// Public constructors -------------------------------------------------------------------------
	
	/** Constructs a closure of a StackOwner */
	static public StandaloneOperation newClosure(Engine pEngine, StackOwner pOwner, ExecSignature pSignature) {
		if(pOwner     == null) throw new NullPointerException();
		if(pSignature == null) throw new NullPointerException();

		ExecSignature ES = pOwner.searchOperation(pEngine, OperationSearchKind.BySignature, false, pSignature, null,
				null);
		if(ES == null) throw new NullPointerException();
		
		StandaloneOperation C = new StandaloneOperation();
		C.Owner     = pOwner; 
		C.Signature = pSignature;
		C.Body      = new OperationAccess(OperationSearchKind.Direct, pSignature, null);
		return C;
	}
	/** Constructs a closure of a StackOwner */
	static public StandaloneOperation newClosure(Engine pEngine, StackOwner pOwner, String pName, TypeRef[] pPTRefs) {
		if(pOwner == null) throw new NullPointerException();
		if(pName  == null) throw new NullPointerException();

		ExecSignature ES = pOwner.searchOperation(pEngine, OperationSearchKind.ByTRefs, false, pName, pPTRefs, null);
		if(ES == null) throw new NullPointerException();
		
		StandaloneOperation C   = new StandaloneOperation();
		C.Owner     = pOwner;
		C.Signature = ES;
		C.Body      = new OperationAccess(OperationSearchKind.Direct, ES, null);
		return C;
	}
	/** Constructs a closure of a StackOwner */
	static public StandaloneOperation newClosure(Engine pEngine, StackOwner pOwner, String pName, ExecInterface pEI) {
		if(pOwner == null) throw new NullPointerException();
		if(pName  == null) throw new NullPointerException();

		ExecSignature ES = pOwner.searchOperation(pEngine, OperationSearchKind.ByNameInterface, false, pName, pEI, null);
		if(ES == null) throw new NullPointerException();
		
		StandaloneOperation C   = new StandaloneOperation();
		C.Owner     = pOwner;
		C.Signature = ES;
		C.Body      = new OperationAccess(OperationSearchKind.Direct, ES, null);
		return C;
	}
	
	/** Constructs a closure of a StackOwner */
	static public StandaloneOperation newClosure(Engine pEngine, StackOwner pOwner, String pName, String pAttrName,
			DataHolder.AccessKind pDHAK) {
		if(pOwner    == null) throw new NullPointerException();
		if(pName     == null) throw new NullPointerException();
		if(pAttrName == null) throw new NullPointerException();
		if(pDHAK     == null) throw new NullPointerException();

		TypeRef TR = pOwner.searchAttribute(pEngine, true, null, pAttrName);
		if(TR == null)
			throw new IllegalArgumentException("The attribute "+UObject.toString(pOwner)+"."+pAttrName+" does not exist.");
		
		ExecSignature ES = null;
		switch(pDHAK) {
			case Set:
				ES = ExecSignature.newSignature(pName,
						new TypeRef[] { TKJava.TAny.getTypeRef() },
						new String[]  { ExecInterface.AutoParamNamePrifix + "0" },
						false, TKJava.TBoolean.getTypeRef(), null, null);
				break;
			case Config: // String pName, Object[] pParams
				String APNP = ExecInterface.AutoParamNamePrifix;
				ES = ExecSignature.newSignature(pName,
						new TypeRef[] { TKJava.TString.getTypeRef(), TKArray.AnyArrayRef },
						new String[]  { APNP + "0",                  APNP + "1"          },
						false, TKJava.TAny.getTypeRef(), null, null);
				break;
			case Get:
				ES = ExecSignature.newProcedureSignature(pName, TKJava.TAny.getTypeRef(), null, null);
				break;
			case GetType:
				ES = ExecSignature.newProcedureSignature(pName, TKJava.TType.getTypeRef(), null, null);
				break;
			case IsReadable:
			case IsWritable:
			case IsNotTypeCheck:
				ES = ExecSignature.newProcedureSignature(pName, TKJava.TBoolean.getTypeRef(), null, null);
				break;
			case GetMoreInfo:
				ES = ExecSignature.newProcedureSignature(pName, TKJava.TAny.getTypeRef(), null, null);
				break;
			case Clone:
			default:
				throw new IllegalArgumentException("Invalid data-holder access kind for accessing the attribute "
						+ UObject.toString(pOwner) + "." + pAttrName + ".");
		}
		
		StandaloneOperation C   = new StandaloneOperation();
		C.Owner     = pOwner;
		C.Signature = ES;
		C.Body      = new AttributeAccess(pDHAK, null, pAttrName);
		return C;
	}
	
	/** Checks if sub-routine is a closure (navigate deep if it is a wrapper) */
	static public boolean isThereClosure(Executable E) {
		if(!(E instanceof Executable.SubRoutine)) return false;
		Executable.SubRoutine SR = (Executable.SubRoutine)E;
		// Ensure SubR has nothing hidden (not a closure)
		while((SR instanceof WrapperExecutable.Wrapper)
				&& (((WrapperExecutable.Wrapper)SR).getWrapped() instanceof Executable.SubRoutine)) {
			SR = (Executable.SubRoutine)((WrapperExecutable.Wrapper)SR).getWrapped();
		}
		if(SR instanceof StandaloneOperation) return true;
		return false;
	}
	
	// Services ------------------------------------------------------------------------------------
	
	/** Checks if this StandAlone executable does not need a parameter */
	public boolean isProcedure() {
		if(this.getSignature() == null)              return true;
		if(this.getSignature().getParamCount() == 0) return true;
		return false;
	}
	// Objectable --------------------------------------------------------------
	/** Returns the hash value of the body - used for calculating has of the executable */
	@Override protected int    getBodyHash()              { return UObject.hash(this.Body); }
	/** Returns the String value of the body - used for toString/toDetail of the executable */
	@Override protected String getBodyStr(Engine pEngine) { return "..."; }
	
	// Executing ---------------------------------------------------------------
	/** Executing this -  For internal to change */
	@Override protected Object run(Context pContext, Object[] pParams) {
		if(!(this.Body instanceof Executable)) {
			if(this.Body instanceof OperationAccess) { 
				OperationAccess OA = (OperationAccess)this.Body;
				return this.Owner.invokeOperation(pContext, null, false, OA.OSKind, null, OA.Param1, OA.Param2, pParams, false);
				
			} else if(this.Body instanceof AttributeAccess) {
				AttributeAccess AA = (AttributeAccess)this.Body;
				Object P1 = ((pParams == null) || (pParams.length < 1))?null:pParams[0];
				Object P2 = ((pParams == null) || (pParams.length < 2))?null:pParams[1];
				switch(AA.DHAKind) {
					case Config: {
						if((pParams == null) || (pParams.length != 2)) throw new IllegalArgumentException();
						break;
					}
					case Set:
					case GetMoreInfo: {
						if((pParams == null) || (pParams.length != 1)) throw new IllegalArgumentException();
						break;
					}
					case Get:
					case GetType:
					case IsReadable:
					case IsWritable:
					case IsNotTypeCheck: {
						if((pParams != null) && (pParams.length != 0)) throw new IllegalArgumentException();
						break;
					}
					case Clone: throw new IllegalArgumentException();
				}
				return this.Owner.accessAttribute(pContext, null, AA.DHAKind, AA.AsType, AA.AName, P1, P2, null);
			}
			return this.Body;
		}
		Executable Exec = ((Executable)this.Body);
		Location   Loc  = Exec.getSignature().getLocation();
		pContext = new Context.ContextStackOwner(pContext, DefaultOperationName, false, Exec, this.Owner, Loc);
		return pContext.getExecutor().execExecutable(pContext, null, Exec, Executable.ExecKind.SubRoutine, true,
				this.Owner, pParams, false, true);
	}

}
