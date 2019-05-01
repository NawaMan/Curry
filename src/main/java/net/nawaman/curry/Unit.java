package net.nawaman.curry;

import net.nawaman.compiler.Code;
import net.nawaman.util.UArray;

final public class Unit {
	
	public Unit(UnitFactory pFactory, String pName) {
		this.Factory = pFactory;
		this.Name    = pName;
		this.PackageNames = (this.Factory == null)
		                     ?new String[0]
		                     :UArray.getLeanArray(this.Factory.listPackage(pName));
		if(this.PackageNames == null) this.PackageNames = new String[0];
		this.Packages = new Package[this.PackageNames.length];
	}
	
	// PackageFactory ---------------------------------------------------------
	             UnitFactory Factory;
	final public UnitFactory getUnitFactory() {
		return this.Factory;
	}
	
	// Resource ---------------------------------------------------------------
	final  String Name;
	public String getUnitName() {
		return this.Name;
	}
	public String getResourceType() {
		return this.Factory.getResourceType();
	}
	
	// Engine -----------------------------------------------------------------
	public Engine getEngine() {
		return this.Factory.getEngine();
	}
	
	// UnitManager -----------------------------------------------------------
	public MUnit getUnitManager() {
		return this.Factory.getUnitManager();
	}
	
	// Code -------------------------------------------------------------------
	String[] CodeNames = null;
	Code[]   Codes     = null;
	public int getCodeCount() {
		return (this.CodeNames == null)?0:this.CodeNames.length;
	}
	/** The name of each code file */
	public String getCodeName(int pIndex) {
		if((pIndex < 0) || (pIndex >= this.getCodeCount())) return null;
		return this.CodeNames[pIndex];
	}
	public Code getCode(String pName) {
		if(pName == null) return null;
		for(int i = this.CodeNames.length; --i >= 0; ) {
			if(pName.equals(this.CodeNames[i])) return this.Codes[i];
		}
		return null;
	}

	// Package ----------------------------------------------------------------
	protected String[]  PackageNames = null;	// Assigned by Factory
	protected Package[] Packages     = null;	// Assigned by Factory
	
	public int getPackageCount() {
		return (this.PackageNames == null)?0:this.PackageNames.length;
	}
	public String getPackageNameAt(int I) {
		if((I < 0) || (I >= this.getPackageCount())) return null;
		return this.PackageNames[I];
	}
	
	protected int indexOfPackage(String pPackageName) {
		for(int i = 0; i < this.PackageNames.length; i++) {
			if(pPackageName.equals(this.PackageNames[i])) return i;
		}
		return -1;
	}
	public boolean containPackage(String pPackageName) {
		return (this.indexOfPackage(pPackageName) != -1);
	}
	public Package getPackage(String pPackageName) {
		return this.getPackage(null, pPackageName);
	}
	public Package getPackage(int pPackageIndex) {
		return this.getPackage(null, pPackageIndex);
	}
	
	Package getPackage(Context pContext, int pPackageIndex) {
		if((pPackageIndex < 0) || (pPackageIndex >= this.getPackageCount())) return null;
		// Load the package, if it has not yet loaded
		if(this.Packages[pPackageIndex] == null) this.loadPackage(pContext, pPackageIndex);
		return this.Packages[pPackageIndex];
	}
	Package getPackage(Context pContext, String pPackageName) {
		int Ind = this.indexOfPackage(pPackageName);
		if(Ind == -1) return null;
		return this.getPackage(pContext, Ind);
	}
	private void loadPackage(Context pContext, int pInd) {
		// Ensure it has not yet loaded
		if(this.Packages[pInd] != null) return;
		String PackageName = this.PackageNames[pInd];
		
		// Ensure that the name of package is valid
		if(PackageName == null) throw new NullPointerException("Invalid package name ('null')");
		// Check the default package
		if(PackageName.equals(EngineExtensions.EE_DefaultPackage.DefaultPackageName)) {
			// Ensure that default package is enabled
			if(this.Factory.Engine.getExtension(EngineExtensions.EE_DefaultPackage.Name) == null) {
				throw new CurryError("The package name '"+EngineExtensions.EE_DefaultPackage.DefaultPackageName+
						             "' is reserved for default package which is not enabled in this engine.", pContext);
			}
			// Ensure that Default package is in the default unit
			if(!this.Name.equals(EngineExtensions.EE_DefaultPackage.DefaultUnitName)) {
				throw new CurryError("The package name '"+EngineExtensions.EE_DefaultPackage.DefaultPackageName+
			             "' is reserved for default package and it must be in a default unit that must be named '"+
			             EngineExtensions.EE_DefaultPackage.DefaultUnitName+"'.", pContext);
			}
		}
		
		// The Package has not yet loaded, Load the Package.
		this.Packages[pInd] = this.Factory.loadPackage(this, PackageName);
		
		// Fail to load
		if(this.Packages[pInd] == null)
			throw new CurryError("An unknown problem preventing the package '"+PackageName+"' from being loaded.", pContext);
		
		// Check compatibility (ignore this step if the unit factory is a memomy)
		StringBuffer SB = new StringBuffer();
		if(!(this.Factory instanceof UnitFactories.UFMemory) &&
		   !this.getEngine().getEngineSpec().checkCompatibility(
				                               this.Packages[pInd].getEngineSpecSignature(), SB)) {

			this.Packages[pInd] = null;
			// Incompatible package
			throw new CurryError("The loaded Package '"+ PackageName+"' is not compatible with " +
				                     "this engine because of " +
				                     ((SB.length() == 0)
				                     	?"an unknown error."
				                     	:"the following error: "+SB.toString()+"."), pContext);
		}
		// Compatible .... So initialize and return.
		this.Packages[pInd].Unit = this;
	}

}
