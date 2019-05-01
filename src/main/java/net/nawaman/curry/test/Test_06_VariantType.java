package net.nawaman.curry.test;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.Accessibility;
import net.nawaman.curry.Engine;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.PackageBuilder;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TKVariant;
import net.nawaman.curry.TLPackage;
import net.nawaman.curry.TLParameter;
import net.nawaman.curry.TLParametered;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeParameterInfo;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.TypeSpec;
import net.nawaman.curry.UnitBuilder;
import net.nawaman.curry.UnitBuilders;
import net.nawaman.curry.test.AllTests.TestCase;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseUnit;
import net.nawaman.util.UNumber;

public class Test_06_VariantType extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }

	static private String  UName  = "TU06";
	static private String  PName  = "P06";
	static private String  TName1 = "TV1";
	static private String  TName2 = "TV2";
	static private String  TName3 = "TV3";

	/** Actually do this test case by running all the test values */
	@Override protected void doTest(final String ... Args) {
		// Ensure the engine
		Engine      E  = AllTests.getEngine();
		MExecutable ME = E.getExecutableManager();

		// Prepare the types
		this.prepareTypes(/* ReCreate? (not load from the package file) */ true);

		TypeRef TR1 = new TLPackage.TRPackage(PName, TName1);
		TypeRef TR2 = new TLPackage.TRPackage(PName, TName2);
		TypeRef TR3 = new TLPackage.TRPackage(PName, TName3);
		
		TKVariant TKV = ((TKVariant)E.getTypeManager().getTypeKind(TKVariant.KindName));
		
		TypeRef TRV1 = TKV.getTypeSpec(null,
				TKJava.TNumber.getTypeRef(),
				new TypeRef[] {
					TKJava.TByte.getTypeRef(),
					TKJava.TShort.getTypeRef(),
					TKJava.TInteger.getTypeRef(),
					TKJava.TLong.getTypeRef(),
					TKJava.TBigInteger.getTypeRef()
				},
				TKJava.TInteger.getTypeRef(),
				null, null, null
			).getTypeRef();
		
		TypeRef TRV2 = TKV.getTypeSpec(null,
				TKJava.TNumber.getTypeRef(),
				new TypeRef[] {
					TKJava.TFloat.getTypeRef(),
					TKJava.TDouble.getTypeRef(),
					TKJava.TBigDecimal.getTypeRef()
				},
				TKJava.TDouble.getTypeRef(),
				null, null, null
			).getTypeRef();
		
		// Should be after prepare Engine
		this.enableOutputCapture();

		this.printSection("Simple Variant Type");
		this.assertValue(TRV1,             "Variant:<Number||byte|short|:int:|long|BigInteger>");
		this.assertValue(TRV2,             "Variant:<Number||float|:double:|BigDecimal>");
		this.assertValue(ME.newType(TRV1), "Variant:<Number||byte|short|:int:|long|BigInteger>");
		this.assertValue(ME.newType(TRV2), "Variant:<Number||float|:double:|BigDecimal>");

		this.printSection("Basic Compatible");
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV1),                (byte) 5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV1),                (short)5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV1),                       5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV1),                (long) 5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV1), UNumber.getBigInteger(5)  ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV1),                (float)5.0 ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV1),                       5.0 ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV1), UNumber.getBigDecimal(5.0)), false);
		this.println();
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV2),                (byte) 5   ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV2),                (short)5   ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV2),                       5   ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV2),                (long) 5   ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV2), UNumber.getBigInteger(5)  ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV2),                (float)5.0 ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV2),                       5.0 ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TRV2), UNumber.getBigDecimal(5.0)),  true);

		this.printSection("Saved Basic Compatible");
		this.assertValue(TR1, "P06=>TV1");
		this.assertValue(TR2, "P06=>TV2");
		this.assertValue(ME.newType(TR1), "P06=>TV1");
		this.assertValue(ME.newType(TR2), "P06=>TV2");
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR1),                (byte) 5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR1),                (short)5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR1),                       5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR1),                (long) 5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR1), UNumber.getBigInteger(5)  ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR1),                (float)5.0 ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR1),                       5.0 ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR1), UNumber.getBigDecimal(5.0)), false);
		this.println();
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR2),                (byte) 5   ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR2),                (short)5   ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR2),                       5   ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR2),                (long) 5   ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR2), UNumber.getBigInteger(5)  ), false);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR2),                (float)5.0 ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR2),                       5.0 ),  true);
		this.assertValue(ME.newExpr("instanceOf", ME.newType(TR2), UNumber.getBigDecimal(5.0)),  true);
		
		// Many more test
		
		TypeRef TRV_Ns = TKV.getTypeSpec(null,
				TKJava.TAny.getTypeRef(),
				new TypeRef[] {
					new TLParameter.TRParameter("N"),
					TKArray.newArrayTypeRef(new TLParameter.TRParameter("N"))
				},
				new TLParameter.TRParameter("N"),
				new ParameterizedTypeInfo(new TypeParameterInfo("N", TKJava.TNumber.getTypeRef())),
				null, null
			).getTypeRef();
		TypeRef TRV_Is = new TLParametered.TRParametered(E, TRV_Ns, TKJava.TInteger.getTypeRef());
		
		this.printSection("Variant Type with parameterization");
		this.assertValue(TRV_Ns, "Variant:<any||:Parameter{N}:|Parameter{N}[]> <N:Number>");
		this.assertValue(TRV_Is, "Variant:<any||:Parameter{N}:|Parameter{N}[]> <N:Number><int>");

		Type TV_Ns = (Type)E.execute(ME.newType(TRV_Ns));
		Type TV_Is = (Type)E.execute(ME.newType(TRV_Is));
		
		this.assertValue(TV_Ns, "Variant:<any||:Number:|Number[]>");
		this.assertValue(TV_Is, "Variant:<any||:int:|int[]>");
		
		this.assertValue(ME.newExpr("instanceOf", TV_Ns,                  5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", TV_Ns,                 "5"  ), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Ns, new int[]     {  5  }), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Ns, new Integer[] {  5  }),  true);
		this.assertValue(ME.newExpr("instanceOf", TV_Ns, new String[]  { "5" }), false);
		
		this.assertValue(ME.newExpr("instanceOf", TV_Is,                  5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", TV_Is,            (byte)5   ), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Is,                 "5"  ), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Is, new Number[]  {  5  }), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Is, new int[]     {  5  }), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Is, new Integer[] {  5  }),  true);
		this.assertValue(ME.newExpr("instanceOf", TV_Is, new String[]  { "5" }), false);
		

		this.printSection("Saved Variant Type with parameterization");
		TRV_Ns = TR3;
		TRV_Is = new TLParametered.TRParametered(E, TR3, TKJava.TInteger.getTypeRef());
		this.assertValue(TRV_Ns, "P06=>TV3");
		this.assertValue(TRV_Is, "P06=>TV3<int>");
		
		this.assertValue(ME.newExpr("instanceOf", TV_Ns,                  5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", TV_Ns,                 "5"  ), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Ns, new int[]     {  5  }), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Ns, new Integer[] {  5  }),  true);
		this.assertValue(ME.newExpr("instanceOf", TV_Ns, new String[]  { "5" }), false);
		
		this.assertValue(ME.newExpr("instanceOf", TV_Is,                  5   ),  true);
		this.assertValue(ME.newExpr("instanceOf", TV_Is,            (byte)5   ), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Is,                 "5"  ), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Is, new Number[]  {  5  }), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Is, new int[]     {  5  }), false);
		this.assertValue(ME.newExpr("instanceOf", TV_Is, new Integer[] {  5  }),  true);
		this.assertValue(ME.newExpr("instanceOf", TV_Is, new String[]  { "5" }), false);

		this.disableOutputCapture();
		
		// Should be the last one
		this.disableOutputCapture();
		this.println();
	}/* */
	
	/** Prepare Types for the testing */
	private void prepareTypes(boolean IsReCreated) {
		Engine      E  = AllTests.getEngine();
		//MExecutable ME = E.getExecutableManager();

		if(!IsReCreated) {
			E.getUnitManager().registerUnitFactory("File://" + TestCaseUnit.UnitFilePrefix + UName);
			return;
		}
	
		UnitBuilder    UB = new UnitBuilders.UBFile(E, TestCaseUnit.UnitFilePrefix, UName, null, (CodeFeeder)null);
		PackageBuilder PB = UB.newPackageBuilder(PName);

		TypeRef TR1 = new TLPackage.TRPackage(PB.getName(), TName1);
		TypeRef TR2 = new TLPackage.TRPackage(PB.getName(), TName2);
		TypeRef TR3 = new TLPackage.TRPackage(PB.getName(), TName3);
		
		// TV1 ---------------------------------------------------------------------------------------------------------
		
		TypeSpec TS1 = ((TKVariant)E.getTypeManager().getTypeKind(TKVariant.KindName)).getTypeSpec(TR1,
				TKJava.TNumber.getTypeRef(),
				new TypeRef[] {
					TKJava.TByte.getTypeRef(),
					TKJava.TShort.getTypeRef(),
					TKJava.TInteger.getTypeRef(),
					TKJava.TLong.getTypeRef(),
					TKJava.TBigInteger.getTypeRef()
				},
				TKJava.TInteger.getTypeRef(),
				null, null, null
			);
		
		PB.addType(Accessibility.Public, TS1, null);
		
		// TV2 ---------------------------------------------------------------------------------------------------------
		
		TypeSpec TS2 = ((TKVariant)E.getTypeManager().getTypeKind(TKVariant.KindName)).getTypeSpec(TR2,
				TKJava.TNumber.getTypeRef(),
				new TypeRef[] {
					TKJava.TFloat.getTypeRef(),
					TKJava.TDouble.getTypeRef(),
					TKJava.TBigDecimal.getTypeRef()
				},
				TKJava.TDouble.getTypeRef(),
				null, null, null
			);
		
		PB.addType(Accessibility.Public, TS2, null);

		// TV3 ---------------------------------------------------------------------------------------------------------
		
		TypeSpec TS3 = ((TKVariant)E.getTypeManager().getTypeKind(TKVariant.KindName)).getTypeSpec(TR3,
				TKJava.TAny.getTypeRef(),
				new TypeRef[] {
					new TLParameter.TRParameter("N"),
					TKArray.newArrayTypeRef(new TLParameter.TRParameter("N"))
				},
				new TLParameter.TRParameter("N"),
				new ParameterizedTypeInfo(new TypeParameterInfo("N", TKJava.TNumber.getTypeRef())),
				null, null
			);
			
		PB.addType(Accessibility.Public, TS3, null);
		
		// SAVE ================================================================================================

		UB.save();
	}
}
