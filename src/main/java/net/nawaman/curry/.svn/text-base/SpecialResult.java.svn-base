package net.nawaman.curry;


/**
 * Result that will not be handle in most intruction but be thrown out.
 * SpecialResult does not exist outside Curry Scope. and it musre out be returning to outside the
 *    scope.
 **/
abstract public class SpecialResult {
	
	SpecialResult(Object O) {}
	
	/** Expression that will be thrown if this Special Result is about to be set out of Curry Scope */
	abstract CurryError getException(Context pContext);
	
	/** Result that represent an error */
	static class ResultError extends SpecialResult {
		ResultError(Object pCause) {
			super(null); this.Cause = pCause;
		}
		Object Cause = null;

		@Override CurryError getException(Context pContext) {
			Throwable TheCause    = null;
			if(this.Cause != null) {
				if(     this.Cause instanceof   Throwable) TheCause = (Throwable)this.Cause;
				else if(this.Cause instanceof ResultError) {
					ResultError RE = (ResultError)this.Cause;
					while(RE.Cause instanceof ResultError) RE = (ResultError)RE.Cause;
					TheCause = ((ResultError)(this.Cause)).getException(pContext);
				}
			}
			return new CurryError("An error is thrown: ", pContext, TheCause);
		}
	}
	/** Return the result */ 
	static class ResultResult extends SpecialResult {

		ResultResult(Object pResult) { super(null); this.Result = pResult; }

		Object Result  = null;
		Object getResult() { return this.Result; }

		@Override CurryError getException(Context pContext) { return null; }
	}

	/** Return the result without caching */ 
	static class ResultNoCache extends ResultResult {
		ResultNoCache(Object pResult) { super(pResult); }
	}

	/** Return the result and force it to cache */ 
	static class ResultForceCache extends ResultResult {
		ResultForceCache(Object pResult) { super(pResult); }
	}
	
	/** Requests to replace the expression with another one (only use with CutShort) */
	static class ResultReplace extends ResultResult {
		ResultReplace(Expression pNewExpr, Object pResult) {
			super(pResult);
			this.NewExpr = pNewExpr;
		}
		Expression NewExpr = null;	// This one should be set to null if the replacement is not an expression
	}

	/** Indicating that the group execution (or a like) to end before done but not as abnormal */
	static class ResultEnd  extends ResultResult { ResultEnd(Object pResult)  { super(pResult); } }
	/** Quit the execution */
	static class ResultQuit extends ResultResult { ResultQuit(Object pResult) { super(pResult); } }

	/** Result with Name - Result for named stack */
	static class ResultNamedResult extends ResultResult {
		ResultNamedResult(String pName, Object pResult) {
			super(pResult);
			this.Name = pName;
		}
		String Name = null;
	}

	/** Exit a block */
	static class ResultExit extends ResultNamedResult {
		ResultExit(String pName, Object pResult) { super(pName, pResult); }
		@Override CurryError getException(Context pContext) {
			if(this.Name == null) return null;
			String SName = (this.Name == null)?"":"named " + this.Name + " ";
			return new CurryError("Unable to exit a stack " + SName + ".", pContext);
		}
	}
	/** Stop a loop */
	static class ResultStopLoop extends ResultNamedResult {
		ResultStopLoop(String pName, Object pResult) { super(pName, pResult); }
		@Override CurryError getException(Context pContext) {
			String LName = (this.Name == null)?"":"named " + this.Name + " ";
			return new CurryError("Unable to stop a loop " + LName + "because there is no such a loop.", pContext);
		}
	}
	/** Continue a loop */
	static class ResultContinueLoop extends ResultNamedResult {
		ResultContinueLoop(String pName, Object pResult) { super(pName, pResult); }
		@Override CurryError getException(Context pContext) {
			String LName = (this.Name == null)?"":"named " + this.Name + " ";
			return new CurryError("Unable to continue a loop " + LName + "because there is no such a loop.", pContext);
		}
	}
	/** Finish the switch or choose */
	static class ResultSwitchDone extends ResultNamedResult {
		ResultSwitchDone(String pName, Object pResult) { super(pName, pResult); }
		@Override CurryError getException(Context pContext) {
			String LName = (this.Name == null)?"":"named " + this.Name + " ";
			return new CurryError("Unable to exit a switch/choose " + LName + "because there is no such expression.", pContext);
		}
	}
	/** Finish a macro/sub-routine and a like */
	static class ResultReturn extends SpecialResult {

		ResultReturn(Object pResult) { super(null); this.Result = pResult; }

		Object Result  = null;
		Object getResult(Context pContext)  { return this.Result; }

		@Override CurryError getException(Context pContext) { return null; }
	}
	
}
