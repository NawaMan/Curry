package net.nawaman.curry;

import java.lang.reflect.Array;

import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.util.FormattableAdaptor;
import net.nawaman.util.UArray;
import net.nawaman.util.UNumber;
import net.nawaman.util.UObject;

public class Instructions_Operations {
	
	// Boolean -------------------------------------------------------------------------------------
	
	static public final class Inst_AND extends Inst_AbstractCutShort {
		static public final String Name = "AND";
		
		Inst_AND(Engine pEngine) {
			super(pEngine, "=" + Name + "(?...):?");
		}
		
		// Configure the process ----------------------------------------------
		
		/**{@inherDoc}*/ @Override
		protected boolean isSingle() {
			return true;
		}
		/**{@inherDoc}*/ @Override
		protected Object getEarlyReturn(Context pContext, Object[] pParams) {
			if((pParams == null) || (pParams.length == 0)) return true;
			return pParams[0];
		}
		/**{@inherDoc}*/ @Override
		protected boolean processSingleValue(Context pContext, Object O) {
			return !Boolean.TRUE.equals(O);
		}
		/**{@inherDoc}*/ @Override
		protected Object getBreakReturn(Context pContext, Object[] pParams) {
			return false;
		}
		/**{@inherDoc}*/ @Override
		protected Object getDoneReturn(Context pContext, Object[] pParams)  {
			return  true;
		}
	}
	
	static public final class Inst_OR extends Inst_AbstractCutShort {
		static public final String Name = "OR";
		
		Inst_OR(Engine pEngine) {
			super(pEngine, "=" + Name + "(?...):?");
		}
		// Configure the process ---------------------------------------------------
		
		/**{@inherDoc}*/ @Override
		protected boolean isSingle() {
			return true;
		}
		/**{@inherDoc}*/ @Override
		protected Object getEarlyReturn(Context pContext, Object[] pParams) {
			if((pParams == null) || (pParams.length == 0)) return true;
			return pParams[0];
		}
		/**{@inherDoc}*/ @Override
		protected boolean processSingleValue(Context pContext, Object O) {
			return Boolean.TRUE.equals(O);
		}
		/**{@inherDoc}*/ @Override
		protected Object getBreakReturn(Context pContext, Object[] pParams) {
			return true;
		}
		/**{@inherDoc}*/ @Override
		protected Object getDoneReturn(Context pContext, Object[] pParams) {
			return false;
		}
	}
	
	static public final class Inst_XOR extends Inst_AbstractSimple {
		static public final String Name = "XOR";
		
		Inst_XOR(Engine pEngine) {
			super(pEngine, "=" + Name + "(?,?):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return (Boolean.TRUE.equals(pParams[0]) ^ Boolean.TRUE.equals(pParams[1]));
		}
	}
	
	static public final class Inst_NOT extends Inst_AbstractSimple {
		static public final String Name = "NOT";
		
		Inst_NOT(Engine pEngine) {
			super(pEngine, "=" + Name + "(?):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return !Boolean.TRUE.equals(pParams[0]);
		}
	}
	
	// Number --------------------------------------------------------------------------------------
	
