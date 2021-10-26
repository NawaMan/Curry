package net.nawaman.curry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import net.nawaman.curry.TLPackage.TRPackage;
import net.nawaman.curry.TLPackage.TRPackage_Internal;
import net.nawaman.curry.UnitBuilder.PackageInputStream;
import net.nawaman.curry.UnitBuilder.PackageOutputStream;
import net.nawaman.util.UObject;
import net.nawaman.util.UString;

/**
 * Package is a container of artifact definitions that will be stored/loaded at the same time.
 * Package also provides name space boundary and centralize dependency mechanism in which every
 *     required artifacts required by artifact definition in a package is grouped and stored.
 \* @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
final public class Package extends StackOwner_Simple implements Serializable {

	static final private long serialVersionUID = 165465645651565465L;
	
	/** An empty  */
	static final Package[] EmptyPackageArray = new Package[0];
	
	// StackOwner --------------------------------------------------------------

	/** Predefined accessibility as Public */
	static final public Accessibility Public  = Accessibility.Public;
	/** Predefined accessibility as Package */
	static final public Access        Package = Access.Package;
	/** Predefined accessibility as Group */
	static final public Access        Group   = Access.Group;
	
	/** An emun type for object type artifact accessibility */
	static abstract public class Access extends Accessibility {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		Access() {}

		/** Predefine package */
		static public final Access Package = new Access() {
            
            private static final long serialVersionUID = -3009836901006887137L;
            
			@Override public    String  getName()   { return "Package"; }
			@Override public    boolean isPackage() { return true;        }
			@Override protected boolean checkEqual(Accessibility pAcc) {
				if(pAcc instanceof Type.Access) return ((Type.Access)pAcc).isProtected();
				return false;
			}
			@Override
			public boolean isAllowed(Engine pEngine, Object pAccess, Type pAccessAsType, Package pAccessPackage,
					StackOwner pHost, Accessible pAccessible) {
				return pEngine.getPackageOf(pHost) == pAccessPackage;
			}
		};
		/** Predefine public */
		static public final Access Group = new Access() {
            
            private static final long serialVersionUID = -3009836901006887137L;
            
			@Override public    String  getName() { return "Group"; }
			@Override public    boolean isGroup() { return true;    }
			@Override protected boolean checkEqual(Accessibility pAcc) {
				if(pAcc instanceof Type.Access) return ((Type.Access)pAcc).isProtected();
				return false;
			}
			@Override
			public boolean isAllowed(Engine pEngine, Object pAccess, Type pAccessAsType, Package pAccessPackage,
					StackOwner pHost, Accessible pAccessible) {
				return pEngine.getPackageOf(pHost).isFriend(pEngine.newRootContext(), pAccessPackage);
			}
		};
		
		/** Checks if the permission is package */
		public boolean isPackage() { return false; }
		/** Checks if the permission is group */
		public boolean isGroup()   { return false; }
	}
	
	// Package will only be created by the Factory or the builder
	Package(String pName, Serializable pEngineSpecSignature) {
		this.Name                = pName;
		this.EngineSpecSignature = pEngineSpecSignature;
	}
	
	// Name -------------------------------------------------------------------
	       String Name = null;
	public String getName() { return this.Name; }
	
	// Unit -------------------------------------------------------------------
	transient Unit Unit = null;
	/** Returns the unit that contains this package. */
	public    Unit getUnit() { return this.Unit; } 
	
	// Engine Signature -------------------------------------------------------
	// TODOLATER - this should be changed in the more flexible way (now all extension of the engine during
	//     the package is being built is saved as 'required').
	       Serializable EngineSpecSignature = null;
	/** Returns the engine specification signature needed for this package.  */
	public Serializable getEngineSpecSignature() { return this.EngineSpecSignature; }
	
	// UnitManager (A fast access) --------------------------------------------
	/** Returns the unit manager that this package is registered to. */
	protected MUnit getUnitManager() { return this.Unit.Factory.UM; }
	
	// Artifact Check ---------------------------------------------------------
	
	/** Checks if the package variable `pVName` exist. */
	public boolean isVarExist(String pVName)          { return this.isAttrExist(pVName);   }
	/** Checks if the function `pES` exist. */
	public boolean isFunctionExist(ExecSignature pES) { return this.isOperExist(pES); }
	/** Checks if the package type `pTName` exist. */
	public boolean isTypeExist(String pTName) {
		if(pTName         == null) return false;
		if(this.TypeInfos == null) return false;
		for(int i = this.TypeInfos.length; --i >= 0; ) {
			if(!pTName.equals(((TRPackage)this.TypeInfos[i].getTypeRef()).TName)) continue;
			return true;
		}
		return false;
	}
	
	// Friend Packages --------------------------------------------------------
	String[] FriendNames = null;
	/** Check if pPackage is a friend of this package. **/
	public boolean isFriend(Context pContext, Package pPackage) {
		if(pPackage == null) return false;
		if((this.FriendNames == null) || (this.FriendNames.length == 0)) return false;
		for(int i = this.FriendNames.length; --i >= 0;) {
			if(!UString.equal(this.FriendNames[i], pPackage.Name)) continue;
			Package P = this.getUnitManager().getPackage(pContext, pPackage.Name);
			if(P != pPackage) return false;
			return true;
		}
		return false;
	}
	
	// Permission --------------------------------------------------------------
	
	/** Returns the accessibility of the type ref */
	static Accessibility getAccessibilityOf(TypeRef pTypeRef) {
		Type T = pTypeRef.getTheType();
		if(T == null) return null;
		return getAccessibilityOf(T);
	}
	/** Returns the accessibility of the given type 'pType' */
	static public Accessibility getAccessibilityOf(Type pType) {
		if(pType == null) return null;
		// Get the origin type ref
		if(!(pType.getTypeRef() instanceof TRPackage_Internal)) return null;
		return ((TRPackage_Internal)pType.getTypeRef()).PAccess;
	}
	
	// StackOwner --------------------------------------------------------------

	/** Returns the current Engine */
	@Override
	protected Engine getEngine() { return this.Unit.getEngine(); }
	
	/** Checks if this StackOwner is appendable */
	@Override protected boolean isElementAppendable() {
		return (this == this.getEngine().DefaultPackage);
	}

	/** Validate the given accessibility and throw an error if the validation fail. */
	@Override protected void validateAccessibility(Accessibility pAccess) {
		if(pAccess.isOther() && !(pAccess instanceof Package.Access))
			throw new IllegalArgumentException("The accessiblity must be a Package.Access.");
	}
	
	// Initialization ---------------------------------------------------------------------
	Required[] RequiredArtifacts = null;
    static class Required      implements Serializable { Required(String pPName)                          {                this.PName = pPName; } String PName;      }
	static class Required_Type extends Required        { Required_Type(String pPName, String pTName)      { super(pPName); this.TName = pTName; } String TName;      }
	static class Required_PVar extends Required        { Required_PVar(String pPName, String pVName)      { super(pPName); this.VName = pVName; } String VName;      }
	static class Required_Func extends Required        { Required_Func(String pPName, ExecSignature pFES) { super(pPName); this.FES   = pFES;   } ExecSignature FES; }
	
	transient boolean IsInitialized = false;
	
	// Main Artifacts ---------------------------------------------------------------------
	TypeSpec[] TypeInfos = null;
	
	// Types -----------------------------------------------------------------------------
	
	/** Returns the type at the index I */
	TypeSpec getTypeSpec(String pTypeName) {
		if(this.TypeInfos == null) return null;
		if(pTypeName      == null) return null;
		for(int i = this.TypeInfos.length; --i >= 0; ) {
			if(!pTypeName.equals(((TRPackage)TypeInfos[i].getTypeRef()).TName)) continue;
			return this.TypeInfos[i];
		}
		return null;
	}
	
	// Serializable --------------------------------------------------------------------------------
	
	/** Write fields from non-serializable super */
	private void writeObject(ObjectOutputStream out) throws IOException {
		if(!(out instanceof PackageOutputStream))
			throw new CurryError("Package must be saved by `PackageOutputStream` ");
		
		PackageOutputStream POS = (PackageOutputStream)out;
		
		// Engine the unit is the same name
		Unit U = this.getUnit();
		if(U != null) {
			String UName = U.getUnitName();
			if((POS.getUnitBuilder() != null) && !POS.getUnitBuilder().getName().equals(UName))
				throw new CurryError("Package can only be saved by `PackageOutputStream` created by its UnitBuilder.");
		}
		
		POS.notifyPackageWritten(this);
				
		// Save all the data needed to save
		POS.writeObject(this.AttrInfos);
		POS.writeObject(this.OperInfos);
		POS.writeObject(this.Name);
		POS.writeObject(this.EngineSpecSignature);
		POS.writeObject(this.FriendNames);
		POS.writeObject(this.RequiredArtifacts);
		POS.writeObject(this.TypeInfos);
	}
	/** Write fields from non-serializable super */
	private void readObject(ObjectInputStream in) throws IOException {
		if(!(in instanceof PackageInputStream))
			throw new CurryError("Package must be loaded by `PackageInputStream` ");
		
		PackageInputStream PIS = (PackageInputStream)in;
		try {
			this.AttrInfos           = (AttributeInfo[])PIS.readObject();
			this.OperInfos           = (OperationInfo[])PIS.readObject();
			this.Name                = (String)         PIS.readObject();
			this.EngineSpecSignature = (Serializable)   PIS.readObject();
			this.FriendNames         = (String[])       PIS.readObject();
			this.RequiredArtifacts   = (Required[])     PIS.readObject();
			this.TypeInfos           = (TypeSpec[])     PIS.readObject();
		} catch (ClassNotFoundException CNFE) {
			throw new CurryError("Error loading package ("+this.getName()+").", CNFE);
		}
		
	}
	
	// Objectable ----------------------------------------------------------------------------------

	/** Returns the short string representation of the object. */
	@Override public String toString()        { return "Package:" + this.getName(); }
	/** Returns the long string representation of the object. */
	@Override public String toDetail()        { return this.toString(); }
	/** Checks if O is the same or consider to be the same object with this object. */	
	@Override public boolean is(Object O)     { return this == O; }
	/** Checks if O equals to this object. */	
	@Override public boolean equals(Object O) { return this == O; }
	/** Returns the integer representation of the object. */
	@Override public int hash()               { return UObject.hash(this.toString()); }
	
}