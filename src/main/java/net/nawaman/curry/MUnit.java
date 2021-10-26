package net.nawaman.curry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.compiler.CodeFeeders;
import net.nawaman.compiler.CompilationOptions;
import net.nawaman.curry.UnitFactories.UFFile;
import net.nawaman.curry.compiler.CFUnit;
import net.nawaman.curry.compiler.CompileProduct;
import net.nawaman.curry.compiler.CurryCompiler;
import net.nawaman.curry.compiler.CurryLanguage;
import net.nawaman.curry.compiler.EE_Language;
import net.nawaman.curry.compiler.UnitBuilderCreator;
import net.nawaman.curry.compiler.UnitDescription;
import net.nawaman.script.ScriptManager;
import net.nawaman.usepath.FileExtFilenameFilter;
import net.nawaman.usepath.FileExtFilter;
import net.nawaman.util.UClass;
import net.nawaman.util.UString;

/** Manage units */
public class MUnit extends EnginePart {
	
	static final public String UNIT_FILE_EXTENSION = "cuf";
	static public final String UNIT_FILE_SIGNATURE = "UNIT 1.0";
	
	protected MUnit(Engine pEngine) {
		super(pEngine);
		
		this.UNames        = new Vector<String>();
		this.ResourceTypes = new Vector<String>();
		this.Units         = new Vector<Unit>();
		
		this.registerResourceKind(UnitFactories.UFMemory.Kind, UnitFactories.UFFile.class);
		this.registerResourceKind(UnitFactories.UFFile.  Kind, UnitFactories.UFFile.class);
	}
		
	/**{@inheritDoc}*/ @Override
	public MUnit getUnitManager() {
		return this.TheEngine.getUnitManager();
	}
	
	// Resource Kind -----------------------------------------------------------
	
	final Hashtable<String, Method> ResourceKinds = new Hashtable<String, Method>();
	
	public boolean registerResourceKind(String pRKName, Class<? extends UnitFactory> pUFClass) {
		if((pRKName  == null) || (pUFClass == null)) return false;
		if(this.ResourceKinds.get(pRKName) != null) return false;
		Method M = UClass.getMethodByParamClasses(pUFClass, "newInstance", true, new Class[] { Engine.class, String.class });
		if(M == null)                                                  return false;
		if(!Modifier.isPublic(M.getModifiers()))                       return false;
		if(!UnitFactory.class.isAssignableFrom(M.getDeclaringClass())) return false;
		
		this.ResourceKinds.put(pRKName, M);
		return true;
	}
	
	public int getResourceKindCount() {
		return (this.ResourceKinds == null)?0:this.ResourceKinds.size();
	}
	public String getResourceKindName(int I) {
		if((I < 0) && (I >= this.getResourceKindCount())) return null;
		int i = I;
		for(String N : this.ResourceKinds.keySet()) { if(--i == 0) return N; }
		return null;
	}
	@SuppressWarnings("unchecked")
	public Class<UnitFactory> getResourceKindClass(int I) {
		if((I < 0) && (I >= this.getResourceKindCount())) return null;
		int i = I;
		for(String N : this.ResourceKinds.keySet()) {
			if(--i == 0) return (Class<UnitFactory>)this.ResourceKinds.get(N).getDeclaringClass();
		}
		return null;
	}
	
	// Unit Factory -----------------------------------------------------------

	HashSet<String> AlreadyDicoveredUP = new HashSet<String>();
	boolean         IsBeingDiscovering = false;
	
	static final FileExtFilenameFilter CUFFilter = new FileExtFilenameFilter.Simple(new FileExtFilter.FEFExtList("cuf"));
	
