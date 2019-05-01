package net.nawaman.curry;

import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.util.UArray;

/** Initializer of a StackOwner object */
public class Instructions_Initializer {
	
	static public class Inst_InvokeThisInitializer_ByParams extends Inst_AbstractInvokeInitializer {
		@SuppressWarnings("hiding") static public final String Name = "this_initialize_ByParams";
		
		Inst_InvokeThisInitializer_ByParams(Engine pEngine) {
			super(pEngine, Name + "(~...):^");
		}
		@Override protected Object getSearchKey(Context pContext, Expression pExpr, Object[] pParams) {
			return null;
		}
		@Override protected Object[] getParams(Context pContext, Expression pExpr, Object[] pParams) {
			return UArray.getObjectArray(pParams[0]);
		}
	}
	
	static abstract public class Inst_AbstractInvokeInitializerBy extends Inst_AbstractInvokeInitializer {
		protected Inst_AbstractInvokeInitializerBy(Engine pEngine, String pName, String pSearch) {
			super(pEngine, pName + "("+pSearch+",~...):^");
		}
		@Override protected Object getSearchKey(Context pContext, Expression pExpr, Object[] pParams) {
			return pParams[0];
		}
		@Override protected Object[] getParams(Context pContext, Expression pExpr, Object[] pParams) {
			return UArray.getObjectArray(pParams[1]);
		}
	}
	
	static public class Inst_InvokeThisInitializer_ByPTRefs extends Inst_AbstractInvokeInitializerBy {
		@SuppressWarnings("hiding") static public final String Name = "this_initialize_ByTRefs";
		
		protected Inst_InvokeThisInitializer_ByPTRefs(Engine pEngine) {
			super(pEngine, Name, TypeRef.class.getCanonicalName()+"[]");
		}
	}
	static public class Inst_InvokeThisInitializer_ByInterface extends Inst_AbstractInvokeInitializerBy {
		@SuppressWarnings("hiding") static public final String Name = "this_initialize_ByInterface";
		
		protected Inst_InvokeThisInitializer_ByInterface(Engine pEngine) {
			super(pEngine, Name, "+" + ExecInterface.class.getCanonicalName());
		}
	}
	
	// Super -----------------------------------------------------------------------------------------------------------
	
	static public class Inst_InvokeSuperInitializer_ByParams extends Inst_AbstractInvokeInitializer {
		@SuppressWarnings("hiding") static public final String Name = "super_initialize_ByParams";
		
		protected Inst_InvokeSuperInitializer_ByParams(Engine pEngine) {
			super(pEngine, Name + "(~...):^");
		}
		/**{@inherDoc}*/ @Override
		protected Object getSearchKey(Context pContext, Expression pExpr, Object[] pParams) {
			return null;
		}
		/**{@inherDoc}*/ @Override
		protected Object[] getParams(Context pContext, Expression pExpr, Object[] pParams) {
			return UArray.getObjectArray(pParams[0]);
		}
		/**{@inherDoc}*/ @Override
		protected Type getAsType(Context pContext, Expression pExpr, Object[] pParams) {
			Type    T  = pContext.getStackOwnerAsType();
			TypeRef TR = T.getSuperRef();
			if((TR == null) || TKJava.TAny.getTypeRef().equals(TR)) this.reportNoSuperError(T, pContext);

			try { return pContext.getEngine().getTypeManager().getTypeFromRefNoCheck(pContext, TR); }
			catch(Exception E) { return null; }
		}
	}
	
	static abstract public class Inst_AbstractInvokeSuperInitializerBy extends Inst_AbstractInvokeInitializer {
		protected Inst_AbstractInvokeSuperInitializerBy(Engine pEngine, String pName, String pSearch) {
			super(pEngine, pName + "("+pSearch+",~...):^");
		}
		/**{@inherDoc}*/ @Override
		protected Object getSearchKey(Context pContext, Expression pExpr, Object[] pParams) {
			return pParams[0];
		}
		/**{@inherDoc}*/ @Override
		protected Object[] getParams(Context pContext, Expression pExpr, Object[] pParams) {
			return UArray.getObjectArray(pParams[1]);
		}
		/**{@inherDoc}*/ @Override
		protected Type getAsType(Context pContext, Expression pExpr, Object[] pParams) {
			Type    T  = pContext.getStackOwnerAsType();
			TypeRef TR = T.getSuperRef();
			if((TR == null) || TKJava.TAny.getTypeRef().equals(TR)) this.reportNoSuperError(T, pContext);
			
			try { return pContext.getEngine().getTypeManager().getTypeFromRefNoCheck(pContext, TR); }
			catch(Exception E) { return null; }
		}
	}
	
