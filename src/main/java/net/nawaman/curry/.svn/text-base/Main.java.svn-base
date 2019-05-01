package net.nawaman.curry;

import java.io.File;
import java.io.FileNotFoundException;

import net.nawaman.curry.compiler.CurryLanguage;
import net.nawaman.curry.script.CurryEngine;
import net.nawaman.script.Executable;
import net.nawaman.script.Function;
import net.nawaman.script.Macro;
import net.nawaman.script.Script;
import net.nawaman.script.Signature;
import net.nawaman.script.Tools;

public class Main {

	static public final String FileExtension = ".curry"; 

	static public void main(String ... $Args) {
		
		//$Args = new String[] { "Test.curry", "", "param", "" };
		
		if(($Args == null) || ($Args.length == 0)) {
			System.out.println("Please tell me something ...\n");
			return;
		}
		
		String   ScriptName  = $Args[0];
		Object[] ScriptParams = new Object[$Args.length - 1];
		for(int i = 0; i < ScriptParams.length; i++) ScriptParams[i] = $Args[i + 1];
		
		Executable Exec = null;
		if(!(new File(ScriptName)).exists()) {
			if((new File(ScriptName + FileExtension)).exists())
				ScriptName = ScriptName + FileExtension;
			else {
				// Simple script file
				try { Exec = Tools.Use(ScriptName); }
				catch (FileNotFoundException E) {}
				catch (Exception E) { throw new RuntimeException(E); }
				
				if(Exec == null) {
					System.out.println("The script file does not exist.");
					return;
				}
			}
		}

		if(Exec == null) {
			Engine.IsToVocal = false;
			CurryLanguage $CLanguage = CurryLanguage.Util.GetGetCurryLanguage("Curry").getCurryLanguage(null, null);
			Engine        $Engine    = $CLanguage.getTargetEngine();
			MUnit         $Units     = $Engine.getUnitManager();
			
			CurryEngine.registerCurryEngine($CLanguage);
			$Units.discoverUsepaths();
			
			File F = new File(ScriptName);
			
			try { Exec = Tools.Use(F, false); }
			catch (Exception E) { throw new RuntimeException(E); }
		}

		if(Exec instanceof Script) {
			((Script)Exec).run();
			return;
		}
		if(Exec instanceof Macro) {
			Signature Signature = ((Macro)Exec).getSignature();
			Object[]  Params    = net.nawaman.script.Signature.Simple.adjustParameters(Signature, (Object[])ScriptParams);
			Object    Result    = ((Macro)Exec).run((Object[])Params);
			
			if(Signature.getReturnType() != Void.class)
				System.out.println(Result);

			return;
		}
		if(Exec instanceof Function) {
			Signature Signature = ((Function)Exec).getSignature();
			Object[]  Params    = net.nawaman.script.Signature.Simple.adjustParameters(Signature, (Object[])ScriptParams);
			Object    Result    = ((Function)Exec).run((Object[])Params);
			
			if(Signature.getReturnType() != Void.class)
				System.out.println(Result);
			
			return;
		}
		
		throw new RuntimeException("Unknown script.");
	}
}
