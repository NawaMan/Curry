package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_07_ArrayOperation extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		this.printSection("Array Operation");
		this.assertValue(" new int[] { 1, 2, 3 };",      new Integer[] { 1, 2, 3 });
		this.assertValue("(new int[] { 1, 2, 3 })[1];", 2);
	}


}
