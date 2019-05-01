package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;
import net.nawaman.testsuite.TestEntry;

public class Test_07_StackVariables extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public TestEntry[] getTestEntries() {
		return this.newTests(
			this.newSection("Some test"),
			
			this.newSection("To be continue ... "),
			
			this.newSection("End")
		);
	}
}