package net.nawaman.curry.compiler;

import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Instruction;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.MType;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Instructions_Context.Inst_NewBorrowedConstant;
import net.nawaman.curry.Instructions_Context.Inst_NewBorrowedVariable;
import net.nawaman.curry.Instructions_Context.Inst_NewConstant;
import net.nawaman.curry.Instructions_Context.Inst_NewGlobalVariable;
import net.nawaman.curry.Instructions_Context.Inst_NewVariable;
import net.nawaman.curry.Instructions_Core.Inst_Group;
import net.nawaman.curry.Instructions_Core.Inst_RunOnce;
import net.nawaman.curry.Instructions_Core.Inst_Stack;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;

public class Util_Statments {

	/** Checks if the given expression is a group, stack or runonce */
	static public boolean CheckIfExprIsGroupOrStackOrRunOnce(Engine $Engine, Expression Expr) {
		if(Expr == null) return false;
		return Expr.isInstruction($Engine, Inst_Group  .Name) ||
		       Expr.isInstruction($Engine, Inst_Stack  .Name) ||
		       Expr.isInstruction($Engine, Inst_RunOnce.Name);
	}
	/** Extract the body of a stack expression as group */
	static public Expression ExtractBody(Engine $Engine, MExecutable $ME, Expression pExpr) {
		if(pExpr == null) return null;
		Expression Expr = pExpr;
		boolean isRunOnce = false;
		if(Expr.isInstruction($Engine, Inst_RunOnce.Name)) {
			if(Expr.getSubExprCount() != 1) return pExpr;
			isRunOnce = true;
			Expr      = Expr.getSubExpr(0);
		}
		if(!Expr.isInstruction($Engine, Inst_Stack.Name)) return pExpr;
		Expression[] Subs = new Expression[Expr.getSubExprCount()];
		for(int i = Subs.length; --i >= 0; ) Subs[i] = Expr.getSubExpr(i);
		return isRunOnce
				?$ME.newRunOnce(Expr.getColumn() , Expr.getLineNumber(), Subs)
				:$ME.newGroup(  Expr.getColumn() , Expr.getLineNumber(), Subs);
	}
	/** Creates a new wrapping stack */
	static public Expression NewWrappingStack(Engine $Engine, MExecutable $ME, CompileProduct $CProduct, int[] Location, Expression Expr) {
		Expr = ExtractBody($Engine, $ME, Expr);

		if(!Expr.isInstruction($Engine, Inst_NewVariable.Name)         && !Expr.isInstruction($Engine, Inst_NewConstant.Name) &&
		   !Expr.isInstruction($Engine, Inst_NewBorrowedVariable.Name) && !Expr.isInstruction($Engine, Inst_NewBorrowedConstant.Name))
			return Expr;
		
		Instruction Inst = $Engine.getInstruction(Inst_Stack.Name);
		Inst.manipulateCompileContextBeforeSub(new Object[]{ null }, $CProduct, -1);

		Expr = $ME.newStack(Location, null, Expr);
		Expr.manipulateCompileContextFinish($CProduct);
		return Expr;
	}
	
	/** Returns the default value */
	static public TypeRef GetValueTypeRef(ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		if($Result.textOf("$New") != null) {
			// New object
			return TKJava.TAny.getTypeRef();
		} else {
			// Other value
			Object Value = $Result.valueOf("#Value", $TPackage, $CProduct);
			return $CProduct.getReturnTypeRefOf(Value); 
		}
	}
	
	/** Returns the default value */
	static public Object GetDefaultValue(TypeRef TRef, ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		if($Result.textOf("$New") != null) {
			ParseResult New = $Result.subOf("#New");
			// New object
			return Util_Atomic.CompileNew(
						TRef,
						(Object[])New.valueOf("#Params", $TPackage, $CProduct),
						New,
						$TPackage, $CProduct);
		} else {
			// Other value
			return $Result.valueOf("#Value", $TPackage, $CProduct);
		}
	}
	
