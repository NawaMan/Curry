package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.CurryError;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseParser;

public class Test_02_Instructions extends TestCaseParser {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		this.WithType = true;
		this.printSection("Numeric");
		this.assertValue("@:plus(    5, 3, 4)"         ,           12);
		this.assertValue("@:subtract(7, 5)"            ,            2);
		this.assertValue("@:multiply(5, 7, 3)"         ,          105);
		this.assertValue("@:divide(  7, 5)"            ,            1);
		this.assertValue("@:modulus( 7, 5)"            ,            2);
		this.assertValue("@:concat(`def`,`init`,`ion`)", "definition");

		this.assertValue("@:format(`%s-%s-%s`,                5,6,7  )", "5-6-7");
		this.assertValue("@:format(`%s-%s-%s`, new Object[] { 5,6,7 })", "5-6-7");
			
		// Cast
		this.printSection("Cast");
		this.assertValue("(:byte?5)",            (byte)5);
		this.assertValue("(:byte? `5` <:= -1b)", (byte)-1);  // Use OrElse
		this.assertValue("(:byte? `5`)",         (byte) 0);  // Use Default
			
		this.printSection("Others");
		// Try not null
		this.assertValue("@:tryNoNull(null, int.type)", 0);
			
		// New Array
		this.assertValue("@:newArrayLiteral_int( 1, 2, 3 )", new Integer[] { 1, 2, 3 });

		// Get Array type
		this.assertValue(
			"@:getTypeInfo(@:getTypeOf(@:newArrayLiteral_int( 1, 2, 3 )), `TypeRef`)",
			TKArray.newArrayTypeRef(TKJava.TInteger.getTypeRef(), 3)
		);
			
		// Instance of
		this.assertValue("@:instanceOf(int[1][2].type, @:newArrayLiteral(int[].type, @:newArrayLiteral_int( 1, 2 ) ))", true);
			
		// Array
		this.assertValue(  "@:getArrayElementAt(@:newArrayLiteral_int( 1, 2, 3 ), 1)", 2);
		this.assertProblem("@:getArrayElementAt(@:newArrayLiteral_int( 1, 2, 3 ), 5)",
					CurryError.class, "java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 5");
			
		this.printSubSection("doWhenNoNull");
		this.assertValue(
				"@:stack(null){ " +
					"@:newVariable(`S`, String.type, `%s`); " +
					"@:doWhenNoNull(" +
						"@:getVarValue(`S`)," +
						"@@:Expr ( @:format(@:getVarValue(`S`), `5`) )," +
						"String.type" +
					");" +
				"}"
				,
				"5");
		this.assertValue(
				"@:toString("+
					"@:tryNoNull("+
						"@:stack(null){ " +
							"@:newVariable(`S`, String.type, null); " +
							"@:doWhenNoNull(" +
								"@:getVarValue(`S`)," +
								"@@:Expr ( @:format(@:getVarValue(`S`), `5`) )," +
								"String.type" +
							");" +
						"}," +
						"String.type"+
					")"+
				")"
				,
				"");
			
		this.printSubSection("doWhenValidIndex");
		this.assertValue(
				"@:toString("+
					"@:stack(null){ " +
						"@:newVariable(`Is`, int[].type, @:newArrayLiteral_int( 1, 2, 3 )); " +
						"@:doWhenValidIndex(" +
							"@:getVarValue(`Is`), 1," +
							"@@:Expr ( @:getArrayElementAt(@:getVarValue(`Is`), 1) )," +
							"int.type" +
						");"+
					"}"+
				")"
				,
				"2");
		this.assertValue(
				"@:toString("+
					"@:stack(null){ " +
						"@:newVariable(`Is`, int[].type, @:newArrayLiteral_int( 1, 2, 3 )); " +
						"@:doWhenValidIndex(" +
							"@:getVarValue(`Is`), 5," +
							"@@:Expr ( @:getArrayElementAt(@:getVarValue(`Is`), 5) )," +
							"int.type" +
						");"+
					"}"+
				")"
				,
				"0");

		this.printSection("Types");
		this.printSubSection("Duck Types");
		this.assertValue("@:toString(  Duck:<{ length():int }>.typeref)",       "Duck:<{length():int}>");
		this.assertValue("@:instanceOf(Duck:<{ length():int }>.type, `String`)", true);

		this.assertValue("@:isKindOf(Duck:<(java.io.Serializable){ toString():String }>.type, String .type)", true);
		this.assertValue("@:isKindOf(Duck:<(java.io.Serializable){ toString():String }>.type, Runtime.type)", false);
		


		this.printSection("TextProcessing");

		this.printSubSection("Curry");
		this.assertValue("\\` %s `(5):{ exit @:format($Text,$0); }", " 5 ");
		
		this.printSubSection("StringFormat");
		this.assertValue("\\f` %s `    (5)",   " 5 "  );
		this.assertValue("\\f` %s-%s ` (5,6)", " 5-6 ");
		// this.assertValue("\\s` %s `(5):{ 5; }", " 5 "  );	// Warning
		this.assertValue("\\f=(` %s`+`-`+`%s `)=(5,6)", " 5-6 ");
			
		this.printSection("String WhenNotNull");
		this.assertValue("(?   5: \\f` %s `(5))",    " 5 ");
		this.assertValue("(?null: \\f` %s `(null))", "");

		this.printSection("Document");
		this.assertValue(
			"@<?{ This is a document }?>: (`Doc: ` + @:getContextInfo(`CurrentDocumentation`));",
			"Doc: <?[---\nThis is a document\n---]?>"
		);
		
		this.printSection("To be continue ...");
			
		this.printSection("End");
	}
}
