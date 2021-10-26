package net.nawaman.curry.compiler;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Random;

import net.nawaman.compiler.Code;
import net.nawaman.compiler.CodeFeeder;
import net.nawaman.compiler.CodeFeeders;
import net.nawaman.compiler.CodeRef;
import net.nawaman.compiler.CompilationOptions;
import net.nawaman.compiler.PartialParseTask;
import net.nawaman.compiler.TaskEntry;
import net.nawaman.compiler.TokenParseTask;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Instructions_Executable;
import net.nawaman.curry.Location;
import net.nawaman.curry.Scope;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Executable.Fragment;
import net.nawaman.curry.compiler.CompileProduct.CompilationState;
import net.nawaman.curry.compiler.ExecutableCompileTasks.*;
import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;
import net.nawaman.util.UClass;

/**
 * CurryLanguage that utilizes Regular Parser
 *
 * The language take a PTypeProvide as a parameter and use to obtains necessary parser types for the compilation. The
 *   parser-type provider must contains at least two parser 'Expr' and 'Statements'. Another type 'File' must also be
 *   available if the language will compile source code file. See each section below for more information.
 *
 * <h2>Compilation of an expression</h2>
 * Compilation of an expression is done by the parser type named 'Expr'. The parser will parse the code that between
 *   'A.Offset' and 'A.EndPos' ('A.' refer to arbitrary data). Its compilation may returns a curry Expression or that 
 *   return object will be considered as literal. The parser will also be given necessary information via Arbitrary data
 *   (See more information in Compilation of an executable below). The parsing of Expression and Fragment is very  
 *   similar, in fact, the process of parsing expression is just parsing fragment. The only different is that expression
 *   is parse and compiled by 'Expr' but fragment is done by Statements.
 *
 * <h2>Compilation of an executable (a fragment, macro or subroutine)</h2>
 * Compilation of the executable is done by the parser type named 'Statements'. The parser will parse the code that
 *   starts with '{' and ends with '}' (which is the code between A.Offset and A.EndPos ('A.' refer to arbitrary data)). 
 *   Its compilation should returns a curry Expression. The parser will also be given necessary information such as the 
 *   executable signature (or null for fragments) (via the CompileProduct.getClosestSignature()). Others information is 
 *   given as arbitrary datas (via CompileProduct.getArbitraryData(...)). The following is the list of all information
 *   given to the parser.
 *   
 *   	<code>CurryCompiler.DNIsOwnerObject: boolean</code> 
 *   	  A flag indicates that the executable is owner by an object.
 *   	<code>CurryCompiler.DNOwnerTypeRef: net.nawaman.curry.TypeRef</code>
 *   	  The type of the owner of this executable or null if this executable is not owned by a type.
 *   	<code>CurryCompiler.DNOwnerPackage: net.nawaman.curry.Package</code>
 *   	  The package of the owner of this executable or null if the owner of this executable is not a part of package.
 *   	<code>CompilationOptions.DNGlobalScope: net.nawaman.curry.Scope</code>
 *   	  The global scope. It may use for compile type check or to extract the frozen variables
 *   	<code>CompilationOptions.DNTopScope: net.nawaman.curry.Scope</code>
 *   	  The scope just above this executable. It may use for compile type check or to extract the frozen variables
 *   	<code>CompilationOptions.DNFVNames: String[]</code>
 *   	  The list of the frozen variable names
 *   	<code>CompilationOptions.DNIsLocal: boolean</code>
 *   	  The flag indicating that this executable is local (so it is save to say for fragment and macro) that it can 
 *   	    access to local variable of the top scope. 
 *
 * <h2>Compilation of code files</h2>
 * Compilation of code files are much more complicated that the compilation of executable. So this is done by
 *   separating the compilation into 4 states.
 * 
 * 	State 1: Type Registration
 *		1. A: Parse the code file
 * 		2. A: Set the state to TypeRegistration.
 * 		3. F: UnitBuilder for each Feeder is created.
 * 		4. C: Let each code register its types with a rough specification.
 * 		      This is done by asking a parser type named 'File' to compile the entire code. The parser MUST detect that
 * 		        the compilation state is TypeRegistration and return FileCompileResult.TypeRegistration that contains
 * 		        list of types found in the code. The return type spec should not have any references to other types
 * 		        since those other types may not registered. Instead, the type spec returned in this state should only
 * 		        provide the information about the type kind and other non-type properties of the type.  
 * 
 * 	State 2: Type Refinition
 * 		1. A: Set the state to TypeRefinition.
 * 		2. C: Let each code redefine its types with more detail definition.
 * 		      This is done by asking a parser type named 'File' to compile the entire code. The parser MUST detect that
 * 		        the compilation state is TypeRefinition and return FileCompileResult.TypeRefinition that contains list 
 * 		        of types definition found in the code. At this state, all the information about the type excepts its
 * 		        elements must be provided.
 *		      The compiler should also check type relationships with other types in the aspect of their kinds. For
 *		        example, if a type is a class and it inherit another type. The second type must also be a type. This
 *		        verification will be in automatically later in the compilation process, however, the verification will
 * 		        be done without the reference to the code so the error report may not be very friendly to the end
 * 		        developers.
 * 
 * 	State 3: Structural Registration
 * 		1. A: Set the state to StructuralRegistration.
 * 		2. C: Let each code register its elements for both the types' and packge's with a rough specification.
 * 		      This is done by asking a parser type named 'File' to compile the entire code. The parser MUST detect that
 * 		        the compilation state is StructuralRegistration and return FileCompileResult_StructuralRegistration that
 * 		        contains list of package and type elements' appender found in the code.
 * 		3. F: Set all the PackageBuilder to be inactive.
 * 
 * 	State 4: Full Compilation
 * 		1. A: Set the state to FullCompilation.
 * 		2. C: Let each code compile the body of all functions/methods as well as all default values of variables/
 * 		        fields.
 * 		      This is done by asking a parser type named 'File' to compile the entire code. The parser MUST detect that 
 * 		        the compilation state is FullCompilation and return FileCompileResult.FullCompilation that contains list
 * 		        of package and type elements' resolver found in the code. At this state, parser of each type may fully
 * 		        verify the type specification.
 *		      The compiler can now safely verify the type definition constrains that involve its elements such as non-
 *		        abstract class should not have an abstract method. Even though this will be verified by the type kind
 *		        when the type is actually loaded, the verification will have no access to the code; thus, the error
 *		        report may not be friendly to the end developers.
 * 
 *  State 5: Type Validation
 *  	1. A: Set the state to TypeValidation
 *  	2. C: Fully initialize each type registed by each code.
 * 		3. F: Save all UnitBuilder
 * 		4. A: Set the state to Normal.
 * 
 **/
