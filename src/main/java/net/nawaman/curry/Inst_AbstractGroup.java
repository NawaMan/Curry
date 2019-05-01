package net.nawaman.curry;

import net.nawaman.curry.compiler.CompileProduct;

abstract public class Inst_AbstractGroup extends Instruction {

	// The engine will be used only for creating ISpec. It is not stored in the
	// instruction until
	// the instruction is actually registered.
	/** Constructs a new Instruction by using auto-generated spec. */
	protected Inst_AbstractGroup(Engine pEngine, String pISpecStr) {
		super(pEngine, pISpecStr);
	}

	// The engine will be used only for creating ISpec. It is not stored in the
	// instruction until
	// the instruction is actually registered.
	/** Constructs a new Instruction by using explicitly specified spec. */
	protected Inst_AbstractGroup(Engine pEngine, InstructionSpec pISpec) {
		super(pEngine, pISpec);
	}

	@Override
	protected Expression createExpression(int pCol, int pRow, Object[] pParameters,
			Expression[] pSubExpressions) {
		return this.newExprGroup(pCol, pRow, this, pSubExpressions);
	}

	// Execution --------------------------------------------------------------
	final protected Object doGroupBody(Context pContext, Expression pExpr, Object[] pParams) {
		// Do the group thing
		Object R = null;
		for (int i = 0; i < pExpr.getSubExprCount(); i++) {
			Expression Expr = pExpr.getSubExpr(i);
			R = this.executeAnExpression(pContext, Expr);
			R = this.processResult(pContext, pExpr, pParams, R);
			if (R instanceof SpecialResult) {
				if      (R instanceof SpecialResult.ResultEnd) R = ((SpecialResult.ResultEnd)R).Result;
				else if (R instanceof SpecialResult.ResultReplace) {
					R = ((SpecialResult.ResultReplace) R).Result;
					Expression WrappedR = Expression.newNonSerializableData(R);
					if(     pExpr instanceof Expression.Expr_Group)   ((Expression.Expr_Group)  pExpr).SubExprs[i] = WrappedR;
					else if(pExpr instanceof Expression.Expr_Complex) ((Expression.Expr_Complex)pExpr).SubExprs[i] = WrappedR;
				}
				break;
			}
		}
		return R;
	}
	
	protected Object runGroup(Context pContext, Expression pExpr, Object[] pParams) {
		Object R = this.preGroup(pContext, pExpr, pParams);
		if (R != null)
			return R;
		
		R = this.doGroupBody(pContext, pExpr, pParams);
		
		// Only local can replace R.
		return this.postGroup(pContext, pExpr, pParams, R);
	}

	/**{@inheritDoc}*/ @Override
	protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
		return this.runGroup(pContext, pExpr, pParams);
	}

	// For internal customization ----------------------------------------------

	/**
	 * Do any pre-process and return null to process group execution. Returns something means to NOT proceed with group
	 * execution and the return value will be returned.
	 */
	protected Object preGroup(Context pContext, Expression pExpr, Object[] pParams) {
		return null;
	}

	/** Process each result. */
	protected Object processResult(Context pContext, Expression pExpr, Object[] pParams,
			Object pResult) {
		return pResult;
	}

	/** Do any post process. Only local that thre return will be replace with R. */
	protected Object postGroup(Context pContext, Expression pExpr, Object[] pParams,
			Object pLastResult) {
		return pLastResult;
	}
	
	/** {@inheritDoc} */ @Override
	public TypeRef getReturnTypeRef(Expression pExpr, CompileProduct pCProduct) {
		TypeRef TR = pCProduct.getReturnTypeRefOf(pExpr.getSubExpr(pExpr.getSubExprCount() - 1));
		if(TR == null) return TKJava.TAny.getTypeRef();
		return TR;
	}
}