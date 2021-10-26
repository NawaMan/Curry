package net.nawaman.curry.compiler;

import net.nawaman.compiler.*;
import net.nawaman.curry.*;
import net.nawaman.task.ProcessContext;
import net.nawaman.task.ProcessDatas;
import net.nawaman.task.TaskEntry;
import net.nawaman.task.TaskOptions;

/** Compiler for curry */
public class CurryCompiler extends net.nawaman.compiler.Compiler {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	/** Data Name for DNUnitBuilder        */ static public final String DNUnitBuilder        = "UnitBuilder";
	/** Data Name for DNUnitBuilderCreater */ static public final String DNUnitBuilderCreator = "UnitBuilderCreator";
	
	/** Data Name for Signature     */ static public final String DNSignature     = "Signature";
	/** Data Name for IsOwnerObject */ static public final String DNIsOwnerObject = "IsOwnerObject";
	/** Data Name for Owner Type    */ static public final String DNOwnerTypeRef  = "OwnerTypeRef";
	/** Data Name for Owner Package */ static public final String DNOwnerPackage  = "OwnerPackage";

	/** Constructs a CurryCompiler */
	public CurryCompiler(String pName, CurryLanguage pCLanguage, SecretID pSecrectID, TaskEntry ... pTEs) {
		super(pName, pTEs);
		if(pCLanguage == null) throw new NullPointerException("CurryCompiler cannot have a null language or engine.");
		this.TheLanguage = pCLanguage;
		this.TheID       = pSecrectID;
	}
	
	CurryLanguage TheLanguage;
	SecretID      TheID;
	
	/** Returns the curry engine this compiler will compile the code for */
	public CurryLanguage getCurryLanguage() {
		return this.TheLanguage;
	}
	
	/** Returns the curry engine this compiler will compile the code for */
	public Engine getEngine() {
		return this.TheLanguage.getTargetEngine();
	}
	
	/** Checks if the secret ID is match */
	public boolean isMacthID(Object pSecrectID) {
		return (this.TheID == pSecrectID) || ((this.TheID != null) && this.TheID.equals(pSecrectID));
	}

	/** Returns the reset value of the compile time checking */
	public CompileProduct.CompileTimeChecking getResetValueCompileTimeChecking() {
		return CompileProduct.CompileTimeChecking.Full;
	}
	
	/** Create a new CompileProduct to be used in the compilation */ @Override
	protected CompileProduct newCompileProduct(CodeFeeders pCodeFeeders, CompilationOptions pCCOptions) {
		CompileProduct CP = new CompileProduct(this, pCodeFeeders);
		
		if(pCCOptions instanceof CompilationOptions.Simple) {
			CompilationOptions.Simple COS = (CompilationOptions.Simple)pCCOptions;
			Object Temp;
			if((Temp = COS.getData(DNIsOwnerObject)) instanceof Boolean) CP.IsOwnerObject    = Boolean.TRUE.equals(Temp);
			if((Temp = COS.getData(DNIsOwnerObject)) instanceof TypeRef) CP.OwnerTypeRef     = (TypeRef)Temp;
			if((Temp = COS.getData(DNOwnerPackage))  instanceof String)  CP.OwnerPackageName = (String) Temp;
		}
		
		return CP;
	}

	/** Creates a new Compilation Option */
	public CompilationOptions newCompilationOptions(CurryCompilationOptions pOptions) {
		return this.newCompilationOptions(null, pOptions);
	}
	/** Creates a new Compilation Option */
	public CompilationOptions newCompilationOptions(ExecSignature pSignature, CurryCompilationOptions pOptions) {
		if(pOptions == null) pOptions = new CurryCompilationOptions();
		CompilationOptions.Simple CCOS = (CompilationOptions.Simple)pOptions.newCompilerCompilationOption();
		CCOS.setData(DNSignature, pSignature);
		CCOS.setData(DNIsOwnerObject, pOptions.IsOwnerObject);
		CCOS.setData(DNOwnerTypeRef,  pOptions.OwnerTypeRef);
		CCOS.setData(DNOwnerPackage,  pOptions.OwnerPackage);
		return CCOS;
	}
	
	/**{@inheritDoc}*/ @Override
	protected void notifyJustBeforeTask(TaskEntry TE, Object[] Ins, ProcessContext pContext, ProcessDatas pDatas,
			TaskOptions pOpts) {
		//System.out.println("Task#" + this.getTaskNumberInProgress());
	}
}
