package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.curry.TLParametered.TRParametered;
import net.nawaman.util.Objectable;

/** General Accessibility */
abstract public class Accessibility implements Objectable, Serializable {
	
	protected Accessibility() {}

	/** Predefine public */
	static public final Accessibility Public = new Accessibility() {
		@Override public    String  getName()                      { return "Public";        }
		@Override protected boolean checkEqual(Accessibility pAcc) { return pAcc.isPublic(); }
		@Override public    boolean isPublic()                     { return true;            }
		@Override public    boolean isAllowed(Engine pEngine, Object pAccess, Type pAccessAsType, Package pAccessPackage,
				StackOwner pHost, Accessible pAccessible) {
			return true;
		}
	};
	/** Predefine public */
	static public final Accessibility Private = new Accessibility() {
		@Override public    String  getName()                      { return "Private";        }
		@Override protected boolean checkEqual(Accessibility pAcc) { return pAcc.isPrivate(); }
		@Override public    boolean isPrivate()                    { return true;             }
		
		@Override
		public boolean isAllowed(Engine pEngine, Object pAccess, Type pAccessAsType, Package pAccessPackage,
				StackOwner pHost, Accessible pAccessible) {
			if(pHost ==            null) return false;
			if(pHost ==         pAccess) return  true;
			if(pHost instanceof Package) return (pHost == pAccessPackage);
			
			MType MT = pEngine.getTypeManager();

			Type AT = pAccessible.getOwnerAsType();
			if((AT == pAccessAsType) || AT.equals(pAccessAsType)) return true;
			
			while(AT.getTypeRef() instanceof TRParametered)
				AT            = MT.getTypeFromRefNoCheck(null, ((TRParametered)AT.getTypeRef()).getTargetTypeRef());
			while(pAccessAsType.getTypeRef()  instanceof TRParametered)
				pAccessAsType = MT.getTypeFromRefNoCheck(null, ((TRParametered)pAccessAsType.getTypeRef()).getTargetTypeRef());	
			
			return (AT == pAccessAsType) || AT.equals(pAccessAsType);
		}
	};
	
	/** Returns the name of the accessibility */
	abstract public    String  getName();
	/** Checks if the accessibility is equal to this one */
	abstract protected boolean checkEqual(Accessibility pAcc);
	/** Checks if the owner in the Access-initializer can access to the StackOwner host */
	abstract public boolean isAllowed(Engine pEngine, Object pAccess, Type pAccessType, Package pAccessPackage,
	                            StackOwner pHost, Accessible pAccessible);
	/** Checks if the owner in the Access-initializer can access to the StackOwner host */
	public boolean isAllowed(Context pContext, StackOwner pHost, Accessible pAccessible) {
		return this.isAllowed(
				(pContext == null)?null:pContext.getEngine(),
				(pContext == null)?null:pContext.getStackOwner(),
				(pContext == null)?null:pContext.getStackOwnerAsType(),
				(pContext == null)?null:pContext.getStackOwnerAsPackage(),
				pHost, pAccessible);
	}
	
	/** Checks if the accessibility is public */
	public boolean isPublic()  { return false; }
	/** Checks if the accessibility is private */
	public boolean isPrivate() { return false; }
	/** Checks if the accessibility is not public or private */
	final public boolean isOther() { return false; }
	// Objectable ----------------------------------------------------------
	/** Checks if the given object is an accessibility and equals to this one */
	@Override public boolean equals(Object O) {
		if(this == O) return true;
		return (O instanceof Accessibility) && (this.checkEqual((Accessibility)O));
	}
	/** Checks if the given object is an accessibility and equals to this one */
	@Override public boolean is(Object O) { return this.equals(O); }
	/** Returns the string representation of this acccessibility */
	@Override public String  toString()   { return this.getName(); }
	/** Returns the detail string representation of this acccessibility */
	@Override public String  toDetail()   { return "StackOwner.Accessibility."+this.getName(); }
	/** Returns the hash code of this acccessibility */
	@Override public int     hash()       { return this.toString().hashCode();                 }
}