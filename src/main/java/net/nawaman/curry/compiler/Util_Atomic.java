package net.nawaman.curry.compiler;

import net.nawaman.curry.Context;
import net.nawaman.curry.CurryError;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Instruction;
import net.nawaman.curry.Instructions_Context;
import net.nawaman.curry.Instructions_Package;
import net.nawaman.curry.Instructions_StackOwner;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.MType;
import net.nawaman.curry.Package;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.TLType;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Instructions_Context.Inst_GetContextInfo;
import net.nawaman.curry.Instructions_Context.Inst_GetEngineVarValue;
import net.nawaman.curry.Instructions_Context.Inst_GetGlobalVarValue;
import net.nawaman.curry.Instructions_Context.Inst_GetVarValue;
import net.nawaman.curry.Instructions_Core.Inst_Cast;
import net.nawaman.curry.Instructions_Core.Inst_CastOrElse;
import net.nawaman.curry.Instructions_DataHolder.Inst_GetDHValue;
import net.nawaman.curry.Instructions_DefaultPackage.Inst_GetDefaultPackage;
import net.nawaman.curry.Instructions_Initializer.Inst_InvokeSuperInitializer_ByPTRefs;
import net.nawaman.curry.Instructions_Initializer.Inst_InvokeThisInitializer_ByPTRefs;
import net.nawaman.curry.Instructions_Operations.InstPrintLn;
import net.nawaman.curry.Instructions_Operations.InstPrintNewLine;
import net.nawaman.curry.Instructions_Package.Inst_GetCurrentPackage;
import net.nawaman.curry.Instructions_Package.Inst_GetPackage;
import net.nawaman.curry.Instructions_StackOwner.Inst_GetAttrValue;
import net.nawaman.curry.Instructions_StackOwner.Inst_Invoke;
import net.nawaman.curry.Instructions_StackOwner.Inst_packageGetAttrValue;
import net.nawaman.curry.Instructions_StackOwner.Inst_packageInvoke;
import net.nawaman.curry.Instructions_StackOwner.Inst_thisGetAttrValue;
import net.nawaman.curry.Instructions_StackOwner.Inst_thisInvoke;
import net.nawaman.curry.Instructions_StackOwner.Inst_typeGetAttrValue;
import net.nawaman.curry.Instructions_StackOwner.Inst_typeInvoke;
import net.nawaman.curry.TKExecutable.TExecutable;
import net.nawaman.curry.compiler.CompileProduct.CompileTimeChecking;
import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.util.UArray;
import net.nawaman.util.UClass;
import net.nawaman.util.UObject;

public class Util_Atomic {
	
	/** Compile a cast expression */
	static public Expression CompileCast(TypeRef $Cast, Object $OrElse, Object $Operand, boolean $UseDefault,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		if($Cast == null) return null;
		
		Engine      $Engine  = $CProduct.getEngine();
		MType       MT       = $Engine.getTypeManager();
		MExecutable ME       = $Engine.getExecutableManager();
		boolean     HasElse  = $UseDefault ||($OrElse != null);
		int[]       Location = $Result.locationCROf(0);
		
		// TypeRef -> Type
		Object Type = ME.newType(Location, $Cast);
		
		if($UseDefault) $OrElse = $Cast.getDefaultValue($Engine);
		
		if(HasElse) {
			if($OrElse != null) {
				// Warn if OrElse is not assignable to the type
				if((Boolean.FALSE.equals(MT.mayTypeRefBeCastedTo($Cast, $CProduct.getReturnTypeRefOf($OrElse)))) &&
				   $CProduct.isCompileTimeCheckingFull()) {
					$CProduct.reportWarning(
							"The OrElse value cannot be assigned by the cast target type '"+$Cast+"' <Util_Atomic:39>", null,
							$Result.getStartPosition());
				}
			
				// Prepare OrElse
				$OrElse = Expression.toExpr($OrElse);
			}
			
			// Create the expression, check parameter validality and return
			Expression Expr = ME.newExprSub(Location, Inst_CastOrElse.Name,
			                      // Parameters
			                      new Object[] { Type, $Operand },
			                      // Sub Expressions
			                      (Expression)$OrElse);
			
			// Ensure the parameter is correctly formed
			if(!Expr.ensureParamCorrect($CProduct)) return null;
			return Expr;
		}
		
		// Warn if the object does not seems to be convertible to the type
		TypeRef OperandTRef = $CProduct.getReturnTypeRefOf($Operand);
		if(($Operand != null) &&
		   (Boolean.FALSE.equals(MT.mayTypeRefBeCastedTo($Cast, OperandTRef))) &&
		   $CProduct.isCompileTimeCheckingFull()) {
			$CProduct.reportWarning(
					"The given value does not seems to be castable to the target type '"+$Cast+"'\n"+
					"You are encourage to use 'castOrElse' or 'tryCast' for a better control of casting. <Util_Atomic:66>", null,
					$Result.getStartPosition());
		}
		
		// Convert to number
		boolean IsNumber = false;
		try { IsNumber = MType.CanTypeRefByAssignableByInstanceOf(null, $Engine, TKJava.TNumber.getTypeRef(), $Cast); }
		catch (CurryError CE) {}
		
		if(IsNumber) {
			try {
				String TypeName = $Cast.toString();
				String InstName = "to" + TypeName.substring(0, 1).toUpperCase() + TypeName.substring(1);
				return $Engine.getExecutableManager().newExpr(Location, InstName, $Operand);
			} catch (CurryError CE) {}
		}

		// Create the expression, check parameter validality and return
		Expression Expr = $Engine.getExecutableManager().newExpr(Location, Inst_Cast.Name, Type, $Operand);
		if(!Expr.ensureParamCorrect($CProduct)) return null;
		return Expr;
	}
	
	/** A class that is responsible to compile the body. */
	public interface BodyCompiler {
		
		/** Compiles the body */
		public Expression[] compileBody(ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct);

		// Simple implementation -------------------------------------------------------------------------------------------

		/** Compiler for a Result that have a parser result entry named "#Body" which has the return result of Expression[] */
		static class Simple implements BodyCompiler {
			/** Compiles the statemets */ @Override
			public Expression[] compileBody(ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
				return (Expression[])($Result.valueOf("#Body", $TPackage, $CProduct));
			}
		}
	}
	
	/** A class that is responsible to compile the parmeter at the index. */
	public interface EachParamCompiler {
		
		/** Compiles the parameter list */
		public Object compileEachParamerter(int Index, ParseResult $Result, PTypePackage $TPackage,
				CompileProduct $CProduct);

