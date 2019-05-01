package net.nawaman.curry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.nawaman.curry.StackOwner.OperationSearchKind;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.util.UClass;

public class Instructions_Java {

	static public class Inst_GetJavaClassByName extends Inst_AbstractSimple {
		static public final String Name = "getJavaClassByName";
		
		Inst_GetJavaClassByName(Engine pEngine) {
			super(pEngine, "=" + Name + "(+$):@");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UClass.getClassByName((String)pParams[0], MClassPaths.getClassLoaderOf(pContext));
		}
	}
	static public class Inst_GetJavaClassOf extends Inst_AbstractSimple {
		static public final String Name = "getJavaClassOf";
		
		Inst_GetJavaClassOf(Engine pEngine) {
			super(pEngine, "=" + Name + "(+~):@");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pParams[0].getClass();
		}
	}
	
	// Method --------------------------------------------------------------------------------------
	
	static public class Inst_GetJavaMethodByParams extends Inst_AbstractSimple {
		static public final String Name = "getJavaMethodByParams";
		
		Inst_GetJavaMethodByParams(Engine pEngine) {
			super(pEngine, "=" + Name + "(+@,+$,+?,~...):"+ Method.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UClass.getMethod((Class<?>)pParams[0], (String)pParams[1], ((Boolean)pParams[2]).booleanValue(),
					pParams[3]);
		}
	}
	static public class Inst_GetJavaMethodByParamClasss extends Inst_AbstractSimple {
		static public final String Name = "getJavaMethodByParamClasss";
		
		Inst_GetJavaMethodByParamClasss(Engine pEngine) {
			super(pEngine, "=" + Name + "(+@,+$,+?,@...):"+ Method.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return UClass.getMethodByParamClasses((Class<?>)pParams[0], (String)pParams[1],
					((Boolean)pParams[2]).booleanValue(), (Class<?>[])pParams[3]);
		}
	}
	
	static public class Inst_InvokeJavaObjectMethodByMethod extends Inst_AbstractSimple {
		static public final String Name = "invokeJavaObjectMethodByMethod";
		
