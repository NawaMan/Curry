package net.nawaman.curry;

import java.lang.reflect.Array;

import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.Executable.Fragment;
import net.nawaman.curry.Instructions_Core.*;
import net.nawaman.util.UArray;
import net.nawaman.util.UObject;

class Executor {

	Executor(Engine pEngine) {
		this(pEngine, null);
	}

	Executor(Engine pEngine, Debugger pDebugger) {
		this.Engine = pEngine;
		this.Debugger = pDebugger;
		this.isDebugging = (pDebugger != null);
	}

	final Engine   Engine;
	final Debugger Debugger;
	
	boolean isDebugging = false;
	boolean HasDone     = true;

	/** Creates a root context without a top scope */
	Context newRootContext(Scope pGlobalScope) {
		return new Context.ContextRoot(this, pGlobalScope, null, null);
	}
	/** Creates a root context out a top scope */
	Context newRootContext(Scope pGlobalScope, Scope pTopScope, Executable pExecutable) {
		return new Context.ContextRoot(this, pGlobalScope, pTopScope, pExecutable);
	}

	// Execute from outside to avoid thread crash
	Object exec(Scope pGlobalScope, Object pObj, boolean IsWrapped) {
		// Early return
		if(pObj == null) return null;

		if(!(pObj instanceof Expression)) {
			if(pObj instanceof SpecialResult) {
				CurryError CE = ((SpecialResult)pObj).getException(null);
				if(pObj instanceof SpecialResult.ResultResult) {
					if(CE == null) return ((SpecialResult.ResultResult)pObj).getResult();
				}
				throw CE;
			}
			return pObj;
		}

		if(!this.HasDone) throw new IllegalArgumentException("Excutor is being executing.");

		Expression Expr = (Expression)pObj;

		Context Context = new Context.ContextGlobal(this, pGlobalScope);
		if(IsWrapped) Context = new Context(Context, null, Expr);

		this.HasDone = false;
		Object Result = null;
		try {
			// `Start` Debugging
			if(isDebugging) {
				if(this.Debugger == null)
					isDebugging = false;
				else if(this.Debugger.isEnable()) this.Debugger.onStart(Context);
			}

			// Actually executing
			Result = this.execExternal(Context, Expr);
		} finally {
			// `Quit` Debugging
			if(isDebugging) { // Beginning
				if(this.Debugger == null)
					isDebugging = false;
				else if(this.Debugger.isEnable()) Result = this.Debugger.onQuit(Context, Result);
			}

			this.HasDone = true;
		}
		return Result;
	}

	/** Execute and ensure that the result is safe to return. */
	Object execExternal(Context pContext, Expression pExpr) {
		if(pExpr == null) return null;
		Object O = this.execInternal(pContext, pExpr);
		// Special Result has no place here.
		if(O instanceof SpecialResult.ResultResult) {
			Object Exc = ((SpecialResult)O).getException(pContext);
			if(Exc == null) return ((SpecialResult.ResultResult)O).getResult();
			throw (CurryError)Exc;
		}
		if(O instanceof SpecialResult) throw ((SpecialResult)O).getException(pContext);
		return O;
	}

	static public Object[] getParamOf(Expression Expr) {
		return (Expr instanceof Expression.Expr_Simple) ? ((Expression.Expr_Simple)Expr).Params : null;
	}

