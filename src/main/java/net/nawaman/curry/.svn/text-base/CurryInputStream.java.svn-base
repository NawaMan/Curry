package net.nawaman.curry;

import java.io.IOException;
import java.io.InputStream;

import net.nawaman.javacompiler.JavaCompilerObjectInputStream;

public class CurryInputStream extends JavaCompilerObjectInputStream {
	
	Engine Engine = null;

	// Extract two ByteArrayInputStream out of one.
	static public CurryInputStream newCIS(Engine pEngine, InputStream pIS)
					throws IOException, ClassNotFoundException {
		return new CurryInputStream(pEngine, getConstructorData(pIS));
	}

	/** Constructs an ObjectWriter */
	protected CurryInputStream(Engine pEngine, ConstructorData pJCOISCD) throws IOException, ClassNotFoundException {
		super(pJCOISCD, pEngine.getClassPaths().getJavaCompiler());
		
		if(pEngine == null) throw new NullPointerException();
		this.Engine = pEngine;
	}
	
	/** Returns the engine of this Package */
	public Engine getEngine() {
		return this.Engine;
	}
}
