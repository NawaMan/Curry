package net.nawaman.curry.compiler;

import net.nawaman.compiler.TaskForCompiler;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.UnitBuilder;
import net.nawaman.curry.compiler.CompileProduct.CompilationState;
import net.nawaman.task.TaskOptions;

/**
 * FileCompileTask StateChange
 * 
 * Change the state so that compiling files can be done properly.
 **/
public class FileCompileTasks_StateChange extends TaskForCompiler.Simple {
    
    private static final long serialVersionUID = 8605071645446256069L;
	
	public FileCompileTasks_StateChange(CompilationState pCState) {
		super("FCTStateChange_" + pCState.toString());
		this.CState = pCState;
	}

	CompilationState CState = null;

	/** {@inheritDoc} */ @Override
	public Object[] doTask(net.nawaman.compiler.CompileProduct pContext,
			net.nawaman.compiler.TaskEntry pTE, TaskOptions pOptions, Object[] pIns) {

		// Change the state
		CompileProduct $CProduct = (CompileProduct) pContext;
		$CProduct.setCompilationState(this.CState, $CProduct.CCompiler.TheID);

		// Try to reset the Pacakge Builder -------------------------------------------------------
		
		// Obtain the UnitBuilderCreater
		Object      UBO = $CProduct.getFeederData($CProduct.getCurrentFeederIndex(), CurryCompiler.DNUnitBuilder);
		UnitBuilder UB  = (UBO instanceof UnitBuilder) ? (UnitBuilder) UBO : null;
		if (UB == null) return null;
		
		// Reset UnitBuilder
		int PBCount = UB.getPackageBuilderCount();
		for(int i = PBCount; --i >= 0; ) {
			PackageBuilder PB = UB.getPackageBuilder(i);
			if(PB == null) continue;
			PB.resetForCompilation();
		}
		
		UB.toInactive();
		
		return null;
	}
}