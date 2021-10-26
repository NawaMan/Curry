package net.nawaman.curry;

import java.io.*;
import java.lang.reflect.*;

import net.nawaman.curry.TKVariant.TVariant;
import net.nawaman.curry.TLBasedOnType.TRBasedOnType;
import net.nawaman.curry.util.*;
import net.nawaman.util.*;

/** Information of an attribute */
abstract public class AttributeInfo implements Respond, Serializable {
    
    private static final long serialVersionUID = 8605071645446256069L;
	
	static public final AttributeInfo[] EmptyAttributeInfoArray = new AttributeInfo[0];
	
	AttributeInfo(Accessibility pReadAcc, Accessibility pWriteAcc, Accessibility pConfigAcc, String pName,
			MoreData pMoreData) {
		this.Name      = pName;
		this.MoreData  = pMoreData;
		this.ReadAcc   = (pReadAcc   == null)?Accessibility.Private:pReadAcc; 
		this.WriteAcc  = (pWriteAcc  == null)?Accessibility.Private:pWriteAcc;
		this.ConfigAcc = (pConfigAcc == null)?Accessibility.Private:pConfigAcc;
		
		if(this.MoreData != null) this.MoreData.toFreeze();
	}
	
	final String   Name;
	final MoreData MoreData;
	private transient StackOwner Owner    = null;	// The declared owner of the attribute where is first declared
	private transient StackOwner Current  = null;	// The current holder of this attribute.
	private transient int        NameHash =    0;	// For fast indexing
	
	/** Returns the name of the attribute */
	public String getName() {
		return this.Name;
	}
	/** Returns the hash of the name for fast search */
	public int getNameHash() {
		return (this.NameHash == 0)?(this.NameHash = UString.hash(this.Name)):this.NameHash;
	}
	
	/** Checks if the value of this attribute is allowed to be null */
	public boolean isNotNull() {
		return false;
	}
	
	/** Returns the owner of this attribute */
	public StackOwner getOwner() {
		return this.Owner;
	}
	/** Returns the type that own this attribute (and that attribute access will be done under it) */
	public Type getOwnerAsType() {
		return (this.Owner instanceof Type)?(Type)this.Owner:null;
	}
	
	/** Returns the current owner of this attribute - in case that the attribute is delegated or borrowed*/
	public StackOwner getCurrent() {
		return this.Current;
	}
	/** Returns the type that currently holds this attribute (and that attribute access will be done under it) */
	public Type getCurrentAsType() {
		return (this.Current instanceof Type)?(Type)this.Current:null;
	}

	// The type of the attribute value which will be resolved at the time its current holder is initialized
	// NOTE: This will be the same value as its declared type ref in the case of attributes that does not belong to a
	//           type or object with a type. In other word, this TypeRef is designed to help implementing
	//           BaseOnType-TypeRef mechanism 
	TypeRef TypeRef;
	/** Returns the type reference of the attribute-value type */
	final public TypeRef getTypeRef() {
		return (this.TypeRef == null)?TKJava.TAny.getTypeRef():this.TypeRef;
	}

	/** Checks it the Attribute have a TypeRef (not a null) */
	abstract public boolean hasTypeRef();
	
	/** Returns the attribute-value TypeRef that is initially declared */
	abstract public TypeRef getDeclaredTypeRef();
	/** Changes the attribute-value TypeRef that is initially declared */
	abstract void setDeclaredTypeRef(TypeRef pTRef);
	
