package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseParser;

public class Test_01_Literals extends TestCaseParser {
	
	static public void main(String ... Args) { runTest(Args); }
	
	String ParaserTypeName = "Command";
	
	/**{@inheritDoc}*/ @Override
	protected String getParserTypeName() {
		return this.ParaserTypeName;
	}

	/**{@inheritDoc}*/ @Override
	protected void doTest(final String ... Args) {
		this.printSection("Cast");
		this.assertValue("(byte)-5",   (byte)-5);
		this.assertValue("((byte)-5)", (byte)-5);
		
		this.printSection("Meta");
		this.assertValue("java.io.File.class", java.io.File.class);
		
		this.printSection("New");
		this.assertValue("new StringBuilder(`Str`)", new StringBuilder("Str"));
		this.assertValue("new java.io.File( `.`)",   new java.io.File("."));
		
		this.printSection("Info");
		this.assertValue("$Engine$.$Info$.EngineName",          "TestEngine");
		this.assertValue("$Engine$.$Info$.EngineName.length()", 10);
	}
}
