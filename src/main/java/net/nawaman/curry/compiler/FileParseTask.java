package net.nawaman.curry.compiler;

import static net.nawaman.regparser.RegParser.newRegParser;

import net.nawaman.compiler.CompileProduct;
import net.nawaman.compiler.ParseTask;
import net.nawaman.compiler.TaskEntry;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.task.TaskOptions;

public class FileParseTask extends ParseTask {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	/** Constructs a ParseTask */
	protected FileParseTask(String pName, ParserTypeProvider pTProvider) {
		super(pName);
		this.setTypeProvider(pTProvider);
	}

	/** Performs the task */ @Override
	public Object[] doTask(CompileProduct pContext, TaskEntry pTE, TaskOptions pOptions, Object[] pIns) {
		if(pIns[0] == null) return null;
		CharSequence Source = (pIns[0] instanceof CharSequence)?(CharSequence)pIns[0]:(pIns[0] == null)?"":pIns[0].toString();
		
		var Parser = newRegParser(
								"#File",	// Need and extra group to parse properly
								ParserTypeRef.of(this.getName(), pContext.getCurrentCodeName()).asRegParser()
							);
		ParseResult PR = Parser.parse(Source, this.getTypeProvider());
		if(PR == null) {
			pContext.reportFatalError("Unmatch!!", null);
			return null;
		}
		if(PR.endPosition() != Source.length()) {
			String S = Source.subSequence(PR.endPosition(), Source.length()).toString().trim();
			if(S.length() != 0) {
				((CompileProduct)pContext).reportError("Left over token <CompileTask:33> ", null, PR.endPosition());
				return null;
			}
		}
		return new Object[] { PR };
	}
}