	public String[] discoverUsepath(File UPDir) {
		if((UPDir == null) || !UPDir.isDirectory() || !UPDir.exists() || !UPDir.canRead()) return UString.EmptyStringArray;
		
		File[] Fs = UPDir.listFiles(CUFFilter);
		if((Fs == null) || (Fs.length == 0)) return UString.EmptyStringArray;
		
		String[] Ss = new String[Fs.length];
		for(int i = 0; i < Ss.length; i++)
			Ss[i] = Fs[i].getAbsolutePath();
		
		return Ss;
	}
	public Boolean discoverUsepaths() {
		if(this.IsBeingDiscovering) return null;
		
		try {
			this.IsBeingDiscovering = true;
			
			String[] UPs = ScriptManager.Usepaths.getUsepaths();
			if(UPs.length == this.AlreadyDicoveredUP.size()) return false;
			
			boolean HasLanguage = (this.getEngine().getExtension(EE_Language.Name) instanceof EE_Language);
			
			HashSet<UnitFactory>     LoadedUFs     = new HashSet<UnitFactory>();
			HashSet<UnitDescription> AllUDs        = null;
			boolean                  IsAllUpToDate = true;
			
			// Recover the new unloaded/unregisted unit factories
			for(int i = 0; i < UPs.length; i++) {
				// Only the one that has not yet discovered
				String UP = UPs[i];
				if((UP == null) || (UP.length() == 0) || (this.AlreadyDicoveredUP.contains(UP))) continue;
				
				File UPFile       = new File(UP);
				File UPFile_Units = new File(UP + "/units");	// Automatically scanned
				
				// Discover
				List<String> LUFs_Normal = Arrays.asList(this.discoverUsepath(UPFile));
				List<String> LUFs_Units  = Arrays.asList(this.discoverUsepath(UPFile_Units));
				
				if((LUFs_Normal.size() != 0) || (LUFs_Units.size() != 0)) {
					Vector<String> LUFs = new Vector<String>();
					LUFs.addAll(LUFs_Normal);
					LUFs.addAll(LUFs_Units);
			
					// Load the UnitFactories but not yet registered
					for(String LUF : LUFs) {
						UnitFactory UF = this.loadUnitFactory(UnitFactories.UFFile.Kind + "://" + LUF);
						if(UF == null) continue;
						
						LoadedUFs.add(UF);
						if(!UF.isUpToDate()) IsAllUpToDate = false;
					}
				}
				
				// If there is a language extension, also try to recover UnitDescription
				if(HasLanguage) {
					if(AllUDs == null) AllUDs = new HashSet<UnitDescription>();
						
					// Discover
					List<UnitDescription> LUDs_Normal = Arrays.asList(UnitDescription.Discover(UPFile));
					List<UnitDescription> LUDs_Units  = Arrays.asList(UnitDescription.Discover(UPFile_Units));
						
					if((LUDs_Normal.size() == 0) && (LUDs_Units.size() == 0)) continue;
					Vector<UnitDescription> LUDs = new Vector<UnitDescription>();
					LUDs.addAll(LUDs_Normal);
					LUDs.addAll(LUDs_Units);
						
					AllUDs.addAll(LUDs);
				}
			}
			
			if(HasLanguage && (!IsAllUpToDate || ((AllUDs != null) && (AllUDs.size() != 0)))) {
				
				// Index of package in unit decription files
				HashMap<String, UnitFactory>     Packages_UpToDate = new HashMap<String, UnitFactory>();
				HashMap<String, UnitDescription> Packages_Dated    = new HashMap<String, UnitDescription>();
				HashMap<String, UnitDescription> Packages_Unsure   = new HashMap<String, UnitDescription>();
				
				// Add the one loaded and up-to-date packages -----------------------------------------
				for(int i = 0; i < this.getUnitCount(); i++) {
					Unit        U  = this.getUnit(i);
					UnitFactory UF = U.getUnitFactory();
					
					int PCount = U.getPackageCount();
					for(int p = 0; p < PCount; p++)
						Packages_UpToDate.put(U.getPackageNameAt(p), UF);	// Up-to-date
				}
	
				// Add the loaded ones as up-to-date if that is it -------------------------------------
				for(UnitFactory UF : LoadedUFs) {
					String[] PNames = UF.getDependencyInfo().PackageNames;
					int PCount = PNames.length;
					for(int p = 0; p < PCount; p++) {
						if(UF.isUpToDate()) Packages_UpToDate.put(PNames[p], UF);	// Up-to-date
					}
				}
	
				// Collects all package name found in UnitDescription and not in the Packages_In_UF list
				// These are list of package that are not up-to-date
				for(UnitDescription UD : AllUDs) {
					for(String PN : UD.getPackages()) {
						if(Packages_UpToDate.containsKey(PN))
							 Packages_Unsure.put(PN, UD);	// Up-to-date with source
						else Packages_Dated .put(PN, UD);	// No-up-to-date
					}
				}
				
				// Find all up-to-date package that required non up-to-date packages and mark
				while(Packages_Dated.size() != 0) {
					UnitFactory          UF_ToBeRemoved = null;
					HashSet<UnitFactory> AllUFs         = new HashSet<UnitFactory>(Packages_UpToDate.values());
					UFLoop: for(UnitFactory UF : AllUFs) {
						DependencyInfo DInfo = UF.getDependencyInfo();
						if(DInfo == null) continue;
						
						int PCount = DInfo.getRequiredPackageCount();
						for(int p = 0; p < PCount; p++) {
							String PName = DInfo.getRequiredPackage(p);
							if(!Packages_Dated.containsKey(PName)) continue;	// Up-to-date use up-to-date
							
							UF_ToBeRemoved = UF;
							break UFLoop;
						}
					}
					if(UF_ToBeRemoved == null) break;
					
					// There is some up-to-date unit to be marked as not up-to-date
					HashSet<String> No_longer_UTD_Packages = new HashSet<String>();
					for(String PName : Packages_UpToDate.keySet()) {
						if(UF_ToBeRemoved != Packages_UpToDate.get(PName)) continue;
						No_longer_UTD_Packages.add(PName);
					}
					for(String PName : No_longer_UTD_Packages) {
						// Remove no-longer-up-to-date packages from up to date list
						Packages_UpToDate.remove(PName);
						// Add UD in to the not up-to-date list and remove the package from the un-sure
						Packages_Dated.put(PName, Packages_Unsure.get(PName));
						Packages_Unsure.remove(PName);
					}
				}
				
				// No files to be compiled
				if(Packages_Dated.size() != 0) {
					
					// Register those that are up-to-date
					for(UnitFactory UF : new HashSet<UnitFactory>(Packages_UpToDate.values())) {
						if(!LoadedUFs.contains(UF)) continue;	// Some UF in Packages_UpToDate are already registeded
						this.registerUnitFactory(UF);
						LoadedUFs.remove(UF);
					}
					
					// All UD to be compiled
					AllUDs = new HashSet<UnitDescription>(Packages_Dated.values());
					
					CodeFeeders CFs; 
					
					// Create Codefeeders
					Vector<CodeFeeder> VCFs = new Vector<CodeFeeder>();
					for(UnitDescription UD : AllUDs)
						VCFs.add(new CFUnit(UD));
						
					// Prepare CodeFeeders
					CFs = new CodeFeeders(VCFs.toArray(new CodeFeeder[VCFs.size()]));
					
					// Prepare option
					CompilationOptions.Simple CCOptions = new CompilationOptions.Simple();
					CCOptions.setData(CurryCompiler.DNUnitBuilderCreator, UnitBuilderCreator.File);
				 
					CurryLanguage  CL = ((EE_Language)this.getEngine().getExtension(EE_Language.Name)).getDefaultLanguage();
					CompileProduct CP = CL.compileFiles(CFs, CCOptions);
	
					if(CP.getFeederCount() == 0) System.out.println("No compile needed.");
					else {
						System.out.println("Compilation needed.");
						System.out.println(CP);
						   
						if(!CP.hasErrMessage()) {
							// Re-load the UnitFactory
							for(UnitFactory UF : LoadedUFs) {
								((UFFile)UF).getResourceType();
								// Reload and register the unit
								String ResName = UFFile.Kind + "://" + UF.listUnits()[0].getUnitName() + "." + MUnit.UNIT_FILE_EXTENSION;
								UF = this.loadUnitFactory(ResName);
								this.registerUnitFactory(UF);
							}
						}
						
						LoadedUFs.clear();
					} 
				}
			}
	
			// Register the UnitFactories ----------------------------------------------------------
			for(UnitFactory UF : LoadedUFs)
				this.registerUnitFactory(UF);
	
			// Add the UPs as already recovered
			this.AlreadyDicoveredUP.addAll(Arrays.asList(UPs));
			
			return false;
			
		} finally {
			this.IsBeingDiscovering = false;
		}
	}
	