	static TypeRef getReturnTypeOfNumbers(Expression pExpr, CompileProduct pCProduct) {
		UNumber.NumberType NT = UNumber.NumberType.BYTE;
		for(int i = pExpr.getParamCount(); --i >= 0; ) { 
			Object  O  = pExpr.getParam(0);
			TypeRef TR = null;
			if(O instanceof Number) TR = pCProduct.getEngine().getTypeManager().getTypeOf(O).getTypeRef();
			else if(O instanceof Expression) {
				Expression Expr = (Expression)O;
				if(     Expr.isData()) TR = pCProduct.getEngine().getTypeManager().getTypeOf(Expr.getData()).getTypeRef();
				else if(Expr.isExpr()) TR = pCProduct.getReturnTypeRefOf(Expr.getExpr());
				else {
					Instruction I = pCProduct.getEngine().getInstruction(null, ((Expression)O).getInstructionNameHash());
					TR = (I == null)?null:I.getReturnTypeRef((Expression)O, pCProduct);
				}
			}
			else TKJava.TNumber.getTypeRef();
			/* */
			if(     TKJava.TInteger.getTypeRef().equals(TR)) NT = UNumber.NumberType.max(NT, UNumber.NumberType.INT);
			else if(TKJava.TDouble.getTypeRef().equals(TR))  NT = UNumber.NumberType.max(NT, UNumber.NumberType.DOUBLE);
			else if(TKJava.TByte.getTypeRef().equals(TR))    NT = UNumber.NumberType.max(NT, UNumber.NumberType.INT);
			else if(TKJava.TLong.getTypeRef().equals(TR))    NT = UNumber.NumberType.max(NT, UNumber.NumberType.LONG);
			else if(TKJava.TShort.getTypeRef().equals(TR))   NT = UNumber.NumberType.max(NT, UNumber.NumberType.INT);
			else if(TKJava.TFloat.getTypeRef().equals(TR))   NT = UNumber.NumberType.max(NT, UNumber.NumberType.DOUBLE);
			else TKJava.TNumber.getTypeRef(); /* */
		}/* */
		if(     NT == UNumber.NumberType.INT)    return TKJava.TInteger.getTypeRef();
		else if(NT == UNumber.NumberType.DOUBLE) return TKJava.TDouble.getTypeRef();
		else if(NT == UNumber.NumberType.BYTE)   return TKJava.TInteger.getTypeRef();
		else if(NT == UNumber.NumberType.LONG)   return TKJava.TLong.getTypeRef();
		else if(NT == UNumber.NumberType.SHORT)  return TKJava.TInteger.getTypeRef();
		else if(NT == UNumber.NumberType.FLOAT)  return TKJava.TDouble.getTypeRef(); /* */
		return TKJava.TNumber.getTypeRef();
	}
	
	static public class InstPlus extends Inst_AbstractSimple {
		static public final String Name = "plus";
		
