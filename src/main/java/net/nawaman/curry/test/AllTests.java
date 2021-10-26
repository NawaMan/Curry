package net.nawaman.curry.test;

import java.util.Arrays;
import java.util.Vector;

import net.nawaman.curry.Engine;
import net.nawaman.curry.EngineExtension;
import net.nawaman.curry.EngineExtensions;
import net.nawaman.curry.EngineSpec;
import net.nawaman.curry.Expression;
import net.nawaman.curry.Type;
import net.nawaman.curry.compiler.EE_Language;
import net.nawaman.curry.extra.type_enum.EE_Enum;
import net.nawaman.curry.extra.type_object.EE_Object;
import net.nawaman.testsuite.TestSuite;
import net.nawaman.util.UObject;

/** TestSuite that require the curry engine */
public class AllTests extends net.nawaman.testsuite.TestSuite {

	// Run the test
	static public void main(final String ... Args) {
		AllTests.getEngine();	// prepare the engine
		runTests(Args);
	}
	
	// Customization of compare and toString ---------------------------------------------------------------------------
	// This is to use Engine to do it if it is not null
	
	/**{@inheritDoc}*/ @Override
	public boolean compare(net.nawaman.testsuite.TestCase TC, Object pTestValue, Object pExpectedValue) {
		return (TheEngine == null)
		           ? UObject  .equal( pTestValue, pExpectedValue)
		           : TheEngine.equals(pTestValue, pExpectedValue);
	}
	
	/**{@inheritDoc}*/ @Override
	public String toString(net.nawaman.testsuite.TestCase TC, Object pValue) {
		return ToString(pValue);
	}

	/**{@inheritDoc}*/ @Override 
	public boolean preTest(net.nawaman.testsuite.TestCase TC, final String ... Args) {
		AllTests.getEngine();
		return true;
	}
	
	/**{@inheritDoc}*/ @Override
	public Object processTestValue(net.nawaman.testsuite.TestCase TC, Object pTestValue) {
		if((pTestValue == null) || ((pTestValue = AllTests.getEngine().execute(pTestValue)) == null)) return null;
		return pTestValue;
	}
	/**{@inheritDoc}*/ @Override
	public Object processExpectedValue(net.nawaman.testsuite.TestCase TC, Object pExpectedValue) {
		if((pExpectedValue == null) || ((pExpectedValue = AllTests.getEngine().execute(pExpectedValue)) == null)) return null;
		return pExpectedValue;
	}
	/**{@inheritDoc}*/ @Override
	public String processCapturedOutput(net.nawaman.testsuite.TestCase TC, String pCapturedOutput) {
		if(pCapturedOutput == null) return null;
		return pCapturedOutput;
	}
	
	// TestCase --------------------------------------------------------------------------------------------------------
	
	/** Simple Curry Test Case */
	static abstract public class TestCase extends net.nawaman.testsuite.TestCase {
			
		public Vector<Class<? extends EngineExtension>> getRequiredExtensionClasses() { return null;  }
		public boolean                                  isOnlyRequiredExtensions()    { return false; }
		
		protected void clearEngine() {
			AllTests.TheEngine = null;
		}

		/**{@inheritDoc}*/ @Override
		protected void doAssertRAW(Object pTestValue, Object pExpectedValue) {
			this.doAssertRAWToStringCompare(pTestValue, pExpectedValue);
		}
		
		/**{@inheritDoc}*/ @Override
		protected void doTest(final String ... Args) {
			AllTests.PrepareEngine(
					TestSuite.Check_isQuiet(Args),
					this.getRequiredExtensionClasses(),
					this.isOnlyRequiredExtensions());
			
			boolean IsQuite = TestSuite.Check_isQuiet(Args);
			super.doTest(Args);
			if(!IsQuite) System.out.println();
		}
		
		/** Assert if the list of obejct operations (ToString) of the given type T contains the needle */
		void assertObjectOpersContains(Type T, String ... Needle) {
			if(Needle == null) return;
			String S = Arrays.toString(T.getSOInfo().getObjectOperationInfos());
			this.assertStringContains(S, (Object[])Needle);
		}
		/** Assert if the list of obejct attributes (ToString) of the given type T contains the needle */
		void assertObjectAttrsContains(Type T, String ... Needle) {
			if(Needle == null) return;
			String S = Arrays.toString(T.getSOInfo().getObjectAttributeInfos());
			this.assertStringContains(S, (Object[])Needle);
		}
	}
	
