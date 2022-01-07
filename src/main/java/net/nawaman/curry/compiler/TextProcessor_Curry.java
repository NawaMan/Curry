package net.nawaman.curry.compiler;

import java.util.Vector;

import net.nawaman.curry.Expression;
import net.nawaman.curry.ExternalContext;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Instructions_Array.Inst_NewArrayLiteral;
import net.nawaman.curry.Instructions_Context.Inst_NewConstant;
import net.nawaman.curry.util.FormattableAdaptor;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;

/** TextProcessor for curry code */
final public class TextProcessor_Curry implements TextProcessor {
    
    private static final long serialVersionUID = 8605071645446256069L;
    
	static public String Name = "c";

	String LangName;
	
	public TextProcessor_Curry() {
		this.LangName = Name;
	}
	public TextProcessor_Curry(String pLangName) {
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
			int[] LocationRC, int Pos, CompileProduct CProduct, ParserTypeProvider $TProvider) {
		
		// If the body is required 
		if($Result.textOf(BodyParseEntryName) == null) {
			CProduct.reportWarning(
				"Curry literal requries the body to process <TextProcessor_Curry:44>.",
				null, Pos
			);
		}
		
		// Set up parameter
		Expression Expr;
		try {
			MExecutable        ME = CProduct.getEngine().getExecutableManager();
			Vector<Expression> Es = new Vector<Expression>();
			Es.add(ME.newExpr(LocationRC, Inst_NewConstant.Name, "$Text",   ME.newType(LocationRC, TKJava.TString.getTypeRef()), Text));
			Es.add(ME.newExpr(LocationRC, Inst_NewConstant.Name, "$Params", ME.newType(LocationRC, TKArray.AnyArrayRef),         ME.newExpr(LocationRC, Inst_NewArrayLiteral.Name, ME.newType(LocationRC, TKJava.TAny.getTypeRef()), Params)));
			
			CProduct.newScope(null, TKJava.TAny.getTypeRef());
			CProduct.newConstant("$Text",   TKJava.TString.getTypeRef());
			CProduct.newConstant("$Params", TKArray.AnyArrayRef);
			
			// prepare the constant
			for(int i = 0; i < ((Params == null) ? 0 : Params.length); i++) {
				String  PName = "$"+i;
				TypeRef PType = CProduct.getReturnTypeRefOf(Params[i]);
				CProduct.newConstant(PName, PType);
				
				// Add the expression to create a new constant
				Es.add(ME.newExpr(LocationRC, Inst_NewConstant.Name, PName, ME.newType(LocationRC, PType), Params[i]));
			}
			
			// Add the body
			Expression[] Body = (Expression[])$Result.valueOf(BodyParseEntryName, $TProvider, CProduct);
			if     (Body.length == 1) Es.add(Body[0]);
			else if(Body.length != 0) Es.add(ME.newGroup(LocationRC, Body));
			
			// Create the expression
			Expr = ME.newStack(LocationRC, Es.toArray(new Expression[Es.size()]));
		} finally { CProduct.exitScope(); }
		
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