		// Simple implementation -------------------------------------------------------------------------------------------

		/**
		 * Compiler for a Result that have a parser result entry named "#Params" which has sub result entries named
		 *    "#Param" of "Expr" type.
		 **/
		static class Simple implements EachParamCompiler {
			/** Compiles the parameter at the index */ @Override
			public Object compileEachParamerter(int Index, ParseResult $Result, PTypePackage $TPackage,
					CompileProduct $CProduct) {
				
				ParseResult ParamList = $Result.subOf("#Params");
				if(ParamList == null) return null;
				
				ParseResult[] Params = ParamList.subsOf("#Param");
				if(Params == null)         return null;
				if(Index >= Params.length) return null;
				
				ParseResult Param = Params[Index];
				PType PT = $TPackage.getType(Param.getTypeNameOfSubOf(0));
				if(PT == null) return Param.getText();
				
				return PT.compile(Param, null, $CProduct, $TPackage);
			}
		}
	}
	
	/** A class that is responsible to compile the parmeter list. */
	public interface ParamsCompiler {
		
		/** Compiles the parameter list */
		public Object[] compileParamerterList(ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct);
		
		// Simple implementation -------------------------------------------------------------------------------------------
		
		/** Compiler for a Result that have a parser result entry named "#Params" which has the return result of Object[] */
		static public class Simple implements ParamsCompiler {
			/** Compiles the parameter list */ @Override
			public Object[] compileParamerterList(ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
				return (Object[])($Result.valueOf("#Params", $TPackage, $CProduct));
			}
		}
	}
	
	/** Compile the parameter list using the given ParamsCompiler or return null if the ParamsCompiler is null */
	static public Expression[] CompileBody(BodyCompiler $BodyCompiler, ParseResult $Result,
			PTypePackage $TPackage, CompileProduct $CProduct) {
		// Early return
		if($BodyCompiler == null) return null;
		
		// Do the compile
		return $BodyCompiler.compileBody($Result, $TPackage, $CProduct);
	}
	
	/**
	 * Compile the parameter list using the given compileEachParamerter or return null if the compileEachParamerter
	 *     is null.
	 **/
	static public Object CompileEachParamerter(EachParamCompiler $EachParamCompiler, int Index, ParseResult $Result,
			PTypePackage $TPackage, CompileProduct $CProduct) {
		// Early return
		if($EachParamCompiler == null) return null;
		
		// Do the compile
		return $EachParamCompiler.compileEachParamerter(Index, $Result, $TPackage, $CProduct);
	}
	
	/** Compile the parameter list using the given ParamsCompiler or return null if the ParamsCompiler is null */
	static public Object[] CompileParamerterList(ParamsCompiler $ParamsCompiler, ParseResult $Result,
			PTypePackage $TPackage, CompileProduct $CProduct) {
		// Early return
		if($ParamsCompiler == null) return null;
		
		// Do the compile
		return $ParamsCompiler.compileParamerterList($Result, $TPackage, $CProduct);
	}
	
	/** Compile an expression which is a direct access to an Instruction */
	static public Expression CompileNewInstruction(boolean $IsCheckFull, String $InstName,
			int $ParameterCount, ParamsCompiler $ParamsCompiler, EachParamCompiler $EachParamCompiler,
			int $SubExprCount,   BodyCompiler   $BodyCompiler,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// STEPS: Base on the design of expression the folowing steps are need to ensure that compile time context is
		//            properly manipulate.
		// 1. The Instruction is optained from the name
		// 2. All of non-wrapped-expression parameters are compiled.
		// 3. Instruction.manipulateCompileContextBeforeSub(...) is called. (e.g. for Stack, or loop the stack name is assigned)
		// 4. The rest of the parameters are compiled. (so more context manipulation is created)
		// 5. The body is compiled.
		// 6. Expression.ensureParamCorrect(...) is called to check the rest of the parameters.
		// 7. Instruction.manipulateCompileContextFinish(...) is called. (to clear the context)
		
		// Get the engine
		Engine $Engine = $CProduct.getEngine();
		
		// Determine the compile-time checking of this instruction and preserve the old value
		CompileTimeChecking CheckingFlag = $CProduct.getCompileTimeChecking();
		
		// Position and Location
		int   Pos = $Result.posOf(0);
		int[] Loc = $Result.locationCROf(0);
			
		Expression Expr = null;
		try {
			// Set the appropriate compile time checking
			if($IsCheckFull) $CProduct.setCompileTimeChecking(CompileTimeChecking.Full);
			else             $CProduct.setCompileTimeChecking(CompileTimeChecking.None);
		
			// Sure to be used data
			String  InstName = $InstName;
			boolean HasSub   = ($SubExprCount != 0);
			
			// Special case --------------------------------------------------------------------------------------------
			
			// Println without parameter
			if(($ParameterCount == 0) && !HasSub && InstPrintLn.Name.equals(InstName)) 
				InstName = InstPrintNewLine.Name;	// @:println()
		
			
			// Get the instruction from the name -----------------------------------------------------------------------
			
			Instruction Inst = $Engine.getInstruction(InstName);
			if(Inst == null) {
				$CProduct.reportFatalError(String.format("The instruction `%s` does not exist", InstName), null, Pos);
				return null;
			}
		
			
			// Manipulate the context - Before -------------------------------------------------------------------------
			
			Inst.manipulateCompileContextStart($CProduct, Pos);
			
			// Get the parameters and Manipulate the compilation context before sub-expression is available ------------
			
			Object[] Params = null;
			
			if($ParameterCount != 0) {
			
				int     ErrBefore  = $CProduct.getErrorMessageCount();
				boolean HasPAsExpr = Inst.getSpecification().hasParamAsExpression();
				
				if(HasPAsExpr) {
					// Get the parameters
					Params = new Object[$ParameterCount];
				
					for(int i = 0; i < $ParameterCount; i++) {
						// Look for the parameter that does not have a type of Expression
						if(TKJava.TExpression == Inst.getSpecification().getParameterType(i)) continue;
						
						Params[i] = CompileEachParamerter($EachParamCompiler, i, $Result, $TPackage, $CProduct);
					}
				} else  Params    = CompileParamerterList($ParamsCompiler, $Result, $TPackage, $CProduct);
			
				// Manipulate the context before sub
				Inst.manipulateCompileContextBeforeSub(Params, $CProduct, Pos);
				
				if(HasPAsExpr) {
					// Get the params that are expression (the rest)
					// This is done so that the expression is compiled after manipulateCompileContextBeforeSub
					
					for(int i = 0; i < $ParameterCount; i++) {
						// Look for the parameter that does not have a type of Expression
						if(TKJava.TExpression != Inst.getSpecification().getParameterType(i)) continue;
	
						Params[i] = CompileEachParamerter(
						                $EachParamCompiler, i, $Result, $TPackage, $CProduct);
					}
				}
	
				// There are more error during the parameter compilation, the compilation of this instruction should stop;
				//     otherwise, there will be too much (unnecessary error) as its consequences compilation process
				//     may depends on the one with the errpr.
				int ErrAfter = $CProduct.getErrorMessageCount();
				if(ErrAfter != ErrBefore) return null;
				
			} else {
				// Manipulate the context before sub
				Inst.manipulateCompileContextBeforeSub(Params, $CProduct, Pos);
			}
			
			// Get the sub-expression ----------------------------------------------------------------------------------
			
			Expression[] Stms = null;
			if(HasSub) {
				int ErrBefore = $CProduct.getFatalErrorMessageCount();
				
				Stms = CompileBody($BodyCompiler, $Result, $TPackage, $CProduct);

				// There are more fatal error during the sub-expression compilation, the compilation of this instruction
				//     should stop; otherwise, there will be too much (unnecessary error) as its consequences sub-expression
				//     may depends on the one with the errpr.
				int ErrAfter = $CProduct.getErrorMessageCount();
				if(ErrAfter != ErrBefore) return null;
			}
			
			
			// Constructing the expression -----------------------------------------------------------------------------
			
			try { Expr = Inst.newExprSubs_Coordinate(Loc, Params, Stms); }
			catch(IllegalArgumentException E) {
				String Msg = E.getMessage();
				
				// Default message
				if((Msg == null) || !Msg.startsWith("Unmatch number of parameters: "))
					Msg = "Error while trying to create an expression of the instruction `"+InstName+"`: " + E.toString();
				
				$CProduct.reportFatalError(Msg, null, Pos);
				return null;
			}
		
			if(!Expr.isData() && !Expr.isExpr()) {
				// Check parameter and Manipulate simulated context --------------------------------------------------------
				if(!Expr.ensureParamCorrect($CProduct) || !Expr.manipulateCompileContextFinish($CProduct)) return null;
			}
			
		} finally {
			$CProduct.setCompileTimeChecking(CheckingFlag);
		}
		
		return Expr;
	}
	
