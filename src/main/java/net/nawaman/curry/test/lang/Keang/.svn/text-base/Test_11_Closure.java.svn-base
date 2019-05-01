package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.Engine;
import net.nawaman.curry.compiler.CurryLanguage;
import net.nawaman.curry.script.CurryEngine;
import net.nawaman.curry.test.lang.Curry.AllTests;

public class Test_11_Closure extends AllTests.TestCaseUnit {
	
	static public void main(String ... Args) { runTest(Args); }

	/**{@inheritDoc}*/ @Override
	protected void doTest(final String ... Args) {
		CurryEngine   CEngine   = CurryEngine.getEngine();
		Engine        $Engine   = CEngine.getTheEngine();
		CurryLanguage $Language = CEngine.getTheLanguage();
		
		AllTests.TheEngine   = $Engine;
		AllTests.TheLanguage = $Language;
		
		String PName1 = "nawaman~>test~>P11_01";
		
		this.addUnit(
			this.newFile(PName1.replaceAll("~>", "/") + "/C1.curry",
				"@@:Package("+PName1+");" +
				
				"@@:TypeDef public class MyList<T:any> {\n"                                    +
				"	@@:Constructor public (Datas:T[]) {\n"                                     +
				"		this.Data = (Datas == null) ? new T[0] : Datas.clone();\n"             +
				"	};\n"                                                                      +
				"	@@:Field private Data:T[] = null;\n"                                       +
				"	@@:Method public Sub indexOf(Selector:Executable:<(int):boolean>):int {\n" +
				"		foreach(T t : this.Data) \n"                                           +
				"			if(Selector(t)) return $Count$;\n"                                 +
				"		\n"                                                                    +
				"		return -1;\n"                                                          +
				"	};\n"                                                                      +
				"};\n"
			)
		);
		this.compile();
		

		this.printSection("Local function");
		this.startCapture();
		this.assertValue(
			"{\n" +
			"	@@:Def sub indexOf(Is:int[], Selector:Executable:<(int):boolean>):int {" +
			"		foreach(int I : Is) \n" +
			"			if(Selector(I)) return $Count$;\n" +
			"		\n" +
			"		return -1;\n" +
			"	};\n" +
			"	int iSearch = 2;\n" +
			"	indexOf(new int[] { 6, 5, 4, 3, 2, 1, 0 }):{:(I:int):boolean;\n" +
			"		@:println(`I = ` + I);" +
			"		return I == iSearch;" +
			"	};\n" +
			"}\n", 4);
		this.assertCaptured("I = 6\nI = 5\nI = 4\nI = 3\nI = 2\n");
		

		this.printSection("Object method");
		this.startCapture();
		this.assertValue(
			"{\n" +
			"	"+PName1+"=>MyList L = new (new int[] { 6, 5, 4, 3, 2, 1, 0 });\n" +
			"	int iSearch = 2;\n" +
			"	L.indexOf():{:(I:int):boolean;\n" +
			"		@:println(`I = ` + I);" +
			"		return I == iSearch;" +
			"	};\n" +
			"}\n", 4);
		this.assertCaptured("I = 6\nI = 5\nI = 4\nI = 3\nI = 2\n");

		

		this.printSection("Object method");
		this.startCapture();
		this.assertValue(
			"{\n" +
			"	"+PName1+"=>MyList L = new (new int[] { 6, 5, 4, 3, 2, 1, 0 });\n" +
			"	int iSearch = 2;\n" +
			"	L.indexOf(@{:(I:int):boolean;\n" +
			"		@:println(`I = ` + I);" +
			"		return I == iSearch;" +
			"	});\n" +
			"}\n", 4);
		this.assertCaptured("I = 6\nI = 5\nI = 4\nI = 3\nI = 2\n");
		
		this.printSection("DONE!!!");
	}

}
