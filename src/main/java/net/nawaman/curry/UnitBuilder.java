package net.nawaman.curry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Vector;

import net.nawaman.compiler.CodeFeeder;
import net.nawaman.curry.Package.Required;
import net.nawaman.curry.TLPackage.TRPackage;

/** A utility class for building a unit */
abstract public class UnitBuilder implements StackOwnerBuilderEncloseObject {
	
	static public final String SAVE_PACKAGE_PROTOCOL_NAME = "PKGS_1.0";
	
	protected UnitBuilder(Engine pEngine, String pName, Object pSecretID, CodeFeeder pCodeFeeder) {
		this.UM = pEngine.getUnitManager();
		if(this.UM == null) throw new IllegalArgumentException("The given engine does not support 'Unit' extension.");
		
		if(pName == null) throw new NullPointerException();
		this.Name = pName;
		
		if((pCodeFeeder != null) && !this.Name.equals(pCodeFeeder.getFeederName()))
			throw new IllegalArgumentException("Unit name and code feeder name must be the same.");
		
		// Register itself into the UM
		this.UM.addUnitBuilder(this);
		
		// Save the SecretID
		this.ID = pSecretID;
	}
	
	MUnit UM = null;
	
	String Name;
	public String getName() { return this.Name; }
	
	/** Returns the Engine that unit builder is attaching on */
	final public Engine getEngine() { return this.UM.getEngine(); }
	
	// Active ----------------------------------------------------------------------------
	
	private boolean IsActive = true;
	/** Checks if this unit builder is still active (have not saved yet) */
	final public boolean isActive() {
		return this.IsActive;
	}
	
	// Secret ID -------------------------------------------------------------------------
	
	private Object ID = null;
	
   	/** Checks if the ID is a valid ID */
   	public boolean isValidID(Object pID) { return (this.ID == pID); }
   	
   	// Satisfy StackOwnerBuilderEncloseObject --------------------------------------------

	/**{@inheritDoc}*/ @Override   	
	public String getPackageName() {
		return null;
	}
	
	/**{@inheritDoc}*/ @Override
	public TREnclosed ensureTypeRefValid(Accessibility pPAccess, TypeSpec TS, Location pLocation) {
		// It cannot hold a type directly
		return null;
	}
   	
   	// Inputs ----------------------------------------------------------------------------
	
	CodeFeeder CodeFeeder = null;
	public CodeFeeder getCodeFeeder() { return this.CodeFeeder; }
	
	// Package Builders -----------------------------------------------------------------

	// Set to private to not changeable
	private Vector<PackageBuilder> PBuilders   = null;
	
	final public int getPackageBuilderCount() {
		return (this.PBuilders == null)?0:this.PBuilders.size();
	}
	
	final public PackageBuilder getPackageBuilder(int I) {
		if((I < 0) || (I >= this.getPackageBuilderCount())) return null;
		return this.PBuilders.get(I);
	}
	
	/** Returns the package builder of the name `pPName`. */
	final public PackageBuilder getPackageBuilder(String pPName) {
		if(pPName         == null) return null;
		if(this.PBuilders == null) return null;
		for(int i = 0; i < this.PBuilders.size(); i++) {
			if(this.PBuilders.get(i) == null) continue;
			if(pPName.equals(this.PBuilders.get(i).Name)) return this.PBuilders.get(i);
		}
		return null;
	}
	
	/** Creates and Returns a package builder name `pPName`. */
	final public PackageBuilder newPackageBuilder(String pPName) {
		if(!this.IsActive)              return null;
		if(this.isPackageExist(pPName)) return null;
		PackageBuilder PB = new PackageBuilder(pPName);
		PB.UB = this;
		
		if(this.PBuilders == null) this.PBuilders   = new Vector<PackageBuilder>();
		this.PBuilders.add(PB);
		return PB;
	}
	
	// Packages ----------------------------------------------------------------
	
