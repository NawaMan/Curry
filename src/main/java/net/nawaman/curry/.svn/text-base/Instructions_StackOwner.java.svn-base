package net.nawaman.curry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;

import net.nawaman.curry.StackOwner.OperationSearchKind;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.TLType.TypeTypeRef;
import net.nawaman.util.UArray;
import net.nawaman.util.UClass;
import net.nawaman.util.UObject;

public class Instructions_StackOwner {
	
	/** Kinds of the processor for AsType (used only in finding Return Type) */
	static public enum AsTypeProcessKind {
		NONE, ASTYPE, THIS, SUPER, TYPE, PACKAGE, OTHER;
					
		/** Checks if NONE which means the type can be found in the object */
		public boolean isNone()    { return this == NONE;    }
		/** Checks if ASTYPE which means the type is given as a parameter */
		public boolean isAsType()  { return this == ASTYPE;  }
		/** Checks if THIS which means the type can be find in the stack info */
		public boolean isThis()    { return this == THIS;    }
		/** Checks if SUPER which means the type can be find in the stack info */
		public boolean isSuper()   { return this == SUPER;   }	
		/** Checks if TYPE which means the type can be find in the stack info */
		public boolean isType()    { return this == TYPE;    }
		/** Checks if PACKAGE which means the type can be extracted from the current package */
		public boolean isPackage() { return this == PACKAGE; }
		/** Checks if OTHER which means the method of find the type will be inherit so at this point do nothing */
		public boolean isOther()   { return this == OTHER;   }
	}
	
	/** Super of all StackOwner element access */
	static abstract public class Inst_StackOwner extends Inst_AbstractComplex {
	
		protected Inst_StackOwner(Engine pEngine, String pISpecStr)       { super(pEngine, pISpecStr); }
		protected Inst_StackOwner(Engine pEngine, InstructionSpec pISpec) { super(pEngine, pISpec);    }
		
		/** Checks if the invocation blinds the caller */
		protected boolean isBlindCaller() { return false; }
		
		/** Returns what kind of AsType Process kind this is */
		abstract protected AsTypeProcessKind getAsTypeProcessKind();
		
		/** Returns what kind of AsType Process kind this is - this method ensures that the value is not null */
		final protected AsTypeProcessKind getTheAsTypeProcessKind() {
			AsTypeProcessKind ATPK = getAsTypeProcessKind();
			if(ATPK == null) ATPK = AsTypeProcessKind.OTHER;
			return ATPK;
		}
		
		/** Returns the index of the object in the parameter array */
		protected int getObjectIndex()    { return -1; }
		/** Returns the index of the AsType in the parameter array */
		protected int getRunAsTypeIndex() { return -1; }
		
		/** Returns the object for the invocation */
		protected Object getObject(Context pContext, Expression pExpr, Object[] pParams) {
			AsTypeProcessKind K = this.getTheAsTypeProcessKind();
			switch(K) {
				case THIS:
				case SUPER:   return this.getStackOwnerAsObject_NoNull(pContext);
				case TYPE:    return pContext.getStackOwnerAsType();
				case PACKAGE: return pContext.getStackOwnerAsPackage();
				default: {
					int Index = this.getObjectIndex();
					if(Index == -1) return null;
					if((pParams == null) || (Index >= pParams.length)) return null;
					return pParams[Index];
				}
			}
		}
		/** Returns the AsType for the invocation */
		protected Type getRunAsType(Context pContext, Expression pExpr, Object[] pParams) {			
			// Process the AsType
			AsTypeProcessKind K = this.getTheAsTypeProcessKind();
			Type AsType = null;
			switch(K) {
				case THIS:
				case TYPE: return pContext.getStackOwnerAsType();
				
				case NONE: 
				case PACKAGE: return null;
				
				case SUPER: {
					Type T = pContext.getStackOwnerAsType();
					if (T == null)
						T = pContext.getEngine().getTypeManager().getTypeOfNoCheck(pContext, pContext.getStackOwner());
					
					if (!TypeInfo.isTypeHasSuper(pContext.getEngine(), T))
						this.reportNoSuperError(T, pContext, pExpr, pParams);
					AsType = pContext.getEngine().getTypeManager().getTypeFromRefNoCheck(pContext, T.getSuperRef());
					break;
				}
				
				default: {
					int AsTypeIndex = this.getRunAsTypeIndex();
					if((AsTypeIndex == -1) || ((AsType = (Type)pParams[AsTypeIndex]) == null)) return null;
				}
			}
			
			Object TheObject = this.getObject(pContext, pExpr, pParams);
			// Ensure that the AsType can take the object as an instance
			if(!AsType.canBeAssignedBy(TheObject)) this.reportNoMatchAsTypeError(AsType, TheObject, pContext, pExpr, pParams);
			return AsType;
		}
		
		// Attribute specific ----------------------------------------------------------------------
		
		protected int getAttrNameIndex() {
			return -1;
		}
		protected String getAttrName(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getAttrNameIndex();
			if(Index == -1) return null;
			return (String)pParams[Index];
		}
		
		// Operation specific ----------------------------------------------------------------------
		
		protected int getParamIndex() {
			return -1;
		}
		protected Object[] getRawParams(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getParamIndex();
			if(Index == -1) return null;
			return UArray.getObjectArray(pParams[Index]);
		}
		
		// Report error ------------------------------------------------------------------------------------------------
		
		/** Throw an error that the current type has no super */
		abstract protected void reportNoSuperError(Type T, Context pContext, Expression pExpr, Object[] pParams);
		
		/** Throw an error that the current type has no super */
		abstract protected void reportNoMatchAsTypeError(Type AsType, Object O, Context pContext, Expression pExpr,
				Object[] pParams);
		
		// For ReturnType Checking -------------------------------------------------------------------------------------
		
		/** Returns the TypeRef of the type to be searched for the operation so that the return type can be determined */
		protected TypeRef getAsTypeRef(Expression pExpr, CompileProduct pCProduct) {		
			// Process the AsType
			AsTypeProcessKind K = this.getTheAsTypeProcessKind();
			switch(K) {
				case THIS: if(!pCProduct.isOwnerObject()) return null;
				
				case TYPE: return pCProduct.getOwnerTypeRef();
				
				case PACKAGE: return null;
				
				case ASTYPE: {
					TypeRef AsType = (TypeRef)pCProduct.getReturnTypeRefOf(pExpr.getParam(this.getRunAsTypeIndex()));
					if(!(AsType instanceof TypeTypeRef)) return null;
					AsType = ((TypeTypeRef)AsType).getTheRef();
					return AsType;
				}
				
				case SUPER: {
					if(!pCProduct.isOwnerObject()) return null;
					
					TypeRef TR = pCProduct.getOwnerTypeRef();
					Type    T  = pCProduct.getTypeAtCompileTime(TR);
					if((T == null) || !TR.isLoaded()) return null;

					if(!TypeInfo.isTypeHasSuper(Engine, T)) return null;
					return T.getTypeRef();
				}
				
				default: {
					// Find the type from the object
					// If the object is a type, return that type
					int Index = this.getObjectIndex();
					if(Index == -1) return null;
					
					Object O = pExpr.getParam(Index);
					TypeRef TR = pCProduct.getReturnTypeRefOf(O);
					if(!(TR instanceof TypeTypeRef)) return TR;
					
					Type T = null;
					try { T = pCProduct.getTypeAtCompileTime(TR); }
					catch (Exception E) {}
					if(T == null) return TR;

					return  ((TypeTypeRef)TR).getTheRef();
				}
			}
		}
		/** Checks if this invocation is static */
		protected boolean isStatic(Expression pExpr, CompileProduct pCProduct) {
			AsTypeProcessKind K = this.getAsTypeProcessKind();
			if(K == null) K = AsTypeProcessKind.OTHER;
			
			switch(K) {
				case TYPE: return true;
				
				case NONE:
				case ASTYPE:{
					// Find the type from the object
					int Index = this.getObjectIndex();
					if(Index != -1) {
						Object O = pExpr.getParam(Index);
						TypeRef TR = pCProduct.getReturnTypeRefOf(O);
						if(TR instanceof TypeTypeRef) return true;
					}
				}
			}
			return false;
		}
	}
	
	// Any StackOwner ******************************************************************************

	// Execute a operation -------------------------------------------------------------------------
	
	// Directly
	static public class Inst_Invoke extends Instructions_StackOwner.Inst_Abst_InvokeDirect {
		@SuppressWarnings("hiding") static public final String Name = "invoke";
		
