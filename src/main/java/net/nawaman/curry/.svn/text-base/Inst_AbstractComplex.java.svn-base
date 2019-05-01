package net.nawaman.curry;

/** A complex instruction (has both parameters and sub-expression) */
abstract public class Inst_AbstractComplex extends Instruction  {
	
	protected Inst_AbstractComplex(Engine pEngine, String pISpecStr)       { super(pEngine, pISpecStr); }
	protected Inst_AbstractComplex(Engine pEngine, InstructionSpec pISpec) { super(pEngine, pISpec);    }
	
	@Override final protected Expression createExpression(int pCol, int pRow, Object[] pParameters, Expression[] pSubExpressions) {
		return this.newExprComplex(pCol, pRow, this, pParameters, pSubExpressions);
	}
	
}