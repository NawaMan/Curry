package net.nawaman.curry;

import java.io.Console;
import java.util.Scanner;

import net.nawaman.curry.Expression.Expr_Expr;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.util.UArray;
import net.nawaman.util.UNumber;

public class Instructions_ForSpeed {
	
	/** Try to return the non-null default value if the given value is null. */
	static abstract class Inst_NullAware extends Inst_AbstractSimple {
		
		Inst_NullAware(Engine pEngine, String ISpecStr) {
			super(pEngine, ISpecStr);
		}
		abstract int getExpressionIndex( Expression pExpr, CompileProduct pCProduct);
		abstract int getDefaultTypeIndex(Expression pExpr, CompileProduct pCProduct);
		
		TypeRef getDefaultTypeTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(this.getDefaultTypeIndex(pExpr, pCProduct)));
		}
		
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(this.getExpressionIndex(pExpr, pCProduct));
			
			TypeRef TR0 = null;
			if(O instanceof Expr_Expr)  TR0 = pCProduct.getReturnTypeRefOf(((Expr_Expr)O).getExpr());
			else                        TR0 = pCProduct.getReturnTypeRefOf(O);

			TypeRef TR1 = this.getDefaultTypeTypeRef(pExpr, pCProduct);
			
			if(TR0 == null) TR0 = TKJava.TAny.getTypeRef();
			if(TR1 == null) TR1 = TKJava.TAny.getTypeRef();
			
			if((TR0 == TR1) || TR0.equals(TR1)) return TR0;
			
			if(TR1 instanceof TypeTypeRef) TR1 = ((TypeTypeRef)TR1).TheRef;
			else {
				Type T1 = pCProduct.getEngine().getTypeManager().getTypeFromRefNoCheck(null, TR1);
				if(T1 == null) return TKJava.TAny.getTypeRef();
				TR1 = T1.getTypeRef();
			}
			
			if(O == null) return TR1;
			
			TypeRef TRef = pCProduct.getEngine().getTypeManager().getClosestSharedAncessorOf(TR0, TR1);
			if(TRef == null) return TKJava.TAny.getTypeRef();
			return TRef;
		}
	}
	
	/** Try to return the non-null default value if the given value is null. */
	static public class Inst_TryNoNull extends Inst_NullAware {
		static public final String Name = "tryNoNull";
		
		Inst_TryNoNull(Engine pEngine) {
			super(pEngine, "=" + Name + "(~,!):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			if(pParams[0] == null) {
				Type T = (Type)pParams[1];
				if(T == null) return null;
				return T.getNoNullDefaultValue(pContext);
			}
			return pParams[0];
		}
		/**{@inherDoc}*/ @Override int getExpressionIndex( Expression pExpr, CompileProduct pCProduct) { return 0; }
		/**{@inherDoc}*/ @Override int getDefaultTypeIndex(Expression pExpr, CompileProduct pCProduct) { return 1; }
	}
	
	/** Do the E if the first input is not null. */
	static public class Inst_DoWhenNoNull extends Inst_NullAware {
		static public final String Name = "doWhenNoNull";
		
		Inst_DoWhenNoNull(Engine pEngine) {
			super(pEngine, Name + "(~,E,!):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Expression E = Expression.toExpr(pParams[1]);
			if(E == null) return null;
			
			if(pParams[0] == null) {
				Type T = (Type)pParams[2];
				if(T == null) return null;
				return T.getDefaultValue(pContext);
			}
			return this.executeAnExpression(pContext, E);
		}
		/**{@inherDoc}*/ @Override int getExpressionIndex( Expression pExpr, CompileProduct pCProduct) { return 1; }
		/**{@inherDoc}*/ @Override int getDefaultTypeIndex(Expression pExpr, CompileProduct pCProduct) { return 2; }
	}
	
	/** Do the E if the first input is not null. */
	static public class Inst_DoWhenValidIndex extends Inst_NullAware {
		static public final String Name = "doWhenValidIndex";
		
		Inst_DoWhenValidIndex(Engine pEngine) {
			super(pEngine, Name + "(~[],i,E,!):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Expression E = Expression.toExpr(pParams[2]);
			if(E == null) return null;

			Object Array =           pParams[0];
			int    Index = ((Integer)pParams[1]).intValue();
			if((Index < 0) || (Array == null) || (Index >= UArray.getLength(Array))) {
				Type T = (Type)pParams[3];
				if(T == null) return null;
				return T.getDefaultValue(pContext);
			}
			return this.executeAnExpression(pContext, E);
		}
		/**{@inherDoc}*/ @Override int getExpressionIndex( Expression pExpr, CompileProduct pCProduct) { return 2; }
		/**{@inherDoc}*/ @Override int getDefaultTypeIndex(Expression pExpr, CompileProduct pCProduct) { return 1; }
	}
	
	// Which ---------------------------------------------------------------------------------------

	static public class Inst_Which extends Inst_AbstractSimple {
		static public final String Name = "which";
		
		Inst_Which(Engine pEngine) {
			super(pEngine, Name + "(?,E,E):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			int I = 2;
			if(Boolean.TRUE.equals(pParams[0])) I = 1;

			Expression Expr = (Expression)pParams[I];
			if(Expr == null) return null;
			if(Expr.isData()) return Expr.getData();
			return this.executeAnExpression(pContext, Expr);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = null;
			
			// First E
			O = pExpr.getParam(1);
			TypeRef TR0 = null;
			if(O instanceof Expr_Expr)  TR0 = pCProduct.getReturnTypeRefOf(((Expr_Expr)O).getExpr());
			else                        TR0 = pCProduct.getReturnTypeRefOf(O);

			// Second E
			O = pExpr.getParam(1);
			TypeRef TR1 = null;
			if(O instanceof Expr_Expr)  TR1 = pCProduct.getReturnTypeRefOf(((Expr_Expr)O).getExpr());
			else                        TR1 = pCProduct.getReturnTypeRefOf(O);
			
			if(TR0 == null) TR0 = TKJava.TAny.getTypeRef();
			if(TR1 == null) TR1 = TKJava.TAny.getTypeRef();
			
			if((TR0 == TR1) || TR0.equals(TR1)) return TR0;
			
			TypeRef TRef = pCProduct.getEngine().getTypeManager().getClosestSharedAncessorOf(TR0, TR1);
			if(TRef == null) return TKJava.TAny.getTypeRef();
			return TRef;
		}
	}
	
	// Operation -----------------------------------------------------------------------------------

	static public class Inst_Abs extends Inst_AbstractSimple {
		static public final String Name = "abs";
		
		Inst_Abs(Engine pEngine) {
			super(pEngine, "=" + Name + "(#):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.abs((Number)pParams[0]);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return Instructions_Operations.getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}

	static public class Inst_Neg extends Inst_AbstractSimple {
		static public final String Name = "neg";
		
		Inst_Neg(Engine pEngine) {
			super(pEngine, "=" + Name + "(#):#");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.neg((Number)pParams[0]);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return Instructions_Operations.getReturnTypeOfNumbers(pExpr, pCProduct);
		}
	}

	static public class Inst_IsZero extends Inst_AbstractSimple {
		static public final String Name = "isZero";
		
		Inst_IsZero(Engine pEngine) {
			super(pEngine, "=" + Name + "(#):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return (pParams[0] == null)?true:((Number)pParams[0]).doubleValue() == 0.0;
		}
	}

	static public class Inst_IsPositive extends Inst_AbstractSimple {
		static public final String Name = "isPositive";
		
		Inst_IsPositive(Engine pEngine) {
			super(pEngine, "=" + Name + "(#):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.isPositive((Number)pParams[0]);
		}
	}

	static public class Inst_IsNegative extends Inst_AbstractSimple {
		static public final String Name = "isNegative";
		
		Inst_IsNegative(Engine pEngine) {
			super(pEngine, "=" + Name + "(#):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UNumber.isNegative((Number)pParams[0]);
		}
	}

	static public class Inst_IsOne extends Inst_AbstractSimple {
		static public final String Name = "isOne";
		
		Inst_IsOne(Engine pEngine) {
			super(pEngine, "=" + Name + "(#):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return ((Number)pParams[0]).intValue() == 1;
		}
	}

	static public class Inst_IsMinusOne extends Inst_AbstractSimple {
		static public final String Name = "isMinusOne";
		
		Inst_IsMinusOne(Engine pEngine) {
			super(pEngine, "=" + Name + "(#):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return ((Number)pParams[0]).intValue() == -1;
		}
	}

	static public class Inst_IsNull extends Inst_AbstractSimple {
		static public final String Name = "isNull";
		
		Inst_IsNull(Engine pEngine) {
			super(pEngine, "=" + Name + "(~):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return (pParams[0] == null);
		}
	}

	static public class Inst_IsNotNull extends Inst_AbstractSimple {
		static public final String Name = "isNotNull";
		
		Inst_IsNotNull(Engine pEngine) {
			super(pEngine, "=" + Name + "(~):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return (pParams[0] != null);
		}
	}
	
	// Iterator ------------------------------------------------------------------------------------
	static public class Inst_IteratorHasNext extends Inst_AbstractSimple {
		static public final String Name = "iteratorHasNext";
		
		Inst_IteratorHasNext(Engine pEngine) {
			super(pEngine, Name + "("+java.util.Iterator.class.getCanonicalName()+"):?");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return ((java.util.Iterator<?>)pParams[0]).hasNext();
		}
	}
	static public class Inst_IteratorNext extends Inst_AbstractSimple {
		static public final String Name = "iteratorNext";
		
		Inst_IteratorNext(Engine pEngine) {
			super(pEngine, Name + "("+java.util.Iterator.class.getCanonicalName()+"):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return ((java.util.Iterator<?>)pParams[0]).next();
		}
	}
	static public class Inst_GetIterator extends Inst_AbstractSimple {
		static public final String Name = "getIterator";
		
		Inst_GetIterator(Engine pEngine) {
			super(pEngine,
					Name + "("+java.lang.Iterable.class.getCanonicalName()+"):"+
					java.util.Iterator.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return ((java.lang.Iterable<?>)pParams[0]).iterator();
		}
	}
	
	// Console -------------------------------------------------------------------------------------
	static public class Inst_ReadConsoleLine extends Inst_AbstractSimple {
		static public final String Name = "readConsoleLine";
		
		Inst_ReadConsoleLine(Engine pEngine) {
			super(pEngine, Name + "():$");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Scanner S = new Scanner(System.in);
			return S.nextLine();
		}
	}
	static public class Inst_ReadConsolePasswordLine extends Inst_AbstractSimple {
		static public final String Name = "readConsolePasswordLine";
		
		Inst_ReadConsolePasswordLine(Engine pEngine) {
			super(pEngine, Name + "():$");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Console C = System.console();
			if(C == null) return null;
			return new String(C.readPassword());
		}
	}
	
}
