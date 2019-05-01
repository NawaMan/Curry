package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.CurryError;

public class Test_14_Constructor extends AllTests.TestCaseUnit {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public TestKind getTestKind() {
		return TestKind.OnMem;
	}
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {		
		//Engine E  = this.getTheEngine();
		
		String PName = "nawaman~>test~>P14_01";
		
		this.addUnit(
			this.newFile(PName.replaceAll("~>", "/") + "/C1.curry",
				"@@:Package("+PName+");\n"                                                      +
				"\n"                                                                            +
				"@@:TypeDef public class Str implements CharSequence {\n"                       +
				"	@@:Constructor public (Initial:CharSequence) {\n"                           +
				"		@:this_setAttrValue(`Data`, @:newInstance(StringBuffer.type, @:getVarValue(`Initial`)));\n" +
				"	};\n" +
				"	<?{ The data }?>\n"                                                                       +
				"	@@:Field  private Data       :StringBuffer = @:newInstance(StringBuffer.type);\n" +
				"	@@:Method public  subSequence(S:int, E:int):CharSequence {\n"               +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `subSequence`, @:getVarValue(`S`), @:getVarValue(`E`)));\n" +
				"	};\n"                                                                       +
				"	@@:Method public charAt(I:int):char {\n"                                    +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `charAt`, @:getVarValue(`I`)));\n" +
				"	};\n"                                                                       +
				"	@@:Method public toString():String {\n"                                     +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `toString`));\n" +
				"	};\n"                                                                       +
				"	@@:Method public length():int {\n"                                          +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `length`));\n"   +
				"	};\n"                                                                       +
				"	@@:Method public append(S:String):CharSequence {\n"                         +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `append`, @:getVarValue(`S`)));\n" +
				"	};\n"                                                                       +
				"};\n"                                                                          +
				"@@:TypeDef public class MyStr extends Str {\n"                                 +
				"	@@:Constructor public () {\n"                                               +
				"		@:super_initialize_ByParams(`MyStr>>`);\n"                              +
				"	};\n"                                                                       +
				"};\n"
			)
		);
		this.compile();
		
		this.printSection("Type Constructor");
		this.assertValue("@:getPackage(`"+PName+"`)", "Package:nawaman~>test~>P14_01");
		this.assertValue(PName + "=>Str.type",        "nawaman~>test~>P14_01=>Str");
		
		this.printSection("Default Constructor");
		this.assertProblem(
				"@:stack(null) {\n" +
				"	@:newVariable(`Str`, "+PName+"=>Str.type, @:newInstance("+PName+"=>Str.type));\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"	@:invokeByParams(@:getVarValue(`Str`), `append`, `A string`);\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"};\n",
				CurryError.class,
				".*Unknown constructor \\(\\) of the type 'nawaman~>test~>P14_01=>Str'.*");

		this.printSection("Parametered Constructor"); 
		this.startCapture();
		this.assertValue(
				"@:stack(null) {\n" +
				"	@:newVariable(`Str`, "+PName+"=>Str.type, @:newInstance("+PName+"=>Str.type, `Start>>`));\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"	@:invokeByParams(@:getVarValue(`Str`), `append`, `A string`);\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"};\n", "Str(15): \"Start>>A string\"\n");
		this.assertCaptured("Str(7): \"Start>>\"\nStr(15): \"Start>>A string\"\n");

		this.printSection("Super Constructor");
		this.startCapture();
		this.assertValue(
				"@:stack(null) {\n" +
				"	@:newVariable(`Str`, "+PName+"=>MyStr.type, @:newInstance("+PName+"=>MyStr.type));\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"	@:invokeByParams(@:getVarValue(`Str`), `append`, `A string`);\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"};\n", "Str(15): \"MyStr>>A string\"\n");
		this.assertCaptured("Str(7): \"MyStr>>\"\nStr(15): \"MyStr>>A string\"\n");
		
	}
}
