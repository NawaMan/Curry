package net.nawaman.curry.compiler;

import net.nawaman.curry.Engine;
import net.nawaman.curry.Expression;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.TKJava;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;

public class Util_Operator {

	static public Object ParseCompileOperationPlusSubstract(ParseResult $Result, PTypePackage $TPackage,
			CompileProduct $CProduct) {
		
		Object[] Os = $Result.valuesOf("#Operand", $TPackage, $CProduct);
		if(Os.length == 1) return Os[0];

		// Get the engine
		Engine      $Engine = $CProduct.getEngine();
		MExecutable $ME     = $Engine.getExecutableManager();

		String[] Ss   = $Result.textsOf(      "$Operator");
		int[][]  LRCs = $Result.locationCRsOf("$Operator");
		boolean  HasMinus  = false;
		boolean  HasString = TKJava.TString.getTypeRef().equals($CProduct.getReturnTypeRefOf(Os[0]));
		
		for(int i = 0; i < Ss.length; i++) {
			if(Ss[i].charAt(0) == '-') {
				HasMinus = true;

				int[] Location = LRCs[i];
				Expression Expr = $ME.newExpr(Location, "neg", Os[i+1]);
				Os[i+1] = Expr;
				if(!Expr.ensureParamCorrect($CProduct)) return null;
			}

			HasString = HasString | TKJava.TString.getTypeRef().equals($CProduct.getReturnTypeRefOf(Os[i+1]));
		}

		if(!HasMinus && HasString) {
			// String Concat
			int[] Location = LRCs[0];
			Expression Expr = $ME.newExpr(Location, "concat", (Object[])Os);

			if(!Expr.ensureParamCorrect($CProduct)) return null;
			return Expr;
		}

		int[] Location = LRCs[0];
		Expression Expr = $ME.newExpr(Location, "plus", (Object[])Os);

		if(!Expr.ensureParamCorrect($CProduct)) return null;
		return Expr;
	}
	
}
