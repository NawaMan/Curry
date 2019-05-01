package net.nawaman.curry.compiler;

import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Scope;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TypeRef;
import net.nawaman.regparser.PTypeProvider;

public class ExecutableCompileTasks {

	/** Task for compiling a fragment body */
	static public class CompileFragmentTask extends CompileTask {
		/** Constructs a CompileFragmentTask */
		protected CompileFragmentTask(String pName, PTypeProvider pTProvider) {
			super(pName, pTProvider);
		}

		/** {@inheritDoc} */ @Override
		void resetContext(CompileProduct $CProduct) {
			// Prepare parameter
			Object  Temp;
			boolean  IsObject = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNIsOwnerObject)) instanceof  Boolean) ? (Boolean.TRUE.equals(Temp)): false;
			TypeRef  OTypeRef = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNOwnerTypeRef))  instanceof  TypeRef) ? (TypeRef)            Temp  :  null;
			String   OPackage = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNOwnerPackage))  instanceof   String) ? (String)             Temp  :  null;
			Scope    GScope   = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNGlobalScope))   instanceof    Scope) ? (Scope)              Temp  :  null;
			Scope    TScope   = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNTopScope))      instanceof    Scope) ? (Scope)              Temp  :  null;
			String[] FVNames  = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNFVNames))       instanceof String[]) ? (String[])           Temp  :  null;
			boolean  IsLocal  = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNIsLocal))       instanceof  Boolean) ? (Boolean.TRUE.equals(Temp)): false;
			// Default Package
			if(OPackage == null) OPackage = $CProduct.getEngine().getDefaultPackage().getName();
			// Reset
			$CProduct.resetContextForFragment($CProduct.CCompiler.TheID, TKJava.TAny.getTypeRef(),
					IsObject, OTypeRef, (OPackage == null) ? null : OPackage, GScope,TScope, FVNames, IsLocal);

			// Add Import
			$CProduct.addImport(((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNImports)) instanceof String[]) ? (String[]) Temp : null);
		}
	}
	
	/** Task for compiling a macro body */
	static public class CompileMacroTask extends CompileTask {
		/** Constructs a CompileMacroTask */
		protected CompileMacroTask(String pName, PTypeProvider pTProvider) {
			super(pName, pTProvider);
		}

		/** {@inheritDoc} */ @Override
		void resetContext(CompileProduct $CProduct) {
			// Prepare parameter
			Object        Temp;
			ExecSignature Signature = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNSignature))     instanceof ExecSignature) ? (ExecSignature)      Temp  :  null;
			TypeRef       OTypeRef  = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNOwnerTypeRef))  instanceof       TypeRef) ? (TypeRef)            Temp  :  null;
			String        OPackage  = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNOwnerPackage))  instanceof        String) ? (String)             Temp  :  null;
			boolean       IsObject  = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNIsOwnerObject)) instanceof       Boolean) ? (Boolean.TRUE.equals(Temp)): false;
			Scope         GScope    = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNGlobalScope))   instanceof         Scope) ? (Scope)              Temp  :  null;
			Scope         TScope    = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNTopScope))      instanceof         Scope) ? (Scope)              Temp  :  null;
			String[]      FVNames   = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNFVNames))       instanceof      String[]) ? (String[])           Temp  :  null;
			boolean       IsLocal   = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNIsLocal))       instanceof       Boolean) ? (Boolean.TRUE.equals(Temp)): false;
			// Default Package
			if (OPackage == null) OPackage = $CProduct.getEngine().getDefaultPackage().getName();
			// Reset
			$CProduct.resetContextForMacro($CProduct.CCompiler.TheID,
					Signature, IsObject, OTypeRef, (OPackage == null) ? null : OPackage,
					GScope, TScope, FVNames, IsLocal);

			// Add Import
			$CProduct.addImport(((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNImports)) instanceof String[]) ? (String[]) Temp : null);
		}
	}
	
	/** Task for compile subroutine body */
	static public class CompileSubRoutineTask extends CompileTask {
		/** Constructs a CompileSubRoutineTask */
		protected CompileSubRoutineTask(String pName, PTypeProvider pTProvider) {
			super(pName, pTProvider);
		}

		/** {@inheritDoc} */ @Override
		void resetContext(CompileProduct $CProduct) {
			// Prepare parameter
			Object Temp;
			ExecSignature Signature = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNSignature))     instanceof ExecSignature) ? (ExecSignature)      Temp   : null;
			boolean       IsObject  = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNIsOwnerObject)) instanceof       Boolean) ? (Boolean.TRUE.equals(Temp)) : false;
			TypeRef       OTypeRef  = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNOwnerTypeRef))  instanceof       TypeRef) ? (TypeRef)            Temp   : null;
			String        OPackage  = ((Temp = $CProduct.getArbitraryData(CurryCompiler.          DNOwnerPackage))  instanceof        String) ? (String)             Temp   : null;
			Scope         GScope    = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNGlobalScope))   instanceof         Scope) ? (Scope)              Temp   : null;
			Scope         TScope    = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNTopScope))      instanceof         Scope) ? (Scope)              Temp   : null;
			String[]      FVNames   = ((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNFVNames))       instanceof      String[]) ? (String[])           Temp   : null;
			// Default Package
			if(OPackage == null) OPackage = $CProduct.getEngine().getDefaultPackage().getName();
			// Reset
			$CProduct.resetContextForSubRoutine($CProduct.CCompiler.TheID,
					Signature, IsObject, OTypeRef, (OPackage == null) ? null : OPackage, GScope, TScope, FVNames);
			// Add Import
			$CProduct.addImport(((Temp = $CProduct.getArbitraryData(CurryCompilationOptions.DNImports)) instanceof String[]) ? (String[]) Temp : null);
		}
	}
}
