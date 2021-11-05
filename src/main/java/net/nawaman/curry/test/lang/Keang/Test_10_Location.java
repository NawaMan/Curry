package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Location;
import net.nawaman.curry.Executable.SubRoutine;
import net.nawaman.curry.compiler.CompileProductContainer;
import net.nawaman.curry.compiler.CurryCompilationOptions;
import net.nawaman.curry.compiler.CurryLanguage;
import net.nawaman.curry.script.CurryEngine;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseParser;

public class Test_10_Location extends TestCaseParser {
	
	static public void main(String ... Args) { runTest(Args); }
	
	String ParaserTypeName = "Command";
	/**{@inheritDoc}*/ @Override protected String getParserTypeName() { return this.ParaserTypeName; }

	/**{@inheritDoc}*/ @Override
	protected void doTest(final String ... Args) {
		CurryEngine   CEngine   = CurryEngine.getEngine();
		Engine        $Engine   = CEngine.getTheEngine();
		CurryLanguage $Language = CEngine.getTheLanguage();
		
		AllTests.TheEngine   = $Engine;
		AllTests.TheLanguage = $Language;
		

		this.printSection("Code 1");
		
		CurryCompilationOptions Options = new CurryCompilationOptions();
		Options.setCodeName("Code1.curry");
		
		CompileProductContainer ProductContainer = new CompileProductContainer();
		
		SubRoutine SR = $Language.compileSubRoutine(
				ExecSignature.newEmptySignature("S1", new Location("Code1.curry", 0, 0), null),
				/* 00 */	"@@:Def macro factorial(I:int):int {\n"                +
				/* 01 */	"	@:println(`F(`+I+`):-----------------------`);\n" +
				/* 02 */	"	@:println($Context$.$Info$.CurrentStackTrace);\n" +
				/* 03 */	"	@:println();\n"                                   +
				/* 04 */	"	return (I <= 1) ? 1 : I * factorial(I - 1);\n"    +
				/* 05 */	"};\n"                                                +
				/* 06 */	"@:println(`1:------------------------------`);\n"    +
				/* 07 */	"@:println($Context$.$Info$.CurrentStackTrace);\n"    +
				/* 08 */	"@:println();\n"                                      +
				/* 09 */	"{\n"                                                 +
				/* 10 */	"	@:println(`2:------------------------------`);\n" +
				/* 11 */	"	@:println($Context$.$Info$.CurrentStackTrace);\n" +
				/* 12 */	"	@:println();\n"                                   +
				/* 13 */	"}\n"                                                 +
				/* 14 */	"@:println(`3:------------------------------`);\n"    +
				/* 15 */	"@:println($Context$.$Info$.CurrentStackTrace);\n"    +
				/* 16 */	"@:println();\n"                                      +
				/* 17 */	"\n"                                                  +
				/* 18 */	"@:println(factorial(4));\n"                          +
				/* 19 */	"@:println();\n"                                      +
				/* 20 */	"\n"                                                  +
				/* 21 */	"@:println(`4:------------------------------`);\n"    +
				/* 22 */	"@:println($Context$.$Info$.CurrentStackTrace);\n"    +
				/* 23 */	"@:println();\n"
				,
				Options, ProductContainer);
		
		this.printSubSection("Compile Product");
		
		this.IsProcessed = false;
		this.assertValue(ProductContainer.toString(),
			"CompileProduct ===================================================================================\n" +
			"CodeFeeder#0 => [Feeder] {\n" + 
	        "	Code: `Code1.curry::S1():any` => [PResult, Code, SubRoutine, Source]\n" +
			"}\n" +
	        "Arbitrary Datas => [OwnerPackage, GlobalScope, EndPos, CodeName, IsOwnerObject, IsLocal, TopScope, Imports, ExtraData, Offset, OwnerTypeRef, FVNames, Signature, CodeFeederName]\n" +
			"--------------------------------------------------------------------------------------------------\n" +
	        "Messages: 0 message(s)\n" +
			"=================================================================================================="
		);
		
		this.startCapture();
		$Engine.execute("call", SR);
		this.assertCaptured(
			"1:------------------------------\n"                                   +
			"\n"                                                                   +
			"	Code1.curry at CR( 10,  7) => S1():any\n"                          +
			"\n"                                                                   +
			"2:------------------------------\n"                                   +
			"\n"                                                                   +
			"	Code1.curry at CR( 11, 11) => S1():any\n"                          +
			"\n"                                                                   +
			"3:------------------------------\n"                                   +
			"\n"                                                                   +
			"	Code1.curry at CR( 10, 15) => S1():any\n"                          +
			"\n"                                                                   +
			"F(4):-----------------------\n"                                       +
			"\n"                                                                   +
			"	Code1.curry::S1():any at CR( 11,  2) => factorial(int):int\n"      +
			"	Code1.curry           at CR( 10, 18) => S1():any\n"                +
			"\n"                                                                   +
			"F(3):-----------------------\n"                                       +
			"\n"                                                                   +
			"	Code1.curry::S1():any at CR( 11,  2) => factorial(int):int\n"      +
			"	Code1.curry::S1():any at CR( 27,  4) => factorial(int):int\n"      +
			"	Code1.curry           at CR( 10, 18) => S1():any\n"                +
			"\n"                                                                   +
			"F(2):-----------------------\n"                                       +
			"\n"                                                                   +
			"	Code1.curry::S1():any at CR( 11,  2) => factorial(int):int\n"      +
			"	Code1.curry::S1():any at CR( 27,  4) => factorial(int):int\n"      +
			"	Code1.curry::S1():any at CR( 27,  4) => factorial(int):int\n"      +
			"	Code1.curry           at CR( 10, 18) => S1():any\n"                +
			"\n"                                                                   +
			"F(1):-----------------------\n"                                       +
			"\n"                                                                   +
			"	Code1.curry::S1():any at CR( 11,  2) => factorial(int):int\n"      +
			"	Code1.curry::S1():any at CR( 27,  4) => factorial(int):int\n"      +
			"	Code1.curry::S1():any at CR( 27,  4) => factorial(int):int\n"      +
			"	Code1.curry::S1():any at CR( 27,  4) => factorial(int):int\n"      +
			"	Code1.curry           at CR( 10, 18) => S1():any\n"                +
			"\n"                                                                   +
			"24\n"                                                                 +
			"\n"                                                                   +
			"4:------------------------------\n"                                   +
			"\n"                                                                   +
			"	Code1.curry at CR( 10, 22) => S1():any\n"                          +
			"\n"
		);
	}
}
