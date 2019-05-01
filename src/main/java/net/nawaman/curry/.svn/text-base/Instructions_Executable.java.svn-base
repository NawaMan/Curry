package net.nawaman.curry;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

import net.nawaman.curry.Executable.ExecKind;
import net.nawaman.curry.Executable.SubRoutine;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.TKType.TType;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.curry.script.CurryScope;
import net.nawaman.curry.util.MoreData;
import net.nawaman.script.FrozenVariableInfos;
import net.nawaman.util.UArray;

public class Instructions_Executable {

	static TypeRef getTypeRefOfClass(Engine E, Class<?> C) {
		return E.getTypeManager().getTypeOfTheInstanceOf(C).getTypeRef();
	}
	static TypeRef getReturnTypeRefOfScriptFunction(Engine E, net.nawaman.script.Function F) {
		return E.getTypeManager().getTypeOfTheInstanceOf(F.getSignature().getReturnType()).getTypeRef();
	}
	
	/** Returns the context-awareness return type */
	static TypeRef getReturnExecutableTypeRef(Expression pExpr, CompileProduct pCProduct) {
		TypeRef TRef = pCProduct.getReturnTypeRefOf(pExpr.getParam(0));
		while(TRef instanceof TypeTypeRef) TRef = ((TypeTypeRef)TRef).getTheRef();
		if(TRef == null) return null;
		
		Engine E = pCProduct.getEngine();
		try { E.getTypeManager().ensureTypeInitialized(TRef); } catch(Exception Eecp) { return null; }
		return (TRef.getTheType() instanceof TKExecutable.TExecutable)
					?((TKExecutable.TExecutable)TRef.getTheType()).getSignature().getReturnTypeRef()
					:null;
	}
	
	// Execute an executable as a fragment ---------------------------------------------------------
	static public class Inst_AbstractRun extends Inst_AbstractSimple {
		Inst_AbstractRun(Engine pEngine, String ISpecStr, boolean pIsUnSefe) {
			super(pEngine, ISpecStr);
			this.IsUnSefe = pIsUnSefe;
		}
		
		boolean IsUnSefe;
		
