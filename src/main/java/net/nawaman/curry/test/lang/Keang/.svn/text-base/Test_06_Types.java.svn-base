package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_06_Types extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		super.doTest(Args);

		this.printSection("Array Related");
		this.assertValue("@:getTypeOf(new int[] { 1, 2, 3 }).getTypeRef();", TKArray.newArrayTypeRef(TKJava.TInteger.getTypeRef(), 3));
		//this.assertValue("(new int[][] { new int[] { 1, 2 } } instanceof int[1][2]);", true);
	}

}