	/** Resolve the TypeRef of this Attribute */
	public boolean resolve(Engine pEngine) {
		if(this.TypeRef != null) return true;
		this.TypeRef = this.getDeclaredTypeRef();
		if(this.TypeRef instanceof TLBasedOnType.TRBasedOnType) {

			TypeRef CurrentTypeRef = this.getCurrentAsType().getTypeRef();
			TypeRef OwnerTypeRef   = this.getOwnerAsType()  .getTypeRef();
			
			if(CurrentTypeRef instanceof TRBasedOnType) {
				TypeRef NewCurrentTypeRef = ((TRBasedOnType)CurrentTypeRef).flatType(pEngine, null, null);
				if(NewCurrentTypeRef != CurrentTypeRef)
					this.Current = pEngine.getTypeManager().getTypeFromRefNoCheck(null, (CurrentTypeRef = NewCurrentTypeRef));
			}
			if(OwnerTypeRef instanceof TRBasedOnType) {
				TypeRef NewOwnerTypeRef = ((TRBasedOnType)OwnerTypeRef).flatType(pEngine, null, null);
				if(NewOwnerTypeRef != OwnerTypeRef)
					this.Owner = pEngine.getTypeManager().getTypeFromRefNoCheck(null, (OwnerTypeRef = NewOwnerTypeRef));
			}
			
			this.TypeRef = TLBasedOnType.newTypeRef(pEngine,
							this.TypeRef,
							CurrentTypeRef,
							OwnerTypeRef,
							null,
							null);
			
			// Update the declared TypeRef
			if(this.TypeRef != this.getDeclaredTypeRef()) this.setDeclaredTypeRef(this.TypeRef);
			// this.TypeRef contains the flat value
			this.TypeRef = TLBasedOnType.flatBaseOnType(pEngine, this.TypeRef, null, null);
		}
		return true;
	}
	
	/**
	 * Changes the owner of this attribute
	 * 
	 * Both Declared owner and current holder will be forcefully changed.
	 **/
	void changeDeclaredOwner(StackOwner pOwner) {
		this.Owner   = pOwner;
		this.Current = pOwner;
	}
	/**
	 * Changes the owner of this attribute.
	 * 
	 * The current holder is always changed followed the input value but the declared owner will only be changed when
	 *     it is previously null. 
	 **/
	void changeCurrentHolder(StackOwner pOwner) {
		if(this.Current == pOwner) return;
		
		this.Current = pOwner;
		
		if(this.Owner != null) return;
		this.Owner = pOwner;
	}
	
	/** Returns the more data of this attribute */
	public MoreData getMoreData() {
		return (this.MoreData == null)?net.nawaman.curry.util.MoreData.Empty:this.MoreData;
	}
	
	/** Clone this AttributeInfo */ @Override
	public AttributeInfo clone() {
		return this.makeClone();
	}
	
	/** Clone this AttributeInfo */
	abstract public AttributeInfo makeClone();
	
	// Accessibility ---------------------------------------------------------------------
	final Accessibility ReadAcc;
	final Accessibility WriteAcc;
	final Accessibility ConfigAcc;
	/** Returns the Accessibility for reading the attribute */
	public Accessibility getReadAccessibility()   { return this.ReadAcc;   }
	/** Returns the Accessibility for writing the attribute */
	public Accessibility getWriteAccessibility()  { return this.WriteAcc;  }
	/** Returns the Accessibility for configuring the attribute */
	public Accessibility getConfigAccessibility() { return this.ConfigAcc; }
	
	// Satisfy Respond -------------------------------------------------------------------

	/** Returns the types of this respond */ @Override
	public RType getRType() {
		return RType.Attribute;
	}
	
	// By Type -------------------------------------------------------
	
	/** Checks if this respond is an attribute */
	final public boolean       isAttributeInfo() { return  true; }
	/** Returns this respond as an attribute */
	final public AttributeInfo asAttributeInfo() { return  this; }
	/** Checks if this respond is an operation */
	final public boolean       isOperationInfo() { return false; }
	/** Returns this respond as an operation */
	final public OperationInfo asOperationInfo() { return  null; }
	
	// By Kind -------------------------------------------------------
	/** Returns this respond as a dynamically handled respond */
	final public AIDynamic   asDynamic()   { return (this instanceof AIDynamic)  ?(AIDynamic)  this:null; }
	/** Returns this respond as a direct respond */
	final public AINative    asNative()    { return (this instanceof AINative)   ?(AINative)   this:null; }
	/** Returns this respond as a direct respond */
	final public AIDirect    asDirect()    { return (this instanceof AIDirect)   ?(AIDirect)   this:null; }
	/** Returns this respond as a delegate respond to a field */
	final public AIDlgAttr   asDlgAttr()   { return (this instanceof AIDlgAttr)  ?(AIDlgAttr)  this:null; }
	/** Returns this respond as a delegate respond to an object */
	final public AIDlgObject asDlgObject() { return (this instanceof AIDlgObject)?(AIDlgObject)this:null; }
	/** Checks if this respond is a resond for variant elements */
	final public AIVariant   asVariant()   { return (this instanceof AIVariant)  ?(AIVariant)  this:null; }
	