public class CLRegParser implements CurryLanguage {
	
	/** Name of the parser type for compiling expression (also used for eval) */
	static public final String ParserTypeName_Expression = "Expression";
	/** Name of the parser type for compiling statements (body of executables) */
	static public final String ParserTypeName_Statements = "Executable";
	/** Name of the parser type for compiling code file */
	static public final String ParserTypeName_File       = "File";
	
	/** Data Name for Parse Result */
	static public final String DNParseResult = "PResult";

	// Data for internal use -------------------------------------------------------------------------------------------
	
	static final Random Random = new Random();

	static String[] ParseInput        = new String[] { "Source" };
	static String[] PartialParseInput = new String[] { "Source", "A.Offset", "A.EndPos" };
	static String[] ParseOutput       = new String[] { DNParseResult };
	static String[] OutputExpr        = new String[] { "Expression"  };
	static String[] OutputFragment    = new String[] { "Fragment"    };
	static String[] OutputMacro       = new String[] { "Macro"       };
	static String[] OutputSubRoutine  = new String[] { "SubRoutine"  };
	static String[] OutputFile        = new String[] { DNParseResult, "F.Packages" };
	static String[] OutputInputFile   = new String[] { DNParseResult, "F.UnitBuilder", "C.PackageName", "C.Imports" };