	/** Execute for internal use - expression only */
	Object execInternal(Context pContext, Expression Expr) {
		if(Expr == null) return null;
		try {
			// Execution -----------------------------------------------------------------

			if(Expr.isData()) return Expr.getData();

			Instruction                 Inst  = this.Engine.getInstruction(Expr, 0);
			Instruction.InstructionSpec ISpec = Inst.ISpec;

			// Check Parameters match and Adjust it --------------------------------------------------------------------
			
			// Make some value local for faster access and smaller code
			Object[] EParams   = (Expr instanceof Expression.Expr_Simple) ? ((Expression.Expr_Simple)Expr).Params : null;
			int      EPCount   = (EParams == null) ? 0 : EParams.length;
			int      IPCount   = ISpec.Params.length;
			boolean  IsVarArgs = ISpec.IsVarArgs;
			
			// The actual parameter array will be exactly the same dimension with the parameter count of the instruction
			//    Spec.
			// VarArgs will be combined into an array of last parameter.
			//    For Example:
			//        (1,2,3,4) for (i,i,i+++) will be adjusted to (1,2,(3,4));
			//        (1,2,3)   for (i,i,i+++) will be adjusted to (1,2,(3));
			//        (1,2)     for (i,i,i+++) will be adjusted to (1,2,()).
			Object[] Params     = null;
			Object[] LastParams = null;

			// The number of parameter must match except in the case of VarArgs. In that case, the number of the
			//     parameter must be at least 1 lower than the required parameters.
			boolean Match = (EPCount >= (IPCount - (IsVarArgs ? 1 : 0)));

			// Has parameters
			if(Match && (IPCount != 0)) {
				int     LastIP         = IPCount - 1;
				boolean IsCutShort     = false;
				int     LastParamsSize = EPCount - LastIP;
				// Create the parameters array
				Params     = new Object[IPCount];
				LastParams = IsVarArgs ? new Object[LastParamsSize] : null;

				// Data
				if(Inst instanceof Inst_Data)
					Params[0] = EParams[0];

				else {
					// Process parameters
					for(int i = 0; i < EPCount; i++) {
						// NOTE: Cannot use reverse loop because the line count will be wrong
						Object P = EParams[i];
						// process each parameter
						if(P instanceof Expression) {
							Expression EP = (Expression)P;
							// Execute the parameter
							Object R = this.execInternal(pContext, EP);
							
							boolean IsForceCache;
							
							// Detect if no caching is needed
							if(R instanceof SpecialResult.ResultNoCache)
								R = ((SpecialResult.ResultNoCache)R).getResult();

							// Detect and forward the special result
							else if(!(IsForceCache = (R instanceof SpecialResult.ResultForceCache)) && (R instanceof SpecialResult)) {
								if(!IsForceCache) return R;
								// To be replace (For CutShort instructions)
								// CutShort result can be `replace` or `return`, in case of `return` the execution is 
								//   done and return out by the line above (together with others special result.)
								// In case of `replace`, the parameter will be moved from sub-expressions to parameter (
								//   CutShort has parameter as its sub-expressions so it does not need to execute it
								//   unless it have to, hence (CutShort)).
								EParams[i] = ((SpecialResult.ResultReplace)R).NewExpr;
								R          = ((SpecialResult.ResultReplace)R).Result;
								if(EParams[i] == null)
									EParams[i] = R;
							}
							
							else if(IsForceCache || EP.isFunctional()) {

								// Replace parameter with data and See if all parameter of EP is data
								// If so, change this parameter to be data (so there is no need to recompute it).
								
								boolean IsAllData = false;
								
								// Get the actual result first
								if(IsForceCache) {
									R = ((SpecialResult.ResultForceCache)R).getResult();
									IsAllData = true;
								
								}
								else if((EP instanceof Expression.Expr_Data) || (EP instanceof Expression.Expr_Group))
									// For Data or RunOnce (RunOnce is a group expression that is functional)
									IsAllData = true;

								else { // Inst_Simple
									IsAllData = true;
									Object[] EPP = ((Expression.Expr_Simple)EP).Params;
									if(EPP != null) {
										// Checks all the parameters if it is all data
										for(int j = EPP.length; --j >= 0;) {
											Object EPPJ = EPP[j];
											// It is an expression but not a data,
											//     so no functional (not all of them are data)
											if((EPPJ instanceof Expression) && !(EPPJ instanceof Expression.Expr_Data)) {
												IsAllData = false;
												break;
											}
										}
									}
								}
								
								// Replace the data (caching and never have to compute it again)
								if(IsAllData)
									EParams[i] = R;
							}

							// P is now the result
							P = R;
						}

						// The index of the main parameter
						int IPIndex = (i >= LastIP) ? LastIP : i;

						// Check type
						boolean IsToAssignLastParams = true;
						// Check if the parameter is compatible unless it is a cut short and the value is null (the
						//     value should be calculate later)
						// If not match, see if it can fit the last parameter as an array
						if(!ISpec.checkCanBeAssignedBy(pContext, IPIndex, P) &&
								((!IsCutShort && !(IsCutShort = (Inst instanceof Inst_AbstractCutShort))) || (P != null))) {

							boolean IsToBreak = true;
							if(IsVarArgs && (EPCount == IPCount) && (IPIndex == LastIP)) {
								Type T = this.Engine.getTypeManager().getAnnonymousArrayTypeOf(ISpec.Params[IPIndex]);
								if(T.canBeAssignedBy(P)) {
									LastParams           = (Object[])P;
									IsToAssignLastParams = false;
									IsToBreak            = false;
								}
							}
							if(IsToBreak) {
								Match = false;
								break;
							}
						}

						// Assigned it to the main parameter array or the last parameter array
						if((i < LastIP) || !IsVarArgs) Params[i]              = P;
						else if(IsToAssignLastParams)  LastParams[i - LastIP] = P;
					}
				}
				
				// Re-process the last-parameter array -----------------------------------------------------------------

				// Duplicate the array to array of the type
				// The last array was previously created as array of java.lang.Object. but if the last parameter should
				//    be an integer, this piece of code will takes care of that.
				if(IsVarArgs && (LastParams != null) && (LastParams.length != 0)) {
					// Get the class of the last parameter type
					Class<?> LastParamClass = ISpec.getParameterType(LastIP).getDataClass(pContext);
					// If the type is virtual or its class is object, just use the already  created array
					if((LastParamClass == null) ||
					   (LastParamClass == Object.class) ||
					   (LastParams.getClass().isArray() && (LastParamClass == LastParams.getClass().getComponentType()))
					) {
						Object   P;
						Class<?> CP;
						if((LastParams.length == 1) && ((P = LastParams[0]) != null)
						   && (CP = P.getClass()).isArray() && LastParamClass.isAssignableFrom(CP.getComponentType()))
							 Params[LastIP] = LastParams[0];
						else Params[LastIP] = LastParams;
					} else if(EPCount != LastIP) {
						// Create the new array
						Object NewLastParams = Array.newInstance(LastParamClass, LastParamsSize);
						System.arraycopy(LastParams, 0, NewLastParams, 0, LastParamsSize);
						Params[LastIP] = NewLastParams;
					}
				}
			}

			// Set the co-ordinate
			pContext.setCurrentExpression(Expr);

			// Debugging -----------------------------------------------------------------
			if(isDebugging) {
				if(this.Debugger == null)
					isDebugging = false;
				else {
					if(this.Debugger.isEnable() && this.Debugger.isMornitorExpressionExecution()) {
						// Run the debugger
						Object R = this.debugExpression(pContext, Expr, Params);
						// Replace the result.
						if(R instanceof Debugger.DebuggerResult)  return ((Debugger.DebuggerResult)R).Result;
						if(R instanceof Debugger.DebuggerReplace) Expr = ((Debugger.DebuggerReplace)R).Expr;
						if(R instanceof SpecialResult)            return R;
					}
				}
			}
			
			// If the parameter is not match, throw an error
			if(!Match) {
				return new SpecialResult.ResultError(
							new CurryError(getParamErrMsg(this.Engine, pContext, Inst, EParams),
							pContext));
			}

			// Executing the parameter by the instructor
			return Inst.run(pContext, Expr, Params);
			
		} catch(Throwable E) {
			// If a Throwable is catch (that is not a runtime exception), wrap it with CurryError (a runtime exception
			//    So that we can add in the location). 
			if(E instanceof CurryError)
			    throw (CurryError)E;
			
			return new SpecialResult.ResultError(
					((E instanceof CurryError) && (((CurryError)E).Locations != null))
						? (CurryError)E
						: new CurryError("An error was thrown: ", pContext, E));
		}
	}

