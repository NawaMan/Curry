package net.nawaman.curry;

/** A simple instruction is an instruction that has no sub-expressions (only parameters). */
abstract public class Inst_AbstractSimple extends Instruction {
	
	protected Inst_AbstractSimple(Engine pEngine, String pISpecStr)       { super(pEngine, pISpecStr); }
	protected Inst_AbstractSimple(Engine pEngine, InstructionSpec pISpec) { super(pEngine, pISpec);    }
	
	/**{@inheritDoc}*/ @Override
	boolean checkSubExpression(int PCount, Expression[] pSubExpressions) {
		if(pSubExpressions != null)
			throw new IllegalArgumentException("Simple expressions cannot have sub-expression ("+this.getName()+")");
		return true;
	}
	
	/**{@inheritDoc}*/ @Override
	final protected Expression createExpression(int pCol, int pRow, Object[] pParameters, Expression[] pSubExpressions) {
		return this.newExprSimple(pCol, pRow, this, pParameters);
	}
	
	// Execution --------------------------------------------------------------
	/**{@inheritDoc}*/ @Override
	final protected Object run(Context pContext, Expression pExpr, Object[] pParams) {
		return this.run(pContext, pParams);
	}
	
	protected Object run(Context pContext, Object[] pParams) {
		return null;
	}
	
}