package net.nawaman.curry.compiler;

import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.ExternalContext;
import net.nawaman.curry.Scope;
import net.nawaman.curry.Executable.ExecKind;

public interface ExecutableCreator {
	
	public String getName();
	
	public Executable newExecutable(CompileProduct CProduct, ExternalContext EC, Engine pEngine, String pParam, ExecKind pKind,
			ExecSignature pSignature, String[] pFVNames, Scope pFrozen, String pCode);

}
