package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_08_Assignment extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		this.printSection("Assign");
		
		this.startCapture();
		this.assertValue("@:newVariable(`I`, int.type, 0); System.out.println(I); I++; System.out.println(I); I;", 1);
		this.assertCaptured("0\n1\n");

		this.startCapture();
		this.assertValue("@:newVariable(`I`, int.type, 0); System.out.println(I); I--; System.out.println(I); I;", -1);
		this.assertCaptured("0\n-1\n");

		this.startCapture();
		this.assertValue("@:newVariable(`I`, int.type, 0); System.out.println(I); I--; System.out.println(I); -I;", 1);
		this.assertCaptured("0\n-1\n");
	}
}