	/** Compile a NewVar Statement */
	static public Expression ParseCompileNewVar(TypeRef TRef, Object Type, String VarName, int VNamePos, int[] TypeRC,
			int CLength, int GLength, int BLength, ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// Get the engine
		Engine      $Engine = $CProduct.getEngine();
		MExecutable $ME     = $Engine.getExecutableManager();
		int         ZeroPos = $Result.posOf(0);

		if("type".equals(VarName)) {
			$CProduct.reportFatalError("A variable must not be named 'type' <NewVar:76>",      null, VNamePos);
			return null;
		}
		if("$$".startsWith(VarName)) {
			$CProduct.reportFatalError("A variable name must not start with '$$' <NewVar:80>", null, VNamePos);
			return null;
		}

		if($CProduct.isCompileTimeCheckingFull()) {
			// Check for double modifier
			if((CLength > 1) || (GLength > 1) || (BLength > 1)) {
				$CProduct.reportWarning(
					String.format("Duplicate modifier of a local variable '%s' <NewVar:95>", VarName),
					null, VNamePos
				);
			}

			// Default and global cannot be together
			if((GLength > 0) && (BLength > 0)) {
				$CProduct.reportError(
					String.format("Global variable cannot be declared with default '%s' <NewVar:103>", VarName),
					null, VNamePos
				);
			}
		}

		Expression Expr = null;
		if(GLength != 0) {	// Global variable

			if($CProduct.isCompileTimeCheckingFull() && $CProduct.isGlobalVariableExist(VarName)) {
				$CProduct.reportWarning(
					String.format("The global variable `%s` is already exist <NewVar: 114>.", VarName),
					null
				);
			}
			
			if (TRef == null) {
				TRef = GetValueTypeRef($Result, $TPackage, $CProduct);
				Type = $ME.newType(TypeRC, TRef);
			}

			Instruction Inst  = $Engine.getInstruction(Inst_NewGlobalVariable.Name);				
			Object      Value = GetDefaultValue(TRef, $Result, $TPackage, $CProduct);

			Inst.manipulateCompileContextStart    (                                                       $CProduct, ZeroPos);
			Inst.manipulateCompileContextBeforeSub(new Object[] { VarName, Type, Value, (CLength != 0) }, $CProduct, ZeroPos);

			Expr = Inst.newExpression_Coordinate(TypeRC, VarName, Type, Value, (CLength != 0));

			// Simulate stack (Avoid manipulate method to avoid repeat warning)
			if(!Expr.manipulateCompileContextFinish($CProduct)) return null;

		} else {	// Local variable
			if($CProduct.isLocalVariableExist(VarName)) {
				String Msg = String.format("The local variable `%s` is already exist.", VarName);
				if($CProduct.isCompileTimeCheckingFull()) { $CProduct.reportError(  Msg, null); return null; }
				else                                        $CProduct.reportWarning(Msg, null);
			}

			if((BLength == 0) && $CProduct.isVariableExist(VarName) && $CProduct.isCompileTimeCheckingFull()) {
				$CProduct.reportWarning(
					String.format("The local variable is hiding another variable `%s`", VarName),
					null, VNamePos
				);
			}

			String InstName;
			if(BLength != 0) {
				if(CLength != 0)
					 InstName = Inst_NewBorrowedConstant.Name;
				else InstName = Inst_NewBorrowedVariable.Name;
			} else {
				if(CLength != 0)
					 InstName = Inst_NewConstant.Name;
				else InstName = Inst_NewVariable.Name;
			}
			
			if (TRef == null) {
				TRef = GetValueTypeRef($Result, $TPackage, $CProduct);
				Type = $ME.newType(TypeRC, TRef);
			}

			Instruction Inst  = $Engine.getInstruction(InstName);
			Object      Value = GetDefaultValue(TRef, $Result, $TPackage, $CProduct);
			
			Inst.manipulateCompileContextStart    (                                       $CProduct, ZeroPos);
			Inst.manipulateCompileContextBeforeSub(new Object[] { VarName, Type, Value }, $CProduct, ZeroPos);

			Expr = $ME.newExpr(TypeRC, InstName, VarName, Type, Value);

			// Simulate stack (Avoid manipulate method to avoid repeat warning)
			if(!Expr.manipulateCompileContextFinish($CProduct))
				return null;
		}

		if(!Expr.ensureParamCorrect($CProduct))
			return null;

		return Expr;
	}

	/** Compile a NewVar Statement */
	static public Expression ParseCompileReturnQuit(int[] Location, String Command, Object RValue, Expression Expr,
			int RValuePos, ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		// If the return value is null, just return it.
		if((RValue == null) || "quit".equals(Command)) return Expr;

		// Get the engine
		Engine $Engine = $CProduct.getEngine();

		TypeRef       RType     = $CProduct.getReturnTypeRefOf(RValue);
		ExecSignature Signature = $CProduct.getClosestSignature();
		if(!$CProduct.isMacro() && !$CProduct.isSubRoutine()) {
			$CProduct.reportWarning("Return statement found in non-returnable context", null, RValuePos);

		} else if(Signature == null) {
			$CProduct.reportFatalError("Internal Error: Unable to retrieve the executable signature", null, RValuePos);
			return null;

		}
		
		Type SRT = $CProduct.getTypeAtCompileTime(Signature.getReturnTypeRef());
		Type RT  = $CProduct.getTypeAtCompileTime(RType);
		
		if(!MType.CanTypeRefByAssignableByInstanceOf(null, $Engine, SRT.getTypeRef(), RT.getTypeRef()) &&
		 	(
		 		!TKJava.TNumber.getTypeInfo().canBeAssignedByInstanceOf(SRT) ||
		 		!TKJava.TNumber.getTypeInfo().canBeAssignedByInstanceOf(RT)
		 	)
		 ) {
			$CProduct.reportError(
				String.format(
					"Invalid return type: %s needed but %s found (%s) <Util_Statments:217>",
					SRT, RT, $Engine.toString(RValue)
				), null, RValuePos);
			return null;
		}
		return Expr;
	}
}
