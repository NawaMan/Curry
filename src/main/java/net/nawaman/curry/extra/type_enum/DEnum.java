package net.nawaman.curry.extra.type_enum;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Vector;

import net.nawaman.curry.*;
import net.nawaman.util.*;

public class DEnum extends DObject implements Serializable {
	
    private static final long serialVersionUID = 8340432679493911154L;
    
    static public final class DE_Independent extends DEnum {
        
        private static final long serialVersionUID = -1385386221678303830L;
        
        DE_Independent(TEnum pTheType, TEMS_Independent pSpec) { super(pTheType, pSpec); }
		@Override public boolean        isIndependent() { return true; }
		@Override public DE_Independent asIndependent() { return this; }
	}
	static public final class DE_Borrowing extends DEnum {
	    
        private static final long serialVersionUID = -685217028176064651L;
        
        DE_Borrowing(TEnum pTheType, TEMS_Borrowing pSpec) { super(pTheType, pSpec); }
		@Override public boolean      isBorrowing() { return true; }
		@Override public DE_Borrowing asBorrowing() { return this; }
		public DEnum getOriginal() {
			if(this.getEnumType().isExpanding()) return this.getEnumType().asExpanding().getSuperEnum().getMember(this.getName());
			if(this.getEnumType().isEmulating()) return this.getEnumType().asEmulating().getSuperEnum().getMember(this.getName());
			if(this.getEnumType().isGrouping())  return this.getEnumType().asGrouping().getSubEnum().getMember(   this.getName());
			return null;
		}
	}
	static abstract class Abstract_DE_Deriving extends DEnum {

        private static final long serialVersionUID = 4630795001497790039L;
        
        Abstract_DE_Deriving(TEnum pTheType, Abstract_TEMS_Deriving pSpec) { super(pTheType, pSpec); }
		public DEnum getTarget() {
			String DN = this.Spec.isDeriving()?this.Spec.asDeriving().getDerivedName():this.Spec.asGrouping().getDerivedName();
			// Check for local first
			DEnum DE = this.getEnumType().getMember(DN);
			if(DE != null) return DE;
			// Find from super or sub
			if(this.getEnumType().isExpanding()) return this.getEnumType().asExpanding().getSuperEnum().getMember(DN);
			if(this.getEnumType().isEmulating()) return this.getEnumType().asEmulating().getSuperEnum().getMember(DN);
			if(this.getEnumType().isGrouping())  return this.getEnumType().asGrouping().getSubEnum().getMember(   DN);
			return null;
		}
	}
	static public final class DE_Deriving extends Abstract_DE_Deriving {
	    
        private static final long serialVersionUID = -504397126553744050L;
        
        DE_Deriving(TEnum pTheType, TEMS_Deriving pSpec) { super(pTheType, pSpec); }
		@Override public boolean     isDeriving() { return true; }
		@Override public DE_Deriving asDeriving() { return this; }
	}
	static public final class DE_Grouping extends Abstract_DE_Deriving {
	    
        private static final long serialVersionUID = -6488048303244750750L;
        
        DE_Grouping(TEnum pTheType, TEMS_Grouping pSpec) { super(pTheType, pSpec); }
		@Override public boolean     isGrouping() { return true; }
		@Override public DE_Grouping asGrouping() { return this; }
		public int   getMemberCount()    { return  this.Spec.asGrouping().getMemberCount(); }
		public DEnum getMember(int pPos) {
			String MName = this.Spec.asGrouping().getMember(pPos);
			return this.getEnumType().asGrouping().Super.getMember(MName);
		}
	}
	
	// Constructors --------------------------------------------------------------------------------
	
	DEnum(TEnum pTheType, TEMemberSpec pSpec) {
		super(pTheType);
		this.Spec    = pSpec;
	}
	
	TEMemberSpec Spec = null;
	
	// Cache ---------------------------------------------------------------------------------------
	HashSet<DEnum> Is_True      = null;
	HashSet<DEnum> Is_False     = null;
	HashSet<DEnum> Equals_True  = null;
	HashSet<DEnum> Equals_False = null;
	
	public String getName() { return this.Spec.getName(); }

	//public TObject getTheType()  { return this.TheType; }
	public TEnum   getEnumType() { return (TEnum)this.getTheType(); }
	
	// Characteristics -----------------------------------------------------------------------------
	
	public boolean isIndependent() { return false; }
	public boolean isBorrowing()   { return false; }
	public boolean isDeriving()    { return false; }
	public boolean isGrouping()    { return false; }
	
	public DE_Independent asIndependent() { return null; }
	public DE_Borrowing   asBorrowing()   { return null; }
	public DE_Deriving    asDeriving()    { return null; }
	public DE_Grouping    asGrouping()    { return null; }
	
