package net.nawaman.curry;

abstract public class LocationSnapshot {

	static public final LocationSnapshot[] EmptyLocationSnapshots = new LocationSnapshot[0];
	
	/** Creates a simple LocationSnapShort */
	static public LocationSnapshot create(String pOwnerName, HasLocation pHasLocation,
			Expression pExpression) {
		return new LocationSnapshotSimple(pOwnerName, pHasLocation, pExpression);
	}
	/** Creates a LocationSnapShort with Coordinate */
	static public LocationSnapshot create(String pOwnerName, HasLocation pHasLocation,
			Expression pExpression, int pCoordinate) {
		if(pCoordinate == -1)
			 return new LocationSnapshotSimple        (pOwnerName, pHasLocation, pExpression);
		else return new LocationSnapshotWithCoordinate(pOwnerName, pHasLocation, pExpression, pCoordinate);
	}
	/** Creates a LocationSnapShort with Documentation */
	static public LocationSnapshot create(String pOwnerName, HasLocation pHasLocation,
			Expression pExpression, Documentation pDocumentation) {
		if(pDocumentation == null)
			 return new LocationSnapshotSimple           (pOwnerName, pHasLocation, pExpression);
		else return new LocationSnapshotWithDocumentation(pOwnerName, pHasLocation, pExpression, pDocumentation);
	}
	/** Creates a LocationSnapShort with Coordinate and Documentation */
	static public LocationSnapshot create(String pOwnerName, HasLocation pHasLocation,
			Expression pExpression, int pCoordinate, Documentation pDocumentation) {
		if     (pCoordinate == -1)
			 return create                                         (pOwnerName, pHasLocation, pExpression, pDocumentation);
		else if(pDocumentation == null)
			 return create                                         (pOwnerName, pHasLocation, pExpression, pCoordinate);
		else return new LocationSnapshotWithCoordinateDocumentation(pOwnerName, pHasLocation, pExpression, pCoordinate, pDocumentation);
	}
	
	// Constructor -----------------------------------------------------------------------------------------------------
	
	LocationSnapshot(String pOwnerName, HasLocation pHasLocation, Expression pExpression) {
		this.OwnerName   = pOwnerName;
		this.HasLocation = pHasLocation;
		this.Expression  = pExpression;
	}
	
	/** The owner name of the location */
	final public String      OwnerName;
	/** The object with a location */
	final public HasLocation HasLocation;
	/** The expression of the location snapshort */
	final public Expression  Expression;

	/** Returns the executable signature of this location. */
	public ExecSignature getSignature() {
		if(this.HasLocation instanceof HasSignature) return ((HasSignature)this.HasLocation).getSignature();
		return null;
	}
	/** Returns the start location of this snapshot. */
	public Location getLocation() {
		if(this.HasLocation == null) return null;
		return this.HasLocation.getLocation();
	}
	/** Returns the line number offset of this snapshort. */
	public int getCoordinate() {
		if(this.Expression == null) return -1;
		return this.Expression.getCoordinate();
	}
	/** Returns the documentation of this snapshort. */
	public Documentation getDocumentation() {
		if(!(this.HasLocation instanceof HasSignature)) return null;
		return Documentation.Util.getDocumentationOf(((HasSignature)this.HasLocation).getSignature());
	}
	
   	/**{@inheritDoc}*/ @Override
   	public String toString() {
   		StringBuilder SB = new StringBuilder();
   		Location      L  = this.getLocation();
   		ExecSignature ES = this.getSignature();
   		if(L == null)
   			 SB.append("<<Unknown code>> at ").append(Location.getCoordinateAsString(this.getCoordinate()));
   		else SB.append(L.toString(this.getCoordinate(), true));
   		
   		SB.append(" => ");
   		if((this.OwnerName != null) && !EngineExtensions.EE_DefaultPackage.DefaultPackageName.equals(this.OwnerName)) {
   			   SB.append(this.OwnerName);
   			   if(ES != null) SB.append(".").append(ES);
   		} else if(ES != null) SB.append(ES);
   		
		return SB.toString();
	}
	
	// Lock --------------------------------------------------------------------
	
	/** This method will help limiting the implementation of this interface to be within this package. */
	abstract public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface);
   	
   	// Subclasses ------------------------------------------------------------------------------------------------------
   	
	/** Location Snapshot with Coordinate */
	static class LocationSnapshotSimple extends LocationSnapshot {
		LocationSnapshotSimple(String pOwnerName, HasLocation pHasLocation, Expression pExpression) {
			super(pOwnerName, pHasLocation, pExpression);
		}
		// Lock --------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
	
	/** Location Snapshot with Coordinate */
	static class LocationSnapshotWithCoordinate extends LocationSnapshot {
		LocationSnapshotWithCoordinate(String pOwnerName, HasLocation pHasLocation, Expression pExpression, int pCoordinate) {
			super(pOwnerName, pHasLocation, pExpression);
			this.Coordinate  = ((pCoordinate == -1) && (pExpression != null)) ? pExpression.getCoordinate() : pCoordinate;
		}
		final int Coordinate;
		/**{@inheritDoc}*/ @Override 
		public int getCoordinate() {
			return this.Coordinate;
		}
		// Lock --------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
	
	/** Location Snapshot with Coordinate and Documentation */
	static class LocationSnapshotWithCoordinateDocumentation extends LocationSnapshot {
		LocationSnapshotWithCoordinateDocumentation(String pOwnerName, HasLocation pHasLocation, Expression pExpression,
				int pCoordinate, Documentation pDocumentation) {
			super(pOwnerName, pHasLocation, pExpression);
			this.Coordinate    = ((pCoordinate == -1) && (pExpression != null)) ? pExpression.getCoordinate() : pCoordinate;
			this.Documentation = pDocumentation;
		}
		final int           Coordinate;
		final Documentation Documentation;
		/**{@inheritDoc}*/ @Override 
		public int getCoordinate() {
			return this.Coordinate;
		}
		/**{@inheritDoc}*/ @Override 
		public Documentation getDocumentation() {
			if(this.Documentation == null) return super.getDocumentation();
			return this.Documentation;
		}
		// Lock --------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
	
	/** Location Snapshot with Coordinate and Documentation */
	static class LocationSnapshotWithDocumentation extends LocationSnapshot {
		LocationSnapshotWithDocumentation(String pOwnerName, HasLocation pHasLocation, Expression pExpression,
				Documentation pDocumentation) {
			super(pOwnerName, pHasLocation, pExpression);
			this.Documentation = pDocumentation;
		}
		final Documentation Documentation;
		/**{@inheritDoc}*/ @Override 
		public Documentation getDocumentation() {
			if(this.Documentation == null) return super.getDocumentation();
			return this.Documentation;
		}
		// Lock --------------------------------------------------------------------
		/**{@inheritDoc}*/ @Override
		public Engine.LocalLock getLocalInterface(Engine.LocalLock pLocalInterface) {
			return pLocalInterface;
		}
	}
}