		Inst_InvokeJavaObjectMethodByMethod(Engine pEngine) {
			super(pEngine, Name + "(+"+Method.class.getCanonicalName() + ",+~,~...):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try {
				Method M = (Method)pParams[0];
				// Change to Native if need and can
				Object   O = pParams[1];
				Class<?> C = M.getDeclaringClass();
				if(!C.isInstance(O) && (O instanceof DObjectStandalone)) {
					if(C.isAssignableFrom(DObject.class)) O = ((DObjectStandalone)O).getAsDObject();
					else                                  O = ((DObjectStandalone)O).getAsNative();
				}
				
				return UClass.invokeMethod(M, O, pParams[2]);
			} catch(Throwable T) {
				return new SpecialResult.ResultError(new CurryError("Java Invocation Error: ", pContext, T));
			}
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if((O instanceof Expression) &&
					pCProduct.getEngine().isExpressionOf(Inst_GetJavaMethodByParamClasss.Name, (Expression)O) &&
					(((Expression)O).getParamCount() >= 3)) {
				Expression Expr = (Expression)O;
				Object   OC  = Expr.getParam(0);
				Object   ON  = Expr.getParam(1);
				Object   OS  = Expr.getParam(2);
				Object   OP1 = Expr.getParam(3);
				Object[] OP2 = (OP1 instanceof Class<?>[])?null:new Object[Expr.getParamCount() - 3];
					
				Class<?>[] PCs = null;
				if(OP2 == null) PCs = (Class<?>[])OP1;
				else {
					PCs = new Class<?>[OP2.length];
					for(int i = OP2.length; --i >= 0; ) {
						if(OP2[i] instanceof Class<?>) PCs[i] = (Class<?>)OP2[i];
						else return super.getReturnTypeRef(pExpr, pCProduct);
					}
				}
					
				if((OC instanceof Class<?>) && (ON instanceof String) && (OS instanceof Boolean) && (PCs != null)) {
					if(PCs.length == 0) PCs = null;
					O = UClass.getMethodByParamClasses((Class<?>)OC, (String)ON, (Boolean)OS, PCs);
				}
			}
			if(O instanceof Method)
				return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(((Method)O).getReturnType()).getTypeRef();
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	static public class Inst_InvokeJavaClassMethodByMethod extends Inst_AbstractSimple {
		static public final String Name = "invokeJavaClassMethodByMethod";
		
		Inst_InvokeJavaClassMethodByMethod(Engine pEngine) {
			super(pEngine, Name + "(+"+Method.class.getCanonicalName()+",~...):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try { return UClass.invokeMethod((Method)pParams[0], null, pParams[1]); }
			catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if((O instanceof Expression) &&
					pCProduct.getEngine().isExpressionOf(Inst_GetJavaMethodByParamClasss.Name, (Expression)O) &&
					(((Expression)O).getParamCount() >= 3)) {
				Expression Expr = (Expression)O;
				Object   OC  = Expr.getParam(0);
				Object   ON  = Expr.getParam(1);
				Object   OS  = Expr.getParam(2);
				Object   OP1 = Expr.getParam(3);
				Object[] OP2 = (OP1 instanceof Class<?>[])?null:new Object[Expr.getParamCount() - 3];
					
				Class<?>[] PCs = null;
				if(OP2 == null) PCs = (Class<?>[])OP1;
				else {
					PCs = new Class<?>[OP2.length];
					for(int i = OP2.length; --i >= 0; ) {
						if(OP2[i] instanceof Class<?>) PCs[i] = (Class<?>)OP2[i];
						else return super.getReturnTypeRef(pExpr, pCProduct);
					}
				}
					
				if((OC instanceof Class<?>) && (ON instanceof String) && (OS instanceof Boolean) && (PCs != null)) {
					if(PCs.length == 0) PCs = null;
					O = UClass.getMethodByParamClasses((Class<?>)OC, (String)ON, (Boolean)OS, PCs);
				}
			}
			if(O instanceof Method)
				return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(((Method)O).getReturnType()).getTypeRef();
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	
	static public class Inst_InvokeJavaObjectMethod extends Inst_AbstractSimple {
		static public final String Name = "invokeJavaObjectMethod";
		
		Inst_InvokeJavaObjectMethod(Engine pEngine) {
			super(pEngine, Name + "(+~,+$,~...):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try {
				// Change to Native if need and can
				return UClass.invokeObjectMethod(pParams[0], (String)pParams[1], pParams[2]);
			} catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = null;
			
			TypeRef[] PTRefs = null;
			if(pExpr.getParam(2) != null) {
				Object[] Ps = (pExpr.getParam(2).getClass().isArray())?(Object[])pExpr.getParam(2):new Object[] { pExpr.getParam(2) };
				PTRefs = new TypeRef[Ps.length];
				for(int i = Ps.length; --i >= 0; ) {
					O = Ps[i];
					Type T = null;
					if(O instanceof Expression) {
						Instruction Inst = pCProduct.getEngine().getInstruction(null, ((Expression)O).getInstructionNameHash());
						TypeRef TR = Inst.getReturnTypeRef(((Expression)O), pCProduct);
						pCProduct.getEngine().getTypeManager().ensureTypeInitialized(TR);
						T = TR.getTheType();
					} else T = pCProduct.getEngine().getTypeManager().getTypeOf(O);
					if(T == null) return super.getReturnTypeRef(pExpr, pCProduct); // Unable to find
					PTRefs[i] = T.getTypeRef();
				}
			}
			
			O = pExpr.getParam(0);
			
			// Get Type
			Type T = null;
			if(O instanceof Expression) {
				Instruction Inst = pCProduct.getEngine().getInstruction(null, ((Expression)O).getInstructionNameHash());
				TypeRef TR = Inst.getReturnTypeRef(((Expression)O), pCProduct);
				pCProduct.getEngine().getTypeManager().ensureTypeInitialized(TR);
				T = TR.getTheType();
			} else T = pCProduct.getEngine().getTypeManager().getTypeOf(O);
			if(T == null) return this.getReturnTypeRef(pExpr, pCProduct);	// Unable to find
			
			O = pExpr.getParam(1);
			// Get Method Name
			String N = null;
			if(O instanceof String) N = (String)O;
			if(N == null) return this.getReturnTypeRef(pExpr, pCProduct);	// Unable to find
			
			ExecSignature ES = T.doData_searchOperationLocal(null, pCProduct.getEngine(), OperationSearchKind.ByTRefs,
					N, PTRefs, null);
			if(ES != null) return ES.getReturnTypeRef();
			
			return null;
		}
	}
	static public class Inst_InvokeJavaClassMethod extends Inst_AbstractSimple {
		static public final String Name = "invokeJavaClassMethod";
		
		Inst_InvokeJavaClassMethod(Engine pEngine) {
			super(pEngine, Name + "(+@,+$,~...):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try { return UClass.invokeStaticMethod((Class<?>)pParams[0], (String)pParams[1], pParams[2]); }
			catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O  = null;
			Engine E  = pCProduct.getEngine();
			MType  TM = E.getTypeManager();
			
			TypeRef[] PTRefs = null;
			if(pExpr.getParam(2) != null) {
				Object[] Ps = (Object[])pExpr.getParam(2);
				PTRefs = new TypeRef[Ps.length];
				for(int i = Ps.length; --i >= 0; ) {
					O = Ps[i];
					Type T = null;
					if(O instanceof Expression) {
						Instruction Inst = E.getInstruction(null, ((Expression)O).getInstructionNameHash());
						TypeRef TR = Inst.getReturnTypeRef(((Expression)O), pCProduct);
						TM.ensureTypeInitialized(TR);
						T = TR.getTheType();
					} else T = TM.getTypeOf(O);
					if(T == null) return super.getReturnTypeRef(pExpr, pCProduct);	// Unable to find
					PTRefs[i] = T.getTypeRef();
				}
			}
			
			O = pExpr.getParam(0);
			
			// Get Type
			Type T = null;
			if(O instanceof Class<?>) T = TM.getTypeOfTheInstanceOf((Class<?>)O);
			if(T == null) return super.getReturnTypeRef(pExpr, pCProduct);	// Unable to find
			
			O = pExpr.getParam(1);
			// Get Method Name
			String N = null;
			if(O instanceof String) N = (String)O;
			if(N == null) return super.getReturnTypeRef(pExpr, pCProduct);	// Unable to find
			
			ExecSignature ES = T.searchOperation(pCProduct.getEngine(), N, PTRefs);
			if(ES != null) return ES.getReturnTypeRef();
			
			return null;
		}
	}
	
	// Fields --------------------------------------------------------------------------------------
	
	static public class Inst_GetJavaField extends Inst_AbstractSimple {
		static public final String Name = "getJavaField";
		
		Inst_GetJavaField(Engine pEngine) {
			super(pEngine, "=" + Name + "(+@,+$,+?):"+ Field.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try {
				return UClass.getField((Class<?>)pParams[0], (String)pParams[1], ((Boolean)pParams[2]).booleanValue());
			} catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
	}
	
	static public class Inst_SetJavaObjectFieldValueByField extends Inst_AbstractSimple {
		static public final String Name = "setJavaObjectFieldValueByField";
		
		Inst_SetJavaObjectFieldValueByField(Engine pEngine) {
			super(pEngine, Name + "(+"+Field.class.getCanonicalName()+",+~,~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try {
				Field F =(Field)pParams[0];
				// Change to Native if need and can
				Object   O = pParams[1];
				Class<?> C = F.getDeclaringClass();
				if(!C.isInstance(O) && (O instanceof DObjectStandalone)) {
					if(C.isAssignableFrom(DObject.class)) O = ((DObjectStandalone)O).getAsDObject();
					else                                  O = ((DObjectStandalone)O).getAsNative();
				}
				
				UClass.setFieldValue(F, O, pParams[2]); return pParams[2];
			}
			catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(2));
		}
	}
	static public class Inst_SetJavaClassFieldValueByField extends Inst_AbstractSimple {
		static public final String Name = "setJavaClassFieldValueByField";
		
		Inst_SetJavaClassFieldValueByField(Engine pEngine) {
			super(pEngine, Name + "(+"+Field.class.getCanonicalName()+",~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try { UClass.setFieldValue((Field)pParams[0], null, pParams[1]); return pParams[1]; }
			catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(1));
		}
	}
	
	static public class Inst_SetJavaObjectFieldValue extends Inst_AbstractSimple {
		static public final String Name = "setJavaObjectFieldValue";
		
		Inst_SetJavaObjectFieldValue(Engine pEngine) {
			super(pEngine, Name + "(+~,+$,~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try { UClass.setObjectFieldValue(pParams[0], (String)pParams[1], pParams[2]); return pParams[2]; }
			catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			if(pExpr.getParam(1) instanceof String) {
				TypeRef  TR = pCProduct.getReturnTypeRefOf(pExpr.getParam(0));
				Type     T  = pCProduct.getTypeAtCompileTime(TR, false);
				Class<?> C  = T.getDataClass();
				if(C != null) {
					Field F = UClass.getField(C, (String)pExpr.getParam(1), false);
					if(F == null) return null;
					return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(F.getType()).getTypeRef();
				}
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	static public class Inst_SetJavaClassFieldValue extends Inst_AbstractSimple {
		static public final String Name = "setJavaClassFieldValue";
		
		Inst_SetJavaClassFieldValue(Engine pEngine) {
			super(pEngine, Name + "(+@,+$,~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try { UClass.setStaticFieldValue((Class<?>)pParams[0], (String)pParams[1], pParams[2]); return pParams[2]; }
			catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return pCProduct.getReturnTypeRefOf(pExpr.getParam(2));
		}
	}
	
	static public class Inst_GetJavaObjectFieldValueByField extends Inst_AbstractSimple {
		static public final String Name = "getJavaObjectFieldValueByField";
		
		Inst_GetJavaObjectFieldValueByField(Engine pEngine) {
			super(pEngine, Name + "(+"+Field.class.getCanonicalName()+",+~):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try {
				Field F =(Field)pParams[0];
				// Change to Native if need and can
				Object   O = pParams[1];
				Class<?> C = F.getDeclaringClass();
				if(!C.isInstance(O) && (O instanceof DObjectStandalone)) {
					if(C.isAssignableFrom(DObject.class)) O = ((DObjectStandalone)O).getAsDObject();
					else                                  O = ((DObjectStandalone)O).getAsNative();
				}
				
				return UClass.getFieldValue(F, O);
			} catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if((O instanceof Expression) && pCProduct.getEngine().isExpressionOf(Inst_GetJavaField.Name, (Expression)O)) {
				Object   TheO = ((Expression)O).getParam(0);
				Class<?> C    = (TheO == null)?Object.class:TheO.getClass();
				Object   N    = ((Expression)O).getParam(1);
				Object   S    = ((Expression)O).getParam(2);
				
				if((N instanceof String) && (S instanceof Boolean)) {
					Field F = UClass.getField((Class<?>)C, (String)N, Boolean.TRUE.equals(S));
					O = F;
				}
			}
			if(O instanceof Field) return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(((Field)O).getType()).getTypeRef();
			
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	static public class Inst_GetJavaClassFieldValueByField extends Inst_AbstractSimple {
		static public final String Name = "getJavaClassFieldValueByField";
		
		Inst_GetJavaClassFieldValueByField(Engine pEngine) {
			super(pEngine, Name + "(+"+Field.class.getCanonicalName()+"):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try { return UClass.getFieldValue((Field)pParams[0], null); }
			catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**@inherDoc()*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if((O instanceof Expression) && pCProduct.getEngine().isExpressionOf(Inst_GetJavaField.Name, (Expression)O)) {
				Object C = ((Expression)O).getParam(0);
				Object N = ((Expression)O).getParam(1);
				Object S = ((Expression)O).getParam(2);
				
				if((C instanceof Class<?>) && (N instanceof String) && (S instanceof Boolean)) {
					Field F = UClass.getField((Class<?>)C, (String)N, Boolean.TRUE.equals(S));
					O = F;
				}
			}
			if(O instanceof Field) return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(((Field)O).getType()).getTypeRef();
			
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	
	static public class Inst_GetJavaObjectFieldValue extends Inst_AbstractSimple {
		static public final String Name = "getJavaObjectFieldValue";
		
		Inst_GetJavaObjectFieldValue(Engine pEngine) {
			super(pEngine, Name + "(+~,+$):~");
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try { return UClass.getObjectFieldValue(pParams[0], (String)pParams[1]); }
			catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(!(O instanceof Class<?>)) {
				O = pCProduct.getReturnTypeRefOf(O);
				O = (O == null)?Object.class:((TypeRef)O).getDataClass(pCProduct.getEngine());
			}
			
			if((O instanceof Class<?>) && (pExpr.getParam(1) instanceof String)) {
				Class<?> C = (Class<?>)O;
				Field F = UClass.getField(C, (String)pExpr.getParam(1), false);
				if(F == null) return null;
				return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(F.getType()).getTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	static public class Inst_GetJavaClassFieldValue extends Inst_AbstractSimple {
		static public final String Name = "getJavaClassFieldValue";
		
		Inst_GetJavaClassFieldValue(Engine pEngine) {
			super(pEngine, Name + "(+@,+$):~");
		}
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			try { return UClass.getStaticFieldValue((Class<?>)pParams[0], (String)pParams[1]); }
			catch(Throwable T) { return new SpecialResult.ResultError(T); }
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(!(O instanceof Class<?>)) {
				O = pCProduct.getReturnTypeRefOf(O);
				O = (O == null)?Object.class:((TypeRef)O).getDataClass(pCProduct.getEngine());
			}
			
			if((O instanceof Class<?>) && (pExpr.getParam(1) instanceof String)) {
				Class<?> C = (Class<?>)O;
				Field F = UClass.getField(C, (String)pExpr.getParam(1), true);
				if(F == null) return null;
				return pCProduct.getEngine().getTypeManager().getTypeOfTheInstanceOf(F.getType()).getTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
}