		InstPlus(Engine pEngine) {
			super(pEngine, "=" + Name + "(#...):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.plus((Number[])UArray.convertArrayToArrayOf(pParams[0], Number.class, true));
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}
	
	static public class InstSubtract extends Inst_AbstractSimple {
		static public final String Name = "subtract";
		
		InstSubtract(Engine pEngine) {
			super(pEngine, "=" + Name + "(#,#):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.subtract((Number)pParams[0], (Number)pParams[1]);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}
	
	static public class InstMultiply extends Inst_AbstractSimple {
		static public final String Name = "multiply";
		
		InstMultiply(Engine pEngine) {
			super(pEngine, "=" + Name + "(#...):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.multiply((Number[])UArray.convertArrayToArrayOf(pParams[0], Number.class, true));
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}
	
	static public class InstDivide extends Inst_AbstractSimple {
		static public final String Name = "divide";
		
		InstDivide(Engine pEngine) {
			super(pEngine, "=" + Name + "(#,#):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			if((pParams[1] == null)?true:((Number)pParams[1]).doubleValue() == 0.0)
				return new SpecialResult.ResultError(new ArithmeticException("DivideByZero"));
			return UNumber.divide((Number)pParams[0], (Number)pParams[1]);
		}
		/** @inheritDoc() */ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}
	
	static public class InstModulus extends Inst_AbstractSimple {
		static public final String Name = "modulus";
		
		InstModulus(Engine pEngine) {
			super(pEngine, "=" + Name + "(#,#):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			if((pParams[1] == null)?true:((Number)pParams[1]).doubleValue() == 0.0)
				return new SpecialResult.ResultError(new ArithmeticException("DivideByZero"));
			return UNumber.modulus((Number)pParams[0], (Number)pParams[1]);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}
	
	static abstract public class InstNoShortLogic extends Inst_AbstractSimple {
		
		InstNoShortLogic(Engine pEngine, String ISpecStr) {
			super(pEngine, ISpecStr);
		}
		/** Returns the boolean result */
		abstract boolean getBooleanResult(boolean bResult, Object O);
		
		/** Returns the number result */
		abstract Number getNumberResult(Number[] Ns);
		
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Number[] Ns            = null;
			boolean  booleanResult = false;
			boolean  isBoolean     = false;
			boolean  isNumber      = false;
			boolean  isError       = false;
			for(int i = Array.getLength(pParams[0]); --i >= 0; ) {
				Object O = Array.get(pParams[0], i);
				if(O instanceof Boolean) {
					if(isNumber) { isError = true; break; }
					
					if(isBoolean) booleanResult = this.getBooleanResult(booleanResult, O);
					else {
						isBoolean     = true;
						booleanResult = Boolean.TRUE.equals(O);
					}
				} else if(O instanceof Number) {
					if(isBoolean) { isError = true; break; }
					
					if(!isNumber) {
						isNumber = true;
						Ns = new Number[Array.getLength(pParams[0])];
					}
					Ns[i] = (Number)O;
				}
			}
			
			if(!isError) {
				if(isBoolean) return booleanResult;
				if(isNumber)  return this.getNumberResult(Ns);
			}
			
			return this.reportParameterError(Engine, pContext,
						"Only all boolean or all number parameters are allowed.", pParams);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			for(int i = 0; i < pExpr.getParamCount(); i++) {
				if(TKJava.TBoolean.getTypeRef().equals(pCProduct.getReturnTypeRefOf(pExpr.getParam(i))))
					return TKJava.TBoolean.getTypeRef();
			}
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
		/**{@inheritDoc}*/ @Override
		public boolean ensureParamCorrect(Expression pExpr, CompileProduct pCProduct) {
			if(pCProduct.isCompileTimeCheckingNone() || pExpr.isData()) return true;
			
			// The parameters must be all boolean OR all number
			boolean isBoolean = false;
			boolean isNumber  = false;
			for(int i = 0; i < pExpr.getParamCount(); i++) {
				TypeRef TR = pCProduct.getReturnTypeRefOf(pExpr.getParam(i));
				Type    T  = pCProduct.getTypeAtCompileTime(TR);
				if(T == null) return false;

				if(TKJava.TBoolean.getTypeRef().equals(T.getTypeRef())) {
					if(isNumber) return false;
					isBoolean = true;
					
				} else if(MType.CanTypeRefByAssignableByInstanceOf(null, pCProduct.getEngine(),
						TKJava.TNumber.getTypeRef(), T.getTypeRef())) {
					if(isBoolean) return false;
					isNumber = true;
				}
				else return false;
			}
			return true;
		}
	}
	
	static public class InstAnd extends InstNoShortLogic {
		static public final String Name = "and";
		
		InstAnd(Engine pEngine) {
			super(pEngine, "=" + Name + "(~...):~");
		}
		/**{@inheritDoc}*/ @Override
		boolean getBooleanResult(boolean bResult, Object O) {
			return (bResult && Boolean.TRUE.equals(O));
		}
		/**{@inheritDoc}*/ @Override
		Number getNumberResult(Number[] Ns) {
			return UNumber.and(Ns);
		}
	}
	
	static public class InstOr extends InstNoShortLogic {
		static public final String Name = "or";
		
		InstOr(Engine pEngine) {
			super(pEngine, "=" + Name + "(~...):~");
		}
		/**{@inheritDoc}*/ @Override
		boolean getBooleanResult(boolean bResult, Object O) {
			return (bResult || Boolean.TRUE.equals(O));
		}
		/**{@inheritDoc}*/ @Override
		Number getNumberResult(Number[] Ns) {
			return UNumber.or(Ns);
		}
	}
	
	static public class InstXor extends InstNoShortLogic {
		static public final String Name = "xor";
		
		InstXor(Engine pEngine) {
			super(pEngine, "=" + Name + "(~...):~");
		}
		/**{@inheritDoc}*/ @Override
		boolean getBooleanResult(boolean bResult, Object O) {
			return (bResult != Boolean.TRUE.equals(O));
		}
		/**{@inheritDoc}*/ @Override
		Number getNumberResult(Number[] Ns) {
			return UNumber.xor(Ns);
		}
	}
	
	static public class InstNot extends Inst_AbstractSimple {
		static public final String Name = "not";
		
		InstNot(Engine pEngine) {
			super(pEngine, "=" + Name + "(~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.not((Number)pParams[0]);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}
	
	static public class InstShiftLeft extends Inst_AbstractSimple {
		static public final String Name = "shiftLeft";
		
		InstShiftLeft(Engine pEngine) {
			super(pEngine, "=" + Name + "(#,#):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.shiftLeft((Number)pParams[0], (Number)pParams[1]);
		}
		/**{@inheritDoc}*/ @Override public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}
	
	static public class InstShiftRight extends Inst_AbstractSimple {
		static public final String Name = "shiftRight";
		
		InstShiftRight(Engine pEngine) {
			super(pEngine, "=" + Name + "(#,#):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.shiftRight((Number)pParams[0], (Number)pParams[1]);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}
	
	static public class InstShiftRightUnsigned extends Inst_AbstractSimple {
		static public final String Name = "shiftRightUnsigned";
		
		InstShiftRightUnsigned(Engine pEngine) {
			super(pEngine, "=" + Name + "(#,#):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.shiftRightUnsigned((Number)pParams[0], (Number)pParams[1]);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}
	
	static public class InstTo extends Inst_AbstractSimple {
		InstTo(Engine pEngine, UNumber.NumberType pNT, String ISpecSymbol) {
			super(pEngine, "=to" + pNT.getName() + "(#):"+ISpecSymbol);
			this.NT = pNT;
		}
		UNumber.NumberType NT = null;
		
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Number N = (Number)pParams[0]; if(N == null) N = 0;
			return UNumber.to(this.NT, N);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			if(this.NT == UNumber.NumberType.INT)    return TKJava.TInteger.getTypeRef();
			if(this.NT == UNumber.NumberType.DOUBLE) return TKJava.TDouble.getTypeRef();
			if(this.NT == UNumber.NumberType.BYTE)   return TKJava.TByte.getTypeRef();
			if(this.NT == UNumber.NumberType.SHORT)  return TKJava.TShort.getTypeRef();
			if(this.NT == UNumber.NumberType.LONG)   return TKJava.TLong.getTypeRef();
			if(this.NT == UNumber.NumberType.FLOAT)  return TKJava.TFloat.getTypeRef();
			return TKJava.TNumber.getTypeRef();
		}
	}
	
	// String --------------------------------------------------------------------------------------
	static public class InstLength extends Inst_AbstractSimple {
		static public final String Name = "length";
		
		InstLength(Engine pEngine) {
			super(pEngine, "=" + Name + "($):i");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			if(O == null) return 0;
			return O.toString().length();
		}
	}
	
	static public class InstCharAt extends Inst_AbstractSimple {
		static public final String Name = "charAt";
		
		InstCharAt(Engine pEngine) {
			super(pEngine, "=" + Name + "(+$,+i):'");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			return O.toString().charAt(((Number)pParams[1]).intValue());
		}
	}
	
	static public class InstCharToInt extends Inst_AbstractSimple {
		static public final String Name = "charToInt";
		
		InstCharToInt(Engine pEngine) {
			super(pEngine, "=" + Name + "('):i");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			return (O == null) ? null : (int)((Character)O).charValue();
		}
	}
	
	static public class InstIntToChar extends Inst_AbstractSimple {
		static public final String Name = "intToChar";
		
		InstIntToChar(Engine pEngine) {
			super(pEngine, "=" + Name + "(i):'");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			return (O == null) ? null : (char)((Integer)O).intValue();
		}
	}
	
	static public class InstConcat extends Inst_AbstractSimple {
		static public final String Name = "concat";
		
		InstConcat(Engine pEngine) {
			super(pEngine, Name + "(~...):$");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			StringBuffer SB = new StringBuffer();
			
			if(pParams != null) {
				pParams = UArray.getObjectArray(pParams[0]);
				// Use toString
				for(int i = 0; i < pParams.length; i++) SB.append(UObject.toString(pParams[i]));
			}
			
			return SB.toString();
		}
	}
	
	static public class InstFormat extends Inst_AbstractSimple {
		static public final String Name = "format";
		
		InstFormat(Engine pEngine) {
			super(pEngine, Name + "(+$,~...):$");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return FormattableAdaptor.doFormat(pContext.getEngine(), (String)pParams[0], UArray.getObjectArray(pParams[1]));
		}
	}
	
	// Console -------------------------------------------------------------------------------------
	// Display and return
	static public class InstShow extends Inst_AbstractSimple {
		static public final String Name = "show";
		
		InstShow(Engine pEngine) {
			super(pEngine, Name + "(+$,~,+$):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			pContext.getEngine().getDefaultPrinter().print(pParams[0] + this.Engine.getDisplayObject(pContext, pParams[1]) + pParams[2]);
			return pParams[1];
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(1));
		}
	}
	static public class InstToDisplayString extends Inst_AbstractSimple {
		static public final String Name = "toDisplayString";
		
		InstToDisplayString(Engine pEngine) {
			super(pEngine, Name + "(~):$");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return this.Engine.getDisplayObject(pContext, pParams[0]);
		}
	}
	
	static public class InstPrint extends Inst_AbstractSimple {
		static public final String Name = "print";
		
		InstPrint(Engine pEngine) {
			super(pEngine, Name + "(~):$");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			String Str;
			Object Obj = pParams[0];
			
			if(!(Obj instanceof DObjectStandalone)) Str = this.Engine.toString(pContext, Obj);
			else {
				Str = (String)((DObject)((DObjectStandalone)Obj).getAsDObject()).invoke(
					pContext,
					null,
					false,
					null,
					"toString",
					UObject.EmptyObjectArray
				);
			}
			
			pContext.getEngine().getDefaultPrinter().print(Str);
			return Str;
		}
	}
	static public class InstPrintLn extends Inst_AbstractSimple {
		static public final String Name = "println";
		
		InstPrintLn(Engine pEngine) {
			super(pEngine, Name + "(~):$");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			String Str;
			Object Obj = pParams[0];
			
			if(!(Obj instanceof DObjectStandalone)) Str = this.Engine.toString(pContext, Obj);
			else {
				Str = (String)((DObject)((DObjectStandalone)Obj).getAsDObject()).invoke(
					pContext,
					null,
					false,
					null,
					"toString",
					UObject.EmptyObjectArray
				);
			}
			
			pContext.getEngine().getDefaultPrinter().println(Str);
			return Str;
		}
	}
	static public class InstPrintNewLine extends Inst_AbstractSimple {
		static public final String Name = "printNewLine";
		
		InstPrintNewLine(Engine pEngine) {
			super(pEngine, Name + "():$");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			pContext.getEngine().getDefaultPrinter().println();
			return "\n";
		}
	}
	
	static public class InstPrintFormat extends Inst_AbstractSimple {
		static public final String Name = "printf";
		
		InstPrintFormat(Engine pEngine) {
			super(pEngine, Name + "(+$,~...):$");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			String Str = FormattableAdaptor.doFormat(pContext.getEngine(), (String)pParams[0], UArray.getObjectArray(pParams[1]));
			pContext.getEngine().getDefaultPrinter().print(Str);
			return Str;
		}
	}
}
