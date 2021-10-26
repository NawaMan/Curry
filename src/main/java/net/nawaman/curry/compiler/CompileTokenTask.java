package net.nawaman.curry.compiler;

import net.nawaman.compiler.TaskEntry;
import net.nawaman.compiler.TaskForCodeUsingRegParser;
import net.nawaman.curry.TKJava;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;
import net.nawaman.task.TaskOptions;

/** Task for compiling a language token */
public class CompileTokenTask  extends TaskForCodeUsingRegParser {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	/** Constructs a CompileTask */
	protected CompileTokenTask(String pName, PTypeProvider pTProvider) {
		super(pName, new Class<?>[] { String.class, ParseResult.class }, null, new Class<?>[] { Object.class });
		this.setTypeProvider(pTProvider);
	}

	/** Performs the task */ @Override
	public Object[] doTask(net.nawaman.compiler.CompileProduct pContext, TaskEntry pTE, TaskOptions pOptions, Object[] pIns) {
		CompileProduct $CProduct = (CompileProduct)pContext;
		// Prepare the context
		$CProduct.resetContextForFragment($CProduct.CCompiler.TheID, TKJava.TAny.getTypeRef(), false, null, null, null,
				null, null, true);
		// Compile
		PType PT = this.getTypeProvider().getType(pIns[0].toString());
		return new Object[] { PT.compile((ParseResult) pIns[1], null, pContext, this.getTypeProvider()) };
	}

}