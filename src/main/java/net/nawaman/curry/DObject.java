package net.nawaman.curry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;

import net.nawaman.curry.util.DataHolder;
import net.nawaman.util.UClass;

public class DObject extends StackOwner_Lite implements TypedData, InvocationHandler, DObjectStandalone {
	
	static public boolean USE_DYNAMIC_PROXY = true;
	
	protected DObject(Type pTheType) {
		if(pTheType == null) throw new NullPointerException();
		this.SOInfo = new ImmutableDObjectInfo(this, pTheType);
	}
	
	/**{@inheritDoc}*/ @Override
	public Engine getEngine() {
		return this.getDObjectInfo().getTheType().getEngine();
	}

	// Info ---------------------------------------------------------------------------------------

	// Make this object immutable
	static class ImmutableDObjectInfo extends DObjectInfo {
		Type TheType;
		ImmutableDObjectInfo(DObject pDO, Type pType) { super(pDO); this.TheType = pType; }
		public @Override boolean isImmutable()        { return true;         }
		public @Override Type    getTheType()         { return this.TheType; }
	}
	
	/** {@inheritDoc}*/ @Override
	public DObjectInfo getSOInfo() {
		return (ImmutableDObjectInfo)this.SOInfo;
	}
	/** Returns the TypeInfo object for this type */
	public DObjectInfo getDObjectInfo() {
		return (ImmutableDObjectInfo)this.SOInfo;
	}

	/** {@inheritDoc}*/ @Override
	public Type getTheType() {
		return this.getDObjectInfo().getTheType();
	}
	
	// Stand-alone --------------------------------------------------------------------------------
	
	Object AsNative = null;
	
	/** Returns the DObject as a native object (that implements interfaces) */
	final public Object getAsNative() {
		if(this.AsNative == null) {
			// Checks whether as Native class should be created or Dynamic proxy is prefered.
			if(!DObject.USE_DYNAMIC_PROXY) {
				Class<? extends DObjectStandalone> Cls = this.getTheType().getAsNativeClass(this.getEngine());
				if(Cls != null) {
					try {
						this.AsNative = Cls.getConstructor().newInstance();
						UClass.invokeObjectMethod(this.AsNative, "setAsDObject", new Object[] { this });
					} catch(Exception E) {
						this.AsNative = null;
					}
				}
			}
			if(this.AsNative == null) {
				// Create the proxy object
				Class<?>[] Interfaces = TKInterface.getJavaInterfaces(this.getEngine(), this.getTheType());
				if((Interfaces != null) && (Interfaces.length != 0)) {
					Interfaces = (new HashSet<Class<?>>(Arrays.asList(Interfaces))).toArray(UClass.EmptyClassArray);
					this.AsNative = Proxy.newProxyInstance(this.getClass().getClassLoader(), Interfaces, this);
				}
			}
		}
		return this.AsNative;
	}
	
	/** Returns the DObject as a native object (that implements interfaces) */
	final public Object getAsDObject() {
		return this;
	}
	