	final Vector<UnitFactory> UFactories = new Vector<UnitFactory>();
	
	/** Register a unit factory */
	public void registerUnitFactory(UnitFactory pUnitFactory) {
		if(pUnitFactory == null) return;
		if(this.UFactories.contains(pUnitFactory)) {
			throw new CurryError(
						"Linkage Error: Error while registering UnitFactory: " +
						"The Resource Type '"+pUnitFactory.toString()+"' is already in used.");
		}
		this.UFactories.add(pUnitFactory);
		// Get units from the factory and register them
		Unit[] Us = pUnitFactory.listUnits();
		for(Unit U : Us) {
			U.Factory = pUnitFactory;
			this.registerUnit(U);
		}
		return;
	}
	
	/** Register a unit factory */
	public void registerUnitFactory(String pUFURI) {
		UnitFactory UF = this.loadUnitFactory(pUFURI);
		if(UF == null) return;

		// It is not up to date - Just warning
		if(!UF.isUpToDate()) System.err.println("A UNIT is NOT UP-TO-DATE!!! ("+pUFURI+")");
		
		this.registerUnitFactory(UF);
	}
	
	/** Register a unit factory */
	UnitFactory loadUnitFactory(String pUFURI) {
		if(pUFURI == null) return null;
		int I = pUFURI.indexOf(ResourceToNameSeparator);
		if(I == -1) throw new CurryError("Linkage Error: Unable to find the unit `"+pUFURI+"`.");
		
		String ResType = getResType(pUFURI);
		String ResName = getUnitName(pUFURI);
		
		Method M = this.ResourceKinds.get(ResType);
		if(M == null) throw new CurryError("Linkage Error: Unknown resource kind `"+ResType+"` (`"+pUFURI+"`).");
		
		UnitFactory UF = null;
		try {
			UF = (UnitFactory)M.invoke(M.getDeclaringClass(), new Object[] { this.TheEngine, ResName });
		} catch (IllegalAccessException IAE) {
			ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
			PrintStream PS = new PrintStream(BAOS);
			IAE.printStackTrace(PS);
			throw new CurryError("Linkage Error: Unknown error cause by `"+IAE.toString()+"` (`"+pUFURI+"`)."
					+ BAOS.toString());
		} catch (InvocationTargetException IAE) {
			ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
			PrintStream PS = new PrintStream(BAOS);
			IAE.printStackTrace(PS);
			throw new CurryError("Linkage Error: Unknown error cause by `"+IAE.toString()+"` (`"+pUFURI+"`)."
					+ BAOS.toString());
		}

		return UF;
	}
	
