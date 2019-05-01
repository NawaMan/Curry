package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_04_ControlFlow extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		this.printSection("If");
		this.assertValue("@:if(@:moreThan(@:plus(1, 5),  4)) { @:return(5); };", 5);
		this.assertValue("@:if(@:lessThan(@:plus(1, 5), 10)) { @:return(5); };", 5);
		this.assertValue("@:if(@:lessThan(@:plus(1, 5),  4)) { @:return(5); };", false);
		this.assertValue("@:if(@:moreThan(@:plus(1, 5), 10)) { @:return(5); };", false);

		this.printSection("Loop");
		this.assertValue("@:newVariable(`S`, String.type, ``); @:for(   null, @@:Expr ( @:newVariable(`i`, int.type, 0) ), @@:Expr ( @:lessThan(@:getVarValue(`i`), 4) ), @@:Expr ( @:assignment(@#LocalVariable#@, @#IncAfter#@, 1, `i`) ) ) { @:if(@:NOT(@:equals(@:getVarValue(`i`), 0))) { @:assignment(@#LocalVariable#@, @#AppendTo#@, ` `, `S`); }; @:assignment(@#LocalVariable#@, @#AppendTo#@, @:toString(@:getVarValue(`i`)), `S`); }; @:return(@:getVarValue(`S`));", "0 1 2 3");
		this.assertValue("@:newVariable(`S`, String.type, ``); @:for(   null, @@:Expr ( @:newVariable(`i`, int.type, 0) ), @@:Expr ( @:lessThan(@:getVarValue(`i`), 7) ), @@:Expr ( @:assignment(@#LocalVariable#@, @#AddTo#@,    2, `i`) ) ) { @:if(@:NOT(@:equals(@:getVarValue(`i`), 0))) { @:assignment(@#LocalVariable#@, @#AppendTo#@, ` `, `S`); }; @:assignment(@#LocalVariable#@, @#AppendTo#@, @:toString(@:getVarValue(`i`)), `S`); }; @:return(@:getVarValue(`S`));", "0 2 4 6");
		this.assertValue("@:newVariable(`S`, String.type, ``); @:fromTo(null, `i`, int.type, 0, 4, 1)                                                                                                                                         { @:if(@:NOT(@:equals(@:getVarValue(`i`), 0))) { @:assignment(@#LocalVariable#@, @#AppendTo#@, ` `, `S`); }; @:assignment(@#LocalVariable#@, @#AppendTo#@, @:toString(@:getVarValue(`i`)), `S`); }; @:return(@:getVarValue(`S`));", "0 1 2 3");
		this.assertValue("@:newVariable(`S`, String.type, ``); @:fromTo(null, `i`, int.type, 0, 7, 2)                                                                                                                                         { @:if(@:NOT(@:equals(@:getVarValue(`i`), 0))) { @:assignment(@#LocalVariable#@, @#AppendTo#@, ` `, `S`); }; @:assignment(@#LocalVariable#@, @#AppendTo#@, @:toString(@:getVarValue(`i`)), `S`); }; @:return(@:getVarValue(`S`));", "0 2 4 6");
		
		this.printSection("To be continue ... ");
			
		this.printSection("End");
	}
}