	// Token (mostly for testing purposes)
	static public final String TokenTaskName = "Token";
	static String[] TokenParseInput   = new String[] { "A.ParserTypeName", "Source", "A.Offset", "A.EndPos" };
	static String[] TokenCompileInput = new String[] { "A.ParserTypeName", DNParseResult };
	static String[] OutputToken       = new String[] { "Token"  };
	
	/** Constructs the curry language that utilizes Regular Parser. **/
	public CLRegParser(String pName, Engine pTargetEngine, PTypeProvider pTProvider) {
		if(pTProvider == null) throw new NullPointerException();
		this.Name         = pName;
		this.TProvider    = pTProvider;
		this.TargetEngine = pTargetEngine;
		
		if((pTProvider.getType(ParserTypeName_Expression) == null) ||
		   (pTProvider.getType(ParserTypeName_Statements) == null))
			throw new IllegalArgumentException(String.format(
					"The parser type provide must contains the both '%s' and '%s' parser types.",
					ParserTypeName_Expression, ParserTypeName_Statements));
		
		// Token -------------------------------------------------------------------------------------------------------
		this.$TokenParser = new CurryCompiler(pName, this, this.TheSecretID, new TaskEntry[] {
				new TaskEntry(new TokenParseTask(  TokenTaskName, this.TProvider), TokenParseInput,   ParseOutput)});
		this.$TokenCompiler = new CurryCompiler(pName, this, this.TheSecretID, new TaskEntry[] {
				new TaskEntry(new TokenParseTask(  TokenTaskName, this.TProvider), TokenParseInput,   ParseOutput),
				new TaskEntry(new CompileTokenTask(TokenTaskName, this.TProvider), TokenCompileInput, OutputToken) });
		
		// Others compiler ---------------------------------------------------------------------------------------------
		this.$ExprCompiler = new CurryCompiler(pName, this, this.TheSecretID, new TaskEntry[] {
			new TaskEntry(new PartialParseTask(     ParserTypeName_Expression, this.TProvider), PartialParseInput, ParseOutput),
			new TaskEntry(new CompileFragmentTask(  ParserTypeName_Expression, this.TProvider), ParseOutput,       OutputExpr) });
		this.$FragmentCompiler = new CurryCompiler(pName, this, this.TheSecretID, new TaskEntry[] {
			new TaskEntry(new PartialParseTask(     ParserTypeName_Statements, this.TProvider), PartialParseInput, ParseOutput),
			new TaskEntry(new CompileFragmentTask(  ParserTypeName_Statements, this.TProvider), ParseOutput,       OutputFragment) });
		this.$MacroCompiler = new CurryCompiler(pName, this, this.TheSecretID, new TaskEntry[] {
			new TaskEntry(new PartialParseTask(     ParserTypeName_Statements, this.TProvider), PartialParseInput, ParseOutput),
			new TaskEntry(new CompileMacroTask(     ParserTypeName_Statements, this.TProvider), ParseOutput,       OutputMacro) });
		this.$SubRoutineCompiler = new CurryCompiler(pName, this, this.TheSecretID, new TaskEntry[] {
			new TaskEntry(new PartialParseTask(     ParserTypeName_Statements, this.TProvider), PartialParseInput, ParseOutput),
			new TaskEntry(new CompileSubRoutineTask(ParserTypeName_Statements, this.TProvider), ParseOutput,       OutputSubRoutine) });
		
		if(pTProvider.getType(ParserTypeName_Expression) != null) {
			this.$CodeFeederCompiler = new CurryCompiler(pName, this, this.TheSecretID,new TaskEntry[] {
				// State 1: Type Registration
				new TaskEntry(new FileParseTask(ParserTypeName_File, this.TProvider), ParseInput, ParseOutput),
				new TaskEntry(new FileCompileTasks_StateChange(CompilationState.TypeRegistration)),
				new TaskEntry(new FileCompileTasks_Feeder.CreateUnitBuilder()),
				new TaskEntry(new FileCompileTasks_Code.TypeRegistration(this.TProvider), OutputInputFile, OutputInputFile),
				
				// State 2: Type Refinition
				new TaskEntry(new FileCompileTasks_StateChange(CompilationState.TypeRefinition)),
				new TaskEntry(new FileCompileTasks_Code.TypeRefinition(this.TProvider), OutputInputFile, OutputInputFile),
				
				// State 3: Structural Registration
				new TaskEntry(new FileCompileTasks_StateChange(CompilationState.StructuralRegistration)),
				new TaskEntry(new FileCompileTasks_Code.StructureRegistration(this.TProvider), OutputInputFile, OutputInputFile),
				new TaskEntry(new FileCompileTasks_Feeder.InactivateUnitBuilder()),
				
				// State 4: Type Validation
				new TaskEntry(new FileCompileTasks_StateChange(CompilationState.TypeValidation)),
				new TaskEntry(new FileCompileTasks_Code.TypeValidation(this.TProvider), OutputInputFile, OutputInputFile),
				
				// State 5: Full Compilation
				new TaskEntry(new FileCompileTasks_StateChange(CompilationState.FullCompilation)),
				new TaskEntry(new FileCompileTasks_Code.FullCompilation(this.TProvider), OutputInputFile, OutputInputFile),
				new TaskEntry(new FileCompileTasks_Feeder.SaveUnitBuilder())
			});
		} else this.$CodeFeederCompiler = null;
	}