	// Unit -------------------------------------------------------------------
	Vector<String> UNames        = null;
	Vector<String> ResourceTypes = null;
	Vector<Unit>   Units         = null;
	
	/** Register a unit in to the system */
	void registerUnit(Unit pUnit) {
		if(pUnit == null) return;
		if(this.getUnit(pUnit.getResourceType(), pUnit.getUnitName()) != null) {
			// Already have this unit
			throw new CurryError("While loading a unit: the unit is already loaded.");
		}
		this.UNames.add(       pUnit.getUnitName());
		this.ResourceTypes.add(pUnit.getResourceType());
		this.Units.add(pUnit);
	}
	/** Returns the number of units */
	public int getUnitCount() {
		if(this.Units == null) return 0;
		return this.Units.size();
	}
	/** Returns the unit name at the index */
	public String getUnitName(int pInd) {
		Unit U = this.getUnit(pInd);
		return (U == null)?null:U.getUnitName();
	}
	/** Returns the unit at the index */
	public Unit getUnit(int pInd) {
		if(this.Units == null) return null;
		if((pInd < 0) || (pInd >= this.getUnitCount())) return null;
		return this.Units.get(pInd);
	}
	/** Returns the unit with the name */
	public Unit getUnit(String pResType, String pUnitName) {
		if(this.Units == null) return null;
		for(int i = 0; i < this.getUnitCount(); i++) {
			if(this.ResourceTypes.get(i).equals(pResType) &&
			   this.UNames.get(i).equals(pUnitName))
				return this.Units.get(i);
		}
		return null;
	}
	
