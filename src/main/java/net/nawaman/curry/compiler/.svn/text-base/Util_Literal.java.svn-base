package net.nawaman.curry.compiler;

import java.math.BigDecimal;
import java.math.BigInteger;

import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;
import net.nawaman.util.UNumber;

public class Util_Literal {
	
	/** No inherit or instance */
	private Util_Literal() {}
	
	/** Compile a char escape */
	static public Character CompileCharEscape(String $Text,
			ParseResult $Result, PTypeProvider $TProvider, CompilationContext $CContext) {
		char F = $Text.toLowerCase().charAt(1);
		switch(F) {
			case  '0': return ($Text.length() == 2) ? '\0' : (char)Integer.parseInt($Text.substring(2),  8);
			case  'x': return (char)Integer.parseInt($Text.substring(2), 16);
			case  'u': return (char)Integer.parseInt($Text.substring(2), 16);
			case '\\': return '\\';
			case '\"': return '\"';
			case '\'': return '\'';
			case  't': return '\t';
			case  'n': return '\n';
			case  'r': return '\r';
			case  'b': return '\b';
			case  'f': return '\f';
			default: {
				String Location = ($Result == null)?"":"near " + $Result.locationOf(0);
				$CContext.reportError("Invalid Escape Character (" + $Text + ") <Util_Literal:28> "+Location, null);
				return null;
			}
		}
	}

	/**
	 * Compile a integer number
	 * 
	 * NOTE: DecMantissa is used first (if it is not null, other Mantissa will be ignored). Then, BinMantissa,
	 *          OctMantissa and finally HexMantissa.
	 * NOTE: $Size chooses the numeric return type [bsiLfdID] for byte, short, int, long, float, double, big integer
	 *          and big decimal. 
	 **/
	static public Number CompileNumberInteger(String $Sign, String $Power, String $DecMantissa, String $BinMantissa,
			String $OctMantissa, String $HexMantissa, String $Size,
			ParseResult $Result, PTypeProvider $TProvider, CompilationContext $CContext) {
		
		char    SS    = (($Size == null) || ($Size.length() == 0))?'0':$Size.charAt(0);
		boolean isNeg = (($Sign != null) && $Sign.equals("-"));
		
		BigInteger I = BigInteger.ZERO;
		
		if(     $DecMantissa != null) I = new BigInteger($DecMantissa, 10);
		else if($BinMantissa != null) I = new BigInteger($BinMantissa,  2);
		else if($OctMantissa != null) I = new BigInteger($OctMantissa,  8);
		else if($HexMantissa != null) I = new BigInteger($HexMantissa, 16);
		
		if($Power != null) I = I.multiply(BigInteger.TEN.pow(Integer.parseInt($Power)));
		if(isNeg)          I = I.negate();
		
		// Default size
		if($Size == null) {
			if(UNumber.canBeInInteger(I)) return I.intValue();
			if(UNumber.canBeInLong(   I)) return I.longValue();
			return I;
		}
		
		switch(SS) {  
			case 'b': return I.byteValue();
			case 's': return I.shortValue();
			case 'i': return I.intValue();
			case 'L': return I.longValue();
			case 'f': return I.floatValue();
			case 'd': return I.doubleValue();
			case 'I': return I;
			case 'D': return UNumber.getBigDecimal(I);
			default: {
				$CContext.reportError("Invalid Numeric size for Integer number (" + SS + ") <Util_Literal:78>", null);
				return null;
			}
		}
	}

	/**
	 * Compile a integer number
	 * 
	 * NOTE: $Size chooses the numeric return type [fdD] for float, double and big decimal. 
	 **/
	static public Number CompileNumberDecimal(String $Text, String $Size,
			ParseResult $Result, PTypeProvider $TProvider, CompilationContext $CContext) {
		
		BigDecimal D  = new BigDecimal($Text);
		// Default size
		if($Size == null) {
			if(UNumber.canBeInDouble(D)) return D.doubleValue();
			return D;
		}
		char SS = ($Size.length() >= 1)?$Size.charAt(0):'d';
		
		switch(SS) {
			case 'f': return D.floatValue();
			case 'd': return D.doubleValue();
			case 'D': return D;
			default: {
				$CContext.reportError("Invalid Numeric size for Decimal number (" + SS + ") <Util_Literal:111>", null);
				return null;
			}
		}
	}

	/**
	 * Compile a string.
	 * 
	 * ParseResult elements:
	 * 	$Chars[]	array of normal characters
	 * 	#Escape		an escape character
	 **/
	static public String ParseCompileString(boolean $IsTrimed,
			ParseResult $Result, PTypeProvider $TProvider, CompilationContext $CContext) {
		// Short string
		StringBuffer SB = new StringBuffer();
		int Count = $Result.count();
		for(int i = 0; i < Count; i++) {
			String Name = $Result.nameOf(i);
			if("$Chars[]".equals(Name)) {
				SB.append($Result.textOf(i));

			} else if("#EscapeChr".equals(Name)) {
				SB.append(CompileCharEscape($Result.textOf(i), $Result, $TProvider, $CContext));

			} else if("#EscapeStr".equals(Name)) {
				SB.append($Result.valueOf(i, $TProvider, $CContext));

			}
		}

		return $IsTrimed?SB.toString().trim():SB.toString();
	}

}
