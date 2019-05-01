package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseStack;

public class Test_09_Executable extends TestCaseStack {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		super.doTest(Args);

		this.printSection("Local executable");
		this.assertValue(
				"{\n" +
				"	@@:Def macro factorial(I:int):int {" +
				"		return (I <= 1) ? 1 : I * factorial(I - 1);\n" +
				"	};\n" +
				"	factorial(3);\n" +
				"}\n", 6);
		
		this.assertValue(
			"{" +
			"	int    Factor    = 10;\n"+
			"	String Separator = ` & `;\n"+
			"	@@:Def sub getSeparator[Separator]():String {\n" +
			"		return Separator;\n" +
			"	};\n"+ 
			"	@@:Def macro factorial(I:int):int {" +
			"		return (I <= 1) ? 1 : I * factorial(I - 1) * Factor;\n"+
			"	};\n"+
			"	String S = ``;\n"+
			"	@@:Def fragment doFactorial():void {\n"+
			"		S += factorial(5);\n" +
			"	};\n"+
			"	doFactorial();\n"+ 
			"	S += getSeparator();\n"+
			"	doFactorial();\n"+
			"	return S;\n" +
			"}"
			,
			"1200000 & 1200000"
		);

		this.printSection("TextProcessing");
		this.assertValue(
				"{\n" +
				"	@@:Def macro spaceWrapped(I:int):String {" +
				"		return \\` %s `(I):{ exit @:format($Text, $0); };\n" +
				"	};\n" +
				"	spaceWrapped(3);\n" +
				"}\n", " 3 ");
		this.assertValue(
				"{\n" +
				"	@@:Def macro spaceWrapped(I:int):String {" +
				"		\\` %s `(I):{ return @:format($Text, $0); };\n" +
				"	};\n" +
				"	spaceWrapped(3);\n" +
				"}\n", " 3 ");
		this.assertValue(
				"{\n" +
				"	@@:Def macro spaceWrapped(I:int):String {" +
				"		\\` %s `:{" +
				"			return @:format($Text, I);" +
				"		};\n" +
				"	};\n" +
				"	spaceWrapped(3);\n" +
				"}\n", " 3 ");
	}
}
