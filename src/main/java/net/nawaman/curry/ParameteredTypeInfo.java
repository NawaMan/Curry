package net.nawaman.curry;

/** Information about a parametered type */
public final class ParameteredTypeInfo extends TypeParameterInfo.TypeParameterInfos {
	
	TypeRef OrgTypeRef;
	
	ParameteredTypeInfo(TypeRef pOrgTypeRef, TypeParameterInfo ... pPTInfos) {
		super(pPTInfos);
		if(pOrgTypeRef == null) throw new NullPointerException();
		
		this.OrgTypeRef = pOrgTypeRef;
	}
	/** Separator between each parameter */
	String getSeparater() {
		return "=";
	}
	/** Returns the original type's reference */
	public TypeRef getOriginalTypeRef() {
		return this.OrgTypeRef;
	}
	
	/**{@inheritDoc}*/ @Override
	public String toString() {
		int Count = this.getParameterTypeCount();
		if(Count == 0) return "<>";
		StringBuilder SB = new StringBuilder();
		SB.append("<");
		for(int i = 0; i < Count; i++) {
			if(i != 0) SB.append(",");
			SB.append(this.getParameterTypeRef(i));
		}
		SB.append(">");
		return SB.toString();
	}
}