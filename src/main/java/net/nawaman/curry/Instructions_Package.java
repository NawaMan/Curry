package net.nawaman.curry;

import net.nawaman.curry.compiler.CompileProduct;

public class Instructions_Package {

	static public class Inst_GetUnitManager extends Inst_AbstractSimple {
		@SuppressWarnings("hiding")
		static public final String Name = "getUnitManager";
		
		Inst_GetUnitManager(Engine pEngine) {
			super(pEngine, "=" + Name + "():"+ MUnit.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return this.Engine.getUnitManager();
		}
	}
	static public class Inst_GetCurrentPackage extends Inst_AbstractSimple {
		@SuppressWarnings("hiding")
		static public final String Name = "getCurrentPackage";
		
		Inst_GetCurrentPackage(Engine pEngine) {
			super(pEngine, "=" + Name + "():"+ Package.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getStackOwnerAsPackage();
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			String PName = pCProduct.getOwnerPackageName();
			if(PName != null) {
				Engine E = pCProduct.getEngine();
				TKPackage TKP = (TKPackage)E.getTypeManager().getTypeKind(TKPackage.KindName);
				if(TKP != null) return TKP.getTypeSpec(PName).getTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	static public class Inst_GetPackage extends Inst_AbstractSimple {
		@SuppressWarnings("hiding")
		static public final String Name = "getPackage";
		
		Inst_GetPackage(Engine pEngine) {
			super(pEngine, "=" + Name + "(+$):"+ Package.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return pContext.getEngine().getUnitManager().getPackage((String)pParams[0]);
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O instanceof String) {
				String PName = (String)O;
				if(PName != null) {
					Engine E = pCProduct.getEngine();
					TKPackage TKP = (TKPackage)E.getTypeManager().getTypeKind(TKPackage.KindName);
					if(TKP != null) return TKP.getTypeSpec(PName).getTypeRef();
				}
			}
			
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	static public class Inst_EnsurePackage extends Inst_AbstractSimple {
		@SuppressWarnings("hiding")
		static public final String Name = "ensurePackage";
		
		Inst_EnsurePackage(Engine pEngine) {
			super(pEngine, "=" + Name + "(+$):"+ Package.class.getCanonicalName());
		}
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			Package P = pContext.getEngine().getUnitManager().getPackage((String)pParams[0]);
			if(P == null) throw new AssertionError(String.format("Package %s is not accessible.", (String)pParams[0]));
			
			return P;
		}
		/**{@inherDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			Object O = pExpr.getParam(0);
			if(O instanceof String) {
				String PName = (String)O;
				if(PName != null) {
					Engine E = pCProduct.getEngine();
					TKPackage TKP = (TKPackage)E.getTypeManager().getTypeKind(TKPackage.KindName);
					if(TKP != null) return TKP.getTypeSpec(PName).getTypeRef();
				}
			}
			
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
}
