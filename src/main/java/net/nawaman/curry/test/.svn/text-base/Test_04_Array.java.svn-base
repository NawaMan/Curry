package net.nawaman.curry.test;

import java.io.File;

import net.nawaman.curry.Engine;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.MType;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.test.AllTests.TestCase;
import net.nawaman.util.UArray;

public class Test_04_Array extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }

	/** Actually do this test case by running all the test values */
	@Override protected void doTest(final String ... Args) {
		// Ensure the engine
		Engine      E  = AllTests.getEngine();
		MExecutable ME = E.getExecutableManager();
		MType       MT = E.getTypeManager();
		
		// Should be after prepare Engine
		this.enableOutputCapture();

		// Starting ----------------------------------------------------------------------------------------------------

		this.printSection("Simple Array");	// so it can be used every where.
		this.assertValue(TKArray.AnyArrayRef,                                                         "any[]");
		this.assertValue(TKArray.NumberArrayRef,                                                      "Number[]");
		this.assertValue(TKArray.newArrayTypeRef(TKJava.TBigInteger.getTypeRef()) ,                   "BigInteger[]");
		this.assertValue(TKArray.newArrayTypeRef(MT.getTypeOfTheInstanceOf(File.class).getTypeRef()), "java.io.File[]");

		Type ATN = (Type)E.execute(ME.newType(TKArray.StringArrayRef));
		
		this.printSection("Static elements of " + ATN.toString());	// so it can be used every where.
		this.assertValue(
			UArray.toString(ATN.getTypeInfo().getAttributeInfos(), "[\n\t", "\n]", ",\n\t"),
			"[\n" +
			"	length:int,\n" +
			"	containTypeRef:TypeRef,\n" +
			"	@Native EmptyTypeArray:Type[] ,\n" +
			"	@Native Public:net.nawaman.curry.Accessibility ,\n" +
			"	@Native Private:net.nawaman.curry.Accessibility ,\n" +
			"	@Native Protected:net.nawaman.curry.Type.Access \n" +
			"]"
		);
		
		this.assertStringContains(
			UArray.toString(ATN.getTypeInfo().getOperationInfos(), "[\n\t", "\n]", ",\n\t"),
			"getContainType():Type:<String>", "getContainTypeRef():TypeRef", "getLength():int");
		
		
		this.printSection("Non-Static elements of " + ATN.toString());	// so it can be used every where.
		this.assertValue(
			UArray.toString(ATN.getTypeInfo().getObjectAttributeInfos(), "[\n\t", "\n]", ",\n\t"),
			"[\n\t\n]"
		);
		
		this.assertStringContains(
			UArray.toString(ATN.getTypeInfo().getObjectOperationInfos(), "[\n\t", "\n]", ",\n\t"),
			"getLength():int", "getData(Number):String", "setData(Number, String):String"
		);
		
		this.printSection("Constructor of " + ATN.toString());	// so it can be used every where.
		this.assertValue(
			UArray.toString(ATN.getTypeInfo().getConstructorInfos(), "[\n\t", "\n]", ",\n\t"),
			"[\n"+
			"	new():void,\n"+
			"	new(String[]):void,\n"+
			"	new(String ... ):void\n"+
			"]"
		);

		TypeRef ATRN5 = TKArray.newArrayTypeRef(TKJava.TNumber.getTypeRef(), 5);
		Type    ATN5  = (Type)E.execute(ME.newType(ATRN5));

		this.printSection("Fix size array");
		this.assertValue(E.execute("toDisplayString", ATRN5), "`Number[5]`:net.nawaman.curry.TLParametered.TRParametered");
		this.assertValue(E.execute("toDisplayString", ATN5),  "`Number[5]`:Type:<Number[5]>");

		TypeRef ATRN57 = TKArray.newArrayTypeRef(ATRN5, 7);
		Type    ATN57  = (Type)E.execute(ME.newType(ATRN57));
		this.printSection("Multiple dimensione array");
		this.assertValue(E.execute("toDisplayString", ATRN57), "`Number[7][5]`:net.nawaman.curry.TLParametered.TRParametered");
		this.assertValue(E.execute("toDisplayString", ATN57),  "`Number[7][5]`:Type:<Number[7][5]>");

		this.printSection("Instance");
		this.assertValue(E.execute("toDisplayString",                      (Object)(new String[] { "One", "Two", "Three" })), "`[One,Two,Three]`:String[3]");
		this.assertValue(E.execute("toDisplayString", E.execute("newInstance", ATN, new String[] { "One", "Two", "Three" })), "`[One,Two,Three]`:String[3]");
	
		// Ending ------------------------------------------------------------------------------------------------------
		// Should be the last one
		this.disableOutputCapture();
		this.println();
	}
}
