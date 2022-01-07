package net.nawaman.curry.compiler;

import java.io.Serializable;

import net.nawaman.curry.Expression;
import net.nawaman.curry.ExternalContext;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.Coordinate;
import net.nawaman.regparser.result.ParseResult;

/**	Process a text - Implementation of this class must have a default constructor */
public interface TextProcessor extends Serializable {

	/** Returns the name of the TextProcessor */
	public String getName();
	
	/** Checks if this processor process the given language name for static text - Compiled to Expression */
	public boolean isProcessStaticText();

	/** Checks if this processor process the given language name for run-time text - Compiled to object */
	public boolean isProcessRuntimeText();

	/** Returns an expression for the text which can all be done at compile time */
	public Expression processStaticTextProcessor(String Text, Object[] Params, ParseResult $Result, String BodyParseEntryName,
			int[] LocationRC, int Pos, CompileProduct CProduct, ParserTypeProvider $TProvider);
	
	/** Returns an expression for the text which can all be done at compile time */
	public default Expression processStaticTextProcessor(String Text, Object[] Params, ParseResult $Result, String BodyParseEntryName,
			Coordinate LocationRC, int Pos, CompileProduct CProduct, ParserTypeProvider $TProvider) {
		return processStaticTextProcessor(Text, Params, $Result, BodyParseEntryName, LocationRC.toArray(), Pos, CProduct, $TProvider);
	}
	
	/** Process the text at runtime */
	public Object processRuntimeTextProcessor(String Text, Object Body, ExternalContext EC, Object[] Parameters);

}