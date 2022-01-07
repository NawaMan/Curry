package net.nawaman.curry.compiler;

import net.nawaman.compiler.TaskEntry;
import net.nawaman.compiler.TaskForCodeUsingRegParser;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.task.TaskOptions;

/** Tasks for compiling a code */
abstract public class CompileTask  extends TaskForCodeUsingRegParser {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	/** Constructs a CompileTask */
	protected CompileTask(String pName, ParserTypeProvider pTProvider) {
		super(pName, new Class<?>[] { ParseResult.class }, null, new Class<?>[] { Object.class });
		this.setTypeProvider(pTProvider);
	}

	/** Performs the task */ @Override
	public Object[] doTask(net.nawaman.compiler.CompileProduct pContext, TaskEntry pTE, TaskOptions pOptions, Object[] pIns) {
		// Prepare the context
		this.resetContext((CompileProduct) pContext);
		// Compile
		ParserType PT = this.getParserType();
		return new Object[] { PT.compile((ParseResult) pIns[0], null, pContext, this.getTypeProvider()) };
	}

	/** Reset the context */
	abstract void resetContext(CompileProduct $CProduct);
}
