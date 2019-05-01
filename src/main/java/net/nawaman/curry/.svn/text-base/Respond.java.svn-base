package net.nawaman.curry;

import net.nawaman.curry.util.MoreData;
import net.nawaman.util.Objectable;

/** The respond to request to access StackOwner element */
public interface Respond extends Accessible, Objectable {
	
	static public final String MDName_IgnoreIfRepeat = "IgnoreWhenRepeat";

	/** Respond Type */
	static enum RType { Operation, Attribute;
	
		/** Checks if this respond is an Operation */
		public boolean isOperation() { return this == Operation; }
		/** Checks if this respond is an Attribute */
		public boolean isAttribute() { return this == Attribute; }
	}
	
	/** Respond Kind */
	static enum RKind { NoPermission, Dynamic, Native, Direct, DlgAttr, DlgObject, Variant;
	
		/** Checks if this respond is `NoPermission` */
		public boolean isNoPermission()  { return this == NoPermission; }
		/** Checks if this respond is `Dynamic` */
		public boolean isDynamicHandle() { return this ==      Dynamic; }
		/** Checks if this respond is a Native */
		public boolean isNative()        { return this ==       Native; }
		/** Checks if this respond is a Direct Respond */
		public boolean isDirect()        { return this ==       Direct; }
		/** Checks if this respond is a delegate respond to a field */
		public boolean isDlgAttr()       { return this ==      DlgAttr; }
		/** Checks if this respond is a delegate respond to an object */
		public boolean isDlgObject()     { return this ==    DlgObject; }
		/** Checks if this respond is a resond for variant elements */
		public boolean isVariant()       { return this ==      Variant; }
	}

	/** Returns the types of this respond */
	RType getRType();
	/** Returns the kinds of this respond */
	RKind getRKind();

	
	// By Type -------------------------------------------------------

	/** Checks if this respond is an operation */
	public boolean       isOperationInfo();
	/** Returns this respond as an operation */
	public OperationInfo asOperationInfo();
	/** Checks if this respond is an attribute */
	public boolean       isAttributeInfo();
	/** Returns this respond as an attribute */
	public AttributeInfo asAttributeInfo();
	
	// By Kind -------------------------------------------------------

	/** Returns this respond as a dynamically handled respond */
	public Dynamic   asDynamic();
	/** Returns this respond as a native respond */
	public Native    asNative();
	/** Returns this respond as a direct respond */
	public Direct    asDirect();
	/** Returns this respond as a delegate respond to a field */
	public DlgAttr   asDlgAttr();
	/** Returns this respond as a delegate respond to an object */
	public DlgObject asDlgObject();
	/** Checks if this respond is a resond for variant elements */
	public Variant   asVariant();
	
	/** Returns the more data of this attribute */
	public MoreData getMoreData();
	
	// Lock --------------------------------------------------------------------
	
	/** This method will help limiting the implementation of this interface to be within this package. */
	public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface);
	
	// Sub interfaces ----------------------------------------------------------
	
	// By Kind -------------------------------------------------------

	/** Respond to a dynamically handled */
	static public interface Dynamic extends Respond {}
	/** Respond to a native A/O */
	static public interface Native extends Respond {
		/** Check if the respond is abstract */
		public boolean isAbstract();
	}
	/** Respond that directly contains the information necessary of processing */
	static public interface Direct extends Respond {
		/** Check if the respond is abstract */
		public boolean isAbstract();
	}
	/** Respond to delegate to a field */
	static public interface DlgAttr extends Respond {
		/** Returns the field name that target of the delegation is */
		public String getDlgAttrName();
	}
	/** Respond to delegate to an object */
	static public interface DlgObject extends Respond {
		/** Returns the target object of the delegation */
		public Object getDlgObject();
	}
	/** Respond to a variant element */
	static public interface Variant extends Respond {}

	/** Detail string representation */
	public String toDetail();
}