	// Execution of the Executable -----------------------------------------------------------------

	/** Invoke an execution (Run a fragment, Execute a macro or Call a sub-routine) */
	Object execExecutable(
			final Context  pContext, final Executable pInitiator,        final Executable pExec,
			final ExecKind pAsKind,  final boolean    pIsBlindCaller,    final Object     SO,
			final Object[] pParams,  final boolean    pIsAlreadyAdjusted,final boolean    pIsControlFlowSafeForFragment
		) {
		if(pExec == null) return null;
		if((pExec instanceof Expression) && ((Expression)pExec).isData())
			return ((Expression)pExec).getData();
		
		Context    $Context   = pContext;
		Executable $Initiator = (pInitiator == null) ? pExec           : pInitiator;
		ExecKind   $AsKind    = (pAsKind    == null) ? pExec.getKind() : pAsKind;
		Object     $SOwner    = SO;

		// Debugging -----------------------------------------------------------------
		boolean IsToRun = true;
		Object R = null;
		if(isDebugging) {
			if(this.Debugger == null)
				isDebugging = false;
			else {
				if(this.Debugger.isEnable() && this.Debugger.isMornitorExecutableInvocation()) {
					R = this.Debugger.onInvokeExecutable($Context, pExec);
					// Replace the result.
					if(R instanceof Debugger.DebuggerResult) return ((Debugger.DebuggerResult)R).Result;
					if(R instanceof Debugger.DebuggerReplace) {
						Expression Expr = ((Debugger.DebuggerReplace)R).Expr;
						if(Expr == null) return null;
						R = this.execInternal($Context, Expr);
						IsToRun = false;
					}
				}
			}
		}

		ExecSignature ES = pExec.getSignature();
		if(IsToRun) {
			// Check for early return
			boolean IsCurry   = pExec.isCurry();
			Object  CurryBody = null;

			// Early returns
			if(IsCurry && !((CurryBody = pExec.asCurry().getBody()) instanceof Expression))
				return CurryBody;
			// NOTE: if IsCurry, CurryBody is always correctly assigned
			
			// Ensure the parameter is not null
			Object[] $Params = (pParams == null) ? UObject.EmptyObjectArray : pParams;

			// Prepare the parameters for the execution
			Object[] Params = $Params;
			if(!pIsAlreadyAdjusted) {
				// Not yet adjusted, so do it
				Object[][] AParams = new Object[1][];
				int Score = ExecInterface.Util.canBeAssignedBy_ByParams(this.Engine, $Context, ES, $Params, AParams);
				Params = AParams[0];
				if((Score == ExecInterface.NotMatch) || (Params == null)) { // Adjust fail
					$Params = new Object[] { $Params };
					// Try again
					Score = ExecInterface.Util.canBeAssignedBy_ByParams(this.Engine, $Context, ES, $Params, AParams);
					Params = AParams[0];
					
					if((Score == ExecInterface.NotMatch) || (Params == null)) { // Adjust fail
					    Type[] aParamTypes = new Type[pParams.length];
					    for(int i = 0; i < pParams.length; i++)
					        aParamTypes[i] = this.Engine.getTypeManager().getTypeOf(pParams[i]);
					    
						// The type are not compatible, Throw an error
						throw new CurryError(
							String.format(
								"Invalid parameters %s:%s for %s %s <Executor:369>",
								UArray.toString(pParams,     "(", ")", ","),
								UArray.toString(aParamTypes, "[", "]", ","),
								pExec.getKind(),
								pExec.getSignature().toString()
							),
							$Context
						);
					}
				}
			} // Already adjusted so no adjust-no check (the checking will be done when the parameter is first used)

			// Prepare the context
			// SubRoutine can only be run as a subroutine
			if(pExec.getKind().isSubRoutine())                         $AsKind = Executable.ExecKind.SubRoutine;
			// Macro can be run as a macro or a subroutine
			else if(pExec.getKind().isMacro() && $AsKind.isFragment()) $AsKind = Executable.ExecKind.Macro;
			// Fragment can only be run as everything (it just have no param)

			// If not a simple Java method, we need to prepare context
			if(!(pExec instanceof JavaExecutable.JavaSubRoutine_Simple)) {

				// Closure carry itsown context so no need to use the current one
				if(pExec instanceof Closure) {
					// Ensure that context is still usable
					Context C = ((Closure)pExec).getTheContext();
					if((C == null) || !C.isAlive($Context))
						return new SpecialResult.ResultError(
								new CurryError("The closure's context is no longer usable <Executor:398>", $Context));

					// Find appropriate StackOwner if none is given
					if(($SOwner == null) && (($SOwner = C.getStackOwner()) == null))
						$SOwner = this.Engine.getDefaultStackOwner();

					// Use the context and the executable kind from the wrapped.
					$Context = C;
					$AsKind  = ((Closure)pExec).getWrappedKind();
				}

				switch($AsKind) {
					case SubRoutine: {
						// Find appropriate StackOwner and Initiator if none is given
						if($SOwner == null)
							$SOwner = this.Engine.getDefaultStackOwner();
						
						// Create Context for SubRoutine
						$Context = new Context.ContextSubRoutine(
								$Context,
								$Initiator,
								$SOwner,
								pExec,
								Params,
								!pIsAlreadyAdjusted,
								pIsBlindCaller);
						break;
					}
					case Macro: {
						// Find appropriate StackOwner and Initiator if none is given
						if(($SOwner == null) && (($SOwner = $Context.getStackOwner()) == null))
							$SOwner = this.Engine.getDefaultStackOwner();
						
						// Macro can be run as a macro or a subroutine
						$Context = new Context.ContextMacro(
									$Context,
									$Initiator,
									$SOwner,
									pExec,
									$Params,
									!pIsAlreadyAdjusted,
									pIsBlindCaller);
						break;
					}
					case Fragment:
						// Find appropriate StackOwner and Initiator if none is given
						if(($SOwner == null) && (($SOwner = $Context.getStackOwner()) == null))
							$SOwner = this.Engine.getDefaultStackOwner();
						
						$Context = new Context.ContextFragment(
									$Context,
									$Initiator,
									$SOwner,
									"",
									(pExec instanceof Closure)
										?((Closure)pExec).getWrappedExecutable().asFragment()
										:(Fragment)pExec);
						break;
				}
			}
			// The actual execute
			if(IsCurry) {
				// For Curry
				R = $Context.getExecutor().execInternal($Context, (Expression)CurryBody);
			} else if(pExec.isJava()) {
				// For Native
				R = pExec.asJava().runRaw($Context, Params);
			} else {
				// throw an error - Unknown executable type
				R = new SpecialResult.ResultError(new CurryError("Unknown executable types (" + pExec.toDetail() + ").", $Context));
			}
		}

		// Process the special result
		if(R instanceof SpecialResult) {
			if($AsKind.isFragment()) {
				if(pIsControlFlowSafeForFragment) {
					if((R instanceof SpecialResult.ResultError) || (R instanceof SpecialResult.ResultQuit)) return R;
					if(R instanceof SpecialResult.ResultResult) return ((SpecialResult.ResultResult)R).Result;
				}
				return R;
			} else {
				if(R instanceof SpecialResult.ResultExit) {
					String SName = ((SpecialResult.ResultExit)R).Name;
					String EName = pExec.getSignature().getName();
					if((SName == null) || (SName == EName) || SName.equals(EName)) {
						// Exit the stack with the same name
						R = ((SpecialResult.ResultExit)R).Result;
					} else {
						// Exit a stack with other name so it must be an error.
						return new SpecialResult.ResultError(((SpecialResult.ResultExit)R).getException($Context));
					}
					
				} else if((R instanceof SpecialResult.ResultError) || (R instanceof SpecialResult.ResultQuit)) {
					// Pass on the error and quit
					return R;

				} else if(R instanceof SpecialResult.ResultReturn) {
					// A return was called
					R = ((SpecialResult.ResultReturn)R).Result;
					
				} else if(R instanceof SpecialResult.ResultReplace) {
					// A return was called
					R = ((SpecialResult.ResultReplace)R).Result;
					
				} else {
					// Change the Unrecognizable to an error
					return new SpecialResult.ResultError(((SpecialResult)R).getException($Context));
				}
			}
		}

		// Check return type
		TypeRef RTRef = ES.getReturnTypeRef();
		if(TKJava.TVoid.getTypeRef().equals(RTRef)) {
			// Ensure that executable with void will return null;
			return null;
		} else if(!MType.CanTypeRefByAssignableBy($Context, this.Engine, RTRef, R)) {
			R = new SpecialResult.ResultError(new CurryError("Executable Error: `"
							+ this.Engine.toString($Context, R)
							+ "`:"+ this.Engine.getTypeManager().getTypeOf(R) +" is not a valid return value of "
							+ pExec.getKind().toString() + " " + ES.toString() + ".", $Context));
		}
		return R;
	}

