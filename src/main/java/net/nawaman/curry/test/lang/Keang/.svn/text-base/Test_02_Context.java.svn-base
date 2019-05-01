package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_02_Context extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }

	/**{@inheritDoc}*/ @Override
	protected void doTest(final String ... Args) {
		this.printSection("Variables");
		this.assertValue("@:newVariable(`i`, int.type, 5); i;",         5);
		this.assertValue("({ @:newVariable(`i`, int.type, 5); i; });",  5);

		this.printSection("Assignment");
		this.assertValue("@:newVariable(`i`, int.type, 5); i =  10;", 10);
		this.assertValue("@:newVariable(`i`, int.type, 5); i += 10;", 15);

		this.printSection("Return");
		this.assertValue("return 5;",     5);
		this.assertValue("{ return 5; }", 5);
	}
}
