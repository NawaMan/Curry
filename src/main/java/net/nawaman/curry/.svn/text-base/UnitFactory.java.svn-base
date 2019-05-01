package net.nawaman.curry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import net.nawaman.curry.UnitBuilder.PackageInputStream;

abstract public class UnitFactory {
	
	protected UnitFactory(Engine pEngine) {
		this.Engine = pEngine;
	}
	
	// ResouceType ------------------------------------------------------------
	abstract public String getResourceType();
	
	// Engine -----------------------------------------------------------------
	final  Engine Engine;
	public Engine getEngine() { return this.Engine; }
	
	// UnitManager ------------------------------------------------------------
	       MUnit UM = null;
	public MUnit getUnitManager() { return this.UM; }
	
	// Unit -------------------------------------------------------------------
	// Returns the list of Units' names found in this current resource
	abstract public Unit[] listUnits();
	
	// Loading ----------------------------------------------------------------
	// Returns the list of Packages' names found in the resource
	abstract protected String[] listPackage(String pUnitName);

	// NOTE If the load fail, this function must report error to the engine
	//     directly and simply return null;
	// This should be called from the Unit where the unit knows the package
	//     exists so if the function fail to read the Package, it must
	//     report error by itself via Engine.
	abstract protected Package loadPackage(Unit pUnit, String pPackageName);

	
	// Returns the dependency information
	abstract public DependencyInfo getDependencyInfo(); 
	// Checks if the UnitFactory is up-to-date
	abstract public boolean        isUpToDate();
	
	// Load and save ---------------------------------------------------------------------------------------------------
	
	/** Load PackagesDependencyInfo from the byte array. **/
	final static protected DependencyInfo LoadPackagesDependencyInfo(byte[] Bytes)
	                    throws IOException, ClassNotFoundException {
		ByteArrayInputStream BAIS = null;
		ObjectInputStream    OIS  = null; 
		try {
			OIS = new ObjectInputStream(BAIS = new ByteArrayInputStream(Bytes));
			
			if(!UnitBuilder.SAVE_PACKAGE_PROTOCOL_NAME.equals(OIS.readUTF()))
				throw new IllegalArgumentException("In compatible data stream format. ");
			
			// Read the object
			return (DependencyInfo)OIS.readObject();
		} finally {
			if(BAIS != null) BAIS.close();
			if(OIS  != null) OIS .close();
		}
	}
	
	/**
	 * Load Package to an array of byte.
	 * 
	 * You MUST use this method to load the package as it maintain the signature and dependency.
	 **/
	final static protected Package[] LoadPackages(Engine pEngine, byte[] Bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream BAIS = null;
		ObjectInputStream    OIS  = null; 
		try {
			OIS = new ObjectInputStream(BAIS = new ByteArrayInputStream(Bytes));
			
			if(!UnitBuilder.SAVE_PACKAGE_PROTOCOL_NAME.equals(OIS.readUTF()))
				throw new IllegalArgumentException("In compatible data stream format. ");
			
			// Read the object
			DependencyInfo PDInfo = (DependencyInfo)OIS.readObject();
			
			// Check this
			if(PDInfo.RequiredEngineExts != null) {
				for(int i = PDInfo.RequiredEngineExts.length; --i >= 0; ) {
					String EName = PDInfo.RequiredEngineExts[i];
					EngineExtension EE = pEngine.getExtension(EName);
					if((EName != null) && (EE == null))
						throw new CurryError("Unknown Engine Extension: `"+EName+"`");
				}
			}
			
			// Read the package
			PackageInputStream PIS  = PackageInputStream.newPIS(pEngine, new ByteArrayInputStream((byte[])OIS.readObject()));
			return (Package[])PIS.readObject();
		} finally {
			if(BAIS != null) BAIS.close();
			if(OIS  != null) OIS .close();
		}
	}

}
