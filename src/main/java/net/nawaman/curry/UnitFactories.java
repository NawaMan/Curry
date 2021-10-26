package net.nawaman.curry;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;

import net.nawaman.compiler.Code;
import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.compiler.UnitDescription;
import net.nawaman.util.UArray;
import net.nawaman.util.UString;

public class UnitFactories {
	
	/** UnitFactory Memory */
	static public final class UFMemory extends UnitFactory {
		static final public String Kind = "Memory";
		UFMemory(Engine pEngine, String pUName, Package[] pPackages, CodeFeeder pCodeFeeder) {
			super(pEngine);
			Unit U = new Unit(this, pUName);
			
			// Save the code
			if((pCodeFeeder != null) && (pCodeFeeder.getCodeCount() != 0)) {
				U.CodeNames = new String[pCodeFeeder.getCodeCount()];
				U.Codes     = new Code[  pCodeFeeder.getCodeCount()];
				for(int i = pCodeFeeder.getCodeCount(); --i >= 0; ) {
					U.CodeNames[i] = pCodeFeeder.getCodeName(i);
					U.Codes[i]     = new Code.Simple(U.CodeNames[i], pCodeFeeder.getCode(U.CodeNames[i]).getSource());
				}
			}
			
			// Prepare the package
			if(pPackages == null) {
				U.PackageNames = UString.EmptyStringArray;
				U.Packages     = Package.EmptyPackageArray;
			} else {
				U.Packages     = UArray.getLeanArray(pPackages);
				U.PackageNames = new String[pPackages.length];
				for(int i = pPackages.length; --i >= 0; ) {
					U.PackageNames[i] = U.Packages[i].getName();
					U.Packages[i].Unit = U;
				}
			}
			
			this.Units = new Unit[] { U };
		}
		// ResouceType ------------------------------------------------------------
		@Override public String getResourceType() { return UFMemory.Kind; }
		// Unit -------------------------------------------------------------------
		// Returns the list of Units' names found in this current resource
		                 Unit[]     Units = null;
		@Override public Unit[] listUnits() { return this.Units; }
		// Loading ----------------------------------------------------------------
		// Returns the list of Packages' names found in the resource
		@Override protected String[] listPackage(String pResourceName) { return null; }
		// NOTE If the load fail, this function must report error to the engine
		//     directly and simply return null;
		@Override protected Package loadPackage(Unit pUnit, String pPackageName) { return null; }

		/** Returns the dependency information */
		public @Override DependencyInfo getDependencyInfo() {
			return null;
		}
		// Checks if the UnitFactory is up-to-date
		@Override public boolean isUpToDate() {
			return true;
		}
	}

	/** UnitFactory File */
	static public final class UFFile extends UnitFactory {
		
		static final public String Kind = "File";
		
