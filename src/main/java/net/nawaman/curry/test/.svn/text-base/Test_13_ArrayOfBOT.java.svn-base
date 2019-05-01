package net.nawaman.curry.test;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Context;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeParameterInfo;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.UnitBuilder;
import net.nawaman.curry.UnitBuilders;
import net.nawaman.curry.Variable;
import net.nawaman.curry.TLCurrent.TRCurrent;
import net.nawaman.curry.TLParameter.TRParameter;
import net.nawaman.curry.extra.type_object.TBClass;
import net.nawaman.curry.extra.type_object.TKClass;
import net.nawaman.curry.test.AllTests.TestCase;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseUnit;
import net.nawaman.curry.util.DataHolderInfo;
import net.nawaman.util.UArray;

public class Test_13_ArrayOfBOT extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }

	static private String  UName      = "TU13";
	static private String  PName      = "P13";
	static private String  TName1     = "TO1";

	// public class TO1<T:Number> {
	//  	public TO1(T pValue) { this.Value = pValue; }
	//  	private T         Value       = 0;
	//  	public  Current[] TheCurrents = null;
	//  	public  Current[] getCurrents(Current ... C) {
	//  		@:print(this.getType().getTypeRef()));
	//  		@:printf("\tDelegated from: %s.\n", $Context$.getDelegateSource());
	//  		return this;
	//  	}
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

		TypeRef TR1 = new TLPackage.TRPackage(PName, TName1);
		Type    T1  = (Type)E.execute(ME.newType(TR1));

		this.printSection("Base Array Types");
		this.assertValue(TR1, "P13=>TO1");
		this.assertValue(T1 , "P13=>TO1<T:Number>");

		this.printSection("Elements");
		this.assertObjectAttrsContains(T1, "TheCurrents:P13=>TO1[]");
		this.assertObjectOpersContains(T1, "getCurrents(P13=>TO1[] ... ):P13=>TO1[]");
		
		this.printSection("Constructor of " + T1.toString());	// so it can be used every where.
		this.assertValue(
			UArray.toString(T1.getTypeInfo().getConstructorInfos(), "[\n\t", "\n]", ",\n\t"),
			"[\n	new(Number):void\n]"
		);
		
		// Should be the last one
		this.disableOutputCapture();
		this.println();
	}
	
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

		TypeRef  TR1 = new TLPackage.TRPackage(PB.getName(), TName1);
		TypeSpec TS1 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, Interfaces,
				TR1,        false,    false, null,  null,
				// ParameterizationInfo
				new ParameterizedTypeInfo(new TypeParameterInfo("T", TKJava.TNumber.getTypeRef())),
				// MoreData, ExtraInfo
				null,        null
			);

		TBClass TB1 = (TBClass)PB.newTypeBuilder(Accessibility.Public, TS1, null);

		TB1.addConstructor(
				Accessibility.Public,
				ME.newMacro(
					ExecSignature.newSignature("new", new TypeRef[] { new TRParameter("T") }, new String[]  { "pValue" }, TKJava.TVoid.getTypeRef()),
					ME.newExpr("this_setAttrValue", "Value", ME.newExpr("getVarValue", "pValue"))
				),
				null
			);
		
		TB1.addOperDirect(Accessibility.Public,
			ME.newSubRoutine(
				//                         Name,         Parameter Types,                   Parameter Names,       Return Type
				ExecSignature.newSignature(
						"getCurrents",
						new TypeRef[] { TKArray.newArrayTypeRef(new TRCurrent()) },
						new String[]  { "C" },
						true, TKArray.newArrayTypeRef(new TRCurrent()), null, null),
				ME.newStack(
					ME.newExpr("printf",
						"%s(%s)\n",
						ME.newExpr("getTypeInfo", ME.newExpr("getTypeOf", ME.newExpr("getVarValue", Context.StackOwner_VarName)), "TypeRef"),
						ME.newExpr("this_getAttrValue", "Value")
					),
					ME.newExpr("printf", "\tD::%s\n", ME.newExpr("getContextInfo", "DelegateSource")),
					ME.newExpr("return", ME.newExpr("getVarValue", Context.StackOwner_VarName))
				),
				null, null
			),
			null);
		
		TB1.addAttrDirect("TheCurrents", false, TKArray.newArrayTypeRef(new TRCurrent()), null, false, null, null, null);
		
		TB1.addAttrDirect(Type.Private, Type.Private, Type.Private, "Value", false, 
				new DataHolderInfo(new TRParameter("T"), 0, Variable.FactoryName, true, false, true, false, null),
				null, null);

		// SAVE ================================================================================================

		UB.save();
	}
}
