package net.nawaman.curry.test.lang.Curry;

import net.nawaman.curry.test.lang.Curry.AllTests.TestCaseParser;

public class Test_00_QuickTest extends TestCaseParser {
	
	static public void main(String ... Args) { runTest(Args); }
	
	String ParaserTypeName = "Command";
	
	/**{@inheritDoc}*/ @Override
	protected String getParserTypeName() {
		return this.ParaserTypeName;
	}
		
	/**{@inheritDoc}*/ @Override
	public void doTest(final String ... Args) {		
		/*
		String DPName = AllTests.getEngine().getDefaultPackage().getName();

		this.assertValue(         "@:toString(Type:<String>.type)",              "Type:<String>");
		this.assertValue(         "@:toString(Type:<Package::"+DPName+">.type)", "Type:<Package::"+DPName+">");
		this.assertValue(         "@:toString(Executable:<(int):int>.type)",     "Executable:<(int):int>");
		this.assertStringContains("@:toString(Variant:<   int|byte >.type)",     "Variant:<any||","byte", "int", ">");
		this.assertValue(         "@:toString(Duck:<{do(int):int}>.type)",       "Duck:<{do(int):int}>");
		this.assertValue(         "@:toString(Duck:<{do(N):N}><N:Number>.type)", "Duck:<{do(Parameter{N}):Parameter{N}}><N:Number>");
		*/
		/* * /
		Engine $Engine = Engine.loadEngine("Engine_Complex.ces");
		
		this.startCapture();
		$Engine.execute("println", "Hello World!!!");
		this.assertCaptured("Hello World!!!\n");
		this.endCapture();
		
		Function F = (Function)ScriptManager.Use("EngineSpec");
		
		PTypePackage TPackage = (PTypePackage)F.run();
		this.println(TPackage);/* * /
		
		this.startCapture();
		CurryEngine.EnsureEngineRegisted();
		ScriptManager.Usepaths.registerUsepath("tests");
		Function F = (Function)ScriptManager.Use("Test.curry");
		F.run();
		this.assertCaptured(
				"Package:nawaman~>Display\n" +
				"Curry: Hello World!!!\n" +
				"Curry: Hi    World!!!\n" +
				"Java: Hello World!!!\n" +
				"Java: Hi    World!!!\n" +
				"Java: Hey   World!!!\n" +
				"Here: Package:nawaman~>Display\n" +
				"List: `{}`:java.util.Vector\n" +
				"Here\n");
		/* */
		
	}
}
