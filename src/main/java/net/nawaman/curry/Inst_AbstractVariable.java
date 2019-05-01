package net.nawaman.curry;

import net.nawaman.curry.compiler.CompileProduct;

/** Abstract class for instruction that involve variables */
abstract public class Inst_AbstractVariable extends Inst_AbstractSimple {

	protected Inst_AbstractVariable(Engine pEngine, String pISpec, VariableInfo pVarInfo) {
		super(pEngine, pISpec);
		this.VarInfo = pVarInfo;
	}
	
	final VariableInfo VarInfo;
	
	/** Returns the identity of the variable in the human friendly */
	protected String getVariableIdentity(Expression pExpr, CompileProduct pCProduct, boolean pIsWithCapitolThe) {
		return this.VarInfo.getVariableIdentity(pExpr, pCProduct, pIsWithCapitolThe);
	}

	/** Checks at the compile time if the parameters is in the correct type */
	protected boolean checkParameterTypes(Expression pExpr, CompileProduct pCProduct) {
		return this.VarInfo.checkParameterTypes(pExpr, pCProduct);
	}

	/** Checks at the compile time if the variable exist */
	protected boolean checkExistanceCompileTime(Expression pExpr, CompileProduct pCProduct) {
		return this.VarInfo.checkExistanceCompileTime(pExpr, pCProduct);
	}

	/**
	 * Returns the TypeRef of the variable (this method will be called after checkParameterTypes(...) so there is not
	 *   need) to re-check that.
	 **/
	protected TypeRef getVariableTypeRef(Expression pExpr, CompileProduct pCProduct) {
		return this.VarInfo.getVariableTypeRef(pExpr, pCProduct);
	}

	/** Ensure that the variable exist and report error if it is not */
	protected boolean ensureVariableExist(Expression pExpr, CompileProduct pCProduct) {
		if(!this.checkParameterTypes(pExpr, pCProduct)) return false;
		
		if(!this.checkExistanceCompileTime(pExpr, pCProduct)) {
			String Identity = this.getVariableIdentity(pExpr, pCProduct, true);
			ReportCompileProblem(
					"Inst_AbstractVariable("+this.getName()+"):44",
					String.format(Identity + " does not exist."),
					pExpr, pCProduct, false, false);
			return false;
		}
		return true;
	}
	
	// Default implementation of the required methods -------------------------------------------------------
	
