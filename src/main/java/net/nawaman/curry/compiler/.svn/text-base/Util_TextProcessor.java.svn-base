package net.nawaman.curry.compiler;

import net.nawaman.curry.Context;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Expression;
import net.nawaman.curry.ExternalContext;
import net.nawaman.curry.JavaExecutable;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.Instructions_Executable.Inst_Call;
import net.nawaman.regparser.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.util.UArray;

/** Utilities for text processor compilation */
public class Util_TextProcessor {
	
	/** Signature of text processor */
	static public final ExecSignature RuntimeTextProcessSignature =
	                                      ExecSignature.newSignature(
	                                         "processText",
	                                         new TypeRef[] { TKJava.TString.getTypeRef(), TKJava.TAny.getTypeRef() },
	                                         new String[]  { "$Text",                     "$Params"                },
	                                         true,
	                                         TKJava.TAny.getTypeRef(),
	                                         null,
	                                         null
	                                      );
	
	/** Class for Macro for Runtime Text processor */
	static public final class RuntimeTextProcessorMacro extends JavaExecutable.JavaMacro_Complex {
		public RuntimeTextProcessorMacro(ExecSignature ES, TextProcessor pTP, Object pBody) {
			super(ES);
			if(pTP == null) throw new NullPointerException("Null TextProcessor");
			
			this.TP   = pTP;
			this.Body = pBody;
		}
		TextProcessor TP;
		Object        Body;
		/**{@inheritDoc}*/ @Override
		protected Object run(Context pContext, Object[] pParams) {
			return TP.processRuntimeTextProcessor(
					(String)pParams[0],
					Body,
					new ExternalContext(pContext),
					UArray.getObjectArray(pParams[1])
				);
		}
	}
	
	/** Compile a text processor */
	static public Expression CompileTextProcessor(String $LangName, String $Text, Object $TextExpr, Object[] $Params,
			String BodyParseEntryName, ParseResult $Result, PTypePackage $TPackage, CompileProduct $CProduct) {
		
		if($LangName == null) $LangName = TextProcessor_Curry.Name;

		// Get the Text Processor
		final TextProcessor TP = ((CLRegParser)$CProduct.getCurryLanguage()).getTextProcessor($LangName);
		if(TP == null) {
			$CProduct.reportError(
				String.format(
					"Unknown TextProcessor language named '%s' <Util_TextProcessor:18>",
					$LangName
				),
				null,
				$Result.getStartPosition()
			);
			return null;
		}
		
		// Static -------------------------------------------------------------------------------------------
		if($Text != null) {
			// Checks if this processor support static text processing
			if(!TP.isProcessStaticText()) {
				$CProduct.reportError(
					String.format(
						"The TextProcessor language named '%s' does not process a static text processing<Util_TextProcessor:34>",
						$LangName
					),
					null,
					$Result.getStartPosition()
				);
				return null;
			}
			
			// Process the static text processor
			return TP.processStaticTextProcessor($Text, $Params, $Result, BodyParseEntryName, $Result.locationCROf(0), $Result.posOf(0),
					$CProduct, $TPackage
				);
			
		// Runtime ------------------------------------------------------------------------------------------
		} else {
			// Checks if this processor support static text processing
			if(!TP.isProcessRuntimeText()) {
				$CProduct.reportError(
					String.format(
						"The TextProcessor language named '%s' does not process a run-time text processing <Util_TextProcessor:50>",
						$LangName
					),
					null,
					$Result.getStartPosition()
				);
				return null;
			}
			
			final Object Body = $Result.valueOf("#Body", $TPackage, $CProduct);
			
			// Prepare the parameters
			Object[] Ps = new Object[(($Params == null) ? 0 : $Params.length) + 2];
			Ps[0] = new RuntimeTextProcessorMacro(RuntimeTextProcessSignature, TP, Body);
			Ps[1] = $TextExpr;
			System.arraycopy($Params, 0, Ps, 2, Ps.length - 2);

			// Process the runtime text processor
			return $CProduct.getEngine().getExecutableManager().newExpr(
					$Result.locationCROf(0),
					Inst_Call.Name,
					(Object[]) Ps
				);
		}
	}

}
