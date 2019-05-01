package net.nawaman.curry.test;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Context;
import net.nawaman.curry.DObject;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.UnitBuilder;
import net.nawaman.curry.UnitBuilders;
import net.nawaman.curry.Variable;
import net.nawaman.curry.TLCurrent.TRCurrent;
import net.nawaman.curry.extra.type_object.TBClass;
import net.nawaman.curry.extra.type_object.TKClass;
import net.nawaman.curry.test.AllTests.TestCase;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseUnit;
import net.nawaman.curry.util.DataHolderInfo;

public class Test_10_CurrentType extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }

	static private String  UName  = "TU10";
	static private String  PName  = "P10";
	static private String  TName1 = "TO1";
	static private String  TName2 = "TO2";
	static private String  TName3 = "TO3";
	static private String  TName4 = "TO4";
	static private String  TName5 = "TO5";
	static private String  TName6 = "TO6";

	// public class TO1 {
	//  	public Current TheCurrent = null;
	//  	public Current getCurrent(Current C) {
	//  		@:print(this.getType().getTypeRef()));
	//  		@:printf("\tDelegated from: %s.\n", $Context$.getDelegateSource());
	//  		return this;
	//  	}
	// }
	//
	// public class TO2 extends TO1 {}
	//
	// public class TO3 {
	//  	@StaticDelegate
	//  	public TO1 MainDelegate = new TO1();
	// }
	//
	// public class TO4 {
	//  	@StaticDelegate
	//  	public TO2 MainDelegate = new TO2();
	// }
	//
	// public class TO5 extends TO2 {}
	//
	// public class TO6 extends TO3 {}

	/** Actually do this test case by running all the test values */
	@Override protected void doTest(final String ... Args) {
		// Ensure the engine
		Engine      E  = AllTests.getEngine();
		MExecutable ME = E.getExecutableManager();

		// Prepare the types
		this.prepareTypes(/* ReCreate? (not load from the package file) */ true);
		
		// Should be after prepare Engine
		this.enableOutputCapture();

		TypeRef TR1 = new TLPackage.TRPackage(PName, TName1);
		TypeRef TR2 = new TLPackage.TRPackage(PName, TName2);
		TypeRef TR3 = new TLPackage.TRPackage(PName, TName3);
		TypeRef TR4 = new TLPackage.TRPackage(PName, TName4);
		TypeRef TR5 = new TLPackage.TRPackage(PName, TName5);
		TypeRef TR6 = new TLPackage.TRPackage(PName, TName6);

		Type T1 = (Type)E.execute(ME.newType(TR1));
		Type T2 = (Type)E.execute(ME.newType(TR2));
		Type T3 = (Type)E.execute(ME.newType(TR3));
		Type T4 = (Type)E.execute(ME.newType(TR4));
		Type T5 = (Type)E.execute(ME.newType(TR5));
		Type T6 = (Type)E.execute(ME.newType(TR6));

		this.printSection("Type initialization");
		this.assertValue(TR1, ME.newExpr("toString", T1));
		this.assertValue(TR2, ME.newExpr("toString", T2));
		this.assertValue(TR3, ME.newExpr("toString", T3));
		this.assertValue(TR4, ME.newExpr("toString", T4));
		this.assertValue(TR5, ME.newExpr("toString", T5));
		this.assertValue(TR6, ME.newExpr("toString", T6));

		this.printSection("List of non-static attributes");
		this.assertObjectAttrsContains(T1, "TheCurrent:P10=>TO1");
		this.assertObjectAttrsContains(T2, "TheCurrent:P10=>TO2");
		this.assertObjectAttrsContains(T3, "MainDelegate:P10=>TO1", "@Delegate TheCurrent => this.MainDelegate.TheCurrent");
		this.assertObjectAttrsContains(T4, "MainDelegate:P10=>TO2", "@Delegate TheCurrent => this.MainDelegate.TheCurrent");
		this.assertObjectAttrsContains(T5, "TheCurrent:P10=>TO5");
		this.assertObjectAttrsContains(T6, "@Delegate TheCurrent => this.MainDelegate.TheCurrent", "MainDelegate:P10=>TO1");
		
		this.printSection("List of non-static operations");
		this.assertObjectOpersContains(T1, "getCurrent(P10=>TO1):P10=>TO1");
		this.assertObjectOpersContains(T2, "getCurrent(P10=>TO2):P10=>TO2");
		this.assertObjectOpersContains(T3, "@Delegate getCurrent(P10=>TO1):P10=>TO1 => this.MainDelegate.getCurrent(P10=>TO1):P10=>TO1");
		this.assertObjectOpersContains(T4, "@Delegate getCurrent(P10=>TO2):P10=>TO2 => this.MainDelegate.getCurrent(P10=>TO2):P10=>TO2");
		this.assertObjectOpersContains(T5, "getCurrent(P10=>TO5):P10=>TO5");
		this.assertObjectOpersContains(T6, "@Delegate getCurrent(P10=>TO1):P10=>TO1 => this.MainDelegate.getCurrent(P10=>TO1):P10=>TO1");

		this.printSection("getCurrent(...)");
		this.assertValue(T1.getTypeInfo().searchObjectOperation(E, "getCurrent", new TypeRef[]{ T1.getTypeRef() }), "getCurrent(P10=>TO1):P10=>TO1");
		this.assertValue(T2.getTypeInfo().searchObjectOperation(E, "getCurrent", new TypeRef[]{ T2.getTypeRef() }), "getCurrent(P10=>TO2):P10=>TO2");
		this.assertValue(T3.getTypeInfo().searchObjectOperation(E, "getCurrent", new TypeRef[]{ T1.getTypeRef() }), "getCurrent(P10=>TO1):P10=>TO1");
		this.assertValue(T4.getTypeInfo().searchObjectOperation(E, "getCurrent", new TypeRef[]{ T2.getTypeRef() }), "getCurrent(P10=>TO2):P10=>TO2");
		this.assertValue(T5.getTypeInfo().searchObjectOperation(E, "getCurrent", new TypeRef[]{ T5.getTypeRef() }), "getCurrent(P10=>TO5):P10=>TO5");
		this.assertValue(T6.getTypeInfo().searchObjectOperation(E, "getCurrent", new TypeRef[]{ T1.getTypeRef() }), "getCurrent(P10=>TO1):P10=>TO1");

		this.printSection("TheCurrent");
		this.assertValue(T1.getTypeInfo().searchObjectAttribute(E, "TheCurrent"), "P10=>TO1");
		this.assertValue(T2.getTypeInfo().searchObjectAttribute(E, "TheCurrent"), "P10=>TO2");
		this.assertValue(T3.getTypeInfo().searchObjectAttribute(E, "TheCurrent"), "P10=>TO1");
		this.assertValue(T4.getTypeInfo().searchObjectAttribute(E, "TheCurrent"), "P10=>TO2");
		this.assertValue(T5.getTypeInfo().searchObjectAttribute(E, "TheCurrent"), "P10=>TO5");
		this.assertValue(T6.getTypeInfo().searchObjectAttribute(E, "TheCurrent"), "P10=>TO1");

		Object O1 = T1.newInstance((Object[])null);
		Object O2 = T2.newInstance((Object[])null);
		Object O3 = T3.newInstance((Object[])null);
		Object O4 = T4.newInstance((Object[])null);
		Object O5 = T5.newInstance((Object[])null);
		Object O6 = T6.newInstance((Object[])null);

		this.printSection("O.getCurrent(O)");
		this.assertValue(((DObject)O1).invoke("getCurrent", O1), O1);                                        this.assertCaptured("P10=>TO1\n\tD::null\n");
		this.assertValue(((DObject)O2).invoke("getCurrent", O2), O2);                                        this.assertCaptured("P10=>TO2\n\tD::null\n");
		this.assertValue(((DObject)O3).invoke("getCurrent", O1), ((DObject)O3).getAttrData("MainDelegate")); this.assertCaptured("P10=>TO1\n\tD::"+((DObject)O3).invoke("toString")+"\n");
		this.assertValue(((DObject)O4).invoke("getCurrent", O2), ((DObject)O4).getAttrData("MainDelegate")); this.assertCaptured("P10=>TO2\n\tD::"+((DObject)O4).invoke("toString")+"\n");
		this.assertValue(((DObject)O5).invoke("getCurrent", O5), O5);                                        this.assertCaptured("P10=>TO5\n\tD::null\n");
		this.assertValue(((DObject)O6).invoke("getCurrent", O1), ((DObject)O6).getAttrData("MainDelegate")); this.assertCaptured("P10=>TO1\n\tD::"+((DObject)O6).invoke("toString")+"\n");
		
		this.printSection("O.TheCurrent");
		this.assertValue(((DObject)O1).getAttrData("TheCurrent"), null);
		this.assertValue(((DObject)O2).getAttrData("TheCurrent"), null);
		this.assertValue(((DObject)O3).getAttrData("TheCurrent"), null);
		this.assertValue(((DObject)O4).getAttrData("TheCurrent"), null);
		this.assertValue(((DObject)O5).getAttrData("TheCurrent"), null);
		this.assertValue(((DObject)O6).getAttrData("TheCurrent"), null);
		
		this.printSection("Test NoNull Default Value");
		this.assertStringContains(ME.newExpr("tryNoNull", null, T1), "P10=>TO1");
		
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
		PackageBuilder PB  = UB.newPackageBuilder(PName);

		// TO1 -----------------------------------------------------------------------------------------------

		TypeRef  TR1 = new TLPackage.TRPackage(PB.getName(), TName1);
		TypeSpec TS1 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, Interfaces, ParameterizationInfo, MoreData, ExtraInfo
				TR1,        false,    false, null,  null,       null,                 null,     null
			);

		TBClass TB1 = (TBClass)PB.newTypeBuilder(Accessibility.Public, TS1, null);

		TB1.addOperDirect(Accessibility.Public,
			ME.newSubRoutine(
				//                         Name,         Parameter Types,                   Parameter Names,       Return Type
				ExecSignature.newSignature("getCurrent", new TypeRef[] { new TRCurrent() }, new String[]  { "C" }, new TRCurrent()),
				ME.newStack(
					ME.newExpr("println", ME.newExpr("getTypeInfo", ME.newExpr("getTypeOf", ME.newExpr("getVarValue", Context.StackOwner_VarName)), "TypeRef")),
					ME.newExpr("printf", "\tD::%s\n", ME.newExpr("getContextInfo", "DelegateSource")),
					ME.newExpr("return", ME.newExpr("getVarValue", Context.StackOwner_VarName))
				),
				null, null
			),
			null);

		TB1.addStaticOperDirect(Accessibility.Public,
			ME.newSubRoutine(
				//                                  Name,                    Return Type
				ExecSignature.newProcedureSignature("getNoNullDefaultValue", new TRCurrent()),
				ME.newStack(
					ME.newExpr("println", "This is inside `getNoNullDefaultValue`"),
					ME.newExpr("return", ME.newExpr("newInstance", ME.newExpr("getVarValue", Context.StackOwnerAsType_VarName)))
				),
				null, null
			),
			null);
		
		TB1.addAttrDirect("TheCurrent", false, new TRCurrent(), null, false, null, null, null);

		// TO2 -----------------------------------------------------------------------------------------------

		TypeRef  TR2 = new TLPackage.TRPackage(PB.getName(), TName2);
		TypeSpec TS2 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, Interfaces, ParameterizationInfo, MoreData, ExtraInfo
				TR2,        false,    false, TR1,   null,       null,                 null,     null
			);

		PB.addType(Accessibility.Public, TS2, null);

		// TO3 -----------------------------------------------------------------------------------------------

		TypeRef  TR3 = new TLPackage.TRPackage(PB.getName(), TName3);
		TypeSpec TS3 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, Interfaces, ParameterizationInfo, MoreData, ExtraInfo
				TR3,        false,    false, null,  null,       null,                 null,     null
			);

		TBClass TB3 = (TBClass)PB.newTypeBuilder(Accessibility.Public, TS3, null);
		
		TB3.addStaticDelegatee("MainDelegate");
		TB3.addAttrDirect(Accessibility.Public, Accessibility.Public, Accessibility.Public, "MainDelegate", false,
			new DataHolderInfo(
				// TypeRef, IValue,                                             FactoryName
				TR1,        ME.newExpr("newInstance", ME.newExpr("type", TR1)), Variable.FactoryName,
				// IsReadable, IsWritable, IsSet, IsExpression, MoreData
				true,          true,       true,  true,         null
			),
			null, null);

		// TO4 -----------------------------------------------------------------------------------------------

		TypeRef  TR4 = new TLPackage.TRPackage(PB.getName(), TName4);
		TypeSpec TS4 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, Interfaces, ParameterizationInfo, MoreData, ExtraInfo
				TR4,        false,    false, null,  null,       null,                 null,     null
			);

		TBClass TB4 = (TBClass)PB.newTypeBuilder(Accessibility.Public, TS4, null);
		
		TB4.addStaticDelegatee("MainDelegate");
		TB4.addAttrDirect(Accessibility.Public, Accessibility.Public, Accessibility.Public, "MainDelegate", false,
			new DataHolderInfo(
				// TypeRef, IValue,                                             FactoryName
				TR2,        ME.newExpr("newInstance", ME.newExpr("type", TR2)), Variable.FactoryName,
				// IsReadable, IsWritable, IsSet, IsExpression, MoreData
				true,          true,       true,  true,         null
			),
			null, null);

		// TO5 -----------------------------------------------------------------------------------------------

		TypeRef  TR5 = new TLPackage.TRPackage(PB.getName(), TName5);
		TypeSpec TS5 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, Interfaces, ParameterizationInfo, MoreData, ExtraInfo
				TR5,        false,    false, TR2,   null,       null,                 null,     null
			);

		PB.addType(Accessibility.Public, TS5, null);
		
		// TO6 -----------------------------------------------------------------------------------------------

		TypeRef  TR6 = new TLPackage.TRPackage(PB.getName(), TName6);
		TypeSpec TS6 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, Interfaces, ParameterizationInfo, MoreData, ExtraInfo
				TR6,        false,    false, TR3,   null,       null,                 null,     null
			);

		PB.addType(Accessibility.Public, TS6, null);

		// SAVE ================================================================================================

		UB.save();
	}
}
