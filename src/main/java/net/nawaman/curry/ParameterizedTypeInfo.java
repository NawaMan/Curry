package net.nawaman.curry;

import net.nawaman.util.UArray;

// Used in the specification of a type to be a generic type
/** Information about a parameterized type */
public final class ParameterizedTypeInfo extends TypeParameterInfo.TypeParameterInfos {
	public ParameterizedTypeInfo(TypeParameterInfo ... pPTInfos) {
		super(pPTInfos);
	}
	/** Create a TypeParameteredInfo from this info and the set of type references. */
	ParameteredTypeInfo getParamteredInfo(Engine pEngine, TypeRef pOrgTypeRef, TypeRef ... pPTypeRefs) {
		int L = (pPTypeRefs == null)?0:pPTypeRefs.length;
		if(this.getParameterTypeCount() != L)
			throw new IllegalArgumentException(String.format(
				"Unmatch number of parameters type ([%s] for %s).",
				(L == 0)?"":UArray.toString(pPTypeRefs, "", "", ", "),
				this.toString()
			));
		
		TypeParameterInfo[] PTIs = null;
		if(L == 0) PTIs = TypeParameterInfo.EmptyParamterTypeInfoArray;
		else {
			PTIs = new TypeParameterInfo[L];
			for(int i = L; --i >= 0; ) {
				TypeRef TRef = pPTypeRefs[i];
				if(!this.getParameterTypeRef(i).canBeAssignedByInstanceOf(pEngine, TRef))
					throw new IllegalArgumentException(String.format(
							"Unmatch commatible parameters type ([%s] for %s).<ParameterizedTypeInfo:29>",
							(L == 0)?"":UArray.toString(pPTypeRefs, "", "", ", "),
							this.toString()
						));
				PTIs[i] = new TypeParameterInfo(this.getParameterTypeName(i), TRef);
			}
		}
		
		return new ParameteredTypeInfo(pOrgTypeRef, PTIs);
	}
	/** Separator between each parameter */
	String getSeparater() {
		return ":";
	}
	/**{@inheritDoc}*/ @Override
	public String toString() {
		int Count = this.getParameterTypeCount();
		if(Count == 0) return "<>";
		StringBuilder SB = new StringBuilder();
		SB.append("<");
		for(int i = 0; i < Count; i++) {
			if(i != 0) SB.append(",");
			SB.append(this.getParameterTypeName(i));
			SB.append(this.getSeparater());
			SB.append(this.getParameterTypeRef(i));
		}
		SB.append(">");
		return SB.toString();
	}
}
