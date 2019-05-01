package net.nawaman.curry.compiler;

import net.nawaman.script.Signature;

public interface GetCurryLanguage {

	/** The Signature for the getCurryLanguage Function */
	static public final Signature SIGNATURE_GET_CURRY_LANGUAGE =
	                                  new Signature.Simple(
	                                		  "getCurryLanguage",
	                                		  GetCurryLanguage.class,
	                                		  false,
	                                		  String.class,
	                                		  String.class
	                                );
	
	
	// Service ---------------------------------------------------------------------------------------------------------
	
	/** Returns the CurryLanguage from the Language Name and EngineName */
	public CurryLanguage getCurryLanguage(String LangName, String EngineName);

}
