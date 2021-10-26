package net.nawaman.curry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.HashSet;
import net.nawaman.curry.AttributeInfo.AIDirect;
import net.nawaman.curry.AttributeInfo.AIDlgAttr;
import net.nawaman.curry.AttributeInfo.AIDlgObject;
import net.nawaman.curry.AttributeInfo.AIDynamic;
import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.OperationInfo.OIDirect;
import net.nawaman.curry.OperationInfo.OIDlgAttr;
import net.nawaman.curry.OperationInfo.OIDlgObject;
import net.nawaman.curry.OperationInfo.OIDynamic;
import net.nawaman.curry.OperationInfo.SimpleOperation;
import net.nawaman.curry.TKJava.TJava;
import net.nawaman.curry.util.DataHolder;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.curry.util.MoreData;
import net.nawaman.curry.util.UCurry;
import net.nawaman.util.*;

/** Owner of stacks */
abstract public class StackOwner implements Objectable {
	
	// Constructor -----------------------------------------------------------------------------------------------------
	
	protected StackOwner() {}
	
	// ---------------------------------------------------------------------------------------------
	// General Behaviors ---------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	/** Returns the engine that this StackOwner is running on */
	abstract protected Engine getEngine();
	
	transient protected StackOwnerInfo SOInfo = null;

	// Make this object immutable
	static class ImmutableStackOwnerInfo extends StackOwnerInfo {
		ImmutableStackOwnerInfo(StackOwner pSO) { super(pSO);  }
		public @Override boolean isImmutable()  { return true; }
	}
	
	/** Get SOInfo which provide information about this object */
	public StackOwnerInfo getSOInfo() {
		if(this.SOInfo == null) this.SOInfo = new ImmutableStackOwnerInfo(this);
		return this.SOInfo;
	}

	// Customize of StackOwner Kind ----------------------------------------------------------------	
	
	/** Returns the name of the operation kind */
	protected String getOperKindName() {
		return "operation";
	}
	/** Returns the name of the attribute kind */
	protected String getAttrKindName() {
		return "attribute";
	}

	// Accessibility -------------------------------------------------------------------------------
	
	/** Validate the given accessibility and throw an error if the validation fail. */
	protected void validateAccessibility(Accessibility pAccess) {
		return;
	}
	
	/** Checks if the given value is consider a private (that is allowed access a private members) */
	protected boolean canAccessPrivate(StackOwner pAccessor) {
		return this == pAccessor;
	}
	
	/** Checks if the operation is allow to be executed **/
	protected boolean isOperAllowed(Context pContext, OperationInfo pOperInfo) {
		Accessibility Access = pOperInfo.getAccessibility();
		if(Access == null) return true;
		return Access.isAllowed(pContext, this, pOperInfo);
	}
	/** Checks if the attribute is allow to be accessed **/
	protected boolean isAttrAllowed(Context pContext, AttributeInfo pAttrInfo, DataHolder.AccessKind pDHAK) {
		Accessibility Access;
		// Select an appropriate accessibility
		if(     pDHAK == DataHolder.AccessKind.Set)    Access = pAttrInfo.getWriteAccessibility();
		else if(pDHAK == DataHolder.AccessKind.Config) Access = pAttrInfo.getConfigAccessibility();
		else                                           Access = pAttrInfo.getReadAccessibility();
		// Check
		if(Access == null) return true;
		return Access.isAllowed(pContext, this, pAttrInfo);
	}

	// Objectable ----------------------------------------------------------------------------------
	
	/** Returns the short string representation of the object. */ @Override
	public String toString() {
		return super.toString();
	}
	/** Returns the long string representation of the object. */ @Override
	public String toDetail() {
		return this.toString();
	}
	/** Checks if O is the same or consider to be the same object with this object. */ @Override
	public boolean is(Object O) {
		return this == O;
	}
	/** Checks if O equals to this object. */ @Override
	public int hash() {
		return this.hashCode();
	}
	
	// ---------------------------------------------------------------------------------------------
	// General A/O related -------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	// Data ----------------------------------------------------------------------------------------

	/** Returns the DataHolder with the AttributeInfo */
	abstract protected DataHolder getDHAt(Context pContext, DataHolder.AccessKind DHAK, AIDirect AI);
		
	// Not Null Attribute --------------------------------------------------------------------------
	
	/** Checks if the 'NotNull' features is now on */
	protected boolean isEnforceNotNull() { return false; }
	
	/** Start Enforce and check if all the attributes with 'isNotNull' is actually not null. */
	protected boolean toEnforceNotNull(Context pContext) { return true; }

	// Dynamic Handling ----------------------------------------------------------------------------
	
	/** Check if dynamic handling is allowed */
	protected boolean isHandleDynamically() { return false; }
	
	/** Do the delegate the operation and attribute access */
	final protected Object doDynamicHandling(Context pContext, Expression pInitiator, boolean pIsBlindCaller,
			Object[] pParams) {
		return this.invokeOper(pContext, pInitiator, pIsBlindCaller, null, StackOwnerInfo.getDynamicHandleSignature(), pParams, true);
	}

	/**
	 * Delegate the operation and attribute access
	 * For Operation, the parameters -> Signature, Parameters
	 * For Attribute, the parameters -> TypeRef, AccessKind, AttrName, Param1, Param2
	 */
	protected Object dynamicHandling(Context pContext, Expression pInitiator, boolean pIsBlindCaller, Object[] pParams) {
		return this.doDynamicHandling(pContext, pInitiator, pIsBlindCaller, pParams);
	}
	
	// Dynamic Delegation --------------------------------------------------------------------------

	/** Returns the number of Dynamic Delegation */
	protected int getDynamicDelegationCount(Context pContext) {
		return 0;
	}
	/** Returns the name of the Attribute for the delegation */
	protected String  getDynamicDelegation(Context pContext, int I) {
		return null;
	}
	/** Returns the type that this StackOwner need to be seen as to get the Delegation Object */
	protected TypeRef getDynamicDelegationAsType(Context pContext, int I) {
		return null;
	}

	// ---------------------------------------------------------------------------------------------
	// Handle A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Get Respond ---------------------------------------------------------------------------------
	
	// Get A/O Local ---------------------------------------------------------------------
	// NOTE: Low level
	// NOTE: pContext must not be null here
	
	/** Get a respond to the operation request that is associated with pSignature (pSignature must be exact match). */
	abstract protected OperationInfo getOperationLocal(Context pContext, Type pAsType, ExecSignature pSignature);
	/** Get a respond to the attribute request that is associated with pName. **/
	abstract protected AttributeInfo getAttributeLocal(Context pContext, DataHolder.AccessKind pDHAK, Type pAsType,
			String pName);
	
	/** Returns an array of all the non-dynamic operation info */
	abstract protected OperationInfo[] getAllNonDynamicOperationInfo(Type pAsType);
	/** Returns an array of all the non-dynamic attribute info */
	abstract protected AttributeInfo[] getAllNonDynamicAttributeInfo(Type pAsType);

	// Get A/O ---------------------------------------------------------------------------
	// NOTE: Middle level
	// NOTE: pContext must not be null here
	// Look locally then look in the Dynamic Delegate
	
	/** Get a respond to the operation request that is associated with pSignature (pSignature must be exact match). */
	final protected OperationInfo getOperation(Context pContext, Expression pInitiator, Type pAsType,
			ExecSignature pSignature) {
		return this.getOperation(pContext, pInitiator, pAsType, pSignature, null);
	}
	