	/** Prepare the parameter arrays */
	static void PrepareParams(Engine $Engine, CompileProduct $CProduct, Object[] Params, TypeRef[] PTRefs) {
		int PCount = (PTRefs == null)?0:PTRefs.length;
		for(int i = PCount; --i >= 0; ) {
			TypeRef PTRef = $CProduct.getReturnTypeRefOf(Params[i]);
			// TypeRef
			while(true) {
				if(PTRef instanceof TLType.TypeTypeRef) PTRef = ((TLType.TypeTypeRef)PTRef).getTheRef();
				else if(PTRef == null) { PTRef = null; break; }
				else break;
			}
			PTRefs[i] = PTRef;
		}
	}

	/** Compile a new instance expression */
	static public Expression CompileNew(TypeRef $TRef, Object[] $Params,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		Engine $Engine = $CProduct.getEngine();
		Type   $Type   = null;
		
		boolean IsTypeValid = true;
		try {
			$Type = $CProduct.getTypeAtCompileTime($TRef);
			IsTypeValid = ($Type != null);
		} catch(Exception E) { IsTypeValid = false;    }

		// See if the type is a curry error and the parameter is suitable to be a curry error (in the sense that we can use)
		if(IsTypeValid && ($Type != null) && CurryError.class.isAssignableFrom($Type.getTypeInfo().getDataClass()) &&
		   (($Params == null) || ($Params.length <= 2)) ) {
			
			Class<?> CT = $TRef.getDataClass($Engine);

			int     IndexMessage = -1;
			int     IndexCause   = -1;
			boolean HasOthers    = false;

			if($Params != null) {
				if($Params.length >= 1) {
					TypeRef TR = $CProduct.getReturnTypeRefOf($Params[0]);
					if(     TKJava.TString.getTypeRef().equals(TR))                     IndexMessage = 0;
					else if(Throwable.class.isAssignableFrom(TR.getDataClass($Engine))) IndexCause   = 0;
					else HasOthers = true;
				}
				if(!HasOthers && ($Params.length >= 2)) {
					TypeRef TR = $CProduct.getReturnTypeRefOf($Params[1]);
					if(     TKJava.TString.getTypeRef().equals(TR))                     IndexMessage = 1;
					else if(Throwable.class.isAssignableFrom(TR.getDataClass($Engine))) IndexCause   = 1;
					else HasOthers = true;
				}
			}

			if(!HasOthers) {	// The parameter is suitable
				boolean IsGoodForNewThrowable = false;
				if((IndexMessage != -1) && (IndexCause != -1)) {	// The message and the cause are given
					Class<?>[] CParamClasses = new Class<?>[] { String.class, Context.class, Throwable.class };
					IsGoodForNewThrowable = (UClass.getConstructorByParamClasses(CT, CParamClasses) != null);

				} else {
					IsGoodForNewThrowable |= (UClass.getConstructorByParamClasses(CT, new Class<?>[] { String.class, Context.class }) != null);
					IsGoodForNewThrowable |= (UClass.getConstructorByParamClasses(CT, new Class<?>[] {               Context.class }) != null);
				}

				if(IsGoodForNewThrowable)	// The parameters are good for NewThrowable
					return $Engine.getExecutableManager().newExpr(
								$Result.locationCROf(0), "newThrowable",
								(IndexMessage != -1) ? $Params[IndexMessage] : null,
								CT,
								(IndexCause   != -1) ? $Params[IndexCause  ] : null);

			}
		}

		// The type is not a throwable, so just go on normally -------------------------------------------------------------
		MExecutable ME   = $Engine.getExecutableManager();
		Object      Type = ME.newType($Result.locationCROf(0), $TRef);

		// Get the typerefs of the parameters
		TypeRef[] TRs = TypeRef.EmptyTypeRefArray;
		if($Params != null) {
			TRs = new TypeRef[$Params.length];
			for(int i = $Params.length; --i >= 0; )
				TRs[i] = $CProduct.getReturnTypeRefOf($Params[i]);
		}
		
		// Check if the constructor exist
		if(!IsTypeValid || ($CProduct.isCompileTimeCheckingFull() && ($Type.getTypeInfo().searchConstructor($Engine, TRs) == null))) {
			String ErrMsg = String.format("Unable to find the constructor new %s(%s)", $TRef, UArray.toString(TRs, "", "", ","));
			$CProduct.reportError(ErrMsg, null, $Result.posOf(0));
		}

		// Prepare the parameter, construct the expression and return it
		Object[] Os = new Object[(($Params == null) ? 0 : $Params.length) + 2];
		Os[0] = Type;
		Os[1] = TRs;
		if($Params != null) System.arraycopy($Params, 0, Os, 2, $Params.length);
		Expression Expr = ME.newExpr($Result.locationCROf(0), "newInstanceByTypeRefs", (Object[])Os);
		if(!Expr.ensureParamCorrect($CProduct))
			return null;
		return Expr;
	}