	// Name -------------------------------------------------------------------------
	final String Name;
	/** {@inheritDoc} */ @Override public String getName() {
		return this.Name;
	}
	// Engine -----------------------------------------------------------------------
	final Engine TargetEngine;
	/** {@inheritDoc} */ @Override
	public Engine getTargetEngine() {
		return this.TargetEngine;
	}
	// Parser -----------------------------------------------------------------------
	final PTypeProvider TProvider;
	/** Returns the Parser TypePackage */
	final public PTypeProvider getTProvider() {
		return this.TProvider;
	}
	
	/** Returns the ParserType with the name */
	final public PType getParserType(String pName) {
		return this.TProvider.getType(pName);
	}

	// Executable Creator -----------------------------------------------------------

	Hashtable<String, ExecutableCreator> ExecutableCreators = null;

	/** Add an executable creator by its name*/
	public boolean registerExecutableCreator(String pECClassName) {
		Class<?> Cls = "Java".equals(pECClassName)
		                   ? ExecutableCreator_Java.class
		                   : UClass.getClassByName(pECClassName);
		if(!ExecutableCreator.class.isAssignableFrom(Cls)) return false;
		return this.registerExecutableCreator(Cls.asSubclass(ExecutableCreator.class));
	}

	/** Add an executable creator */
	public boolean registerExecutableCreator(Class<? extends ExecutableCreator> pEC) {
		if(pEC == null) return false;
		try { return this.registerExecutableCreator(pEC.getConstructor().newInstance()); }
		catch (InstantiationException e)    { return false; }
		catch (IllegalAccessException e)    { return false; }
		catch (IllegalArgumentException e)  { return false; }
		catch (InvocationTargetException e) { return false; }
		catch (NoSuchMethodException e)     { return false; }
		catch (SecurityException e)         { return false; }
	}

	/** Add an executable creator */
	public boolean registerExecutableCreator(ExecutableCreator pEC) {
		if(pEC == null) return false;
		ExecutableCreator EC = this.getExecutableCreator(pEC.getName());
		if(EC != null) return false;
		if(this.ExecutableCreators == null) this.ExecutableCreators = new Hashtable<String, ExecutableCreator>();
		this.ExecutableCreators.put(pEC.getName(), pEC);
		return true;
	}
	
	/** Returns the executable creator that this language supports */
	public ExecutableCreator getExecutableCreator(String pECName) {
		if(this.ExecutableCreators == null) return null;
		return this.ExecutableCreators.get(pECName);
	}
	
	// TextProcessor --------------------------------------------------------------------

	Hashtable<String, TextProcessor> TextProcessors = null;

