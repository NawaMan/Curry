package net.nawaman.curry.compiler;

import java.io.File;
import java.util.HashMap;

import net.nawaman.compiler.CodeFeeders;
import net.nawaman.compiler.CompilationOptions;
import net.nawaman.curry.Engine;
import net.nawaman.curry.EngineExtension;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Scope;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.typepackage.PTypePackage;
import net.nawaman.script.Function;
import net.nawaman.script.ScriptManager;
import net.nawaman.util.UString;

/** Language of Curry */
public interface CurryLanguage {
		
	/** The file extension for language spec */
	static public final String LANGUAGE_SPEC_FILE_EXTENSION     = "cls";
	/** Name of the default language */
	static public final String DEFAULT_LANGUAGE_NAME            = "Curry";
	/** The file name for default Language spec file */
	static public final String DEFAULT_LANGUAGE_SPEC_FILE_NAME  = DEFAULT_LANGUAGE_NAME + "." + LANGUAGE_SPEC_FILE_EXTENSION;
	/** Name of the default language */
	static public final String DEFAULT_LANGUAGE_PARSERNAME_NAME = "CurryCompiler";

	/** Returns the name of the language */
	public String getName();

	/** Returns the target engine */
	public Engine getTargetEngine();

	/** Evaluate the given code with the name in the given scope */
	public Object eval(String pCodeName, Scope pScope, String pCode, CompileProductContainer pCPContainer);

	/** Compile an expression from the given code with the name in the given scope */
	public Expression            compileExpression(String pCodeName,        String pCode, CurryCompilationOptions pOptions, CompileProductContainer pCPContainer);
	/** Compile a code fragment from the given code with the name in the given scope */
	public Executable.Fragment   compileFragment(  String pCodeName,        String pCode, CurryCompilationOptions pOptions, CompileProductContainer pCPContainer);
	/** Compile a macro from the given code with the name in the given scope */
	public Executable.Macro      compileMacro(     ExecSignature pSignture, String pCode, CurryCompilationOptions pOptions, CompileProductContainer pCPContainer);
	/** Compile a sub routine from the given code with the name in the given scope */
	public Executable.SubRoutine compileSubRoutine(ExecSignature pSignture, String pCode, CurryCompilationOptions pOptions, CompileProductContainer pCPContainer);

	/** Compile the given code feeder */
	public CompileProduct compileFiles(CodeFeeders pCodeFeeders, CompilationOptions pOptions);

	/** Returns the executable creator that this language supports */
	public ExecutableCreator getExecutableCreator(String pECName);
	
	// Utility class ---------------------------------------------------------------------------------------------------

	static public class Util {
		
		static HashMap<String, GetCurryLanguage> GetCurryLanguages = new HashMap<String, GetCurryLanguage>();
		static HashMap<String, CurryLanguage>    CurryLanguages    = new HashMap<String, CurryLanguage>();

		/** Returns the default CurryLanguage */
		static public CurryLanguage GetDefaultCurryLanguage() {
			return GetCurryLanguage(null, null, false);
		}

		/** Returns the CurryLanguage loaded from an LSFileName (relative to current dir). The file have ".cls" extension. */
		static public CurryLanguage GetCurryLanguage(String LSFileName, String ESFileName) {
			return GetCurryLanguage(LSFileName, ESFileName, false);
		}
		
		static public void ClearCachedLanguages() {
		    GetCurryLanguages.clear();
		    CurryLanguages.clear();
		}

		/** Returns the CurryLanguage loaded from an LSFileName (relative to current dir). The file have ".cls" extension. */
		static public CurryLanguage GetCurryLanguage(String LSFileName, String ESFileName, boolean IsForceCreate) {
			if(LSFileName == null) LSFileName = DEFAULT_LANGUAGE_NAME;			
			
			CurryLanguage CL     = null;
			String        CLName = LSFileName + ((ESFileName == null) ? "" : ":" + ESFileName);
			
			// Try to get from the cache
			if(!IsForceCreate && ((CL = CurryLanguages.get(CLName)) != null))
				return CL;
			
			GetCurryLanguage GCL = GetGetCurryLanguage(LSFileName);
			if(GCL == null) return null;
			
			// Gets the CurryLanguage
			CL = GCL.getCurryLanguage(LSFileName, ESFileName);
			
			// Set this language as the default language if not yet set
			EngineExtension EE = CL.getTargetEngine().getExtension(EE_Language.Name);
			if((EE instanceof EE_Language) && (((EE_Language)EE).Language == null))
				((EE_Language)EE).Language = CL;

			// Add if not there
			if(!CurryLanguages.containsKey(CLName)) CurryLanguages.put(LSFileName, CL);
			
			return CL;
		}
		