		static public UFFile newInstance(Engine pEngine, String pName) {
			if(pName.endsWith("." + MUnit.UNIT_FILE_EXTENSION))
				pName = pName.substring(0, pName.length() - ("." + MUnit.UNIT_FILE_EXTENSION).length()) ;
			
			String UnitName = pName;
			String FileName = pName + "." + MUnit.UNIT_FILE_EXTENSION;
			
			DependencyInfo DInfo     = null;
			Package[]      Ps        = null;
			String[]       CodeNames = null;
			Code[]         Codes     = null;
			// Type to Open the file
			// If the file can't be open and it's not in the corrent format return null
			FileInputStream FIS = null;
			try {
				Object O = null;
				FIS = new FileInputStream(FileName);
				try (var OIS = new ObjectInputStream(FIS)) {
    				
    				// Check Signature of the file
    				O = OIS.readObject();
    				if(!MUnit.UNIT_FILE_SIGNATURE.equals(O)) {
    					System.err.println("File Unit Loading Error: Incompatible unit file (Wrong File Signature): " + pName);
    					return null;
    				}
    				
    				// Check Signature of the engine
    				O = OIS.readObject();
    				StringBuffer SB = new StringBuffer();
    				if(!pEngine.getEngineSpec().checkCompatibility((Serializable)O, SB) || (SB.length() != 0)) {
    					System.err.println(
    						String.format(
    							"File Unit Loading Error: Incompatible package unit (Wrong Engine Signature: %s): %s",
    							SB, pName
    						)
    					);
    					return null;
    				}
    				
    				// Get Packages
    				O = OIS.readObject();
    				if(O == null) return null;
    				if(!(O instanceof byte[])) {
    					System.err.println("File Unit Loading Error: Incompatible package unit (Invalid byte array): " + pName);
    					return null;
    				}
    				
    				byte[] Bytes = (byte[])O;
    				// Load the dependency info
    				O = LoadPackagesDependencyInfo(Bytes);
    				if(O == null) return null;
    				if(!(O instanceof DependencyInfo)) {
    					System.err.println("File Unit Loading Error: Incompatible package unit (Invalid dependency info array): " + pName);
    					return null;
    				}
    				DInfo = (DependencyInfo)O;
    				
    				// Load the package
    				O = LoadPackages(pEngine, Bytes);
    				if(O == null) return null;
    				if(!(O instanceof Package[])) {
    					System.err.println("File Unit Loading Error: Incompatible package unit (Invalid package Array): " + pName);
    					return null;
    				}
    				Ps = (Package[])O;
    
    				// Get Code Names
    				O = OIS.readObject();
    				if((O != null) && !(O instanceof String[])) {
    					System.err.println("File Unit Loading Error: Incompatible package unit (Invalid code name Array): " + pName);
    					return null;
    				}
    				CodeNames = (String[])O;
    
    				// Get Codes
    				O = OIS.readObject();
    				if((O != null) && !(O instanceof Code[])) {
    					System.err.println("File Unit Loading Error: Incompatible package unit (Invalid code Array): " + FileName);
    					return null;
    				}
    				Codes = (Code[])O;
    				
    				if((CodeNames != null) || (Codes != null)) {
    					boolean IsError = ((CodeNames == null) || (Codes == null));
    					if(IsError || (CodeNames.length != Codes.length)) {
    						System.err.println("File Unit Loading Error: Incompatible package unit (code names and codes "
    								+ "have different dimension): " + pName);
    						return null;
    					}
    				}
				}
				
			} catch(Exception E) {
				throw new CurryError("File Unit Loading Error: An error occurs while trying to load a unit: " + FileName, E);
				
			} finally {
				try { if(FIS != null) FIS.close(); }
				catch(Exception E) { return null;  }
			}
			
			// Checks if this up-to-date
			File            UDFile = new File(pName);
			UnitDescription UD     = null;
			boolean         IsUTD  = true;
			if(UDFile.exists() && (((UD = UnitDescription.Load(UDFile)) == null) || !UD.isUpToDate() || !UD.DInfo.equals(DInfo)))
				IsUTD = false;

			// Prepare the package
			Vector<Package> Packages = new Vector<Package>();
			for(int i = 0; i < Ps.length; i++) {
				if(Ps[i] != null) Packages.add(Ps[i]);
			}
			return new UFFile(UnitName, pEngine, Packages, DInfo, IsUTD, CodeNames, Codes);
		}
		
		UFFile(String pName, Engine pEngine, Vector<Package> pPackages, DependencyInfo pDInfo, boolean pIsUpTtDate,
				String[] pCodeNames, Code[] pCodes) {
			super(pEngine);
			
			this.DInfo      = pDInfo;
			this.IsUpToDate = pIsUpTtDate;
			
			Unit U = new Unit(this, pName);
			U.CodeNames = pCodeNames;
			U.Codes     = pCodes;
			
			// Unit
			this.Units = new Unit[] { U };
			// Packages
			this.Packages = new Package[pPackages.size()];
			pPackages.toArray(this.Packages);
			// PackageNames
			this.PackageNames = new String[pPackages.size()];
			for(int i = 0; i < this.Packages.length; i++) {
				Package P = this.Packages[i];
				if(P == null) continue;
				this.PackageNames[i] = P.getName();
			}
			this.Units[0].PackageNames = this.PackageNames;
			this.Units[0].Packages     = new Package[pPackages.size()];
		}
		
		// ResouceType ------------------------------------------------------------
		@Override public String getResourceType() {
			return UFFile.Kind;
		}
		
		// Unit ----------------------------------------------------------------
		// Returns the list of Units' names found in this current resource
		Unit[] Units = null;
		@Override public Unit[] listUnits() {
			return this.Units;
		}
		
		// Loading ----------------------------------------------------------------
		// Returns the list of Packages' names found in the unit
		String[]       PackageNames = null;
		Package[]      Packages     = null;
		
		final DependencyInfo DInfo;
		final boolean        IsUpToDate;
		@Override protected String[] listPackage(String pUnitName) {
			return this.PackageNames;
		}
		@Override protected Package loadPackage(Unit pUnit, String pPackageName) {
			if(pUnit != this.Units[0]) return null;
			for(int i = 0; i< this.Packages.length; i++) {
				if(UString.equal(pPackageName, this.Packages[i].getName())) return this.Packages[i];
			}
			return null;
		}
		
