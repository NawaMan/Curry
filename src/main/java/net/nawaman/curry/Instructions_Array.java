/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's Curry.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.curry;

import java.lang.reflect.Array;
import java.util.Vector;

import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.util.DataArray;
import net.nawaman.util.UArray;

/**
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class Instructions_Array {

	// Array ---------------------------------------------------------------------------------------
	static public class Inst_IsArray extends Inst_AbstractSimple {
		static public final String Name = "isArray";
		
		Inst_IsArray(Engine pEngine) {
			super(pEngine, "=" + Name + "(~):?");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			return (O instanceof DataArray<?>) || ((O != null) && O.getClass().isArray());
		}
	}
	static public class Inst_IsJavaArray extends Inst_AbstractSimple {
		static public final String Name = "isJavaArray";
		
		Inst_IsJavaArray(Engine pEngine) {
			super(pEngine, "=" + Name + "(~):?");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			return (O != null) && O.getClass().isArray();
		}
	}
	static public class Inst_IsDataArray extends Inst_AbstractSimple {
		static public final String Name = "isDataArray";
		
		Inst_IsDataArray(Engine pEngine) {
			super(pEngine, "=" + Name + "(~):?");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			return (O instanceof DataArray<?>);
		}
	}
	static public class Inst_IsArrayOf extends Inst_AbstractSimple {
		static public final String Name = "isArrayOf";
		
		Inst_IsArrayOf(Engine pEngine) {
			super(pEngine, "=" + Name + "(!,~):?");
		}
		@SuppressWarnings("rawtypes")
        @Override protected Object run(Context pContext, Object[] pParams) {
			Object O0 = pParams[0];
			Object O1 = pParams[1];
			if((O0 == null) || (O1 == null)) return false;
			
			Class<?> Cls;
			if(O1 instanceof DataArray)      Cls = ((DataArray)O1).getComponentClass();
			else if(O1.getClass().isArray()) Cls = O1.getClass().getComponentType();
			else return false;
			
			Class<?> TCls = (Class<?>)O0;
			return TCls.isAssignableFrom(Cls);
		}
	}

	static public class Inst_IsKindOfArray extends Inst_AbstractSimple {
		static public final String Name = "isKindOf_Array";
		
		Inst_IsKindOfArray(Engine pEngine) {
			super(pEngine, "=" + Name + "(!):?");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			if(O == null) return false;
			Type T = (Type)O;
			if((T instanceof TKArray.TArray) || (T == TKJava.TDataArray)) return true;
			if(TKJava.TDataArray.canBeAssignedByInstanceOf(T))            return true;
			return false;
		}
	}
	static public class Inst_IsKindOfArrayOf extends Inst_AbstractSimple {
		static public final String Name = "isKindOf_ArrayOf";
		
		Inst_IsKindOfArrayOf(Engine pEngine) {
			super(pEngine, "=" + Name + "(!,!):?");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Object O0 = pParams[0];
			Object O1 = pParams[1];
			if((O0 == null) || (O1 == null)) return false;
			Type T0 = (Type)O0;
			Type T1 = (Type)O1;
			
			if(T0 instanceof TKArray.TArray)
				return ((TKArray.TArray)T0).getContainType().canBeAssignedByInstanceOf(T1);
			
			// DataArray Type does not have information about component type  
			if((T0 == TKJava.TDataArray) || TKJava.TDataArray.canBeAssignedByInstanceOf(T0)) return false;
			return false;
		}
	}
	
	static public class Inst_GetLengthArrayObject extends Inst_AbstractSimple {
		static public final String Name = "getLengthArrayObject";
		
		Inst_GetLengthArrayObject(Engine pEngine) {
			super(pEngine, Name + "(+~[]):i");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return UArray.getLength(pParams[0]);
		}
	}
	static public class Inst_GetLengthArrayType extends Inst_AbstractSimple {
		static public final String Name = "getLengthArrayType";
		
		Inst_GetLengthArrayType(Engine pEngine) {
			super(pEngine, "=" + Name + "(+"+TKArray.TArray.class.getCanonicalName()+"):i");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return ((TKArray.TArray)pParams[0]).getLength();
		}
	}

	static public class Inst_GetComponentTypeArrayObject extends Inst_AbstractSimple {
		static public final String Name = "getComponentTypeArrayObject";
		
		Inst_GetComponentTypeArrayObject(Engine pEngine) {
			super(pEngine, "=" + Name + "(+~[]):!");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return UArray.getComponentType_OfInstance(pParams[0]);
		}
	}
	static public class Inst_GetComponentTypeArrayType extends Inst_AbstractSimple {
		
		static public final String Name = "getComponentTypeArrayType";
		
		Inst_GetComponentTypeArrayType(Engine pEngine) {
			super(pEngine, "=" + Name + "(+"+TKArray.TArray.class.getCanonicalName()+"):!");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			return ((TKArray.TArray)pParams[0]).getContainTypeRef().getTheType();
		}
	}

	static public class Inst_SetArrayElementAt extends Inst_AbstractSimple {
		static public final String Name = "setArrayElementAt";
		
		Inst_SetArrayElementAt(Engine pEngine) {
			super(pEngine, Name + "(+~[],+i,~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			int P = ((Integer)pParams[1]).intValue();
			if((P < 0) || (P >= UArray.getLength(pParams[0]))) 
				return new SpecialResult.ResultError(new ArrayIndexOutOfBoundsException(P));
			
			Type T = this.Engine.getTypeManager().getTypeOfTheInstanceOfNoCheck(pContext, UArray.getComponentType_OfInstance(pParams[0]));
			if(!T.canBeAssignedBy(pParams[2]))
				return this.reportParameterError(this.getEngine(), pContext, "invalid type assignment", pParams);
			
			UArray.set(pParams[0], P, pParams[2]);
			return pParams[2];
		}
		/**{@inherDoc}*/ @Override
		public boolean ensureParamCorrect(Expression E, CompileProduct CP) {
			if(CP.isCompileTimeCheckingNone()) return true;
			Object OArray = E.getParam(0);
			Object OValue = E.getParam(2);
			if(OValue == null) return true;

			TypeRef ATR  = CP.getReturnTypeRefOf(OArray);
			if(!TKArray.isArrayTypeRef(CP.getEngine(), ATR)) {
				return ReportCompileProblem(
						"setArrayElementAt:89", String.format("The given value is not an array (%s)", ATR),
						E, CP, false, false);
			}
			TypeRef ACTR = TKArray.getArrayComponentTypeRef(CP.getEngine(), ATR);
			ACTR = (ACTR == null) ? TKJava.TAny.getTypeRef() : ACTR;
			
			// Value
			TypeRef TRef = CP.getReturnTypeRefOf(OValue);
			
			Boolean MayMatch = CP.getEngine().getTypeManager().mayTypeRefBeCastedTo(ACTR, TRef);
			if(!Boolean.TRUE.equals(MayMatch))
				return ReportCompileProblem(
						"setArrayElementAt:353", String.format("Imcompatible type (%s for %s)", TRef, ACTR),
						E, CP, (MayMatch == null), false);

			return true;
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(2);
			if(O instanceof Expression) return ((Expression)O).getReturnTypeRef(pCProduct);
			return pCProduct.getEngine().getTypeManager().getTypeOf(O).getTypeRef();
		}
	}
	static public class Inst_GetArrayElementAt extends Inst_AbstractSimple {
		static public final String Name = "getArrayElementAt";
		
		Inst_GetArrayElementAt(Engine pEngine) {
			super(pEngine, Name + "(+~[],+i):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			int P = ((Integer)pParams[1]).intValue();
			if((P < 0) || (P >= UArray.getLength(pParams[0])))
				return new SpecialResult.ResultError(new CurryError("", pContext, new ArrayIndexOutOfBoundsException(P)));
			return UArray.get(pParams[0], P);
		}
		/**{@inherDoc}*/ @Override
		public boolean ensureParamCorrect(Expression E, CompileProduct CP) {
			if(CP.isCompileTimeCheckingNone()) return true;
			Object OArray = E.getParam(0);
			
			TypeRef ATR = CP.getReturnTypeRefOf(OArray);
			if(!TKArray.isArrayTypeRef(CP.getEngine(), ATR)) {
				return ReportCompileProblem(
						"getArrayElementAt:126", String.format("The given value is not an array (%s)", ATR),
						E, CP, false, false);
			}
			return true;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			if(pCProduct == null) return super.getReturnTypeRef(pExpr, pCProduct);
			Object O = pExpr.getParam(0);
			if(O == null) return TKJava.TAny.getTypeRef();
			
			// Get the array type
			TypeRef TR = pCProduct.getReturnTypeRefOf(O);
			TR = TKArray.getArrayComponentTypeRef(pCProduct.getEngine(), TR);
			return (TR == null) ? TKJava.TAny.getTypeRef() : TR;
		}
	}
	
	static public class Inst_GetArrayType extends Inst_AbstractSimple {
		static public final String Name = "getArrayType";
		
		Inst_GetArrayType(Engine pEngine) {
			super(pEngine, "=" + Name + "(+!,+i):!");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Type T = (Type)pParams[0];
			int Length = ((Integer)pParams[1]).intValue();
			TKArray TKA = ((TKArray)this.Engine.getTypeManager().getTypeKind(TKArray.KindName));
			return TKA.getType(null, pContext, TKA.getTypeSpec(null, T.getTypeRef(), Length));
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			if(pCProduct == null) return super.getReturnTypeRef(pExpr, pCProduct);
			Object O = pExpr.getParam(0);
			Object I = pExpr.getParam(2);
			int i = ((I instanceof Number) && !(I instanceof Double) && !(I instanceof Float))?((Number)I).intValue():-1;
			
			TypeRef TR = pCProduct.getReturnTypeRefOf(O);
			if(!(TR instanceof TypeTypeRef)) {
				if(TR == null) TR = TKJava.TAny.getTypeRef();
				return new TypeTypeRef(TKArray.newArrayTypeRef(TR, i));
			}
			return new TypeTypeRef(TKArray.newArrayTypeRef(((TypeTypeRef)TR).getTheRef(), i));
		}
	}
	// Create Native Array by default
	static public class Inst_NewArray extends Inst_AbstractSimple {
		static public final String Name = "newArray";
		
		Inst_NewArray(Engine pEngine) {
			super(pEngine, Name + "(+!,+i):~[]");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Type     T = (Type)pParams[0];
			Class<?> C = T.getDataClass(); 
			if(C == null) C = Object.class;
			int Length = ((Integer)pParams[1]).intValue();
			Object A = UArray.newArray(C, Length);
			
			// Set default value for primitive array			
			if((T instanceof TKJava.TJava) &&
				(
					Number   .class.isAssignableFrom(((TKJava.TJava)T).getTheDataClass()) ||
					Character.class.isAssignableFrom(((TKJava.TJava)T).getTheDataClass()) ||
					Boolean  .class.isAssignableFrom(((TKJava.TJava)T).getTheDataClass())
			)) {
				for(int i = UArray.getLength(A); --i >= 0; ) {
					Object O = null;
					if(     T == TKJava.TBoolean)   O =    false;
					else if(T == TKJava.TCharacter) O =      '0';
					else if(T == TKJava.TInteger)   O =        0;
					else if(T == TKJava.TDouble)    O =      0.0;
					else if(T == TKJava.TByte)      O =  (byte)0;
					else if(T == TKJava.TLong)      O =  (long)0;
					else if(T == TKJava.TFloat)     O =     0.0f;
					else if(T == TKJava.TShort)     O = (short)0;
					UArray.set(A, i, O);
				}
			}
			return A;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			if(pCProduct == null) return super.getReturnTypeRef(pExpr, pCProduct);
			Object O = pExpr.getParam(0);
			Object I = pExpr.getParam(1);
			int i = ((I instanceof Number) && !(I instanceof Double) && !(I instanceof Float))?((Number)I).intValue():-1;
			
			TypeRef TR = pCProduct.getReturnTypeRefOf(O);
			if(!(TR instanceof TypeTypeRef)) {
				if(TR == null) TR = TKJava.TAny.getTypeRef();
				return TKArray.newArrayTypeRef(TR, i);
			}
			return TKArray.newArrayTypeRef(((TypeTypeRef)TR).getTheRef(), i);
		}
	}
	// Create Native Array by default
	static public class Inst_NewArrayByClass extends Inst_AbstractSimple {
		static public final String Name = "newArrayByClass";

		Inst_NewArrayByClass(Engine pEngine) {
			super(pEngine, Name + "(+@,+i):~[]");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Class<?> C = (Class<?>)pParams[0]; 
			if(C == null) C = Object.class;
			int Length = ((Integer)pParams[1]).intValue();
			if(C.isPrimitive()) {
				if(C == int.class)     return new int[Length];
				if(C == boolean.class) return new boolean[Length];
				if(C == double.class)  return new double[Length];
				if(C == char.class)    return new char[Length];
				if(C == byte.class)    return new byte[Length];
				if(C == long.class)    return new long[Length];
				if(C == short.class)   return new short[Length];
				if(C == float.class)   return new float[Length];
			}
			return UArray.newArray(C, Length);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			Object I = pExpr.getParam(1);
			int i = ((I instanceof Number) && !(I instanceof Double) && !(I instanceof Float))?((Number)I).intValue():-1;
			if(O instanceof Class<?>) return TKArray.newArrayTypeRef(TKJava.Instance.getTypeByClass(pCProduct.getEngine(), null, (Class<?>)O).getTypeRef(), i);
			return TKArray.newArrayTypeRef(TKJava.TAny.getTypeRef(), i);
		}
	}
	
	static public class Inst_NewArrayLiteral extends Inst_AbstractSimple {	
		static public final String Name = "newArrayLiteral";
		
		Type TheType = null; 
		Inst_NewArrayLiteral(Engine pEngine, String ISpecSymbol, Type pTypeType) {
			super(pEngine, Name +
							((pTypeType==null)?"(+!,":("_"+pTypeType.toString()+"(")) +
							ISpecSymbol+"...):"+ISpecSymbol+"[]");
			this.TheType = pTypeType;
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Type T      = this.TheType;
			int  Offset = 0;
			
			if(this.TheType == null) {
				T = (Type)pParams[0];
				if(T == null) T = TKJava.TAny;
				Offset = 1;
			}
			
			pParams = UArray.getObjectArray(pParams[Offset]);
			int PCount = (pParams == null)?0:pParams.length;
			Object Result = Array.newInstance(T.getDataClass(), PCount);
			for(int i = 0; i < PCount; i++) {
				Object O = pParams[i];
				if((O != null) && !T.canBeAssignedBy(O)) {
					if(T.canBeAssignedBy(pParams)) {
						Result = Array.newInstance(T.getDataClass(), 1);
						Array.set(Result, 0, pParams);
						return Result;
					}
					
					return new SpecialResult.ResultError(
							new CurryError("Imcompatible Type Error: The value '"+ pContext.getEngine().getDisplayObject(O) +"' cannot " +
								"be assigned into " +
								"the array type of \""+ T +"\".",
								pContext
							)
						);
				}
				Array.set(Result, i, O);
			}
			return Result;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			if(pCProduct    == null) return super.getReturnTypeRef(pExpr, pCProduct);
			if(this.TheType != null) return TKArray.newArrayTypeRef(this.TheType.getTypeRef());
			
			int i  = pExpr.getParamCount() - 1;
			
			Object O = pExpr.getParam(0);
			TypeRef TR = pCProduct.getReturnTypeRefOf(O);
			if(!(TR instanceof TypeTypeRef)) {
				if(TR == null) TR = TKJava.TAny.getTypeRef();
				return TKArray.newArrayTypeRef(TR, i);
			}
			return TKArray.newArrayTypeRef(((TypeTypeRef)TR).getTheRef(), i);
		}
	}
	
	static public class Inst_ArrayToIterator extends Inst_AbstractSimple {
		static public final String Name = "arrayToIterator";
		
		Inst_ArrayToIterator(Engine pEngine) {
			super(pEngine, Name + "(~[]):"+java.util.Iterator.class.getCanonicalName());
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			if(O == null) return null;
			Type T = this.Engine.getTypeManager().getTypeOfNoCheck(pContext, O);
			if(T == null) T = TKJava.TAny;
			else          T = ((TKArray.TArray)T).getContainTypeRef().getTheType();
			return new net.nawaman.curry.util.IteratorArray<Object>(T, O);
		}
	}
	static public class Inst_IteratorToArray extends Inst_AbstractSimple {
		static public final String Name = "iteratorToArray";
		
		Inst_IteratorToArray(Engine pEngine) {
			super(pEngine, Name + "("+java.util.Iterator.class.getCanonicalName()+"):~[]");
		}
		@Override protected Object run(Context pContext, Object[] pParams) {
			Object O = pParams[0];
			if(O == null) return null;
			Type T = TKJava.TAny;
			if(O instanceof net.nawaman.curry.util.Iterator<?>) T = ((net.nawaman.curry.util.Iterator<?>)O).getType();
			if(T == null) T = TKJava.TAny;
			java.util.Iterator<?> I = (java.util.Iterator<?>)O;
			Vector<Object> TempI = new Vector<Object>();
			while(I.hasNext()) TempI.add(I.next());
			Object A = Array.newInstance(T.getDataClass(), TempI.size());
			for(int i = 0; i < TempI.size(); i++) Array.set(A, i, TempI.get(i));
			return A;
		}
	}
}
