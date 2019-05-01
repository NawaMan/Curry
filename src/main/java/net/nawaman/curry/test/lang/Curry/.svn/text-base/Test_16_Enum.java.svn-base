package net.nawaman.curry.test.lang.Curry;

public class Test_16_Enum extends AllTests.TestCaseUnit {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public TestKind getTestKind() {
		return TestKind.OnMem;
	}
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {		
		//Engine E  = this.getTheEngine();
		
		String PName = "nawaman~>test~>P16_01";
		
		this.addUnit(
			this.newFile(PName.replaceAll("~>", "/") + "/C1.curry",
				"@@:Package("+PName+");\n"           +
				"\n"                                 +
				"@@:TypeDef public enum Metal [\n"   +
				"	Iron,\n"                         +
				"	Copper,\n"                       +
				"	Mercury,\n"                      +
				"	Gold,\n"                         +
				"	Silver,\n"                       +
				"	Steel -> Iron,\n"                +
				"	Others\n"                        +
				"];\n"                               +
				"\n"                                 +
				"@@:TypeDef public enum ExpensiveMetal emulates Metal [\n" +
				"	Gold^,\n"                        +
				"	Silver^\n"                       +
				"];\n"
			)
		);
		this.compile();
		
		this.printSection("Type");
		this.assertValue(PName+"=>Metal",                                "nawaman~>test~>P16_01=>Metal");
		this.assertValue(PName+"=>Metal.getTypeInfo().getDescription()", "nawaman~>test~>P16_01=>Metal::Enum [Iron,Copper,Mercury,Gold,Silver,Steel->Iron,Others]");

		this.printSection("Member");
		this.assertValue(PName+"=>Metal.Iron",   "Iron");
		this.assertValue(PName+"=>Metal.Copper", "Copper");

		this.assertValue(PName+"=>Metal.getMemberCount()",    7);
		this.assertValue(PName+"=>Metal.getMemberAt(2)",      "Mercury");
		this.assertValue(PName+"=>Metal.getMemberAt(3)",      "Gold");
		this.assertValue(PName+"=>Metal.getMember(`Silver`)", "Silver");
		this.assertValue(PName+"=>Metal.getMember(`Others`)", "Others");
		
		
		// Many more test
		
		this.printSection("DONE");
	}

}
