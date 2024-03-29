package net.nawaman.curry.compiler;

import net.nawaman.curry.Engine;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Inst_Assignment;
import net.nawaman.curry.Instruction;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Inst_Assignment.ANDTo;
import net.nawaman.curry.Inst_Assignment.AddTo;
import net.nawaman.curry.Inst_Assignment.AppendTo;
import net.nawaman.curry.Inst_Assignment.DivTo;
import net.nawaman.curry.Inst_Assignment.ModulusOf;
import net.nawaman.curry.Inst_Assignment.MulTo;
import net.nawaman.curry.Inst_Assignment.ORTo;
import net.nawaman.curry.Inst_Assignment.OperatorProvider;
import net.nawaman.curry.Inst_Assignment.ShiftLeftBy;
import net.nawaman.curry.Inst_Assignment.ShiftRightBy;
import net.nawaman.curry.Inst_Assignment.ShiftRightUnsignedBy;
import net.nawaman.curry.Inst_Assignment.SubtractFrom;
import net.nawaman.curry.Inst_Assignment.XORTo;
import net.nawaman.curry.Inst_Assignment.bitwiseAndTo;
import net.nawaman.curry.Inst_Assignment.bitwiseOrTo;
import net.nawaman.curry.Inst_Assignment.bitwiseXorTo;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;

public class Util_Operation {

	static TypeRef TA = TKJava.TAny    .getTypeRef();
	static TypeRef TI = TKJava.TInteger.getTypeRef();
	static TypeRef TN = TKJava.TNumber .getTypeRef();
	static TypeRef TS = TKJava.TString .getTypeRef();
	static TypeRef TB = TKJava.TBoolean.getTypeRef();
	
	static boolean CheckParam(CompileProduct CP, Object O, TypeRef pPosibleType, String pOperator, String OperandStr, boolean IsSwap, int pPosition) {
		if(CP.isCompileTimeCheckingNone()) return true;
		TypeRef TRef    = CP.getReturnTypeRefOf(O);
		Boolean IsMatch = IsSwap
							? CP.getEngine().getTypeManager().mayTypeRefBeCastedTo(TRef, pPosibleType)
							: CP.getEngine().getTypeManager().mayTypeRefBeCastedTo(pPosibleType, TRef);
		if(Boolean.TRUE.equals(IsMatch)) return true;
		if(IsMatch == null)
			 CP.reportWarning(String.format("Invalid operand for '%s' (%s: %s)", pOperator, OperandStr, TRef), null, pPosition);
		else CP.reportError(  String.format("Invalid operand for '%s' (%s: %s)", pOperator, OperandStr, TRef), null, pPosition);
		return false;
	}
	
	/** Compile a cast expression */
	static public Expression CompileAssign(Expression Operand, String OperandStr, String OperatorStr,
			String ValueStr, Object Value, int OperandPos, int ValuePos,
			ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {

		// Get the engine
		Engine $Engine = $CProduct.getEngine();
		
		int OperLength = OperatorStr.length();

		// Get the source assignment
		Inst_Assignment InstAssign = (Inst_Assignment)$Engine.getInstruction(Inst_Assignment.Name);

		String InstName   = Expression.getInstructionName(Operand, $Engine);
		int    SourceHash = InstAssign.getSourceHashOf(InstName);
		if(SourceHash == -1) {
			// TODO = This is a Hack
			if((OperLength == 0) && InstName.startsWith("get")) {
				InstName = "set" + InstName.substring(3);
				
				Instruction Inst      = $Engine.getInstruction(InstName);
				if(Inst != null) {
					Object[]    NewParams = new Object[Operand.getParamCount() + 1];
					for(int i = 0; i < Operand.getParamCount(); i++)
						NewParams[i] = Operand.getParam(i);
					NewParams[Operand.getParamCount()] = Value;
					
					Expression Expr = $Engine.getExecutableManager().newExpr(InstName, (Object[])NewParams);
					if(!Expr.ensureParamCorrect($CProduct) || !Expr.manipulateCompileContextFinish($CProduct)) return null;
					return Expr;
				}
			}
				
			$CProduct.reportFatalError("Unassignable <Util_Operation:63>.", null, $Result.startPositionOf(0));
			return null;
		}

		// Get the expression
		if(OperLength != 0) {
			// Incremental - so need to have a new value expression
			char    Operator     = OperatorStr.charAt(0);
			int     OperNameHash = -1;
			TypeRef OperTypeRef  = null;
			TypeRef ValueTypeRef = null;

			switch(Operator) {
				case '*': { OperNameHash = MulTo.NameHash;     OperTypeRef = TN; ValueTypeRef = TN; break; }
				case '/': { OperNameHash = DivTo.NameHash;     OperTypeRef = TN; ValueTypeRef = TN; break; }
				case '%': { OperNameHash = ModulusOf.NameHash; OperTypeRef = TN; ValueTypeRef = TI; break; }
				case '+': {
					TypeRef TR = $CProduct.getReturnTypeRefOf(Operand);
					if((TR == null) || TKJava.TAny.getTypeRef().equals(TR)) TR = $CProduct.getReturnTypeRefOf(Value);

					if(TS.equals(TR)) { OperNameHash = AppendTo.NameHash; OperTypeRef = TS; ValueTypeRef = TA; }
					else              { OperNameHash = AddTo   .NameHash; OperTypeRef = TN; ValueTypeRef = TN; }
					break;
				}
				case '-': { OperNameHash = SubtractFrom.NameHash; OperTypeRef = TN; ValueTypeRef = TI; break; }
				case '&': {
					if(OperLength == 1) { OperNameHash = bitwiseAndTo.NameHash; OperTypeRef = TN; ValueTypeRef = TN; }
					else                { OperNameHash = ANDTo.NameHash; OperTypeRef = TB; ValueTypeRef = TB; }
					break;
				}
				case '^': {
					if(OperLength == 1) { OperNameHash = bitwiseXorTo.NameHash; OperTypeRef = TN; ValueTypeRef = TN; }
					else                { OperNameHash = XORTo.NameHash; OperTypeRef = TB; ValueTypeRef = TB; }
					break;
				}
				case '|': {
					if(OperLength == 1) { OperNameHash = bitwiseOrTo.NameHash; OperTypeRef = TN; ValueTypeRef = TN; }
					else                { OperNameHash = ORTo.NameHash; OperTypeRef = TB; ValueTypeRef = TB; }
					break;
				}
				case '<': { OperNameHash = ShiftLeftBy.NameHash; OperTypeRef = TN; ValueTypeRef = TI; break; }
				case '>': {
					OperNameHash = (OperLength == 2) ? ShiftRightBy.NameHash : ShiftRightUnsignedBy.NameHash;
					OperTypeRef  = TN;
					ValueTypeRef = TI;
					break;
				}
			}

			if(!CheckParam($CProduct, Operand, OperTypeRef,  OperatorStr, OperandStr, true,  OperandPos)) return null;
			if(!CheckParam($CProduct, Value,   ValueTypeRef, OperatorStr, ValueStr,   false, ValuePos))   return null;

			OperatorProvider OP = InstAssign.getOperatorProvider(OperNameHash);
			if(OP == null) { $CProduct.reportFatalError("Unknown operator "+OperatorStr, null, OperandPos); return null; }

			// Create the Expression
			return Inst_Assignment.newAssExpr($Engine, Operand, OP.getName(), Value);
		} else {
			Expression Expr = Inst_Assignment.newSetExpr($Engine, Operand, Value);
			if(!Expr.ensureParamCorrect($CProduct) || !Expr.manipulateCompileContextFinish($CProduct)) return null;
			return Expr;
		}
	}

}