	/** Get a respond to the operation request that is associated with pSignature (pSignature must be exact match). */
	final protected OperationInfo getOperation(Context pContext, Expression pInitiator, Type pAsType,
			ExecSignature pSignature, HashSet<Object> pObjects) {
		
		OperationInfo OI = this.getOperationLocal(pContext, pAsType, pSignature);
		// TODO - May check the abstract here
		if(OI != null) return OI;
		
		try {
			// Remember what object has been searched (delegate to search there)
			if((pObjects != null) && (pObjects.contains(this))) return null;
			if(pObjects == null) pObjects = new HashSet<Object>();
			pObjects.add(this);
			
			// Create default context
			if((pContext == null) && (this.getEngine() != null)) pContext = this.getEngine().newRootContext();
			
			// Change the Context to be owned by this so that the getAttrData() and getOperation is done from this object
			pContext = new Context.ContextDelegate(pContext, null, pInitiator, this, null);
			
			// Find it
			int DDCount = this.getDynamicDelegationCount(pContext);
			for(int i = 0; i < DDCount; i++) {
				// Get Name
				String AName  = this.getDynamicDelegation(pContext, i);
				if(AName == null) continue;
				
				// Get AsType
				TypeRef AsTypeRef = this.getDynamicDelegationAsType(pContext, i);
				Type    AsType    = null;
				if(AsTypeRef != null) {
					pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, AsTypeRef);
					AsType = AsTypeRef.getTheType();
				}
				
				// If O is not 
				Object O = this.getAttrData(pContext, null, AsType, AName, pObjects);
				if(!(O instanceof StackOwner)) {
					if(O == null) continue;
					Type OType = pContext.getEngine().getTypeManager().getTypeOf(O);
					// There is no need to put pObject here because non-DObject have delegated
					OI = OType.doData_getOperation(null, pContext, pInitiator, null, pSignature);
				} else {
					OI = ((StackOwner)O).getOperation(pContext, null, null, pSignature, pObjects);
				}
				// Not found or no not accessible
				if((OI == null) || (OI.getRKind().isNoPermission())) continue;
				// Delegate to work with the object
				return new OperationInfo.OIDlgObject(OI.getAccessibility(), pSignature, O, null);
			}
			return null;
		} finally { if(pObjects != null) pObjects.remove(this); }
	}

	// NOTE: When look in DynamicDelegation, AsType will be Ignored
	/** Get a respond to the attribute request that is associated with pName. * */
	final protected AttributeInfo getAttribute(Context pContext, Expression pInitiator, DataHolder.AccessKind pDHAK,
			Type pAsType, String pName) {
		return this.getAttribute(pContext, pInitiator, pDHAK, pAsType, pName, null);
	}

	// NOTE: When look in DynamicDelegation, AsType will be Ignored
	/** Get a respond to the attribute request that is associated with pName. */
	final protected AttributeInfo getAttribute(Context pContext, Expression pInitiator, DataHolder.AccessKind pDHAK,
			Type pAsType, String pName, HashSet<Object> pObjects) {
		
		AttributeInfo AI = this.getAttributeLocal(pContext, pDHAK, pAsType, pName);
		// May check the abstract here
		if(AI != null) return AI;
	
		try {
			// Remember what object has been searched (delegate to search there)
			if((pObjects != null) && (pObjects.contains(this))) return null;
			if(pObjects == null) pObjects = new HashSet<Object>();
			pObjects.add(this);
			
			// Change the Context to be owned by this so that the getAttrData() and getOperation is done from this object
			pContext = new Context.ContextDelegate(pContext, null, pInitiator, this, null, null);
			
			int DDCount = this.getDynamicDelegationCount(pContext);
			for(int i = 0; i < DDCount; i++) {
				String AName  = this.getDynamicDelegation(pContext, i);
				if(AName == null) continue;	// Just in case
	
				// Get AsType
				TypeRef AsTypeRef = this.getDynamicDelegationAsType(pContext, i);
				Type    AsType    = null;
				if(AsTypeRef != null) {
					pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, AsTypeRef);
					AsType = AsTypeRef.getTheType();
				}
				
				// If O is not 
				Object O = this.getAttrData(pContext, null, AsType, AName, pObjects);
				if(!(O instanceof StackOwner)) {
					if(O == null) continue;
					Type OType = pContext.getEngine().getTypeManager().getTypeOf(O);
					AI = OType.doData_getAttribute(null, pContext, pInitiator, pDHAK, null, pName);
				} else {
					AI = ((StackOwner)O).getAttribute(pContext, null, pDHAK, null, pName, pObjects);
				}
				// Not found or no not accessible
				if((AI == null) || (AI.getRKind().isNoPermission())) continue;
				// Delegate to work with the object
				AIDlgObject NewAI = new AttributeInfo.AIDlgObject(
						AI.getReadAccessibility(), AI.getWriteAccessibility(), AI.getConfigAccessibility(),pName, O, null);
				
				NewAI.resolve(pContext.getEngine());
				return NewAI;
			}
			return AI;
		} finally { if(pObjects != null) pObjects.remove(this); }
	}
	
	// Handle A/O ----------------------------------------------------------------------------------
	// NOTE: Low level
	// NOTE: pContext must not be null here
	
	/** Execute an operation of this by 'pContext.getStackOwner()' */
	final Object invokeOper(Context pContext, Expression pInitiator, boolean pIsBlindCaller,
			Type pAsType, ExecSignature pSignature, Object[] pParams, boolean pIsAlreadyAdjusted) {
		// Get the respond
		OperationInfo OI = this.getOperation(pContext, pInitiator, pAsType, pSignature, null);

		// Null respond - the operation is not found
		if(OI == null) {
			// See what else we do
			Object Result = this.doOperationRespondNotFound(pContext, pInitiator, pIsBlindCaller, pAsType, pSignature,
								pParams, pIsAlreadyAdjusted);
			
			// See if this is a return result or a replace OperationInfo
			if(!(Result instanceof OperationInfo)) {
				// Check compatibility of the result against the signature type
				pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, pSignature.getReturnTypeRef());
				if(pSignature.getReturnTypeRef().getTheType().canBeAssignedBy(Result)) return Result;
				// No! It's not compatible
				this.throwOperation("Operation Invocation Error: `"
						+ pContext.getEngine().toString(pContext, Result)
						+ "` is not a valid return value of " + this.toString() + "."
						+ pSignature.toString() + ".", pContext, pAsType, pSignature);
				return null;
			}
			OI = (OperationInfo)Result;
		}

		// Allow to reconsider after Invalid Respond was found
		while(!OI.getRKind().isDirect()) {

			// Insufficient permission
			if(OI.getRKind().isNoPermission()) {
				this.throwOperation("Operation Invocation Error: "
						+ "Insufficient permission to access the "
						+ this.getOperKindName() + " (" + this.toString() + "."
						+ pSignature.toString() + ").", pContext, pAsType, pSignature);
				return null;
			}

			// Handle dynamically
			if(OI.getRKind().isDynamicHandle()) {
				// Change the Context to be owned by this with the type that owns the OI so that
				//    the getAttrData() and getOperation is done from this object
				Context ThisContext = new Context.ContextStackOwner(pContext, pSignature.getName(), false, pInitiator,
						this, OI.getOwnerAsType(), null);
				
				// Make sure that dynamic handling is supported in this StackOwner.
				if(this.isHandleDynamically()) {
					// Search in this StackOwner for the operation with the signature
					// `Respond.DynamicHandleSignature`
					Object Result = this.dynamicHandling(ThisContext, pInitiator, pIsBlindCaller, new Object[] { pSignature, pParams });
					
					if(!(Result instanceof SpecialResult)) {
						this.getEngine().getTypeManager().ensureTypeInitialized(ThisContext, pSignature.getReturnTypeRef());
						Type RT = pSignature.getReturnTypeRef().getTheType();
						if(!RT.canBeAssignedBy(ThisContext, Result)) {
							this.throwOperation(
									"Executable Error: Invalid return type ('"+
									this.getEngine().toString(ThisContext, Result)+"')",
									ThisContext, pAsType, pSignature);
						}
					}
					return Result;
				} else {
					// See what else we can do
					Object Result = this.doDynamicDelegationNotSupported(ThisContext, pInitiator,
										pIsBlindCaller, pAsType, pSignature, pParams,
										pIsAlreadyAdjusted);
					
					// See if this is a return result or a replace OperationInfo
					if(!(Result instanceof OperationInfo)) {
						// Check compatibility of the result against the signature type
						TypeRef RTRef = pSignature.getReturnTypeRef();
						pContext.getEngine().getTypeManager().ensureTypeInitialized(ThisContext, RTRef);
						if(RTRef.getTheType().canBeAssignedBy(Result)) return Result;
						
						// No! It's not compatible
						this.throwOperation("Operaion Invocation Error: `"
								+ ThisContext.getEngine().toString(ThisContext, Result)
								+ "` is not a valid return value of " + this.toString() + "."
								+ pSignature.toString() + ".", ThisContext, pAsType, pSignature);
						return null;
					}
					// If it is still the same, throw an error
					if(OI == Result) {
						this.throwDynamicDelegationNotSupported(ThisContext, pAsType, pSignature);
						return null;
					}
					// Re consider the respond
					OI = (OperationInfo)Result;
					// Continue to reconsider it
					continue;
				}
			}

			// Delegate the respond to Target
			// Handle Native
			if(OI.getRKind().isNative()) {
				Method M = ((OperationInfo.OINative)OI).getMethod();
				try {
					if(UClass.isMemberStatic(M))
						return UClass.invokeMethod(M, null, pParams);
					else {
						// Change to Native if need and can
						Object   O = this;
						Class<?> C = M.getDeclaringClass();
						if(!C.isInstance(O) && (O instanceof DObjectStandalone)) {
							if(C.isAssignableFrom(DObject.class)) O = ((DObjectStandalone)O).getAsDObject();
							else                                  O = ((DObjectStandalone)O).getAsNative();
						}
							
						return UClass.invokeMethod(M, O, pParams);
					}
				} catch(Throwable T) {
					throw new CurryError("Operaion Invocation Error: "
							+ "An error occurs while invoking the java method `"
							+ M.toString() + "`.", pContext, T);
				}
			}

			// Change the Context to be owned by this with the type that owns the OI so that
			//    the getAttrData() and getOperation is done from this object
			Context ThisContext = new Context.ContextDelegate(pContext, pSignature.getName(), pInitiator, this, OI.getOwnerAsType(), null);

			Object Target = null;
			// Get the target as This
			if(OI.getRKind().isDlgObject()) {
				Target = OI.asDlgObject().getDlgObject();
				if(Target == null) {
					this.throwOperation("Delegation Error: The delegation null object for the "
							+ this.getOperKindName() + " " + this.toString() + "."
							+ pSignature.toString() + " is null.", pContext, pAsType, pSignature);
					return null;
				}
			} else if(OI.getRKind().isDlgAttr()) {
				
				// Get the target from the delegated field
				Target = this.getAttrData(ThisContext, pInitiator, OI.getOwnerAsType(), OI.asDlgAttr().getDlgAttrName(), null);
				if(Target == null) {
					this.throwOperation("Delegation Error: The delegation field `"
							+ OI.asDlgAttr().getDlgAttrName() + "` for the "
							+ this.getOperKindName() + " " + this.toString() + "."
							+ pSignature.toString() + " is null.", ThisContext, pAsType, pSignature);
					return null;
				}
			} else {
				this.throwOperation("Operaion Invocation Error: Unknown OperationInfo Kind ("
						+OI.getRKind().toString()+").", pContext, pAsType, pSignature);
				return null;
			}

			// Get the Target - Now delegate there.
			if(Target instanceof StackOwner) { // Target is a StackOwner
				StackOwner TargetSO = (StackOwner)Target;
				ExecSignature ES = TargetSO.searchOperation(ThisContext.getEngine(), pSignature);
				// Execute the target as the specific type from the target me
				return TargetSO.invokeOper(ThisContext, pInitiator, pIsBlindCaller, null,
						ES, pParams, pIsAlreadyAdjusted);
			} else { // Target is not a StackOwner
				// Do as native (Non-Static only)
				Method M = UClass.getMethod(Target.getClass(), pSignature.getName(), false, pParams);
				if(M == null) {
					this.throwOperation("Delegation Error: "
							+ "There is no such a compatible method in the delegated Java Object `"
							+ Target.toString() + "`.", ThisContext, pAsType, pSignature);
					return null;
				}
				try {
					// Change to Native if need and can
					Object   O = Target;
					Class<?> C = M.getDeclaringClass();
					if(!C.isInstance(O) && (O instanceof DObjectStandalone)) {
						if(C.isAssignableFrom(DObject.class)) O = ((DObjectStandalone)O).getAsDObject();
						else                                  O = ((DObjectStandalone)O).getAsNative();
					}
					
					return UClass.invokeMethod(M, O, pParams);
				} catch(Throwable T) {
					throw new CurryError("Operaion Invocation Error: "
							+ "An error occurs while invoking the delegating method `"
							+ M.toString() + "`.", ThisContext, T);
				}
			}
		}

		// Check if this is an abstract operation
		if(OI.asDirect().isAbstract()) {
			// Abstract operation
			return this.doAbstractOperation(pContext, pInitiator, pIsBlindCaller, pAsType,
					pSignature, pParams, pIsAlreadyAdjusted);
		}
		// Get the body and do the early return
		Executable Exec = ((OperationInfo.OIDirect)OI);
		return pContext.getExecutor().execExecutable(pContext, pInitiator, Exec, Exec.getKind(), pIsBlindCaller,
				this, pParams, pIsAlreadyAdjusted, true);
	}

	/** Access the attribute */
	final Object accessAttr(Context pContext, Expression pInitiator, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName, Object pParam1, Object pParam2, HashSet<Object> pObjects) {
		// Get the respond
		AttributeInfo AI = this.getAttribute(pContext, pInitiator, pAKind, pAsType, pAttrName, pObjects);

		// Get the type of this Attribute
		Type ResultType = null;
		switch(pAKind) {
			case Set:
			case Get:
				// TO-DO-LATER - This must be reconsider
				ResultType = TKJava.TAny;
				break;
			case IsReadable:
			case IsWritable:
			case IsNotTypeCheck:
				ResultType = TKJava.TBoolean;
				break;
			case GetType:
				ResultType = TKJava.TType;
				break;
			case Clone:
				// TO-DO-LATER - Make a separate method for this
				throw new CurryError("Attribute Access Error: Clone Attribute is not allowed.", pContext);
			case Config:
			case GetMoreInfo:
				ResultType = TKJava.TAny;
				break;
		}

		// Null respond - the operation is not found
		if(AI == null) {
			// See what else we do
			Object Result = this.doAttributeRespondNotFound(pContext, pInitiator, pAKind, pAsType, pAttrName, pParam1, pParam2);
			
			if(!(Result instanceof Respond)) {
				if(ResultType == null) {
					// The return type of Get is not known when R is null;
					return Result;
				}
				// Check the return type
				// pContext.getEngine().ensureTypeInitialized(ResultType); - Commented out because
				// all above types are Java
				if(ResultType.canBeAssignedBy(Result)) return Result;
				this.throwAttribute("Attribute Access Error: `"
						+ pContext.getEngine().toString(pContext, Result)
						+ "` is not a valid return value of ("
						+ this.toString() + ")" + this.toString() + "."
						+ pAttrName + ".", pContext, pAKind, pAsType, pAttrName);
				return null;
			}
			AI = (AttributeInfo)Result;
		}

		// Allow to reconsider after Invalid Respond was found
		while(!AI.getRKind().isDirect()) {

			// Insufficient permission
			if(AI.getRKind().isNoPermission()) {
				Type T = pContext.getEngine().getTypeManager().getTypeOf(this);
				this.throwAttribute("Attribute Access Error: Insufficient permission to access the "
						+ this.getAttrKindName() + " `" + T + "`." + pAttrName + ").",
						pContext, pAKind, pAsType, pAttrName);
			}

			// Handle dynamically
			if(AI.getRKind().isDynamicHandle()) {
				
				// Change the Context to be owned by this with the type that owns the OI so that
				//    the getAttrData() and getOperation is done from this object
				Context ThisContext = new Context.ContextStackOwner(pContext, null, false, pInitiator, this,
						AI.getOwnerAsType(), null);
				
				// Make sure that dynamic handling is supported in this StackOwner.
				if(this.isHandleDynamically()) {
					// Search in this StackOwner for the operation with the signature
					// `Respond.DynamicHandleSignature`
					
					TypeRef TR = ((AttributeInfo.AIDynamic)AI).getTypeRef();
						
					Object Result = this.dynamicHandling(ThisContext, pInitiator, false, new Object[] { TR, pAKind, pAttrName, pParam1, pParam2 });
					if(!(Result instanceof SpecialResult)) {
						
						this.getEngine().getTypeManager().ensureTypeInitialized(ThisContext, TR);
						Type RT = TR.getTheType();
						if(!RT.canBeAssignedBy(ThisContext, Result)) {
							this.throwAttribute("Executable Error: Invalid return type ('"+
									this.getEngine().toString(ThisContext, Result)+"')",
									ThisContext, pAKind, pAsType, pAttrName, pParam1, pParam2);
						}
					}
					return Result;
				} else {
					Object Result = this.doDynamicDelegationNotSupported(ThisContext, pInitiator,
							pAKind, pAsType, pAttrName, pParam1, pParam2);
					// See what else we can do
					if(!(Result instanceof Respond)) {
						if(ResultType == null) {
							// The return type of Get is not known when R is null;
							return Result;
						}
						// Check the return type
						// pContext.getEngine().ensureTypeInitialized(ResultType); - Commented out
						// because all above tyes are Java
						if(ResultType.canBeAssignedBy(Result)) return Result;
						this.throwAttribute("Attribute Access Error: `"
								+ ThisContext.getEngine().toString(ThisContext, Result)
								+ "` is not a valid return value of ("
								+ this.toString() + ")" + this.toString() + "."
								+ pAttrName + ".", ThisContext, pAKind, pAsType, pAttrName);
						return null;
					}
					// If it is still the same, throw an error
					if(AI == Result) {
						// If it still
						this.throwDynamicDelegationNotSupported(ThisContext, pAKind, pAsType, pAttrName, pParam1, pParam2);
						return null;
					}
					// Re consider the respond
					AI = (AttributeInfo)Result;
					continue;
				}
			}

			// Handle Native
			if(AI.getRKind().isNative()) {
				Field F = ((AttributeInfo.AINative)AI).Field;
				try {
					switch(pAKind) {
						case Get:
							if(UClass.isMemberStatic(F))
								return UClass.getFieldValue(F, null);
							else {
								// Change to Native if need and can
								Object   O = this;
								Class<?> C = F.getDeclaringClass();
								if(!C.isInstance(O) && (O instanceof DObjectStandalone)) {
									if(C.isAssignableFrom(DObject.class)) O = ((DObjectStandalone)O).getAsDObject();
									else                                  O = ((DObjectStandalone)O).getAsNative();
								}
								
								return UClass.getFieldValue(F, O);
							}
						case Set:
							if(UClass.isMemberStatic(F))
								return UClass.setFieldValue(F, null, pParam1);
							else {
								// Change to Native if need and can
								Object   O = this;
								Class<?> C = F.getDeclaringClass();
								if(!C.isInstance(O) && (O instanceof DObjectStandalone)) {
									if(C.isAssignableFrom(DObject.class)) O = ((DObjectStandalone)O).getAsDObject();
									else                                  O = ((DObjectStandalone)O).getAsNative();
								}
								
								return UClass.setFieldValue(F, O, pParam1);
							}
						case IsReadable:
							return true;
						case IsWritable:
							return UClass.isMemberFinal(F);
						case IsNotTypeCheck:
							return false;
						case GetType:
							return pContext.getEngine().getTypeManager().getTypeOfTheInstanceOf(F.getType());
						case Clone:
							// TO-DO-LATER - Make a separate method for this
							this.throwAttribute("Attribute Access Error: Clone Attribute is not allowed.",
									pContext, pAKind, pAsType, pAttrName);
							return null;
						case Config:
						case GetMoreInfo:
							return null;
					}
				} catch(Throwable T) {
					throw new CurryError("An error occurs while accessing the java field `"
							+ F.toString() + "` ((" + this.toString() + ")"
							+ this.toString() + "." + pAttrName + ").", pContext, T);
				}
			}
			

			// Change the Context to be owned by this with the type that owns the OI so that
			//    the getAttrData() and getOperation is done from this object
			Context ThisContext = new Context.ContextDelegate(pContext, null, pInitiator, this,
					AI.getOwnerAsType(), null);

			Object Target = null;
			// Get the target as This
			if(AI.getRKind().isDlgObject()) {
				Target = AI.asDlgObject().getDlgObject();
				if(Target == null) {
					this.throwAttribute("Delegation Error: The delegation null object for the "
							+ this.getAttrKindName() + " (" + this.toString()
							+ ")" + this.toString() + "." + pAttrName + " is null.", ThisContext,
							pAKind, pAsType, pAttrName);
					return null;
				}
				
				// Resolve the type here
				if(((AttributeInfo.AIDlgObject)AI).TypeRef == null) {
					Type T = null;
					if(Target instanceof StackOwner) {
						T = (Type)(((StackOwner)Target).accessAttr(
								ThisContext, pInitiator, DataHolder.AccessKind.GetType, null, pAttrName,
								pParam1, pParam2, null));
					} else {
						Field F = UClass.getField(Target.getClass(), pAttrName, false);
						if(F != null) pContext.getEngine().getTypeManager().getTypeOfTheInstanceOfNoCheck(pContext, F.getType()) ;
					}
					// The type does not exist
					if(T == null) {
						this.throwAttribute("Delegation Error: The delegation to the attribute '"+pAttrName+"' that" +
								"does not exist in the object '"+pContext.getEngine().getDisplayObject(Target)+
								"'; for the " + this.getAttrKindName() + " (" + this.toString()
								+ ")" + this.toString() + "." + pAttrName + " is null.", ThisContext,
								pAKind, pAsType, pAttrName);
						return null;
					}
					
					((AttributeInfo.AIDlgAttr)AI).TypeRef = T.getTypeRef();
				}
				
			} else if(AI.getRKind().isDlgAttr()) {				
				// Get the target from the delegated field
				Target = this.getAttrData(ThisContext, pInitiator, AI.getOwnerAsType(), AI.asDlgAttr().getDlgAttrName(), null);
				if(Target == null) {
					this.throwAttribute("Delegation Error: The delegation field #" + AI.asDlgAttr().getDlgAttrName() + " for the "
							+ this.getAttrKindName() + " (" + this.toString() + ")" + this.toString() + "." + pAttrName + " is null.",
							ThisContext,
							pAKind, pAsType, pAttrName);
					return null;
				}
			} else {
				this.throwAttribute("Operaion Invocation Error: Unknown OperationInfo Kind ("
						+AI.getRKind().toString()+").", ThisContext, pAKind, pAsType, pAttrName);
				return null;
			}

			// Do the delegate
			if(Target instanceof StackOwner) { // Target is a StackOwner
				StackOwner TargetSO = (StackOwner)Target;
				// Execute the target as the specific type from the target me
				return TargetSO.accessAttr(ThisContext, pInitiator, pAKind, null, pAttrName, pParam1, pParam2, null);
			} else { // Target is not a StackOwner
				// Do as native (Non-Static only)
				Field F = UClass.getField(Target.getClass(), pAttrName, false);
				if(F == null) {
					this.throwAttribute(
							"Delegation Error: There is no such a field in the delegated Java Object `"
									+ Target.toString() + "` (("
									+ this.toString() + ")" + this.toString()
									+ "." + pAttrName + ").", ThisContext, pAKind, pAsType, pAttrName);
					return null;
				}
				try {
					switch(pAKind) {
						case Get: {
							// Change to Native if need and can
							Object   O = Target;
							Class<?> C = F.getDeclaringClass();
							if(!C.isInstance(O) && (O instanceof DObjectStandalone)) {
								if(C.isAssignableFrom(DObject.class)) O = ((DObjectStandalone)O).getAsDObject();
								else                                  O = ((DObjectStandalone)O).getAsNative();
							}

							return UClass.getFieldValue(F, O);
						}
						case Set: {
							// Change to Native if need and can
							Object   O = Target;
							Class<?> C = F.getDeclaringClass();
							if(!C.isInstance(O) && (O instanceof DObjectStandalone)) {
								if(C.isAssignableFrom(DObject.class)) O = ((DObjectStandalone)O).getAsDObject();
								else                                  O = ((DObjectStandalone)O).getAsNative();
							}
								
							return UClass.setFieldValue(F, O, pParam1);
						}
						case IsReadable:
							return true;
						case IsWritable:
							return UClass.isMemberFinal(F);
						case IsNotTypeCheck:
							return false;
						case GetType:
							return ThisContext.getEngine().getTypeManager().getTypeOfTheInstanceOf(F.getType());
						case Clone:
							// TO-DO-LATER - Make a separate method for this
							throw new CurryError(
									"Attribute Access Error: Clone Attribute is not allowed.",
									ThisContext);
						case Config:
						case GetMoreInfo:
							return null;
					}
				} catch(Throwable T) {
					throw new CurryError("An error occurs while accessing the delegating "
							+ this.getAttrKindName() + " `" + F.toString() + "` (("
							+ this.toString() + ")" + this.toString() + "."
							+ pAttrName + ").", ThisContext, T);
				}
			}
		}
		
		// Check if this is an abstract operation
		if(AI.asDirect().isAbstract()) {
			if((pAsType == null) || (pAsType == this))
				this.throwAbstractAttribute(pContext, pAKind, pAsType, pAttrName, pParam1, pParam2);
			return this.accessAttr(pContext, pInitiator, pAKind, null, pAttrName, pParam1, pParam2, pObjects);
		}

		// Get the DataHolder and early return
		DataHolder DH = this.getDHAt(pContext, pAKind, (AttributeInfo.AIDirect)AI);
		// Check if this is an abstract operation
		if(DH == null) {
			Object O = AI.getOwner();
			if(O != this) {
				if(O instanceof StackOwner)
					 return ((StackOwner)O).accessAttr(pContext, pInitiator, pAKind, pAsType, pAttrName, pParam1, pParam2, pObjects);
			}
			this.throwAttribute("Internal Error: the data-holder not found even though the attribute is found.",
					pContext, pAKind, pAsType, pAttrName, pParam1, pParam2);
		}
		
		// Change the Context to be owned by this with the type that owns the OI so that
		//    the getAttrData() and getOperation is done from this object
		Context ThisContext = pContext;
		
		// Change the context
		if(!UCurry.isDataHolderNormal(DH)) {
			// Create a new context for the curry DH
			ThisContext = new Context.ContextStackOwner(ThisContext, pAttrName, true, pInitiator, this,
							AI.getOwnerAsType(), null);
		}

		// Access and return
		switch(pAKind) {
			case Get:
				Object Result = ThisContext.getEngine().getDataHolderManager().getDHData(ThisContext, pAttrName, DH);
				// Enforce 'isNoNull'
				if(this.isEnforceNotNull() && AI.isNotNull() && (pParam1 == Result))
					this.throwAttributeShouldNotBeNull(ThisContext, pAKind, pAsType, pAttrName);
				return Result;
			case Set:
				// Enforce 'isNoNull'
				if(this.isEnforceNotNull() && AI.isNotNull() && (pParam1 == null))
					this.throwAttributeMustNotBeNull(ThisContext, pAKind, pAsType, pAttrName);
				return ThisContext.getEngine().getDataHolderManager().setDHData(ThisContext, pAttrName, DH, pParam1);
			case IsReadable:
				return ThisContext.getEngine().getDataHolderManager().isDHReadable(ThisContext, pAttrName, DH);
			case IsWritable:
				return ThisContext.getEngine().getDataHolderManager().isDHWritable(ThisContext, pAttrName, DH);
			case IsNotTypeCheck:
				return ThisContext.getEngine().getDataHolderManager().isDHNoTypeCheck(ThisContext, pAttrName, DH);
			case GetType:
				return ThisContext.getEngine().getDataHolderManager().getDHType(ThisContext, pAttrName, DH);
			case Clone:
				// TO-DO-LATER - Make a separate method for this
				throw new CurryError("Attribute Access Error: Clone Attribute is not allowed.", ThisContext);
			case Config:
				return ThisContext.getEngine().getDataHolderManager().configDH(ThisContext, DH, (String)pParam1,
						(Object[])pParam2);
			case GetMoreInfo:
				return ThisContext.getEngine().getDataHolderManager().getDHMoreInfo(ThisContext, DH, (String)pParam1);
		}
		return null;
	}

	// Handle A/O ----------------------------------------------------------------------------------
	// NOTE: Middle level
	// NOTE: pContext must not be null here
	// Allow customizable to the high-level without having to override multiple method
	// Do appropriate search (for Operation)

	/** Kinds of operation access */
	static public enum OperationSearchKind {
		Direct,          // No search use a exact Signature          (ExecSignature)
		ByParams,        // Search using Parameters                  (Name, Object[], Object[][])
		ByTRefs,         // Search by the parameter type references  (Name, TypeRef[])
		ByNameInterface, // Search by name and interface             (Name, ExecInterface)
		BySignature      // Search by Signature                      (ExecSignature)
	}

	/** Execute an operation */
	@SuppressWarnings("incomplete-switch")
    protected Object invokeOperation(Context pContext, Expression pInitiator, boolean pIsBlindCaller,
			OperationSearchKind pOSKind, Type pAsType, Object pParam1, Object pParam2, Object[] pParams,
			boolean pIsAlreadyAdjusted) {
		
		if(pParam1 == null) throw new NullPointerException();
		if(pOSKind == null) throw new NullPointerException();
		ExecSignature Signature  = null;
		Object[]      Parameters = pParams;
		switch(pOSKind) {
			case Direct: {
				Signature = (ExecSignature)pParam1;
				break;
			}
			case BySignature: {
				ExecSignature ES = (ExecSignature)pParam1;
				Signature = this.searchOperation(pContext.getEngine(), ES);
				if(Signature == null) throw new CurryError("The "+this.getOperKindName()+" "+ES.toString()+" is not found.");
				break;
			}
			// The following group is a search with Name
			default: {
				String OName = (String)pParam1; 
				// Search
				switch(pOSKind) {
					case ByParams: {
						Object[][] AParams = new Object[1][];
						Signature          = this.searchOperation(pContext.getEngine(), OName, pParams, AParams);
						if(AParams[0] != null) {	// The search adjusted the parameters.
							Parameters         = AParams[0];
							pIsAlreadyAdjusted = true;
						}
						break;
					}
					case ByTRefs:         Signature = this.searchOperation(pContext.getEngine(), OName, (TypeRef[])pParam2);     break;
					case ByNameInterface: Signature = this.searchOperation(pContext.getEngine(), OName, (ExecInterface)pParam2); break;
				}
				if(Signature == null) {
					
					String ParamToString = null;
					if(pParam2 == null) ParamToString = "()";
					else if(pOSKind == OperationSearchKind.ByParams) {
						StringBuffer SB = new StringBuffer();
						if(pParam2.getClass().isArray()) {
							for(int i = 0; i < UArray.getLength(pParam2); i++) {
								if(i != 0) SB.append(", ");
								SB.append(pContext.getEngine().getTypeManager().getTypeOfNoCheck(pContext, UArray.get(pParam2, i)));
							}
						}
						ParamToString = "(" + SB.toString() + ")";
					} else ParamToString = UArray.toString(pParam2, "(", ")", ",");
					
					throw new CurryError("The "+this.getOperKindName()+" "+OName+ParamToString+" is not found.");
				}
				break;
			}
		}
		return this.invokeOper(pContext, pInitiator, pIsBlindCaller, pAsType, Signature, Parameters, pIsAlreadyAdjusted);
	}
	
	/** Access Attribute in the Generic way (for customization) */
	protected Object accessAttribute(Context pContext, Expression pInitiator, DataHolder.AccessKind pAKind,
			            Type pAsType, String pAttrName, Object pParam1, Object pParam2, HashSet<Object> pObjects) {
		return this.accessAttr(pContext, pInitiator, pAKind, pAsType, pAttrName, pParam1, pParam2, pObjects);
	}

	// Handle A/O ----------------------------------------------------------------------------------
	// NOTE: High level
	// For reliability (type checking of parameters) and Convenient for the users

	// Operations ------------------------------------------------------------------------
	
	// For Internal use --------------------------------------------------------
	// These methods may be used in case pContext must be pasted on.

	/** Execute an operation directly (no search) */
	final protected Object invokeDirect(Context pContext, Expression pInitiator,
			boolean pIsBlindCaller, Type pAsType, ExecSignature pES, Object[] pParams,
			boolean pIsAlreadyAdjusted) {
		if(pES == null) throw new NullPointerException();
		return invokeOperation(pContext, pInitiator, pIsBlindCaller,
				OperationSearchKind.Direct, pAsType, pES, pParams, pParams, pIsAlreadyAdjusted);
	}

	/** Execute an operation directly (no search) */
	final protected Object invokeDirect(Context pContext, Expression pInitiator,
			boolean pIsBlindCaller, Type pAsType, ExecSignature pES, Object... pParams) {
		if(pES == null) throw new NullPointerException();
		return invokeOperation(pContext, pInitiator, pIsBlindCaller,
				OperationSearchKind.Direct, pAsType, pES, pParams, pParams, false);
	}

	/** Execute an operation */
	final protected Object invoke(Context pContext, Expression pInitiator, boolean pIsBlindCaller,
			Type pAsType, ExecSignature pES, Object... pParams) {
		if(pES == null) throw new NullPointerException();
		return invokeOperation(pContext, pInitiator, pIsBlindCaller,
				OperationSearchKind.BySignature, pAsType, pES, null, pParams, false);
	}

	/** Execute an operation */
	final protected Object invoke(Context pContext, Expression pInitiator, boolean pIsBlindCaller,
			Type pAsType, String pOName, Object... pParams) {
		if(pOName == null) throw new NullPointerException();
		return invokeOperation(pContext, pInitiator, pIsBlindCaller, OperationSearchKind.ByParams, pAsType, pOName,
				pParams, pParams, false);
	}

	/** Execute an operation */
	final protected Object invoke(Context pContext, Expression pInitiator, boolean pIsBlindCaller,
			Type pAsType, String pOName, TypeRef[] pPTypeRefs, Object... pParams) {
		if(pOName == null) throw new NullPointerException();
		if(pPTypeRefs == null) pPTypeRefs = TypeRef.EmptyTypeRefArray;
		return invokeOperation(pContext, pInitiator, pIsBlindCaller,
				OperationSearchKind.ByTRefs, pAsType, pOName, pPTypeRefs, pParams, false);
	}

	/** Execute an operation */
	final protected Object invoke(Context pContext, Expression pInitiator, boolean pIsBlindCaller,
			Type pAsType, String pOName, ExecInterface pEI, Object... pParams) {
		if(pOName == null) throw new NullPointerException();
		if(pEI == null) pEI = ExecInterface.EmptyInterface;
		return this.invokeOperation(pContext, pInitiator, pIsBlindCaller,
				OperationSearchKind.ByNameInterface, pAsType, pOName, pEI, pParams, false);
	}

	// For public use ----------------------------------------------------------
	// These methods are for public use, they hide pContext (for Stand-alone use).
	
	// With AsType ---------------------------------------------------

	/** Execute an operation directly (no search) */
	final protected Object invokeDirect(Type pAsType, ExecSignature pES, Object[] pParams,
			boolean pIsAlreadyAdjusted) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invokeDirect(C, null, false, pAsType, pES, pParams, pIsAlreadyAdjusted);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation directly (no search) */
	final protected Object invokeDirect(Type pAsType, ExecSignature pES, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object R = this.invokeDirect(C, null, false, pAsType, pES, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final protected Object invoke(Type pAsType, ExecSignature pES, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object R = this.invoke(C, null, false, pAsType, pES, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final protected Object invoke(Type pAsType, String pFName, Object... pParams) {
		Engine E;
		if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) {
			if(this instanceof TJava) {
				Method M = UClass.getMethod(((TJava)this).getDataClass(), pFName, true, pParams);
				try { return UClass.invokeMethod(M, null, pParams); }
				catch(Exception Ex) {
					throw new CurryError("There is an error setting the field "+this.toString()+"."+pFName
							+ UArray.toString(pParams, "(", ")", ",")+".", Ex);
				}
			} else throw new NullPointerException(); // No Engine
		}
		Context C = E.newRootContext();
		Object  R = this.invoke(C, null, false, pAsType, pFName, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final protected Object invoke(Type pAsType, String pFName, Type[] pPTypes, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invoke(C, null, false, pAsType, pFName, pPTypes, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final protected Object invoke(Type pAsType, String pFName, TypeRef[] pPTypeRefs, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invoke(C, null, false, pAsType, pFName, pPTypeRefs, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final protected Object invoke(Type pAsType, String pFName, ExecInterface pEI, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invoke(C, null, false, pAsType, pFName, pEI, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}
	
	// Without AsType ------------------------------------------------
	
	/** Execute an operation directly (no search) */
	final public Object invokeDirect(ExecSignature pES, Object[] pParams, boolean pIsAlreadyAdjusted) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null))
			throw new NullPointerException("No Engine!!!");
		
		Context C = E.newRootContext();
		Object  R = this.invokeDirect(C, null, false, null, pES, pParams, pIsAlreadyAdjusted);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation directly (no search) */
	final public Object invokeDirect(ExecSignature pES, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invokeDirect(C, null, false, null, pES, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final public Object invoke(ExecSignature pES, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invoke(C, null, false, null, pES, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final public Object invoke(String pFName, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null))
			throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invoke(C, null, false, null, pFName, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final public Object invoke(String pFName, Type[] pPTypes, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null))
			throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invoke(C, null, false, null, pFName, pPTypes, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final public Object invoke(String pFName, TypeRef[] pPTypeRefs, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invoke(C, null, false, null, pFName, pPTypeRefs, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Execute an operation */
	final public Object invoke(String pFName, ExecInterface pEI, Object... pParams) {
		Engine  E; if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) throw new NullPointerException("No Engine!!!");
		Context C = E.newRootContext();
		Object  R = this.invoke(C, null, false, null, pFName, pEI, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	// Attributes ------------------------------------------------------------------------

	// For Internal Use --------------------------------------------------------
	// These methods may be used in case pContext must be pasted on.

	/** Set value to the attribute */
	final protected Object setAttrData(Context pContext, Expression pInitiator, Type pAsType, String pAttrName, Object pData) {
		return this.accessAttribute(pContext, pInitiator, DataHolder.AccessKind.Set, pAsType, pAttrName, pData, null, null);
	}

	/** Get value to the attribute */
	final protected Object getAttrData(Context pContext, Expression pInitiator, Type pAsType, String pAttrName, HashSet<Object> pObjects) {
		return this.accessAttribute(pContext, pInitiator, DataHolder.AccessKind.Get, pAsType, pAttrName, null, null, pObjects);
	}

	/** Check if the attribute is readable */
	final protected boolean isAttrReadable(Context pContext, Expression pInitiator, Type pAsType, String pAttrName) {
		return (Boolean)this.accessAttribute(pContext, pInitiator, DataHolder.AccessKind.IsReadable, pAsType, pAttrName, null, null, null);
	}

	/** Check if the attribute is writable */
	final protected boolean isAttrWritable(Context pContext, Expression pInitiator, Type pAsType, String pAttrName) {
		return (Boolean)this.accessAttribute(pContext, pInitiator, DataHolder.AccessKind.IsWritable, pAsType, pAttrName, null, null, null);
	}

	/** Check if type checking of this attribute must be skipped */
	final protected boolean isAttrNoTypeCheck(Context pContext, Expression pInitiator, Type pAsType, String pAttrName) {
		return (Boolean)this.accessAttribute(pContext, pInitiator, DataHolder.AccessKind.IsNotTypeCheck, pAsType, pAttrName, null, null, null);
	}

	/** Get the attribute type */
	final protected Type getAttrType(Context pContext, Expression pInitiator, Type pAsType, String pAttrName) {
		return (Type)this.accessAttribute(pContext, pInitiator, DataHolder.AccessKind.GetType, pAsType, pAttrName, null, null, null);
	}
	final protected Object configAttr(Context pContext, Expression pInitiator, Type pAsType, String pAttrName,
			String pCName, Object[] pParams) {
		return this.accessAttribute(pContext, pInitiator, DataHolder.AccessKind.Config, pAsType, pAttrName, pCName, pParams, null);
	}

	/** Performs advance configuration to the attribute. */
	final protected Object getAttrMoreInfo(Context pContext, Expression pInitiator, Type pAsType, String pAttrName,
			String pMIName) {
		return this.accessAttribute(pContext, pInitiator, DataHolder.AccessKind.GetMoreInfo, pAsType, pAttrName, pMIName, null, null);
	}

	// For public use ----------------------------------------------------------
	// These methods are for public use, they hide pContext (for Stand-alone use).

	/** Set value to the attribute */
	final public Object setAttrData(String pAttrName, Object pData) {
		Engine E;
		if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) {
			if(this instanceof TJava) {
				Field F = UClass.getField(((TJava)this).getDataClass(), pAttrName, true);
				if(F == null) {
					if(this.searchAttribute((E = Engine.An_Engine), false, null, pAttrName) == null)
						throw new CurryError("There is no such field "+this.toString()+"."+pAttrName+".");
				} else {
					try { UClass.setFieldValue(F, null, pData); return true; }
					catch(Exception Ex) {
						throw new CurryError("There is an error setting the field "+this.toString()+"."+pAttrName+".", Ex);
					}
				}
			} else throw new NullPointerException(); // No Engine
		}
		Context C = E.newRootContext();
		return this.setAttrData(C, null, null, pAttrName, pData);
	}

	/** Get value to the attribute */
	final public Object getAttrData(String pAttrName) {
		Engine E;
		if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) {
			if(this instanceof TJava) {
				Field F = UClass.getField(((TJava)this).getDataClass(), pAttrName, true);
				if(F == null) {
					if(this.searchAttribute((E = Engine.An_Engine), false, null, pAttrName) == null)
						throw new CurryError("There is no such field "+this.toString()+"."+pAttrName+".");
				} else {
					try { return UClass.getFieldValue(F, null); }
					catch(Exception Ex) {
						throw new CurryError("There is an error getting the field "+this.toString()+"."+pAttrName+".", Ex);
					}
				}
			} else throw new NullPointerException(); // No Engine
		}
		Context C = E.newRootContext();
		Object R = this.getAttrData(C, null, null, pAttrName, null);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Check if the attribute is readable */
	final public boolean isAttrReadable(String pAttrName) {
		Engine E;
		if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) {
			if(this instanceof TJava) {
				Field F = UClass.getField(((TJava)this).getDataClass(), pAttrName, true);
				if(F != null) return true;

				if(this.searchAttribute((E = Engine.An_Engine), false, null, pAttrName) == null)
					throw new CurryError("There is no such field "+this.toString()+"."+pAttrName+".");
				
			} else throw new NullPointerException(); // No Engine
		}
		Context C = E.newRootContext();
		return this.isAttrReadable(C, null, null, pAttrName);
	}

	/** Check if the attribute is writable */
	final public boolean isAttrWritable(String pAttrName) {
		Engine E;
		if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) {
			if(this instanceof TJava) {
				Field F = UClass.getField(((TJava)this).getDataClass(), pAttrName, true);
				if(F != null) return Modifier.isFinal(F.getModifiers());

				if(this.searchAttribute((E = Engine.An_Engine), false, null, pAttrName) == null)
					throw new CurryError("There is no such field "+this.toString()+"."+pAttrName+".");
				
			} else throw new NullPointerException(); // No Engine
		}
		Context C = E.newRootContext();
		return this.isAttrWritable(C, null, null, pAttrName);
	}

	/** Check if type checking of this attribute must be skipped */
	final public boolean isAttrNoTypeCheck(String pAttrName) {
		Engine E;
		if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) {
			if(this instanceof TJava) {
				Field F = UClass.getField(((TJava)this).getDataClass(), pAttrName, true);
				if(F != null) return true;

				if(this.searchAttribute((E = Engine.An_Engine), false, null, pAttrName) == null)
					throw new CurryError("There is no such field "+this.toString()+"."+pAttrName+".");
				
			} else throw new NullPointerException(); // No Engine
		}
		Context C = E.newRootContext();
		return this.isAttrNoTypeCheck(C, null, null, pAttrName);
	}

	/** Get the attribute type */
	final public Type getAttrType(String pAttrName) {
		Engine E;
		if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) {
			if(this instanceof TJava) {
				Field F = UClass.getField(((TJava)this).getDataClass(), pAttrName, true);
				if(F != null) return TKJava.Instance.getTypeByClass(E = Engine.An_Engine, null, F.getType()) ;

				if(this.searchAttribute((E = Engine.An_Engine), false, null, pAttrName) == null)
					throw new CurryError("There is no such field "+this.toString()+"."+pAttrName+".");
				
			} else throw new NullPointerException(); // No Engine
		}
		Context C = E.newRootContext();
		return this.getAttrType(C, null, null, pAttrName);
	}
	/** Performs advance configuration to the attribute. */
	final public Object configAttr(String pAttrName, String pCName, Object ... pParams) {
		Engine E;
		if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) {
			if(this instanceof TJava) {
				Field F = UClass.getField(((TJava)this).getDataClass(), pAttrName, true);
				if(F != null) return null;

				if(this.searchAttribute((E = Engine.An_Engine), false, null, pAttrName) == null)
					throw new CurryError("There is no such field "+this.toString()+"."+pAttrName+".");
				
			} else throw new NullPointerException(); // No Engine
		}
		Context C = E.newRootContext();
		Object  R = this.configAttr(C, null, null, pAttrName, pCName, pParams);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	/** Performs advance configuration to the attribute. */
	final public Object getAttrMoreInfo(String pAttrName, String pMIName) {
		Engine E;
		if(((E = this.getEngine()) == null) && ((E = Engine.An_Engine) == null)) {
			if(this instanceof TJava) {
				Field F = UClass.getField(((TJava)this).getDataClass(), pAttrName, true);
				if(F != null) return null;

				if(this.searchAttribute((E = Engine.An_Engine), false, null, pAttrName) == null)
					throw new CurryError("There is no such field "+this.toString()+"."+pAttrName+".");
				
			} else throw new NullPointerException("There is no embeded engine to execute the stackowner."); // No Engine
		}
		Context C = E.newRootContext();
		Object R = this.getAttrMoreInfo(C, null, null, pAttrName, pMIName);
		if(R instanceof SpecialResult) throw ((SpecialResult)R).getException(C);
		return R;
	}

	// ---------------------------------------------------------------------------------------------
	// Search A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Search A/O Local ------------------------------------------------------------------
	// NOTE: Low level
	// NOTE: pEngine must not be null here

	/** Search attribute of this StackOwner as the type */
	abstract protected TypeRef searchAttributeLocal(Engine pEngine, Type pAsType, String pName);

	/**
	 * Search operation of this StackOwner as the type using name and parameters. <br />
	 *    If pOSKind is ByParams and the pParam3 is Object[1][], the pParam3 is pAdjParams (the
	 *        adjusted values). The method should adjust the params and assign it as the first element
	 *        pAdjParams[0] or set it to null if the method does not support parameter adjustment.
	 **/
	abstract protected ExecSignature searchOperationLocal(Engine pEngine, 
			                            OperationSearchKind pOSKind, Object pParam1, Object pParam2,
			                            Object pParam3);

	// Get A/O ---------------------------------------------------------------------------
	// NOTE: Middle level
	// NOTE: pContext must not be null here
	// Look locally then look in the Dynamic Delegate
	
	/** Do the search for Signature of an operation. */
	protected ExecSignature searchOperation(Engine pEngine, OperationSearchKind pOSKind,
			boolean pIsSearchInDynamicDelegation, Object pParam1, Object pParam2, Object pParam3) {
		if(((pEngine = this.getEngine()) == null) && ((pEngine = Engine.An_Engine) == null))
			throw new NullPointerException("No Engine!!!");
		
		ExecSignature ES = this.searchOperationLocal(pEngine, pOSKind, pParam1, pParam2, pParam3);
		// Found it
		if(ES != null) return ES;
		
		if(!pIsSearchInDynamicDelegation) return null;
		
		// Change the Context to be owned by this so that the getAttrData() and getOperation is done from this object
		Context ThisContext = new Context.ContextDelegate(pEngine.newRootContext(), null, null, this, null);
		// Find it
		int DDCount = this.getDynamicDelegationCount(ThisContext);
		for(int i = 0; i < DDCount; i++) {
			String AName  = this.getDynamicDelegation(ThisContext, i);
			if(AName == null) continue;

			// Get AsType
			TypeRef AsTypeRef = this.getDynamicDelegationAsType(ThisContext, i);
			Type    AsType    = null;
			if(AsTypeRef != null) {
				pEngine.getTypeManager().ensureTypeInitialized(ThisContext, AsTypeRef);
				AsType = AsTypeRef.getTheType();
			}
			
			// If O is not 
			Object O = this.getAttrData(ThisContext, null, AsType, AName, null);
			if(!(O instanceof StackOwner)) {
				if(O == null) continue;
				Type OType = pEngine.getTypeManager().getTypeOf(O);
				ES = OType.searchObjectOperation(O, pEngine, pOSKind, pIsSearchInDynamicDelegation, 
						pParam1, pParam2, pParam3);
			} else {
				ES = ((StackOwner)O).searchOperationLocal(pEngine, pOSKind, pParam1, pParam2, pParam3);
			}
			// Found it
			if(ES != null) return ES;
		}
		return null;
	}

	/** Search attribute of this StackOwner as the type */
	protected TypeRef searchAttribute(Engine pEngine, boolean pIsSearchInDynamicDelegation, Type pAsType, String pName) {
		if(((pEngine = this.getEngine()) == null) && ((pEngine = Engine.An_Engine) == null))
			throw new NullPointerException("No Engine!!!");

		TypeRef TR = this.searchAttributeLocal(pEngine, pAsType, pName);
		if(TR != null) return TR;
		
		if(!pIsSearchInDynamicDelegation) return null;
		
		// Change the Context to be owned by this so that the getAttrData() and getOperation is done from this object
		Context ThisContext = new Context.ContextDelegate(pEngine.newRootContext(), null, null, this, null);
		
		int DDCount = this.getDynamicDelegationCount(ThisContext);
		for(int i = 0; i < DDCount; i++) {
			String AName  = this.getDynamicDelegation(ThisContext, i);
			if(AName == null) continue;

			// Get AsType
			TypeRef AsTypeRef = this.getDynamicDelegationAsType(ThisContext, i);
			pEngine.getTypeManager().ensureTypeInitialized(ThisContext, AsTypeRef);
			Type    AsType    = AsTypeRef.getTheType();
			
			// If O is not a stack owner
			Object O = this.getAttrData(ThisContext, null, AsType, AName, null);
			if(!(O instanceof StackOwner)) {
				if(O == null) continue;
				Type OType = pEngine.getTypeManager().getTypeOf(O);
				TR = OType.searchObjectAttribute(O, pEngine, pIsSearchInDynamicDelegation, pAsType, pName);
				if(TR != null) return TR;
			} else {
				TR = ((StackOwner)O).searchAttribute(pEngine, pIsSearchInDynamicDelegation, pAsType, pName);
				if(TR != null) return TR;
			}
		}
		return null;
	}

	/** Search operation of this StackOwner as the type using name and parameters */
	final protected ExecSignature searchOperation(Engine pEngine, String pOName, Object[] pParams, Object[][] pAdjParams) {
		return this.searchOperation(pEngine, OperationSearchKind.ByParams, true, pOName, pParams, pAdjParams);
	}

	/** Search operation of this StackOwner as the type using name and parameter type refs */
	final protected ExecSignature searchOperation(Engine pEngine, String pOName, TypeRef[] pPTypeRefs) {
		return this.searchOperation(pEngine, OperationSearchKind.ByTRefs, true, pOName, pPTypeRefs, null);
	}

	/** Search operation of this StackOwner as the type using name and interface */
	final protected ExecSignature searchOperation(Engine pEngine, String pOName, ExecInterface pExecInterface) {
		return this.searchOperation(pEngine, OperationSearchKind.ByNameInterface, true, pOName, pExecInterface, null);
	}
		/**
	 * Search operation of this StackOwner as the type using name and signature (if the signature
	 * are exact match, you can use it to execute)
	 */
	final protected ExecSignature searchOperation(Engine pEngine, ExecSignature pExecSignature) {
		return this.searchOperation(pEngine, OperationSearchKind.BySignature, true, pExecSignature, null, null);
	}

	// ---------------------------------------------------------------------------------------------
	// Handle Abnormality --------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Respond not found -------------------------------------------------------

	/**
	 * Handle OperationRespondNotFound. <br />
	 * This method may do the following: 1: returns a replacement respond 2: returns the execution
	 * value 3: throws an exception. (default action)
	 */
	protected Object doOperationRespondNotFound(Context pContext, Expression pInitiator,
			boolean pIsBlindCaller, Type pAsType, ExecSignature pSignature, Object[] pParams,
			boolean pIsAlreadyAdjusted) {
		this.throwOperationRespondNotFound(pContext, pAsType, pSignature);
		return null;
	}

	/**
	 * Handle AttributeRespondNotFound. <br />
	 * This method may do the following: 1: returns a replacement respond 2: returns the execution
	 * value 3: throws an exception. (default action)
	 */
	protected Object doAttributeRespondNotFound(Context pContext, Expression pInitiator,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName, Object pParam1,
			Object pParam2) {
		this.throwAttributeRespondNotFound(pContext, pAKind, pAsType, pAttrName, pParam1, pParam2);
		return null;
	}

	// Dynamic Delegation Does Not Support -------------------------------------

	/**
	 * Handle a dynamic handling no support for a delegation of operation. <br />
	 * This method may do the following: 1: returns a replacement respond 2: returns the execution
	 * value 3: throws an execution. (default action)
	 */
	protected Object doDynamicDelegationNotSupported(Context pContext, Expression pInitiator,
			boolean pIsBlindCaller, Type pAsType, ExecSignature pSignature, Object[] pParams,
			boolean pIsAlreadyAdjusted) {
		this.throwDynamicDelegationNotSupported(pContext, pAsType, pSignature);
		return null;
	}

	/**
	 * Handle a dynamic handling no support for a delegation of attribute. <br />
	 * This method may do the following: 1: returns a replacement respond 2: returns the execution
	 * value 3: throws an execution. (default action)
	 */
	protected Object doDynamicDelegationNotSupported(Context pContext, Expression pInitiator,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName, Object pParam1,
			Object pParam2) {
		this.throwDynamicDelegationNotSupported(pContext, pAKind, pAsType, pAttrName, pParam1, pParam2);
		return null;
	}

	// Abstract Operation ------------------------------------------------------

	/**
	 * Handle a abstract operation. <br />
	 * This method may do the following: 1: returns a replacement respond 2: returns the execution
	 * value 3: throws an execution. (default action)
	 */
	protected Object doAbstractOperation(Context pContext, Expression pInitiator,
			boolean pIsBlindCaller, Type pAsType, ExecSignature pSignature, Object[] pParams,
			boolean pIsAlreadyAdjusted) {
		this.throwAbstractOperation(pContext, pAsType, pSignature);
		return null;
	}

	/**
	 * Handle a dynamic handling no support for a delegation of attribute. <br />
	 * This method may do the following: 1: returns a replacement respond 2: returns the execution
	 * value 3: throws an execution. (default action)
	 */
	protected Object doAbstractAttribute(Context pContext, Expression pInitiator,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName, Object pParam1,
			Object pParam2) {
		this.throwAbstractAttribute(pContext, pAKind, pAsType, pAttrName, pParam1, pParam2);
		return null;
	}

	// Internal utilities (for this SO to use) -----------------------------------------------------

	// Report Error ----------------------------------------------------------------------

	/** Throw an error message that involve operation */
	final protected void throwOperation(String pErrMsg, Context pContext, Type pAsType, 
			ExecSignature pSignature) {
		throw new CurryError(pErrMsg + " ("
				+ this.getOperationAccessToString(OperationSearchKind.BySignature, pAsType,
						pSignature, null) + ").", pContext);
	}

	/** Throw an error message that involve operation */
	final protected void throwOperation(String pErrMsg, Context pContext, Type pAsType,
			String pSignatureStr) {
			throw new CurryError(pErrMsg + " ("
					+ this.getOperationAccessToString(null, pAsType, pSignatureStr, null)
					+ ").", pContext);
	}

	/** Throw an error message that involve attribute */
	final protected void throwAttribute(String pErrMsg, Context pContext, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName, Object pParam1, Object pParam2) {
		throw new CurryError(pErrMsg + " ("
				+ this.getAttributeAccessToString(pAKind, pAsType, pAttrName, pParam1, pParam2)
				+ ").", pContext);
	}

	/** Throw an error message that involve attribute */
	final protected void throwAttribute(String pErrMsg, Context pContext, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName) {
		throw new CurryError(pErrMsg + " ("
				+ this.getAttributeAccessToString(pAKind, pAsType, pAttrName) + ").", pContext);
	}

	// Operation not found

	/** Throw an InvalidOperationRespondObject error */
	final protected void throwOperationRespondNotFound(Context pContext, Type pAsType,
			ExecSignature pSignature) {
		this.throwOperation("Operation Invocation Error: Operation is not found", pContext, pAsType,
				pSignature);
	}

	/** Throw an InvalidAbstractRespondObject error */
	final protected void throwAttributeRespondNotFound(Context pContext,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName,
			Object pParam1, Object pParam2) {
		this.throwAttribute("Attribute is not found", pContext, pAKind, pAsType, pAttrName,
				pParam1, pParam2);
	}

	/** Throw an InvalidOperationRespondObject error */
	final protected void throwDynamicDelegationNotSupported(Context pContext, Type pAsType,
			ExecSignature pSignature) {
		this.throwOperation("Dynamic handling of access is not supported", pContext, pAsType,
				pSignature);
	}

	/** Throw an InvalidOperationRespondObject error */
	final protected void throwDynamicDelegationNotSupported(Context pContext, 
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName,
			Object pParam1, Object pParam2) {
		this.throwAttribute("Dynamic handling of access is not supported", pContext, pAKind,
				pAsType, pAttrName, pParam1, pParam2);
	}

	/** Throw a dynamic handling no support for a delegation of operation. */
	final protected void throwAbstractOperation(Context pContext, Type pAsType,
			ExecSignature pSignature) {
		this.throwOperation("The required " + this.getOperKindName() + " is abstract", pContext,
				pAsType, pSignature);
	}

	/** Throw an InvalidOperationRespondObject error */
	final protected void throwAbstractAttribute(Context pContext, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName, Object pParam1, Object pParam2) {
		this.throwAttribute("The required " + this.getAttrKindName() + " is abstract", pContext,
				pAKind, pAsType, pAttrName, pParam1, pParam2);
	}

	// isNotNull -----------------------------------------------------------------------------------

	final protected void throwAttributeShouldNotBeNull(Context pContext, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName) {
		this.throwAttribute("Attribute Assignment Error: The " + this.getAttrKindName()
				+ " should not be set to null", pContext, pAKind, pAsType, pAttrName);
	}

	final protected void throwAttributeMustNotBeNull(Context pContext, DataHolder.AccessKind pAKind,
			Type pAsType, String pAttrName) {
		this.throwAttribute("Attribute Assignment Error: The " + this.getAttrKindName()
				+ " must not be set to null", pContext, pAKind, pAsType, pAttrName);
	}

	// ---------------------------------------------------------------------------------------------
	// Utilities -----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// ActionRecord ----------------------------------------------------------------------
	
	/** Create an action record. */
	final protected ActionRecord newActionRecord(Context pContext) {
		return this.newActionRecord(pContext, (MoreData)null);
	}
	/** Create an action record. */
	final protected ActionRecord newActionRecord(Context pContext, MoreData pExtraData) {
		LocationSnapshot LS = pContext.getCurrentLocationSnapshot();
		return (pExtraData == null)
		        ? new ActionRecord(this,                           LS            )
		        : new ActionRecord.ActionRecord_WithMoreData(this, LS, pExtraData);
	}
	/** Create an action record. */
	final protected ActionRecord newActionRecord(Context pContext, Documentation pDocument) {
		return this.newActionRecord(pContext, (pDocument == null) ? null : Documentation.Util.NewMoreData(pDocument));
	}
	/** Create an action record. */
	final protected ActionRecord newActionRecord(Context pContext, String pDocumentText) {
		return this.newActionRecord(pContext, (pDocumentText == null) ? null : Documentation.Util.NewMoreData(pDocumentText));
	}
	
	// Closure -------------------------------------------------------------------------------------
	
	/** Creates a new closure and use the name of the body is not an executable (a regular data) */
	final protected StandaloneOperation newClosure(Engine pEngine, String pName, Object pBody) {
		return new StandaloneOperation(pEngine, this, pName, pBody);
	}
	/** Creates a new closure */
	final protected StandaloneOperation newClosure(Engine pEngine, Executable pBody) {
		return new StandaloneOperation(pEngine, this, pBody);
	}
	
	// Duplicate and Creating responds -------------------------------------------------------------
	
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected OperationInfo borrowOperationInfo(OperationInfo pOI) {
		OperationInfo OI = pOI.makeClone();
		if(     OI instanceof SimpleOperation) ((SimpleOperation)OI).changeDeclaredOwner(this);
		else if(OI instanceof OIDirect)        ((OIDirect)       OI).changeDeclaredOwner(this);		
		return OI;
	}
	/** Make a duplicate of the attribute info (the owner is assigned to this StackOwner) */
	final protected AttributeInfo borrowAttributeInfo(AttributeInfo pAI) {
		AttributeInfo AI = pAI.makeClone();
		AI.changeDeclaredOwner(this);
		return AI;
	}
	
	/** Make a clone of the attribute info */
	final protected OperationInfo cloneOperationInfo(OperationInfo pOI) { return pOI.makeClone(); }
	/** Make a clone of the attribute info */
	final protected AttributeInfo cloneAttributeInfo(AttributeInfo pAI) { return pAI.makeClone(); }

	// Dynamic --------------------------------------------------------
	/** Creates a new operation info */
	final protected OIDynamic newOIDynamic(Accessibility pAccess, ExecSignature pES, MoreData pMoreData) {
		this.validateAccessibility(pAccess);
		OIDynamic OID = new OperationInfo.OIDynamic(pAccess, pES, pMoreData);
		OID.changeDeclaredOwner(this);
		return OID;
	}
	/** Creates a new attribute info */
	final protected AIDynamic newAIDynamic(Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, TypeRef pTRef, MoreData pMoreData) {
		this.validateAccessibility(pARead);
		this.validateAccessibility(pAWrite);
		this.validateAccessibility(pAConfig);
		AIDynamic AI = new AttributeInfo.AIDynamic(pARead, pAWrite, pAConfig, pVName, pTRef, pMoreData);
		AI.changeDeclaredOwner(this);
		return AI;
	}
	
	// Field --------------------------------------------------------
	/** Creates a new operation info */
	final protected OIDlgAttr newOIDlgAttr(Accessibility pAccess, ExecSignature pES, String pDlgAttr,
			MoreData pMoreData) {
		this.validateAccessibility(pAccess);
		OIDlgAttr OID = new OperationInfo.OIDlgAttr(pAccess, pES, pDlgAttr, pMoreData);
		OID.changeDeclaredOwner(this);
		return OID;
	}
	/** Creates a new attribute info */
	final protected AIDlgAttr newAIDlgAttr(Accessibility pARead, Accessibility pAWrite, Accessibility pAConfig,
			String pVName, String pDlgAttr, MoreData pMoreData) {
		this.validateAccessibility(pARead);
		this.validateAccessibility(pAWrite);
		this.validateAccessibility(pAConfig);
		AIDlgAttr AI = new AttributeInfo.AIDlgAttr(pARead, pAWrite, pAConfig, pVName, pDlgAttr, pMoreData);
		AI.changeDeclaredOwner(this);
		return AI;
	}
	
	// Object --------------------------------------------------------
	/** Creates a new operation info */
	final protected OIDlgObject newOIDlgObject(Accessibility pAccess, ExecSignature pES, Object pDlgObject,
			MoreData pMoreData) {
		this.validateAccessibility(pAccess);
		OIDlgObject OID = new OperationInfo.OIDlgObject(pAccess, pES, pDlgObject, pMoreData);
		OID.changeDeclaredOwner(this);
		return OID;
	}
	/** Creates a new attribute info */
	final protected AIDlgObject newAIDlgObject(Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, boolean pIsNotNull, Object pDlgObject, MoreData pMoreData) {
		this.validateAccessibility(pARead);
		this.validateAccessibility(pAWrite);
		this.validateAccessibility(pAConfig);
		AIDlgObject AI = new AttributeInfo.AIDlgObject(pARead, pAWrite, pAConfig, pVName, pDlgObject, pMoreData);
		AI.changeDeclaredOwner(this);
		return AI;
	}

	// Direct --------------------------------------------------------
	/** Creates a new operation info */
	final protected OIDirect newOIDirect(Accessibility pAccess, Executable pExec, MoreData pMoreData) {
		this.validateAccessibility(pAccess);
		OIDirect OID = new OperationInfo.OIDirect(pAccess, pExec, pMoreData);
		OID.changeDeclaredOwner(this);
		return OID;
	}
	/** Creates a new abstract operation info */
	final protected OIDirect newOIDirect(Accessibility pAccess, ExecSignature pSignature, Executable.ExecKind pKind,
			MoreData pMoreData) {
		this.validateAccessibility(pAccess);
		OIDirect OID = new OperationInfo.OIDirect(pAccess, pSignature, (pKind != null)?pKind:ExecKind.SubRoutine, pMoreData);
		OID.changeDeclaredOwner(this);
		return OID;
	}
	/** Creates a new attribute info */
	final protected AIDirect newAIDirect(Accessibility pARead, Accessibility pAWrite,
			Accessibility pAConfig, String pVName, boolean pIsNotNull, DataHolderInfo pDHI,
			Location pLocation, MoreData pMoreData) {
		this.validateAccessibility(pARead);
		this.validateAccessibility(pAWrite);
		this.validateAccessibility(pAConfig);
		AIDirect AI = new AttributeInfo.AIDirect(pARead, pAWrite, pAConfig, pVName, pIsNotNull, pDHI, pLocation, pMoreData);
		AI.changeDeclaredOwner(this);
		return AI;
	}

	// Display information ---------------------------------------------------------------

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final protected String getAttributeAccessToString(DataHolder.AccessKind pAKind, Type pAsType, 
			String pAttrName, Object pParam1, Object pParam2) {
		boolean hasParam2 =
				(pAKind == DataHolder.AccessKind.Config)
						|| (pAKind == DataHolder.AccessKind.GetMoreInfo);
		boolean hasParam1 = (pAKind == DataHolder.AccessKind.Set) || hasParam2;
		return this.getAttributeAccessToString(pAKind, pAsType, pAttrName) + "("
				+ (!hasParam1 ? "" : UObject.toString(pParam1))
				+ (!hasParam2 ? "" : (", " + UObject.toString(pParam2)) + ")") + ")";
	}

	/** Get the String display for this element with AsType e.g. (String)"Now" */
	final protected String getAttributeAccessToString(DataHolder.AccessKind pAKind, Type pAsType, String pAttrName) {
		Type   ThisType = (this instanceof DObject)?((DObject)this).getTheType():null;
		String ToString = ((ThisType == null)?this.getClass().toString():ThisType.toString()) + "@" + this.hashCode();
		return (((pAsType == null) || (pAsType == this))?"":"("+pAsType.toString()+")")
				+ ToString + "." + pAttrName + "->" + pAKind.toString().toLowerCase();
	}

	/** Display the operation access as a string */
	final protected String getOperationAccessToString(OperationSearchKind pOSKind, Type pAsType,
			Object pParam1, Object pParam2) {
		String Prefix = "";
		if((pAsType != null) && (pAsType != this)) Prefix = "("+pAsType.toString()+")";
		
		if(pOSKind == null) return Prefix + this.toString() + "." + pParam1.toString();
		switch(pOSKind) {
			case Direct:
			case BySignature:
				return Prefix+ this.toString() + "." + pParam1.toString();
			case ByParams:
			case ByTRefs:
				
				String ParamToString = null;
				if(pParam2 == null) ParamToString = "()";
				else if(pOSKind == OperationSearchKind.ByParams) {
					if(this.getEngine() == null) ParamToString = "(...)";
					else {
						StringBuffer SB = new StringBuffer();
						if(pParam2.getClass().isArray()) {
							for(int i = 0; i < UArray.getLength(pParam2); i++) {
								if(i != 0) SB.append(", ");
								SB.append(this.getEngine().getTypeManager().getTypeOf(UArray.get(pParam2, i)));
							}
						}
						ParamToString = SB.toString();
					}
				} else ParamToString = UArray.toString(pParam2, "(", ")", ",");
				
				return Prefix+ this.toString() + "." + pParam1.toString() + ParamToString;
			case ByNameInterface:
				return Prefix+ this.toString() + "."
						+ ExecInterface.Util.toString((ExecInterface)pParam2, (String)pParam1);
		}

		return null;
	}

}
