package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.Engine;
import net.nawaman.curry.Executable.Fragment;
import net.nawaman.curry.Executable.Macro;
import net.nawaman.curry.Executable.SubRoutine;
import net.nawaman.curry.compiler.CLRegParser;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_10_Executable extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		super.doTest(Args);
		
		Engine      $E =              AllTests.getEngine();
		CLRegParser CL = (CLRegParser)AllTests.getLanguage();
		Object      O  = null;
		Fragment    F  = null;
		Macro       M  = null;
		SubRoutine  S  = null;
		
		this.printSection("No parameter executable");
		
		this.printSubSection("Group");
		O = CL.compileToken("Exec_Body", "{ 11; }", null);
		this.assertValue(O, 11);
		
		O = CL.compileToken("Exec_Body", "@@Java:{ return 12; }:Java:", null);
		this.assertValue($E.execute(O), 12);
		
		this.printSubSection("Fragment");
		F = (Fragment)$E.execute(CL.compileToken("Atomic_Executable", "@@:Def fragment F():int { 17; }", null));
		this.assertValue(F, "Fragment F(...){...}");
		this.assertValue($E.execute("run", F), 17);
		
		this.printSubSection("Macro");
		M = (Macro)$E.execute(CL.compileToken("Atomic_Executable", "@@:Def macro M():int { 18; }", null));
		this.assertValue(M, "Macro M(...){...}");
		this.assertValue($E.execute("exec", M), 18);

		this.printSubSection("SubRoutine");
		S = (SubRoutine)$E.execute(CL.compileToken("Atomic_Executable", "@@:Def sub   S():int { 19; }", null));
		this.assertValue(S, "SubRoutine S(...){...}");
		this.assertValue($E.execute("call", S), 19);
		
		this.printSection("Parametered executable");
		
		this.printSubSection("Macro");
		M = (Macro)$E.execute(CL.compileToken("Atomic_Executable", "@@:Def macro M(I:int):int { @:plus(@:getVarValue(`I`), 20); }", null));
		this.assertValue(M, "Macro M(...){...}");
		this.assertValue($E.execute("exec", M, 20), 40);

		this.printSubSection("SubRoutine");
		S = (SubRoutine)$E.execute(CL.compileToken("Atomic_Executable", "@@:Def sub   S(I:int):int { @:plus(@:getVarValue(`I`), 21); }", null));
		this.assertValue(S, "SubRoutine S(...){...}");
		this.assertValue($E.execute("call", S, 20), 41);
		
		this.printSection("Java executable");
		
		this.printSubSection("Macro");
		M = (Macro)$E.execute(CL.compileToken("Atomic_Executable", "@@:Def macro M(I:int):int @@Java:{ return I + 22; }:Java:", null));
		this.assertValue(M, "Macro M(...){...}");
		this.assertValue($E.execute("exec", M, 20), 42);

		this.printSubSection("SubRoutine");
		S = (SubRoutine)$E.execute(CL.compileToken("Atomic_Executable", "@@:Def sub   S(I:int):int @@Java:{ return I + 23; }:Java:", null));
		this.assertValue(S, "SubRoutine S(...){...}");
		this.assertValue($E.execute("call", S, 20), 43);

		this.printSection("Group in Stack");
		this.assertValue("@:if(true)  {                            @:println( `T` );                                     @:println( `F` );          };",  true, "T\n");
		this.assertValue("@:if(false) {                            @:println( `T` );                                     @:println( `F` );          };", false, "F\n");
		this.assertValue("@:if(true)  { @@:Group        {          @:println( `T` ); };       @@:Group        {          @:println( `F` ); };       };",  true, "T\n");
		this.assertValue("@:if(false) { @@:Group        {          @:println( `T` ); };       @@:Group        {          @:println( `F` ); };       };", false, "F\n");
		this.assertValue("@:if(true)  { @@:Group @@Java:{ System.out.println(\"T\"); }:Java:; @@:Group @@Java:{ System.out.println(\"F\"); }:Java:; };",  true, "T\n");
		this.assertValue("@:if(false) { @@:Group @@Java:{ System.out.println(\"T\"); }:Java:; @@:Group @@Java:{ System.out.println(\"F\"); }:Java:; };", false, "F\n");
		
		this.printSection("Local executable");
		this.assertValue(
			"@:newVariable(`Factor`,    int.type,     10);\n"+
			"@:newVariable(`Separator`, String.type, ` & `);\n"+
			"@@:Def sub getSeparator[Separator]():String {\n" +
				"@:return(@:getVarValue(`Separator`));\n" +
			"};\n"+ 
			"@@:Def macro factorial(I:int):int {\n"+
				"@:return(\n"+
					"@@:Choose(null, @:lessThanEqual(@:getVarValue(`I`), 1)) {\n"+
						"@@:Case(true) { 1; }\n"+
						"@@:Default {\n"+
							"@:multiply(\n"+
								"@:getVarValue(`I`),\n"+
								"@:exec(@:getVarValue(`factorial`), @:subtract(@:getVarValue(`I`), 1)),\n"+
								"@:getVarValue(`Factor`)\n"+
							");\n"+
						"}\n"+
					"}\n"+
				");\n"+
			"};\n"+
			"@:newVariable(`S`, String.type, ``);\n"+
			"@@:Def fragment doFactorial():void {\n"+
				"@:assignment(@#LocalVariable#@, @#AppendTo#@, @:toString(@:exec(@:getVarValue(`factorial`), 5)), `S`);\n"+
				"null;\n"+
			"};\n"+
			"@:run(@:getVarValue(`doFactorial`));\n"+ 
			"@:assignment(@#LocalVariable#@, @#AppendTo#@, @:toString(@:call(@:getVarValue(`getSeparator`))), `S`);\n"+
			"@:run(@:getVarValue(`doFactorial`));\n"+
			"@:return(@:getVarValue(`S`));\n"
			,
			"1200000 & 1200000"
		);
		
		this.assertValue(
				"@@:Def fragment ensureI():void {"+
					"@:if(@:isVarExist(`I`)) {" +
						"// Continue when I is odd. \n"+
						"@:if(@:equals(@:modulus(#:toInt(#:getVarValue(`I`)), 2), 1)) {" +
							"#:continue(null, null);" +
							"@:if(@:moreThanEqual(#:toInt(#:getVarValue(`I`)), 10)) {" +
								"#:stop(null, null);" +
							"};" +
						"};"+
					"};"+
					"null;"+
				"};"+
				"@:newVariable(`S`, String.type, ``);"+
				"@:fromTo(null, `I`, int.type, 0, 20, 1) {"+
					"@:assignment(@#LocalVariable#@, @#AppendTo#@, @:format(`<%s`, @:getVarValue(`I`)), `S`);"+
					"@:run_Unsafe(@:getVarValue(`ensureI`));"+
					"@:assignment(@#LocalVariable#@, @#AppendTo#@, @:format(`-%s>`, @:getVarValue(`I`)), `S`);"+
				"};"+
				"@:return(@:getVarValue(`S`));"
				,
				"<0-0><1<2-2><3<4-4><5<6-6><7<8-8><9<10"
			);
		
		// Closure carry its own context while Macro just run on whatever context is.
		// From this this variables accessed by Closure are those exists by the time the closure is created.
		this.assertValue(
			"@:newVariable(`S`,        String.type, ``     );"+
			"@:newVariable(`Location`, String.type, `O`    );"+
			"@:newVariable(`Greeting`, String.type, `Hello`);"+
			"@@:Def macro   sayM():any { @:assignment(@#LocalVariable#@, @#AppendTo#@, @:format(` M{%s.%s}`, @:getVarValue(`Location`), @:getVarValue(`Greeting`)), `S`); };"+
			"@@:Def closure sayC():any { @:assignment(@#LocalVariable#@, @#AppendTo#@, @:format(` C{%s.%s}`, @:getVarValue(`Location`), @:getVarValue(`Greeting`)), `S`); };"+
			"@:exec(@:getVarValue(`sayM`));"+
			"@:exec(@:getVarValue(`sayC`));"+
			"@:assignment(@#LocalVariable#@, @#AppendTo#@, ` -=- `, `S`);"+
			"@:stack(null) {"+
				"@:newVariable(`Location`, String.type, `I`  );"+
				"@:newVariable(`Greeting`, String.type, `Hey`);"+
				"@:exec(@:getVarValue(`sayM`));"+
				"@:exec(@:getVarValue(`sayC`));"+
			"};"+
			"@:assignment(@#LocalVariable#@, @#AppendTo#@, ` -=- `, `S`);"+
			"@:setVarValue(`Greeting`, `Hi`  );"+
			"@:exec(@:getVarValue(`sayM`));"+
			"@:exec(@:getVarValue(`sayC`));"+
			"@:return(@:getVarValue(`S`));"
			,
			" M{O.Hello} C{O.Hello} -=-  M{I.Hey} C{O.Hello} -=-  M{O.Hi} C{O.Hi}"
		);
		
		this.printSubSection("Executable as character.");
		this.assertValue(
			"@@:Def sub selectCapitals(Charable:Variant:<String|Executable:<(int):char>>):String {\n" +
			"	// Early return\n" +
			"	@:if(@:isNull(@:getVarValue(`Charable`))) { @:return(``); };\n" +
			"	@:newConstant(`SB`, StringBuffer.type, @:newInstance(StringBuffer.type));\n" +
			"	@:newVariable(`c`,  char        .type,                             null);\n" +
			"	@:newVariable(`i`,  int         .type,                                0);\n" +
			"	@:stack(null) {\n" +
			"		@:while(null, @@:Expr ( true )) {\n" +
			"			@:if( @:instanceOf(String.type, @:getVarValue(`Charable`))) {\n" +
			"				@:if( @:moreThanEqual(@:getVarValue(`i`), @:length((:String ? @:getVarValue(`Charable`))))) {\n" +
			"					@:stop(null, null);\n" +
			"					@:setVarValue(`c`, @:charAt((:String? @:getVarValue(`Charable`) ), @:getVarValue(`i`)));\n" +
			"				};\n" +
			"				@:stack(null) {\n" +
			"					@:setVarValue(`c`, @:call  ((:Executable:<(int):char> ? @:getVarValue(`Charable`)), @:getVarValue(`i`)));\n" +
			"					@:if( @:isNull(@:getVarValue(`c`)) ) { @:stop(null, null); };\n" +
			"				};\n" +
			"			};\n" +
			"			@:assignment(@#LocalVariable#@, @#IncBefore#@, 1, `i`);\n" +
			"			@:if( @:lessThanEqual(@:charToInt('A'), @:getVarValue(`c`), @:charToInt('Z')) ) {\n"+
			"				@:invokeByParams(@:getVarValue(`SB`), `append`, @:getVarValue(`c`));\n" +
			"			};\n" +
			"		};\n" +
			"	};\n" +
			"	@:return(@:toString(@:getVarValue(`SB`)));\n" +
			"};\n" +
			"@:println(@:exec(@:getVarValue(`selectCapitals`), `Cola is so a reindeer`));\n" +
			"@:newConstant(`getASCII127`, Executable:<sub (int):char>.type,\n" +
			"	@@:New sub (I:int):char {\n" +
			"		@:if( @:moreThan(@:getVarValue(`I`), 127)) { @:return(null); };\n" +
			"		@:intToChar(@:getVarValue(`I`));\n" +
			"	}\n" +
			");\n" +
			"@:println(@:exec(@:getVarValue(`selectCapitals`), @:getVarValue(`getASCII127`)));\n" +
			"`Success`;\n",
			"Success",
			"C\nABCDEFGHIJKLMNOPQRSTUVWXYZ\n"
		);

		this.printSection("To be continue ... ");
					
		this.printSection("End");
	}
}