	/**{@inheritDoc}*/ @Override
	public String toString() {
		return this.getName();
	}
	/**{@inheritDoc}*/ @Override
	public String toDetail() {
		return this.getName() + ":" + this.getDeclaredTypeRef();
	}
	/**{@inheritDoc}*/ @Override	
	public boolean is(Object O) {
		return this.equals(O);
	}
	/**{@inheritDoc}*/ @Override
	public int hash() {
		return this.hashCode();
	}
	
	// Serializable --------------------------------------------------------------------------------
	
	/** Write fields from non-serializable super */
	private void writeObject(ObjectOutputStream out) throws IOException {
		this.Owner   = null;
		this.Current = null;
		
		out.defaultWriteObject();
	}
	
	// Constants ----------------------------------------------------------------------------------

	/** Information of a delegating attribute */
	static final public AttributeInfo NoPermission = new AttributeInfo(null, null, null, null, null) {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		/**{@inheritDoc}*/ @Override public RKind         getRKind()                        { return RKind.NoPermission; }
		/**{@inheritDoc}*/ @Override public AttributeInfo makeClone()                       { return this; }
		/**{@inheritDoc}*/ @Override public boolean       resolve(Engine pEngine)           { return true; }
		/**{@inheritDoc}*/ @Override public TypeRef       getDeclaredTypeRef()              { return null; }
		/**{@inheritDoc}*/ @Override        void          setDeclaredTypeRef(TypeRef pTRef) {}
		/**{@inheritDoc}*/ @Override public boolean       hasTypeRef() { return true; }
		// Lock --------------------------------------------------------------------
		/** This method will limits the implementation to within this package. */
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	};
	
	// SubClasses ----------------------------------------------------------------------------------

	/** Information of a delegating attribute */
	static final public class AIDynamic extends AttributeInfo implements Dynamic {
	    
        private static final long serialVersionUID = 8605071645446256069L;
        
        AIDynamic(Accessibility pReadAcc, Accessibility pWriteAcc, Accessibility pConfigAcc, String pName, TypeRef pTRef,
				MoreData pMoreData) {
			super(pReadAcc, pWriteAcc, pConfigAcc, pName, pMoreData);
			
			this.DeclareTypeRef = pTRef;
			if(this.DeclareTypeRef == null) this.DeclareTypeRef = TKJava.TAny.getTypeRef();
		}
		
		// The declared attribute type (before effected by the current holder of the attribute)
		TypeRef DeclareTypeRef = null;