	/** This method will help limiting the implementation of this interface to be within this package. */
	public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) { return pLocalInterface;  }
	
	/** To satisfy Proxy */
	public Object invoke(Object pProxy, Method M, Object[] pArgs) throws Throwable {
		// Three default method
		Class<?>[] PTypes = M.getParameterTypes();
		if(PTypes.length == 0) {
			if("getAsNative" .equals(M.getName())) return pProxy;
			if("getAsDObject".equals(M.getName())) return this;
		} else if((PTypes.length == 1) && (PTypes[0] == Engine.LocalLock.class)) {
			return pArgs[0];
		}
		
		// The ES is automatically cache by ExecSignature so hopefully this is fastest as we can easily implemented.
		ExecSignature ES = ExecSignature.newSignature(this.getEngine(), M);
		return this.invoke(ES.getName(), ES.getInterface(), pArgs);
	}
	
	// Internal Services ---------------------------------------------------------------------------
	
	/** Returns the action record of the initializer caller */
	final protected ActionRecord getInitializerCaller(Context pContext) {
		if(pContext == null) return null;
		return pContext.getInitializerCaller();
	}
	

	// ---------------------------------------------------------------------------------------------
	// General Behaviours --------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Customize of StackOwner Kind ----------------------------------------------------------------	
	
	/**{@inheritDoc}*/ @Override
	protected String getOperKindName() {
		return this.getTheType().doData_getOperKindName(this);
	}
	/**{@inheritDoc}*/ @Override
	protected String getAttrKindName() {
		return this.getTheType().doData_getAttrKindName(this);
	}

	// Accessibility -------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected void validateAccessibility(Accessibility pAccess) {
		this.getTheType().doData_validateAccessibility(this, pAccess);
		return;
	}
	
	// ---------------------------------------------------------------------------------------------
	// General A/O related -------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	// Data ----------------------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected void doEnsureDHSpace(Context pContext) {
		if(this.Attrs == null) 
			throw new CurryError("The object is not yet initialized ("+(((DObject)this).getTheType())+"). <DObject:51>");
		
		super.doEnsureDHSpace(pContext);
	}
		
	// Not Null Attribute --------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public boolean isEnforceNotNull() {
		return this.getTheType().doData_isEnforceNotNull(this);
	}

	// Dynamic Handling ----------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected boolean isHandleDynamically() {
		return this.getTheType().doData_isHandleDynamically(this);
	}

	/**{@inheritDoc}*/ @Override
	protected Object dynamicHandling(Context pContext, Expression pInitiator,
			boolean pIsBlindCaller, Object[] pParams) {
		return super.dynamicHandling(pContext, pInitiator, pIsBlindCaller, pParams);
	}
	
	// Dynamic Delegation --------------------------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	protected int getDynamicDelegationCount(Context pContext) {
		return this.getTheType().doData_getDynamicDelegationCount(this, pContext);
	}
	/**{@inheritDoc}*/ @Override
	protected String getDynamicDelegation(Context pContext, int I) {
		return this.getTheType().doData_getDynamicDelegation(this, pContext, I);
	}
	/**{@inheritDoc}*/ @Override
	protected TypeRef getDynamicDelegationAsType(Context pContext, int I) {
		return this.getTheType().doData_getDynamicDelegationAsType(this, pContext, I);
	}

	// ---------------------------------------------------------------------------------------------
	// Appended A/O --------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	// Attribute Info --------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected int getAttrInfoCount() {
		return this.getTheType().doData_getAttrInfoCount(this);
	}
	
	/**{@inheritDoc}*/ @Override
	protected AttributeInfo getAttrInfoAt(int pIndex) {
		return this.getTheType().doData_getAttrInfoAt(this, pIndex);
	}
	
	/**{@inheritDoc}*/ @Override
	protected int getMaxDHIndex() {
		return this.getTheType().doData_getMaxDHIndex(this);
	}
	
	// ---------------------------------------------------------------------------------------------
	// Handle A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------
	
	// Get Respond ---------------------------------------------------------------------------------
	
	// Get A/O Local ---------------------------------------------------------------------
	// NOTE: Low level
	// NOTE: pContext must not be null here
	
	/**{@inheritDoc}*/ @Override
	 protected OperationInfo getOperationLocal(Context pContext, Type pAsType, ExecSignature pSignature) {
		return this.getTheType().doData_getOperationLocal(this, pContext, pAsType, pSignature);
	}
	/**{@inheritDoc}*/ @Override
	protected AttributeInfo getAttributeLocal(Context pContext, DataHolder.AccessKind pDHAK, Type pAsType, String pName) {
		return this.getTheType().doData_getAttributeLocal(this, pContext, pDHAK, pAsType, pName);
	}
	
	/**{@inheritDoc}*/ @Override
	protected OperationInfo[] getAllNonDynamicOperationInfo(Type pAsType) {
		return this.getTheType().doData_getAllNonDynamicOperationInfo(this, pAsType);
	}
	/**{@inheritDoc}*/ @Override
	protected AttributeInfo[] getAllNonDynamicAttributeInfo(Type pAsType) {
		return this.getTheType().doData_getAllNonDynamicAttributeInfo(this, pAsType);
	}

	// ---------------------------------------------------------------------------------------------
	// Search A/O ----------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------------------

	// Search A/O Local ------------------------------------------------------------------
	// NOTE: Low level
	// NOTE: pEngine must not be null here

	/**{@inheritDoc}*/ @Override
	protected TypeRef searchAttributeLocal(Engine pEngine, Type pAsType, String pName) {
		return this.getTheType().doData_searchAttributeLocal(this, pEngine, pAsType, pName);
	}

	/**{@inheritDoc}*/ @Override
	 protected ExecSignature searchOperationLocal(Engine pEngine, 
			                            OperationSearchKind pOSKind, Object pParam1, Object pParam2,
			                            Object pParam3) {
		return this.getTheType().doData_searchOperationLocal(this, pEngine, pOSKind, pParam1, pParam2, pParam3);
	}
	
	/**{@inheritDoc}*/ @Override
	protected ExecSignature searchOperation(Engine pEngine, OperationSearchKind pOSKind,
			boolean pIsSearchInDynamicDelegation, Object pParam1, Object pParam2, Object pParam3) {
		ExecSignature ES = super.searchOperation(pEngine, pOSKind, pIsSearchInDynamicDelegation, pParam1, pParam2, pParam3);
		if(ES == null) {
			// Not find in the object, try in type
			Type T = this.getTheType();
			ES = T.searchOperation(pEngine, pOSKind, pIsSearchInDynamicDelegation, pParam1, pParam2, pParam3);
		}
		return ES;
	}

	/**{@inheritDoc}*/ @Override
	protected TypeRef searchAttribute(Engine pEngine, boolean pIsSearchInDynamicDelegation, Type pAsType,
			String pName) {
		TypeRef TR = super.searchAttribute(pEngine, pIsSearchInDynamicDelegation, pAsType, pName);
		if(TR == null) {
			// Not find in the object, try in type
			Type T = this.getTheType();
			TR = T.searchAttribute(pEngine, pIsSearchInDynamicDelegation, pAsType, pName);
		}
		return TR;
	}

	// Respond not found -------------------------------------------------------

	/**{@inheritDoc}*/ @Override
	 protected Object doOperationRespondNotFound(Context pContext, Expression pInitiator, boolean pIsBlindCaller,
			Type pAsType, ExecSignature pSignature, Object[] pParams, boolean pIsAlreadyAdjusted) {
		
		// Not find in the object, try in type
		Type T = this.getTheType();
		OperationInfo OI = T.getOperation(pContext, pInitiator, pAsType, pSignature, null);
		
		// Still not found, throw an error
		if(OI == null) this.throwOperationRespondNotFound(pContext, pAsType, pSignature);
		
		return OI;
	}

	/**{@inheritDoc}*/ @Override
	protected Object doAttributeRespondNotFound(Context pContext, Expression pInitiator,
			DataHolder.AccessKind pAKind, Type pAsType, String pAttrName, Object pParam1, Object pParam2) {
		
		// Not find in the object, try in type
		Type T = this.getTheType();
		AttributeInfo AI = T.getAttribute(pContext, pInitiator, pAKind, pAsType, pAttrName, null);
		
		// Still not found, throw an error
		if(AI == null) this.throwAttributeRespondNotFound(pContext, pAKind, pAsType, pAttrName, pParam1, pParam2);
		
		return AI;
	}
	
	// Objectable ------------------------------------------------------------------------------------------------------

	/** Do the default toString */
	protected String doDefault_toString() {
		return this.getTheType().toString() + "@" + this.doDefault_hashCode();
	}

	/** Do the default toDetail */
	protected String doDefault_toDetail(Context pContext, HashSet<Object> pObjects) {
		if(pObjects == null) pObjects = new HashSet<Object>();
		if(pContext == null) {
			pContext = this.getEngine().newRootContext();
			pContext = new Context.ContextStackOwner(pContext, "hash" + this.getTheType().toString(), false, null, this,
							this.getTheType(), true, null);
		}
		
		// Ensure no repeat to avoid recursive
		if(pObjects.contains(this)) return this.doDefault_toString();
		pObjects.add(this);
		
		AttributeInfo[] AIs = this.getAllNonDynamicAttributeInfo(null);
		
		StringBuffer ToDetail = new StringBuffer();
		ToDetail.append(this.getTheType()).append("@").append(this.hashCode()).append(" {");
		
		if(AIs != null) {
			ToDetail.append("\n");
			HashSet<String> ANames = new HashSet<String>();
			for(int i = AIs.length; --i >= 0; ) {
				Object O = this.getAttrData(pContext, null, AIs[i].getOwnerAsType(), AIs[i].getName(), null);

				String ToD = null;
				// Hind the data
				if(!AIs[i].getReadAccessibility().isPublic()) ToD = "<<non-public value>>";
				else {
					if(!(O instanceof DObject)) ToD = this.getEngine().toDetail(null, O);
					else {
						// See if the operation is this method (native and belong to DObject)
						OperationInfo OI = ((DObject)O).getOperation(pContext, null, null, ExecSignature.getES_Hash());
						if((OI instanceof OperationInfo.OINative) &&
							(((OperationInfo.OINative)OI).getMethod().getDeclaringClass() == DObject.class))
							
							 ToD = ((DObject)O).doDefault_toDetail(null, pObjects);
						else ToD = ((DObject)O).toDetail();
					}
				}
				ToD = ToD.replaceAll("\n", "\n\t");
				ToDetail.append("\t");
				if(ANames.contains(AIs[i].getName())) {
					ToDetail.append("<").append(AIs[i].getOwnerAsType().toString()).append(">");
				} else ANames.add(AIs[i].getName());
				ToDetail.append(AIs[i].getName()).append(" = ").append(ToD).append(";\n");
			}
		}
		ToDetail.append("}");
		return ToDetail.toString();
	}

	/** Do the default is */
	protected boolean doDefault_is(Object O) {
		if(null          == O) return false;
		if(this          == O) return true;
		if(this.AsNative == O) return true;
		return false;
	}

	/** Do the default equals */
	protected boolean doDefault_equals(Object O) {
		if(null          == O) return false;
		if(this          == O) return true;
		if(this.AsNative == O) return true;
		if(!(O instanceof DObject)) return this.hashCode() == O.hashCode();
		return this.hash() == ((DObject)O).hash();
	}

	/** Do the default hash */
	protected int doDefault_hash(Context pContext, HashSet<Object> pObjects) {
		if(pObjects == null) pObjects = new HashSet<Object>();
		if(pContext == null) {
			pContext = this.getEngine().newRootContext();
			pContext = new Context.ContextStackOwner(pContext, "hash" + this.getTheType().toString(), false, null, this,
							this.getTheType(), true, null);
		}
		
		// Ensure no repeat to avoid recursive
		if(pObjects.contains(this)) return super.hashCode();
		pObjects.add(this);
		
		int Hash = 0;
		AttributeInfo[] AIs = this.getAllNonDynamicAttributeInfo(null);
		if(AIs == null) return Hash;
		
		for(int i = AIs.length; --i >= 0; ) {
			Object O = this.getAttrData(pContext, null, AIs[i].getOwnerAsType(), AIs[i].getName(), null);
			if(O == null) continue;
			
			int h = 0;
			
			if(!(O instanceof DObject)) h = this.getEngine().hash(null, O);
			else {
				// See if the operation is this method (native and belong to DObject)
				OperationInfo OI = ((DObject)O).getOperation(pContext, null, null, ExecSignature.getES_Hash());
				if((OI instanceof OperationInfo.OINative) &&
					(((OperationInfo.OINative)OI).getMethod().getDeclaringClass() == DObject.class))
					 h = ((DObject)O).doDefault_hash(null, pObjects);
				else h = ((DObject)O).hash();
			}
			
			// TODO - Should do non-public field hash scramble
			//if(!AIs[i].getReadAccessibility().isPublic()) h = h*100;
			Hash += AIs[i].getName().hashCode()*h;
		}
		return Hash;
	}

	/** Do the default hashCode */
	protected int doDefault_hashCode() {
		return super.hashCode();
	}
	
	// Native one so that sub classes can refer to this ----------------------------------------------------------------

	/** Do the default toString */
	static public String  doDObject_Default_toString(DObject pDO)           { return pDO.doDefault_toString();           }
	/** Do the default toDetail */
	static public String  doDObject_Default_toDetail(DObject pDO)           { return pDO.doDefault_toDetail(null, null); }
	/** Do the default is */
	static public boolean doDObject_Default_is(      DObject pDO, Object O) { return pDO.doDefault_is(O);                }
	/** Do the default equals */
	static public boolean doDObject_Default_equals(  DObject pDO, Object O) { return pDO.doDefault_equals(O);            }
	/** Do the default hash */
	static public int     doDObject_Default_hash(    DObject pDO)           { return pDO.doDefault_hash(null, null);     }
	/** Do the object default hashCode */
	static public int     doDObject_Default_hashCode(DObject pDO)           { return pDO.doDefault_hashCode();           }
	
	// The actual visible one ---------------------------------------------------------------------
	// This method helps merging native and curry methods for toString, toDetail, hash, hashCode, is and equals
	
	/**{@inheritDoc}*/ @Override
	public String toString() {
		// See if the operation is this method (native and belong to DObject)
		OperationInfo OI = this.getOperation(null, null, null, ExecSignature.getES_ToString());
		if((OI instanceof OperationInfo.OINative) &&
			(((OperationInfo.OINative)OI).getMethod().getDeclaringClass() == DObject.class))
			// If so, run the native
			return this.doDefault_toString();
		
		// Else, run the StackOwner's one
		return (String)this.invoke(ExecSignature.getES_ToString(), (Object[])null);
	}

	/**{@inheritDoc}*/ @Override
	public String toDetail() {
		// See if the operation is this method (native and belong to DObject)
		OperationInfo OI = this.getOperation(null, null, null, ExecSignature.getES_ToDetail());
		if((OI instanceof OperationInfo.OINative) && (((OperationInfo.OINative)OI).getMethod().getDeclaringClass() == DObject.class))
			// If so, run the native
			return this.doDefault_toDetail(null, null);
		
		// Else, run the StackOwner's one
		return (String)this.invoke(ExecSignature.getES_ToDetail(), (Object[])null);
	}

	/**{@inheritDoc}*/ @Override
	public boolean is(Object O) {
        if (this == O)
            return true;
        
		// See if the operation is this method (native and belong to DObject)
		OperationInfo OI = this.getOperation(null, null, null, ExecSignature.getES_Is());
		if((OI instanceof OperationInfo.OINative) && (((OperationInfo.OINative)OI).getMethod().getDeclaringClass() == DObject.class))
			// If so, run the native
			return Boolean.TRUE.equals(this.doDefault_is(O));
		
		// Else, run the StackOwner's one
		return Boolean.TRUE.equals(this.invoke(ExecSignature.getES_Is(), O));
	}

	/**{@inheritDoc}*/ @Override
	public boolean equals(Object O) {
	    if (this == O)
	        return true;
		// See if the operation is this method (native and belong to DObject)
		OperationInfo OI = this.getOperation(null, null, null, ExecSignature.getES_Equal());
		if((OI instanceof OperationInfo.OINative) && (((OperationInfo.OINative)OI).getMethod().getDeclaringClass() == DObject.class))
			// If so, run the native
			return Boolean.TRUE.equals(this.doDefault_equals(O));
		
		// Else, run the StackOwner's one
		return Boolean.TRUE.equals(this.invoke(ExecSignature.getES_Equal(), O));
	}

	/**{@inheritDoc}*/ @Override
	public int hash() {
		// See if the operation is this method (native and belong to DObject)
		OperationInfo OI = this.getOperation(null, null, null, ExecSignature.getES_Hash());
		if((OI instanceof OperationInfo.OINative) && (((OperationInfo.OINative)OI).getMethod().getDeclaringClass() == DObject.class))
			// If so, run the native
			return this.doDefault_hash(null, null);
		
		// Else, run the StackOwner's one
		Integer I = (Integer)this.invoke(ExecSignature.getES_Hash(), (Object[])null);
		if(I == null) return 0;
		return I.intValue();
	}

	/**{@inheritDoc}*/ @Override
	public int hashCode() {
		// See if the operation is this method (native and belong to DObject)
		OperationInfo OI = this.getOperation(null, null, null, ExecSignature.getES_HashCode());
		if((OI instanceof OperationInfo.OINative) && (((OperationInfo.OINative)OI).getMethod().getDeclaringClass() == DObject.class))
			// If so, run the native
			return this.doDefault_hashCode();
		
		// Else, run the StackOwner's one
		Integer I = (Integer)this.invoke(ExecSignature.getES_HashCode(), (Object[])null);
		if(I == null) return 0;
		return I.intValue();
	}

}
