package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.Engine;
import net.nawaman.curry.MUnit;
import net.nawaman.curry.Package;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.Type;
import net.nawaman.util.UArray;

public class Test_13_TypeElements extends AllTests.TestCaseUnit {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public TestKind getTestKind() {
		return TestKind.OnMem;
	}
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {		
		Engine E  = this.getTheEngine();
		MUnit  MU = E.getUnitManager();
		
		String PName1 = "nawaman~>test~>P13_01";
		
		this.addUnit(
			this.newFile(PName1.replaceAll("~>", "/") + "/C1.curry",
				"@@:Package("+PName1+");\n" +
				"\n" + 
				"@@:TypeDef public wrapper MyString of String {\n" +
				"	<?{ Returns escape string }?>\n" +
				"	@@:Method public Sub escapeText():String @@Java:{\n" +
				"		if($This$ == null) return null;\n" +
				"		return net.nawaman.util.UString.escapeText((String)$This$).toString();\n" +
				"	}:Java:;\n" +
				"};\n"+
				"@@:TypeDef public variant Chars<T:any> as <any||:char:|char[]|CharSequence> {\n" +
				"	@@:Method public Sub showOff(t:T):int {\n" +
				"		@:println(T.type);\n" +
				"		@:println(@:instanceOf(T.type, 5));\n" +
				"		@:println(@:instanceOf(T.type, `5`));\n" +
				"		@:return(@:invokeAsTypeByParams(@:getVarValue(`this`), Chars.type, `count`));\n" +
				"	};\n" +
				"	@@:Method public Sub count():int {\n" +
				"		@:if(@:instanceOf(char  .type,       @:getVarValue(`this`))){ @:return(1); };\n" +
				"		@:if(@:instanceOf(char[].type,       @:getVarValue(`this`))){ @:return(@:getLengthArrayObject((:char[]?@:getVarValue(`this`)))); };\n" +
				"		@:if(@:instanceOf(CharSequence.type, @:getVarValue(`this`))){ @:return(@:invokeByParams((:CharSequence?@:getVarValue(`this`)), `length`)); };\n" +
				"		@:return(0);\n" +
				"	};\n" +
				"};\n" +
				"\n" +
				"@@:TypeDef public duck HasLength { @@:Method public Sub length():int; };\n" +
				"\n"                                                                              +
				"@@:TypeDef public class Str implements HasLength, CharSequence {\n"            +
				"	@@:Constant static public  DefaultData:String       = `Str:`;\n"            +
				"	@@:Field           private Data       :StringBuffer = @:newInstance(StringBuffer.type);\n"   +
				"	@@:Method public Sub subSequence(S:int, E:int):CharSequence {\n"            +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `subSequence`, @:getVarValue(`S`), @:getVarValue(`E`)));\n" +
				"	};\n"                                                                       +
				"	@@:Method public Sub charAt(I:int):char {\n"                                +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `charAt`, @:getVarValue(`I`)));\n" +
				"	};\n"                                                                       +
				"	@@:Method public Sub toString():String {\n"                                 +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `toString`));\n" +
				"	};\n"                                                                       +
				"	@@:Method public Sub length():int {\n"                                      +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `length`));\n"   +
				"	};\n"                                                                       +
				"	@@:Method public Sub append(S:String):CharSequence {\n"                     +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`), `append`, @:getVarValue(`S`)));\n" +
				"	};\n"                                                                       +
                "	@@:Method public Sub chars():java.util.stream.IntStream {\n"                +
                "		@:return(null);\n"                                                      +
                "	};\n"                                                                       +
                "   @@:Method public Sub codePoints():java.util.stream.IntStream {\n"                +
                "       @:return(null);\n"                                                      +
                "   };\n"                                                                       +
				"};\n"                                                                          +
				"\n"                                                                            +
				"@@:TypeDef public class Str2 implements HasLength, CharSequence, Appendable {\n" +
				"	@@:StaticDelegatee"                                                         +
				"	@@:Field private Data:StringBuffer = @:newInstance(StringBuffer.type);\n"   +
				"	@@:Method public toString():String {\n"                                 +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`),`toString`));\n"  +
				"	};\n"                                                                       +
				"	@@:Method public length():int {\n"                                   +
				"		@:return(@:invokeByParams(@:this_getAttrValue(`Data`),`length`));\n"    +
				"	};\n"                                                                       +
				"};\n"
			)
		);
		this.compile();
		
		Package P1 = MU.getPackage(PName1);
		
		this.printSection("Type Elements");
		this.assertValue("@:getPackage(`"+PName1+"`)",           P1);
		this.assertValue("@:toDetail("+PName1+"=>Chars.type)",   PName1+"=>Chars<T:any>:Variant");
		
		this.assertValue("@:instanceOf("+PName1+"=>Chars.type, 'c')",                                        true);
		this.assertValue("@:instanceOf("+PName1+"=>Chars.type, @:newArrayLiteral_char('c','h','a','r'))",    true);
		this.assertValue("@:instanceOf("+PName1+"=>Chars.type, @:newInstance(StringBuilder.type, `Chars`))", true);
		this.assertValue("@:instanceOf("+PName1+"=>Chars.type, `String`)",                                   true);

		this.assertValue("@:instanceOf("+PName1+"=>Chars.type, @:charToInt('c'))",                   false);
		this.assertValue("@:instanceOf("+PName1+"=>Chars.type, 10b)",                                false);
		this.assertValue("@:instanceOf("+PName1+"=>Chars.type, @:newArrayLiteral_int( 1, 2, 3, 4))", false);

		this.printSection("Call Variant method");
		this.assertValue("@:invokeAsTypeByParams('c',                                    "+PName1+"=>Chars.type, `count`)", 1);
		this.assertValue("@:invokeAsTypeByParams(@:newArrayLiteral_char('c','h','a','r'),"+PName1+"=>Chars.type, `count`)", 4);
		this.assertValue("@:invokeAsTypeByParams(`String`,                               "+PName1+"=>Chars.type, `count`)", 6);
		
		this.printSection("Call Variant method and Generic");
		this.assertValue(  "@:invokeAsTypeByParams('c', "+PName1+"=>Chars<int>.type, `showOff`,  5)",  1, "any\ntrue\ntrue\n");
		this.assertProblem(
			"@:invokeAsTypeByParams('c', "+PName1+"=>Chars<int>.type, `showOff`, `5`)",
			RuntimeException.class,
			".*operation `\\(\\(char\\)c\\)\\.showOff\\(String\\)` is not found.*");

		this.printSection("Wrapper");
		this.assertValue("@:instanceOf("+PName1+"=>MyString.type, `String`)",    true);
		this.assertValue("@:isKindOf(  "+PName1+"=>MyString.type, String.type)", true);
		this.assertValue("@:isKindOf(String.type, "+PName1+"=>MyString.type)",   true);	// Virtual Type in the work here.
		this.assertValue("@:invokeAsTypeByParams(`Cola`, "+PName1+"=>MyString.type, `escapeText`)", "Cola");

		this.printSection("Interface");
		this.assertValue("@:instanceOf("+PName1+"=>HasLength.type, `String`)", true);
		
		this.printSection("Class");
		this.assertValue("@:isKindOf("+PName1+"=>HasLength.type, "+PName1+"=>Str.type)", true);
		Type T = (Type)E.execute("type", new TLPackage.TRPackage(PName1, "Str"));
		this.println(T);
		this.println(UArray.toString(T.getTypeInfo().getObjectOperationInfos(), "[\n\t", "]", "\n\t"));
		
		this.printSection("Class II");
		this.startCapture();
		this.assertValue(
				"@:stack(null) {\n" +
				"	@:newVariable(`Str`, "+PName1+"=>Str.type, @:newInstance("+PName1+"=>Str.type));\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"	@:invokeByParams(@:getVarValue(`Str`), `append`, `A string`);\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"};\n", "Str(8): \"A string\"\n");
		this.assertCaptured("Str(0): \"\"\nStr(8): \"A string\"\n");
		this.assertValue("@:getAttrValue("+PName1+"=>Str.type, `DefaultData`);", "Str:");

		this.printSection("Class III");
		this.startCapture();
		this.assertValue(
				"@:stack(null) {\n" +
				"	@:newVariable(`Str`, "+PName1+"=>Str2.type, @:newInstance("+PName1+"=>Str2.type));\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"	@:invokeByParams(@:getVarValue(`Str`), `append`, `A string`);\n" +
				"	@:printf(`Str(%s): \\\"%s\\\"\\n`, @:invokeByParams(@:getVarValue(`Str`), `length`), @:getVarValue(`Str`));\n" +
				"};\n", "Str(8): \"A string\"\n");
		this.assertCaptured("Str(0): \"\"\nStr(8): \"A string\"\n");
	}
}
