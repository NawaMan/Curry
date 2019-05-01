package net.nawaman.curry;

import net.nawaman.util.UString;

public class TLPackage extends TypeLoader {
	
	/** KindName of this Type Loader */
	static final public String KindName = "Package";
	
	/** Constructs a PackageTypeLoader. */
	TLPackage(Engine pEngine) { super(pEngine); }
	
	// Classification --------------------------------------------------------------------
	
	/** Returns the kind name of this type. */
	@Override public String getKindName() { return KindName; }
	
	// Services -------------------------------------------------------------------------

	/**
	 * Load a type spec from the given type reference.<br />
	 * @return Type spec if success or Throwable if fail.
	 ***/ @Override
	 protected Object loadTypeSpec(Context pContext, TypeRef pTRef) {
		if(!(pTRef instanceof TRPackage)) {
			// Returns the error
			return new CurryError(
				"Type Loading Error: Wrong TypeRef Class. nawa.curry.TLPackage.TRPackage " +
				"expected but "+pTRef.getClass().getCanonicalName()+" found.(TRPackage.java#31)",
				pContext
			);
		}
		
		TRPackage TR = ((TRPackage)pTRef);
		
		// Pre-defined types
		if(MType.PREDEFINEDTYPE_PACKAGENAME.equals(TR.PName))
			return this.getEngine().getTypeManager().getPrefineTypeSpec(TR.TName);
		
		TypeSpec  TS    = null;
		MUnit     MUnit = this.getEngine().getUnitManager();
		Package   P     = MUnit.getPackageAtCompileTime(pContext, TR.PName);
		if(P != null) TS = P.getTypeSpec(TR.TName);
		else {
			PackageBuilder PB = MUnit.getPackageBuilder(null, TR.PName);
			if(PB != null) {
				TypeBuilder TB = PB.getTypeBuilder(TR.TName);
				if(TB != null) TS = TB.getTypeSpec();
			} else {
				// Returns the error
				return new CurryError(
					"Type Loading Error: The package named '"+TR.getPackageName()+"' is not found ("+TR.toDetail()+").",
					pContext
				);
			}
		}
		
		if(TS == null) {
			// Returns the error
			return new CurryError(
				"Type Loading Error: The type named '"+TR.getTypeName()+"' is not found in the package '"+TR.getPackageName()+"' ("+TR.toDetail()+").",
				pContext
			);
		}
		return TS;
	}
	
	/** TypeRef that refer to a type in a package. */
	static public class TRPackage extends TREnclosed {
		
		/** Constructs a alias type reference. */
		public TRPackage(String pPName, String pTName) {
			if(pPName == null) throw new NullPointerException("An package type ref cannot be constucted without the package name.");
			if(pTName == null) throw new NullPointerException("An package type ref cannot be constucted without the type name.");
			this.PName    = pPName;
			this.TName    = pTName;
		}
		
		/**{@inheritDoc}*/ @Override
		final public String getRefKindName() {
			return TLPackage.KindName;
		}
		
		/** Name of the package of the type that this type reference is referring. */
		       String PName = null;
		/** Returns the package name of the type that this type reference is referring. */
		final public String getPackageName() {
			return this.PName;
		}
		
		/** Name of the type that this type reference is referring. */
		       String TName = null;
		/**{@inheritDoc}*/ @Override
		final public String getTypeName() {
			return this.TName;
		}
		
		/**{@inheritDoc}*/ @Override 
		public TypeRef clone() {
			return new TRPackage(this.PName, this.TName);
		}
		
		/**{@inheritDoc}*/ @Override
		Object getEncloseObject(Context pContext) {
			Engine E = null;
			if(pContext == null) {
				Type T = this.getTheType();
				if(T != null) E = T.getEngine();
				if(E == null) return null;
			} else E = pContext.getEngine();
			
			MUnit UM = E.getUnitManager();
			if(UM == null) return null;
			
			return UM.getPackage(this.getPackageName());
		}
		
