package net.nawaman.curry.compiler;

import java.util.Vector;

import net.nawaman.compiler.CompilationOptions;
import net.nawaman.curry.Scope;
import net.nawaman.curry.TypeRef;
import net.nawaman.curry.util.MoreData;

/** Compilation options */
public class CurryCompilationOptions {

	/** Data Name for Code Feeder Name */ static public final String DNCodeFeederName = "CodeFeederName";
	/** Data Name for Code Name        */ static public final String DNCodeName       = "CodeName";
	/** Data Name for Global Scope     */ static public final String DNGlobalScope    = "GlobalScope";
	/** Data Name for Top Scope        */ static public final String DNTopScope       = "TopScope";
	/** Data Name for Frozen Variables */ static public final String DNFVNames        = "FVNames";
	/** Data Name for IsLocal          */ static public final String DNIsLocal        = "IsLocal";
	/** Data Name for Offset           */ static public final String DNOffset         = "Offset";
	/** Data Name for EndPos           */ static public final String DNEndPos         = "EndPos";
	/** Data Name for Imports          */ static public final String DNImports        = "Imports";
	/** Data Name for ExtraData        */ static public final String DNExtraData      = "ExtraData";
	
	public CurryCompilationOptions() {}
	
	boolean        IsFrozen    = false;
	int            Offset      =    -1;
	int            EndPos      =    -1;
	Scope          GlobalScope =  null;
	Scope          TopScope    =  null;
	boolean        IsLocal     = false;
	Vector<String> FVNames     =  null;
	Vector<String> Imports     =  null;
	MoreData       ExtraData   =  null;
	String         CFName      =  null;
	String         CName       =  null;
	
	// This set of properties will only be set by a CProduct 
	boolean IsOwnerObject = false;
	TypeRef OwnerTypeRef  =  null;
	String  OwnerPackage  =  null;
	
	public void    freeze()   { this.IsFrozen = true; }
	public boolean isFrozen() { return this.IsFrozen; }

	public boolean setCodeFeederName(String pCFName) {
		if(this.IsFrozen) return false;
		this.CFName = pCFName;
		return true;
	}
	public String getCodeFeederName() {
		return this.CFName;
	}
	
	public boolean setCodeName(String pCName) {
		if(this.IsFrozen) return false;
		this.CName = pCName;
		return true;
	}
	public String getCodeName() {
		return this.CName;
	}
	
	public boolean setOffset(     int pOffset)   { if(this.IsFrozen) return false; this.Offset      = pOffset; return true; }
	public boolean setEndPos(     int pEndPos)   { if(this.IsFrozen) return false; this.EndPos      = pEndPos; return true; }
	public boolean setGlobalScope(Scope pGScope) { if(this.IsFrozen) return false; this.GlobalScope = pGScope; return true; }
	public boolean setTopScope(   Scope pTScope) { if(this.IsFrozen) return false; this.TopScope    = pTScope; return true; }
	public boolean toLocal()                     { if(this.IsFrozen) return false; this.IsLocal     =    true; return true; }
	
	public boolean setFrozens(String[] pFrozenVars) {
		if(this.IsFrozen) return false;
		this.FVNames = (pFrozenVars == null)?null:new Vector<String>(java.util.Arrays.asList(pFrozenVars));
		return true;
	}
	public boolean addFrozen(String pName) {
		if(this.IsFrozen) return false;
		if(pName == null) return false;
		if(this.FVNames == null) this.FVNames = new Vector<String>();
		this.FVNames.add(pName);
		return true;
	}
	
	public boolean setImports(String[] pImports) {
		if(this.IsFrozen) return false;
		this.Imports = (pImports == null)?null:new Vector<String>(java.util.Arrays.asList(pImports));
		return true;
	}
	public boolean addImport(String pImport) {
		if(this.IsFrozen) return false;
		if(pImport == null) return false;
		if(this.Imports == null) this.Imports = new Vector<String>();
		this.Imports.add(pImport);
		return true;
	}

	public boolean addMoreData(String pName, String pValue) {
		if(this.IsFrozen) return false;
		if(pName == null) return false;
		if(this.ExtraData == null)    this.ExtraData = new MoreData();
		if(this.ExtraData.isFrozen()) return false;
		this.ExtraData.setData(pName, pValue);
		return true;
	}
	
	// Other services --------------------------------------------------------------------------------------------------

	public net.nawaman.compiler.CompilationOptions newCompilerCompilationOption() {
		CompilationOptions.Simple CCOS = new CompilationOptions.Simple();
		this.setCompilerCompilationOption(CCOS);
		return CCOS;
	}
	public void setCompilerCompilationOption(net.nawaman.compiler.CompilationOptions.Simple pCCOS) {
		if(pCCOS == null) return;
		pCCOS.setData(DNGlobalScope,    this.GlobalScope);
		pCCOS.setData(DNTopScope,       this.TopScope   );
		pCCOS.setData(DNFVNames,        (this.FVNames == null)?null:this.FVNames.toArray(new String[this.FVNames.size()]));
		pCCOS.setData(DNIsLocal,        this.IsLocal    );
		pCCOS.setData(DNOffset,         this.Offset     );
		pCCOS.setData(DNEndPos,         this.EndPos     );
		pCCOS.setData(DNImports,        (this.Imports == null)?null:this.Imports.toArray(new String[this.Imports.size()]));
		pCCOS.setData(DNExtraData,      this.ExtraData  );
		pCCOS.setData(DNCodeFeederName, this.CFName);
		pCCOS.setData(DNCodeName,       this.CName);
	}

}
