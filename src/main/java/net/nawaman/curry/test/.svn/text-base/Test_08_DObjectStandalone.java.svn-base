package net.nawaman.curry.test;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Expression;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.Type;
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

public class Test_08_DObjectStandalone extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }

	static private String  UName  = "TU08";
	static private String  PName  = "P08";
	static private String  TName1 = "TO1";
	static private String  TName2 = "TO2";

	static public class TestCS {
		public TestCS(CharSequence pCS) {
			this.CS = pCS;
		}
		CharSequence CS = null;
		public void test() {
			this.CS.toString();
			this.CS.subSequence(0, 5);
			this.CS.length();
			this.CS.charAt(0);
		}
	}
	
	// public class TO1 implements Runnable {
	//  	public void run() {
	//  		@:println(Test from run.);
	//  	}
	// }
	// public class TO2 implements CharSequence {
	//  	public String Text = "Hello" @StaticDelegate;
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
		this.assertValue(TR1, "P08=>TO1");
		this.assertValue(T1,  "P08=>TO1");
		this.assertValue(E.execute("instanceOf", T,  O1),    true);
		this.assertValue(E.execute("isKindOf",   T,  T1),    true);

		this.printSection("Java Thread Interface");

		// (new ThreadO1()).start();
		TRef     = TKJava.TThread.getTypeRef();
		T        = (Type)E.execute(ME.newType(TRef));
		Object O = E.execute("newInstance", T, O1);
		//this.startCapture();
		E.execute("invokeByParams", O, "start");
		//try { Thread.sleep(100); } catch (Exception e) {}
		//this.assertCaptured("Test from run.\n");
		
		
		
		TypeRef TR2 = new TLPackage.TRPackage(PName, TName2);
		Type    T2  = (Type)E.execute(ME.newType(TR2));
		Object  O2  = E.execute("newInstance", T2);

		T = (Type)E.execute("getTypeOfClass", TestCS.class);
		O = E.execute("newInstance", T, O2);
		
		this.printSection("CharSequence Interface");
		this.assertValue(E.execute("invokeByParams", O2, "toString"),          "Hello");
		this.assertValue(E.execute("invokeByParams", O2, "subSequence", 0, 5), "Hello");
		this.assertValue(E.execute("invokeByParams", O2, "length"),                  5);
		this.assertValue(E.execute("invokeByParams", O2, "charAt",         0),     'H');


		Expression Expr = ME.newExpr("invokeByParams", O, "test");
		
		this.printSection("Speed tests");

		long EngTime   = 0;
		long StartTime = 0;
		long AvgTime   = 0;
		int  LoopCount = 256;
		
		for(int i = LoopCount; --i >= 0; ) {
			StartTime = System.nanoTime();
			E.execute("invokeByParams", O, "test");
			EngTime = System.nanoTime();
			AvgTime += (EngTime - StartTime);
		}
		this.printf("Time (Curry Direct): %8d / %d loops\n", AvgTime/LoopCount, LoopCount);

		AvgTime = 0;
		for(int i = LoopCount; --i >= 0; ) {
			StartTime = System.nanoTime();
			E.execute(Expr);
			EngTime = System.nanoTime();
			AvgTime += (EngTime - StartTime);
		}
		this.printf("Time (Curry Stack) : %8d / %d loops\n", AvgTime/LoopCount, LoopCount);
		
		String Txt = "Hello";
		O = new TestCS(Txt);
		Expr = ME.newExpr("invokeByParams", O, "test");

		AvgTime = 0;
		for(int i = LoopCount; --i >= 0; ) {
			StartTime = System.nanoTime();
			E.execute(Expr);
			EngTime = System.nanoTime();
			AvgTime += (EngTime - StartTime);
		}
		this.printf("Time (Java Curry)  : %8d / %d loops\n", AvgTime/LoopCount, LoopCount);

		AvgTime = 0;
		for(int i = LoopCount; --i >= 0; ) {
			StartTime = System.nanoTime();
			Txt.toString();
			Txt.subSequence(0, 5);
			Txt.length();
			Txt.charAt(0);
			EngTime = System.nanoTime();
			AvgTime += (EngTime - StartTime);
		}
		this.printf("Time (Java Direct) : %8d / %d loops\n", AvgTime/LoopCount, LoopCount);
		
		this.disableOutputCapture();
		
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

		TypeRef TR1 = new TLPackage.TRPackage(PB.getName(), TName1);
		TypeRef TR2 = new TLPackage.TRPackage(PB.getName(), TName2);
		
		// TO1 -----------------------------------------------------------------------------------------------
		
		TypeSpec TS1 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, 
				TR1,        false,    false, null,
				// Interfaces
				new TypeRef[] { TKJava.TRunnable.getTypeRef() },
				// ParameterizationInfo, MoreData, ExtraInfo
				null,                    null,     null
			);

		TBClass TB1 = (TBClass)PB.newTypeBuilder(Accessibility.Public, TS1, null);

		TB1.addOperDirect(Accessibility.Public,
			ME.newSubRoutine(
				//                         Name,  Parameter Types, Parameter Names, Return Type
				ExecSignature.newSignature("run", null,            null,            TKJava.TVoid.getTypeRef()),
				ME.newExpr("println", "Test from run."),
				null, null
			),
			null);
		
		PB.addType(Accessibility.Public, TS1, null);

		// TO2 -----------------------------------------------------------------------------------------------
		
		TypeSpec TS2 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, 
				TR2,        false,    false, null,
				// Interfaces
				new TypeRef[] { TKJava.TCharSequence.getTypeRef() },
				// ParameterizationInfo, MoreData, ExtraInfo
				null,                    null,     null
			);

		TBClass TB2 = (TBClass)PB.newTypeBuilder(Accessibility.Public, TS2, null);

		TB2.addAttrDirect(Accessibility.Public, Accessibility.Public, Accessibility.Public, "Text", false,
				new DataHolderInfo(
					// TypeRef,                  IValue,  FactoryName,          IsReadable, IsWritable, IsSet, IsExpression, MoreData
					TKJava.TString.getTypeRef(), "Hello", Variable.FactoryName, true,       true,       true,  false,        null
				),
				null, null);

		TB2.addStaticDelegatee("Text");

		TB2.addOperDirect(Accessibility.Public,
			ME.newSubRoutine(
				//                         Name,       Parameter Types, Parameter Names, Return Type
				ExecSignature.newSignature("toString", null,            null,            TKJava.TString.getTypeRef()),
				ME.newExpr("toString", ME.newExpr("this_getAttrValue", "Text")),
				null, null
			),
			null);
		
		PB.addType(Accessibility.Public, TS2, null);


		// SAVE ================================================================================================

		UB.save();
	}

}
