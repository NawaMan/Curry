package net.nawaman.curry;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.nawaman.curry.TKVariant.TVariant;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.TLCurrent.TRCurrent;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UClass;

/** Information about operation */
public interface OperationInfo extends Respond, HasSignature, Serializable, Cloneable {
	
	static public final OperationInfo[] EmptyOperationInfoArray = new OperationInfo[0];
	
	/** Returns the Accessibility of the operation */
	public Accessibility getAccessibility();
	
	/**
	 * Returns the signature of this operation.
	 * When the type is not initialized, this method will returns the declared signature. 
	 **/
	public ExecSignature getSignature();
	/**
	 * Returns the hash of this operation's signature for faster search.
	 * When the type is not initialized, this method will returns the hash of the declared signature.
	 **/
	public int getSignatureHash();
	
	/** Returns the declared signature of this operation */
	public ExecSignature getDeclaredSignature();
	
	/** Clone this OperationInfo */
	public OperationInfo makeClone();
	/** Returns the owner of this operation */
	public StackOwner    getOwner();
	/** Returns the type that own this operation (and that operation access will be done under it) */
	public Type          getOwnerAsType();

	/** Returns the current owner of this operation - in case that the operation is delegated or borrowed*/
	public StackOwner getCurrent();
	/** Returns the type that currently holds this operation (and that attribute access will be done under it) */
	public Type getCurrentAsType();
	
	// By Kind -------------------------------------------------------

	/** Returns this respond as a dynamically handled respond */
	public OIDynamic   asDynamic();
	/** Returns this respond as a native respond */
	public OINative    asNative();
	/** Returns this respond as a direct respond */
	public OIDirect    asDirect();
	/** Returns this respond as a delegate respond to a field */
	public OIDlgAttr   asDlgAttr();
	/** Returns this respond as a delegate respond to an object */
	public OIDlgObject asDlgObject();
	/** Checks if this respond is a resond for variant elements */
	public OIVariant   asVariant();
	
	// Constants -----------------------------------------------------------------------------------
	
