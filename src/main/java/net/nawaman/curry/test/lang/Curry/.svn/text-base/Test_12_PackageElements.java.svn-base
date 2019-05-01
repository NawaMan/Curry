package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.CurryError;
import net.nawaman.curry.Engine;
import net.nawaman.curry.MUnit;
import net.nawaman.curry.Package;

public class Test_12_PackageElements extends AllTests.TestCaseUnit {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public TestKind getTestKind() {
		return TestKind.OnMem;
	}
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		this.printSection("Package Variables");
		
		Engine E  = this.getTheEngine();
		MUnit  MU = E.getUnitManager();
		
		String PName1 = "P12_01";
		String PName2 = "P12_02";

		this.addUnit(
			this.newFile(PName1 + "/C1.curry",
				"@@:Package("+PName1+");\n" +
				
				"<?{ Array of 5 ints }?>" +
				"@@:TypeDef public array Int5 as int[5];\n" +

				"@@:TypeDef public variant ThingOrThings<T:any> as <any||:T:|T[]>;\n" +
				"@@:TypeDef public variant IntNumber            as <Number||byte|short|:int:|long>;\n" +
				
				"<?{ Get notified if the task is half done. The subroutine may return false if no need to do more. }?>\n" +
				"@@:TypeDef public executable NotifyHalfDone as <sub(String):boolean>;\n" +

				"<?{ Get notified if the task is half done. The subroutine may return false if no need to do more. }?>\n" +
				"@@:TypeDef public executable NotifyAllDone as <(String):boolean>;\n" +

				"<?{ Get notified if the task is half done. The subroutine may return false if no need to do more.}?>\n" +
				"@@:TypeDef public executable Notify<V:any> as <(V):boolean>;\n" +

				"<?{ Simple integer variable }?>\n" +
				"@@:Variable I:int = 3;\n" +
				"@@:Constant J:int = 3;\n" +
				
				"<?{ This is a function in the package "+PName1+" }?>\n" +
				"@@:Sub times(N:Number):Number {\n" +
					"@:return(@:multiply(@:package_getAttrValue(`I`), @:getVarValue(`N`)));\n" +
				"};\n"
			),
			this.newFile(PName2 + "/C2.curry",
				"@@:Package("+PName2+");" +
				
				"<?{ This is a function in the package "+PName2+" }?>\n" +
				"@@:Sub add(N:Number):Number {\n" +
					"@:return(@:plus(@:getAttrValue(@:getPackage(`"+PName1+"`), `I`), @:getVarValue(`N`)));\n" +
				"};\n"
			)
		);
		this.compile();
		
		Package P1 = MU.getPackage(PName1);
		Package P2 = MU.getPackage(PName2);

		this.printSection("Package");
		this.assertValue("@:getPackage(`"+PName1+"`)",  P1);

		this.printSection("Package Variable");
		this.assertValue(P1.getAttrData("I"   ), 3);
		this.assertValue(P1.setAttrData("I", 5), 5);
		this.assertValue(P1.setAttrData("I", P1.getAttrData("J")), 3);
		this.assertProblem("@:setAttrValue(@:getPackage(`"+PName1+"`), `J`, 5)", CurryError.class, "J is not writable.*");
		
		this.printSection("Package Executable");
		this.assertValue(P1.invoke("times", 7), 21);
		this.assertValue(P2.invoke("add",   7), 10);
		this.assertValue("@:getAttrValue(  @:getPackage(`"+PName1+"`), `I`       )",   3);
		this.assertValue("@:invokeByParams(@:getPackage(`"+PName1+"`), `times`, 5)",  15);
		

		this.printSection("Package Type");
		this.assertValue(PName1+"=>Int5.typeref", "P12_01=>Int5");
		
		this.assertValue("@:toString(@:newInstance("+PName1+"=>Int5.type))", "[null,null,null,null,null]");
		
		this.assertValue("@:toString(@:newInstance("+PName1+"=>IntNumber.type, 5))", 5);
		this.assertValue("@:instanceOf("+PName1+"=>IntNumber.type, 10)",  true);
		this.assertValue("@:instanceOf("+PName1+"=>IntNumber.type, 10b)", true);
		this.assertValue("@:instanceOf("+PName1+"=>IntNumber.type, 10L)", true);
		this.assertValue("@:instanceOf("+PName1+"=>IntNumber.type, 10f)", false);
		this.assertValue("@:instanceOf("+PName1+"=>IntNumber.type, 10I)", false);
		
		this.assertValue("@:instanceOf("+PName1+"=>NotifyHalfDone.type, @@:New sub(    Msg:String):boolean { @:return(true); })",  true);
		this.assertValue("@:instanceOf("+PName1+"=>NotifyHalfDone.type, @@:New macro(  Msg:String):boolean { @:return(true); })", false);
		this.assertValue("@:instanceOf("+PName1+"=>NotifyHalfDone.type, @@:New closure(Msg:String):boolean { @:return(true); })",  true);

		this.assertValue("@:instanceOf("+PName1+"=>NotifyAllDone.type, @@:New sub(    Msg:String):boolean { @:return(true); })",  true);
		this.assertValue("@:instanceOf("+PName1+"=>NotifyAllDone.type, @@:New macro(  Msg:String):boolean { @:return(true); })",  true);
		this.assertValue("@:instanceOf("+PName1+"=>NotifyAllDone.type, @@:New closure(Msg:String):boolean { @:return(true); })",  true);

		this.printSection("Executable and Generic");
		this.assertValue("@:isKindOf(  "+PName1+"=>Notify.type,         "+PName1+"=>NotifyAllDone.type)",  true);
		this.assertValue("@:instanceOf("+PName1+"=>Notify.type,         @@:New closure(Msg:String):boolean { @:return(true); })",  true);
		this.assertValue("@:instanceOf("+PName1+"=>Notify<String>.type, @@:New closure(Msg:String):boolean { @:return(true); })",  true);

		this.printSection("Variant and Generic");
		this.assertValue("@:toDetail("       +PName1+"=>ThingOrThings<int>.type)", PName1+"=>ThingOrThings<int>:Variant");
		this.assertStringContains("@:toDisplayString("+PName1+"=>ThingOrThings<int>.type)",
				"Variant:<any||", ":int:", "int[]", ":Type:<"+PName1+"=>ThingOrThings<int>>");
		this.assertValue("@:instanceOf("+PName1+"=>ThingOrThings.type,      5)",                                             true);
		this.assertValue("@:instanceOf("+PName1+"=>ThingOrThings.type,      @:newInstance("+PName1+"=>IntNumber.type, 5))",  true);
		this.assertValue("@:instanceOf("+PName1+"=>ThingOrThings<int>.type, @:newInstance("+PName1+"=>IntNumber.type, 5))",  true);
	}
}
