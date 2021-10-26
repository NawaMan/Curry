package net.nawaman.curry.script;

import net.nawaman.curry.Engine;
import net.nawaman.curry.ExecSignature;
import net.nawaman.curry.Location;
import net.nawaman.curry.MType;
import net.nawaman.curry.TKJava;
import net.nawaman.curry.Type;
import net.nawaman.curry.TypeRef;
import net.nawaman.script.ExecutableInfo;
import net.nawaman.script.ScriptManager;
import net.nawaman.script.Signature;
import net.nawaman.util.UClass;
import net.nawaman.util.UString;

public class CurrySignature implements Signature {
    
    private static final long serialVersionUID = -3009836901006887137L;
	
	public CurrySignature(CurryEngine pCEngine, ExecSignature pES) {
		if(pES == null) throw new NullPointerException();
		this.CEName       = pCEngine.getName();
		this.TheSignature = pES;
	}

	String        CEName;
	String        CEParam;
	ExecSignature TheSignature;
	
	protected Engine getTheEngine() {
		CurryEngine CEngine = null;
		
		if(!CurryEngine.DEFAULT_ENGINE_NAME.equals(this.CEName))
			 CEngine = (CurryEngine)ScriptManager.Instance.getDefaultEngineOf(this.CEName);
		else CEngine = CurryEngine.getEngine(this.CEParam);
		
		return CEngine.TheEngine;
	}
	
	/**{@inheritDoc}}*/@Override
	public String  getName() {
		return this.TheSignature.getName();
	}
	/**{@inheritDoc}}*/@Override
	public boolean isVarArgs() {
		return this.TheSignature.isVarArgs();
	}
	/**{@inheritDoc}}*/@Override
	public int getParamCount() {
		return this.TheSignature.getParamCount();
	}

	/**{@inheritDoc}}*/@Override
	public Class<?> getParamType(int pPos) {
		if((pPos < 0) || (pPos >= this.getParamCount())) return null;
		Class<?> PClass = this.getTheEngine().getTypeManager().getDataClassOf(this.TheSignature.getParamTypeRef(pPos));
		
		// TODO SimpleScript Signature are not the same as Curry Signature
		if(this.TheSignature.isVarArgs())
			PClass = PClass.getComponentType();
		
		return PClass;
	}
	/**{@inheritDoc}}*/@Override
	public Class<?> getReturnType() {
		return this.getTheEngine().getTypeManager().getDataClassOf(this.TheSignature.getReturnTypeRef());
	}

	// Utilities -------------------------------------------------------------------------------------------------------
	
	/** Returns the signature from ExecSignature */
	static public Signature toSignature(Engine pEngine, ExecSignature pExecSign) {
		MType MT = pEngine.getTypeManager();
		
		Class<?> Return = MT.getDataClassOf(pExecSign.getReturnTypeRef());
		if(Return == null) Return = Object.class;
		
		int        PCount = pExecSign.getParamCount();
		Class<?>[] Params = (PCount == 0) ? UClass.EmptyClassArray : new Class<?>[PCount];
		for(int i = PCount; --i >= 0; ) {
			Params[i] = MT.getDataClassOf(pExecSign.getParamTypeRef(i));
			if(Params[i] == null) Params[i] = Object.class; 
		}
		
		String  Name      = pExecSign.getName();
		boolean IsVarArgs = pExecSign.isVarArgs();

		// TODO SimpleScript Signature are not the same as Curry Signature
		if(IsVarArgs)
			Params[PCount - 1] = Params[PCount - 1].getComponentType();
		
		return new Signature.Simple(Name, Return, IsVarArgs, Params);
	}
	
	/** Returns the signature from ExecSignature */
	static public ExecutableInfo toExecutableInfo(Engine pEngine, String Kind, ExecSignature pExecSign) {
		Signature Signature = null;
		if     ("main"    .equals(Kind)) return ExecutableInfo.getMainExecutableInfo(pExecSign.getName());
		else if("macro"   .equals(Kind)) Signature = toSignature(pEngine, pExecSign);
		else if("function".equals(Kind)) Signature = toSignature(pEngine, pExecSign);
		
		String[] ParamNames = null;
		if(Signature != null) {
			int PCount = pExecSign.getParamCount();
			ParamNames = (PCount == 0) ? UString.EmptyStringArray : new String[PCount];
			for(int i = PCount; --i >= 0; ) {
				ParamNames[i] = pExecSign.getParamName(i);
				if(ParamNames[i] == null) ParamNames[i] = "Param" + i;
			}
		}
		
		return new ExecutableInfo(null, Kind, Signature, null, ParamNames);
	}

	/** Returns the ExecSignature from signature */
	static public ExecSignature toExecSignature(Engine pEngine, Signature Signature, String[] PNames, Location Location) {
		MType MT = pEngine.getTypeManager();
		Type  RT = MT.getTypeOfTheInstanceOf(Signature.getReturnType());
		
		String  SName     = Signature.getName();
		boolean IsVarArgs = Signature.isVarArgs();
		TypeRef Return    = (RT == null) ? TKJava.TAny.getTypeRef() : RT.getTypeRef();
		
		if(PNames == null)   PNames = UString.EmptyStringArray;
		if(Location == null) Location = new Location(SName);
		
		int       PCount     = Signature.getParamCount();
		String[]  ParamNames = (PCount != PNames.length) ? new String[PCount]        : PNames;
		TypeRef[] ParamTypes = (PCount ==             0) ? TypeRef.EmptyTypeRefArray : new TypeRef[PCount];
		for(int i = PCount; --i >= 0; ) {
			String Name = ((PNames == null) || (i >= PNames.length)) ? null : PNames[i];
			if((Name == null) || (Name.length() == 0)) Name = ExecSignature.AutoParamNamePrifix + 1;
			ParamNames[i] = Name;
			
			Type T        = MT.getTypeOfTheInstanceOf(Signature.getParamType(i));
			ParamTypes[i] = (T == null) ? TKJava.TAny.getTypeRef() : T.getTypeRef();
		}
		
		return ExecSignature.newSignature(SName, ParamTypes, ParamNames, IsVarArgs, Return, Location, null);
	}
	
}