	static public class Inst_InvokeSuperInitializer_ByPTRefs extends Inst_AbstractInvokeSuperInitializerBy {
		@SuppressWarnings("hiding") static public final String Name = "super_initialize_ByTRefs";
		
		protected Inst_InvokeSuperInitializer_ByPTRefs(Engine pEngine) {
			super(pEngine, Name, TypeRef.class.getCanonicalName()+"[]");
		}
	}
	static public class Inst_InvokeSuperInitializer_ByInterface extends Inst_AbstractInvokeSuperInitializerBy {
		@SuppressWarnings("hiding") static public final String Name = "super_initialize_ByInterface";
		
		protected Inst_InvokeSuperInitializer_ByInterface(Engine pEngine) {
			super(pEngine, Name, "+" + ExecInterface.class.getCanonicalName());
		}
	}
	
	// Abstract --------------------------------------------------------------------------------------------------------
	
	static abstract public class Inst_AbstractInvokeInitializer extends Inst_AbstractComplex {
		protected Inst_AbstractInvokeInitializer(Engine pEngine, String pISpecStr) { super(pEngine, pISpecStr); }
		
		protected Object getNewInstance(Context pContext, Expression pExpr, Object[] pParams) {
			Object NewInstance = pContext.getStackOwner();
			// Ensure that the caller is an object
			if((NewInstance == pContext.getStackOwnerAsType()) || (NewInstance == pContext.getStackOwnerAsPackage()))
				throw new CurryError("Object initialization Error: Only object is allowed to call initializer ("
						+ pContext.getEngine().toString(pContext, NewInstance)+").", pContext);
			
			return NewInstance;
		}
		protected Type getAsType(Context pContext, Expression pExpr, Object[] pParams) {
			return pContext.getStackOwnerAsType();
		}
		/** Throw an error that the current type has no super */
		protected void reportNoSuperError(Type T, Context pContext) {
			throw new CurryError("The type `"+T.toString()+"` is not a Type.TypeWithSuper so curry is unable to get " +
					"its super type for the invocation of '"+this.getSignature()+"'.", pContext);
		}
		
		abstract protected Object   getSearchKey(Context pContext, Expression pExpr, Object[] pParams);
		abstract protected Object[] getParams(   Context pContext, Expression pExpr, Object[] pParams);
		
		/**{@inherDoc}*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			if(!pContext.isConstructor())
				throw new CurryError("Object Initialization Error: Initializer can only be called during the "
						+ "initialization process of an object.", pContext);

			Type AsType = this.getAsType(pContext, pExpr, pParams);
			if(AsType instanceof TKJava.TJava) return null;
			
			Object NewInstance = this.getNewInstance(pContext, pExpr, pParams);
			if(!(NewInstance instanceof StackOwner)) return null;
			
			Object   SearchKey = this.getSearchKey(pContext, pExpr, pParams);
			Object[] IParams   = this.getParams(   pContext, pExpr, pParams);
			
			AsType.getTypeKind().initializeNewTypeInstance(pContext, pExpr, AsType, (StackOwner)NewInstance, SearchKey, IParams);
			return null;
		}
		
		/**{@inherDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			if(!super.manipulateCompileContextFinish(pExpr, pCProduct)) return false;

			if(!pCProduct.isConstructor()) {
				pCProduct.reportError(
					"Initializer can only be called with in a constructor <Inst_AbstractInvokeInitializer:169>",
					null, pCProduct.getPosition(pExpr));
				return false;
			}
			
			if(!pCProduct.notifyInvokeSuperConstructor(pExpr)) return false;
			return true;
		}
	}
}