	// UnitBuilders ---------------------------------------------------------
	private Vector<UnitBuilder> UnitBuilders = null;
	void addUnitBuilder(UnitBuilder UB) {
		if(this.UnitBuilders == null) this.UnitBuilders = new Vector<UnitBuilder>();
		if(!this.UnitBuilders.contains(UB)) {
			this.UnitBuilders.add(UB);
		}
	}
	void removeUnitBuilder(UnitBuilder UB, boolean IsRemoveUnit) {
		if(this.UnitBuilders == null) return;
		if(UB == null) return;
		// Remove the Builder
		this.UnitBuilders.remove(UB);
		
		// Reload the new set of package (created when the UnitBuilder is saved)
		Unit U = this.getUnit(UnitFactories.UFMemory.Kind, UB.getName());
		if(U != null) {
			if(IsRemoveUnit) {
				this.Units.remove(U);
			} else {
				for(int i = U.Packages.length; --i >= 0; ) {
					U.Packages[i]      = UB.Packages[i];
					U.Packages[i].Unit = U;
				}
			}
		}
	}
	
	public int getUnitBuilderCount() {
		if(this.UnitBuilders == null) return 0;
		return this.UnitBuilders.size();
	}
	
	public UnitBuilder getUnitBuilder(int I) {
		if((I < 0) || (I >= this.getUnitBuilderCount())) return null;
		return this.UnitBuilders.get(I);
	}
	
	public UnitBuilder getUnitBuilder(String pName) {
		if(pName == null) return null;
		if(this.UnitBuilders == null) return null;
		for(int i = 0; i < this.UnitBuilders.size(); i++) {
			UnitBuilder UB = this.UnitBuilders.get(i);
			if(UB == null) continue;
			if(pName.equals(UB.getName())) return UB;
		}
		return null;
	}
	
	// Package --------------------------------------------------------------

	static private HashSet<Package> BeingInitialized = new HashSet<Package>();

	/**
	 * Returns the package with the name. The package returned by this method will not be initialized. The method should
	 * be use to obtain package at compile time 
	 **/
	public Package getRawPackage(String pPackageName) {
		return this.getPackage(null, pPackageName, true, true, true);
	}
	/** Returns the package with the name */
	public Package getPackage(String pPackageName) {
		return this.getPackage(null, pPackageName, false, true, true);
	}
	/** Returns the package with the name */
	public Package getPackage(String pPackageName, boolean isBeingCompiledIncluded) {
		return this.getPackage(null, pPackageName, false, true, isBeingCompiledIncluded);
	}
	/** Returns the package with the name */
	Package getPackage(Context pContext, String pPackageName) {
		return this.getPackage(pContext, pPackageName, false, true, true);
	}
	/** Returns the package with the name */
	Package getPackageAtCompileTime(Context pContext, String pPackageName) {
		return this.getPackage(pContext, pPackageName, false, false, true);
	}
	/** Returns the package with the name */
	Package getPackage(Context pContext, String pPackageName, boolean isRaw, boolean isToDiscover, boolean isBeingCompiledIncluded) {
		Package P = this.ensurePackageLoaded(pContext, pPackageName);
		if(P == null) {
			if(isToDiscover && (this.discoverUsepaths() != null)) {
				P = this.ensurePackageLoaded(pContext, pPackageName);
				if(P != null) return P;
			}
			if(isBeingCompiledIncluded) {
				int UBCount = this.getUnitBuilderCount();
				for(int i = 0; i < UBCount; i++) {
					UnitBuilder UB = this.getUnitBuilder(i);
					if(UB == null) continue;
					PackageBuilder PB = UB.getPackageBuilder(pPackageName);
					if(PB == null) continue;
					P = PB.getPackage();
					if(P != null) return P;
				}
			}
			return null;
		}
		
		if(isRaw || P.IsInitialized)     return P;
		if(BeingInitialized.contains(P)) return P;
		try {
			BeingInitialized.add(P);
			if(P.RequiredArtifacts != null) {
				// Initialize P
			
				// Ensure all required Package exist
				for(int i = P.RequiredArtifacts.length; --i >= 0; ) {
					Package.Required R = P.RequiredArtifacts[i];
					if(!this.isPackageExist(R.PName))
						throw new CurryError("Package Resolution Error: The package '"+P.Name+"' requires the package '"+R.PName+"' but it is not found.");
				}
				
				// Ensure all artifact exist
				for(int i = P.RequiredArtifacts.length; --i >= 0; ) {
					Package.Required R = P.RequiredArtifacts[i];
					Package RP = this.ensurePackageLoaded(pContext, R.PName);
					if(R instanceof Package.Required_Type) {
						String TName = ((Package.Required_Type)R).TName;
						if(!RP.isTypeExist(TName))
							throw new CurryError("Package Resolution Error: The package '"+P.Name+"' requires the type '"+R.PName+"=>"+TName+"' but it is not found.");
					} else if(R instanceof Package.Required_Func) {
						ExecSignature FES = ((Package.Required_Func)R).FES;
						if(!RP.isFunctionExist(FES))
							throw new CurryError("Package Resolution Error: The package '"+P.Name+"' requires the function '"+R.PName+"=>"+FES.toString()+"' but it is not found.");
					} else if(R instanceof Package.Required_PVar) {
						String VName = ((Package.Required_PVar)R).VName;
						if(!RP.isTypeExist(VName))
							throw new CurryError("Package Resolution Error: The package '"+P.Name+"' requires the package variable '"+R.PName+"=>"+VName+"' but it is not found.");
					}
				}
			}
			P.initializeElements(pContext, this.getEngine());
			P.IsInitialized = true;
			return P;
		} finally {
			BeingInitialized.remove(P);
		}
	}
	/** Returns the package with the name */
	Package ensurePackageLoaded(Context pContext, String pPackageName) {
		for(int i = 0; i < this.getUnitCount(); i++) {
			Unit U = this.getUnit(i);
			if(U == null) continue;
			Package P = U.getPackage(pContext, pPackageName);
			if(P == null) continue;
			return P;
		}
		return null;
	}
	