	/**{@inheritDoc}*/ @Override
	public boolean ensureParamCorrect(Expression pExpr, CompileProduct pCProduct, boolean pIsIgnoreReturnTypeCheck) {
		if(pCProduct.isCompileTimeCheckingNone()) return true;
		return this.ensureVariableExist(pExpr, pCProduct);
	}
	/**{@inheritDoc}*/ @Override
	public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
		if(!this.checkParameterTypes(pExpr, pCProduct)) return TKJava.TType.getTypeRef();
		return this.getVariableTypeRef(pExpr, pCProduct);
	}
	/**@inherDoc()*/ @Override
	public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
		if(pCProduct.isCompileTimeCheckingNone())            return true;
		if(!this.ensureParamCorrect(pExpr, pCProduct, true)) return false;

		// Not our concert
		if(!pCProduct.isConstructor())                             return true;
		if(this.VarInfo != Instructions_Context.LocalVar.Instance) return true;
		if(!Context.StackOwner_VarName.equals(pExpr.getParam(0)) &&
		   !"this"                    .equals(pExpr.getParam(0)))  return true;
		
		return pCProduct.notifyAccessingElement(pExpr);
	}
	
	// SubClasses ------------------------------------------------------------------------------------------------------

	/** Abstract class of set-variable-value instruction */
	static abstract public class Inst_SetVariableValue extends Inst_AbstractVariable {
		
		protected Inst_SetVariableValue(Engine pEngine, String pISpec, VariableInfo pVarInfo) {
			super(pEngine, pISpec, pVarInfo);
		}

		/** Checks at the compile time if the variable is constant */
		protected boolean checkConstantCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return this.VarInfo.checkConstantCompileTime(pExpr, pCProduct);
		}

		/** Checks at the compile time if the variable is need to be given compatible-typed value when assigned */
		protected boolean checkNeedCompatibleTypeCompileTime(Expression pExpr, CompileProduct pCProduct) {
			return this.VarInfo.checkNeedCompatibleTypeCompileTime(pExpr, pCProduct);
		}
		
		/** Returns the TypeRef of the assigned value */
		protected TypeRef getAssignedValueTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return this.VarInfo.getAssignedValueTypeRef(pExpr, pCProduct);
		}
		
		/** Checks if the assigned value is null */
		protected boolean isAssignedValueNull(Expression pExpr, CompileProduct pCProduct) {
			return this.VarInfo.isAssignedValueNull(pExpr, pCProduct);
		}

		/** Ensure that the variable exist and report error if it is not */
		protected boolean ensureVariableWritable(Expression pExpr, CompileProduct pCProduct) {
			if(!this.checkParameterTypes(pExpr, pCProduct)) return false;
			
			if(this.checkConstantCompileTime(pExpr, pCProduct)) {
				String Identity = this.getVariableIdentity(pExpr, pCProduct, true);
				ReportCompileProblem(
						"Inst_AbstractVariable("+this.getName()+"):99",
						String.format(Identity + " is a constant."),
						pExpr, pCProduct, false, false);
				return false;
			}
			return true;
		}

		/** Ensure that the value set to the variable is compatible with the type */
		protected boolean ensureVariableSetCompatibility(Expression pExpr, CompileProduct pCProduct, TypeRef pTRefValue) {
			if(!this.checkParameterTypes(pExpr, pCProduct)) return false;
			if(pTRefValue == null)                          return false;
			if(this.isAssignedValueNull(pExpr, pCProduct))  return  true;

			TypeRef TR   = this.getVariableTypeRef(     pExpr, pCProduct);
			TypeRef TRef = this.getAssignedValueTypeRef(pExpr, pCProduct);
			Boolean MayMatch = pCProduct.getEngine().getTypeManager().mayTypeRefBeCastedTo(TR, TRef);
			if(!Boolean.TRUE.equals(MayMatch)) {
				String Identity = this.getVariableIdentity(pExpr, pCProduct, true);
				ReportCompileProblem(
						"Inst_AbstractVariable:119", String.format("Imcompatible type for `%s` (%s to %s)", Identity, TRef, TR),
						pExpr, pCProduct, (MayMatch == null), false);
				return false;
			}
			return true;
		}
		
		// Satisfy Instruction -----------------------------------------------------------------------------------------
		
		/**{@inheritDoc}*/ @Override
		public boolean ensureParamCorrect(Expression pExpr, CompileProduct pCProduct, boolean pIsIgnoreReturnTypeCheck) {
			// Variable must exist
			if(!super.ensureParamCorrect(pExpr, pCProduct, pIsIgnoreReturnTypeCheck)) return false;
			// It must be writable
			if(!this.ensureVariableWritable(pExpr, pCProduct)) return false;
			// Check assignment type compatibility
			if(this.checkNeedCompatibleTypeCompileTime(pExpr, pCProduct) &&
			  !this.ensureVariableSetCompatibility(pExpr, pCProduct, this.getAssignedValueTypeRef(pExpr, pCProduct)))
				return false;
			return true;
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			// Variable must exist
			if(!super.ensureParamCorrect(pExpr, pCProduct, true)) return null;
			return this.getAssignedValueTypeRef(pExpr, pCProduct);
		}
	}
	

	/** Abstract class of set-variable-value instruction */
	static abstract public class Inst_IsVariableExist extends Inst_AbstractSimple {
		protected Inst_IsVariableExist(Engine pEngine, String pISpec) {
			super(pEngine, pISpec);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return TKJava.TBoolean.getTypeRef();
		}
	}
	
	static public class Inst_IsVariableConstant extends Inst_AbstractVariable {
		protected Inst_IsVariableConstant(Engine pEngine, String pISpec, VariableInfo pVarInfo) {
			super(pEngine, pISpec, pVarInfo);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return TKJava.TBoolean.getTypeRef();
		}
	}

	static public class Inst_GetVariableType extends Inst_AbstractVariable {
		protected Inst_GetVariableType(Engine pEngine, String pISpec, VariableInfo pVarInfo) {
			super(pEngine, pISpec, pVarInfo);
		}
		/**{@inheritDoc}*/ @Override
		public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
			return TKJava.TType.getTypeRef();
		}
	}
	
	/** Abstract class of set-variable-value instruction */
	static public interface VariableInfo {
		
		/** Returns the identity of the variable in the human friendly */
		public String getVariableIdentity(Expression pExpr, CompileProduct pCProduct, boolean pIsWithCapitolThe);

		/** Checks at the compile time if the parameters is in the correct type */
		public boolean checkParameterTypes(Expression pExpr, CompileProduct pCProduct);

		/** Checks at the compile time if the variable exist */
		public boolean checkExistanceCompileTime(Expression pExpr, CompileProduct pCProduct);

		/**
		 * Returns the TypeRef of the variable (this method will be called after checkParameterTypes(...) so there is 
		 *   not need) to re-check that.
		 **/
		public TypeRef getVariableTypeRef(Expression pExpr, CompileProduct pCProduct);
		
		/** Checks at the compile time if the variable is constant */
		public boolean checkConstantCompileTime(Expression pExpr, CompileProduct pCProduct);

		/** Checks at the compile time if the variable is need to be given compatible-typed value when assigned */
		public boolean checkNeedCompatibleTypeCompileTime(Expression pExpr, CompileProduct pCProduct);
		
		/** Returns the TypeRef of the assigned value */
		public TypeRef getAssignedValueTypeRef(Expression pExpr, CompileProduct pCProduct);

		/** Checks if the assigned value is null */
		public boolean isAssignedValueNull(Expression pExpr, CompileProduct pCProduct);

	}
	
}
