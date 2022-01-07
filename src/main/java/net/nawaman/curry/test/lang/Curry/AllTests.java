package net.nawaman.curry.test.lang.Curry;

import java.io.File;
import java.util.HashMap;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.compiler.CodeFeeders;
import net.nawaman.compiler.CompilationOptions;
import net.nawaman.curry.Debugger;
import net.nawaman.curry.Debugger_Simple;
import net.nawaman.curry.Engine;
import net.nawaman.curry.MUnit;
import net.nawaman.curry.Scope;
import net.nawaman.curry.UnitBuilders;
import net.nawaman.curry.UnitFactories;
import net.nawaman.curry.UnitBuilders.UBFile;
import net.nawaman.curry.compiler.CLRegParser;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.compiler.CompileProductContainer;
import net.nawaman.curry.compiler.CurryCompilationOptions;
import net.nawaman.curry.compiler.CurryCompiler;
import net.nawaman.curry.compiler.CurryLanguage;
import net.nawaman.curry.compiler.ExecutableCreator_Java;
import net.nawaman.curry.compiler.TextProcessor_Curry;
import net.nawaman.curry.compiler.TextProcessor_StringFormat;
import net.nawaman.curry.compiler.UnitBuilderCreator;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.typepackage.PTypePackage;

public class AllTests extends net.nawaman.curry.test.AllTests {

	// Run the test
	static public void main(final String ... Args) {
		AllTests.getEngine();	// prepare the engine
		AllTests.getLanguage();	// prepare the language
		runTests(Args);
	}
	
	// Customization of compare and toString ---------------------------------------------------------------------------
	// This is to use Engine to do it if it is not null

	/**{@inheritDoc}*/ @Override 
	public boolean preTest(net.nawaman.testsuite.TestCase TC, final String ... Args) {
		AllTests.getEngine();	// prepare the engine
		AllTests.getLanguage();	// prepare the language
		return true;
	}
	
	// TestCase --------------------------------------------------------------------------------------------------------

	/** Simple Curry Test Case */
	static abstract public class TestCaseParser extends net.nawaman.curry.test.AllTests.TestCase {

		static public final String CodeName = "C1";

		protected CompileProductContainer CPContainer = new CompileProductContainer();
		
		protected boolean  WithType    = false;
		protected boolean  IsProcessed = true;
		protected boolean  IsDebug     = false;
		protected Debugger Debugger    = new Debugger_Simple();
		protected Scope    TopScope    = null;
		
		protected boolean isWithType() { return this.WithType; }
		protected boolean isStacked()  { return false; }
		
		public String getPrefix() { return this.isStacked()?"@:call(@@:New sub():Object {":""; }
		public String getSuffix() { return this.isStacked()?"})"                          :""; }
		
		protected String getParserTypeName() {
			return null;
		}
		
		protected Engine getTheEngine() {
			return getEngine();
		}

		public String ToString(Object O) {
			if(this.isWithType()) return getEngine().getDisplayObject(O);
			return AllTests.ToString(O);
		}
		
		public Object processObject(Object O) {
			if(O == null)              return "null";
			if(!(O instanceof String)) return O;

			String StrExpr = this.getPrefix() + O.toString() + this.getSuffix();
			Object Expr;
			
			CurryCompilationOptions CCOptions = null;
			if(this.TopScope != null) {
				CCOptions = new CurryCompilationOptions();
				CCOptions.setTopScope(this.TopScope);
				CCOptions.toLocal();
			}
			
			Engine E = getEngine();
			
			String PName = getParserTypeName();
			if(PName != null)
				 Expr = ((CLRegParser)getLanguage()).compileToken(PName, StrExpr, this.CPContainer);				
			else Expr = ((CLRegParser)getLanguage()).compileExpression(TestCaseParser.CodeName, StrExpr,  CCOptions, this.CPContainer);

			if(this.CPContainer.getCompileProduct().hasErrMessage())
				System.err.println(this.CPContainer);
			
			Object Value = this.IsDebug ? E.debug(this.TopScope, this.Debugger, Expr) : E.execute(this.TopScope, Expr);
			
			return this.ToString(Value);
		}
		
		/**{@inheritDoc}*/ @Override
		public Object processTestValue(Object pTestValue) {
			if(!this.IsProcessed) return pTestValue;
			return this.processObject(pTestValue);
		}
		/**{@inheritDoc}*/ @Override
		public Object processExpectedValue(Object pExpectedValue) {
			if((pExpectedValue == null) || ((pExpectedValue = AllTests.getEngine().execute(pExpectedValue)) == null))
				return "null";
			return this.ToString(pExpectedValue);
		}
	
		/**{@inheritDoc}*/ @Override
		protected void clearEngine() {
			super.clearEngine();
			AllTests.TheLanguage = null;
		}

