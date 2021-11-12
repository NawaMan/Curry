package net.nawaman.curry.compiler;

import net.nawaman.curry.Location;
import net.nawaman.regparser.result.Coordinate;
import net.nawaman.regparser.result.ParseResult;

public class Util_Curry {

	/** Gets the first character of the text value of the given entry name. */
	static public Location GetLocationOf(ParseResult $Result, CompileProduct $CProduct, String EntryName) {
		Coordinate LRC = (EntryName != null) ? $Result.coordinateOf(EntryName) : $Result.coordinateOf(0);
		return new Location($CProduct.getCurrentFeederName(), $CProduct.getCurrentCodeName(), LRC);
	}

}
