package net.nawaman.curry.compiler;

import net.nawaman.curry.Engine;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Instruction;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.TypeRef;
import net.nawaman.regparser.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;

public class Util_StmLoop {

	/** Extract the body of a stack expression as group */
	static Expression ExtractBody(Engine $Engine, MExecutable $ME, Expression pExpr) {
		if(pExpr == null) return null;
		Expression Expr = pExpr;
		boolean isRunOnce = false;
		if(Expr.isInstruction($Engine, "runOnce")) {
			if(Expr.getSubExprCount() != 1) return pExpr;
			isRunOnce = true;
			Expr      = Expr.getSubExpr(0);
		}
		if(!Expr.isInstruction($Engine, "stack")) return pExpr;
		Expression[] Subs = new Expression[Expr.getSubExprCount()];
		for(int i = Subs.length; --i >= 0; ) Subs[i] = Expr.getSubExpr(i);
		return isRunOnce
				?$ME.newRunOnce(Expr.getColumn() , Expr.getLineNumber(), Subs)
				:$ME.newGroup(  Expr.getColumn() , Expr.getLineNumber(), Subs);
	}

	/** Parse and compile ForEach Statement */
	static public Object ParseCompileStmForEach(ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		// Get the engine
		Engine      $Engine = $CProduct.getEngine();
		MExecutable $ME     = $Engine.getExecutableManager();

		// Before ----------------------------------------------------------------------------------------------------------
		Instruction Inst = $Engine.getInstruction("forEach");
		// Manipulate the context - Before
		Inst.manipulateCompileContextStart($CProduct, $Result.posOf(0));

		// Parameters (check ourself for better error report) --------------------------------------------------------------
		String  Label      =          $Result.textOf("$Label");
		String  Name       =          $Result.textOf("$VarName");
		TypeRef TRef       = (TypeRef)$Result.valueOf("#TypeRef",    $TPackage, $CProduct);
		Object  Collection =          $Result.valueOf("#Collection", $TPackage, $CProduct);

		Object[] Params = new Object[] { Label, Name, $ME.newType($Result.locationCROf("#TypeRef"), TRef), Collection };
		// Manipulate the context before sub
		Inst.manipulateCompileContextBeforeSub(Params, $CProduct, $Result.posOf(0));

		// Body ------------------------------------------------------------------------------------------------------------
		Expression Body = ExtractBody($Engine, $ME, Expression.toExpr($Result.valueOf("#Body", $TPackage, $CProduct)));
		Expression Expr = $ME.newExprSub($Result.locationCROf("$Command"), "forEach", Params, Body);
		if(!Expr.ensureParamCorrect($CProduct) || !Expr.manipulateCompileContextFinish($CProduct)) return null;

		return Expr;
	}	
}
