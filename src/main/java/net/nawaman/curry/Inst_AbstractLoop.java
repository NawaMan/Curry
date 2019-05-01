package net.nawaman.curry;

import net.nawaman.curry.compiler.CompileProduct;

/** Loop instruction */
public class Inst_AbstractLoop extends Inst_AbstractStack {
	
	static public final String AdvanceSuffix = "_Advance";

	// The engine will be used only for creating ISpec. It is not stored in the instruction until
	// the instruction is actually registered.
	/**
	 * Constructs a new Instruction by using auto-generated spec with standard advance parameter type-list suffix.
	 * 
	 * @param pEngine      the engine in which this instruction will be used in
	 * @param pName        the name of the instruction (without advance suffix)
	 * @param pParams      the parameter type list (in ISpec format) without advance differentiated
	 * @param pIsAdvance   whether or not this loop is and advance loop 
	 **/
	protected Inst_AbstractLoop(Engine pEngine, String pName, String pParams, boolean pIsAdvance) {
		this(pEngine, pName, pParams, null, pIsAdvance);
	}
	// The engine will be used only for creating ISpec. It is not stored in the instruction until
	// the instruction is actually registered.
	/**
	 * Constructs a new Instruction by using auto-generated spec.
	 * 
	 * @param pEngine      the engine in which this instruction will be used in
	 * @param pName        the name of the instruction (without advance suffix)
	 * @param pParams      the parameter type list (in ISpec format) without advance differentiated
	 * @param pParamSuffix the "parameter type list" suffix when the instruction is in advance mode or null for standard
	 * @param pIsAdvance   whether or not this loop is and advance loop
	 **/
	protected Inst_AbstractLoop(Engine pEngine, String pName, String pParams, String pParamSuffix, boolean pIsAdvance) {
		// $ here is for StackName
		super(pEngine, String.format("%s($,%s%s%s){}:%s",
					GetName(pName, pIsAdvance),
					pParams,
					(pIsAdvance && (pParams != null) && (pParams.length() != 0))?",":"",
					GetAdvanceParams(pParamSuffix, pIsAdvance),
					GetAdvanceReturn(pIsAdvance)
				));
		this.IsAdvance = pIsAdvance;
	}
	// The engine will be used only for creating ISpec. It is not stored in the instruction until
	// the instruction is actually registered.
	/**
	 * Constructs a new Instruction by using explicitly specified spec.
	 * 
	 * @param pEngine    the engine in which this instruction will be used in
	 * @param pISpec     the ISpec string
	 * @param pIsAdvance whether or not this loop is and advance loop
	 **/
	protected Inst_AbstractLoop(Engine pEngine, InstructionSpec pISpec, boolean pIsAdvance) {
		super(pEngine, pISpec);
		this.IsAdvance = pIsAdvance;
	}

	static public final String LoopCountName       = "$Count$";
	static public final String LoopIsJustResetName = "$IsJustReset$";
	static public final String LoopEachName        = "$Each$";
	static public final String LoopIsStoppedName   = "$IsStopped$";

	boolean	IsAdvance;
	
	/** Returns a standard naming for non-advance and advance loop */
	static protected String GetName(String pName, boolean pIsAdvance) {
		return pName + (pIsAdvance?AdvanceSuffix:"");
	}
	/** Returns a parameter spec string suffix based one if the type is advance */
	static protected String GetAdvanceParams(String pParamSuffix, boolean pIsAdvance) {
		if(!pIsAdvance)          return           "";
		if(pParamSuffix != null) return pParamSuffix;
		// (Start) (Each) (Final)
		return "E,E,E";
	}
	/** Returns a return spec string based one if the type is advance */
	static protected String GetAdvanceReturn(boolean pIsAdvance) {
		return pIsAdvance?"~":"?";
	}

	// Customize -----------------------------------------------------------------------------------

	@Override protected String getStackName(Object[] pParams) {
		return (String)pParams[0];
	}
	
	protected Expression getToEnterCondExpr(Context pContext, Object[] pParams) { return null; }
	protected Expression getToExitCondExpr( Context pContext, Object[] pParams) { return null; }
	protected Expression onStartExpr(       Context pContext, Object[] pParams) { return null; }
	protected Expression onEachExpr(        Context pContext, Object[] pParams) { return null; }
	protected Expression onFinalExpr(       Context pContext, Object[] pParams) { return null; }

	// Direct
	protected Object onStart(Context pContext, Expression pExpr, Object[] pParams) {
		Expression Expr = this.onStartExpr(pContext, pParams);
		if(Expr != null) {
			Object R = this.executeAnExpression(pContext, Expr);
			if(R instanceof SpecialResult) return super.processResult(pContext, pExpr, pParams, R);
		}
		return null;
	}

