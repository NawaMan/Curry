package net.nawaman.curry;

import java.io.Serializable;

import net.nawaman.compiler.Code;

/** Executional location. */
final public class Location implements HasLocation, Serializable, Cloneable {
	
	/** Constructs a location using the code or name and the start line number. */
	public Location(String pCodeOrName) {
		this(null, pCodeOrName, -1, -1);
	}
	
	/** Constructs a location using the code or name and the start line number. */
	public Location(String pCodeOrName, int pCol, int pRow) {
		this(null, pCodeOrName, pCol, pRow);
	}
	
	/** Constructs a location using the code or name and the start line number. */
	public Location(String pCodeOrName, int[] pColRow) {
		this(null, pCodeOrName, (pColRow == null)?-1:pColRow[0], (pColRow == null)?-1:pColRow[1]);
	}
	
	/** Constructs a location using the code or name and the start line number. */
	public Location(String pUnitName, String pCodeOrName, int pCol, int pRow) {
		this.UnitName   = pUnitName;
		this.CodeOrName = pCodeOrName;
		this.Coordinate = getCoordinate(pCol, pRow);
	}
	
	/** Constructs a location using the code or name and the start line number. */
	public Location(String pUnitName, String pCodeOrName, int[] pColRow) {
		this(pUnitName, pCodeOrName, (pColRow == null)?-1:pColRow[0], (pColRow == null)?-1:pColRow[1]);
	}

    private String UnitName;
	private String CodeOrName;
    private int    Coordinate;
	
    /** Returns the unit name of the code or the name. */
    final public String getUnitName() {
    	return this.UnitName;
    }
    
    /** Checks if the CodeOrName is a code */
    final boolean isCode() {
    	return (this.CodeOrName != null) && (this.CodeOrName.equals(CodePrefix));
    }
    
    /** Checks if the CodeOrName is a name */
    final boolean isName() {
    	return (this.CodeOrName != null) && !(this.CodeOrName.equals(CodePrefix));
    }
	
    /** Returns the location coordinate */
    int getCoordinate() {
    	return this.Coordinate;
    }
    
	// Returns the code 
	final public String getCodeName() {
		if(this.isCode()) return NameForNoName;
		return this.CodeOrName;	// It is a name
	}

	final public String getSourceCode() {
		if(this.isCode()) return this.CodeOrName;
		return this.getSourceCode(null);
	}
    
    // Returns the code or the name
    final public String getSourceCode(Engine pEngine) {
    	if(this.isCode()) return this.CodeOrName.substring(CodePrefix.length());
    	Code C = this.getCode(pEngine);
    	return (C == null)?CodeForNoCode:C.getSourceString();
    }
  
    // Returns the code 
    final public Code getCode(Engine pEngine) {
    	if(this.isCode())                    return null;
    	if(pEngine == null)                  return null;
    	if(pEngine.getUnitManager() == null) return null;
    	String ResType = MUnit.getResType(this.UnitName);
    	String UName   = MUnit.getUnitName(this.UnitName);
    	Unit U = pEngine.getUnitManager().getUnit(ResType, UName);
    	if(U == null) return null;
    	return U.getCode(this.CodeOrName);
    }

	/** Return the line number */
    final public int getLineNumber() {
    	return Location.getRow(this.Coordinate);
    }
	/** Return the column */
    final public int getColumn() {
    	return Location.getCol(this.Coordinate);
    }
    
    /**{@inheritDoc}*/ @Override
    final public Location getLocation() {
    	return this;
    }
	 
	static final public String NameForNoName  = "<<No Name Code>>";
	static final public String CodeForNoCode  = "<< No Code >>";
	static final public String CodePrefix     = "Code: ";

	public String getCoordinateAsString() {
		return getCoordinateAsString(this.Coordinate);
	}
	
	/**{@inheritDoc}*/ @Override
	public Location clone() {
		return new Location(this.getCodeName(), this.getColumn(), this.getLineNumber());
	}

	/**{@inheritDoc}*/ @Override
	public String toString() {
		if(this.isCode()) return this.getCoordinateAsString() + " of " + this.getSourceCode();
		else              return ((this.getCodeName() == null)?NameForNoName:this.getCodeName()) +
									" at " + this.getCoordinateAsString();
	}

   	/** Returns the string representation of this location. */
	public String toString(int pReplaceCoordinate, boolean IsShortFormed) {
		if(pReplaceCoordinate < 0) pReplaceCoordinate = this.getCoordinate();
		if(this.isCode())
			 return "<<Umnamed Code>>" + " at " + getCoordinateAsString(pReplaceCoordinate) + (IsShortFormed ? "" : (" of " + this.getSourceCode()));
		else return this.getCodeName() + " at " + getCoordinateAsString(pReplaceCoordinate);
	}

   	/** Returns the string representation of a location. */
	static public String ToString(Location pLocation) {
		return ((pLocation == null)?"":pLocation.toString());
	}
	
	// Utilities -------------------------------------------------------------------------------------------------------
	
	static final int ROW_BITS = 17;
	static final int COL_BITS = 13;
	
	static final int ROW_MAX = (1 << ROW_BITS) - 1;	// == 131,071 rows 
	static final int COL_MAX = (1 << COL_BITS) - 1;	// ==  16,383 cols
	
	static final int ROW_MASK   = ROW_MAX;
	static final int COL_MASK   = COL_MAX << ROW_BITS;
	static final int EXTRA_MASK = 1 << (ROW_BITS + COL_BITS);
	
	/** Gets the coordinate value from the row and col */
	static public int getCoordinate(int pCol, int pRow) {
		return getCoordinate(pCol, pRow, false);
	}
	/** Gets the coordinate value from the row, col and extra bit*/
	static public int getCoordinate(int pCol, int pRow, boolean isExtraBitSet) {
		// Boundary control
		if(pRow <  0) return -1; if(pRow >= ROW_MAX) pRow = ROW_MAX;
		if(pCol <  0) return -1; if(pCol >= COL_MAX) pCol = COL_MAX;
		// Calculate the position value
		return (pRow + (pCol << ROW_BITS)) | (isExtraBitSet?EXTRA_MASK:0);
	}
	/** Returns the row from the coordinate */
	static public int getRow(int pCoordinate) {
		if(pCoordinate < 0) return -1;
		return pCoordinate & ROW_MASK;
	}
	/** Returns the row from the coordinate */
	static public int getCol(int pCoordinate) {
		if(pCoordinate < 0) return -1;
		return ((pCoordinate & COL_MASK) >>> ROW_BITS);
	}
	/** Checks if the extra bit is set */
	static public boolean isExtrabitSet(int pCoordinate) {
		if(pCoordinate < 0) return false;
		return ((pCoordinate & EXTRA_MASK) != 0);
	}
	/** Returns the Coordinate and String i.e. `(Col, Row)` */
	static public String getCoordinateAsString(int pCoordinate) {
		if(pCoordinate < 0) return "CR(xxx,xxx)";
		return String.format("CR(%3s,%3s)", getCol(pCoordinate), getRow(pCoordinate));
	}
	/** Returns the Coordinate and String i.e. `(Col, Row)` */
	static public String getCoordinateAsString(Location pLocation) {
		if(pLocation == null) return "CR(xxx,xxx)";
		return getCoordinateAsString(pLocation.Coordinate);
	}
}