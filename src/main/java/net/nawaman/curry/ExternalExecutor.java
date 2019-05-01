package net.nawaman.curry;

import net.nawaman.curry.Executable.ExecKind;

/**
 * External Executor that will allow to be run in the curry scope 
 **/
public interface ExternalExecutor {
	
	/** Returns the interface of the operation ID */
	public ExecSignature getSignature(Object ID);
	
	/** Returns the executable kind of the operation ID */
	public ExecKind getExecKind(Object ID);
	
	/**
	 * Executing
	 * @param ID			an object that will used to indentify the operation to be executed
	 * @param SpecialParam	a parameter for the operation identifying or other use
	 * @param ExecParams	parameters of the execution
	 **/
	public Object execute(Context pContext, Object ID, Object SpecialParam, Object[] ExecParams);

}