package net.nawaman.curry.test;

import java.util.regex.Pattern;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Context;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.TKInterface;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TLCurrent;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.TLParameter;
import net.nawaman.curry.TLParametered;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeBuilder;
import net.nawaman.curry.TypeParameterInfo;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.UnitBuilder;
import net.nawaman.curry.UnitBuilders;
import net.nawaman.curry.Variable;
import net.nawaman.curry.extra.type_object.TBClass;
import net.nawaman.curry.extra.type_object.TKClass;
import net.nawaman.curry.test.AllTests.TestCase;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseUnit;
import net.nawaman.curry.util.DataHolderInfo;

public class Test_09_Interface extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }

	static private String  UName  = "TU09";
	static private String  PName  = "P09";
	static private String  TName1 = "TO1";
	static private String  TName2 = "TI2";
	static private String  TName3 = "TI3";
	static private String  TName4 = "TI4";
	static private String  TName5 = "TI5";
	
	// public class TO1<V:Any> implements Runnable, CharSequence, TI2, TI3, TI5<TO1, V> {
	//                                           // All needed for CharSequence is provided by Text
	//  	public String Text = "Hello" @StaticDelegate;
	//  	public void run() {
	//  		@:println(Test from run.);
	//  		return this;
	//  	}
	//  	public int showText() {
	//  		return this.showText("%s");
	//  	}
	//  	public int showText(String pFormat) {
	//  		@printf(pFormat, this.Text);
	//  		return this.Text.length();
	//  	}
	//  	public Current append(String Str, V Value) {
	//  		this.Text += String.format("(%s)", V);
	//  	}
	// }
	//
	// public interface TI2 {
	//  	public int showText(String pFormat);
	// }
	//
	// public interface TI3 @NonStick extends TI2 {
	//  	public int showText();
	// }
	//
	// public interface TI4 @NonStick {
	//  	public int length();
	// }
	//
	// public interface TI5<T:Any, V:Any> {
	//  	public T append(V Value);
	// }

	/** Actually do this test case by running all the test values */
	@Override protected void doTest(final String ... Args) {
		// Ensure the engine
		Engine      E  = AllTests.getEngine();
		MExecutable ME = E.getExecutableManager();

		// Prepare the types
		this.prepareTypes(/* ReCreate? (not load from the package file) */ true);
		
		// Should be after prepare Engine
		this.enableOutputCapture();
		
		TypeRef TRef = TKJava.TRunnable.getTypeRef();
		Type    T    = (Type)E.execute(ME.newType(TRef));
		
		TypeRef TR1 = new TLPackage.TRPackage(PName, TName1);
		Type    T1  = (Type)E.execute(ME.newType(TR1));
		Object  O1  = E.execute("newInstance", T1);		
		
		this.printSection("Java Runnable Interface");
		this.assertValue(TR1, "P09=>TO1");
		this.assertValue(T1,  "P09=>TO1<V:any>");
		this.assertValue(E.execute("instanceOf", T,  O1),    true);
		this.assertValue(E.execute("isKindOf",   T,  T1),    true);

		TRef = TKJava.TCharSequence.getTypeRef();
		T    = (Type)E.execute(ME.newType(TRef));
		this.assertValue(E.execute("instanceOf", T,  O1),    true);
		this.assertValue(E.execute("isKindOf",   T,  T1),    true);
		
		this.startCapture();
		E.execute("invokeByParams", O1, "run");
		this.assertCaptured("Test from run.\n");

		this.printSection("Java Thread Interface");
		
		this.disableOutputCapture();
		// (new ThreadO1()).start();
		TRef     = TKJava.TThread.getTypeRef();
		T        = (Type)E.execute(ME.newType(TRef));
		Object O = E.execute("newInstance", T, O1);
		this.startCapture();
		E.execute("invokeByParams", O, "start");
		try { Thread.sleep(100); } catch (Exception e) {}
		this.assertCaptured("Test from run.\n");
		this.enableOutputCapture();
		
		// (new ThreadO1()).start();
		TRef = E.getTypeManager().getTypeOfTheInstanceOf(Pattern.class).getTypeRef();
		T    = (Type)E.execute(ME.newType(TRef));
		this.assertValue(E.execute(ME.newExpr("getAttrValue", O1, "Text")), "Hello");
		this.assertFalse(Boolean.TRUE.equals(E.execute("invokeByParams", T, "matches", "Hell",  ME.newExpr("getAttrValue", O1, "Text"))));
		this.assertTrue( Boolean.TRUE.equals(E.execute("invokeByParams", T, "matches", "Hello", ME.newExpr("getAttrValue", O1, "Text"))));
		this.assertValue(E.execute("invokeByParams", O1, "getClass"), "DObject");

		TypeRef TR2 = new TLPackage.TRPackage(PName, TName2);
		this.printSection("Curry Interface");
		this.assertValue(E.execute("instanceOf", ME.newType(TR2), O1), true);
		this.startCapture();
		this.assertValue(E.execute(ME.newExpr("invokeByParams", O1, "showText", "Text: %s\n")), "5");
		this.assertCaptured("Text: Hello\n");

		TypeRef TR3 = new TLPackage.TRPackage(PName, TName3);
		this.printSection("NonStrict Interface");
		this.assertValue(E.execute("instanceOf", ME.newType(TR3), O1), true);
		this.startCapture();
		this.assertValue(E.execute(ME.newExpr("invokeByParams", O1, "showText")), "5");
		this.assertCaptured("Hello");

		TypeRef TR4 = new TLPackage.TRPackage(PName, TName4);
		this.printSection("NonStrict Interface on Java Object");
		this.assertValue(E.execute("instanceOf", ME.newType(TR4), O1), true);
		this.assertValue(E.execute(ME.newExpr("invokeByParams", O1, "length")), 5);
		this.assertValue(E.execute("instanceOf", ME.newType(TR4), "Hey!"), true);
		this.assertValue(E.execute(ME.newExpr("invokeByParams",   "Hey!", "length")), 4);
		this.assertValue(E.execute("instanceOf", ME.newType(TR4), new StringBuilder("Hi!")), true);
		this.assertValue(E.execute(ME.newExpr("invokeByParams",   new StringBuilder("Hi!"), "length")), 3);

		TypeRef TR1_N = new TLParametered.TRParametered(E, TR1, TKJava.TNumber.getTypeRef());
		Type    T1_N  = (Type)E.execute(ME.newType(TR1_N));
		Object  O1_N  = E.execute("newInstance", T1_N);
		this.printSection("Parameterized Interface");
		this.assertValue(TR1_N, "P09=>TO1<Number>");
		this.assertValue(T1_N,  "P09=>TO1<Number>");
		this.assertValue(E.execute("invokeByParams", O1_N, "append", 1), O1_N);
		this.startCapture();
		this.assertValue(E.execute(ME.newExpr("invokeByParams", O1_N, "showText")), "8");
		this.assertCaptured("Hello(1)");

		Type T3  = (Type)E.execute(ME.newType(TR3));
		this.printSection("Interface Inheritance");
		this.assertValue(T3, "P09=>TI3");
		this.assertObjectOpersContains(T3, "showText():int");
		this.assertObjectOpersContains(T3, "showText(String):int");

		TKInterface TKI = (TKInterface)E.getTypeManager().getTypeKind(TKInterface.KindName);
		this.printSection("NoName Interface (Duck Type)");
		TypeRef NNITRef = TKI.newDuckTypeSpec(
				ExecSignature.newProcedureSignature("length", TKJava.TInteger.getTypeRef()),
				ExecSignature.newSignature("charAt",
						new TypeRef[] { TKJava.TInteger.getTypeRef() },
						new String[]  { "I" },
						TKJava.TCharacter.getTypeRef())
		).getTypeRef();
		
		this.assertValue(NNITRef, "Duck:<{length():int; charAt(int):char}>");
		this.assertValue(NNITRef.canBeAssignedBy(E, new StringBuilder()), true);
		this.assertValue(NNITRef.canBeAssignedBy(E, new Object() {
				@SuppressWarnings("unused") public int  length()      { return 0; }
			}), false);
		this.assertValue(NNITRef.canBeAssignedBy(E, new Object() {
				@SuppressWarnings("unused") public int  length()      { return   0; }
				@SuppressWarnings("unused") public char charAt(int I) { return 'o'; }
			}), true);
		this.assertValue(TKInterface.isTypeRefStrictInterface(E, NNITRef), false);
		this.assertValue(TKInterface.isTypeRefStrictInterface(E, TR2    ),  true);
		
		// Should be the last one
		this.disableOutputCapture();
		this.println();
	}/* */
	
	/** Prepare Types for the testing */
	private void prepareTypes(boolean IsReCreated) {
		Engine      E  = AllTests.getEngine();
		MExecutable ME = E.getExecutableManager();

		if(!IsReCreated) {
			E.getUnitManager().registerUnitFactory("File://" + TestCaseUnit.UnitFilePrefix + UName);
			return;
		}
	
		UnitBuilder    UB = new UnitBuilders.UBFile(E, TestCaseUnit.UnitFilePrefix, UName, null, (CodeFeeder)null);
		PackageBuilder PB = UB.newPackageBuilder(PName);

		// TO1 -----------------------------------------------------------------------------------------------

		TypeRef TR1 = new TLPackage.TRPackage(PB.getName(), TName1);
		TypeRef TR2 = new TLPackage.TRPackage(PB.getName(), TName2);
		TypeRef TR3 = new TLPackage.TRPackage(PB.getName(), TName3);
		TypeRef TR4 = new TLPackage.TRPackage(PB.getName(), TName4);
		TypeRef TR5 = new TLPackage.TRPackage(PB.getName(), TName5);
		
		TypeSpec TS1 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, 
				TR1,        false,    false, null,
				// Interfaces
				new TypeRef[] { TKJava.TRunnable.getTypeRef(), TKJava.TCharSequence.getTypeRef(), TR2 },
				// ParameterizationInfo
				new ParameterizedTypeInfo(new TypeParameterInfo("V")),
				// MoreData, ExtraInfo
				null,        null			

				
			);

		TBClass TB1 = (TBClass)PB.newTypeBuilder(Accessibility.Public, TS1, null);

		TB1.addOperDirect(Accessibility.Public,
			ME.newSubRoutine(
				//                         Name,  Parameter Types, Parameter Names, Return Type
				ExecSignature.newSignature("run", null,            null,            TKJava.TVoid.getTypeRef()),
				ME.newStack(ME.newExpr("println", "Test from run.")),
				null, null
			),
			null);
		
		TB1.addAttrDirect(Accessibility.Public, Accessibility.Public, Accessibility.Public, "Text", false,
				new DataHolderInfo(
					// TypeRef,                  IValue,  FactoryName,          IsReadable, IsWritable, IsSet, IsExpression, MoreData
					TKJava.TString.getTypeRef(), "Hello", Variable.FactoryName, true,       true,       true,  false,        null
				),
				null, null);

		TB1.addStaticDelegatee("Text");

		TB1.addOperDirect(Accessibility.Public,
			ME.newSubRoutine(
				//                         Name,       
				ExecSignature.newSignature("showText",
				//  Parameter Types,                               Parameter Names,
					new TypeRef[] { TKJava.TString.getTypeRef() }, new String[] { "pFormat" },
				//  Return Type
					TKJava.TInteger.getTypeRef()),
				ME.newStack(
					ME.newExpr("printf", ME.newExpr("getVarValue", "pFormat"), ME.newExpr("this_getAttrValue", "Text")),
					ME.newExpr("length", ME.newExpr("this_getAttrValue", "Text"))
				)
			),
			null);

		TB1.addOperDirect(Accessibility.Public,
			ME.newSubRoutine(
				//                         Name,       Parameter Types, Parameter Names, Return Type
				ExecSignature.newSignature("showText", null,            null,            TKJava.TInteger.getTypeRef()),
				ME.newStack(ME.newExpr("return", ME.newExpr("this_invokeByParams", "showText", "%s")))
			),
			null);

		//  	public Current append(V Value) {
		//  		this.Text += String.format("(%s)", V);
		//  	}
		TB1.addOperDirect(Accessibility.Public,
				ME.newSubRoutine(
					//                         Name,
					ExecSignature.newSignature("append",
						//  Parameter Types,                             
						new TypeRef[] { new TLParameter.TRParameter("V") },
						//  Parameter Names,
						new String[]  { "Value" },
						//  Return Type
						new TLCurrent.TRCurrent()
					),
					ME.newStack(
						ME.newExpr(        "this_setAttrValue", "Text",
							ME.newExpr(    "format",            "%s(%s)",
								ME.newExpr("this_getAttrValue", "Text" ),
								ME.newExpr("getVarValue",       "Value")
							)
						),
						ME.newExpr("getVarValue", Context.StackOwner_VarName)
					)
				),
				null);
		
		PB.addType(Accessibility.Public, TS1, null);

		// TI2 -----------------------------------------------------------------------------------------------
		
		TypeSpec TS2 = ((TKInterface)E.getTypeManager().getTypeKind(TKInterface.KindName)).getTypeSpec(
				// TypeRef, Interfaces, ParameterizationInfo, TargetRef, IsStrict, ExtraInfo
				TR2,        null,       null,                 null,      true,     null
			);

		TypeBuilder TB2 = PB.newTypeBuilder(Accessibility.Public, TS2, null);

		TB2.addAbstractOperDirect(Accessibility.Public,
				//                         Name,       
				ExecSignature.newSignature("showText",
				//  Parameter Types,                               Parameter Names,
					new TypeRef[] { TKJava.TString.getTypeRef() }, new String[] { "pFormat" },
				//  Return Type
					TKJava.TInteger.getTypeRef()),
				Executable.ExecKind.SubRoutine,
				null);
		
		PB.addType(Accessibility.Public, TS2, null);

		// TI3 -----------------------------------------------------------------------------------------------
		
		TypeSpec TS3 = ((TKInterface)E.getTypeManager().getTypeKind(TKInterface.KindName)).getTypeSpec(
				// TypeRef, Interfaces,            ParameterizationInfo, TargetRef, IsStrict, ExtraInfo
				TR3,        new TypeRef[] { TR2 }, null,                 null,      false,    null
			);

		TypeBuilder TB3 = PB.newTypeBuilder(Accessibility.Public, TS3, null);

		TB3.addAbstractOperDirect(Accessibility.Public,
				//                         Name,       Parameter Types, Parameter Names, Return Type
				ExecSignature.newSignature("showText", null,            null,            TKJava.TInteger.getTypeRef()),
				Executable.ExecKind.SubRoutine,
				null);
		
		PB.addType(Accessibility.Public, TS3, null);

		// TI4 -----------------------------------------------------------------------------------------------
		
		TypeSpec TS4 = ((TKInterface)E.getTypeManager().getTypeKind(TKInterface.KindName)).getTypeSpec(
				// TypeRef, Interfaces, ParameterizationInfo, TargetRef, IsStrict, ExtraInfo
				TR4,        null,       null,                 null,      false,    null
			);

		TypeBuilder TB4 = PB.newTypeBuilder(Accessibility.Public, TS4, null);

		TB4.addAbstractOperDirect(Accessibility.Public,
				//                         Name,     Parameter Types, Parameter Names, Return Type
				ExecSignature.newSignature("length", null,            null,            TKJava.TInteger.getTypeRef()),
				Executable.ExecKind.SubRoutine,
				null);
		
		PB.addType(Accessibility.Public, TS4, null);

		// TI5 -----------------------------------------------------------------------------------------------

		// public interface TI5<T:Any, V:Any> {
		//  	public T append(V Value);
		// }	
		TypeSpec TS5 = ((TKInterface)E.getTypeManager().getTypeKind(TKInterface.KindName)).getTypeSpec(
				// TypeRef, Interfaces,
				TR5,        null,
				// ParameterizationInfo,
				new ParameterizedTypeInfo(new TypeParameterInfo("T"), new TypeParameterInfo("V")),
				// TargetRef, IsStrict, ExtraInfo
				null,         false,    null
			);

		TypeBuilder TB5 = PB.newTypeBuilder(Accessibility.Public, TS5, null);

		TB5.addAbstractOperDirect(Accessibility.Public,
				//                         Name,
				ExecSignature.newSignature("append",
				//  Parameter Types,                             
					new TypeRef[] { new TLParameter.TRParameter("V") },
				//  Parameter Names,
					new String[]  { "Value" },
				//  Return Type
					new TLParameter.TRParameter("T")
				),
				Executable.ExecKind.SubRoutine,
				null);
		
		PB.addType(Accessibility.Public, TS5, null);

		// SAVE ================================================================================================

		UB.save();
	}
}
