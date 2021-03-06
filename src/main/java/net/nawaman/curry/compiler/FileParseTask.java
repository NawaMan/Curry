package net.nawaman.curry.compiler;

import net.nawaman.compiler.CompileProduct;
import net.nawaman.compiler.ParseTask;
import net.nawaman.compiler.TaskEntry;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.PTypeRef;
import net.nawaman.regparser.ParseResult;
import net.nawaman.regparser.RegParser;
import net.nawaman.task.TaskOptions;

public class FileParseTask extends ParseTask {
	/** Constructs a ParseTask */
	protected FileParseTask(String pName, PTypeProvider pTProvider) {
		super(pName);
		this.setTypeProvider(pTProvider);
	}

	/** Performs the task */ @Override
	public Object[] doTask(CompileProduct pContext, TaskEntry pTE, TaskOptions pOptions, Object[] pIns) {
		if(pIns[0] == null) return null;
		CharSequence Source = (pIns[0] instanceof CharSequence)?(CharSequence)pIns[0]:(pIns[0] == null)?"":pIns[0].toString();
		
		RegParser Parser = RegParser.newRegParser(
								"#File",	// Need and extra group to parse properly
								RegParser.newRegParser(
									new PTypeRef.Simple(this.getName(), pContext.getCurrentCodeName())
								)
							);
		ParseResult PR = Parser.parse(Source, this.getTypeProvider());
		if(PR == null) {
			pContext.reportFatalError("Unmatch!!", null);
			return null;
		}
		if(PR.getEndPosition() != Source.length()) {
			String S = Source.subSequence(PR.getEndPosition(), Source.length()).toString().trim();
			if(S.length() != 0) {
				((CompileProduct)pContext).reportError("Left over token <CompileTask:33> ", null, PR.getEndPosition());
				return null;
			}
		}
		return new Object[] { PR };
	}
}