	protected Object getToEnterCond(Context pContext, Expression pExpr, Object[] pParams) {
		Expression Expr = this.getToEnterCondExpr(pContext, pParams);
		boolean IsToEnter = false;
		if(Expr != null) {
			Object R = this.executeAnExpression(pContext, Expr);
			if(R != null) { // Null is default
				if(!(R instanceof Boolean)) {
					String SName = this.getStackName(pParams);
					if(SName != null)
						SName = "the '" + SName + "'";
					else
						SName = "a";
					throw new CurryError("Condition to enter " + SName
							+ " loop must be a boolean but "
							+ this.Engine.getDisplayObject(pContext, R)
							+ " was returned.(Inst_AbstractLoop.java#48).", pContext);
				}
				IsToEnter = ((Boolean)R).booleanValue();
			}
		}
		return IsToEnter;
	}

	protected Object getToExitCond(Context pContext, Expression pExpr, Object[] pParams) {
		Expression Expr = this.getToExitCondExpr(pContext, pParams);
		boolean IsToExit = false;
		if(Expr != null) {
			Object R = this.executeAnExpression(pContext, Expr);
			if(R != null) { // Null is default
				if(!(R instanceof Boolean)) {
					String SName = this.getStackName(pParams);
					if(SName != null)
						SName = "the '" + SName + "'";
					else
						SName = "a";
					throw new CurryError("Condition to exit " + SName
							+ " loop must be a boolean but "
							+ this.Engine.getDisplayObject(pContext, R)
							+ " was returned.(Inst_AbstractLoop.java#70).", pContext);
				}
				IsToExit = ((Boolean)R).booleanValue();
			}
		}
		return IsToExit;
	}

	protected Object onEach(Context pContext, Expression pExpr, Object[] pParams, Object pResult) {
		Expression Expr = this.onEachExpr(pContext, pParams);
		if(Expr != null) {
			if(IsAdvance) pContext.newConstant(pContext.getEngine(), LoopEachName, TKJava.TAny, pResult);
			Object R = this.executeAnExpression(pContext, Expr);
			if(R instanceof SpecialResult) return super.processResult(pContext, pExpr, pParams, R);
			if(IsAdvance) pContext.removeVariable(LoopEachName);
		}
		return null;
	}

	// Execution --------------------------------------------------------------
	@Override
	final protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
		String SName = this.getStackName(pParams);

		// This is the same context creation with the one in super.run()
		Context LoopContext = new Context(pContext, SName, pExpr);
		Engine  E           = LoopContext.getEngine();

		boolean IsBreak   = false;
		int     Count     = 0;
		boolean JustReset = false;
		LoopContext.newConstant(E, LoopCountName,       TKJava.TInteger, Count);
		LoopContext.newConstant(E, LoopIsJustResetName, TKJava.TBoolean, JustReset);

		Variable VCountName = ((Variable)LoopContext.getDataHolder(LoopCountName));
		Variable VResetName = ((Variable)LoopContext.getDataHolder(LoopIsJustResetName));

		// Do thing before starting the loop
		Object R = this.onStart(LoopContext, pExpr, pParams);
		if(R instanceof SpecialResult)
			return R;

