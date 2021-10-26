package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.curry.util.MoreData;
import net.nawaman.util.UObject;

abstract public class CurryExecutable extends AbstractExecutable implements Executable.Curry {
    
    private static final long serialVersionUID = -3009836901006887137L;
    
	CurryExecutable(Engine pEngine, ExecSignature pSignature, Serializable pBody, String[] pFVNames, Scope pFrozenScope) {
		super(pEngine, pFVNames, pFrozenScope);
		this.Signature = pSignature;
		this.Body      = pBody;
		if(this.Signature == null)
			throw new NullPointerException("Null signature for CurryExecutable.");
	}
	
	ExecSignature Signature;
	Serializable  Body;
	
	/**{@inheritDoc}*/ @Override
	final public ExecSignature getSignature() {
		return this.Signature;
	}
	/**{@inheritDoc}*/ @Override
	final public Location getLocation() {
		if(this.Signature == null) return null;
		return this.Signature.getLocation();
	}
	/**{@inheritDoc}*/ @Override
	final public Serializable getBody() {
		return this.Body;
	}
	// Objectable --------------------------------------------------------------
	/**{@inheritDoc}*/ @Override
	int getBodyHash() {
		return UObject.hash(this.Body);
	}
	/**{@inheritDoc}*/ @Override
	String getBodyStr(Engine pEngine) {
		if((this.Body instanceof Expression) && (pEngine != null)) return ((Expression)this.Body).toDetail(pEngine);
		return UObject.toDetail(this.Body);
	}
	
	/** Curry Fragment */
	static public class CurryFragment extends CurryExecutable implements Executable.Fragment {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		public CurryFragment(ExecSignature pSignture, Serializable pBody) {
			super(null, pSignture, pBody, null, null);
		}
		public CurryFragment(Engine pEngine, String pName, TypeRef pReturnTypeRef, Location pLocation,
				MoreData pExtraData, Serializable pBody, String[] pFVNames, Scope pFrozenScope) {
			super(pEngine,
					ExecSignature.newProcedureSignature(
						pName,
						(pReturnTypeRef == null)?TKJava.TAny.getTypeRef():pReturnTypeRef,
						pLocation,
						pExtraData
					),
					pBody,
					pFVNames, pFrozenScope);
		}
		// Clone -------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public CurryFragment clone() {
			if(this.Body instanceof Expression)
				 return new CurryFragment(this.Signature, ((Expression)this.Body).makeClone());
			else return new CurryFragment(this.Signature,              this.Body             );
		}
	}
	
	/** Curry Macro */
	static public class CurryMacro extends CurryExecutable implements Executable.Macro {
        
        private static final long serialVersionUID = 8605071645446256069L;
        
		public CurryMacro(Engine pEngine, ExecSignature pSignature, Serializable pBody, String[] pFVNames,
				Scope pFrozenScope) {
			super(pEngine, pSignature, pBody, pFVNames, pFrozenScope);
		}
		// Clone -------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public CurryMacro clone() {
			if(this.Body instanceof Expression)
				 return new CurryMacro(null, this.Signature, ((Expression)this.Body).makeClone(), null, null);
			else return new CurryMacro(null, this.Signature,              this.Body             , null, null);
		}
	}

	/** Curry SubRoutine */
	static public class CurrySubRoutine extends CurryExecutable implements Executable.SubRoutine {
        
        private static final long serialVersionUID = -3009836901006887137L;
        
		public CurrySubRoutine(Engine pEngine, ExecSignature pSignature, Serializable pBody, String[] pFVNames,
				Scope pFrozenScope) {
			super(pEngine, pSignature, pBody, pFVNames, pFrozenScope);
		}
		// Clone -------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public CurrySubRoutine clone() {
			if(this.Body instanceof Expression)
				 return new CurrySubRoutine(null, this.Signature, ((Expression)this.Body).makeClone(), null, null);
			else return new CurrySubRoutine(null, this.Signature,              this.Body             , null, null);
		}
	}
}