	// Services ------------------------------------------------------------------------------------
	
	public boolean isImmediatelyDerivedFrom(DEnum DE) {
		if(DE == null) return false;
		if(this == DE) return false;
		if(this.isDeriving()) return this.asDeriving().getTarget() == DE;
		if(DE.isGrouping()) {
			for(int i = DE.asGrouping().getMemberCount(); --i >= 0; ) {
				if(this == DE.asGrouping().getMember(i)) return true;
			}
		}
		return false;
	}

	public DEnum asInstanceOf(TEnum pTargetType) {
		if(pTargetType == null) return null;

		// Same type?
		if(this.getEnumType() == pTargetType) {
			// Same type, here it is
			return this;
		}
		
		if(this.getEnumType().getTypeInfo().canBeAssignedByInstanceOf(pTargetType)) {
			// This type is above the target type so go down (Borrow only)
			DEnum DE = pTargetType.getMember(this.getName());
			// Not found the same type there.
			if((DE == null) || !DE.isBorrowing()) return null;
			// Let's DE go up instead
			DEnum TDE = DE.asInstanceOf(this.getEnumType());
			// If the result of going up is not this, the we can find the going down one for sure
			if(this.is(TDE)) return TDE;
			
			// Do the same value so return null;
			return null;
			
		} else if(pTargetType.getTypeInfo().canBeAssignedByInstanceOf(this.getEnumType())) {
			// The target type is about this type, so go up
			
			// Prepare inherit path
			this.getEnumType().getInheritPath();
			pTargetType       .getInheritPath();
	
			Vector<TEnum> TTEs = this.getEnumType().InheritPath.getSource();
			Vector<TEnum> OTEs = pTargetType       .InheritPath.getSource();
			
			// Are they both group or both non-group
			if(this.getEnumType().isGrouping() == pTargetType.isGrouping()) {
				// If so, they may share the same path
				
				Vector<TEnum> Long  = (TTEs.size() >= OTEs.size())?TTEs:OTEs;
				Vector<TEnum> Short = (TTEs.size() >= OTEs.size())?OTEs:TTEs;
				
				// So they are not in the same path
				if(!Long.contains(Short.get(Short.size() - 1))) return null;
				
				if(!this.getEnumType().isGrouping()) {
					// Path of non-group, its's straight-forward
					DEnum DE_L = this.moveUpNoGroup(this, pTargetType);
					// Found it :D
					if((DE_L != null) && (DE_L.getEnumType() == pTargetType)) return DE_L;
					return null;
				}
				
				// So the are path of group, we have to go from Short to Long
				DEnum DE_L = this.moveUpGroup(this, pTargetType, Long, Long.size() - 2);
				// Found it :D
				if((DE_L != null) && (DE_L.getEnumType() == pTargetType)) return DE_L;
				return null;
			}
			
			// See if it is a case of Group-join-NoGroup or NoGroup-join-Group
			
			TEnum MidT = null;
			
			// TODO Find out what this is and decide what to do
			// Is TTEs is group and join the non-group -> Go down ... don't go 
			// if(this.getEnumType().isGrouping() && OTEs.contains(TTEs.get(TTEs.size() - 1))) ...
			
			// Is OTEs is group and join the non-group -> Go Up ... let's go
			if(pTargetType.isGrouping() && TTEs.contains(OTEs.get(OTEs.size() - 1))) {
				MidT = OTEs.get(OTEs.size() - 1);
				
			// Is TTEs is not group and it join a group -> Go Up ... let's go
			} else if(!this.getEnumType().isGrouping() && OTEs.contains(TTEs.get(0))) {
				MidT = TTEs.get(0);
				
			}
			// Is OTEs is not group and it join a group -> Go down ... don't go
			// if(!ODE.getEnumType().isGrouping() && TTEs.contains(OTEs.get(0))) ...
			
			// Not any of the case above, so return null.
			else return null;
	
			// Move Start up to MidT
			DEnum DE = this.moveUpNoGroup(this, MidT);
			if(DE == null) return null;
			// Move from MidT to the target type
			DE = this.moveUpGroup(DE, pTargetType, OTEs, OTEs.indexOf(MidT));
			
			// Found it :D
			if((DE != null) && (DE.getEnumType() == pTargetType)) return DE;
			return null;
		}
		// Not related
		return null;
	}
	
