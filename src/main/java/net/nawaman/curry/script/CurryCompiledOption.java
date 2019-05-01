package net.nawaman.curry.script;

import net.nawaman.curry.compiler.CurryCompilationOptions;
import net.nawaman.script.CompileOption;

public class CurryCompiledOption extends CurryCompilationOptions implements CompileOption {
	
	static CurryCompiledOption DefaultOption = new CurryCompiledOption(); 
	
	/** Create a Java script option */
	public CurryCompiledOption() {}
	
}