	// Error report --------------------------------------------------------------------------------

	static String getParamErrMsg(Engine pEngine, Context pContext, Instruction pInst, Object[] pEParams) {
		return "Illegal parameters " + getParamErrMsg_ParamListOnly(pEngine, pContext, pInst, pEParams);
	}

	static String getParamErrMsg_ParamListOnly(Engine pEngine, Context pContext, Instruction pInst, Object[] pEParams) {
		int EPCount = (pEParams == null) ? 0 : pEParams.length;
		String[] EParamStrs = new String[EPCount];
		if(pEParams != null) {
			for(int i = EPCount; --i >= 0;) EParamStrs[i] = pEngine.getDisplayObject(pContext, pEParams[i]);
		}
		return UArray.toString(EParamStrs, "(", ")", ",") + " for " + pInst.getName()
				+ UArray.toString(pInst.ISpec.Params, "(", "", ",")
				+ ((pInst.ISpec.IsVarArgs) ? "...)" : ")") + ".";
	}

	// Debugging -----------------------------------------------------------------------------------

	Object debugExpression(Context pContext, Expression Expr, Object[] pParams) {
		// Debug Line Number change
		if(this.Debugger.isMornitorLineChange() && (pContext.getCurrentLineNumber() != Expr.getLineNumber())) {
			this.Debugger.onLineChanged(pContext);
		}
		// Debug Expression
		return this.Debugger.onExecuteExpression(pContext, Expr, pParams);
	}

	// Debug an executable
	Object debugExecutable(Context pContext, Executable E) {
		return this.Debugger.onInvokeExecutable(pContext, E);
	}
}