		/**{@inheritDoc}*/ @Override
		void setDeclaredTypeRef(TypeRef pTRef) {
			this.DeclareTypeRef = pTRef;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getDeclaredTypeRef() {
			return this.DeclareTypeRef;
		}
		/**{@inheritDoc}*/ @Override
		public boolean hasTypeRef() {
			return this.DeclareTypeRef != null;
		}
		
		// Satisfy AttributeInfo -----------------------------------------------
		
		/** Returns the kinds of this respond */
		final public RKind getRKind() {
			return RKind.Dynamic;
		}
		/**{@inheritDoc}*/ @Override
		public AIDynamic makeClone() {
			AIDynamic AI = new AIDynamic(
								this.ReadAcc, this.WriteAcc, this.ConfigAcc,
								this.Name, this.TypeRef.clone(),
								this.MoreData);
			AI.changeDeclaredOwner(this.getOwner());
			return AI;
		}
		/**{@inheritDoc}*/ @Override
		public String toString() {
			return String.format("@Dynamic %s:%s ", this.Name, this.getTypeRef());
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			return String.format("@Dynamic %s:%s ", this.Name, this.getDeclaredTypeRef());
		}
		// Lock --------------------------------------------------------------------
		/** This method will limits the implementation to within this package. */
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
	
	/** Information of a java attribute */
	static final public class AINative extends AttributeInfo implements Native {
        
        private static final long serialVersionUID = 8605071645446256069L;
		
		static MoreData prepareMoreData(Field pField) {
			if(pField == null) return null;
			MoreData MD = new MoreData();
			MD.setData("Field", pField.getDeclaringClass().getCanonicalName() + "." + pField.getName());
			return MD;
		}
		
		AINative(Engine pEngine, Field pField) {
			super(Accessibility.Public, Accessibility.Public, Accessibility.Public, pField.getName(), prepareMoreData(pField));
			this.Field   = pField;
			this.TypeRef = (pEngine == null)
			                    ?TKJava.Instance.getTypeByClass(pEngine, null, this.Field.getType()).getTypeRef()
			                    :pEngine.getTypeManager().getTypeOfTheInstanceOf(this.Field.getType()).getTypeRef();
		}
		private AINative(Field pField, TypeRef pTypeRef) {
			super(Accessibility.Public, Accessibility.Public, Accessibility.Public, pField.getName(), null);
			this.Field   = pField;
			this.TypeRef = pTypeRef;
		}
		
		final Field Field;
		
		/** Returns the Field of this attribute */
		public Field getField() {
			return this.Field;
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean isAbstract() {
			return false;
		}
		
		// Satisfy AttributeInfo -----------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public RKind getRKind() {
			return RKind.Native;
		}

		/**{@inheritDoc}*/ @Override
		void setDeclaredTypeRef(TypeRef pTRef) {}
		/**{@inheritDoc}*/ @Override
		public TypeRef getDeclaredTypeRef() {
			return this.TypeRef;
		}
		/**{@inheritDoc}*/ @Override
		public boolean hasTypeRef() {
			return this.TypeRef != null;
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean resolve(Engine pEngine) {
			return true;
		}
		
		/**{@inheritDoc}*/ @Override
		public AINative makeClone() {
			return new AINative(this.Field, this.TypeRef.clone());
		}
		
		/**{@inheritDoc}*/ @Override
		public String toString() {
			return String.format("@Native %s:%s ", this.Name , this.getTypeRef());
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			return String.format("@Native %s:%s ", this.Name , this.getDeclaredTypeRef());
		}
		
		// Lock --------------------------------------------------------------------
		/** This method will limits the implementation to within this package. */
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
	/** Information of a direct attribute */
	static final public class AIDirect extends AttributeInfo implements Direct {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		AIDirect(Accessibility pReadAcc, Accessibility pWriteAcc, Accessibility pConfigAcc, String pName,
				boolean pIsNotNull, DataHolderInfo pDHInfo, Location pLocation, MoreData pMoreData) {
			super(pReadAcc, pWriteAcc, pConfigAcc, pName, pMoreData);
			this.IsNotNull = pIsNotNull;
			this.DHInfo    = pDHInfo;
			this.Location  = pLocation;
			// Abstract value
			this.IsAbstract = (this.DHInfo.getDHFactoryName() == null);
			this.TypeRef    = null;
		}
		final boolean  IsNotNull;
		final Location Location;
		final boolean  IsAbstract;
		
		DataHolderInfo DHInfo;

		transient int DHIndex = -1;
		
		/** Returns the DataHolder Information of this attribute */
		public DataHolderInfo getDHInfo() {
			return this.DHInfo;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getDeclaredTypeRef() {
			return this.DHInfo.getTypeRef();
		}
		/**{@inheritDoc}*/ @Override
		public boolean hasTypeRef() {
			return this.DHInfo.hasTypeRef();
		}

		/**{@inheritDoc}*/ @Override
		void setDeclaredTypeRef(TypeRef pTRef) {
			// Recreate the DHInfo
			this.DHInfo = new DataHolderInfo(
							pTRef,
							this.DHInfo.getIValue(),
							this.DHInfo.getDHFactoryName(),
							this.DHInfo.isReadable(),
							this.DHInfo.isWritable(),
							this.DHInfo.isSet(),
							this.DHInfo.isExpression(),
							this.DHInfo.getMoreInfo());
		}
		
		// DH index -------------------------------------------------------------------------------
		
		/** Returns the DataHolder index of this attribute */
		public int  getDHIndex() {
			return this.DHIndex;
		}
		/** Set the value of the DHIndex */
		protected boolean setDHIndex(int pIndex) {
			if(this.DHIndex != -1) { this.DHIndex = pIndex; return true; }
			else return (this.DHIndex == pIndex);
		}
		/** Returns the Location that this attribute is declared */
		public Location getLocation() {
			return this.Location;
		}
		/** Checks if this DataHolder is abstract */
		public boolean isAbstract() {
			return this.IsAbstract;
		}
		
		
		// To satisfy Respond --------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public boolean  isNotNull() {
			return this.IsNotNull;
		}
		/** Returns the kinds of this respond */
		public RKind getRKind() {
			return RKind.Direct;
		}
		
		/**{@inheritDoc}**/ @Override
		public AIDirect makeClone() {
			AIDirect AI = new AIDirect(this.ReadAcc, this.WriteAcc, this.ConfigAcc, this.Name, this.IsNotNull,
					this.DHInfo.clone(), this.Location, this.MoreData);
			AI.changeDeclaredOwner(this.getOwner());
			return AI;
		}
		
		/**{@inheritDoc}*/ @Override
		public String toString() {
			TypeRef TRef = this.getTypeRef();
			if(TRef == null) TRef = this.getDHInfo().getTypeRef();
			return this.getName() + ":" + TRef;
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			TypeRef TRef = this.getTypeRef();
			if(TRef == null) TRef = this.getDHInfo().getTypeRef();
			return this.getName() + ":" + TRef;
		}
		
		// Lock --------------------------------------------------------------------
		/** This method will limits the implementation to within this package. */
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
	/** Information of a delegating attribute */
	static final public class AIDlgAttr extends AttributeInfo implements DlgAttr {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		AIDlgAttr(Accessibility pReadAcc, Accessibility pWriteAcc, Accessibility pConfigAcc, String pName,
				String pDlgName, MoreData pMoreData) {
			super(pReadAcc, pWriteAcc, pConfigAcc, pName, pMoreData);
			if(pDlgName == null) throw new NullPointerException();
			this.DlgName = pDlgName;
		}
		
		final String DlgName;
		
		/** Returns the field name that delegate target field name of the delegation */
		public String getDlgAttrName() {
			return this.DlgName;
		}

		/**{@inheritDoc}*/ @Override
		void setDeclaredTypeRef(TypeRef pTRef) {}
		/**{@inheritDoc}*/ @Override
		public TypeRef getDeclaredTypeRef() {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		public boolean hasTypeRef() {
			return this.TypeRef != null;
		}

		/**{@inheritDoc}*/ @Override
		public boolean resolve(Engine pEngine) {
			if(this.TypeRef != null) return true;
			try {
				Type TOwner = this.getOwnerAsType();
				if(TOwner == null) return false;

				// Get the type of the delegate
				TypeRef ATRef = TOwner.searchObjectAttribute(pEngine, this.getDlgAttrName());
				if(ATRef == null) return false;
				
				this.TypeRef = ATRef.searchObjectAttribute(pEngine, this.getName());
				return (this.TypeRef != null);
			} catch (Exception E) { return false; }
		}
		
		// Satisfy AttributeInfo -----------------------------------------------
		/** Returns the kinds of this respond */
		public RKind getRKind() {
			return RKind.DlgAttr;
		}
		/** Clone this attribute **/
		@Override public AIDlgAttr makeClone() {
			AIDlgAttr AI = new AIDlgAttr(this.ReadAcc, this.WriteAcc, this.ConfigAcc, this.Name, this.DlgName, this.MoreData);
			AI.changeDeclaredOwner(this.getOwner());
			return AI;
		}
		
		// Lock --------------------------------------------------------------------
		/** This method will limits the implementation to within this package. */
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
		/**{@inheritDoc}*/ @Override
		public String toString() {
			return "@Delegate " + this.getName() + " => this." + this.getDlgAttrName()+ "." + this.getName();
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			return "@Delegate " + this.getName() + " => this." + this.getDlgAttrName()+ "." + this.getName();
		}
	}
	/** Information of a delegating attribute */
	static final public class AIDlgObject extends AttributeInfo implements DlgObject {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		AIDlgObject(Accessibility pReadAcc, Accessibility pWriteAcc, Accessibility pConfigAcc, String pName,
				Object pTarget, MoreData pMoreData) {
			super(pReadAcc, pWriteAcc, pConfigAcc, pName, pMoreData);
			this.Target = pTarget;
		}
		final Object Target;
		/** Returns the index of the target of the delegation */
		public Object getDlgObject() {
			return this.Target;
		}
		
		// Satisfy AttributeInfo -----------------------------------------------
		
		/** Returns the kinds of this respond */
		public RKind getRKind() {
			return RKind.DlgObject;
		}

		/**{@inheritDoc}*/ @Override
		void setDeclaredTypeRef(TypeRef pTRef) {}
		/**{@inheritDoc}*/ @Override
		public TypeRef getDeclaredTypeRef() {
			return null;
		}
		/**{@inheritDoc}*/ @Override
		public boolean hasTypeRef() {
			return this.TypeRef != null;
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean resolve(Engine pEngine) {
			if(this.TypeRef != null) return true;
			try {
				Type T = null;
				
				if(this.Target instanceof StackOwner) {
					this.TypeRef = ((StackOwner)this.Target).searchAttribute(pEngine, false, null, this.getName());
					//T = ((StackOwner)this.Target).getAttrType(this.getName());
				} else {
					Field F = UClass.getField(this.Target.getClass(), this.getName(), false);
					if(F != null) T = pEngine.getTypeManager().getTypeOfTheInstanceOf(F.getType()) ;
				}
				
				if(T != null)
					this.TypeRef = T.getTypeRef();
				
				return (this.TypeRef != null);
			} catch (Exception E) { return false; }
		}
		
		/**{@inheritDoc}*/ @Override
		public AIDlgObject makeClone() {
			AIDlgObject AI = new AIDlgObject(this.ReadAcc, this.WriteAcc, this.ConfigAcc, this.Name, this.Target, this.MoreData);
			AI.changeDeclaredOwner(this.getOwner());
			return AI;
		}
		/**{@inheritDoc}*/ @Override
		public String toString() {
			return "@Delegate " + this.getName() + " => " + this.getDlgObject() + "." + this.getName();
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			return "@Delegate " + this.getName() + " => " + this.getDlgObject() + "." + this.getName();
		}
		// Lock --------------------------------------------------------------------
		/** This method will limits the implementation to within this package. */
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
	/** Information of a variant attribute */
	static final public class AIVariant extends AttributeInfo implements Variant {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		AIVariant(String pName, TypeRef pTypeRef) {
			super(Accessibility.Public, Accessibility.Public, Accessibility.Public, pName, null);
			this.DlcTypeRef = (pTypeRef == null) ? TKJava.TAny.getTypeRef() : pTypeRef;
			this.TypeRef    = null;
		}
		
		TypeRef DlcTypeRef;
		/**{@inheritDoc}*/ @Override
		public TypeRef getDeclaredTypeRef() {
			return this.DlcTypeRef;
		}

		/**{@inheritDoc}*/ @Override
		void setDeclaredTypeRef(TypeRef pTRef) {
			// Recreate the DHInfo
			this.DlcTypeRef = pTRef;
		}
		/**{@inheritDoc}*/ @Override
		public boolean hasTypeRef() {
			return this.DlcTypeRef != null;
		}
		
		// To satisfy Respond --------------------------------------------------

		/** Returns the kinds of this respond */
		public RKind getRKind() {
			return RKind.Variant;
		}
		
		/**{@inheritDoc}**/ @Override
		public AIVariant makeClone() {
			AIVariant AI = new AIVariant(this.Name, this.DlcTypeRef);
			AI.changeDeclaredOwner(this.getOwner());
			return AI;
		}
		
		/**{@inheritDoc}*/ @Override
		public String toString() {
			TypeRef TRef = this.getTypeRef();
			if(TRef == null) TRef = this.DlcTypeRef;
			return this.getName() + ":" + TRef;
		}
		/**{@inheritDoc}*/ @Override
		public String toDetail() {
			TypeRef TRef = this.getTypeRef();
			if(TRef == null) TRef = this.DlcTypeRef;
			return this.getName() + ":" + TRef;
		}

		/**{@inheritDoc}*/ @Override
		void changeCurrentHolder(StackOwner pOwner) {
			if((pOwner != null) && !(pOwner instanceof TVariant))
				throw new CurryError(
					String.format("Only a variant type can be owner of a variant element (%s) <AttributeInfo:654>.", this));
			
			super.changeCurrentHolder(pOwner);
		}
		
		// Lock --------------------------------------------------------------------
		/** This method will limits the implementation to within this package. */
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
}