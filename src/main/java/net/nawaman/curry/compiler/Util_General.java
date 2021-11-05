package net.nawaman.curry.compiler;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;

import net.nawaman.curry.util.MoreData;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.typepackage.PTypePackage;

public class Util_General {

	/** Gets the first character of the text value of the given entry name. */
	static public char GetFirstCharOf(ParseResult $Result, String EntryName, char Default) {
		if(EntryName == null) return Default;
		String Text = $Result.textOf(EntryName);
		if((Text == null) || (Text.length() == 0)) return Default;
		return Text.charAt(0);
	}
	
	/**
	 * Ensure that each of very given entry name must exist only once as a first level entry of the $Result.
	 * 
	 * If it is not An error will reported to $CProduct with the given format using the following function.
	 * 
	 *  <code>
	 * 	String.format($ErrorFomat,
	 * 	              ((PropertyNames != null) && (PropertyNames.get(PropertyName) != null))
	 * 	                  ? PropertyNames.get(PropertyName)
	 * 	                  : PropertyName.substring(1)
	 * 	              ,
	 * 	              (($forObject == null)
	 * 	                  ? "" :
	 * 	                  " of " + $forObject),
	 * 	              (($ErrorID == null)
	 * 	                  ? "Util_General:30"
	 * 	                  : $ErrorID))
	 *  </code>
	 *  
	 *  $ErrorFomat  is the format of the message. If $ErrorFomat is null, "Multiple declarations of property `%s`%s %s". 
	 *  PropertyName is the property name that this method found to be repeated. (The prefix '$' and '#' will be ignored)
	 *  $forObject   is a string represetation of the owner of this properties 
	 *  $ErrorID     is a string represent the error for easy debugging
	 *  PropertyNames
	 **/
	static public boolean EnsureNoRepeat(ParseResult $Result, CompileProduct $CProduct, boolean ToReportErrorNoWarning,
			String $ErrorFomat, String $forObject, String $ErrorID, Map<String, String> PropertyNames,
			String ... EntryNames) {
		
		if(EntryNames        == null) return true;
		if(EntryNames.length ==    0) return true;
		
		boolean HasProblem = false;
		
		for(String PropertyName : EntryNames) {
			if(PropertyName == null) continue;
			
			String[] Strs;
			if(((Strs = $Result.textsOf(PropertyName)) == null) || (Strs.length <= 1)) continue;
		
			if($ErrorFomat == null) $ErrorFomat = "Multiple declarations of property property `%s`%s %s";
			String Message = 
				 String.format($ErrorFomat,
				 	              ((PropertyNames != null) && (PropertyNames.get(PropertyName) != null))
				 	                  ? PropertyNames.get(PropertyName)
				 	                  : PropertyName.substring(1)
				 	              ,
				 	              (($forObject == null)
				 	                  ? "" :
				 	                  " of " + $forObject),
				 	              (($ErrorID == null)
				 	                  ? "<Util_General:48>"
				 	                  : "<" + $ErrorID + ">"));
			
			HasProblem = true;
			
			if(!ToReportErrorNoWarning)
				   $CProduct.reportWarning(Message, null, $Result.locationCROf(PropertyName)[1]);
			else { $CProduct.reportError(  Message, null, $Result.locationCROf(PropertyName)[1]); return false; }
		}
		 
		return HasProblem;
	}

	/** Compile MoreData object */
	static public MoreData CompileMoreData(Object[] $Names, Object[] $Values, int[] $Position,
			ParseResult $Result, PTypePackage $TPackage, CompilationContext $CContext) {
		if(($Names == null) || ($Names.length == 0)) return null;
		
		HashSet<String> NameSet = new HashSet<String>();
		
		MoreData.Entry[] Entries = new MoreData.Entry[$Names.length];
		for(int i = 0; i < $Names.length; i++) {
			String Name = $Names[i].toString();
			if(NameSet.contains(Name)) {
				$CContext.reportWarning("The entry name is already exist. The later one will be ignored.", null,
						(($Position == null) || (i >= $Position.length))?-1:$Position[i]);
				continue;
			}
			NameSet.add(Name);
			Entries[i] = new MoreData.Entry(
			                   Name,
			                   (($Values == null) || (i >= $Values.length)) ? null : (Serializable)$Values[i]
			                );
		}
		
		return new MoreData(Entries);
	}
}