	/** Get package builder by name */
	public PackageBuilder getPackageBuilder(String pUName, String pPName) {
		if(pUName != null) {
			UnitBuilder UB = this.getUnitBuilder(pUName);
			if(UB != null) {
				PackageBuilder PB = UB.getPackageBuilder(pPName);
				if(PB != null) return PB;
			}
		}
		
		int Count = this.getUnitBuilderCount();
		for(int i = 0; i < Count; i++) {
			UnitBuilder UB = this.getUnitBuilder(i);
			if(UB == null) continue;
			PackageBuilder PB = UB.getPackageBuilder(pPName);
			if(PB == null) continue;
			return PB;
		}
		return null;
	}
	
	/** Checks if a package with the name exist (also look into a being built unit) */
	public boolean isPackageExist(String pPName) {
		// Look in what already loaded first.
		for(int i = 0; i < this.getUnitCount(); i++) {
			Unit U = this.getUnit(i);
			if(U == null) continue;
			if(U.containPackage(pPName)) return true;
		}
		// Look in the one being built
		if(this.UnitBuilders == null) return false;
		for(int i = 0; i < this.UnitBuilders.size(); i++) {
			UnitBuilder UB = this.UnitBuilders.get(i);
			if((UB == null) || !UB.isActive()) continue;
			if(UB.isPackageExist(pPName)) return true;
		}
		return false;
	}
	
	// Get package as a whole ----------------------------------------------------------------------
	
	/** Returns the number of unit and UnitBuilder as if they are the same thing */
	public int getAllUnitCount() {
		return this.getUnitBuilderCount() + this.getUnitCount();
	}
	
	/** Returns the number of package in unit or unit builder at the given index */
	public int getPackageCountOf(int pUnitIndex) {
		if(pUnitIndex < 0) return 0;
		
		// UnitBuilder
		if(pUnitIndex < this.getUnitBuilderCount()) {
			UnitBuilder UB = this.getUnitBuilder(pUnitIndex);
			if(UB == null) return 0;
			return UB.getPackageBuilderCount();
		}
		
		// Unit
		pUnitIndex -= this.getUnitBuilderCount();
		if(pUnitIndex < this.getUnitCount()) {
			Unit U = this.getUnit(pUnitIndex);
			if(U == null) return 0;
			return U.getPackageCount();
		}
		return 0;
	}
	
	public String getPackagetName(int pUnitIndex, int pPackageIndex) {
		return this.getPackagetName(null, pUnitIndex, pPackageIndex);
	}
	
