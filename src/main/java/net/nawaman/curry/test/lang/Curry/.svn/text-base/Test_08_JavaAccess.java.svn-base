package net.nawaman.curry.test.lang.Curry;

import java.io.File;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_08_JavaAccess extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		this.printSection("Some test");
		this.assertValue(
			"@@:Import(java.io.*); \n" +
			"@:return(" +
				"@:invokeJavaObjectMethodByMethod(" +
					"@:getJavaMethodByParamClasss(java.io.File.class, `getParent`, false)," +
					"@:invokeJavaObjectMethodByMethod(" +
						"@:getJavaMethodByParamClasss(java.io.File.class, `getAbsoluteFile`, false)," +
						"@:newInstance(java.io.File.type, `.`)" +
					")" +
				")" +
			"); ",
			(new File(".")).getAbsoluteFile().getParent()
		);
			
		this.printSection("To be continue ... ");
			
		this.printSection("End");
	}
}