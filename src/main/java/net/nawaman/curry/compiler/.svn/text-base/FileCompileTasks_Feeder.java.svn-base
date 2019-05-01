package net.nawaman.curry.compiler;

import net.nawaman.compiler.TaskForFeeder;
import net.nawaman.curry.UnitBuilder;
import net.nawaman.task.TaskOptions;

/** FileCompileTasks for feeder */
public class FileCompileTasks_Feeder {
	
	/**
	 * FileCompileTaskFeeder for creating UnitBuilder
	 * 
	 * This task will create UnitBuilder for every code feeder.
	 **/
	static public class CreateUnitBuilder extends TaskForFeeder.Simple {
		
		public CreateUnitBuilder() {
			super("CreateUnitBuilder");
		}

		/** {@inheritDoc} */ @Override
		public Object[] doTask(net.nawaman.compiler.CompileProduct pContext,
				net.nawaman.compiler.TaskEntry pTE, TaskOptions pOptions, Object[] pIns) {
			CompileProduct $CProduct = (CompileProduct) pContext;

			// Obtain the UnitBuilderCreater
			Object             UBCO = $CProduct.getArbitraryData(CurryCompiler.DNUnitBuilderCreator);
			UnitBuilderCreator UBC  = (UBCO instanceof UnitBuilderCreator) ? (UnitBuilderCreator) UBCO : null;
			if (UBC == null) {
				UBC = new UnitBuilderCreator.UBCMemory();
				if (UBCO == null) pContext.setArbitraryData("UnitBuilderCreator", UBC);
			}

			// Create the unit
			UnitBuilder UB = UBC.createUnitBuilder($CProduct.getEngine(), $CProduct.CCompiler.TheID, pContext.getCurrentFeeder());

			// Save it for later task
			pContext.setFeederData(pContext.getCurrentFeederIndex(), CurryCompiler.DNUnitBuilder, UB);

			return null;
		}
	}

	/**
	 * FileCompileTaskFeeder for setting UnitBuilder to inactive
	 * 
	 * This tasks in-activates all UnitBuilder currently being compiled.
	 **/
	static public class InactivateUnitBuilder extends TaskForFeeder.Simple {
		public InactivateUnitBuilder() {
			super("InactivateUnitBuilder");
		}

		/** {@inheritDoc} */ @Override
		public Object[] doTask(net.nawaman.compiler.CompileProduct pContext, net.nawaman.compiler.TaskEntry pTE,
				TaskOptions pOptions, Object[] pIns) {
			CompileProduct $CProduct = (CompileProduct) pContext;

			// Obtain the UnitBuilderCreater
			Object      UBO = $CProduct.getFeederData($CProduct.getCurrentFeederIndex(), CurryCompiler.DNUnitBuilder);
			UnitBuilder UB  = (UBO instanceof UnitBuilder) ? (UnitBuilder) UBO : null;
			if (UB == null) {
				String FeederName = pContext.getCurrentFeederName();
				String CodeName = pContext.getCurrentCodeName();

				$CProduct.reportError(String.format(
						"Internal Error: Missing UnitBuilder for \'%s:%s\' <FileCompileTask_File:553>",
						FeederName, CodeName), null);
				return null;
			}
			UB.toInactive();
			return null;
		}
	}
	
	/**
	 * FileCompileTaskFeeder for saving UnitBuilder
	 * 
	 * This tasks save all UnitBuilder currently being compiled.
	 **/
	static public class SaveUnitBuilder extends TaskForFeeder.Simple {
		public SaveUnitBuilder() {
			super("SaveUnitBuilder");
		}

		/** {@inheritDoc} */ @Override
		public Object[] doTask(net.nawaman.compiler.CompileProduct pContext, net.nawaman.compiler.TaskEntry pTE,
				TaskOptions pOptions, Object[] pIns) {
			CompileProduct $CProduct = (CompileProduct) pContext;

			// Obtain the UnitBuilder
			Object      UBO = $CProduct.getFeederData($CProduct.getCurrentFeederIndex(), CurryCompiler.DNUnitBuilder);
			UnitBuilder UB  = (UBO instanceof UnitBuilder) ? (UnitBuilder) UBO : null;
			if (UB == null) {
				String FeederName = pContext.getCurrentFeederName();
				String CodeName = pContext.getCurrentCodeName();

				$CProduct.reportError(String.format(
						"Internal Error: Missing UnitBuilder for \'%s:%s\' <FileCompileTask_File:568>",
						FeederName, CodeName), null);
				return null;
			}
			if($CProduct.hasErrMessage()) UB.cancel();
			else                          UB.save();

			return null;
		}
	}
}
