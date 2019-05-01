package net.nawaman.curry.compiler;

import net.nawaman.curry.Expression;
import net.nawaman.curry.ExternalContext;
import net.nawaman.curry.Instructions_Operations.InstFormat;
import net.nawaman.curry.util.FormattableAdaptor;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;

/** TextProcessor for String format */
public class TextProcessor_StringFormat implements TextProcessor {

	static public String Name = "s";
	
	String LangName;
	
	public TextProcessor_StringFormat() {
		this.LangName = Name;
	}
	public TextProcessor_StringFormat(String pLangName) {
		this.LangName = pLangName;
	}
	
	/** Returns the name of the TextProcessor */
	public String getName() {
		return this.LangName;
	}
	
	/**{@inheritDoc}*/ @Override
	public boolean isProcessStaticText() {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	public boolean isProcessRuntimeText() {
		return true;
	}

	/**{@inheritDoc}*/ @Override
	public Expression processStaticTextProcessor(String Text, Object[] Params, ParseResult $Result, String BodyParseEntryName,
			int[] LocationRC, int Pos, CompileProduct CProduct, PTypeProvider $TProvider) {
		
		// If the body is not proccessed
		if($Result.textOf(BodyParseEntryName) != null) {
			CProduct.reportWarning(
				"String format literal does not process the body <TextProcessor_StringFormat:33>.",
				null, Pos
			);
		}
		
		// Prepare the parameters
		Object[] Ps = new Object[((Params == null) ? 0 : Params.length) + 1];
		Ps[0] = Text;
		System.arraycopy(Params, 0, Ps, 1, Ps.length - 1);
		
		// Create the expression
		Expression Expr = CProduct.getEngine().getExecutableManager().newExpr(LocationRC, InstFormat.Name, Ps);
		
		// Ensure its parameters
		if(!Expr.ensureParamCorrect(CProduct)) return null;
		return Expr;
	}
	
	/**{@inheritDoc}*/ @Override
	public Object processRuntimeTextProcessor(String Text, Object Body, ExternalContext EC, Object[] Parameters) {

		// If the body is not proccessed
		if(Body != null) {
			System.err.println("String format literal does not process the body <TextProcessor_StringFormat:57>.");
		}
		
		// Do the formating of the text
		return FormattableAdaptor.doFormat(EC.getEngine(), Text, Parameters);
	}
}
