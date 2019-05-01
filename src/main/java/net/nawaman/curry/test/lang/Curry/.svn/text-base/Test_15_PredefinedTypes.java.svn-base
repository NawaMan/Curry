package net.nawaman.curry.test.lang.Curry;

public class Test_15_PredefinedTypes extends AllTests.TestCaseUnit {
	
	static public void main(String ... Args) { runTest(Args); }
	
	/**{@inheritDoc}*/ @Override
	public TestKind getTestKind() {
		return TestKind.OnMem;
	}
	
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {		
		//Engine E  = this.getTheEngine();
		
		String PName = "nawaman~>test~>P15_01";
		
		this.addUnit(
			this.newFile(PName.replaceAll("~>", "/") + "/C1.curry",
				"@@:Package("+PName+");\n"                                                      +
				"\n"                                                                            +
				"@@:TypeDef public class ComparableNumber<ValueType:Number> implements curry=>Comparable<ValueType>, java.lang.Comparable {\n" +
				"	\n"                                                                            +
				"	@@:Field private Value: ValueType = ((ValueType)null)??;\n" +
				"	\n" +
				"	@@:Constructor public (pValue:ValueType) {\n" +
				"		this.Value = pValue;\n" +
				"	};\n" +
				"	\n" +
				"	@@:Method public compareTo(O:curry=>ComparableValueType<ValueType>):int {\n" +
				"		@:print(`ValueType:` + ValueType.type + ` = `);\n" +
				"		if(     O instanceof ComparableNumber)             return this.Value - ((ComparableNumber)O).Value;\n" +
				"		else if(O instanceof ValueType)                    return this.Value <#> O?.hash();\n" +
				"		else if(O instanceof curry=>Comparable<ValueType>) return -((curry=>Comparable<ValueType>)O).compareTo(this.Value);\n" +
				"		else                                               return this.Value <#> O?.hash();\n" +
				"	};\n" +
				"	\n" +
				"	@@:Method public compareTo(O:any):int {\n" +
				"		unless(O instanceof curry=>ComparableValueType<ValueType>) return java.lang.Integer.MIN_VALUE;\n" +
				"		return this.compareTo((curry=>ComparableValueType<ValueType>)O);\n" +
				"	};\n" +
				"	\n" +
				"	@@:Method public toString():String {\n" +
				"		return `CN:` + this.Value;\n" +
				"	};\n" +
				"};\n" +
				"\n" +
				"@@:TypeDef public class ArrayIterator <ValueType:any> implements curry=>Iterator<ValueType> {\n" +
				"	\n" +
				"	@@:Field private Data: ValueType[] = null;\n" +
				"	@@:Field private Index:int         =    0;\n" +
				"	\n" +
				"	@@:Constructor public (pData:ValueType[]) {\n" +
				"		if(pData == null) this.Data = new ValueType[0];\n" +
				"		else              this.Data = pData.clone();\n" +
				"	};\n" +
				"	\n" +
				"	@@:Method public hasNext():boolean {\n" +
				"		return (this.Index < this.Data.length);\n" +
				"	};\n" +
				"	\n" +
				"	@@:Method public next():ValueType {\n" +
				"		if(!this.hasNext()) return null;\n" +
				"		return this.Data[this.Index++];\n" +
				"	};\n" +
				"	\n" +
				"	@@:Method public remove():void {\n" +
				"		throw new UnsupportedOperationException(`ArrayIterator does not support remove operation.`);\n" +
				"	};\n" +
				"};" +
				"\n" +
				"@@:TypeDef public class ArrayIterable <ValueType:any> implements curry=>Iterable<ValueType> {\n" +
				"	\n" +
				"	@@:Field private Data: ValueType[] = null;\n" +
				"	\n" +
				"	@@:Constructor public (pData:ValueType[]) {\n" +
				"		if(pData == null) this.Data = new ValueType[0];\n" +
				"		else              this.Data = pData.clone();\n" +
				"	};\n" +
				"	\n" +
				"	@@:Method public iterator():curry=>Iterator<ValueType> {\n" +
				"		return new ArrayIterator<ValueType>(this.Data);\n" +
				"	};\n" +
				"};"
			)
		);
		this.compile();
		
		this.printSection("Package && Type");
		this.assertValue("@:getPackage(`"+PName+"`)",       "Package:"+PName+"");
		this.assertValue(PName + "=>ComparableNumber.type", PName+"=>ComparableNumber<ValueType:Number>");
		
		this.printSection("Comparable");
		this.assertValue(
				"{\n" +
				"	@@:Import("+PName+"=>*);\n" +
				"	ComparableNumber<int> CN5 = new (5);\n" +
				"	CN5;\n" +
				"}",
				"CN:5"
			);
		
		this.startCapture();
		this.assertValue(
				"{\n" +
				"	@@:Import("+PName+"=>*);\n" +
				"	ComparableNumber<int> CN5 = new (5);\n" +
				"	@:println(CN5 <#> 3);\n" +
				"	@:println(CN5 <#> 4);\n" +
				"	@:println(CN5 <#> 5);\n" +
				"	@:println(CN5 <#> 6);\n" +
				"	@:println(CN5 <#> 7);\n" +
				"	null;\n" +
				"}",
				"null"
			);
		this.assertCaptured(
				"ValueType:int = 2\n" +
				"ValueType:int = 1\n" +
				"ValueType:int = 0\n" +
				"ValueType:int = -1\n" +
				"ValueType:int = -2\n");
		
		this.startCapture();
		this.assertValue(
				"{\n" +
				"	@@:Import("+PName+"=>*);\n" +
				"	ComparableNumber<int> CN5 = new (5);\n" +
				"	@:println(3 <#> CN5);\n" +
				"	@:println(4 <#> CN5);\n" +
				"	@:println(5 <#> CN5);\n" +
				"	@:println(6 <#> CN5);\n" +
				"	@:println(7 <#> CN5);\n" +
				"	null;\n" +
				"}",
				"null"
			);
		this.assertCaptured(
				"ValueType:int = -2\n" +
				"ValueType:int = -1\n" +
				"ValueType:int = 0\n" +
				"ValueType:int = 1\n" +
				"ValueType:int = 2\n");
		
		this.startCapture();
		this.assertValue(
				"{\n" +
				"	@@:Import("+PName+"=>*);\n" +
				"	ComparableNumber<int>  CNI5 = new (5);\n" +
				"	ComparableNumber<byte> CNB5 = new (5b);\n" +
				"	@:println(CNI5 <#> CNB5);\n" +
				"	@:println(CNB5 <#> CNI5);\n" +
				"	null;\n" +
				"}",
				"null"
			);
		this.assertCaptured(
				"ValueType:int = 0\n" +
				"ValueType:int = 0\n"
			);
		
		this.startCapture();
		this.assertValue(
				"{\n" +
				"	@@:Import("+PName+"=>*);\n" +
				"	ComparableNumber<Number> CNN5 = new (5);\n"  +
				"	ComparableNumber<byte>   CNB5 = new (5b);\n" +
				"	@:println(CNB5 <#> CNN5);\n"                 +
				"	@:println(CNN5 <#> CNB5);\n"                 +
				"	null;\n" +
				"}",
				"null"
			);
		this.assertCaptured("ValueType:Number = 0\nValueType:Number = 0\n");

		/*
		// Test for error
		this.startCapture();
		this.assertValue(
				"{\n" +
				"	@@:Import("+PName+"=>*);\n" +
				"	ComparableNumber<int>    CNI5 = new (5);\n" +
				"	ComparableNumber<String> CNS5 = new (`S`);\n" +
				"	@:println(CNI5 <#> CNS5);\n" +
				"	@:println(CNS5 <#> CNI5);\n" +
				"	null;\n" +
				"}",
				"null"
			);
		this.assertCaptured("ValueType:int = 0\nValueType:int = 0\n");
		/* */
		
		this.printSection("Array & Iterator & Iterable");
		
		this.printSubSection("Array");
		this.assertValue(
				"{\n"                                                                     +
				"	String S = ``;\n"                                                     +
				"	foreach(int I : new int[] { 0,6,1,5,2,4,3 }) {\n"                     +
				"		if($Count$ != 0) S += `,`;\n"                                     +
				"		S += I;\n"                                                        +
				"	}\n"                                                                  +
				"	S;\n"                                                                 +
				"}",
				"0,6,1,5,2,4,3");
		
		this.printSubSection("Iterator");
		this.assertValue(
				"{\n"                                                                         +
				"	@@:Import("+PName+"=>*);\n"                                               +
				"	String S = ``;\n"                                                         +
				"	foreach(int I : new ArrayIterator<int>(new int[] { 0,6,1,5,2,4,3 })) {\n" +
				"		if($Count$ != 0) S += `,`;\n"                                         +
				"		S += I;\n"                                                            +
				"	}\n"                                                                      +
				"	S;\n"                                                                     +
				"}",
				"0,6,1,5,2,4,3");
		
		// TODO - Fix this later
		/*
		this.printSubSection("Iterable");
		this.assertValue(
				"{\n"                                                                         +
				"	@@:Import("+PName+"=>*);\n"                                               +
				"	String S = ``;\n"                                                         +
				"	foreach(int I : new ArrayIterable<int>(new int[] { 0,6,1,5,2,4,3 })) {\n" +
				"		if($Count$ != 0) S += `,`;\n"                                         +
				"		S += I;\n"                                                            +
				"	}\n"                                                                      +
				"	S;\n"                                                                     +
				"}",
				"0,6,1,5,2,4,3");
		*/
		this.printSection("DONE");
	}
}