	/** Compile an access to local element */
	static public Expression CompileAtomicLocal(Character Check, String VarName, boolean IsExec, Object[] Params,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		// Get the engine
		Engine      $Engine = $CProduct.getEngine();
		MExecutable $ME     = $Engine.getExecutableManager();

		// Determine the compile-time checking of this instruction and presever the old value
		CompileTimeChecking CheckingFlag = $CProduct.getCompileTimeChecking();

		EE_Language EEL = (EE_Language)$Engine.getExtension(EE_Language.Name);
		final boolean IsAccess_this_AsVar = (EEL != null) && EEL.isStackOwnerVariableShouldBeTreatedAsVariable;

		// Sure to be used
		int[]      Location  = $Result.locationCROf(0);
		int        Position  = $Result.posOf(0);
		TypeRef[]  PTRefs    = null;
		int        PCount    = (Params == null) ? 0 : Params.length;
		Expression Expr      = null;
		
		if(Params == null) Params = UObject.EmptyObjectArray;

		boolean   IsCheckFull = ((Check != null) && (Check == '@')) || ((Check == null) && $CProduct.isCompileTimeCheckingFull());
		Check = IsCheckFull?'@':'#';

		GetExpr: while(true) {
			
			if((PTRefs == null) && (Params != null)) {
				PTRefs = new TypeRef[PCount];
				PrepareParams($Engine, $CProduct, Params, PTRefs);
			}

			// Access to predefine variable --------------------------------------------------------------------------------
			if("this".equals(VarName) || Context.StackOwner_VarName.equals(VarName)) {	// this
				if(IsCheckFull && !$CProduct.isOwnerObject()) {
					$CProduct.reportError(
							"The current context does not belong to an object <Util_Atomic:452>",
							null, Position);
					return null;
				}

				if($CProduct.isOwnerObject()) {
					if(IsExec) {
						TypeRef TRef = $CProduct.getOwnerTypeRef();
						if((TRef != null) && ((TRef.searchConstructor($Engine, PTRefs)) == null)) {
							$CProduct.reportError(
								String.format(
									"The current type does not have the constructor: %s  <Util_Atomic:552>",
									$Result.getText()
								),
								null, Position
							);
							return null;
						}
						
						Object[] NewParams = new Object[1 + Params.length];
						System.arraycopy(Params, 0, NewParams, 1, Params.length);
						NewParams[0] = PTRefs;
						Expr = $ME.newExpr(Location, Inst_InvokeThisInitializer_ByPTRefs.Name, NewParams);
						
						IsExec = false;	// Set IsExec to false if there is an execution
						break GetExpr;
						
					} else {
						if(IsAccess_this_AsVar)
							 Expr = $ME.newExpr(Location, Inst_GetVarValue   .Name, VarName);
						else Expr = $ME.newExpr(Location, Inst_GetContextInfo.Name, Instructions_Context.Inst_GetContextInfo.StackOwner);
						break GetExpr;
					}
				}
			}


			// Access to predefine variable --------------------------------------------------------------------------------
			if(IsExec && "super".equals(VarName)) {	// super constructor

				if(!$CProduct.isOwnerObject()) {
					if(IsCheckFull) {
						$CProduct.reportError(
								"The current context does not belong to an object <Util_Atomic:452>",
								null, Position);
						return null;
					}
					
				} else {
					TypeRef SuperTRef = $CProduct.getOwnerTypeRef().getTypeSpec($Engine).getSuperRef();
					if((SuperTRef != null) && (SuperTRef.searchConstructor($Engine, PTRefs) == null)) {
						$CProduct.reportError(
							String.format(
								"The super type does not have the constructor: %s  <Util_Atomic:552>",
								$Result.getText()
							),
							null, Position
						);
						return null;
					}
					
				}
			
				Object[] NewParams = new Object[1 + Params.length];
				System.arraycopy(Params, 0, NewParams, 1, Params.length);
				NewParams[0] = PTRefs;
				Expr = $ME.newExpr(Location, Inst_InvokeSuperInitializer_ByPTRefs.Name, NewParams);

				IsExec = false;	// Set IsExec to false if there is an execution
				break GetExpr;
			}

			if(Context.StackOwnerAsType_VarName.equals(VarName)) {	// type
				if(IsCheckFull && ($CProduct.getOwnerTypeRef() == null)) {
					$CProduct.reportError(
							"The current context does not belong to an object or a type <Util_Atomic:452>",
							null, Position);
					return null;
				}

				if($CProduct.getOwnerTypeRef() != null) {
					if(IsAccess_this_AsVar)
						 Expr = $ME.newExpr(Location, Inst_GetVarValue   .Name, VarName);
					else Expr = $ME.newExpr(Location, Inst_GetContextInfo.Name, Instructions_Context.Inst_GetContextInfo.StackOwner_As_Type);
					break GetExpr;
				}
			}

			if(Context.StackOwnerAsPackage_VarName.equals(VarName)) {	// current package
				if(IsCheckFull && ($CProduct.getOwnerPackageBuilder() == null)) {
					$CProduct.reportError("The current context does not belong to a package <Util_Atomic:520>", null, Position);
					return null;
				}

				if($CProduct.getOwnerPackageBuilder() != null) {
					if(IsAccess_this_AsVar)
						 Expr = $ME.newExpr(Location, Inst_GetVarValue      .Name, VarName);
					else Expr = $ME.newExpr(Location, Inst_GetCurrentPackage.Name);
					break GetExpr;
				}
			}

			if("$Default$".equals(VarName)) {	// default package
				if(IsCheckFull && ($Engine.getDefaultPackage() == null)) {
					$CProduct.reportError("The default package is not support. <Util_Atomic:532>", null, Position);
					return null;
				}

				if($Engine.getDefaultPackage() == null) {
					Instruction Inst = $Engine.getInstruction(Inst_GetDefaultPackage.Name);
					if(Inst != null) Expr = Inst.newExpression_Coordinate(Location);
					break GetExpr;
				}
			}

			if("$TheEngine$".equals(VarName)) {	// Engine
				Expr = $Engine.getInstruction("getEngine").newExpression_Coordinate(Location);
				break GetExpr;
			}

			// Search LocalVariable ----------------------------------------------------------------------------------------
			if($CProduct.isVariableExist(VarName)) {
				// See if variable is a transparental dataholder
				if(MType.CanTypeRefByAssignableByInstanceOf(null, $Engine,
				       TKJava.TDataHolder.getTypeRef(),
				       $CProduct.getVariableTypeRef(VarName))) {
					MoreData MD = $CProduct.getVariableMoreData(VarName, false);
					if((MD != null) && Boolean.TRUE.equals(MD.getData(CompileProduct.MDName_IsTransparentDH))) {
						
						Expr = $Engine.getInstruction(Inst_GetVarValue.Name).newExpression_Coordinate(Location, VarName);
						Expr = $Engine.getInstruction(Inst_GetDHValue .Name).newExpression_Coordinate(Location, Expr);
						break GetExpr;						
					}	
				}
					
				// Found the local variable so return the expression to read it.
				Expr = $Engine.getInstruction(Inst_GetVarValue.Name).newExpression_Coordinate(Location, VarName);
				break GetExpr;
			}

			// Search in Current StackOwner
			TypeRef SPTRef = $CProduct.getOwnerTypeRef();
			if(SPTRef != null) {
				boolean IsStatic = !$CProduct.isOwnerObject();

				if(IsExec) {	// Try executable first
					if((PTRefs == null) && (Params != null)) {
						PTRefs = new TypeRef[PCount];
						PrepareParams($Engine, $CProduct, Params, PTRefs);
					}

					ExecSignature Signature = null;

					if(!IsStatic)           Signature = SPTRef.searchObjectOperation($Engine, VarName, PTRefs);
					if(Signature == null) { Signature = SPTRef.searchTypeOperation(  $Engine, VarName, PTRefs); IsStatic = (Signature != null); }
					if(Signature != null) {

						if(IsAccess_this_AsVar) {
							Expr = $ME.newExpr(Location, Inst_GetVarValue.Name, IsStatic ? Context.StackOwnerAsType_VarName : "this");
							
							Object[] TheParams = new Object[2 + ((PTRefs == null)?0:PTRefs.length)];
							TheParams[0] = Expr;
							TheParams[1] = Signature;
							if(PCount != 0) System.arraycopy(Params, 0, TheParams, 2, PTRefs.length);
							
							Expr = $ME.newExpr(Location, Inst_Invoke.Name, TheParams);
							
						} else {
							Object[] TheParams = new Object[1 + ((PTRefs == null)?0:PTRefs.length)];
							TheParams[0] = Signature;
							if(PCount != 0) System.arraycopy(Params, 0, TheParams, 1, PTRefs.length);
							// Access this type or this
							Expr   = $ME.newExpr(Location, IsStatic ? Inst_typeInvoke.Name : Inst_thisInvoke.Name, TheParams);
						}
						IsExec = false;	// Set IsExec to false if there is an execution
						break GetExpr;
					}
				}

				// Try attrbute
				TypeRef ATRef = null;

				if(!IsStatic)       ATRef = SPTRef.searchObjectAttribute($Engine, VarName);
				if(ATRef == null) { ATRef = SPTRef.searchTypeAttribute(  $Engine, VarName); IsStatic = (ATRef != null); }
				if(ATRef != null) {
					if(IsAccess_this_AsVar) {
						Expr = $ME.newExpr(Location, Inst_GetVarValue .Name, IsStatic ? Context.StackOwnerAsType_VarName : "this");
						Expr = $ME.newExpr(Location, Inst_GetAttrValue.Name, Expr, VarName);
						
					} else {
						// Access this type or this
						Expr = $ME.newExpr(Location, IsStatic? Inst_typeGetAttrValue.Name : Inst_thisGetAttrValue.Name, VarName);
					}
					break GetExpr;
				}
			}

			// Search in import
			String[] Is = $CProduct.getImports();
			for(int i = 0; i < (2 + ((Is == null)?0:Is.length)); i++) {
				Package P = null;

				switch(i) {
					case 0:	// Search in Current Package
						PackageBuilder PB = $CProduct.getOwnerPackageBuilder();
						if(PB == null) continue;
						P = PB.getPackage();
						if(P == null) {
							$CProduct.reportWarning(
								String.format(
									"The current package builder %s is not yet inactivated so it cannot be used <Atom_Local:629>.",
									PB.getName()
								),
								null
							);
							continue;
						}
						break;
					case 1:	// Search in Default Package
						P = $Engine.getDefaultPackage();
						break;

					default: {
						String Import = Is[i - 2]; 
						if(Import == null) continue;

						boolean IsDot = false;
						if((IsDot = Import.endsWith(".")) || Import.endsWith("=>")){
							if(IsDot) Import = Import.substring(0, Import.lastIndexOf('.'));
							else      Import = Import.substring(0, Import.lastIndexOf("=>"));
							P = $Engine.getUnitManager().getRawPackage(Import);
						} else {
							// A direct import
							if     (Import.endsWith("." + VarName))
								P = $Engine.getUnitManager().getRawPackage(Import.substring(0, Import.lastIndexOf('.')));
							else if(Import.endsWith("=>" + VarName))
								P = $Engine.getUnitManager().getRawPackage(Import.substring(0, Import.lastIndexOf("=>")));
						}
					}
				}

				if(P == null) continue;

				if(IsExec) {	// Try executable first
					if((PTRefs == null) && (Params != null)) {
						PTRefs = new TypeRef[Params.length];
						PrepareParams($Engine, $CProduct, Params, PTRefs);
					}
					
					ExecSignature Signature = P.getSOInfo().searchOperation($Engine, VarName, PTRefs);
					if(Signature != null) {
						if(i == 0) {		// Current Package
							if(IsAccess_this_AsVar) {
								Object[] TheParams = new Object[2 + ((PTRefs == null)?0:PTRefs.length)];
								TheParams[0] = $ME.newExpr(Location, Inst_GetVarValue.Name, Context.StackOwnerAsPackage_VarName);
								TheParams[1] = Signature;
								if(PCount != 0) System.arraycopy(Params, 0, TheParams, 2, PTRefs.length);
								// Access this type or this
								Expr = $ME.newExpr(Location, Inst_Invoke.Name, TheParams);
								
							} else {
								Object[] TheParams = new Object[1 + ((PTRefs == null)?0:PTRefs.length)];
								TheParams[0] = Signature;
								if(PCount != 0) System.arraycopy(Params, 0, TheParams, 1, PTRefs.length);
								// Access this type or this
								Expr   = $ME.newExpr(Location, Inst_packageInvoke.Name, TheParams);
							}

						} else {
							Object[] TheParams = new Object[2 + ((PTRefs == null)?0:PTRefs.length)];
							TheParams[1] = Signature;
							if(PCount != 0) System.arraycopy(Params, 0, TheParams, 2, PTRefs.length);
							
							if(i == 1) TheParams[0] = $ME.newExpr(Location, Inst_GetDefaultPackage.Name);               // Default
							else       TheParams[0] = $ME.newExpr(Location, Inst_GetPackage       .Name, P.getName());	// From Import
							
							Expr = $ME.newExpr(Location, Inst_Invoke.Name, TheParams);							
						}

						IsExec = false;	// Set IsExec to false if there is an execution
						break GetExpr;
					}
				}

				if(P.isVarExist(VarName)) {
					if(i == 0) {
						Expr = $ME.newExpr(Location, Inst_packageGetAttrValue.Name, VarName); // Current Package

					} else if(i == 1) {	// Default Package
						Expression DP = $ME.newExpr(Location, Inst_GetDefaultPackage.Name);
						Expr          = $ME.newExpr(Location, Inst_GetAttrValue     .Name, DP, VarName);
						
					} else {	// Others - From Import
						Expression DP = $ME.newExpr(Location, Inst_GetPackage  .Name, P.getName());
						Expr          = $ME.newExpr(Location, Inst_GetAttrValue.Name, DP, VarName);
					}

					Expr = $ME.newExpr(Location, Inst_packageGetAttrValue.Name, VarName);
					break GetExpr;
				}

				if(P.isTypeExist(VarName)) {
					Expr = $ME.newType(Location, new TLPackage.TRPackage(P.getName(), VarName));
					break GetExpr;
				}
			}

			// Search as a package -----------------------------------------------------------------------------------------
			for(int i = 0; i < ((Is == null)?0:Is.length); i++ ) {
				String I = Is[i]; 
				if(I == null) continue;
				
				if(I.endsWith("."))  I = I.substring(0, I.lastIndexOf('.'));
				if(I.endsWith("=>")) I = I.substring(0, I.lastIndexOf("=>"));
				
				if(!VarName.equals(I)) continue;
					
				Package P = $Engine.getUnitManager().getRawPackage(I);
				if(P == null) continue;
				
				Expr = $ME.newExpr(Location, Inst_GetPackage.Name, I);
				break GetExpr;
			}

			// Search GlobalVariable ---------------------------------------------------------------------------------------
			if($CProduct.isGlobalVariableExist(VarName)) {
				// Found the local variable so return the expression to read it.
				Expr = $ME.newExpr(Location, Inst_GetGlobalVarValue.Name, VarName);
				break GetExpr;
			}

			// Search EngineVariable --------------------------------------------------------------------------------------------
			if($CProduct.isEngineVariableExist(VarName)) {
				// Found the local variable so return the expression to read it.
				Expr = $ME.newExpr(Location, Inst_GetEngineVarValue.Name, VarName);
				break GetExpr;
			}

			break GetExpr;
		}

		if(Expr == null) {
			if(IsCheckFull) {
				$CProduct.reportError(
					String.format("Unknown variable '%s' <Atom_Local:248>", VarName),
					null, Position
				);
				return null;
			}

			// Not checking in full, assume the access is a local variable
			Expr = $Engine.getInstruction(Inst_GetVarValue.Name).newExpression_Coordinate(Location, VarName);
		}

		try {
			switch(Check) {
				case '@': { $CProduct.setCompileTimeChecking(CompileTimeChecking.Full); break; }
				case '#': { $CProduct.setCompileTimeChecking(CompileTimeChecking.None); break; }
			}

			// Check parameter and Manipulate simulated context ------------------------------------------------------------
			if(!Expr.ensureParamCorrect($CProduct) || !Expr.manipulateCompileContextFinish($CProduct)) Expr = null;

		} finally { $CProduct.setCompileTimeChecking(CheckingFlag); } // Restore the compile-time checking setting

		if(IsExec) {
			TypeRef TRef = $CProduct.getReturnTypeRefOf(Expr);

			boolean IsFragment   = false;
			boolean IsMacro      = false;
			boolean IsSubRoutine = false;
			
			Type T = null;
			try { T = $CProduct.getTypeAtCompileTime(TRef); }
			catch (Exception E) {}
			
			if(!(T instanceof TExecutable)) {
				if(     TKJava.TScriptScript  .getTypeRef().canBeAssignedByInstanceOf($Engine, TRef)) IsFragment   = true;
				else if(TKJava.TScriptMacro   .getTypeRef().canBeAssignedByInstanceOf($Engine, TRef)) IsMacro      = true;
				else if(TKJava.TScriptFunction.getTypeRef().canBeAssignedByInstanceOf($Engine, TRef)) IsSubRoutine = true;
				else {
					$CProduct.reportError("Non-Executable <Atomic_Local:705>.", null, Position);
					return null;
				}
			} else {
				if(((TExecutable)T).getExecKind() == null) IsSubRoutine = true;
				else {
					switch(((TExecutable)T).getExecKind()) {
						case Fragment:   IsFragment   = true; break;
						case Macro:      IsMacro      = true; break;
						case SubRoutine: IsSubRoutine = true; break;
						default: {
							$CProduct.reportError("Non-Executable <Atomic_Local:715>.", null, Position);
							return null;
						}
					}
				}
			}

			if(IsMacro || IsSubRoutine) {
				// Prepare parameters ----------------------------------------------------

				Object[] NewParams = new Object[PCount + 1];
				NewParams[0] = Expr;
				if(Params != null) System.arraycopy(Params, 0, NewParams, 1, NewParams.length - 1);
				// Create the expression -------------------------------------------------
				if(IsSubRoutine)
					 Expr = $ME.newExpr(Location, "call", NewParams);
				else Expr = $ME.newExpr(Location, "exec", NewParams);
			} else if(IsFragment)
				     Expr = $ME.newExpr(Location, "run",  Expr);
		}

		if(!Expr.ensureParamCorrect($CProduct)) return null;
		return Expr;
	}