		protected Inst_Invoke(Engine pEngine, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine, Name + "(~,+"+ExecSignature.class.getCanonicalName()+",~...)", pIsBlindCaller, pIsAdjusted);
		}
		
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getSignatureIndex() { return 1; }
		@Override protected int getParamIndex()     { return 2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	// Search by Parameters
	static public class Inst_Invoke_ByParams extends Instructions_StackOwner.Inst_Abst_InvokeByParams {
		@SuppressWarnings("hiding") static public final String Name = "invokeByParams";
		
		protected Inst_Invoke_ByParams(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name + "(~,+$,~...)", pIsBlindCaller);
		}
		
		@Override protected int getObjectIndex() { return 0; }
		@Override protected int getONameIndex()  { return 1; }
		@Override protected int getParamIndex()  { return 2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	// Search by PTRefs
	static public class Inst_Invoke_ByPTRefs extends Instructions_StackOwner.Inst_Abst_InvokeByPTRefs {
		@SuppressWarnings("hiding") static public final String Name = "invokeByPTRefs";
		
		protected Inst_Invoke_ByPTRefs(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name + "(~,+$,+"+TypeRef.class.getCanonicalName()+"[],~...)", pIsBlindCaller);
		}
		
		@Override protected int getObjectIndex() { return 0; }
		@Override protected int getONameIndex()  { return 1; }
		@Override protected int getPTRefsIndex() { return 2; }
		@Override protected int getParamIndex()  { return 3; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	// Search by ParamInterfaces
	static public class Inst_Invoke_ByInterface extends Instructions_StackOwner.Inst_Abst_InvokeByInterface {
		@SuppressWarnings("hiding") static public final String Name = "invokeByInterface";
		
		protected Inst_Invoke_ByInterface(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name + "(~,+$,+"+ExecInterface.class.getCanonicalName()+",~...)", pIsBlindCaller);
		}
		
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getONameIndex()     { return 1; }
		@Override protected int getInterfaceIndex() { return 2; }
		@Override protected int getParamIndex()     { return 3; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	
	// Access Package Variable ---------------------------------------------------------------------
	
	static public class Inst_SetAttrValue extends Instructions_StackOwner.Inst_Abst_SetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "setAttrValue";
		
		protected Inst_SetAttrValue(Engine pEngine) {
			super(pEngine, Name + "(~,+$,~)");
		}
		@Override protected int getObjectIndex()   { return 0; }
		@Override protected int getAttrNameIndex() { return 1; }
		@Override protected int getDataIndex()     { return 2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	static public class Inst_GetAttrValue extends Instructions_StackOwner.Inst_Abst_GetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "getAttrValue";
		
		protected Inst_GetAttrValue(Engine pEngine) {
			super(pEngine, Name + "(~,+$)");
		}
		@Override protected int getObjectIndex()   { return 0; }
		@Override protected int getAttrNameIndex() { return 1; }
		
		/**{@inheritDoc}*/ @Override
		protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	static public class Inst_GetAttrType extends Instructions_StackOwner.Inst_Abst_GetAttrType {
		@SuppressWarnings("hiding") static public final String Name = "getAttrType";
		
		protected Inst_GetAttrType(Engine pEngine) {
			super(pEngine, Name + "(~,+$)");
		}
		@Override protected int getObjectIndex()   { return 0; }
		@Override protected int getAttrNameIndex() { return 1; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	static public class Inst_IsAttrReadable extends Instructions_StackOwner.Inst_Abst_IsAttrReadable {
		@SuppressWarnings("hiding") static public final String Name = "isAttrReadable";
		
		protected Inst_IsAttrReadable(Engine pEngine) {
			super(pEngine, Name + "(~,+$)");
		}
		@Override protected int getObjectIndex()   { return  0; }
		@Override protected int getAttrNameIndex() { return  1; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	static public class Inst_IsAttrWritable extends Instructions_StackOwner.Inst_Abst_IsAttrWritable {
		@SuppressWarnings("hiding") static public final String Name = "isAttrWritable";
		
		protected Inst_IsAttrWritable(Engine pEngine) {
			super(pEngine, Name + "(~,+$)");
		}
		@Override protected int getObjectIndex()   { return  0; }
		@Override protected int getAttrNameIndex() { return  1; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	static public class Inst_IsAttrNoTypeCheck extends Instructions_StackOwner.Inst_Abst_IsAttrNoTypeCheck {
		@SuppressWarnings("hiding") static public final String Name = "isAttrNoTypeCheck";
		
		protected Inst_IsAttrNoTypeCheck(Engine pEngine) {
			super(pEngine, Name + "(~,+$)");
		}
		@Override protected int getObjectIndex()   { return  0; }
		@Override protected int getAttrNameIndex() { return  1; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	static public class Inst_ConfigAttr extends Instructions_StackOwner.Inst_Abst_ConfigAttr {
		@SuppressWarnings("hiding") static public final String Name = "configAttr";
		
		protected Inst_ConfigAttr(Engine pEngine) {
			super(pEngine, Name + "(~,+$,+$,~...)");
		}
		@Override protected int getObjectIndex()   { return  0; }
		@Override protected int getAttrNameIndex() { return  1; }
		@Override protected int getCNameIndex()    { return  2; }
		@Override protected int getCParamsIndex()  { return  3; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	static public class Inst_GetAttrMoreInfo extends Instructions_StackOwner.Inst_Abst_GetAttrMoreInfo {
		@SuppressWarnings("hiding") static public final String Name = "getAttrMoreInfo";
		
		protected Inst_GetAttrMoreInfo(Engine pEngine) {
			super(pEngine, Name + "(~,+$,+$)");
		}
		@Override protected int getObjectIndex()   { return  0; }
		@Override protected int getAttrNameIndex() { return  1; }
		@Override protected int getMINameIndex()   { return  2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.NONE;
		}
	}
	
	// Any StackOwner AsType ***********************************************************************
	
	// Execute a operation -------------------------------------------------------------------------
	
	// Directly
	static public class Inst_InvokeAsType extends Instructions_StackOwner.Inst_Abst_InvokeDirect {
		@SuppressWarnings("hiding") static public final String Name = "invokeAsType";
		
		protected Inst_InvokeAsType(Engine pEngine, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine, Name + "(~,!,+"+ExecSignature.class.getCanonicalName()+",~...)", pIsBlindCaller, pIsAdjusted);
		}
		
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getSignatureIndex() { return 2; }
		@Override protected int getParamIndex()     { return 3; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	// Search by Parameters
	static public class Inst_InvokeAsType_ByParams extends Instructions_StackOwner.Inst_Abst_InvokeByParams {
		@SuppressWarnings("hiding") static public final String Name = "invokeAsTypeByParams";
		
		protected Inst_InvokeAsType_ByParams(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name + "(~,!,+$,~...)", pIsBlindCaller);
		}
		
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getONameIndex()     { return 2; }
		@Override protected int getParamIndex()     { return 3; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	// Search by PTRefs
	static public class Inst_InvokeAsType_ByPTRefs extends Instructions_StackOwner.Inst_Abst_InvokeByPTRefs {
		@SuppressWarnings("hiding") static public final String Name = "invokeAsTypeByPTRefs";
		
		protected Inst_InvokeAsType_ByPTRefs(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name + "(~,!,+$,+"+TypeRef.class.getCanonicalName()+"[],~...)", pIsBlindCaller);
		}
		
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getONameIndex()     { return 2; }
		@Override protected int getPTRefsIndex()    { return 3; }
		@Override protected int getParamIndex()     { return 4; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	// Search by ParamInterfaces
	static public class Inst_InvokeAsType_ByInterface extends Instructions_StackOwner.Inst_Abst_InvokeByInterface {
		@SuppressWarnings("hiding") static public final String Name = "invokeAsTypeByInterface";
		
		protected Inst_InvokeAsType_ByInterface(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name + "(~,!,+$,+"+ExecInterface.class.getCanonicalName()+",~...)", pIsBlindCaller);
		}
				
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getONameIndex()     { return 2; }
		@Override protected int getInterfaceIndex() { return 3; }
		@Override protected int getParamIndex()     { return 4; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	
	// Access Package Variable ---------------------------------------------------------------------
	
	static public class Inst_SetAttrDataAsType extends Instructions_StackOwner.Inst_Abst_SetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "setAttrValueAsType";
		
		protected Inst_SetAttrDataAsType(Engine pEngine) {
			super(pEngine, Name + "(~,!,+$,~)");
		}
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getAttrNameIndex()  { return 2; }
		@Override protected int getDataIndex()      { return 3; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	static public class Inst_GetAttrValueAsType extends Instructions_StackOwner.Inst_Abst_GetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "getAttrValueAsType";
		
		protected Inst_GetAttrValueAsType(Engine pEngine) {
			super(pEngine, Name + "(~,!,+$)");
		}
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getAttrNameIndex()  { return 2; }
		
		/**{@inheritDoc}*/ @Override
		protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	static public class Inst_GetAttrTypeAsType extends Instructions_StackOwner.Inst_Abst_GetAttrType {
		@SuppressWarnings("hiding") static public final String Name = "getAttrTypeAsType";
		
		protected Inst_GetAttrTypeAsType(Engine pEngine) {
			super(pEngine, Name + "(~,!,+$)");
		}
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getAttrNameIndex()  { return 2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	static public class Inst_IsAttrReadableAsType extends Instructions_StackOwner.Inst_Abst_IsAttrReadable {
		@SuppressWarnings("hiding") static public final String Name = "isAttrReadableAsType";
		
		protected Inst_IsAttrReadableAsType(Engine pEngine) {
			super(pEngine, Name + "(~,!,+$)");
		}
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getAttrNameIndex()  { return 2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	static public class Inst_IsAttrWritableAsType extends Instructions_StackOwner.Inst_Abst_IsAttrWritable {
		@SuppressWarnings("hiding") static public final String Name = "isAttrWritableAsType";
		
		protected Inst_IsAttrWritableAsType(Engine pEngine) {
			super(pEngine, Name + "(~,!,+$)");
		}
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getAttrNameIndex()  { return 2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	static public class Inst_IsAttrNoTypeCheckAsType extends Instructions_StackOwner.Inst_Abst_IsAttrNoTypeCheck {
		@SuppressWarnings("hiding") static public final String Name = "isAttrNoTypeCheckAsType";
		
		protected Inst_IsAttrNoTypeCheckAsType(Engine pEngine) {
			super(pEngine, Name + "(~,!,+$)");
		}
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getAttrNameIndex()  { return 2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	static public class Inst_ConfigAttrAsType extends Instructions_StackOwner.Inst_Abst_ConfigAttr {
		@SuppressWarnings("hiding") static public final String Name = "configAttrAsType";
		
		protected Inst_ConfigAttrAsType(Engine pEngine) {
			super(pEngine, Name + "(~,!,+$,+$,~...)");
		}
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getAttrNameIndex()  { return 2; }
		@Override protected int getCNameIndex()     { return 3; }
		@Override protected int getCParamsIndex()   { return 4; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}
	static public class Inst_GetAttrMoreInfoAsType extends Instructions_StackOwner.Inst_Abst_GetAttrMoreInfo {
		@SuppressWarnings("hiding") static public final String Name = "getAttrMoreInfoAsType";
		
		protected Inst_GetAttrMoreInfoAsType(Engine pEngine) {
			super(pEngine, Name + "(~,!,+$,+$)");
		}
		@Override protected int getObjectIndex()    { return 0; }
		@Override protected int getRunAsTypeIndex() { return 1; }
		@Override protected int getAttrNameIndex()  { return 2; }
		@Override protected int getMINameIndex()    { return 3; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.ASTYPE;
		}
	}

	// this StackOwner *****************************************************************************

	// Execute a operation -------------------------------------------------------------------------
	
	// Directly
	static public class Inst_thisInvoke extends Inst_typeInvoke {
		@SuppressWarnings("hiding") static public final String Name = "this_invoke";
		
		Inst_thisInvoke(Engine pEngine, String pName, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine, pName, pIsAdjusted, pIsBlindCaller);
		}
		protected Inst_thisInvoke(Engine pEngine, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine, Name, pIsAdjusted, pIsBlindCaller);
		}
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	// Search by Parameters
	static public class Inst_thisInvoke_ByParams extends Inst_typeInvoke_ByParams {
		@SuppressWarnings("hiding") static public final String Name = "this_invokeByParams";
		
		Inst_thisInvoke_ByParams(Engine pEngine, String pName, boolean pIsBlindCaller) {
			super(pEngine, pName, pIsBlindCaller);
		}
		protected Inst_thisInvoke_ByParams(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name, pIsBlindCaller);
		}
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	// Search by PTRefs
	static public class Inst_thisInvoke_ByPTRefs extends Inst_typeInvoke_ByPTRefs {
		@SuppressWarnings("hiding") static public final String Name = "this_invokeByPTRefs";
		protected Inst_thisInvoke_ByPTRefs(Engine pEngine, String pName, boolean pIsBlindCaller) {
			super(pEngine, pName, pIsBlindCaller);
		}
		protected Inst_thisInvoke_ByPTRefs(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name, pIsBlindCaller);
		}
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	// Search by ParamInterface
	static public class Inst_thisInvoke_ByInterface extends Inst_typeInvoke_ByInterface {
		@SuppressWarnings("hiding") static public final String Name = "this_invokeByInterface";
		
		Inst_thisInvoke_ByInterface(Engine pEngine, String pName, boolean pIsBlindCaller) {
			super(pEngine, pName, pIsBlindCaller);
		}
		protected Inst_thisInvoke_ByInterface(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name, pIsBlindCaller);
		}
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	
	// Access Package Variable ---------------------------------------------------------------------
	
	static public class Inst_thisSetAttrValue extends Inst_typeSetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "this_setAttrValue";
		
		protected Inst_thisSetAttrValue(Engine pEngine) {
			super(pEngine, Name);
		}
		@Override protected int getDataIndex() {
			return 1;
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	static public class Inst_thisGetAttrValue extends Inst_typeGetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "this_getAttrValue";
		
		protected Inst_thisGetAttrValue(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override
		protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	static public class Inst_thisGetAttrType extends Inst_typeGetAttrType {
		@SuppressWarnings("hiding") static public final String Name = "this_getAttrType";
		
		protected Inst_thisGetAttrType(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	static public class Inst_thisIsAttrReadable extends Inst_typeIsAttrReadable {
		@SuppressWarnings("hiding") static public final String Name = "this_isAttrReadable";
		
		protected Inst_thisIsAttrReadable(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	static public class Inst_thisIsAttrWritable extends Inst_typeIsAttrWritable {
		@SuppressWarnings("hiding") static public final String Name = "this_isAttrWritable";
		
		protected Inst_thisIsAttrWritable(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	static public class Inst_thisIsAttrNoTypeCheck extends Inst_typeIsAttrNoTypeCheck {
		@SuppressWarnings("hiding") static public final String Name = "this_isAttrNoTypeCheck";
		
		protected Inst_thisIsAttrNoTypeCheck(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	static public class Inst_thisConfigAttr extends Inst_typeConfigAttr {
		@SuppressWarnings("hiding") static public final String Name = "this_configAttr";
		
		protected Inst_thisConfigAttr(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}
	static public class Inst_thisGetAttrMoreInfo extends Inst_typeGetAttrMoreInfo {
		@SuppressWarnings("hiding") static public final String Name = "this_getAttrMoreInfo";
		
		protected Inst_thisGetAttrMoreInfo(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.THIS;
		}
	}

	// super StackOwner *****************************************************************************

	// Directly
	static public class Inst_superInvoke extends Inst_thisInvoke {
		@SuppressWarnings("hiding") static public final String Name = "super_invoke";
		
		protected Inst_superInvoke(Engine pEngine, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine, Name, pIsAdjusted, pIsBlindCaller);
		}
		/**{@inheritDoc}*/ @Override
		protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.SUPER;
		}
	}
	// Search by Parameters
	static public class Inst_superInvoke_ByParams extends Inst_thisInvoke_ByParams {
		@SuppressWarnings("hiding") static public final String Name = "super_invokeByParams";
		
		protected Inst_superInvoke_ByParams(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name, pIsBlindCaller);
		}
		/**{@inheritDoc}*/ @Override
		protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.SUPER;
		}
	}
	// Search by PTRefs
	static public class Inst_superInvoke_ByPTRefs extends Inst_thisInvoke_ByPTRefs {
		@SuppressWarnings("hiding") static public final String Name = "super_invokeByPTRefs";
		
		protected Inst_superInvoke_ByPTRefs(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name, pIsBlindCaller);
		}
		/**{@inheritDoc}*/ @Override
		protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.SUPER;
		}
	}
	// Search by ParamInterface
	static public class Inst_superInvoke_ByInterface extends Inst_thisInvoke_ByInterface {
		@SuppressWarnings("hiding") static public final String Name = "super_invokeByInterface";
		
		protected Inst_superInvoke_ByInterface(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name, pIsBlindCaller);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.SUPER;
		}
	}

	// This StackOwner *****************************************************************************

	// Execute a operation -------------------------------------------------------------------------
	
	// Directly
	static public class Inst_typeInvoke extends Inst_Abst_InvokeDirect {
		@SuppressWarnings("hiding") static public final String Name = "type_invoke";

		Inst_typeInvoke(Engine pEngine, String pName, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine, pName + "(+"+ExecSignature.class.getCanonicalName()+",~...)", pIsBlindCaller, pIsAdjusted);
		}
		protected Inst_typeInvoke(Engine pEngine, boolean pIsBlindCaller, boolean pIsAdjusted) {
			this(pEngine, Name, pIsBlindCaller, pIsAdjusted);
		}

		@Override protected int getSignatureIndex() { return 0; }
		@Override protected int getParamIndex()     { return 1; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	// Search by Parameters
	static public class Inst_typeInvoke_ByParams extends Inst_Abst_InvokeByParams {
		@SuppressWarnings("hiding") static public final String Name = "type_invokeByParams";
		
		Inst_typeInvoke_ByParams(Engine pEngine, String pName, boolean pIsBlindCaller) {
			super(pEngine, pName + "(+$,~...)", pIsBlindCaller);
		}
		protected Inst_typeInvoke_ByParams(Engine pEngine, boolean pIsBlindCaller) {
			this(pEngine, Name, pIsBlindCaller);
		}

		@Override protected int getONameIndex() { return 0; }
		@Override protected int getParamIndex() { return 1; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	// Search by PTRefs
	static public class Inst_typeInvoke_ByPTRefs extends Inst_Abst_InvokeByPTRefs {
		@SuppressWarnings("hiding") static public final String Name = "type_invokeByPTRefs";

		Inst_typeInvoke_ByPTRefs(Engine pEngine, String pName, boolean pIsBlindCaller) {
			super(pEngine, pName + "(+$,+"+TypeRef.class.getCanonicalName()+"[],~...)", pIsBlindCaller);
		}
		protected Inst_typeInvoke_ByPTRefs(Engine pEngine, boolean pIsBlindCaller) {
			this(pEngine, Name, pIsBlindCaller);
		}

		@Override protected int getONameIndex()  { return 0; }
		@Override protected int getPTRefsIndex() { return 1; }
		@Override protected int getParamIndex()  { return 2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	// Search by ParamInterfaces
	static public class Inst_typeInvoke_ByInterface extends Inst_Abst_InvokeByInterface {
		@SuppressWarnings("hiding") static public final String Name = "type_invokeByInterface";
		
		protected Inst_typeInvoke_ByInterface(Engine pEngine, String pName, boolean pIsBlindCaller) {
			super(pEngine, pName + "(+$,+"+ExecInterface.class.getCanonicalName()+",~...)", pIsBlindCaller);
		}
		protected Inst_typeInvoke_ByInterface(Engine pEngine, boolean pIsBlindCaller) {
			this(pEngine, Name, pIsBlindCaller);
		}

		@Override protected int getONameIndex()     { return 0; }
		@Override protected int getInterfaceIndex() { return 1; }
		@Override protected int getParamIndex()     { return 2; }
		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	
	// Current Type Variable -----------------------------------------------------------------------
	
	static public class Inst_typeSetAttrValue extends Inst_Abst_SetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "type_setAttrValue";
		
		Inst_typeSetAttrValue(Engine pEngine, String pName) {
			super(pEngine, pName + "(+$,~)");
		}
		protected Inst_typeSetAttrValue(Engine pEngine) {
			this(pEngine, Name);
		}
		@Override protected int getAttrNameIndex() {
			return 0;
		}
		@Override protected int getDataIndex() {
			return 1;
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	static public class Inst_typeGetAttrValue extends Inst_Abst_GetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "type_getAttrValue";
		
		Inst_typeGetAttrValue(Engine pEngine, String pName) {
			super(pEngine, pName + "(+$)");
		}
		protected Inst_typeGetAttrValue(Engine pEngine) {
			this(pEngine, Name);
		}
		@Override protected int getAttrNameIndex() {
			return 0;
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	static public class Inst_typeGetAttrType extends Inst_Abst_GetAttrType {
		@SuppressWarnings("hiding") static public final String Name = "type_getAttrType";
		
		Inst_typeGetAttrType(Engine pEngine, String pName) {
			super(pEngine, pName + "(+$)");
		}
		protected Inst_typeGetAttrType(Engine pEngine) {
			this(pEngine, Name);
		}
		@Override protected int getAttrNameIndex() {
			return 0;
		}		
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	static public class Inst_typeIsAttrReadable extends Inst_Abst_IsAttrReadable {
		@SuppressWarnings("hiding") static public final String Name = "type_isAttrReadable";
		
		Inst_typeIsAttrReadable(Engine pEngine, String pName) {
			super(pEngine, pName + "(+$)");
		}
		protected Inst_typeIsAttrReadable(Engine pEngine) {
			this(pEngine, Name);
		}
		@Override protected int getAttrNameIndex() {
			return 0;
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	static public class Inst_typeIsAttrWritable extends Inst_Abst_IsAttrWritable {
		@SuppressWarnings("hiding") static public final String Name = "type_isAttrWritable";
		
		Inst_typeIsAttrWritable(Engine pEngine, String pName) {
			super(pEngine, pName + "(+$)");
		}
		protected Inst_typeIsAttrWritable(Engine pEngine) {
			this(pEngine, Name);
		}
		@Override protected int getAttrNameIndex() {
			return 0;
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	static public class Inst_typeIsAttrNoTypeCheck extends Inst_Abst_IsAttrNoTypeCheck {
		@SuppressWarnings("hiding") static public final String Name = "type_isAttrNoTypeCheck";
		
		Inst_typeIsAttrNoTypeCheck(Engine pEngine, String pName) {
			super(pEngine, pName + "(+$)");
		}
		protected Inst_typeIsAttrNoTypeCheck(Engine pEngine) {
			this(pEngine, Name);
		}
		@Override protected int getAttrNameIndex() {
			return 0;
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	static public class Inst_typeConfigAttr extends Inst_Abst_ConfigAttr {
		@SuppressWarnings("hiding") static public final String Name = "type_configAttr";
		
		Inst_typeConfigAttr(Engine pEngine, String pName) {
			super(pEngine, pName + "(+$,+$,~...)");
		}
		protected Inst_typeConfigAttr(Engine pEngine) {
			this(pEngine, Name);
		}
		@Override protected int getAttrNameIndex() {
			return 0;
		}
		@Override protected int getCNameIndex() {
			return 1;
		}
		@Override protected int getCParamsIndex() {
			return 2;
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	static public class Inst_typeGetAttrMoreInfo extends Inst_Abst_GetAttrMoreInfo {
		@SuppressWarnings("hiding") static public final String Name = "type_getAttrMoreInfo";
		
		Inst_typeGetAttrMoreInfo(Engine pEngine, String pName) {
			super(pEngine, pName + "(+$,+$)");
		}
		protected Inst_typeGetAttrMoreInfo(Engine pEngine) {
			this(pEngine, Name);
		}
		@Override protected int getAttrNameIndex() {
			return 0;
		}
		@Override protected int getMINameIndex() {
			return 1;
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.TYPE;
		}
	}
	
	// Package StackOwner **************************************************************************

	// Execute a operation -------------------------------------------------------------------------
	
	// Directly
	static public class Inst_packageInvoke extends Inst_typeInvoke {
		@SuppressWarnings("hiding") static public final String Name = "package_invoke";

		protected Inst_packageInvoke(Engine pEngine, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine, Name, pIsAdjusted, pIsBlindCaller);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	// Search by Parameters
	static public class Inst_packageInvoke_ByParams extends Inst_typeInvoke_ByParams {
		@SuppressWarnings("hiding") static public final String Name = "package_invokeByParams";
		
		protected Inst_packageInvoke_ByParams(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name, pIsBlindCaller);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	// Search by PTRefs
	static public class Inst_packageInvoke_ByPTRefs extends Inst_typeInvoke_ByPTRefs {
		@SuppressWarnings("hiding") static public final String Name = "package_invokeByPTRefs";
		
		protected Inst_packageInvoke_ByPTRefs(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name, pIsBlindCaller);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	// Search by ParamInterfaces
	static public class Inst_packageInvoke_ByInterface extends Inst_typeInvoke_ByInterface {
		@SuppressWarnings("hiding") static public final String Name = "package_invokeByInterface";
		
		protected Inst_packageInvoke_ByInterface(Engine pEngine, boolean pIsBlindCaller) {
			super(pEngine, Name, pIsBlindCaller);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	
	// Access Package Variable ---------------------------------------------------------------------
	
	static public class Inst_packageSetAttrValue extends Inst_typeSetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "package_setAttrValue";
		
		protected Inst_packageSetAttrValue(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	static public class Inst_packageGetAttrValue extends Inst_typeGetAttrValue {
		@SuppressWarnings("hiding") static public final String Name = "package_getAttrValue";
		
		protected Inst_packageGetAttrValue(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	static public class Inst_packageGetAttrType extends Inst_typeGetAttrType {
		@SuppressWarnings("hiding") static public final String Name = "package_getAttrType";
		
		protected Inst_packageGetAttrType(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	static public class Inst_packageIsAttrReadable extends Inst_typeIsAttrReadable {
		@SuppressWarnings("hiding") static public final String Name = "package_isAttrReadable";
		
		protected Inst_packageIsAttrReadable(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	static public class Inst_packageIsAttrWritable extends Inst_typeIsAttrWritable {
		@SuppressWarnings("hiding") static public final String Name = "package_isAttrWritable";
		
		protected Inst_packageIsAttrWritable(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	static public class Inst_packageIsAttrNoTypeCheck extends Inst_typeIsAttrNoTypeCheck {
		@SuppressWarnings("hiding") static public final String Name = "package_isAttrNoTypeCheck";
		
		protected Inst_packageIsAttrNoTypeCheck(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	static public class Inst_packageConfigAttr extends Inst_typeConfigAttr {
		@SuppressWarnings("hiding") static public final String Name = "package_configAttr";
		
		protected Inst_packageConfigAttr(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	static public class Inst_packageGetAttrMoreInfo extends Inst_typeGetAttrMoreInfo {
		@SuppressWarnings("hiding") static public final String Name = "package_getAttrMoreInfo";
		
		protected Inst_packageGetAttrMoreInfo(Engine pEngine) {
			super(pEngine, Name);
		}
		/**{@inheritDoc}*/ @Override protected AsTypeProcessKind getAsTypeProcessKind() {
			return AsTypeProcessKind.PACKAGE;
		}
	}
	
	// *********************************************************************************************
	// **** The Abstracts **************************************************************************
	// *********************************************************************************************
	
	// For internal inheritance ----------------------------------------------------------
	
	// Execute an operation ----------------------------------------------------
	
	static public abstract class Inst_Abst_Invoke extends Inst_StackOwner {
		
		protected Inst_Abst_Invoke(Engine pEngine, String pISpecStr, boolean pIsBlindCaller) {
			super(pEngine, pISpecStr);
			this.IsBlindCaller = pIsBlindCaller;
		}
		
		boolean IsBlindCaller;
		
		/**{@inheritDoc}*/ @Override
		protected boolean isBlindCaller() {
			return this.IsBlindCaller;
		}
		
		/**{@inheritDoc}*/ @Override
		protected void reportNoSuperError(Type T, Context pContext, Expression pExpr,
				Object[] pParams) {
			throw new CurryError("The type `"+((T == null)?"null":T.toString())+
					"` is not a Type.TypeWithSuper so curry is unable to get " +
					"its super type for the invocation of '"+this.getSignature()+"'.", pContext);
		}
		/**{@inheritDoc}*/ @Override
		protected void reportNoMatchAsTypeError(Type AsType, Object O, Context pContext,
				Expression pExpr, Object[] pParams) {
			throw new CurryError("The the invocation of '"+this.getSignature()+"' of '"+O.toString()+
					"' cannot be done as '"+AsType.toString()+"'.", pContext);
		}
		
		// Utilities -------------------------------------------------------------------------------

		// NOTE - Since this method is used by two other methods one with already search ES and one is not.
		//           There is some overlap work here (search twice) so we may find the way to optimize it later.
		
		// NOTE - We also are not using the Adjust parameter in optimization as well
		
		/** Invoke an operation of a non-StackOwner object */
		final protected Object invokeDirectNonStackOwner(Object O, Context pContext, Expression pInitiator,
				Object[] pRawParams, ExecSignature pES, Object[] pParams) {
			
			if(pES == null) throw new NullPointerException();
			
			// Short cut for better speed since this is one of the most used
			if(O instanceof String) {
				if("length".equals(pES.getName()) && (pES.getParamCount() == 0) &&
						(TKJava.TInteger.getTypeRef().equals(pES.getReturnTypeRef()))) {
					return ((String)O).length();
				}
			}
			
			// Get Method
			Engine  E         = pContext.getEngine();
			MType   MT        = E.getTypeManager();
			boolean IsReplace = false;
			Type    TO        = MT.getTypeOf(O);
			MT.ensureTypeInitialized(pContext, TO);
			
			Type AsType = this.getRunAsType( pContext, pInitiator, pRawParams);
			if((AsType != null) && (AsType != TO)) {
				TO = AsType;
				IsReplace = true;
			}
			
			// Find using Signature ------------------------------------------------------------------------------------
			// Try search non-static operation
			OperationInfo OI = TO.doData_getOperation(null, pContext, null, AsType, pES);
			if(OI == null) {
				// Try search static operation
				OI = TO.getOperation(pContext, null, AsType, pES);
				if(OI != null) {
					O  = TO;
					return TO.invokeOperation(pContext, pInitiator, this.isBlindCaller(), OperationSearchKind.Direct, TO,
							pES, null, pParams, false);
				}
			}
			if((OI == null) && (IsReplace || (AsType != null))) {
				if(IsReplace) TO = MT.getTypeOf(O);
				AsType = null;
				OI = TO.doData_getOperation(null, pContext, null, AsType, pES);
				
				// Repeat using the TO
				if(OI == null) {
					// Try search static operation
					OI = TO.getOperation(pContext, null, AsType, pES);
					if(OI != null) {
						O  = TO;
						return TO.invokeOperation(pContext, pInitiator, this.isBlindCaller(), OperationSearchKind.Direct, TO,
								pES, null, pParams, false);
					}
				}
			}
			
			// Find using parameters (In case pES contains TRBaseOnType) ----------------------------------------------- 
			// Try search non-static operation
			if(OI == null) {
				String OName = pES.getName();
				ExecSignature ES = null;
				
				if(AsType != null) ES = AsType.searchObjectOperation(E, OName, pParams, null);
				if(ES     == null) ES = TO    .searchObjectOperation(E, OName, pParams, null);
				if(ES     != null) OI = TO    .doData_getOperation(null, pContext, null, AsType, ES);
				if(OI == null) {
					// Try search static operation
					if(AsType != null) ES = AsType.searchOperation(E, OName, pParams, null);
					if(ES     == null) ES = TO    .searchOperation(E, OName, pParams, null);
					if(ES     != null) OI = TO    .getOperation(pContext, null, AsType, pES);
					if(OI != null) {
						O   = TO;
						pES = ES;
						return TO.invokeOperation(pContext, pInitiator, this.isBlindCaller(), OperationSearchKind.Direct, TO,
								pES, null, pParams, false);
					}
				} else pES = ES;
				
				if((OI == null) && (IsReplace || (AsType != null))) {
					if(IsReplace) TO = MT.getTypeOf(O);

					AsType = null;
					ES     = TO.searchObjectOperation(E, OName, pParams, null);
					OI     = TO.doData_getOperation(null, pContext, null, null, ES);
					
					// Repeat using the TO
					if(OI == null) {
						// Try search static operation
						ES = TO.searchObjectOperation(E, OName, pParams, null);
						OI = TO.getOperation(pContext, null, null, pES);
						if(OI != null) {
							O   = TO;
							pES = ES;
							return TO.invokeOperation(pContext, pInitiator, this.isBlindCaller(),
									OperationSearchKind.Direct, TO, pES, null, pParams, false);
						}
					} else pES = ES;
				}
			}
			
			// Process -------------------------------------------------------------------------------------------------
			
			Method M = null;
			if(!(OI instanceof OperationInfo.OINative)) {
				if(OI == null) { 
					throw new CurryError("Internal Error: Operation `("+pContext.getEngine().getTypeManager().getTypeOf(O)+")"+O.toString()+"`."
							+ UObject.toString(pES)+"` is not found.", pContext);
				}
				if(OI instanceof OperationInfo.OIDirect) {
					Accessibility Access = OI.getAccessibility();
					if(!Access.isAllowed(pContext, TO, OI)) {
					throw new CurryError("The operation `("+MT.getTypeOf(O)+")"+O.toString()+
							"` is not accessible to the current stack." + UObject.toString(pES)+"` is not found.",
							pContext);
					}
					
					Executable Exec = (OperationInfo.OIDirect)OI;
					if(Exec == null) return null;
					return pContext.getExecutor().execExecutable(pContext, pInitiator, Exec,
							Exec.getKind(), this.isBlindCaller(), O, pParams, false, true);
				} else {
					throw new CurryError("Internal Error: Invalid respond object - "
							+ "operation `(("+MT.getTypeOf(O)+")"+O.toString()+")."
							+ UObject.toString(pES)+"` is found to be in an invalid type.",
							pContext);
				}
			} else M = ((OperationInfo.OINative)OI).getMethod();
			if(M == null) {
				TO.throwOperation("Unable to invoke a native method.", pContext, AsType,
						TO.getOperationAccessToString(OperationSearchKind.Direct, AsType, pES,
								null));
				return null;
			}

			try {
				if(UClass.isMemberStatic(M)) return UClass.invokeMethod(M, null, pParams);
				else                         return UClass.invokeMethod(M,    O, pParams);
			} catch (Throwable T) {
				throw new CurryError("An error occurs while invoke a native method ("
						+ TO.getOperationAccessToString(OperationSearchKind.Direct, AsType, pES,
								null)+")", pContext, T);
			}
		}
		/** Search and Invoke an operation of a non-StackOwner object */
		final protected Object search_invokeNonStackOwner(Object O, Context pContext, Expression pInitiator,
				Object[] pRawParams, OperationSearchKind pOSKind, Object pParam1, Object pParam2, Object[] pParams) {			
			// Not a StackOwner
			Object[][] AFParams = new Object[1][];
			AFParams[0] = null;
			
			boolean IsReplace = false;
			Type TO = pContext.getEngine().getTypeManager().getTypeOf(O);
			pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, TO);
			
			Type AsType = this.getRunAsType( pContext, pInitiator, pRawParams);
			if((AsType != null) && (AsType != TO)) {
				TO = AsType;
				IsReplace = true;
			}

			// Get Method
			ExecSignature ES = TO.doData_searchOperationLocal(null, Engine, pOSKind, pParam1, pParams, AFParams);
			
			if(ES == null) {
				// Try search static operation
				ES = TO.searchOperationLocal(pContext.getEngine(), pOSKind, pParam1, pParam2, null);
				if(ES != null) return TO.invokeDirect(pContext, pInitiator, true, null, ES, pParams);
			}
			
			if((ES == null) && IsReplace) {
				// Search in the object type
				TO = pContext.getEngine().getTypeManager().getTypeOf(O);
				ES = TO.doData_searchOperationLocal(null, pContext.getEngine(), pOSKind, pParam1, pParams, AFParams);
				
				// Repeat using the TO
				if(ES == null) {
					// Try search static operation
					ES = TO.searchOperationLocal(pContext.getEngine(), pOSKind, pParam1, pParam2, null);
					if(ES != null)  return TO.invokeDirect(pContext, pInitiator, true, null, ES, pParams);
				}
			}
			
			if(ES == null) {
				String ESString = null;
				switch(pOSKind) {
					case Direct: ESString = "()"; break;
					case ByParams:
					case ByTRefs:
						StringBuffer SB = new StringBuffer();
						if((pParam2 != null) && (pParam2.getClass().isArray())) {
							for(int i = 0; i < UArray.getLength(pParam2); i++) {
								if(i != 0) SB.append(", ");
								SB.append(pContext.getEngine().getTypeManager().getTypeOfNoCheck(pContext, UArray.get(pParam2, i)));
							}
						}
						ESString = pParam1 + "("+SB.toString()+")";
						break;
					case ByNameInterface:
					case BySignature:     ESString = pParam1.toString();
				}
					
				throw new CurryError("Internal Error: Invalid respond object - "
						+ "operation `(("+pContext.getEngine().getTypeManager().getTypeOf(O)+")"+O.toString()+")." + ESString+
						"` is not found.", pContext);
			}
			
			return this.invokeDirectNonStackOwner(O, pContext, pInitiator, pRawParams, ES, (AFParams[0]==null)?pParams:AFParams[0]);
		}

		/**{@inherDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			if(!super.manipulateCompileContextFinish(pExpr, pCProduct)) return false;

			// Not our concert
			if(!this.getAsTypeProcessKind().isThis() || !pCProduct.isConstructor()) return true;
			
			return pCProduct.notifyAccessingElement(pExpr);
		}
	}
	// Execute Directly
	static public abstract class Inst_Abst_InvokeDirect extends Inst_Abst_Invoke {
		protected Inst_Abst_InvokeDirect(Engine pEngine, String pISpecStr, boolean pIsBlindCaller, boolean pIsAdjusted) {
			super(pEngine, pISpecStr, pIsBlindCaller);
			this.IsAdjusted = pIsAdjusted;
		}

		boolean IsAdjusted;

		protected boolean isAdjusted() {
			return this.IsAdjusted;
		}
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getParamIndex(); 
		
		protected int           getSignatureIndex() { return -1; }
		protected ExecSignature getSignature(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getSignatureIndex();
			if(Index == -1) return null;
			return (ExecSignature)pParams[Index];
		}
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			ExecSignature ES      = this.getSignature( pContext, pExpr, pParams);
			Object[]      FParams = this.getRawParams( pContext, pExpr, pParams);
			
			// If this is a StackOwner, run it as usual
			if(O instanceof StackOwner) {
				StackOwner SO     = (StackOwner)O;
				Type       AsType = this.getRunAsType( pContext, pExpr, pParams);
				boolean    IsBC   = this.isBlindCaller();
				boolean    IsA    = this.isAdjusted();
				
				try                 { return SO.invokeDirect(pContext, pExpr, IsBC, AsType, ES, FParams, IsA); }
				catch (Exception E) {
					if(E instanceof RuntimeException) throw (RuntimeException)E;
					return new CurryError("Invocation Error", pContext, E);
				}
			}
			// Not a StackOwner
			return this.invokeDirectNonStackOwner(O, pContext, pExpr, pParams, ES, FParams);
		}
		
		// For ReturnType Checking -------------------------------------------------------------------------------------
				
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			int Index = this.getSignatureIndex();
			ExecSignature ES = null;
			if((Index == -1) || ((ES = (ExecSignature)pExpr.getParam(Index)) == null))
				return super.getReturnTypeRef(pExpr, pCProduct);
			
			int PIndex = this.getParamIndex();
			if(PIndex == -1) return ES.getReturnTypeRef();
			
			// Check Parameters
			TypeRef[] TRefs = new TypeRef[pExpr.getParamCount() - PIndex];
			for(int i = TRefs.length; --i >= 0; ) TRefs[i] = pCProduct.getReturnTypeRefOf(pExpr.getParam(i + PIndex));
			if(ExecInterface.Util.canBeAssignedBy_ByPTRefs(pCProduct.getEngine(), ES, TRefs) == ExecInterface.NotMatch)
				return null;
			
			return ES.getReturnTypeRef();
		}
	}
	// Search by Params
	static abstract public class Inst_Abst_InvokeByParams extends Inst_Abst_Invoke {
		
		protected Inst_Abst_InvokeByParams(Engine pEngine, String pISpecStr, boolean pIsBlindCaller) {
			super(pEngine, pISpecStr, pIsBlindCaller);
		}
		
		// abstract protected boolean isAdjusted();
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getParamIndex();
		
		protected int    getONameIndex() { return -1; }
		protected String getOName(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getONameIndex();
			if(Index == -1) return null;
			return (String)pParams[Index];
		}
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String   OName   = this.getOName(    pContext, pExpr, pParams);
			Object[] OParams = this.getRawParams(pContext, pExpr, pParams);
			
			// If this is a StackOwner, run it as usual
			if(O instanceof StackOwner) {
				StackOwner SO     = (StackOwner)O;
				Type       AsType = this.getRunAsType( pContext, pExpr, pParams);
				boolean    IsBC   = this.isBlindCaller();
				
				return SO.invoke(pContext, pExpr, IsBC, AsType, OName, OParams);
			}
			// Not a StackOwner
			return this.search_invokeNonStackOwner(O, pContext, pExpr, pParams, OperationSearchKind.ByParams, OName, OParams, OParams);
		}
		
		// For ReturnType Checking -------------------------------------------------------------------------------------
				
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			int Index = this.getONameIndex();
			if(Index == -1) return super.getReturnTypeRef(pExpr, pCProduct);
			String TheName = (String)pExpr.getParam(Index);
			
			Index = this.getParamIndex();

			if(Index != -1) {
				int Count = pExpr.getParamCount() - Index;
				TypeRef[] TRefs = (Count == 0) ? TypeRef.EmptyTypeRefArray : new TypeRef[Count];
				for(int i = Index; i < Count; i++) {
					TRefs[i] = pCProduct.getReturnTypeRefOf(pExpr.getParam(i));
					if(TRefs[i] == null) TRefs[i] = TKJava.TAny.getTypeRef();
				}

				TypeRef       TR    = this.getAsTypeRef(pExpr, pCProduct);
				ExecSignature TheES = null;
				if(TR == null) {
					PackageBuilder PB = pCProduct.getOwnerPackageBuilder();
					Package        P  = (PB != null)?PB.getPackage():null;
					TheES = (P != null)?P.searchOperation(pCProduct.getEngine(), TheName, TRefs):null;
					
				} else {
					Type T = pCProduct.getTypeAtCompileTime(TR);
					boolean IsStatic = this.isStatic(pExpr, pCProduct);
					TheES = IsStatic
								?T.searchOperation(      pCProduct.getEngine(), TheName, TRefs)
								:T.searchObjectOperation(pCProduct.getEngine(), TheName, TRefs);
	
					if(TheES == null) {
						// Try to find the static operation if the non-static is not found
						if(!IsStatic) TheES = T.searchOperation(pCProduct.getEngine(), TheName, TRefs);
					}
				}
				if(TheES == null) return TKJava.TAny.getTypeRef();
					
				return TheES.getReturnTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	// Search by PTRefs
	static abstract public class Inst_Abst_InvokeByPTRefs extends Inst_Abst_Invoke {
		protected Inst_Abst_InvokeByPTRefs(Engine pEngine, String pISpecStr, boolean pIsBlindCaller) {
			super(pEngine, pISpecStr, pIsBlindCaller);
		}
		
		// abstract protected boolean isAdjusted();
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getParamIndex();
		
		protected int    getONameIndex() { return -1; }
		protected String getOName(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getONameIndex();
			if(Index == -1) return null;
			return (String)pParams[Index];
		}
		protected int       getPTRefsIndex() { return -1; }
		protected TypeRef[] getPTRefs(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getPTRefsIndex();
			if(Index == -1) return null;
			return (TypeRef[])pParams[Index];
		}
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String    OName   = this.getOName(     pContext, pExpr, pParams);
			TypeRef[] OPTRefs = this.getPTRefs(    pContext, pExpr, pParams);
			Object[]  OParams = this.getRawParams( pContext, pExpr, pParams);
			
			// If this is a StackOwner, run it as usual
			if(O instanceof StackOwner) {
				StackOwner SO     = (StackOwner)O;
				Type       AsType = this.getRunAsType( pContext, pExpr, pParams);
				boolean    IsBC   = this.isBlindCaller();
				
				return SO.invoke(pContext, pExpr, IsBC, AsType, OName, OPTRefs, OParams);
			}
			// Not a StackOwner
			return this.search_invokeNonStackOwner(O, pContext, pExpr, pParams, OperationSearchKind.ByTRefs, OName,
					OPTRefs, OParams);
		}
		
		// For ReturnType Checking -------------------------------------------------------------------------------------
				
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			int Index = this.getONameIndex();
			if(Index == -1) return super.getReturnTypeRef(pExpr, pCProduct);
			String TheName = (String)pExpr.getParam(Index);
			
			Index = this.getPTRefsIndex();
			if(Index != -1) {
				Object O = pExpr.getParam(Index);
				
				if(!(O instanceof TypeRef[])) return super.getReturnTypeRef(pExpr, pCProduct);
				
				TypeRef[] TRefs = (TypeRef[])O;
				
				TypeRef       TR    = this.getAsTypeRef(pExpr, pCProduct);
				ExecSignature TheES = null;
				if(TR == null) {
					PackageBuilder PB = pCProduct.getOwnerPackageBuilder();
					Package        P  = (PB != null)?PB.getPackage():null;
					TheES = (P != null)?P.searchOperation(pCProduct.getEngine(), TheName, TRefs):null;
					
				} else {
					Type T = pCProduct.getTypeAtCompileTime(TR);
	
					boolean IsStatic = this.isStatic(pExpr, pCProduct);
					TheES = IsStatic
								?T.searchOperation(      pCProduct.getEngine(), TheName, TRefs)
								:T.searchObjectOperation(pCProduct.getEngine(), TheName, TRefs);
	
					if(TheES == null) {
						// Try to find the static operation if the non-statc is not found
						if(!IsStatic) TheES = T.searchOperation(pCProduct.getEngine(), TheName, TRefs);
					}
				}
				if(TheES == null) return null;
					
				return TheES.getReturnTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}
	// Search by ParamInterfaces
	static abstract public class Inst_Abst_InvokeByInterface extends Inst_Abst_Invoke {
		protected Inst_Abst_InvokeByInterface(Engine pEngine, String pISpecStr, boolean pIsBlindCaller) {
			super(pEngine, pISpecStr, pIsBlindCaller);
		}
		
		// abstract protected boolean isAdjusted();
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getParamIndex();
		
		protected int    getONameIndex() { return -1; }
		protected String getOName(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getONameIndex();
			if(Index == -1) return null;
			return (String)pParams[Index];
		}
		protected int           getInterfaceIndex() { return -1; }
		protected ExecInterface getInterface(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getInterfaceIndex();
			if(Index == -1) return null;
			return (ExecInterface)pParams[Index];
		}
		
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String        OName   = this.getOName(     pContext, pExpr, pParams);
			ExecInterface FEI     = this.getInterface( pContext, pExpr, pParams);
			Object[]      OParams = this.getRawParams( pContext, pExpr, pParams);
			
			// If this is a StackOwner, run it as usual
			if(O instanceof StackOwner) {
				StackOwner SO     = (StackOwner)O;
				Type       AsType = this.getRunAsType( pContext, pExpr, pParams);
				boolean    IsBC   = this.isBlindCaller();
				
				return SO.invoke(pContext, pExpr, IsBC, AsType, OName, FEI, OParams);
			}
			// Not a StackOwner
			return this.search_invokeNonStackOwner(O, pContext, pExpr, pParams, OperationSearchKind.ByTRefs, OName, FEI,
					OParams);
		}
		
		// For ReturnType Checking -------------------------------------------------------------------------------------
				
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			int Index = this.getONameIndex();
			if(Index == -1) return super.getReturnTypeRef(pExpr, pCProduct);
			String TheName = (String)pExpr.getParam(Index);
			
			Index = this.getInterfaceIndex();
			if(Index != -1) {
				ExecInterface EI = (ExecInterface)pExpr.getParam(Index);
				
				TypeRef       TR    = this.getAsTypeRef(pExpr, pCProduct);
				ExecSignature TheES = null;
				if(TR == null) {
					PackageBuilder PB = pCProduct.getOwnerPackageBuilder();
					Package        P  = (PB != null)?PB.getPackage():null;
					TheES = (P != null)?P.searchOperation(pCProduct.getEngine(), TheName, EI):null;
					
				} else {
					Type T = pCProduct.getTypeAtCompileTime(TR);
	
					boolean IsStatic = this.isStatic(pExpr, pCProduct);
					TheES = IsStatic
								?T.searchOperation(      pCProduct.getEngine(), TheName, EI)
								:T.searchObjectOperation(pCProduct.getEngine(), TheName, EI);
	
					if(TheES == null) {
						// Try to find the static operation if the non-statc is not found
						if(!IsStatic) TheES = T.searchOperation(pCProduct.getEngine(), TheName, EI);
					}
				}
				if(TheES == null) return null;
					
				return TheES.getReturnTypeRef();
			}
			return super.getReturnTypeRef(pExpr, pCProduct);
		}
	}

	// Access Attribute --------------------------------------------------------
	
	static abstract public class Inst_Access extends Inst_StackOwner {
		protected Inst_Access(Engine pEngine, String pISpecStr) { super(pEngine, pISpecStr); }
		
		final protected Object accessAttrNonStackOwner(Object O, Context pContext, Expression pExpr,
				String pAttrName, net.nawaman.curry.util.DataHolder.AccessKind DHAK, Type pAsType,
				Object pParam1, Object pParam2) {
			
			// Short cut for better speed since this is one of the must used
			if(O.getClass().isArray()) {
				if("length".equals(pAttrName) && (DHAK == net.nawaman.curry.util.DataHolder.AccessKind.Get)) return UArray.getLength(O);
			}
			
			// Get Field
			Type TO = pContext.getEngine().getTypeManager().getTypeOf(O);
			pContext.getEngine().getTypeManager().ensureTypeInitialized(pContext, TO);
			
			AttributeInfo AI = TO.doData_getAttribute(null, pContext, pExpr, DHAK, pAsType, pAttrName);
			// If not found, find somewhere else (with no AsType, Static)
			if(AI == null) AI = TO.doData_getAttribute(null, pContext, pExpr, DHAK, null, pAttrName);
			if(AI == null) AI = TO.getAttribute(pContext, pExpr, DHAK, pAsType, pAttrName);
			if(AI == null) AI = TO.getAttribute(pContext, pExpr, DHAK, null, pAttrName);
					
			if(!(AI instanceof AttributeInfo.AINative)) {
				if(AI == null) {
					throw new CurryError("Internal Error: Invalid respond object - "
							+ "attribute `(("+pContext.getEngine().getTypeManager().getTypeOf(O)+")"+O.toString()+")."+pAttrName+"` is not found.",
							pContext);
				}
				return TO.accessAttribute(pContext, pExpr, DHAK, TO, pAttrName, pParam1, pParam2, null);
			}

			Field F = ((AttributeInfo.AINative)AI).Field;
			
			// Get the type of this Attribute
			switch(DHAK) {
				case Get: {
					// Get Parameters
					try {
						if(UClass.isMemberStatic(F)) return UClass.getFieldValue(F, null);
						else                         return UClass.getFieldValue(F,    O);
					} catch (Throwable T) {
						throw new CurryError("An error occurs while getting the value of the field "
								+ "'"+pAttrName+"' of '"+pContext.getEngine().toString(pContext, O)
								+ "'." , pContext);
					}
				}
				case Set: {
					try {
						if(UClass.isMemberStatic(F)) return UClass.setFieldValue(F, null, pParam1);
						else                         return UClass.setFieldValue(F,    O, pParam1);
					} catch (Throwable T) {
						throw new CurryError("An error occurs while setting the value of the field "
								+ "'"+pAttrName+"' with '"+pContext.getEngine().toString(pParam1)
								+"' of '"+pContext.getEngine().toString(pContext, O)+"'."
								, pContext);
					}
				}
				case IsReadable:
					return true;
				case IsWritable:
					return !UClass.isMemberFinal(F);
				case IsNotTypeCheck:
					return false;
				case GetType:
					return pContext.getEngine().getTypeManager().getTypeOfTheInstanceOf(F.getType());
				case Clone:
					throw new CurryError("Attribute Access Error: Clone Attribute is not allowed.",
							pContext);
				case Config:
				case GetMoreInfo:
					return null;
			}
			throw new CurryError("Internal Error: "
					+"An unknow access kind (Instructions_StackOwner.java (960)).", pContext);
		}
		
		// Report error for access ---------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		protected void reportNoSuperError(Type T, Context pContext, Expression pExpr,
				Object[] pParams) {
			throw new CurryError("The type `"+T.toString()+"` is not a Type.TypeWithSuper so curry is unable to get " +
					"its super type for the access of '"+this.getAttrName(pContext, pExpr, pParams)+"'.", pContext);
		}
		
		/**{@inheritDoc}*/ @Override
		protected void reportNoMatchAsTypeError(Type AsType, Object O, Context pContext,
				Expression pExpr, Object[] pParams) {
			throw new CurryError("The the access of '"+this.getAttrName(pContext, pExpr, pParams)+"' of '"+O.toString()+
					"' cannot be done as '"+AsType.toString()+"'.", pContext);
		}
		
		// For ReturnType Checking -------------------------------------------------------------------------------------
		
		/** Return the DataHolder Access kind for this instruction */
		abstract protected net.nawaman.curry.util.DataHolder.AccessKind getDHAK();
				
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			net.nawaman.curry.util.DataHolder.AccessKind DHAK = this.getDHAK();
			
			int Index = this.getAttrNameIndex();
			if(Index == -1) return super.getReturnTypeRef(pExpr, pCProduct);
			String TheName = (String)pExpr.getParam(Index);
			if(TheName == null) return null;
			
			TypeRef TheTR = null;
			if(this.getAsTypeProcessKind().isPackage()) {
				PackageBuilder PB = pCProduct.getOwnerPackageBuilder();
				HashSet<AttributeInfo> AIs = PB.getAttrInfos();
				for(AttributeInfo AI : AIs) {
					if((AI == null) || !TheName.equals(AI.getName())) continue;
					TheTR = AI.getTypeRef();
					break;
				}
			} else {
				TypeRef TR = this.getAsTypeRef(pExpr, pCProduct);
				if(TR == null) {
					PackageBuilder PB = pCProduct.getOwnerPackageBuilder();
					Package        P  = (PB != null)?PB.getPackage():null;
					TheTR = (P != null)?P.searchAttribute(pCProduct.getEngine(), false, null, TheName):null;
					
				} else {
					Type T = pCProduct.getTypeAtCompileTime(TR);
					
					boolean IsStatic = this.isStatic(pExpr, pCProduct);
					AttributeInfo[] AIs = IsStatic
							? T.getTypeInfo().getAttributeInfos()
							: T.getTypeInfo().getObjectAttributeInfos();
					
					for(AttributeInfo AI : AIs) {
						if((AI == null) || !TheName.equals(AI.getName())) continue;
						if((TheTR = AI.getDeclaredTypeRef()) == null) TheTR = AI.getTypeRef();
						break;
					}
					if(TheTR == null) {
						// Try static, if the non-static is found
						if(!IsStatic) {
							AIs = TR.getTheType().getTypeInfo().getAttributeInfos();
							for(AttributeInfo AI : AIs) {
								if((AI == null) || !TheName.equals(AI.getName())) continue;
								if((TheTR = AI.getDeclaredTypeRef()) == null) TheTR = AI.getTypeRef();
								break;
							}
						}
						
						if(TheTR == null) return null;
					}
				}
			}
					
			// Get the type of this Attribute
			switch(DHAK) {
				case Get:
				case Set:            return TheTR;
				case IsReadable:     return TKJava.TBoolean.getTypeRef();
				case IsWritable:     return TKJava.TBoolean.getTypeRef();
				case IsNotTypeCheck: return TKJava.TBoolean.getTypeRef();
				case GetType:        return TKJava.TType.getTypeRef();
				case Clone:          return TKJava.TVoid.getTypeRef();
				case Config:
				case GetMoreInfo:    return TKJava.TAny.getTypeRef();
			}
			
			return null;
		}

		/**{@inherDoc}*/ @Override
		public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
			if(!super.manipulateCompileContextFinish(pExpr, pCProduct)) return false;

			// Not our concert
			if(!this.getAsTypeProcessKind().isThis() || !pCProduct.isConstructor()) return true;
			
			return pCProduct.notifyAccessingElement(pExpr);
		}
	}

	static abstract public class Inst_Abst_SetAttrValue extends Inst_Access {
		protected Inst_Abst_SetAttrValue(Engine pEngine, String pISpecStr) { super(pEngine, pISpecStr); }
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getAttrNameIndex();
		
		protected int getDataIndex() { return -1; }
		
		Object getData(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getDataIndex();
			if(Index == -1) return null;
			return pParams[Index];
		}
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String AN = this.getAttrName(  pContext, pExpr, pParams);
			Type   AT = this.getRunAsType( pContext, pExpr, pParams);
			Object P  = this.getData(      pContext, pExpr, pParams);
			if(O instanceof StackOwner) {
				StackOwner SO = (StackOwner)O;
				return SO.setAttrData(pContext, pExpr, AT, AN, P);
			}
			return this.accessAttrNonStackOwner(O, pContext, pExpr, AN,
					net.nawaman.curry.util.DataHolder.AccessKind.Set, AT, P, null);
		}
		
		/**{inheritDoc}*/ @Override
		protected net.nawaman.curry.util.DataHolder.AccessKind getDHAK() {
			return net.nawaman.curry.util.DataHolder.AccessKind.Set;
		}
	}
	static abstract public class Inst_Abst_GetAttrValue extends Inst_Access {
		protected Inst_Abst_GetAttrValue(Engine pEngine, String pISpecStr) { super(pEngine, pISpecStr); }
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getAttrNameIndex();
		
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String AN = this.getAttrName(  pContext, pExpr, pParams);
			Type   AT = this.getRunAsType( pContext, pExpr, pParams);
			if(O instanceof StackOwner) {
				StackOwner SO = (StackOwner)O;
				return SO.getAttrData(pContext, pExpr, AT, AN, null);
			}
			return this.accessAttrNonStackOwner(O, pContext, pExpr, AN,
					net.nawaman.curry.util.DataHolder.AccessKind.Get, AT, null, null);
		}
		
		/**{inheritDoc}*/ @Override
		protected net.nawaman.curry.util.DataHolder.AccessKind getDHAK() {
			return net.nawaman.curry.util.DataHolder.AccessKind.Get;
		}
	}
	static abstract public class Inst_Abst_GetAttrType extends Inst_Access {
		protected Inst_Abst_GetAttrType(Engine pEngine, String pISpecStr) { super(pEngine, pISpecStr); }
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getAttrNameIndex();
		
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String AN = this.getAttrName(  pContext, pExpr, pParams);
			Type   AT = this.getRunAsType( pContext, pExpr, pParams);
			if(O instanceof StackOwner) {
				StackOwner SO = (StackOwner)O;
				return SO.getAttrType(pContext, pExpr, AT, AN);
			}
			return this.accessAttrNonStackOwner(O, pContext, pExpr, AN,
					net.nawaman.curry.util.DataHolder.AccessKind.GetType, AT, null, null);
		}
		
		/**{inheritDoc}*/ @Override protected net.nawaman.curry.util.DataHolder.AccessKind getDHAK() {
			return net.nawaman.curry.util.DataHolder.AccessKind.GetType;
		}
	}
	static abstract public class Inst_Abst_IsAttrReadable extends Inst_Access {
		protected Inst_Abst_IsAttrReadable(Engine pEngine, String pISpecStr) { super(pEngine, pISpecStr); }
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getAttrNameIndex();
		
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String AN = this.getAttrName(  pContext, pExpr, pParams);
			Type   AT = this.getRunAsType( pContext, pExpr, pParams);
			if(O instanceof StackOwner) {
				StackOwner SO = (StackOwner)O;
				return SO.isAttrReadable(pContext, pExpr, AT, AN);
			}
			return this.accessAttrNonStackOwner(O, pContext, pExpr, AN,
					net.nawaman.curry.util.DataHolder.AccessKind.IsReadable, AT, null, null);
		}
		
		/**{inheritDoc}*/ @Override protected net.nawaman.curry.util.DataHolder.AccessKind getDHAK() {
			return net.nawaman.curry.util.DataHolder.AccessKind.IsReadable;
		}
	}
	static abstract public class Inst_Abst_IsAttrWritable extends Inst_Access {
		protected Inst_Abst_IsAttrWritable(Engine pEngine, String pISpecStr) { super(pEngine, pISpecStr); }
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getAttrNameIndex();
		
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String AN = this.getAttrName(  pContext, pExpr, pParams);
			Type   AT = this.getRunAsType( pContext, pExpr, pParams);
			if(O instanceof StackOwner) {
				StackOwner SO = (StackOwner)O;
				return SO.isAttrWritable(pContext, pExpr, AT, AN);
			}
			return this.accessAttrNonStackOwner(O, pContext, pExpr, AN,
					net.nawaman.curry.util.DataHolder.AccessKind.IsWritable, AT, null, null);
		}
		
		/**{inheritDoc}*/ @Override protected net.nawaman.curry.util.DataHolder.AccessKind getDHAK() {
			return net.nawaman.curry.util.DataHolder.AccessKind.IsWritable;
		}
	}
	static abstract public class Inst_Abst_IsAttrNoTypeCheck extends Inst_Access {
		protected Inst_Abst_IsAttrNoTypeCheck(Engine pEngine, String pISpecStr) { super(pEngine, pISpecStr); }
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getAttrNameIndex();
		
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String AN = this.getAttrName(  pContext, pExpr, pParams);
			Type   AT = this.getRunAsType( pContext, pExpr, pParams);
			if(O instanceof StackOwner) {
				StackOwner SO = (StackOwner)O;
				return SO.isAttrNoTypeCheck(pContext, pExpr, AT, AN);
			}
			return this.accessAttrNonStackOwner(O, pContext, pExpr, AN,
					net.nawaman.curry.util.DataHolder.AccessKind.IsNotTypeCheck, AT, null, null);
		}
		
		/**{inheritDoc}*/ @Override protected net.nawaman.curry.util.DataHolder.AccessKind getDHAK() {
			return net.nawaman.curry.util.DataHolder.AccessKind.IsNotTypeCheck;
		}
	}
	static abstract public class Inst_Abst_ConfigAttr extends Inst_Access {
		protected Inst_Abst_ConfigAttr(Engine pEngine, String pISpecStr) { super(pEngine, pISpecStr); }
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getAttrNameIndex();
		
		protected int getCNameIndex()   { return -1; }
		protected int getCParamsIndex() { return -1; }
		
		protected String getCName(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getCNameIndex();
			if(Index == -1) return null;
			return (String)pParams[Index];
		}
		protected Object[] getCParams(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getCParamsIndex();
			if(Index == -1) return null;
			return (Object[])pParams[Index];
		}
		
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null operand: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String   AN = this.getAttrName(  pContext, pExpr, pParams);
			Type     AT = this.getRunAsType( pContext, pExpr, pParams);
			String   CN = this.getCName(     pContext, pExpr, pParams);
			Object[] CP = this.getCParams(   pContext, pExpr, pParams);
			if(O instanceof StackOwner) {
				StackOwner SO = (StackOwner)O;
				return SO.configAttr(pContext, pExpr, AT, AN, CN, CP);
			}
			return this.accessAttrNonStackOwner(O, pContext, pExpr, AN,
					net.nawaman.curry.util.DataHolder.AccessKind.Config, AT, CN, CP);
		}
		
		/**{inheritDoc}*/ @Override protected net.nawaman.curry.util.DataHolder.AccessKind getDHAK() {
			return net.nawaman.curry.util.DataHolder.AccessKind.Config;
		}
	}
	static abstract public class Inst_Abst_GetAttrMoreInfo extends Inst_Access {
		protected Inst_Abst_GetAttrMoreInfo(Engine pEngine, String pISpecStr) {
			super(pEngine, pISpecStr);
		}
		
		// abstract protected int getStackOwnerIndex();
		// abstract protected int getRunAsTypeIndex();
		// abstract protected int getAttrNameIndex();
		
		protected int getMINameIndex() { return -1; }
		String getMIName(Context pContext, Expression pExpr, Object[] pParams) {
			int Index = this.getMINameIndex();
			if(Index == -1) return null;
			return (String)pParams[Index];
		}
		
		@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
			Object O = this.getObject(pContext, pExpr, pParams);
			if(O == null) {
				throw new CurryError(
						"Null stack Owner: " + pExpr.toDetail(this.Engine),
						pContext,
						new NullPointerException(pExpr.toDetail(this.Engine)));
			}
			
			String   AN = this.getAttrName(  pContext, pExpr, pParams);
			Type     AT = this.getRunAsType( pContext, pExpr, pParams);
			String   MI = this.getMIName(    pContext, pExpr, pParams);
			if(O instanceof StackOwner) {
				StackOwner SO = (StackOwner)O;
				return SO.getAttrMoreInfo(pContext, pExpr, AT, AN, MI);
			}
			return this.accessAttrNonStackOwner(O, pContext, pExpr, AN,
					net.nawaman.curry.util.DataHolder.AccessKind.GetMoreInfo, AT, MI, null);
		}
		
		/**{inheritDoc}*/ @Override protected net.nawaman.curry.util.DataHolder.AccessKind getDHAK() {
			return net.nawaman.curry.util.DataHolder.AccessKind.GetMoreInfo;
		}
	}
}