	final public boolean isPackageExist(String pPName) {
		if(!this.IsActive)         return false;
		if(pPName         == null) return false;
		if(this.PBuilders == null) return false;
		for(int i = 0; i < this.PBuilders.size(); i++) {
			if(this.PBuilders.get(i) == null) continue;
			if(pPName.equals(this.PBuilders.get(i).Name)) return true;
		}
		return false;
	}
	
	// Artifacts ---------------------------------------------------------------
	
	/** Checks if the package variable `pVName` exist. */
	final public boolean isVarExist(String pPName, String pVName) {
		PackageBuilder PB = this.getPackageBuilder(pPName);
		if(PB == null) return false;
		return PB.isVarExist(pVName);
	}
	/** Checks if the function `pES` exist. */
	final public boolean isFunctionExist(String pPName, ExecSignature pES) {
		PackageBuilder PB = this.getPackageBuilder(pPName);
		if(PB == null) return false;
		return PB.isFunctionExist(pES);
	}
	/** Checks if the package type `pTName` exist. */
	final public boolean isTypeExist(String pPName, String pTName) {
		PackageBuilder PB = this.getPackageBuilder(pPName);
		if(PB == null) return false;
		return PB.isTypeExist(pTName);
	}

	// Save Unit -------------------------------------------------------------------------
	
	final public void toInactive() {
		if(!this.IsActive) return;
		// Create Package Instance of all
		Package[] Ps = null;
		if(this.PBuilders != null) {
			Vector<Package> PL = new Vector<Package>();
			for(int i = this.PBuilders.size(); --i >= 0; ) {
				PackageBuilder PB = this.PBuilders.get(i);
				if(PB == null) continue;
				PB.toInactive();
				Package P = new Package(PB.Name, this.UM.getEngine().getSignature());
				PB.ThePackage = P;
				
				// Save the package first
				if((PB.FriendNames       != null) && (PB.FriendNames.size()       != 0)) { P.FriendNames       = new String[       PB.FriendNames.size()];       PB.FriendNames.toArray(      P.FriendNames      ); }
				if((PB.RequiredArtifacts != null) && (PB.RequiredArtifacts.size() != 0)) { P.RequiredArtifacts = new Required[     PB.RequiredArtifacts.size()]; PB.RequiredArtifacts.toArray(P.RequiredArtifacts); }
				if((PB.AttrInfos         != null) && (PB.AttrInfos.size()         != 0)) { P.AttrInfos         = new AttributeInfo[PB.AttrInfos.size()];         PB.AttrInfos.toArray(        P.AttrInfos        ); }
				if((PB.OperInfos         != null) && (PB.OperInfos.size()         != 0)) { P.OperInfos         = new OperationInfo[PB.OperInfos.size()];         PB.OperInfos.toArray(        P.OperInfos        ); }
				if((PB.TypeInfos         != null) && (PB.TypeInfos.size()         != 0)) { P.TypeInfos         = new TypeSpec[     PB.TypeInfos.size()];         PB.TypeInfos.toArray(        P.TypeInfos        ); }

				if((PB.TypeInfos != null) && (P.TypeInfos != null)) {
					for(int p = 0; p < P.TypeInfos.length; p++) {
						TypeSpec TS = P.TypeInfos[p];
						if(TS == null) continue;
						
						String TName = ((TRPackage)TS.getTypeRef()).getTypeName();
						if(TName == null) continue;
						
						TypeBuilder TB = PB.getTypeBuilder(TName);
						if(TB == null) continue;

						try {
							P.TypeInfos[p] = (TS = TB.getSpec());
							TS.resetForCompilation();
						} catch (Exception E) {}
					}
				}
				
				PL.add(P);
			}
			if(PL.size() != 0) {
				Ps = new Package[PL.size()];
				PL.toArray(Ps);
			}
		}
		
		// Keep the packages to be saved later
		this.Packages = Ps;
		// Load the packages in to the engine (use UFMemory)
		this.UM.registerUnitFactory(new UnitFactories.UFMemory(this.UM.getEngine(), this.Name, this.Packages, this.CodeFeeder));
		
		this.IsActive = false;
	}
	
