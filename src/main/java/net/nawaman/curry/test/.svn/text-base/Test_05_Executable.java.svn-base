package net.nawaman.curry.test;

import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Executable;
import net.nawaman.curry.JavaExecutable;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.ParameterizedTypeInfo;
import net.nawaman.curry.TKExecutable;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.TLParameter;
import net.nawaman.curry.TLParametered;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeParameterInfo;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.test.AllTests.TestCase;
import net.nawaman.util.UNumber;

public class Test_05_Executable extends TestCase {
	
	static public void main(String ... Args) { runTest(Args); }

	/** Actually do this test case by running all the test values */
	@Override protected void doTest(final String ... Args) {
		// Ensure the engine
		Engine      E  = AllTests.getEngine();
		MExecutable ME = E.getExecutableManager();
		
		TKExecutable TKE = ((TKExecutable)E.getTypeManager().getTypeKind(TKExecutable.KindName));

		ExecSignature ExSign1 = ExecSignature.newSignature("factorial",
		                            new TypeRef[] { TKJava.TNumber.getTypeRef() },
		                            new String[]  { "Number" }, TKJava.TNumber.getTypeRef());

		ExecSignature ExSign2 = ExecSignature.newSignature("factorial",
		                            new TypeRef[] { TKJava.TInteger.getTypeRef() },
		                            new String[]  { "Integer" }, TKJava.TInteger.getTypeRef());
		
		Executable.SubRoutine Exec1 = new JavaExecutable.JavaSubRoutine_Simple(ExSign1) {
			// Executing -----------------------------------------------------------
			protected @Override Object run(Object[] pParams) {
				Number N = (Number)pParams[0];
				if(UNumber.isZero(N)) return UNumber.plus(N, (byte)1);
				if(UNumber.isOne(N))  return N;
				return UNumber.multiply(N, (Number)this.run(new Object[] { UNumber.subtract(N, (byte)1) }));
			}
		};
		
		Executable.SubRoutine Exec2 = new JavaExecutable.JavaSubRoutine_Simple(ExSign2) {
			// Executing -----------------------------------------------------------
			protected @Override Object run(Object[] pParams) {
				if(pParams[0] == null) return 1;
				
				int I = ((Integer)pParams[0]).intValue();
				if(I <= 1) return 1;
				return I*((Integer)this.run(new Object[] { I - 1 })).intValue();
			}
		};
		
		// Should be after prepare Engine
		this.enableOutputCapture();

		// Starting ----------------------------------------------------------------------------------------------------

		this.printSection("Simple Executable");
		
		this.startCapture();
		for(int i = 0; i < 20; i++) System.out.println(ME.callSubRoutine(Exec1, i));
		this.assertCaptured(
			"1\n" +
			"1\n" +
			"2\n" +
			"6\n" +
			"24\n" +
			"120\n" +
			"720\n" +
			"5040\n" +
			"40320\n" +
			"362880\n" +
			"3628800\n" +
			"39916800\n" +
			"479001600\n" +
			"6227020800\n" +
			"87178291200\n" +
			"1307674368000\n" +
			"20922789888000\n" +
			"355687428096000\n" +
			"6402373705728000\n" +
			"121645100408832000\n"
		);
		

		for(int i = 0; i < 20; i++) System.out.println(ME.callSubRoutine(Exec2, i));
		this.assertCaptured(
			"1\n" +
			"1\n" +
			"2\n" +
			"6\n" +
			"24\n" +
			"120\n" +
			"720\n" +
			"5040\n" +
			"40320\n" +
			"362880\n" +
			"3628800\n" +
			"39916800\n" +
			"479001600\n" +
			"1932053504\n" +
			"1278945280\n" +
			"2004310016\n" +
			"2004189184\n" +
			"-288522240\n" +
			"-898433024\n" +
			"109641728\n"
		);

		this.printSection("Executable with Frozen Variables");
		

		this.printSection("Executable Type"); // =======================================================================
		TypeRef TRExec1 = TKE.getTypeSpec(null, null, ExSign1, null, null).getTypeRef();
		Type    TExec1  = (Type)E.execute(ME.newType(TRExec1));
		this.printSection("Simple Executable Type");
		this.assertTrue(TExec1.getTypeInfo().canBeAssignedBy(Exec1));
		this.assertValue(TRExec1, "Executable:<(Number):Number>");
		this.assertValue(TExec1,  "Executable:<(Number):Number>");

		this.printSection("Parameterized Executable Type"); // =========================================================
		ExSign1 = ExecSignature.newSignature("factorial",
                new TypeRef[] { new TLParameter.TRParameter("N") },
                new String[]  { "Number" }, new TLParameter.TRParameter("N"));
		
		TypeRef TRExec1_N = TKE.getTypeSpec(null, null, ExSign1,
				new ParameterizedTypeInfo(new TypeParameterInfo("N", TKJava.TNumber.getTypeRef())),
				null, null).getTypeRef();
		TypeRef TRExec1_I = new TLParametered.TRParametered(E, TRExec1_N, TKJava.TInteger.getTypeRef());

		Type TExec1_N = (Type)E.execute(ME.newType(TRExec1_N));
		Type TExec1_I = (Type)E.execute(ME.newType(TRExec1_I));
		
		this.assertValue(TRExec1_N, "Executable:<(N):N> <N:Number>");
		this.assertValue(TRExec1_I, "Executable:<(N):N> <N:Number><int>");
		this.assertValue(TExec1_N,  "Executable:<(Number):Number>");
		this.assertValue(TExec1_I,  "Executable:<(int):int>");
		this.assertTrue( TExec1_N.getTypeInfo().canBeAssignedBy(Exec1));
		this.assertFalse(TExec1_I.getTypeInfo().canBeAssignedBy(Exec1));
		this.assertTrue( TExec1_N.getTypeInfo().canBeAssignedBy(Exec2));
		this.assertTrue( TExec1_I.getTypeInfo().canBeAssignedBy(Exec2));
		
		// Ending ------------------------------------------------------------------------------------------------------
		// Should be the last one
		this.disableOutputCapture();
		this.println();
	}
}