		if(!IsBreak) {
			Context LoopBodyContext = new Context(LoopContext, SName, pExpr);
			while(true) {
				if(LoopBodyContext.DataHolders != null) LoopBodyContext.DataHolders.clear();
				
				// True is to enter
				Object B = this.getToEnterCond(LoopBodyContext, pExpr, pParams);
				if(Boolean.FALSE.equals(B)) break; // Finish normally
				if(B instanceof SpecialResult) {
					if((R instanceof SpecialResult.ResultNamedResult) && ((SpecialResult.ResultNamedResult)R).Name == null) {
						Object NewR = ((SpecialResult.ResultNamedResult)R).Result;
						if(R instanceof SpecialResult.ResultContinueLoop)
							R = NewR;
						else {
							// Abnormal exit
							if(R instanceof SpecialResult.ResultStopLoop) R = NewR;
							IsBreak = true;
							break;
						}
					} else {
						// Abnormal exit
						IsBreak = true;
						return R;
					}
				}

				R = this.runGroup(LoopBodyContext, pExpr, pParams);
				if(R instanceof SpecialResult) {
					if((R instanceof SpecialResult.ResultNamedResult) && ((SpecialResult.ResultNamedResult)R).Name == null) {
						Object NewR = ((SpecialResult.ResultNamedResult)R).Result;
						if(R instanceof SpecialResult.ResultContinueLoop)
							R = NewR;
						else {
							// Abnormal exit
							if(R instanceof SpecialResult.ResultStopLoop) R = NewR;
							IsBreak = true;
							break;
						}
					} else {
						// Abnormal exit
						IsBreak = true;
						return R;
					}
				}

				// Process result of each loop
				R = this.onEach(LoopBodyContext, pExpr, pParams, R);
				if(R instanceof SpecialResult) {
					if((R instanceof SpecialResult.ResultNamedResult) && ((SpecialResult.ResultNamedResult)R).Name == null) {
						Object NewR = ((SpecialResult.ResultNamedResult)R).Result;
						if(R instanceof SpecialResult.ResultContinueLoop)
							R = NewR;
						else {
							// Abnormal exit
							if(R instanceof SpecialResult.ResultStopLoop) R = NewR;
							IsBreak = true;
							break;
						}
					} else {
						// Abnormal exit
						IsBreak = true;
						return R;
					}
				}

				// True is to exit
				B = this.getToExitCond(LoopBodyContext, pExpr, pParams);
				if(Boolean.TRUE.equals(B)) break; // Finish normally
				if(B instanceof SpecialResult) {
					if((R instanceof SpecialResult.ResultNamedResult) && ((SpecialResult.ResultNamedResult)R).Name == null) {
						Object NewR = ((SpecialResult.ResultNamedResult)R).Result;
						if(R instanceof SpecialResult.ResultContinueLoop)
							R = NewR;
						else {
							// Abnormal exit
							if(R instanceof SpecialResult.ResultStopLoop) R = NewR;
							IsBreak = true;
							break;
						}
					} else {
						// Abnormal exit
						IsBreak = true;
						return R;
					}
				}

				// Update Count, set result when overfloat
				Count++;
				if(Count < 0) {
					Count = 0; // In case of reset
					JustReset = true;
				} else
					JustReset = false;

				// NOTE: Violate encapsulation for speed
				// Forcefully change the value
				VCountName.TheData = Count;
				VResetName.TheData = JustReset;
			}
		}

		// See if there is a final block
		// Final means only break of this stack or complete it. Breaking above this stack does not
		// invoke final
		if(IsAdvance) {
			Expression Expr = this.onFinalExpr(LoopContext, pParams);
			if(Expr != null) {
				LoopContext.newConstant(LoopContext.getEngine(), LoopIsStoppedName, TKJava.TBoolean, IsBreak);
				R = this.executeAnExpression(LoopContext, Expr);
				if(R instanceof SpecialResult) return R;
				return R;
			}
		}

		return !IsBreak;
	}

	/** Process exit result. */
	@Override protected Object processResult(Context pContext, Expression pExpr, Object[] pParams, Object pResult) {
		// Trap break.
		if(pResult instanceof SpecialResult.ResultExit)
			return super.processResult(pContext, pExpr, pParams, pResult);
		if(pResult instanceof SpecialResult.ResultStopLoop) {
			String SName = ((SpecialResult.ResultStopLoop)pResult).Name;
			String EName = this.getStackName(pParams);
			if((SName == null) || (SName == EName) || SName.equals(EName)) {
				((SpecialResult.ResultStopLoop)pResult).Name = null;
				return new SpecialResult.ResultEnd(pResult);
			}
		}
		if(pResult instanceof SpecialResult.ResultContinueLoop) {
			String SName = ((SpecialResult.ResultContinueLoop)pResult).Name;
			String EName = this.getStackName(pParams);
			if((SName == null) || (SName == EName) || SName.equals(EName)) {
				((SpecialResult.ResultContinueLoop)pResult).Name = null;
				return new SpecialResult.ResultEnd(pResult);
			}
			// return ((ResultContinueLoop)pResult).Result;
		}
		return pResult;
	}
	
	/**{@inheritDoc}*/ @Override
	public boolean manipulateCompileContextStart(CompileProduct pCProduct, int pPosition) {
		pCProduct.newScope(null, null);
		return true;
	}
	/**{@inheritDoc}*/ @Override
	public boolean manipulateCompileContextBeforeSub(Object[] pParams,
				CompileProduct pCProduct, int pPosition) {
		// Create
		Object O = pParams[0];
		if((O != null) && !(O instanceof String)) 
			pCProduct.reportWarning("Loop label cannot be determined at compile time", null, pPosition);
		pCProduct.newLoopScope((O instanceof String)?(String)O:null, null);
		
		// Create variables for loop
		pCProduct.newConstant(LoopCountName,       TKJava.TInteger.getTypeRef());
		pCProduct.newConstant(LoopIsJustResetName, TKJava.TBoolean.getTypeRef());
		pCProduct.newConstant(LoopEachName,        TKJava.TAny    .getTypeRef());
		pCProduct.newConstant(LoopIsStoppedName,   TKJava.TBoolean.getTypeRef());
		return true;
	}
	/**{@inheritDoc}*/ @Override public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
		pCProduct.stopLoopScope();
		pCProduct.exitScope();
		return true;
	}
}