	/** Add a Text processor by its class name*/
	public boolean registerTextProcessor(String TPName) {
		Class<?> Cls = TextProcessor_StringFormat.Name.equals(TPName)
		                  ? TextProcessor_StringFormat.class
		                  : UClass.getClassByName(TPName);
		if(!TextProcessor.class.isAssignableFrom(Cls)) return false;
		return this.registerTextProcessor(Cls.asSubclass(TextProcessor.class));
	}

	/** Add a Text processor by its class name*/
	public boolean registerTextProcessor(String TPClassName, String TPName) {
		Class<?> Cls = TextProcessor_StringFormat.Name.equals(TPClassName)
		                  ? TextProcessor_StringFormat.class
		                  : UClass.getClassByName(TPClassName);
		if(!TextProcessor.class.isAssignableFrom(Cls)) return false;
		return this.registerTextProcessor(Cls.asSubclass(TextProcessor.class), TPName);
	}
	
	/** Add a Text processor */
	public boolean registerTextProcessor(Class<? extends TextProcessor> TPCls, String TPName) {
		if(TPCls == null) return false;
		try { return this.registerTextProcessor(UClass.newInstance(TPCls, new Object[] { TPName })); }
		catch (NoSuchMethodException     E) { return false; }
		catch (InstantiationException    E) { return false; }
		catch (IllegalAccessException    E) { return false; }
		catch (InvocationTargetException E) { throw new RuntimeException(E); }
	}

	/** Add a Text processor */
	public boolean registerTextProcessor(Class<? extends TextProcessor> TPCls) {
		if(TPCls == null) return false;
		try { return this.registerTextProcessor(TPCls.getConstructor().newInstance()); }
		catch (InstantiationException e)    { return false; }
		catch (IllegalAccessException e)    { return false; }
        catch (IllegalArgumentException e)  { return false; }
        catch (InvocationTargetException e) { return false; }
        catch (NoSuchMethodException e)     { return false; }
        catch (SecurityException e)         { return false; }
	}

	/** Add a Text processor  */
	public boolean registerTextProcessor(TextProcessor TP) {
		if(TP == null) return false;
		ExecutableCreator EC = this.getExecutableCreator(TP.getName());
		if(EC != null) return false;
		if(this.TextProcessors == null) this.TextProcessors = new Hashtable<String, TextProcessor>();
		this.TextProcessors.put(TP.getName(), TP);
		return true;
	}
	
	/** Returns the Text processor that this language supports */
	public TextProcessor getTextProcessor(String TPName) {
		if(this.TextProcessors == null) return null;
		return this.TextProcessors.get(TPName);
	}
	
	// Services -------------------------------------------------------------------------

	/** ID for compiler */
	final SecretID TheSecretID = new SecretID();

	/** Token parser */
	final CurryCompiler $TokenParser;
	/** Token parser compiler */
	final CurryCompiler $TokenCompiler;
	/** Expression parser compiler */
	final CurryCompiler $ExprCompiler;
	/** Fragment parser compiler */
	final CurryCompiler $FragmentCompiler;
	/** Macro parser compiler */
	final CurryCompiler $MacroCompiler;
	/** SubRoutine parser compiler */
	final CurryCompiler $SubRoutineCompiler;
	/** SubRoutine parser compiler */
	final CurryCompiler $CodeFeederCompiler;

	/**
	 * Parse any token.
	 * 
	 * This is used mostly for testing. It enables the parsing and compilation of any parser types of the language.
	 **/
	public ParseResult parseToken(String pTokenTypeName, String pCode, CompileProductContainer pCPContainer) {

		CurryCompiler $Compiler = this.$TokenParser;
		// Create inputs
		CodeFeeders               Inputs    = new CodeFeeders(new CodeFeeder.CFCharSequence("TheFeeder", "TheCode", pCode));
		CompilationOptions.Simple CCOptions = new CompilationOptions.Simple();
		CCOptions.setData(TokenParseInput[0].substring(TokenParseInput[0].indexOf('.') + 1), pTokenTypeName);
		// Compile
		CompileProduct $CProduct = (CompileProduct)$Compiler.compile(Inputs, CCOptions);
		return (ParseResult)$CProduct.getCodeData(new CodeRef.Simple(0, "TheCode"), DNParseResult);
	}
	