	/** Compile an access to package element */
	static public Expression CompileAtomicPackageAccess(ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		// Get the engine
		Engine      $Engine = $CProduct.getEngine();
		MExecutable $ME     = $Engine.getExecutableManager();

		// Get the type
		int[]    Location = $Result.locationCROf("$Access");
		String[] Names    = $Result.textsOf("$Name");
		String   AccName  = $Result.textOf("$AccName");
		boolean  IsExec   = ($Result.textOf("$IsExec") != null);
		Object[] Params   = (Object[])$Result.valueOf("#Params", $TPackage, $CProduct);
		
		StringBuilder Name = new StringBuilder();
		for(int i = 0; i < Names.length; i++)
			Name.append(Names[i]);
		
		Expression Expr;
		if(IsExec) {
			TypeRef[] PTRefs = new TypeRef[Params.length];
			for(int i = 0; i < PTRefs.length; i++)
				PTRefs[i] = $CProduct.getReturnTypeRefOf(Params[i]);
			
			if(Params == null) Params = UObject.EmptyObjectArray;
			Object[] NewParams = new Object[3 + Params.length];
			NewParams[0] = $ME.newExpr(Location, Instructions_Package.Inst_GetPackage.Name, Name.toString());
			NewParams[1] = AccName;
			NewParams[2] = PTRefs;
			System.arraycopy(Params, 0, NewParams, 3, Params.length);
			
			Expr = $ME.newExpr(
					Location,
					Instructions_StackOwner.Inst_Invoke_ByPTRefs.Name,
					NewParams
				);
			
			if(!Expr.ensureParamCorrect($CProduct)) return null;
			
		} else {
			Expr = $ME.newExpr(
					Instructions_StackOwner.Inst_GetAttrValue.Name,
					$ME.newExpr(Instructions_Package.Inst_GetPackage.Name, Name.toString()),
					AccName
				);
			Expr = $ME.newExpr(
					Location,
					Instructions_StackOwner.Inst_GetAttrValue.Name,
					$ME.newExpr(Location, Instructions_Package.Inst_GetPackage.Name, Name.toString()),
					AccName
				);
			if(!Expr.ensureParamCorrect($CProduct)) return null;
		}
		
		return Expr;
	}