	// The current engine ----------------------------------------------------------------------------------------------

	static public Engine TheEngine = null;
	
	/** Default Engine Extensions' classes */
	static private Vector<Class<? extends EngineExtension>> AllExtClasses = new Vector<Class<? extends EngineExtension>>();
	static {
		AllExtClasses.add(EngineExtensions.EE_Java.class);
		AllExtClasses.add(EngineExtensions.EE_StackOwner.class);
		AllExtClasses.add(EngineExtensions.EE_StackOwnerCustomizable.class);
		AllExtClasses.add(EngineExtensions.EE_Unit.class);
		AllExtClasses.add(EngineExtensions.EE_DefaultPackage.class);
		AllExtClasses.add(EngineExtensions.EE_DataHolder.class);
		AllExtClasses.add(EngineExtensions.EE_AdvanceLoop.class);
		AllExtClasses.add(EE_Enum.class);
		AllExtClasses.add(EE_Object.class);
		AllExtClasses.add(EE_Language.class);
	};

	/** The current engine */
	static public Engine getEngine() {
		if(AllTests.TheEngine == null) {
			PrepareEngine(false, null, false);
			ShowEngineCreating = false;
		}
		return AllTests.TheEngine;
	}
	
	static public boolean ShowEngineCreating = true;
	
	/** Prepare the engine */
	static public Engine PrepareEngine(boolean IsQuite, Vector<Class<? extends EngineExtension>> ExtClasses, boolean IsOnlyExts) {
		if(!IsQuite && ShowEngineCreating) System.out.println();
		
		EngineExtension[] Exts = null;
		// Default Extension class list
		if(ExtClasses == null) ExtClasses = AllTests.AllExtClasses;
		
		Engine E = AllTests.TheEngine;
		if(E != null) {
			// Create the extension list
			Exts = E.getEngineSpec().getEngineExtensions();
			
			Vector<Class<? extends EngineExtension>> ExtCls = new Vector<Class<? extends EngineExtension>>(ExtClasses);
			for(int i = 0; i < Exts.length; i++) {
				EngineExtension EE = Exts[i];
				if(EE == null) continue;
				Class<? extends EngineExtension> EECls = EE.getClass();
				if(ExtCls.contains(EECls)) ExtCls.remove(EECls);
				else {
					// One of the required engine extension is missing
					Exts = null;
					break;
				}
			}

			// Check if some extension is left there
			if(IsOnlyExts && (Exts != null) && (ExtCls.size() != 0)) Exts = null;
		}
		
		if(Exts != null) return null;
		else {
			Vector<EngineExtension> EEs = new Vector<EngineExtension>();
			// Required to create a new engine
			for(Class<? extends EngineExtension> EECls : ExtClasses) {
				try { EEs.add((EngineExtension)EECls.getConstructor().newInstance()); }
				catch (Exception Exc) { throw new RuntimeException("Problem constructing an engine.", Exc); }
			}
			Exts = new EngineExtension[EEs.size()];
			EEs.toArray(Exts);
			
		} 
		
		// Create an engine
		final EngineExtension[] TheExts = Exts;
		EngineSpec ES = new EngineSpec() {
			@Override public String getEngineName() { return "TestEngine"; }
			@Override protected EngineExtension[] getExtensions() { return TheExts; }
		};
		
		Engine $E = net.nawaman.curry.Engine.newEngine(ES, !IsQuite && ShowEngineCreating);
		TheEngine = $E;
		if(!IsQuite && ShowEngineCreating) System.out.println();
		
		return $E;
	}
	
	/** Returns the string representation of the object with the help of the engine */
	static public String ToString(Object O) {
		if(TheEngine == null)       return UObject  .toString(O);
		if(O instanceof Expression) return TheEngine.toDetail(O);
		else                        return TheEngine.toString(O);
	}

}