		/**{@inheritDoc}*/ @Override
		protected void doFinally(boolean IsAllSuccess, String ... pArgs) {
			CompileProduct CProduct = this.CPContainer.getCompileProduct();
			if(CProduct == null) return;
			
			// If there is a fail test, show the compile product
			if(!IsAllSuccess) {
				Engine         Engine   = CProduct.getEngine();
				System.out.println("Parse Result: ====================================================================================");
				System.out.println(CProduct.getCodeData(0, TestCaseParser.CodeName, CLRegParser.DNParseResult));
				System.out.println();

				System.out.println("Compile Result: ==================================================================================");
				Object O;
				if((O = CProduct.getCodeData(0, TestCaseParser.CodeName, "Expression")) != null) System.out.println("Expression: " + Engine.toDetail(O));
				if((O = CProduct.getCodeData(0, TestCaseParser.CodeName, "Fragment"  )) != null) System.out.println("Fragment: "   + Engine.toDetail(O));
				if((O = CProduct.getCodeData(0, TestCaseParser.CodeName, "Macro"     )) != null) System.out.println("Macro: "      + Engine.toDetail(O));
				if((O = CProduct.getCodeData(0, TestCaseParser.CodeName, "SubRoutine")) != null) System.out.println("SubRoutine: " + Engine.toDetail(O));
				if((O = CProduct.getCodeData(0, TestCaseParser.CodeName, "Token"     )) != null) System.out.println("Token: "      + Engine.toDetail(O));
			}

			if(!IsAllSuccess || !this.isQuiet()) {
				System.out.println();
				if(CProduct != null) System.out.println(CProduct);
				System.out.println();
			}
		}
	}

	/** Simple Curry Test Case */
	static abstract public class TestCaseStack extends TestCaseParser {
		
		/**{@inheritDoc}*/ @Override
		protected boolean isStacked()  {
			return true;
		}
		
	}

	/** Simple Curry Test Case */
	static abstract public class TestCaseUnit extends TestCaseParser {

		static public final String UnitFilePrefix = "tests/";
		
		static protected enum TestKind {
			OnMem, Save, Load; 
			
			public boolean isOnMem()    { return this == OnMem; }
			public boolean isTestSave() { return this == Save;  }
			public boolean isTestLoad() { return this == Load;  }
		}
		
		/** Only run the test no save/load testing */
		public TestKind getTestKind() { return TestKind.OnMem; }

		// Checks if the unit should be saved to file
		public boolean toSaveUnit()     { return this.getTestKind() == TestKind.Save;  }
		// Checks if the unit should be recreated if the file is missing
		public boolean toReCreated()    { return this.getTestKind() != TestKind.OnMem; }
		// Checks if the saved file should be deleted when done
		public boolean toDeleteUnit()   { return this.getTestKind() == TestKind.OnMem; }

		static int UCount = 0;
		
		CodeFeeders Feeders = null;
		
		HashMap<String, CodeUnit> CodeUnits = new HashMap<String, CodeUnit>();
		
		
		/** Add a new code unit for testing */
		protected void addUnit(CodeFile ... pCodeFiles) {
			if(this.Feeders != null) throw new IllegalAccessError("Already compiled.");
			
			String UName = "TestUnit_" + this.getClass().getSimpleName() + "_" + UCount++;
			if(this.CodeUnits.containsKey(UName)) throw new IllegalAccessError("Repeat unit name `"+UName+"`.");
			
			CodeUnit CUnit = new CodeUnit(UName, pCodeFiles);
			this.CodeUnits.put(UName, CUnit);
		}
		
		/** Creates a new code file for testing */
		protected CodeFile newFile(String pName, String pCode) {
			if(pName == null) throw new NullPointerException("No package name");
			return new CodeFile(pName, pCode);
		}
		
		/** Compile all the code */
		protected boolean compile() {
			return this.compile(false);
		}
		
		/** Compile all the code */
		protected boolean compile(boolean IsForceReCreate) {
			if(this.Feeders != null) throw new IllegalAccessError("Already compiled.");

			this.Feeders = this.getCodeFeeders(this.CodeUnits.values().toArray(new CodeUnit[this.CodeUnits.size()]));

			if(this.Feeders == null) return false;
			
			boolean IsToSave = IsForceReCreate || this.toSaveUnit();
			if(IsToSave) {
				CompileProduct CP = getLanguage().compileFiles(this.Feeders, this.newCompileOptions());
				if(CP.hasErrMessage()) throw new RuntimeException("Error compiling :\n" + CP.toString());
				this.CPContainer = new CompileProductContainer(CP);
				
			} else {
				MUnit  UM      = getEngine().getUnitManager();
				String ResType = UnitFactories.UFFile.Kind;
				for(int i = 0; i < this.Feeders.getFeederCount(); i++) {
					CodeFeeder CF = this.Feeders.getFeeder(i);
					File   F     = new File(UnitFilePrefix + CF.getFeederName());
					String FName = F.getAbsolutePath();
					
					if((new File(UnitFilePrefix + UBFile.getFileName(FName))).exists()) {
						UM.registerUnitFactory(ResType + "://" + FName);
						continue;
					}
					
					this.Feeders = null;
					return this.compile(true);
				}
			}	
			
			// We want to test loading but we have just force created, so we clear up the engine and language
			if(IsForceReCreate && (this.getTestKind() == TestKind.Load)) {
				this.clearEngine();
				// Run compile again the file should already be there.
				this.Feeders = null;
				return this.compile();
			}
			
			return true;
		}

