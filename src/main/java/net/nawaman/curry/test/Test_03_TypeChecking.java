package net.nawaman.curry.test;

import java.util.Arrays;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.Accessibility;
import net.nawaman.curry.CurryError;
import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.MType;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TKVariant;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeParameterInfo;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.UnitBuilder;
import net.nawaman.curry.UnitBuilders;
import net.nawaman.curry.extra.type_object.TBClass;
import net.nawaman.curry.extra.type_object.TKClass;
import net.nawaman.curry.test.AllTests.TestCase;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseUnit;

public class Test_03_TypeChecking extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }

	static private String  UName  = "TU03";
	static private String  PName  = "P03";
	static private String  TName1 = "TO1";
	
	// public class TO1<V:Any> implements Runnable {
	//  	public void run() {
	//  		@:println($This$ + " is running.");
	//  		return this;
	//  	}
	// }

	/**{@inheritDoc}*/ @Override
	protected void doTest(final String ... Args) {
		// Ensure the engine
		Engine E  = AllTests.getEngine();
		MType  MT = E.getTypeManager();

		// Prepare the types
		this.prepareTypes(/* ReCreate? (not load from the package file) */ true);

		TypeRef TR1 = new TLPackage.TRPackage(PName, TName1);
		
		this.printSection("Type from String");
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(null, null, null, "int")),
			"`int`:net.nawaman.curry.TLPrimitive.TRPrimitive"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(null, null, null, "BigInteger")),
			"`BigInteger`:net.nawaman.curry.TLPrimitive.TRPrimitive"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(null, null, null, "Throwable")),
			"`Throwable`:net.nawaman.curry.TLPrimitive.TRPrimitive"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(null, null, TKArray.AnyArrayRef, "T")),
			"`Parameter{T}`:net.nawaman.curry.TLParameter.TRParameter"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(null, null, TR1, "V")),
			"`Parameter{V}`:net.nawaman.curry.TLParameter.TRParameter"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(null, null, null, "java.io.File")),
			"`java.io.File`:net.nawaman.curry.TLPrimitive.TRPrimitive"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { "java.io." }),     null, null, "File")),
			"`java.io.File`:net.nawaman.curry.TLPrimitive.TRPrimitive"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { "java.io.File" }), null, null, "File")),
			"`java.io.File`:net.nawaman.curry.TLPrimitive.TRPrimitive"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { "net.nawaman.curry.Executable" }), null, null, "Executable.Fragment")),
			"`Fragment`:net.nawaman.curry.TLPrimitive.TRPrimitive"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { "net.nawaman.util.UNumber" }), null, null, "UNumber.NumberType")),
			"`net.nawaman.util.UNumber.NumberType`:net.nawaman.curry.TLPrimitive.TRPrimitive"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(null, null, null, PName + "=>" + TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(null, null, null, PName + "." + TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { PName + "=>" }), null, null, TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { PName + "." }), null, null, TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { PName + "." + TName1 }), null, null, TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { PName + "=>" + TName1 }), null, null, TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { PName + "." + TName1 }), null, null, PName + "=>" + TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { PName + "=>" + TName1 }), null, null, PName + "=>" + TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { PName + "." + TName1 }), null, null, PName + "." + TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);
		this.assertValue(
			E.getDisplayObject(MT.searchTypeRef(Arrays.asList(new String[] { PName + "=>" + TName1 }), null, null, PName + "." + TName1)),
			"`P03=>TO1`:net.nawaman.curry.TLPackage.TRPackage_Internal"
		);

		MExecutable ME = E.getExecutableManager();
		
		// Variant Type <Byte | Short | Integer | Long || Number>
		TypeSpec TS = ((TKVariant)MT.getTypeKind(TKVariant.KindName)).getTypeSpec(
				null, TKJava.TNumber.getTypeRef(),
				new TypeRef[] {
					TKJava.TByte   .getTypeRef(),
					TKJava.TShort  .getTypeRef(),
					TKJava.TInteger.getTypeRef(),
					TKJava.TLong   .getTypeRef()
				},
				TKJava.TInteger.getTypeRef(), null, null, null);
		
		TypeRef TIntegerNumberRef = TS.getTypeRef();
		Type    TIntegerNumber    = (Type)E.execute(ME.newType(TIntegerNumberRef));

		this.printSection("Simple Types");
		this.assertValue(TKJava.TInteger, E.getTypeManager().getTypeOf(                          5));
		this.assertValue(true,            TKJava.TNumber.getTypeInfo().canBeAssignedBy(          5));
		this.assertValue(true,            TKJava.TNumber.getTypeInfo().canBeAssignedBy(          (byte)5));
		this.assertValue(true,            TKJava.TNumber.getTypeInfo().canBeAssignedByInstanceOf(TKJava.TInteger));

		this.printSection("Variant Types");
		this.assertValue("Variant:<Number||byte|short|:int:|long>", TIntegerNumberRef.toString());
		this.assertValue("Variant:<Number||byte|short|:int:|long>", TIntegerNumber.toString());
		this.assertValue(true,           TIntegerNumber.getTypeInfo().canBeAssignedBy(          5));
		this.assertValue(true,           TIntegerNumber.getTypeInfo().canBeAssignedBy(          (byte)5));
		this.assertValue(true,           TIntegerNumber.getTypeInfo().canBeAssignedBy(          (long)5));
		this.assertValue(true,           TIntegerNumber.getTypeInfo().canBeAssignedByInstanceOf(TKJava.TInteger));
		this.assertValue(true,           TIntegerNumber.getTypeInfo().canBeAssignedByInstanceOf(TKJava.TLong));
		this.assertValue(false,          TIntegerNumber.getTypeInfo().canBeAssignedByInstanceOf(TKJava.TDouble));
		this.assertValue(Number[].class, ME.newExpr("invokeByParams", ME.newExpr("newArray", TIntegerNumber, 5), "getClass"));
		this.assertValue(5,              ME.newExpr("newInstance", TIntegerNumber, 5));
		this.assertValue(5,              ME.newExpr("newInstance", TIntegerNumber, "5"));
		this.assertValue(true,           TKJava.TNumber.getTypeInfo().canBeAssignedByInstanceOf(TIntegerNumber));

		this.printSection("Type of Type");
		this.assertValue("Type:<Number>", ME.newExpr("getTypeOf", ME.newType(TKJava.TNumber.getTypeRef())));
		this.assertValue( true, ME.newExpr("instanceOf", ME.newExpr("getTypeOf", ME.newType(TKJava.TNumber.getTypeRef())), TKJava.TNumber));
		this.assertValue( true, ME.newExpr("instanceOf", ME.newExpr("getTypeOf", ME.newType(TKJava.TNumber.getTypeRef())), TKJava.TByte));
		this.assertValue(false, ME.newExpr("instanceOf", ME.newExpr("getTypeOf", ME.newType(TKJava.TNumber.getTypeRef())), TKJava.TString));
		this.assertValue("Type:<Type:<Number>>", ME.newExpr("getTypeOf", ME.newExpr("getTypeOf", ME.newType(TKJava.TNumber.getTypeRef()))));
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

		TypeRef TR1 = new TLPackage.TRPackage(PB.getName(), TName1);
		
		TypeSpec TS1 = ((TKClass)E.getTypeManager().getTypeKind(TKClass.KindName)).getTypeSpec(
				// TypeRef, Abstract, Final, Super, 
				TR1,        false,    false, null,
				// Interfaces
				new TypeRef[] { TKJava.TRunnable.getTypeRef() },
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
				ME.newStack(ME.newExpr("printf", "%s is running.\n", ME.newExpr("type_invokeByParams", "toString"))),
				null, null
			),
			null);

		// SAVE ================================================================================================

		Exception Exec = UB.save();
		if(Exec != null) throw new CurryError("Type preparation fail: ", Exec);
	}
}