	private boolean is_NOCHECK(DEnum DE) {
		if(this == DE) return true;
		
		// 'is' for same type is '==' which is not possible if this != DE already
		if(this.getEnumType() == DE.getEnumType()) {
			if(this.Is_False == null) this.Is_False = new HashSet<DEnum>();
			this.Is_False.add(DE);
			return false;
		}
		
		// Track up and up from the 'borrowing' relationship
		if(this.isBorrowing() && this.asBorrowing().getOriginal().is_NOCHECK(DE)) {
			if(this.Is_True == null) this.Is_True = new HashSet<DEnum>();
			this.Is_True.add(DE);
			return true;
		}
		
		// Track down
		// No need to cache because DE will do it by itself
		if(DE.isBorrowing() && DE.asBorrowing().getOriginal().is_NOCHECK(this)) return true;

		if(this.Is_False == null) this.Is_False = new HashSet<DEnum>();
		this.Is_False.add(DE);
		return false;
	}
	@Override public boolean is(Object O) {
		if(O == null)             return false;
		if(this == O)             return true;
		if(!(O instanceof DEnum)) return false;
		
		// Name are not equal so no need further investigation
		if(!this.getName().equals(((DEnum)O).getName())) return false;

		// See from the cache
		if((this.Is_True  != null) && this.Is_True.contains(O))  return true;
		if((this.Is_False != null) && this.Is_False.contains(O)) return false;
		
		// Check it
		return this.is_NOCHECK((DEnum)O);
	}

	private DEnum moveUpNoGroup(DEnum Down, TEnum UpType) {
		// Path of non-group, its's straight-forward
		while(Down.getEnumType() != UpType) {
			if(Down.isBorrowing()) { Down = Down.asBorrowing().getOriginal(); continue; }
			if(Down.isDeriving())  { Down = Down.asDeriving().getTarget();    continue; }
			
			if(Down.isIndependent()) return null; // Should not be
			if(Down.isGrouping())    return null; // Should not be
		}
		return Down;
	}
	private DEnum moveUpGroup(DEnum Down, TEnum UpType, Vector<TEnum> Path, int Start) {
		int Pos = Start - 1;
		while(Down.getEnumType() != UpType) {
			TEnum NextTE = Path.get(Pos);
			if(!NextTE.isGrouping()) return null; // It should be here
			// Find member of group that derive from the current DE_L;
			boolean IsNext = false;
			for(int i = NextTE.asGrouping().getMemberCount(); --i >= 0;) {
				DEnum NewDE = NextTE.asGrouping().getMemberByIndex(i);
				if(NewDE.isIndependent()) continue;
				if(NewDE.isBorrowing()) {
					if(NewDE.asBorrowing().getOriginal() == Down) {
						IsNext = true;
						Down = NewDE;
						break;
					}
					continue;
				}
				if(NewDE.isDeriving())  continue;	 // Local deriving, so no connection
				if(!NewDE.isGrouping()) return null; // It should not happend
				// Check if this Member (NewDE) derive from the current DEnum DE_L
				for(int m = NewDE.asGrouping().getMemberCount(); --m >= 0;) {
					DEnum MDE = NewDE.asGrouping().getMember(m);
					if(MDE != Down) continue;
					IsNext = true;
					Down = NewDE;
					break;
				}
				if(IsNext) break;
			}
			Pos--;
			if(!IsNext) return null;
		}
		return Down;
	}
	private boolean equalLocal(DEnum Down, DEnum Top) {
		// Same type but not the same value, it will be equal only when it's derive
		while(Down != Top) {
			if(Down instanceof Abstract_DE_Deriving) {
				// Check if it go pass over this type without being equal
				if(Down.getEnumType() != ((Abstract_DE_Deriving)Down).getTarget().getEnumType()) return false;
				// Go further
				Down = ((Abstract_DE_Deriving)Down).getTarget();
				continue;
			}
			// No deriving, so return false;
			return false;
		}
		// They are equal now :D
		return true;
	}
	