		public CompilationOptions newCompileOptions(final String ... Args) {
			CompilationOptions.Simple CCOptions = new CompilationOptions.Simple();
			CCOptions.setData(
					CurryCompiler.DNUnitBuilderCreator,
					this.toSaveUnit() || this.toReCreated()
						? UnitBuilderCreator.File
						: UnitBuilderCreator.Memory);
			return CCOptions;
		}
		
		/**{@inheritDoc}*/ @Override
		protected boolean isStacked() {
			return false;
		}
		
		/**{@inheritDoc}*/ @Override
		protected String getParserTypeName() {
			return "Command";
		}
		
		/**{@inheritDoc}*/ @Override
		public boolean postTest(final String ... Args) {
			if(!this.toDeleteUnit()) return true;
			if(this.Feeders == null) return true;
			
			for(int i = 0; i < this.Feeders.getFeederCount(); i++) {
				CodeFeeder CF = this.Feeders.getFeeder(i);
				File F = new File(UnitFilePrefix + UnitBuilders.UBFile.getFileName(CF.getFeederName()));
				if(F.exists()) F.delete();
			}
			
			// Clear the engine
			this.clearEngine();
			return true;
		}
		
		// Accessory classes -------------------------------------------------------------------------------------------
		
		static protected class CodeFile {
			String Name;
			String Code;
			protected CodeFile(String pName, String pCode) {
				this.Name = pName;
				this.Code = pCode;
			}
		}
		
		static protected class CodeUnit {
			String     UName;
			CodeFile[] Files;
			protected CodeUnit(String UName, CodeFile ... pCodeFiles) {
				this.UName = UName;
				this.Files = pCodeFiles;
			}
		}

		// Returns the unit info of all the unit to be tested
		CodeFeeders getCodeFeeders(CodeUnit[] pCodeUnits) {
			if((pCodeUnits == null) || (pCodeUnits.length == 0)) return null;
			
			HashMap<String, HashMap<String, String>> Units = new HashMap<String, HashMap<String, String>>();
			for(int i = 0; i < pCodeUnits.length; i++) {
				CodeUnit CUnit = pCodeUnits[i];
				if(CUnit == null) continue;
				
				String UName = CUnit.UName;
				if(UName == null)            throw new IllegalAccessError("Null unit name: Unit #"+i+".");
				if(Units.containsKey(UName)) throw new IllegalAccessError("Repeat unit name `"+UName+"`.");
				
				HashMap<String, String> Codes = new HashMap<String, String>();
				for(int c = 0; c < CUnit.Files.length; c++) {
					String CName = CUnit.Files[c].Name;
					String Code  = CUnit.Files[c].Code;

					if(CName == null)            throw new IllegalAccessError("Null code name: Unit #"+c+".");
					if(Codes.containsKey(CName)) throw new IllegalAccessError("Repeat code name`"+CName+"`.");
					Codes.put(CName, Code);
				}
				
				Units.put(UName, Codes);
			}
			
			CodeFeeder[] CFs = new CodeFeeder[Units.size()];
			int i = 0;
			for(String UName : Units.keySet()) {
				CFs[i++] = new CodeFeeder.CFCharSequences(UName, Units.get(UName));
			}

			return new CodeFeeders(CFs);
		}
	}
	
	// Language --------------------------------------------------------------------------------------------------------

	static public CurryLanguage TheLanguage = null;

	/** The current engine */
	static public CurryLanguage getLanguage() {
		if(AllTests.TheLanguage == null) PrepareLanguage(false);
		return AllTests.TheLanguage;
	}
	
	static public void PrepareLanguage(boolean IsQuite) {
		ParserTypeProvider TPackage = null;
		try { TPackage = PTypePackage.Use("CurryCompiler"); }
		catch(Exception E) {
			System.err.println(E);
			throw new RuntimeException(E);
		}
		
		AllTests.TheLanguage = new CLRegParser("Test", AllTests.getEngine(), TPackage);
		
		CLRegParser CLanguage = (CLRegParser)AllTests.TheLanguage;
		CLanguage.registerExecutableCreator(new ExecutableCreator_Java());
		CLanguage.registerTextProcessor(    new TextProcessor_StringFormat("f"));
		CLanguage.registerTextProcessor(    new TextProcessor_StringFormat("format"));
		CLanguage.registerTextProcessor(    new TextProcessor_Curry("c"));
		CLanguage.registerTextProcessor(    new TextProcessor_Curry("curry"));
	}
}
