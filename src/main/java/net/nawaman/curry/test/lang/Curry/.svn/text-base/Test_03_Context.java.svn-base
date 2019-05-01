package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_03_Context extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		this.printSection("Returns");
		this.assertValue("5;"              ,  5);
		this.assertValue("@:plus(5, 3, 4);", 12);
		this.assertValue("@:return(5);",      5);

		this.printSection("Variables");
		this.assertValue("@:newVariable(`i`, int.type, 5);        @:getVarValue(`i`)    ;",  5);
		this.assertValue("@:newVariable(`i`, int.type, 5); @:plus(@:getVarValue(`i`),10);", 15);

		this.printSection("To be continue ... ");
			
		this.printSection("End");
	}
}