	// Save related
	boolean   IsSaved  = false;
	Package[] Packages = null;
	
	/** Save this being built unit */
	abstract protected Exception saveThis(Package[] Ps);
	
	/** Save this being built unit */
	final public Exception cancel() {
		// De-activate all package builder and remove them off
		if(this.PBuilders != null) {
			for(int i = this.PBuilders.size(); --i >= 0; ) {
				PackageBuilder PB = this.PBuilders.get(i);
				if(PB == null) continue;
				PB.FriendNames       = null;
				PB.RequiredArtifacts = null;
				PB.AttrInfos         = null;
				PB.OperInfos         = null;
				PB.TypeInfos         = null;
			}
		}
		this.PBuilders = null;

		// De-activate all package and remove them off
		if(this.Packages != null) {
			for(int i = this.Packages.length; --i >= 0; ) {
				Package P = this.Packages[i];
				if(P == null) continue;
				P.FriendNames       = null;
				P.RequiredArtifacts = null;
				P.AttrInfos         = null;
				P.OperInfos         = null;
				P.TypeInfos         = null;
			}
		}
		this.PBuilders = null;
		
		this.UM.removeUnitBuilder(this, true);
		this.IsActive = false;
		this.IsSaved  = true;
		return null;
	}
	
	/** Save this being built unit */
	final public Exception save() {
		if(this.IsSaved) return new CurryError("The package is already saved.");
		this.toInactive();
		
		// Call save.
		Exception Exc = this.saveThis(this.Packages);
		
		// Return error
		if(Exc != null) return Exc;

		// Re-Create Package Instance of all
		Package[] Ps = null;
		if(this.PBuilders != null) {
			Vector<Package> PL = new Vector<Package>();
			for(int i = this.PBuilders.size(); --i >= 0; ) {
				PackageBuilder PB = this.PBuilders.get(i);
				if(PB == null) continue;
				PB.toInactive();
				Package P = new Package(PB.Name, this.UM.getEngine().getSignature());
				PB.ThePackage = P;
				
				// Save the package first
				if((PB.FriendNames       != null) && (PB.FriendNames.size()       != 0)) { P.FriendNames       = new String       [PB.FriendNames.size()];       PB.FriendNames.toArray(      P.FriendNames      ); }
				if((PB.RequiredArtifacts != null) && (PB.RequiredArtifacts.size() != 0)) { P.RequiredArtifacts = new Required     [PB.RequiredArtifacts.size()]; PB.RequiredArtifacts.toArray(P.RequiredArtifacts); }
				if((PB.AttrInfos         != null) && (PB.AttrInfos.size()         != 0)) { P.AttrInfos         = new AttributeInfo[PB.AttrInfos.size()];         PB.AttrInfos.toArray(        P.AttrInfos        ); }
				if((PB.OperInfos         != null) && (PB.OperInfos.size()         != 0)) { P.OperInfos         = new OperationInfo[PB.OperInfos.size()];         PB.OperInfos.toArray(        P.OperInfos        ); }

				if((PB.TypeInfos != null) && (PB.TypeInfos.size() != 0)) {
					P.TypeInfos = new TypeSpec[PB.TypeInfos.size()];
					PB.TypeInfos.toArray(P.TypeInfos);
					
					for(int t = 0; t < P.TypeInfos.length; t++) {
						TypeSpec TS = P.TypeInfos[t];
						if(TS == null) continue;
						
						// Reset the spec
						TS.resetForCompilation();
					}
				}
				
				PL.add(P);
			}
			if(PL.size() != 0) {
				Ps = new Package[PL.size()];
				PL.toArray(Ps);
			}
		}
		
		// Keep the packages to be saved later
		this.Packages = Ps;
		
		// De-activate all package builder and remove them off
		if(this.PBuilders != null) {
			for(int i = this.PBuilders.size(); --i >= 0; ) {
				PackageBuilder PB = this.PBuilders.get(i);
				if(PB == null) continue;
				PB.FriendNames       = null;
				PB.RequiredArtifacts = null;
				PB.AttrInfos         = null;
				PB.OperInfos         = null;
				PB.TypeInfos         = null;
			}
		}
		this.PBuilders = null;
		
		// Remove this builder out of the unit manager
		this.UM.removeUnitBuilder(this, false);
		this.UM = null;
		
		this.IsSaved = true;
		return null;
	}
	
