package net.nawaman.curry.test.lang.Keang;

import net.nawaman.curry.TKJava;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseParser;

public class Test_03_BasicOperations extends TestCaseParser {
	
	static public void main(String ... Args) { runTest(Args); }
	
	String ParaserTypeName = "Command";
	
	/**{@inheritDoc}*/ @Override
	protected String getParserTypeName() {
		return this.ParaserTypeName;
	}

	/**{@inheritDoc}*/ @Override
	protected void doTest(final String ... Args) {
		this.printSection("TryOrElse");
		this.assertValue("try(5 <:= 10)",  5);
		this.assertValue("try(I <:= 10)", 10);

		this.printSection("Multiplication");
		this.assertValue("5*10",   5*10);
		this.assertValue("10/2",   10/2);
		this.assertValue("10/2/5", 10/2/5);

		this.printSection("Plus");
		this.assertValue("5+10",   5+10);
		this.assertValue("10+2",   10+2);
		this.assertValue("10+2-5", 10+2-5);
		this.assertValue("10+2",   10+2);
		this.assertValue("10+2*5", 10+2*5);

		this.printSection("BitShift");
		this.assertValue("   5 <<  2",    5 <<  2);
		this.assertValue(" 100 >>  2",  100 >>  2);
		this.assertValue(" 100 >>> 2",  100 >>> 2);
		this.assertValue("-100 >>  2", -100 >>  2);
		this.assertValue("-100 >>> 2", -100 >>> 2);

		this.printSection("Compare");
		this.assertValue("5 <  2",    5 <  2);
		this.assertValue("5 <  5",    5 <  5);
		this.assertValue("5 <= 2",    5 <= 2);
		this.assertValue("5 <= 5",    5 <= 5);
		this.assertValue("5 >  2",    5 >  2);
		this.assertValue("5 >  5",    5 >  5);
		this.assertValue("5 >= 2",    5 >= 2);
		this.assertValue("5 >= 5",    5 >= 5);
		
		this.printSection("InstanceOf");
		this.assertValue("5 instanceof int",                    (new Integer(5)) instanceof Integer);
		this.assertValue("(new Integer(5)) instanceof Integer", (new Integer(5)) instanceof Integer);

		this.printSection("Equality");
		this.assertValue("5          === 5",          true);	// is
		this.assertValue("System.out === System.out", true);	// is
		this.assertValue("5          === 5b",         false);	// is

		this.assertValue("5          == 5",          true);	// equals
		this.assertValue("System.out == System.out", true);	// equals
		this.assertValue("5          == 5b",         true);	// equals

		this.printSection("InstanceOf");
		this.assertValue(" 5  ==> int",          true);
		this.assertValue("`5` ==> int",          false);
		this.assertValue("`5` ==> String",       true);
		this.assertValue("`5` ==> CharSequence", true);

		this.printSection("KindOf");
		this.assertValue("int           --> Number",               true);
		this.assertValue("byte          --> Number",               true);
		this.assertValue("String        --> CharSequence",         true);
		this.assertValue("StringBuilder --> CharSequence",         true);
		this.assertValue("String        --> Number",               false);
		this.assertValue("StringBuilder --> Number",               false);
		this.assertValue("int           --> CharSequence",         false);
		this.assertValue("byte          --> CharSequence",         false);
		this.assertValue("int           --> java.io.Serializable", true);
		this.assertValue("byte          --> java.io.Serializable", true);
		this.assertValue("String        --> java.io.Serializable", true);
		this.assertValue("StringBuilder --> java.io.Serializable", true);

		this.assertValue("5 <#> 5",  0);	// compare
		this.assertValue("5 <#> 4",  1);	// compare
		this.assertValue("5 <#> 6", -1);	// compare

		this.printSection("Logic");
		this.assertValue("5 <  2 && 5 > 2",    5 <  2 && 5 > 2);
		this.assertValue("5 <  2 || 5 > 2",    5 <  2 || 5 > 2);

		this.printSection("Condition");
		this.assertValue("5 < 2 ? 5 : 2", 5 < 2 ? 5 : 2);
		this.assertValue("5 > 2 ? 5 : 2", 5 > 2 ? 5 : 2);
		
		this.printSection("Try NoNull");
		this.assertValue("{ int I = null; I??; }", 0);
		
		this.printSection("Do WhenNoNull");
		this.assertValue("{ String S = null; S?.length(); }", "0");
		this.assertValue("{ ((String)null)?.length();     }",   "0");
		this.assertValue("{ ((String)null)?.getType();    }",   TKJava.TString);
		this.assertValue("{ ((String)null)?.toString();   }",   "");
		this.assertValue("{ ((String)null)?.charAt(0);    }",   '\0');
		this.assertValue("{ ((String)null)?.charAt(0b);   }",   '\0');
		this.assertValue("{ ((String)null)?.equals(null); }",   true);	// False because false is a default value of boolean
		
		this.assertValue("{ System.out?.print(5); 5; }", 5, "5");
		this.assertValue("{ System?.out.print(5); 5; }", 5, "5");
		
		this.printSection("Do WhenValidIndex");
		this.assertValue("{ int[]     Is = new int[] { 0, 1, 2 }; Is?.length; }", "3");
		this.assertValue("{ int[3]    Is = null;                  Is?.length; }", "0");
		this.assertValue("{ int[3]    Is = null;                  Is?[0];     }", "0");
		this.assertValue("{ String[3] Ss = null;                  Ss?[0];     }",  "");

		this.assertValue("{ int[]    Is = new int[]    {  0,   3,   6  }; Is [1]; }",  3 );
		this.assertValue("{ String[] Ss = new String[] { `0`, `3`, `6` }; Ss [1]; }", "3");
		this.assertValue("{ int[]    Is = new int[]    {  0,   3,   6  }; Is?[1]; }",  3 );
		this.assertValue("{ String[] Ss = new String[] { `0`, `3`, `6` }; Ss?[1]; }", "3");
		this.assertValue("{ int[]    Is = null;                           Is?[1]; }",  0 );
		this.assertValue("{ String[] Ss = null;                           Ss?[1]; }", "");
	}

}