		/**{@inheritDoc}*/ @Override
		public Accessibility getAccessibility() {
			TypeRef TRef;
			if(this.isLoaded() && ((TRef = this.getTheType().getTypeRef()) != this) && (TRef instanceof TREnclosed))
				return ((TREnclosed)TRef).getAccessibility();
			return null;
		}
		
		/**{@inheritDoc}*/ @Override
		public Location getLocation() {
			TypeRef TRef;
			if(this.isLoaded() && ((TRef = this.getTheType().getTypeRef()) != this) && (TRef instanceof TREnclosed))
				return ((TREnclosed)TRef).getLocation();
			return null;
		}
		
		/**{@inheritDoc}*/ @Override
		public TREnclosed newInternalTypeRef(Engine pEngine, Accessibility pAccessibility, Location pLocation) {
			return new TRPackage_Internal(
					this.getTypeSpec(pEngine),
					this.getPackageName(),
					this.getTypeName(),
					pAccessibility,
					pLocation);
		}
		
		// Objectable -----------------------------------------------------------------------
		
		String getToDetailPrefix() {
			return "";
		}
		/**{@inheritDoc}*/ @Override
		final public String toString() {
			return this.PName + "=>" + this.TName;
		}
		/**{@inheritDoc}*/ @Override
		final public String toDetail() {
			return this.getToDetailPrefix() + this.toString();
		}
		/**{@inheritDoc}*/ @Override
		final public boolean equals(Object O) {
			if(this == O)                 return  true;
			if(!(O instanceof TRPackage)) return false;
			// Both are no-name, compare type spec.
			return this.PName.equals(((TRPackage)O).PName) && this.TName.equals(((TRPackage)O).TName);
		}
		/**{@inheritDoc}*/ @Override
		final public int hash() {
			return UString.hash("PackageTypeRef") + UString.hash(this.toString());
		}
	}
	
	/** TypeRef that refer to a type in a package. */
	static final class TRPackage_Internal extends TRPackage {

		/** Constructs a alias type reference. */
		TRPackage_Internal(TypeSpec pTheSpec, String pPName, String pTName, Accessibility pPA, Location pLocation) {
			super(pPName, pTName); //, pLocation);
			this.TheSpec  = pTheSpec;
			this.PAccess  = pPA;
			this.Location = pLocation;
		}
		
		Accessibility PAccess = Package.Package;
		
		/**{@inheritDoc}*/ @Override
		public Accessibility getAccessibility() {
			return this.PAccess;
		}
		
		/**{@inheritDoc}*/ @Override
		String getToDetailPrefix() {
			return ((this.PAccess == null)?"":this.PAccess.toString()).toLowerCase() + " ";
		}
		/**{@inheritDoc}*/ @Override 
		public TypeRef clone() {
			return new TRPackage_Internal(this.TheSpec, this.PName, this.TName, this.PAccess, this.Location);
		}
		
		// TypeSpec --------------------------------------------------------------------------------------------------------
		
		TypeSpec TheSpec = null;
		
		/**{@inheritDoc}*/ @Override
		protected TypeSpec getTypeSpecWithoutEngine() {
			return this.TheSpec;
		}
		
		/**{@inheritDoc}*/ @Override
		void setTheType(Type pTheType) {
			super.setTheType(pTheType);
			this.TheSpec = (pTheType == null) ? null : pTheType.getTypeSpec();
		}

		/**{@inheritDoc}*/ @Override
		public void resetTypeRefForCompilation() {
			super.resetTypeRefForCompilation();
			if(this.TheSpec != null)
				this.TheSpec = null;
		}
		
		// Location --------------------------------------------------------------------------
		
		Location Location;
		
		/**
		 * Returns the locations of this type.<br />
		 * Implements this method and the method `hasLocation()` to provide the location.
		 **/ @Override
		public Location getLocation() {
			 return this.Location;
		}
		/**
		 * Checks if this type ref has the location of the type
		 * Implements this method and the method `getLocation()` to provide the location.
		 **/
		protected boolean hasLocation() {
			return (this.Location != null);
		}
	}
}