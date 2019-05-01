package net.nawaman.curry;

import net.nawaman.util.UObject;

/** External executable */
public class ExternalExecutable extends JavaExecutable {
	
	ExternalExecutable(ExternalExecutor pEE, Object pID, Object pSC) {
		super(null, pEE.getSignature(pID), null, null);
		
		this.EE = pEE;
		this.ID = pID;
		this.SC = pSC;

		// Ensure kind
		if(this.getKind() != pEE.getExecKind(pID))
			throw new IllegalArgumentException("The opertaion `"+UObject.toString(pID)+"` is not a "+this.getKind()+".");
	}
	ExternalExecutor EE;	
	Object           ID;	// The ID of for this executable (to select what executable this the EE must perform)
	Object           SC;	// Secret code (this can be useful to implement access control)
	
	// Objectable --------------------------------------------------------------
	/**{@inheritDoc}*/ @Override
	protected int getBodyHash() {
		return UObject.hash(this.EE) + UObject.hash(this.ID) + UObject.hash(this.SC);
	}		
	/**{@inheritDoc}*/ @Override
	protected String getBodyStr(Engine pEngine) {
		return "<< External >>";
	}
	// Clone -------------------------------------------------------------------
	/**{@inheritDoc}*/ @Override
	final public ExternalExecutable clone() {
		return this;
	}
	// Executing ---------------------------------------------------------------
	/** Executing this */ @Override
	Object run(Context pContext, Object[] pParams) {
		Object R = this.EE.execute(pContext, this.ID, this.SC, pParams);
		if(R instanceof SpecialResult) return R; // Special result
		return R;
	}
	
	// Sub-Classes -----------------------------------------------------------------------------------------------------

	/** External Fragment */
	static class ExternalFragment extends ExternalExecutable implements Executable.Fragment {
		ExternalFragment(ExternalExecutor pEE, Object pID, Object pSC) {
			super(pEE, pID, pSC);
		}
	}
	
	/** External Macro */
	static class ExternalMacro extends ExternalExecutable implements Executable.Macro {
		ExternalMacro(ExternalExecutor pEE, Object pID, Object pSC) {
			super(pEE, pID, pSC);
		}
	}

	/** External SubRoutine */
	static class ExternalSubRoutine extends ExternalExecutable implements Executable.SubRoutine {
		ExternalSubRoutine(ExternalExecutor pEE, Object pID, Object pSC) {
			super(pEE, pID, pSC);
		}
	}
}