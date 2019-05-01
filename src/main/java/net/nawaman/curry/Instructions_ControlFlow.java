package net.nawaman.curry;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

import net.nawaman.curry.Expression.Expr_Expr;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.util.UClass;
import net.nawaman.util.UNumber;
import net.nawaman.util.UObject;

public class Instructions_ControlFlow {
	
	static public class Inst_Quit extends Inst_AbstractSimple {
		static public final String Name = "quit";
		
		Inst_Quit(Engine pEngine) {
			super(pEngine, Name + "(~):~");
		}
		/**{@inherDoc}*/ @Override protected Object run(Context pContext, Object[] pParams) {
			return new SpecialResult.ResultQuit(pParams[0]);
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O instanceof Expression) return ((Expression)O).getReturnTypeRef(pCProduct);
			return pCProduct.getEngine().getTypeManager().getTypeOf(O).getTypeRef();
		}
	}
	
	static public class Inst_Exit extends Inst_AbstractSimple {
		static public final String Name = "exit";
		
		Inst_Exit(Engine pEngine) {
			super(pEngine, Name + "($,~):~");
		}
		/**{@inherDoc}*/ @Override protected Object run(Context pContext, Object[] pParams) {
			return new SpecialResult.ResultExit((String)pParams[0], pParams[1]);
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(1);
			if(O instanceof Expression) return ((Expression)O).getReturnTypeRef(pCProduct);
			return pCProduct.getEngine().getTypeManager().getTypeOf(O).getTypeRef();
		}
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			if(!pCProduct.isCompileTimeCheckingFull()) return true;
			
			Object Label = pExpr.getParam(0);
			Object Value;
			if((Label != null) && !(Label instanceof String)) {
				pCProduct.reportWarning("Unable to determine the label at compile time", null, pExpr.getColumn(), pExpr.getLineNumber());
			} else if(!pCProduct.isInsideScope()) {
				pCProduct.reportWarning("Done outside switch",                           null, pExpr.getColumn(), pExpr.getLineNumber());
			} else if((Label != null) && !pCProduct.isInsideScope((String)Label)) {
				pCProduct.reportWarning("Label not found ("+Label+")", null);
			} else if(!pCProduct.canExitScope((String)Label, Value = pExpr.getParam(1))) {
				pCProduct.reportWarning(
						String.format("Incompatible return type: %s needed but %s found", 
							pCProduct.getClosestSignature().getReturnTypeRef(),
							pCProduct.getReturnTypeRefOf(Value)
						),
						null, pExpr.getColumn(), pExpr.getLineNumber());
			}
			return true;
		}
	}
	
	static public class Inst_If extends Inst_AbstractComplex {
		static public final String[] Names = new String[] { "if","unless" };
		
		Inst_If(Engine pEngine, boolean pIsBoolean) {
			super(pEngine, (pIsBoolean?Names[0]:Names[1]) + "(?){}:?");
			this.IsBoolean = pIsBoolean;
		}
		boolean IsBoolean = true;
		@Override protected int    getMinimumNumberOfSub(int PCount) { return 1; }
		@Override protected int    getMaximumNumberOfSub(int PCount) { return 2; }
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			if(Boolean.TRUE.equals(pParams[0]) == this.IsBoolean) {
				Object R = this.executeAnExpression(pContext, pExpr.getSubExpr(0));
				if(R instanceof SpecialResult) return R;
				return true;
			} else {
				if(pExpr.getSubExprCount() >= 2) {
					Object R = this.executeAnExpression(pContext, pExpr.getSubExpr(1));
					if(R instanceof SpecialResult) return R;
				}
				return false;
			}
		}
	}
	
	static public class Inst_Stop extends Inst_AbstractSimple {
		static public final String Name = "stop";
		
		Inst_Stop(Engine pEngine) {
			super(pEngine, Name + "($,~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return new SpecialResult.ResultStopLoop((String)pParams[0], pParams[1]);
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(1);
			if(O instanceof Expression) return ((Expression)O).getReturnTypeRef(pCProduct);
			return pCProduct.getEngine().getTypeManager().getTypeOf(O).getTypeRef();
		}
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			if(!pCProduct.isCompileTimeCheckingFull()) return true;
			
			Object Label = pExpr.getParam(0);
			Object Value;
			if((Label != null) && !(Label instanceof String)) {
				pCProduct.reportWarning(
						"Unable to determine the label at compile time", null, pExpr.getColumn(), pExpr.getLineNumber());
			} else if(!pCProduct.isInsideLoopScope()) {
				pCProduct.reportWarning(
						"Stop outside loop", null, pExpr.getColumn(), pExpr.getLineNumber());
			} else if((Label != null) && !pCProduct.isInsideLoopScope((String)Label)) {
				pCProduct.reportWarning(
						"Label not found ("+Label+")", null);
			} else if(!pCProduct.canStopLoopScope((String)Label, Value = pExpr.getParam(1))) {
				pCProduct.reportWarning(
						String.format("Incompatible return type: %s needed but %s found", 
							pCProduct.getClosestSignature().getReturnTypeRef(),
							pCProduct.getReturnTypeRefOf(Value)
						),
						null, pExpr.getColumn(), pExpr.getLineNumber());
			}
			
			return true;
		}
	}
	static public class Inst_Continue extends Inst_AbstractSimple {
		static public final String Name = "continue";
		
		Inst_Continue(Engine pEngine) {
			super(pEngine, Name + "($,~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return new SpecialResult.ResultContinueLoop((String)pParams[0], pParams[1]);
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(1);
			if(O instanceof Expression) return ((Expression)O).getReturnTypeRef(pCProduct);
			return pCProduct.getEngine().getTypeManager().getTypeOf(O).getTypeRef();
		}
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			if(!pCProduct.isCompileTimeCheckingFull()) return true;
			
			Object Label = pExpr.getParam(0);
			Object Value;
			if((Label != null) && !(Label instanceof String)) {
				pCProduct.reportWarning(
						"Unable to determine the label at compile time", null, pExpr.getColumn(), pExpr.getLineNumber());
			} else if(!pCProduct.isInsideLoopScope()) {
				pCProduct.reportWarning(
						"Continue outside loop",null, pExpr.getColumn(), pExpr.getLineNumber());
			} else if((Label != null) && !pCProduct.isInsideLoopScope((String)Label)) {
				pCProduct.reportWarning(
						"Label not found ("+Label+")", null);
			} else if(!pCProduct.canContinueLoopScope((String)Label, Value = pExpr.getParam(1))) {
				pCProduct.reportWarning(
						String.format("Incompatible return type: %s needed but %s found", 
							pCProduct.getClosestSignature().getReturnTypeRef(),
							pCProduct.getReturnTypeRefOf(Value)
						),
						null, pExpr.getColumn(), pExpr.getLineNumber());
			}
			return true;
		}
	}
	
	static public class Inst_Loop extends Inst_AbstractLoop {
		static public final String Name = "loop";
		
		// StackName, Start, Enter, Exit, Each, Final
		protected Inst_Loop(Engine pEngine) {
			super(pEngine, InstructionSpec.newISpec(pEngine, String.format("%s($,%s){}:%s", Name, "E,E,E,E,E","~")), true);
		}
		
		@Override protected Expression onStartExpr(       Context pContext, Object[] pParams) { return (Expression)pParams[1]; }
		@Override protected Expression getToEnterCondExpr(Context pContext, Object[] pParams) { return (Expression)pParams[2]; }
		@Override protected Expression getToExitCondExpr( Context pContext, Object[] pParams) { return (Expression)pParams[3]; }
		@Override protected Expression onEachExpr(        Context pContext, Object[] pParams) { return (Expression)pParams[4]; }
		@Override protected Expression onFinalExpr(       Context pContext, Object[] pParams) { return (Expression)pParams[5]; }
	}
	static public class Inst_While extends Inst_AbstractLoop {
		static public final String Name = "while";
		
		// StackName, Enter [Start Each Final]
		protected Inst_While(Engine pEngine, boolean pIsAdvance) {
			super(pEngine, Name, "E", pIsAdvance);
		}
		@Override protected Expression getToEnterCondExpr(Context pContext, Object[] pParams) {
			return (Expression)pParams[1];
		}
		@Override protected Object getToExitCond(Context pContext, Expression pExpr, Object[] pParams) {
			return false; // Never exit by exit condition
		}
		@Override protected Expression onStartExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[2]);
		}
		@Override protected Expression onEachExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[3]);
		}
		@Override protected Expression onFinalExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[4]);
		}
	}
	static public class Inst_Repeat extends Inst_AbstractLoop {
		static public final String Name = "repeat";
		
		// StackName, Exit, [Start Each Final]
		protected Inst_Repeat(Engine pEngine, boolean pIsAdvance) {
			super(pEngine, Name, "E", pIsAdvance);
		}
		@Override protected Expression getToExitCondExpr(Context pContext, Object[] pParams) {
			return (Expression)pParams[1];
		}
		@Override protected Object getToExitCond(Context pContext, Expression pExpr, Object[] pParams) {
			return super.getToExitCond(pContext, pExpr, pParams);
		}
		@Override protected Object getToEnterCond(Context pContext, Expression pExpr, Object[] pParams) {
			return true; // Always enter by enter condition
		}
		@Override protected Expression onStartExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[2]);
		}
		@Override protected Expression onEachExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[3]);
		}
		@Override protected Expression onFinalExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[4]);
		}
	}
	static public class Inst_For extends Inst_AbstractLoop {
		static public final String Name = "for";
		
		// StackName, Initial, Enter, Exit, [Each Final]
		// Note Exit condition is used as Update statement and it always return false (no exit)
		protected Inst_For(Engine pEngine, boolean pIsAdvance) {
			// Use non-standard parameter prefix
			super(pEngine, Name, "E,E,E", "E,E", pIsAdvance);
		}
		@Override protected Expression onStartExpr(Context pContext, Object[] pParams) {
			return (Expression)pParams[1];
		}
		@Override protected Expression getToEnterCondExpr(Context pContext, Object[] pParams) {
			return (Expression)pParams[2];
		}
		@Override protected Object getToExitCond(Context pContext, Expression pExpr, Object[] pParams) {
			Expression Expr = (Expression)pParams[3];
			if(Expr != null) this.executeAnExpression(pContext, Expr);
			return false;
		}
		@Override protected Expression onEachExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[4]);
		}
		@Override protected Expression onFinalExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[5]);
		}
	}
	static public class Inst_FromTo extends Inst_AbstractLoop {
		static public final String Name = "fromTo";
		
		// StackName, IndexName, From, To, Step, [Start Each Final]
		protected Inst_FromTo(Engine pEngine, boolean pIsAdvance) {
			super(pEngine, Name, "+$,+!,+#,+#,+#", pIsAdvance);
		}
		@Override protected Object onStart(Context pContext, Expression pExpr, Object[] pParams) {
			String IName = (String)pParams[1];
			Type   IType = (Type)  pParams[2];
			pContext.newVariable(pContext.getEngine(), IName, IType, pParams[3]);
			return super.onStart(pContext, pExpr, pParams);
		}
		// Always enter
		@Override protected Object getToEnterCond(Context pContext, Expression pExpr, Object[] pParams) {
			String IName   = (String)pParams[1];
			Object Current = pContext.getVariableValue(IName);
			Object To      = pParams[4];
			return !Current.equals(To);
		}
		@Override protected Object getToExitCond(Context pContext, Expression pExpr, Object[] pParams) {
			String IName   = (String)pParams[1];
			Type   IType   = (Type)  pParams[2];
			double From    = ((Number)pParams[3]).doubleValue();
			double To      = ((Number)pParams[4]).doubleValue();
			double Step    = ((Number)pParams[5]).doubleValue();
			double Current = ((Number)pContext.getVariableValue(IName)).doubleValue();
			Current += Step;
			if((From == To) || ((From < To) && (Current >= To)) || ((From > To) && (Current <= To))) return true;
			pContext.setVariableValue(IName, TKJava.tryToCastTo(Current, IType));
			return false;
		}
		@Override protected Expression onStartExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[5]);
		}
		@Override protected Expression onEachExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[6]);
		}
		@Override protected Expression onFinalExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[7]);
		}

		// Parameter index and name (for reference)
		static private final int[]    CPIndexs = new int[]    {      3,    4,      5 };
		static private final String[] CPNames  = new String[] { "from", "to", "step" };
		
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextBeforeSub(Object[] pParams,
				CompileProduct pCProduct, int pPosition) {
			// Create 
			if(!super.manipulateCompileContextBeforeSub(pParams, pCProduct, pPosition)) return false;
			
			if(!pCProduct.isCompileTimeCheckingFull()) return true;
			
			if((pParams == null) || (pParams.length < 6)) return true;
				
			if(!(pParams[1] instanceof String)) {
				pCProduct.reportError("Unable to determine the variable name", null, pPosition);
				return false;
			}

			String  VName = (String)pParams[1];
			Object  O     = pCProduct.getReturnTypeRefOf(pParams[2]);
			TypeRef TRef  = (O instanceof TypeTypeRef)?((TypeTypeRef)O).getTheRef():TKJava.TAny.getTypeRef();
			
			if(!TKJava.TNumber.getTypeRef().canBeAssignedByInstanceOf(pCProduct.getEngine(), TRef)) {
				pCProduct.reportError("The type must be a number type ("+TRef+") <Inst_FromTo:337>", null, pPosition);
				return false;
			}

			UNumber.NumberType NTVar = UNumber.NumberType.BYTE;
			NTVar = NTVar.max((Number)TRef.getDefaultValue(pCProduct.getEngine()));
			
			for(int i = 0; i < CPIndexs.length; i++) {
				TypeRef TR = pCProduct.getReturnTypeRefOf(CPIndexs[i]);
				if(TR == null) TR = TKJava.TInteger.getTypeRef();

				if(!TKJava.TNumber.getTypeRef().canBeAssignedByInstanceOf(pCProduct.getEngine(), TR)) {
					pCProduct.reportError("The "+CPNames[i]+" value must be a number ("+TR+")", null, pPosition);
					return false;
				}
				if(NTVar.max((Number)TR.getDefaultValue(pCProduct.getEngine())) != NTVar) {
					pCProduct.reportError("The "+CPNames[i]+" value cannot be assigned into the variable ("+TR+")", null, pPosition);
					return false;
				}
			}

			// Create variables for loop
			pCProduct.newConstant(VName, TRef);
			return true;
		}
	}
	static public class Inst_ForEach extends Inst_AbstractLoop {
		static public final String Name = "forEach";
		
		Type IteratorType = null;
		Type IterableType = null;
		boolean HaveAttemptToGetType = false;

		// StackName, VarName, VarType, Collection (Array, DataArray, Iterator, List), [Start Each Final]
		protected Inst_ForEach(Engine pEngine, boolean pIsAdvance) {
			super(pEngine, Name, "+$,+!,~", pIsAdvance);
			this.IsAdvance = pIsAdvance;
		}
		
		String getVarName(Context pContext, Object[] pParams) {
			return (String)pParams[1];
		}
		Type getVarType(Context pContext, Object[] pParams) {
			return (Type)pParams[2];
		}
		// Replace the data so that we can do iterable 
		int getCollectionParamIndex() {
			return 3;
		}          
		Object  getVarData(Context pContext, Object[] pParams) {
			return pParams[this.getCollectionParamIndex()];
		}          
		boolean setVarData(Context pContext, Object[] pParams, Object NewData) {
			// Can do this because pParams is now the clone one
			pParams[this.getCollectionParamIndex()] = NewData;
			return true;
		}
		
		/**{@inheritDoc}*/ @Override
		protected Object onStart(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getVarData(pContext, pParams);
			if(O == null) return null;

			MType MT        = this.Engine.getTypeManager();
			Type  VType     = this.getVarType(pContext, pParams);
			Type  ValueType = null;
			
			// Ensure if the object can be a DObject, to be a DObject
			if(O instanceof DObjectStandalone) O = ((DObjectStandalone)O).getAsDObject();
			
			// Try as DObject first
			if(O instanceof DObject) {
				if(!this.HaveAttemptToGetType) {
					this.IteratorType = MT.getTypeFromRefNoCheck(pContext, MT.getPrefineTypeRef("Iterator"));
					this.IterableType = MT.getTypeFromRefNoCheck(pContext, MT.getPrefineTypeRef("Iterable"));
					this.HaveAttemptToGetType = true;
				} 

				// If it is a iterable, make it an iterator
				if((this.IterableType != null) && this.IterableType.canBeAssignedBy(pContext, O)) {
					DObject DO     = (DObject)O;
					Object  Result = DO.invoke(pContext, pExpr, false, null, "iterator", UObject.EmptyObjectArray);
					if(Result instanceof SpecialResult)
						return Result;
					
					O = Result;
				}
				
				// If it is an iterator, extract the ValueType
				if((this.IteratorType != null) && this.IteratorType.canBeAssignedBy(pContext, O)) {
					TypeRef[] VT = TypeSpec.ExtractParameterFrom(
					                pContext,
					                this.Engine,
					                MT.getTypeOfNoCheck(pContext, O).getTypeRef(),
					                this.IteratorType.getTypeRef()
					            );
					if((VT != null) && (VT.length == 1)) {
						ValueType = MT.getTypeFromRefNoCheck(pContext, VT[0]);
						// Set the data
						if(ValueType != null)
							this.setVarData(pContext, pParams, O);
					}
					

				} else {
					// Try as a native (in case the object implement native interface that can be used)
					O = ((DObject)O).getAsNative();
				}

			// If it is an array, use it component type
			} else if(O.getClass().isArray()) {
				ValueType = MT.getTypeOfTheInstanceOf(O.getClass().getComponentType());

				if(ValueType != null) {
					// Check valid type
					TypeRef ATRef = TKArray.newArrayTypeRef(VType.getTypeRef());
					if(!MType.CanTypeRefByAssignableBy(pContext, pContext.getEngine(), ATRef, O)) {
						return new CurryError(
								"Invalid variable type for foreach loop: '"+ValueType+"' found but '"+VType+
								"' is needed <Instruction_ControlFlow:476>."
							);
					}

					this.setVarData(pContext, pParams, O);
					
				}
			}
			
			if(ValueType == null) {

				// If none of the above, try native collection
				if(ValueType == null) {
					ValueType = VType;
					if     (O instanceof Iterator<?>) this.setVarData(pContext, pParams, O);
					else if(O instanceof Iterable<?>) this.setVarData(pContext, pParams, ((Iterable<?>)O).iterator());
					else if(O instanceof   Map<?, ?>) this.setVarData(pContext, pParams, ((Map<?, ?>)O).keySet().iterator());
					else                              ValueType = null;
				}
				
				if(ValueType == null) { // Throw an error
					return new CurryError(
							"For loop cannot accept the data collection typed '"+this.Engine.getTypeManager().getTypeOf(O)+
							"'."
						);
				}
				
				// Check valid type
				if(!VType.canBeAssignedByInstanceOf(pContext, ValueType)) {
					return new CurryError(
							"Invalid variable type for foreach loop: '"+ValueType+"' found but '"+VType+
							"' is needed <Instruction_ControlFlow:476>."
						);
				}
			}
			
			pContext.newVariable(pContext.getEngine(), this.getVarName(pContext, pParams), VType, null);
			return super.onStart(pContext, pExpr, pParams);
		}		
		/**{@inheritDoc}*/ @Override
		protected Object getToEnterCond(Context pContext, Expression pExpr, Object[] pParams) {
			// Check Condition
			Object  O = this.getVarData(pContext, pParams);
			if(O == null) return false;
			
			Object  V = null;
			int     C = ((Integer)pContext.getVariableValue(LoopCountName)).intValue();
			boolean E = true;

			if(!this.HaveAttemptToGetType) {
				MType MT = this.Engine.getTypeManager();
				this.IteratorType = MT.getTypeFromRefNoCheck(pContext, MT.getPrefineTypeRef("Iterator"));
				this.IterableType = MT.getTypeFromRefNoCheck(pContext, MT.getPrefineTypeRef("Iterable"));
				this.HaveAttemptToGetType = true;
			}
			
			if(O instanceof DObject) {
				// If it a DObject, it is an Iterator
				DObject DO     = (DObject)O;
				Object  Result = DO.invoke(pContext, pExpr, false, null, "hasNext", UObject.EmptyObjectArray);
				if(Result instanceof SpecialResult) return Result;
				if(!Boolean.TRUE.equals(Result)) E = false;
				else {
					Result = DO.invoke(pContext, pExpr, false, null, "next", UObject.EmptyObjectArray);
					if(Result instanceof SpecialResult) return Result;
					V = Result;
				}
			}
			else if(O.getClass().isArray())   { if(C >= Array.getLength(O))     E = false; else V = Array.get(O, C);         }
			else if(O instanceof Iterator<?>) { if(!((Iterator<?>)O).hasNext()) E = false; else V = ((Iterator<?>)O).next(); }
			else return false;
			
			if(E) pContext.setVariableValue(this.getVarName(pContext, pParams), V);
			return E;
		}
		/**{@inheritDoc}*/ @Override
		protected Expression onStartExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[4]);
		}
		/**{@inheritDoc}*/ @Override
		protected Expression onEachExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[5]);
		}
		/**{@inheritDoc}*/ @Override
		protected Expression onFinalExpr(Context pContext, Object[] pParams) {
			return (Expression)(!this.IsAdvance?null:pParams[6]);
		}
		
		public TypeRef getContainTypeRef(CompileProduct pCProduct, TypeRef CollectionTRef, int pPosition) {
			Engine  E     = pCProduct.getEngine();
			MType   MT    = E.getTypeManager();
			
			TypeRef VTRef = null;
			
			// Since only array can be check for the component type, we only look for an array
			if(!TKArray.AnyArrayRef.canBeAssignedByInstanceOf(pCProduct.getEngine(), CollectionTRef)) {
				if(!HaveAttemptToGetType) {
					this.IteratorType = MT.getTypeFromRefNoCheck(null, MT.getPrefineTypeRef("Iterator"));
					this.IterableType = MT.getTypeFromRefNoCheck(null, MT.getPrefineTypeRef("Iterable"));
				}

				// If it is a iterable, make it an iterator
				if((this.IterableType != null) && MType.CanTypeRefByAssignableByInstanceOf(null, E, this.IterableType.getTypeRef(), CollectionTRef)) {
					ExecSignature ES = CollectionTRef.searchObjectOperation(E, "iterator", TypeRef.EmptyTypeRefArray);
					if(ES != null) CollectionTRef = ES.getReturnTypeRef();
				}
				// Check the type of the iterator
				if((this.IteratorType != null) && MType.CanTypeRefByAssignableByInstanceOf(null, E, this.IteratorType.getTypeRef(), CollectionTRef)) {
					try { pCProduct.getEngine().getTypeManager().ensureTypeInitialized(CollectionTRef); }
					catch(Exception Exec) {}
					
					ExecSignature ES = CollectionTRef.searchObjectOperation(E, "next", TypeRef.EmptyTypeRefArray);
					if(ES != null) VTRef = ES.getReturnTypeRef();
				}
				
			} else { // It's array so we can extract the component type
				try { pCProduct.getEngine().getTypeManager().ensureTypeInitialized(CollectionTRef); }
				catch(Exception Exec) {}
				
				if(!CollectionTRef.isLoaded() || !CollectionTRef.getTheType().isInitialized()) {
					pCProduct.reportWarning(
						"The compiler is unable to initialize the collection type ("+CollectionTRef+")",
						null,
						pPosition
					);
					
				} else {
					VTRef = ((TKArray.TArray)CollectionTRef.getTheType()).getContainTypeRef();
				}
			}
			
			return VTRef;
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextBeforeSub(Object[] pParams,
				CompileProduct pCProduct, int pPosition) {
			// Create 
			if(!super.manipulateCompileContextBeforeSub(pParams, pCProduct, pPosition)) return false;
			
			if(!pCProduct.isCompileTimeCheckingFull())    return true;
			if((pParams == null) || (pParams.length < 4)) return true;
				
			if(!(pParams[1] instanceof String)) {
				pCProduct.reportError("Unable to determine the variable name", null, pPosition);
				return false;
			}

			String  VName = (String)pParams[1];
			Object  O     = pCProduct.getReturnTypeRefOf(pParams[2]);
			TypeRef TRef  = (O instanceof TypeTypeRef)?((TypeTypeRef)O).getTheRef():TKJava.TAny.getTypeRef();
			TypeRef CTRef = pCProduct.getReturnTypeRefOf(pParams[3]);
			TypeRef VTRef = this.getContainTypeRef(pCProduct, CTRef, pPosition);		
			
			if(VTRef == null) {
				if(!TKJava.TAny.getTypeRef().equals(TRef)) {
					// Warn that we cannot find the component type
					pCProduct.reportWarning(
						"The collection is generic which curry is not capable to extract its component type at compile time ("+
						CTRef+")",
						null,
						pPosition
					);
				}
			} else if(!TRef.canBeAssignedByInstanceOf(pCProduct.getEngine(), VTRef)) {
				pCProduct.reportError(
					"The component type of the given collection is not assignable to the variable type " +
					"(variable type: `" + TRef + "` and collection type `" + VTRef + "`)",
					null,
					pPosition
				);
				return false;
			}
			
			// Create variables for loop
			pCProduct.newConstant(VName, TRef);
			return true;
		}
	}

	static public class Inst_Switch extends Inst_AbstractComplex {
		
		static public final String Name = "switch";
		
		static public final CaseEntry[] EmptyCaseEntries = new CaseEntry[0];
		
		final static public class CaseEntry implements Serializable {
			
			static public CaseEntry newCaseEntry(Object pCaseValue) {
				return newCaseEntry(pCaseValue, null);
			}
			static public CaseEntry newCaseEntry(Object pCaseValue, Expression pCaseBody) {
				CaseEntry CE = new CaseEntry();
				CE.CaseValue = pCaseValue;
				CE.CaseBody  = pCaseBody;
				return CE;
			}
			Object     CaseValue;
			Expression CaseBody;
			public Object     getCaseValue() { return this.CaseValue; }
			public Expression getCaseBody()  { return this.CaseBody;  }
			
			@Override
			public String toString() {
				return "case `"+CaseValue+"`: " + this.CaseBody;
			}
		}
		
		Inst_Switch(Engine pEngine, String pName, boolean IsContinue) {
			super(pEngine, pName + "($,~,+"+CaseEntry.class.getCanonicalName()+"[],E):~");
			this.isContinue = IsContinue;
		}
		Inst_Switch(Engine pEngine) {
			this(pEngine, Name, true);
		}
		
		boolean isContinue = false; 
		boolean isMatch(Context pContext, Object Select, Object Choice) {
			return (Select == Choice) || this.Engine.equals(pContext, Select, Choice);
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			String SName = ((String)pParams[0]);

			pContext = new Context(pContext, SName, pExpr);
			
			Object      Select   = pParams[1];
			CaseEntry[] CEntries = (CaseEntry[])pParams[2];
			boolean IsFound = false;
			
			for(int i = 0; i < CEntries.length; i++) {
				if(CEntries[i] == null) continue; //throw new NullPointerException("Case entry #"+i+" is null.");
				Object Value = CEntries[i].getCaseValue();
				Value = (Value instanceof Expression)
						? this.executeAnExpression(pContext, (Expression)Value)
						: Value; 
				
				if(!(IsFound || this.isMatch(pContext, Select, Value))) continue;
				IsFound = true;
				Object R = this.executeAnExpression(pContext, CEntries[i].CaseBody);
				if(!this.isContinue) return R;
				if(R instanceof SpecialResult) {
					// Trap exit.
					if(R instanceof SpecialResult.ResultExit) {
						String RName = ((SpecialResult.ResultExit)R).Name;
						if((RName == null) || (RName == SName) || RName.equals(SName))
							return ((SpecialResult.ResultExit)R).Result;
					}
					// Trap done.
					if(R instanceof SpecialResult.ResultSwitchDone) {
						String RName = ((SpecialResult.ResultSwitchDone)R).Name;
						if((RName == null) || (RName == SName) || RName.equals(SName))
							return ((SpecialResult.ResultSwitchDone)R).Result;
					}
				}
			}
			
			Expression Default = (Expression)pParams[3];
			if(Default != null) {
				Object R = this.executeAnExpression(pContext, Default);
				if(R instanceof SpecialResult) {
					// Trap exit.
					if(R instanceof SpecialResult.ResultExit) {
						String RName = ((SpecialResult.ResultExit)R).Name;
						if((RName == null) || (RName == SName) || RName.equals(SName))
							return ((SpecialResult.ResultExit)R).Result;
					}
					// Trap done.
					if(R instanceof SpecialResult.ResultSwitchDone) {
						String RName = ((SpecialResult.ResultSwitchDone)R).Name;
						if((RName == null) || (RName == SName) || RName.equals(SName))
							return ((SpecialResult.ResultSwitchDone)R).Result;
					}
				}
				// Returns true for normal exit
				return R;
			}
			// Returns false for abnormal exit
			return null;
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			
			Object CaseEntryObj = pExpr.getParam(pExpr.getParamCount() - 2);
			
			if(!(CaseEntryObj instanceof CaseEntry[]))
				return super.getReturnTypeRef(pExpr, pCProduct);
			
			TypeRef ReturnTypeRef = null;
			Object Default = pExpr.getParam(pExpr.getParamCount() - 1);
			if(Default != null) {
				if(Default instanceof Expression.Expr_Expr) {
					Expression Expr = ((Expression.Expr_Expr)Default).getExpr();
					ReturnTypeRef = (Expr == null)?null:Expr.getReturnTypeRef(pCProduct);
				} else ReturnTypeRef = pCProduct.getReturnTypeRefOf(Default);
			}
			
			CaseEntry[] CEs = (CaseEntry[])CaseEntryObj;
			if(CEs.length != 0) {
				for(int i = CEs.length; --i >= 0; ) {
					Object Body = CEs[i].getCaseBody();
					if(Body == null) continue;
					TypeRef TR = pCProduct.getReturnTypeRefOf(Body);
					
					if(ReturnTypeRef == null) ReturnTypeRef = TR;
					else {
						ReturnTypeRef = pCProduct.getEngine().getTypeManager().getClosestSharedAncessorOf(ReturnTypeRef, TR);
						// Some type cannot be resolved at compile time
						if(ReturnTypeRef == null) break;
					}
				}
			}
			return (ReturnTypeRef == null)
					?super.getReturnTypeRef(pExpr, pCProduct)
					:ReturnTypeRef;
		}
		/**{@inheritDoc}*/ @Override 
		public boolean manipulateCompileContextBeforeSub(Object[] pParams, CompileProduct pCProduct, int pPosition) {
			Object O = pParams[0];
			pCProduct.newSwitchScope((O instanceof String)?(String)O:null, TKJava.TAny.getTypeRef());
			return true;
		}
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			pCProduct.doneSwitchScope();
			return true;
		}
	}
	static public class Inst_Choose extends Inst_Switch {
		@SuppressWarnings("hiding")
		static public final String Name = "choose";
		
		Inst_Choose(Engine pEngine) {
			super(pEngine, Name, false);
		}
	}
	
	static public class Inst_Done extends Inst_AbstractSimple {
		static public final String Name = "done";
		
		Inst_Done(Engine pEngine) {
			super(pEngine, Name + "($,~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return new SpecialResult.ResultSwitchDone((String)pParams[0], pParams[1]);
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(1);
			if(O instanceof Expression) return ((Expression)O).getReturnTypeRef(pCProduct);
			return pCProduct.getEngine().getTypeManager().getTypeOf(O).getTypeRef();
		}
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			if(!pCProduct.isCompileTimeCheckingFull()) return true;
			
			Object Label = pExpr.getParam(0);
			Object Value;
			if((Label != null) && !(Label instanceof String)) {
				pCProduct.reportWarning(
						"Unable to determine the label at compile time", null, pExpr.getColumn(), pExpr.getLineNumber());
			} else if(!pCProduct.isInsideSwitchScope()) {
				pCProduct.reportWarning(
						"Done outside switch", null, pExpr.getColumn(), pExpr.getLineNumber());
			} else if((Label != null) && !pCProduct.isInsideSwitchScope((String)Label)) {
				pCProduct.reportWarning(
						"Label not found ("+Label+")", null);
			} else if(!pCProduct.canDoneSwitchScope((String)Label, Value = pExpr.getParam(1))) {
				pCProduct.reportWarning(
						String.format("Incompatible return type: %s needed but %s found", 
							pCProduct.getClosestSignature().getReturnTypeRef(),
							pCProduct.getReturnTypeRefOf(Value)
						),
						null, pExpr.getColumn(), pExpr.getLineNumber());
			}
			return true;
		}
	}

	static public class Inst_Throw extends Inst_AbstractSimple {
		static public final String Name = "throw";
		
		Inst_Throw(Engine pEngine) {
			super(pEngine, Name + "(+"+Throwable.class.getCanonicalName()+"):^");
		}
		/**{@inherDoc}*/ @Override protected Object run(Context pContext, Object[] pParams) {
			return new SpecialResult.ResultError(pParams[0]);
		}
	}
	@SuppressWarnings("unchecked")
	static public class Inst_NewThrowable extends Inst_AbstractSimple {
		// String Message, Class<Exception> Class, Throwable Cause
		static public final String Name = "newThrowable";
		
		Inst_NewThrowable(Engine pEngine) {
			super(pEngine, Name + "(+$,@,"+Throwable.class.getCanonicalName()+"):"+ Throwable.class.getCanonicalName());
		}

		static final Class<?>[] CurryFullConstructorClass = new Class<?>[] { String.class, Context.class, Throwable.class };
		static final Class<?>[] CurryHalfConstructorClass = new Class<?>[] { String.class, Context.class                  };
		static final Class<?>[] CurryNoneConstructorClass = new Class<?>[] { Context.class };
		
		static final Class<?>[] FullConstructorClass  = new Class<?>[] { String.class,                Throwable.class };
		static final Class<?>[] HalfConstructorClass  = new Class<?>[] { String.class                                 };
		static final Class<?>[] NoneConstructorClass  = new Class<?>[] {};
		
		/**{@inherDoc}*/ @Override protected Object run(Context pContext, Object[] pParams) {
			Class<?> Cls = (Class)pParams[1];
			if(Cls == null) Cls = CurryError.class;
			if(!Throwable.class.isAssignableFrom(Cls)) {
				return this.reportParameterError(this.getEngine(), pContext, "The parameter must be a throwable class", pParams);
			}
			Class<? extends Throwable> CT = (Class<? extends Throwable>)pParams[1];

			String    M = (String)   pParams[0];
			Throwable C = (Throwable)pParams[2];
			
			if(CurryError.class.isAssignableFrom(CT)) {
				if(UClass.getConstructorByParamClasses(CT, CurryFullConstructorClass) != null)
					try { return UClass.newInstance(CT, new Object[] { M, pContext, C }); } catch(Exception E) {}
				if(UClass.getConstructorByParamClasses(CT, CurryHalfConstructorClass) != null)
					try { return UClass.newInstance(CT, new Object[] { M, pContext    }); } catch(Exception E) {}
				if(UClass.getConstructorByParamClasses(CT, CurryNoneConstructorClass) != null)
					try { return UClass.newInstance(CT, new Object[] { pContext       }); } catch(Exception E) {}
			}
			
			Object    T = null;
			Exception E = null;
			try {
				if(     UClass.getConstructorByParamClasses(CT, FullConstructorClass) != null) T = UClass.newInstance(CT, new Object[] { M, C });
				else if(UClass.getConstructorByParamClasses(CT, HalfConstructorClass) != null) T = UClass.newInstance(CT, new Object[] { M    });
				else if(UClass.getConstructorByParamClasses(CT, NoneConstructorClass) != null) T = UClass.newInstance(CT, new Object[] {      });
				return new SpecialResult.ResultError((Throwable)T);
			} catch(Exception Excp) { E = Excp; }
			
			return new CurryError(
					"An error occurs while creating a java throwable named " +
					(((Class)pParams[1]).getCanonicalName())+".",
					pContext,
					E
				);
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(1);
			if((O instanceof Class) && (pCProduct != null))
				return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf((Class)O).getTypeRef();
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}

	static public class Inst_TryOrElse extends Inst_AbstractSimple {
		static public final String Name = "tryOrElse";
		
		Inst_TryOrElse(Engine pEngine) {
			super(pEngine, Name + "(E,E):~");
		}

		/**@inherDoc()*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object R = null;
			
			try {
				R = pContext.getExecutor().execInternal(pContext, (Expression)pParams[0]);
				if(!(R instanceof SpecialResult.ResultError)) return R;
			} catch(Exception E) {}
			
			return pContext.getExecutor().execInternal(pContext, (Expression)pParams[1]);
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O0 = pExpr.getParam(0);
			Object O1 = pExpr.getParam(1);
			
			if(!(O0 instanceof Expr_Expr) || !(O1 instanceof Expr_Expr)) return TKJava.TAny.getTypeRef();
			
			TypeRef TR0 = pCProduct.getReturnTypeRefOf(((Expr_Expr)O0).getExpr());
			TypeRef TR1 = pCProduct.getReturnTypeRefOf(((Expr_Expr)O1).getExpr());
			
			if((TR0 == null) || (TR1 == null)) return TKJava.TAny.getTypeRef();
			
			TypeRef TR = pCProduct.getEngine().getTypeManager().getClosestSharedAncessorOf(TR0, TR1);
			return (TR != null)?TR:TKJava.TAny.getTypeRef();
		}
	}
	
	// NOTE: CatchBody and Finally Body must be compiled as if it is wrapped by a stack.
	static public class Inst_TryCatch extends Inst_AbstractStack {
		
		static public final String IsCatchedName   = "$IsCatched$";
		static public final String ThrownName      = "$Thrown$";
		static public final String ReturnValueName = "$ReturnValue$";
		
		final static public class CatchEntry implements Serializable {
			static public CatchEntry newCatchEntry(Engine pEngine, String pCatchName,
					Class<? extends Throwable> pCatchType, Expression pCatchBody) {
				
				if(pCatchName == null) throw new NullPointerException("Catch name is null.");
				if(pCatchType == null) throw new NullPointerException("Catch type is null.");
					
				Type T = pEngine.getTypeManager().getTypeOfTheInstanceOf(pCatchType);
				if(T == null) throw new IllegalArgumentException("Catch type does not have a curry type.");
				
				CatchEntry CE = new CatchEntry();
				CE.CatchName = pCatchName;
				CE.CatchTRef = T.getTypeRef();
				CE.CatchBody = pCatchBody;
				return CE;
			}
			static public CatchEntry newCatchEntry(Engine pEngine, String pCatchName, TypeRef pCatchTRef,
					Expression pCatchBody) {
				
				if(pCatchName == null) throw new NullPointerException("Catch name is null.");
				if(pCatchTRef == null) throw new NullPointerException("Catch type is null.");
				
				Class<?> Cls = pEngine.getTypeManager().getDataClassOf(pCatchTRef);
				if((Cls == null) || !Throwable.class.isAssignableFrom(Cls))
					throw new IllegalArgumentException("Catch type is not a throwable.");
					
				CatchEntry CE = new CatchEntry();
				CE.CatchName = pCatchName;
				CE.CatchTRef = pCatchTRef;
				CE.CatchBody = pCatchBody;
				return CE;
			}
			String     CatchName;
			TypeRef    CatchTRef;
			Expression CatchBody;
			public String     getCatchName()    { return this.CatchName; }
			public TypeRef    getCatchTypeRef() { return this.CatchTRef; }
			public Expression getCatchBody()    { return this.CatchBody; }
			
			@Override public String toString() {
				return String.format("catch (%s %s) { %s }", this.getCatchTypeRef(), this.getCatchName(), this.getCatchBody());
			}
		}
		
		// (String StackName, String[] VarNames, Class<Throwable>[] Catches, E[] CatchBodies, E Finally) { Body }
		Inst_TryCatch(Engine pEngine, String          pISpecStr) { super(pEngine, pISpecStr); }
		Inst_TryCatch(Engine pEngine, InstructionSpec pISpec)    { super(pEngine, pISpec);    }
		
		@SuppressWarnings("hiding") static public final String Name = "tryCatch";
		
		Inst_TryCatch(Engine pEngine) {
			super(pEngine, Name + "($,"+CatchEntry.class.getCanonicalName()+"[],E){}:~");
		}

		/**{@inherDoc}*/ @Override
		protected String getStackName(Object[] pParams) {
			return (String)((pParams == null)?null:pParams[0]);
		}
		// Execution --------------------------------------------------------------
		/**{@inherDoc}*/ @Override
		protected Object runGroup(Context pContext, Expression pExpr, Object[] pParams) {
			Object R = this.preGroup(pContext, pExpr, pParams);
			if(R != null) return R;
			// Do the body and cache the problem
			try { R = this.doGroupBody(pContext, pExpr, pParams); }
			catch(Throwable T) {
				if(T instanceof CurryError)
					 R = new SpecialResult.ResultError(T);
				else R = new SpecialResult.ResultError(new CurryError((String)pParams[0], pContext, T));
			}
			return this.postGroup(pContext, pExpr, pParams, R);
		}
		// Execution --------------------------------------------------------------
		/**{@inherDoc}*/ @Override
		protected Object postGroup(Context pContext, Expression pExpr, Object[] pParams, Object pLastResult) {
			// Do catch first
			boolean   IsMatch = false;
			Throwable Thrown  = null;
			if(pLastResult instanceof SpecialResult.ResultError) {
				SpecialResult.ResultError RE = (SpecialResult.ResultError)pLastResult;
				while(RE.Cause instanceof SpecialResult.ResultError) RE = (SpecialResult.ResultError)RE.Cause;
				if(RE.Cause == null) {
					// Throw an error
					Thrown = new CurryError("Internal Error: A result error does not contain an exception.(Instructions_ControlFlow.java#450)", pContext);
				} else Thrown = ((Throwable)RE.Cause);
				
				CatchEntry[] CatchEntries = (CatchEntry[])pParams[1];
				if(CatchEntries != null) {
					MainLoop: for(int i = 0; i < CatchEntries.length; i++) {
						if(CatchEntries[i] == null) throw new NullPointerException("Catch entry #"+i+" is null.");
						Throwable T = Thrown;
						// If match
						TypeRef TRef = CatchEntries[i].CatchTRef;
						if(!TRef.canBeAssignedBy(pContext.getEngine(), T)) {
							if((T instanceof CurryError) && (((CurryError)T).getCause() != null)) {
								while(((T instanceof CurryError) && (((CurryError)T).getCause() != null))) {
									T = ((CurryError)T).getCause();
									if((T == null) || !TRef.canBeAssignedBy(pContext.getEngine(), T)) continue MainLoop;
								}  
							} else continue;
						}
						
						// Execute the body
						Context NewContext = new Context(pContext, null, pExpr);

						NewContext.newVariable(NewContext.getEngine(), CatchEntries[i].CatchName, CatchEntries[i].CatchTRef.getTheType(), T);
						Object R = this.executeAnExpression(NewContext, CatchEntries[i].CatchBody);
						if(R instanceof SpecialResult) {
							// Trap break.
							if((R instanceof SpecialResult.ResultExit) && (((SpecialResult.ResultExit)R).Name == null))
								return ((SpecialResult.ResultExit)R).Result;
							else return R;
						}
						IsMatch = true;
						break;
					}
				}
			}
			
			// Do final
			Expression FinalExpr = (Expression)pParams[2];
			if(FinalExpr != null) {
				Context NewContext = new Context(pContext, null, pExpr);
				
				NewContext.newVariable(NewContext.getEngine(), IsCatchedName,   TKJava.TBoolean,   IsMatch);
				NewContext.newVariable(NewContext.getEngine(), ThrownName,      TKJava.TThrowable, Thrown);
				NewContext.newVariable(NewContext.getEngine(), ReturnValueName, TKJava.TAny,       pLastResult);
				Object R = this.executeAnExpression(pContext, FinalExpr);
				if(R instanceof SpecialResult) {
					// Trap break.
					if((R instanceof SpecialResult.ResultExit) && (((SpecialResult.ResultExit)R).Name == null))
						return ((SpecialResult.ResultExit)R).Result;
					else return R;
				}
			}
			return !IsMatch;
		}
	}
	
	static public class Inst_TryCast extends Inst_AbstractStack {
		static public final String Name = "tryCast";
		
		// StackName, Variable name, Type to cast to, OrElse
		Inst_TryCast(Engine pEngine, String          pISpecStr) { super(pEngine, pISpecStr); }
		Inst_TryCast(Engine pEngine, InstructionSpec pISpec)    { super(pEngine, pISpec);    }
		
		Inst_TryCast(Engine pEngine) {
			super(pEngine, Name + "($,+$,+!,~,E,E){}:~");
		}
		
		/**{@inherDoc}*/ @Override
		protected String getStackName(Object[] pParams) {
			return (String)((pParams == null)?null:pParams[0]);
		}
		// Execution --------------------------------------------------------------
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = pParams[3];
			Type   T = (Type)pParams[2];
			Object V = O;
			boolean CanCast = (
				(O != null) &&
				(
					T.canBeAssignedBy(O) ||
					((O != null) && !(O instanceof DObjectStandalone) && ((V = TKJava.tryToCastTo(O, T)) != null))
				)
			);
			Object Return = null;
			if(CanCast) {
				pContext.newVariable(pContext.getEngine(), (String)pParams[1], T, V);
				Return = super.runGroup(pContext, pExpr, pParams);
			} else {
				// Process the else
				Expression Else = (Expression)pParams[4];
				Return = pContext.getExecutor().execInternal(pContext, Else);
			}
			Expression Finally = (Expression)pParams[5];
			if(Finally != null) Return = pContext.getExecutor().execInternal(pContext, Finally);
			return Return;
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pCProduct.getReturnTypeRefOf(pExpr.getParam(2));
			if(!(O instanceof TypeTypeRef)) return new TypeTypeRef(TKJava.TAny.getTypeRef());
			return ((TypeTypeRef)O).getTheRef();
		}
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextBeforeSub(Object[] pParams, CompileProduct pCProduct, int pPosition) {
			// Create 
			if(!super.manipulateCompileContextBeforeSub(pParams, pCProduct, pPosition)) return false;
			
			Object O1 = pParams[1];
			Object O2 = pParams[2];
			if(!(O1 instanceof String) || !(O2 instanceof Expression) || !Expression.isInstruction(((Expression)O2), pCProduct.getEngine(), "type")) return true;
			TypeRef TRef = pCProduct.getReturnTypeRefOf(O2);
			TRef = ((TypeTypeRef)TRef).getTheRef();
			pCProduct.newConstant((String)O1, TRef);			
			return true;
		}
	}
}
