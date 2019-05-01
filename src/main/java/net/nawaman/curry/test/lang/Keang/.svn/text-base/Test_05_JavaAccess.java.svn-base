package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseParser;

public class Test_05_JavaAccess extends TestCaseParser {
	
	static public void main(String ... Args) { runTest(Args); }
	
	String ParaserTypeName = "Command";
	
	/**{@inheritDoc}*/ @Override
	protected String getParserTypeName() {
		return this.ParaserTypeName;
	}

	/**{@inheritDoc}*/ @Override
	protected void doTest(final String ... Args) {
		this.printSection("Element");
		this.assertValue("System.out",              System.out);
		this.assertValue("System.err",              System.err);
		this.assertValue("java.io.File.separator",  java.io.File.separator);
		
		this.startCapture();
		this.assertValue("System.out.println(`Hello World!!!`)", null);
		this.assertCaptured("Hello World!!!\n");
	}

}