	static final TypeRef TRAny          = TKJava.TAny         .getTypeRef();
	static final TypeRef TRBoolean      = TKJava.TBoolean     .getTypeRef();
	static final TypeRef TRString       = TKJava.TString      .getTypeRef();
	static final TypeRef TRInteger      = TKJava.TInteger     .getTypeRef();
	static final TypeRef TRDouble       = TKJava.TDouble      .getTypeRef();
	static final TypeRef TRCharacter    = TKJava.TCharacter   .getTypeRef();
	static final TypeRef TRByte         = TKJava.TByte        .getTypeRef();
	static final TypeRef TRLong         = TKJava.TLong        .getTypeRef();
	static final TypeRef TRNumber       = TKJava.TNumber      .getTypeRef();
	static final TypeRef TRSerializable = TKJava.TSerializable.getTypeRef();
	static final TypeRef TRClass        = TKJava.TClass       .getTypeRef();
	static final TypeRef TRType         = TKJava.TType        .getTypeRef();
	static final TypeRef TRTypeRef      = TKJava.TTypeRef     .getTypeRef();
	static final TypeRef TRCatchEntry   = TKJava.TCatchEntry  .getTypeRef();
	static final TypeRef TRCaseEntry    = TKJava.TCaseEntry   .getTypeRef();
	static final TypeRef TRShort        = TKJava.TShort       .getTypeRef();
	static final TypeRef TRFloat        = TKJava.TFloat       .getTypeRef();
	