	/**
	 * Compile any token.
	 * 
	 * This is used mostly for testing. It enables the parsing and compilation of any parser types of the language.
	 **/
	public Object compileToken(String pTokenTypeName, String pCode, CompileProductContainer pCPContainer) {

		CurryCompiler $Compiler  = this.$TokenCompiler;
		String        ResultName = OutputToken[0];
		// Create inputs
		CodeFeeders               Inputs    = new CodeFeeders(new CodeFeeder.CFCharSequence("TheFeeder", "TheCode", pCode));
		CompilationOptions.Simple CCOptions = new CompilationOptions.Simple();
		CCOptions.setData(TokenParseInput[0].substring(TokenParseInput[0].indexOf('.') + 1), pTokenTypeName);
		// Compile
		CompileProduct $CProduct = (CompileProduct)$Compiler.compile(Inputs, CCOptions);
		Object         Result    = $CProduct.getCodeData(new CodeRef.Simple(0, "TheCode"), ResultName);

		// Save the product
		if(pCPContainer != null) pCPContainer.CProduct = $CProduct;

		// Has error
		if($CProduct.hasFatalErrMessage() || $CProduct.hasErrMessage()) {
			if (pCPContainer != null) return null;
			else throw new RuntimeException(String.format("There is a problem compiling %s:%s \n%s:",
						ResultName, "TheCode", $CProduct.toString()));
		}
		return Result;
	}
	
	// Others compilations (as Required by CurryLanguage) --------------------------------------------------------------

	/** Compile a raw expression (make return non-expression like 10) */
	protected Object complieRAW(boolean IsExpr, String pFeederName, String pCodeName, String pCode,
			CurryCompilationOptions pOptions, CompileProductContainer pCPContainer) {

		if ((pCodeName == null) || (pCodeName.length() == 0))
			pCodeName = CodeFeeder.UnknownCodeFeederName;

		CurryCompiler $Compiler  = IsExpr ? this.$ExprCompiler : this.$FragmentCompiler;
		String        ResultName = IsExpr ? OutputExpr[0]      : OutputFragment[0];
		// Create inputs
		CodeFeeders        Inputs    = new CodeFeeders(new CodeFeeder.CFCharSequence(pFeederName, pCodeName, pCode));
		CompilationOptions CCOptions = (pOptions == null) ? null : pOptions.newCompilerCompilationOption();
		CompileProduct     $CProduct = (CompileProduct)$Compiler.compile(Inputs, CCOptions);
		Object             Result    = $CProduct.getCodeData(new CodeRef.Simple(0,pCodeName), ResultName);

		// Save the product
		if(pCPContainer != null) pCPContainer.CProduct = $CProduct;

		// Has error
		if($CProduct.hasFatalErrMessage() || $CProduct.hasErrMessage()) {
			if (pCPContainer != null) return null;
			else throw new RuntimeException(String.format("There is a problem compiling %s:%s \n%s:",
						ResultName, pCodeName, $CProduct.toString()));
		}
		return Result;
	}
	
	// Evaluation ------------------------------------------------------------------------------------------------------

	/** {@inheritDoc} */ @Override
	public Object eval(String pCodeName, Scope pScope, String pCode, CompileProductContainer pCPContainer) {
		CurryCompilationOptions Options = new CurryCompilationOptions();
		Options.setTopScope(pScope);
		Options.toLocal();
		
		Object Result = this.complieRAW(true, null, pCodeName, pCode, Options, pCPContainer);
		if (!(Result instanceof Expression)) return Result;
		return this.getTargetEngine().execute(pScope, Result);
	}

	// Expression ------------------------------------------------------------------------------------------------------

