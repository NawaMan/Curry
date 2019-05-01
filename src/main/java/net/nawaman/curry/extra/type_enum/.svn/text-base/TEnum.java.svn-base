package net.nawaman.curry.extra.type_enum;

import java.util.Vector;

import net.nawaman.curry.*;
import net.nawaman.util.DataArray;
import net.nawaman.util.DataArray_Proxy;
import net.nawaman.util.UString;

abstract public class TEnum extends Type {

	static public final class TE_Independent extends TEnum {
		TE_Independent(TypeKind pTKind, TSEnum pTSpec) { super(pTKind, pTSpec); }
		@Override public boolean        isIndependent() { return true; }
		@Override public TE_Independent asIndependent() { return this; }
	}
	static abstract class TE_Sub extends TEnum {
		TE_Sub(TypeKind pTKind, TSEnum pTSpec, TEnum pSuper) {
			super(pTKind, pTSpec);
			this.Super = pSuper;
		}
		TEnum Super = null; 
		public Type    getSuper()     { return this.Super;             }
		public TEnum   getSuperEnum() { Type T = this.getSuper(); return (T instanceof TEnum)?(TEnum)T:null; }
	}
	static public final class TE_Expanding extends TE_Sub {
		TE_Expanding(TypeKind pTKind, TSEnum pTSpec, TEnum pSuper) { super(pTKind, pTSpec, pSuper);}
		@Override public boolean      isExpanding() { return true; }
		@Override public TE_Expanding asExpanding() { return this; }
	}
	static public final class TE_Emulating extends TE_Sub {
		TE_Emulating(TypeKind pTKind, TSEnum pTSpec, TEnum pSuper) { super(pTKind, pTSpec, pSuper); }
		@Override public boolean      isEmulating() { return true; }
		@Override public TE_Emulating asEmulating() { return this; }
	}
	static public class TE_Grouping extends TEnum {
		TE_Grouping(TypeKind pTKind, TSEnum pTSpec, TEnum pSuper) { super(pTKind, pTSpec); this.Super = pSuper; }
		TEnum Super = null;
		@Override public boolean     isGrouping() { return true; }
		@Override public TE_Grouping asGrouping() { return this; }
		public TypeRef getSubRef()  { return this.getInheritedRef(); }
		public Type    getSub()     { return this.Super;             }
		public TEnum   getSubEnum() { Type T = this.getSub(); return (T instanceof TEnum)?(TEnum)T:null; }
	}
	
	// Construction --------------------------------------------------------------------------------
	
	TEnum(TypeKind pTKind, TSEnum pTSpec) {
		super(pTKind, pTSpec);
		
		TSEnum TSE    = pTSpec;
		TEnum   TSuper = null;

		EnumKind TEK = TSE.getEnumKind();
		if(     TEK == EnumKind.Expanding) TSuper = this.asExpanding().getSuperEnum();
		else if(TEK == EnumKind.Emulating) TSuper = this.asEmulating().getSuperEnum();
		else if(TEK == EnumKind.Grouping)  TSuper = this.asGrouping().getSubEnum();
		
		// Create Members
		this.Members = new DEnum[TSE.getMemberSpecs().length + ((TEK == EnumKind.Expanding)?TSuper.getMemberCount():0)];
		
		// Create locally define.
		for(int i = TSE.getMemberSpecs().length; --i >= 0; ) {
			TEMemberSpec TEMS = TSE.getMemberSpecs()[i];
			
			if(TEMS.isIndependent()) this.Members[i] = new DEnum.DE_Independent(this, TEMS.asIndependent());
			if(TEMS.isBorrowing())   this.Members[i] = new DEnum.DE_Borrowing(  this, TEMS.asBorrowing());
			if(TEMS.isDeriving())    this.Members[i] = new DEnum.DE_Deriving(   this, TEMS.asDeriving());
			if(TEMS.isGrouping())    this.Members[i] = new DEnum.DE_Grouping(   this, TEMS.asGrouping());
		}
		// Include borrowing in case of expand
		if(TEK == EnumKind.Expanding) {
			for(int i = TSuper.getMemberCount(); --i >= 0; ) {
				DEnum DE = TSuper.Members[i];
				this.Members[TSE.getMemberSpecs().length + i] = new DEnum.DE_Borrowing(this, new TEMS_Borrowing(DE.getName()));
			}
		}
	}
	