	/** Compile an access to local element */
	static public Expression CompileAtomicArray(TypeRef TypeRef, Object Dimension, boolean WithElement,
			Object[] Elements, ParseResult[] PRSubDimensions, int[] pTypeRefLocation, ParseResult $Result,
			PTypePackage $TPackage, CompileProduct $CProduct) {

		// Get the engine
		Engine      $Engine  = $CProduct.getEngine();
		MExecutable $ME      = $Engine.getExecutableManager();
		int[]       Location = $Result.locationCROf(0);
		int         Position = $Result.posOf(0);

		Object[]      SubDimensions   = null;
		for(int i = ((PRSubDimensions == null)?0:PRSubDimensions.length); --i >= 0; ) {
			if(SubDimensions == null) SubDimensions = new Object[PRSubDimensions.length];
			if(PRSubDimensions[i] == null) { SubDimensions[i] = -1; continue; }
			String D = PRSubDimensions[i].textOf("#Dimension");
			SubDimensions[i] = (D == null)?-1:PRSubDimensions[i].valueOf("#Dimension", $TPackage, $CProduct);
		}

		// Process the array
		int   Count     = ((PRSubDimensions == null)?0:PRSubDimensions.length);
		int[] Locations = null;
		for(int i = Count; --i >= 0; ) {
			Object D = SubDimensions[i];
			if((D instanceof Integer) || (D instanceof Byte) || (D instanceof Long) || (D instanceof Short)) D = ((Number)D).intValue();
			else {
				if($CProduct.isCompileTimeCheckingFull()) {
					TypeRef DTRef = $CProduct.getReturnTypeRefOf(D);
					if(!$Engine.getTypeManager().mayTypeRefBeCastedTo(TKJava.TNumber.getTypeRef(), DTRef)) {
						if(Locations == null) Locations = PRSubDimensions[i].locationCROf(0);

						String Err = "Invalid array literal declaration: "+
									 "Array dimension must be an integer number (`"+$Engine.toDetail(D)+"`).";
						$CProduct.reportError(Err, null, Locations[i]);
					}
				}
				D = -1;
			}

			TypeRef = ((TKArray)$Engine.getTypeManager().getTypeKind(TKArray.KindName)).getTypeSpec(null, TypeRef, (Integer)D).getTypeRef();
		}

		// Get the type expression
		Object Type = $ME.newExpr(pTypeRefLocation, "type", TypeRef);

		// No instance given
		if(!WithElement) {
			// Check if the dimension is a number
			TypeRef DTRef = $CProduct.getReturnTypeRefOf(Dimension);
			if(!$Engine.getTypeManager().mayTypeRefBeCastedTo(TKJava.TNumber.getTypeRef(), DTRef)) {
				if($CProduct.isCompileTimeCheckingFull()) {
					String Err = "Invalid array literal declaration: Array dimension must be an integer number " +
							     "(`"+$Engine.toDetail(Dimension)+"`).";
					$CProduct.reportError(Err, null, Position);
					return null;
				}
				Dimension = -1;
			}

			Expression Expr = $ME.newExpr(Location, "newArray", Type, Dimension);
			if(!Expr.ensureParamCorrect($CProduct)) return null;
			return Expr;
		}

		// With Instance

		if(Elements == null) Elements = net.nawaman.util.UObject.EmptyObjectArray;

		if(Dimension != null) {
			Object D = Dimension;
			if((D instanceof Integer) || (D instanceof Byte) || (D instanceof Long) || (D instanceof Short)) {
				if(((Number)D).intValue() != Elements.length) {
					String Err = "Unmatch array dimension and literal elements.";
					if($CProduct.isCompileTimeCheckingFull()) {
						$CProduct.reportError(Err, null);
						return null;
					}
				}
			} else {
				String Err = "The compiler will not be able to guarantee that the the dimension of the array will be match.";
				if($CProduct.isCompileTimeCheckingFull()) {
					$CProduct.reportError(Err, null);
					return null;
				}
			}
		}

		// Predefine types ---------------------------------------------------------------------------------------------
		String InstName = null;
		if(     TypeRef == TRAny)          InstName = "newArrayLiteral_any";
		else if(TypeRef == TRBoolean)      InstName = "newArrayLiteral_boolean";
		else if(TypeRef == TRString)       InstName = "newArrayLiteral_String";
		else if(TypeRef == TRInteger)      InstName = "newArrayLiteral_int";
		else if(TypeRef == TRDouble)       InstName = "newArrayLiteral_double";
		else if(TypeRef == TRCharacter)    InstName = "newArrayLiteral_char";
		else if(TypeRef == TRByte)         InstName = "newArrayLiteral_byte";
		else if(TypeRef == TRLong)         InstName = "newArrayLiteral_long";
		else if(TypeRef == TRNumber)       InstName = "newArrayLiteral_number";
		else if(TypeRef == TRSerializable) InstName = "newArrayLiteral_Serializable";
		else if(TypeRef == TRClass)        InstName = "newArrayLiteral_Class";
		else if(TypeRef == TRType)         InstName = "newArrayLiteral_Type";
		else if(TypeRef == TRTypeRef)      InstName = "newArrayLiteral_TypeRef";
		else if(TypeRef == TRShort)        InstName = "newArrayLiteral_short";
		else if(TypeRef == TRFloat)        InstName = "newArrayLiteral_float";
		
		if(InstName != null)
			return $ME.newExpr(Location, InstName, (Object[])Elements);

		// Other type --------------------------------------------------------------------------------------------------
		Object[] Os = new Object[Elements.length + 1];
		Os[0] = Type;
		System.arraycopy(Elements, 0, Os, 1, Elements.length);
		Expression Expr = $ME.newExpr(Location, "newArrayLiteral", (Object[])Os);
		if(!Expr.ensureParamCorrect($CProduct)) return null;
		return Expr;
	}
	