	// Serialization ---------------------------------------------------------------------------------------------------
	
	/**
	 * Save Package to an array of byte.
	 * 
	 * You MUST use this method to save the package as it maintain the signature and dependency.
	 **/
	final protected byte[] savePackages(net.nawaman.curry.Package ... Pkgs) throws IOException {
		// Save the package in the buffer first.
		// During this process, Expression and TypeRef saved with the packages will report the extension, access to
		//    other package that they needed so we can collect them and save that together
		ByteArrayOutputStream Buffer = new ByteArrayOutputStream();
		PackageOutputStream   POS    = PackageOutputStream.newPOS(this, Buffer);
		POS.writeObject(Pkgs);
		POS.flush();
		POS.close();
		
		// The actual save
		ByteArrayOutputStream SavedBuffer = new ByteArrayOutputStream();
		ObjectOutputStream    Saved       = new ObjectOutputStream(SavedBuffer);
		// Write the Protocol name (for later evolution)
		Saved.writeUTF(SAVE_PACKAGE_PROTOCOL_NAME);
		// Write the list of Package Names, the list of required Extension and the list of required Packages 
		Saved.writeObject(POS.getPackagesDependencyInfo());
		// Write the Packages
		Saved.writeObject(Buffer.toByteArray());

		// Save to an array of byte
		byte[] Return = SavedBuffer.toByteArray();
		
		// Flush and close
		Saved.flush();
		Saved.close();
		Buffer.flush();
		Buffer.close();
		SavedBuffer.flush();
		SavedBuffer.close();
		
		// Returns
		return Return;
	}
	
	// Helper class ----------------------------------------------------------------------------------------------------
	
	/** This is class is required to be used to save package as it ensure that all expression hash in the original form */
	static public class PackageOutputStream extends CurryOutputStream {
		
		UnitBuilder UBuilder = null;		 
		
		/** Creates a new CurryOutputStream */
		static public PackageOutputStream newPOS(UnitBuilder pUBuilder, OutputStream pOS) throws IOException {
			return new PackageOutputStream(pUBuilder, new ByteArrayOutputStream(), pOS);
		}
		
		/** Constructs an CurryOutputStream */
		protected PackageOutputStream(UnitBuilder pUBuilder, ByteArrayOutputStream pBAOS, OutputStream pOS) throws IOException {
			super(pUBuilder.getEngine(), pBAOS, pOS);
			this.UBuilder = pUBuilder;
		}
		
		/** Returns the UnitBuilder */
		public UnitBuilder getUnitBuilder() {
			return this.UBuilder;
		}
	}
	
	/** This is class is required to be used to save package as it ensure that all expression hash in the original form */
	static public class PackageInputStream extends CurryInputStream {

		// Extract two ByteArrayInputStream out of one.
		static public PackageInputStream newPIS(Engine pEngine, InputStream pIS)
						throws IOException, ClassNotFoundException {
			return new PackageInputStream(pEngine, getConstructorData(pIS));
		}

		/** Constructs an ObjectWriter */
		protected PackageInputStream(Engine pEngine, ConstructorData pJCOISCD)
		                       throws IOException, ClassNotFoundException {
			super(pEngine, pJCOISCD);
		}
	}


	// Lock ------------------------------------------------------------------------------------------------------------
	
	/** This method will help limiting the implementation of this interface to be within this package. */
	public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
		return pLocalInterface;
	}
}