	/** Compile an expression from the given code with the name in the given scope */
	public Expression compileExpression(String pCodeName, String pCode,
			CurryCompilationOptions pOptions, CompileProductContainer pCPContainer) {

		// Pre-process the parameters
		Scope    TScope  = null;
		String[] FVNames = null;
		if (pOptions != null) {
			TScope  = pOptions.TopScope;
			FVNames = (pOptions.FVNames == null) ? null : pOptions.FVNames.toArray(new String[pOptions.FVNames.size()]);
		}
		String CFName = (pOptions == null)?null:pOptions.CFName;
		// Compile
		Object Result = this.complieRAW(true, CFName, pCodeName, pCode, pOptions, pCPContainer);
		// Has no error so return the result
		Expression Expr = Expression.toExpr(Result);
		// Save the frozen variables by wrapping it with fragment
		if ((FVNames != null) && (FVNames.length != 0) && (TScope != null)) {
			Engine   $Engine  = this.getTargetEngine();
			Fragment Fragment = $Engine.getExecutableManager().newFragment(
								pCodeName, TKJava.TAny.getTypeRef(), new Location(null, pCodeName, 0, 0),
								null, Expr, FVNames, TScope);
			Expr = $Engine.getInstruction(Instructions_Executable.Inst_Run.Name).newExpression(Fragment);
		}
		return Expr;
	}
	
	// Fragment --------------------------------------------------------------------------------------------------------

	/** Compile a fragment body from the given code with the name in the given scope */
	public Executable.Fragment compileFragment(String pCodeName, String pCode, CurryCompilationOptions pOptions,
			CompileProductContainer pCPContainer) {
		// Pre-process the parameters
		int      Offset    = -1;
		Scope    TScope    = null;
		String[] FVNames   = null;
		MoreData ExtraData = null;
		if (pOptions != null) {
			Offset    = pOptions.Offset;
			TScope    = pOptions.TopScope;
			FVNames   = (pOptions.FVNames   == null) ? null : pOptions.FVNames.toArray(new String[pOptions.FVNames.size()]);
			ExtraData = (pOptions.ExtraData == null) ? null : pOptions.ExtraData.clone();
		}

		// Ensure parameters are in an invalid value
		if (Offset < 0) Offset = 0;
		else if((pCode != null) && (Offset >= pCode.length())) Offset = pCode.length();

		// Compile
		Object Result = this.complieRAW(false, null, pCodeName, pCode, pOptions, pCPContainer);
		// Make it serializable
		if (!(Result instanceof Serializable)) Result = Expression.newNonSerializableData(Result);
		int Col = 0;
		int Row = 0;
		if (pCPContainer != null) {
			Object C = (Code) pCPContainer.getCompileProduct().getCodeData(0, pCodeName, "Code");
			if (C instanceof Code) {
				Col = ((Code) C).getColOf(Offset);
				Row = ((Code) C).getRowOf(Offset);
			}
		}
		// Has no error so return the result
		return this.getTargetEngine().getExecutableManager().newFragment(pCodeName,
				TKJava.TAny.getTypeRef(),
				new Location(pCodeName, Col, Row),
				ExtraData,
				(Serializable) Result,
				FVNames, TScope);
	}

	// Macro -----------------------------------------------------------------------------------------------------------

