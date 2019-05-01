package net.nawaman.curry.test;

import net.nawaman.curry.CurryError;
import net.nawaman.curry.Engine;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.test.AllTests.TestCase;

public class Test_02_CoreInstruction extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }

	/** Actually do this test case by running all the test values */
	@Override protected void doTest(final String ... Args) {
		Engine      E  = AllTests.getEngine();
		MExecutable ME = E.getExecutableManager();
		

		this.printSection("Core Instructions");
		this.printSubSection("Data");
		this.assertValue(E.execute(5),          5);
		this.assertValue(ME.newExpr("data", 5), 5);

		this.printSubSection("Printing");
		this.assertValue(E.getExecutableManager().newExpr("println", 5), 5, "5\n");
		
		this.printSubSection("Divided by Zero");
		this.assertProblem(
				E.getExecutableManager().newExpr("divide", 5, 0),
				CurryError.class, "java\\.lang\\.ArithmeticException: DivideByZero");

		// ... More here
		this.println();
	}
}