		Object getExecutable(Context pContext, Object[] pParams) {
			return pParams[0];
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object E = this.getExecutable(pContext, pParams);
			if(E instanceof Expression) {
				return pContext.getExecutor().execInternal(pContext, (Expression)E);
				
			} else if(E instanceof Executable) {
				Object   SO = pContext.getStackOwner();
				return pContext.getExecutor().execExecutable(pContext, (Executable)E, (Executable)E, ExecKind.Fragment,
						false, SO, null, false, !this.IsUnSefe);
				
			} else if(E instanceof net.nawaman.script.Script) {
				net.nawaman.script.Script S   = (net.nawaman.script.Script)E;
				CurryScopeContext         CSC = null; 
				try { CSC = new CurryScopeContext(pContext); return S.run(CSC); }
				finally { CSC.TheContext = null; }
				
			}
			return this.reportParameterError(pContext.getEngine(), pContext, "Parameter #0 is not a fragment nor a script", pParams);
		}
		/**{@inherDoc}*/ @Override public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			TypeRef TR = getReturnExecutableTypeRef(pExpr, pCProduct);
			if(TR == null) return super.getReturnTypeRef(pExpr, pCProduct);
			return TR;
		}
	}
	static public class Inst_Run extends Inst_AbstractRun {
		static public final String Name = "run";
		Inst_Run(Engine pEngine) {
			super(pEngine, Name + "(~):~", false);
		}
	}
	static public class Inst_RunSelf extends Inst_AbstractRun {
		static public final String Name = "runSelf";
		
		Inst_RunSelf(Engine pEngine) {
			super(pEngine, Name + "():~", true);
		}
		/**{@inherDoc}*/ @Override
		Object getExecutable(Context pContext, Object[] pParams) {
			return pContext.getExecutable();
		}
	}
	// Execute an executable as a fragment ---------------------------------------------------------
	static public class Inst_Run_Unsafe extends Inst_AbstractRun {
		static public final String Name = "run_Unsafe";
		
		Inst_Run_Unsafe(Engine pEngine) {
			super(pEngine, Name + "(~):~", true);
		}
	}
	
	// Execute an executable as a macro ------------------------------------------------------------

	static public class Inst_AbstractExec extends Inst_AbstractComplex {
		Inst_AbstractExec(Engine pEngine, String pISpecStr, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine,  pISpecStr);
			this.IsAdjusted = pIsAdjusted;
		}

		boolean IsAdjusted;

		int getRawParametersIndex() {
			return 1;
		}
		Object getExecutable(Context pContext, Object[] pParams) {
			return pParams[0];
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object E = this.getExecutable(pContext, pParams);
			
			if(E instanceof Executable) {
				Executable M  = (Executable)E;
				Object     SO = pContext.getStackOwner();
				Object[]   Ps = UArray.getObjectArray(pParams[this.getRawParametersIndex()]);
				return pContext.getExecutor().execExecutable(pContext, M, M, ExecKind.Macro, false, SO, Ps, this.IsAdjusted, true);
				
			} else if(E instanceof net.nawaman.script.Macro) {
				net.nawaman.script.Macro M   = (net.nawaman.script.Macro)E;
				CurryScopeContext        CSC = null;
				Object[]                 Ps  = UArray.getObjectArray(pParams[this.getRawParametersIndex()]);
				try { CSC = new CurryScopeContext(pContext); pParams[0] = CSC; return M.run(CSC, (Object[])Ps); }
				finally { CSC.TheContext = null; }
				
			}
			return this.reportParameterError(pContext.getEngine(), pContext, "Parameter #0 is not a macro", pParams);
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			TypeRef TR = getReturnExecutableTypeRef(pExpr, pCProduct);
			if(TR == null) return super.getReturnTypeRef(pExpr, pCProduct);
			return TR;
		}
	}
	
	// Execute an executable as a macro --------------------------------------------------
	static public class Inst_Exec extends Inst_AbstractExec {
		static public final String Name = "exec";
		
		Inst_Exec(Engine pEngine, boolean pIsAdjusted) {
			super(pEngine, Name + "(~,~...):~", false, pIsAdjusted);
		}
	}
	// Execute an executable as a macro --------------------------------------------------
	static public class Inst_Exec_Blind extends Inst_AbstractExec {
		static public final String Name = "exec_Blind";
		
		Inst_Exec_Blind(Engine pEngine, boolean pIsAdjusted) {
			super(pEngine, Name + "(~,~...):~", false, pIsAdjusted);
		}
	}
	// Re-execute the current executable as a macro. -------------------------------------
	static public class Inst_ExecSelf extends Inst_AbstractExec {
		static public final String Name = "execSelf";
		
		Inst_ExecSelf(Engine pEngine, boolean pIsAdjusted) {
			super(pEngine, Name + "(~...):~", false, pIsAdjusted);
		}
		/**{@inherDoc}*/ @Override
		int getRawParametersIndex() {
			return 0;
		}
		/**{@inherDoc}*/ @Override
		Object getExecutable(Context pContext, Object[] pParams) {
			return pContext.getExecutable();
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			ExecSignature Signature = pCProduct.getClosestMacroSignature();
			if(Signature == null) return super.getReturnTypeRef(pExpr, pCProduct);
			return Signature.getReturnTypeRef();
		}
	}
	
	// Execute an executable as a sub-routine ------------------------------------------------------
	
	// Execute an executable as a sub-routine --------------------------------------------
	static public class Inst_AbstractCall extends Inst_AbstractComplex {
		Inst_AbstractCall(Engine pEngine, String pISpecStr, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine, pISpecStr);
			this.IsBlindCaller = pIsBlindCaller;
			this.IsAdjusted    = pIsAdjusted;
		}
		
		boolean IsBlindCaller;
		boolean IsAdjusted;

		int getRawParametersIndex() {
			return 1;
		}
		Object getExecutable(Context pContext, Object[] pParams) {
			return pParams[0];
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object E = this.getExecutable(pContext, pParams);
			
			if(E instanceof Executable) {
				Executable S  = (Executable)E;
				Object     SO = pContext.getStackOwner();
				Object[]   Ps = UArray.getObjectArray(pParams[this.getRawParametersIndex()]);
				return pContext.getExecutor().execExecutable(pContext, S, S, ExecKind.SubRoutine, this.IsBlindCaller,
						SO, Ps, this.IsAdjusted, true);
				
			} else if(E instanceof net.nawaman.script.Function) {
				net.nawaman.script.Function F   = (net.nawaman.script.Function)E;
				CurryScopeContext           CSC = null;
				Object[]                    Ps  = UArray.getObjectArray(pParams[this.getRawParametersIndex()]);
				try { CSC = new CurryScopeContext(pContext); pParams[0] = CSC; return F.run(CSC, (Object[])Ps); }
				finally { CSC.TheContext = null; }
				
			}
			return this.reportParameterError(pContext.getEngine(), pContext, "Parameter #0 is not a sub-routine nor a function.", pParams);
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			TypeRef TR = getReturnExecutableTypeRef(pExpr, pCProduct);
			if(TR == null) return super.getReturnTypeRef(pExpr, pCProduct);
			return TR;
		}
	}

	// Re-execute the current executable as a subroutine. --------------------------------
	static public class Inst_Call extends Instructions_Executable.Inst_AbstractCall {
		static public final String Name = "call";
		
		Inst_Call(Engine pEngine, boolean pIsAdjusted) {
			super(pEngine, Name + "(~,~...):~", false, pIsAdjusted);
		}
	}
	// Re-execute the current executable as a subroutine. --------------------------------
	static public class Inst_Call_Blind extends Instructions_Executable.Inst_AbstractCall {
		static public final String Name = "call_Blind";
		
		Inst_Call_Blind(Engine pEngine, boolean pIsAdjusted) {
			super(pEngine, Name + "(~,~...):~", true, pIsAdjusted);
		}
	}
	// Re-execute the current executable as a subroutine. --------------------------------
	static public class Inst_CallSelf extends Instructions_Executable.Inst_AbstractCall {
		static public final String Name = "callSelf";
		
		Inst_CallSelf(Engine pEngine, boolean pIsAdjusted) {
			super(pEngine, Name + "(~...):~", false, pIsAdjusted);
		}
		/**{@inherDoc}*/ @Override
		int getRawParametersIndex() {
			return 0;
		}
		/**{@inherDoc}*/ @Override
		Object getExecutable(Context pContext, Object[] pParams) {
			return pContext.getExecutable();
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			ExecSignature Signature = pCProduct.getSubRoutineSignature();
			if(Signature == null) return super.getReturnTypeRef(pExpr, pCProduct);
			return Signature.getReturnTypeRef();
		}
	}
	
	// Returns -------------------------------------------------------------------------------------
	
	static public class Inst_Return extends Inst_AbstractSimple {
		static public final String Name = "return";
		
		Inst_Return(Engine pEngine) {
			super(pEngine, Name + "(~):^");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object  Result    = pParams[0];
			boolean IsProblem = false;
			// Local executable should not be returned out of its created Context
			if(Result instanceof Executable) {
				ExecSignature ES = ((Executable)Result).getSignature();
				if(Result instanceof Closure) {
					if(((Closure)Result).getTheContext() == pContext) IsProblem = true;
				} else {
					MoreData MD = ES.getExtraData();
					if((MD != null) && Boolean.TRUE.equals(MD.getData(CompileProduct.MDName_IsLocal))) {
						// See if this context is the one creating it
						String N = ES.getName();
						if(pContext.isLocalVariableExist(N) && (pContext.getValue(pContext.getEngine(), N) == Result))
							IsProblem = true;
					}
				}
				if(IsProblem) {
					return new SpecialResult.ResultError(
							new CurryError(String.format(
								"Local executable cannot be returned out of its creating context (%s).", ES),
								pContext, null));
				}
			}
			return new SpecialResult.ResultReturn(Result);
		}
		/**{@inherDoc}*/ @Override
		public boolean ensureParamCorrect(Expression E, CompileProduct CP) {
			return this.ensureParamCorrect(E, CP, (E.getParam(0) == null));
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(0));
		}
		/**{@inheritDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			if(!pCProduct.isCompileTimeCheckingFull()) return true;
			
			Object Value;
			if(!pCProduct.isInsideMacroOrSubRoutine()) {
				pCProduct.reportWarning("Return outside Macro/SubRoutine", null, pExpr.getColumn(), pExpr.getLineNumber());
			} else if(!pCProduct.canReturnExecutableScope(Value = pExpr.getParam(0))) {
				pCProduct.reportError(
					String.format("Incompatible return type: %s needed but %s found", 
						pCProduct.getClosestSignature().getReturnTypeRef(),
						pCProduct.getReturnTypeRefOf(Value)
					),
					null, pExpr.getColumn(), pExpr.getLineNumber());
			}
			return true;
		}
	}
	
	// Recreate ------------------------------------------------------------------------------------
	
	static public class Inst_ReCreate extends Inst_AbstractSimple {
		static public final String Name = "reCreate";
		
		Inst_ReCreate(Engine pEngine) {
			super(pEngine, Name + "("+Executable.class.getCanonicalName()+"):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object  P = pParams[0];
			boolean IsExecutable = (P instanceof Executable);
			boolean IsScript     = (P instanceof net.nawaman.script.Executable);
			
			if(IsExecutable) {
				// Check for every early return
				Executable E = (Executable)P;
				while(E instanceof WrapperExecutable) E = (WrapperExecutable)E;
				if(!(E instanceof AbstractExecutable)) return E;
				AbstractExecutable AE  = (AbstractExecutable)E;
				/*
				Engine $Engine = pContext.getEngine();
				MType  $MT     = $Engine.getTypeManager();
*/
				Scope   S            = new Scope();
				Scope   AES          = AE.getFrozenScope();
				boolean IsEmptyScope = (AES == AbstractExecutable.EmptyScope);
				for(int i = AE.getFrozenVariableCount(); --i >= 0; ) {
					if(S == null) S = new Scope();
					// Get the frozen variable name
					String FName = AE.getFrozenVariableName(i);
					if(FName == null) continue;
					
					// Get the frozen variable type
					Type FType = TKJava.TAny;
					if(!IsEmptyScope && AES.isVariableExist(FName))
						FType = AES.getType(pContext.getEngine(), FName);
					else {
						/*
						// TODO - Use this one (but have to fix a bug)
						TypeRef FTRef = AE.getFrozenVariableTypeRef($Engine, i);
						FTRef = (FTRef == null) ? TKJava.TAny.getTypeRef() : FTRef;		
						FType = $MT.getTypeFromRef(pContext, FTRef);
						*/		
						FType = TKJava.TAny;
					}
					
					// TODO This is a Hack
					if("$Type$".equals(FName) && !(FType instanceof TType))
						FType = pContext.getEngine().getTypeManager().getTypeOf(FType);
						

					// Get the value
					Object FValue = null;
					if(pContext.isVariableExist(FName))
						FValue = pContext.getValue(pContext.getEngine(), FName);
					
					S.newConstant(FName, FType, FValue);
				}
				
				return AE.reCreate(pContext.getEngine(), S);
			
			} else if(IsScript) {
				
				FrozenVariableInfos FVInfos = ((net.nawaman.script.Executable)P).getFVInfos();
				
				CurryScope S = null;
				if((FVInfos != null) && (FVInfos.getFrozenVariableCount() != 0)) {
					Scope FVS = new Scope();
					for(int i = FVInfos.getFrozenVariableCount(); --i >= 0; ) {
						if(FVS == null) FVS = new Scope();
						// Get the frozen variable name
						String FName = FVInfos.getFrozenVariableName(i);
						if(FName == null) continue;
						// Get the frozen variable type
						Type FType = pContext.getEngine().getTypeManager().
										getTypeOfTheInstanceOf(FVInfos.getFrozenVariableType(i));
						if(FType == null) FType = TKJava.TAny;
						// Get the value
						Object FValue = null;
						if(pContext.isVariableExist(FName)) FValue = pContext.getValue(pContext.getEngine(), FName);
						FVS.newConstant(FName, FType, FValue);
					}
					if(FVS != null) S = new CurryScope(pContext.getEngine(), FVS);
				}
				
				// No recreation
				if(S == null) return P;

				// ReCreate
				return ((net.nawaman.script.Executable)P).reCreate(S);
			}
			
			throw new CurryError("The current context is not in an curry executable or executable script.", pContext);
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object Param = pExpr.getParam(0);
			if(Param instanceof Executable) {
				if(((Executable)Param).isFragment())   return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(Executable.Fragment  .class).getTypeRef();
				if(((Executable)Param).isMacro())      return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(Executable.Macro     .class).getTypeRef();
				if(((Executable)Param).isSubRoutine()) return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(Executable.SubRoutine.class).getTypeRef();
			}
			return pCProduct.getReturnTypeRefOf(Param);
		}
	}
	
	// Creates a new closure from a subroutine -------------------------------------------------------------------------
	
	static public class Inst_NewClosure extends Inst_AbstractSimple {
		static public final String Name = "newClosure";
		
		Inst_NewClosure(Engine pEngine) {
			super(pEngine, Name+"("+Executable.class.getCanonicalName()+"):"+SubRoutine.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return new Closure(pContext, (Executable)pParams[0]);
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			TypeRef TR = pCProduct.getReturnTypeRefOf(pExpr.getParam(0));
			Type    T  = null;
			try {
				pCProduct.getEngine().getTypeManager().getTypeFromRef(TR);
				T = TR.getTheType();
				
				TKExecutable TKE = (TKExecutable)pCProduct.getEngine().getTypeManager().getTypeKind(TKExecutable.KindName);
				TKExecutable.TExecutable TE = (TKExecutable.TExecutable)T;
				
				StringBuffer SB = new StringBuffer();
				TypeRef TRef = TKE.getNoNameTypeRef(
						ExecKind.SubRoutine,
						TE.getSignature().getInterface(),
						TE.getSignature().getExtraData(), SB);
				
				if(SB.length() != 0) {
					pCProduct.reportError(String.format(
							"There is an error constructing a no name sub-routine type for '%s' <Instruction_Executable:382>",
							TR
						),
						null, pExpr.getColumn(), pExpr.getLineNumber());
					return null;
				}
				
				return TRef;
			} catch(Exception E) {}
			
			return TKJava.TSubRoutine.getTypeRef();
		}
	}
	
	/** Script Scope wrapping Curry Context */
	static class CurryScopeContext implements net.nawaman.script.Scope {
		
		CurryScopeContext(Context pTheContext) {
			this.TheContext = pTheContext;
		}
		
		net.nawaman.curry.Context TheContext = null;
		
		/**
		 * Returns a variable and constant names
		 * 
		 * This method is just to satisfy net.nawaman.script.Scope. Its implementation is heavy so it is not recommend that
		 *     this method to be used in the job where performance is needed.
		 * The reason for this is that Curry Scope may have parent. Listing all the variable means to includes all the
		 *     variables in the parent and their parents.
		 **/
		public Set<String> getVariableNames() {
			return net.nawaman.curry.Scope.getVariableNamesOf(this.TheContext);
		}
		
		/**
		 * Returns the variable count
		 * 
		 * This method is just to satisfy net.nawaman.script.Scope. Its implementation is heavy so it is not recommend that
		 *     this method to be used in the job where performance is needed.
		 * The reason for this is that Curry Scope may have parent. Listing all the variable means to includes all the
		 *     variables in the parent and their parents.
		 * NOTE: This method use Scope.StandAlone.getVariableNames(); therefore, if you need to also get variable names
		 *       user the one above and ask its for the size instead of calling this method.
		 **/
		public int getVarCount() {
			Set<String> S = net.nawaman.curry.Scope.getVariableNamesOf(this.TheContext);
			return (S == null)?0:S.size();
		}
		
		/**{@inheritDoc}*/@Override
		public Object getValue(String pName) {
			return this.TheContext.getValue(this.TheContext.getEngine(), pName);
		}
		/**{@inheritDoc}*/@Override
		public Object setValue(String pName, Object pValue) {
			return this.TheContext.setValue(this.TheContext.getEngine(), pName, pValue);
		}
		/**{@inheritDoc}*/@Override
		public boolean newVariable(String pName, Class<?> pType, Object pValue) {
			if(this.isExist(pName)) return false;
			try {
				Engine E = this.TheContext.getEngine();
				this.TheContext.newVariable(E, pName, E.getTypeManager().getTypeOfTheInstanceOf(pType), pValue);
				return true;
			} catch (Exception E) { return false; }
		}
		/**{@inheritDoc}*/@Override
		public boolean newConstant(String pName, Class<?> pType, Object pValue) {
			if(this.isExist(pName)) return false;
			try {
				Engine E = this.TheContext.getEngine();
				this.TheContext.newConstant(E, pName, E.getTypeManager().getTypeOfTheInstanceOf(pType), pValue);
				return true;
			} catch (Exception E) { return false; }
		}
		
		/**{@inheritDoc}*/@Override
		public boolean removeVariable(String pName) {
			return this.TheContext.removeVariable(pName);
		}
		
		/**{@inheritDoc}*/@Override
		public Class<?> getTypeOf(String pName) {
			return this.TheContext.getType(this.TheContext.getEngine(), pName).getDataClass();
		}
		/**{@inheritDoc}*/@Override
		public boolean isExist(String pName) {
			return this.TheContext.isVariableExist(pName);
		}
		/**{@inheritDoc}*/@Override
		public boolean isWritable(String pName) {
			return !this.TheContext.isVariableConstant(pName);
		}
		
		/** Checks if this scope support constant declaration */
		public boolean isConstantSupport() { return true; }
		
		Writer Out = new OutputStreamWriter(System.out);
		Writer Err = new OutputStreamWriter(System.err);
		Reader In  = new InputStreamReader( System.in);
	    
		/**{@inheritDoc}*/ @Override
		public void setWriter(Writer pWriter) {
			this.Err = (pWriter != null)?pWriter:Simple.DErr;
		}
	    /**{@inheritDoc}*/ @Override
	    public void setErrorWriter(Writer pWriter) {
	    	this.Err = (pWriter != null)?pWriter:Simple.DErr;
	    }
	    /**{@inheritDoc}*/ @Override
	    public void setReader(Reader pReader) {
	    	this.In  = (pReader != null)?pReader:Simple.DIn;
	    }

	    /**{@inheritDoc}*/ @Override
	    public Writer getWriter() {
	    	return Out;
	    }
	    /**{@inheritDoc}*/ @Override
	    public Writer getErrorWriter() {
	    	return Err;
	    }
	    /**{@inheritDoc}*/ @Override
	    public Reader getReader() {
	    	return In;
	    }
	    
	}
}