	boolean equals_CHECK(DEnum ODE) {

		if(this.getEnumType().getTypeInfo().canBeAssignedByInstanceOf(ODE.getEnumType())) {
			DEnum DE = ODE.asInstanceOf(this.getEnumType());
			if(DE == null) return false;
			return this.equalLocal(DE, this);
		} else if(ODE.getEnumType().getTypeInfo().canBeAssignedByInstanceOf(this.getEnumType())) {
			DEnum DE = this.asInstanceOf(ODE.getEnumType());
			if(DE == null) return false;
			return this.equalLocal(DE, ODE);
		} else {
			// Not related
			return false;
		}
		
		/*
		
		// TODO - Find out what this is and decide what to do with it
		// Same type?
		if(this.getEnumType() == ODE.getEnumType()) {
			// Same type, so check deriving
			return this.equalLocal(this, ODE);
		}
		
		// Prepare inherit path
		this.getEnumType().getInheritPath();
		ODE.getEnumType().getInheritPath();

		List<TEnum> TTEs = this.getEnumType().InheritPath.getSource();
		List<TEnum> OTEs = ODE.getEnumType().InheritPath.getSource();
		
		// Are they both group or both non-group
		if(this.getEnumType().isGrouping() == ODE.getEnumType().isGrouping()) {
			// If so, they may share the same path
			
			List<TEnum> Long  = (TTEs.size() >= OTEs.size())?TTEs:OTEs;
			List<TEnum> Short = (TTEs.size() >= OTEs.size())?OTEs:TTEs;
			DEnum       DE_L  = (TTEs.size() >= OTEs.size())?this:ODE;
			DEnum       DE_S  = (TTEs.size() >= OTEs.size())?ODE:this;
			
			// So they are not in the same path, so they will never equal.
			if(!Long.contains(Short.get(Short.size() - 1))) return false;
			
			if(!this.getEnumType().isGrouping()) {
				// Path of non-group, its's straight-forward
				DE_L = this.moveUpNoGroup(DE_L, DE_S.getEnumType());
				if(DE_L == null) return false; // Cound not make it to the top somehow
				if(DE_L == DE_S) return  true; // We go it :D
				// Same type but not the same value, it will be equal only when it's derive
				return this.equalLocal(DE_L, DE_S);
			} else {
				// So the are path of group, we have to go from Short to Long
				DE_L = this.moveUpGroup(DE_L, DE_S.getEnumType(), Long, Long.size() - 2);
				if(DE_L == null) return false; // Cound not make it to the top somehow
				if(DE_L == DE_S) return  true; // We go it :D
				// Same type but not the same value, it will be equal only when it's derive
				return this.equalLocal(DE_L, DE_S);
			}
		} else {
			// See if it is a case of Group-join-NoGroup or NoGroup-join-Group
			
			DEnum StartD = null;
			TEnum MidT   = null;
			List<TEnum> GPath  = null;
			
			// Is TTEs is group and join the non-group 
			if(this.getEnumType().isGrouping() && OTEs.contains(TTEs.get(TTEs.size() - 1))) {
				StartD = ODE;
				MidT   = TTEs.get(TTEs.size() - 1);
				GPath  = TTEs;
			
			// Is OTEs is group and join the non-group 
			} else if(ODE.getEnumType().isGrouping() && TTEs.contains(OTEs.get(OTEs.size() - 1))) {
				StartD = this;
				MidT   = OTEs.get(OTEs.size() - 1);
				GPath  = OTEs;
				
			// Is TTEs is not group and it join a group
			} else if(!this.getEnumType().isGrouping() && OTEs.contains(TTEs.get(0))) {
				StartD = this;
				MidT   = TTEs.get(0);
				GPath  = OTEs;
				
			// Is OTEs is not group and it join a group
			} else if(!ODE.getEnumType().isGrouping() && TTEs.contains(OTEs.get(0))) {
				StartD = ODE;
				MidT   = OTEs.get(0);
				GPath  = TTEs;
			} else return false;

			DEnum EndD = (StartD == this)?ODE:this;

			// Move Start up to MidT
			DEnum DE = this.moveUpNoGroup(StartD, MidT);
			if(DE == null) return false;
			DE = this.moveUpGroup(DE, GPath.get(0), GPath, GPath.indexOf(MidT));
			
			if(DE == null) return false; // Cound not make it to the top somehow
			if(DE == this) return  true; // We go it :D
			// Same type but not the same value, it will be equal only when it's derive
			return this.equalLocal(DE, EndD);
		}
		*/
	}
	
	@Override public boolean equals(Object O) {
		if(O == null)             return false;
		if(this == O)             return  true;
		if(!(O instanceof DEnum)) return false;
		if(this.getName().equals(((DEnum)O).getName())) return this.is_NOCHECK((DEnum)O);
		
		if((this.Equals_True  != null) && (this.Equals_True.contains(O)))  return true;
		if((this.Equals_False != null) && (this.Equals_False.contains(O))) return false;
		
		boolean Result = this.equals_CHECK((DEnum)O);
		if(Result) {
			if(this.Equals_True == null) this.Equals_True = new HashSet<DEnum>();
			this.Equals_True.add((DEnum)O);
			return true;
		} else {
			if(this.Equals_False == null) this.Equals_False = new HashSet<DEnum>();
			this.Equals_False.add((DEnum)O);
			return false;
		}
	}
	
	@Override public String  toString() { return this.getName(); }
	@Override public String  toDetail() { return this.getName() + ((this instanceof Abstract_DE_Deriving)?("->"+((Abstract_DE_Deriving)this).getTarget().toString()):"") +":" + this.getEnumType().getTypeRef(); }
	@Override public int     hash()     { return UObject.hash(this.getEnumType().hash()) + UString.hash(this.getName()); }
	
}
