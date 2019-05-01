package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_04_ControlFlow extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }

	/**{@inheritDoc}*/ @Override
	protected void doTest(final String ... Args) {
		this.printSection("Condition");
		this.assertValue("{ if(1+5 >  4) return 5; }",     5);
		this.assertValue("{ if(1+5 < 10) return 5; }",     5);
		this.assertValue("{ if(1+5 <  4) return 5; }", false);
		this.assertValue("{ if(1+5 > 10) return 5; }", false);
		
		this.assertValue("{ unless(1+5 >  4) return 5; }", false);
		this.assertValue("{ unless(1+5 < 10) return 5; }", false);
		this.assertValue("{ unless(1+5 <  4) return 5; }",     5);
		this.assertValue("{ unless(1+5 > 10) return 5; }",     5);

		this.printSection("FromTo Loop");
		this.assertValue("{ String S = ``; fromto(int i = 0:4)          { if(i != 0) S = S + ` `; S = S + i; } return S; }", "0 1 2 3");
		this.assertValue("{ String S = ``; fromto(int i = 0:2:7)        { if(i != 0) S = S + ` `; S = S + i; } return S; }", "0 2 4 6");
		
		this.printSection("For Loop");
		this.assertValue("{ String S = ``; for(int i = 0; i < 4; i++)   { if(i != 0) S = S + ` `; S = S + i; } return S; }", "0 1 2 3");
		this.assertValue("{ String S = ``; for(int i = 0; i < 7; i=i+2) { if(i != 0) S = S + ` `; S = S + i; } return S; }", "0 2 4 6");
		
		this.printSection("For Loop");
		this.assertValue("{ String S = ``; foreach(int i : new int[] { 0, 1, 2, 3 }) { if(i != 0) S = S + ` `; S = S + i; } return S; }", "0 1 2 3");
		this.assertValue("{ String S = ``; foreach(int i : new int[] { 0, 2, 4, 6 }) { if(i != 0) S = S + ` `; S = S + i; } return S; }", "0 2 4 6");
		
		this.printSection("While Loop");
		this.assertValue("{ String S = ``; int i = 0; while(i < 4) { if(i != 0) S = S + ` `; S = S + i; i++;  } return S; }", "0 1 2 3");
		this.assertValue("{ String S = ``; int i = 0; while(i < 7) { if(i != 0) S = S + ` `; S = S + i; i+=2; } return S; }", "0 2 4 6");

		this.printSection("Repeat-Until Loop");
		this.assertValue("{ String S = ``; int i = 0; repeat { if(i != 0) S = S + ` `; S = S + i; i++;  } until(i > 3); return S; }", "0 1 2 3");
		this.assertValue("{ String S = ``; int i = 0; repeat { if(i != 0) S = S + ` `; S = S + i; i+=2; } until(i > 6); return S; }", "0 2 4 6");

		this.printSection("Cast");
		this.assertValue("int    I = 5;   cast(int V = I) { return `Match: ` + V; }", "Match: 5");
		this.assertValue("String S = `5`; cast(int V = S) { return `Match: ` + V; }", null);

		this.printSection("Try");
		this.assertValue(
			"String S = ``;"+
			"foreach(int I : new int[] { null, 0, 1 }) {\n" +
			"	S += `-Before main-`;\n"+
			"	try {\n"+
			"		S += `5/` + I + ` = ` + (5/I);\n" +
			"		S += `-Main Block-`;\n"+
			"	} catch(ArithmeticException E1) {\n"+
			"		S += `-E1-`;"+
			"	} catch(NullPointerException E1) {\n"+
			"		S += `-E2-`;\n"+
			"	} finally {\n"+
			"		S += `-Finally-`;\n"+
			"	}\n"+
			"}\n"+
			"return S;\n"
			,
			"-Before main--E1--Finally--Before main--E1--Finally--Before main-5/1 = 5-Main Block--Finally-"
		);
		this.printSection("Choose & Switch");
		
		this.printSubSection("Choose");
		this.assertValue("choose(5 <= 1) { case true: 1; default: 2; }", 2);
		this.assertValue("choose(1 <= 5) { case true: 1; default: 2; }", 1);

		this.printSubSection("Switch");
		this.assertValue(
			"String S = ``;" +
			"foreach(int I : new int[] { 0, 1, 2, 3, 4 }) {" +
				"switch(I) {" +
					"case(0): { S +=  `0`;       } " +
					"case(1): { S +=  `1`; done; } " +
					"case(2): { S +=  `2`;       } " +
					"case(3): { S +=  `3`; done; } " +
					"default: { S += `-1`; done; } " +
				"}"+
			"}"+
			"return S;"
			,
			"011233-1"
		);
	}

}