	/** Information of a delegating attribute */
	static final public OperationInfo NoPermission = new EmptyOperation() {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		/** Returns the kinds of this respond */
		final public RKind getRKind() { return RKind.NoPermission; }
		/** Returns the more data of this operation */
		@Override
		final public MoreData getMoreData() { return null; }
		// Lock --------------------------------------------------------------------
		/** This method will limits the implementation to within this package. */
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) { return pLocalInterface; }
	};
	
	// -----------------------------------------------------------------------------------------------------------------
	// SubClasses ------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------
	
	// EmptyOperation --------------------------------------------------------------------------------------------------
	
	static abstract class EmptyOperation implements OperationInfo {
        
        private static final long serialVersionUID = -3009836901006887137L;
		
		/**{@inheritDoc}*/ @Override public Accessibility getAccessibility()     { return Accessibility.Private; }
		/**{@inheritDoc}*/ @Override public ExecSignature getSignature()         { return null; }
		/**{@inheritDoc}*/ @Override public int           getSignatureHash()     { return 0;    }
		/**{@inheritDoc}*/ @Override public ExecSignature getDeclaredSignature() { return null; }
		/**{@inheritDoc}*/ @Override public OperationInfo makeClone()            { return this; }
		/**{@inheritDoc}*/ @Override public StackOwner    getOwner()             { return null; }
		/**{@inheritDoc}*/ @Override public Type          getOwnerAsType()       { return null; }
		/**{@inheritDoc}*/ @Override public StackOwner    getCurrent()           { return null; }
		/**{@inheritDoc}*/ @Override public Type          getCurrentAsType()     { return null; }
		/**{@inheritDoc}*/ @Override public MoreData      getMoreData()          { return null; }
		
		// Satisfy Respond ---------------------------------------------------------------
		/**{@inheritDoc}*/ @Override final public RType getRType() { return RType.Operation;    }
		
		// By Kind -------------------------------------------------------

		/**{@inheritDoc}*/ @Override final public OINative    asNative()    { return null; }
		/**{@inheritDoc}*/ @Override final public OIDirect    asDirect()    { return null; }
		/**{@inheritDoc}*/ @Override final public OIDynamic   asDynamic()   { return (this instanceof OIDynamic  )?(OIDynamic)  this:null; }
		/**{@inheritDoc}*/ @Override final public OIDlgAttr   asDlgAttr()   { return (this instanceof OIDlgAttr  )?(OIDlgAttr)  this:null; }
		/**{@inheritDoc}*/ @Override final public OIDlgObject asDlgObject() { return (this instanceof OIDlgObject)?(OIDlgObject)this:null; }
		/**{@inheritDoc}*/ @Override final public OIVariant   asVariant()   { return null; }
		
		// By Type -------------------------------------------------------
		/**{@inheritDoc}*/ @Override final public boolean       isAttributeInfo() { return false; }
		/**{@inheritDoc}*/ @Override final public AttributeInfo asAttributeInfo() { return null;  }
		/**{@inheritDoc}*/ @Override final public boolean       isOperationInfo() { return true;  }
		/**{@inheritDoc}*/ @Override
		final public OperationInfo asOperationInfo() {
			return this;
		}

		/** Reset the Respond */
		public void reset() {}
		
		/**{@inheritDoc}*/ @Override 
		public String toString() {
			ExecSignature ES = this.getSignature();
			return (ES == null)?"any ()":ES.toString();
		}
		/**{@inheritDoc}*/ @Override 
		public String toDetail() {
			ExecSignature ES = this.getDeclaredSignature();
			return (ES == null)?"any ()":ES.toDetail();
		}
		
		/**{@inheritDoc}*/ @Override	
		public boolean is(Object O) {
			return this.equals(O);
		}
		/**{@inheritDoc}*/ @Override
		public int hash() {
			return this.hashCode();
		}
	}
	
	/** Simple implementation of Operation */
	static abstract class SimpleOperation extends EmptyOperation {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		private ExecSignature DclSignature;
		private Accessibility Access;
		
		private transient StackOwner Owner   = null;
		private transient StackOwner Current = null;
		
		transient ExecSignature Signature     = null;
		transient int           SignatureHash =    0;
		
		SimpleOperation(Accessibility pAccess, ExecSignature pSignature, MoreData pMoreData) {
			this.DclSignature = pSignature;
			this.Access       = (pAccess == null)?Accessibility.Private:pAccess;
			
			// Combine the two extra data
			this.DclSignature.ExtraData = MoreData.combineMoreData(pMoreData, this.DclSignature.ExtraData);
		}
		
		/**{@inheritDoc}*/ @Override
		public Accessibility getAccessibility() {
			return this.Access;
		}
		
		/**{@inheritDoc}*/ @Override
		public ExecSignature getSignature() {
			if(this.Signature == null) return this.DclSignature;
			return this.Signature;
		}
		
		/**{@inheritDoc}*/ @Override
		public ExecSignature getDeclaredSignature() {
			return this.DclSignature;
		}
		/**{@inheritDoc}*/ @Override
		public int getSignatureHash() {
			return (this.SignatureHash == 0)
			            ?(this.SignatureHash = this.getSignature().hash_WithoutParamNamesReturnType())
			            :this.SignatureHash;
		}

		/**{@inheritDoc}*/ @Override
		public void reset() {
			this.Signature     = null;
			this.SignatureHash =    0;
		}
		
		/**{@inheritDoc}*/ @Override
		public StackOwner getOwner() {
			return this.Owner;
		}
		/**{@inheritDoc}*/ @Override
		public Type getOwnerAsType() {
			return (this.Owner instanceof Type)?(Type)this.Owner:null;
		}

		/**{@inheritDoc}*/ @Override
		public StackOwner getCurrent() {
			return this.Current;
		}
		/**{@inheritDoc}*/ @Override
		public Type getCurrentAsType() {
			return (this.Current instanceof Type)?(Type)this.Current:null;
		}
		
		/**{@inheritDoc}*/ @Override
		public MoreData   getMoreData()    {
			return this.getSignature().getExtraData();
		}

		// BOT related -------------------------------------------------------------------------------------------------
		
		/**
		 * Changes the owner of this attribute
		 * Both Declared owner and current holder will be forcefully changed.
		 **/
		void changeDeclaredOwner(StackOwner pOwner) {
			this.Owner         = pOwner;
			this.Current       = pOwner;
			this.SignatureHash =      0;
		}
		/**
		 * Changes the owner of this attribute.
		 * The current holder is always changed followed the input value but the declared owner will only be changed when
		 *     it is previously null. 
		 **/
		void changeCurrentHolder(StackOwner pOwner) {	
			if(this.Current == pOwner) return;
			
			this.Current       = pOwner;
			this.SignatureHash =      0;
			if(this.Owner == null) this.Owner = pOwner;
		}

		/** Resolve the Signature of this Constructor */
		public boolean resolve(Engine pEngine) {
			if(this.Signature != null) return true;
			
			TypeRef CurrentTypeRef = this.getCurrentAsType().getTypeRef();
			TypeRef OwnerTypeRef   = this.getOwnerAsType()  .getTypeRef();

			// Save the DclSignature for a while
			this.Signature = this.DclSignature;
			// Update the signature 
			this.DclSignature = SimpleOperation.ChangeBaseTypeSignature(
			                     pEngine,
			                     CurrentTypeRef,
			                     OwnerTypeRef,
			                     this.DclSignature);
			
			// If the signature has at least one BOT TypeRef, flatten the signature. 
			if(this.Signature != this.DclSignature) {
				this.Signature = SimpleOperation.flatSignature(
				                     pEngine,
				                     CurrentTypeRef,
				                     this.Signature);
			}
			this.SignatureHash = 0;
			return true;
		}
		
		// Lock --------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		final public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
		
		// Utilities ---------------------------------------------------------------------------------------------------
		
		/** Returns the signature */
		static ExecSignature ChangeBaseTypeSignature(Engine pEngine, TypeRef pCurrentTypeRef, TypeRef pOwnerTypeRef,
				ExecSignature TheSignature) {
			return OperationInfo.SimpleOperation.ChangeBaseTypeSignature(pEngine, pCurrentTypeRef, pOwnerTypeRef,
					TheSignature, null, null);
		}

		/** Returns the signature */
		static ExecSignature ChangeBaseTypeSignature(Engine pEngine, TypeRef pCurrentTypeRef, TypeRef pOwnerTypeRef,
				ExecSignature TheSignature, String ErrorMessageWithCurrentType) {
			return OperationInfo.SimpleOperation.ChangeBaseTypeSignature(pEngine, pCurrentTypeRef, pOwnerTypeRef,
					TheSignature, ErrorMessageWithCurrentType, null);
		}
		// NOTE: pForceNewReturnTypeRef is only used with ConstructorInfo
		/** Returns the signature */
		static ExecSignature ChangeBaseTypeSignature(Engine pEngine, TypeRef pCurrentTypeRef, TypeRef pOwnerTypeRef,
				ExecSignature TheSignature, String ErrorMessageWithCurrentType, TypeRef pForceNewReturnTypeRef) {
			boolean IsDisallowCurrent = (ErrorMessageWithCurrentType != null);
			
			TypeRef TRef     = TheSignature.getReturnTypeRef();
			boolean HasTRBOT = (TRef instanceof TRBasedOnType);
			boolean HasTRCUR = (TRef instanceof     TRCurrent) && IsDisallowCurrent;
			
			if(!HasTRBOT && !HasTRCUR) {
				int PCount = TheSignature.getParamCount();
				for(int i = PCount; --i >= 0; ) {
					TRef    = TheSignature.getParamTypeRef(i);
					HasTRBOT = (TRef instanceof TRBasedOnType);
					HasTRCUR = (TRef instanceof     TRCurrent) && IsDisallowCurrent;
					if(HasTRBOT || HasTRCUR) break;
				}
			}
			
			// Throw an error when Current type is found but it is not allowed.
			if(HasTRCUR) throw new CurryError(String.format(ErrorMessageWithCurrentType, TheSignature));

			if(HasTRBOT || (pForceNewReturnTypeRef != null)) {
				HasTRBOT = false;
				
				// Has at least on TRBOT, clone and apply the owner change

				int       PCount        = TheSignature.getParamCount();
				TypeRef   ReturnTypeRef = TheSignature.getReturnTypeRef();
				TypeRef[] ParamTypeRefs = new TypeRef[PCount];
				String [] ParamNames    = new String [PCount];
				
				if(pForceNewReturnTypeRef != null) {
					ReturnTypeRef = pForceNewReturnTypeRef;
					HasTRBOT      = true;
					
				} else if(TRef instanceof TRBasedOnType) {
					// Change the Return TypeRef
					TRef = TheSignature.getReturnTypeRef();
					if(TRef instanceof TRBasedOnType) {
						TypeRef NTRef = TLBasedOnType.newTypeRef(pEngine, TRef, pCurrentTypeRef, pOwnerTypeRef, null, null);
						if(TRef != NTRef) {
							ReturnTypeRef = NTRef;
							HasTRBOT      = true;
						}
					}
				}
			
				// Change all the parameter
				int LastIndex = (PCount - 1);
				for(int i = PCount; --i >= 0; ) {
					ParamNames[i]    = TheSignature.getParamName(i);
					ParamTypeRefs[i] = (TRef = TheSignature.getParamTypeRef(i));

					// Reduce from array to none array (If a signature is VarArg, it will returns the last parameter
					//    type as an array).
					if((i == LastIndex) && TheSignature.isVarArgs())
						ParamTypeRefs[i] = (TRef = TheSignature.getLastVarArgParamTypeRef_As_NonArray());
					
					if(TRef instanceof TRBasedOnType) {
						TypeRef NTRef = TLBasedOnType.newTypeRef(pEngine, TRef, pCurrentTypeRef, pOwnerTypeRef, null, null);
						if(TRef != NTRef) {
							ParamTypeRefs[i] = NTRef;
							HasTRBOT = true;
						}
					}
				}
				
				if(HasTRBOT) {
					return ExecSignature.newSignature(
							TheSignature.getName(),
							ParamTypeRefs,
							ParamNames,
							TheSignature.isVarArgs(),
							ReturnTypeRef,
							TheSignature.getLocation(),
							TheSignature.getExtraData());
				}
			}
			
			return TheSignature;
		}
		
		/** Flats the signature */
		static ExecSignature flatSignature(Engine pEngine, TypeRef pCurrentTypeRef, ExecSignature TheSignature) {

			int     PCount   = TheSignature.getParamCount();
			TypeRef TRef     = TheSignature.getReturnTypeRef();
			boolean HasTRBOT = (TRef instanceof TRBasedOnType);
			
			if(!HasTRBOT) {
				for(int i = PCount; --i >= 0; ) {
					TRef     = TheSignature.getParamTypeRef(i);
					HasTRBOT = (TRef instanceof TRBasedOnType);
					if(!HasTRBOT) break;
				}
			}

			if(HasTRBOT) {
				HasTRBOT = false;
				TypeRef   ReturnTRef = TheSignature.getReturnTypeRef();
				TypeRef[] ParamTRefs = new TypeRef[PCount];
				
				if((TRef instanceof TRBasedOnType)) {
					// Change the Return TypeRef
					TRef = TheSignature.getReturnTypeRef();
					if(TRef instanceof TRBasedOnType) {
						ReturnTRef = TLBasedOnType.flatBaseOnType(pEngine, TRef, null, null);
						if(ReturnTRef != TRef) HasTRBOT = true;
					}
				}
			
				// Change all the parameter
				int LastIndex = (PCount - 1);
				for(int i = PCount; --i >= 0; ) {
					TRef = TheSignature.getParamTypeRef(i);

					// Reduce from array to none array (If a signature is VarArg, it will returns the last parameter
					//    type as an array).
					if((i == LastIndex) && TheSignature.isVarArgs())
						TRef = TheSignature.getLastVarArgParamTypeRef_As_NonArray();
					
					if(TRef instanceof TRBasedOnType) {
						ParamTRefs[i] = TLBasedOnType.flatBaseOnType(pEngine, TRef, null, null);
						if(ParamTRefs[i] != TRef) HasTRBOT = true;
						
					} else ParamTRefs[i] = TRef;
				}
				
				// Create a new one
				if(HasTRBOT) {
					// Parameter name
					String[] PNames = new String[PCount];
					for(int i = PCount; --i >= 0; ) PNames[i] = TheSignature.getParamName(i);
					// Recreate a new Parameter
					TheSignature = ExecSignature.newSignature(
					                TheSignature.getName(),
									ParamTRefs, PNames, TheSignature.isVarArgs(),
									ReturnTRef,
									TheSignature.getLocation(),
									TheSignature.getExtraData());
				}
			}
			
			return TheSignature;
		}
		
		// Serializable --------------------------------------------------------------------------------
		
		/** Write fields from non-serializable super */
		private void writeObject(ObjectOutputStream out) throws IOException {
			this.Owner   = null;
			this.Current = null;
			
			out.defaultWriteObject();
		}
	}
	

	/** Information of an operation that hold Executable as a data */
	static abstract class OIExec extends WrapperExecutable.Wrapper implements OperationInfo, Executable {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		/** Create a new concrete OperationInfo_Exec */
		OIExec(Accessibility pAccess, Executable pExec) {
			this.DclExec = pExec;
			this.Access  = (pAccess == null)?Accessibility.Private:pAccess;
			
			if(StandaloneOperation.isThereClosure(this.DclExec))
				throw new IllegalArgumentException("An operation of a StackOwner must not be a closure.");
		}
		/** Create a new abstract OperationInfo_Exec */
		OIExec(Accessibility pAccess, ExecSignature pSignature, Executable.ExecKind pKind) {
			this.Access = pAccess;
			if(pKind == null) pKind = ExecKind.SubRoutine;
			switch (pKind) {
				case Fragment  : this.DclExec = new CurryExecutable.CurryFragment(        pSignature, null);             break;
				case Macro     : this.DclExec = new CurryExecutable.CurryMacro(     null, pSignature, null, null, null); break;
				case SubRoutine: this.DclExec = new CurryExecutable.CurrySubRoutine(null, pSignature, null, null, null); break;
			}
		}
		
		final private Accessibility Access;
		
		Executable DclExec;
		
		private           Executable Exec          =  null;
		private transient StackOwner Owner         =  null;
		private transient StackOwner Current       =  null;
		private transient int        SignatureHash =     0;
				
		/**{@inheritDoc}*/ @Override
		protected Executable getWrapped() {
			return (this.Exec == null)
					?this.DclExec
					:this.Exec;
		}
		
		/** Returns the decalred executable */
		final public Executable getDeclaredExecutable() {
			return this.DclExec;
		}
		/** Changes the decalred executable */
		final Executable setDeclaredExecutable(Executable pDExec) {
			return this.DclExec = pDExec;
		}
		/**{@inheritDoc}*/ @Override
		public ExecSignature getDeclaredSignature() {
			return this.DclExec.getSignature();
		}
		/**{@inheritDoc}*/ @Override
		final public Location getLocation() {
			ExecSignature ES = this.getDeclaredExecutable().getSignature();
			if(ES == null) return null;
			return ES.getLocation();
		}
		
		/**{@inheritDoc}*/ @Override
		public int getSignatureHash() {
			return (this.SignatureHash > 0)
						? this.SignatureHash
						: (this.SignatureHash = this.getSignature().hash_WithoutParamNamesReturnType());
		}
		
		/**{@inheritDoc}*/ @Override
		public StackOwner getOwner() {
			return this.Owner;
		}
		/**{@inheritDoc}*/ @Override
		public Type getOwnerAsType() {
			return (this.Owner instanceof Type)?(Type)this.Owner:null;
		}
		
		/**{@inheritDoc}*/ @Override
		public StackOwner getCurrent() {
			return this.Owner;
		}
		/**{@inheritDoc}*/ @Override
		public Type getCurrentAsType() {
			return (this.Current instanceof Type)?(Type)this.Current:null;
		}
		
		
		/**{@inheritDoc}*/ @Override
		public MoreData getMoreData() {
			return this.getSignature().getExtraData();
		}
		/**{@inheritDoc}*/ @Override
		public OIDirect reCreate(Engine pEngine, Scope pFrozenScope) {
			throw new CurryError("Operation cannot be recreated.");
		}

		// BOT related -------------------------------------------------------------------------------------------------
		
		/**
		 * Changes the owner of this attribute
		 * Both Declared owner and current holder will be forcefully changed.
		 **/
		void changeDeclaredOwner(StackOwner pOwner) {
			this.Owner         = pOwner;
			this.Current       = pOwner;
			this.Exec          =   null;
			this.SignatureHash =      0;
		}
		/**
		 * Changes the owner of this attribute.
		 * The current holder is always changed followed the input value but the declared owner will only be changed when
		 *     it is previously null. 
		 **/
		void changeCurrentHolder(StackOwner pOwner) {
			if(this.Current == pOwner) return;
			
			this.Current       = pOwner;
			this.Exec          =   null;
			this.SignatureHash =      0;
			if(this.Owner == null) this.Owner = pOwner;
		}

		/** Resolve the Signature of this Constructor */
		public boolean resolve(Engine pEngine) {
			if(this.Exec != null) return true;
			
			TypeRef CurrentTypeRef = this.getCurrentAsType().getTypeRef();
			TypeRef OwnerTypeRef   = this.getOwnerAsType()  .getTypeRef();
						
			// Save the DclSignature for a while
			ExecSignature DclSignature = this.DclExec.getSignature();
			// Update the signature 
			ExecSignature NewSignature = SimpleOperation.ChangeBaseTypeSignature(
			                     pEngine,
			                     CurrentTypeRef,
			                     OwnerTypeRef,
			                     DclSignature);
			
			// If the signature has at least one BOT TypeRef, flatten the signature. 
			if((NewSignature != DclSignature) || (this.Exec == null)) {
				// Update the declared
				Executable EC = this.DclExec.clone();
				// Change the Signature
				if(      EC instanceof JavaExecutable)    ((JavaExecutable)   EC).Signature = NewSignature;
				else if (EC instanceof CurryExecutable)   ((CurryExecutable)  EC).Signature = NewSignature;
				else if (EC instanceof WrapperExecutable) ((WrapperExecutable)EC).Signature = NewSignature;
				//else if (EC instanceof OperationInfo)	    // OperationInfo cannot be used in OperationInfo 
				this.DclExec = EC;
				
				// Update the flatten
				NewSignature = SimpleOperation.flatSignature(
				                     pEngine,
				                     CurrentTypeRef,
				                     NewSignature);
				// Update the declared
				EC = this.DclExec.clone();
				// Change the Signature
				if(      EC instanceof JavaExecutable)    ((JavaExecutable)   EC).Signature = NewSignature;
				else if (EC instanceof CurryExecutable)   ((CurryExecutable)  EC).Signature = NewSignature;
				else if (EC instanceof WrapperExecutable) ((WrapperExecutable)EC).Signature = NewSignature;
				//else if (EC instanceof OperationInfo)	    // OperationInfo cannot be used in OperationInfo 
				this.Exec = EC;
			}
			
			this.Signature     = null;
			this.SignatureHash =    0;
			return true;
		}
		
		// Satisfy OperationInfo --------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public Accessibility getAccessibility() {
			return this.Access;
		}
		
		// Satisfy Respond ---------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		final public RType getRType() {
			return RType.Operation;
		}
		
		// By Type -------------------------------------------------------
		/**{@inheritDoc}*/ @Override final public boolean       isAttributeInfo() { return false; }
		/**{@inheritDoc}*/ @Override final public AttributeInfo asAttributeInfo() { return null;  }
		/**{@inheritDoc}*/ @Override final public boolean       isOperationInfo() { return true;  }
		/**{@inheritDoc}*/ @Override final public OperationInfo asOperationInfo() { return this;  }
		
		// By Kind -------------------------------------------------------

		/**{@inheritDoc}*/ @Override public OIDynamic   asDynamic()   { return null; }
		/**{@inheritDoc}*/ @Override public OINative    asNative()    { return null; }
		/**{@inheritDoc}*/ @Override public OIDirect    asDirect()    { return null; }
		/**{@inheritDoc}*/ @Override public OIDlgAttr   asDlgAttr()   { return null; }
		/**{@inheritDoc}*/ @Override public OIDlgObject asDlgObject() { return null; }
		/**{@inheritDoc}*/ @Override public OIVariant   asVariant()   { return null; }
		
		/**{@inheritDoc}*/ @Override
		final public String toString() {
			ExecSignature ES = (this.Exec == null) ? this.getSignature() : this.Exec.getSignature();
			String EStr = (ES == null)?"any ()":ES.toString();
			return EStr;
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			ExecSignature ES = this.getDeclaredSignature();
			String EStr = (ES == null)?"any ()":ES.toDetail();
			return EStr;
		}
		
		// Serializable --------------------------------------------------------------------------------
		
		/** Clear the owner before the object is being written */
		protected void clearBeforeWrite() throws IOException {
			this.Owner   = null;
			this.Current = null;
		}
	}
	
	// Dynamic ---------------------------------------------------------------------------------------------------------

	/** Information of a delegating attribute */
	static public class OIDynamic extends SimpleOperation implements Dynamic {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		OIDynamic(Accessibility pAccess, ExecSignature pSignature, MoreData pMoreData) {
			super(pAccess, pSignature, pMoreData);
		}
		
		/**{@inheritDoc}*/ @Override
		public OIDynamic makeClone() {
			OIDynamic OID = new OIDynamic(this.getAccessibility(), this.getDeclaredSignature(), null);
			OID.changeDeclaredOwner(this.getOwner());
			return OID;
		}
		
		// Satisfy Respond ---------------------------------------------------------------
		/** Returns the kinds of this respond */
		final public RKind getRKind() {
			return RKind.Dynamic;
		}

		/**{@inheritDoc}*/ @Override
		public String toString() {
			return "@Dynamic " + super.toString();
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			return "@Dynamic " + super.toDetail();
		}
	}

	/** Information of a native operation */
	static class OINative extends NativeExecutable.JavaMethodInvoke implements OperationInfo, Executable.SubRoutine,
									Respond, Native {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		OINative(Engine pEngine, Method pMethod) {
			super(pEngine, pMethod);
		}
		OINative(Method pMethod, ExecSignature pSignature) {
			super(pMethod, pSignature);
		}
		
		transient StackOwner Owner         = null;
		transient StackOwner Current       = null;
		transient int        SignatureHash =    0;
		
		/**{@inheritDoc}*/ @Override
		public int getSignatureHash() {
			return (this.SignatureHash == 0)
					?(this.SignatureHash = this.getSignature().hash_WithoutParamNamesReturnType())
					:this.SignatureHash;
		}

		/**{@inheritDoc}*/ @Override
		public ExecSignature getDeclaredSignature() {
			return this.getSignature();
		}
		
		/**{@inheritDoc}*/ @Override
		public StackOwner getOwner() {
			return this.Owner;
		}
		/**{@inheritDoc}*/ @Override
		public Type getOwnerAsType() {
			return (this.Owner instanceof Type)?(Type)this.Owner:null;
		}
		/**{@inheritDoc}*/ @Override
		public StackOwner getCurrent() {
			return this.Current;
		}
		/**{@inheritDoc}*/ @Override
		public Type getCurrentAsType() {
			return (this.Current instanceof Type)?(Type)this.Current:null;
		}
		
		/**{@inheritDoc}*/ @Override
		public MoreData getMoreData() {
			return this.getSignature().getExtraData();
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean isAbstract() {
			return Modifier.isAbstract(this.getMethod().getModifiers());
		}

		// Invoke ------------------------------------------------------------------------
		/** Executing this -  For internal to change */ @Override
		Object run(Context pContext, Object[] pParams) {
			throw new CurryError("OINative cannot be invoke directly." , pContext);
		}
		
		/**{@inheritDoc}*/ @Override
		public OINative makeClone() {
			return new OINative(this.getMethod(), this.getDeclaredSignature());
		}
		/**{@inheritDoc}*/ @Override
		public Accessibility getAccessibility() {
			return Accessibility.Public;
		}
		
		// Satisfy Respond ---------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		final public RType getRType() {
			return RType.Operation;
		}
		/**{@inheritDoc}*/ @Override
		final public RKind getRKind() {
			return RKind.Native;
		}
		
		// By Type -------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override final public boolean       isAttributeInfo() { return false; }
		/**{@inheritDoc}*/ @Override final public AttributeInfo asAttributeInfo() { return null;  }
		/**{@inheritDoc}*/ @Override final public boolean       isOperationInfo() { return true;  }
		/**{@inheritDoc}*/ @Override final public OperationInfo asOperationInfo() { return this;  }
		
		// By Kind -------------------------------------------------------

		/**{@inheritDoc}*/ @Override final public OIDynamic   asDynamic()   { return null; }
		/**{@inheritDoc}*/ @Override final public OINative    asNative()    { return this; }
		/**{@inheritDoc}*/ @Override final public OIDirect    asDirect()    { return null; }
		/**{@inheritDoc}*/ @Override final public OIDlgAttr   asDlgAttr()   { return null; }
		/**{@inheritDoc}*/ @Override final public OIDlgObject asDlgObject() { return null; }
		/**{@inheritDoc}*/ @Override final public OIVariant   asVariant()   { return null; }
		
		// Lock --------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		final public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
		
		/**{@inheritDoc}*/ @Override
		final public String toString() {
			ExecSignature ES = this.getSignature();
			String EStr = (ES == null)?"any ()":ES.toString();
			return "@Native " + EStr;
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			ExecSignature ES = this.getDeclaredSignature();
			String EStr = (ES == null)?"any ()":ES.toDetail();
			return "@Native " + EStr;
		}
		
		// Serializable --------------------------------------------------------------------------------
		
		/** Write fields from non-serializable super */
		private void writeObject(ObjectOutputStream out) throws IOException {
			this.Owner   = null;
			this.Current = null;
			
			out.defaultWriteObject();
		}
	}
	
	/** Information of a direct operation */
	static public class OIDirect extends OIExec implements Direct {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		/** Create a new concrete OperationInfo_Direct */
		OIDirect(Accessibility pAccess, Executable pExec, MoreData pMoreData) {
			super(pAccess, pExec);
			// Combine the two extra data
			this.DclExec.getSignature().ExtraData = MoreData.combineMoreData(pMoreData, this.DclExec.getSignature().ExtraData);
		}
		/** Create a new abstract OperationInfo_Direct */
		OIDirect(Accessibility pAccess, ExecSignature pSignature, Executable.ExecKind pKind, MoreData pMoreData) {
			super(pAccess, pSignature, pKind);
			// Combine the two extra data
			this.DclExec.getSignature().ExtraData = MoreData.combineMoreData(pMoreData, this.DclExec.getSignature().ExtraData);
		}
		
		boolean    IsAbstract = false;
		
		/**{@inheritDoc}*/ @Override
		public boolean isAbstract() {
			return this.IsAbstract;
		}
		
		/** Clone this attribute **/
		@Override public OIDirect clone() {
			return this.makeClone();
		}
		/** Clone this attribute **/
		public OIDirect makeClone() {
			OIDirect OI   = new OIDirect(this.getAccessibility(), this.DclExec.clone(), null);
			OI.IsAbstract = this.IsAbstract;
			OI.changeDeclaredOwner(this.getOwner());
			return OI;
		}
		
		// Satisfy Respond ---------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		final public RKind getRKind() {
			return RKind.Direct;
		}
		
		// By Kind -------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		final public OIDirect asDirect() {
			return this;
		}
		
		// Serializable --------------------------------------------------------------------------------
		
		/** Write fields from non-serializable super */
		private void writeObject(ObjectOutputStream out) throws IOException {
			this.clearBeforeWrite();
			out.defaultWriteObject();
		}
	}

	/** Information of a delegating operation */
	static public class OIDlgAttr extends SimpleOperation implements DlgAttr {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		OIDlgAttr(Accessibility pAccess, ExecSignature pSignature, String pDlgName, MoreData pMoreData) {
			super(pAccess, pSignature, pMoreData);
			this.DlgName = pDlgName;
		}

		String DlgName;

		/** Returns the field name that target of the delegation*/
		public String getDlgAttrName() {
			return this.DlgName;
		}
		/**{@inheritDoc}*/ @Override
		public OIDlgAttr clone() {
			return this.makeClone();
		}
		/**{@inheritDoc}*/ @Override
		public OIDlgAttr makeClone() {
			OIDlgAttr OI = new OIDlgAttr(this.getAccessibility(), this.getSignature().clone(), this.DlgName, null);
			OI.changeDeclaredOwner(this.getOwner());
			return OI;
		}

		/**{@inheritDoc}*/ @Override
		public boolean resolve(Engine pEngine) {
			if(this.Signature != null) return true;
			try {
				Type TOwner = this.getOwnerAsType();
				if(TOwner == null) return false;
				
				// Get the type of the delegate
				TypeRef ATRef = TOwner.searchObjectAttribute(pEngine, this.getDlgAttrName());
				if(ATRef == null) return false;

				// This value is a flatten value
				this.SignatureHash = 0;
				this.Signature     = TOwner.searchObjectOperation(
				                     pEngine,
				                     this.getDeclaredSignature().getName(),
				                     this.getDeclaredSignature());
				
				return (this.Signature != null);
			} catch (Exception E) { return false; }
		}
		
		// Satisfy Respond ---------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		final public RKind getRKind() {
			return RKind.DlgAttr;
		}

		/**{@inheritDoc}*/ @Override
		final public String toString() {
			ExecSignature ES = this.getSignature();
			String EStr = (ES == null)?"any ()":ES.toString();
			return "@Delegate " + EStr + " => this." + this.getDlgAttrName()+ "." + EStr;
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			ExecSignature ES = this.getDeclaredSignature();
			String EStr = (ES == null)?"any ()":ES.toDetail();
			return "@Delegate " + EStr + " => this." + this.getDlgAttrName()+ "." + EStr;
		}
	}

	/** Information of a delegating operation */
	static public class OIDlgObject extends SimpleOperation implements DlgObject {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		OIDlgObject(Accessibility pAccess, ExecSignature pSignature, Object pDlgObject, MoreData pMoreData) {
			super(pAccess, pSignature, pMoreData);
			this.Target = pDlgObject;
		}
		
		Object Target;
		
		/** Returns the field name that target of the delegation*/
		public Object getDlgObject() {
			return this.Target;
		}
		/**{@inheritDoc}*/ @Override
		public OIDlgObject clone() {
			return this.makeClone();
		}
		/**{@inheritDoc}*/ @Override
		public OIDlgObject makeClone() {
			OIDlgObject OI = new OIDlgObject(this.getAccessibility(), this.getDeclaredSignature().clone(), this.Target, null);
			OI.changeDeclaredOwner(this.getOwner());
			return OI;
		}

		/**{@inheritDoc}*/ @Override
		public boolean resolve(Engine pEngine) {
			if(this.Signature != null) return true;
			try {
				ExecSignature DSignature   = this.getDeclaredSignature();
				ExecSignature TheSignature = null;
				
				if(this.Target instanceof StackOwner) {
					TheSignature = ((StackOwner)this.Target).searchOperation(pEngine, DSignature.getName(), DSignature);
				} else {
					Class<?>[] PCls =  new Class<?>[DSignature.getParamCount()];
					for(int i = PCls.length; --i >= 0; ) {
						Class<?> Cls = DSignature.getParamTypeRef(i).getDataClass(pEngine);
						PCls[i] = (Cls == null)?Object.class:Cls;
					}
					
					Method M = UClass.getMethodByParamClasses(this.Target.getClass(), DSignature.getName(), false, PCls);
					if(M == null) return false;
					TheSignature = ExecSignature.newSignature(pEngine, M);
				}
				
				if(TheSignature != null) {
					this.Signature     = TheSignature;
					this.SignatureHash =            0;
				}
				
				return (this.Signature != null);
			} catch (Exception E) { return false; }
		}
		
		// Satisfy Respond ---------------------------------------------------------------
		/** Returns the kinds of this respond */
		final public RKind getRKind() { return RKind.DlgObject; }

		/**{@inheritDoc}*/ @Override
		final public String toString() {
			ExecSignature ES = this.getSignature();
			String EStr = (ES == null)?"any ()":ES.toString();
			return "@Delegate " + EStr + " => " + this.getDlgObject() + "." + EStr;
		}
		/**{@inheritDoc}*/ @Override
		final public String toDetail() {
			ExecSignature ES = this.getDeclaredSignature();
			String EStr = (ES == null)?"any ()":ES.toDetail();
			return "@Delegate " + EStr + " => " + this.getDlgObject() + "." + EStr;
		}
	}

	/** Information of a direct operation */
	static public class OIVariant extends OIExec implements Variant {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		/** Create a new abstract OperationInfo_Direct */
		OIVariant(ExecSignature pSignature, Executable.ExecKind pKind) {
			super(Accessibility.Public, pSignature, pKind);
		}
		
		/** Clone this attribute **/
		@Override public OIVariant clone() {
			return this.makeClone();
		}
		/** Clone this attribute **/
		public OIVariant makeClone() {
			OIVariant OI  = new OIVariant(this.DclExec.getSignature(), this.DclExec.getKind());
			OI.changeDeclaredOwner(this.getOwner());
			return OI;
		}
		
		// Satisfy Respond ---------------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		final public RKind getRKind() {
			return RKind.Variant;
		}
		
		// By Kind -------------------------------------------------------

		/**{@inheritDoc}*/ @Override
		final public OIVariant asVariant() {
			return this;
		}

		/**{@inheritDoc}*/ @Override
		void changeCurrentHolder(StackOwner pOwner) {
			if((pOwner != null) && !(pOwner instanceof TVariant))
				throw new CurryError(
					String.format("Only a variant type can be owner of a variant element (%s) <OperationInfo:654>.", this));
			
			super.changeCurrentHolder(pOwner);
		}
		
		// Serializable --------------------------------------------------------------------------------
		
		/** Write fields from non-serializable super */
		private void writeObject(ObjectOutputStream out) throws IOException {
			this.clearBeforeWrite();
			out.defaultWriteObject();
		}
	}
}