		/**
		 * Construct a GetCurryLanguage by loading it from an LSFileName (relative to current dir).
		 * The file have ".cls" extension.
		 **/
		static public GetCurryLanguage GetGetCurryLanguage(String LSFileName) {
			
			if(LSFileName == null) LSFileName = DEFAULT_LANGUAGE_NAME;

			GetCurryLanguage GetCurryLanguage = null;
			
			GetCurryLanguage = GetCurryLanguages.get(LSFileName);
			if(GetCurryLanguage != null) return GetCurryLanguage;
			
			PTypePackage.EnsureEngineRegisted();
			
			// Default engine - When the name is "Curry" and the file is not found
			if(LSFileName.equals(DEFAULT_LANGUAGE_NAME) || LSFileName.equals(DEFAULT_LANGUAGE_PARSERNAME_NAME)) {
				PTypePackage P = null;
				
				try { P = PTypePackage.Use(DEFAULT_LANGUAGE_PARSERNAME_NAME); }
				catch (Exception E) { return null; }
				
				final PTypePackage Parser = P;
				
				if(Parser == null) {
					String Message = "Unable to load Parser file `"+DEFAULT_LANGUAGE_PARSERNAME_NAME+"`. Ensure that " +
					                 "it is in the class path";
					System.err.println(Message);
					throw new RuntimeException(Message);
				}
				
				// Engine with default spec and all known Extensions
				GetCurryLanguage = 
					new GetCurryLanguage() {
						public CurryLanguage getCurryLanguage(String LangName, String EngineName) {
							CLRegParser CL = new CLRegParser(DEFAULT_LANGUAGE_NAME, Engine.loadEngine(), Parser);
							CL.registerExecutableCreator(new ExecutableCreator_Java());
							CL.registerTextProcessor(    new TextProcessor_StringFormat("f"));
							CL.registerTextProcessor(    new TextProcessor_StringFormat("format"));
							CL.registerTextProcessor(    new TextProcessor_Curry("c"));
							CL.registerTextProcessor(    new TextProcessor_Curry("curry"));
							return CL;
						}
					};
					
			} else {
				ParserTypeProvider TPackage = null;
				try { 
					Function F = (Function)ScriptManager.Use("LanguageSpec");
					TPackage = (PTypePackage)F.run();
				} catch(Exception E) {
					String Message =
						"There is a problem loading the LanguageSpec Parser/Compiler please ensure that `LanguageSpec.tpt` is " +
						"available in the class path";
					System.err.println(Message + ": \n" + E);
					throw new RuntimeException(Message, E);
				}
				
				try {
					CompilationContext CC = new CompilationContext.Simple();
					
					if(!(new File(LSFileName)).exists()) {
						String FileName = LSFileName + "." + LANGUAGE_SPEC_FILE_EXTENSION;
						if(!(new File(FileName)).exists()) {
							String Message =
								"The curry language parse file `"+LSFileName+"` is not found. Parse files are not " +
								"searched as part of Usepath but using direct path reference (relative to working " +
								"directory)";
							throw new RuntimeException(Message);
						}
						LSFileName = FileName;
					}
					
					String ESpec = UString.loadTextFile(LSFileName);
					ParserType PT = TPackage.type("LanguageSpecFile");
					GetCurryLanguage = (GetCurryLanguage)PT.compile(ESpec, LSFileName, CC, TPackage);

					
				} catch (Exception E) {
					System.err.println(E);
					throw new RuntimeException(E);
				}
			}
			
			GetCurryLanguages.put(LSFileName, GetCurryLanguage);
			
			return GetCurryLanguage;
		}
	}
}