	String getPackagetName(Context pContext, int pUnitIndex, int pPackageIndex) {
		if((pPackageIndex < 0) || (pPackageIndex >= this.getPackageCountOf(pUnitIndex))) return null;
		
		// UnitBuilder
		if(pUnitIndex < this.getUnitBuilderCount()) {
			UnitBuilder UB = this.getUnitBuilder(pUnitIndex);
			if(UB == null) return null;
			PackageBuilder PB = UB.getPackageBuilder(pPackageIndex);
			return (PB == null)?null:PB.getName();
		}
		
		// Unit
		pUnitIndex -= this.getUnitBuilderCount();
		if(pUnitIndex < this.getUnitCount()) {
			Unit U = this.getUnit(pUnitIndex);
			if(U == null) return null;
			Package P = U.getPackage(pContext, pPackageIndex);
			return (P == null)?null:P.getName();
		}
		
		return null;
	}
	
	// Artifacts ---------------------------------------------------------------
	
	/** Checks if the package variable `pVName` exist. */
	boolean isVarExist(Context pContext, String pPName, String pVName) {
		// Look in the one being built
		if(this.UnitBuilders == null) {
			for(int i = 0; i < this.UnitBuilders.size(); i++) {
				UnitBuilder UB = this.UnitBuilders.get(i);
				if((UB == null) || !UB.isActive()) continue;
				if(UB.isVarExist(pPName, pVName)) return true;
			}
		}
		Package P = this.ensurePackageLoaded(pContext, pPName);
		if(P != null) return P.isVarExist(pVName);
		return false;
	}
	/** Checks if the function `pES` exist. */
	boolean isFunctionExist(Context pContext, String pPName, ExecSignature pES) {
		// Look in the one being built
		if(this.UnitBuilders == null) {
			for(int i = 0; i < this.UnitBuilders.size(); i++) {
				UnitBuilder UB = this.UnitBuilders.get(i);
				if((UB == null) || !UB.isActive()) continue;
				if(UB.isFunctionExist(pPName, pES)) return true;
			}
		}
		Package P = this.ensurePackageLoaded(pContext, pPName);
		if(P != null) return P.isFunctionExist(pES);
		return false;
	}
	/** Checks if the package type `pTName` exist. */
	boolean isTypeExist(Context pContext, String pPName, String pTName) {
		// Look in the one being built
		if(this.UnitBuilders != null) {
			for(int i = 0; i < this.UnitBuilders.size(); i++) {
				UnitBuilder UB = this.UnitBuilders.get(i);
				if((UB == null) || !UB.isActive()) continue;
				if(UB.isTypeExist(pPName, pTName)) return true;
			}
		}
		Package P = this.ensurePackageLoaded(pContext, pPName);
		if(P != null) return P.isTypeExist(pTName);
		return false;
	}
	
	/** Checks if the package variable `pVName` exist. */
	public boolean isVarExist(String pPName, String pVName) {
		return this.isVarExist(null, pPName, pVName);
	}
	/** Checks if the function `pES` exist. */
	public boolean isFunctionExist(String pPName, ExecSignature pES) {
		return this.isFunctionExist(null, pPName, pES);
	}
	/** Checks if the package type `pTName` exist. */
	public boolean isTypeExist(String pPName, String pTName) {
		return this.isTypeExist(null, pPName, pTName);
	}
	
	// Utilities -------------------------------------------------------------------------------------------------------
	
	/** Separator between Resource to Name */
	static public String ResourceToNameSeparator = "://";
	
	/** Returns the URI from the resource type and the unit name */
	static public String getURI(String pResType, String pUName) {
		return pResType + ResourceToNameSeparator + pUName;
	}
	/** Returns the URI from the unit */
	static public String getURI(Unit pUnit) {
		return getURI(pUnit.getResourceType(), pUnit.getUnitName());
	}
	/** Returns the resource type from the URI */
	static public String getResType(String pURI) {
		if(pURI == null) return null;
		int I = pURI.indexOf(ResourceToNameSeparator);
		if(I == -1) return null;
		return pURI.substring(0, I);
	}
	/** Returns the unit name from the URI */
	static public String getUnitName(String pURI) {
		if(pURI == null) return null;
		int I = pURI.indexOf(ResourceToNameSeparator);
		if(I == -1) return null;
		return pURI.substring(I + ResourceToNameSeparator.length());
	}
}
