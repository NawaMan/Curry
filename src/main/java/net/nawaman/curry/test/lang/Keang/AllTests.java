package net.nawaman.curry.test.lang.Keang;

public class AllTests extends net.nawaman.curry.test.lang.Curry.AllTests {
	// Run the test
	static public void main(final String ... Args) {
		AllTests.getEngine();	// prepare the engine
		AllTests.getLanguage();	// prepare the language
		runTests(Args);
	}
}