		/** Returns the dependency information */
		public @Override DependencyInfo getDependencyInfo() {
			return this.DInfo;
		}
		// Checks if the UnitFactory is up-to-date
		public @Override boolean isUpToDate() {
			return this.IsUpToDate;
		}
	}
		
	/** UnitFactory Folder * /
	static public final class UFFolder extends UnitFactory {
		
		static final public String Kind = "Folder";
		
		static public UFFolder newInstance(Engine pEngine, String pBasePath) {
			return new UFFolder(pEngine, pBasePath);
		}
		
		public UFFolder(Engine pEngine, String pBasePath) {
			super(pEngine);

			// TODO - Check -- Is this suppose to load only one unit but many package files?
			
			if(pBasePath == null) pBasePath = "";
			this.BasePath = pBasePath;
			File   Dir     = new File(pBasePath);
			File[] SubDirs = Dir.listFiles();
			Vector<Unit> UnitList = new Vector<Unit>();
			// Get Package List
			for(int i = 0; i < SubDirs.length; i++) {
				// Unit is a sub-folder of the base path that has at least a file with the extension.
				if(!SubDirs[i].isDirectory()) continue;
				File[] Files = SubDirs[i].listFiles();
				for(int j = 0; j < Files.length; j++) {
					if(!Files[j].isFile()) continue;
					if(!Files[j].getName().endsWith("." + UnitManager.PackageFileExtension)) continue;
					UnitList.add(new Unit(this, SubDirs[i].getName()));
					break;
				}
			}

			// TODO - This is not done yet
			String[]  CodeNames = null;
			Code[]    Codes     = null;
			
			this.Units = new Unit[UnitList.size()];
			UnitList.toArray(this.Units);
		}
		
		// ResouceType ------------------------------------------------------------
		@Override public String getResourceType() { return UFFolder.Kind; }
		
		// BasePath ---------------------------------------------------------------
		String BasePath = null;
		public String getBasePath() { return this.BasePath; }
		
		// Unit -------------------------------------------------------------------
		// Returns the list of Units' names found in this current resource
		Unit[] Units = null;
		@Override public Unit[] listUnits() { return this.Units; }
		
		// Loading ----------------------------------------------------------------
		// Returns the list of Packages' names found in the unit
		@Override protected String[] listPackage(String pUnitName) {
			Vector<String> PackageNameList = new Vector<String>();
			// package is each *.pkg files in those unit folder
			String DName = ((this.BasePath==null)?"":(this.BasePath + java.io.File.separator)) + pUnitName;
			File UnitFile = new File(DName);
			File[] Files = UnitFile.listFiles();
			for(int i = 0; i < Files.length; i++) {
				if(!Files[i].isFile()) continue;
				if(!Files[i].getName().endsWith("." + UnitManager.PackageFileExtension)) continue;
				PackageNameList.add(Files[i].getName().substring(0, (Files[i].getName().length() - 4)));
			}
			String[] PackageNames = new String[PackageNameList.size()];
			PackageNameList.toArray(PackageNames);
			
			return PackageNames;
		}

		@Override protected Package loadPackage(Unit pUnit, String pPackageName) {
			String DName = ((this.BasePath==null)?"":(this.BasePath + java.io.File.separator)) + pUnit.Name;
			String FName = DName + java.io.File.separator + pPackageName + "." + UnitManager.PackageFileExtension;
			
			FileInputStream FIS = null;
			Package P = null;
			try {
				Object O = null;
				FIS = new FileInputStream(FName);
				ObjectInputStream OIS = new ObjectInputStream(FIS);
				// Check Signature of the file
				O = OIS.readObject();
				if(!UnitManager.PackageFileSignature.equals(O))
					throw new CurryError("File Unit Loading Error: Incompatible package file (Wrong File Signature).");
				
				// Check Signature of the engine
				O = OIS.readObject();
				StringBuffer SB = new StringBuffer();
				if(!this.Engine.getEngineSpec().checkCompatibility((Serializable)O, SB) || (SB.length() != 0))
					throw new CurryError("File Unit Loading Error: Incompatible package file (Wrong Engine Signature)("+SB.toString()+").");
					
				O = OIS.readObject();
				if(O == null) return null;
				if(!(O instanceof Package)) 
					throw new CurryError("File Unit Loading Error: Incompatible package data (Invalid package Array).");
				
				P = (Package)O;
				return P;
			} catch(Exception E) {
				throw new CurryError("File Unit Loading Error: An error occurs while trying to load a unit.", E);
			} finally {
				try { if(FIS != null) FIS.close(); }
				catch(Exception E) { return P;     }
			}
		}
	}*/
}
