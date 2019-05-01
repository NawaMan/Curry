package net.nawaman.curry.test.lang.Curry;

import java.io.Serializable;

import net.nawaman.curry.Documentation;
import net.nawaman.curry.Engine;
import net.nawaman.curry.Expression;
import net.nawaman.curry.MExecutable;
import net.nawaman.curry.MType;
import net.nawaman.curry.Package;
import net.nawaman.curry.TKArray;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseParser;
import net.nawaman.util.UNumber;
import net.nawaman.util.UString;

public class Test_01_Literals extends TestCaseParser {
	
	static public void main(String ... Args) { runTest(Args); }
	
	String ParaserTypeName = "Literal";
	
	/**{@inheritDoc}*/ @Override
	protected String getParserTypeName() {
		return this.ParaserTypeName;
	}
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {
		this.printSection("Null and Boolean");
		this.assertValue("null",  null);
		this.assertValue("true",  true);
		this.assertValue("false", false);
			
		this.printSection("Character");
		this.assertValue(  "'a'"  ,    "a");
		this.assertValue("'\\t'"  , "	");
		this.assertValue("'\\n'"  ,   "\n");
		//this.assertValue("'\\h'",      "!");		// cause error;
		this.assertValue("'\\''",      "'");
		this.assertValue("'\\040'",    " ");
		this.assertValue("'\\041'",    "!");
		this.assertValue("'\\x20'",    " ");
		this.assertValue("'\\x21'",    "!");
		this.assertValue("'\\u0020'",  " ");
		this.assertValue("'\\u0021'",  "!");

		this.printSection("String");
		this.assertValue("\"abc\"", "abc");
		this.assertValue("`def`",   "def");
		//this.assertValue("`d\nef`",   "d\nef");	// cause error
			
		this.assertValue("<\"[]\">",                                                   "");
		this.assertValue("<\"[ghi]\">",                                                "ghi");
		this.assertValue("<`[ghi]`>",                                                  "ghi");
		this.assertValue("<\"[ghi\"jkl\"mno]\">",                                      "ghi\"jkl\"mno");
		this.assertValue("<\"[ghi\"j\nkl\"mno]\">",                                    "ghi\"j\nkl\"mno");
		this.assertValue("<\"[  pqr  ]\">",                                            "  pqr  ");
		this.assertValue("<\"{  pqr  }\">",                                            "pqr");				// Trim
		this.assertValue("<\"[\\Esc(`-Escape-`)]\">",                                  "-Escape-");
		this.assertValue("<\"[\\Esc(`-]\">-`)]\">",                                    "-]\">-");
		this.assertValue("<\"[--- Some comment ---\n  stu  ]\">",                      "  stu  ");
		this.assertValue("<\"[--- Some comment ---\n  vwx  \n--- End comment ---]\">", "  vwx  ");
		this.assertValue("<\"{--- Some comment ---\n  yz  \n--- End comment ---}\">",  "yz");

		// Escape (look closely to Tab)
		this.assertValue("`Some\\ttext`",                "Some	text");
		this.assertValue("<\"{Some\\ttext}\">",          "Some\\ttext");
		this.assertValue("<\"{Some\\Esc(`\\t`)text}\">", "Some	text");

		this.printSection("Hash of Constant");
		this.assertValue("@#ABC#@",   UString.hash("ABC"));
		this.assertValue("@#DEF#@",   UString.hash("DEF"));
		this.assertValue("@#ทดสอบ#@", UString.hash("ทดสอบ"));
		this.assertValue("@#`#@`#@",  UString.hash("#@"));
		
		Engine      E  = AllTests.getEngine();
		MExecutable ME = E.getExecutableManager();
		MType       MT = E.getTypeManager();

		this.WithType = true;
		
		this.printSection("Number");
		this.assertValue("5",   5);
		this.assertValue("5.0", 5.0);
		
		this.assertValue("5b", (byte) 5);
		this.assertValue("5s", (short)5);
		this.assertValue("5i",        5);
		this.assertValue("5L",       5L);
		this.assertValue("5f",     5.0f);
		this.assertValue("5d",      5.0);
		this.assertValue("5I",  UNumber.getBigInteger(5));
		this.assertValue("5D",  UNumber.getBigDecimal(5));

		this.assertValue("5.0f", 5.0f);
		this.assertValue("5.0d", 5.0);
		this.assertValue("5.0D", UNumber.getBigDecimal(5));
		

		this.printSection("Meta");
		this.ParaserTypeName = "Atomic_Meta";
		this.assertValue("int.class",   Integer.class);
		this.assertValue("int.typeref", TKJava.TInteger.getTypeRef());
		this.assertValue("int.type",    TKJava.TInteger);

		this.ParaserTypeName = "TR_ArrayDimentions";
		this.assertValue("[5][][7]", new int[] { 5, -1, 7 });
		
		this.ParaserTypeName = "Atomic_WrappedExpr";
		this.assertValue("@@:Expr    ( @:plus(1, 7) )", Expression.newExpr(ME.newExpr("plus", 1,7)));
		//this.assertValue("@@:Expr () { @:plus(1, 7); }", Expression.newExpr(ME.newExpr("plus", 1,7)));	// Error
		
		this.ParaserTypeName = "Comment";
		this.assertValue("//\t123\n", "  \t   \n");
		this.assertValue("/*\t123*/", "  \t     ");
		this.assertValue("(*\t123*)", "  \t     ");
		
		this.ParaserTypeName = "Acc_Package";
		this.assertValue("public",  Package.Public);
		this.assertValue("group",   Package.Group);
		this.assertValue("package", Package.Package);
		
		this.ParaserTypeName = "Acc_Type";
		this.assertValue("public",    Type.Public);
		this.assertValue("protected", Type.Protected);
		this.assertValue("group",     Package.Group);
		this.assertValue("package",   Package.Package);
		this.assertValue("private",   Type.Private);

		this.ParaserTypeName = "Documentation";
		this.assertValue("<?[ This function does something ]?>",
				new Documentation.Simple(" This function does something "));
		this.assertValue("<?@HTML:[ This function does something ]?>",
				new Documentation.Simple("HTML", " This function does something "));
		this.assertValue("<?@HTML(5,0,`stricted`):[ This function does something ]?>",
				new Documentation.Simple("HTML", new Serializable[] { 5,0, "stricted" }, " This function does something "));

		this.assertValue("<?{ Text closed by \"\\Esc(`]?>`)\" }?>", new Documentation.Simple("Text closed by \"]?>\""));

		this.printSection("TypeRef");
		this.ParaserTypeName = "TypeRef";
		this.assertValue("int",   TKJava.TInteger.getTypeRef());
		this.assertValue("int[]", TKArray.IntegerArrayRef);
		
		this.assertValue("java.io.File[5]", TKArray.newArrayTypeRef(MT.getTypeOfTheInstanceOf(java.io.File.class).getTypeRef(), 5));
		this.assertValue("TypeRef:<Array, newArrayTypeRef, int.typeref, 5>", TKArray.newArrayTypeRef(TKJava.TInteger.getTypeRef(), 5));
		
		/* */
		this.printSection("End");
	}

}