	/** Compile an access to local element */
	static public Expression CompileAtomicStackVariable(
			boolean     IsCheckFull, String       PreDefineStackName, String         StackName,       String VarName,
			int         ParentCount, int[]        VarNameLocation,    int            VarNamePosition,
			ParseResult $Result,     PTypePackage $TPackage,          CompileProduct $CProduct) {

		// Get the engine
		Engine      $Engine  = $CProduct.getEngine();
		MExecutable $ME      = $Engine.getExecutableManager();
	
		Expression Expr = null;
		// Non-Parent stack ------------------------------------------------------------------------------------------------
		if(PreDefineStackName != null) {
			StackName = PreDefineStackName;
			// Predefined ----------------------------------------------------------------------------------------------
	
			// Local --------------------------------------------------------------------------
			if("$Local$".equals(StackName)) {
				if(IsCheckFull && !$CProduct.isVariableExist(VarName)) {
					$CProduct.reportError("Local variable does not exist ("+VarName+") <Util_Atomic:895>", null, VarNamePosition);
					return null;
				}
				Expr = $ME.newExpr(VarNameLocation, "getVarValue", VarName);
			// Engine --------------------------------------------------------------------------
			} else if("$Engine$".equals(StackName)) {
				if(IsCheckFull && !$CProduct.isEngineVariableExist(VarName)) {
					$CProduct.reportError("Engine variable does not exist ("+VarName+") <Util_Atomic:902>", null, VarNamePosition);
					return null;
				}
				Expr = $ME.newExpr(VarNameLocation, "getEngineVarValue", VarName);
			// Global --------------------------------------------------------------------------
			} else if("$Global$".equals(StackName)) {
				if(IsCheckFull && !$CProduct.isEngineVariableExist(VarName)) {
					$CProduct.reportError("Global variable does not exist ("+VarName+") <Util_Atomic:909>", null, VarNamePosition);
					return null;
				}
				Expr = $ME.newExpr(VarNameLocation, "getGlobalVarValue", VarName);
			}
	
		} else if(StackName != null) {
			// Named stack ---------------------------------------------------------------------------------------------
			if(IsCheckFull) {
				// Check if the scope or stack with the name exist
				if(!$CProduct.hasScopeNamed(StackName)) {
					$CProduct.reportError("Untable to find a stack named (" + StackName + ") <Util_Atomic:920>", null, VarNamePosition);
					return null;
				}
				if(!$CProduct.isParentVariableExist(StackName, VarName)) {
					$CProduct.reportError("Parent variable does not exist ("+$Result.getText()+") <Util_Atomic:924>", null, VarNamePosition);
					return null;
				}
			}
			Expr = $ME.newExpr($Result.locationCROf(0), "getParentVarValueByStackName", StackName, VarName);
	
		} else {
			// Parent stack ------------------------------------------------------------------------------------------------
			if(!$CProduct.isParentVariableExist(ParentCount, VarName)) {
				$CProduct.reportError("Parent variable does not exist ("+$Result.getText()+") <Util_Atomic:934>", null, $Result.posOf(0));
				return null;
			}
			Expr = $ME.newExpr($Result.locationCROf(0), "getParentVarValue", ParentCount, VarName);
		}
	
		return Expr;
	}
}
