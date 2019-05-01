package net.nawaman.curry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import net.nawaman.javacompiler.JavaCompiler;
import net.nawaman.util.UString;

public class MClassPaths extends EnginePart {
	
	protected MClassPaths(Engine pEngine) {
		super(pEngine);
	}
	
	/**{@inheritDoc}*/ @Override
	public MClassPaths getClassPaths() {
		return this;
	}
	
	// ClassPaths services ---------------------------------------------------------------------------------------------
	
	final private Vector<String> ClassPaths = new Vector<String>();
	final private JavaCompiler   JCompiler  = JavaCompiler.Instance;
	
	/** Returns the current java compiler of this engine. */
	public JavaCompiler getJavaCompiler() {
		return this.JCompiler;
	}
	
	/**
	 * Returns the class loaded that the given engine use. In this case, its JCompiler. If the given engine is null, the
	 * ClassLoader of Engine.class will be returned.
	 */
	static public ClassLoader getClassLoaderOf(Engine E) {
		if(E == null) return Engine.class.getClassLoader();
		return E.getClassPaths().getJavaCompiler().getCurrentClassLoader();
	}
	
	/**
	 * Returns the class loaded that the given engine use. In this case, its JCompiler. If the given engine is null, the
	 * ClassLoader of Engine.class will be returned.
	 */
	static ClassLoader getClassLoaderOf(Context C) {
		Engine E = null;
		if((C == null) || ((E = C.getEngine()) == null)) return Engine.class.getClassLoader();
		return E.getClassPaths().getJavaCompiler().getCurrentClassLoader();
	}
	
	/**
	 * Returns the class loaded that the given engine use. In this case, its JCompiler. If the given engine is null, the
	 * ClassLoader of Engine.class will be returned.
	 */
	static ClassLoader getClassLoaderOf(Context C, Engine E) {
		if((E != null) && ((C == null) || ((E = C.getEngine()) == null))) return Engine.class.getClassLoader();
		return E.getClassPaths().getJavaCompiler().getCurrentClassLoader();
	}
	
	/** Returns an array of the current set of class paths */
	public String[] getClassPathArray() {
		if(this.ClassPaths == null) return UString.EmptyStringArray;
		return this.ClassPaths.toArray(UString.EmptyStringArray);
	} 
	
	/** Add a required ClassPath */
	public boolean addClassPath(String pURLString) {
		if(pURLString == null) return false;
		try {
			URL Url = new URL(pURLString);
			if(this.ClassPaths == null) this.ClassPaths.clear();
			String URL = Url.toString().trim();
			this.JCompiler.addClasspathURL(URL);
			this.ClassPaths.add(URL);
			return true;
		} catch(MalformedURLException E) {
			try {
				URL Url = new URL("file://" + pURLString);
				if(this.ClassPaths == null) this.ClassPaths.clear();
				String URL = Url.toString().trim();
				this.JCompiler.addClasspathURL(URL);
				this.ClassPaths.add(URL);
				return true;
			} catch(MalformedURLException E2) {}
		}
		return false;
	}
	
	/** Add the no longer-required ClassPath */
	public boolean removeClassPath(String pURLString) {
		if(pURLString      == null) return false;
		if(this.ClassPaths == null) return false;
		try {
			URL Url = new URL(pURLString);
			if(this.ClassPaths == null) this.ClassPaths.clear();
			String URL = Url.toString().trim();
			if(this.ClassPaths.contains(URL)) {
				this.JCompiler.removeClasspathURL(URL);
				this.ClassPaths.remove(URL);
				return true;
			}
		} catch(MalformedURLException E) {
			try {
				URL Url = new URL("file://" + pURLString);
				if(this.ClassPaths == null) this.ClassPaths.clear();
				String URL = Url.toString().trim();
				if(this.ClassPaths.contains(URL)) {
					this.JCompiler.removeClasspathURL(URL);
					this.ClassPaths.remove(URL);
					return true;
				}
			} catch(MalformedURLException E2) {}
		}
		return false;
	}

}
