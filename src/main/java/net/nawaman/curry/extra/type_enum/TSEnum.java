package net.nawaman.curry.extra.type_enum;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import net.nawaman.curry.Engine;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.Util;
import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UArray;

class TSEnum extends TypeSpec {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	// Constants ------------------------------------------------------------------------
	
	static final public String KindName = "Enum";
	
	final static public int Index_IsFinal   = 0;
	final static public int Index_EnumKind  = 1;
	final static public int Index_Members   = 2;
	final static public int Index_ExtraData = 3;

	final static public int Index_SuperType = 0;
	
	// Service ---------------------------------------------------------------------------
	
	protected TSEnum(TypeRef pTypeRef, TypeRef pSuper, boolean IsFinal, EnumKind pEnumKind,
			TEMemberSpec[] pMemberSpecs, MoreData pExtraData) {
		super(	pTypeRef,
				((pTypeRef == null) || (pExtraData == null))
					? new Serializable[] { IsFinal, pEnumKind, pMemberSpecs.clone(), }
					: new Serializable[] { IsFinal, pEnumKind, pMemberSpecs.clone(), pExtraData },
				new TypeRef[] { pSuper },
				null
			);
		
		if(pExtraData != null) pExtraData.toFreeze();
	}
	
	// Classification --------------------------------------------------------------------
	/**{@inheritDoc}*/ @Override
	public String getKindName() {
		return KindName;
	}
	
	// Services --------------------------------------------------------------------------
	
	boolean isFinal() {
		return ((Boolean)this.getData(Index_IsFinal)).booleanValue();
	}
	EnumKind getEnumKind() {
		return (EnumKind)this.getData(Index_EnumKind);
	}
	TEMemberSpec[] getMemberSpecs() {
		return ((TEMemberSpec[])this.getData(Index_Members));
	}
	
	/**{@inheritDoc}*/ @Override
	public TypeRef getSuperRef() {
		return this.getRequiredTypeRef(Index_SuperType);
	}
	
	/**{@inheritDoc}*/ @Override
	protected int getMoreDataIndex() { 
		return -1;
	}
	/**{@inheritDoc}*/ @Override
	protected int getExtraInfoIndex() {
		return (this.getDataCount() == 4)?Index_ExtraData:-1;
	}
	
	// For compilation only --------------------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected void resetTypeSpecForCompilation() {
		Util.ResetTypeRefs(this.getSuperRef());
	}

	// Parameterization --------------------------------------------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	protected void resetTypeSpecForParameterization() {}
		
	// Representation -------------------------------------------------------------------
		
	/**{@inheritDoc}*/ @Override
	protected String getToString() {
		StringBuffer SB = new StringBuffer();
		TEMemberSpec[] MSs = this.getMemberSpecs();
		SB.append("enum(");
		for(int i = 0; i < MSs.length; i++ ) {
			if(i != 0) SB.append(",");
			SB.append(MSs[i].toString());
		}
		SB.append(")");
		return SB.toString();
	}
		
	/**{@inheritDoc}*/ @Override
	protected String getDescriptionDetail(Engine pEngine) {
		pEngine.getTypeManager().ensureTypeInitialized(this.getTypeRef());
		
		TEnum TE = (TEnum)this.getThisType(pEngine);
		StringBuffer SB = new StringBuffer();
		if(TE.isFinal()) SB.append(" final");
		//SB.append("enum");
		//if(!(TE.getTypeRef() instanceof TLNoName.TRNoName)) SB.append(" ").append(TE.getTypeRef()).append(" ");
		if(TE.getInheritedRef() != null) {
			if(TE.isExpanding()) SB.append(" expands");
			if(TE.isEmulating()) SB.append(" emulates");
			if(TE.isGrouping())  SB.append(" groups");
			if(!TE.isIndependent()) SB.append(TE.getInheritedRef().toString());
		}
		SB.append(" [");
		for(int i = 0; i < TE.getMemberCount(); i++ ) {
			if(i != 0) SB.append(",");
			DEnum DE = TE.getMemberByIndex(i); 
			SB.append(DE.toString());
			
			if(DE.isGrouping()) {
				// Show member
				List<String> GMs = new Vector<String>();
				for(int m = DE.asGrouping().getMemberCount(); --m >= 0; ) {
					GMs.add(DE.asGrouping().getMember(m).getName());
				}
				SB.append(UArray.toString("{", "}", ",", GMs.toArray()));
			}
			
			DEnum Super = null;
			if(DE.isIndependent()) continue;
			if(DE.isBorrowing()) Super = DE.asBorrowing().getOriginal();
			if(DE.isDeriving())  Super = DE.asDeriving().getTarget();
			if(DE.isGrouping())  Super = DE.asGrouping().getTarget();
			
			if(Super != null) {
				if(DE.isBorrowing()) SB.append("^");
				else SB.append("->").append(Super.toString());
			}
		}
		SB.append("]");
		
		return SB.toString();
	}
}