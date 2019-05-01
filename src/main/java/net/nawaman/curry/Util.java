package net.nawaman.curry;

import net.nawaman.curry.TypeParameterInfo.TypeParameterInfos;

public class Util {
	
	// Reset -----------------------------------------------------------------------------------------------------------

	/** Resets an iterable of TypeRefs */
	final static void ResetTypeRefs(boolean IsToResetSpec, Iterable<TypeRef> TRefs) {
		if(TRefs == null) return;
		
		Type T;
		for(TypeRef TRef : TRefs) {
			if(TRef == null) continue;
			
			TRef.resetForCompilation();
			if((T = TRef.getTheType()) == null) continue;
			
			if(IsToResetSpec && (T.TSpec != null))
				T.TSpec.resetForCompilation();
		}
	}
	/** Resets an iterable of TypeRefs */
	final static public void ResetTypeRefs(Iterable<TypeRef> TRefs) {
		ResetTypeRefs(true, TRefs);
	}
	/** Resets an array of TypeRefs */
	final static public void ResetTypeRefs(boolean IsToResetSpec, TypeRef ... TRefs) {
		if(TRefs == null) return;
		
		Type T;
		for(TypeRef TRef : TRefs) {
			if(TRef == null) continue;
			
			TRef.resetForCompilation();
			if((T = TRef.getTheType()) == null) continue;

			if(IsToResetSpec && (T.TSpec != null))
				T.TSpec.resetForCompilation();
		}
	}
	/** Resets an array of TypeRefs */
	final static public void ResetTypeRefs(TypeRef ... TRefs) {
		ResetTypeRefs(true, TRefs);
	}
	/** Resets an array of TypeParameterInfos */
	final static public void ResetTypeParameterInfos(TypeParameterInfos TPIs) {
		if(TPIs == null) return;
		int PCount = TPIs.getParameterTypeCount();
		for(int i = PCount; --i >= 0;) {
			TypeRef PTRef = TPIs.getParameterTypeRef(i);
			if(PTRef != null) PTRef.resetForCompilation();
		}
	}

	/** Resets an iterable of ConstructorInfos */
	final static public void ResetConstructorInfos(Iterable<ConstructorInfo> CIs) {
		if(CIs == null) return;
		for(ConstructorInfo CI : CIs)
			ResetConstructorInfo(CI);
	}
	/** Resets an iterable of AttributeInfos */
	final static public void ResetAttributeInfos(Iterable<AttributeInfo> AIs) {
		if(AIs == null) return;
		for(AttributeInfo AI : AIs)
			ResetAttributeInfo(AI);
	}
	/** Resets an iterable of AttributeInfos */
	final static public void ResetOperationInfos(Iterable<OperationInfo> OIs) {
		if(OIs == null) return;
		for(OperationInfo OI : OIs)
			ResetOperationInfo(OI);
	}
	/** Resets an constructor info */
	final static public void ResetConstructorInfo(ConstructorInfo CI) {
		if(CI == null) return;
		ResetExecSignature(CI.getDeclaredSignature());
		ResetExecSignature(CI.getSignature());
	}
	/** Resets an attribute info */
	final static public void ResetAttributeInfo(AttributeInfo AI) {
		if(AI == null) return;
		TypeRef TR;
		if((TR = AI.getDeclaredTypeRef()) != null) TR.resetForCompilation();
		if((TR = AI.getTypeRef())         != null) TR.resetForCompilation();
	}
	/** Resets an attribute info */
	final static public void ResetOperationInfo(OperationInfo OI) {
		if(OI == null) return;
		ResetExecSignature(OI.getDeclaredSignature());
		ResetExecSignature(OI.getSignature());
	}
	
	/** Resets an attribute info */
	final static public void ResetExecSignature(ExecSignature ES) {
		if(ES == null) return;
		TypeRef TR;
		// Reset the return type
		if((TR = ES.getReturnTypeRef()) != null) TR.resetForCompilation();
		// Reset each parameters
		for(int i = ES.getParamCount(); --i >= 0; )
			if((TR = ES.getParamTypeRef(i)) != null) TR.resetForCompilation();
	}
}
