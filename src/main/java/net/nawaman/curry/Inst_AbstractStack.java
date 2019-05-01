package net.nawaman.curry;

import net.nawaman.curry.compiler.CompileProduct;

public class Inst_AbstractStack extends Inst_AbstractGroup {

	protected Inst_AbstractStack(Engine pEngine, String pISpecStr) {
		super(pEngine, pISpecStr);
	}

	protected Inst_AbstractStack(Engine pEngine, InstructionSpec pISpec) {
		super(pEngine, pISpec);
	}

	@Override protected Expression createExpression(int pCol, int pRow, Object[] pParameters,
			Expression[] pSubExpressions) {
		return this.newExprComplex(pCol, pRow, this, pParameters, pSubExpressions);
	}

	// Execution --------------------------------------------------------------
	@Override protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
		return super.run(new Context(pContext, this.getStackName(pParams), pExpr), pExpr, pParams);
	}

	protected String getStackName(Object[] pParams) {
		return null;
	}

	/**{@inheritDoc}*/ @Override
	protected Object processResult(Context pContext, Expression pExpr, Object[] pParams, Object pResult) {
		// Trap break.
		if(pResult instanceof SpecialResult.ResultExit) {
			String SName = ((SpecialResult.ResultExit)pResult).Name;
			String EName = this.getStackName(pParams);
			if((SName == null) || (SName == EName) || SName.equals(EName))
				return new SpecialResult.ResultEnd(((SpecialResult.ResultExit)pResult).Result);
		}
		return pResult;
	}
	/**{@inheritDoc}*/ @Override
	public boolean manipulateCompileContextBeforeSub(Object[] pParams,
			CompileProduct pCProduct, int pPosition) {
		Object O = this.getStackName(pParams);
		if((O != null) && !(O instanceof String))
			pCProduct.reportWarning("Stack label cannot be determined at compile time", null, pPosition);
		pCProduct.newScope((O instanceof String)?(String)O:null, null);
		return true;
	}
	/**{@inheritDoc}*/ @Override
	public boolean manipulateCompileContextFinish(Expression pExpr, CompileProduct pCProduct) {
		pCProduct.exitScope();
		return true;
	}
	
	/** {@inheritDoc} */ @Override
	public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
		// TODO - The last command and all exit with the name or the most outer ones with no name are the return type
		return super.getReturnTypeRef(pExpr, pCProduct);
	}
}