	// Characteristics -----------------------------------------------------------------------------
	
	public boolean isIndependent() { return false; }
	public boolean isExpanding()   { return false; }
	public boolean isEmulating()   { return false; }
	public boolean isGrouping()    { return false; }
	
	public TE_Independent asIndependent() { return null; }
	public TE_Expanding   asExpanding()   { return null; }
	public TE_Emulating   asEmulating()   { return null; }
	public TE_Grouping    asGrouping()    { return null; }
	
	// Services ------------------------------------------------------------------------------------

	public boolean  isFinal()     { return ((TSEnum)this.getTypeSpec()).isFinal();     }
	public EnumKind getEnumKind() { return ((TSEnum)this.getTypeSpec()).getEnumKind(); }
	
	TSEnum getEnumSpec() { return (TSEnum)this.getTypeSpec(); }
	
	TypeRef getInheritedRef() {
		TypeRef TR  = ((TSEnum)this.getTypeSpec()).getSuperRef();
		return (TR == null)?TKJava.TAny.getTypeRef():TR;
	}
	
	// Type path -----------------------------------------------------------------------------------
	
	static class TEIP extends DataArray_Proxy<TEnum> {
		public TEIP(Vector<TEnum> pSource) { super(pSource, TEnum.class, false); }
		@SuppressWarnings("unchecked")
		Vector<TEnum> getSource() { return (Vector<TEnum>)this.Source; }
	}

	// The higher to the lower level of inherit
	TEIP                InheritPath = null;
	DataArray<TEnum> getInheritPath() {
		if(this.InheritPath != null) return this.InheritPath;		
		TEnum         TE   = this;
		Vector<TEnum> TEs  = new Vector<TEnum>();
		this.InheritPath = new TEIP(TEs);

		if(this.isIndependent()) {
			TEs.add(TE);
			return this.InheritPath;
		}
		if(this.isGrouping()) {
			TEs.add(TE);
			TE = TE.asGrouping().getSubEnum();
			if(TE.isGrouping()) for(TEnum _TE : TE.getInheritPath().toArray()) TEs.add(_TE);
			else                TEs.add(TE);
			return this.InheritPath;
		} else {
			if(TE.isExpanding()) TE = TE.asExpanding().getSuperEnum();
			if(TE.isEmulating()) TE = TE.asEmulating().getSuperEnum();
			
			if(TE.isGrouping()) TEs.add(TE);
			else {
				for(TEnum _TE : TE.getInheritPath().toArray()) TEs.add(_TE);
				TEs.add(this);
			}
			return this.InheritPath;
		}
	}
	
	// Members -------------------------------------------------------------------------------------
	
	DEnum[] Members = null;
	
	public DEnum getMember(String pName) {
		if(pName == null) return null;
		int hSearch = UString.hash(pName);
		for(int i = Members.length; --i >= 0; ) {
			if(UString.hash(Members[i].getName()) == hSearch) return Members[i];
		}
		return null;
	}
	public boolean isMember(String pName) {
		return this.getMember(pName) != null;
	}
	public boolean isMember(DEnum pDE) {
		if(pDE == null) return false;
		for(int i = Members.length; --i >= 0; ) if(Members[i] == pDE) return true;
		return false;
	}
	
	public int getMemberCount() {
		return this.Members.length;
	}
	public DEnum getMemberByIndex(int pIndex) {
		if((pIndex < 0) || (pIndex >= this.Members.length)) return null;
		return this.Members[pIndex];
	}
}