	/** Compile a macro from the given code with the name in the given scope */
	public Executable.Macro compileMacro(ExecSignature pSignature, String pCode, CurryCompilationOptions pOptions,
			CompileProductContainer pCPContainer) {
		// Pre-process the parameters
		Scope    TScope  = null;
		String[] FVNames = null;
		if (pOptions != null) {
			TScope  = pOptions.TopScope;
			FVNames = (pOptions.FVNames == null) ? null : pOptions.FVNames.toArray(new String[pOptions.FVNames.size()]);
		}

		String CFName   = (pOptions == null)?null:pOptions.CFName;
		String CodeName = (pOptions == null)?""  :pOptions.CName;
		CodeName += "::" + pSignature.toString();
		// Create inputs
		CodeFeeders        Inputs    = new CodeFeeders(new CodeFeeder.CFCharSequence(CFName, CodeName, pCode));
		CompilationOptions CCOptions = this.$MacroCompiler.newCompilationOptions(pSignature, pOptions);
		CompileProduct     $CProduct = (CompileProduct) this.$MacroCompiler.compile(Inputs, CCOptions);
		Object             Result    = $CProduct.getCodeData(new CodeRef.Simple(0,CodeName), OutputMacro[0]);

		// Save the product
		if (pCPContainer != null) pCPContainer.CProduct = $CProduct;

		// Has error
		if ($CProduct.hasFatalErrMessage() || $CProduct.hasErrMessage()) {
			if (pCPContainer != null) return null;
			else throw new RuntimeException(String.format(
						"There is a problem compiling Macro:%s \n%s:",
						CodeName, $CProduct.toString()));
		}
		// Make it serializable
		if (!(Result instanceof Serializable)) Result = Expression.newNonSerializableData(Result);
		// Has no error so return the result
		return this.getTargetEngine().getExecutableManager().newMacro(pSignature, (Serializable) Result, FVNames, TScope);
	}

	// SubRoutine ------------------------------------------------------------------------------------------------------

	/** Compile a sub routine from the given code with the name in the given scope */
	public Executable.SubRoutine compileSubRoutine(ExecSignature pSignature, String pCode, CurryCompilationOptions pOptions,
			CompileProductContainer pCPContainer) {
		// Pre-process the parameters
		Scope    TScope  = null;
		String[] FVNames = null;
		if (pOptions != null) {
			TScope  = pOptions.TopScope;
			FVNames = (pOptions.FVNames == null) ? null : pOptions.FVNames.toArray(new String[pOptions.FVNames.size()]);
		}

		String CFName   = (pOptions == null)?null:pOptions.CFName;
		String CodeName = (pOptions == null)?""  :pOptions.CName;
		CodeName += "::" + pSignature.toString();
		// Create inputs
		CodeFeeders        Inputs    = new CodeFeeders(new CodeFeeder.CFCharSequence(CFName, CodeName, pCode));
		CompilationOptions CCOptions = this.$SubRoutineCompiler.newCompilationOptions(pSignature, pOptions);
		CompileProduct     $CProduct = (CompileProduct) this.$SubRoutineCompiler.compile(Inputs, CCOptions);
		Object             Result    = $CProduct.getCodeData(new CodeRef.Simple(0,CodeName), OutputSubRoutine[0]);

		// Save the product
		if (pCPContainer != null) pCPContainer.CProduct = $CProduct;

		// Has error
		if ($CProduct.hasFatalErrMessage() || $CProduct.hasErrMessage()) {
			if (pCPContainer != null) return null;
			else throw new RuntimeException(String.format(
						"There is a problem compiling SubRoutine:%s \n%s:",
						CodeName, $CProduct.toString()));
		}
		// Make it serializable
		if (!(Result instanceof Serializable)) Result = Expression.newNonSerializableData(Result);
		// Has no error so return the result
		return this.getTargetEngine().getExecutableManager().newSubRoutine(pSignature,(Serializable) Result, FVNames, TScope);
	}
	
	/** {@inheritDoc} */ @Override
	public CompileProduct compileFiles(CodeFeeders pCodeFeeders,
			CompilationOptions pOptions) {
		return (CompileProduct) this.$CodeFeederCompiler.compile(pCodeFeeders, pOptions);
	}
	
	/** {@inheritDoc} */ @Override
	public boolean equals(Object O) {
		if(!(O instanceof CLRegParser))                                       return false;
		if(!this.getName()        .equals(((CLRegParser)O).getName()))         return false;
		if(this.getTargetEngine() .equals(((CLRegParser)O).getTargetEngine())) return false;
		return this.getTProvider().equals(((CLRegParser)O).getTProvider());
	}
	
	/** {@inheritDoc} */ @Override
	public int hashCode() {
		return CLRegParser.class     .hashCode() +
		       this.getName()        .hashCode() +
		       this.getTargetEngine().hashCode() +
		       this.getTProvider()   .hashCode();
	}
}
