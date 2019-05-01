package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_05_SpecialControlFlow extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		this.printSection("Choose & Switch");
		
		this.printSubSection("Choose");
		this.assertValue("@@:Choose(null, @:lessThanEqual(5, 1)) { @@:Case(true) { 1; } @@:Default { 2; } };", 2);
		this.assertValue("@@:Choose(null, @:lessThanEqual(1, 5)) { @@:Case(true) { 1; } @@:Default { 2; } };", 1);
		
		this.printSubSection("Switch");
		this.assertValue(
			"@:newVariable(`S`, String.type, ``);" +
			"@:forEach(null, `I`, int.type, @:newArrayLiteral_int(0, 1, 2, 3, 4)) {" +
				"@@:Switch(null, @:getVarValue(`I`)) {" +
					"@@:Case(0) { @:assignment(@#LocalVariable#@, @#AppendTo#@, `0`, `S`);                     } " +
					"@@:Case(1) { @:assignment(@#LocalVariable#@, @#AppendTo#@, `1`, `S`); @:done(null, null); } " +
					"@@:Case(2) { @:assignment(@#LocalVariable#@, @#AppendTo#@, `2`, `S`);                     } " +
					"@@:Case(3) { @:assignment(@#LocalVariable#@, @#AppendTo#@, `3`, `S`); @:done(null, null); } " +
					"@@:Default { @:assignment(@#LocalVariable#@, @#AppendTo#@,`-1`, `S`); @:done(null, null); } " +
				"};"+
			"};"+
			"@:return(@:getVarValue(`S`));"
			,
			"011233-1"
		);
		
		this.printSection("Try");
		this.assertValue(
			"@:newVariable(`S`, String.type, ``);"+
			"@:forEach(null, `I`, int.type, @:newArrayLiteral_int(null, 0, 1)) {" +
				"@:assignment(@#LocalVariable#@, @#AppendTo#@, `-Before main-`, `S`);"+
				"@@:Try(null) {"+
					"@@:Body {"+
						"@:assignment(@#LocalVariable#@, @#AppendTo#@, @:concat(`5/`, @:getVarValue(`I`), ` = `, @:divide(5, @:getVarValue(`I`))), `S`);"+
						"@:assignment(@#LocalVariable#@, @#AppendTo#@, `-Main Block-`, `S`);"+
					"}"+
					"@@:Catch(E1:ArithmeticException) {"+
						"@:assignment(@#LocalVariable#@, @#AppendTo#@, `-E1-`, `S`);"+
					"}"+
					"@@:Catch(E2:NullPointerException) {"+
						"@:assignment(@#LocalVariable#@, @#AppendTo#@, `-E2-`, `S`);"+
					"}"+
					"@@:Finally {"+
						"@:assignment(@#LocalVariable#@, @#AppendTo#@, `-Finally-`, `S`);"+
					"}"+
				"};"+
			"};"+
			"@:return(@:getVarValue(`S`));"
			,
			"-Before main--E1--Finally--Before main--E1--Finally--Before main-5/1 = 5-Main Block--Finally-"
		);
		
		this.printSection("End");
	}
}
